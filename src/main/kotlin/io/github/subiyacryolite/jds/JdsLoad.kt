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

                    val blobStatement = connection.prepareStatement("SELECT * FROM jds_store_blob WHERE $filterColumn IN $questionsString")
                    val booleanStatement = connection.prepareStatement("SELECT * FROM jds_store_boolean WHERE $filterColumn IN $questionsString")
                    val dateStatement = connection.prepareStatement("SELECT * FROM jds_store_date WHERE $filterColumn IN $questionsString")
                    val dateTimeStatement = connection.prepareStatement("SELECT * FROM jds_store_date_time WHERE $filterColumn IN $questionsString")
                    val dateTimeCollectionStatement = connection.prepareStatement("SELECT * FROM jds_store_date_time_collection WHERE $filterColumn IN $questionsString")
                    val doubleStatement = connection.prepareStatement("SELECT * FROM jds_store_double WHERE $filterColumn IN $questionsString")
                    val doubleCollectionStatement = connection.prepareStatement("SELECT * FROM jds_store_double_collection WHERE $filterColumn IN $questionsString")
                    val durationStatement = connection.prepareStatement("SELECT * FROM jds_store_duration WHERE $filterColumn IN $questionsString")
                    val enumStatement = connection.prepareStatement("SELECT * FROM jds_store_enum WHERE $filterColumn IN $questionsString")
                    val enumCollectionStatement = connection.prepareStatement("SELECT * FROM jds_store_enum_collection WHERE $filterColumn IN $questionsString")
                    val floatStatement = connection.prepareStatement("SELECT * FROM jds_store_float WHERE $filterColumn IN $questionsString")
                    val floatCollectionStatement = connection.prepareStatement("SELECT * FROM jds_store_float_collection WHERE $filterColumn IN $questionsString")
                    val intStatement = connection.prepareStatement("SELECT * FROM jds_store_integer WHERE $filterColumn IN $questionsString")
                    val intCollectionStatement = connection.prepareStatement("SELECT * FROM jds_store_integer_collection WHERE $filterColumn IN $questionsString")
                    val longStatement = connection.prepareStatement("SELECT * FROM jds_store_long WHERE $filterColumn IN $questionsString")
                    val longCollectionStatement = connection.prepareStatement("SELECT * FROM jds_store_long_collection WHERE $filterColumn IN $questionsString")
                    val monthDayStatement = connection.prepareStatement("SELECT * FROM jds_store_month_day WHERE $filterColumn IN $questionsString")
                    val periodStatement = connection.prepareStatement("SELECT * FROM jds_store_period WHERE $filterColumn IN $questionsString")
                    val stringStatement = connection.prepareStatement("SELECT * FROM jds_store_text WHERE $filterColumn IN $questionsString")
                    val stringCollectionStatement = connection.prepareStatement("SELECT * FROM jds_store_text_collection WHERE $filterColumn IN $questionsString")
                    val timeStatement = connection.prepareStatement("SELECT * FROM jds_store_time WHERE $filterColumn IN $questionsString")
                    val yearMonthStatement = connection.prepareStatement("SELECT * FROM jds_store_year_month WHERE $filterColumn IN $questionsString")
                    val zonedDateTimeStatement = connection.prepareStatement("SELECT * FROM jds_store_zoned_date_time WHERE $filterColumn IN $questionsString")
                    val populateEmbeddedAndArrayObjectsStmt = connection.prepareStatement(populateEmbeddedAndArrayObjects)

                    //work in batches to not break prepared statement
                    uuids.forEachIndexed { index, uuid ->
                        setParameterForStatement(index + 1, uuid, preparedStatement)
                        setParameterForStatement(index + 1, uuid, populateEmbeddedAndArrayObjectsStmt)
                        //===========================================================
                        setParameterForStatement(index + 1, uuid, blobStatement)
                        setParameterForStatement(index + 1, uuid, booleanStatement)
                        setParameterForStatement(index + 1, uuid, dateStatement)
                        setParameterForStatement(index + 1, uuid, dateTimeStatement)
                        setParameterForStatement(index + 1, uuid, dateTimeCollectionStatement)
                        setParameterForStatement(index + 1, uuid, doubleStatement)
                        setParameterForStatement(index + 1, uuid, doubleCollectionStatement)
                        setParameterForStatement(index + 1, uuid, durationStatement)
                        setParameterForStatement(index + 1, uuid, enumStatement)
                        setParameterForStatement(index + 1, uuid, enumCollectionStatement)
                        setParameterForStatement(index + 1, uuid, floatStatement)
                        setParameterForStatement(index + 1, uuid, floatCollectionStatement)
                        setParameterForStatement(index + 1, uuid, intStatement)
                        setParameterForStatement(index + 1, uuid, intCollectionStatement)
                        setParameterForStatement(index + 1, uuid, longStatement)
                        setParameterForStatement(index + 1, uuid, longCollectionStatement)
                        setParameterForStatement(index + 1, uuid, monthDayStatement)
                        setParameterForStatement(index + 1, uuid, periodStatement)
                        setParameterForStatement(index + 1, uuid, stringStatement)
                        setParameterForStatement(index + 1, uuid, stringCollectionStatement)
                        setParameterForStatement(index + 1, uuid, timeStatement)
                        setParameterForStatement(index + 1, uuid, yearMonthStatement)
                        setParameterForStatement(index + 1, uuid, zonedDateTimeStatement)
                    }
                    //catch embedded/pre-created objects objects as well
                    if (jdsDb.options.initialisePrimitives || jdsDb.options.initialiseDatesAndTimes || jdsDb.options.initialiseObjects) {
                        createEntities(entities, preparedStatement)
                        entities.filterIsInstance(JdsLoadListener::class.java).forEach { it.onPreLoad(OnPreLoadEventArgument(jdsDb, connection, alternateConnections)) }
                        //all entities have been initialised, now we populate them
                        if (jdsDb.options.isWritingToPrimaryDataTables) {
                            booleanStatement.use { populateBoolean(entities, it) }
                            doubleStatement.use { populateDouble(entities, it) }
                            enumStatement.use { populateEnum(entities, it) }
                            floatStatement.use { populateFloat(entities, it) }
                            intStatement.use { populateInteger(entities, it) }
                            longStatement.use { populateLong(entities, it) }
                            stringStatement.use { populateString(entities, it) }
                        }
                        if (jdsDb.options.isWritingToPrimaryDataTables || jdsDb.options.isWritingArrayValues) {
                            doubleCollectionStatement.use { populateDoubleCollection(entities, it) }
                            dateTimeCollectionStatement.use { populateDateTimeCollection(entities, it) }
                            enumCollectionStatement.use { populateEnumCollection(entities, it) }
                            floatCollectionStatement.use { populateFloatCollection(entities, it) }
                            intCollectionStatement.use { populateIntegerCollection(entities, it) }
                            longCollectionStatement.use { populateLongCollection(entities, it) }
                            stringCollectionStatement.use { populateStringCollection(entities, it) }
                        }
                        if (jdsDb.options.isWritingToPrimaryDataTables && jdsDb.options.initialiseDatesAndTimes) {
                            dateStatement.use { populateDate(entities, it) }
                            dateTimeStatement.use { populateDateTime(entities, it) }
                            durationStatement.use { populateDuration(entities, it) }
                            monthDayStatement.use { populateMonthDay(entities, it) }
                            periodStatement.use { populatePeriod(entities, it) }
                            timeStatement.use { populateTimes(entities, it) }
                            yearMonthStatement.use { populateYearMonth(entities, it) }
                            zonedDateTimeStatement.use { populateZonedDateTime(entities, it) }
                        }
                        if (jdsDb.options.initialiseObjects) {
                            if (jdsDb.options.isWritingToPrimaryDataTables)
                                blobStatement.use { populateBlobs(entities, it) }
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
    private fun <T : JdsEntity> populateDateTime(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("uuid")
                val uuidLocation = it.getString("uuid_location")
                val uuidLocationVersion = it.getInt("uuid_version")
                val value = it.getTimestamp("value")
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, uuidLocation, uuidLocationVersion).forEach {
                    it.populateProperties(JdsFieldType.DATE_TIME, fieldId, value)
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
    private fun <T : JdsEntity> populateDate(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("uuid")
                val uuidLocation = it.getString("uuid_location")
                val uuidLocationVersion = it.getInt("uuid_version")
                val value = it.getTimestamp("value")
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, uuidLocation, uuidLocationVersion).forEach {
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
    private fun <T : JdsEntity> populateDateTimeCollection(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("uuid")
                val uuidLocation = it.getString("uuid_location")
                val uuidLocationVersion = it.getInt("uuid_version")
                val value = it.getTimestamp("value")
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, uuidLocation, uuidLocationVersion).forEach {
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
    private fun <T : JdsEntity> populateDoubleCollection(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("uuid")
                val uuidLocation = it.getString("uuid_location")
                val uuidLocationVersion = it.getInt("uuid_version")
                val value = it.getObject("value") //primitives can be null
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, uuidLocation, uuidLocationVersion).forEach {
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
    private fun <T : JdsEntity> populateInteger(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("uuid")
                val uuidLocation = it.getString("uuid_location")
                val uuidLocationVersion = it.getInt("uuid_version")
                val value = it.getObject("value") //primitives can be null
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, uuidLocation, uuidLocationVersion).forEach {
                    it.populateProperties(JdsFieldType.INT, fieldId, value)
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
    private fun <T : JdsEntity> populateEnum(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("uuid")
                val uuidLocation = it.getString("uuid_location")
                val uuidLocationVersion = it.getInt("uuid_version")
                val value = it.getObject("value") //primitives can be null
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, uuidLocation, uuidLocationVersion).forEach {
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
    private fun <T : JdsEntity> populateIntegerCollection(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("uuid")
                val uuidLocation = it.getString("uuid_location")
                val uuidLocationVersion = it.getInt("uuid_version")
                val value = it.getObject("value") //primitives can be null
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, uuidLocation, uuidLocationVersion).forEach {
                    it.populateProperties(JdsFieldType.INT_COLLECTION, fieldId, value)
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
    private fun <T : JdsEntity> populateEnumCollection(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("uuid")
                val uuidLocation = it.getString("uuid_location")
                val uuidLocationVersion = it.getInt("uuid_version")
                val value = it.getObject("value") //primitives can be null
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, uuidLocation, uuidLocationVersion).forEach {
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
    private fun <T : JdsEntity> populateFloatCollection(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("uuid")
                val uuidLocation = it.getString("uuid_location")
                val uuidLocationVersion = it.getInt("uuid_version")
                val value = it.getObject("value") //primitives can be null
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, uuidLocation, uuidLocationVersion).forEach {
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
    private fun <T : JdsEntity> populateLong(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("uuid")
                val uuidLocation = it.getString("uuid_location")
                val uuidLocationVersion = it.getInt("uuid_version")
                val fieldId = it.getLong("field_id")
                val value = it.getObject("value") //primitives can be null
                optimalEntityLookup(entities, uuid, uuidLocation, uuidLocationVersion).forEach {
                    it.populateProperties(JdsFieldType.LONG, fieldId, value)
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
    private fun <T : JdsEntity> populateLongCollection(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("uuid")
                val uuidLocation = it.getString("uuid_location")
                val uuidLocationVersion = it.getInt("uuid_version")
                val fieldId = it.getLong("field_id")
                val value = it.getObject("value") //primitives can be null
                optimalEntityLookup(entities, uuid, uuidLocation, uuidLocationVersion).forEach {
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
    private fun <T : JdsEntity> populateDuration(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("uuid")
                val uuidLocation = it.getString("uuid_location")
                val uuidLocationVersion = it.getInt("uuid_version")
                val fieldId = it.getLong("field_id")
                val value = it.getObject("value") //primitives can be null
                optimalEntityLookup(entities, uuid, uuidLocation, uuidLocationVersion).forEach {
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
    private fun <T : JdsEntity> populateString(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("uuid")
                val uuidLocation = it.getString("uuid_location")
                val uuidLocationVersion = it.getInt("uuid_version")
                val value = it.getString("value")
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, uuidLocation, uuidLocationVersion).forEach {
                    it.populateProperties(JdsFieldType.STRING, fieldId, value)
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
    private fun <T : JdsEntity> populateStringCollection(entities: Collection<T>, preparedStatement: PreparedStatement) {
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
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : JdsEntity> populateMonthDay(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("uuid")
                val uuidLocation = it.getString("uuid_location")
                val uuidLocationVersion = it.getInt("uuid_version")
                val value = it.getString("value")
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, uuidLocation, uuidLocationVersion).forEach {
                    it.populateProperties(JdsFieldType.MONTH_DAY, fieldId, value)
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
    private fun <T : JdsEntity> populateYearMonth(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("uuid")
                val uuidLocation = it.getString("uuid_location")
                val uuidLocationVersion = it.getInt("uuid_version")
                val value = it.getString("value")
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, uuidLocation, uuidLocationVersion).forEach {
                    it.populateProperties(JdsFieldType.YEAR_MONTH, fieldId, value)
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
    private fun <T : JdsEntity> populatePeriod(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("uuid")
                val uuidLocation = it.getString("uuid_location")
                val uuidLocationVersion = it.getInt("uuid_version")
                val value = it.getString("value")
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, uuidLocation, uuidLocationVersion).forEach {
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