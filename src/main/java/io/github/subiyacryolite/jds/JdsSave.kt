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

/**
 * This class is responsible for persisting on or more [JdsEntities][JdsEntity]
 */
class JdsSave private constructor(private val alternateConnections: ConcurrentMap<Int, Connection>, private val jdsDb: JdsDb, private val connection: Connection, private val batchSize: Int, private val entities: Iterable<JdsEntity>, private val recursiveInnerCall: Boolean, private val onPreSaveEventArguments: OnPreSaveEventArguments = OnPreSaveEventArguments(jdsDb, connection, alternateConnections), private val onPostSaveEventArguments: OnPostSaveEventArguments = OnPostSaveEventArguments(jdsDb, connection, alternateConnections)) : Callable<Boolean> {


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
            entities.forEach { processCrt(jdsDb, connection, alternateConnections, it) }

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
                alternateConnections.forEach { it.value.close() }
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
            it.executeSave(jdsDb, connection, alternateConnections, entity, onPostSaveEventArguments)
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
                if (jdsDb.isPrintingOutput)
                    println("Saving Overview [$record of $recordTotal]")
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    /**
     * @param writeToPrimaryDataTables
     * @param blobProperties
     */
    private fun saveBlobs(writeToPrimaryDataTables: Boolean, blobProperties: HashMap<String, HashMap<Long, BlobProperty>>) {
        var record = 0
        try {
            val upsert = if (jdsDb.supportsStatements) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveBlob()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveBlob())
            val log = onPostSaveEventArguments.getOrAddStatement(jdsDb.saveOldBlobValues())
            for ((uuid, value) in blobProperties) {
                record++
                var innerRecord = 0
                val innerRecordSize = value.size
                if (innerRecordSize == 0) continue
                for ((fieldId, blobProperty) in value) {
                    innerRecord++
                    if (writeToPrimaryDataTables) {
                        upsert.setString("uuid", uuid)
                        upsert.setLong("fieldId", fieldId)
                        upsert.setBytes("value", blobProperty.get()!!)
                        upsert.addBatch()
                    }
                    if (jdsDb.isPrintingOutput)
                        println("Updating record [$record]. Blob fieldEntity [$innerRecord of $innerRecordSize]")
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
    }

    /**
     * @param booleanProperties
     * @param writeToPrimaryDataTables
     */
    private fun saveBooleans(writeToPrimaryDataTables: Boolean, booleanProperties: HashMap<String, HashMap<Long, BooleanProperty>>) {
        var record = 0
        try {
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
                    if (writeToPrimaryDataTables) {
                        upsert.setString("uuid", uuid)
                        upsert.setLong("fieldId", fieldId)
                        upsert.setBoolean("value", value)
                        upsert.addBatch()
                    }
                    if (jdsDb.isPrintingOutput)
                        println("Updating record [$record]. Boolean fieldEntity [$innerRecord of $innerRecordSize]")
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
    }

    /**
     * @param integerProperties
     * @param writeToPrimaryDataTables
     */
    private fun saveIntegers(writeToPrimaryDataTables: Boolean, integerProperties: HashMap<String, HashMap<Long, IntegerProperty>>) {
        var record = 0
        try {
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
                    if (writeToPrimaryDataTables) {
                        upsert.setString("uuid", uuid)
                        upsert.setLong("fieldId", fieldId)
                        upsert.setInt("value", value)
                        upsert.addBatch()
                    }
                    if (jdsDb.isPrintingOutput)
                        println("Updating record [$record]. Integer fieldEntity [$innerRecord of $innerRecordSize]")
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
    }

    /**
     * @param floatProperties
     * @param writeToPrimaryDataTables
     */
    private fun saveFloats(writeToPrimaryDataTables: Boolean, floatProperties: HashMap<String, HashMap<Long, FloatProperty>>) {
        var record = 0
        try {
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
                    if (writeToPrimaryDataTables) {
                        upsert.setString("uuid", uuid)
                        upsert.setLong("fieldId", fieldId)
                        upsert.setFloat("value", value)
                        upsert.addBatch()
                    }
                    if (jdsDb.isPrintingOutput)
                        System.out.printf("Updating record $record. Float fieldEntity [$innerRecord of $innerRecordSize]")
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
    }

    /**
     * @param doubleProperties
     * @param writeToPrimaryDataTables
     */
    private fun saveDoubles(writeToPrimaryDataTables: Boolean, doubleProperties: HashMap<String, HashMap<Long, DoubleProperty>>) {
        var record = 0
        try {
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
                    if (writeToPrimaryDataTables) {
                        upsert.setString("uuid", uuid)
                        upsert.setLong("fieldId", fieldId)
                        upsert.setDouble("value", value)
                        upsert.addBatch()
                    }
                    if (jdsDb.isPrintingOutput)
                        println("Updating record $record. Double fieldEntity [$innerRecord of $innerRecordSize]")
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
    }

    /**
     * @param longProperties
     * @param writeToPrimaryDataTables
     */
    private fun saveLongs(writeToPrimaryDataTables: Boolean, longProperties: HashMap<String, HashMap<Long, LongProperty>>) = try {
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
                if (writeToPrimaryDataTables) {
                    upsert.setString("uuid", uuid)
                    upsert.setLong("fieldId", fieldId)
                    upsert.setLong("value", value)
                    upsert.addBatch()
                }
                if (jdsDb.isPrintingOutput)
                    System.out.printf("Updating record [$record]. Long fieldEntity [$innerRecord of $innerRecordSize]")
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
     * @param writeToPrimaryDataTables
     */
    private fun saveStrings(writeToPrimaryDataTables: Boolean, stringProperties: HashMap<String, HashMap<Long, StringProperty>>) = try {
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
                if (writeToPrimaryDataTables) {
                    upsert.setString("uuid", uuid)
                    upsert.setLong("fieldId", fieldId)
                    upsert.setString("value", value)
                    upsert.addBatch()
                }
                if (jdsDb.isPrintingOutput)
                    println("Updating record [$record]. Text fieldEntity [$innerRecord of $innerRecordSize]")
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
     * @param writeToPrimaryDataTables
     * @param monthDayProperties
     * @param yearMonthProperties
     * @param periodProperties
     * @param durationProperties
     */
    private fun saveDateConstructs(writeToPrimaryDataTables: Boolean,
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
                if (writeToPrimaryDataTables) {
                    upsertText.setString("uuid", uuid)
                    upsertText.setLong("fieldId", fieldId)
                    upsertText.setString("value", value)
                    upsertText.addBatch()
                }
                if (jdsDb.isPrintingOutput)
                    println("Updating record [$record]. MonthDay fieldEntity [$innerRecord of $innerRecordSize]")
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
                if (writeToPrimaryDataTables) {
                    upsertText.setString("uuid", uuid)
                    upsertText.setLong("fieldId", fieldId)
                    upsertText.setString("value", value)
                    upsertText.addBatch()
                }
                if (jdsDb.isPrintingOutput)
                    println("Updating record [$record]. YearMonth fieldEntity [$innerRecord of $innerRecordSize]")
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
                if (writeToPrimaryDataTables) {
                    upsertText.setString("uuid", uuid)
                    upsertText.setLong("fieldId", fieldId)
                    upsertText.setString("value", value)
                    upsertText.addBatch()
                }
                if (jdsDb.isPrintingOutput)
                    println("Updating record [$record]. Period fieldEntity [$innerRecord of $innerRecordSize]")
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
                if (writeToPrimaryDataTables) {
                    upsertLong.setString("uuid", uuid)
                    upsertLong.setLong("fieldId", fieldId)
                    upsertLong.setLong("value", value)
                    upsertLong.addBatch()
                }
                if (jdsDb.isPrintingOutput)
                    println("Updating record [$record]. Duration fieldEntity [$innerRecord of $innerRecordSize]")
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
     * @param writeToPrimaryDataTables
     */
    private fun saveDatesAndDateTimes(writeToPrimaryDataTables: Boolean, localDateTimeProperties: HashMap<String, HashMap<Long, ObjectProperty<Temporal>>>, localDateProperties: HashMap<String, HashMap<Long, ObjectProperty<Temporal>>>) = try {
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
                if (writeToPrimaryDataTables) {
                    upsert.setString("uuid", uuid)
                    upsert.setLong("fieldId", fieldId)
                    upsert.setTimestamp("value", Timestamp.valueOf(localDateTime))
                    upsert.addBatch()
                }
                if (jdsDb.isPrintingOutput)
                    println("Updating record [$record]. LocalDateTime fieldEntity [$innerRecord of $innerRecordSize]")
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
                    println("Updating record [$record]. LocalDate fieldEntity [$innerRecord of $innerRecordSize]")
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
     * @param writeToPrimaryDataTables
     */
    private fun saveTimes(writeToPrimaryDataTables: Boolean, localTimeProperties: HashMap<String, HashMap<Long, ObjectProperty<Temporal>>>) = try {
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
                if (writeToPrimaryDataTables) {
                    upsert.setString("uuid", uuid)
                    upsert.setLong("fieldId", fieldId)
                    upsert.setLocalTime("value", localTime, jdsDb)
                    upsert.addBatch()
                }
                if (jdsDb.isPrintingOutput)
                    println("Updating record [$record]. LocalTime fieldEntity [$innerRecord of $innerRecordSize]")
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
     * @param writeToPrimaryDataTables
     */
    private fun saveZonedDateTimes(writeToPrimaryDataTables: Boolean, zonedDateProperties: HashMap<String, HashMap<Long, ObjectProperty<Temporal>>>) = try {
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
                if (writeToPrimaryDataTables) {
                    upsert.setString("uuid", uuid)
                    upsert.setLong("fieldId", fieldId)
                    upsert.setZonedDateTime("value", zonedDateTime, jdsDb)
                    upsert.addBatch()
                }
                if (jdsDb.isPrintingOutput)
                    System.out.printf("Updating record [$record]. ZonedDateTime fieldEntity [$innerRecord of $innerRecordSize]")
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
     * @param writeToPrimaryDataTables
     */
    private fun saveEnums(writeToPrimaryDataTables: Boolean, enums: HashMap<String, HashMap<JdsFieldEnum<*>, ObjectProperty<Enum<*>>>>) = try {
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
                if (writeToPrimaryDataTables) {
                    upsert.setString("uuid", uuid)
                    upsert.setLong("fieldId", jdsFieldEnum.field.id)
                    upsert.setInt("value", jdsFieldEnum.indexOf(value))
                    upsert.addBatch()
                }
                if (jdsDb.isPrintingOutput)
                    println("Updating record [$record]. Enum fieldEntity [$innerRecord of $innerRecordSize]")
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
     * @param writeToPrimaryDataTables
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5]to[3,4]
     */
    private fun saveArrayDates(writeToPrimaryDataTables: Boolean, dateTimeArrayProperties: HashMap<String, HashMap<Long, MutableCollection<LocalDateTime>>>) = try {
        val deleteSql = "DELETE FROM JdsStoreDateTimeArray WHERE FieldId = ? AND Uuid = ?"
        val insertSql = "INSERT INTO JdsStoreDateTimeArray (Sequence,Value,FieldId,Uuid) VALUES (?,?,?,?)"

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
                    if (writeToPrimaryDataTables) {
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
                        println("Inserting array record [$record]. DateTime fieldEntity [$innerRecord of $innerTotal]")
                }
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param floatArrayProperties
     * @param writeToPrimaryDataTables
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection k.e [3,4,5]to[3,4]
     */
    private fun saveArrayFloats(writeToPrimaryDataTables: Boolean, floatArrayProperties: HashMap<String, HashMap<Long, MutableCollection<Float>>>) = try {
        val deleteSql = "DELETE FROM JdsStoreFloatArray WHERE FieldId = ? AND Uuid = ?"
        val insertSql = "INSERT INTO JdsStoreFloatArray (FieldId,Uuid,Value,Sequence) VALUES (?,?,?,?)"

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
                        log.setFloat(4, value!!)
                        if (!jdsDb.isLoggingAppendOnly) {
                            log.setString(5, uuid)
                            log.setLong(6, fieldId)
                            log.setInt(7, index.get())
                            log.setFloat(8, value!!)
                        }
                        log.addBatch()
                    }
                    if (writeToPrimaryDataTables) {//delete
                        delete.setLong(1, fieldId)
                        delete.setString(2, uuid)
                        delete.addBatch()
                        //insert
                        insert.setInt(1, index.get())
                        insert.setFloat(2, value!!)
                        insert.setLong(3, fieldId)
                        insert.setString(4, uuid)
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

    /**
     * @param integerArrayProperties
     * @param writeToPrimaryDataTables
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection k.e [3,4,5] to [3,4]
     */
    private fun saveArrayIntegers(writeToPrimaryDataTables: Boolean, integerArrayProperties: HashMap<String, HashMap<Long, MutableCollection<Int>>>) = try {
        val deleteSql = "DELETE FROM JdsStoreIntegerArray WHERE FieldId = :fieldId AND Uuid = :uuid"
        val insertSql = "INSERT INTO JdsStoreIntegerArray (FieldId,Uuid,Sequence,Value) VALUES (:fieldId, :uuid, :sequence, :value)"

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
                        log.setInt(4, value!!)
                        if (!jdsDb.isLoggingAppendOnly) {
                            log.setString(5, uuid)
                            log.setLong(6, fieldId)
                            log.setInt(7, index.get())
                            log.setInt(8, value!!)
                        }
                        log.addBatch()
                    }
                    if (writeToPrimaryDataTables) {
                        //delete
                        delete.setLong("fieldId", fieldId)
                        delete.setString("uuid", uuid)
                        delete.addBatch()
                        //insert
                        insert.setInt("sequence", index.get())
                        insert.setInt("value", value!!)
                        insert.setLong("fieldId", fieldId)
                        insert.setString("uuid", uuid)
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

    /**
     * @param doubleArrayProperties
     * @param writeToPrimaryDataTables
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection k.e [3,4,5]to[3,4]
     */
    private fun saveArrayDoubles(writeToPrimaryDataTables: Boolean, doubleArrayProperties: HashMap<String, HashMap<Long, MutableCollection<Double>>>) = try {
        val deleteSql = "DELETE FROM JdsStoreDoubleArray WHERE FieldId = :fieldId AND Uuid = :uuid"
        val insertSql = "INSERT INTO JdsStoreDoubleArray (FieldId,Uuid,Sequence,Value) VALUES (:fieldId, :uuid, :sequence, :value)"

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
                        log.setDouble(4, value!!)
                        if (!jdsDb.isLoggingAppendOnly) {
                            log.setString(5, uuid)
                            log.setLong(6, fieldId)
                            log.setInt(7, index.get())
                            log.setDouble(8, value!!)
                        }
                        log.addBatch()
                    }
                    if (writeToPrimaryDataTables) {
                        //delete
                        delete.setLong("fieldId", fieldId)
                        delete.setString("uuid", uuid)
                        delete.addBatch()
                        //insert
                        insert.setInt("fieldId", index.get())
                        insert.setDouble("uuid", value!!)
                        insert.setLong("sequence", fieldId)
                        insert.setString("value", uuid)
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

    /**
     * @param longArrayProperties
     * @param writeToPrimaryDataTables
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection k.e [3,4,5]to[3,4]
     */
    private fun saveArrayLongs(writeToPrimaryDataTables: Boolean, longArrayProperties: HashMap<String, HashMap<Long, MutableCollection <Long>>>) = try {
        val deleteSql = "DELETE FROM JdsStoreDoubleArray WHERE FieldId = ? AND Uuid = ?"
        val insertSql = "INSERT INTO JdsStoreDoubleArray (FieldId,Uuid,Sequence,Value) VALUES (?,?,?,?)"

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
                        log.setLong(4, value!!)
                        if (!jdsDb.isLoggingAppendOnly) {
                            log.setString(5, uuid)
                            log.setLong(6, fieldId)
                            log.setInt(7, index.get())
                            log.setLong(8, value!!)
                        }
                        log.addBatch()
                    }
                    if (writeToPrimaryDataTables) {//delete
                        delete.setLong(1, fieldId)
                        delete.setString(2, uuid)
                        delete.addBatch()
                        //insert
                        insert.setInt(1, index.get())
                        insert.setLong(2, value!!)
                        insert.setLong(3, fieldId)
                        insert.setString(4, uuid)
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

    /**
     * @param stringArrayProperties
     * @param writeToPrimaryDataTables
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection k.e [3,4,5]to[3,4]
     */
    private fun saveArrayStrings(writeToPrimaryDataTables: Boolean, stringArrayProperties: HashMap<String, HashMap<Long, MutableCollection<String>>>) = try {
        val deleteSql = "DELETE FROM JdsStoreTextArray WHERE FieldId = :fieldId AND Uuid = :uuid"
        val insertSql = "INSERT INTO JdsStoreTextArray (FieldId,Uuid,Sequence,Value) VALUES (:fieldId, :uuid, :sequence, :value)"

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
                    if (writeToPrimaryDataTables) {
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
                        println("Inserting array record [$record]. String fieldEntity [$innerRecord of $innerTotal]")
                }
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param enumStrings
     * @param writeToPrimaryDataTables
     * @apiNote Enums are actually saved as index based integer arrays
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection k.e [3,4,5]to[3,4]
     */
    private fun saveEnumCollections(writeToPrimaryDataTables: Boolean, enumStrings: HashMap<String, HashMap<JdsFieldEnum<*>, MutableCollection<Enum<*>>>>) = try {
        var record = 0
        val recordTotal = enumStrings.size
        val deleteSql = "DELETE FROM JdsStoreIntegerArray WHERE FieldId = :fieldId AND Uuid = :uuid"
        val insertSql = "INSERT INTO JdsStoreIntegerArray (FieldId,Uuid,Sequence,Value) VALUES (:fieldId, :uuid, :sequence, :value)"

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
                    if (writeToPrimaryDataTables) {
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
                        println("Updating enum [$sequence]. Object fieldEntity [$record of $recordTotal]")
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

        for ((parentUuid, value) in objectArrayProperties) {
            for ((key, value1) in value) {
                record.set(0)
                changesMade.set(false)
                value1.filter { jdsEntity -> jdsEntity != null }.forEach { jdsEntity ->
                    if (!changesMade.get()) {
                        //only clear if changes are made. else you wipe out old bindings regardless
                        changesMade.set(true)

                        val parentEntityBinding = JdsParentEntityBinding()
                        parentEntityBinding.parentUuid = parentUuid
                        parentEntityBinding.entityId = jdsEntity.overview.entityId
                        parentEntityBinding.fieldId = key.fieldEntity.id
                        parentEntityBindings.add(parentEntityBinding)


                    }
                    val parentChildBinding = JdsParentChildBinding()
                    parentChildBinding.parentUuid = parentUuid
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

        //save children first
        JdsSave(alternateConnections, jdsDb, connection, -1, entities, true, onPreSaveEventArguments, onPostSaveEventArguments).call()

        //bind children below
        //If a parent doesn't have this property everything will be fine, as it wont be loaded
        //thus the delete call will not be executed
        val clearOldBindings = onPostSaveEventArguments.getOrAddNamedStatement("DELETE FROM JdsEntityBinding WHERE ParentUuid = :parentUuid AND FieldId = :fieldId")
        val writeNewBindings = onPostSaveEventArguments.getOrAddNamedStatement("INSERT INTO JdsEntityBinding(ParentUuid, ChildUuid, FieldId, ChildEntityId) Values(:parentUuid, :childUuid, :fieldId, :childEntityId)")
        for (parentEntityBinding in parentEntityBindings) {
            //delete all entries of this field
            clearOldBindings.setString("parentUuid", parentEntityBinding.parentUuid)
            clearOldBindings.setLong("fieldId", parentEntityBinding.fieldId)
            clearOldBindings.addBatch()
        }
        for (jdsEntity in entities) {
            writeNewBindings.setString("parentUuid", getParentUuid(parentChildBindings, jdsEntity.overview.uuid))
            writeNewBindings.setString("childUuid", jdsEntity.overview.uuid)
            writeNewBindings.setLong("childEntityId", jdsEntity.overview.entityId)
            writeNewBindings.setLong("fieldId", map[jdsEntity.overview.uuid]!!)
            writeNewBindings.addBatch()
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

        for ((parentUuid, value) in objectProperties) {
            for ((key, value1) in value) {
                record.set(0)
                val jdsEntity = value1.get()
                changesMade.set(false)
                if (jdsEntity != null) {
                    if (!changesMade.get()) {
                        changesMade.set(true)
                        val parentEntityBinding = JdsParentEntityBinding()
                        parentEntityBinding.parentUuid = parentUuid
                        parentEntityBinding.entityId = value1.get().overview.entityId
                        parentEntityBinding.fieldId = key.fieldEntity.id
                        parentEntityBindings.add(parentEntityBinding)
                    }
                    jdsEntities.add(jdsEntity)
                    val parentChildBinding = JdsParentChildBinding()
                    parentChildBinding.parentUuid = parentUuid
                    parentChildBinding.childUuid = jdsEntity.overview.uuid

                    parentChildBindings.add(parentChildBinding)
                    uuidToFieldMap.put(value1.get().overview.uuid, key.fieldEntity.id)

                    record.set(record.get() + 1)
                    if (jdsDb.isPrintingOutput)
                        println("Binding object ${record.get()}")
                }
            }
        }

        //save children first
        JdsSave(alternateConnections, jdsDb, connection, -1, jdsEntities, true, onPreSaveEventArguments, onPostSaveEventArguments).call()

        //bind children below
        //If a parent doesn't have this property everything will be fine, as it wont be loaded
        //thus the delete call will not be executed
        val clearOldBindings = onPostSaveEventArguments.getOrAddNamedStatement("DELETE FROM JdsEntityBinding WHERE ParentUuid = :parentUuid AND FieldId = :fieldId")
        val writeNewBindings = onPostSaveEventArguments.getOrAddNamedStatement("INSERT INTO JdsEntityBinding(ParentUuid, ChildUuid, FieldId, ChildEntityId) Values(:parentUuid, :childUuid, :fieldId, :childEntityId)")
        for (parentEntityBinding in parentEntityBindings) {
            //delete all entries of this field
            clearOldBindings.setString("parentUuid", parentEntityBinding.parentUuid)
            clearOldBindings.setLong("fieldId", parentEntityBinding.fieldId)
            clearOldBindings.addBatch()
        }
        for (jdsEntity in jdsEntities) {
            writeNewBindings.setString("parentUuid", getParentUuid(parentChildBindings, jdsEntity.overview.uuid))
            writeNewBindings.setString("childUuid", jdsEntity.overview.uuid)
            writeNewBindings.setLong("childEntityId", jdsEntity.overview.entityId)
            writeNewBindings.setLong("fieldId", uuidToFieldMap[jdsEntity.overview.uuid]!!)
            writeNewBindings.addBatch()
        }
    }

    /**
     * @param parentChildBindings
     * @param childUuid
     * @return
     */
    private fun getParentUuid(parentChildBindings: Collection<JdsParentChildBinding>, childUuid: String): String {
        val any = parentChildBindings.stream().filter { parentChildBinding -> parentChildBinding.childUuid == childUuid }.findAny()
        return if (any.isPresent) any.get().parentUuid else ""
    }
}