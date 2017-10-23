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

import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation
import io.github.subiyacryolite.jds.enums.JdsFieldType
import io.github.subiyacryolite.jds.enums.JdsImplementation
import io.github.subiyacryolite.jds.events.JdsLoadListener
import io.github.subiyacryolite.jds.events.OnPostLoadEventArguments
import io.github.subiyacryolite.jds.events.OnPreLoadEventArguments
import java.sql.PreparedStatement
import java.sql.SQLException
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.stream.Stream

/**
 * This class is responsible for loading an [entityVersions][JdsEntity] [fields][JdsField]
 */
class JdsLoad<T : JdsEntity> : Callable<MutableList<T>> {
    private val collections = ArrayList<T>()
    private val jdsDb: JdsDb
    private val referenceType: Class<T>
    private val searchGuids: Array<out String>
    private var comparator: Comparator<in T>? = null

    /**
     * @param jdsDb
     * @param referenceType
     * @param comparator
     * @param searchGuids
     */
    constructor(jdsDb: JdsDb, referenceType: Class<T>, comparator: Comparator<T>, vararg searchGuids: String) {
        this.jdsDb = jdsDb
        this.referenceType = referenceType
        this.searchGuids = searchGuids
        this.comparator = comparator
    }

    /**
     * @param jdsDb
     * @param referenceType
     * @param searchGuids
     */
    constructor(jdsDb: JdsDb, referenceType: Class<T>, vararg searchGuids: String) {
        this.jdsDb = jdsDb
        this.referenceType = referenceType
        this.searchGuids = searchGuids
    }


    /**
     * @param jdsDb
     * @param referenceType
     * @param entities
     * @param initialisePrimitives
     * @param initialiseDatesAndTimes
     * @param initialiseObjects
     * @param entityGuids
     * @param <T>
    </T> */

    private fun <T : JdsEntity> populateInner(jdsDb: JdsDb,
                                              referenceType: Class<T>?,
                                              entities: MutableCollection<T>,
                                              initialisePrimitives: Boolean,
                                              initialiseDatesAndTimes: Boolean,
                                              initialiseObjects: Boolean,
                                              entityGuids: Collection<String>) {
        val questionsString = getQuestions(entityGuids.size)
        //primitives
        val sqlTextValues = String.format("SELECT EntityGuid, Value, FieldId FROM JdsStoreText WHERE EntityGuid IN (%s)", questionsString)
        val sqlLongValues = String.format("SELECT EntityGuid, Value, FieldId FROM JdsStoreLong WHERE EntityGuid IN (%s)", questionsString)
        val sqlIntegerValues = String.format("SELECT EntityGuid, Value, FieldId FROM JdsStoreInteger WHERE EntityGuid IN (%s)", questionsString)
        val sqlFloatValues = String.format("SELECT EntityGuid, Value, FieldId FROM JdsStoreFloat WHERE EntityGuid IN (%s)", questionsString)
        val sqlDoubleValues = String.format("SELECT EntityGuid, Value, FieldId FROM JdsStoreDouble WHERE EntityGuid IN (%s)", questionsString)
        val sqlDateTimeValues = String.format("SELECT EntityGuid, Value, FieldId FROM JdsStoreDateTime WHERE EntityGuid IN (%s)", questionsString)
        val sqlTimeValues = String.format("SELECT EntityGuid, Value, FieldId FROM JdsStoreTime WHERE EntityGuid IN (%s)", questionsString)
        val sqlZonedDateTimeValues = String.format("SELECT EntityGuid, Value, FieldId FROM JdsStoreZonedDateTime WHERE EntityGuid IN (%s)", questionsString)
        //blobs
        val sqlBlobs = String.format("SELECT EntityGuid, Value, FieldId FROM JdsStoreBlob WHERE EntityGuid IN (%s)", questionsString)
        //array
        val sqlTextArrayValues = String.format("SELECT EntityGuid, Value, FieldId, Sequence FROM JdsStoreTextArray WHERE EntityGuid IN (%s)", questionsString)
        val sqlIntegerArrayAndEnumValues = String.format("SELECT EntityGuid, Value, FieldId, Sequence FROM JdsStoreIntegerArray WHERE EntityGuid IN (%s)", questionsString)
        val sqlLongArrayValues = String.format("SELECT EntityGuid, Value, FieldId, Sequence FROM JdsStoreLongArray WHERE EntityGuid IN (%s)", questionsString)
        val sqlFloatArrayValues = String.format("SELECT EntityGuid, Value, FieldId, Sequence FROM JdsStoreFloatArray WHERE EntityGuid IN (%s)", questionsString)
        val sqlDoubleArrayValues = String.format("SELECT EntityGuid, Value, FieldId, Sequence FROM JdsStoreDoubleArray WHERE EntityGuid IN (%s)", questionsString)
        val sqlDateTimeArrayValues = String.format("SELECT EntityGuid, Value, FieldId, Sequence FROM JdsStoreDateTimeArray WHERE EntityGuid IN (%s)", questionsString)
        val sqlEmbeddedAndArrayObjects = String.format("SELECT ChildEntityGuid, ParentEntityGuid, ChildEntityId, FieldId FROM JdsStoreEntityBinding WHERE ParentEntityGuid IN (%s)", questionsString)
        //overviews
        val sqlOverviews = String.format("SELECT EntityGuid, DateCreated, DateModified, Live, Version FROM JdsStoreEntityOverview WHERE EntityGuid IN (%s)", questionsString)
        try {
            jdsDb.getConnection().use { connection ->
                connection.prepareStatement(sqlBlobs).use { blobs ->
                    connection.prepareStatement(sqlTextValues).use { strings ->
                        connection.prepareStatement(sqlLongValues).use { longs ->
                            connection.prepareStatement(sqlIntegerValues).use { integers ->
                                connection.prepareStatement(sqlFloatValues).use { floats ->
                                    connection.prepareStatement(sqlDoubleValues).use { doubles ->
                                        connection.prepareStatement(sqlDateTimeValues).use { dateTimes ->
                                            connection.prepareStatement(sqlTimeValues).use { times ->
                                                connection.prepareStatement(sqlZonedDateTimeValues).use { zonedDateTimes ->
                                                    connection.prepareStatement(sqlTextArrayValues).use { textArrays ->
                                                        connection.prepareStatement(sqlIntegerArrayAndEnumValues).use { integerArraysAndEnums ->
                                                            connection.prepareStatement(sqlLongArrayValues).use { longArrays ->
                                                                connection.prepareStatement(sqlFloatArrayValues).use { floatArrays ->
                                                                    connection.prepareStatement(sqlDoubleArrayValues).use { doubleArrays ->
                                                                        connection.prepareStatement(sqlDateTimeArrayValues).use { dateTimeArrays ->
                                                                            connection.prepareStatement(sqlEmbeddedAndArrayObjects).use { embeddedAndArrayObjects ->
                                                                                connection.prepareStatement(sqlOverviews).use { overviews ->
                                                                                    //work in batches to not break prepared statement
                                                                                    var batchSequence = 1
                                                                                    for (entityGuid in entityGuids) {
                                                                                        if (referenceType != null && (initialisePrimitives || initialiseDatesAndTimes || initialiseObjects)) {
                                                                                            //sometimes the entityVersions would already have been instanciated, thus we only need to populate
                                                                                            val entity = referenceType.newInstance()
                                                                                            entity.overview.entityGuid = entityGuid
                                                                                            entities.add(entity)
                                                                                        }
                                                                                        //primitives
                                                                                        setParameterForStatement(batchSequence, entityGuid, strings)
                                                                                        setParameterForStatement(batchSequence, entityGuid, integers)
                                                                                        setParameterForStatement(batchSequence, entityGuid, longs)
                                                                                        setParameterForStatement(batchSequence, entityGuid, floats)
                                                                                        setParameterForStatement(batchSequence, entityGuid, doubles)
                                                                                        setParameterForStatement(batchSequence, entityGuid, dateTimes)
                                                                                        setParameterForStatement(batchSequence, entityGuid, times)
                                                                                        setParameterForStatement(batchSequence, entityGuid, zonedDateTimes)
                                                                                        //blobs
                                                                                        setParameterForStatement(batchSequence, entityGuid, blobs)
                                                                                        //array
                                                                                        setParameterForStatement(batchSequence, entityGuid, textArrays)
                                                                                        setParameterForStatement(batchSequence, entityGuid, longArrays)
                                                                                        setParameterForStatement(batchSequence, entityGuid, floatArrays)
                                                                                        setParameterForStatement(batchSequence, entityGuid, doubleArrays)
                                                                                        setParameterForStatement(batchSequence, entityGuid, dateTimeArrays)
                                                                                        setParameterForStatement(batchSequence, entityGuid, integerArraysAndEnums)
                                                                                        //object and object arrays
                                                                                        setParameterForStatement(batchSequence, entityGuid, embeddedAndArrayObjects)
                                                                                        //overview
                                                                                        setParameterForStatement(batchSequence, entityGuid, overviews)
                                                                                        batchSequence++
                                                                                    }
                                                                                    //catch embedded/pre-created objects objects as well
                                                                                    for (entity in entities)
                                                                                        if (entity is JdsLoadListener)
                                                                                            (entity as JdsLoadListener).onPreLoad(OnPreLoadEventArguments(jdsDb, connection, entity.overview.entityGuid, batchSequence, entityGuids.size))

                                                                                    if (jdsDb.isWritingToPrimaryDataTables && initialisePrimitives) {
                                                                                        //primitives
                                                                                        populateTextMonthDayYearMonthAndPeriod(entities, strings)
                                                                                        populateLongAndDuration(entities, longs)
                                                                                        populateIntegerEnumAndBoolean(entities, integers)
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
                                                                                        if (jdsDb.isWritingToPrimaryDataTables) {
                                                                                            //blobs
                                                                                            populateBlobs(entities, blobs)
                                                                                        }
                                                                                        populateObjectEntriesAndObjectArrays(jdsDb, entities, embeddedAndArrayObjects, initialisePrimitives, initialiseDatesAndTimes, initialiseObjects)
                                                                                    }
                                                                                    populateOverviews(entities, overviews)
                                                                                    //catch embedded/pre-created objects objects as well
                                                                                    for (entity in entities)
                                                                                        if (entity is JdsLoadListener)
                                                                                            (entity as JdsLoadListener).onPostLoad(OnPostLoadEventArguments(jdsDb, connection, entity.overview.entityGuid))

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
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateObjectEntriesAndObjectArrays(jdsDb: JdsDb, jdsEntities: Collection<T>,
                                                                     preparedStatement: PreparedStatement,
                                                                     initialisePrimitives: Boolean,
                                                                     initialiseDatesAndTimes: Boolean,
                                                                     initialiseObjects: Boolean) {
        val entityGuids = HashSet<String>()//ids should be unique
        val innerObjects = ConcurrentLinkedQueue<JdsEntity>()//can be multiple copies of the same object however
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val parentEntityGuid = resultSet.getString("ParentEntityGuid")
                val entityGuid = resultSet.getString("ChildEntityGuid")
                val fieldId = resultSet.getLong("FieldId")
                val entityId = resultSet.getLong("ChildEntityId")
                optimalEntityLookup(jdsEntities, parentEntityGuid).forEach { it.populateObjects(jdsDb, fieldId, entityId, entityGuid, innerObjects, entityGuids) }
            }
        }
        val batches = createProcessingBatches(entityGuids)
        batches.stream().forEach { batch -> populateInner(jdsDb, null, innerObjects, initialisePrimitives, initialiseDatesAndTimes, initialiseObjects, batch) }
    }

    /**
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateFloatArrays(jdsEntities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val entityGuid = resultSet.getString("EntityGuid")
                val value = resultSet.getFloat("Value")
                val fieldId = resultSet.getLong("FieldId")
                optimalEntityLookup(jdsEntities, entityGuid).forEach { it.populateProperties(JdsFieldType.ARRAY_FLOAT, fieldId, value) }
            }
        }
    }

    /**
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateDoubleArrays(jdsEntities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val entityGuid = resultSet.getString("EntityGuid")
                val value = resultSet.getDouble("Value")
                val fieldId = resultSet.getLong("FieldId")
                optimalEntityLookup(jdsEntities, entityGuid).forEach { it.populateProperties(JdsFieldType.ARRAY_DOUBLE, fieldId, value) }
            }
        }
    }

    /**
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateLongArrays(jdsEntities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val entityGuid = resultSet.getString("EntityGuid")
                val value = resultSet.getLong("Value")
                val fieldId = resultSet.getLong("FieldId")
                optimalEntityLookup(jdsEntities, entityGuid).forEach { it.populateProperties(JdsFieldType.ARRAY_LONG, fieldId, value) }
            }
        }
    }

    /**
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateDateTimeArrays(jdsEntities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val entityGuid = resultSet.getString("EntityGuid")
                val value = resultSet.getTimestamp("Value")
                val fieldId = resultSet.getLong("FieldId")
                optimalEntityLookup(jdsEntities, entityGuid).forEach { it.populateProperties(JdsFieldType.ARRAY_DATE_TIME, fieldId, value) }
            }
        }
    }

    /**
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateStringArrays(jdsEntities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val entityGuid = resultSet.getString("EntityGuid")
                val value = resultSet.getString("Value")
                val fieldId = resultSet.getLong("FieldId")
                optimalEntityLookup(jdsEntities, entityGuid).forEach { it.populateProperties(JdsFieldType.ARRAY_TEXT, fieldId, value) }
            }
        }
    }

    /**
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateIntegerArraysAndEnums(jdsEntities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val entityGuid = resultSet.getString("EntityGuid")
                val value = resultSet.getInt("Value")
                val fieldId = resultSet.getLong("FieldId")
                optimalEntityLookup(jdsEntities, entityGuid).forEach {
                    it.populateProperties(JdsFieldType.ARRAY_INT, fieldId, value)
                    it.populateProperties(JdsFieldType.ENUM_COLLECTION, fieldId, value)
                }
            }
        }
    }

    /**
     * @param jdsEntities
     * @param entityGuid
     * @return
     */
    private fun <T : JdsEntity> optimalEntityLookup(jdsEntities: Collection<T>, entityGuid: String): Stream<T> {
        return jdsEntities.parallelStream().filter { it.overview.entityGuid == entityGuid }
    }

    /**
     * @param entityGuids
     * @return
     */
    private fun createProcessingBatches(entityGuids: Collection<String>): MutableList<MutableList<String>> {
        val batches = ArrayList<MutableList<String>>()
        var index = 0
        var batch = 0
        for (entityGuid in entityGuids) {
            if (index == MAX_BATCH_SIZE) {
                batch++
                index = 0
            }
            if (index == 0)
                batches.add(ArrayList())
            batches[batch].add(entityGuid)
            index++
        }
        return batches
    }

    /**
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateOverviews(jdsEntities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val entityGuid = resultSet.getString("EntityGuid")
                val dateCreated = resultSet.getTimestamp("DateCreated")
                val dateModified = resultSet.getTimestamp("DateModified")
                val version = resultSet.getLong("Version")
                val live = resultSet.getBoolean("Live")
                optimalEntityLookup(jdsEntities, entityGuid).forEach { entity ->
                    entity.overview.dateModified = dateModified.toLocalDateTime()
                    entity.overview.dateCreated = dateCreated.toLocalDateTime()
                    entity.overview.version = version
                    entity.overview.live = live
                }
            }
        }
    }

    /**
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateDateTimeAndDate(jdsEntities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val entityGuid = resultSet.getString("EntityGuid")
                val value = resultSet.getTimestamp("Value")
                val fieldId = resultSet.getLong("FieldId")
                optimalEntityLookup(jdsEntities, entityGuid).forEach { entity ->
                    entity.populateProperties(JdsFieldType.DATE_TIME, fieldId, value)
                    entity.populateProperties(JdsFieldType.DATE, fieldId, value)
                }
            }
        }
    }

    /**
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateDouble(jdsEntities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val entityGuid = resultSet.getString("EntityGuid")
                val value = resultSet.getDouble("Value")
                val fieldId = resultSet.getLong("FieldId")
                optimalEntityLookup(jdsEntities, entityGuid).forEach { it.populateProperties(JdsFieldType.DOUBLE, fieldId, value) }
            }
        }
    }

    /**
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateBlobs(jdsEntities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val entityGuid = resultSet.getString("EntityGuid")
                val value = resultSet.getBytes("Value")
                val fieldId = resultSet.getLong("FieldId")
                optimalEntityLookup(jdsEntities, entityGuid).forEach { it.populateProperties(JdsFieldType.BLOB, fieldId, value) }
            }
        }
    }

    /**
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateIntegerEnumAndBoolean(jdsEntities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val entityGuid = resultSet.getString("EntityGuid")
                val value = resultSet.getInt("Value")
                val fieldId = resultSet.getLong("FieldId")
                optimalEntityLookup(jdsEntities, entityGuid).forEach { entity ->
                    entity.populateProperties(JdsFieldType.INT, fieldId, value)
                    entity.populateProperties(JdsFieldType.BOOLEAN, fieldId, value)
                    entity.populateProperties(JdsFieldType.ENUM, fieldId, value)
                }
            }
        }
    }

    /**
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateTimes(jdsEntities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val entityGuid = resultSet.getString("EntityGuid")
                val value = resultSet.getInt("Value")
                val fieldId = resultSet.getLong("FieldId")
                optimalEntityLookup(jdsEntities, entityGuid).forEach { it.populateProperties(JdsFieldType.TIME, fieldId, value) }
            }
        }
    }

    /**
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateFloat(jdsEntities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val entityGuid = resultSet.getString("EntityGuid")
                val value = resultSet.getFloat("Value")
                val fieldId = resultSet.getLong("FieldId")
                optimalEntityLookup(jdsEntities, entityGuid).forEach { it.populateProperties(JdsFieldType.FLOAT, fieldId, value) }
            }
        }
    }

    /**
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateLongAndDuration(jdsEntities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val entityGuid = resultSet.getString("EntityGuid")
                val value = resultSet.getLong("Value")
                val fieldId = resultSet.getLong("FieldId")
                optimalEntityLookup(jdsEntities, entityGuid).forEach {
                    it.populateProperties(JdsFieldType.LONG, fieldId, value)
                    it.populateProperties(JdsFieldType.DURATION, fieldId, value)
                }
            }
        }
    }

    /**
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateZonedDateTime(jdsEntities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val entityGuid = resultSet.getString("EntityGuid")
                val value = when (jdsDb.implementation) {
                    JdsImplementation.TSQL -> resultSet.getString("Value")
                    JdsImplementation.POSTGRES -> resultSet.getObject("Value")
                    JdsImplementation.MYSQL, JdsImplementation.ORACLE -> resultSet.getTimestamp("Value")
                    else -> resultSet.getLong("Value")
                }
                val fieldId = resultSet.getLong("FieldId")
                optimalEntityLookup(jdsEntities, entityGuid).forEach { it.populateProperties(JdsFieldType.ZONED_DATE_TIME, fieldId, value) }
            }
        }
    }

    /**
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateTextMonthDayYearMonthAndPeriod(jdsEntities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val entityGuid = resultSet.getString("EntityGuid")
                val value = resultSet.getString("Value")
                val fieldId = resultSet.getLong("FieldId")
                optimalEntityLookup(jdsEntities, entityGuid).forEach {
                    it.populateProperties(JdsFieldType.TEXT, fieldId, value)
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
     * @param filterGuids
     */
    @Throws(SQLException::class, ClassNotFoundException::class)
    private fun prepareActionBatches(jdsDataBase: JdsDb, batchSize: Int, entityId: Long, filterBatches: MutableList<MutableList<String>>, filterGuids: Array<out String>) {
        var batchIndex = 0
        var batchContents = 0

        val entityAndChildren = ArrayList<Long>()
        entityAndChildren.add(entityId)

        jdsDataBase.getConnection().use { connection ->
            connection.prepareStatement("SELECT ChildEntityCode FROM JdsRefEntityInheritance WHERE ParentEntityCode = ?").use { preparedStatement ->
                preparedStatement.setLong(1, entityAndChildren[0])
                preparedStatement.executeQuery().use { rs ->
                    while (rs.next()) {
                        entityAndChildren.add(rs.getLong("ChildEntityCode"))
                    }
                }
            }

            val entityHeirarchy = StringJoiner(",")
            for (id in entityAndChildren)
                entityHeirarchy.add(id.toString() + "")


            val rawSql = "SELECT DISTINCT EntityGuid FROM JdsStoreEntityInheritance WHERE EntityId IN (%s)"
            val rawSql2 = "SELECT DISTINCT EntityGuid from JdsStoreEntityInheritance WHERE EntityGuid IN (%s)"
            connection.prepareStatement(String.format(rawSql, entityHeirarchy)).use { preparedStatement1 ->
                connection.prepareStatement(String.format(rawSql2, quote(filterGuids))).use { preparedStatement2 ->
                    if (filterGuids.size == 0) {
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
                                filterBatches[batchIndex].add(rs.getString("EntityGuid"))
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
                                filterBatches[batchIndex].add(rs.getString("EntityGuid"))
                                batchContents++
                            }
                        }
                    }
                }
            }
        }
    }

    private fun quote(filterGuids: Array<out String>): String {
        val list = filterGuids.map { String.format("'%s'", it) }
        return list.joinToString(",")
    }

    /**
     * @param size
     * @return
     */
    private fun getQuestions(size: Int): String {
        val questionArray = arrayOfNulls<String>(size)
        for (index in 0 until size) {
            questionArray[index] = "?"
        }
        return questionArray.joinToString(",")
    }

    /**
     * @param preparedStatement
     * @param index
     * @param entityGuid
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun setParameterForStatement(index: Int, entityGuid: String, preparedStatement: PreparedStatement) {
        preparedStatement.setString(index, entityGuid)
    }

    @Throws(Exception::class)
    override fun call(): MutableList<T> {
        val annotation = referenceType.getAnnotation(JdsEntityAnnotation::class.java)
        val entityId = annotation.entityId
        val filterBatches = ArrayList(ArrayList<MutableList<String>>())
        val castCollection = collections
        prepareActionBatches(jdsDb, MAX_BATCH_SIZE, entityId, filterBatches, searchGuids)
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

        /**
         * @param jdsDb
         * @param referenceType
         * @param comparator
         * @param entityGuids
         * @param <T>
         * @return
        </T> */
        @Deprecated("please refer to <a href=\"https://github.com/SubiyaCryolite/Jenesis-Data-Store\"> the readme</a> for the most up to date CRUD approach", ReplaceWith("JdsLoad(jdsDb, referenceType, comparator, *entityGuids).call()", "io.github.subiyacryolite.jds.JdsLoad"))
        @Throws(Exception::class)
        fun <T : JdsEntity> load(jdsDb: JdsDb, referenceType: Class<T>, comparator: Comparator<T>, vararg entityGuids: String): List<T> {
            return JdsLoad(jdsDb, referenceType, comparator, *entityGuids).call()
        }

        /**
         * @param jdsDb
         * @param referenceType
         * @param entityGuids
         * @param <T>
         * @return
        </T> */
        @Deprecated("please refer to <a href=\"https://github.com/SubiyaCryolite/Jenesis-Data-Store\"> the readme</a> for the most up to date CRUD approach", ReplaceWith("JdsLoad(jdsDb, referenceType, *entityGuids).call()", "io.github.subiyacryolite.jds.JdsLoad"))
        @Throws(Exception::class)
        fun <T : JdsEntity> load(jdsDb: JdsDb, referenceType: Class<T>, vararg entityGuids: String): List<T> {
            return JdsLoad(jdsDb, referenceType, *entityGuids).call()
        }
    }
}
