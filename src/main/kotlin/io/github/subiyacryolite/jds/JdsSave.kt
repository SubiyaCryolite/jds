/**
 * Jenesis Data Store Copyright (c) 2017 Ifunga Ndana. All rights reserved.
 *
 * 1. Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 2. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 3. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * Neither the name Jenesis Data Store nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package io.github.subiyacryolite.jds

import com.javaworld.NamedCallableStatement
import com.javaworld.NamedPreparedStatement
import io.github.subiyacryolite.jds.JdsExtensions.setLocalTime
import io.github.subiyacryolite.jds.JdsExtensions.setZonedDateTime
import io.github.subiyacryolite.jds.events.JdsSaveEvent
import io.github.subiyacryolite.jds.events.JdsSaveListener
import io.github.subiyacryolite.jds.events.OnPostSaveEventArguments
import io.github.subiyacryolite.jds.events.OnPreSaveEventArguments
import java.sql.Connection
import java.sql.SQLException
import java.sql.Timestamp
import java.time.*
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.coroutines.experimental.buildSequence

/**
 * This class is responsible for persisting on or more [JdsEntities][JdsEntity]
 */
class JdsSave private constructor(private val alternateConnections: ConcurrentMap<Int, Connection> = ConcurrentHashMap(), private val jdsDb: JdsDb, private val connection: Connection, private val batchSize: Int, private val entities: Iterable<JdsEntity>, private val onPreSaveEventArguments: OnPreSaveEventArguments = OnPreSaveEventArguments(jdsDb, connection, alternateConnections), private val onPostSaveEventArguments: OnPostSaveEventArguments = OnPostSaveEventArguments(jdsDb, connection, alternateConnections), var closeConnection: Boolean = true) : Callable<Boolean> {

    private val jdsSaveEvents = LinkedList<JdsSaveEvent>()

    /**
     * @param jdsDb
     * @param entities
     */
    @Throws(SQLException::class, ClassNotFoundException::class)
    constructor(jdsDb: JdsDb, entities: Iterable<JdsEntity>) : this(jdsDb, entities, 0)

    /**
     * @param jdsDb
     * @param batchSize
     * @param entities
     */
    @Throws(SQLException::class, ClassNotFoundException::class)
    constructor(jdsDb: JdsDb, entities: Iterable<JdsEntity>, batchSize: Int) : this(jdsDb, entities, jdsDb.getConnection(), batchSize)

    /**
     * @param jdsDb
     * @param connection
     * @param entities
     */
    @Throws(SQLException::class, ClassNotFoundException::class)
    constructor(jdsDb: JdsDb, entities: Iterable<JdsEntity>, connection: Connection) : this(jdsDb, entities, connection, 0)

    /**
     * @param jdsDb
     * @param batchSize
     * @param connection
     * @param entities
     */
    @Throws(SQLException::class, ClassNotFoundException::class)
    constructor(jdsDb: JdsDb, entities: Iterable<JdsEntity>, connection: Connection, batchSize: Int) : this(ConcurrentHashMap(), jdsDb, connection, batchSize, entities)

    /**
     * Computes a result, or throws an exception if unable to do so.
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Throws(Exception::class)
    override fun call(): Boolean {
        val allEntities = buildSequence { entities.forEach { yieldAll(it.getAllEntities()) } }
        saveOverviews(allEntities)
        try {
            val actualBatchSize = if (batchSize == 0) entities.count() else batchSize
            allEntities.chunked(actualBatchSize).forEachIndexed { step, it ->
                saveInner(it)
                if (jdsDb.options.isPrintingOutput)
                    println("Processing saves. Step $step")
            }
        } catch (ex: Exception) {
            throw ex
        } finally {
            jdsSaveEvents.forEach { it.onSave(allEntities.asIterable(), connection) }
            onPreSaveEventArguments.closeBatches()
            onPostSaveEventArguments.closeBatches()
            alternateConnections.forEach { it.value.close() }
            if (closeConnection)
                connection.close()
        }
        return true
    }

    /**
     * @param batchEntities
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun saveInner(batchEntities: Iterable<JdsEntity>) {
        try {
            //ensure that overviews are submitted before handing over to listeners
            batchEntities.filterIsInstance<JdsSaveListener>().forEach { it.onPreSave(onPreSaveEventArguments) }

            batchEntities.forEach {
                it.bindChildren()
                if (jdsDb.options.isWritingToPrimaryDataTables) {
                    //time constraints
                    saveDateConstructs(it)
                    saveDatesAndDateTimes(it)
                    saveZonedDateTimes(it)
                    saveTimes(it)
                    //primitives, can be null
                    saveBooleans(it)
                    saveLongs(it)
                    saveDoubles(it)
                    saveIntegers(it)
                    saveFloats(it)
                    //strings never null
                    saveStrings(it)
                    //blobs
                    saveBlobs(it)
                    //enumProperties
                    saveEnums(it)
                }
                if (jdsDb.options.isWritingArrayValues) {
                    //array properties [NOTE arrays have old entries deleted first, for cases where a user reduced the amount of entries in the collection]
                    saveArrayDates(it)
                    saveArrayStrings(it)
                    saveArrayLongs(it)
                    saveArrayDoubles(it)
                    saveArrayIntegers(it)
                    saveArrayFloats(it)
                    saveEnumCollections(it)
                }
                //objects and object arrays
                //object entity overviews and entity bindings are ALWAYS persisted
                if (jdsDb.options.isWritingToPrimaryDataTables || jdsDb.options.isWritingOverviewFields || jdsDb.options.isWritingArrayValues) {
                    saveAndBindObjects(it)
                    saveAndBindObjectArrays(it)
                }
            }

            batchEntities.filterIsInstance<JdsSaveListener>().forEach { it.onPostSave(onPostSaveEventArguments) }

            //crt point
            if (jdsDb.tables.isNotEmpty()) {
                batchEntities.forEach {
                    processCrt(jdsDb, connection, alternateConnections, it)
                }
            }

            //respect execution sequence
            //respect JDS batches in each call
            onPreSaveEventArguments.executeBatches()
            onPostSaveEventArguments.executeBatches()
        } catch (ex: Exception) {
            throw ex
        }
    }

    /**
     * @param jdsDb
     * @param connection
     * @param alternateConnections
     * @param entity
     */
    private fun processCrt(jdsDb: JdsDb, connection: Connection, alternateConnections: ConcurrentMap<Int, Connection>, entity: JdsEntity) {
        jdsDb.tables.forEach {
            it.executeSave(jdsDb, connection, alternateConnections, entity, onPostSaveEventArguments)
        }
    }

    /**
     * @param overviews
     */
    @Throws(SQLException::class)
    private fun saveOverviews(overviews: Sequence<JdsEntity>) = try {
        val saveOverview = if (jdsDb.supportsStatements) NamedCallableStatement(connection, jdsDb.saveOverview()) else NamedPreparedStatement(connection, jdsDb.saveOverview())
        val saveOverviewInheritance = if (jdsDb.supportsStatements) NamedCallableStatement(connection, jdsDb.saveOverviewInheritance()) else NamedPreparedStatement(connection, jdsDb.saveOverviewInheritance())
        var total = 0
        overviews.forEach {
            //Entity Overview
            saveOverview.setString("compositeKey", it.overview.compositeKey)
            saveOverview.setString("uuid", it.overview.uuid)
            saveOverview.setString("uuidLocation", it.overview.uuidLocation)
            saveOverview.setInt("uuidLocationVersion", it.overview.uuidLocationVersion)
            saveOverview.setLong("entityId", it.overview.entityId)
            saveOverview.setBoolean("live", it.overview.live)
            saveOverview.setString("parentUuid", it.overview.parentUuid)
            saveOverview.setTimestamp("lastEdit", Timestamp.valueOf(it.overview.lastEdit))
            saveOverview.setLong("entityVersion", it.overview.entityVersion) //always update date modified!!!
            saveOverview.addBatch()
            //Entity Inheritance
            saveOverviewInheritance.setString("uuid", it.overview.compositeKey)
            saveOverviewInheritance.setLong("entityId", it.overview.entityId)
            saveOverviewInheritance.addBatch()
            total++
        }

        saveOverview.executeBatch()
        saveOverviewInheritance.executeBatch()

        saveOverview.close()
        saveOverviewInheritance.close()

        if (jdsDb.options.isPrintingOutput) {
            println("Saving $total overview record(s)")
        } else {
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param entity
     */
    private fun saveBlobs(entity: JdsEntity) = try {
        val upsert = if (jdsDb.supportsStatements) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveBlob()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveBlob())
        entity.blobProperties.forEach { fieldId, blobProperty ->
            upsert.setString("uuid", entity.overview.compositeKey)
            upsert.setLong("fieldId", fieldId)
            upsert.setBytes("value", blobProperty.get()!!)
            upsert.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param entity
     */
    private fun saveBooleans(entity: JdsEntity) = try {
        val upsert = if (jdsDb.supportsStatements) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveBoolean()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveBoolean())
        entity.booleanProperties.forEach { fieldId, entry ->
            upsert.setString("uuid", entity.overview.compositeKey)
            upsert.setLong("fieldId", fieldId)
            upsert.setObject("value", entry.value) //primitives could be null, default value has meaning
            upsert.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param entity
     */
    private fun saveIntegers(entity: JdsEntity) = try {
        val upsert = if (jdsDb.supportsStatements) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveInteger()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveInteger())
        entity.integerProperties.forEach { fieldId, entry ->
            upsert.setString("uuid", entity.overview.compositeKey)
            upsert.setLong("fieldId", fieldId)
            upsert.setObject("value", entry.value) //primitives could be null, default value has meaning
            upsert.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param entity
     */
    private fun saveFloats(entity: JdsEntity) = try {
        val upsert = if (jdsDb.supportsStatements) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveFloat()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveFloat())
        entity.floatProperties.forEach { fieldId, entry ->
            upsert.setString("uuid", entity.overview.compositeKey)
            upsert.setLong("fieldId", fieldId)
            upsert.setObject("value", entry.value) //primitives could be null, default value has meaning
            upsert.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param entity
     */
    private fun saveDoubles(entity: JdsEntity) = try {
        val upsert = if (jdsDb.supportsStatements) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveDouble()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveDouble())
        entity.doubleProperties.forEach { fieldId, entry ->
            upsert.setString("uuid", entity.overview.compositeKey)
            upsert.setLong("fieldId", fieldId)
            upsert.setObject("value", entry.value) //primitives could be null, default value has meaning
            upsert.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param entity
     */
    private fun saveLongs(entity: JdsEntity) = try {
        val upsert = if (jdsDb.supportsStatements) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveLong()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveLong())
        entity.longProperties.forEach { fieldId, entry ->
            upsert.setString("uuid", entity.overview.compositeKey)
            upsert.setLong("fieldId", fieldId)
            upsert.setObject("value", entry.value) //primitives could be null, default value has meaning
            upsert.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param entity
     */
    private fun saveStrings(entity: JdsEntity) = try {
        val upsert = if (jdsDb.supportsStatements) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveString()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveString())
        entity.stringProperties.forEach { fieldId, value2 ->
            val value = value2.get()
            upsert.setString("uuid", entity.overview.compositeKey)
            upsert.setLong("fieldId", fieldId)
            upsert.setString("value", value)
            upsert.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param entity
     */
    private fun saveDateConstructs(entity: JdsEntity) = try {
        val upsertText = if (jdsDb.supportsStatements) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveString()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveString())
        val upsertLong = if (jdsDb.supportsStatements) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveLong()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveLong())
        entity.monthDayProperties.forEach { fieldId, monthDayProperty ->
            val monthDay = monthDayProperty.get()
            val value = monthDay.toString()
            upsertText.setString("uuid", entity.overview.compositeKey)
            upsertText.setLong("fieldId", fieldId)
            upsertText.setString("value", value)
            upsertText.addBatch()
        }
        entity.yearMonthProperties.forEach { fieldId, yearMonthProperty ->
            val yearMonth = yearMonthProperty.get() as YearMonth
            val value = yearMonth.toString()
            upsertText.setString("uuid", entity.overview.compositeKey)
            upsertText.setLong("fieldId", fieldId)
            upsertText.setString("value", value)
            upsertText.addBatch()
        }
        entity.periodProperties.forEach { fieldId, periodProperty ->
            val period = periodProperty.get()
            val value = period.toString()
            upsertText.setString("uuid", entity.overview.compositeKey)
            upsertText.setLong("fieldId", fieldId)
            upsertText.setString("value", value)
            upsertText.addBatch()
        }
        entity.durationProperties.forEach { fieldId, durationProperty ->
            val duration = durationProperty.get()
            val value = duration.toNanos()
            upsertLong.setString("uuid", entity.overview.compositeKey)
            upsertLong.setLong("fieldId", fieldId)
            upsertLong.setLong("value", value)
            upsertLong.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param entity
     */
    private fun saveDatesAndDateTimes(entity: JdsEntity) = try {
        val upsert = if (jdsDb.supportsStatements) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveDateTime()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveDateTime())
        entity.localDateTimeProperties.forEach { fieldId, value1 ->
            val localDateTime = value1.get() as LocalDateTime
            upsert.setString("uuid", entity.overview.compositeKey)
            upsert.setLong("fieldId", fieldId)
            upsert.setTimestamp("value", Timestamp.valueOf(localDateTime))
            upsert.addBatch()
        }
        entity.localDateProperties.forEach { fieldId, value1 ->
            val localDate = value1.get() as LocalDate
            upsert.setString("uuid", entity.overview.compositeKey)
            upsert.setLong("fieldId", fieldId)
            upsert.setTimestamp("value", Timestamp.valueOf(localDate.atStartOfDay()))
            upsert.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param entity
     */
    private fun saveTimes(entity: JdsEntity) = try {
        val upsert = if (jdsDb.supportsStatements) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveTime()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveTime())
        entity.localTimeProperties.forEach { fieldId, value1 ->
            val localTime = value1.get() as LocalTime
            if (jdsDb.options.isWritingToPrimaryDataTables) {
                upsert.setString("uuid", entity.overview.compositeKey)
                upsert.setLong("fieldId", fieldId)
                upsert.setLocalTime("value", localTime, jdsDb)
                upsert.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param entity
     */
    private fun saveZonedDateTimes(entity: JdsEntity) = try {
        val upsert = if (jdsDb.supportsStatements) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveZonedDateTime()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveZonedDateTime())
        entity.zonedDateTimeProperties.forEach { fieldId, value1 ->
            val zonedDateTime = value1.get() as ZonedDateTime
            upsert.setString("uuid", entity.overview.compositeKey)
            upsert.setLong("fieldId", fieldId)
            upsert.setZonedDateTime("value", zonedDateTime, jdsDb)
            upsert.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param entity
     */
    private fun saveEnums(entity: JdsEntity) = try {
        val upsert = if (jdsDb.supportsStatements) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveInteger()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveInteger())
        entity.enumProperties.forEach { jdsFieldEnum, value2 ->
            val value = value2.get()
            upsert.setString("uuid", entity.overview.compositeKey)
            upsert.setLong("fieldId", jdsFieldEnum.field.id)
            upsert.setInt("value", jdsFieldEnum.indexOf(value))
            upsert.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * Save all dates in one go
     * @param entity
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5]to[3,4]
     */
    private fun saveArrayDates(entity: JdsEntity) = try {
        val deleteSql = "DELETE FROM jds_store_date_time_array WHERE field_id = ? AND composite_key = ?"
        val insertSql = "INSERT INTO jds_store_date_time_array (sequence, value,field_id, composite_key) VALUES (?,?,?,?)"

        val delete = onPostSaveEventArguments.getOrAddStatement(deleteSql)
        val insert = onPostSaveEventArguments.getOrAddStatement(insertSql)

        entity.dateTimeArrayProperties.forEach { fieldId, u ->
            u.forEachIndexed { index, value ->
                delete.setLong(1, fieldId)
                delete.setString(2, entity.overview.compositeKey)
                delete.addBatch()
                //insert
                insert.setInt(1, index)
                insert.setTimestamp(2, Timestamp.valueOf(value))
                insert.setLong(3, fieldId)
                insert.setString(4, entity.overview.compositeKey)
                insert.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param floatArrayProperties

     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection k.e [3,4,5]to[3,4]
     */
    private fun saveArrayFloats(entity: JdsEntity) = try {
        val deleteSql = "DELETE FROM jds_store_float_array WHERE field_id = ? AND composite_key = ?"
        val insertSql = "INSERT INTO jds_store_float_array (field_id, composite_key, value, sequence) VALUES (?,?,?,?)"
        val delete = onPostSaveEventArguments.getOrAddStatement(deleteSql)
        val insert = onPostSaveEventArguments.getOrAddStatement(insertSql)
        entity.floatArrayProperties.forEach { fieldId, u ->
            u.forEachIndexed { index, value ->
                delete.setLong(1, fieldId)
                delete.setString(2, entity.overview.compositeKey)
                delete.addBatch()
                //insert
                insert.setInt(1, index)
                insert.setObject(2, value) //primitives could be null, default value has meaning
                insert.setLong(3, fieldId)
                insert.setString(4, entity.overview.compositeKey)
                insert.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param entity
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection k.e [3,4,5] to [3,4]
     */
    private fun saveArrayIntegers(entity: JdsEntity) = try {
        val deleteSql = "DELETE FROM jds_store_integer_array WHERE field_id = :fieldId AND composite_key = :uuid"
        val insertSql = "INSERT INTO jds_store_integer_array (field_id, composite_key, sequence, value) VALUES (:fieldId, :uuid, :sequence, :value)"

        val delete = onPostSaveEventArguments.getOrAddNamedStatement(deleteSql)
        val insert = onPostSaveEventArguments.getOrAddNamedStatement(insertSql)
        var record = 0

        entity.integerArrayProperties.forEach { fieldId, u ->
            u.forEachIndexed { index, value ->
                //delete
                delete.setLong("fieldId", fieldId)
                delete.setString("uuid", entity.overview.compositeKey)
                delete.addBatch()
                //insert
                insert.setInt("sequence", index)
                insert.setObject("value", value) //primitives could be null, default value has meaning
                insert.setLong("fieldId", fieldId)
                insert.setString("uuid", entity.overview.compositeKey)
                insert.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param entity
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection k.e [3,4,5]to[3,4]
     */
    private fun saveArrayDoubles(entity: JdsEntity) = try {
        val deleteSql = "DELETE FROM jds_store_double_array WHERE field_id = :fieldId AND composite_key = :uuid"
        val insertSql = "INSERT INTO jds_store_double_array (field_id, composite_key, sequence, value) VALUES (:fieldId, :uuid, :sequence, :value)"
        val delete = onPostSaveEventArguments.getOrAddNamedStatement(deleteSql)
        val insert = onPostSaveEventArguments.getOrAddNamedStatement(insertSql)
        entity.doubleArrayProperties.forEach { fieldId, u ->
            u.forEachIndexed { index, value ->
                //delete
                delete.setLong("fieldId", fieldId)
                delete.setString("uuid", entity.overview.compositeKey)
                delete.addBatch()
                //insert
                insert.setLong("fieldId", fieldId)
                insert.setObject("uuid", value) //primitives could be null, default value has meaning
                insert.setInt("sequence", index)
                insert.setString("value", entity.overview.compositeKey)
                insert.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param entity
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection k.e [3,4,5]to[3,4]
     */
    private fun saveArrayLongs(entity: JdsEntity) = try {
        val deleteSql = "DELETE FROM jds_store_double_array WHERE field_id = ? AND composite_key = ?"
        val insertSql = "INSERT INTO jds_store_double_array (field_id, composite_key, sequence, value) VALUES (?,?,?,?)"
        val delete = onPostSaveEventArguments.getOrAddStatement(deleteSql)
        val insert = onPostSaveEventArguments.getOrAddStatement(insertSql)
        entity.longArrayProperties.forEach { fieldId, u ->
            u.forEachIndexed { index, value ->
                delete.setLong(1, fieldId)
                delete.setString(2, entity.overview.compositeKey)
                delete.addBatch()
                //insert
                insert.setInt(1, index)
                insert.setObject(2, value) //primitives could be null, default value has meaning
                insert.setLong(3, fieldId)
                insert.setString(4, entity.overview.compositeKey)
                insert.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param entity
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection k.e [3,4,5]to[3,4]
     */
    private fun saveArrayStrings(entity: JdsEntity) = try {
        val deleteSql = "DELETE FROM jds_store_text_array WHERE field_id = :fieldId AND composite_key = :uuid"
        val insertSql = "INSERT INTO jds_store_text_array (field_id, composite_key, sequence, value) VALUES (:fieldId, :uuid, :sequence, :value)"
        val delete = onPostSaveEventArguments.getOrAddNamedStatement(deleteSql)
        val insert = onPostSaveEventArguments.getOrAddNamedStatement(insertSql)
        entity.stringArrayProperties.forEach { fieldId, u ->
            u.forEachIndexed { index, value ->
                //delete
                delete.setLong("fieldId", fieldId)
                delete.setString("uuid", entity.overview.compositeKey)
                delete.addBatch()
                //insert
                insert.setInt("fieldId", index)
                insert.setString("uuid", entity.overview.compositeKey)
                insert.setLong("sequence", fieldId)
                insert.setString("value", value)
                insert.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     *@param entity
     * @apiNote Enums are actually saved as index based integer arrays
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection k.e [3,4,5]to[3,4]
     */
    private fun saveEnumCollections(entity: JdsEntity) = try {
        val deleteSql = "DELETE FROM jds_store_integer_array WHERE field_id = :fieldId AND composite_key = :uuid"
        val insertSql = "INSERT INTO jds_store_integer_array (field_id, composite_key, sequence, value) VALUES (:fieldId, :uuid, :sequence, :value)"
        val delete = onPostSaveEventArguments.getOrAddNamedStatement(deleteSql)
        val insert = onPostSaveEventArguments.getOrAddNamedStatement(insertSql)
        entity.enumCollectionProperties.forEach { jdsFieldEnum, u ->
            u.forEachIndexed { index, anEnum ->
                //delete
                delete.setLong("fieldId", jdsFieldEnum.field.id)
                delete.setString("uuid", entity.overview.compositeKey)
                delete.addBatch()
                //insert
                insert.setLong("fieldId", jdsFieldEnum.field.id)
                insert.setString("uuid", entity.overview.compositeKey)
                insert.setInt("sequence", index)
                insert.setInt("value", jdsFieldEnum.indexOf(anEnum))
                insert.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param entity
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection k.e [3,4,5]to[3,4]
     * @implNote For the love of Christ don't use parallel stream here
     */
    @Throws(Exception::class)
    private fun saveAndBindObjectArrays(entity: JdsEntity) {
        if (entity.objectArrayProperties.isEmpty()) return
        val clearOldBindings = onPostSaveEventArguments.getOrAddStatement("DELETE FROM jds_entity_binding WHERE parent_composite_key = ? AND field_id = ?")
        val writeNewBindings = onPostSaveEventArguments.getOrAddStatement("INSERT INTO jds_entity_binding (parent_composite_key, child_composite_key, field_id, child_entity_id) Values(?, ?, ?, ?)")

        entity.objectArrayProperties.forEach { jdsFieldEnum, jdseEntityCollection ->
            jdseEntityCollection.forEach {
                //delete all entries of this field
                clearOldBindings.setString(1, it.overview.parentCompositeKey)
                clearOldBindings.setLong(2, jdsFieldEnum.fieldEntity.id)
                clearOldBindings.addBatch()
                //
                writeNewBindings.setString(1, it.overview.parentCompositeKey)
                writeNewBindings.setString(2, it.overview.compositeKey)
                writeNewBindings.setLong(3, jdsFieldEnum.fieldEntity.id)
                writeNewBindings.setLong(4, it.overview.entityId)
                writeNewBindings.addBatch()
            }
        }
    }

    /**
     * @param entity
     * @implNote For the love of Christ don't use parallel stream here
     */
    @Throws(Exception::class)
    private fun saveAndBindObjects(entity: JdsEntity) {
        if (entity.objectProperties.isEmpty()) return //prevent stack overflow :)
        val clearOldBindings = onPostSaveEventArguments.getOrAddStatement("DELETE FROM jds_entity_binding WHERE parent_composite_key = ? AND field_id = ?")
        val writeNewBindings = onPostSaveEventArguments.getOrAddStatement("INSERT INTO jds_entity_binding(parent_composite_key, child_composite_key, field_id, child_entity_id) Values(?, ?, ?, ?)")
        entity.objectProperties.forEach { key, objectProperty ->
            val jdsEntity = objectProperty.get()
            //delete all entries of this field
            clearOldBindings.setString(1, jdsEntity.overview.parentCompositeKey)
            clearOldBindings.setLong(2, key.fieldEntity.id)
            clearOldBindings.addBatch()
            //add new binding
            writeNewBindings.setString(1, jdsEntity.overview.parentCompositeKey)
            writeNewBindings.setString(2, jdsEntity.overview.uuid)
            writeNewBindings.setLong(3, key.fieldEntity.id)
            writeNewBindings.setLong(4, jdsEntity.overview.entityId)
            writeNewBindings.addBatch()
        }
    }

    /**
     * Helper method allowing you to batch custom statements to a jds save event
     * @query the SQL code to execute
     */
    fun getOrAddStatement(query: String) = onPostSaveEventArguments.getOrAddStatement(query)

    /**
     * Helper method allowing you to batch custom named statements to a jds save event
     * @query the SQL code to execute
     */
    fun getOrAddNamedStatement(query: String) = onPostSaveEventArguments.getOrAddNamedStatement(query)

    /**
     * Helper method allowing you to batch custom calls to a jds save event
     * @query the SQL code to execute
     */
    fun getOrAddCall(query: String) = onPostSaveEventArguments.getOrAddCall(query)

    /**
     * Helper method allowing you to batch custom named calls to a jds save event
     * @query the SQL code to execute
     */
    fun getOrAddNamedCall(query: String) = onPostSaveEventArguments.getOrAddNamedCall(query)

    /**
     * Helper method allowing you to add custom logic preceding a normal JDS save event
     */
    fun addCustomSaveEvent(jdsSaveEvent: JdsSaveEvent) {
        jdsSaveEvents.add(jdsSaveEvent)
    }
}