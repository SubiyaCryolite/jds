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
import io.github.subiyacryolite.jds.JdsExtensions.toByteArray
import io.github.subiyacryolite.jds.events.EventArguments
import io.github.subiyacryolite.jds.events.JdsSaveEvent
import io.github.subiyacryolite.jds.events.JdsSaveListener
import java.sql.Connection
import java.sql.SQLException
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.concurrent.Callable

/**
 * This class is responsible for persisting on or more [JdsEntities][JdsEntity]
 */
class JdsSave(private val jdsDb: JdsDb,
              private val entities: Iterable<JdsEntity>,
              private val postSaveEvent: JdsSaveEvent? = null) : Callable<Boolean> {

    private lateinit var preSaveEventArguments: EventArguments
    private lateinit var onSaveEventArguments: EventArguments
    private lateinit var postSaveEventArguments: EventArguments

    constructor(jdsDb: JdsDb, entities: Array<JdsEntity>, postSaveEvent: JdsSaveEvent? = null) : this(jdsDb, entities.asIterable(), postSaveEvent)

    constructor(jdsDb: JdsDb, entity: JdsEntity, postSaveEvent: JdsSaveEvent? = null) : this(jdsDb, listOf(entity), postSaveEvent)

    /**
     * Computes a result, or throws an exception if unable to do so.
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    override fun call(): Boolean {
        try {
            val chunks = entities.chunked(512)
            val totalChunks = chunks.count()
            jdsDb.dataSource.connection.use { connection ->
                chunks.forEachIndexed { index, batch ->
                    try {
                        val orderedList = sequence { batch.forEach { yieldAll(it.getNestedEntities()) } }
                        saveInner(orderedList.asIterable(), connection)
                        if (jdsDb.options.isLoggingOutput)
                            println("Processing saves. Batch ${index + 1} of $totalChunks")
                    } catch (ex: Exception) {
                        ex.printStackTrace(System.err)
                    }
                }
                if (jdsDb.options.isDeletingOldDataFromReportTablesAfterSave)
                    jdsDb.deleteOldDataFromReportTables(connection)
            }
            return true
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
            return false
        }
    }

    /**
     * @param entities
     * @param connection
     */
    @Throws(Exception::class)
    private fun saveInner(entities: Iterable<JdsEntity>, connection: Connection) {
        try {
            preSaveEventArguments = EventArguments(connection)
            onSaveEventArguments = EventArguments(connection)
            postSaveEventArguments = EventArguments(connection)

            //ensure that overviews are submitted before handing over to listeners
            saveOverview(entities)
            entities.forEach { entity ->
                if (jdsDb.options.isWritingValuesToEavTables) {
                    saveDateConstructs(entity)
                    saveDatesAndDateTimes(entity)
                    saveZonedDateTimes(entity)
                    saveTimes(entity)
                    saveBooleans(entity)
                    saveLongs(entity)
                    saveDoubles(entity)
                    saveIntegers(entity)
                    saveFloats(entity)
                    saveShorts(entity)
                    saveUuids(entity)
                    saveStrings(entity)
                    saveBlobs(entity)
                    saveEnums(entity)
                }
                if (jdsDb.options.isWritingCollectionsToEavTables) {
                    //NOTE arrays have old entries deleted first, for cases where a user reduced the amount of entries in the collection
                    saveArrayDates(entity)
                    saveArrayStrings(entity)
                    saveArrayLongs(entity)
                    saveArrayDoubles(entity)
                    saveArrayIntegers(entity)
                    saveArrayFloats(entity)
                    saveEnumCollections(entity)
                }
                if (jdsDb.options.isWritingEntityBindings) {
                    saveObjectBindings(entity)
                    saveObjectArrayBindings(entity)
                }
                if (entity is JdsSaveListener)
                    entity.onPostSave(postSaveEventArguments)
                postSaveEvent?.onSave(entity, preSaveEventArguments, postSaveEventArguments, connection)
            }
            preSaveEventArguments.use { it.execute() }
            onSaveEventArguments.use { it.execute() }
            postSaveEventArguments.use { it.execute() }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    /**
     * @param jdsDb
     * @param saveEventArguments
     * @param entity
     */
    private fun processCrt(jdsDb: JdsDb, saveEventArguments: EventArguments, entity: JdsEntity) {
        jdsDb.tables.forEach {
            it.executeSave(jdsDb, entity, saveEventArguments)
        }
    }

    private fun regularStatement(eventArguments: EventArguments, sql: String) = eventArguments.getOrAddStatement(sql)

    private fun regularStatementOrCall(eventArguments: EventArguments, sql: String) = if (jdsDb.supportsStatements) eventArguments.getOrAddCall(sql) else regularStatement(eventArguments, sql)

    /**
     * @param entities
     */
    @Throws(Exception::class)
    private fun saveOverview(entities: Iterable<JdsEntity>) = try {

        val saveOverview = regularStatementOrCall(onSaveEventArguments, jdsDb.saveOverview())
        val saveLiveVersion = regularStatementOrCall(onSaveEventArguments, jdsDb.saveEntityLiveVersion())
        val updateLiveVersion = regularStatement(onSaveEventArguments, "UPDATE jds_entity_live_version SET edit_version = ? WHERE uuid = ? AND (edit_version < ? OR edit_version IS NULL)")

        entities.forEach {
            if (jdsDb.options.isWritingToOverviewTable) {
                saveOverview.setString(1, it.overview.uuid)
                saveOverview.setInt(2, it.overview.editVersion)
                saveOverview.setLong(3, it.overview.entityId)
                saveOverview.addBatch()
            }
            if (jdsDb.options.isWritingLatestEntityVersion) {
                saveLiveVersion.setString(1, it.overview.uuid)
                saveLiveVersion.addBatch()

                updateLiveVersion.setInt(1, it.overview.editVersion)
                updateLiveVersion.setString(2, it.overview.uuid)
                updateLiveVersion.setInt(3, it.overview.editVersion)
                updateLiveVersion.addBatch()
            }
            if (jdsDb.options.isWritingToReportingTables && jdsDb.tables.isNotEmpty())
                processCrt(jdsDb, onSaveEventArguments, it)
            if (it is JdsSaveListener)
                it.onPreSave(preSaveEventArguments)
        }
    } catch (ex: SQLException) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveBlobs(jdsEntity: JdsEntity) = try {
        val upsert = regularStatementOrCall(onSaveEventArguments, jdsDb.saveBlob())
        jdsEntity.blobValues.forEach { (fieldId, blobProperty) ->
            upsert.setString(1, jdsEntity.overview.uuid)
            upsert.setInt(2, jdsEntity.overview.editVersion)
            upsert.setLong(3, fieldId)
            upsert.setBytes(4, blobProperty.value)
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
        val upsert = regularStatementOrCall(onSaveEventArguments, jdsDb.saveBoolean())
        jdsEntity.booleanValues.forEach { (fieldId, entry) ->
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
        val upsert = regularStatementOrCall(onSaveEventArguments, jdsDb.saveInteger())
        jdsEntity.integerValues.forEach { (fieldId, entry) ->
            upsert.setString(1, jdsEntity.overview.uuid)
            upsert.setInt(2, jdsEntity.overview.editVersion)
            upsert.setLong(3, fieldId)
            upsert.setObject(4, entry.value?.toInt()) //primitives could be null, default value has meaning
            upsert.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    private fun saveFloats(jdsEntity: JdsEntity) = try {
        val upsert = regularStatementOrCall(onSaveEventArguments, jdsDb.saveFloat())
        jdsEntity.floatValues.forEach { (fieldId, entry) ->
            upsert.setString(1, jdsEntity.overview.uuid)
            upsert.setInt(2, jdsEntity.overview.editVersion)
            upsert.setLong(3, fieldId)
            upsert.setObject(4, entry.value?.toFloat()) //primitives could be null, default value has meaning
            upsert.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    private fun saveShorts(jdsEntity: JdsEntity) = try {
        val upsert = regularStatementOrCall(onSaveEventArguments, jdsDb.saveShort())
        jdsEntity.shortValues.forEach { (fieldId, entry) ->
            upsert.setString(1, jdsEntity.overview.uuid)
            upsert.setInt(2, jdsEntity.overview.editVersion)
            upsert.setLong(3, fieldId)
            upsert.setObject(4, entry.value?.toShort()) //primitives could be null, default value has meaning
            upsert.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    private fun saveUuids(jdsEntity: JdsEntity) = try {
        val upsert = regularStatementOrCall(onSaveEventArguments, jdsDb.saveUuid())
        jdsEntity.uuidValues.forEach { (fieldId, entry) ->
            upsert.setString(1, jdsEntity.overview.uuid)
            upsert.setInt(2, jdsEntity.overview.editVersion)
            upsert.setLong(3, fieldId)
            if ((jdsDb.isOracleDb || jdsDb.isMySqlDb) && entry.value != null) {
                upsert.setObject(4, entry.value.toByteArray())
            } else {
                upsert.setObject(4, entry.value) //primitives could be null, default value has meaning
            }
            upsert.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    private fun saveDoubles(jdsEntity: JdsEntity) = try {
        val upsert = regularStatementOrCall(onSaveEventArguments, jdsDb.saveDouble())
        jdsEntity.doubleValues.forEach { (fieldId, entry) ->
            upsert.setString(1, jdsEntity.overview.uuid)
            upsert.setInt(2, jdsEntity.overview.editVersion)
            upsert.setLong(3, fieldId)
            upsert.setObject(4, entry.value?.toDouble()) //primitives could be null, default value has meaning
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
        val upsert = regularStatementOrCall(onSaveEventArguments, jdsDb.saveLong())
        jdsEntity.longValues.forEach { (fieldId, entry) ->
            upsert.setString(1, jdsEntity.overview.uuid)
            upsert.setInt(2, jdsEntity.overview.editVersion)
            upsert.setLong(3, fieldId)
            upsert.setObject(4, entry.value?.toLong()) //primitives could be null, default value has meaning
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
        val upsert = regularStatementOrCall(onSaveEventArguments, jdsDb.saveString())
        jdsEntity.stringValues.forEach { (fieldId, stringProperty) ->
            upsert.setString(1, jdsEntity.overview.uuid)
            upsert.setInt(2, jdsEntity.overview.editVersion)
            upsert.setLong(3, fieldId)
            upsert.setString(4, stringProperty.value)
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
        val upsertMonthDay = regularStatementOrCall(onSaveEventArguments, jdsDb.saveMonthDay())
        val upsertYearMonth = regularStatementOrCall(onSaveEventArguments, jdsDb.saveYearMonth())
        val upsertPeriod = regularStatementOrCall(onSaveEventArguments, jdsDb.savePeriod())
        val upsertDuration = regularStatementOrCall(onSaveEventArguments, jdsDb.saveDuration())
        jdsEntity.monthDayValues.forEach { (fieldId, monthDayProperty) ->
            upsertMonthDay.setString(1, jdsEntity.overview.uuid)
            upsertMonthDay.setInt(2, jdsEntity.overview.editVersion)
            upsertMonthDay.setLong(3, fieldId)
            upsertMonthDay.setString(4, monthDayProperty.value.toString())
            upsertMonthDay.addBatch()
        }
        jdsEntity.yearMonthValues.forEach { (fieldId, yearMonthProperty) ->
            upsertYearMonth.setString(1, jdsEntity.overview.uuid)
            upsertYearMonth.setInt(2, jdsEntity.overview.editVersion)
            upsertYearMonth.setLong(3, fieldId)
            upsertYearMonth.setString(4, yearMonthProperty.value.toString())
            upsertYearMonth.addBatch()
        }
        jdsEntity.periodValues.forEach { (fieldId, periodProperty) ->
            upsertPeriod.setString(1, jdsEntity.overview.uuid)
            upsertPeriod.setInt(2, jdsEntity.overview.editVersion)
            upsertPeriod.setLong(3, fieldId)
            upsertPeriod.setString(4, periodProperty.value.toString())
            upsertPeriod.addBatch()
        }
        jdsEntity.durationValues.forEach { (fieldId, durationProperty) ->
            upsertDuration.setString(1, jdsEntity.overview.uuid)
            upsertDuration.setInt(2, jdsEntity.overview.editVersion)
            upsertDuration.setLong(3, fieldId)
            upsertDuration.setObject(4, durationProperty.value?.toNanos())
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
        val upsert = regularStatementOrCall(onSaveEventArguments, jdsDb.saveDateTime())
        val upsertz = regularStatementOrCall(onSaveEventArguments, jdsDb.saveDate())
        jdsEntity.localDateTimeValues.forEach { (fieldId, value1) ->
            val localDateTime = value1.value as LocalDateTime?
            upsert.setString(1, jdsEntity.overview.uuid)
            upsert.setInt(2, jdsEntity.overview.editVersion)
            upsert.setLong(3, fieldId)
            upsert.setTimestamp(4, if (localDateTime != null) Timestamp.valueOf(localDateTime) else null)
            upsert.addBatch()
        }
        jdsEntity.localDateValues.forEach { (fieldId, value1) ->
            val localDate = value1.value as LocalDate?
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
        val upsert = regularStatementOrCall(onSaveEventArguments, jdsDb.saveTime())
        jdsEntity.localTimeValues.forEach { (fieldId, localTimeProperty) ->
            val localTime = localTimeProperty.value as LocalTime?
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
        val upsert = regularStatementOrCall(onSaveEventArguments, jdsDb.saveZonedDateTime())
        jdsEntity.zonedDateTimeValues.forEach { (fieldId, value1) ->
            val zonedDateTime = value1.value as ZonedDateTime?
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
        val upsert = regularStatementOrCall(onSaveEventArguments, jdsDb.saveEnum())
        jdsEntity.enumValues.forEach { (jdsFieldEnum, value2) ->
            val value = value2.value
            upsert.setString(1, jdsEntity.overview.uuid)
            upsert.setInt(2, jdsEntity.overview.editVersion)
            upsert.setLong(3, jdsFieldEnum)
            upsert.setObject(4, value?.ordinal            )
            upsert.addBatch()
        }
        val upsertEnumString = regularStatementOrCall(onSaveEventArguments, jdsDb.saveEnumString())
        jdsEntity.stringEnumValues.forEach { (jdsFieldEnum, value2) ->
            val value = value2.value
            upsertEnumString.setString(1, jdsEntity.overview.uuid)
            upsertEnumString.setInt(2, jdsEntity.overview.editVersion)
            upsertEnumString.setLong(3, jdsFieldEnum)
            upsertEnumString.setString(4, value?.name)
            upsertEnumString.addBatch()
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
        val insert = regularStatementOrCall(onSaveEventArguments, jdsDb.saveDateTimeCollections())
        jdsEntity.dateTimeCollections.forEach { (fieldId, dateTimeArray) ->
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
        val insert = regularStatementOrCall(onSaveEventArguments, jdsDb.saveFloatCollections())
        jdsEntity.floatCollections.forEach { (fieldId, u) ->
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
        val insert = regularStatementOrCall(onSaveEventArguments, jdsDb.saveIntegerCollections())
        jdsEntity.integerCollections.forEach { (fieldId, u) ->
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
        val insert = regularStatementOrCall(onSaveEventArguments, jdsDb.saveDoubleCollections())
        jdsEntity.doubleCollections.forEach { (fieldId, u) ->
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
        val insert = regularStatementOrCall(onSaveEventArguments, jdsDb.saveLongCollections())
        jdsEntity.longCollections.forEach { (fieldId, u) ->
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
        val insert = regularStatementOrCall(onSaveEventArguments, jdsDb.saveStringCollections())
        jdsEntity.stringCollections.forEach { (fieldId, u) ->
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
        val insert = regularStatementOrCall(onSaveEventArguments, jdsDb.saveEnumCollections())
        jdsEntity.enumCollections.forEach { (jdsFieldEnum, u) ->
            u.forEach { anEnum ->
                insert.setString(1, jdsEntity.overview.uuid)
                insert.setInt(2, jdsEntity.overview.editVersion)
                insert.setLong(3, jdsFieldEnum)
                insert.setObject(4, anEnum.ordinal)
                insert.addBatch()
            }
        }
        val insertString = regularStatementOrCall(onSaveEventArguments, jdsDb.saveEnumStringCollections())
        jdsEntity.enumStringCollections.forEach { (jdsFieldEnum, u) ->
            u.forEach { anEnum ->
                insertString.setString(1, jdsEntity.overview.uuid)
                insertString.setInt(2, jdsEntity.overview.editVersion)
                insertString.setLong(3, jdsFieldEnum)
                insertString.setString(4, anEnum.name)
                insertString.addBatch()
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
    private fun saveObjectArrayBindings(parentEntity: JdsEntity) = try {
        val updateFieldId = regularStatementOrCall(onSaveEventArguments, jdsDb.saveEntityBindings())
        parentEntity.objectCollections.forEach { (fieldEntity, entityCollection) ->
            entityCollection.forEach {
                updateFieldId.setString(1, parentEntity.overview.uuid)
                updateFieldId.setInt(2, parentEntity.overview.editVersion)
                updateFieldId.setString(3, it.overview.uuid)
                updateFieldId.setInt(4, it.overview.editVersion)
                updateFieldId.setLong(5, fieldEntity.field.id)
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
    private fun saveObjectBindings(parentEntity: JdsEntity) = try {
        val updateFieldId = regularStatementOrCall(onSaveEventArguments, jdsDb.saveEntityBindings())
        parentEntity.objectValues.forEach { (fieldEntity, v) ->
            updateFieldId.setString(1, parentEntity.overview.uuid)
            updateFieldId.setInt(2, parentEntity.overview.editVersion)
            updateFieldId.setString(3, v.value.overview.uuid)
            updateFieldId.setInt(4, v.value.overview.editVersion)
            updateFieldId.setLong(5, fieldEntity.field.id)
            updateFieldId.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }
}