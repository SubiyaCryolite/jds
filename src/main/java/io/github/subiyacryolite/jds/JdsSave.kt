/*
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

import io.github.subiyacryolite.jds.events.JdsSaveListener
import io.github.subiyacryolite.jds.events.OnPostSaveEventArguments
import io.github.subiyacryolite.jds.events.OnPreSaveEventArguments
import javafx.beans.property.*
import java.sql.Connection
import java.sql.SQLException
import java.sql.Timestamp
import java.time.*
import java.time.temporal.Temporal
import java.util.*
import java.util.concurrent.Callable

/**
 * This class is responsible for persisting on or more [JdsEntities][JdsEntity]
 */
class JdsSave private constructor(private val jdsDb: JdsDb, private val connection: Connection, private val batchSize: Int, private val entities: Collection<JdsEntity>, private val recursiveInnerCall: Boolean, private val onPreSaveEventArguments: OnPreSaveEventArguments = OnPreSaveEventArguments(connection), private val onPostSaveEventArguments: OnPostSaveEventArguments = OnPostSaveEventArguments(connection)) : Callable<Boolean> {

    /**
     * @param jdsDb
     * @param entities
     */
    @Throws(SQLException::class, ClassNotFoundException::class)
    constructor(jdsDb: JdsDb, entities: Collection<JdsEntity>) : this(jdsDb, jdsDb.getConnection(), 0, entities, false) {
    }

    /**
     * @param jdsDb
     * @param batchSize
     * @param entities
     */
    @Throws(SQLException::class, ClassNotFoundException::class)
    constructor(jdsDb: JdsDb, batchSize: Int, entities: Collection<JdsEntity>) : this(jdsDb, jdsDb.getConnection(), batchSize, entities, false) {
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Throws(Exception::class)
    override fun call(): Boolean? {
        val saveContainer = JdsSaveContainer()
        val batchEntities = ArrayList<MutableCollection<JdsEntity>>()
        setupBatches(batchSize, entities, saveContainer, batchEntities)
        var step = 0
        val steps = batchEntities.size
        for (current in batchEntities) {
            saveInner(current, saveContainer, step, steps)
            step++
            if (jdsDb.isPrintingOutput)
                System.out.printf("Processed batch [%s of %s]\n", step, steps + 1)
        }
        return true
    }

    /**
     * @param batchSize
     * @param entities
     * @param container
     * @param batchEntities
     */
    private fun setupBatches(batchSize: Int, entities: Collection<JdsEntity>, container: JdsSaveContainer, batchEntities: MutableList<MutableCollection<JdsEntity>>) {
        //create batches
        var currentBatch = 0
        //default bach is 0 or -1 which means one large chunk. Anything above is a single batch
        var iteration = 0
        if (batchSize > 0) {
            for (jdsEntity in entities) {
                if (iteration == batchSize) {
                    currentBatch++
                    iteration = 0
                }
                if (iteration == 0) {
                    createBatchCollection(container, batchEntities)
                }
                batchEntities[currentBatch].add(jdsEntity)
                iteration++
            }
        } else {
            //single large batch, good luck
            createBatchCollection(container, batchEntities)
            for (jdsEntity in entities) {
                batchEntities[0].add(jdsEntity)
            }
        }
    }

    /**
     * @param saveContainer
     * @param batchEntities
     */
    private fun createBatchCollection(saveContainer: JdsSaveContainer, batchEntities: MutableList<MutableCollection<JdsEntity>>) {
        batchEntities.add(ArrayList())
        saveContainer.overviews.add(HashSet<IJdsOverview>())
        //time constructs
        saveContainer.localDateTimeProperties.add(HashMap<String, HashMap<Long, SimpleObjectProperty<Temporal>>>())
        saveContainer.zonedDateTimeProperties.add(HashMap<String, HashMap<Long, SimpleObjectProperty<Temporal>>>())
        saveContainer.localTimeProperties.add(HashMap<String, HashMap<Long, SimpleObjectProperty<Temporal>>>())
        saveContainer.localDateProperties.add(HashMap<String, HashMap<Long, SimpleObjectProperty<Temporal>>>())
        saveContainer.monthDayProperties.add(HashMap<String, HashMap<Long, SimpleObjectProperty<MonthDay>>>())
        saveContainer.yearMonthProperties.add(HashMap<String, HashMap<Long, SimpleObjectProperty<Temporal>>>())
        saveContainer.periodProperties.add(HashMap<String, HashMap<Long, SimpleObjectProperty<Period>>>())
        saveContainer.durationProperties.add(HashMap<String, HashMap<Long, SimpleObjectProperty<Duration>>>())
        //primitives
        saveContainer.booleanProperties.add(HashMap<String, HashMap<Long, SimpleBooleanProperty>>())
        saveContainer.floatProperties.add(HashMap<String, HashMap<Long, SimpleFloatProperty>>())
        saveContainer.doubleProperties.add(HashMap<String, HashMap<Long, SimpleDoubleProperty>>())
        saveContainer.longProperties.add(HashMap<String, HashMap<Long, SimpleLongProperty>>())
        saveContainer.integerProperties.add(HashMap<String, HashMap<Long, SimpleIntegerProperty>>())
        //string
        saveContainer.stringProperties.add(HashMap<String, HashMap<Long, SimpleStringProperty>>())
        //blob
        saveContainer.blobProperties.add(HashMap<String, HashMap<Long, SimpleBlobProperty>>())
        //arrays
        saveContainer.stringCollections.add(HashMap<String, HashMap<Long, SimpleListProperty<String>>>())
        saveContainer.localDateTimeCollections.add(HashMap<String, HashMap<Long, SimpleListProperty<LocalDateTime>>>())
        saveContainer.floatCollections.add(HashMap<String, HashMap<Long, SimpleListProperty<Float>>>())
        saveContainer.doubleCollections.add(HashMap<String, HashMap<Long, SimpleListProperty<Double>>>())
        saveContainer.longCollections.add(HashMap<String, HashMap<Long, SimpleListProperty<Long>>>())
        saveContainer.integerCollections.add(HashMap<String, HashMap<Long, SimpleListProperty<Int>>>())
        //enumProperties
        saveContainer.enumProperties.add(HashMap<String, HashMap<JdsFieldEnum<*>, SimpleObjectProperty<Enum<*>>>>())
        saveContainer.enumCollections.add(HashMap<String, HashMap<JdsFieldEnum<*>, SimpleListProperty<Enum<*>>>>())
        //objects
        saveContainer.objects.add(HashMap<String, HashMap<JdsFieldEntity<*>, SimpleObjectProperty<JdsEntity>>>())
        //object arrays
        saveContainer.objectCollections.add(HashMap<String, HashMap<JdsFieldEntity<*>, SimpleListProperty<JdsEntity>>>())
    }

    /**
     * @param entities
     * @param saveContainer
     * @param step
     */
    @Throws(Exception::class)
    private fun saveInner(entities: Collection<JdsEntity>, saveContainer: JdsSaveContainer, step: Int, steps: Int) {
        //fire
        for ((sequence, entity) in entities.withIndex()) {
            //update the modified date to time of commit
            entity.overview.dateModified = LocalDateTime.now()
            saveContainer.overviews[step].add(entity.overview)
            //assign properties
            entity.assign(step, saveContainer)
        }
        //share one connection for raw saves, helps with performance
        val finalStep = !recursiveInnerCall && step == steps - 1
        try {
            val writeToPrimaryDataTables = jdsDb.isWritingToPrimaryDataTables
            //always save overviews
            saveOverviews(saveContainer.overviews[step])
            //ensure that overviews are submitted before handing over to listeners

            entities.filterIsInstance<JdsSaveListener>().forEach { it.onPreSave(onPreSaveEventArguments) }

            //time constraints
            saveDateConstructs(writeToPrimaryDataTables,
                    saveContainer.monthDayProperties[step],
                    saveContainer.yearMonthProperties[step],
                    saveContainer.periodProperties[step],
                    saveContainer.durationProperties[step])
            saveDatesAndDateTimes(writeToPrimaryDataTables, saveContainer.localDateTimeProperties[step], saveContainer.localDateProperties[step])
            saveZonedDateTimes(writeToPrimaryDataTables, saveContainer.zonedDateTimeProperties[step])
            saveTimes(writeToPrimaryDataTables, saveContainer.localTimeProperties[step])
            //primitives
            saveBooleans(writeToPrimaryDataTables, saveContainer.booleanProperties[step])
            saveLongs(writeToPrimaryDataTables, saveContainer.longProperties[step])
            saveDoubles(writeToPrimaryDataTables, saveContainer.doubleProperties[step])
            saveIntegers(writeToPrimaryDataTables, saveContainer.integerProperties[step])
            saveFloats(writeToPrimaryDataTables, saveContainer.floatProperties[step])
            //strings
            saveStrings(writeToPrimaryDataTables, saveContainer.stringProperties[step])
            //blobs
            saveBlobs(writeToPrimaryDataTables, saveContainer.blobProperties[step])
            //array properties [NOTE arrays have old entries deleted first, for cases where a user reduced the amount of entries in the collection]
            saveArrayDates(writeToPrimaryDataTables, saveContainer.localDateTimeCollections[step])
            saveArrayStrings(writeToPrimaryDataTables, saveContainer.stringCollections[step])
            saveArrayLongs(writeToPrimaryDataTables, saveContainer.longCollections[step])
            saveArrayDoubles(writeToPrimaryDataTables, saveContainer.doubleCollections[step])
            saveArrayIntegers(writeToPrimaryDataTables, saveContainer.integerCollections[step])
            saveArrayFloats(writeToPrimaryDataTables, saveContainer.floatCollections[step])
            //enumProperties
            saveEnums(writeToPrimaryDataTables, saveContainer.enumProperties[step])
            saveEnumCollections(writeToPrimaryDataTables, saveContainer.enumCollections[step])
            //objects and object arrays
            //object entity overviews and entity bindings are ALWAYS persisted
            saveAndBindObjects(connection, saveContainer.objects[step])
            saveAndBindObjectArrays(connection, saveContainer.objectCollections[step])

            entities.filterIsInstance<JdsSaveListener>().forEach { it.onPostSave(onPostSaveEventArguments) }

            //respect execution sequence
            //respect JDS batches in each call
            onPreSaveEventArguments.executeBatches()
            onPostSaveEventArguments.executeBatches()
        } catch (ex: Exception) {
            throw ex
        } finally {
            if (finalStep) {
                onPreSaveEventArguments.closeBatches()
                onPostSaveEventArguments.closeBatches()
                connection.close()
            }
        }
    }

    /**
     * @param overviews
     */
    @Throws(SQLException::class)
    private fun saveOverviews(overviews: HashSet<IJdsOverview>) {
        var record = 0
        val recordTotal = overviews.size
        try {
            val upsert = if (jdsDb.supportsStatements()) onPreSaveEventArguments.getOrAddNamedCall(jdsDb.saveOverview()) else onPreSaveEventArguments.getOrAddNamedStatement(jdsDb.saveOverview())
            val inheritance = if (jdsDb.supportsStatements()) onPreSaveEventArguments.getOrAddNamedCall(jdsDb.saveOverviewInheritance()) else onPreSaveEventArguments.getOrAddNamedStatement(jdsDb.saveOverviewInheritance())
            for (overview in overviews) {
                record++
                //Entity Overview
                upsert.setString("entityGuid", overview.entityGuid)
                upsert.setTimestamp("dateCreated", Timestamp.valueOf(overview.dateCreated))
                upsert.setTimestamp("dateModified", Timestamp.valueOf(LocalDateTime.now())) //always update date modified!!!
                upsert.setBoolean("live", overview.live)
                upsert.setLong("version", overview.version) //always update date modified!!!
                upsert.addBatch()
                //Entity Inheritance
                inheritance.setString("entityGuid", overview.entityGuid)
                inheritance.setLong("entityId", overview.entityId)
                inheritance.addBatch()
                if (jdsDb.isPrintingOutput)
                    System.out.printf("Saving Overview [%s of %s]\n", record, recordTotal)
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    /**
     * @param blobProperties
     */
    private fun saveBlobs(writeToPrimaryDataTables: Boolean, blobProperties: HashMap<String, HashMap<Long, SimpleBlobProperty>>) {
        var record = 0
        //log byte array as text???
        try {
            val upsert = if (jdsDb.supportsStatements()) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveBlob()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveBlob())
            val log = onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveOldBlobValues())
            for ((entityGuid, value) in blobProperties) {
                record++
                var innerRecord = 0
                val innerRecordSize = value.size
                if (innerRecordSize == 0) continue
                for ((fieldId, value1) in value) {
                    innerRecord++
                    if (writeToPrimaryDataTables) {
                        upsert.setString("entityGuid", entityGuid)
                        upsert.setLong("fieldId", fieldId)
                        upsert.setBytes("value", value1.get()!!)
                        upsert.addBatch()
                    }
                    if (jdsDb.isPrintingOutput)
                        System.out.printf("Updating record [%s]. Blob fieldEntity [%s of %s]\n", record, innerRecord, innerRecordSize)
                    if (!jdsDb.isLoggingEdits) continue
                    log.setString("entityGuid", entityGuid)
                    log.setLong("fieldId", fieldId)
                    log.setBytes("value", value1.get()!!)
                    log.setInt("sequence", 0)
                    log.addBatch()
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    /**
     * @param booleanProperties
     * @implNote Booleans are saved as integers behind the scenes
     */
    private fun saveBooleans(writeToPrimaryDataTables: Boolean, booleanProperties: HashMap<String, HashMap<Long, SimpleBooleanProperty>>) {
        var record = 0
        try {
            val upsert = if (jdsDb.supportsStatements()) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveInteger()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveInteger())
            val log = onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveOldIntegerValues())
            for ((entityGuid, value1) in booleanProperties) {
                record++
                var innerRecord = 0
                val innerRecordSize = value1.size
                if (innerRecordSize == 0) continue
                for ((fieldId, value2) in value1) {
                    innerRecord++
                    val value = if (value2.get()) 1 else 0
                    if (writeToPrimaryDataTables) {
                        upsert.setString("entityGuid", entityGuid)
                        upsert.setLong("fieldId", fieldId)
                        upsert.setInt("value", value)
                        upsert.addBatch()
                    }
                    if (jdsDb.isPrintingOutput)
                        System.out.printf("Updating record [%s]. Boolean fieldEntity [%s of %s]\n", record, innerRecord, innerRecordSize)
                    if (!jdsDb.isLoggingEdits) continue
                    log.setString("entityGuid", entityGuid)
                    log.setLong("fieldId", fieldId)
                    log.setInt("value", value)
                    log.setInt("sequence", 0)
                    log.addBatch()
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    /**
     * @param integerProperties
     */
    private fun saveIntegers(writeToPrimaryDataTables: Boolean, integerProperties: HashMap<String, HashMap<Long, SimpleIntegerProperty>>) {
        var record = 0
        try {
            val upsert = if (jdsDb.supportsStatements()) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveInteger()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveInteger())
            val log = onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveOldIntegerValues())
            for ((entityGuid, value1) in integerProperties) {
                record++
                var innerRecord = 0
                val innerRecordSize = value1.size
                if (innerRecordSize == 0) continue
                for ((fieldId, value2) in value1) {
                    innerRecord++
                    val value = value2.get()
                    if (writeToPrimaryDataTables) {
                        upsert.setString("entityGuid", entityGuid)
                        upsert.setLong("fieldId", fieldId)
                        upsert.setInt("value", value)
                        upsert.addBatch()
                    }
                    if (jdsDb.isPrintingOutput)
                        System.out.printf("Updating record [%s]. Integer fieldEntity [%s of %s]\n", record, innerRecord, innerRecordSize)
                    if (!jdsDb.isLoggingEdits) continue
                    log.setString("entityGuid", entityGuid)
                    log.setLong("fieldId", fieldId)
                    log.setInt("value", value)
                    log.setInt("sequence", 0)
                    log.addBatch()
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    /**
     * @param floatProperties
     */
    private fun saveFloats(writeToPrimaryDataTables: Boolean, floatProperties: HashMap<String, HashMap<Long, SimpleFloatProperty>>) {
        var record = 0
        try {
            val upsert = if (jdsDb.supportsStatements()) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveFloat()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveFloat())
            val log = onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveOldFloatValues())
            for ((entityGuid, value1) in floatProperties) {
                record++
                var innerRecord = 0
                val innerRecordSize = value1.size
                if (innerRecordSize == 0) continue
                for ((fieldId, value2) in value1) {
                    innerRecord++
                    val value = value2.get()
                    if (writeToPrimaryDataTables) {
                        upsert.setString("entityGuid", entityGuid)
                        upsert.setLong("fieldId", fieldId)
                        upsert.setFloat("value", value)
                        upsert.addBatch()
                    }
                    if (jdsDb.isPrintingOutput)
                        System.out.printf("Updating record [%s]. Float fieldEntity [%s of %s]\n", record, innerRecord, innerRecordSize)
                    if (!jdsDb.isLoggingEdits) continue
                    log.setString("entityGuid", entityGuid)
                    log.setLong("fieldId", fieldId)
                    log.setFloat("value", value)
                    log.setInt("sequence", 0)
                    log.addBatch()
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    /**
     * @param doubleProperties
     */
    private fun saveDoubles(writeToPrimaryDataTables: Boolean, doubleProperties: HashMap<String, HashMap<Long, SimpleDoubleProperty>>) {
        var record = 0
        try {
            val upsert = if (jdsDb.supportsStatements()) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveDouble()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveDouble())
            val log = onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveOldDoubleValues())
            for ((entityGuid, value1) in doubleProperties) {
                record++
                var innerRecord = 0
                val innerRecordSize = value1.size
                if (innerRecordSize == 0) continue
                for ((fieldId, value2) in value1) {
                    innerRecord++
                    val value = value2.get()
                    if (writeToPrimaryDataTables) {
                        upsert.setString("entityGuid", entityGuid)
                        upsert.setLong("fieldId", fieldId)
                        upsert.setDouble("value", value)
                        upsert.addBatch()
                    }
                    if (jdsDb.isPrintingOutput)
                        System.out.printf("Updating record [%s]. Double fieldEntity [%s of %s]\n", record, innerRecord, innerRecordSize)
                    if (!jdsDb.isLoggingEdits) continue
                    log.setString("entityGuid", entityGuid)
                    log.setLong("fieldId", fieldId)
                    log.setDouble("value", value)
                    log.setInt("sequence", 0)
                    log.addBatch()
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    /**
     * @param longProperties
     */
    private fun saveLongs(writeToPrimaryDataTables: Boolean, longProperties: HashMap<String, HashMap<Long, SimpleLongProperty>>) {
        var record = 0
        try {
            val upsert = if (jdsDb.supportsStatements()) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveLong()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveLong())
            val log = onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveOldLongValues())
            for ((entityGuid, value1) in longProperties) {
                record++
                var innerRecord = 0
                val innerRecordSize = value1.size
                if (innerRecordSize == 0) continue
                for ((fieldId, value2) in value1) {
                    innerRecord++
                    val value = value2.get()
                    if (writeToPrimaryDataTables) {
                        upsert.setString("entityGuid", entityGuid)
                        upsert.setLong("fieldId", fieldId)
                        upsert.setLong("value", value)
                        upsert.addBatch()
                    }
                    if (jdsDb.isPrintingOutput)
                        System.out.printf("Updating record [%s]. Long fieldEntity [%s of %s]\n", record, innerRecord, innerRecordSize)
                    if (!jdsDb.isLoggingEdits) continue
                    log.setString("entityGuid", entityGuid)
                    log.setLong("fieldId", fieldId)
                    log.setLong("value", value)
                    log.setInt("sequence", 0)
                    log.addBatch()
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    /**
     * @param stringProperties
     */
    private fun saveStrings(writeToPrimaryDataTables: Boolean, stringProperties: HashMap<String, HashMap<Long, SimpleStringProperty>>) {
        var record = 0
        try {
            val upsert = if (jdsDb.supportsStatements()) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveString()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveString())
            val log = onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveOldTextValues())
            for ((entityGuid, value1) in stringProperties) {
                record++
                var innerRecord = 0
                val innerRecordSize = value1.size
                if (innerRecordSize == 0) continue
                for ((fieldId, value2) in value1) {
                    innerRecord++
                    val value = value2.get()
                    if (writeToPrimaryDataTables) {
                        upsert.setString("entityGuid", entityGuid)
                        upsert.setLong("fieldId", fieldId)
                        upsert.setString("value", value)
                        upsert.addBatch()
                    }
                    if (jdsDb.isPrintingOutput)
                        System.out.printf("Updating record [%s]. Text fieldEntity [%s of %s]\n", record, innerRecord, innerRecordSize)
                    if (!jdsDb.isLoggingEdits) continue
                    log.setString("entityGuid", entityGuid)
                    log.setLong("fieldId", fieldId)
                    log.setString("value", value)
                    log.setInt("sequence", 0)
                    log.addBatch()
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    private fun saveDateConstructs(writeToPrimaryDataTables: Boolean,
                                   monthDayProperties: HashMap<String, HashMap<Long, SimpleObjectProperty<MonthDay>>>,
                                   yearMonthProperties: HashMap<String, HashMap<Long, SimpleObjectProperty<Temporal>>>,
                                   periodProperties: HashMap<String, HashMap<Long, SimpleObjectProperty<Period>>>,
                                   durationProperties: HashMap<String, HashMap<Long, SimpleObjectProperty<Duration>>>) {
        var record = 0
        try {
            val upsertText = if (jdsDb.supportsStatements()) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveString()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveString())
            val upsertLong = if (jdsDb.supportsStatements()) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveLong()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveLong())

            val logText = onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveOldTextValues())
            val logLong = onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveOldLongValues())

            for ((entityGuid, hashMap) in monthDayProperties) {
                record++
                var innerRecord = 0
                val innerRecordSize = hashMap.size
                if (innerRecordSize == 0) continue
                for ((fieldId, monthDayProperty) in hashMap) {
                    innerRecord++
                    val monthDay = monthDayProperty.get()
                    val value = monthDay.toString()
                    if (writeToPrimaryDataTables) {
                        upsertText.setString("entityGuid", entityGuid)
                        upsertText.setLong("fieldId", fieldId)
                        upsertText.setString("value", value)
                        upsertText.addBatch()
                    }
                    if (jdsDb.isPrintingOutput)
                        System.out.printf("Updating record [%s]. MonthDay fieldEntity [%s of %s]\n", record, innerRecord, innerRecordSize)
                    if (!jdsDb.isLoggingEdits) continue
                    logText.setString("entityGuid", entityGuid)
                    logText.setLong("fieldId", fieldId)
                    logText.setString("value", value)
                    logText.setInt("sequence", 0)
                    logText.addBatch()
                }
            }

            for ((entityGuid, hashMap) in yearMonthProperties) {
                record++
                var innerRecord = 0
                val innerRecordSize = hashMap.size
                if (innerRecordSize == 0) continue
                for ((fieldId, yearMonthProperty) in hashMap) {
                    innerRecord++
                    val yearMonth = yearMonthProperty.get() as YearMonth
                    val value = yearMonth.toString()
                    if (writeToPrimaryDataTables) {
                        upsertText.setString("entityGuid", entityGuid)
                        upsertText.setLong("fieldId", fieldId)
                        upsertText.setString("value", value)
                        upsertText.addBatch()
                    }
                    if (jdsDb.isPrintingOutput)
                        System.out.printf("Updating record [%s]. YearMonth fieldEntity [%s of %s]\n", record, innerRecord, innerRecordSize)
                    if (!jdsDb.isLoggingEdits) continue
                    logText.setString("entityGuid", entityGuid)
                    logText.setLong("fieldId", fieldId)
                    logText.setString("value", value)
                    logText.setInt("sequence", 0)
                    logText.addBatch()
                }
            }

            for ((entityGuid, hashMap) in periodProperties) {
                record++
                var innerRecord = 0
                val innerRecordSize = hashMap.size
                if (innerRecordSize == 0) continue
                for ((fieldId, periodProperty) in hashMap) {
                    innerRecord++
                    val period = periodProperty.get()
                    val value = period.toString()
                    if (writeToPrimaryDataTables) {
                        upsertText.setString("entityGuid", entityGuid)
                        upsertText.setLong("fieldId", fieldId)
                        upsertText.setString("value", value)
                        upsertText.addBatch()
                    }
                    if (jdsDb.isPrintingOutput)
                        System.out.printf("Updating record [%s]. Period fieldEntity [%s of %s]\n", record, innerRecord, innerRecordSize)
                    if (!jdsDb.isLoggingEdits) continue
                    logText.setString("entityGuid", entityGuid)
                    logText.setLong("fieldId", fieldId)
                    logText.setString("value", value)
                    logText.setInt("sequence", 0)
                    logText.addBatch()
                }
            }

            for ((entityGuid, hashMap) in durationProperties) {
                record++
                var innerRecord = 0
                val innerRecordSize = hashMap.size
                if (innerRecordSize == 0) continue
                for ((fieldId, durationProperty) in hashMap) {
                    innerRecord++
                    val duration = durationProperty.get()
                    val value = duration.toNanos()
                    if (writeToPrimaryDataTables) {
                        upsertLong.setString("entityGuid", entityGuid)
                        upsertLong.setLong("fieldId", fieldId)
                        upsertLong.setLong("value", value)
                        upsertLong.addBatch()
                    }
                    if (jdsDb.isPrintingOutput)
                        System.out.printf("Updating record [%s]. Duration fieldEntity [%s of %s]\n", record, innerRecord, innerRecordSize)
                    if (!jdsDb.isLoggingEdits) continue
                    logLong.setString("entityGuid", entityGuid)
                    logLong.setLong("fieldId", fieldId)
                    logLong.setLong("value", value)
                    logLong.setInt("sequence", 0)
                    logLong.addBatch()
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    /**
     * @param localDateTimeProperties
     * @param localDateProperties
     */
    private fun saveDatesAndDateTimes(writeToPrimaryDataTables: Boolean, localDateTimeProperties: HashMap<String, HashMap<Long, SimpleObjectProperty<Temporal>>>, localDateProperties: HashMap<String, HashMap<Long, SimpleObjectProperty<Temporal>>>) {
        var record = 0
        try {
            val upsert = if (jdsDb.supportsStatements()) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveDateTime()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveDateTime())
            val log = onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveOldDateTimeValues())
            for ((entityGuid, value) in localDateTimeProperties) {
                record++
                var innerRecord = 0
                val innerRecordSize = value.size
                if (innerRecordSize == 0) continue
                for ((fieldId, value1) in value) {
                    innerRecord++
                    val localDateTime = value1.get() as LocalDateTime
                    if (writeToPrimaryDataTables) {
                        upsert.setString("entityGuid", entityGuid)
                        upsert.setLong("fieldId", fieldId)
                        upsert.setTimestamp("value", Timestamp.valueOf(localDateTime))
                        upsert.addBatch()
                    }
                    if (jdsDb.isPrintingOutput)
                        System.out.printf("Updating record [%s]. LocalDateTime fieldEntity [%s of %s]\n", record, innerRecord, innerRecordSize)
                    if (!jdsDb.isLoggingEdits) continue
                    log.setString("entityGuid", entityGuid)
                    log.setLong("fieldId", fieldId)
                    log.setTimestamp("value", Timestamp.valueOf(localDateTime))
                    log.setInt("sequence", 0)
                    log.addBatch()
                }
            }
            for ((entityGuid, value) in localDateProperties) {
                record++
                var innerRecord = 0
                val innerRecordSize = value.size
                if (innerRecordSize == 0) continue
                for ((fieldId, value1) in value) {
                    innerRecord++
                    val localDate = value1.get() as LocalDate
                    upsert.setString("entityGuid", entityGuid)
                    upsert.setLong("fieldId", fieldId)
                    upsert.setTimestamp("value", Timestamp.valueOf(localDate.atStartOfDay()))
                    upsert.addBatch()
                    if (jdsDb.isPrintingOutput)
                        System.out.printf("Updating record [%s]. LocalDate fieldEntity [%s of %s]\n", record, innerRecord, innerRecordSize)
                    if (!jdsDb.isLoggingEdits) continue
                    log.setString("entityGuid", entityGuid)
                    log.setLong("fieldId", fieldId)
                    log.setTimestamp("value", Timestamp.valueOf(localDate.atStartOfDay()))
                    log.setInt("sequence", 0)
                    log.addBatch()
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    /**
     * @param localTimeProperties
     */
    private fun saveTimes(writeToPrimaryDataTables: Boolean, localTimeProperties: HashMap<String, HashMap<Long, SimpleObjectProperty<Temporal>>>) {
        var record = 0
        try {
            val upsert = if (jdsDb.supportsStatements()) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveTime()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveTime())
            val log = onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveOldIntegerValues())
            for ((entityGuid, value) in localTimeProperties) {
                record++
                var innerRecord = 0
                val innerRecordSize = value.size
                if (innerRecordSize == 0) continue
                for ((fieldId, value1) in value) {
                    innerRecord++
                    val localTime = value1.get() as LocalTime
                    val secondOfDay = localTime.toSecondOfDay()
                    if (writeToPrimaryDataTables) {
                        upsert.setString("entityGuid", entityGuid)
                        upsert.setLong("fieldId", fieldId)
                        upsert.setInt("value", secondOfDay)
                        upsert.addBatch()
                    }
                    if (jdsDb.isPrintingOutput)
                        System.out.printf("Updating record [%s]. LocalTime fieldEntity [%s of %s]\n", record, innerRecord, innerRecordSize)
                    if (!jdsDb.isLoggingEdits) continue
                    log.setString("entityGuid", entityGuid)
                    log.setLong("fieldId", fieldId)
                    log.setInt("value", secondOfDay)
                    log.setInt("sequence", 0)
                    log.addBatch()
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    /**
     * @param zonedDateProperties
     */
    private fun saveZonedDateTimes(writeToPrimaryDataTables: Boolean, zonedDateProperties: HashMap<String, HashMap<Long, SimpleObjectProperty<Temporal>>>) {
        var record = 0
        try {
            val upsert = if (jdsDb.supportsStatements()) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveZonedDateTime()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveZonedDateTime())
            val log = onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveOldLongValues())
            for ((entityGuid, value) in zonedDateProperties) {
                record++
                var innerRecord = 0
                val innerRecordSize = value.size
                if (innerRecordSize == 0) continue
                for ((fieldId, value1) in value) {
                    innerRecord++
                    val zonedDateTime = value1.get() as ZonedDateTime
                    if (writeToPrimaryDataTables) {
                        upsert.setString("entityGuid", entityGuid)
                        upsert.setLong("fieldId", fieldId)
                        upsert.setLong("value", zonedDateTime.toInstant().toEpochMilli())
                        upsert.addBatch()
                    }
                    if (jdsDb.isPrintingOutput)
                        System.out.printf("Updating record [%s]. ZonedDateTime fieldEntity [%s of %s]\n", record, innerRecord, innerRecordSize)
                    if (!jdsDb.isLoggingEdits) continue
                    log.setString("entityGuid", entityGuid)
                    log.setLong("fieldId", fieldId)
                    log.setLong("value", zonedDateTime.toInstant().toEpochMilli())
                    log.setInt("sequence", 0)
                    log.addBatch()
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    /**
     * @param enums
     */
    fun saveEnums(writeToPrimaryDataTables: Boolean, enums: HashMap<String, HashMap<JdsFieldEnum<*>, SimpleObjectProperty<Enum<*>>>>) {
        var record = 0
        try {
            val upsert = if (jdsDb.supportsStatements()) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveInteger()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveInteger())
            val log = onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveOldIntegerValues())
            for ((entityGuid, value1) in enums) {
                record++
                var innerRecord = 0
                val innerRecordSize = value1.size
                if (innerRecordSize == 0) continue
                for ((jdsFieldEnum, value2) in value1) {
                    innerRecord++
                    val value = value2.get()
                    if (writeToPrimaryDataTables) {
                        upsert.setString("entityGuid", entityGuid)
                        upsert.setLong("fieldId", jdsFieldEnum.field.id)
                        upsert.setInt("value", jdsFieldEnum.indexOf(value))
                        upsert.addBatch()
                    }
                    if (jdsDb.isPrintingOutput)
                        System.out.printf("Updating record [%s]. Enum fieldEntity [%s of %s]\n", record, innerRecord, innerRecordSize)
                    if (!jdsDb.isLoggingEdits) continue
                    log.setString("entityGuid", entityGuid)
                    log.setLong("fieldId", jdsFieldEnum.field.id)
                    log.setInt("value", jdsFieldEnum.indexOf(value))
                    log.setInt("sequence", 0)
                    log.addBatch()
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    /**
     * Save all dates in one go
     *
     * @param dateTimeArrayProperties
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5]to[3,4]
     */
    private fun saveArrayDates(writeToPrimaryDataTables: Boolean, dateTimeArrayProperties: HashMap<String, HashMap<Long, SimpleListProperty<LocalDateTime>>>) {
        val deleteSql = "DELETE FROM JdsStoreDateTimeArray WHERE FieldId = ? AND EntityGuid = ?"
        val insertSql = "INSERT INTO JdsStoreDateTimeArray (Sequence,Value,FieldId,EntityGuid) VALUES (?,?,?,?)"
        try {
            val log = onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveOldDateTimeValues())
            val delete = onPostSaveEventArguments.getOrAddStatement(deleteSql)
            val insert = onPostSaveEventArguments.getOrAddStatement(insertSql)
            var record = 0
            for ((entityGuid, value) in dateTimeArrayProperties) {
                record++
                val index = SimpleIntegerProperty(0)
                for ((fieldId, value1) in value) {
                    val innerRecord = 0
                    val innerTotal = value1.get().size
                    for (value in value1.get()) {
                        if (jdsDb.isLoggingEdits) {
                            log.setString("entityGuid", entityGuid)
                            log.setLong("fieldId", fieldId)
                            log.setTimestamp("value", Timestamp.valueOf(value))
                            log.setInt("sequence", index.get())
                            log.addBatch()
                        }
                        if (writeToPrimaryDataTables) {
                            delete.setLong(1, fieldId)
                            delete.setString(2, entityGuid)
                            delete.addBatch()
                            //insert
                            insert.setInt(1, index.get())
                            insert.setTimestamp(2, Timestamp.valueOf(value))
                            insert.setLong(3, fieldId)
                            insert.setString(4, entityGuid)
                            insert.addBatch()
                        }
                        index.set(index.get() + 1)
                        if (jdsDb.isPrintingOutput)
                            System.out.printf("Inserting array record [%s]. DateTime fieldEntity [%s of %s]\n", record, innerRecord, innerTotal)
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    /**
     * @param floatArrayProperties
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5]to[3,4]
     */
    private fun saveArrayFloats(writeToPrimaryDataTables: Boolean, floatArrayProperties: HashMap<String, HashMap<Long, SimpleListProperty<Float>>>) {
        val deleteSql = "DELETE FROM JdsStoreFloatArray WHERE FieldId = ? AND EntityGuid = ?"
        val insertSql = "INSERT INTO JdsStoreFloatArray (FieldId,EntityGuid,Value,Sequence) VALUES (?,?,?,?)"
        try {
            val log = onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveOldFloatValues())
            val delete = onPostSaveEventArguments.getOrAddStatement(deleteSql)
            val insert = onPostSaveEventArguments.getOrAddStatement(insertSql)
            var record = 0
            for ((entityGuid, value) in floatArrayProperties) {
                record++
                val index = SimpleIntegerProperty(0)
                for ((fieldId, value1) in value) {
                    val innerRecord = 0
                    val innerTotal = value1.get().size
                    for (value in value1.get()) {
                        if (jdsDb.isLoggingEdits) {
                            log.setString("entityGuid", entityGuid)
                            log.setLong("fieldId", fieldId)
                            log.setFloat("value", value!!)
                            log.setInt("sequence", index.get())
                            log.addBatch()
                        }
                        if (writeToPrimaryDataTables) {//delete
                            delete.setLong(1, fieldId)
                            delete.setString(2, entityGuid)
                            delete.addBatch()
                            //insert
                            insert.setInt(1, index.get())
                            insert.setFloat(2, value!!)
                            insert.setLong(3, fieldId)
                            insert.setString(4, entityGuid)
                            insert.addBatch()
                        }
                        index.set(index.get() + 1)
                        if (jdsDb.isPrintingOutput)
                            System.out.printf("Inserting array record [%s]. Float fieldEntity [%s of %s]\n", record, innerRecord, innerTotal)

                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    /**
     * @param integerArrayProperties
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5] to [3,4]
     */
    private fun saveArrayIntegers(writeToPrimaryDataTables: Boolean, integerArrayProperties: HashMap<String, HashMap<Long, SimpleListProperty<Int>>>) {
        val deleteSql = "DELETE FROM JdsStoreIntegerArray WHERE FieldId = :fieldId AND EntityGuid = :entityGuid"
        val insertSql = "INSERT INTO JdsStoreIntegerArray (FieldId,EntityGuid,Sequence,Value) VALUES (:fieldId, :entityGuid, :sequence, :value)"
        try {
            val log = onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveOldIntegerValues())
            val delete = onPostSaveEventArguments.getOrAddNamedStatement(deleteSql)
            val insert = onPostSaveEventArguments.getOrAddNamedStatement(insertSql)
            var record = 0
            for ((entityGuid, value) in integerArrayProperties) {
                record++
                val index = SimpleIntegerProperty(0)
                for ((fieldId, value1) in value) {
                    val innerRecord = 0
                    val innerTotal = value1.get().size
                    for (value in value1.get()) {
                        if (jdsDb.isLoggingEdits) {
                            log.setString("entityGuid", entityGuid)
                            log.setLong("fieldId", fieldId)
                            log.setInt("value", value!!)
                            log.setInt("sequence", index.get())
                            log.addBatch()
                        }
                        if (writeToPrimaryDataTables) {
                            //delete
                            delete.setLong("fieldId", fieldId)
                            delete.setString("entityGuid", entityGuid)
                            delete.addBatch()
                            //insert
                            insert.setInt("sequence", index.get())
                            insert.setInt("value", value!!)
                            insert.setLong("fieldId", fieldId)
                            insert.setString("entityGuid", entityGuid)
                            insert.addBatch()
                        }
                        index.set(index.get() + 1)
                        if (jdsDb.isPrintingOutput)
                            System.out.printf("Inserting array record [%s]. Integer fieldEntity [%s of %s]\n", record, innerRecord, innerTotal)
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    /**
     * @param doubleArrayProperties
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5]to[3,4]
     */
    private fun saveArrayDoubles(writeToPrimaryDataTables: Boolean, doubleArrayProperties: HashMap<String, HashMap<Long, SimpleListProperty<Double>>>) {
        val deleteSql = "DELETE FROM JdsStoreDoubleArray WHERE FieldId = :fieldId AND EntityGuid = :entityGuid"
        val insertSql = "INSERT INTO JdsStoreDoubleArray (FieldId,EntityGuid,Sequence,Value) VALUES (:fieldId, :entityGuid, :sequence, :value)"
        try {
            val log = onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveOldDoubleValues())
            val delete = onPostSaveEventArguments.getOrAddNamedStatement(deleteSql)
            val insert = onPostSaveEventArguments.getOrAddNamedStatement(insertSql)
            var record = 0
            for ((entityGuid, value) in doubleArrayProperties) {
                record++
                val index = SimpleIntegerProperty(0)
                for ((fieldId, value1) in value) {
                    val innerRecord = 0
                    val innerTotal = value1.get().size
                    for (value in value1.get()) {
                        if (jdsDb.isLoggingEdits) {
                            log.setString("entityGuid", entityGuid)
                            log.setLong("fieldId", fieldId)
                            log.setDouble("value", value!!)
                            log.setInt("sequence", index.get())
                            log.addBatch()
                        }
                        if (writeToPrimaryDataTables) {
                            //delete
                            delete.setLong("fieldId", fieldId)
                            delete.setString("entityGuid", entityGuid)
                            delete.addBatch()
                            //insert
                            insert.setInt("fieldId", index.get())
                            insert.setDouble("entityGuid", value!!)
                            insert.setLong("sequence", fieldId)
                            insert.setString("value", entityGuid)
                            insert.addBatch()
                        }
                        index.set(index.get() + 1)
                        if (jdsDb.isPrintingOutput)
                            System.out.printf("Inserting array record [%s]. Double fieldEntity [%s of %s]\n", record, innerRecord, innerTotal)
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    /**
     * @param longArrayProperties
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5]to[3,4]
     */
    private fun saveArrayLongs(writeToPrimaryDataTables: Boolean, longArrayProperties: HashMap<String, HashMap<Long, SimpleListProperty<Long>>>) {
        val deleteSql = "DELETE FROM JdsStoreDoubleArray WHERE FieldId = ? AND EntityGuid = ?"
        val insertSql = "INSERT INTO JdsStoreDoubleArray (FieldId,EntityGuid,Sequence,Value) VALUES (?,?,?,?)"
        try {
            val log = onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveOldLongValues())
            val delete = onPostSaveEventArguments.getOrAddStatement(deleteSql)
            val insert = onPostSaveEventArguments.getOrAddStatement(insertSql)
            var record = 0
            for ((entityGuid, value) in longArrayProperties) {
                record++
                val index = SimpleIntegerProperty(0)
                for ((fieldId, value1) in value) {
                    val innerRecord = 0
                    val innerTotal = value1.get().size
                    for (value in value1.get()) {
                        if (jdsDb.isLoggingEdits) {
                            log.setString("entityGuid", entityGuid)
                            log.setLong("fieldId", fieldId)
                            log.setLong("value", value!!)
                            log.setInt("sequence", index.get())
                            log.addBatch()
                        }
                        if (writeToPrimaryDataTables) {//delete
                            delete.setLong(1, fieldId)
                            delete.setString(2, entityGuid)
                            delete.addBatch()
                            //insert
                            insert.setInt(1, index.get())
                            insert.setLong(2, value!!)
                            insert.setLong(3, fieldId)
                            insert.setString(4, entityGuid)
                            insert.addBatch()
                        }
                        index.set(index.get() + 1)
                        if (jdsDb.isPrintingOutput)
                            System.out.printf("Inserting array record [%s]. Long fieldEntity [%s of %s]\n", record, innerRecord, innerTotal)
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    /**
     * @param stringArrayProperties
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5]to[3,4]
     */
    private fun saveArrayStrings(writeToPrimaryDataTables: Boolean, stringArrayProperties: HashMap<String, HashMap<Long, SimpleListProperty<String>>>) {
        val deleteSql = "DELETE FROM JdsStoreTextArray WHERE FieldId = :fieldId AND EntityGuid = :entityGuid"
        val insertSql = "INSERT INTO JdsStoreTextArray (FieldId,EntityGuid,Sequence,Value) VALUES (:fieldId, :entityGuid, :sequence, :value)"
        try {
            val log = onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveOldTextValues())
            val delete = onPostSaveEventArguments.getOrAddNamedStatement(deleteSql)
            val insert = onPostSaveEventArguments.getOrAddNamedStatement(insertSql)
            var record = 0
            for ((entityGuid, value) in stringArrayProperties) {
                record++
                val index = SimpleIntegerProperty(0)
                for ((fieldId, value1) in value) {
                    val innerRecord = 0
                    val innerTotal = value1.get().size
                    for (value in value1.get()) {
                        if (jdsDb.isLoggingEdits) {
                            log.setString("entityGuid", entityGuid)
                            log.setLong("fieldId", fieldId)
                            log.setInt("sequence", index.get())
                            log.setString("value", value)
                            log.addBatch()
                        }
                        if (writeToPrimaryDataTables) {
                            //delete
                            delete.setLong("fieldId", fieldId)
                            delete.setString("entityGuid", entityGuid)
                            delete.addBatch()
                            //insert
                            insert.setInt("fieldId", index.get())
                            insert.setString("entityGuid", value)
                            insert.setLong("sequence", fieldId)
                            insert.setString("value", entityGuid)
                            insert.addBatch()
                        }
                        index.set(index.get() + 1)
                        if (jdsDb.isPrintingOutput)
                            System.out.printf("Inserting array record [%s]. String fieldEntity [%s of %s]\n", record, innerRecord, innerTotal)
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    /**
     * @param enumStrings
     * @apiNote Enums are actually saved as index based integer arrays
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5]to[3,4]
     */
    private fun saveEnumCollections(writeToPrimaryDataTables: Boolean, enumStrings: HashMap<String, HashMap<JdsFieldEnum<*>, SimpleListProperty<Enum<*>>>>) {
        var record = 0
        val recordTotal = enumStrings.size
        val deleteSql = "DELETE FROM JdsStoreIntegerArray WHERE FieldId = :fieldId AND EntityGuid = :entityGuid"
        val insertSql = "INSERT INTO JdsStoreIntegerArray (FieldId,EntityGuid,Sequence,Value) VALUES (:fieldId, :entityGuid, :sequence, :value)"
        try {
            val log = onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveOldIntegerValues())
            val delete = onPostSaveEventArguments.getOrAddNamedStatement(deleteSql)
            val insert = onPostSaveEventArguments.getOrAddNamedStatement(insertSql)
            for ((entityGuid, value) in enumStrings) {
                record++
                for ((jdsFieldEnum, value1) in value) {
                    var sequence = 0
                    val textValues = value1.get()
                    if (textValues.size == 0) continue
                    for (anEnum in textValues) {
                        if (jdsDb.isLoggingEdits) {
                            log.setString("entityGuid", entityGuid)
                            log.setLong("fieldId", jdsFieldEnum.field.id)
                            log.setInt("value", jdsFieldEnum.indexOf(anEnum))
                            log.setInt("sequence", sequence)
                            log.addBatch()
                        }
                        if (writeToPrimaryDataTables) {
                            //delete
                            delete.setLong("fieldId", jdsFieldEnum.field.id)
                            delete.setString("entityGuid", entityGuid)
                            delete.addBatch()
                            //insert
                            insert.setLong("fieldId", jdsFieldEnum.field.id)
                            insert.setString("entityGuid", entityGuid)
                            insert.setInt("sequence", sequence)
                            insert.setInt("value", jdsFieldEnum.indexOf(anEnum))
                            insert.addBatch()
                        }
                        if (jdsDb.isPrintingOutput)
                            System.out.printf("Updating enum [%s]. Object fieldEntity [%s of %s]\n", sequence, record, recordTotal)
                        sequence++
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    /**
     * @param connection
     * @param objectArrayProperties
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5]to[3,4]
     * @implNote For the love of Christ don't use parallel stream here
     */
    @Throws(Exception::class)
    private fun saveAndBindObjectArrays(connection: Connection, objectArrayProperties: HashMap<String, HashMap<JdsFieldEntity<*>, SimpleListProperty<JdsEntity>>>) {
        if (objectArrayProperties.isEmpty()) return
        val jdsEntities = ArrayList<JdsEntity>()
        val parentEntityBindings = ArrayList<JdsParentEntityBinding>()
        val parentChildBindings = ArrayList<JdsParentChildBinding>()
        val record = SimpleIntegerProperty(0)
        val changesMade = SimpleBooleanProperty(false)
        val map: MutableMap<String, Long> = HashMap<String, Long>()

        for ((parentGuid, value) in objectArrayProperties) {
            for ((key, value1) in value) {
                record.set(0)
                changesMade.set(false)
                value1.stream().filter { jdsEntity -> jdsEntity != null }.forEach { jdsEntity ->
                    if (!changesMade.get()) {
                        //only clear if changes are made. else you wipe out old bindings regardless
                        changesMade.set(true)

                        val parentEntityBinding = JdsParentEntityBinding()
                        parentEntityBinding.parentGuid = parentGuid
                        parentEntityBinding.entityId = jdsEntity.overview.entityId
                        parentEntityBinding.fieldId = key.fieldEntity.id
                        parentEntityBindings.add(parentEntityBinding)


                    }
                    val parentChildBinding = JdsParentChildBinding()
                    parentChildBinding.parentGuid = parentGuid
                    parentChildBinding.childGuid = jdsEntity.overview.entityGuid
                    parentChildBindings.add(parentChildBinding)

                    jdsEntities.add(jdsEntity)
                    map.put(jdsEntity.overview.entityGuid, key.fieldEntity.id)

                    record.set(record.get() + 1)
                    if (jdsDb.isPrintingOutput)
                        System.out.printf("Binding array object %s\n", record.get())
                }
            }
        }

        //save children first
        JdsSave(jdsDb, connection, -1, jdsEntities, true, onPreSaveEventArguments, onPostSaveEventArguments).call()

        //bind children below
        try {
            val clearOldBindings = onPostSaveEventArguments.getOrAddNamedStatement("DELETE FROM JdsStoreEntityBinding WHERE ParentEntityGuid = :parentEntityGuid AND ChildEntityId = :childEntityId AND FieldId = :fieldId")
            val writeNewBindings = onPostSaveEventArguments.getOrAddNamedStatement("INSERT INTO JdsStoreEntityBinding(ParentEntityGuid, ChildEntityGuid, FieldId, ChildEntityId) Values(:parentEntityGuid, :childEntityGuid, :fieldId, :childEntityId)")
            for (parentEntityBinding in parentEntityBindings) {
                clearOldBindings.setString("parentEntityGuid", parentEntityBinding.parentGuid)
                clearOldBindings.setLong("childEntityId", parentEntityBinding.entityId)
                clearOldBindings.setLong("fieldId", parentEntityBinding.fieldId)
                clearOldBindings.addBatch()
            }
            for (jdsEntity in jdsEntities) {
                writeNewBindings.setString("parentEntityGuid", getParent(parentChildBindings, jdsEntity.overview.entityGuid))
                writeNewBindings.setString("childEntityGuid", jdsEntity.overview.entityGuid)
                writeNewBindings.setLong("childEntityId", jdsEntity.overview.entityId)
                writeNewBindings.setLong("fieldId", map[jdsEntity.overview.entityGuid]!!)
                writeNewBindings.addBatch()
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    /**
     * @param connection
     * @param objectProperties
     * @implNote For the love of Christ don't use parallel stream here
     */
    @Throws(Exception::class)
    private fun saveAndBindObjects(connection: Connection, objectProperties: HashMap<String, HashMap<JdsFieldEntity<*>, SimpleObjectProperty<JdsEntity>>>) {
        if (objectProperties.isEmpty()) return //prevent stack overflow :)
        val record = SimpleIntegerProperty(0)
        val changesMade = SimpleBooleanProperty(false)
        val parentEntityBindings = ArrayList<JdsParentEntityBinding>()
        val parentChildBindings = ArrayList<JdsParentChildBinding>()
        val jdsEntities = ArrayList<JdsEntity>()
        val map: MutableMap<String, Long> = HashMap<String, Long>()

        for ((parentGuid, value) in objectProperties) {
            for ((key, value1) in value) {
                record.set(0)
                val jdsEntity = value1.get()
                changesMade.set(false)
                if (jdsEntity != null) {
                    if (!changesMade.get()) {
                        changesMade.set(true)
                        val parentEntityBinding = JdsParentEntityBinding()
                        parentEntityBinding.parentGuid = parentGuid
                        parentEntityBinding.entityId = value1.get().overview.entityId
                        parentEntityBinding.fieldId = key.fieldEntity.id
                        parentEntityBindings.add(parentEntityBinding)
                    }
                    jdsEntities.add(jdsEntity)
                    val parentChildBinding = JdsParentChildBinding()
                    parentChildBinding.parentGuid = parentGuid
                    parentChildBinding.childGuid = jdsEntity.overview.entityGuid

                    parentChildBindings.add(parentChildBinding)
                    map.put(value1.get().overview.entityGuid, key.fieldEntity.id)

                    record.set(record.get() + 1)
                    if (jdsDb.isPrintingOutput)
                        System.out.printf("Binding object %s\n", record.get())
                }
            }
        }

        //save children first
        JdsSave(jdsDb, connection, -1, jdsEntities, true, onPreSaveEventArguments, onPostSaveEventArguments).call()

        //bind children below
        try {
            val clearOldBindings = onPostSaveEventArguments.getOrAddNamedStatement("DELETE FROM JdsStoreEntityBinding WHERE ParentEntityGuid = :parentEntityGuid AND ChildEntityId = :childEntityId AND FieldId = :fieldId")
            val writeNewBindings = onPostSaveEventArguments.getOrAddNamedStatement("INSERT INTO JdsStoreEntityBinding(ParentEntityGuid, ChildEntityGuid, FieldId, ChildEntityId) Values(:parentEntityGuid, :childEntityGuid, :fieldId, :childEntityId)")
            for (parentEntityBinding in parentEntityBindings) {
                clearOldBindings.setString("parentEntityGuid", parentEntityBinding.parentGuid)
                clearOldBindings.setLong("childEntityId", parentEntityBinding.entityId)
                clearOldBindings.setLong("fieldId", parentEntityBinding.fieldId)
                clearOldBindings.addBatch()
            }
            for (jdsEntity in jdsEntities) {
                writeNewBindings.setString("parentEntityGuid", getParent(parentChildBindings, jdsEntity.overview.entityGuid))
                writeNewBindings.setString("childEntityGuid", jdsEntity.overview.entityGuid)
                writeNewBindings.setLong("childEntityId", jdsEntity.overview.entityId)
                writeNewBindings.setLong("fieldId", map[jdsEntity.overview.entityGuid]!!)
                writeNewBindings.addBatch()
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    /**
     * @param jdsParentChildBindings
     * @param childGuid
     * @return
     */
    private fun getParent(jdsParentChildBindings: Collection<JdsParentChildBinding>, childGuid: String): String {
        val any = jdsParentChildBindings.stream().filter { parentChildBinding -> parentChildBinding.childGuid == childGuid }.findAny()
        return if (any.isPresent) any.get().parentGuid else ""
    }

    companion object {

        /**
         * @param jdsDb
         * @param batchSize
         * @param entities
         */
        @Deprecated("please refer to <a href=\"https://github.com/SubiyaCryolite/Jenesis-Data-Store\"> the readme</a> for the most up to date CRUD approach", ReplaceWith("JdsSave(jdsDb, batchSize, entities).call()", "io.github.subiyacryolite.jds.JdsSave"))
        @Throws(Exception::class)
        fun save(jdsDb: JdsDb, batchSize: Int, entities: Collection<JdsEntity>) {
            JdsSave(jdsDb, batchSize, entities).call()
        }

        /**
         * @param jdsDb
         * @param batchSize
         * @param entities
         */
        @Deprecated("please refer to <a href=\"https://github.com/SubiyaCryolite/Jenesis-Data-Store\"> the readme</a> for the most up to date CRUD approach", ReplaceWith("save(jdsDb, batchSize, Arrays.asList(*entities))", "io.github.subiyacryolite.jds.JdsSave.Companion.save", "java.util.Arrays"))
        @Throws(Exception::class)
        fun save(jdsDb: JdsDb, batchSize: Int, vararg entities: JdsEntity) {
            save(jdsDb, batchSize, Arrays.asList(*entities))
        }
    }
}
