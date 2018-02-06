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
import javafx.beans.property.BlobProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.WritableValue
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
class JdsSave private constructor(private val alternateConnections: ConcurrentMap<Int, Connection> = ConcurrentHashMap(), private val jdsDb: JdsDb, private val connection: Connection, private val batchSize: Int, private val entities: Iterable<JdsEntity>, private val recursiveInnerCall: Boolean, private val onPreSaveEventArguments: OnPreSaveEventArguments = OnPreSaveEventArguments(jdsDb, connection, alternateConnections), private val onPostSaveEventArguments: OnPostSaveEventArguments = OnPostSaveEventArguments(jdsDb, connection, alternateConnections), var closeConnection: Boolean = true) : Callable<Boolean> {

    private val jdsSaveEvents = LinkedList<JdsSaveEvent>()

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
    constructor(jdsDb: JdsDb, entities: Iterable<JdsEntity>, connection: Connection, batchSize: Int) : this(ConcurrentHashMap(), jdsDb, connection, batchSize, entities, false)

    /**
     * Computes a result, or throws an exception if unable to do so.
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Throws(Exception::class)
    override fun call(): Boolean {
        val saveContainer = JdsSaveContainer()
        val batches = LinkedList<LinkedList<JdsEntity>>()
        val allEntities = buildSequence { entities.forEach { yieldAll(it.getAllEntities()) } }

        setupBatches(batchSize, allEntities, saveContainer, batches)
        val steps = batches.size
        batches.forEachIndexed { step, batch ->
            saveInner(batch, saveContainer, step, steps, allEntities)
            saveContainer.reset(step)//free mem
            if (jdsDb.options.isPrintingOutput)
                println("Processed batch [$step of $steps]")
        }
        saveContainer.reset()//free mem
        return true
    }

    /**
     * @param batchSize
     * @param entities
     * @param container
     * @param batchEntities
     */
    private fun setupBatches(batchSize: Int, entities: Sequence<JdsEntity>, container: JdsSaveContainer, batchEntities: MutableList<LinkedList<JdsEntity>>) {
        //default bach is 0 or -1 which means one large chunk. Anything above is a single batch
        entities.forEachIndexed { iteration, it ->
            if (batchSize > 0 && iteration % batchSize == 0) {
                createBatchCollection(container, batchEntities)
            } else if (batchSize <= 0 && iteration == 0)
                createBatchCollection(container, batchEntities)
            batchEntities[batchEntities.size - 1].add(it)
        }
    }

    /**
     * @param saveContainer
     * @param batchEntities
     */
    private fun createBatchCollection(saveContainer: JdsSaveContainer, batchEntities: MutableList<LinkedList<JdsEntity>>) {
        batchEntities.add(LinkedList())
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
     * @param batchEntities
     * @param saveContainer
     * @param currentStep
     * @param totalSteps
     */
    @Throws(Exception::class)
    private fun saveInner(batchEntities: Iterable<JdsEntity>, saveContainer: JdsSaveContainer, currentStep: Int, totalSteps: Int, allEntities: Sequence<JdsEntity>) {

        //share one connection for raw saves, helps with performance
        val finalStep = !recursiveInnerCall && currentStep == totalSteps - 1
        try {
            //always save overviews first
            if (jdsDb.options.isWritingOverviewFields) {
                batchEntities.forEach {
                    saveContainer.overviews[currentStep].add(it.overview)
                    it.assign(currentStep, saveContainer)
                }
                if (currentStep == 0) //if first save capture ALL overviews beforehand so that bindings work flawlessly
                    saveOverviews(allEntities)
            }

            //ensure that overviews are submitted before handing over to listeners
            batchEntities.filterIsInstance<JdsSaveListener>().forEach { it.onPreSave(onPreSaveEventArguments) }

            if (jdsDb.options.isWritingToPrimaryDataTables) {
                //time constraints
                saveDateConstructs(
                        saveContainer.monthDayProperties[currentStep],
                        saveContainer.yearMonthProperties[currentStep],
                        saveContainer.periodProperties[currentStep],
                        saveContainer.durationProperties[currentStep])
                saveDatesAndDateTimes(saveContainer.localDateTimeProperties[currentStep], saveContainer.localDateProperties[currentStep])
                saveZonedDateTimes(saveContainer.zonedDateTimeProperties[currentStep])
                saveTimes(saveContainer.localTimeProperties[currentStep])
                //primitives, can be null
                saveBooleans(saveContainer.booleanProperties[currentStep])
                saveLongs(saveContainer.longProperties[currentStep])
                saveDoubles(saveContainer.doubleProperties[currentStep])
                saveIntegers(saveContainer.integerProperties[currentStep])
                saveFloats(saveContainer.floatProperties[currentStep])
                //strings never null
                saveStrings(saveContainer.stringProperties[currentStep])
                //blobs
                saveBlobs(saveContainer.blobProperties[currentStep])
                //enumProperties
                saveEnums(saveContainer.enumProperties[currentStep])
            }
            if (jdsDb.options.isWritingArrayValues) {
                //array properties [NOTE arrays have old entries deleted first, for cases where a user reduced the amount of entries in the collection]
                saveArrayDates(saveContainer.localDateTimeCollections[currentStep])
                saveArrayStrings(saveContainer.stringCollections[currentStep])
                saveArrayLongs(saveContainer.longCollections[currentStep])
                saveArrayDoubles(saveContainer.doubleCollections[currentStep])
                saveArrayIntegers(saveContainer.integerCollections[currentStep])
                saveArrayFloats(saveContainer.floatCollections[currentStep])
                saveEnumCollections(saveContainer.enumCollections[currentStep])
            }
            //objects and object arrays
            //object entity overviews and entity bindings are ALWAYS persisted
            saveAndBindObjects(saveContainer.objects[currentStep])
            saveAndBindObjectArrays(saveContainer.objectCollections[currentStep])

            batchEntities.filterIsInstance<JdsSaveListener>().forEach { it.onPostSave(onPostSaveEventArguments) }

            //crt point
            if (jdsDb.tables.isNotEmpty()) {
                batchEntities.forEach {
                    processCrt(jdsDb, connection, alternateConnections, it)
                }
            }

            //respect execution sequence
            //respect JDS batches in each call
            onPreSaveEventArguments.executeBatches()
            onPostSaveEventArguments.executeBatches()
        } catch (ex: Exception) {
            throw ex
        } finally {
            if (finalStep) {
                jdsSaveEvents.forEach { it.onSave(batchEntities, connection) }
                onPreSaveEventArguments.closeBatches()
                onPostSaveEventArguments.closeBatches()
                if (closeConnection)
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
    private fun saveOverviews(overviews: Sequence<JdsEntity>) = try {
        val saveOverview = if (jdsDb.supportsStatements) onPreSaveEventArguments.getOrAddNamedCall(jdsDb.saveOverview()) else onPreSaveEventArguments.getOrAddNamedStatement(jdsDb.saveOverview())
        val saveOverviewInheritance = if (jdsDb.supportsStatements) onPreSaveEventArguments.getOrAddNamedCall(jdsDb.saveOverviewInheritance()) else onPreSaveEventArguments.getOrAddNamedStatement(jdsDb.saveOverviewInheritance())
        overviews.forEachIndexed { record, it ->
            //Entity Overview
            saveOverview.setString("compositeKey", it.overview.compositeKey)
            saveOverview.setString("uuid", it.overview.uuid)
            saveOverview.setString("uuidLocation", it.overview.uuidLocation)
            saveOverview.setInt("uuidLocationVersion", it.overview.uuidLocationVersion)
            saveOverview.setLong("entityId", it.overview.entityId)
            saveOverview.setBoolean("live", it.overview.live)
            saveOverview.setString("parentUuid", it.overview.parentUuid)
            saveOverview.setTimestamp("lastEdit", Timestamp.valueOf(it.overview.lastEdit))
            saveOverview.setLong("entityVersion", it.overview.entityVersion) //always update date modified!!!
            saveOverview.addBatch()
            //Entity Inheritance
            saveOverviewInheritance.setString("uuid", it.overview.compositeKey)
            saveOverviewInheritance.setLong("entityId", it.overview.entityId)
            saveOverviewInheritance.addBatch()
            if (jdsDb.options.isPrintingOutput) {
                println("Saving Overview [record $record]")
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
        for ((uuid, value) in blobProperties) {
            record++
            var innerRecord = 0
            val innerRecordSize = value.size
            if (innerRecordSize == 0) continue
            for ((fieldId, blobProperty) in value) {
                innerRecord++
                if (jdsDb.options.isWritingToPrimaryDataTables) {
                    upsert.setString("uuid", uuid)
                    upsert.setLong("fieldId", fieldId)
                    upsert.setBytes("value", blobProperty.get()!!)
                    upsert.addBatch()
                }
                if (jdsDb.options.isPrintingOutput)
                    println("Updating record [$record]. Blob field [$innerRecord of $innerRecordSize]")
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param batch
     */
    private fun saveBooleans(batch: HashMap<String, HashMap<Long, WritableValue<Boolean>>>) = try {
        var record = 0
        val upsert = if (jdsDb.supportsStatements) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveBoolean()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveBoolean())
        for ((uuid, batchEntries) in batch) {
            record++
            var innerRecord = 0
            val innerRecordSize = batchEntries.size
            if (innerRecordSize == 0) continue
            for ((fieldId, entry) in batchEntries) {
                innerRecord++
                if (jdsDb.options.isWritingToPrimaryDataTables) {
                    upsert.setString("uuid", uuid)
                    upsert.setLong("fieldId", fieldId)
                    upsert.setObject("value", entry.value) //primitives could be null, default value has meaning
                    upsert.addBatch()
                }
                if (jdsDb.options.isPrintingOutput)
                    println("Updating record [$record]. Boolean field [$innerRecord of $innerRecordSize]")
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param batch
     */
    private fun saveIntegers(batch: HashMap<String, HashMap<Long, WritableValue<Int>>>) = try {
        var record = 0
        val upsert = if (jdsDb.supportsStatements) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveInteger()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveInteger())
        for ((uuid, batchEntries) in batch) {
            record++
            var innerRecord = 0
            val innerRecordSize = batchEntries.size
            if (innerRecordSize == 0) continue
            for ((fieldId, entry) in batchEntries) {
                innerRecord++
                if (jdsDb.options.isWritingToPrimaryDataTables) {
                    upsert.setString("uuid", uuid)
                    upsert.setLong("fieldId", fieldId)
                    upsert.setObject("value", entry.value) //primitives could be null, default value has meaning
                    upsert.addBatch()
                }
                if (jdsDb.options.isPrintingOutput)
                    println("Updating record [$record]. Integer field [$innerRecord of $innerRecordSize]")
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param batch
     */
    private fun saveFloats(batch: HashMap<String, HashMap<Long, WritableValue<Float>>>) = try {
        var record = 0
        val upsert = if (jdsDb.supportsStatements) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveFloat()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveFloat())
        for ((uuid, batchEntries) in batch) {
            record++
            var innerRecord = 0
            val innerRecordSize = batchEntries.size
            if (innerRecordSize == 0) continue
            for ((fieldId, entry) in batchEntries) {
                innerRecord++
                if (jdsDb.options.isWritingToPrimaryDataTables) {
                    upsert.setString("uuid", uuid)
                    upsert.setLong("fieldId", fieldId)
                    upsert.setObject("value", entry.value) //primitives could be null, default value has meaning
                    upsert.addBatch()
                }
                if (jdsDb.options.isPrintingOutput)
                    System.out.printf("Updating record $record. Float field [$innerRecord of $innerRecordSize]")
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param batch
     */
    private fun saveDoubles(batch: HashMap<String, HashMap<Long, WritableValue<Double>>>) = try {
        var record = 0
        val upsert = if (jdsDb.supportsStatements) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveDouble()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveDouble())
        for ((uuid, batchEntries) in batch) {
            record++
            var innerRecord = 0
            val innerRecordSize = batchEntries.size
            if (innerRecordSize == 0) continue
            for ((fieldId, entry) in batchEntries) {
                if (jdsDb.options.isWritingToPrimaryDataTables) {
                    upsert.setString("uuid", uuid)
                    upsert.setLong("fieldId", fieldId)
                    upsert.setObject("value", entry.value) //primitives could be null, default value has meaning
                    upsert.addBatch()
                }
                if (jdsDb.options.isPrintingOutput)
                    println("Updating record $record. Double field [$innerRecord of $innerRecordSize]")
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param batch
     */
    private fun saveLongs(batch: HashMap<String, HashMap<Long, WritableValue<Long>>>) = try {
        var record = 0
        val upsert = if (jdsDb.supportsStatements) onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveLong()) else onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveLong())
        for ((uuid, batchEntries) in batch) {
            record++
            var innerRecord = 0
            val innerRecordSize = batchEntries.size
            if (innerRecordSize == 0) continue
            for ((fieldId, entry) in batchEntries) {
                innerRecord++
                if (jdsDb.options.isWritingToPrimaryDataTables) {
                    upsert.setString("uuid", uuid)
                    upsert.setLong("fieldId", fieldId)
                    upsert.setObject("value", entry.value) //primitives could be null, default value has meaning
                    upsert.addBatch()
                }
                if (jdsDb.options.isPrintingOutput)
                    System.out.printf("Updating record [$record]. Long field [$innerRecord of $innerRecordSize]")
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
        for ((uuid, value1) in stringProperties) {
            record++
            var innerRecord = 0
            val innerRecordSize = value1.size
            if (innerRecordSize == 0) continue
            for ((fieldId, value2) in value1) {
                innerRecord++
                val value = value2.get()
                if (jdsDb.options.isWritingToPrimaryDataTables) {
                    upsert.setString("uuid", uuid)
                    upsert.setLong("fieldId", fieldId)
                    upsert.setString("value", value)
                    upsert.addBatch()
                }
                if (jdsDb.options.isPrintingOutput)
                    println("Updating record [$record]. Text field [$innerRecord of $innerRecordSize]")
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
        for ((uuid, hashMap) in monthDayProperties) {
            record++
            var innerRecord = 0
            val innerRecordSize = hashMap.size
            if (innerRecordSize == 0) continue
            for ((fieldId, monthDayProperty) in hashMap) {
                innerRecord++
                val monthDay = monthDayProperty.get()
                val value = monthDay.toString()
                if (jdsDb.options.isWritingToPrimaryDataTables) {
                    upsertText.setString("uuid", uuid)
                    upsertText.setLong("fieldId", fieldId)
                    upsertText.setString("value", value)
                    upsertText.addBatch()
                }
                if (jdsDb.options.isPrintingOutput)
                    println("Updating record [$record]. MonthDay field [$innerRecord of $innerRecordSize]")
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
                if (jdsDb.options.isWritingToPrimaryDataTables) {
                    upsertText.setString("uuid", uuid)
                    upsertText.setLong("fieldId", fieldId)
                    upsertText.setString("value", value)
                    upsertText.addBatch()
                }
                if (jdsDb.options.isPrintingOutput)
                    println("Updating record [$record]. YearMonth field [$innerRecord of $innerRecordSize]")
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
                if (jdsDb.options.isWritingToPrimaryDataTables) {
                    upsertText.setString("uuid", uuid)
                    upsertText.setLong("fieldId", fieldId)
                    upsertText.setString("value", value)
                    upsertText.addBatch()
                }
                if (jdsDb.options.isPrintingOutput)
                    println("Updating record [$record]. Period field [$innerRecord of $innerRecordSize]")
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
                if (jdsDb.options.isWritingToPrimaryDataTables) {
                    upsertLong.setString("uuid", uuid)
                    upsertLong.setLong("fieldId", fieldId)
                    upsertLong.setLong("value", value)
                    upsertLong.addBatch()
                }
                if (jdsDb.options.isPrintingOutput)
                    println("Updating record [$record]. Duration field [$innerRecord of $innerRecordSize]")
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
        for ((uuid, value) in localDateTimeProperties) {
            record++
            var innerRecord = 0
            val innerRecordSize = value.size
            if (innerRecordSize == 0) continue
            for ((fieldId, value1) in value) {
                innerRecord++
                val localDateTime = value1.get() as LocalDateTime
                if (jdsDb.options.isWritingToPrimaryDataTables) {
                    upsert.setString("uuid", uuid)
                    upsert.setLong("fieldId", fieldId)
                    upsert.setTimestamp("value", Timestamp.valueOf(localDateTime))
                    upsert.addBatch()
                }
                if (jdsDb.options.isPrintingOutput)
                    println("Updating record [$record]. LocalDateTime field [$innerRecord of $innerRecordSize]")
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
                if (jdsDb.options.isPrintingOutput)
                    println("Updating record [$record]. LocalDate field [$innerRecord of $innerRecordSize]")
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
        for ((uuid, value) in localTimeProperties) {
            record++
            var innerRecord = 0
            val innerRecordSize = value.size
            if (innerRecordSize == 0) continue
            for ((fieldId, value1) in value) {
                innerRecord++
                val localTime = value1.get() as LocalTime
                if (jdsDb.options.isWritingToPrimaryDataTables) {
                    upsert.setString("uuid", uuid)
                    upsert.setLong("fieldId", fieldId)
                    upsert.setLocalTime("value", localTime, jdsDb)
                    upsert.addBatch()
                }
                if (jdsDb.options.isPrintingOutput)
                    println("Updating record [$record]. LocalTime field [$innerRecord of $innerRecordSize]")
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
        for ((uuid, value) in zonedDateProperties) {
            record++
            var innerRecord = 0
            val innerRecordSize = value.size
            if (innerRecordSize == 0) continue
            for ((fieldId, value1) in value) {
                innerRecord++
                val zonedDateTime = value1.get() as ZonedDateTime
                if (jdsDb.options.isWritingToPrimaryDataTables) {
                    upsert.setString("uuid", uuid)
                    upsert.setLong("fieldId", fieldId)
                    upsert.setZonedDateTime("value", zonedDateTime, jdsDb)
                    upsert.addBatch()
                }
                if (jdsDb.options.isPrintingOutput)
                    System.out.printf("Updating record [$record]. ZonedDateTime field [$innerRecord of $innerRecordSize]")
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
        for ((uuid, value1) in enums) {
            record++
            var innerRecord = 0
            val innerRecordSize = value1.size
            if (innerRecordSize == 0) continue
            for ((jdsFieldEnum, value2) in value1) {
                innerRecord++
                val value = value2.get()
                if (jdsDb.options.isWritingToPrimaryDataTables) {
                    upsert.setString("uuid", uuid)
                    upsert.setLong("fieldId", jdsFieldEnum.field.id)
                    upsert.setInt("value", jdsFieldEnum.indexOf(value))
                    upsert.addBatch()
                }
                if (jdsDb.options.isPrintingOutput)
                    println("Updating record [$record]. Enum field [$innerRecord of $innerRecordSize]")
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
        val deleteSql = "DELETE FROM jds_store_date_time_array WHERE field_id = ? AND composite_key = ?"
        val insertSql = "INSERT INTO jds_store_date_time_array (sequence, value,field_id, composite_key) VALUES (?,?,?,?)"

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
                    if (jdsDb.options.isWritingToPrimaryDataTables) {
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
                    if (jdsDb.options.isPrintingOutput)
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
        val deleteSql = "DELETE FROM jds_store_float_array WHERE field_id = ? AND composite_key = ?"
        val insertSql = "INSERT INTO jds_store_float_array (field_id, composite_key, value, sequence) VALUES (?,?,?,?)"

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
                    if (jdsDb.options.isWritingToPrimaryDataTables) {//delete
                        delete.setLong(1, fieldId)
                        delete.setString(2, uuid)
                        delete.addBatch()
                        //insert
                        insert.setInt(1, index.get())
                        insert.setObject(2, value) //primitives could be null, default value has meaning
                        insert.setLong(3, fieldId)
                        insert.setString(4, uuid)
                        insert.addBatch()
                    }
                    index.set(index.get() + 1)
                    if (jdsDb.options.isPrintingOutput)
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
        val deleteSql = "DELETE FROM jds_store_integer_array WHERE field_id = :fieldId AND composite_key = :uuid"
        val insertSql = "INSERT INTO jds_store_integer_array (field_id, composite_key, sequence, value) VALUES (:fieldId, :uuid, :sequence, :value)"

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
                    if (jdsDb.options.isWritingToPrimaryDataTables) {
                        //delete
                        delete.setLong("fieldId", fieldId)
                        delete.setString("uuid", uuid)
                        delete.addBatch()
                        //insert
                        insert.setInt("sequence", index.get())
                        insert.setObject("value", value) //primitives could be null, default value has meaning
                        insert.setLong("fieldId", fieldId)
                        insert.setString("uuid", uuid)
                        insert.addBatch()
                    }
                    index.set(index.get() + 1)
                    if (jdsDb.options.isPrintingOutput)
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
        val deleteSql = "DELETE FROM jds_store_double_array WHERE field_id = :fieldId AND composite_key = :uuid"
        val insertSql = "INSERT INTO jds_store_double_array (field_id, composite_key, sequence, value) VALUES (:fieldId, :uuid, :sequence, :value)"
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
                    if (jdsDb.options.isWritingToPrimaryDataTables) {
                        //delete
                        delete.setLong("fieldId", fieldId)
                        delete.setString("uuid", uuid)
                        delete.addBatch()
                        //insert
                        insert.setInt("fieldId", index.get())
                        insert.setObject("uuid", value) //primitives could be null, default value has meaning
                        insert.setLong("sequence", fieldId)
                        insert.setString("value", uuid)
                        insert.addBatch()
                    }
                    index.set(index.get() + 1)
                    if (jdsDb.options.isPrintingOutput)
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
        val deleteSql = "DELETE FROM jds_store_double_array WHERE field_id = ? AND composite_key = ?"
        val insertSql = "INSERT INTO jds_store_double_array (field_id, composite_key, sequence, value) VALUES (?,?,?,?)"

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
                    if (jdsDb.options.isWritingToPrimaryDataTables) {//delete
                        delete.setLong(1, fieldId)
                        delete.setString(2, uuid)
                        delete.addBatch()
                        //insert
                        insert.setInt(1, index.get())
                        insert.setObject(2, value) //primitives could be null, default value has meaning
                        insert.setLong(3, fieldId)
                        insert.setString(4, uuid)
                        insert.addBatch()
                    }
                    index.set(index.get() + 1)
                    if (jdsDb.options.isPrintingOutput)
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
    private fun saveArrayStrings(stringArrayProperties: HashMap<String, HashMap<Long, MutableCollection<String>>>) = try {
        val deleteSql = "DELETE FROM jds_store_text_array WHERE field_id = :fieldId AND composite_key = :uuid"
        val insertSql = "INSERT INTO jds_store_text_array (field_id, composite_key, sequence, value) VALUES (:fieldId, :uuid, :sequence, :value)"
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
                    if (jdsDb.options.isWritingToPrimaryDataTables) {
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
                    if (jdsDb.options.isPrintingOutput)
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
        val deleteSql = "DELETE FROM jds_store_integer_array WHERE field_id = :fieldId AND composite_key = :uuid"
        val insertSql = "INSERT INTO jds_store_integer_array (field_id, composite_key, sequence, value) VALUES (:fieldId, :uuid, :sequence, :value)"
        val delete = onPostSaveEventArguments.getOrAddNamedStatement(deleteSql)
        val insert = onPostSaveEventArguments.getOrAddNamedStatement(insertSql)
        for ((uuid, value) in enumStrings) {
            record++
            for ((jdsFieldEnum, value1) in value) {
                if (value1.isEmpty()) continue
                for ((sequence, anEnum) in value1.withIndex()) {
                    if (jdsDb.options.isWritingToPrimaryDataTables) {
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
                    if (jdsDb.options.isPrintingOutput)
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
    private fun saveAndBindObjectArrays(objectArrayProperties: HashMap<String, HashMap<JdsFieldEntity<*>, MutableCollection<JdsEntity>>>) {
        if (objectArrayProperties.isEmpty()) return
        val entities = ArrayList<JdsEntity>()
        val record = SimpleIntegerProperty(0)
        val uuidToFieldMap = HashMap<String, Long>()
        if (jdsDb.options.isWritingToPrimaryDataTables || jdsDb.options.isWritingOverviewFields) {
            for ((parentCompositeKey, entityCollections) in objectArrayProperties) {
                for ((key, entityCollection) in entityCollections) {
                    record.set(0)
                    entityCollection.forEach { jdsEntity ->
                        uuidToFieldMap[jdsEntity.overview.uuid] = key.fieldEntity.id
                        entities.add(jdsEntity)
                        record.set(record.get() + 1)
                        if (jdsDb.options.isPrintingOutput)
                            println("Binding array object ${record.get()}")
                    }
                }
            }
        }

        //bind children below
        //If a parent doesn't have this property everything will be fine, as it wont be loaded
        //thus the delete call will not be executed
        if (jdsDb.options.isWritingToPrimaryDataTables || jdsDb.options.isWritingOverviewFields) {
            val clearOldBindings = onPostSaveEventArguments.getOrAddStatement("DELETE FROM jds_entity_binding WHERE parent_composite_key = ? AND field_id = ?")
            val writeNewBindings = onPostSaveEventArguments.getOrAddStatement("INSERT INTO jds_entity_binding (parent_composite_key, child_composite_key, field_id, child_entity_id) Values(?, ?, ?, ?)")
            for (jdsEntity in entities) {
                //delete all entries of this field
                clearOldBindings.setString(1, jdsEntity.overview.parentCompositeKey)
                clearOldBindings.setLong(2, uuidToFieldMap[jdsEntity.overview.uuid]!!)
                clearOldBindings.addBatch()

                writeNewBindings.setString(1, jdsEntity.overview.parentCompositeKey)
                writeNewBindings.setString(2, jdsEntity.overview.compositeKey)
                writeNewBindings.setLong(3, uuidToFieldMap[jdsEntity.overview.uuid]!!)
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
    private fun saveAndBindObjects(objectProperties: HashMap<String, HashMap<JdsFieldEntity<*>, ObjectProperty<JdsEntity>>>) {
        if (objectProperties.isEmpty()) return //prevent stack overflow :)
        val record = SimpleIntegerProperty(0)
        val jdsEntities = ArrayList<JdsEntity>()
        val uuidToFieldMap = HashMap<String, Long>()

        if (jdsDb.options.isWritingToPrimaryDataTables) {
            for ((parentuuid, value) in objectProperties) {
                for ((key, value1) in value) {
                    record.set(0)
                    val jdsEntity = value1.get()
                    jdsEntities.add(jdsEntity)
                    uuidToFieldMap[value1.get().overview.uuid] = key.fieldEntity.id
                    record.set(record.get() + 1)
                    if (jdsDb.options.isPrintingOutput)
                        println("Binding object ${record.get()}")
                }
            }
        }

        //bind children below
        //If a parent doesn't have this property everything will be fine, as it wont be loaded
        //thus the delete call will not be executed
        if (jdsDb.options.isWritingToPrimaryDataTables) {
            val clearOldBindings = onPostSaveEventArguments.getOrAddStatement("DELETE FROM jds_entity_binding WHERE parent_composite_key = ? AND field_id = ?")
            val writeNewBindings = onPostSaveEventArguments.getOrAddStatement("INSERT INTO jds_entity_binding(parent_composite_key, child_composite_key, field_id, child_entity_id) Values(?, ?, ?, ?)")
            for (jdsEntity in jdsEntities) {
                //delete all entries of this field
                clearOldBindings.setString(1, jdsEntity.overview.parentCompositeKey)
                clearOldBindings.setLong(2, uuidToFieldMap[jdsEntity.overview.uuid]!!)
                clearOldBindings.addBatch()

                writeNewBindings.setString(1, jdsEntity.overview.parentCompositeKey)
                writeNewBindings.setString(2, jdsEntity.overview.uuid)
                writeNewBindings.setLong(3, uuidToFieldMap[jdsEntity.overview.uuid]!!)
                writeNewBindings.setLong(4, jdsEntity.overview.entityId)
                writeNewBindings.addBatch()
            }
        }
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