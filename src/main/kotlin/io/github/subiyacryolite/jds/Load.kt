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

import io.github.subiyacryolite.jds.extensions.*
import io.github.subiyacryolite.jds.annotations.EntityAnnotation
import io.github.subiyacryolite.jds.context.DbContext
import io.github.subiyacryolite.jds.enums.FieldType
import io.github.subiyacryolite.jds.enums.FilterBy
import io.github.subiyacryolite.jds.events.EventArguments
import io.github.subiyacryolite.jds.events.LoadListener
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.stream.Stream
import kotlin.collections.ArrayList

/**
 * This class is responsible for loading an [entity's][Entity] [fields][Field]
 * @param dbContext
 * @param referenceType
 * @param filterBy
 * @param T
 */
class Load<T : Entity>(
        private val dbContext: DbContext,
        private val referenceType: Class<T>,
        private val filterBy: FilterBy
) : Callable<MutableCollection<T>> {

    private val alternateConnections: ConcurrentMap<Int, Connection> = ConcurrentHashMap()
    private var filterIds: Iterable<String> = emptyList()

    companion object {
        /**
         * Java supports up to 1000 prepared supportsStatements depending on the driver
         */
        const val MaxBatchSize = 1000

        fun prepareParameterSequence(size: Int): String {
            val questionArray = StringJoiner(",", "(", ")")
            for (index in 0 until size) {
                questionArray.add("?")
            }
            return questionArray.toString()
        }
    }

    /**
     *
     * @param dbContext
     * @param referenceType
     * @param T
     */
    constructor(dbContext: DbContext, referenceType: Class<T>) : this(dbContext, referenceType, FilterBy.Id)

    /**
     *
     * @param dbContext
     * @param referenceType
     * @param filterBy
     * @param filterIds
     * @param T
     */
    constructor(dbContext: DbContext, referenceType: Class<T>, filterBy: FilterBy, filterIds: Collection<String>) : this(dbContext, referenceType, filterBy) {
        this.filterIds = filterIds
    }

    @Throws(Exception::class)
    override fun call(): MutableCollection<T> {
        val entitiesToLoad = HashSet<CompositeKey>()
        val annotation = referenceType.getAnnotation(EntityAnnotation::class.java)
        val entityId = annotation.id
        prepareActionBatches(entityId, entitiesToLoad, filterIds)
        val collections = ArrayList<T>()
        populateInner(collections, entitiesToLoad)
        return collections
    }

    /**
     * @param entities
     * @param compositeKeys
     * @param T
     */
    private fun <T : Entity> populateInner(entities: MutableCollection<T>, compositeKeys: Collection<CompositeKey>) {
        if (compositeKeys.isEmpty()) return
        try {
            dbContext.dataSource.connection.use { connection ->
                compositeKeys.chunked(MaxBatchSize).forEach { compositeKeyBatch ->
                    val questionsString = prepareParameterSequence(compositeKeyBatch.size)

                    val overviewSql = """
                        SELECT
                          repo.id,
                          repo.edit_version,
                          entity_id
                        FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.EntityOverview)} repo
                          JOIN (SELECT
                                  id,
                                  max(edit_version) AS edit_version
                                FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.EntityOverview)}
                                WHERE id IN $questionsString
                                GROUP BY id) latest
                            ON repo.id = latest.id AND repo.edit_version = latest.edit_version
                    """.trimIndent()

                    connection.prepareStatement(overviewSql).use { overviewStatement ->
                        //create sql to populate fields
                        val populateEmbeddedAndArrayObjects = """
                            SELECT child.* FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.EntityBinding)} child JOIN ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.EntityOverview)} parent ON parent.id IN $questionsString 
                                AND parent.id = child.parent_id 
                                AND parent.edit_version = child.parent_edit_version;
                        """.trimIndent()

                        val blobStatement = connection.prepareStatement("SELECT * FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.StoreBlob)} WHERE id IN $questionsString")
                        val booleanStatement = connection.prepareStatement("SELECT * FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.StoreBoolean)} WHERE id IN $questionsString")
                        val dateStatement = connection.prepareStatement("SELECT * FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.StoreDate)} WHERE id IN $questionsString")
                        val dateTimeStatement = connection.prepareStatement("SELECT * FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.StoreDateTime)} WHERE id IN $questionsString")
                        val dateTimeCollectionStatement = connection.prepareStatement("SELECT * FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.StoreDateTimeCollection)} WHERE id IN $questionsString")
                        val doubleStatement = connection.prepareStatement("SELECT * FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.StoreDouble)} WHERE id IN $questionsString")
                        val doubleCollectionStatement = connection.prepareStatement("SELECT * FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.StoreDoubleCollection)} WHERE id IN $questionsString")
                        val durationStatement = connection.prepareStatement("SELECT * FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.StoreDuration)} WHERE id IN $questionsString")
                        val enumStatement = connection.prepareStatement("SELECT * FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.StoreEnum)} WHERE id IN $questionsString")
                        val enumStringStatement = connection.prepareStatement("SELECT * FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.StoreEnumString)} WHERE id IN $questionsString")
                        val enumCollectionStatement = connection.prepareStatement("SELECT * FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.StoreEnumCollection)} WHERE id IN $questionsString")
                        val enumStringCollectionStatement = connection.prepareStatement("SELECT * FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.StoreEnumStringCollection)} WHERE id IN $questionsString")
                        val floatStatement = connection.prepareStatement("SELECT * FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.StoreFloat)} WHERE id IN $questionsString")
                        val floatCollectionStatement = connection.prepareStatement("SELECT * FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.StoreFloatCollection)} WHERE id IN $questionsString")
                        val intStatement = connection.prepareStatement("SELECT * FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.StoreInteger)} WHERE id IN $questionsString")
                        val shortStatement = connection.prepareStatement("SELECT * FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.StoreShort)} WHERE id IN $questionsString")
                        val uuidStatement = connection.prepareStatement("SELECT * FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.StoreUuid)} WHERE id IN $questionsString")
                        val intCollectionStatement = connection.prepareStatement("SELECT * FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.StoreIntegerCollection)} WHERE id IN $questionsString")
                        val uuidCollectionStatement = connection.prepareStatement("SELECT * FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.StoreUuidCollection)} WHERE id IN $questionsString")
                        val longStatement = connection.prepareStatement("SELECT * FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.StoreLong)} WHERE id IN $questionsString")
                        val longCollectionStatement = connection.prepareStatement("SELECT * FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.StoreLongCollection)} WHERE id IN $questionsString")
                        val monthDayStatement = connection.prepareStatement("SELECT * FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.StoreMonthDay)} WHERE id IN $questionsString")
                        val periodStatement = connection.prepareStatement("SELECT * FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.StorePeriod)} WHERE id IN $questionsString")
                        val stringStatement = connection.prepareStatement("SELECT * FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.StoreText)} WHERE id IN $questionsString")
                        val stringCollectionStatement = connection.prepareStatement("SELECT * FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.StoreTextCollection)} WHERE id IN $questionsString")
                        val timeStatement = connection.prepareStatement("SELECT * FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.StoreTime)} WHERE id IN $questionsString")
                        val yearMonthStatement = connection.prepareStatement("SELECT * FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.StoreYearMonth)} WHERE id IN $questionsString")
                        val zonedDateTimeStatement = connection.prepareStatement("SELECT * FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.StoreZonedDateTime)} WHERE id IN $questionsString")
                        val populateEmbeddedAndArrayObjectsStmt = connection.prepareStatement(populateEmbeddedAndArrayObjects)

                        //work in batches to not break prepared statement
                        compositeKeyBatch.forEachIndexed { index, compositeKey ->
                            overviewStatement.setString(index + 1, compositeKey.id)
                            populateEmbeddedAndArrayObjectsStmt.setString(index + 1, compositeKey.id)
                            //===========================================================
                            blobStatement.setString(index + 1, compositeKey.id)
                            booleanStatement.setString(index + 1, compositeKey.id)
                            dateStatement.setString(index + 1, compositeKey.id)
                            dateTimeStatement.setString(index + 1, compositeKey.id)
                            dateTimeCollectionStatement.setString(index + 1, compositeKey.id)
                            doubleStatement.setString(index + 1, compositeKey.id)
                            doubleCollectionStatement.setString(index + 1, compositeKey.id)
                            durationStatement.setString(index + 1, compositeKey.id)
                            enumStatement.setString(index + 1, compositeKey.id)
                            enumStringStatement.setString(index + 1, compositeKey.id)
                            enumCollectionStatement.setString(index + 1, compositeKey.id)
                            enumStringCollectionStatement.setString(index + 1, compositeKey.id)
                            floatStatement.setString(index + 1, compositeKey.id)
                            floatCollectionStatement.setString(index + 1, compositeKey.id)
                            intStatement.setString(index + 1, compositeKey.id)
                            intCollectionStatement.setString(index + 1, compositeKey.id)
                            longStatement.setString(index + 1, compositeKey.id)
                            longCollectionStatement.setString(index + 1, compositeKey.id)
                            monthDayStatement.setString(index + 1, compositeKey.id)
                            periodStatement.setString(index + 1, compositeKey.id)
                            stringStatement.setString(index + 1, compositeKey.id)
                            stringCollectionStatement.setString(index + 1, compositeKey.id)
                            timeStatement.setString(index + 1, compositeKey.id)
                            yearMonthStatement.setString(index + 1, compositeKey.id)
                            shortStatement.setString(index + 1, compositeKey.id)
                            uuidStatement.setString(index + 1, compositeKey.id)
                            zonedDateTimeStatement.setString(index + 1, compositeKey.id)
                        }
                        //catch embedded/pre-created objects objects as well
                        if (dbContext.options.initialisePrimitives || dbContext.options.initialiseDatesAndTimes || dbContext.options.initialiseObjects) {
                            createEntities(entities, overviewStatement)
                            entities.filterIsInstance(LoadListener::class.java).forEach { it.onPreLoad(EventArguments(connection)) }
                            //all entities have been initialised, now we populate them
                            if (dbContext.options.writeValuesToEavTables) {
                                booleanStatement.use { statement -> populateBoolean(entities, statement) }
                                doubleStatement.use { statement -> populateDouble(entities, statement) }
                                enumStatement.use { statement -> populateEnum(entities, statement) }
                                enumStringStatement.use { statement -> populateEnumString(entities, statement) }
                                floatStatement.use { statement -> populateFloat(entities, statement) }
                                intStatement.use { statement -> populateInteger(entities, statement) }
                                longStatement.use { statement -> populateLong(entities, statement) }
                                stringStatement.use { statement -> populateString(entities, statement) }
                                uuidStatement.use { statement -> populateUuid(entities, statement) }
                                shortStatement.use { statement -> populateShort(entities, statement) }
                            }
                            if (dbContext.options.writeValuesToEavTables || dbContext.options.writeCollectionsToEavTables) {
                                doubleCollectionStatement.use { statement -> populateDoubleCollection(entities, statement) }
                                dateTimeCollectionStatement.use { statement -> populateDateTimeCollection(entities, statement) }
                                enumCollectionStatement.use { statement -> populateEnumCollection(entities, statement) }
                                enumStringCollectionStatement.use { statement -> populateEnumStringCollection(entities, statement) }
                                floatCollectionStatement.use { statement -> populateFloatCollection(entities, statement) }
                                intCollectionStatement.use { statement -> populateIntegerCollection(entities, statement) }
                                uuidCollectionStatement.use { statement -> populateUuidCollection(entities, statement) }
                                longCollectionStatement.use { statement -> populateLongCollection(entities, statement) }
                                stringCollectionStatement.use { statement -> populateStringCollection(entities, statement) }
                            }
                            if (dbContext.options.writeValuesToEavTables && dbContext.options.initialiseDatesAndTimes) {
                                dateStatement.use { statement -> populateDate(entities, statement) }
                                dateTimeStatement.use { statement -> populateDateTime(entities, statement) }
                                durationStatement.use { statement -> populateDuration(entities, statement) }
                                monthDayStatement.use { statement -> populateMonthDay(entities, statement) }
                                periodStatement.use { statement -> populatePeriod(entities, statement) }
                                timeStatement.use { statement -> populateTimes(entities, statement) }
                                yearMonthStatement.use { statement -> populateYearMonth(entities, statement) }
                                zonedDateTimeStatement.use { statement -> populateZonedDateTime(entities, statement) }
                            }
                            if (dbContext.options.initialiseObjects) {
                                if (dbContext.options.writeValuesToEavTables)
                                    blobStatement.use { statement -> populateBlobs(entities, statement) }
                                populateEmbeddedAndArrayObjectsStmt.use { statement -> populateObjectEntriesAndObjectArrays(entities, statement) }
                            }
                            entities.filterIsInstance(LoadListener::class.java).forEach { it.onPostLoad(EventArguments(connection)) }
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

    private fun <T : Entity> createEntities(entities: MutableCollection<T>, statement: PreparedStatement) {
        statement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val entityId = resultSet.getInt("entity_id")
                val id = resultSet.getString("id")
                val editVersion = resultSet.getInt("edit_version")
                if (dbContext.classes.containsKey(entityId)) {
                    val refType = dbContext.classes[entityId]!!
                    val entity = refType.getDeclaredConstructor().newInstance()
                    entity.overview.id = id
                    entity.overview.editVersion = editVersion
                    entities.add(entity as T)
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
    private fun <T : Entity> populateObjectEntriesAndObjectArrays(entities: Collection<T>, preparedStatement: PreparedStatement) {
        val compositeKeys = HashSet<CompositeKey>()//ids should be unique
        val innerObjects = HashSet<Entity>()//can be multiple copies of the same object however
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val parentId = resultSet.getString("parent_id")
                val parentEditVersion = resultSet.getInt("parent_edit_version")
                val childId = resultSet.getString("child_id")
                val childEditVersion = resultSet.getInt("child_edit_version")
                val fieldId = resultSet.getInt("field_id")
                val entityId = resultSet.getInt("entity_id")
                optimalEntityLookup(entities, parentId, parentEditVersion).forEach { jdsEntity ->
                    jdsEntity.populateObjects(dbContext, fieldId, entityId, childId, childEditVersion, innerObjects, compositeKeys)
                }
            }
        }
        populateInner(innerObjects, compositeKeys)
    }

    /**
     * @param entities
     * @param id
     * @return
     */
    private fun <T : Entity> optimalEntityLookup(entities: Collection<T>, id: String, editVersion: Int): Stream<T> {
        return entities.stream().filter { it.overview.id == id && it.overview.editVersion == editVersion }
    }

    /**
     * @param entities
     * @param preparedStatement
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun <T : Entity> populateDateTime(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val id = resultSet.getString("id")
                val editVersion = resultSet.getInt("edit_version")
                val value = resultSet.getTimestamp("value")
                val fieldId = resultSet.getInt("field_id")
                optimalEntityLookup(entities, id, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(dbContext, FieldType.DateTime, fieldId, value)
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
    private fun <T : Entity> populateDate(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val id = resultSet.getString("id")
                val editVersion = resultSet.getInt("edit_version")
                val value = resultSet.getLocalDate("value", this.dbContext)
                val fieldId = resultSet.getInt("field_id")
                optimalEntityLookup(entities, id, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(dbContext, FieldType.Date, fieldId, value)
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
    private fun <T : Entity> populateDateTimeCollection(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val id = resultSet.getString("id")
                val editVersion = resultSet.getInt("edit_version")
                val value = resultSet.getTimestamp("value")
                val fieldId = resultSet.getInt("field_id")
                optimalEntityLookup(entities, id, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(dbContext, FieldType.DateTimeCollection, fieldId, value)
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
    private fun <T : Entity> populateDouble(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val id = resultSet.getString("id")
                val editVersion = resultSet.getInt("edit_version")
                val value = resultSet.getObject("value") //primitives can be null
                val fieldId = resultSet.getInt("field_id")
                optimalEntityLookup(entities, id, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(dbContext, FieldType.Double, fieldId, value)
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
    private fun <T : Entity> populateDoubleCollection(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val id = resultSet.getString("id")
                val editVersion = resultSet.getInt("edit_version")
                val value = resultSet.getObject("value") //primitives can be null
                val fieldId = resultSet.getInt("field_id")
                optimalEntityLookup(entities, id, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(dbContext, FieldType.DoubleCollection, fieldId, value)
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
    private fun <T : Entity> populateBlobs(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val id = resultSet.getString("id")
                val editVersion = resultSet.getInt("edit_version")
                val value = resultSet.getBytes("value")
                val fieldId = resultSet.getInt("field_id")
                optimalEntityLookup(entities, id, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(dbContext, FieldType.Blob, fieldId, value)
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
    private fun <T : Entity> populateInteger(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val id = resultSet.getString("id")
                val editVersion = resultSet.getInt("edit_version")
                val value = resultSet.getObject("value") //primitives can be null
                val fieldId = resultSet.getInt("field_id")
                optimalEntityLookup(entities, id, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(dbContext, FieldType.Int, fieldId, value)
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
    private fun <T : Entity> populateEnum(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val id = resultSet.getString("id")
                val editVersion = resultSet.getInt("edit_version")
                val value = resultSet.getObject("value") //primitives can be null
                val fieldId = resultSet.getInt("field_id")
                optimalEntityLookup(entities, id, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(dbContext, FieldType.Enum, fieldId, value)
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
    private fun <T : Entity> populateEnumString(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val id = resultSet.getString("id")
                val editVersion = resultSet.getInt("edit_version")
                val value = resultSet.getString("value") //primitives can be null
                val fieldId = resultSet.getInt("field_id")
                optimalEntityLookup(entities, id, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(dbContext, FieldType.EnumString, fieldId, value)
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
    private fun <T : Entity> populateIntegerCollection(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val id = resultSet.getString("id")
                val editVersion = resultSet.getInt("edit_version")
                val value = resultSet.getObject("value") //primitives can be null
                val fieldId = resultSet.getInt("field_id")
                optimalEntityLookup(entities, id, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(dbContext, FieldType.IntCollection, fieldId, value)
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
    private fun <T : Entity> populateUuidCollection(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val id = resultSet.getString("id")
                val editVersion = resultSet.getInt("edit_version")
                val value = resultSet.getObject("value") //primitives can be null
                val fieldId = resultSet.getInt("field_id")
                optimalEntityLookup(entities, id, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(dbContext, FieldType.UuidCollection, fieldId, value)
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
    private fun <T : Entity> populateEnumCollection(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val id = resultSet.getString("id")
                val editVersion = resultSet.getInt("edit_version")
                val value = resultSet.getObject("value") //primitives can be null
                val fieldId = resultSet.getInt("field_id")
                optimalEntityLookup(entities, id, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(dbContext, FieldType.EnumCollection, fieldId, value)
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
    private fun <T : Entity> populateEnumStringCollection(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val id = resultSet.getString("id")
                val editVersion = resultSet.getInt("edit_version")
                val value = resultSet.getString("value") //primitives can be null
                val fieldId = resultSet.getInt("field_id")
                optimalEntityLookup(entities, id, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(dbContext, FieldType.EnumStringCollection, fieldId, value)
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
    private fun <T : Entity> populateBoolean(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val id = resultSet.getString("id")
                val editVersion = resultSet.getInt("edit_version")
                val value = resultSet.getObject("value") //primitives can be null
                val fieldId = resultSet.getInt("field_id")
                optimalEntityLookup(entities, id, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(dbContext, FieldType.Boolean, fieldId, value)
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
    private fun <T : Entity> populateTimes(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val id = resultSet.getString("id")
                val editVersion = resultSet.getInt("edit_version")
                val value = resultSet.getLocalTime("value", this.dbContext)
                val fieldId = resultSet.getInt("field_id")
                optimalEntityLookup(entities, id, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(dbContext, FieldType.Time, fieldId, value)
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
    private fun <T : Entity> populateFloat(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val id = resultSet.getString("id")
                val editVersion = resultSet.getInt("edit_version")
                val value = resultSet.getObject("value") //primitives can be null
                val fieldId = resultSet.getInt("field_id")
                optimalEntityLookup(entities, id, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(dbContext, FieldType.Float, fieldId, value)
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
    private fun <T : Entity> populateFloatCollection(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val id = resultSet.getString("id")
                val editVersion = resultSet.getInt("edit_version")
                val value = resultSet.getObject("value") //primitives can be null
                val fieldId = resultSet.getInt("field_id")
                optimalEntityLookup(entities, id, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(dbContext, FieldType.FloatCollection, fieldId, value)
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
    private fun <T : Entity> populateLong(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val id = resultSet.getString("id")
                val editVersion = resultSet.getInt("edit_version")
                val fieldId = resultSet.getInt("field_id")
                val value = resultSet.getObject("value") //primitives can be null
                optimalEntityLookup(entities, id, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(dbContext, FieldType.Long, fieldId, value)
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
    private fun <T : Entity> populateLongCollection(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val id = resultSet.getString("id")
                val editVersion = resultSet.getInt("edit_version")
                val fieldId = resultSet.getInt("field_id")
                val value = resultSet.getObject("value") //primitives can be null
                optimalEntityLookup(entities, id, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(dbContext, FieldType.LongCollection, fieldId, value)
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
    private fun <T : Entity> populateDuration(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val id = resultSet.getString("id")
                val editVersion = resultSet.getInt("edit_version")
                val fieldId = resultSet.getInt("field_id")
                val value = resultSet.getObject("value") //primitives can be null
                optimalEntityLookup(entities, id, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(dbContext, FieldType.Duration, fieldId, value)
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
    private fun <T : Entity> populateZonedDateTime(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val id = resultSet.getString("id")
                val editVersion = resultSet.getInt("edit_version")
                val value = resultSet.getZonedDateTime("value", this.dbContext)
                val fieldId = resultSet.getInt("field_id")
                optimalEntityLookup(entities, id, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(dbContext, FieldType.ZonedDateTime, fieldId, value)
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
    private fun <T : Entity> populateString(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val id = resultSet.getString("id")
                val editVersion = resultSet.getInt("edit_version")
                val value = resultSet.getString("value")
                val fieldId = resultSet.getInt("field_id")
                optimalEntityLookup(entities, id, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(dbContext, FieldType.String, fieldId, value)
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
    private fun <T : Entity> populateUuid(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val id = resultSet.getString("id")
                val editVersion = resultSet.getInt("edit_version")
                val value = resultSet.getObject("value") //UUID may be null
                val fieldId = resultSet.getInt("field_id")
                optimalEntityLookup(entities, id, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(dbContext, FieldType.Uuid, fieldId, value)
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
    private fun <T : Entity> populateShort(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val id = resultSet.getString("id")
                val editVersion = resultSet.getInt("edit_version")
                val value = resultSet.getObject("value") //primitives can be null
                val fieldId = resultSet.getInt("field_id")
                optimalEntityLookup(entities, id, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(dbContext, FieldType.Short, fieldId, value)
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
    private fun <T : Entity> populateStringCollection(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val id = resultSet.getString("id")
                val editVersion = resultSet.getInt("edit_version")
                val value = resultSet.getString("value")
                val fieldId = resultSet.getInt("field_id")
                optimalEntityLookup(entities, id, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(dbContext, FieldType.StringCollection, fieldId, value)
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
    private fun <T : Entity> populateMonthDay(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val id = resultSet.getString("id")
                val editVersion = resultSet.getInt("edit_version")
                val value = resultSet.getString("value")
                val fieldId = resultSet.getInt("field_id")
                optimalEntityLookup(entities, id, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(dbContext, FieldType.MonthDay, fieldId, value)
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
    private fun <T : Entity> populateYearMonth(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val id = resultSet.getString("id")
                val editVersion = resultSet.getInt("edit_version")
                val value = resultSet.getString("value")
                val fieldId = resultSet.getInt("field_id")
                optimalEntityLookup(entities, id, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(dbContext, FieldType.YearMonth, fieldId, value)
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
    private fun <T : Entity> populatePeriod(entities: Collection<T>, preparedStatement: PreparedStatement) {
        preparedStatement.executeQuery().use { resultSet ->
            while (resultSet.next()) {
                val id = resultSet.getString("id")
                val editVersion = resultSet.getInt("edit_version")
                val value = resultSet.getString("value")
                val fieldId = resultSet.getInt("field_id")
                optimalEntityLookup(entities, id, editVersion).forEach { jdsEntity ->
                    jdsEntity.populateProperties(dbContext, FieldType.Period, fieldId, value)
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
    private fun prepareActionBatches(entityId: Int, entitiesToLoad: MutableCollection<CompositeKey>, filterUUIDs: Iterable<String>) {
        val searchByType = filterUUIDs.none()
        dbContext.dataSource.connection.use {
            if (searchByType) {
                //if no ids supplied we are looking for all instances of the entity.
                //load ALL entityVersions in the in heirarchy
                val loadAllByTypeSql = "SELECT eo.id, eo.edit_version FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.EntityOverview)} eo WHERE eo.entity_id IN (SELECT ? UNION SELECT child_entity_id FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.EntityInheritance)} WHERE parent_entity_id = ?)"
                it.prepareStatement(loadAllByTypeSql).use { statement ->
                    statement.setInt(1, entityId)//base instances
                    statement.setInt(2, entityId)//derived instances
                    statement.executeQuery().use { resultSet ->
                        while (resultSet.next())
                            entitiesToLoad.add(CompositeKey(resultSet.getString("id"), resultSet.getInt("edit_version")))
                    }
                }
            } else {
                //load all in filter
                val loadByUuidSql = "SELECT id, edit_version FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.EntityOverview)} WHERE id IN (${parametize(filterUUIDs)})"
                it.prepareStatement(loadByUuidSql).use { statement ->
                    filterUUIDs.forEachIndexed { index, filterValue ->
                        statement.setString(index + 1, filterValue)
                    }
                    statement.executeQuery().use { resultSet ->
                        while (resultSet.next())
                            entitiesToLoad.add(CompositeKey(resultSet.getString("id"), resultSet.getInt("edit_version")))
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