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

import io.github.subiyacryolite.jds.JdsExtensions.filterSensitiveFields
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
                        if (jdsDb.options.logOutput)
                            println("Processing saves. Batch ${index + 1} of $totalChunks")
                    } catch (ex: Exception) {
                        ex.printStackTrace(System.err)
                    }
                }
                if (jdsDb.options.deleteOutdatedTransposeDataPostSave)
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
                if (jdsDb.options.writeValuesToEavTables) {
                    saveDateConstructs(entity)
                    saveLocalDates(entity)
                    saveZonedDateTimes(entity)
                    saveLocalTime(entity)
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
                if (jdsDb.options.writeCollectionsToEavTables) {
                    //NOTE arrays have old entries deleted first, for cases where a user reduced the amount of entries in the collection
                    saveDateTimeCollections(entity)
                    saveStringCollections(entity)
                    saveLongCollections(entity)
                    saveDoubleCollections(entity)
                    saveIntegerCollections(entity)
                    saveFloatCollections(entity)
                    saveEnumCollections(entity)
                }
                if (jdsDb.options.writeEntityBindings) {
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

        entities.forEach { entity ->
            if (jdsDb.options.writeOverviewInformation) {
                saveOverview.setString(1, entity.overview.uuid)
                saveOverview.setInt(2, entity.overview.editVersion)
                saveOverview.setLong(3, entity.overview.entityId)
                saveOverview.addBatch()
            }
            if (jdsDb.options.writeLatestEntityVersion) {
                saveLiveVersion.setString(1, entity.overview.uuid)
                saveLiveVersion.addBatch()

                updateLiveVersion.setInt(1, entity.overview.editVersion)
                updateLiveVersion.setString(2, entity.overview.uuid)
                updateLiveVersion.setInt(3, entity.overview.editVersion)
                updateLiveVersion.addBatch()
            }
            if (jdsDb.options.writeToTransposedTables && jdsDb.tables.isNotEmpty())
                processCrt(jdsDb, onSaveEventArguments, entity)
            if (entity is JdsSaveListener)
                entity.onPreSave(preSaveEventArguments)
        }
    } catch (ex: SQLException) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveBlobs(jdsEntity: JdsEntity) = try {
        val saveBlob = regularStatementOrCall(onSaveEventArguments, jdsDb.saveBlob())
        jdsEntity.blobValues.filterSensitiveFields(jdsDb).forEach { (fieldId, writableValue) ->
            saveBlob.setString(1, jdsEntity.overview.uuid)
            saveBlob.setInt(2, jdsEntity.overview.editVersion)
            saveBlob.setLong(3, fieldId)
            saveBlob.setBytes(4, writableValue.value)
            saveBlob.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveBooleans(jdsEntity: JdsEntity) = try {
        val saveBoolean = regularStatementOrCall(onSaveEventArguments, jdsDb.saveBoolean())
        jdsEntity.booleanValues.filterSensitiveFields(jdsDb).forEach { (fieldId, writableValue) ->
            saveBoolean.setString(1, jdsEntity.overview.uuid)
            saveBoolean.setInt(2, jdsEntity.overview.editVersion)
            saveBoolean.setLong(3, fieldId)
            saveBoolean.setObject(4, writableValue.value) //primitives could be null, default value has meaning
            saveBoolean.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    private fun saveIntegers(jdsEntity: JdsEntity) = try {
        val saveInteger = regularStatementOrCall(onSaveEventArguments, jdsDb.saveInteger())
        jdsEntity.integerValues.filterSensitiveFields(jdsDb).forEach { (fieldId, writableValue) ->
            saveInteger.setString(1, jdsEntity.overview.uuid)
            saveInteger.setInt(2, jdsEntity.overview.editVersion)
            saveInteger.setLong(3, fieldId)
            saveInteger.setObject(4, writableValue.value?.toInt()) //primitives could be null, default value has meaning
            saveInteger.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    private fun saveFloats(jdsEntity: JdsEntity) = try {
        val saveFloat = regularStatementOrCall(onSaveEventArguments, jdsDb.saveFloat())
        jdsEntity.floatValues.filterSensitiveFields(jdsDb).forEach { (fieldId, writableValue) ->
            saveFloat.setString(1, jdsEntity.overview.uuid)
            saveFloat.setInt(2, jdsEntity.overview.editVersion)
            saveFloat.setLong(3, fieldId)
            saveFloat.setObject(4, writableValue.value?.toFloat()) //primitives could be null, default value has meaning
            saveFloat.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    private fun saveShorts(jdsEntity: JdsEntity) = try {
        val saveShort = regularStatementOrCall(onSaveEventArguments, jdsDb.saveShort())
        jdsEntity.shortValues.filterSensitiveFields(jdsDb).forEach { (fieldId, writableValue) ->
            saveShort.setString(1, jdsEntity.overview.uuid)
            saveShort.setInt(2, jdsEntity.overview.editVersion)
            saveShort.setLong(3, fieldId)
            saveShort.setObject(4, writableValue.value?.toShort()) //primitives could be null, default value has meaning
            saveShort.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    private fun saveUuids(jdsEntity: JdsEntity) = try {
        val saveUuid = regularStatementOrCall(onSaveEventArguments, jdsDb.saveUuid())
        jdsEntity.uuidValues.filterSensitiveFields(jdsDb).forEach { (fieldId, writableValue) ->
            saveUuid.setString(1, jdsEntity.overview.uuid)
            saveUuid.setInt(2, jdsEntity.overview.editVersion)
            saveUuid.setLong(3, fieldId)
            if ((jdsDb.isOracleDb || jdsDb.isMySqlDb) && writableValue.value != null) {
                saveUuid.setObject(4, writableValue.value.toByteArray())
            } else {
                saveUuid.setObject(4, writableValue.value) //primitives could be null, default value has meaning
            }
            saveUuid.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    private fun saveDoubles(jdsEntity: JdsEntity) = try {
        val saveDouble = regularStatementOrCall(onSaveEventArguments, jdsDb.saveDouble())
        jdsEntity.doubleValues.filterSensitiveFields(jdsDb).forEach { (fieldId, writableValue) ->
            saveDouble.setString(1, jdsEntity.overview.uuid)
            saveDouble.setInt(2, jdsEntity.overview.editVersion)
            saveDouble.setLong(3, fieldId)
            saveDouble.setObject(4, writableValue.value?.toDouble()) //primitives could be null, default value has meaning
            saveDouble.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveLongs(jdsEntity: JdsEntity) = try {
        val saveLong = regularStatementOrCall(onSaveEventArguments, jdsDb.saveLong())
        jdsEntity.longValues.filterSensitiveFields(jdsDb).forEach { (fieldId, writableValue) ->
            saveLong.setString(1, jdsEntity.overview.uuid)
            saveLong.setInt(2, jdsEntity.overview.editVersion)
            saveLong.setLong(3, fieldId)
            saveLong.setObject(4, writableValue.value?.toLong()) //primitives could be null, default value has meaning
            saveLong.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveStrings(jdsEntity: JdsEntity) = try {
        val saveString = regularStatementOrCall(onSaveEventArguments, jdsDb.saveString())
        jdsEntity.stringValues.filterSensitiveFields(jdsDb).forEach { (fieldId, writableValue) ->
            saveString.setString(1, jdsEntity.overview.uuid)
            saveString.setInt(2, jdsEntity.overview.editVersion)
            saveString.setLong(3, fieldId)
            saveString.setString(4, writableValue.value)
            saveString.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveDateConstructs(jdsEntity: JdsEntity) = try {
        val saveMonthDay = regularStatementOrCall(onSaveEventArguments, jdsDb.saveMonthDay())
        val saveYearMonth = regularStatementOrCall(onSaveEventArguments, jdsDb.saveYearMonth())
        val savePeriod = regularStatementOrCall(onSaveEventArguments, jdsDb.savePeriod())
        val saveDuration = regularStatementOrCall(onSaveEventArguments, jdsDb.saveDuration())

        jdsEntity.monthDayValues.filterSensitiveFields(jdsDb).forEach { (fieldId, writableValue) ->
            saveMonthDay.setString(1, jdsEntity.overview.uuid)
            saveMonthDay.setInt(2, jdsEntity.overview.editVersion)
            saveMonthDay.setLong(3, fieldId)
            saveMonthDay.setString(4, writableValue.value.toString())
            saveMonthDay.addBatch()
        }

        jdsEntity.yearMonthValues.filterSensitiveFields(jdsDb).forEach { (fieldId, writableValue) ->
            saveYearMonth.setString(1, jdsEntity.overview.uuid)
            saveYearMonth.setInt(2, jdsEntity.overview.editVersion)
            saveYearMonth.setLong(3, fieldId)
            saveYearMonth.setString(4, writableValue.value.toString())
            saveYearMonth.addBatch()
        }

        jdsEntity.periodValues.filterSensitiveFields(jdsDb).forEach { (fieldId, writableValue) ->
            savePeriod.setString(1, jdsEntity.overview.uuid)
            savePeriod.setInt(2, jdsEntity.overview.editVersion)
            savePeriod.setLong(3, fieldId)
            savePeriod.setString(4, writableValue.value.toString())
            savePeriod.addBatch()
        }

        jdsEntity.durationValues.filterSensitiveFields(jdsDb).forEach { (fieldId, writableValue) ->
            saveDuration.setString(1, jdsEntity.overview.uuid)
            saveDuration.setInt(2, jdsEntity.overview.editVersion)
            saveDuration.setLong(3, fieldId)
            saveDuration.setObject(4, writableValue.value?.toNanos())
            saveDuration.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveLocalDates(jdsEntity: JdsEntity) = try {
        val saveLocalDateTime = regularStatementOrCall(onSaveEventArguments, jdsDb.saveDateTime())
        val saveLocalDate = regularStatementOrCall(onSaveEventArguments, jdsDb.saveDate())
        jdsEntity.localDateTimeValues.filterSensitiveFields(jdsDb).forEach { (fieldId, writableValue) ->
            val localDateTime = writableValue.value as LocalDateTime?
            saveLocalDateTime.setString(1, jdsEntity.overview.uuid)
            saveLocalDateTime.setInt(2, jdsEntity.overview.editVersion)
            saveLocalDateTime.setLong(3, fieldId)
            saveLocalDateTime.setTimestamp(4, if (localDateTime != null) Timestamp.valueOf(localDateTime) else null)
            saveLocalDateTime.addBatch()
        }
        jdsEntity.localDateValues.filterSensitiveFields(jdsDb).forEach { (fieldId, writableValue) ->
            val localDate = writableValue.value as LocalDate?
            saveLocalDate.setString(1, jdsEntity.overview.uuid)
            saveLocalDate.setInt(2, jdsEntity.overview.editVersion)
            saveLocalDate.setLong(3, fieldId)
            saveLocalDate.setLocalDate(4, localDate, jdsDb)
            saveLocalDate.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveLocalTime(jdsEntity: JdsEntity) = try {
        val saveLocalTime = regularStatementOrCall(onSaveEventArguments, jdsDb.saveTime())
        jdsEntity.localTimeValues.filterSensitiveFields(jdsDb).forEach { (fieldId, writableValue) ->
            val localTime = writableValue.value as LocalTime?
            saveLocalTime.setString(1, jdsEntity.overview.uuid)
            saveLocalTime.setInt(2, jdsEntity.overview.editVersion)
            saveLocalTime.setLong(3, fieldId)
            saveLocalTime.setLocalTime(4, localTime, jdsDb)
            saveLocalTime.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveZonedDateTimes(jdsEntity: JdsEntity) = try {
        val saveZonedDateTime = regularStatementOrCall(onSaveEventArguments, jdsDb.saveZonedDateTime())
        jdsEntity.zonedDateTimeValues.filterSensitiveFields(jdsDb).forEach { (fieldId, writableValue) ->
            val zonedDateTime = writableValue.value as ZonedDateTime?
            saveZonedDateTime.setString(1, jdsEntity.overview.uuid)
            saveZonedDateTime.setInt(2, jdsEntity.overview.editVersion)
            saveZonedDateTime.setLong(3, fieldId)
            saveZonedDateTime.setZonedDateTime(4, zonedDateTime, jdsDb)
            saveZonedDateTime.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveEnums(jdsEntity: JdsEntity) = try {
        val saveEnum = regularStatementOrCall(onSaveEventArguments, jdsDb.saveEnum())
        jdsEntity.enumValues.filterSensitiveFields(jdsDb).forEach { (jdsFieldEnum, writableValue) ->
            val value = writableValue.value
            saveEnum.setString(1, jdsEntity.overview.uuid)
            saveEnum.setInt(2, jdsEntity.overview.editVersion)
            saveEnum.setLong(3, jdsFieldEnum)
            saveEnum.setObject(4, value?.ordinal)
            saveEnum.addBatch()
        }
        val saveStringEnum = regularStatementOrCall(onSaveEventArguments, jdsDb.saveEnumString())
        jdsEntity.stringEnumValues.filterSensitiveFields(jdsDb).forEach { (jdsFieldEnum, writableValue) ->
            val value = writableValue.value
            saveStringEnum.setString(1, jdsEntity.overview.uuid)
            saveStringEnum.setInt(2, jdsEntity.overview.editVersion)
            saveStringEnum.setLong(3, jdsFieldEnum)
            saveStringEnum.setString(4, value?.name)
            saveStringEnum.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * Save all dates in one go
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveDateTimeCollections(jdsEntity: JdsEntity) = try {
        val saveDateTimeCollection = regularStatementOrCall(onSaveEventArguments, jdsDb.saveDateTimeCollections())
        jdsEntity.dateTimeCollections.filterSensitiveFields(jdsDb).forEach { (fieldId, collection) ->
            collection.forEach { value ->
                saveDateTimeCollection.setString(1, jdsEntity.overview.uuid)
                saveDateTimeCollection.setInt(2, jdsEntity.overview.editVersion)
                saveDateTimeCollection.setLong(3, fieldId)
                saveDateTimeCollection.setTimestamp(4, Timestamp.valueOf(value))
                saveDateTimeCollection.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveFloatCollections(jdsEntity: JdsEntity) = try {
        val saveFloatCollection = regularStatementOrCall(onSaveEventArguments, jdsDb.saveFloatCollections())
        jdsEntity.floatCollections.filterSensitiveFields(jdsDb).forEach { (fieldId, collection) ->
            collection.forEach { value ->
                saveFloatCollection.setString(1, jdsEntity.overview.uuid)
                saveFloatCollection.setInt(2, jdsEntity.overview.editVersion)
                saveFloatCollection.setLong(3, fieldId)
                saveFloatCollection.setObject(4, value) //primitives could be null, default value has meaning
                saveFloatCollection.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveIntegerCollections(jdsEntity: JdsEntity) = try {
        val saveIntegerCollection = regularStatementOrCall(onSaveEventArguments, jdsDb.saveIntegerCollections())
        jdsEntity.integerCollections.filterSensitiveFields(jdsDb).forEach { (fieldId, collection) ->
            collection.forEach { value ->
                saveIntegerCollection.setString(1, jdsEntity.overview.uuid)
                saveIntegerCollection.setInt(2, jdsEntity.overview.editVersion)
                saveIntegerCollection.setLong(3, fieldId)
                saveIntegerCollection.setObject(4, value) //primitives could be null, default value has meaning
                saveIntegerCollection.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveDoubleCollections(jdsEntity: JdsEntity) = try {
        val saveDoubleCollection = regularStatementOrCall(onSaveEventArguments, jdsDb.saveDoubleCollections())
        jdsEntity.doubleCollections.filterSensitiveFields(jdsDb).forEach { (fieldId, collection) ->
            collection.forEach { value ->
                saveDoubleCollection.setString(1, jdsEntity.overview.uuid)
                saveDoubleCollection.setInt(2, jdsEntity.overview.editVersion)
                saveDoubleCollection.setLong(3, fieldId)
                saveDoubleCollection.setObject(4, value) //primitives could be null, default value has meaning
                saveDoubleCollection.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveLongCollections(jdsEntity: JdsEntity) = try {
        val saveLongCollection = regularStatementOrCall(onSaveEventArguments, jdsDb.saveLongCollections())
        jdsEntity.longCollections.filterSensitiveFields(jdsDb).forEach { (fieldId, collection) ->
            collection.forEach { value ->
                saveLongCollection.setString(1, jdsEntity.overview.uuid)
                saveLongCollection.setInt(2, jdsEntity.overview.editVersion)
                saveLongCollection.setLong(3, fieldId)
                saveLongCollection.setObject(4, value) //primitives could be null, default value has meaning
                saveLongCollection.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveStringCollections(jdsEntity: JdsEntity) = try {
        val saveStringCollection = regularStatementOrCall(onSaveEventArguments, jdsDb.saveStringCollections())
        jdsEntity.stringCollections.filterSensitiveFields(jdsDb).forEach { (fieldId, collection) ->
            collection.forEach { value ->
                saveStringCollection.setString(1, jdsEntity.overview.uuid)
                saveStringCollection.setInt(2, jdsEntity.overview.editVersion)
                saveStringCollection.setLong(3, fieldId)
                saveStringCollection.setString(4, value)
                saveStringCollection.addBatch()
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
        val saveEnumCollection = regularStatementOrCall(onSaveEventArguments, jdsDb.saveEnumCollections())
        jdsEntity.enumCollections.filterSensitiveFields(jdsDb).forEach { (jdsFieldEnum, collection) ->
            collection.forEach { anEnum ->
                saveEnumCollection.setString(1, jdsEntity.overview.uuid)
                saveEnumCollection.setInt(2, jdsEntity.overview.editVersion)
                saveEnumCollection.setLong(3, jdsFieldEnum)
                saveEnumCollection.setObject(4, anEnum.ordinal)
                saveEnumCollection.addBatch()
            }
        }
        val saveStringEnumCollection = regularStatementOrCall(onSaveEventArguments, jdsDb.saveEnumStringCollections())
        jdsEntity.enumStringCollections.filterSensitiveFields(jdsDb).forEach { (jdsFieldEnum, collection) ->
            collection.forEach { anEnum ->
                saveStringEnumCollection.setString(1, jdsEntity.overview.uuid)
                saveStringEnumCollection.setInt(2, jdsEntity.overview.editVersion)
                saveStringEnumCollection.setLong(3, jdsFieldEnum)
                saveStringEnumCollection.setString(4, anEnum.name)
                saveStringEnumCollection.addBatch()
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
        val saveObjectArrayBinding = regularStatementOrCall(onSaveEventArguments, jdsDb.saveEntityBindings())
        parentEntity.objectCollections.forEach { (fieldEntity, entityCollection) ->
            entityCollection.forEach {
                saveObjectArrayBinding.setString(1, parentEntity.overview.uuid)
                saveObjectArrayBinding.setInt(2, parentEntity.overview.editVersion)
                saveObjectArrayBinding.setString(3, it.overview.uuid)
                saveObjectArrayBinding.setInt(4, it.overview.editVersion)
                saveObjectArrayBinding.setLong(5, fieldEntity.field.id)
                saveObjectArrayBinding.addBatch()
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
        val saveObjectBinding = regularStatementOrCall(onSaveEventArguments, jdsDb.saveEntityBindings())
        parentEntity.objectValues.forEach { (fieldEntity, v) ->
            saveObjectBinding.setString(1, parentEntity.overview.uuid)
            saveObjectBinding.setInt(2, parentEntity.overview.editVersion)
            saveObjectBinding.setString(3, v.value.overview.uuid)
            saveObjectBinding.setInt(4, v.value.overview.editVersion)
            saveObjectBinding.setLong(5, fieldEntity.field.id)
            saveObjectBinding.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }
}