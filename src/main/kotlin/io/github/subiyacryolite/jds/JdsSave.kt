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
import io.github.subiyacryolite.jds.events.JdsSaveEvent
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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.collections.HashMap
import kotlin.coroutines.experimental.buildSequence

/**
 * This class is responsible for persisting on or more [JdsEntities][JdsEntity]
 */
class JdsSave private constructor(private val alternateConnections: ConcurrentMap<Int, Connection>, private val jdsDb: JdsDb, private val connection: Connection, private val batchSize: Int, private val entities: Iterable<JdsEntity>, private val recursiveInnerCall: Boolean, private val onPreSaveEventArguments: OnPreSaveEventArguments = OnPreSaveEventArguments(jdsDb, connection, alternateConnections), private val onPostSaveEventArguments: OnPostSaveEventArguments = OnPostSaveEventArguments(jdsDb, connection, alternateConnections)) : Callable<Boolean> {

    private val jdsSaveEvents = LinkedList<JdsSaveEvent>()

    /**
     * @param jdsDb
     * @param entities
     */
    @Throws(SQLException::class, ClassNotFoundException::class)
    constructor(jdsDb: JdsDb, vararg entities: JdsEntity) : this(jdsDb, entities.asIterable())

    /**
     * @param jdsDb
     * @param entities
     */
    @Throws(SQLException::class, ClassNotFoundException::class)
    constructor(jdsDb: JdsDb, batchSize: Int, vararg entities: JdsEntity) : this(jdsDb, batchSize, entities.asIterable())

    /**
     * @param jdsDb
     * @param entities
     */
    @Throws(SQLException::class, ClassNotFoundException::class)
    constructor(jdsDb: JdsDb, entities: Iterable<JdsEntity>) : this(ConcurrentHashMap(), jdsDb, jdsDb.getConnection(), 0, entities, false)

    /**
     * @param jdsDb
     * @param batchSize
     * @param entities
     */
    @Throws(SQLException::class, ClassNotFoundException::class)
    constructor(jdsDb: JdsDb, batchSize: Int, entities: Iterable<JdsEntity>) : this(ConcurrentHashMap(), jdsDb, jdsDb.getConnection(), batchSize, entities, false)

    /**
     * Computes a result, or throws an exception if unable to do so.
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Throws(Exception::class)
    override fun call(): Boolean {
        val saveContainer = JdsSaveContainer()
        val batchEntities = ArrayList<MutableCollection<JdsEntity>>()
        setupBatches(batchSize, entities, saveContainer, batchEntities)
        var step = 0
        val steps = batchEntities.size
        for (current in batchEntities) {
            saveInner(current, saveContainer, step, steps)
            saveContainer.reset()//don't repersist the same files in batching
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
    private fun setupBatches(batchSize: Int, entities: Iterable<JdsEntity>, container: JdsSaveContainer, batchEntities: MutableList<MutableCollection<JdsEntity>>) {
        //create batches
        var currentBatch = 0
        //default bach is 0 or -1 which means one large chunk. Anything above is a single batch
        var iteration = 0
        if (batchSize > 0) {
            entities.forEach {
                if (iteration == batchSize) {
                    currentBatch++
                    iteration = 0
                }
                if (iteration == 0) {
                    createBatchCollection(container, batchEntities)
                }
                batchEntities[currentBatch].add(it)
                iteration++
            }
        } else {
            //single large batch, good luck
            createBatchCollection(container, batchEntities)
            entities.forEach { batchEntities[0].add(it) }
        }
    }

    /**
     * @param saveContainer
     * @param batchEntities
     */
    private fun createBatchCollection(saveContainer: JdsSaveContainer, batchEntities: MutableList<MutableCollection<JdsEntity>>) {
        batchEntities.add(ArrayList())
        saveContainer.overviews.add(HashSet())
        //time constructs
        saveContainer.localDateTimeProperties.add(HashMap())
        saveContainer.zonedDateTimeProperties.add(HashMap())
        saveContainer.localTimeProperties.add(HashMap())
        saveContainer.localDateProperties.add(HashMap())
        saveContainer.monthDayProperties.add(HashMap())
        saveContainer.yearMonthProperties.add(HashMap())
        saveContainer.periodProperties.add(HashMap())
        saveContainer.durationProperties.add(HashMap())
        //primitives
        saveContainer.booleanProperties.add(HashMap())
        saveContainer.floatProperties.add(HashMap())
        saveContainer.doubleProperties.add(HashMap())
        saveContainer.longProperties.add(HashMap())
        saveContainer.integerProperties.add(HashMap())
        //string
        saveContainer.stringProperties.add(HashMap())
        //blob
        saveContainer.blobProperties.add(HashMap())
        //arrays
        saveContainer.stringCollections.add(HashMap())
        saveContainer.localDateTimeCollections.add(HashMap())
        saveContainer.floatCollections.add(HashMap())
        saveContainer.doubleCollections.add(HashMap())
        saveContainer.longCollections.add(HashMap())
        saveContainer.integerCollections.add(HashMap())
        //enumProperties
        saveContainer.enumProperties.add(HashMap())
        saveContainer.enumCollections.add(HashMap())
        //objects
        saveContainer.objects.add(HashMap())
        //object arrays
        saveContainer.objectCollections.add(HashMap())
    }

    /**
     * @param entities
     * @param saveContainer
     * @param step
     * @param steps
     */
    @Throws(Exception::class)
    private fun saveInner(entities: Iterable<JdsEntity>, saveContainer: JdsSaveContainer, step: Int, steps: Int) {
        //fire
        entities.forEach {
            //update the modified date to time of commit
            it.overview.dateModified = LocalDateTime.now()
            saveContainer.overviews[step].add(it.overview)
            //assign properties
            it.assign(step, saveContainer)
        }
        //share one connection for raw saves, helps with performance
        val finalStep = !recursiveInnerCall && step == steps - 1

        try {
            //always save overviews first
            if (jdsDb.isWritingOverviewFields)
                saveOverviews(saveContainer.overviews[step])

            //ensure that overviews are submitted before handing over to listeners
            entities.filterIsInstance<JdsSaveListener>().forEach { it.onPreSave(onPreSaveEventArguments) }

            if (jdsDb.isLoggingEdits || jdsDb.isWritingToPrimaryDataTables) {
                //time constraints
                saveDateConstructs(
                        saveContainer.monthDayProperties[step],
                        saveContainer.yearMonthProperties[step],
                        saveContainer.periodProperties[step],
                        saveContainer.durationProperties[step])
                saveDatesAndDateTimes(saveContainer.localDateTimeProperties[step], saveContainer.localDateProperties[step])
                saveZonedDateTimes(saveContainer.zonedDateTimeProperties[step])
                saveTimes(saveContainer.localTimeProperties[step])
                //primitives
                saveBooleans(saveContainer.booleanProperties[step])
                saveLongs(saveContainer.longProperties[step])
                saveDoubles(saveContainer.doubleProperties[step])
                saveIntegers(saveContainer.integerProperties[step])
                saveFloats(saveContainer.floatProperties[step])
                //strings
                saveStrings(saveContainer.stringProperties[step])
                //blobs
                saveBlobs(saveContainer.blobProperties[step])
                //enumProperties
                saveEnums(saveContainer.enumProperties[step])
            }
            if (jdsDb.isLoggingEdits || jdsDb.isWritingArrayValues) {
                //array properties [NOTE arrays have old entries deleted first, for cases where a user reduced the amount of entries in the collection]
                saveArrayDates(saveContainer.localDateTimeCollections[step])
                saveArrayStrings(saveContainer.stringCollections[step])
                saveArrayLongs(saveContainer.longCollections[step])
                saveArrayDoubles(saveContainer.doubleCollections[step])
                saveArrayIntegers(saveContainer.integerCollections[step])
                saveArrayFloats(saveContainer.floatCollections[step])
                saveEnumCollections(saveContainer.enumCollections[step])
            }
            //objects and object arrays
            //object entity overviews and entity bindings are ALWAYS persisted
            saveAndBindObjects(connection, saveContainer.objects[step])
            saveAndBindObjectArrays(connection, saveContainer.objectCollections[step])

            entities.filterIsInstance<JdsSaveListener>().forEach { it.onPostSave(onPostSaveEventArguments) }

            //crt point
            val nestedEntities = overviewsSequence(entities)
            nestedEntities.forEach {
                processCrt(jdsDb, connection, alternateConnections, it)
            }

            //respect execution sequence
            //respect JDS batches in each call
            onPreSaveEventArguments.executeBatches()
            onPostSaveEventArguments.executeBatches()
        } catch (ex: Exception) {
            throw ex
        } finally {
            if (finalStep) {
                jdsSaveEvents.forEach { it.onSave(entities, connection) }
                onPreSaveEventArguments.closeBatches()
                onPostSaveEventArguments.closeBatches()
                connection.close()
                alternateConnections.forEach { it.value.close() }
            }
        }
    }

    private fun overviewsSequence(hashSet: Iterable<JdsEntity>): Sequence<JdsEntity> = buildSequence {
        hashSet.forEach {
            yieldAll(it.yieldOverviews())
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
    private fun saveOverviews(overviews: Iterable<IJdsOverview>) = try {
        var record = 0
        var recordTotal = 1
        val upsert = if (jdsDb.supportsStatements) onPreSaveEventArguments.getOrAddNamedCall(jdsDb.saveOverview()) else onPreSaveEventArguments.getOrAddNamedStatement(jdsDb.saveOverview())
        val inheritance = if (jdsDb.supportsStatements) onPreSaveEventArguments.getOrAddNamedCall(jdsDb.saveOverviewInheritance()) else onPreSaveEventArguments.getOrAddNamedStatement(jdsDb.saveOverviewInheritance())
        for (overview in overviews) {
            record++
            //Entity Overview
            upsert.setString("uuid", overview.uuid)
            upsert.setTimestamp("dateCreated", Timestamp.valueOf(overview.dateCreated))
            upsert.setTimestamp("dateModified", Timestamp.valueOf(LocalDateTime.now())) //always update date modified!!!
            upsert.setBoolean("live", overview.live)
            upsert.setLong("version", overview.version) //always update date modified!!!
            upsert.addBatch()
            //Entity Inheritance
            inheritance.setString("uuid", overview.uuid)
            inheritance.setLong("entityId", overview.entityId)
            inheritance.addBatch()
            if (jdsDb.isPrintingOutput) {
                println("Saving Overview [$record of $recordTotal]")
                recordTotal++
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**

     * @param blobProperties
     */
    private fun saveBlobs(blobProperties: HashMap<String, HashMap<Long, BlobProperty>>) = try {
        var record = 0
        val upsert = if (jdsDb.supportsStatements) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveBlob()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveBlob())
        val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldBlobValues())
        for ((uuid, value) in blobProperties) {
            record++
            var innerRecord = 0
            val innerRecordSize = value.size
            if (innerRecordSize == 0) continue
            for ((fieldId, blobProperty) in value) {
                innerRecord++
                if (jdsDb.isWritingToPrimaryDataTables) {
                    upsert.setString("uuid", uuid)
                    upsert.setLong("fieldId", fieldId)
                    upsert.setBytes("value", blobProperty.get()!!)
                    upsert.addBatch()
                }
                if (jdsDb.isPrintingOutput)
                    println("Updating record [$record]. Blob field [$innerRecord of $innerRecordSize]")
                if (!jdsDb.isLoggingEdits) continue
                log.setString(1, uuid)
                log.setLong(2, fieldId)
                log.setInt(3, 0)
                log.setBytes(4, blobProperty.get()!!)
                log.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param booleanProperties
     */
    private fun saveBooleans(booleanProperties: HashMap<String, HashMap<Long, BooleanProperty>>) = try {
        var record = 0
        val upsert = if (jdsDb.supportsStatements) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveBoolean()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveBoolean())
        val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldBooleanValues())
        for ((uuid, value1) in booleanProperties) {
            record++
            var innerRecord = 0
            val innerRecordSize = value1.size
            if (innerRecordSize == 0) continue
            for ((fieldId, value2) in value1) {
                innerRecord++
                val value = value2.get()
                if (jdsDb.isWritingToPrimaryDataTables) {
                    upsert.setString("uuid", uuid)
                    upsert.setLong("fieldId", fieldId)
                    upsert.setBoolean("value", value)
                    upsert.addBatch()
                }
                if (jdsDb.isPrintingOutput)
                    println("Updating record [$record]. Boolean field [$innerRecord of $innerRecordSize]")
                if (!jdsDb.isLoggingEdits) continue
                log.setString(1, uuid)
                log.setLong(2, fieldId)
                log.setInt(3, 0)
                log.setBoolean(4, value)
                if (!jdsDb.isLoggingAppendOnly) {
                    log.setString(5, uuid)
                    log.setLong(6, fieldId)
                    log.setInt(7, 0)
                    log.setBoolean(8, value)
                }
                log.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param integerProperties
     */
    private fun saveIntegers(integerProperties: HashMap<String, HashMap<Long, IntegerProperty>>) = try {
        var record = 0
        val upsert = if (jdsDb.supportsStatements) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveInteger()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveInteger())
        val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldIntegerValues())
        for ((uuid, value1) in integerProperties) {
            record++
            var innerRecord = 0
            val innerRecordSize = value1.size
            if (innerRecordSize == 0) continue
            for ((fieldId, value2) in value1) {
                innerRecord++
                val value = value2.get()
                if (jdsDb.isWritingToPrimaryDataTables) {
                    upsert.setString("uuid", uuid)
                    upsert.setLong("fieldId", fieldId)
                    upsert.setInt("value", value)
                    upsert.addBatch()
                }
                if (jdsDb.isPrintingOutput)
                    println("Updating record [$record]. Integer field [$innerRecord of $innerRecordSize]")
                if (!jdsDb.isLoggingEdits) continue
                log.setString(1, uuid)
                log.setLong(2, fieldId)
                log.setInt(3, 0)
                log.setInt(4, value)
                if (!jdsDb.isLoggingAppendOnly) {
                    log.setString(5, uuid)
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

    /**
     * @param floatProperties
     */
    private fun saveFloats(floatProperties: HashMap<String, HashMap<Long, FloatProperty>>) = try {
        var record = 0
        val upsert = if (jdsDb.supportsStatements) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveFloat()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveFloat())
        val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldFloatValues())
        for ((uuid, value1) in floatProperties) {
            record++
            var innerRecord = 0
            val innerRecordSize = value1.size
            if (innerRecordSize == 0) continue
            for ((fieldId, value2) in value1) {
                innerRecord++
                val value = value2.get()
                if (jdsDb.isWritingToPrimaryDataTables) {
                    upsert.setString("uuid", uuid)
                    upsert.setLong("fieldId", fieldId)
                    upsert.setFloat("value", value)
                    upsert.addBatch()
                }
                if (jdsDb.isPrintingOutput)
                    System.out.printf("Updating record $record. Float field [$innerRecord of $innerRecordSize]")
                if (!jdsDb.isLoggingEdits) continue
                log.setString(1, uuid)
                log.setLong(2, fieldId)
                log.setInt(3, 0)
                log.setFloat(4, value)
                if (!jdsDb.isLoggingAppendOnly) {
                    log.setString(5, uuid)
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

    /**
     * @param doubleProperties
     */
    private fun saveDoubles(doubleProperties: HashMap<String, HashMap<Long, DoubleProperty>>) = try {
        var record = 0
        val upsert = if (jdsDb.supportsStatements) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveDouble()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveDouble())
        val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldDoubleValues())
        for ((uuid, value1) in doubleProperties) {
            record++
            var innerRecord = 0
            val innerRecordSize = value1.size
            if (innerRecordSize == 0) continue
            for ((fieldId, value2) in value1) {
                innerRecord++
                val value = value2.get()
                if (jdsDb.isWritingToPrimaryDataTables) {
                    upsert.setString("uuid", uuid)
                    upsert.setLong("fieldId", fieldId)
                    upsert.setDouble("value", value)
                    upsert.addBatch()
                }
                if (jdsDb.isPrintingOutput)
                    println("Updating record $record. Double field [$innerRecord of $innerRecordSize]")
                if (!jdsDb.isLoggingEdits) continue
                log.setString(1, uuid)
                log.setLong(2, fieldId)
                log.setInt(3, 0)
                log.setDouble(4, value)
                if (!jdsDb.isLoggingAppendOnly) {
                    log.setString(5, uuid)
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

    /**
     * @param longProperties
     */
    private fun saveLongs(longProperties: HashMap<String, HashMap<Long, LongProperty>>) = try {
        var record = 0
        val upsert = if (jdsDb.supportsStatements) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveLong()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveLong())
        val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldLongValues())
        for ((uuid, value1) in longProperties) {
            record++
            var innerRecord = 0
            val innerRecordSize = value1.size
            if (innerRecordSize == 0) continue
            for ((fieldId, value2) in value1) {
                innerRecord++
                val value = value2.get()
                if (jdsDb.isWritingToPrimaryDataTables) {
                    upsert.setString("uuid", uuid)
                    upsert.setLong("fieldId", fieldId)
                    upsert.setLong("value", value)
                    upsert.addBatch()
                }
                if (jdsDb.isPrintingOutput)
                    System.out.printf("Updating record [$record]. Long field [$innerRecord of $innerRecordSize]")
                if (!jdsDb.isLoggingEdits) continue
                log.setString(1, uuid)
                log.setLong(2, fieldId)
                log.setInt(3, 0)
                log.setLong(4, value)
                if (!jdsDb.isLoggingAppendOnly) {
                    log.setString(5, uuid)
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

    /**
     * @param stringProperties
     */
    private fun saveStrings(stringProperties: HashMap<String, HashMap<Long, StringProperty>>) = try {
        var record = 0
        val upsert = if (jdsDb.supportsStatements) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveString()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveString())
        val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldStringValues())
        for ((uuid, value1) in stringProperties) {
            record++
            var innerRecord = 0
            val innerRecordSize = value1.size
            if (innerRecordSize == 0) continue
            for ((fieldId, value2) in value1) {
                innerRecord++
                val value = value2.get()
                if (jdsDb.isWritingToPrimaryDataTables) {
                    upsert.setString("uuid", uuid)
                    upsert.setLong("fieldId", fieldId)
                    upsert.setString("value", value)
                    upsert.addBatch()
                }
                if (jdsDb.isPrintingOutput)
                    println("Updating record [$record]. Text field [$innerRecord of $innerRecordSize]")
                if (!jdsDb.isLoggingEdits) continue
                log.setString(1, uuid)
                log.setLong(2, fieldId)
                log.setInt(3, 0)
                log.setString(4, value)
                if (!jdsDb.isLoggingAppendOnly) {
                    log.setString(5, uuid)
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

    /**
     * @param monthDayProperties
     * @param yearMonthProperties
     * @param periodProperties
     * @param durationProperties
     */
    private fun saveDateConstructs(
            monthDayProperties: HashMap<String, HashMap<Long, ObjectProperty<MonthDay>>>,
            yearMonthProperties: HashMap<String, HashMap<Long, ObjectProperty<Temporal>>>,
            periodProperties: HashMap<String, HashMap<Long, ObjectProperty<Period>>>,
            durationProperties: HashMap<String, HashMap<Long, ObjectProperty<Duration>>>) = try {
        var record = 0
        val upsertText = if (jdsDb.supportsStatements) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveString()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveString())
        val upsertLong = if (jdsDb.supportsStatements) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveLong()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveLong())

        val logText = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldStringValues())
        val logLong = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldLongValues())

        for ((uuid, hashMap) in monthDayProperties) {
            record++
            var innerRecord = 0
            val innerRecordSize = hashMap.size
            if (innerRecordSize == 0) continue
            for ((fieldId, monthDayProperty) in hashMap) {
                innerRecord++
                val monthDay = monthDayProperty.get()
                val value = monthDay.toString()
                if (jdsDb.isWritingToPrimaryDataTables) {
                    upsertText.setString("uuid", uuid)
                    upsertText.setLong("fieldId", fieldId)
                    upsertText.setString("value", value)
                    upsertText.addBatch()
                }
                if (jdsDb.isPrintingOutput)
                    println("Updating record [$record]. MonthDay field [$innerRecord of $innerRecordSize]")
                if (!jdsDb.isLoggingEdits) continue
                logText.setString(1, uuid)
                logText.setLong(2, fieldId)
                logText.setInt(3, 0)
                logText.setString(4, value)
                if (!jdsDb.isLoggingAppendOnly) {
                    logText.setString(5, uuid)
                    logText.setLong(6, fieldId)
                    logText.setInt(7, 0)
                    logText.setString(8, value)
                }
                logText.addBatch()
            }
        }

        for ((uuid, hashMap) in yearMonthProperties) {
            record++
            var innerRecord = 0
            val innerRecordSize = hashMap.size
            if (innerRecordSize == 0) continue
            for ((fieldId, yearMonthProperty) in hashMap) {
                innerRecord++
                val yearMonth = yearMonthProperty.get() as YearMonth
                val value = yearMonth.toString()
                if (jdsDb.isWritingToPrimaryDataTables) {
                    upsertText.setString("uuid", uuid)
                    upsertText.setLong("fieldId", fieldId)
                    upsertText.setString("value", value)
                    upsertText.addBatch()
                }
                if (jdsDb.isPrintingOutput)
                    println("Updating record [$record]. YearMonth field [$innerRecord of $innerRecordSize]")
                if (!jdsDb.isLoggingEdits) continue
                logText.setString(1, uuid)
                logText.setLong(2, fieldId)
                logText.setInt(3, 0)
                logText.setString(4, value)
                if (!jdsDb.isLoggingAppendOnly) {
                    logText.setString(5, uuid)
                    logText.setLong(6, fieldId)
                    logText.setInt(7, 0)
                    logText.setString(8, value)
                }
                logText.addBatch()
            }
        }

        for ((uuid, hashMap) in periodProperties) {
            record++
            var innerRecord = 0
            val innerRecordSize = hashMap.size
            if (innerRecordSize == 0) continue
            for ((fieldId, periodProperty) in hashMap) {
                innerRecord++
                val period = periodProperty.get()
                val value = period.toString()
                if (jdsDb.isWritingToPrimaryDataTables) {
                    upsertText.setString("uuid", uuid)
                    upsertText.setLong("fieldId", fieldId)
                    upsertText.setString("value", value)
                    upsertText.addBatch()
                }
                if (jdsDb.isPrintingOutput)
                    println("Updating record [$record]. Period field [$innerRecord of $innerRecordSize]")
                if (!jdsDb.isLoggingEdits) continue
                logText.setString(1, uuid)
                logText.setLong(2, fieldId)
                logText.setInt(3, 0)
                logText.setString(4, value)
                if (!jdsDb.isLoggingAppendOnly) {
                    logText.setString(5, uuid)
                    logText.setLong(6, fieldId)
                    logText.setInt(7, 0)
                    logText.setString(8, value)
                }
                logText.addBatch()
            }
        }

        for ((uuid, hashMap) in durationProperties) {
            record++
            var innerRecord = 0
            val innerRecordSize = hashMap.size
            if (innerRecordSize == 0) continue
            for ((fieldId, durationProperty) in hashMap) {
                innerRecord++
                val duration = durationProperty.get()
                val value = duration.toNanos()
                if (jdsDb.isWritingToPrimaryDataTables) {
                    upsertLong.setString("uuid", uuid)
                    upsertLong.setLong("fieldId", fieldId)
                    upsertLong.setLong("value", value)
                    upsertLong.addBatch()
                }
                if (jdsDb.isPrintingOutput)
                    println("Updating record [$record]. Duration field [$innerRecord of $innerRecordSize]")
                if (!jdsDb.isLoggingEdits) continue
                logLong.setString(1, uuid)
                logLong.setLong(2, fieldId)
                logLong.setInt(3, 0)
                logLong.setLong(4, value)
                if (!jdsDb.isLoggingAppendOnly) {
                    logLong.setString(5, uuid)
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

    /**
     * @param localDateTimeProperties
     * @param localDateProperties

     */
    private fun saveDatesAndDateTimes(localDateTimeProperties: HashMap<String, HashMap<Long, ObjectProperty<Temporal>>>, localDateProperties: HashMap<String, HashMap<Long, ObjectProperty<Temporal>>>) = try {
        var record = 0
        val upsert = if (jdsDb.supportsStatements) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveDateTime()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveDateTime())
        val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldDateTimeValues())
        for ((uuid, value) in localDateTimeProperties) {
            record++
            var innerRecord = 0
            val innerRecordSize = value.size
            if (innerRecordSize == 0) continue
            for ((fieldId, value1) in value) {
                innerRecord++
                val localDateTime = value1.get() as LocalDateTime
                if (jdsDb.isWritingToPrimaryDataTables) {
                    upsert.setString("uuid", uuid)
                    upsert.setLong("fieldId", fieldId)
                    upsert.setTimestamp("value", Timestamp.valueOf(localDateTime))
                    upsert.addBatch()
                }
                if (jdsDb.isPrintingOutput)
                    println("Updating record [$record]. LocalDateTime field [$innerRecord of $innerRecordSize]")
                if (!jdsDb.isLoggingEdits) continue
                log.setString(1, uuid)
                log.setLong(2, fieldId)
                log.setInt(3, 0)
                log.setTimestamp(4, Timestamp.valueOf(localDateTime))
                if (!jdsDb.isLoggingAppendOnly) {
                    log.setString(5, uuid)
                    log.setLong(6, fieldId)
                    log.setInt(7, 0)
                    log.setTimestamp(8, Timestamp.valueOf(localDateTime))
                }
                log.addBatch()
            }
        }
        for ((uuid, value) in localDateProperties) {
            record++
            var innerRecord = 0
            val innerRecordSize = value.size
            if (innerRecordSize == 0) continue
            for ((fieldId, value1) in value) {
                innerRecord++
                val localDate = value1.get() as LocalDate
                upsert.setString("uuid", uuid)
                upsert.setLong("fieldId", fieldId)
                upsert.setTimestamp("value", Timestamp.valueOf(localDate.atStartOfDay()))
                upsert.addBatch()
                if (jdsDb.isPrintingOutput)
                    println("Updating record [$record]. LocalDate field [$innerRecord of $innerRecordSize]")
                if (!jdsDb.isLoggingEdits) continue
                log.setString(1, uuid)
                log.setLong(2, fieldId)
                log.setInt(3, 0)
                log.setTimestamp(4, Timestamp.valueOf(localDate.atStartOfDay()))
                if (!jdsDb.isLoggingAppendOnly) {
                    log.setString(5, uuid)
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

    /**
     * @param localTimeProperties

     */
    private fun saveTimes(localTimeProperties: HashMap<String, HashMap<Long, ObjectProperty<Temporal>>>) = try {
        var record = 0
        val upsert = if (jdsDb.supportsStatements) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveTime()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveTime())
        val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldTimeValues())
        for ((uuid, value) in localTimeProperties) {
            record++
            var innerRecord = 0
            val innerRecordSize = value.size
            if (innerRecordSize == 0) continue
            for ((fieldId, value1) in value) {
                innerRecord++
                val localTime = value1.get() as LocalTime
                if (jdsDb.isWritingToPrimaryDataTables) {
                    upsert.setString("uuid", uuid)
                    upsert.setLong("fieldId", fieldId)
                    upsert.setLocalTime("value", localTime, jdsDb)
                    upsert.addBatch()
                }
                if (jdsDb.isPrintingOutput)
                    println("Updating record [$record]. LocalTime field [$innerRecord of $innerRecordSize]")
                if (!jdsDb.isLoggingEdits) continue
                log.setString(1, uuid)
                log.setLong(2, fieldId)
                log.setInt(3, 0)
                log.setLocalTime(4, localTime, jdsDb)
                if (!jdsDb.isLoggingAppendOnly) {
                    log.setString(5, uuid)
                    log.setLong(6, fieldId)
                    log.setInt(7, 0)
                    log.setLocalTime(8, localTime, jdsDb)
                }
                log.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param zonedDateProperties
     */
    private fun saveZonedDateTimes(zonedDateProperties: HashMap<String, HashMap<Long, ObjectProperty<Temporal>>>) = try {
        var record = 0
        val upsert = if (jdsDb.supportsStatements) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveZonedDateTime()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveZonedDateTime())
        val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldZonedDateTimeValues())
        for ((uuid, value) in zonedDateProperties) {
            record++
            var innerRecord = 0
            val innerRecordSize = value.size
            if (innerRecordSize == 0) continue
            for ((fieldId, value1) in value) {
                innerRecord++
                val zonedDateTime = value1.get() as ZonedDateTime
                if (jdsDb.isWritingToPrimaryDataTables) {
                    upsert.setString("uuid", uuid)
                    upsert.setLong("fieldId", fieldId)
                    upsert.setZonedDateTime("value", zonedDateTime, jdsDb)
                    upsert.addBatch()
                }
                if (jdsDb.isPrintingOutput)
                    System.out.printf("Updating record [$record]. ZonedDateTime field [$innerRecord of $innerRecordSize]")
                if (!jdsDb.isLoggingEdits) continue
                log.setString(1, uuid)
                log.setLong(2, fieldId)
                log.setInt(3, 0)
                log.setZonedDateTime(4, zonedDateTime, jdsDb)
                if (!jdsDb.isLoggingAppendOnly) {
                    log.setString(5, uuid)
                    log.setLong(6, fieldId)
                    log.setInt(7, 0)
                    log.setZonedDateTime(8, zonedDateTime, jdsDb)
                }
                log.addBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param enums

     */
    private fun saveEnums(enums: HashMap<String, HashMap<JdsFieldEnum<*>, ObjectProperty<Enum<*>>>>) = try {
        var record = 0
        val upsert = if (jdsDb.supportsStatements) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveInteger()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveInteger())
        val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldIntegerValues())
        for ((uuid, value1) in enums) {
            record++
            var innerRecord = 0
            val innerRecordSize = value1.size
            if (innerRecordSize == 0) continue
            for ((jdsFieldEnum, value2) in value1) {
                innerRecord++
                val value = value2.get()
                if (jdsDb.isWritingToPrimaryDataTables) {
                    upsert.setString("uuid", uuid)
                    upsert.setLong("fieldId", jdsFieldEnum.field.id)
                    upsert.setInt("value", jdsFieldEnum.indexOf(value))
                    upsert.addBatch()
                }
                if (jdsDb.isPrintingOutput)
                    println("Updating record [$record]. Enum field [$innerRecord of $innerRecordSize]")
                if (!jdsDb.isLoggingEdits) continue
                log.setString(1, uuid)
                log.setLong(2, jdsFieldEnum.field.id)
                log.setInt(3, 0)
                log.setInt(4, jdsFieldEnum.indexOf(value))
                if (!jdsDb.isLoggingAppendOnly) {
                    log.setString(5, uuid)
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

    /**
     * Save all dates in one go
     *
     * @param dateTimeArrayProperties

     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5]to[3,4]
     */
    private fun saveArrayDates(dateTimeArrayProperties: HashMap<String, HashMap<Long, MutableCollection<LocalDateTime>>>) = try {
        val deleteSql = "DELETE FROM jds_store_date_time_array WHERE field_id = ? AND uuid = ?"
        val insertSql = "INSERT INTO jds_store_date_time_array (Sequence, value,field_id, uuid) VALUES (?,?,?,?)"

        val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldDateTimeValues())
        val delete = onPostSaveEventArguments.getOrAddStatement(deleteSql)
        val insert = onPostSaveEventArguments.getOrAddStatement(insertSql)
        var record = 0
        for ((uuid, value) in dateTimeArrayProperties) {
            record++
            val index = SimpleIntegerProperty(0)
            for ((fieldId, value1) in value) {
                val innerRecord = 0
                val innerTotal = value1.size
                for (value in value1) {
                    if (jdsDb.isLoggingEdits) {
                        log.setString(1, uuid)
                        log.setLong(2, fieldId)
                        log.setInt(3, index.get())
                        log.setTimestamp(4, Timestamp.valueOf(value))
                        if (!jdsDb.isLoggingAppendOnly) {
                            log.setString(5, uuid)
                            log.setLong(6, fieldId)
                            log.setInt(7, index.get())
                            log.setTimestamp(8, Timestamp.valueOf(value))
                        }
                        log.addBatch()
                    }
                    if (jdsDb.isWritingToPrimaryDataTables) {
                        delete.setLong(1, fieldId)
                        delete.setString(2, uuid)
                        delete.addBatch()
                        //insert
                        insert.setInt(1, index.get())
                        insert.setTimestamp(2, Timestamp.valueOf(value))
                        insert.setLong(3, fieldId)
                        insert.setString(4, uuid)
                        insert.addBatch()
                    }
                    index.set(index.get() + 1)
                    if (jdsDb.isPrintingOutput)
                        println("Inserting array record [$record]. DateTime field [$innerRecord of $innerTotal]")
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
    private fun saveArrayFloats(floatArrayProperties: HashMap<String, HashMap<Long, MutableCollection<Float>>>) = try {
        val deleteSql = "DELETE FROM jds_store_float_array WHERE field_id = ? AND uuid = ?"
        val insertSql = "INSERT INTO jds_store_float_array (field_id, uuid, value, sequence) VALUES (?,?,?,?)"

        val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldFloatValues())
        val delete = onPostSaveEventArguments.getOrAddStatement(deleteSql)
        val insert = onPostSaveEventArguments.getOrAddStatement(insertSql)
        var record = 0
        for ((uuid, value) in floatArrayProperties) {
            record++
            val index = SimpleIntegerProperty(0)
            for ((fieldId, value1) in value) {
                val innerRecord = 0
                val innerTotal = value1.size
                for (value in value1) {
                    if (jdsDb.isLoggingEdits) {
                        log.setString(1, uuid)
                        log.setLong(2, fieldId)
                        log.setInt(3, index.get())
                        log.setFloat(4, value)
                        if (!jdsDb.isLoggingAppendOnly) {
                            log.setString(5, uuid)
                            log.setLong(6, fieldId)
                            log.setInt(7, index.get())
                            log.setFloat(8, value)
                        }
                        log.addBatch()
                    }
                    if (jdsDb.isWritingToPrimaryDataTables) {//delete
                        delete.setLong(1, fieldId)
                        delete.setString(2, uuid)
                        delete.addBatch()
                        //insert
                        insert.setInt(1, index.get())
                        insert.setFloat(2, value)
                        insert.setLong(3, fieldId)
                        insert.setString(4, uuid)
                        insert.addBatch()
                    }
                    index.set(index.get() + 1)
                    if (jdsDb.isPrintingOutput)
                        println("Inserting array record [$record]. Float field [$innerRecord of $innerTotal]")

                }
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param integerArrayProperties

     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection k.e [3,4,5] to [3,4]
     */
    private fun saveArrayIntegers(integerArrayProperties: HashMap<String, HashMap<Long, MutableCollection<Int>>>) = try {
        val deleteSql = "DELETE FROM jds_store_integer_array WHERE field_id = :fieldId AND uuid = :uuid"
        val insertSql = "INSERT INTO jds_store_integer_array (field_id, uuid, sequence, value) VALUES (:fieldId, :uuid, :sequence, :value)"

        val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldIntegerValues())
        val delete = onPostSaveEventArguments.getOrAddNamedStatement(deleteSql)
        val insert = onPostSaveEventArguments.getOrAddNamedStatement(insertSql)
        var record = 0
        for ((uuid, value) in integerArrayProperties) {
            record++
            val index = SimpleIntegerProperty(0)
            for ((fieldId, value1) in value) {
                val innerRecord = 0
                val innerTotal = value1.size
                for (value in value1) {
                    if (jdsDb.isLoggingEdits) {
                        log.setString(1, uuid)
                        log.setLong(2, fieldId)
                        log.setInt(3, index.get())
                        log.setInt(4, value)
                        if (!jdsDb.isLoggingAppendOnly) {
                            log.setString(5, uuid)
                            log.setLong(6, fieldId)
                            log.setInt(7, index.get())
                            log.setInt(8, value)
                        }
                        log.addBatch()
                    }
                    if (jdsDb.isWritingToPrimaryDataTables) {
                        //delete
                        delete.setLong("fieldId", fieldId)
                        delete.setString("uuid", uuid)
                        delete.addBatch()
                        //insert
                        insert.setInt("sequence", index.get())
                        insert.setInt("value", value)
                        insert.setLong("fieldId", fieldId)
                        insert.setString("uuid", uuid)
                        insert.addBatch()
                    }
                    index.set(index.get() + 1)
                    if (jdsDb.isPrintingOutput)
                        println("Inserting array record [$record]. Integer field [$innerRecord of $innerTotal]")
                }
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param doubleArrayProperties

     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection k.e [3,4,5]to[3,4]
     */
    private fun saveArrayDoubles(doubleArrayProperties: HashMap<String, HashMap<Long, MutableCollection<Double>>>) = try {
        val deleteSql = "DELETE FROM jds_store_double_array WHERE field_id = :fieldId AND uuid = :uuid"
        val insertSql = "INSERT INTO jds_store_double_array (field_id, uuid, sequence, value) VALUES (:fieldId, :uuid, :sequence, :value)"
        val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldDoubleValues())
        val delete = onPostSaveEventArguments.getOrAddNamedStatement(deleteSql)
        val insert = onPostSaveEventArguments.getOrAddNamedStatement(insertSql)
        var record = 0
        for ((uuid, value) in doubleArrayProperties) {
            record++
            val index = SimpleIntegerProperty(0)
            for ((fieldId, value1) in value) {
                val innerRecord = 0
                val innerTotal = value1.size
                for (value in value1) {
                    if (jdsDb.isLoggingEdits) {
                        log.setString(1, uuid)
                        log.setLong(2, fieldId)
                        log.setInt(3, index.get())
                        log.setDouble(4, value)
                        if (!jdsDb.isLoggingAppendOnly) {
                            log.setString(5, uuid)
                            log.setLong(6, fieldId)
                            log.setInt(7, index.get())
                            log.setDouble(8, value)
                        }
                        log.addBatch()
                    }
                    if (jdsDb.isWritingToPrimaryDataTables) {
                        //delete
                        delete.setLong("fieldId", fieldId)
                        delete.setString("uuid", uuid)
                        delete.addBatch()
                        //insert
                        insert.setInt("fieldId", index.get())
                        insert.setDouble("uuid", value)
                        insert.setLong("sequence", fieldId)
                        insert.setString("value", uuid)
                        insert.addBatch()
                    }
                    index.set(index.get() + 1)
                    if (jdsDb.isPrintingOutput)
                        println("Inserting array record [$record]. Double field [$innerRecord of $innerTotal]")
                }
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param longArrayProperties

     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection k.e [3,4,5]to[3,4]
     */
    private fun saveArrayLongs(longArrayProperties: HashMap<String, HashMap<Long, MutableCollection<Long>>>) = try {
        val deleteSql = "DELETE FROM jds_store_double_array WHERE field_id = ? AND uuid = ?"
        val insertSql = "INSERT INTO jds_store_double_array (field_id, uuid, sequence, value) VALUES (?,?,?,?)"

        val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldLongValues())
        val delete = onPostSaveEventArguments.getOrAddStatement(deleteSql)
        val insert = onPostSaveEventArguments.getOrAddStatement(insertSql)
        var record = 0
        for ((uuid, value) in longArrayProperties) {
            record++
            val index = SimpleIntegerProperty(0)
            for ((fieldId, value1) in value) {
                val innerRecord = 0
                val innerTotal = value1.size
                for (value in value1) {
                    if (jdsDb.isLoggingEdits) {
                        log.setString(1, uuid)
                        log.setLong(2, fieldId)
                        log.setInt(3, index.get())
                        log.setLong(4, value)
                        if (!jdsDb.isLoggingAppendOnly) {
                            log.setString(5, uuid)
                            log.setLong(6, fieldId)
                            log.setInt(7, index.get())
                            log.setLong(8, value)
                        }
                        log.addBatch()
                    }
                    if (jdsDb.isWritingToPrimaryDataTables) {//delete
                        delete.setLong(1, fieldId)
                        delete.setString(2, uuid)
                        delete.addBatch()
                        //insert
                        insert.setInt(1, index.get())
                        insert.setLong(2, value)
                        insert.setLong(3, fieldId)
                        insert.setString(4, uuid)
                        insert.addBatch()
                    }
                    index.set(index.get() + 1)
                    if (jdsDb.isPrintingOutput)
                        println("Inserting array record [$record]. Long field [$innerRecord of $innerTotal]")
                }
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param stringArrayProperties

     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection k.e [3,4,5]to[3,4]
     */
    private fun saveArrayStrings(stringArrayProperties: HashMap<String, HashMap<Long, MutableCollection<String?>>>) = try {
        val deleteSql = "DELETE FROM jds_store_text_array WHERE field_id = :fieldId AND uuid = :uuid"
        val insertSql = "INSERT INTO jds_store_text_array (field_id, uuid, sequence, value) VALUES (:fieldId, :uuid, :sequence, :value)"

        val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldStringValues())
        val delete = onPostSaveEventArguments.getOrAddNamedStatement(deleteSql)
        val insert = onPostSaveEventArguments.getOrAddNamedStatement(insertSql)
        var record = 0
        for ((uuid, value) in stringArrayProperties) {
            record++
            val index = SimpleIntegerProperty(0)
            for ((fieldId, value1) in value) {
                val innerRecord = 0
                val innerTotal = value1.size
                for (value in value1) {
                    if (jdsDb.isLoggingEdits) {
                        log.setString(1, uuid)
                        log.setLong(2, fieldId)
                        log.setString(3, value)
                        log.setInt(4, index.get())
                        if (!jdsDb.isLoggingAppendOnly) {
                            log.setString(5, uuid)
                            log.setLong(6, fieldId)
                            log.setString(7, value)
                            log.setInt(8, index.get())
                        }
                        log.addBatch()
                    }
                    if (jdsDb.isWritingToPrimaryDataTables) {
                        //delete
                        delete.setLong("fieldId", fieldId)
                        delete.setString("uuid", uuid)
                        delete.addBatch()
                        //insert
                        insert.setInt("fieldId", index.get())
                        insert.setString("uuid", value)
                        insert.setLong("sequence", fieldId)
                        insert.setString("value", uuid)
                        insert.addBatch()
                    }
                    index.set(index.get() + 1)
                    if (jdsDb.isPrintingOutput)
                        println("Inserting array record [$record]. String field [$innerRecord of $innerTotal]")
                }
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param enumStrings

     * @apiNote Enums are actually saved as index based integer arrays
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection k.e [3,4,5]to[3,4]
     */
    private fun saveEnumCollections(enumStrings: HashMap<String, HashMap<JdsFieldEnum<*>, MutableCollection<Enum<*>>>>) = try {
        var record = 0
        val recordTotal = enumStrings.size
        val deleteSql = "DELETE FROM jds_store_integer_array WHERE field_id = :fieldId AND uuid = :uuid"
        val insertSql = "INSERT INTO jds_store_integer_array (field_id, uuid, sequence, value) VALUES (:fieldId, :uuid, :sequence, :value)"

        val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldIntegerValues())
        val delete = onPostSaveEventArguments.getOrAddNamedStatement(deleteSql)
        val insert = onPostSaveEventArguments.getOrAddNamedStatement(insertSql)
        for ((uuid, value) in enumStrings) {
            record++
            for ((jdsFieldEnum, value1) in value) {
                if (value1.isEmpty()) continue
                for ((sequence, anEnum) in value1.withIndex()) {
                    if (jdsDb.isLoggingEdits) {
                        log.setString(1, uuid)
                        log.setLong(2, jdsFieldEnum.field.id)
                        log.setInt(3, sequence)
                        log.setInt(4, jdsFieldEnum.indexOf(anEnum))
                        if (!jdsDb.isLoggingAppendOnly) {
                            log.setString(5, uuid)
                            log.setLong(6, jdsFieldEnum.field.id)
                            log.setInt(7, sequence)
                            log.setInt(8, jdsFieldEnum.indexOf(anEnum))
                        }
                        log.addBatch()
                    }
                    if (jdsDb.isWritingToPrimaryDataTables) {
                        //delete
                        delete.setLong("fieldId", jdsFieldEnum.field.id)
                        delete.setString("uuid", uuid)
                        delete.addBatch()
                        //insert
                        insert.setLong("fieldId", jdsFieldEnum.field.id)
                        insert.setString("uuid", uuid)
                        insert.setInt("sequence", sequence)
                        insert.setInt("value", jdsFieldEnum.indexOf(anEnum))
                        insert.addBatch()
                    }
                    if (jdsDb.isPrintingOutput)
                        println("Updating enum [$sequence]. Object field [$record of $recordTotal]")
                }
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param connection
     * @param objectArrayProperties
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection k.e [3,4,5]to[3,4]
     * @implNote For the love of Christ don't use parallel stream here
     */
    @Throws(Exception::class)
    private fun saveAndBindObjectArrays(connection: Connection, objectArrayProperties: HashMap<String, HashMap<JdsFieldEntity<*>, MutableCollection<JdsEntity>>>) {
        if (objectArrayProperties.isEmpty()) return
        val entities = ArrayList<JdsEntity>()
        val parentEntityBindings = ArrayList<JdsParentEntityBinding>()
        val parentChildBindings = ArrayList<JdsParentChildBinding>()
        val record = SimpleIntegerProperty(0)
        val changesMade = SimpleBooleanProperty(false)
        val map: MutableMap<String, Long> = HashMap<String, Long>()

        if (jdsDb.isWritingToPrimaryDataTables) {
            for ((parentuuid, value) in objectArrayProperties) {
                for ((key, value1) in value) {
                    record.set(0)
                    changesMade.set(false)
                    value1.filter { jdsEntity -> jdsEntity != null }.forEach { jdsEntity ->
                        if (!changesMade.get()) {
                            //only clear if changes are made. else you wipe out old bindings regardless
                            changesMade.set(true)

                            val parentEntityBinding = JdsParentEntityBinding()
                            parentEntityBinding.parentUuid = parentuuid
                            parentEntityBinding.entityId = jdsEntity.overview.entityId
                            parentEntityBinding.fieldId = key.fieldEntity.id
                            parentEntityBindings.add(parentEntityBinding)


                        }
                        val parentChildBinding = JdsParentChildBinding()
                        parentChildBinding.parentUuid = parentuuid
                        parentChildBinding.childUuid = jdsEntity.overview.uuid
                        parentChildBindings.add(parentChildBinding)

                        entities.add(jdsEntity)
                        map.put(jdsEntity.overview.uuid, key.fieldEntity.id)

                        record.set(record.get() + 1)
                        if (jdsDb.isPrintingOutput)
                            println("Binding array object ${record.get()}")
                    }
                }
            }
        }

        //save ALL children first
        JdsSave(alternateConnections, jdsDb, connection, -1, entities, true, onPreSaveEventArguments, onPostSaveEventArguments).call()

        //bind children below
        //If a parent doesn't have this property everything will be fine, as it wont be loaded
        //thus the delete call will not be executed
        if (jdsDb.isWritingToPrimaryDataTables) {
            val clearOldBindings = onPostSaveEventArguments.getOrAddStatement("DELETE FROM jds_entity_binding WHERE parent_uuid = ? AND field_id = ?")
            val writeNewBindings = onPostSaveEventArguments.getOrAddStatement("INSERT INTO jds_entity_binding (parent_uuid, child_uuid, field_id, child_entity_id) Values(?, ?, ?, ?)")
            for (parentEntityBinding in parentEntityBindings) {
                //delete all entries of this field
                clearOldBindings.setString(1, parentEntityBinding.parentUuid)
                clearOldBindings.setLong(2, parentEntityBinding.fieldId)
                clearOldBindings.addBatch()
            }
            for (jdsEntity in entities) {
                writeNewBindings.setString(1, getParentuuid(parentChildBindings, jdsEntity.overview.uuid))
                writeNewBindings.setString(2, jdsEntity.overview.uuid)
                writeNewBindings.setLong(3, map[jdsEntity.overview.uuid]!!)
                writeNewBindings.setLong(4, jdsEntity.overview.entityId)
                writeNewBindings.addBatch()
            }
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
        val uuidToFieldMap: MutableMap<String, Long> = HashMap<String, Long>()

        if (jdsDb.isWritingToPrimaryDataTables) {
            for ((parentuuid, value) in objectProperties) {
                for ((key, value1) in value) {
                    record.set(0)
                    val jdsEntity = value1.get()
                    changesMade.set(false)
                    if (jdsEntity != null) {
                        if (!changesMade.get()) {
                            changesMade.set(true)
                            val parentEntityBinding = JdsParentEntityBinding()
                            parentEntityBinding.parentUuid = parentuuid
                            parentEntityBinding.entityId = value1.get().overview.entityId
                            parentEntityBinding.fieldId = key.fieldEntity.id
                            parentEntityBindings.add(parentEntityBinding)
                        }
                        jdsEntities.add(jdsEntity)
                        val parentChildBinding = JdsParentChildBinding()
                        parentChildBinding.parentUuid = parentuuid
                        parentChildBinding.childUuid = jdsEntity.overview.uuid

                        parentChildBindings.add(parentChildBinding)
                        uuidToFieldMap.put(value1.get().overview.uuid, key.fieldEntity.id)

                        record.set(record.get() + 1)
                        if (jdsDb.isPrintingOutput)
                            println("Binding object ${record.get()}")
                    }
                }
            }
        }

        //save ALL children first
        JdsSave(alternateConnections, jdsDb, connection, -1, jdsEntities, true, onPreSaveEventArguments, onPostSaveEventArguments).call()

        //bind children below
        //If a parent doesn't have this property everything will be fine, as it wont be loaded
        //thus the delete call will not be executed
        if (jdsDb.isWritingToPrimaryDataTables) {
            val clearOldBindings = onPostSaveEventArguments.getOrAddStatement("DELETE FROM jds_entity_binding WHERE parent_uuid = ? AND field_id = ?")
            val writeNewBindings = onPostSaveEventArguments.getOrAddStatement("INSERT INTO jds_entity_binding(parent_uuid, child_uuid, field_id, child_entity_id) Values(?, ?, ?, ?)")
            for (parentEntityBinding in parentEntityBindings) {
                //delete all entries of this field
                clearOldBindings.setString(1, parentEntityBinding.parentUuid)
                clearOldBindings.setLong(2, parentEntityBinding.fieldId)
                clearOldBindings.addBatch()
            }
            for (jdsEntity in jdsEntities) {
                writeNewBindings.setString(1, getParentuuid(parentChildBindings, jdsEntity.overview.uuid))
                writeNewBindings.setString(2, jdsEntity.overview.uuid)
                writeNewBindings.setLong(3, uuidToFieldMap[jdsEntity.overview.uuid]!!)
                writeNewBindings.setLong(4, jdsEntity.overview.entityId)
                writeNewBindings.addBatch()
            }
        }
    }

    /**
     * @param parentChildBindings
     * @param childuuid
     * @return
     */
    private fun getParentuuid(parentChildBindings: Collection<JdsParentChildBinding>, childuuid: String): String {
        return parentChildBindings.firstOrNull { parentChildBinding -> parentChildBinding.childUuid == childuuid }?.parentUuid ?: ""
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