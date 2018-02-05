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

import io.github.subiyacryolite.jds.JdsExtensions.getLocalTime
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
import kotlin.collections.ArrayList

/**
 * This class is responsible for loading an [entity's][JdsEntity] [fields][JdsField]
 */
class JdsLoad<T : JdsEntity> : Callable<MutableList<T>> {
    private val collections = ArrayList<T>()
    private val jdsDb: JdsDb
    private val searchUUIDs: Iterable<out String>
    private val alternateConnections: ConcurrentMap<Int, Connection> = ConcurrentHashMap()
    private val referenceType: Class<T>

    /**
     * @param jdsDb
     * @param referenceType
     * @param searchUUIDs
     */
    @JvmOverloads
    constructor(jdsDb: JdsDb, referenceEntity: Class<T>, searchUUIDs: Collection<String> = emptyList()) {
        this.jdsDb = jdsDb
        this.searchUUIDs = searchUUIDs
        this.referenceType = referenceEntity
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
                                              entities: MutableCollection<T>,
                                              initialisePrimitives: Boolean,
                                              initialiseDatesAndTimes: Boolean,
                                              initialiseObjects: Boolean,
                                              uuids: Collection<String>) {
        val questionsString = prepareParamaterSequence(uuids.size)
        //val entity information
        val sqlEntities = "SELECT uuid, uuid_location, uuid_location_version, entity_id FROM jds_entity_overview WHERE composite_key IN ($questionsString)"
        //primitives
        val sqlBooleans = "SELECT composite_key, value, field_id FROM jds_store_boolean WHERE composite_key IN ($questionsString)"
        val sqlStrings = "SELECT composite_key, value, field_id FROM jds_store_text WHERE composite_key IN ($questionsString)"
        val sqlLongs = "SELECT composite_key, value, field_id FROM jds_store_long WHERE composite_key IN ($questionsString)"
        val sqlIntegers = "SELECT composite_key, value, field_id FROM jds_store_integer WHERE composite_key IN ($questionsString)"
        val sqlFloats = "SELECT composite_key, value, field_id FROM jds_store_float WHERE composite_key IN ($questionsString)"
        val sqlDoubles = "SELECT composite_key, value, field_id FROM jds_store_double WHERE composite_key IN ($questionsString)"
        val sqlDateTimes = "SELECT composite_key, value, field_id FROM jds_store_date_time WHERE composite_key IN ($questionsString)"
        val sqlTimes = "SELECT composite_key, value, field_id FROM jds_store_time WHERE composite_key IN ($questionsString)"
        val sqlZonedDateTimes = "SELECT composite_key, value, field_id FROM jds_store_zoned_date_time WHERE composite_key IN ($questionsString)"
        //blobs
        val sqlBlobs = "SELECT composite_key, value, field_id FROM jds_store_blob WHERE composite_key IN ($questionsString)"
        //array
        val sqlStringCollections = "SELECT composite_key, value, field_id, sequence FROM jds_store_text_array WHERE composite_key IN ($questionsString)"
        val sqlIntegerAndEnumCollections = "SELECT composite_key, value, field_id, sequence FROM jds_store_integer_array WHERE composite_key IN ($questionsString)"
        val sqlLongCollections = "SELECT composite_key, value, field_id, sequence FROM jds_store_long_array WHERE composite_key IN ($questionsString)"
        val sqlFloatCollections = "SELECT composite_key, value, field_id, sequence FROM jds_store_float_array WHERE composite_key IN ($questionsString)"
        val sqlDoubleCollections = "SELECT composite_key, value, field_id, sequence FROM jds_store_double_array WHERE composite_key IN ($questionsString)"
        val sqlDateTimeCollections = "SELECT composite_key, value, field_id, sequence FROM jds_store_date_time_array WHERE composite_key IN ($questionsString)"
        val sqlEmbeddedAndArrayObjects = "SELECT eb.child_composite_key, eb.parent_composite_key, eb.child_entity_id, eb.field_id, eo.uuid_location, eo.uuid_location_version FROM jds_entity_binding eb JOIN jds_entity_overview eo ON eo.composite_key = eb.child_composite_key AND eb.parent_composite_key IN ($questionsString)"
        //overviews
        val sqlOverviews = "SELECT uuid, live, entity_version FROM jds_entity_overview WHERE uuid IN ($questionsString)"
        try {

            jdsDb.getConnection().use { connection ->
                connection.prepareStatement(sqlEntities).use { entityLookUp ->
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
                                                                                                setParameterForStatement(batchSequence, uuid, entityLookUp)
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
                                                                                            if (initialisePrimitives || initialiseDatesAndTimes || initialiseObjects) {
                                                                                                createEntities(entities, entityLookUp)
                                                                                            }
                                                                                            entities.filterIsInstance<JdsLoadListener>().forEach { it.onPreLoad(OnPreLoadEventArguments(jdsDb, connection, alternateConnections)) }
                                                                                            populateOverviews(entities, overviews)
                                                                                            if (jdsDb.isWritingToPrimaryDataTables && initialisePrimitives) {
                                                                                                //strings, derived from strings and string arrays
                                                                                                populateTextMonthDayYearMonthAndPeriod(entities, strings)
                                                                                                populateStringArrays(entities, textArrays)
                                                                                                //primitives
                                                                                                populateLongAndDuration(entities, longs)
                                                                                                populateBoolean(entities, booleans)
                                                                                                populateIntegerAndEnum(entities, integers)
                                                                                                populateFloat(entities, floats)
                                                                                                populateDouble(entities, doubles)
                                                                                                //primitive arrays
                                                                                                //despite being primitives, array types are assumed to not contain null elements
                                                                                                populateIntegerArraysAndEnums(entities, integerArraysAndEnums)
                                                                                                populateFloatArrays(entities, floatArrays)
                                                                                                populateLongArrays(entities, longArrays)
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
                                                                                            //catch embedded/pre-created objects objects as well
                                                                                            entities.filterIsInstance<JdsLoadListener>().forEach { it.onPostLoad(OnPostLoadEventArguments(jdsDb, connection, alternateConnections)) }

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
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    private fun <T : JdsEntity> createEntities(entities: MutableCollection<in T>, entityLookUp: PreparedStatement) {
        entityLookUp.executeQuery().use {
            while (it.next()) {
                val entityId = it.getLong("entity_id")
                val uuid = it.getString("uuid")
                val uuidLocation = it.getString("uuid_location")
                val uuidLocationVersion = it.getInt("uuid_location_version")
                if (jdsDb.classes.containsKey(entityId)) {
                    val refType = jdsDb.classes[entityId]!!
                    val entity = refType.newInstance()
                    entity.overview.uuid = uuid
                    entity.overview.uuidLocation = uuidLocation
                    entity.overview.uuidLocationVersion = uuidLocationVersion
                    entities.add(entity as T)
                }
            }
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
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val parentUUID = it.getString("parent_composite_key")
                val uuid = it.getString("child_composite_key")
                val fieldId = it.getLong("field_id")
                val entityId = it.getLong("child_entity_id")
                val uuidLocation = it.getString("uuid_location")
                val uuidLocationVersion = it.getInt("uuid_location_version")
                optimalEntityLookup(entities, parentUUID).forEach { it.populateObjects(jdsDb, fieldId, entityId, uuid, uuidLocation, uuidLocationVersion, parentUUID, innerObjects, uuids) }
            }
        }
        val batches = createProcessingBatches(uuids)
        batches.forEach { populateInner(jdsDb, innerObjects, initialisePrimitives, initialiseDatesAndTimes, initialiseObjects, it) }
    }

    /**
     * @param entities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateFloatArrays(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("composite_key")
                val value = it.getFloat("value")
                val fieldId = it.getLong("field_id")
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
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("composite_key")
                val value = it.getDouble("value")
                val fieldId = it.getLong("field_id")
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
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("composite_key")
                val value = it.getLong("value")
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid).forEach {
                    it.populateProperties(JdsFieldType.LONG_COLLECTION, fieldId, value)
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
    private fun <T : JdsEntity> populateDateTimeArrays(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("composite_key")
                val value = it.getTimestamp("value")
                val fieldId = it.getLong("field_id")
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
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("composite_key")
                val value = it.getString("value")
                val fieldId = it.getLong("field_id")
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
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("composite_key")
                val value = it.getInt("value")
                val fieldId = it.getLong("field_id")
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
    private fun <T : JdsEntity> optimalEntityLookup(entities: Collection<T>, uuid: String): List<T> {
        return entities.filter { it.overview.compositeKey == uuid }
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
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("composite_key")
                val version = it.getLong("entity_version")
                val live = it.getBoolean("live")
                optimalEntityLookup(entities, uuid).forEach {
                    it.overview.version = version
                    it.overview.live = live
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
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("composite_key")
                val value = it.getTimestamp("value")
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid).forEach {
                    it.populateProperties(JdsFieldType.DATE_TIME, fieldId, value)
                    it.populateProperties(JdsFieldType.DATE, fieldId, value)
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
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("composite_key")
                val value = it.getObject("value") //primitives can be null
                val fieldId = it.getLong("field_id")
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
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("composite_key")
                val value = it.getBytes("value")
                val fieldId = it.getLong("field_id")
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
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("composite_key")
                val value = it.getObject("value") //primitives can be null
                val fieldId = it.getLong("field_id")
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
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("composite_key")
                val value = it.getObject("value") //primitives can be null
                val fieldId = it.getLong("field_id")
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
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("composite_key")
                val value = it.getLocalTime("value", jdsDb)
                val fieldId = it.getLong("field_id")
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
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("composite_key")
                val value = it.getObject("value") //primitives can be null
                val fieldId = it.getLong("field_id")
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
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("composite_key")
                val fieldId = it.getLong("field_id")
                val value = it.getObject("value") //primitives can be null
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
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("composite_key")
                val value = it.getZonedDateTime("value", jdsDb)
                val fieldId = it.getLong("field_id")
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
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("composite_key")
                val value = it.getString("value")
                val fieldId = it.getLong("field_id")
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
     * @param filterUUIDs
     */
    @Throws(SQLException::class, ClassNotFoundException::class)
    private fun prepareActionBatches(jdsDataBase: JdsDb, batchSize: Int, entityId: Long, filterBatches: MutableList<MutableList<String>>, filterUUIDs: Iterable<out String>) {
        var batchIndex = 0
        var batchContents = 0

        val entityAndChildren = ArrayList<Long>()
        entityAndChildren.add(entityId)

        jdsDataBase.getConnection().use {
            it.prepareStatement("SELECT child_entity_id FROM jds_ref_entity_inheritance WHERE parent_entity_id = ?").use {
                it.setLong(1, entityAndChildren[0])
                it.executeQuery().use {
                    while (it.next())
                        entityAndChildren.add(it.getLong("child_entity_id"))
                }
            }

            val entityHeirarchy = StringJoiner(",")
            for (id in entityAndChildren)
                entityHeirarchy.add(id.toString())


            val rawSql = "SELECT DISTINCT entity_composite_key FROM jds_entity_instance WHERE entity_id IN ($entityHeirarchy)"
            val rawSql2 = "SELECT DISTINCT composite_key FROM jds_entity_overview WHERE uuid IN (${quote(filterUUIDs)})"
            it.prepareStatement(rawSql).use { preparedStatement1 ->
                it.prepareStatement(rawSql2).use { preparedStatement2 ->
                    if (filterUUIDs.none()) {
                        //if no ids supplied we are looking for all instances of the entity.
                        //load ALL entityVersions in the in heirarchy
                        preparedStatement1.executeQuery().use {
                            while (it.next()) {
                                if (batchContents == batchSize) {
                                    batchIndex++
                                    batchContents = 0
                                }
                                if (batchContents == 0)
                                    filterBatches.add(ArrayList())
                                filterBatches[batchIndex].add(it.getString("entity_composite_key"))
                                batchContents++
                            }
                        }
                    } else {
                        //load all in filter
                        preparedStatement2.executeQuery().use {
                            while (it.next()) {
                                if (batchContents == batchSize) {
                                    batchIndex++
                                    batchContents = 0
                                }
                                if (batchContents == 0)
                                    filterBatches.add(ArrayList())
                                filterBatches[batchIndex].add(it.getString("composite_key"))
                                batchContents++
                            }
                        }
                    }
                }
            }
        }
    }

    private fun quote(filterGuids: Iterable<out String>): String {
        val list = filterGuids.map { "'$it'" }
        return list.joinToString(",")
    }

    /**
     * @param size
     * @return
     */
    private fun prepareParamaterSequence(size: Int): String {
        val questionArray = StringJoiner(",")
        for (index in 0 until size)
            questionArray.add("?")
        return questionArray.toString()
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
        val filterBatches = ArrayList(ArrayList<MutableList<String>>())
        if (referenceType != null) {
            val annotation = referenceType.getAnnotation(JdsEntityAnnotation::class.java)
            val entityId = annotation.entityId
            prepareActionBatches(jdsDb, MAX_BATCH_SIZE, entityId, filterBatches, searchUUIDs)
        }
        val initialisePrimitives = true
        val initialiseDatesAndTimes = true
        val initialiseObjects = true
        filterBatches.forEach {
            populateInner(jdsDb, collections, initialisePrimitives, initialiseDatesAndTimes, initialiseObjects, it)
        }
        return collections
    }

    companion object {

        /**
         * Java supports up to 1000 prepared supportsStatements depending on the driver
         */
        const val MAX_BATCH_SIZE = 1000
    }
}
