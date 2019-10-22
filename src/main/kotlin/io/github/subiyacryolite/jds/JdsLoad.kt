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

import io.github.subiyacryolite.jds.JdsExtensions.getLocalDate
import io.github.subiyacryolite.jds.JdsExtensions.getLocalTime
import io.github.subiyacryolite.jds.JdsExtensions.getZonedDateTime
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation
import io.github.subiyacryolite.jds.enums.JdsFieldType
import io.github.subiyacryolite.jds.enums.JdsFilterBy
import io.github.subiyacryolite.jds.events.EventArguments
import io.github.subiyacryolite.jds.events.JdsLoadListener
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
 * @param db
 * @param referenceType
 * @param filterBy
 * @param T
 */
class JdsLoad<T : JdsEntity>(private val db: JdsDb, private val referenceType: Class<T>, private val filterBy: JdsFilterBy) : Callable<MutableCollection<T>> {

    private val alternateConnections: ConcurrentMap<Int, Connection> = ConcurrentHashMap()
    private var filterIds: Iterable<String> = emptyList()

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

    @Throws(Exception::class)
    override fun call(): MutableCollection<T> {
        val entitiesToLoad = ArrayList<JdsEntityComposite>()
        val annotation = referenceType.getAnnotation(JdsEntityAnnotation::class.java)
        val entityId = annotation.id
        prepareActionBatches(db, entityId, entitiesToLoad, filterIds)
        val collections = ArrayList<T>()
        populateInner(db, collections, entitiesToLoad)
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
        try {
            jdsDb.dataSource.connection.use { connection ->
                uuids.chunked(MAX_BATCH_SIZE).forEach { uuids ->
                    val questionsString = prepareParamaterSequence(uuids.size)
                    val getOverviewRecords = StringBuilder()
                    getOverviewRecords.append("SELECT\n")
                    getOverviewRecords.append("  repo.uuid,\n")
                    getOverviewRecords.append("  repo.edit_version,\n")
                    getOverviewRecords.append("  entity_id\n")
                    getOverviewRecords.append("FROM jds_entity_overview repo\n")
                    getOverviewRecords.append("  JOIN (SELECT\n")
                    getOverviewRecords.append("          uuid,\n")
                    getOverviewRecords.append("          max(edit_version) AS edit_version\n")
                    getOverviewRecords.append("        FROM jds_entity_overview\n")
                    getOverviewRecords.append("        WHERE uuid IN $questionsString\n")
                    getOverviewRecords.append("        GROUP BY uuid) latest\n")
                    getOverviewRecords.append("    ON repo.uuid = latest.uuid AND repo.edit_version = latest.edit_version")

                    connection.prepareStatement(getOverviewRecords.toString()).use { preparedStatement ->
                        //create sql to populate fields
                        val populateEmbeddedAndArrayObjects = "SELECT child.* FROM jds_entity_binding child JOIN jds_entity_overview parent ON parent.uuid IN $questionsString " +
                                "AND parent.uuid = child.parent_uuid " +
                                "AND parent.edit_version = child.parent_edit_version"

                        val blobStatement = connection.prepareStatement("SELECT * FROM jds_str_blob WHERE uuid IN $questionsString")
                        val booleanStatement = connection.prepareStatement("SELECT * FROM jds_str_boolean WHERE uuid IN $questionsString")
                        val dateStatement = connection.prepareStatement("SELECT * FROM jds_str_date WHERE uuid IN $questionsString")
                        val dateTimeStatement = connection.prepareStatement("SELECT * FROM jds_str_date_time WHERE uuid IN $questionsString")
                        val dateTimeCollectionStatement = connection.prepareStatement("SELECT * FROM jds_str_date_time_col WHERE uuid IN $questionsString")
                        val doubleStatement = connection.prepareStatement("SELECT * FROM jds_str_double WHERE uuid IN $questionsString")
                        val doubleCollectionStatement = connection.prepareStatement("SELECT * FROM jds_str_double_col WHERE uuid IN $questionsString")
                        val durationStatement = connection.prepareStatement("SELECT * FROM jds_str_duration WHERE uuid IN $questionsString")
                        val enumStatement = connection.prepareStatement("SELECT * FROM jds_str_enum WHERE uuid IN $questionsString")
                        val enumStringStatement = connection.prepareStatement("SELECT * FROM jds_str_enum_string WHERE uuid IN $questionsString")
                        val enumCollectionStatement = connection.prepareStatement("SELECT * FROM jds_str_enum_col WHERE uuid IN $questionsString")
                        val enumStringCollectionStatement = connection.prepareStatement("SELECT * FROM jds_str_enum_string_col WHERE uuid IN $questionsString")
                        val floatStatement = connection.prepareStatement("SELECT * FROM jds_str_float WHERE uuid IN $questionsString")
                        val floatCollectionStatement = connection.prepareStatement("SELECT * FROM jds_str_float_col WHERE uuid IN $questionsString")
                        val intStatement = connection.prepareStatement("SELECT * FROM jds_str_integer WHERE uuid IN $questionsString")
                        val shortStatement = connection.prepareStatement("SELECT * FROM jds_str_short WHERE uuid IN $questionsString")
                        val uuidStatement = connection.prepareStatement("SELECT * FROM jds_str_uuid WHERE uuid IN $questionsString")
                        val intCollectionStatement = connection.prepareStatement("SELECT * FROM jds_str_integer_col WHERE uuid IN $questionsString")
                        val longStatement = connection.prepareStatement("SELECT * FROM jds_str_long WHERE uuid IN $questionsString")
                        val longCollectionStatement = connection.prepareStatement("SELECT * FROM jds_str_long_col WHERE uuid IN $questionsString")
                        val monthDayStatement = connection.prepareStatement("SELECT * FROM jds_str_month_day WHERE uuid IN $questionsString")
                        val periodStatement = connection.prepareStatement("SELECT * FROM jds_str_period WHERE uuid IN $questionsString")
                        val stringStatement = connection.prepareStatement("SELECT * FROM jds_str_text WHERE uuid IN $questionsString")
                        val stringCollectionStatement = connection.prepareStatement("SELECT * FROM jds_str_text_col WHERE uuid IN $questionsString")
                        val timeStatement = connection.prepareStatement("SELECT * FROM jds_str_time WHERE uuid IN $questionsString")
                        val yearMonthStatement = connection.prepareStatement("SELECT * FROM jds_str_year_month WHERE uuid IN $questionsString")
                        val zonedDateTimeStatement = connection.prepareStatement("SELECT * FROM jds_str_zoned_date_time WHERE uuid IN $questionsString")
                        val populateEmbeddedAndArrayObjectsStmt = connection.prepareStatement(populateEmbeddedAndArrayObjects)

                        //work in batches to not break prepared statement
                        uuids.forEachIndexed { index, uuid ->
                            preparedStatement.setString(index + 1, uuid.uuid)
                            populateEmbeddedAndArrayObjectsStmt.setString(index + 1, uuid.uuid)
                            //===========================================================
                            blobStatement.setString(index + 1, uuid.uuid)
                            booleanStatement.setString(index + 1, uuid.uuid)
                            dateStatement.setString(index + 1, uuid.uuid)
                            dateTimeStatement.setString(index + 1, uuid.uuid)
                            dateTimeCollectionStatement.setString(index + 1, uuid.uuid)
                            doubleStatement.setString(index + 1, uuid.uuid)
                            doubleCollectionStatement.setString(index + 1, uuid.uuid)
                            durationStatement.setString(index + 1, uuid.uuid)
                            enumStatement.setString(index + 1, uuid.uuid)
                            enumStringStatement.setString(index + 1, uuid.uuid)
                            enumCollectionStatement.setString(index + 1, uuid.uuid)
                            enumStringCollectionStatement.setString(index + 1, uuid.uuid)
                            floatStatement.setString(index + 1, uuid.uuid)
                            floatCollectionStatement.setString(index + 1, uuid.uuid)
                            intStatement.setString(index + 1, uuid.uuid)
                            intCollectionStatement.setString(index + 1, uuid.uuid)
                            longStatement.setString(index + 1, uuid.uuid)
                            longCollectionStatement.setString(index + 1, uuid.uuid)
                            monthDayStatement.setString(index + 1, uuid.uuid)
                            periodStatement.setString(index + 1, uuid.uuid)
                            stringStatement.setString(index + 1, uuid.uuid)
                            stringCollectionStatement.setString(index + 1, uuid.uuid)
                            timeStatement.setString(index + 1, uuid.uuid)
                            yearMonthStatement.setString(index + 1, uuid.uuid)
                            shortStatement.setString(index + 1, uuid.uuid)
                            uuidStatement.setString(index + 1, uuid.uuid)
                            zonedDateTimeStatement.setString(index + 1, uuid.uuid)
                        }
                        //catch embedded/pre-created objects objects as well
                        if (jdsDb.options.initialisePrimitives || jdsDb.options.initialiseDatesAndTimes || jdsDb.options.initialiseObjects) {
                            createEntities(entities, preparedStatement)
                            entities.filterIsInstance(JdsLoadListener::class.java).forEach { it.onPreLoad(EventArguments(connection)) }
                            //all entities have been initialised, now we populate them
                            if (jdsDb.options.isWritingValuesToEavTables) {
                                booleanStatement.use { populateBoolean(entities, it) }
                                doubleStatement.use { populateDouble(entities, it) }
                                enumStatement.use { populateEnum(entities, it) }
                                enumStringStatement.use { populateEnumString(entities, it) }
                                floatStatement.use { populateFloat(entities, it) }
                                intStatement.use { populateInteger(entities, it) }
                                longStatement.use { populateLong(entities, it) }
                                stringStatement.use { populateString(entities, it) }
                                uuidStatement.use { populateUuid(entities, it) }
                                shortStatement.use { populateShort(entities, it) }
                            }
                            if (jdsDb.options.isWritingValuesToEavTables || jdsDb.options.isWritingCollectionsToEavTables) {
                                doubleCollectionStatement.use { populateDoubleCollection(entities, it) }
                                dateTimeCollectionStatement.use { populateDateTimeCollection(entities, it) }
                                enumCollectionStatement.use { populateEnumCollection(entities, it) }
                                enumStringCollectionStatement.use { populateEnumStringCollection(entities, it) }
                                floatCollectionStatement.use { populateFloatCollection(entities, it) }
                                intCollectionStatement.use { populateIntegerCollection(entities, it) }
                                longCollectionStatement.use { populateLongCollection(entities, it) }
                                stringCollectionStatement.use { populateStringCollection(entities, it) }
                            }
                            if (jdsDb.options.isWritingValuesToEavTables && jdsDb.options.initialiseDatesAndTimes) {
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
                                if (jdsDb.options.isWritingValuesToEavTables)
                                    blobStatement.use { populateBlobs(entities, it) }
                                populateEmbeddedAndArrayObjectsStmt.use { populateObjectEntriesAndObjectArrays(jdsDb, entities, it) }
                            }
                            entities.filterIsInstance(JdsLoadListener::class.java).forEach { it.onPostLoad(EventArguments(connection)) }
                        }
                        //close alternate connections
                        alternateConnections.forEach { it.value.close() }
                    }
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
                val editVersion = it.getInt("edit_version")
                if (db.classes.containsKey(entityId)) {
                    val refType = db.classes[entityId]!!
                    val entity = refType.getDeclaredConstructor().newInstance()
                    entity.overview.uuid = uuid
                    entity.overview.editVersion = editVersion
                    entities.add(entity as T)
                }
            }
        }
    }

    /**
     * @param jdsDb
     * @param entities
     * @param preparedStatement
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
                val parentEditVersion = it.getInt("parent_edit_version")
                val childUuid = it.getString("child_uuid")
                val childEditVersion = it.getInt("child_edit_version")
                val fieldId = it.getLong("field_id")
                val entityId = it.getLong("entity_id")
                optimalEntityLookup(entities, parentUuid, parentEditVersion).forEach { jdsEntity ->
                    jdsEntity.populateObjects(jdsDb, fieldId, entityId, childUuid, childEditVersion, innerObjects, uuids)
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
    private fun <T : JdsEntity> optimalEntityLookup(entities: Collection<T>, uuid: String, editVersion: Int): Stream<T> {
        return entities.stream().filter { it.overview.uuid == uuid && it.overview.editVersion == editVersion }
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
                val editVersion = it.getInt("edit_version")
                val value = it.getTimestamp("value")
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(JdsFieldType.DATE_TIME, fieldId, value)
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
                val editVersion = it.getInt("edit_version")
                val value = it.getLocalDate("value", db)
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(JdsFieldType.DATE, fieldId, value)
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
                val editVersion = it.getInt("edit_version")
                val value = it.getTimestamp("value")
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(JdsFieldType.DATE_TIME_COLLECTION, fieldId, value)
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
                val editVersion = it.getInt("edit_version")
                val value = it.getObject("value") //primitives can be null
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(JdsFieldType.DOUBLE, fieldId, value)
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
                val editVersion = it.getInt("edit_version")
                val value = it.getObject("value") //primitives can be null
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(JdsFieldType.DOUBLE_COLLECTION, fieldId, value)
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
                val editVersion = it.getInt("edit_version")
                val value = it.getBytes("value")
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(JdsFieldType.BLOB, fieldId, value)
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
                val editVersion = it.getInt("edit_version")
                val value = it.getObject("value") //primitives can be null
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(JdsFieldType.INT, fieldId, value)
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
                val editVersion = it.getInt("edit_version")
                val value = it.getObject("value") //primitives can be null
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(JdsFieldType.ENUM, fieldId, value)
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
    private fun <T : JdsEntity> populateEnumString(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("uuid")
                val editVersion = it.getInt("edit_version")
                val value = it.getString("value") //primitives can be null
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(JdsFieldType.ENUM_STRING, fieldId, value)
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
                val editVersion = it.getInt("edit_version")
                val value = it.getObject("value") //primitives can be null
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(JdsFieldType.INT_COLLECTION, fieldId, value)
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
                val editVersion = it.getInt("edit_version")
                val value = it.getObject("value") //primitives can be null
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(JdsFieldType.ENUM_COLLECTION, fieldId, value)
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
    private fun <T : JdsEntity> populateEnumStringCollection(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("uuid")
                val editVersion = it.getInt("edit_version")
                val value = it.getString("value") //primitives can be null
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(JdsFieldType.ENUM_STRING_COLLECTION, fieldId, value)
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
                val editVersion = it.getInt("edit_version")
                val value = it.getObject("value") //primitives can be null
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(JdsFieldType.BOOLEAN, fieldId, value)
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
                val editVersion = it.getInt("edit_version")
                val value = it.getLocalTime("value", db)
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(JdsFieldType.TIME, fieldId, value)
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
                val editVersion = it.getInt("edit_version")
                val value = it.getObject("value") //primitives can be null
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(JdsFieldType.FLOAT, fieldId, value)
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
                val editVersion = it.getInt("edit_version")
                val value = it.getObject("value") //primitives can be null
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(JdsFieldType.FLOAT_COLLECTION, fieldId, value)
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
                val editVersion = it.getInt("edit_version")
                val fieldId = it.getLong("field_id")
                val value = it.getObject("value") //primitives can be null
                optimalEntityLookup(entities, uuid, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(JdsFieldType.LONG, fieldId, value)
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
                val editVersion = it.getInt("edit_version")
                val fieldId = it.getLong("field_id")
                val value = it.getObject("value") //primitives can be null
                optimalEntityLookup(entities, uuid, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(JdsFieldType.LONG_COLLECTION, fieldId, value)
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
                val editVersion = it.getInt("edit_version")
                val fieldId = it.getLong("field_id")
                val value = it.getObject("value") //primitives can be null
                optimalEntityLookup(entities, uuid, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(JdsFieldType.DURATION, fieldId, value)
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
                val editVersion = it.getInt("edit_version")
                val value = it.getZonedDateTime("value", db)
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(JdsFieldType.ZONED_DATE_TIME, fieldId, value)
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
                val editVersion = it.getInt("edit_version")
                val value = it.getString("value")
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(JdsFieldType.STRING, fieldId, value)
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
    private fun <T : JdsEntity> populateUuid(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("uuid")
                val editVersion = it.getInt("edit_version")
                val value = it.getObject("value") //UUID may be null
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(JdsFieldType.UUID, fieldId, value)
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
    private fun <T : JdsEntity> populateShort(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use {
            while (it.next()) {
                val uuid = it.getString("uuid")
                val editVersion = it.getInt("edit_version")
                val value = it.getObject("value") //primitives can be null
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(JdsFieldType.SHORT, fieldId, value)
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
                val editVersion = it.getInt("edit_version")
                val value = it.getString("value")
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(JdsFieldType.STRING_COLLECTION, fieldId, value)
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
                val editVersion = it.getInt("edit_version")
                val value = it.getString("value")
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(JdsFieldType.MONTH_DAY, fieldId, value)
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
                val editVersion = it.getInt("edit_version")
                val value = it.getString("value")
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(JdsFieldType.YEAR_MONTH, fieldId, value)
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
                val editVersion = it.getInt("edit_version")
                val value = it.getString("value")
                val fieldId = it.getLong("field_id")
                optimalEntityLookup(entities, uuid, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(JdsFieldType.PERIOD, fieldId, value)
                }
            }
        }
    }

    /**
     * @param jdsDb
     * @param entityId
     * @param entitiesToLoad
     * @param filterUUIDs
     */
    @Throws(SQLException::class, ClassNotFoundException::class)
    private fun prepareActionBatches(jdsDb: JdsDb, entityId: Long, entitiesToLoad: MutableCollection<JdsEntityComposite>, filterUUIDs: Iterable<String>) {
        val searchByType = filterUUIDs.none()
        jdsDb.dataSource.connection.use {
            if (searchByType) {
                //if no ids supplied we are looking for all instances of the entity.
                //load ALL entityVersions in the in heirarchy
                val loadAllByTypeSql = "SELECT eo.uuid, eo.edit_version FROM jds_entity_overview eo WHERE eo.entity_id IN (SELECT child_entity_id FROM jds_ref_entity_inheritance WHERE parent_entity_id = ?)"
                it.prepareStatement(loadAllByTypeSql).use { statement ->
                    statement.setLong(1, entityId)
                    statement.executeQuery().use { resultSet ->
                        while (resultSet.next())
                            entitiesToLoad.add(JdsEntityComposite(resultSet.getString("uuid"), resultSet.getInt("edit_version")))
                    }
                }
            } else {
                //load all in filter
                val loadByUuidSql = "SELECT uuid, edit_version FROM jds_entity_overview WHERE uuid IN (${parametize(filterUUIDs)})"
                it.prepareStatement(loadByUuidSql).use { statement ->
                    filterUUIDs.forEachIndexed { index, filterValue ->
                        statement.setString(index + 1, filterValue)
                    }
                    statement.executeQuery().use { resultSet ->
                        while (resultSet.next())
                            entitiesToLoad.add(JdsEntityComposite(resultSet.getString("uuid"), resultSet.getInt("edit_version")))
                    }
                }
            }
        }
    }

    /**
     * @param filterUuids
     */
    private fun parametize(filterUuids: Iterable<String>): String {
        val stringJoiner = StringJoiner(",")
        filterUuids.forEach { _ -> stringJoiner.add("?") }
        return stringJoiner.toString()
    }
}