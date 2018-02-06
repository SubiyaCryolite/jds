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
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation
import io.github.subiyacryolite.jds.enums.JdsFieldType
import io.github.subiyacryolite.jds.enums.JdsImplementation
import io.github.subiyacryolite.jds.enums.JdsFilterBy
import io.github.subiyacryolite.jds.events.EventArguments
import java.io.Serializable
import java.sql.Connection
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.ConcurrentMap
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashMap


open class JdsTable() : Serializable {

    var name = ""
    var uniqueEntries = false
    var onlyLiveRecords = false
    var onlyDeprecatedRecords = false
    var entities = HashSet<Long>()
    var fields = HashSet<Long>()
    var uniqueBy = JdsFilterBy.UUID
    private val columnToFieldMap = LinkedHashMap<String, JdsField>()
    private val enumOrdinals = HashMap<String, Int>()
    private val columnNames = LinkedList<String>()
    private var targetConnection = 0
    private var deleteByCompositeKeySql = ""
    private var deleteByUuidSql = ""
    private var deleteByUuidLocationSql = ""
    private var deleteByParentUuidSql = ""
    private var insertSql = ""
    private var generatedOrUpdatedSchema = false

    /**
     * @param entity
     * @param uniqueEntries
     */
    @JvmOverloads
    constructor(entity: Class<out IJdsEntity>, uniqueEntries: Boolean = false) : this() {
        val annotatedClass = entity.isAnnotationPresent(JdsEntityAnnotation::class.java)
        val annotatedParent = entity.superclass.isAnnotationPresent(JdsEntityAnnotation::class.java)
        if (annotatedClass || annotatedParent) {
            val entityAnnotation = when (annotatedClass) {
                true -> entity.getAnnotation(JdsEntityAnnotation::class.java)
                false -> entity.superclass.getAnnotation(JdsEntityAnnotation::class.java)
            }
            name = entityAnnotation.entityName
            this.uniqueEntries = uniqueEntries
            registerEntity(entity, true)
        }
    }

    /**
     * If you are creating a JdsTable in memory
     * Register entityVersions here
     * @param entityClass
     * @param registerFields
     */
    @JvmOverloads
    fun registerEntity(entityClass: Class<out IJdsEntity>, registerFields: Boolean = false) {
        val annotatedClass = entityClass.isAnnotationPresent(JdsEntityAnnotation::class.java)
        val annotatedParent = entityClass.superclass.isAnnotationPresent(JdsEntityAnnotation::class.java)
        if (annotatedClass || annotatedParent) {
            val entityAnnotation = when (annotatedClass) {
                true -> entityClass.getAnnotation(JdsEntityAnnotation::class.java)
                false -> entityClass.superclass.getAnnotation(JdsEntityAnnotation::class.java)
            }
            entities.add(entityAnnotation.entityId)
            if (registerFields) {
                val entity = entityClass.newInstance()
                entity.registerFields(this)
            }
        }
    }

    /**
     * Method used to register [JdsField]s that shall be persisted to [JdsTable]s
     * @param fieldId a numeric value corresponding to a [JdsField] which's value shall be persisted to this[JdsTable]
     */
    fun registerField(fieldId: Long) {
        fields.add(fieldId)
    }

    /**
     * Method used to register [JdsField]s that shall be persisted to [JdsTable]s
     * @param jdsField a [JdsField] which's value shall be persisted to this[JdsTable]
     */
    fun registerField(jdsField: JdsField) {
        registerField(jdsField.id)
    }

    /**
     *
     * @param jdsDb an instance of JdsDb, used to lookup mapped classes and determine SQL types based on implementation
     * @param alternateConnections a [ConcurrentMap] of holding a pool of alternate [Connections] to write to
     * @param entity a [JdsEntity] that may have [JdsField]s persisted to this [JdsTable]
     * @param eventArguments [EventArguments] that will hold batched SQL queries for execution
     * @throws Exception General IO errors
     */
    @Throws(Exception::class)
    fun executeSave(jdsDb: JdsDb, connection: Connection, alternateConnections: ConcurrentMap<Int, Connection>, entity: JdsEntity, eventArguments: EventArguments) {
        if (!generatedOrUpdatedSchema)
            throw ExceptionInInitializerError("You must call forceGenerateOrUpdateSchema()")
        val satisfied = satisfiesConditions(jdsDb, entity)
        if (satisfied) {
            val iConnection = when (jdsDb.isSqLiteDb && targetConnection == 0) {
                true -> connection
                else -> {
                    if (!alternateConnections.containsKey(targetConnection))
                        alternateConnections[targetConnection] = jdsDb.getConnection(targetConnection)
                    alternateConnections[targetConnection]!!
                }
            }

            if (uniqueEntries) {
                deleteExistingRecords(eventArguments, iConnection, entity)
            }

            val insertStatement = eventArguments.getOrAddStatement(iConnection, insertSql)
            insertStatement.setString(1, entity.overview.compositeKey)
            insertStatement.setString(2, entity.overview.uuid)
            insertStatement.setString(3, entity.overview.uuidLocation)
            insertStatement.setInt(4, entity.overview.uuidLocationVersion)
            insertStatement.setString(5, entity.overview.parentUuid)
            insertStatement.setLong(6, entity.overview.entityId)
            //order will be maintained by linked list
            columnNames.forEachIndexed { columnIndex, columnName ->
                val field = columnToFieldMap[columnName]!!
                val value = when (field.type == JdsFieldType.ENUM_COLLECTION) {
                    true -> entity.getReportAtomicValue(field.id, enumOrdinals[columnName]!!)
                    false -> entity.getReportAtomicValue(field.id, 0)
                }
                when (value) {
                    is ZonedDateTime -> insertStatement.setZonedDateTime(columnIndex + 7, value, jdsDb)
                    is LocalTime -> insertStatement.setLocalTime(columnIndex + 7, value, jdsDb)
                    else -> insertStatement.setObject(columnIndex + 7, value ?: null)
                }
            }
            insertStatement.addBatch()
        }
    }

    fun deleteExistingRecords(eventArguments: EventArguments, iConnection: Connection, entity: JdsEntity) {
        when (uniqueBy) {
            JdsFilterBy.COMPOSITE_KEY -> deleteRecordByCompositeKeyInternal(eventArguments, iConnection, entity.overview.uuid)
            JdsFilterBy.UUID -> deleteRecordByUuidInternal(eventArguments, iConnection, entity.overview.uuid)
            JdsFilterBy.UUID_LOCATION -> deleteRecordByUuidLocationInternal(eventArguments, iConnection, entity.overview.uuid)
            JdsFilterBy.PARENT_UUID -> deleteRecordByParentUuidInternal(eventArguments, iConnection, entity.overview.parentUuid!!)
        }
    }

    /**
     * Empty an entire table
     * @param connection the [Connection] to use for this operation
     */
    fun truncateTable(connection: Connection) {
        val preparedStatement = connection.prepareStatement("TRUNCATE TABLE $name")
        preparedStatement.executeUpdate()
    }

    /**
     * @param jdsDb The [JdsDb] instance to use for this operation
     * @param eventArguments the [EventArguments] to use for this operation
     * @param connection the [Connection] to use for this operation
     * @param entity the uuid to target for record deletion
     */
    fun deleteRecordById(jdsDb: JdsDb, eventArguments: EventArguments, connection: Connection, entity: JdsEntity) {
        val satisfied = satisfiesConditions(jdsDb, entity)
        if (satisfied)
            deleteRecordByUuidInternal(eventArguments, connection, entity.overview.uuid)
    }


    /**
     * @param eventArguments the [EventArguments] to use for this operation
     * @param connection the [Connection] to use for this operation
     * @param parentId the uuid to target for record deletion
     */
    fun deleteRecordByParentId(eventArguments: EventArguments, connection: Connection, parentId: String) {
        deleteRecordByParentUuidInternal(eventArguments, connection, parentId)
    }

    /**
     * @param eventArguments the [EventArguments] to use for this operation
     * @param connection the [Connection] to use for this operation
     * @param uuid the uuid to target for record deletion
     */
    private fun deleteRecordByParentUuidInternal(eventArguments: EventArguments, connection: Connection, uuid: String) {
        val deleteStatement = eventArguments.getOrAddStatement(connection, deleteByParentUuidSql)
        deleteStatement.setString(1, uuid)
        deleteStatement.addBatch()
    }

    /**
     * @param eventArguments the [EventArguments] to use for this operation
     * @param connection the [Connection] to use for this operation
     * @param parentId the uuid to target for record deletion
     */
    fun deleteRecordByUuidLocation(eventArguments: EventArguments, connection: Connection, parentId: String) {
        deleteRecordByUuidLocationInternal(eventArguments, connection, parentId)
    }

    /**
     * @param eventArguments the [EventArguments] to use for this operation
     * @param connection the [Connection] to use for this operation
     * @param uuid the uuid to target for record deletion
     */
    private fun deleteRecordByUuidLocationInternal(eventArguments: EventArguments, connection: Connection, uuid: String) {
        val deleteStatement = eventArguments.getOrAddStatement(connection, deleteByUuidLocationSql)
        deleteStatement.setString(1, uuid)
        deleteStatement.addBatch()
    }

    /**
     * @param eventArguments the [EventArguments] to use for this operation
     * @param connection the [Connection] to use for this operation
     * @param parentId the uuid to target for record deletion
     */
    fun deleteRecordByCompositeKey(eventArguments: EventArguments, connection: Connection, parentId: String) {
        deleteRecordByCompositeKeyInternal(eventArguments, connection, parentId)
    }

    /**
     * @param eventArguments the [EventArguments] to use for this operation
     * @param connection the [Connection] to use for this operation
     * @param uuid the uuid to target for record deletion
     */
    private fun deleteRecordByCompositeKeyInternal(eventArguments: EventArguments, connection: Connection, uuid: String) {
        val deleteStatement = eventArguments.getOrAddStatement(connection, deleteByCompositeKeySql)
        deleteStatement.setString(1, uuid)
        deleteStatement.addBatch()
    }

    /**
     * @param eventArguments the [EventArguments] to use for this operation
     * @param connection the [Connection] to use for this operation
     * @param parentId the uuid to target for record deletion
     */
    fun deleteRecordByUuid(eventArguments: EventArguments, connection: Connection, parentId: String) {
        deleteRecordByUuidInternal(eventArguments, connection, parentId)
    }

    /**
     * @param eventArguments the [EventArguments] to use for this operation
     * @param connection the [Connection] to use for this operation
     * @param uuid the uuid to target for record deletion
     */
    private fun deleteRecordByUuidInternal(eventArguments: EventArguments, connection: Connection, uuid: String) {
        val deleteStatement = eventArguments.getOrAddStatement(connection, deleteByUuidSql)
        deleteStatement.setString(1, uuid)
        deleteStatement.addBatch()
    }

    /**
     * @param jdsDb an instance of [JdsDb], used determine SQL types based on implementation
     * @param pool a shared connection pool to quickly query the target schemas
     */
    internal fun forceGenerateOrUpdateSchema(jdsDb: JdsDb, pool: HashMap<Int, Connection>) {

        if (!pool.containsKey(targetConnection))
            pool[targetConnection] = jdsDb.getConnection(targetConnection)

        val connection = pool[targetConnection]!!

        val tableFields = JdsField.values.filter { fields.contains(it.value.id) }.map { it.value }
        val createTableSql = JdsSchema.generateTable(jdsDb, name, uniqueEntries)
        val createColumnsSql = JdsSchema.generateColumns(jdsDb, tableFields, columnToFieldMap, enumOrdinals)

        if (!jdsDb.doesTableExist(connection, name))
            connection.prepareStatement(createTableSql).use {
                it.executeUpdate()
                if (jdsDb.options.isPrintingOutput)
                    println("Created $name")
            }

        val insertColumns = StringJoiner(",")
        val insertParameters = StringJoiner(",")

        //mandatory primary key
        insertColumns.add(JdsSchema.compositeKeyColumn)
        insertColumns.add(JdsSchema.uuidColumn)
        insertColumns.add(JdsSchema.uuidLocationColumn)
        insertColumns.add(JdsSchema.uuidLocationVersionColumn)
        insertColumns.add(JdsSchema.parentUuidColumn)
        insertColumns.add(JdsSchema.entityIdColumn)
        insertParameters.add("?")
        insertParameters.add("?")
        insertParameters.add("?")
        insertParameters.add("?")
        insertParameters.add("?")
        insertParameters.add("?")

        val delimiter = when (jdsDb.implementation) {
            JdsImplementation.TSQL, JdsImplementation.ORACLE -> " "
            else -> "ADD COLUMN "
        }
        val prefix = when (jdsDb.implementation) {
            JdsImplementation.TSQL -> "ADD "
            JdsImplementation.ORACLE -> "ADD ("
            else -> "ADD COLUMN ";
        }
        val suffix = when (jdsDb.implementation) {
            JdsImplementation.ORACLE -> ")"
            else -> ""
        }

        var foundChanges = false
        val stringJoiner = StringJoiner(", $delimiter", prefix, suffix)
        val sqliteJoiner = LinkedList<String>()
        createColumnsSql.forEach { columnName, createColumnSql ->
            if (!jdsDb.doesColumnExist(connection, name, columnName)) {
                if (!jdsDb.isSqLiteDb)
                    stringJoiner.add(createColumnSql)
                else
                    sqliteJoiner.add(createColumnSql)
                foundChanges = true
            }
            //regardless of column existing add to the query
            columnNames.add(columnName)
            insertColumns.add(columnName)
            insertParameters.add("?")
        }

        if (foundChanges) {
            if (!jdsDb.isSqLiteDb) {
                val addNewColumnsSql = String.format(jdsDb.getDbAddColumnSyntax(), name, stringJoiner)
                connection.prepareStatement(addNewColumnsSql).use { it.executeUpdate() }
            } else {
                //sqlite must alter columns once per entry
                sqliteJoiner.forEach {
                    val addNewColumnsSql = String.format(jdsDb.getDbAddColumnSyntax(), name, "ADD COLUMN $it")
                    connection.prepareStatement(addNewColumnsSql).use { it.executeUpdate() }
                }
            }
        }

        deleteByCompositeKeySql = "DELETE FROM $name WHERE ${JdsSchema.compositeKeyColumn} = ?"
        deleteByParentUuidSql = "DELETE FROM $name WHERE ${JdsSchema.parentUuidColumn} = ?"
        deleteByUuidSql = "DELETE FROM $name WHERE ${JdsSchema.uuidColumn} = ?"
        deleteByUuidLocationSql = "DELETE FROM $name WHERE ${JdsSchema.uuidLocationColumn} = ?"


        insertSql = "INSERT INTO $name ($insertColumns) VALUES ($insertParameters)"

        generatedOrUpdatedSchema = true
    }

    /**
     * Determine if this entity should have its properties persisted to this table
     * @param jdsDb an instance of [JdsDb], used to lookup mapped classes
     * @param entity a [JdsEntity] that may have [JdsField]s written to this table
     */
    private fun satisfiesConditions(jdsDb: JdsDb, entity: JdsEntity): Boolean {

        if (onlyLiveRecords && !entity.overview.live)
            return false

        if (onlyDeprecatedRecords && entity.overview.live)
            return false

        if (entity is IJdsTableFilter)
            if (!entity.satisfiesCondition(this))
                return false

        if (entities.isEmpty())
            return true

        entities.forEach {
            val entityType = jdsDb.classes[it]
            if (entityType != null) {
                if (entityType.isInstance(entity))
                    return true
            } else
                println("JdsTable :: Entity ID $it is not mapped, will not be written to table '$name'")
        }

        return false
    }

    /**
     * @param targetConnection
     */
    fun setTargetConnection(targetConnection: Int) {
        this.targetConnection = targetConnection
    }

    companion object {

        private const val serialVersionUID = 20171109_0853L
    }
}