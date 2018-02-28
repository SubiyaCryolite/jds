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
import java.util.stream.Stream
import kotlin.collections.ArrayList

/**
 * This class is responsible for loading an [entity's][JdsEntity] [fields][JdsField]
 * @param jdsDb
 * @param referenceType
 * @param filterBy
 * @param T
 */
class JdsLoad<T : JdsEntity>(private val jdsDb: JdsDb, private val referenceType: Class<T>, private val filterBy: JdsFilterBy) : Callable<MutableList<T>> {

    private val alternateConnections: ConcurrentMap<Int, Connection> = ConcurrentHashMap()
    private var filterIds: Iterable<out String> = emptyList()

    companion object {
        /**
         * Java supports up to 1000 prepared supportsStatements depending on the driver
         */
        const val MAX_BATCH_SIZE = 1000
    }

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
                JdsFilterBy.UUID -> "uuid"
                JdsFilterBy.COMPOSITE_KEY -> "composite_key"
                JdsFilterBy.PARENT_UUID -> "parent_uuid"
            }
        }

    @Throws(Exception::class)
    override fun call(): MutableList<T> {
        val entitiesToLoad = ArrayList<String>()
        val annotation = referenceType.getAnnotation(JdsEntityAnnotation::class.java)
        val entityId = annotation.id
        prepareActionBatches(jdsDb, entityId, entitiesToLoad, filterIds)
        val collections = ArrayList<T>()
        entitiesToLoad.chunked(MAX_BATCH_SIZE).forEach { populateInner(jdsDb, collections, it) }
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
        val sb = StringBuilder()
        sb.append("SELECT\n")
        sb.append("  repo.uuid,\n")
        sb.append("  repo.uuid_location_version,\n")
        sb.append("  composite_key,\n")
        sb.append("  uuid_location,\n")
        sb.append("  entity_id,\n")
        sb.append("  entity_version,\n")
        sb.append("  last_edit,\n")
        sb.append("  live,\n")
        sb.append("  parent_uuid\n")
        sb.append("FROM jds_entity_overview repo\n")
        sb.append("  JOIN (SELECT\n")
        sb.append("          uuid,\n")
        sb.append("          max(uuid_location_version) AS uuid_location_version\n")
        sb.append("        FROM jds_entity_overview\n")
        sb.append("        WHERE $filterColumn IN ($questionsString)\n")
        sb.append("        GROUP BY uuid) latest\n")
        sb.append("    ON repo.uuid = latest.uuid AND repo.uuid_location_version = latest.uuid_location_version")
        try {
            jdsDb.getConnection().use { connection ->
                connection.prepareStatement(sb.toString()).use {
                    //work in batches to not break prepared statement
                    var batchSequence = 1
                    for (uuid in uuids) {
                        setParameterForStatement(batchSequence, uuid, it)
                        batchSequence++
                    }
                    //catch embedded/pre-created objects objects as well
                    if (jdsDb.options.initialisePrimitives || jdsDb.options.initialiseDatesAndTimes || jdsDb.options.initialiseObjects) {
                        val compositeKeys = HashSet<String>()
                        createEntities(entities, it, compositeKeys)

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
                        val populateEmbeddedAndArrayObjects = "SELECT child.composite_key, child.parent_composite_key, child.entity_id, child.field_id, child.uuid_location, child.uuid_location_version FROM jds_entity_overview child JOIN jds_entity_overview parent ON parent.composite_key IN ($parameters) AND parent.composite_key = child.parent_composite_key"

                        if (jdsDb.options.isWritingToPrimaryDataTables) {
                            //strings, derived from strings and string arrays
                            connection.prepareStatement(populateStrings).use {
                                compositeKeys.forEachIndexed { index, value -> it.setString(index + 1, value) }
                                populateTextMonthDayYearMonthAndPeriod(entities, it)
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
                        }
                        if (jdsDb.options.initialiseObjects) {
                            if (jdsDb.options.isWritingToPrimaryDataTables)
                                connection.prepareStatement(populateBlobs).use {
                                    compositeKeys.forEachIndexed { index, value -> it.setString(index + 1, value) }
                                    populateBlobs(entities, it)
                                }
                            connection.prepareStatement(populateEmbeddedAndArrayObjects).use {
                                compositeKeys.forEachIndexed { index, value -> it.setString(index + 1, value) }
                                populateObjectEntriesAndObjectArrays(jdsDb, entities, it)
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
                    entity.overview.uuidLocation = uuidLocation ?: "" //oracle treats empty strings as null
                    entity.overview.uuidLocationVersion = uuidLocationVersion
                    entity.overview.lastEdit = lastEdit.toLocalDateTime()
                    entity.overview.entityVersion = version
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
                                                                     preparedStatement: PreparedStatement) {
        val uuids = HashSet<String>()//ids should be unique
        val innerObjects = ConcurrentLinkedQueue<JdsEntity>()//can be multiple copies of the same object however
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val parentCompositeKey = it.getString("parent_composite_key")
                val uuid = it.getString("composite_key")
                val fieldId = it.getLong("field_id")
                val entityId = it.getLong("entity_id")
                val uuidLocation = it.getString("uuid_location")
                val uuidLocationVersion = it.getInt("uuid_location_version")
                optimalEntityLookup(entities, parentCompositeKey).forEach {
                    it.populateObjects(jdsDb, fieldId, entityId, uuid, uuidLocation, uuidLocationVersion, it.overview.uuid, innerObjects, uuids)
                }
            }
        }
        uuids.chunked(MAX_BATCH_SIZE).forEach { populateInner(jdsDb, innerObjects, it) }
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
                optimalEntityLookup(entities, compositeKey).forEach {
                    it.populateProperties(JdsFieldType.STRING_COLLECTION, fieldId, value)
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
        return entities.parallelStream().filter { it.overview.compositeKey == uuid }
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
                optimalEntityLookup(entities, compositeKey).forEach {
                    it.populateProperties(JdsFieldType.DATE_TIME, fieldId, value)
                    it.populateProperties(JdsFieldType.DATE, fieldId, value)
                    it.populateProperties(JdsFieldType.DATE_TIME_COLLECTION, fieldId, value)
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
                optimalEntityLookup(entities, compositeKey).forEach {
                    it.populateProperties(JdsFieldType.DOUBLE, fieldId, value)
                    it.populateProperties(JdsFieldType.DOUBLE_COLLECTION, fieldId, value)
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
                optimalEntityLookup(entities, compositeKey).forEach {
                    it.populateProperties(JdsFieldType.BLOB, fieldId, value)
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
                optimalEntityLookup(entities, compositeKey).forEach {
                    it.populateProperties(JdsFieldType.INT, fieldId, value)
                    it.populateProperties(JdsFieldType.ENUM, fieldId, value)
                    it.populateProperties(JdsFieldType.INT_COLLECTION, fieldId, value)
                    it.populateProperties(JdsFieldType.ENUM_COLLECTION, fieldId, value)
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
                optimalEntityLookup(entities, compositeKey).forEach {
                    it.populateProperties(JdsFieldType.BOOLEAN, fieldId, value)
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
                optimalEntityLookup(entities, compositeKey).forEach {
                    it.populateProperties(JdsFieldType.TIME, fieldId, value)
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
                optimalEntityLookup(entities, compositeKey).forEach {
                    it.populateProperties(JdsFieldType.FLOAT, fieldId, value)
                    it.populateProperties(JdsFieldType.FLOAT_COLLECTION, fieldId, value)
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
                optimalEntityLookup(entities, compositeKey).forEach {
                    it.populateProperties(JdsFieldType.LONG, fieldId, value)
                    it.populateProperties(JdsFieldType.DURATION, fieldId, value)
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
    private fun <T : JdsEntity> populateZonedDateTime(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val compositeKey = it.getString("composite_key")
                val value = it.getZonedDateTime("value", jdsDb)
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, compositeKey).forEach {
                    it.populateProperties(JdsFieldType.ZONED_DATE_TIME, fieldId, value)
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
                optimalEntityLookup(entities, compositeKey).forEach {
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
     * @param entitiesToLoad
     * @param filterUUIDs
     */
    @Throws(SQLException::class, ClassNotFoundException::class)
    private fun prepareActionBatches(jdsDataBase: JdsDb, entityId: Long, entitiesToLoad: MutableList<String>, filterUUIDs: Iterable<out String>) {
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
                        while (it.next())
                            entitiesToLoad.add(it.getString(filterColumn))
                    }
                }
            } else {
                //load all in filter
                val loadByUuidSql = "SELECT DISTINCT $filterColumn FROM jds_entity_overview WHERE uuid IN (${quote(filterUUIDs)})"
                it.prepareStatement(loadByUuidSql).use {
                    it.executeQuery().use {
                        while (it.next())
                            entitiesToLoad.add(it.getString(filterColumn))
                    }
                }
            }
        }
    }

    /**
     * @param filterUuids
     */
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
}