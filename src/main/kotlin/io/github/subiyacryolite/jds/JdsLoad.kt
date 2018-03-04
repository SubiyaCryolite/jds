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
import io.github.subiyacryolite.jds.events.OnPostLoadEventArgument
import io.github.subiyacryolite.jds.events.OnPreLoadEventArgument
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

        fun prepareParamaterSequence(size: Int): String {
            val questionArray = StringJoiner(",", "(", ")")
            for (index in 0 until size)
                questionArray.add("?")
            return questionArray.toString()
        }
    }

    /**
     *
     * @param jdsDb
     * @param referenceType
     * @param T
     */
    constructor(jdsDb: JdsDb, referenceType: Class<T>) : this(jdsDb, referenceType, JdsFilterBy.UUID)

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
                JdsFilterBy.UUID_LOCATION -> "uuid_location"
            }
        }

    @Throws(Exception::class)
    override fun call(): MutableList<T> {
        val entitiesToLoad = ArrayList<JdsEntityComposite>()
        val annotation = referenceType.getAnnotation(JdsEntityAnnotation::class.java)
        val entityId = annotation.id
        prepareActionBatches(jdsDb, entityId, entitiesToLoad, filterIds)
        val collections = ArrayList<T>()
        populateInner(jdsDb, collections, entitiesToLoad)
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
                                              uuids: ArrayList<JdsEntityComposite>) {
        if (uuids.isEmpty()) return
        val questionsString = prepareParamaterSequence(uuids.size)
        val getOverviewRecords = StringBuilder()
        getOverviewRecords.append("SELECT\n")
        getOverviewRecords.append("  uuid,\n")
        getOverviewRecords.append("  uuid_version,\n")
        getOverviewRecords.append("  uuid_location,\n")
        getOverviewRecords.append("  entity_id,\n")
        getOverviewRecords.append("  entity_version,\n")
        getOverviewRecords.append("  last_edit,\n")
        getOverviewRecords.append("  live\n")
        getOverviewRecords.append("  FROM jds_entity_overview repo\n")
        getOverviewRecords.append(" WHERE $filterColumn IN $questionsString")
        try {
            jdsDb.getConnection().use { connection ->
                connection.prepareStatement(getOverviewRecords.toString()).use { preparedStatement ->
                    //create sql to populate fields
                    val populateEmbeddedAndArrayObjects = "SELECT child.* FROM jds_entity_binding child JOIN jds_entity_overview parent ON parent.$filterColumn IN $questionsString " +
                            "AND parent.uuid = child.parent_uuid " +
                            "AND parent.uuid_location = child.parent_uuid_location " +
                            "AND parent.uuid_version = child.parent_uuid_version"
                    val populateBooleanStmt = connection.prepareStatement("SELECT * FROM jds_store_boolean WHERE $filterColumn IN $questionsString")
                    val populateStringStmt = connection.prepareStatement("SELECT * FROM jds_store_text WHERE $filterColumn IN $questionsString")
                    val populateStringCollectionStmt = connection.prepareStatement("SELECT * FROM jds_store_text_collection WHERE $filterColumn IN $questionsString")
                    val populateLongStmt = connection.prepareStatement("SELECT * FROM jds_store_long WHERE $filterColumn IN $questionsString")
                    val populateLongCollectionStmt = connection.prepareStatement("SELECT * FROM jds_store_long_collection WHERE $filterColumn IN $questionsString")
                    val populateIntegerStmt = connection.prepareStatement("SELECT * FROM jds_store_integer WHERE $filterColumn IN $questionsString")
                    val populateIntegerCollectionStmt = connection.prepareStatement("SELECT * FROM jds_store_integer_collection WHERE $filterColumn IN $questionsString")
                    val populateFloatStmt = connection.prepareStatement("SELECT * FROM jds_store_float WHERE $filterColumn IN $questionsString")
                    val populateFloatCollectionStmt = connection.prepareStatement("SELECT * FROM jds_store_float_collection WHERE $filterColumn IN $questionsString")
                    val populateDoubleStmt = connection.prepareStatement("SELECT * FROM jds_store_double WHERE $filterColumn IN $questionsString")
                    val populateDoubleCollectionStmt = connection.prepareStatement("SELECT * FROM jds_store_double_collection WHERE $filterColumn IN $questionsString")
                    val populateDateTimeStmt = connection.prepareStatement("SELECT * FROM jds_store_date_time WHERE $filterColumn IN $questionsString")
                    val populateTimeStmt = connection.prepareStatement("SELECT * FROM jds_store_time WHERE $filterColumn IN $questionsString")
                    val populateZonedDateTimesStmt = connection.prepareStatement("SELECT * FROM jds_store_zoned_date_time WHERE $filterColumn IN $questionsString")
                    val populateBlobStmt = connection.prepareStatement("SELECT * FROM jds_store_blob WHERE $filterColumn IN $questionsString")
                    val populateEmbeddedAndArrayObjectsStmt = connection.prepareStatement(populateEmbeddedAndArrayObjects)

                    //work in batches to not break prepared statement
                    uuids.forEachIndexed { index, uuid ->
                        setParameterForStatement(index + 1, uuid, preparedStatement)
                        setParameterForStatement(index + 1, uuid, populateStringStmt)
                        setParameterForStatement(index + 1, uuid, populateBooleanStmt)
                        setParameterForStatement(index + 1, uuid, populateLongStmt)
                        setParameterForStatement(index + 1, uuid, populateIntegerStmt)
                        setParameterForStatement(index + 1, uuid, populateFloatStmt)
                        setParameterForStatement(index + 1, uuid, populateDoubleStmt)
                        setParameterForStatement(index + 1, uuid, populateDateTimeStmt)
                        setParameterForStatement(index + 1, uuid, populateTimeStmt)
                        setParameterForStatement(index + 1, uuid, populateZonedDateTimesStmt)
                        setParameterForStatement(index + 1, uuid, populateBlobStmt)
                        setParameterForStatement(index + 1, uuid, populateEmbeddedAndArrayObjectsStmt)
                    }
                    //catch embedded/pre-created objects objects as well
                    if (jdsDb.options.initialisePrimitives || jdsDb.options.initialiseDatesAndTimes || jdsDb.options.initialiseObjects) {
                        createEntities(entities, preparedStatement)
                        entities.filterIsInstance(JdsLoadListener::class.java).forEach { it.onPreLoad(OnPreLoadEventArgument(jdsDb, connection, alternateConnections)) }

                        //all entities have been initialised, now we populate them
                        if (jdsDb.options.isWritingToPrimaryDataTables) {
                            //strings, derived from strings and string arrays
                            populateStringStmt.use {
                                populateTextMonthDayYearMonthAndPeriod(entities, it)
                                populateStringArrays(entities, it)
                            }
                            //primitives
                            populateLongStmt.use { populateLongAndDuration(entities, it) }
                            populateBooleanStmt.use { populateBoolean(entities, it) }
                            populateIntegerStmt.use { populateIntegerAndEnum(entities, it) }
                            populateFloatStmt.use { populateFloat(entities, it) }
                            populateDoubleStmt.use { populateDouble(entities, it) }
                        }
                        if (jdsDb.options.isWritingToPrimaryDataTables && jdsDb.options.initialiseDatesAndTimes) {
                            populateZonedDateTimesStmt.use { populateZonedDateTime(entities, it) }
                            populateDateTimeStmt.use { populateDateTimeAndDate(entities, it) }
                            populateTimeStmt.use { populateTimes(entities, it) }
                        }
                        if (jdsDb.options.initialiseObjects) {
                            if (jdsDb.options.isWritingToPrimaryDataTables)
                                populateBlobStmt.use { populateBlobs(entities, it) }
                            populateEmbeddedAndArrayObjectsStmt.use { populateObjectEntriesAndObjectArrays(jdsDb, entities, it) }
                        }
                        entities.filterIsInstance(JdsLoadListener::class.java).forEach { it.onPostLoad(OnPostLoadEventArgument(jdsDb, connection, alternateConnections)) }
                    }
                    //close alternate connections
                    alternateConnections.forEach { it.value.close() }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    private fun <T : JdsEntity> createEntities(entities: MutableCollection<T>, entityLookUp: PreparedStatement) {
        entityLookUp.executeQuery().use {
            while (it.next()) {
                val entityId = it.getLong("entity_id")
                val uuid = it.getString("uuid")
                val uuidLocation = it.getString("uuid_location")
                val uuidLocationVersion = it.getInt("uuid_version")
                val lastEdit = it.getTimestamp("last_edit")
                val live = it.getBoolean("live")
                val version = it.getLong("entity_version")
                if (jdsDb.classes.containsKey(entityId)) {
                    val refType = jdsDb.classes[entityId]!!
                    val entity = refType.newInstance()
                    entity.overview.uuid = uuid
                    entity.overview.uuidLocation = uuidLocation ?: "" //oracle treats empty strings as null
                    entity.overview.uuidLocationVersion = uuidLocationVersion
                    entity.overview.lastEdit = lastEdit.toLocalDateTime()
                    entity.overview.entityVersion = version
                    entity.overview.live = live
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
    private fun <T : JdsEntity> populateObjectEntriesAndObjectArrays(jdsDb: JdsDb,
                                                                     entities: Collection<T>,
                                                                     preparedStatement: PreparedStatement) {
        val uuids = ArrayList<JdsEntityComposite>()//ids should be unique
        val innerObjects = ConcurrentLinkedQueue<JdsEntity>()//can be multiple copies of the same object however
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val parentUuid = it.getString("parent_uuid")
                val parentUuidLocation = it.getString("parent_uuid_location")
                val parentUuidLocationVersion = it.getInt("parent_uuid_version")
                val childUuid = it.getString("child_uuid")
                val childUuidLocation = it.getString("child_uuid_location")
                val childLocationVersion = it.getInt("child_uuid_version")
                val fieldId = it.getLong("field_id")
                val entityId = it.getLong("entity_id")
                optimalEntityLookup(entities, parentUuid, parentUuidLocation, parentUuidLocationVersion).forEach {
                    it.populateObjects(jdsDb, fieldId, entityId, childUuid, childUuidLocation, childLocationVersion, innerObjects, uuids)
                }
            }
        }
        populateInner(jdsDb, innerObjects, uuids)
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
                val uuid = it.getString("uuid")
                val uuidLocation = it.getString("uuid_location")
                val uuidLocationVersion = it.getInt("uuid_version")
                val value = it.getString("value")
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, uuidLocation, uuidLocationVersion).forEach {
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
    private fun <T : JdsEntity> optimalEntityLookup(entities: Collection<T>, uuid: String, uuidLocation: String, uuidLocationVersion: Int): Stream<T> {
        return entities.parallelStream().filter {
            it.overview.uuid == uuid && it.overview.uuidLocation == uuidLocation && it.overview.uuidLocationVersion == uuidLocationVersion
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
                val uuid = it.getString("uuid")
                val uuidLocation = it.getString("uuid_location")
                val uuidLocationVersion = it.getInt("uuid_version")
                val value = it.getTimestamp("value")
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, uuidLocation, uuidLocationVersion).forEach {
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
                val uuid = it.getString("uuid")
                val uuidLocation = it.getString("uuid_location")
                val uuidLocationVersion = it.getInt("uuid_version")
                val value = it.getObject("value") //primitives can be null
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, uuidLocation, uuidLocationVersion).forEach {
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
                val uuid = it.getString("uuid")
                val uuidLocation = it.getString("uuid_location")
                val uuidLocationVersion = it.getInt("uuid_version")
                val value = it.getBytes("value")
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, uuidLocation, uuidLocationVersion).forEach {
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
                val uuid = it.getString("uuid")
                val uuidLocation = it.getString("uuid_location")
                val uuidLocationVersion = it.getInt("uuid_version")
                val value = it.getObject("value") //primitives can be null
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, uuidLocation, uuidLocationVersion).forEach {
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
                val uuid = it.getString("uuid")
                val uuidLocation = it.getString("uuid_location")
                val uuidLocationVersion = it.getInt("uuid_version")
                val value = it.getObject("value") //primitives can be null
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, uuidLocation, uuidLocationVersion).forEach {
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
                val uuid = it.getString("uuid")
                val uuidLocation = it.getString("uuid_location")
                val uuidLocationVersion = it.getInt("uuid_version")
                val value = it.getLocalTime("value", jdsDb)
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, uuidLocation, uuidLocationVersion).forEach {
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
                val uuid = it.getString("uuid")
                val uuidLocation = it.getString("uuid_location")
                val uuidLocationVersion = it.getInt("uuid_version")
                val value = it.getObject("value") //primitives can be null
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, uuidLocation, uuidLocationVersion).forEach {
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
                val uuid = it.getString("uuid")
                val uuidLocation = it.getString("uuid_location")
                val uuidLocationVersion = it.getInt("uuid_version")
                val fieldId = it.getLong("field_id")
                val value = it.getObject("value") //primitives can be null
                optimalEntityLookup(entities, uuid, uuidLocation, uuidLocationVersion).forEach {
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
                val uuid = it.getString("uuid")
                val uuidLocation = it.getString("uuid_location")
                val uuidLocationVersion = it.getInt("uuid_version")
                val value = it.getZonedDateTime("value", jdsDb)
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, uuidLocation, uuidLocationVersion).forEach {
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
                val uuid = it.getString("uuid")
                val uuidLocation = it.getString("uuid_location")
                val uuidLocationVersion = it.getInt("uuid_version")
                val value = it.getString("value")
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, uuidLocation, uuidLocationVersion).forEach {
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
    private fun prepareActionBatches(jdsDataBase: JdsDb, entityId: Long, entitiesToLoad: MutableList<JdsEntityComposite>, filterUUIDs: Iterable<out String>) {
        val searchByType = filterUUIDs.none()
        jdsDataBase.getConnection().use {
            if (searchByType) {
                //if no ids supplied we are looking for all instances of the entity.
                //load ALL entityVersions in the in heirarchy
                val loadAllByTypeSql = "SELECT eo.uuid, eo.uuid_location, eo.uuid_version FROM jds_entity_overview eo WHERE eo.entity_id IN (SELECT child_entity_id FROM jds_ref_entity_inheritance WHERE parent_entity_id = ?)"
                it.prepareStatement(loadAllByTypeSql).use {
                    it.setLong(1, entityId)
                    it.executeQuery().use {
                        while (it.next())
                            entitiesToLoad.add(JdsEntityComposite(it.getString("uuid"), it.getString("uuid_location"), it.getInt("uuid_version")))
                    }
                }
            } else {
                //load all in filter
                val loadByUuidSql = "SELECT uuid, uuid_location, uuid_version FROM jds_entity_overview WHERE $filterColumn IN (${parametize(filterUUIDs)})"
                it.prepareStatement(loadByUuidSql).use { prepStmt ->
                    filterUUIDs.forEachIndexed { index, filterValue ->
                        prepStmt.setString(index + 1, filterValue)
                    }
                    prepStmt.executeQuery().use {
                        while (it.next())
                            entitiesToLoad.add(JdsEntityComposite(it.getString("uuid"), it.getString("uuid_location"), it.getInt("uuid_version")))
                    }
                }
            }
        }
    }

    /**
     * @param filterUuids
     */
    private fun parametize(filterUuids: Iterable<out String>): String {
        val stringJoiner = StringJoiner(",")
        filterUuids.forEach { stringJoiner.add("?") }
        return stringJoiner.toString()
    }


    /**
     * @param preparedStatement
     * @param index
     * @param uuid
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun setParameterForStatement(index: Int, uuid: JdsEntityComposite, preparedStatement: PreparedStatement) {
        when (filterColumn) {
            "uuid" -> preparedStatement.setString(index, uuid.uuid)
            "uuid_location" -> preparedStatement.setString(index, uuid.uuidLocation)
        }
    }
}