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
import io.github.subiyacryolite.jds.enums.JdsFilterBy
import io.github.subiyacryolite.jds.enums.JdsImplementation
import io.github.subiyacryolite.jds.events.EventArgument
import java.io.Serializable
import java.sql.Connection
import java.sql.Timestamp
import java.time.*
import java.util.*
import java.util.concurrent.ConcurrentMap
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashMap


open class JdsTable() : Serializable {

    var name = ""
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
    private var deleteByParentUuidSql = ""
    private var deleteByUuidSql = ""
    private var generatedOrUpdatedSchema = false
    private val storedProcedureName: String
        get() {
            val stringJoiner = StringJoiner("_")
            entities.forEach { stringJoiner.add(it.toString()) }
            return "jds_str_${name}_$stringJoiner"
        }

    /**
     * @param entity
     * @param uniqueEntries
     */
    @JvmOverloads
    constructor(entity: Class<out IJdsEntity>) : this() {
        val annotatedClass = entity.isAnnotationPresent(JdsEntityAnnotation::class.java)
        val annotatedParent = entity.superclass.isAnnotationPresent(JdsEntityAnnotation::class.java)
        if (annotatedClass || annotatedParent) {
            val entityAnnotation = when (annotatedClass) {
                true -> entity.getAnnotation(JdsEntityAnnotation::class.java)
                false -> entity.superclass.getAnnotation(JdsEntityAnnotation::class.java)
            }
            name = entityAnnotation.name
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
            entities.add(entityAnnotation.id)
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
     * @param entity a [JdsEntity][JdsEntity] that may have [JdsField'S][JdsField] persisted to this [JdsTable]
     * @param eventArgument The [EventArgument][EventArgument] that will hold batched SQL queries for execution
     * @throws Exception General IO errors
     */
    @Throws(Exception::class)
    fun executeSave(jdsDb: JdsDb, entity: JdsEntity, eventArgument: EventArgument, deleteColumns: HashMap<String, MutableCollection<Any>>) {
        if (!generatedOrUpdatedSchema)
            throw ExceptionInInitializerError("You must call forceGenerateOrUpdateSchema()")
        val satisfied = satisfiesConditions(jdsDb, entity)
        if (satisfied) {
            if (jdsDb.options.isUpdatingCustomReportTablesPerSave)
                deleteExistingRecords(entity, deleteColumns)

            //prepare insert placeholder
            if (!jdsDb.supportsStatements) {
                val stmt = eventArgument.getOrAddStatement("INSERT INTO $name(uuid,  edit_version) VALUES(?,  ?)")
                stmt.setString(1, entity.overview.uuid)
                stmt.setInt(2, entity.overview.editVersion)
                stmt.addBatch()
            }

            val updateValues = LinkedList<Any?>() //maintain order
            val updateColumns = LinkedList<String>() //maintain order
            columnNames.forEach {
                val field = columnToFieldMap[it]!!
                val value = when (field.type == JdsFieldType.ENUM_COLLECTION) {
                    true -> entity.getReportAtomicValue(field.id, enumOrdinals[it]!!)
                    false -> entity.getReportAtomicValue(field.id, 0)
                }
                updateColumns.add(it)
                updateValues.add(value)
            }

            if (updateColumns.isEmpty()) return //VERY important or else well have an invalid update with no columns

            val joiner = StringJoiner(", ")
            updateColumns.forEach {
                joiner.add(if (jdsDb.supportsStatements) "?" else "$it = ?")
            }
            if (jdsDb.supportsStatements) {
                joiner.add("?")//last parameter for composite keys
                joiner.add("?")//last parameter for composite keys
            }

            val statementQuery = when (jdsDb.supportsStatements) {
                true -> "{call $storedProcedureName($joiner)}"
                false -> {
                    val stringBuilder = StringBuilder()
                    stringBuilder.append("UPDATE ")
                    stringBuilder.append(name)
                    stringBuilder.append(" SET ")
                    stringBuilder.append(joiner)
                    stringBuilder.append(" WHERE ")
                    stringBuilder.append("uuid = ? AND edit_version = ?")//last parameter for composite key
                    stringBuilder.toString()
                }
            }

            val insertStatement = when (jdsDb.supportsStatements) {
                true -> eventArgument.getOrAddCall(statementQuery)
                false -> eventArgument.getOrAddStatement(statementQuery)
            }

            updateValues.forEachIndexed { index, value ->
                when (value) {
                    is ZonedDateTime -> insertStatement.setZonedDateTime(index + 1, value, jdsDb)
                    is LocalTime -> insertStatement.setLocalTime(index + 1, value, jdsDb)
                    is LocalDateTime -> insertStatement.setTimestamp(index + 1, Timestamp.valueOf(value))
                    is LocalDate -> insertStatement.setTimestamp(index + 1, Timestamp.valueOf(value.atStartOfDay()))
                    is MonthDay -> insertStatement.setString(index + 1, value.toString())
                    is YearMonth -> insertStatement.setString(index + 1, value.toString())
                    is Period -> insertStatement.setString(index + 1, value.toString())
                    is Duration -> insertStatement.setLong(index + 1, value.toNanos())
                    else -> insertStatement.setObject(index + 1, value ?: null)
                }
            }
            insertStatement.setString(columnNames.size + 1, entity.overview.uuid)
            insertStatement.setInt(columnNames.size + 2, entity.overview.editVersion)
            insertStatement.addBatch()
        }
    }

    fun deleteExistingRecords(entity: JdsEntity, deleteColumns: HashMap<String, MutableCollection<Any>>) {
        when (uniqueBy) {
            JdsFilterBy.UUID -> deleteColumns.getOrPut(deleteByUuidSql) { LinkedList() }.addAll(arrayOf(entity.overview.uuid, entity.overview.editVersion))
            JdsFilterBy.UUID_LOCATION -> deleteColumns.getOrPut(deleteByParentUuidSql) { LinkedList() }.addAll(arrayOf(entity.overview.uuid, entity.overview.editVersion))
        }
    }

    /**
     * Empty an entire table
     * @param connection the [connection][Connection] used for this operation
     */
    fun truncateTable(connection: Connection) {
        connection.prepareStatement("TRUNCATE TABLE $name").use { it.executeUpdate() }
    }

    /**
     * @param jdsDb The [JdsDb] instance to use for this operation
     * @param eventArgument the [EventArgument] to use for this operation
     * @param connection the [Connection] to use for this operation
     * @param entity the uuid to target for record deletion
     */
    fun deleteRecordById(jdsDb: JdsDb, eventArgument: EventArgument, connection: Connection, entity: JdsEntity) {
        val satisfied = satisfiesConditions(jdsDb, entity)
        if (satisfied)
            deleteRecordByUuidInternal(eventArgument, connection, entity.overview.uuid)
    }

    /**
     * @param eventArgument the [EventArgument] to use for this operation
     * @param connection the [Connection] to use for this operation
     * @param uuid the uuid to target for record deletion
     */
    private fun deleteRecordByParentUuidInternal(eventArgument: EventArgument, connection: Connection, uuid: String) {
        val deleteStatement = eventArgument.getOrAddStatement(connection, deleteByParentUuidSql)
        deleteStatement.setString(1, uuid)
        deleteStatement.addBatch()
    }

    /**
     * @param eventArgument the [EventArgument] to use for this operation
     * @param connection the [Connection] to use for this operation
     * @param uuid the uuid to target for record deletion
     */
    private fun deleteRecordByCompositeKeyInternal(eventArgument: EventArgument, connection: Connection, uuid: String) {
        val deleteStatement = eventArgument.getOrAddStatement(connection, deleteByCompositeKeySql)
        deleteStatement.setString(1, uuid)
        deleteStatement.addBatch()
    }

    /**
     * @param eventArgument the [EventArgument] to use for this operation
     * @param connection the [Connection] to use for this operation
     * @param uuid the uuid to target for record deletion
     */
    private fun deleteRecordByUuidInternal(eventArgument: EventArgument, connection: Connection, uuid: String) {
        val deleteStatement = eventArgument.getOrAddStatement(connection, deleteByUuidSql)
        deleteStatement.setString(1, uuid)
        deleteStatement.addBatch()
    }

    /**
     * @param jdsDb an instance of [JdsDb], used determine SQL types based on implementation
     * @param pool a shared connection pool to quickly query the target schemas
     */
    @Throws(Exception::class)
    internal fun forceGenerateOrUpdateSchema(jdsDb: JdsDb, pool: HashMap<Int, Connection>) = try {

        if (!pool.containsKey(targetConnection))
            pool[targetConnection] = jdsDb.getConnection(targetConnection)

        val connection = pool[targetConnection]!!

        val tableFields = JdsField.values.filter { fields.contains(it.value.id) }.map { it.value }
        val createColumnsSql = JdsSchema.generateColumns(jdsDb, tableFields, columnToFieldMap, enumOrdinals)
        if (!jdsDb.doesTableExist(connection, name)) {
            connection.prepareStatement(JdsSchema.generateTable(jdsDb, name)).use {
                it.executeUpdate()
                if (jdsDb.options.isPrintingOutput)
                    println("Created $name")
            }
        }

        val insertColumns = LinkedList<String>()
        val delimiter = when (jdsDb.implementation) {
            JdsImplementation.TSQL, JdsImplementation.ORACLE -> " "
            else -> "ADD COLUMN "
        }
        val prefix = when (jdsDb.implementation) {
            JdsImplementation.TSQL -> "ADD "
            JdsImplementation.ORACLE -> "ADD ("
            else -> "ADD COLUMN "
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
        }

        if (foundChanges) {
            if (jdsDb.supportsStatements) {
                val addNewColumnsSql = String.format(jdsDb.getDbAddColumnSyntax(), name, stringJoiner)
                connection.prepareStatement(addNewColumnsSql).use { it.executeUpdate() }

                val tableColumns = generateNativeColumnTypes(jdsDb, columnToFieldMap)
                tableColumns["uuid"] = JdsSchema.getDbDataType(jdsDb, JdsFieldType.STRING, 128)
                tableColumns["edit_version"] = JdsSchema.getDbDataType(jdsDb, JdsFieldType.INT)
                val createOrAlteredProcedureSQL = jdsDb.createOrAlterProc(storedProcedureName, name, tableColumns, setOf("uuid", "edit_version"), tableColumns.isEmpty())
                connection.prepareStatement(createOrAlteredProcedureSQL).use { it.executeUpdate() }
            } else {
                //sqlite must alter columns once per entry
                sqliteJoiner.forEach {
                    val addNewColumnsSql = String.format(jdsDb.getDbAddColumnSyntax(), name, "ADD COLUMN $it")
                    connection.prepareStatement(addNewColumnsSql).use { it.executeUpdate() }
                }
            }
        }

        //deleteByCompositeKeySql = "DELETE FROM $name WHERE ${JdsSchema.compositeKeyColumn} IN %s"
        //deleteByParentUuidSql = "DELETE FROM $name WHERE composite_key IN (SELECT composite_key FROM jds_entity_overview WHERE parent_uuid IN %s)"
        //deleteByUuidSql = "DELETE FROM $name WHERE composite_key IN (SELECT composite_key FROM jds_entity_overview WHERE uuid IN %s)"

        /**
         * SELECT agent_code,agent_name,working_area,commission
        FROM agents
        WHERE exists
        (SELECT *
        FROM customer
        WHERE grade=3 AND agents.agent_code=customer.agent_code)
         */

        generatedOrUpdatedSchema = true
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    private fun generateNativeColumnTypes(jdsDb: JdsDb, columnToFieldMap: LinkedHashMap<String, JdsField>): LinkedHashMap<String, String> {
        val collection = LinkedHashMap<String, String>()
        columnToFieldMap.forEach { columnName, field ->
            if (!JdsSchema.isIgnoredType(field.type))
                collection[columnName] = JdsSchema.getDbDataType(jdsDb, field.type)
        }
        return collection
    }

    /**
     * Determine if this entity should have its properties persisted to this table
     * @param jdsDb an instance of [JdsDb][JdsDb], used to lookup mapped classes
     * @param entity a [JdsEntity][JdsEntity] that may have [JdsField's][JdsField] written to this table
     */
    private fun satisfiesConditions(jdsDb: JdsDb, entity: JdsEntity): Boolean {

        if (entity is IJdsTableFilter)
            if (!entity.satisfiesCondition(this))
                return false

        if (entities.isEmpty())
            return true //this means this crt applies to all entities i.e unfiltered

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