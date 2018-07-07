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

import io.github.subiyacryolite.jds.JdsExtensions.setLocalDate
import io.github.subiyacryolite.jds.JdsExtensions.setLocalTime
import io.github.subiyacryolite.jds.JdsExtensions.setZonedDateTime
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation
import io.github.subiyacryolite.jds.enums.JdsFieldType
import io.github.subiyacryolite.jds.enums.JdsImplementation
import io.github.subiyacryolite.jds.events.EventArgument
import java.io.Serializable
import java.sql.Connection
import java.sql.Timestamp
import java.time.*
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashMap


open class JdsTable() : Serializable {

    var name = ""
    var entities = HashSet<Long>()
    var fields = HashSet<Long>()
    var isStoringLiveRecordsOnly = true
    private val columnToFieldMap = LinkedHashMap<String, JdsField>()
    private val enumOrdinals = HashMap<String, Int>()
    private val columnNames = LinkedList<String>()
    private var generatedOrUpdatedSchema = false
    private val deleteByUuidSql: String
        get() = "DELETE FROM $name WHERE uuid = ?"
    private val storedProcedureName: String
        get() {
            val stringJoiner = StringJoiner("_")
            entities.forEach { stringJoiner.add(it.toString()) }
            return "jds_str_${name}_$stringJoiner"
        }

    /**
     * @param entity
     */
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
     * Method used to register [JdsField's][JdsField] that shall be persisted to a [JdsTable][JdsTable]
     * @param fieldId a numeric value corresponding to a [JdsField][JdsField] which's value shall be persisted to this[JdsTable]
     */
    fun registerField(fieldId: Long) {
        fields.add(fieldId)
    }

    /**
     * Method used to register [JdsField's][JdsField] that shall be persisted to a [JdsTable][JdsTable]
     * @param jdsField a [JdsField][JdsField] which's value shall be persisted to this[JdsTable]
     */
    fun registerField(jdsField: JdsField) {
        registerField(jdsField.id)
    }

    /**
     * Method used to register [JdsField's][JdsField] that shall be persisted to a [JdsTable][JdsTable]
     * @param jdsFields an array of [JdsField's][JdsField] whose values shall be persisted to this[JdsTable]
     */
    fun registerFields(vararg jdsFields: JdsField) {
        jdsFields.forEach { registerField(it) }
    }

    /**
     *
     * @param jdsDb an instance of JdsDb, used to lookup mapped classes and determine SQL types based on implementation
     * @param entity a [JdsEntity][JdsEntity] that may have [JdsField'S][JdsField] persisted to this [JdsTable]
     * @param eventArgument The [EventArgument][EventArgument] that will hold batched SQL queries for execution
     * @throws Exception General IO errors
     */
    @Throws(Exception::class)
    fun executeSave(jdsDb: JdsDb, entity: JdsEntity, eventArgument: EventArgument) {
        if (!generatedOrUpdatedSchema)
            throw ExceptionInInitializerError("You must call forceGenerateOrUpdateSchema() before you can persist this table: $name")
        val satisfied = satisfiesConditions(jdsDb, entity)
        if (satisfied) {
            //prepare insert placeholder
            if (jdsDb.isSqLiteDb) {
                val stmt = eventArgument.getOrAddStatement("INSERT OR REPLACE INTO $name(uuid, edit_version) VALUES(?,  ?)")
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
            if (!jdsDb.isSqLiteDb) {
                joiner.add("?")//last parameter for composite keys
                joiner.add("?")//last parameter for composite keys
            }

            val statementQuery = when (!jdsDb.isSqLiteDb) {
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

            val insertStatement = when (!jdsDb.isSqLiteDb) {
                true -> eventArgument.getOrAddCall(statementQuery)
                false -> eventArgument.getOrAddStatement(statementQuery)
            }

            updateValues.forEachIndexed { index, value ->
                when (value) {
                    is ZonedDateTime? -> insertStatement.setZonedDateTime(index + 1, value, jdsDb)
                    is LocalTime? -> insertStatement.setLocalTime(index + 1, value, jdsDb)
                    is LocalDateTime? -> insertStatement.setTimestamp(index + 1, Timestamp.valueOf(value))
                    is LocalDate? -> insertStatement.setLocalDate(index + 1, value, jdsDb)
                    is Duration? -> insertStatement.setObject(index + 1, value?.toNanos())
                    is MonthDay? -> insertStatement.setString(index + 1, value.toString())
                    is YearMonth? -> insertStatement.setString(index + 1, value.toString())
                    is Period? -> insertStatement.setString(index + 1, value.toString())
                    is Boolean? -> {
                        if (jdsDb.isOracleDb && value != null)
                            insertStatement.setBoolean(index + 1, value)
                        else
                            insertStatement.setObject(index + 1, value)
                    }
                    else -> insertStatement.setObject(index + 1, value)
                }
            }
            insertStatement.setString(columnNames.size + 1, entity.overview.uuid)
            insertStatement.setInt(columnNames.size + 2, entity.overview.editVersion)
            insertStatement.addBatch()
        }
    }

    /**
     * @param jdsDb an instance of [JdsDb], used determine SQL types based on implementation
     * @param pool a shared connection pool to quickly query the target schemas
     */
    @Throws(Exception::class)
    internal fun forceGenerateOrUpdateSchema(jdsDb: JdsDb, connection: Connection) = try {

        val tableFields = JdsField.values.filter { fields.contains(it.value.id) }.map { it.value }
        val createColumnsSql = JdsSchema.generateColumns(jdsDb, tableFields, columnToFieldMap, enumOrdinals)
        if (!jdsDb.doesTableExist(connection, name)) {
            connection.prepareStatement(JdsSchema.generateTable(jdsDb, name)).use {
                it.executeUpdate()
                if (jdsDb.options.isLoggingOutput)
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
            if (jdsDb.isSqLiteDb) {
                //sqlite must alter columns once per entry
                sqliteJoiner.forEach { connection.prepareStatement(String.format(jdsDb.getDbAddColumnSyntax(), name, "ADD COLUMN $it")).use { it.executeUpdate() } }
            } else {
                val addNewColumnsSql = String.format(jdsDb.getDbAddColumnSyntax(), name, stringJoiner)
                connection.prepareStatement(addNewColumnsSql).use { it.executeUpdate() }
            }
        }

        if (jdsDb.supportsStatements && (!jdsDb.doesProcedureExist(connection, storedProcedureName) || foundChanges)) {
            val tableColumns = generateNativeColumnTypes(jdsDb, columnToFieldMap)
            tableColumns["uuid"] = JdsSchema.getDbDataType(jdsDb, JdsFieldType.STRING, 128)
            tableColumns["edit_version"] = JdsSchema.getDbDataType(jdsDb, JdsFieldType.INT)

            //MySQL && MariaDB don't support CREATE OR ALTER. Old definitions must be dropped first
            if (setOf(JdsImplementation.MARIADB, JdsImplementation.MYSQL).contains(jdsDb.implementation))
                connection.prepareStatement("DROP PROCEDURE IF EXISTS $storedProcedureName").use { it.executeUpdate() }

            val createOrAlteredProcedureSQL = jdsDb.createOrAlterProc(storedProcedureName, name, tableColumns, setOf("uuid", "edit_version"), tableColumns.count() <= 2)
            connection.prepareStatement(createOrAlteredProcedureSQL).use { it.executeUpdate() }
        }
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
    private fun deleteRecordByUuidInternal(eventArgument: EventArgument, connection: Connection, uuid: String) {
        val deleteStatement = eventArgument.getOrAddStatement(connection, deleteByUuidSql)
        deleteStatement.setString(1, uuid)
        deleteStatement.addBatch()
    }

    /**
     * Generate SQL to delete records that are not longer represent the most recent state of the database
     * @implNote Use the recommended style for each DB Engine to ensure optimal performance
     */
    internal fun deleteOldRecords(jdsDb: JdsDb) = when (jdsDb.implementation) {
        JdsImplementation.TSQL -> {
            when (jdsDb.options.isWritingLatestEntityVersion) {
                true -> "DELETE $name FROM $name report_table\n" +
                        "INNER JOIN jds_entity_live_version live_records\n" +
                        "ON live_records.uuid = report_table.uuid\n" +
                        "AND live_records.edit_version = report_table.edit_version\n" +
                        "WHERE live_records.uuid IS NULL"
                false -> "DELETE $name FROM $name report_table\n" +
                        "  LEFT JOIN (SELECT\n" +
                        "                MAX(eo.edit_version) AS edit_version,\n" +
                        "                eo.uuid\n" +
                        "              FROM ${jdsDb.dimensionTable} eo\n" +
                        "              GROUP BY eo.uuid) live_records\n" +
                        "    ON live_records.uuid = report_table.uuid AND live_records.edit_version = report_table.edit_version\n" +
                        "WHERE live_records.uuid IS NULL"
            }
        }
        JdsImplementation.POSTGRES -> {
            when (jdsDb.options.isWritingLatestEntityVersion) {
                true -> "DELETE FROM $name AS report_table\n" +
                        "WHERE NOT EXISTS ( SELECT * from jds_entity_live_version AS live_records\n" +
                        "WHERE report_table.edit_version = live_records.edit_version\n" +
                        "AND report_table.uuid = live_records.uuid)"
                false -> "DELETE FROM $name AS report_table\n" +
                        "WHERE NOT EXISTS(SELECT\n" +
                        "                   MAX(eo.edit_version) AS edit_version,\n" +
                        "                   eo.uuid\n" +
                        "                 FROM ${jdsDb.dimensionTable} eo\n" +
                        "                 WHERE eo.uuid = report_table.uuid AND eo.edit_version = report_table.edit_version\n" +
                        "                 GROUP BY eo.uuid)"
            }
        }
        JdsImplementation.MARIADB, JdsImplementation.MYSQL -> {
            when (jdsDb.options.isWritingLatestEntityVersion) {
                true -> "DELETE report_table FROM $name report_table\n" +
                        "LEFT JOIN jds_entity_live_version live_records ON live_records.uuid = report_table.uuid\n" +
                        "AND live_records.edit_version = report_table.edit_version\n" +
                        "WHERE live_records.uuid IS NULL"
                false -> "DELETE report_table FROM $name report_table\n" +
                        "  LEFT JOIN (SELECT\n" +
                        "               MAX(eo.edit_version) AS edit_version,\n" +
                        "               eo.uuid\n" +
                        "             FROM ${jdsDb.dimensionTable} eo\n" +
                        "             GROUP BY eo.uuid) live_records ON live_records.uuid = report_table.uuid\n" +
                        "                                               AND live_records.edit_version = report_table.edit_version\n" +
                        "WHERE live_records.uuid IS NULL"
            }
        }
        JdsImplementation.ORACLE, JdsImplementation.SQLITE -> {
            when (jdsDb.options.isWritingLatestEntityVersion) {
                true -> "DELETE FROM $name\n" +
                        "WHERE NOT EXISTS(SELECT *\n" +
                        "FROM jds_entity_live_version\n" +
                        "WHERE $name.uuid = jds_entity_live_version.uuid AND\n" +
                        "$name.edit_version = jds_entity_live_version.edit_version)"
                false -> "DELETE FROM $name\n" +
                        "WHERE NOT EXISTS(SELECT\n" +
                        "                   MAX(eo.edit_version) AS edit_version,\n" +
                        "                   eo.uuid\n" +
                        "                 FROM ${jdsDb.dimensionTable} eo\n" +
                        "                 WHERE eo.uuid = $name.uuid AND eo.edit_version = $name.edit_version\n" +
                        "                 GROUP BY eo.uuid)"
            }
        }
    }


    companion object {

        private const val serialVersionUID = 20171109_0853L
    }
}