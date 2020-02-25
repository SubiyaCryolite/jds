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

import io.github.subiyacryolite.jds.context.DbContext
import io.github.subiyacryolite.jds.events.EventArguments
import io.github.subiyacryolite.jds.events.SaveEvent
import io.github.subiyacryolite.jds.events.SaveListener
import io.github.subiyacryolite.jds.extensions.*
import java.sql.Connection
import java.sql.SQLException
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.concurrent.Callable

/**
 * This class is responsible for persisting on or more [JdsEntities][Entity]
 */
class Save(
        private val dbContext: DbContext,
        private val entities: Iterable<Entity>,
        private val postSaveEvent: SaveEvent? = null
) : Callable<Boolean> {

    private lateinit var preSaveEventArguments: EventArguments
    private lateinit var onSaveEventArguments: EventArguments
    private lateinit var postSaveEventArguments: EventArguments

    constructor(dbContext: DbContext, entities: Array<Entity>, postSaveEvent: SaveEvent? = null) : this(dbContext, entities.asIterable(), postSaveEvent)

    constructor(dbContext: DbContext, entity: Entity, postSaveEvent: SaveEvent? = null) : this(dbContext, listOf(entity), postSaveEvent)

    /**
     * Computes a result, or throws an exception if unable to do so.
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    override fun call(): Boolean {
        try {
            val chunks = entities.chunked(512)
            val totalChunks = chunks.count()
            dbContext.dataSource.connection.use { connection ->
                chunks.forEachIndexed { index, batch ->
                    try {
                        val orderedList = sequence { batch.forEach { yieldAll(it.getNestedEntities()) } }
                        saveInner(orderedList.asIterable(), connection)
                        if (dbContext.options.logOutput)
                            println("Processing saves. Batch ${index + 1} of $totalChunks")
                    } catch (ex: Exception) {
                        ex.printStackTrace(System.err)
                    }
                }
                if (dbContext.options.deleteOutdatedTransposeDataPostSave)
                    dbContext.deleteOldDataFromReportTables(connection)
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
    private fun saveInner(entities: Iterable<Entity>, connection: Connection) {
        try {
            preSaveEventArguments = EventArguments(connection)
            onSaveEventArguments = EventArguments(connection)
            postSaveEventArguments = EventArguments(connection)

            //ensure that overviews are submitted before handing over to listeners
            saveOverview(entities)
            entities.forEach { entity ->
                if (dbContext.options.writeValuesToEavTables) {
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
                if (dbContext.options.writeCollectionsToEavTables) {
                    //NOTE arrays have old entries deleted first, for cases where a user reduced the amount of entries in the collection
                    saveDateTimeCollections(entity)
                    saveStringCollections(entity)
                    saveLongCollections(entity)
                    saveDoubleCollections(entity)
                    saveIntegerCollections(entity)
                    saveUuidCollections(entity)
                    saveFloatCollections(entity)
                    saveEnumCollections(entity)
                }
                if (dbContext.options.writeEntityBindings) {
                    saveObjectBindings(entity)
                    saveObjectArrayBindings(entity)
                }
                if (entity is SaveListener)
                    entity.onPostSave(postSaveEventArguments)
                postSaveEvent?.onSave(entity, preSaveEventArguments, postSaveEventArguments, connection)
            }
            preSaveEventArguments.use { eventArguments ->
                eventArguments.execute()
            }
            onSaveEventArguments.use { eventArguments ->
                eventArguments.execute()
            }
            postSaveEventArguments.use { eventArguments ->
                eventArguments.execute()
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    /**
     * @param dbContext
     * @param saveEventArguments
     * @param entity
     */
    private fun processCrt(dbContext: DbContext, saveEventArguments: EventArguments, entity: Entity) {
        dbContext.tables.forEach {
            it.executeSave(dbContext, entity, saveEventArguments)
        }
    }

    private fun regularStatement(eventArguments: EventArguments, sql: String) = eventArguments.getOrAddStatement(sql)

    private fun regularStatementOrCall(eventArguments: EventArguments, sql: String) = if (dbContext.supportsStatements) eventArguments.getOrAddCall(sql) else regularStatement(eventArguments, sql)

    /**
     * @param entities
     */
    @Throws(Exception::class)
    private fun saveOverview(entities: Iterable<Entity>) = try {

        val saveOverview = regularStatementOrCall(onSaveEventArguments, dbContext.populateEntityOverview())
        val saveLiveVersion = regularStatementOrCall(onSaveEventArguments, dbContext.populateEntityLive())
        val updateLiveVersion = regularStatement(onSaveEventArguments, "UPDATE ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.EntityLive)} SET edit_version = ? WHERE id = ? AND (edit_version < ? OR edit_version IS NULL)")

        entities.forEach { entity ->
            if (dbContext.options.writeOverviewInformation) {
                saveOverview.setString(1, entity.overview.id)
                saveOverview.setInt(2, entity.overview.editVersion)
                saveOverview.setInt(3, entity.overview.entityId)
                saveOverview.addBatch()
            }
            if (dbContext.options.writeLatestEntityVersion) {
                saveLiveVersion.setString(1, entity.overview.id)
                saveLiveVersion.addBatch()

                updateLiveVersion.setInt(1, entity.overview.editVersion)
                updateLiveVersion.setString(2, entity.overview.id)
                updateLiveVersion.setInt(3, entity.overview.editVersion)
                updateLiveVersion.addBatch()
            }
            if (dbContext.options.writeToTransposedTables && dbContext.tables.isNotEmpty())
                processCrt(dbContext, onSaveEventArguments, entity)
            if (entity is SaveListener)
                entity.onPreSave(preSaveEventArguments)
        }
    } catch (ex: SQLException) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveBlobs(jdsEntity: Entity) = try {
        val saveBlob = regularStatementOrCall(onSaveEventArguments, dbContext.populateStoreBlob())
        jdsEntity.blobValues.filterIgnored(dbContext).forEach { (fieldId, writableValue) ->
            saveBlob.setString(1, jdsEntity.overview.id)
            saveBlob.setInt(2, jdsEntity.overview.editVersion)
            saveBlob.setInt(3, fieldId)
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
    private fun saveBooleans(jdsEntity: Entity) = try {
        val saveBoolean = regularStatementOrCall(onSaveEventArguments, dbContext.populateStoreBoolean())
        jdsEntity.booleanValues.filterIgnored(dbContext).forEach { (fieldId, writableValue) ->
            saveBoolean.setString(1, jdsEntity.overview.id)
            saveBoolean.setInt(2, jdsEntity.overview.editVersion)
            saveBoolean.setInt(3, fieldId)
            saveBoolean.setObject(4, writableValue.value) //primitives could be null, default value has meaning
            saveBoolean.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    private fun saveIntegers(jdsEntity: Entity) = try {
        val saveInteger = regularStatementOrCall(onSaveEventArguments, dbContext.populateStoreInteger())
        jdsEntity.integerValues.filterIgnored(dbContext).forEach { (fieldId, writableValue) ->
            saveInteger.setString(1, jdsEntity.overview.id)
            saveInteger.setInt(2, jdsEntity.overview.editVersion)
            saveInteger.setInt(3, fieldId)
            saveInteger.setObject(4, writableValue.value?.toInt()) //primitives could be null, default value has meaning
            saveInteger.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    private fun saveFloats(jdsEntity: Entity) = try {
        val saveFloat = regularStatementOrCall(onSaveEventArguments, dbContext.populateStoreFloat())
        jdsEntity.floatValues.filterIgnored(dbContext).forEach { (fieldId, writableValue) ->
            saveFloat.setString(1, jdsEntity.overview.id)
            saveFloat.setInt(2, jdsEntity.overview.editVersion)
            saveFloat.setInt(3, fieldId)
            saveFloat.setObject(4, writableValue.value?.toFloat()) //primitives could be null, default value has meaning
            saveFloat.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    private fun saveShorts(jdsEntity: Entity) = try {
        val saveShort = regularStatementOrCall(onSaveEventArguments, dbContext.populateStoreShort())
        jdsEntity.shortValues.filterIgnored(dbContext).forEach { (fieldId, writableValue) ->
            saveShort.setString(1, jdsEntity.overview.id)
            saveShort.setInt(2, jdsEntity.overview.editVersion)
            saveShort.setInt(3, fieldId)
            saveShort.setObject(4, writableValue.value?.toShort()) //primitives could be null, default value has meaning
            saveShort.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    private fun saveUuids(jdsEntity: Entity) = try {
        val saveUuid = regularStatementOrCall(onSaveEventArguments, dbContext.populateStoreUuid())
        jdsEntity.uuidValues.filterIgnored(dbContext).forEach { (fieldId, writableValue) ->
            saveUuid.setString(1, jdsEntity.overview.id)
            saveUuid.setInt(2, jdsEntity.overview.editVersion)
            saveUuid.setInt(3, fieldId)
            if ((dbContext.isOracleDb || dbContext.isMySqlDb) && writableValue.value != null) {
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
    private fun saveDoubles(jdsEntity: Entity) = try {
        val saveDouble = regularStatementOrCall(onSaveEventArguments, dbContext.populateStoreDouble())
        jdsEntity.doubleValues.filterIgnored(dbContext).forEach { (fieldId, writableValue) ->
            saveDouble.setString(1, jdsEntity.overview.id)
            saveDouble.setInt(2, jdsEntity.overview.editVersion)
            saveDouble.setInt(3, fieldId)
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
    private fun saveLongs(jdsEntity: Entity) = try {
        val saveLong = regularStatementOrCall(onSaveEventArguments, dbContext.populateStoreLong())
        jdsEntity.longValues.filterIgnored(dbContext).forEach { (fieldId, writableValue) ->
            saveLong.setString(1, jdsEntity.overview.id)
            saveLong.setInt(2, jdsEntity.overview.editVersion)
            saveLong.setInt(3, fieldId)
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
    private fun saveStrings(jdsEntity: Entity) = try {
        val saveString = regularStatementOrCall(onSaveEventArguments, dbContext.populateStoreText())
        jdsEntity.stringValues.filterIgnored(dbContext).forEach { (fieldId, writableValue) ->
            saveString.setString(1, jdsEntity.overview.id)
            saveString.setInt(2, jdsEntity.overview.editVersion)
            saveString.setInt(3, fieldId)
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
    private fun saveDateConstructs(jdsEntity: Entity) = try {
        val saveMonthDay = regularStatementOrCall(onSaveEventArguments, dbContext.populateStoreMonthDay())
        val saveYearMonth = regularStatementOrCall(onSaveEventArguments, dbContext.populateStoreYearMonth())
        val savePeriod = regularStatementOrCall(onSaveEventArguments, dbContext.populateStorePeriod())
        val saveDuration = regularStatementOrCall(onSaveEventArguments, dbContext.populateStoreDuration())

        jdsEntity.monthDayValues.filterIgnored(dbContext).forEach { (fieldId, writableValue) ->
            saveMonthDay.setString(1, jdsEntity.overview.id)
            saveMonthDay.setInt(2, jdsEntity.overview.editVersion)
            saveMonthDay.setInt(3, fieldId)
            saveMonthDay.setString(4, writableValue.value.toString())
            saveMonthDay.addBatch()
        }

        jdsEntity.yearMonthValues.filterIgnored(dbContext).forEach { (fieldId, writableValue) ->
            saveYearMonth.setString(1, jdsEntity.overview.id)
            saveYearMonth.setInt(2, jdsEntity.overview.editVersion)
            saveYearMonth.setInt(3, fieldId)
            saveYearMonth.setString(4, writableValue.value.toString())
            saveYearMonth.addBatch()
        }

        jdsEntity.periodValues.filterIgnored(dbContext).forEach { (fieldId, writableValue) ->
            savePeriod.setString(1, jdsEntity.overview.id)
            savePeriod.setInt(2, jdsEntity.overview.editVersion)
            savePeriod.setInt(3, fieldId)
            savePeriod.setString(4, writableValue.value.toString())
            savePeriod.addBatch()
        }

        jdsEntity.durationValues.filterIgnored(dbContext).forEach { (fieldId, writableValue) ->
            saveDuration.setString(1, jdsEntity.overview.id)
            saveDuration.setInt(2, jdsEntity.overview.editVersion)
            saveDuration.setInt(3, fieldId)
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
    private fun saveLocalDates(jdsEntity: Entity) = try {
        val saveLocalDateTime = regularStatementOrCall(onSaveEventArguments, dbContext.populateStoreDateTime())
        val saveLocalDate = regularStatementOrCall(onSaveEventArguments, dbContext.populateStoreDate())
        jdsEntity.localDateTimeValues.filterIgnored(dbContext).forEach { (fieldId, writableValue) ->
            val localDateTime = writableValue.value as LocalDateTime?
            saveLocalDateTime.setString(1, jdsEntity.overview.id)
            saveLocalDateTime.setInt(2, jdsEntity.overview.editVersion)
            saveLocalDateTime.setInt(3, fieldId)
            saveLocalDateTime.setTimestamp(4, if (localDateTime != null) Timestamp.valueOf(localDateTime) else null)
            saveLocalDateTime.addBatch()
        }
        jdsEntity.localDateValues.filterIgnored(dbContext).forEach { (fieldId, writableValue) ->
            val localDate = writableValue.value as LocalDate?
            saveLocalDate.setString(1, jdsEntity.overview.id)
            saveLocalDate.setInt(2, jdsEntity.overview.editVersion)
            saveLocalDate.setInt(3, fieldId)
            saveLocalDate.setLocalDate(4, localDate, dbContext)
            saveLocalDate.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveLocalTime(jdsEntity: Entity) = try {
        val saveLocalTime = regularStatementOrCall(onSaveEventArguments, dbContext.populateStoreTime())
        jdsEntity.localTimeValues.filterIgnored(dbContext).forEach { (fieldId, writableValue) ->
            val localTime = writableValue.value as LocalTime?
            saveLocalTime.setString(1, jdsEntity.overview.id)
            saveLocalTime.setInt(2, jdsEntity.overview.editVersion)
            saveLocalTime.setInt(3, fieldId)
            saveLocalTime.setLocalTime(4, localTime, dbContext)
            saveLocalTime.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveZonedDateTimes(jdsEntity: Entity) = try {
        val saveZonedDateTime = regularStatementOrCall(onSaveEventArguments, dbContext.populateStoreZonedDateTime())
        jdsEntity.zonedDateTimeValues.filterIgnored(dbContext).forEach { (fieldId, writableValue) ->
            val zonedDateTime = writableValue.value as ZonedDateTime?
            saveZonedDateTime.setString(1, jdsEntity.overview.id)
            saveZonedDateTime.setInt(2, jdsEntity.overview.editVersion)
            saveZonedDateTime.setInt(3, fieldId)
            saveZonedDateTime.setZonedDateTime(4, zonedDateTime, dbContext)
            saveZonedDateTime.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveEnums(jdsEntity: Entity) = try {
        val saveEnum = regularStatementOrCall(onSaveEventArguments, dbContext.populateStoreEnum())
        jdsEntity.enumValues.filterIgnored(dbContext).forEach { (jdsFieldEnum, writableValue) ->
            val value = writableValue.value
            saveEnum.setString(1, jdsEntity.overview.id)
            saveEnum.setInt(2, jdsEntity.overview.editVersion)
            saveEnum.setInt(3, jdsFieldEnum)
            saveEnum.setObject(4, value?.ordinal)
            saveEnum.addBatch()
        }
        val saveStringEnum = regularStatementOrCall(onSaveEventArguments, dbContext.populateStoreEnumString())
        jdsEntity.stringEnumValues.filterIgnored(dbContext).forEach { (jdsFieldEnum, writableValue) ->
            val value = writableValue.value
            saveStringEnum.setString(1, jdsEntity.overview.id)
            saveStringEnum.setInt(2, jdsEntity.overview.editVersion)
            saveStringEnum.setInt(3, jdsFieldEnum)
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
    private fun saveDateTimeCollections(jdsEntity: Entity) = try {
        val saveDateTimeCollection = regularStatementOrCall(onSaveEventArguments, dbContext.populateStoreDateTimeCollection())
        jdsEntity.dateTimeCollections.filterIgnored(dbContext).forEach { (fieldId, collection) ->
            collection.forEach { value ->
                saveDateTimeCollection.setString(1, jdsEntity.overview.id)
                saveDateTimeCollection.setInt(2, jdsEntity.overview.editVersion)
                saveDateTimeCollection.setInt(3, fieldId)
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
    private fun saveFloatCollections(jdsEntity: Entity) = try {
        val saveFloatCollection = regularStatementOrCall(onSaveEventArguments, dbContext.populateStoreFloatCollection())
        jdsEntity.floatCollections.filterIgnored(dbContext).forEach { (fieldId, collection) ->
            collection.forEach { value ->
                saveFloatCollection.setString(1, jdsEntity.overview.id)
                saveFloatCollection.setInt(2, jdsEntity.overview.editVersion)
                saveFloatCollection.setInt(3, fieldId)
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
    private fun saveIntegerCollections(jdsEntity: Entity) = try {
        val saveIntegerCollection = regularStatementOrCall(onSaveEventArguments, dbContext.populateStoreIntegerCollection())
        jdsEntity.integerCollections.filterIgnored(dbContext).forEach { (fieldId, collection) ->
            collection.forEach { value ->
                saveIntegerCollection.setString(1, jdsEntity.overview.id)
                saveIntegerCollection.setInt(2, jdsEntity.overview.editVersion)
                saveIntegerCollection.setInt(3, fieldId)
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
    private fun saveUuidCollections(jdsEntity: Entity) = try {
        val saveUuidCollection = regularStatementOrCall(onSaveEventArguments, dbContext.populateStoreUuidCollection())
        jdsEntity.uuidCollections.filterIgnored(dbContext).forEach { (fieldId, collection) ->
            collection.forEach { value ->
                saveUuidCollection.setString(1, jdsEntity.overview.id)
                saveUuidCollection.setInt(2, jdsEntity.overview.editVersion)
                saveUuidCollection.setInt(3, fieldId)
                if ((dbContext.isOracleDb || dbContext.isMySqlDb)) {
                    saveUuidCollection.setObject(4, value.toByteArray())
                } else {
                    saveUuidCollection.setObject(4, value)
                }
                saveUuidCollection.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param jdsEntity
     */
    @Throws(Exception::class)
    private fun saveDoubleCollections(jdsEntity: Entity) = try {
        val saveDoubleCollection = regularStatementOrCall(onSaveEventArguments, dbContext.populateStoreDoubleCollection())
        jdsEntity.doubleCollections.filterIgnored(dbContext).forEach { (fieldId, collection) ->
            collection.forEach { value ->
                saveDoubleCollection.setString(1, jdsEntity.overview.id)
                saveDoubleCollection.setInt(2, jdsEntity.overview.editVersion)
                saveDoubleCollection.setInt(3, fieldId)
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
    private fun saveLongCollections(jdsEntity: Entity) = try {
        val saveLongCollection = regularStatementOrCall(onSaveEventArguments, dbContext.populateStoreLongCollection())
        jdsEntity.longCollections.filterIgnored(dbContext).forEach { (fieldId, collection) ->
            collection.forEach { value ->
                saveLongCollection.setString(1, jdsEntity.overview.id)
                saveLongCollection.setInt(2, jdsEntity.overview.editVersion)
                saveLongCollection.setInt(3, fieldId)
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
    private fun saveStringCollections(jdsEntity: Entity) = try {
        val saveStringCollection = regularStatementOrCall(onSaveEventArguments, dbContext.populateStoreTextCollection())
        jdsEntity.stringCollections.filterIgnored(dbContext).forEach { (fieldId, collection) ->
            collection.forEach { value ->
                saveStringCollection.setString(1, jdsEntity.overview.id)
                saveStringCollection.setInt(2, jdsEntity.overview.editVersion)
                saveStringCollection.setInt(3, fieldId)
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
    private fun saveEnumCollections(jdsEntity: Entity) = try {
        val saveEnumCollection = regularStatementOrCall(onSaveEventArguments, dbContext.populateStoreEnumCollection())
        jdsEntity.enumCollections.filterIgnored(dbContext).forEach { (jdsFieldEnum, collection) ->
            collection.forEach { anEnum ->
                saveEnumCollection.setString(1, jdsEntity.overview.id)
                saveEnumCollection.setInt(2, jdsEntity.overview.editVersion)
                saveEnumCollection.setInt(3, jdsFieldEnum)
                saveEnumCollection.setObject(4, anEnum.ordinal)
                saveEnumCollection.addBatch()
            }
        }
        val saveStringEnumCollection = regularStatementOrCall(onSaveEventArguments, dbContext.populateStoreEnumStringCollection())
        jdsEntity.enumStringCollections.filterIgnored(dbContext).forEach { (jdsFieldEnum, collection) ->
            collection.forEach { anEnum ->
                saveStringEnumCollection.setString(1, jdsEntity.overview.id)
                saveStringEnumCollection.setInt(2, jdsEntity.overview.editVersion)
                saveStringEnumCollection.setInt(3, jdsFieldEnum)
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
    private fun saveObjectArrayBindings(parentEntity: Entity) = try {
        val saveObjectArrayBinding = regularStatementOrCall(onSaveEventArguments, dbContext.populateEntityBinding())
        parentEntity.objectCollections.forEach { (fieldEntity, entityCollection) ->
            entityCollection.forEach {
                saveObjectArrayBinding.setString(1, parentEntity.overview.id)
                saveObjectArrayBinding.setInt(2, parentEntity.overview.editVersion)
                saveObjectArrayBinding.setString(3, it.overview.id)
                saveObjectArrayBinding.setInt(4, it.overview.editVersion)
                saveObjectArrayBinding.setInt(5, fieldEntity.field.id)
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
    private fun saveObjectBindings(parentEntity: Entity) = try {
        val saveObjectBinding = regularStatementOrCall(onSaveEventArguments, dbContext.populateEntityBinding())
        parentEntity.objectValues.forEach { (fieldEntity, v) ->
            saveObjectBinding.setString(1, parentEntity.overview.id)
            saveObjectBinding.setInt(2, parentEntity.overview.editVersion)
            saveObjectBinding.setString(3, v.value.overview.id)
            saveObjectBinding.setInt(4, v.value.overview.editVersion)
            saveObjectBinding.setInt(5, fieldEntity.field.id)
            saveObjectBinding.addBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }
}