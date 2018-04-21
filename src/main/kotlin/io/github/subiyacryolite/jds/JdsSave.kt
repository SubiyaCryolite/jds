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

import io.github.subiyacryolite.jds.JdsExtensions.setLocalDate
import io.github.subiyacryolite.jds.JdsExtensions.setLocalTime
import io.github.subiyacryolite.jds.JdsExtensions.setZonedDateTime
import io.github.subiyacryolite.jds.enums.JdsImplementation
import io.github.subiyacryolite.jds.events.JdsSaveEvent
import io.github.subiyacryolite.jds.events.JdsSaveListener
import io.github.subiyacryolite.jds.events.SaveEventArgument
import java.sql.Connection
import java.sql.SQLException
import java.sql.Timestamp
import java.time.*
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.coroutines.experimental.buildSequence

/**
 * This class is responsible for persisting on or more [JdsEntities][JdsEntity]
 */
class JdsSave private constructor(private val jdsDb: JdsDb,
                                  private val connection: Connection,
                                  private val entities: Iterable<JdsEntity>,
                                  private val postSaveEvent: JdsSaveEvent? = null,
                                  private val alternateConnections: ConcurrentMap<Int, Connection> = ConcurrentHashMap(),
                                  private val preSaveEventArgument: SaveEventArgument = SaveEventArgument(jdsDb, connection, alternateConnections),
                                  private val postSaveEventArgument: SaveEventArgument = SaveEventArgument(jdsDb, connection, alternateConnections),
                                  var closeConnection: Boolean = true,
                                  private val recursiveInnerCall: Boolean = false) : Callable<Boolean> {


    /**
     * @param jdsDb
     * @param batchSize
     * @param entities
     */
    @Throws(SQLException::class, ClassNotFoundException::class)
    constructor(jdsDb: JdsDb, entities: Iterable<JdsEntity>, postSaveEvent: JdsSaveEvent? = null) : this(jdsDb, entities, jdsDb.connection, postSaveEvent)


    /**
     * @param jdsDb
     * @param batchSize
     * @param connection
     * @param entities
     */
    @Throws(SQLException::class, ClassNotFoundException::class)
    constructor(jdsDb: JdsDb, entities: Iterable<JdsEntity>, connection: Connection, postSaveEvent: JdsSaveEvent? = null) : this(jdsDb, connection, entities, postSaveEvent)

    /**
     * Computes a result, or throws an exception if unable to do so.
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    override fun call(): Boolean {
        val chunks = entities.chunked(1024)
        val totalChunks = chunks.count()
        chunks.forEachIndexed { index, batch ->
            try {
                val orderedList = buildSequence { batch.forEach { yieldAll(it.getNestedEntities()) } }
                saveInner(orderedList.asIterable(), index == (totalChunks - 1))
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
            saveOverview(entities)
            entities.forEach {
                if (jdsDb.options.isWritingToPrimaryDataTables) {
                    saveDateConstructs(it)
                    saveDatesAndDateTimes(it)
                    saveZonedDateTimes(it)
                    saveTimes(it)
                    saveBooleans(it)
                    saveLongs(it)
                    saveDoubles(it)
                    saveIntegers(it)
                    saveFloats(it)
                    saveStrings(it)
                    saveBlobs(it)
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
                if (jdsDb.options.isWritingToPrimaryDataTables || jdsDb.options.isWritingOverviewFields || jdsDb.options.isWritingArrayValues) {
                    saveAndBindObjects(it)
                    saveAndBindObjectArrays(it)
                }
                if (it is JdsSaveListener)
                    it.onPostSave(postSaveEventArgument)

                postSaveEvent?.onSave(it, postSaveEventArgument, connection)
            }
            preSaveEventArgument.executeBatches()
            postSaveEventArgument.executeBatches()
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
    private fun processCrt(jdsDb: JdsDb, saveEventArguments: SaveEventArgument, entity: JdsEntity) {
        jdsDb.tables.forEach {
            it.executeSave(jdsDb, entity, saveEventArguments)
            //if flat tables only store latest data delete all entries that dont have a map in JdsLatestEntry
            if (it.isStoringLiveRecordsOnly) {
                val stmt = saveEventArguments.getOrAddStatement(it.deleteOldRecords(jdsDb))
                stmt.addBatch()
            }
        }
    }


    private fun namedStatement(eventArguments: SaveEventArgument, sql: String) = eventArguments.getOrAddNamedStatement(connection, sql)

    private fun namedStatementOrCall(eventArguments: SaveEventArgument, sql: String) = if (jdsDb.supportsStatements) eventArguments.getOrAddNamedCall(sql) else namedStatement(eventArguments, sql)

    private fun regularStatement(eventArguments: SaveEventArgument, sql: String) = eventArguments.getOrAddStatement(sql)

    private fun regularStatementOrCall(eventArguments: SaveEventArgument, sql: String) = if (jdsDb.supportsStatements) eventArguments.getOrAddCall(sql) else regularStatement(eventArguments, sql)

    /**
     * @param overviews
     */
    @Throws(Exception::class)
    private fun saveOverview(entities: Iterable<JdsEntity>) = try {

        val saveOverview = regularStatementOrCall(preSaveEventArgument, jdsDb.saveOverview())
        val saveLiveVersion = regularStatementOrCall(preSaveEventArgument, jdsDb.saveEntityLiveVersion())
        val updateLiveVersion = regularStatement(preSaveEventArgument, "UPDATE jds_entity_live_version SET edit_version = ? WHERE uuid = ? AND (edit_version < ? OR edit_version IS NULL)")

        entities.forEach {
            saveOverview.setString(1, it.overview.uuid)
            saveOverview.setInt(2, it.overview.editVersion)
            saveOverview.setLong(3, it.overview.entityId)
            saveOverview.setLong(4, it.overview.entityVersion)
            saveOverview.addBatch()

            saveLiveVersion.setString(1, it.overview.uuid)
            saveLiveVersion.addBatch()

            updateLiveVersion.setInt(1, it.overview.editVersion)
            updateLiveVersion.setString(2, it.overview.uuid)
            updateLiveVersion.setInt(3, it.overview.editVersion)
            updateLiveVersion.addBatch()

            if (jdsDb.options.isWritingToReportingTables && jdsDb.tables.isNotEmpty())
                processCrt(jdsDb, postSaveEventArgument, it)

            if (it is JdsSaveListener)
                it.onPreSave(preSaveEventArgument)
        }

//        if (jdsDb.options.isUpdatingCustomReportTablesPerSave) {
//            customTablesDelete.forEach { placeHolderQuery, filterKeys ->
//                val placeHolder = String.format(placeHolderQuery, JdsLoad.prepareParamaterSequence(filterKeys.size))
//                val stmt = preSaveEventArgument.getOrAddStatement(placeHolder)
//                filterKeys.forEachIndexed { index, filterKey ->
//                    stmt.setObject(index + 1, filterKey)
//                }
//                stmt.addBatch()
//            }
//        } else {
//        }
    } catch (ex: SQLException) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveBlobs(jdsEntity: JdsEntity) = try {
        val upsert = regularStatementOrCall(postSaveEventArgument, jdsDb.saveBlob())
        jdsEntity.blobProperties.forEach { fieldId, blobProperty ->
            upsert.setString(1, jdsEntity.overview.uuid)
            upsert.setInt(2, jdsEntity.overview.editVersion)
            upsert.setLong(3, fieldId)
            upsert.setBytes(4, blobProperty.get())
            upsert.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveBooleans(jdsEntity: JdsEntity) = try {
        val upsert = regularStatementOrCall(postSaveEventArgument, jdsDb.saveBoolean())
        jdsEntity.booleanProperties.forEach { fieldId, entry ->
            upsert.setString(1, jdsEntity.overview.uuid)
            upsert.setInt(2, jdsEntity.overview.editVersion)
            upsert.setLong(3, fieldId)
            upsert.setObject(4, entry.value) //primitives could be null, default value has meaning
            upsert.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    private fun saveIntegers(jdsEntity: JdsEntity) = try {
        val upsert = regularStatementOrCall(postSaveEventArgument, jdsDb.saveInteger())
        jdsEntity.integerProperties.forEach { fieldId, entry ->
            upsert.setString(1, jdsEntity.overview.uuid)
            upsert.setInt(2, jdsEntity.overview.editVersion)
            upsert.setLong(3, fieldId)
            upsert.setObject(4, entry.value) //primitives could be null, default value has meaning
            upsert.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    private fun saveFloats(jdsEntity: JdsEntity) = try {
        val upsert = regularStatementOrCall(postSaveEventArgument, jdsDb.saveFloat())
        jdsEntity.floatProperties.forEach { fieldId, entry ->
            upsert.setString(1, jdsEntity.overview.uuid)
            upsert.setInt(2, jdsEntity.overview.editVersion)
            upsert.setLong(3, fieldId)
            upsert.setObject(4, entry.value) //primitives could be null, default value has meaning
            upsert.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    private fun saveDoubles(jdsEntity: JdsEntity) = try {
        val upsert = regularStatementOrCall(postSaveEventArgument, jdsDb.saveDouble())
        jdsEntity.doubleProperties.forEach { fieldId, entry ->
            upsert.setString(1, jdsEntity.overview.uuid)
            upsert.setInt(2, jdsEntity.overview.editVersion)
            upsert.setLong(3, fieldId)
            upsert.setObject(4, entry.value) //primitives could be null, default value has meaning
            upsert.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveLongs(jdsEntity: JdsEntity) = try {
        val upsert = regularStatementOrCall(postSaveEventArgument, jdsDb.saveLong())
        jdsEntity.longProperties.forEach { fieldId, entry ->
            upsert.setString(1, jdsEntity.overview.uuid)
            upsert.setInt(2, jdsEntity.overview.editVersion)
            upsert.setLong(3, fieldId)
            upsert.setObject(4, entry.value) //primitives could be null, default value has meaning
            upsert.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveStrings(jdsEntity: JdsEntity) = try {
        val upsert = regularStatementOrCall(postSaveEventArgument, jdsDb.saveString())
        jdsEntity.stringProperties.forEach { fieldId, stringProperty ->
            val value = stringProperty.get()
            upsert.setString(1, jdsEntity.overview.uuid)
            upsert.setInt(2, jdsEntity.overview.editVersion)
            upsert.setLong(3, fieldId)
            upsert.setString(4, value)
            upsert.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveDateConstructs(jdsEntity: JdsEntity) = try {
        val upsertMonthDay = regularStatementOrCall(postSaveEventArgument, jdsDb.saveMonthDay())
        val upsertYearMonth = regularStatementOrCall(postSaveEventArgument, jdsDb.saveYearMonth())
        val upsertPeriod = regularStatementOrCall(postSaveEventArgument, jdsDb.savePeriod())
        val upsertDuration = regularStatementOrCall(postSaveEventArgument, jdsDb.saveDuration())
        jdsEntity.monthDayProperties.forEach { fieldId, monthDayProperty ->
            val monthDay = monthDayProperty.get()
            val value = monthDay.toString()
            upsertMonthDay.setString(1, jdsEntity.overview.uuid)
            upsertMonthDay.setInt(2, jdsEntity.overview.editVersion)
            upsertMonthDay.setLong(3, fieldId)
            upsertMonthDay.setString(4, value)
            upsertMonthDay.addBatch()
        }
        jdsEntity.yearMonthProperties.forEach { fieldId, yearMonthProperty ->
            val yearMonth = yearMonthProperty.get() as YearMonth
            val value = yearMonth.toString()
            upsertYearMonth.setString(1, jdsEntity.overview.uuid)
            upsertYearMonth.setInt(2, jdsEntity.overview.editVersion)
            upsertYearMonth.setLong(3, fieldId)
            upsertYearMonth.setString(4, value)
            upsertYearMonth.addBatch()
        }
        jdsEntity.periodProperties.forEach { fieldId, periodProperty ->
            val period = periodProperty.get()
            val value = period.toString()
            upsertPeriod.setString(1, jdsEntity.overview.uuid)
            upsertPeriod.setInt(2, jdsEntity.overview.editVersion)
            upsertPeriod.setLong(3, fieldId)
            upsertPeriod.setString(4, value)
            upsertPeriod.addBatch()
        }
        jdsEntity.durationProperties.forEach { fieldId, durationProperty ->
            val duration = durationProperty.get()
            val value = duration.toNanos()
            upsertDuration.setString(1, jdsEntity.overview.uuid)
            upsertDuration.setInt(2, jdsEntity.overview.editVersion)
            upsertDuration.setLong(3, fieldId)
            upsertDuration.setLong(4, value)
            upsertDuration.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveDatesAndDateTimes(jdsEntity: JdsEntity) = try {
        val upsert = regularStatementOrCall(postSaveEventArgument, jdsDb.saveDateTime())
        val upsertz = regularStatementOrCall(postSaveEventArgument, jdsDb.saveDate())
        jdsEntity.localDateTimeProperties.forEach { fieldId, value1 ->
            val localDateTime = value1.get() as LocalDateTime
            upsert.setString(1, jdsEntity.overview.uuid)
            upsert.setInt(2, jdsEntity.overview.editVersion)
            upsert.setLong(3, fieldId)
            upsert.setTimestamp(4, Timestamp.valueOf(localDateTime))
            upsert.addBatch()
        }
        jdsEntity.localDateProperties.forEach { fieldId, value1 ->
            val localDate = value1.get() as LocalDate
            upsertz.setString(1, jdsEntity.overview.uuid)
            upsertz.setInt(2, jdsEntity.overview.editVersion)
            upsertz.setLong(3, fieldId)
            upsertz.setLocalDate(4, localDate, jdsDb)
            upsertz.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveTimes(jdsEntity: JdsEntity) = try {
        val upsert = regularStatementOrCall(postSaveEventArgument, jdsDb.saveTime())
        jdsEntity.localTimeProperties.forEach { fieldId, localTimeProperty ->
            val localTime = localTimeProperty.get() as LocalTime
            upsert.setString(1, jdsEntity.overview.uuid)
            upsert.setInt(2, jdsEntity.overview.editVersion)
            upsert.setLong(3, fieldId)
            upsert.setLocalTime(4, localTime, jdsDb)
            upsert.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveZonedDateTimes(jdsEntity: JdsEntity) = try {
        val upsert = regularStatementOrCall(postSaveEventArgument, jdsDb.saveZonedDateTime())
        jdsEntity.zonedDateTimeProperties.forEach { fieldId, value1 ->
            val zonedDateTime = value1.get() as ZonedDateTime
            upsert.setString(1, jdsEntity.overview.uuid)
            upsert.setInt(2, jdsEntity.overview.editVersion)
            upsert.setLong(3, fieldId)
            upsert.setZonedDateTime(4, zonedDateTime, jdsDb)
            upsert.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveEnums(jdsEntity: JdsEntity) = try {
        val upsert = regularStatementOrCall(postSaveEventArgument, jdsDb.saveEnum())
        jdsEntity.enumProperties.forEach { jdsFieldEnum, value2 ->
            val value = value2.get()
            upsert.setString(1, jdsEntity.overview.uuid)
            upsert.setInt(2, jdsEntity.overview.editVersion)
            upsert.setLong(3, jdsFieldEnum)
            upsert.setObject(4, when (value == null) {
                true -> null
                false -> value!!.ordinal
            }
            )
            upsert.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * Save all dates in one go
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveArrayDates(jdsEntity: JdsEntity) = try {
        val insert = regularStatementOrCall(postSaveEventArgument, jdsDb.saveDateTimeCollections())
        jdsEntity.dateTimeArrayProperties.forEach { fieldId, dateTimeArray ->
            dateTimeArray.forEach { value ->
                insert.setString(1, jdsEntity.overview.uuid)
                insert.setInt(2, jdsEntity.overview.editVersion)
                insert.setLong(3, fieldId)
                insert.setTimestamp(4, Timestamp.valueOf(value))
                insert.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveArrayFloats(jdsEntity: JdsEntity) = try {
        val insert = regularStatementOrCall(postSaveEventArgument, jdsDb.saveFloatCollections())
        jdsEntity.floatArrayProperties.forEach { fieldId, u ->
            u.forEach { value ->
                insert.setString(1, jdsEntity.overview.uuid)
                insert.setInt(2, jdsEntity.overview.editVersion)
                insert.setLong(3, fieldId)
                insert.setObject(4, value) //primitives could be null, default value has meaning
                insert.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveArrayIntegers(jdsEntity: JdsEntity) = try {
        val insert = regularStatementOrCall(postSaveEventArgument, jdsDb.saveIntegerCollections())
        jdsEntity.integerArrayProperties.forEach { fieldId, u ->
            u.forEach { value ->
                insert.setString(1, jdsEntity.overview.uuid)
                insert.setInt(2, jdsEntity.overview.editVersion)
                insert.setLong(3, fieldId)
                insert.setObject(4, value) //primitives could be null, default value has meaning
                insert.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveArrayDoubles(jdsEntity: JdsEntity) = try {
        val insert = regularStatementOrCall(postSaveEventArgument, jdsDb.saveDoubleCollections())
        jdsEntity.doubleArrayProperties.forEach { fieldId, u ->
            u.forEach { value ->
                insert.setString(1, jdsEntity.overview.uuid)
                insert.setInt(2, jdsEntity.overview.editVersion)
                insert.setLong(3, fieldId)
                insert.setObject(4, value) //primitives could be null, default value has meaning
                insert.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveArrayLongs(jdsEntity: JdsEntity) = try {
        val insert = regularStatementOrCall(postSaveEventArgument, jdsDb.saveLongCollections())
        jdsEntity.longArrayProperties.forEach { fieldId, u ->
            u.forEach { value ->
                insert.setString(1, jdsEntity.overview.uuid)
                insert.setInt(2, jdsEntity.overview.editVersion)
                insert.setLong(3, fieldId)
                insert.setObject(4, value) //primitives could be null, default value has meaning
                insert.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveArrayStrings(jdsEntity: JdsEntity) = try {
        val insert = regularStatementOrCall(postSaveEventArgument, jdsDb.saveStringCollections())
        jdsEntity.stringArrayProperties.forEach { fieldId, u ->
            u.forEach { value ->
                insert.setString(1, jdsEntity.overview.uuid)
                insert.setInt(2, jdsEntity.overview.editVersion)
                insert.setLong(3, fieldId)
                insert.setString(4, value)
                insert.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     * @apiNote Enums are actually saved as index based integer arrays
     */
    @Throws(Exception::class)
    private fun saveEnumCollections(jdsEntity: JdsEntity) = try {
        val insert = regularStatementOrCall(postSaveEventArgument, jdsDb.saveEnumCollections())
        jdsEntity.enumCollectionProperties.forEach { jdsFieldEnum, u ->
            u.forEach { anEnum ->
                insert.setString(1, jdsEntity.overview.uuid)
                insert.setInt(2, jdsEntity.overview.editVersion)
                insert.setLong(3, jdsFieldEnum)
                insert.setObject(4, anEnum.ordinal)
                insert.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param parentEntity
     * @implNote For the love of Christ don't use parallel stream here
     */
    @Throws(Exception::class)
    private fun saveAndBindObjectArrays(parentEntity: JdsEntity) = try {
        val updateFieldId = regularStatementOrCall(preSaveEventArgument, jdsDb.saveEntityBindings())
        parentEntity.objectArrayProperties.forEach { fieldEntity, entityCollection ->
            entityCollection.forEach {
                updateFieldId.setString(1, parentEntity.overview.uuid)
                updateFieldId.setInt(2, parentEntity.overview.editVersion)
                updateFieldId.setString(3, it.overview.uuid)
                updateFieldId.setInt(4, it.overview.editVersion)
                updateFieldId.setLong(5, fieldEntity.fieldEntity.id)
                updateFieldId.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param parentEntity
     * @implNote For the love of Christ don't use parallel stream here
     */
    @Throws(Exception::class)
    private fun saveAndBindObjects(parentEntity: JdsEntity) = try {
        val updateFieldId = regularStatementOrCall(preSaveEventArgument, jdsDb.saveEntityBindings())
        parentEntity.objectProperties.forEach { fieldEntity, v ->
            updateFieldId.setString(1, parentEntity.overview.uuid)
            updateFieldId.setInt(2, parentEntity.overview.editVersion)
            updateFieldId.setString(3, v.value.overview.uuid)
            updateFieldId.setInt(4, v.value.overview.editVersion)
            updateFieldId.setLong(5, fieldEntity.fieldEntity.id)
            updateFieldId.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }
}