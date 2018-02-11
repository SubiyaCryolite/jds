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

import io.github.subiyacryolite.jds.JdsExtensions.setLocalTime
import io.github.subiyacryolite.jds.JdsExtensions.setZonedDateTime
import io.github.subiyacryolite.jds.events.JdsSaveListener
import io.github.subiyacryolite.jds.events.SaveEventArguments
import java.sql.Connection
import java.sql.SQLException
import java.sql.Timestamp
import java.time.*
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * This class is responsible for persisting on or more [JdsEntities][JdsEntity]
 */
class JdsSave private constructor(private val jdsDb: JdsDb, private val connection: Connection, private val batchSize: Int, private val entities: Iterable<JdsEntity>, private val alternateConnections: ConcurrentMap<Int, Connection> = ConcurrentHashMap(), private val saveEventArguments: SaveEventArguments = SaveEventArguments(jdsDb, connection, alternateConnections), var closeConnection: Boolean = true, private val innerCall: Boolean = false) : Callable<Boolean> {

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
    constructor(jdsDb: JdsDb, entities: Iterable<JdsEntity>, connection: Connection, batchSize: Int) : this(jdsDb, connection, batchSize, entities)

    /**
     * Computes a result, or throws an exception if unable to do so.
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Throws(Exception::class)
    override fun call(): Boolean {
        try {
            val actualBatchSize = if (batchSize == 0) entities.count() else batchSize
            if (actualBatchSize > 0)
                entities.chunked(actualBatchSize).forEachIndexed { step, it ->
                    saveOverview(it)
                    saveInner(it)
                    if (jdsDb.options.isPrintingOutput)
                        println("Processing saves. Step $step")
                }
        } catch (ex: Exception) {
            throw ex
        } finally {
            if (closeConnection && !innerCall) {
                alternateConnections.forEach { it.value.close() }
                connection.close()
            }
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
            batchEntities.filterIsInstance<JdsSaveListener>().forEach { it.onPreSave(saveEventArguments) }

            batchEntities.forEach {
                it.bindChildrenAndUpdateLastEdit()
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

                    //save inner objects beforehand
                    val innerEntities = ArrayList<JdsEntity>()
                    it.objectProperties.forEach { t, u -> innerEntities.add(it) }
                    it.objectArrayProperties.forEach { t, u -> innerEntities.addAll(u) }
                    if (innerEntities.isNotEmpty()) {
                        val innerSave = JdsSave(jdsDb, connection, 0, innerEntities, alternateConnections, saveEventArguments, false, true)
                        innerSave.call()
                    }
                    //bind inner objects
                    saveAndBindObjects(it)
                    saveAndBindObjectArrays(it)
                }
            }

            batchEntities.filterIsInstance<JdsSaveListener>().forEach { it.onPostSave(saveEventArguments) }

            //crt point
            if (jdsDb.tables.isNotEmpty()) {
                batchEntities.forEach {
                    processCrt(jdsDb, connection, alternateConnections, it)
                }
            }

            //respect execution sequence
            //respect JDS batches in each call
            if (!innerCall) {
                saveEventArguments.executeBatches()
            }
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
            it.executeSave(jdsDb, connection, alternateConnections, entity, saveEventArguments)
        }
    }

    /**
     * @param overviews
     */
    @Throws(SQLException::class)
    private fun saveOverview(entities: Collection<JdsEntity>) {
        try {
            if (entities.isEmpty()) return
            val saveOverview = if (jdsDb.supportsStatements) saveEventArguments.getOrAddNamedCall(jdsDb.saveOverview()) else saveEventArguments.getOrAddNamedStatement(jdsDb.saveOverview())
            val saveOverviewInheritance = if (jdsDb.supportsStatements) saveEventArguments.getOrAddNamedCall(jdsDb.saveOverviewInheritance()) else saveEventArguments.getOrAddNamedStatement(jdsDb.saveOverviewInheritance())
            entities.forEach { entity ->
                //:compositeKey, :uuid, :uuidLocation, :uuidLocationVersion, :parentUuid, :entityId, :live, :entityVersion, :lastEdit
                saveOverview.setString("compositeKey", entity.overview.compositeKey)
                saveOverview.setString("uuid", entity.overview.uuid)
                saveOverview.setString("uuidLocation", entity.overview.uuidLocation)
                saveOverview.setInt("uuidLocationVersion", entity.overview.uuidLocationVersion)
                saveOverview.setLong("entityId", entity.overview.entityId)
                saveOverview.setBoolean("live", entity.overview.live)
                saveOverview.setString("parentUuid", entity.overview.parentUuid)
                saveOverview.setTimestamp("lastEdit", Timestamp.valueOf(entity.overview.lastEdit))
                saveOverview.setLong("entityVersion", entity.overview.entityVersion) //always update date modified!!!
                saveOverview.addBatch()

                saveOverviewInheritance.setString("uuid", entity.overview.compositeKey)
                saveOverviewInheritance.setLong("entityId", entity.overview.entityId)
                saveOverviewInheritance.addBatch()
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    /**
     * @param entity
     */
    private fun saveBlobs(entity: JdsEntity) {
        if (entity.blobProperties.isEmpty()) return
        val upsert = if (jdsDb.supportsStatements) saveEventArguments.getOrAddNamedCall(jdsDb.saveBlob()) else saveEventArguments.getOrAddNamedStatement(jdsDb.saveBlob())
        entity.blobProperties.forEach { fieldId, blobProperty ->
            upsert.setString("uuid", entity.overview.compositeKey)
            upsert.setLong("fieldId", fieldId)
            upsert.setBytes("value", blobProperty.get()!!)
            upsert.addBatch()
        }
    }

    /**
     * @param entity
     */
    private fun saveBooleans(entity: JdsEntity) {
        if (entity.booleanProperties.isEmpty()) return
        val upsert = if (jdsDb.supportsStatements) saveEventArguments.getOrAddNamedCall(jdsDb.saveBoolean()) else saveEventArguments.getOrAddNamedStatement(jdsDb.saveBoolean())
        entity.booleanProperties.forEach { fieldId, entry ->
            upsert.setString("uuid", entity.overview.compositeKey)
            upsert.setLong("fieldId", fieldId)
            upsert.setObject("value", entry.value) //primitives could be null, default value has meaning
            upsert.addBatch()
        }
    }

    /**
     * @param entity
     */
    private fun saveIntegers(entity: JdsEntity) {
        if (entity.integerProperties.isEmpty()) return
        val upsert = if (jdsDb.supportsStatements) saveEventArguments.getOrAddNamedCall(jdsDb.saveInteger()) else saveEventArguments.getOrAddNamedStatement(jdsDb.saveInteger())
        entity.integerProperties.forEach { fieldId, entry ->
            upsert.setString("uuid", entity.overview.compositeKey)
            upsert.setLong("fieldId", fieldId)
            upsert.setObject("value", entry.value) //primitives could be null, default value has meaning
            upsert.addBatch()
        }
    }

    /**
     * @param entity
     */
    private fun saveFloats(entity: JdsEntity) {
        if (entity.floatProperties.isEmpty()) return
        val upsert = if (jdsDb.supportsStatements) saveEventArguments.getOrAddNamedCall(jdsDb.saveFloat()) else saveEventArguments.getOrAddNamedStatement(jdsDb.saveFloat())
        entity.floatProperties.forEach { fieldId, entry ->
            upsert.setString("uuid", entity.overview.compositeKey)
            upsert.setLong("fieldId", fieldId)
            upsert.setObject("value", entry.value) //primitives could be null, default value has meaning
            upsert.addBatch()
        }
    }

    /**
     * @param entity
     */
    private fun saveDoubles(entity: JdsEntity) {
        if (entity.doubleProperties.isEmpty()) return
        val upsert = if (jdsDb.supportsStatements) saveEventArguments.getOrAddNamedCall(jdsDb.saveDouble()) else saveEventArguments.getOrAddNamedStatement(jdsDb.saveDouble())
        entity.doubleProperties.forEach { fieldId, entry ->
            upsert.setString("uuid", entity.overview.compositeKey)
            upsert.setLong("fieldId", fieldId)
            upsert.setObject("value", entry.value) //primitives could be null, default value has meaning
            upsert.addBatch()
        }
    }

    /**
     * @param entity
     */
    private fun saveLongs(entity: JdsEntity) {
        if (entity.longProperties.isEmpty()) return
        val upsert = if (jdsDb.supportsStatements) saveEventArguments.getOrAddNamedCall(jdsDb.saveLong()) else saveEventArguments.getOrAddNamedStatement(jdsDb.saveLong())
        entity.longProperties.forEach { fieldId, entry ->
            upsert.setString("uuid", entity.overview.compositeKey)
            upsert.setLong("fieldId", fieldId)
            upsert.setObject("value", entry.value) //primitives could be null, default value has meaning
            upsert.addBatch()
        }
    }

    /**
     * @param entity
     */
    private fun saveStrings(entity: JdsEntity) {
        if (entity.stringProperties.isEmpty()) return
        val upsert = if (jdsDb.supportsStatements) saveEventArguments.getOrAddNamedCall(jdsDb.saveString()) else saveEventArguments.getOrAddNamedStatement(jdsDb.saveString())
        entity.stringProperties.forEach { fieldId, value2 ->
            val value = value2.get()
            upsert.setString("uuid", entity.overview.compositeKey)
            upsert.setLong("fieldId", fieldId)
            upsert.setString("value", value)
            upsert.addBatch()
        }
    }

    /**
     * @param entity
     */
    private fun saveDateConstructs(entity: JdsEntity) {
        if (entity.monthDayProperties.isEmpty() && entity.yearMonthProperties.isEmpty() && entity.periodProperties.isEmpty() && entity.durationProperties.isEmpty()) return
        val upsertText = if (jdsDb.supportsStatements) saveEventArguments.getOrAddNamedCall(jdsDb.saveString()) else saveEventArguments.getOrAddNamedStatement(jdsDb.saveString())
        val upsertLong = if (jdsDb.supportsStatements) saveEventArguments.getOrAddNamedCall(jdsDb.saveLong()) else saveEventArguments.getOrAddNamedStatement(jdsDb.saveLong())
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
    }

    /**
     * @param entity
     */
    private fun saveDatesAndDateTimes(entity: JdsEntity) {
        if (entity.localDateTimeProperties.isEmpty() && entity.localDateProperties.isEmpty()) return
        val upsert = if (jdsDb.supportsStatements) saveEventArguments.getOrAddNamedCall(jdsDb.saveDateTime()) else saveEventArguments.getOrAddNamedStatement(jdsDb.saveDateTime())
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
    }

    /**
     * @param entity
     */
    private fun saveTimes(entity: JdsEntity) {
        if (entity.localTimeProperties.isEmpty()) return
        val upsert = if (jdsDb.supportsStatements) saveEventArguments.getOrAddNamedCall(jdsDb.saveTime()) else saveEventArguments.getOrAddNamedStatement(jdsDb.saveTime())
        entity.localTimeProperties.forEach { fieldId, value1 ->
            val localTime = value1.get() as LocalTime
            if (jdsDb.options.isWritingToPrimaryDataTables) {
                upsert.setString("uuid", entity.overview.compositeKey)
                upsert.setLong("fieldId", fieldId)
                upsert.setLocalTime("value", localTime, jdsDb)
                upsert.addBatch()
            }
        }
    }

    /**
     * @param entity
     */
    private fun saveZonedDateTimes(entity: JdsEntity) {
        if (entity.zonedDateTimeProperties.isEmpty()) return
        val upsert = if (jdsDb.supportsStatements) saveEventArguments.getOrAddNamedCall(jdsDb.saveZonedDateTime()) else saveEventArguments.getOrAddNamedStatement(jdsDb.saveZonedDateTime())
        entity.zonedDateTimeProperties.forEach { fieldId, value1 ->
            val zonedDateTime = value1.get() as ZonedDateTime
            upsert.setString("uuid", entity.overview.compositeKey)
            upsert.setLong("fieldId", fieldId)
            upsert.setZonedDateTime("value", zonedDateTime, jdsDb)
            upsert.addBatch()
        }
    }

    /**
     * @param entity
     */
    private fun saveEnums(entity: JdsEntity) {
        if (entity.enumProperties.isEmpty()) return
        val upsert = if (jdsDb.supportsStatements) saveEventArguments.getOrAddNamedCall(jdsDb.saveInteger()) else saveEventArguments.getOrAddNamedStatement(jdsDb.saveInteger())
        entity.enumProperties.forEach { jdsFieldEnum, value2 ->
            val value = value2.get()
            upsert.setString("uuid", entity.overview.compositeKey)
            upsert.setLong("fieldId", jdsFieldEnum.field.id)
            upsert.setInt("value", jdsFieldEnum.indexOf(value))
            upsert.addBatch()
        }
    }

    /**
     * Save all dates in one go
     * @param entity
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5]to[3,4]
     */
    private fun saveArrayDates(entity: JdsEntity) {
        if (entity.dateTimeArrayProperties.isEmpty()) return
        val deleteSql = "DELETE FROM jds_store_date_time_array WHERE field_id = :fieldId AND composite_key = :compositeKey"
        val insertSql = "INSERT INTO jds_store_date_time_array (field_id, composite_key, sequence, value) VALUES (:fieldId, :compositeKey, :sequence, :value)"
        val delete = saveEventArguments.getOrAddNamedStatement(deleteSql)
        val insert = saveEventArguments.getOrAddNamedStatement(insertSql)
        entity.dateTimeArrayProperties.forEach { fieldId, u ->
            u.forEachIndexed { sequence, value ->
                delete.setLong("fieldId", fieldId)
                delete.setString("compositeKey", entity.overview.compositeKey)
                delete.addBatch()
                //insert
                insert.setInt("sequence", sequence)
                insert.setTimestamp("value", Timestamp.valueOf(value))
                insert.setLong("fieldId", fieldId)
                insert.setString("compositeKey", entity.overview.compositeKey)
                insert.addBatch()
            }
        }
    }

    /**
     * @param floatArrayProperties

     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection k.e [3,4,5]to[3,4]
     */
    private fun saveArrayFloats(entity: JdsEntity) {
        if (entity.floatArrayProperties.isEmpty()) return
        val deleteSql = "DELETE FROM jds_store_float_array WHERE field_id = :fieldId AND composite_key = :compositeKey"
        val insertSql = "INSERT INTO jds_store_float_array (field_id, composite_key, sequence, value) VALUES (:fieldId, :compositeKey, :sequence, :value)"
        val delete = saveEventArguments.getOrAddNamedStatement(deleteSql)
        val insert = saveEventArguments.getOrAddNamedStatement(insertSql)
        entity.floatArrayProperties.forEach { fieldId, u ->
            u.forEachIndexed { sequence, value ->
                delete.setLong("fieldId", fieldId)
                delete.setString("compositeKey", entity.overview.compositeKey)
                delete.addBatch()
                //insert
                insert.setInt("sequence", sequence)
                insert.setObject("value", value) //primitives could be null, default value has meaning
                insert.setLong("fieldId", fieldId)
                insert.setString("compositeKey", entity.overview.compositeKey)
                insert.addBatch()
            }
        }
    }

    /**
     * @param entity
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection k.e [3,4,5] to [3,4]
     */
    private fun saveArrayIntegers(entity: JdsEntity) {
        if (entity.integerArrayProperties.isEmpty()) return

        val deleteSql = "DELETE FROM jds_store_integer_array WHERE field_id = :fieldId AND composite_key = :compositeKey"
        val insertSql = "INSERT INTO jds_store_integer_array (field_id, composite_key, sequence, value) VALUES (:fieldId, :compositeKey, :sequence, :value)"

        val delete = saveEventArguments.getOrAddNamedStatement(deleteSql)
        val insert = saveEventArguments.getOrAddNamedStatement(insertSql)

        entity.integerArrayProperties.forEach { fieldId, u ->
            u.forEachIndexed { sequence, value ->
                //delete
                delete.setLong("fieldId", fieldId)
                delete.setString("compositeKey", entity.overview.compositeKey)
                delete.addBatch()
                //insert
                insert.setInt("sequence", sequence)
                insert.setObject("value", value) //primitives could be null, default value has meaning
                insert.setLong("fieldId", fieldId)
                insert.setString("compositeKey", entity.overview.compositeKey)
                insert.addBatch()
            }
        }
    }

    /**
     * @param entity
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection k.e [3,4,5]to[3,4]
     */
    private fun saveArrayDoubles(entity: JdsEntity) {
        if (entity.doubleArrayProperties.isEmpty()) return
        val deleteSql = "DELETE FROM jds_store_double_array WHERE field_id = :fieldId AND composite_key = :compositeKey"
        val insertSql = "INSERT INTO jds_store_double_array (field_id, composite_key, sequence, value) VALUES (:fieldId, :compositeKey, :sequence, :value)"
        val delete = saveEventArguments.getOrAddNamedStatement(deleteSql)
        val insert = saveEventArguments.getOrAddNamedStatement(insertSql)
        entity.doubleArrayProperties.forEach { fieldId, u ->
            u.forEachIndexed { sequence, value ->
                //delete
                delete.setLong("fieldId", fieldId)
                delete.setString("compositeKey", entity.overview.compositeKey)
                delete.addBatch()
                //insert
                insert.setLong("fieldId", fieldId)
                insert.setObject("value", value) //primitives could be null, default value has meaning
                insert.setInt("sequence", sequence)
                insert.setString("compositeKey", entity.overview.compositeKey)
                insert.addBatch()
            }
        }
    }

    /**
     * @param entity
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection k.e [3,4,5]to[3,4]
     */
    private fun saveArrayLongs(entity: JdsEntity) {
        if (entity.longArrayProperties.isEmpty()) return
        val deleteSql = "DELETE FROM jds_store_double_array WHERE field_id = :fieldId AND composite_key = :compositeKey"
        val insertSql = "INSERT INTO jds_store_double_array (field_id, composite_key, sequence, value) VALUES (:fieldId, :compositeKey, :sequence, :value)"
        val delete = saveEventArguments.getOrAddNamedStatement(deleteSql)
        val insert = saveEventArguments.getOrAddNamedStatement(insertSql)
        entity.longArrayProperties.forEach { fieldId, u ->
            u.forEachIndexed { sequence, value ->
                delete.setLong("fieldId", fieldId)
                delete.setString("compositeKey", entity.overview.compositeKey)
                delete.addBatch()
                //insert
                insert.setLong("fieldId", fieldId)
                insert.setString("compositeKey", entity.overview.compositeKey)
                insert.setInt("sequence", sequence)
                insert.setObject("value", value) //primitives could be null, default value has meaning
                insert.addBatch()
            }
        }
    }

    /**
     * @param entity
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection k.e [3,4,5]to[3,4]
     */
    private fun saveArrayStrings(entity: JdsEntity) {
        if (entity.stringArrayProperties.isEmpty()) return
        val deleteSql = "DELETE FROM jds_store_text_array WHERE field_id = :fieldId AND composite_key = :compositeKey"
        val insertSql = "INSERT INTO jds_store_text_array (field_id, composite_key, sequence, value) VALUES (:fieldId, :compositeKey, :sequence, :value)"
        val delete = saveEventArguments.getOrAddNamedStatement(deleteSql)
        val insert = saveEventArguments.getOrAddNamedStatement(insertSql)
        entity.stringArrayProperties.forEach { fieldId, u ->
            u.forEachIndexed { sequence, value ->
                //delete
                delete.setLong("fieldId", fieldId)
                delete.setString("compositeKey", entity.overview.compositeKey)
                delete.addBatch()
                //insert
                insert.setLong("fieldId", fieldId)
                insert.setString("compositeKey", entity.overview.compositeKey)
                insert.setInt("sequence", sequence)
                insert.setString("value", value)
                insert.addBatch()
            }
        }
    }

    /**
     *@param entity
     * @apiNote Enums are actually saved as index based integer arrays
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection k.e [3,4,5]to[3,4]
     */
    private fun saveEnumCollections(entity: JdsEntity) {
        if (entity.enumCollectionProperties.isEmpty()) return
        val deleteSql = "DELETE FROM jds_store_integer_array WHERE field_id = :fieldId AND composite_key = :compositeKey"
        val insertSql = "INSERT INTO jds_store_integer_array (field_id, composite_key, sequence, value) VALUES (:fieldId, :compositeKey, :sequence, :value)"
        val delete = saveEventArguments.getOrAddNamedStatement(deleteSql)
        val insert = saveEventArguments.getOrAddNamedStatement(insertSql)
        entity.enumCollectionProperties.forEach { jdsFieldEnum, u ->
            u.forEachIndexed { sequence, anEnum ->
                //delete
                delete.setLong("fieldId", jdsFieldEnum.field.id)
                delete.setString("compositeKey", entity.overview.compositeKey)
                delete.addBatch()
                //insert
                insert.setLong("fieldId", jdsFieldEnum.field.id)
                insert.setString("compositeKey", entity.overview.compositeKey)
                insert.setInt("sequence", sequence)
                insert.setInt("value", jdsFieldEnum.indexOf(anEnum))
                insert.addBatch()
            }
        }
    }

    /**
     * @param entity
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection k.e [3,4,5]to[3,4]
     * @implNote For the love of Christ don't use parallel stream here
     */
    @Throws(Exception::class)
    private fun saveAndBindObjectArrays(entity: JdsEntity) {
        if (entity.objectArrayProperties.isEmpty()) return
        val clearOldBindings = saveEventArguments.getOrAddStatement("DELETE FROM jds_entity_binding WHERE parent_composite_key = ? AND field_id = ?")
        val writeNewBindings = saveEventArguments.getOrAddStatement("INSERT INTO jds_entity_binding (parent_composite_key, child_composite_key, field_id, child_entity_id) Values(?, ?, ?, ?)")
        entity.objectArrayProperties.forEach { jdsFieldEnum, jdseEntityCollection ->
            jdseEntityCollection.forEach {
                //delete all entries of this field
                clearOldBindings.setString(1, it.overview.parentCompositeKey)
                clearOldBindings.setLong(2, jdsFieldEnum.fieldEntity.id)
                clearOldBindings.addBatch()
                //add new binding
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
        val clearOldBindings = saveEventArguments.getOrAddStatement("DELETE FROM jds_entity_binding WHERE parent_composite_key = ? AND field_id = ?")
        val writeNewBindings = saveEventArguments.getOrAddStatement("INSERT INTO jds_entity_binding(parent_composite_key, child_composite_key, field_id, child_entity_id) Values(?, ?, ?, ?)")
        entity.objectProperties.forEach { key, objectProperty ->
            val jdsEntity = objectProperty.get()
            //overview first
            //delete all entries of this field
            clearOldBindings.setString(1, jdsEntity.overview.parentCompositeKey)
            clearOldBindings.setLong(2, key.fieldEntity.id)
            clearOldBindings.addBatch()
            //add new binding
            writeNewBindings.setString(1, jdsEntity.overview.parentCompositeKey)
            writeNewBindings.setString(2, jdsEntity.overview.compositeKey)
            writeNewBindings.setLong(3, key.fieldEntity.id)
            writeNewBindings.setLong(4, jdsEntity.overview.entityId)
            writeNewBindings.addBatch()
        }
    }

    /**
     * Helper method allowing you to batch custom statements to a jds save event
     * @query the SQL code to execute
     */
    fun getOrAddStatement(query: String) = saveEventArguments.getOrAddStatement(query)

    /**
     * Helper method allowing you to batch custom named statements to a jds save event
     * @query the SQL code to execute
     */
    fun getOrAddNamedStatement(query: String) = saveEventArguments.getOrAddNamedStatement(query)

    /**
     * Helper method allowing you to batch custom calls to a jds save event
     * @query the SQL code to execute
     */
    fun getOrAddCall(query: String) = saveEventArguments.getOrAddCall(query)

    /**
     * Helper method allowing you to batch custom named calls to a jds save event
     * @query the SQL code to execute
     */
    fun getOrAddNamedCall(query: String) = saveEventArguments.getOrAddNamedCall(query)

}