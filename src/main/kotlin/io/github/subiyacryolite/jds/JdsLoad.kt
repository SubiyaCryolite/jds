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
import io.github.subiyacryolite.jds.enums.JdsFilterBy
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
 * @param jdsDb
 * @param referenceType
 * @param filterBy
 * @param T
 */
class JdsLoad<T : JdsEntity>(private val jdsDb: JdsDb, private val referenceType: Class<T>, private val filterBy: JdsFilterBy) : Callable<MutableList<T>> {
    private val collections = ArrayList<T>()
    private val alternateConnections: ConcurrentMap<Int, Connection> = ConcurrentHashMap()
    private var filterIds: Iterable<out String> = emptyList()

    /**
     *
     * @param jdsDb
     * @param referenceType
     * @param T
     */
    constructor(jdsDb: JdsDb, referenceType: Class<T>) : this(jdsDb, referenceType, JdsFilterBy.COMPOSITE_KEY)

    /**
     *
     * @param jdsDb
     * @param referenceType
     * @param filterBy
     * @param filterIds
     * @param T
     */
    constructor(jdsDb: JdsDb, referenceType: Class<T>, filterBy: JdsFilterBy, filterIds: Collection<String>) : this(jdsDb, referenceType, filterBy) {
        this.filterIds = filterIds
    }

    private val filterColumn: String
        get() {
            return when (filterBy) {
                JdsFilterBy.COMPOSITE_KEY -> "composite_key"
                JdsFilterBy.UUID -> "uuid"
                JdsFilterBy.UUID_LOCATION -> "uuid_location"
                JdsFilterBy.PARENT_UUID -> "parent_uuid"
            }
        }

    @Throws(Exception::class)
    override fun call(): MutableList<T> {
        val filterBatches = ArrayList(ArrayList<MutableList<String>>())
        val annotation = referenceType.getAnnotation(JdsEntityAnnotation::class.java)
        val entityId = annotation.entityId
        prepareActionBatches(jdsDb, MAX_BATCH_SIZE, entityId, filterBatches, filterIds)
        val initialisePrimitives = true
        val initialiseDatesAndTimes = true
        val initialiseObjects = true
        filterBatches.forEach {
            populateInner(jdsDb, collections, it)
        }
        return collections
    }


    /**
     * @param jdsDb
     * @param entities
     * @param uuids
     * @param T
     */
    private fun <T : JdsEntity> populateInner(jdsDb: JdsDb,
                                              entities: MutableCollection<T>,
                                              uuids: Collection<String>) {

        val questionsString = prepareParamaterSequence(uuids.size)
        val sqlEntities = "SELECT composite_key, uuid, uuid_location, uuid_location_version, entity_id, entity_version, last_edit, live, parent_uuid FROM jds_entity_overview WHERE $filterColumn IN ($questionsString)"
        try {

            jdsDb.getConnection().use { connection ->
                connection.prepareStatement(sqlEntities).use { entityLookUp ->
                    //work in batches to not break prepared statement
                    var batchSequence = 1
                    for (uuid in uuids) {
                        setParameterForStatement(batchSequence, uuid, entityLookUp)
                        batchSequence++
                    }
                    //catch embedded/pre-created objects objects as well
                    if (jdsDb.options.initialisePrimitives || jdsDb.options.initialiseDatesAndTimes || jdsDb.options.initialiseObjects) {
                        val compositeKeys = HashSet<String>()
                        createEntities(entities, entityLookUp, compositeKeys)

                        entities.filterIsInstance(JdsLoadListener::class.java).forEach { it.onPreLoad(OnPreLoadEventArguments(jdsDb, connection, alternateConnections)) }

                        val parameters = prepareParamaterSequence(compositeKeys.size)
                        val populateBooleans = "SELECT composite_key, value, field_id FROM jds_store_boolean WHERE composite_key IN ($parameters)"
                        val populateStrings = "SELECT composite_key, value, field_id FROM jds_store_text WHERE composite_key IN ($parameters)"
                        val populateLongs = "SELECT composite_key, value, field_id FROM jds_store_long WHERE composite_key IN ($parameters)"
                        val populateIntegers = "SELECT composite_key, value, field_id FROM jds_store_integer WHERE composite_key IN ($parameters)"
                        val populateFloats = "SELECT composite_key, value, field_id FROM jds_store_float WHERE composite_key IN ($parameters)"
                        val populateDoubles = "SELECT composite_key, value, field_id FROM jds_store_double WHERE composite_key IN ($parameters)"
                        val populateDateTimes = "SELECT composite_key, value, field_id FROM jds_store_date_time WHERE composite_key IN ($parameters)"
                        val populateTimes = "SELECT composite_key, value, field_id FROM jds_store_time WHERE composite_key IN ($parameters)"
                        val populateZonedDateTimes = "SELECT composite_key, value, field_id FROM jds_store_zoned_date_time WHERE composite_key IN ($parameters)"
                        val populateBlobs = "SELECT composite_key, value, field_id FROM jds_store_blob WHERE composite_key IN ($parameters)"
                        val populateStringCollections = "SELECT composite_key, value, field_id, sequence FROM jds_store_text_array WHERE composite_key IN ($parameters)"
                        val populateIntegerAndEnumCollections = "SELECT composite_key, value, field_id, sequence FROM jds_store_integer_array WHERE composite_key IN ($parameters)"
                        val populateLongCollections = "SELECT composite_key, value, field_id, sequence FROM jds_store_long_array WHERE composite_key IN ($parameters)"
                        val populateFloatCollections = "SELECT composite_key, value, field_id, sequence FROM jds_store_float_array WHERE composite_key IN ($parameters)"
                        val populateDoubleCollections = "SELECT composite_key, value, field_id, sequence FROM jds_store_double_array WHERE composite_key IN ($parameters)"
                        val populateDateTimeCollections = "SELECT composite_key, value, field_id, sequence FROM jds_store_date_time_array WHERE composite_key IN ($parameters)"
                        val populateEmbeddedAndArrayObjects = "SELECT eb.child_composite_key, eb.parent_composite_key, eb.child_entity_id, eb.field_id, eo.uuid_location, eo.uuid_location_version FROM jds_entity_binding eb JOIN jds_entity_overview eo ON eo.composite_key = eb.child_composite_key AND eb.parent_composite_key IN ($parameters)"

                        if (jdsDb.options.isWritingToPrimaryDataTables) {
                            //strings, derived from strings and string arrays
                            connection.prepareStatement(populateStrings).use {
                                compositeKeys.forEachIndexed { index, value -> it.setString(index + 1, value) }
                                populateTextMonthDayYearMonthAndPeriod(entities, it)
                            }
                            connection.prepareStatement(populateStringCollections).use {
                                compositeKeys.forEachIndexed { index, value -> it.setString(index + 1, value) }
                                populateStringArrays(entities, it)
                            }
                            //primitives
                            connection.prepareStatement(populateLongs).use {
                                compositeKeys.forEachIndexed { index, value -> it.setString(index + 1, value) }
                                populateLongAndDuration(entities, it)
                            }
                            connection.prepareStatement(populateBooleans).use {
                                compositeKeys.forEachIndexed { index, value -> it.setString(index + 1, value) }
                                populateBoolean(entities, it)
                            }
                            connection.prepareStatement(populateIntegers).use {
                                compositeKeys.forEachIndexed { index, value -> it.setString(index + 1, value) }
                                populateIntegerAndEnum(entities, it)
                            }
                            connection.prepareStatement(populateFloats).use {
                                compositeKeys.forEachIndexed { index, value -> it.setString(index + 1, value) }
                                populateFloat(entities, it)
                            }
                            connection.prepareStatement(populateDoubles).use {
                                compositeKeys.forEachIndexed { index, value -> it.setString(index + 1, value) }
                                populateDouble(entities, it)
                            }
                            //primitive arrays
                            //despite being primitives, array types are assumed to not contain null elements
                            connection.prepareStatement(populateIntegerAndEnumCollections).use {
                                compositeKeys.forEachIndexed { index, value -> it.setString(index + 1, value) }
                                populateIntegerArraysAndEnums(entities, it)
                            }
                            connection.prepareStatement(populateFloatCollections).use {
                                compositeKeys.forEachIndexed { index, value -> it.setString(index + 1, value) }
                                populateFloatArrays(entities, it)
                            }
                            connection.prepareStatement(populateLongCollections).use {
                                compositeKeys.forEachIndexed { index, value -> it.setString(index + 1, value) }
                                populateLongArrays(entities, it)
                            }
                            connection.prepareStatement(populateDoubleCollections).use {
                                compositeKeys.forEachIndexed { index, value -> it.setString(index + 1, value) }
                                populateDoubleArrays(entities, it)
                            }
                        }
                        if (jdsDb.options.isWritingToPrimaryDataTables && jdsDb.options.initialiseDatesAndTimes) {
                            connection.prepareStatement(populateZonedDateTimes).use {
                                compositeKeys.forEachIndexed { index, value -> it.setString(index + 1, value) }
                                populateZonedDateTime(entities, it)
                            }
                            connection.prepareStatement(populateDateTimes).use {
                                compositeKeys.forEachIndexed { index, value -> it.setString(index + 1, value) }
                                populateDateTimeAndDate(entities, it)
                            }
                            connection.prepareStatement(populateTimes).use {
                                compositeKeys.forEachIndexed { index, value -> it.setString(index + 1, value) }
                                populateTimes(entities, it)
                            }
                            connection.prepareStatement(populateDateTimeCollections).use {
                                compositeKeys.forEachIndexed { index, value -> it.setString(index + 1, value) }
                                populateDateTimeArrays(entities, it)
                            }
                        }
                        if (jdsDb.options.initialiseObjects) {
                            if (jdsDb.options.isWritingToPrimaryDataTables)
                                connection.prepareStatement(populateBlobs).use {
                                    compositeKeys.forEachIndexed { index, value -> it.setString(index + 1, value) }
                                    populateBlobs(entities, it)
                                }
                            connection.prepareStatement(populateEmbeddedAndArrayObjects).use {
                                compositeKeys.forEachIndexed { index, value -> it.setString(index + 1, value) }
                                populateObjectEntriesAndObjectArrays(jdsDb, entities, it, jdsDb.options.initialisePrimitives, jdsDb.options.initialiseDatesAndTimes, jdsDb.options.initialiseObjects)
                            }
                        }
                        entities.filterIsInstance(JdsLoadListener::class.java).forEach { it.onPostLoad(OnPostLoadEventArguments(jdsDb, connection, alternateConnections)) }
                    }
                    //close alternate connections
                    alternateConnections.forEach { it.value.close() }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    private fun <T : JdsEntity> createEntities(entities: MutableCollection<T>, entityLookUp: PreparedStatement, compositeKeys: HashSet<String>) {
        entityLookUp.executeQuery().use {
            while (it.next()) {
                val compositeKey = it.getString("composite_key")!!
                val entityId = it.getLong("entity_id")
                val uuid = it.getString("uuid")
                val uuidLocation = it.getString("uuid_location")
                val uuidLocationVersion = it.getInt("uuid_location_version")
                val lastEdit = it.getTimestamp("last_edit")
                val live = it.getBoolean("live")
                val version = it.getLong("entity_version")
                val parentUuid = it.getString("parent_uuid")
                if (jdsDb.classes.containsKey(entityId)) {
                    val refType = jdsDb.classes[entityId]!!
                    val entity = refType.newInstance()
                    entity.overview.uuid = uuid
                    entity.overview.uuidLocation = uuidLocation
                    entity.overview.uuidLocationVersion = uuidLocationVersion
                    entity.overview.lastEdit = lastEdit.toLocalDateTime()
                    entity.overview.version = version
                    entity.overview.live = live
                    entity.overview.parentUuid = parentUuid
                    entities.add(entity as T)
                    compositeKeys.add(compositeKey)
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
                val parentCompositeKey = it.getString("parent_composite_key")
                val uuid = it.getString("child_composite_key")
                val fieldId = it.getLong("field_id")
                val entityId = it.getLong("child_entity_id")
                val uuidLocation = it.getString("uuid_location")
                val uuidLocationVersion = it.getInt("uuid_location_version")
                optimalEntityLookup(entities, parentCompositeKey).forEach { entity ->
                    entity.populateObjects(jdsDb, fieldId, entityId, uuid, uuidLocation, uuidLocationVersion, entity.overview.uuid, innerObjects, uuids)
                }
            }
        }
        val batches = createProcessingBatches(uuids)
        batches.forEach { populateInner(jdsDb, innerObjects, it) }
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
                val compositeKey = it.getString("composite_key")
                val value = it.getFloat("value")
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, compositeKey).forEach { entity ->
                    entity.populateProperties(JdsFieldType.FLOAT_COLLECTION, fieldId, value)
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
    private fun <T : JdsEntity> populateDoubleArrays(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val compositeKey = it.getString("composite_key")
                val value = it.getDouble("value")
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, compositeKey).forEach { entity ->
                    entity.populateProperties(JdsFieldType.DOUBLE_COLLECTION, fieldId, value)
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
    private fun <T : JdsEntity> populateLongArrays(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val compositeKey = it.getString("composite_key")
                val value = it.getLong("value")
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, compositeKey).forEach { entity ->
                    entity.populateProperties(JdsFieldType.LONG_COLLECTION, fieldId, value)
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
                val compositeKey = it.getString("composite_key")
                val value = it.getTimestamp("value")
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, compositeKey).forEach { entity ->
                    entity.populateProperties(JdsFieldType.DATE_TIME_COLLECTION, fieldId, value)
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
    private fun <T : JdsEntity> populateStringArrays(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val compositeKey = it.getString("composite_key")
                val value = it.getString("value")
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, compositeKey).forEach { entity ->
                    entity.populateProperties(JdsFieldType.STRING_COLLECTION, fieldId, value)
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
    private fun <T : JdsEntity> populateIntegerArraysAndEnums(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val compositeKey = it.getString("composite_key")
                val value = it.getInt("value")
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, compositeKey).forEach { entity ->
                    entity.populateProperties(JdsFieldType.INT_COLLECTION, fieldId, value)
                    entity.populateProperties(JdsFieldType.ENUM_COLLECTION, fieldId, value)
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
    private fun <T : JdsEntity> populateDateTimeAndDate(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val compositeKey = it.getString("composite_key")
                val value = it.getTimestamp("value")
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, compositeKey).forEach { entity ->
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
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val compositeKey = it.getString("composite_key")
                val value = it.getObject("value") //primitives can be null
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, compositeKey).forEach { entity ->
                    entity.populateProperties(JdsFieldType.DOUBLE, fieldId, value)
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
    private fun <T : JdsEntity> populateBlobs(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val compositeKey = it.getString("composite_key")
                val value = it.getBytes("value")
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, compositeKey).forEach { entity ->
                    entity.populateProperties(JdsFieldType.BLOB, fieldId, value)
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
    private fun <T : JdsEntity> populateIntegerAndEnum(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val compositeKey = it.getString("composite_key")
                val value = it.getObject("value") //primitives can be null
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, compositeKey).forEach { entity ->
                    entity.populateProperties(JdsFieldType.INT, fieldId, value)
                    entity.populateProperties(JdsFieldType.ENUM, fieldId, value)
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
                val compositeKey = it.getString("composite_key")
                val value = it.getObject("value") //primitives can be null
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, compositeKey).forEach { entity ->
                    entity.populateProperties(JdsFieldType.BOOLEAN, fieldId, value)
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
    private fun <T : JdsEntity> populateTimes(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val compositeKey = it.getString("composite_key")
                val value = it.getLocalTime("value", jdsDb)
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, compositeKey).forEach { entity ->
                    entity.populateProperties(JdsFieldType.TIME, fieldId, value)
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
    private fun <T : JdsEntity> populateFloat(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val compositeKey = it.getString("composite_key")
                val value = it.getObject("value") //primitives can be null
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, compositeKey).forEach { entity ->
                    entity.populateProperties(JdsFieldType.FLOAT, fieldId, value)
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
    private fun <T : JdsEntity> populateLongAndDuration(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val compositeKey = it.getString("composite_key")
                val fieldId = it.getLong("field_id")
                val value = it.getObject("value") //primitives can be null
                optimalEntityLookup(entities, compositeKey).forEach { entity ->
                    entity.populateProperties(JdsFieldType.LONG, fieldId, value)
                    entity.populateProperties(JdsFieldType.DURATION, fieldId, value)
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
                val compositeKey = it.getString("composite_key")
                val value = it.getZonedDateTime("value", jdsDb)
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, compositeKey).forEach { entity ->
                    entity.populateProperties(JdsFieldType.ZONED_DATE_TIME, fieldId, value)
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
    private fun <T : JdsEntity> populateTextMonthDayYearMonthAndPeriod(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val compositeKey = it.getString("composite_key")
                val value = it.getString("value")
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, compositeKey).forEach { entity ->
                    entity.populateProperties(JdsFieldType.STRING, fieldId, value)
                    entity.populateProperties(JdsFieldType.MONTH_DAY, fieldId, value)
                    entity.populateProperties(JdsFieldType.YEAR_MONTH, fieldId, value)
                    entity.populateProperties(JdsFieldType.PERIOD, fieldId, value)
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

        val searchByType = filterUUIDs.none()

        jdsDataBase.getConnection().use {
            it.prepareStatement("SELECT child_entity_id FROM jds_ref_entity_inheritance WHERE parent_entity_id = ?").use {
                it.setLong(1, entityAndChildren[0])
                it.executeQuery().use {
                    while (it.next())
                        entityAndChildren.add(it.getLong("child_entity_id"))
                }
            }

            val entityHeirarchy = StringJoiner(",")
            if (searchByType)
                entityAndChildren.forEach { entityHeirarchy.add(it.toString()) }

            if (searchByType) {
                //if no ids supplied we are looking for all instances of the entity.
                //load ALL entityVersions in the in heirarchy
                val loadAllByTypeSql = "SELECT DISTINCT eo.$filterColumn FROM jds_entity_instance ei JOIN jds_entity_overview eo ON ei.entity_composite_key = eo.composite_key WHERE ei.entity_id IN ($entityHeirarchy)"
                it.prepareStatement(loadAllByTypeSql).use {
                    it.executeQuery().use {
                        while (it.next()) {
                            if (batchContents == batchSize) {
                                batchIndex++
                                batchContents = 0
                            }
                            if (batchContents == 0)
                                filterBatches.add(ArrayList())
                            filterBatches[batchIndex].add(it.getString(filterColumn))
                            batchContents++
                        }
                    }
                }
            } else {
                //load all in filter
                val loadByUuidSql = "SELECT DISTINCT $filterColumn FROM jds_entity_overview WHERE uuid IN (${quote(filterUUIDs)})"
                it.prepareStatement(loadByUuidSql).use {
                    it.executeQuery().use {
                        while (it.next()) {
                            if (batchContents == batchSize) {
                                batchIndex++
                                batchContents = 0
                            }
                            if (batchContents == 0)
                                filterBatches.add(ArrayList())
                            filterBatches[batchIndex].add(it.getString(filterColumn))
                            batchContents++
                        }
                    }
                }
            }
        }
    }

    private fun quote(filterUuids: Iterable<out String>): String {
        val list = filterUuids.map { it }
        return list.joinToString(",", "'", "'")
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

    companion object {

        /**
         * Java supports up to 1000 prepared supportsStatements depending on the driver
         */
        const val MAX_BATCH_SIZE = 1000
    }
}
