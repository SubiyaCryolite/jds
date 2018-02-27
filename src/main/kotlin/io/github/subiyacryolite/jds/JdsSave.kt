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
class JdsSave private constructor(private val jdsDb: JdsDb, private val connection: Connection, private val entities: Iterable<JdsEntity>, private val alternateConnections: ConcurrentMap<Int, Connection> = ConcurrentHashMap(), private val preSaveEventArguments: SaveEventArguments = SaveEventArguments(jdsDb, connection, alternateConnections), private val postSaveEventArguments: SaveEventArguments = SaveEventArguments(jdsDb, connection, alternateConnections), var closeConnection: Boolean = true, val recursiveInnerCall: Boolean = false) : Callable<Boolean> {


    /**
     * @param jdsDb
     * @param batchSize
     * @param entities
     */
    @Throws(SQLException::class, ClassNotFoundException::class)
    constructor(jdsDb: JdsDb, entities: Iterable<JdsEntity>) : this(jdsDb, entities, jdsDb.getConnection())


    /**
     * @param jdsDb
     * @param batchSize
     * @param connection
     * @param entities
     */
    @Throws(SQLException::class, ClassNotFoundException::class)
    constructor(jdsDb: JdsDb, entities: Iterable<JdsEntity>, connection: Connection) : this(jdsDb, connection, entities)

    /**
     * Computes a result, or throws an exception if unable to do so.
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    override fun call(): Boolean {
        val chunks = entities.chunked(1000)
        val totalChunks = chunks.count()
        chunks.forEachIndexed { index, batch ->
            try {
                saveInner(batch, index == (totalChunks - 1))
                if (jdsDb.options.isPrintingOutput)
                    println("Processing saves. Batch ${index + 1} of $totalChunks")
            } catch (ex: Exception) {
                ex.printStackTrace(System.err)
            }
        }
        return true
    }

    /**
     * @param batchEntities
     * @throws Exception
     */
    private fun saveInner(entities: Iterable<JdsEntity>, finalStep: Boolean) {
        try {
            //ensure that overviews are submitted before handing over to listeners
            entities.forEach { it.bindChildrenAndUpdateLastEdit() }
            saveOverview(entities)
            entities.filterIsInstance<JdsSaveListener>().forEach { it.onPreSave(preSaveEventArguments) }
            if (jdsDb.options.isWritingToPrimaryDataTables) {
                saveDateConstructs(entities)
                saveDatesAndDateTimes(entities)
                saveZonedDateTimes(entities)
                saveTimes(entities)
                saveBooleans(entities)
                saveLongs(entities)
                saveDoubles(entities)
                saveIntegers(entities)
                saveFloats(entities)
                saveStrings(entities)
                saveBlobs(entities)
                saveEnums(entities)
            }
            if (jdsDb.options.isWritingArrayValues) {
                //array properties [NOTE arrays have old entries deleted first, for cases where a user reduced the amount of entries in the collection]
                saveArrayDates(entities)
                saveArrayStrings(entities)
                saveArrayLongs(entities)
                saveArrayDoubles(entities)
                saveArrayIntegers(entities)
                saveArrayFloats(entities)
                saveEnumCollections(entities)
            }
            if (jdsDb.options.isWritingToPrimaryDataTables || jdsDb.options.isWritingOverviewFields || jdsDb.options.isWritingArrayValues) {
                saveAndBindObjects(entities)
                saveAndBindObjectArrays(entities)
            }

            entities.filterIsInstance<JdsSaveListener>().forEach { it.onPostSave(postSaveEventArguments) }

            //crt point
            if (jdsDb.options.isWritingToReportingTables && jdsDb.tables.isNotEmpty()) {
                entities.forEach { processCrt(jdsDb, connection, alternateConnections, it) }
            }

            preSaveEventArguments.executeBatches()
            postSaveEventArguments.executeBatches()
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        } finally {
            if (!recursiveInnerCall && finalStep && closeConnection) {
                alternateConnections.forEach { it.value.close() }
                connection.close()
            }
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
            it.executeSave(jdsDb, connection, alternateConnections, entity, postSaveEventArguments)
        }
    }


    private fun namedStatement(eventArguments: SaveEventArguments, sql: String) = eventArguments.getOrAddNamedStatement(connection, sql)

    private fun namedStatementOrCall(eventArguments: SaveEventArguments, sql: String) = if (jdsDb.supportsStatements) eventArguments.getOrAddNamedCall(sql) else namedStatement(eventArguments, sql)

    private fun regularStatement(eventArguments: SaveEventArguments, sql: String) = eventArguments.getOrAddStatement(sql)

    private fun regularStatementOrCall(eventArguments: SaveEventArguments, sql: String) = if (jdsDb.supportsStatements) eventArguments.getOrAddCall(sql) else regularStatement(eventArguments, sql)

    /**
     * @param overviews
     */
    @Throws(Exception::class)
    private fun saveOverview(entities: Iterable<JdsEntity>) = try {
        val saveOverview = regularStatementOrCall(preSaveEventArguments, jdsDb.saveOverview())
        val saveOverviewInheritance = namedStatementOrCall(preSaveEventArguments, jdsDb.saveOverviewInheritance())
        entities.forEach {
            saveOverview.setString(1, it.overview.compositeKey)
            saveOverview.setString(2, it.overview.uuid)
            saveOverview.setString(3, it.overview.uuidLocation)
            saveOverview.setInt(4, it.overview.uuidLocationVersion)
            saveOverview.setString(5, it.overview.parentUuid)
            saveOverview.setString(6, it.overview.parentCompositeKey)
            saveOverview.setLong(7, it.overview.entityId)
            saveOverview.setLong(8, it.overview.entityVersion)
            saveOverview.setBoolean(9, it.overview.live)
            saveOverview.setTimestamp(10, Timestamp.valueOf(it.overview.lastEdit))
            saveOverview.addBatch()

            saveOverviewInheritance.setString("uuid", it.overview.compositeKey)
            saveOverviewInheritance.setLong("entityId", it.overview.entityId)
            saveOverviewInheritance.addBatch()
        }
    } catch (ex: SQLException) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param entity
     */
    @Throws(Exception::class)
    private fun saveBlobs(entities: Iterable<JdsEntity>) = try {
        val upsert = namedStatementOrCall(postSaveEventArguments, jdsDb.saveBlob())
        entities.forEach {
            it.blobProperties.forEach { fieldId, blobProperty ->
                upsert.setString("uuid", it.overview.compositeKey)
                upsert.setLong("fieldId", fieldId)
                upsert.setBytes("value", blobProperty.get())
                upsert.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param entity
     */
    @Throws(Exception::class)
    private fun saveBooleans(entities: Iterable<JdsEntity>) = try {
        val upsert = namedStatementOrCall(postSaveEventArguments, jdsDb.saveBoolean())
        entities.forEach {
            it.booleanProperties.forEach { fieldId, entry ->
                upsert.setString("uuid", it.overview.compositeKey)
                upsert.setLong("fieldId", fieldId)
                upsert.setObject("value", entry.value) //primitives could be null, default value has meaning
                upsert.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param entity
     */
    private fun saveIntegers(entities: Iterable<JdsEntity>) = try {
        val upsert = namedStatementOrCall(postSaveEventArguments, jdsDb.saveInteger())
        entities.forEach {
            it.integerProperties.forEach { fieldId, entry ->
                upsert.setString("uuid", it.overview.compositeKey)
                upsert.setLong("fieldId", fieldId)
                upsert.setInt("sequence", 0)
                upsert.setObject("value", entry.value) //primitives could be null, default value has meaning
                upsert.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param entity
     */
    private fun saveFloats(entities: Iterable<JdsEntity>) = try {
        val upsert = namedStatementOrCall(postSaveEventArguments, jdsDb.saveFloat())
        entities.forEach {
            it.floatProperties.forEach { fieldId, entry ->
                upsert.setString("uuid", it.overview.compositeKey)
                upsert.setLong("fieldId", fieldId)
                upsert.setObject("value", entry.value) //primitives could be null, default value has meaning
                upsert.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param entity
     */
    private fun saveDoubles(entities: Iterable<JdsEntity>) = try {
        val upsert = namedStatementOrCall(postSaveEventArguments, jdsDb.saveDouble())
        entities.forEach {
            it.doubleProperties.forEach { fieldId, entry ->
                upsert.setString("uuid", it.overview.compositeKey)
                upsert.setLong("fieldId", fieldId)
                upsert.setObject("value", entry.value) //primitives could be null, default value has meaning
                upsert.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param entity
     */
    @Throws(Exception::class)
    private fun saveLongs(entities: Iterable<JdsEntity>) = try {
        val upsert = namedStatementOrCall(postSaveEventArguments, jdsDb.saveLong())
        entities.forEach {
            it.longProperties.forEach { fieldId, entry ->
                upsert.setString("uuid", it.overview.compositeKey)
                upsert.setLong("fieldId", fieldId)
                upsert.setObject("value", entry.value) //primitives could be null, default value has meaning
                upsert.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param entity
     */
    @Throws(Exception::class)
    private fun saveStrings(entities: Iterable<JdsEntity>) = try {
        val upsert = namedStatementOrCall(postSaveEventArguments, jdsDb.saveString())
        entities.forEach {
            it.stringProperties.forEach { fieldId, stringProperty ->
                val value = stringProperty.get()
                upsert.setString("uuid", it.overview.compositeKey)
                upsert.setLong("fieldId", fieldId)
                upsert.setString("value", value)
                upsert.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param entity
     */
    @Throws(Exception::class)
    private fun saveDateConstructs(entities: Iterable<JdsEntity>) = try {
        val upsertText = namedStatementOrCall(postSaveEventArguments, jdsDb.saveString())
        val upsertLong = namedStatementOrCall(postSaveEventArguments, jdsDb.saveLong())
        entities.forEach {
            it.monthDayProperties.forEach { fieldId, monthDayProperty ->
                val monthDay = monthDayProperty.get()
                val value = monthDay.toString()
                upsertText.setString("uuid", it.overview.compositeKey)
                upsertText.setLong("fieldId", fieldId)
                upsertText.setInt("sequence", 0)
                upsertText.setString("value", value)
                upsertText.addBatch()
            }
            it.yearMonthProperties.forEach { fieldId, yearMonthProperty ->
                val yearMonth = yearMonthProperty.get() as YearMonth
                val value = yearMonth.toString()
                upsertText.setString("uuid", it.overview.compositeKey)
                upsertText.setLong("fieldId", fieldId)
                upsertText.setInt("sequence", 0)
                upsertText.setString("value", value)
                upsertText.addBatch()
            }
            it.periodProperties.forEach { fieldId, periodProperty ->
                val period = periodProperty.get()
                val value = period.toString()
                upsertText.setString("uuid", it.overview.compositeKey)
                upsertText.setLong("fieldId", fieldId)
                upsertText.setInt("sequence", 0)
                upsertText.setString("value", value)
                upsertText.addBatch()
            }
            it.durationProperties.forEach { fieldId, durationProperty ->
                val duration = durationProperty.get()
                val value = duration.toNanos()
                upsertLong.setString("uuid", it.overview.compositeKey)
                upsertLong.setLong("fieldId", fieldId)
                upsertLong.setInt("sequence", 0)
                upsertLong.setLong("value", value)
                upsertLong.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param entity
     */
    @Throws(Exception::class)
    private fun saveDatesAndDateTimes(entities: Iterable<JdsEntity>) = try {
        val upsert = namedStatementOrCall(postSaveEventArguments, jdsDb.saveDateTime())
        entities.forEach {
            it.localDateTimeProperties.forEach { fieldId, value1 ->
                val localDateTime = value1.get() as LocalDateTime
                upsert.setString("uuid", it.overview.compositeKey)
                upsert.setLong("fieldId", fieldId)
                upsert.setInt("sequence", 0)
                upsert.setTimestamp("value", Timestamp.valueOf(localDateTime))
                upsert.addBatch()
            }
            it.localDateProperties.forEach { fieldId, value1 ->
                val localDate = value1.get() as LocalDate
                upsert.setString("uuid", it.overview.compositeKey)
                upsert.setLong("fieldId", fieldId)
                upsert.setInt("sequence", 0)
                upsert.setTimestamp("value", Timestamp.valueOf(localDate.atStartOfDay()))
                upsert.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param entity
     */
    @Throws(Exception::class)
    private fun saveTimes(entities: Iterable<JdsEntity>) = try {
        val upsert = namedStatementOrCall(postSaveEventArguments, jdsDb.saveTime())
        entities.forEach {
            it.localTimeProperties.forEach { fieldId, localTimeProperty ->
                val localTime = localTimeProperty.get() as LocalTime
                upsert.setString("uuid", it.overview.compositeKey)
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
    @Throws(Exception::class)
    private fun saveZonedDateTimes(entities: Iterable<JdsEntity>) = try {
        val upsert = namedStatementOrCall(postSaveEventArguments, jdsDb.saveZonedDateTime())
        entities.forEach {
            it.zonedDateTimeProperties.forEach { fieldId, value1 ->
                val zonedDateTime = value1.get() as ZonedDateTime
                upsert.setString("uuid", it.overview.compositeKey)
                upsert.setLong("fieldId", fieldId)
                upsert.setZonedDateTime("value", zonedDateTime, jdsDb)
                upsert.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param entity
     */
    @Throws(Exception::class)
    private fun saveEnums(entities: Iterable<JdsEntity>) = try {
        val upsert = namedStatementOrCall(postSaveEventArguments, jdsDb.saveInteger())
        entities.forEach {
            it.enumProperties.forEach { jdsFieldEnum, value2 ->
                val value = value2.get()
                upsert.setString("uuid", it.overview.compositeKey)
                upsert.setLong("fieldId", jdsFieldEnum.field.id)
                upsert.setInt("sequence", 0)
                upsert.setObject("value", when (value == null) {
                    true -> null
                    false -> jdsFieldEnum.indexOf(value!!)
                }
                )
                upsert.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * Save all dates in one go
     * @param entity
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5]to[3,4]
     */
    @Throws(Exception::class)
    private fun saveArrayDates(entities: Iterable<JdsEntity>) = try {
        val insert = namedStatementOrCall(postSaveEventArguments, jdsDb.saveDateTime())
        entities.forEach {
            it.dateTimeArrayProperties.forEach { fieldId, dateTimeArray ->
                dateTimeArray.forEachIndexed { sequence, value ->
                    insert.setInt("sequence", sequence)
                    insert.setTimestamp("value", Timestamp.valueOf(value))
                    insert.setLong("fieldId", fieldId)
                    insert.setString("uuid", it.overview.compositeKey)
                    insert.addBatch()
                }
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param floatArrayProperties

     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection k.e [3,4,5]to[3,4]
     */
    @Throws(Exception::class)
    private fun saveArrayFloats(entities: Iterable<JdsEntity>) = try {
        val insert = namedStatementOrCall(postSaveEventArguments, jdsDb.saveFloat())
        entities.forEach {
            it.floatArrayProperties.forEach { fieldId, u ->
                u.forEachIndexed { sequence, value ->
                    insert.setInt("sequence", sequence)
                    insert.setObject("value", value) //primitives could be null, default value has meaning
                    insert.setLong("fieldId", fieldId)
                    insert.setString("uuid", it.overview.compositeKey)
                    insert.addBatch()
                }
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param entity
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection k.e [3,4,5] to [3,4]
     */
    @Throws(Exception::class)
    private fun saveArrayIntegers(entities: Iterable<JdsEntity>) = try {
        val insert = namedStatementOrCall(postSaveEventArguments, jdsDb.saveInteger())
        entities.forEach {
            it.integerArrayProperties.forEach { fieldId, u ->
                u.forEachIndexed { sequence, value ->
                    insert.setInt("sequence", sequence)
                    insert.setObject("value", value) //primitives could be null, default value has meaning
                    insert.setLong("fieldId", fieldId)
                    insert.setString("uuid", it.overview.compositeKey)
                    insert.addBatch()
                }
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param entity
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection k.e [3,4,5]to[3,4]
     */
    @Throws(Exception::class)
    private fun saveArrayDoubles(entities: Iterable<JdsEntity>) = try {
        val insert = namedStatementOrCall(postSaveEventArguments, jdsDb.saveDouble())
        entities.forEach {
            it.doubleArrayProperties.forEach { fieldId, u ->
                u.forEachIndexed { sequence, value ->
                    insert.setLong("fieldId", fieldId)
                    insert.setObject("value", value) //primitives could be null, default value has meaning
                    insert.setInt("sequence", sequence)
                    insert.setString("uuid", it.overview.compositeKey)
                    insert.addBatch()
                }
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param entity
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection k.e [3,4,5]to[3,4]
     */
    @Throws(Exception::class)
    private fun saveArrayLongs(entities: Iterable<JdsEntity>) = try {
        val insert = namedStatementOrCall(postSaveEventArguments, jdsDb.saveLong())
        entities.forEach {
            it.longArrayProperties.forEach { fieldId, u ->
                u.forEachIndexed { sequence, value ->
                    insert.setLong("fieldId", fieldId)
                    insert.setString("uuid", it.overview.compositeKey)
                    insert.setInt("sequence", sequence)
                    insert.setObject("value", value) //primitives could be null, default value has meaning
                    insert.addBatch()
                }
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param entity
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection k.e [3,4,5]to[3,4]
     */
    @Throws(Exception::class)
    private fun saveArrayStrings(entities: Iterable<JdsEntity>) = try {
        val insert = namedStatementOrCall(postSaveEventArguments, jdsDb.saveString())
        entities.forEach {
            it.stringArrayProperties.forEach { fieldId, u ->
                u.forEachIndexed { sequence, value ->
                    insert.setLong("fieldId", fieldId)
                    insert.setString("uuid", it.overview.compositeKey)
                    insert.setInt("sequence", sequence)
                    insert.setString("value", value)
                    insert.addBatch()
                }
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
    @Throws(Exception::class)
    private fun saveEnumCollections(entities: Iterable<JdsEntity>) = try {
        val insert = namedStatementOrCall(postSaveEventArguments, jdsDb.saveInteger())
        entities.forEach {
            it.enumCollectionProperties.forEach { jdsFieldEnum, u ->
                u.forEachIndexed { sequence, anEnum ->
                    //insert
                    if (anEnum != null) {
                        insert.setLong("fieldId", jdsFieldEnum.field.id)
                        insert.setString("uuid", it.overview.compositeKey)
                        insert.setInt("sequence", sequence)
                        insert.setObject("value", jdsFieldEnum.indexOf(anEnum))
                        insert.addBatch()
                    }
                }
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
    private fun saveAndBindObjectArrays(entities: Iterable<JdsEntity>) = try {
        val updateFieldId = regularStatement(postSaveEventArguments, "UPDATE jds_entity_overview SET field_id = ? WHERE composite_key = ?")
        entities.forEach {
            it.objectArrayProperties.forEach { jdsFieldEnum, jdseEntityCollection ->
                JdsSave(jdsDb, connection, jdseEntityCollection.map { it }, alternateConnections, preSaveEventArguments, postSaveEventArguments, false, true).call()
                jdseEntityCollection.forEach {
                    updateFieldId.setLong(1, jdsFieldEnum.fieldEntity.id)
                    updateFieldId.setString(2, it.overview.compositeKey)
                    updateFieldId.addBatch()
                }
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param entity
     * @implNote For the love of Christ don't use parallel stream here
     */
    @Throws(Exception::class)
    private fun saveAndBindObjects(entities: Iterable<JdsEntity>) = try {
        val updateFieldId = regularStatement(postSaveEventArguments, "UPDATE jds_entity_overview SET field_id = ? WHERE composite_key = ?")
        entities.forEach {
            JdsSave(jdsDb, connection, it.objectProperties.values.map { it.value }, alternateConnections, preSaveEventArguments, postSaveEventArguments, false, true).call()
            it.objectProperties.forEach { k, v ->
                updateFieldId.setLong(1, k.fieldEntity.id)
                updateFieldId.setString(2, v.value.overview.compositeKey)
                updateFieldId.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }
}