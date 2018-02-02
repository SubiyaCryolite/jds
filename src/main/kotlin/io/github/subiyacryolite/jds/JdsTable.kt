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
    private val columnToFieldMap = LinkedHashMap<String, JdsField>()
    private val enumOrdinals = HashMap<String, Int>()
    private val columnNames = LinkedList<String>()
    private val insertColumns = StringJoiner(",")
    private val insertParameters = StringJoiner(",")
    private var targetConnection = 0
    private var deleteSql = ""
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
     * If you are creating a JdsTable in memory
     * Register fieldIds here
     * @param fieldId
     */
    fun registerField(fieldId: Long) {
        fields.add(fieldId)
    }

    /**
     * If you are creating a JdsTable in memory
     * Register fieldIds here
     * @param jdsField
     */
    fun registerField(jdsField: JdsField) {
        registerField(jdsField.id)
    }

    /**
     *
     * @param jdsDb an instance of JdsDb, used to lookup mapped classes and determine SQL types based on implementation
     * @param alternateConnections
     * @param entity an entity that may have properties of interest
     * @param eventArguments event arguments that will hold batched SQL queries for execution
     * @throws Exception General IO errors
     */
    @Throws(Exception::class)
    fun executeSave(jdsDb: JdsDb, connection: Connection, alternateConnections: ConcurrentMap<Int, Connection>, entity: JdsEntity, eventArguments: EventArguments) {
        if (!generatedOrUpdatedSchema)
            throw ExceptionInInitializerError("You must call forceGenerateOrUpdateSchema()")

        val iConnection = when (jdsDb.isSqLiteDb && targetConnection == 0) {
            true -> connection
            else -> {
                if (!alternateConnections.containsKey(targetConnection))
                    alternateConnections[targetConnection] = jdsDb.getConnection(targetConnection)
                alternateConnections[targetConnection]!!
            }
        }

        val satisfied = satisfiesConditions(jdsDb, entity)
        if (satisfied) {
            if (uniqueEntries)
                deleteRecordInternal(eventArguments, iConnection, entity.overview.uuid)

            val insertStatement = eventArguments.getOrAddStatement(iConnection, insertSql)
            insertStatement.setString(1, entity.overview.uuid)
            insertStatement.setLong(2, entity.overview.entityId)
            //order will be maintained by linked list
            columnNames.forEachIndexed { columnIndex, columnName ->
                val field = columnToFieldMap[columnName]!!
                val value = when (field.type == JdsFieldType.ENUM_COLLECTION) {
                    true -> entity.getReportAtomicValue(field.id, enumOrdinals[columnName]!!)
                    false -> entity.getReportAtomicValue(field.id, 0)
                }
                when (value) {
                    is ZonedDateTime -> insertStatement.setZonedDateTime(columnIndex + 3, value, jdsDb)
                    is LocalTime -> insertStatement.setLocalTime(columnIndex + 3, value, jdsDb)
                    else -> insertStatement.setObject(columnIndex + 3, value ?: null)
                }
            }
            insertStatement.addBatch()
        }
    }

    /**
     * Empty an entire table
     * @param connection
     */
    fun truncateTable(connection: Connection) {
        val preparedStatement = connection.prepareStatement("TRUNCATE TABLE $name")
        preparedStatement.executeUpdate()
    }

    /**
     * @param jdsDb
     * @param eventArguments
     * @param connection
     * @param entity
     */
    fun deleteRecord(jdsDb: JdsDb, eventArguments: EventArguments, connection: Connection, entity: JdsEntity) {
        val satisfied = satisfiesConditions(jdsDb, entity)
        if (satisfied)
            deleteRecordInternal(eventArguments, connection, entity.overview.uuid)
    }

    /**
     * @param eventArguments
     * @param connection
     * @param uuid
     */
    private fun deleteRecordInternal(eventArguments: EventArguments, connection: Connection, uuid: String) {
        //if unique delete old entries
        val deleteStatement = eventArguments.getOrAddStatement(connection, deleteSql)
        deleteStatement.setString(1, uuid)
        deleteStatement.addBatch()
    }

    /**
     * @param jdsDb an instance of JdsDb, used determine SQL types based on implementation
     * @param pool a shared connection pool to quickly query the target schemas
     */
    internal fun forceGenerateOrUpdateSchema(jdsDb: JdsDb, pool: HashMap<Int, Connection>) {

        if (!pool.containsKey(targetConnection))
            pool[targetConnection] = jdsDb.getConnection(targetConnection)

        val connection = pool[targetConnection]!!

        val tableFields = JdsField.values.filter { fields.contains(it.value.id) }.map { it.value }
        val createTableSql = JdsSchema.generateTable(jdsDb, name, uniqueEntries)
        val createColumnsSql = JdsSchema.generateColumns(jdsDb, tableFields, columnToFieldMap, enumOrdinals)
        val primaryKeyColumn = JdsSchema.getPrimaryKeyColumn()
        val entityIdColumn = JdsSchema.getEntityIdColumn()

        if (!jdsDb.doesTableExist(connection, name))
            connection.prepareStatement(createTableSql).use {
                it.executeUpdate()
                if (jdsDb.isPrintingOutput)
                    println("Created $name")
            }

        //mandatory primary key
        insertColumns.add(primaryKeyColumn)
        insertColumns.add(entityIdColumn)
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


        deleteSql = "DELETE FROM $name WHERE $primaryKeyColumn = ?"
        insertSql = "INSERT INTO $name ($insertColumns) VALUES ($insertParameters)"

        generatedOrUpdatedSchema = true
    }

    /**
     * Determine if this entity should have its properties persisted to this table
     * @param jdsDb an instance of JdsDb, used to lookup mapped classes
     * @param entity an entity that may have properties of interest
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