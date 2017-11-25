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

import io.github.subiyacryolite.jds.JdsExtensions.getTime
import io.github.subiyacryolite.jds.JdsExtensions.getZonedDateTime
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation
import io.github.subiyacryolite.jds.enums.JdsFieldType
import io.github.subiyacryolite.jds.events.JdsLoadListener
import io.github.subiyacryolite.jds.events.OnPostLoadEventArguments
import io.github.subiyacryolite.jds.events.OnPreLoadEventArguments
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ConcurrentMap
import java.util.stream.Stream

/**
 * This class is responsible for loading an [entity's][JdsEntity] [fields][JdsField]
 */
class JdsLoad<T : JdsEntity> : Callable<MutableList<T>> {
    private val collections = ArrayList<T>()
    private val jdsDb: JdsDb
    private val referenceType: Class<T>
    private val searchUuids: Iterable<out String>
    private var comparator: Comparator<in T>? = null
    private val alternateConnections: ConcurrentMap<Int, Connection> = ConcurrentHashMap()

    /**
     * @param jdsDb
     * @param referenceType
     * @param comparator
     * @param searchUuids
     */
    constructor(jdsDb: JdsDb, referenceType: Class<T>, comparator: Comparator<T>, vararg searchUuids: String) {
        this.jdsDb = jdsDb
        this.referenceType = referenceType
        this.searchUuids = searchUuids.asIterable()
        this.comparator = comparator
    }

    /**
     * @param jdsDb
     * @param referenceType
     * @param searchUuids
     */
    constructor(jdsDb: JdsDb, referenceType: Class<T>, vararg searchUuids: String) {
        this.jdsDb = jdsDb
        this.referenceType = referenceType
        this.searchUuids = searchUuids.asIterable()
    }

    /**
     * @param jdsDb
     * @param referenceType
     * @param searchUuids
     */
    constructor(jdsDb: JdsDb, referenceType: Class<T>, searchUuids: Collection<String>) {
        this.jdsDb = jdsDb
        this.referenceType = referenceType
        this.searchUuids = searchUuids
    }


    /**
     * @param jdsDb
     * @param referenceType
     * @param entities
     * @param initialisePrimitives
     * @param initialiseDatesAndTimes
     * @param initialiseObjects
     * @param uuids
     * @param <T>
     */
    private fun <T : JdsEntity> populateInner(jdsDb: JdsDb,
                                              referenceType: Class<T>?,
                                              entities: MutableCollection<T>,
                                              initialisePrimitives: Boolean,
                                              initialiseDatesAndTimes: Boolean,
                                              initialiseObjects: Boolean,
                                              uuids: Collection<String>) {
        val questionsString = prepareParamaterSequence(uuids.size)
        //primitives
        val sqlBooleans = String.format("SELECT Uuid, Value, FieldId FROM JdsStoreBoolean WHERE Uuid IN (%s)", questionsString)
        val sqlStrings = String.format("SELECT Uuid, Value, FieldId FROM JdsStoreText WHERE Uuid IN (%s)", questionsString)
        val sqlLongs = String.format("SELECT Uuid, Value, FieldId FROM JdsStoreLong WHERE Uuid IN (%s)", questionsString)
        val sqlIntegers = String.format("SELECT Uuid, Value, FieldId FROM JdsStoreInteger WHERE Uuid IN (%s)", questionsString)
        val sqlFloats = String.format("SELECT Uuid, Value, FieldId FROM JdsStoreFloat WHERE Uuid IN (%s)", questionsString)
        val sqlDoubles = String.format("SELECT Uuid, Value, FieldId FROM JdsStoreDouble WHERE Uuid IN (%s)", questionsString)
        val sqlDateTimes = String.format("SELECT Uuid, Value, FieldId FROM JdsStoreDateTime WHERE Uuid IN (%s)", questionsString)
        val sqlTimes = String.format("SELECT Uuid, Value, FieldId FROM JdsStoreTime WHERE Uuid IN (%s)", questionsString)
        val sqlZonedDateTimes = String.format("SELECT Uuid, Value, FieldId FROM JdsStoreZonedDateTime WHERE Uuid IN (%s)", questionsString)
        //blobs
        val sqlBlobs = String.format("SELECT Uuid, Value, FieldId FROM JdsStoreBlob WHERE Uuid IN (%s)", questionsString)
        //array
        val sqlStringCollections = String.format("SELECT Uuid, Value, FieldId, Sequence FROM JdsStoreTextArray WHERE Uuid IN (%s)", questionsString)
        val sqlIntegerAndEnumCollections = String.format("SELECT Uuid, Value, FieldId, Sequence FROM JdsStoreIntegerArray WHERE Uuid IN (%s)", questionsString)
        val sqlLongCollections = String.format("SELECT Uuid, Value, FieldId, Sequence FROM JdsStoreLongArray WHERE Uuid IN (%s)", questionsString)
        val sqlFloatCollections = String.format("SELECT Uuid, Value, FieldId, Sequence FROM JdsStoreFloatArray WHERE Uuid IN (%s)", questionsString)
        val sqlDoubleCollections = String.format("SELECT Uuid, Value, FieldId, Sequence FROM JdsStoreDoubleArray WHERE Uuid IN (%s)", questionsString)
        val sqlDateTimeCollections = String.format("SELECT Uuid, Value, FieldId, Sequence FROM JdsStoreDateTimeArray WHERE Uuid IN (%s)", questionsString)
        val sqlEmbeddedAndArrayObjects = String.format("SELECT ChildUuid, ParentUuid, ChildEntityId, FieldId FROM JdsEntityBinding WHERE ParentUuid IN (%s)", questionsString)
        //overviews
        val sqlOverviews = String.format("SELECT Uuid, DateCreated, DateModified, Live, Version FROM JdsEntityOverview WHERE Uuid IN (%s)", questionsString)
        try {
            jdsDb.getConnection().use { connection ->
                connection.prepareStatement(sqlBooleans).use { booleans ->
                    connection.prepareStatement(sqlBlobs).use { blobs ->
                        connection.prepareStatement(sqlStrings).use { strings ->
                            connection.prepareStatement(sqlLongs).use { longs ->
                                connection.prepareStatement(sqlIntegers).use { integers ->
                                    connection.prepareStatement(sqlFloats).use { floats ->
                                        connection.prepareStatement(sqlDoubles).use { doubles ->
                                            connection.prepareStatement(sqlDateTimes).use { dateTimes ->
                                                connection.prepareStatement(sqlTimes).use { times ->
                                                    connection.prepareStatement(sqlZonedDateTimes).use { zonedDateTimes ->
                                                        connection.prepareStatement(sqlStringCollections).use { textArrays ->
                                                            connection.prepareStatement(sqlIntegerAndEnumCollections).use { integerArraysAndEnums ->
                                                                connection.prepareStatement(sqlLongCollections).use { longArrays ->
                                                                    connection.prepareStatement(sqlFloatCollections).use { floatArrays ->
                                                                        connection.prepareStatement(sqlDoubleCollections).use { doubleArrays ->
                                                                            connection.prepareStatement(sqlDateTimeCollections).use { dateTimeArrays ->
                                                                                connection.prepareStatement(sqlEmbeddedAndArrayObjects).use { embeddedAndArrayObjects ->
                                                                                    connection.prepareStatement(sqlOverviews).use { overviews ->
                                                                                        //work in batches to not break prepared statement
                                                                                        var batchSequence = 1
                                                                                        for (uuid in uuids) {
                                                                                            if (referenceType != null && (initialisePrimitives || initialiseDatesAndTimes || initialiseObjects)) {
                                                                                                //sometimes the entityVersions would already have been instanciated, thus we only need to populate
                                                                                                val entity = referenceType.newInstance()
                                                                                                entity.overview.uuid = uuid
                                                                                                entities.add(entity)
                                                                                            }
                                                                                            //primitives
                                                                                            setParameterForStatement(batchSequence, uuid, booleans)
                                                                                            setParameterForStatement(batchSequence, uuid, strings)
                                                                                            setParameterForStatement(batchSequence, uuid, integers)
                                                                                            setParameterForStatement(batchSequence, uuid, longs)
                                                                                            setParameterForStatement(batchSequence, uuid, floats)
                                                                                            setParameterForStatement(batchSequence, uuid, doubles)
                                                                                            setParameterForStatement(batchSequence, uuid, dateTimes)
                                                                                            setParameterForStatement(batchSequence, uuid, times)
                                                                                            setParameterForStatement(batchSequence, uuid, zonedDateTimes)
                                                                                            //blobs
                                                                                            setParameterForStatement(batchSequence, uuid, blobs)
                                                                                            //array
                                                                                            setParameterForStatement(batchSequence, uuid, textArrays)
                                                                                            setParameterForStatement(batchSequence, uuid, longArrays)
                                                                                            setParameterForStatement(batchSequence, uuid, floatArrays)
                                                                                            setParameterForStatement(batchSequence, uuid, doubleArrays)
                                                                                            setParameterForStatement(batchSequence, uuid, dateTimeArrays)
                                                                                            setParameterForStatement(batchSequence, uuid, integerArraysAndEnums)
                                                                                            //object and object arrays
                                                                                            setParameterForStatement(batchSequence, uuid, embeddedAndArrayObjects)
                                                                                            //overview
                                                                                            setParameterForStatement(batchSequence, uuid, overviews)
                                                                                            batchSequence++
                                                                                        }
                                                                                        //catch embedded/pre-created objects objects as well
                                                                                        for (entity in entities)
                                                                                            if (entity is JdsLoadListener)
                                                                                                (entity as JdsLoadListener).onPreLoad(OnPreLoadEventArguments(jdsDb, connection, alternateConnections))

                                                                                        if (jdsDb.isWritingToPrimaryDataTables && initialisePrimitives) {
                                                                                            //primitives
                                                                                            populateTextMonthDayYearMonthAndPeriod(entities, strings)
                                                                                            populateLongAndDuration(entities, longs)
                                                                                            populateBoolean(entities, booleans)
                                                                                            populateIntegerAndEnum(entities, integers)
                                                                                            populateFloat(entities, floats)
                                                                                            populateDouble(entities, doubles)
                                                                                            //integer arrays and enumProperties
                                                                                            populateIntegerArraysAndEnums(entities, integerArraysAndEnums)
                                                                                            populateFloatArrays(entities, floatArrays)
                                                                                            populateLongArrays(entities, longArrays)
                                                                                            populateStringArrays(entities, textArrays)
                                                                                            populateDoubleArrays(entities, doubleArrays)
                                                                                        }
                                                                                        if (jdsDb.isWritingToPrimaryDataTables && initialiseDatesAndTimes) {
                                                                                            populateZonedDateTime(entities, zonedDateTimes)
                                                                                            populateDateTimeAndDate(entities, dateTimes)
                                                                                            populateTimes(entities, times)
                                                                                            populateDateTimeArrays(entities, dateTimeArrays)
                                                                                        }
                                                                                        if (initialiseObjects) {
                                                                                            if (jdsDb.isWritingToPrimaryDataTables)
                                                                                                populateBlobs(entities, blobs)
                                                                                            populateObjectEntriesAndObjectArrays(jdsDb, entities, embeddedAndArrayObjects, initialisePrimitives, initialiseDatesAndTimes, initialiseObjects)
                                                                                        }
                                                                                        populateOverviews(entities, overviews)
                                                                                        //catch embedded/pre-created objects objects as well
                                                                                        for (entity in entities)
                                                                                            if (entity is JdsLoadListener)
                                                                                                (entity as JdsLoadListener).onPostLoad(OnPostLoadEventArguments(jdsDb, connection, alternateConnections))

                                                                                        //close alternate connections
                                                                                        alternateConnections.forEach { it.value.close() }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    /**
     * @param jdsDb
     * @param entities
     * @param preparedStatement
     * @param initialisePrimitives
     * @param initialiseDatesAndTimes
     * @param initialiseObjects
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateObjectEntriesAndObjectArrays(jdsDb: JdsDb, entities: Collection<T>,
                                                                     preparedStatement: PreparedStatement,
                                                                     initialisePrimitives: Boolean,
                                                                     initialiseDatesAndTimes: Boolean,
                                                                     initialiseObjects: Boolean) {
        val uuids = HashSet<String>()//ids should be unique
        val innerObjects = ConcurrentLinkedQueue<JdsEntity>()//can be multiple copies of the same object however
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val parentUuid = resultSet.getString("ParentUuid")
                val uuid = resultSet.getString("ChildUuid")
                val fieldId = resultSet.getLong("FieldId")
                val entityId = resultSet.getLong("ChildEntityId")
                optimalEntityLookup(entities, parentUuid).forEach { it.populateObjects(jdsDb, fieldId, entityId, uuid, innerObjects, uuids) }
            }
        }
        val batches = createProcessingBatches(uuids)
        batches.stream().forEach { batch -> populateInner(jdsDb, null, innerObjects, initialisePrimitives, initialiseDatesAndTimes, initialiseObjects, batch) }
    }

    /**
     * @param entities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateFloatArrays(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val uuid = resultSet.getString("Uuid")
                val value = resultSet.getFloat("Value")
                val fieldId = resultSet.getLong("FieldId")
                optimalEntityLookup(entities, uuid).forEach { it.populateProperties(JdsFieldType.FLOAT_COLLECTION, fieldId, value) }
            }
        }
    }

    /**
     * @param entities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateDoubleArrays(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val uuid = resultSet.getString("Uuid")
                val value = resultSet.getDouble("Value")
                val fieldId = resultSet.getLong("FieldId")
                optimalEntityLookup(entities, uuid).forEach { it.populateProperties(JdsFieldType.DOUBLE_COLLECTION, fieldId, value) }
            }
        }
    }

    /**
     * @param entities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateLongArrays(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val uuid = resultSet.getString("Uuid")
                val value = resultSet.getLong("Value")
                val fieldId = resultSet.getLong("FieldId")
                optimalEntityLookup(entities, uuid).forEach { it.populateProperties(JdsFieldType.LONG_COLLECTION, fieldId, value) }
            }
        }
    }

    /**
     * @param entities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateDateTimeArrays(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val uuid = resultSet.getString("Uuid")
                val value = resultSet.getTimestamp("Value")
                val fieldId = resultSet.getLong("FieldId")
                optimalEntityLookup(entities, uuid).forEach { it.populateProperties(JdsFieldType.DATE_TIME_COLLECTION, fieldId, value) }
            }
        }
    }

    /**
     * @param entities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateStringArrays(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val uuid = resultSet.getString("Uuid")
                val value = resultSet.getString("Value")
                val fieldId = resultSet.getLong("FieldId")
                optimalEntityLookup(entities, uuid).forEach { it.populateProperties(JdsFieldType.STRING_COLLECTION, fieldId, value) }
            }
        }
    }

    /**
     * @param entities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateIntegerArraysAndEnums(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val uuid = resultSet.getString("Uuid")
                val value = resultSet.getInt("Value")
                val fieldId = resultSet.getLong("FieldId")
                optimalEntityLookup(entities, uuid).forEach {
                    it.populateProperties(JdsFieldType.INT_COLLECTION, fieldId, value)
                    it.populateProperties(JdsFieldType.ENUM_COLLECTION, fieldId, value)
                }
            }
        }
    }

    /**
     * @param entities
     * @param uuid
     * @return
     */
    private fun <T : JdsEntity> optimalEntityLookup(entities: Collection<T>, uuid: String): Stream<T> {
        return entities.parallelStream().filter { it.overview.uuid == uuid }
    }

    /**
     * @param uuids
     * @return
     */
    private fun createProcessingBatches(uuids: Collection<String>): MutableList<MutableList<String>> {
        val batches = ArrayList<MutableList<String>>()
        var index = 0
        var batch = 0
        for (uuid in uuids) {
            if (index == MAX_BATCH_SIZE) {
                batch++
                index = 0
            }
            if (index == 0)
                batches.add(ArrayList())
            batches[batch].add(uuid)
            index++
        }
        return batches
    }

    /**
     * @param entities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateOverviews(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val uuid = resultSet.getString("Uuid")
                val dateCreated = resultSet.getTimestamp("DateCreated")
                val dateModified = resultSet.getTimestamp("DateModified")
                val version = resultSet.getLong("Version")
                val live = resultSet.getBoolean("Live")
                optimalEntityLookup(entities, uuid).forEach { entity ->
                    entity.overview.dateModified = dateModified.toLocalDateTime()
                    entity.overview.dateCreated = dateCreated.toLocalDateTime()
                    entity.overview.version = version
                    entity.overview.live = live
                }
            }
        }
    }

    /**
     * @param entities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateDateTimeAndDate(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val uuid = resultSet.getString("Uuid")
                val value = resultSet.getTimestamp("Value")
                val fieldId = resultSet.getLong("FieldId")
                optimalEntityLookup(entities, uuid).forEach { entity ->
                    entity.populateProperties(JdsFieldType.DATE_TIME, fieldId, value)
                    entity.populateProperties(JdsFieldType.DATE, fieldId, value)
                }
            }
        }
    }

    /**
     * @param entities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateDouble(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val uuid = resultSet.getString("Uuid")
                val value = resultSet.getDouble("Value")
                val fieldId = resultSet.getLong("FieldId")
                optimalEntityLookup(entities, uuid).forEach { it.populateProperties(JdsFieldType.DOUBLE, fieldId, value) }
            }
        }
    }

    /**
     * @param entities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateBlobs(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val uuid = resultSet.getString("Uuid")
                val value = resultSet.getBytes("Value")
                val fieldId = resultSet.getLong("FieldId")
                optimalEntityLookup(entities, uuid).forEach { it.populateProperties(JdsFieldType.BLOB, fieldId, value) }
            }
        }
    }

    /**
     * @param entities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateIntegerAndEnum(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val uuid = resultSet.getString("Uuid")
                val value = resultSet.getInt("Value")
                val fieldId = resultSet.getLong("FieldId")
                optimalEntityLookup(entities, uuid).forEach {
                    it.populateProperties(JdsFieldType.INT, fieldId, value)
                    it.populateProperties(JdsFieldType.ENUM, fieldId, value)
                }
            }
        }
    }

    /**
     * @param entities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateBoolean(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val uuid = resultSet.getString("Uuid")
                val value = resultSet.getBoolean("Value")
                val fieldId = resultSet.getLong("FieldId")
                optimalEntityLookup(entities, uuid).forEach { it.populateProperties(JdsFieldType.BOOLEAN, fieldId, value) }
            }
        }
    }

    /**
     * @param entities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateTimes(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val uuid = resultSet.getString("Uuid")
                val value = resultSet.getTime("Value", jdsDb)
                val fieldId = resultSet.getLong("FieldId")
                optimalEntityLookup(entities, uuid).forEach { it.populateProperties(JdsFieldType.TIME, fieldId, value) }
            }
        }
    }

    /**
     * @param entities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateFloat(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val uuid = resultSet.getString("Uuid")
                val value = resultSet.getFloat("Value")
                val fieldId = resultSet.getLong("FieldId")
                optimalEntityLookup(entities, uuid).forEach { it.populateProperties(JdsFieldType.FLOAT, fieldId, value) }
            }
        }
    }

    /**
     * @param entities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateLongAndDuration(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val uuid = resultSet.getString("Uuid")
                val value = resultSet.getLong("Value")
                val fieldId = resultSet.getLong("FieldId")
                optimalEntityLookup(entities, uuid).forEach {
                    it.populateProperties(JdsFieldType.LONG, fieldId, value)
                    it.populateProperties(JdsFieldType.DURATION, fieldId, value)
                }
            }
        }
    }

    /**
     * @param entities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateZonedDateTime(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val uuid = resultSet.getString("Uuid")
                val value = resultSet.getZonedDateTime("Value", jdsDb)
                val fieldId = resultSet.getLong("FieldId")
                optimalEntityLookup(entities, uuid).forEach { it.populateProperties(JdsFieldType.ZONED_DATE_TIME, fieldId, value) }
            }
        }
    }

    /**
     * @param entities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateTextMonthDayYearMonthAndPeriod(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val uuid = resultSet.getString("Uuid")
                val value = resultSet.getString("Value")
                val fieldId = resultSet.getLong("FieldId")
                optimalEntityLookup(entities, uuid).forEach {
                    it.populateProperties(JdsFieldType.STRING, fieldId, value)
                    it.populateProperties(JdsFieldType.MONTH_DAY, fieldId, value)
                    it.populateProperties(JdsFieldType.YEAR_MONTH, fieldId, value)
                    it.populateProperties(JdsFieldType.PERIOD, fieldId, value)
                }
            }
        }
    }

    /**
     * @param jdsDataBase
     * @param batchSize
     * @param entityId
     * @param filterBatches
     * @param filterUuids
     */
    @Throws(SQLException::class, ClassNotFoundException::class)
    private fun prepareActionBatches(jdsDataBase: JdsDb, batchSize: Int, entityId: Long, filterBatches: MutableList<MutableList<String>>, filterUuids: Iterable<out String>) {
        var batchIndex = 0
        var batchContents = 0

        val entityAndChildren = ArrayList<Long>()
        entityAndChildren.add(entityId)

        jdsDataBase.getConnection().use {
            it.prepareStatement("SELECT ChildEntityCode FROM JdsEntityInheritance WHERE ParentEntityCode = ?").use {
                it.setLong(1, entityAndChildren[0])
                it.executeQuery().use {
                    while (it.next())
                        entityAndChildren.add(it.getLong("ChildEntityCode"))
                }
            }

            val entityHeirarchy = StringJoiner(",")
            for (id in entityAndChildren)
                entityHeirarchy.add(id.toString() + "")


            val rawSql = "SELECT DISTINCT Uuid FROM JdsEntityInstance WHERE EntityId IN (%s)"
            val rawSql2 = "SELECT DISTINCT Uuid from JdsEntityInstance WHERE Uuid IN (%s)"
            it.prepareStatement(String.format(rawSql, entityHeirarchy)).use { preparedStatement1 ->
                it.prepareStatement(String.format(rawSql2, quote(filterUuids))).use { preparedStatement2 ->
                    if (filterUuids.none()) {
                        //if no ids supplied we are looking for all instances of the entity.
                        //load ALL entityVersions in the in heirarchy
                        preparedStatement1.executeQuery().use { rs ->
                            while (rs.next()) {
                                if (batchContents == batchSize) {
                                    batchIndex++
                                    batchContents = 0
                                }
                                if (batchContents == 0)
                                    filterBatches.add(ArrayList())
                                filterBatches[batchIndex].add(rs.getString("Uuid"))
                                batchContents++
                            }
                        }
                    } else {
                        //load all in filter
                        preparedStatement2.executeQuery().use { rs ->
                            while (rs.next()) {
                                if (batchContents == batchSize) {
                                    batchIndex++
                                    batchContents = 0
                                }
                                if (batchContents == 0)
                                    filterBatches.add(ArrayList())
                                filterBatches[batchIndex].add(rs.getString("Uuid"))
                                batchContents++
                            }
                        }
                    }
                }
            }
        }
    }

    private fun quote(filterGuids: Iterable<out String>): String {
        val list = filterGuids.map { String.format("'%s'", it) }
        return list.joinToString(",")
    }

    /**
     * @param size
     * @return
     */
    private fun prepareParamaterSequence(size: Int): String {
        val questionArray = arrayOfNulls<String>(size)
        for (index in 0 until size) {
            questionArray[index] = "?"
        }
        return questionArray.joinToString(",")
    }

    /**
     * @param preparedStatement
     * @param index
     * @param uuid
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun setParameterForStatement(index: Int, uuid: String, preparedStatement: PreparedStatement) {
        preparedStatement.setString(index, uuid)
    }

    @Throws(Exception::class)
    override fun call(): MutableList<T> {
        val annotation = referenceType.getAnnotation(JdsEntityAnnotation::class.java)
        val entityId = annotation.entityId
        val filterBatches = ArrayList(ArrayList<MutableList<String>>())
        val castCollection = collections
        prepareActionBatches(jdsDb, MAX_BATCH_SIZE, entityId, filterBatches, searchUuids)
        val initialisePrimitives = true
        val initialiseDatesAndTimes = true
        val initialiseObjects = true
        for (currentBatch in filterBatches) {
            populateInner(jdsDb, referenceType, castCollection, initialisePrimitives, initialiseDatesAndTimes, initialiseObjects, currentBatch)
        }
        if (comparator != null)
            collections.sortWith(comparator!!)
        return collections
    }

    companion object {

        /**
         * Java supports up to 1000 prepared supportsStatements depending on the driver
         */
        val MAX_BATCH_SIZE = 1000
    }
}
