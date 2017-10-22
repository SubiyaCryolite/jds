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
class JdsSave private constructor(private val jdsDb: JdsDb, private val connection: Connection, private val batchSize: Int, private val entities: Collection<JdsEntity>, private val recursiveInnerCall: Boolean, private val onPreSaveEventArguments: OnPreSaveEventArguments = OnPreSaveEventArguments(jdsDb, connection), private val onPostSaveEventArguments: OnPostSaveEventArguments = OnPostSaveEventArguments(jdsDb, connection)) : Callable<Boolean> {

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
                println("Processed batch [$step of ${steps + 1}]")
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
        saveContainer.localDateTimeProperties.add(HashMap<String, HashMap<Long, ObjectProperty<Temporal>>>())
        saveContainer.zonedDateTimeProperties.add(HashMap<String, HashMap<Long, ObjectProperty<Temporal>>>())
        saveContainer.localTimeProperties.add(HashMap<String, HashMap<Long, ObjectProperty<Temporal>>>())
        saveContainer.localDateProperties.add(HashMap<String, HashMap<Long, ObjectProperty<Temporal>>>())
        saveContainer.monthDayProperties.add(HashMap<String, HashMap<Long, ObjectProperty<MonthDay>>>())
        saveContainer.yearMonthProperties.add(HashMap<String, HashMap<Long, ObjectProperty<Temporal>>>())
        saveContainer.periodProperties.add(HashMap<String, HashMap<Long, ObjectProperty<Period>>>())
        saveContainer.durationProperties.add(HashMap<String, HashMap<Long, ObjectProperty<Duration>>>())
        //primitives
        saveContainer.booleanProperties.add(HashMap<String, HashMap<Long, BooleanProperty>>())
        saveContainer.floatProperties.add(HashMap<String, HashMap<Long, FloatProperty>>())
        saveContainer.doubleProperties.add(HashMap<String, HashMap<Long, DoubleProperty>>())
        saveContainer.longProperties.add(HashMap<String, HashMap<Long, LongProperty>>())
        saveContainer.integerProperties.add(HashMap<String, HashMap<Long, IntegerProperty>>())
        //string
        saveContainer.stringProperties.add(HashMap<String, HashMap<Long, StringProperty>>())
        //blob
        saveContainer.blobProperties.add(HashMap<String, HashMap<Long, BlobProperty>>())
        //arrays
        saveContainer.stringCollections.add(HashMap<String, HashMap<Long, ListProperty<String>>>())
        saveContainer.localDateTimeCollections.add(HashMap<String, HashMap<Long, ListProperty<LocalDateTime>>>())
        saveContainer.floatCollections.add(HashMap<String, HashMap<Long, ListProperty<Float>>>())
        saveContainer.doubleCollections.add(HashMap<String, HashMap<Long, ListProperty<Double>>>())
        saveContainer.longCollections.add(HashMap<String, HashMap<Long, ListProperty<Long>>>())
        saveContainer.integerCollections.add(HashMap<String, HashMap<Long, ListProperty<Int>>>())
        //enumProperties
        saveContainer.enumProperties.add(HashMap<String, HashMap<JdsFieldEnum<*>, ObjectProperty<Enum<*>>>>())
        saveContainer.enumCollections.add(HashMap<String, HashMap<JdsFieldEnum<*>, ListProperty<Enum<*>>>>())
        //objects
        saveContainer.objects.add(HashMap<String, HashMap<JdsFieldEntity<*>, ObjectProperty<JdsEntity>>>())
        //object arrays
        saveContainer.objectCollections.add(HashMap<String, HashMap<JdsFieldEntity<*>, ListProperty<JdsEntity>>>())
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

            //crt point
            entities.forEach { processCrt(jdsDb, it) }

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

    private fun processCrt(jdsDb: JdsDb, jdsEntity: JdsEntity) {
        jdsDb.tables.forEach {
            it.executeSave(jdsEntity, onPostSaveEventArguments)
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
                    println("Saving Overview [$record of $recordTotal]")
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    /**
     * @param blobProperties
     */
    private fun saveBlobs(writeToPrimaryDataTables: Boolean, blobProperties: HashMap<String, HashMap<Long, BlobProperty>>) {
        var record = 0
        try {
            val upsert = if (jdsDb.supportsStatements()) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveBlob()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveBlob())
            val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldBlobValues())
            for ((entityGuid, value) in blobProperties) {
                record++
                var innerRecord = 0
                val innerRecordSize = value.size
                if (innerRecordSize == 0) continue
                for ((fieldId, blobProperty) in value) {
                    innerRecord++
                    if (writeToPrimaryDataTables) {
                        upsert.setString("entityGuid", entityGuid)
                        upsert.setLong("fieldId", fieldId)
                        upsert.setBytes("value", blobProperty.get()!!)
                        upsert.addBatch()
                    }
                    if (jdsDb.isPrintingOutput)
                        println("Updating record [$record]. Blob fieldEntity [$innerRecord of $innerRecordSize]")
                    if (!jdsDb.isLoggingEdits) continue
                    log.setString(1, entityGuid)
                    log.setLong(2, fieldId)
                    log.setInt(3, 0)
                    log.setBytes(4, blobProperty.get()!!)
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
    private fun saveBooleans(writeToPrimaryDataTables: Boolean, booleanProperties: HashMap<String, HashMap<Long, BooleanProperty>>) {
        var record = 0
        try {
            val upsert = if (jdsDb.supportsStatements()) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveInteger()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveInteger())
            val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldIntegerValues())
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
                        println("Updating record [$record]. Boolean fieldEntity [$innerRecord of $innerRecordSize]")
                    if (!jdsDb.isLoggingEdits) continue
                    log.setString(1, entityGuid)
                    log.setLong(2, fieldId)
                    log.setInt(3, 0)
                    log.setInt(4, value)
                    if (!jdsDb.isLoggingAppendOnly) {
                        log.setString(5, entityGuid)
                        log.setLong(6, fieldId)
                        log.setInt(7, 0)
                        log.setInt(8, value)
                    }
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
    private fun saveIntegers(writeToPrimaryDataTables: Boolean, integerProperties: HashMap<String, HashMap<Long, IntegerProperty>>) {
        var record = 0
        try {
            val upsert = if (jdsDb.supportsStatements()) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveInteger()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveInteger())
            val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldIntegerValues())
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
                        println("Updating record [$record]. Integer fieldEntity [$innerRecord of $innerRecordSize]")
                    if (!jdsDb.isLoggingEdits) continue
                    log.setString(1, entityGuid)
                    log.setLong(2, fieldId)
                    log.setInt(3, 0)
                    log.setInt(4, value)
                    if (!jdsDb.isLoggingAppendOnly) {
                        log.setString(5, entityGuid)
                        log.setLong(6, fieldId)
                        log.setInt(7, 0)
                        log.setInt(8, value)
                    }
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
    private fun saveFloats(writeToPrimaryDataTables: Boolean, floatProperties: HashMap<String, HashMap<Long, FloatProperty>>) {
        var record = 0
        try {
            val upsert = if (jdsDb.supportsStatements()) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveFloat()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveFloat())
            val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldFloatValues())
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
                        System.out.printf("Updating record $record. Float fieldEntity [$innerRecord of $innerRecordSize]")
                    if (!jdsDb.isLoggingEdits) continue
                    log.setString(1, entityGuid)
                    log.setLong(2, fieldId)
                    log.setInt(3, 0)
                    log.setFloat(4, value)
                    if (!jdsDb.isLoggingAppendOnly) {
                        log.setString(5, entityGuid)
                        log.setLong(6, fieldId)
                        log.setInt(7, 0)
                        log.setFloat(8, value)
                    }
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
    private fun saveDoubles(writeToPrimaryDataTables: Boolean, doubleProperties: HashMap<String, HashMap<Long, DoubleProperty>>) {
        var record = 0
        try {
            val upsert = if (jdsDb.supportsStatements()) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveDouble()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveDouble())
            val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldDoubleValues())
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
                        println("Updating record $record. Double fieldEntity [$innerRecord of $innerRecordSize]")
                    if (!jdsDb.isLoggingEdits) continue
                    log.setString(1, entityGuid)
                    log.setLong(2, fieldId)
                    log.setInt(3, 0)
                    log.setDouble(4, value)
                    if (!jdsDb.isLoggingAppendOnly) {
                        log.setString(5, entityGuid)
                        log.setLong(6, fieldId)
                        log.setInt(7, 0)
                        log.setDouble(8, value)
                    }
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
    private fun saveLongs(writeToPrimaryDataTables: Boolean, longProperties: HashMap<String, HashMap<Long, LongProperty>>) {
        var record = 0
        try {
            val upsert = if (jdsDb.supportsStatements()) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveLong()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveLong())
            val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldLongValues())
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
                        System.out.printf("Updating record [$record]. Long fieldEntity [$innerRecord of $innerRecordSize]")
                    if (!jdsDb.isLoggingEdits) continue
                    log.setString(1, entityGuid)
                    log.setLong(2, fieldId)
                    log.setInt(3, 0)
                    log.setLong(4, value)
                    if (!jdsDb.isLoggingAppendOnly) {
                        log.setString(5, entityGuid)
                        log.setLong(6, fieldId)
                        log.setInt(7, 0)
                        log.setLong(8, value)
                    }
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
    private fun saveStrings(writeToPrimaryDataTables: Boolean, stringProperties: HashMap<String, HashMap<Long, StringProperty>>) {
        var record = 0
        try {
            val upsert = if (jdsDb.supportsStatements()) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveString()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveString())
            val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldTextValues())
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
                        println("Updating record [$record]. Text fieldEntity [$innerRecord of $innerRecordSize]")
                    if (!jdsDb.isLoggingEdits) continue
                    log.setString(1, entityGuid)
                    log.setLong(2, fieldId)
                    log.setInt(3, 0)
                    log.setString(4, value)
                    if (!jdsDb.isLoggingAppendOnly) {
                        log.setString(5, entityGuid)
                        log.setLong(6, fieldId)
                        log.setInt(7, 0)
                        log.setString(8, value)
                    }
                    log.addBatch()
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    private fun saveDateConstructs(writeToPrimaryDataTables: Boolean,
                                   monthDayProperties: HashMap<String, HashMap<Long, ObjectProperty<MonthDay>>>,
                                   yearMonthProperties: HashMap<String, HashMap<Long, ObjectProperty<Temporal>>>,
                                   periodProperties: HashMap<String, HashMap<Long, ObjectProperty<Period>>>,
                                   durationProperties: HashMap<String, HashMap<Long, ObjectProperty<Duration>>>) {
        var record = 0
        try {
            val upsertText = if (jdsDb.supportsStatements()) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveString()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveString())
            val upsertLong = if (jdsDb.supportsStatements()) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveLong()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveLong())

            val logText = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldTextValues())
            val logLong = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldLongValues())

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
                        println("Updating record [$record]. MonthDay fieldEntity [$innerRecord of $innerRecordSize]")
                    if (!jdsDb.isLoggingEdits) continue
                    logText.setString(1, entityGuid)
                    logText.setLong(2, fieldId)
                    logText.setInt(3, 0)
                    logText.setString(4, value)
                    if (!jdsDb.isLoggingAppendOnly) {
                        logText.setString(5, entityGuid)
                        logText.setLong(6, fieldId)
                        logText.setInt(7, 0)
                        logText.setString(8, value)
                    }
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
                        println("Updating record [$record]. YearMonth fieldEntity [$innerRecord of $innerRecordSize]")
                    if (!jdsDb.isLoggingEdits) continue
                    logText.setString(1, entityGuid)
                    logText.setLong(2, fieldId)
                    logText.setInt(3, 0)
                    logText.setString(4, value)
                    if (!jdsDb.isLoggingAppendOnly) {
                        logText.setString(5, entityGuid)
                        logText.setLong(6, fieldId)
                        logText.setInt(7, 0)
                        logText.setString(8, value)
                    }
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
                        println("Updating record [$record]. Period fieldEntity [$innerRecord of $innerRecordSize]")
                    if (!jdsDb.isLoggingEdits) continue
                    logText.setString(1, entityGuid)
                    logText.setLong(2, fieldId)
                    logText.setInt(3, 0)
                    logText.setString(4, value)
                    if (!jdsDb.isLoggingAppendOnly) {
                        logText.setString(5, entityGuid)
                        logText.setLong(6, fieldId)
                        logText.setInt(7, 0)
                        logText.setString(8, value)
                    }
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
                        println("Updating record [$record]. Duration fieldEntity [$innerRecord of $innerRecordSize]")
                    if (!jdsDb.isLoggingEdits) continue
                    logLong.setString(1, entityGuid)
                    logLong.setLong(2, fieldId)
                    logLong.setInt(3, 0)
                    logLong.setLong(4, value)
                    if (!jdsDb.isLoggingAppendOnly) {
                        logLong.setString(5, entityGuid)
                        logLong.setLong(6, fieldId)
                        logLong.setInt(7, 0)
                        logLong.setLong(8, value)
                    }
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
    private fun saveDatesAndDateTimes(writeToPrimaryDataTables: Boolean, localDateTimeProperties: HashMap<String, HashMap<Long, ObjectProperty<Temporal>>>, localDateProperties: HashMap<String, HashMap<Long, ObjectProperty<Temporal>>>) {
        var record = 0
        try {
            val upsert = if (jdsDb.supportsStatements()) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveDateTime()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveDateTime())
            val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldDateTimeValues())
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
                        println("Updating record [$record]. LocalDateTime fieldEntity [$innerRecord of $innerRecordSize]")
                    if (!jdsDb.isLoggingEdits) continue
                    log.setString(1, entityGuid)
                    log.setLong(2, fieldId)
                    log.setInt(3, 0)
                    log.setTimestamp(4, Timestamp.valueOf(localDateTime))
                    if (!jdsDb.isLoggingAppendOnly) {
                        log.setString(5, entityGuid)
                        log.setLong(6, fieldId)
                        log.setInt(7, 0)
                        log.setTimestamp(8, Timestamp.valueOf(localDateTime))
                    }
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
                        println("Updating record [$record]. LocalDate fieldEntity [$innerRecord of $innerRecordSize]")
                    if (!jdsDb.isLoggingEdits) continue
                    log.setString(1, entityGuid)
                    log.setLong(2, fieldId)
                    log.setInt(3, 0)
                    log.setTimestamp(4, Timestamp.valueOf(localDate.atStartOfDay()))
                    if (!jdsDb.isLoggingAppendOnly) {
                        log.setString(5, entityGuid)
                        log.setLong(6, fieldId)
                        log.setInt(7, 0)
                        log.setTimestamp(8, Timestamp.valueOf(localDate.atStartOfDay()))
                    }
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
    private fun saveTimes(writeToPrimaryDataTables: Boolean, localTimeProperties: HashMap<String, HashMap<Long, ObjectProperty<Temporal>>>) {
        var record = 0
        try {
            val upsert = if (jdsDb.supportsStatements()) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveTime()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveTime())
            val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldIntegerValues())
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
                        println("Updating record [$record]. LocalTime fieldEntity [$innerRecord of $innerRecordSize]\n")
                    if (!jdsDb.isLoggingEdits) continue
                    log.setString(1, entityGuid)
                    log.setLong(2, fieldId)
                    log.setInt(3, 0)
                    log.setInt(4, secondOfDay)
                    if (!jdsDb.isLoggingAppendOnly) {
                        log.setString(5, entityGuid)
                        log.setLong(6, fieldId)
                        log.setInt(7, 0)
                        log.setInt(8, secondOfDay)
                    }
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
    private fun saveZonedDateTimes(writeToPrimaryDataTables: Boolean, zonedDateProperties: HashMap<String, HashMap<Long, ObjectProperty<Temporal>>>) {
        var record = 0
        try {
            val upsert = if (jdsDb.supportsStatements()) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveZonedDateTime()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveZonedDateTime())
            val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldLongValues())
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
                        System.out.printf("Updating record [$record]. ZonedDateTime fieldEntity [$innerRecord of $innerRecordSize]")
                    if (!jdsDb.isLoggingEdits) continue
                    log.setString(1, entityGuid)
                    log.setLong(2, fieldId)
                    log.setInt(3, 0)
                    log.setLong(4, zonedDateTime.toInstant().toEpochMilli())
                    if (!jdsDb.isLoggingAppendOnly) {
                        log.setString(5, entityGuid)
                        log.setLong(6, fieldId)
                        log.setInt(7, 0)
                        log.setLong(8, zonedDateTime.toInstant().toEpochMilli())
                    }
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
    fun saveEnums(writeToPrimaryDataTables: Boolean, enums: HashMap<String, HashMap<JdsFieldEnum<*>, ObjectProperty<Enum<*>>>>) {
        var record = 0
        try {
            val upsert = if (jdsDb.supportsStatements()) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveInteger()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveInteger())
            val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldIntegerValues())
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
                        println("Updating record [$record]. Enum fieldEntity [$innerRecord of $innerRecordSize]")
                    if (!jdsDb.isLoggingEdits) continue
                    log.setString(1, entityGuid)
                    log.setLong(2, jdsFieldEnum.field.id)
                    log.setInt(3, 0)
                    log.setInt(4, jdsFieldEnum.indexOf(value))
                    if (!jdsDb.isLoggingAppendOnly) {
                        log.setString(5, entityGuid)
                        log.setLong(6, jdsFieldEnum.field.id)
                        log.setInt(7, 0)
                        log.setInt(8, jdsFieldEnum.indexOf(value))
                    }
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
    private fun saveArrayDates(writeToPrimaryDataTables: Boolean, dateTimeArrayProperties: HashMap<String, HashMap<Long, ListProperty<LocalDateTime>>>) {
        val deleteSql = "DELETE FROM JdsStoreDateTimeArray WHERE FieldId = ? AND EntityGuid = ?"
        val insertSql = "INSERT INTO JdsStoreDateTimeArray (Sequence,Value,FieldId,EntityGuid) VALUES (?,?,?,?)"
        try {
            val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldDateTimeValues())
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
                            log.setString(1, entityGuid)
                            log.setLong(2, fieldId)
                            log.setInt(3, index.get())
                            log.setTimestamp(4, Timestamp.valueOf(value))
                            if (!jdsDb.isLoggingAppendOnly) {
                                log.setString(5, entityGuid)
                                log.setLong(6, fieldId)
                                log.setInt(7, index.get())
                                log.setTimestamp(8, Timestamp.valueOf(value))
                            }
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
                            println("Inserting array record [$record]. DateTime fieldEntity [$innerRecord of $innerTotal]")
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
    private fun saveArrayFloats(writeToPrimaryDataTables: Boolean, floatArrayProperties: HashMap<String, HashMap<Long, ListProperty<Float>>>) {
        val deleteSql = "DELETE FROM JdsStoreFloatArray WHERE FieldId = ? AND EntityGuid = ?"
        val insertSql = "INSERT INTO JdsStoreFloatArray (FieldId,EntityGuid,Value,Sequence) VALUES (?,?,?,?)"
        try {
            val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldFloatValues())
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
                            log.setString(1, entityGuid)
                            log.setLong(2, fieldId)
                            log.setInt(3, index.get())
                            log.setFloat(4, value!!)
                            if (!jdsDb.isLoggingAppendOnly) {
                                log.setString(5, entityGuid)
                                log.setLong(6, fieldId)
                                log.setInt(7, index.get())
                                log.setFloat(8, value!!)
                            }
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
                            println("Inserting array record [$record]. Float fieldEntity [$innerRecord of $innerTotal]")

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
    private fun saveArrayIntegers(writeToPrimaryDataTables: Boolean, integerArrayProperties: HashMap<String, HashMap<Long, ListProperty<Int>>>) {
        val deleteSql = "DELETE FROM JdsStoreIntegerArray WHERE FieldId = :fieldId AND EntityGuid = :entityGuid"
        val insertSql = "INSERT INTO JdsStoreIntegerArray (FieldId,EntityGuid,Sequence,Value) VALUES (:fieldId, :entityGuid, :sequence, :value)"
        try {
            val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldIntegerValues())
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
                            log.setString(1, entityGuid)
                            log.setLong(2, fieldId)
                            log.setInt(3, index.get())
                            log.setInt(4, value!!)
                            if (!jdsDb.isLoggingAppendOnly) {
                                log.setString(5, entityGuid)
                                log.setLong(6, fieldId)
                                log.setInt(7, index.get())
                                log.setInt(8, value!!)
                            }
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
                            println("Inserting array record [$record]. Integer fieldEntity [$innerRecord of $innerTotal]")
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
    private fun saveArrayDoubles(writeToPrimaryDataTables: Boolean, doubleArrayProperties: HashMap<String, HashMap<Long, ListProperty<Double>>>) {
        val deleteSql = "DELETE FROM JdsStoreDoubleArray WHERE FieldId = :fieldId AND EntityGuid = :entityGuid"
        val insertSql = "INSERT INTO JdsStoreDoubleArray (FieldId,EntityGuid,Sequence,Value) VALUES (:fieldId, :entityGuid, :sequence, :value)"
        try {
            val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldDoubleValues())
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
                            log.setString(1, entityGuid)
                            log.setLong(2, fieldId)
                            log.setInt(3, index.get())
                            log.setDouble(4, value!!)
                            if (!jdsDb.isLoggingAppendOnly) {
                                log.setString(5, entityGuid)
                                log.setLong(6, fieldId)
                                log.setInt(7, index.get())
                                log.setDouble(8, value!!)
                            }
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
                            println("Inserting array record [$record]. Double fieldEntity [$innerRecord of $innerTotal]")
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
    private fun saveArrayLongs(writeToPrimaryDataTables: Boolean, longArrayProperties: HashMap<String, HashMap<Long, ListProperty<Long>>>) {
        val deleteSql = "DELETE FROM JdsStoreDoubleArray WHERE FieldId = ? AND EntityGuid = ?"
        val insertSql = "INSERT INTO JdsStoreDoubleArray (FieldId,EntityGuid,Sequence,Value) VALUES (?,?,?,?)"
        try {
            val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldLongValues())
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
                            log.setString(1, entityGuid)
                            log.setLong(2, fieldId)
                            log.setInt(3, index.get())
                            log.setLong(4, value!!)
                            if (!jdsDb.isLoggingAppendOnly) {
                                log.setString(5, entityGuid)
                                log.setLong(6, fieldId)
                                log.setInt(7, index.get())
                                log.setLong(8, value!!)
                            }
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
                            println("Inserting array record [$record]. Long fieldEntity [$innerRecord of $innerTotal]")
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
    private fun saveArrayStrings(writeToPrimaryDataTables: Boolean, stringArrayProperties: HashMap<String, HashMap<Long, ListProperty<String>>>) {
        val deleteSql = "DELETE FROM JdsStoreTextArray WHERE FieldId = :fieldId AND EntityGuid = :entityGuid"
        val insertSql = "INSERT INTO JdsStoreTextArray (FieldId,EntityGuid,Sequence,Value) VALUES (:fieldId, :entityGuid, :sequence, :value)"
        try {
            val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldTextValues())
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
                            log.setString(1, entityGuid)
                            log.setLong(2, fieldId)
                            log.setString(3, value)
                            log.setInt(4, index.get())
                            if (!jdsDb.isLoggingAppendOnly) {
                                log.setString(5, entityGuid)
                                log.setLong(6, fieldId)
                                log.setString(7, value)
                                log.setInt(8, index.get())
                            }
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
                            println("Inserting array record [$record]. String fieldEntity [$innerRecord of $innerTotal]")
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
    private fun saveEnumCollections(writeToPrimaryDataTables: Boolean, enumStrings: HashMap<String, HashMap<JdsFieldEnum<*>, ListProperty<Enum<*>>>>) {
        var record = 0
        val recordTotal = enumStrings.size
        val deleteSql = "DELETE FROM JdsStoreIntegerArray WHERE FieldId = :fieldId AND EntityGuid = :entityGuid"
        val insertSql = "INSERT INTO JdsStoreIntegerArray (FieldId,EntityGuid,Sequence,Value) VALUES (:fieldId, :entityGuid, :sequence, :value)"
        try {
            val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldIntegerValues())
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
                            log.setString(1, entityGuid)
                            log.setLong(2, jdsFieldEnum.field.id)
                            log.setInt(3, sequence)
                            log.setInt(4, jdsFieldEnum.indexOf(anEnum))
                            if (!jdsDb.isLoggingAppendOnly) {
                                log.setString(5, entityGuid)
                                log.setLong(6, jdsFieldEnum.field.id)
                                log.setInt(7, sequence)
                                log.setInt(8, jdsFieldEnum.indexOf(anEnum))
                            }
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
                            println("Updating enum [$sequence]. Object fieldEntity [$record of $recordTotal]")
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
    private fun saveAndBindObjectArrays(connection: Connection, objectArrayProperties: HashMap<String, HashMap<JdsFieldEntity<*>, ListProperty<JdsEntity>>>) {
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
                        println("Binding array object ${record.get()}")
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
    private fun saveAndBindObjects(connection: Connection, objectProperties: HashMap<String, HashMap<JdsFieldEntity<*>, ObjectProperty<JdsEntity>>>) {
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
                        println("Binding object ${record.get()}")
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
        @Deprecated("please refer to <a href=\"https://github.com/SubiyaCryolite/Jenesis-Data-Store\"> the readme</a> for the most up to date CRUD approach", ReplaceWith("JdsSave(jdsDb, batchSize, entityVersions).call()", "io.github.subiyacryolite.jds.JdsSave"))
        @Throws(Exception::class)
        fun save(jdsDb: JdsDb, batchSize: Int, entities: Collection<JdsEntity>) {
            JdsSave(jdsDb, batchSize, entities).call()
        }

        /**
         * @param jdsDb
         * @param batchSize
         * @param entities
         */
        @Deprecated("please refer to <a href=\"https://github.com/SubiyaCryolite/Jenesis-Data-Store\"> the readme</a> for the most up to date CRUD approach", ReplaceWith("save(jdsDb, batchSize, Arrays.asList(*entityVersions))", "io.github.subiyacryolite.jds.JdsSave.Companion.save", "java.util.Arrays"))
        @Throws(Exception::class)
        fun save(jdsDb: JdsDb, batchSize: Int, vararg entities: JdsEntity) {
            save(jdsDb, batchSize, Arrays.asList(*entities))
        }
    }
}