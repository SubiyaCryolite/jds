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

import com.javaworld.NamedPreparedStatement
import io.github.subiyacryolite.jds.enums.JdsImplementation
import java.sql.Connection
import java.util.*

/**
 * The MySQL implementation of [JdsDataBase][JdsDb]
 */
abstract class JdsDbMySql : JdsDb {

    constructor(implementation: JdsImplementation, supportsStatements: Boolean) : super(implementation, supportsStatements)

    constructor() : this(JdsImplementation.MYSQL, true)

    override fun tableExists(connection: Connection, tableName: String): Int {
        try {
            NamedPreparedStatement(connection, "SELECT 1 AS Result FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = :tableSchema AND TABLE_NAME = :tableName").use {
                it.setString("tableName", tableName)
                it.setString("tableSchema", connection.catalog)
                it.executeQuery().use {
                    while (it.next())
                        return it.getInt("Result")
                }
            }
            return 0
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
            return 0
        }
    }

    override fun procedureExists(connection: Connection, procedureName: String): Int {
        try {
            NamedPreparedStatement(connection, "SELECT 1 AS Result FROM INFORMATION_SCHEMA.ROUTINES WHERE ROUTINE_SCHEMA = :procedureSchema AND ROUTINE_TYPE='PROCEDURE' AND ROUTINE_NAME = :procedureName").use {
                it.setString("procedureName", procedureName)
                it.setString("procedureSchema", connection.catalog)
                it.executeQuery().use {
                    while (it.next())
                        return it.getInt("Result")
                }
            }
            return 0
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
            return 0
        }
    }

    override fun viewExists(connection: Connection, viewName: String): Int {
        try {
            NamedPreparedStatement(connection, "SELECT 1 AS Result FROM INFORMATION_SCHEMA.VIEWS WHERE TABLE_SCHEMA = :viewSchema AND TABLE_NAME = :viewName").use {
                it.setString("viewName", viewName)
                it.setString("viewSchema", connection.catalog)
                it.executeQuery().use {
                    while (it.next())
                        return it.getInt("Result")
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
        return 0
    }

    override fun columnExists(connection: Connection, tableName: String, columnName: String): Int {
        return columnExistsCommonImpl(connection, tableName, columnName, "SELECT 1 AS Result FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = :tableCatalog AND TABLE_NAME = :tableName AND COLUMN_NAME = :columnName")
    }

    override fun createStoreEntities(connection: Connection) {
        executeSqlFromFile(connection, "sql/mysql/jds_ref_entity.sql")
    }

    override fun createRefEnumValues(connection: Connection) {
        executeSqlFromFile(connection, "sql/mysql/jds_ref_enum.sql")
    }

    override fun createRefFields(connection: Connection) {
        executeSqlFromFile(connection, "sql/mysql/jds_ref_field.sql")
    }

    override fun createRefFieldTypes(connection: Connection) {
        executeSqlFromFile(connection, "sql/mysql/jds_ref_field_type.sql")
    }

    override fun createBindEntityFields(connection: Connection) {
        executeSqlFromFile(connection, "sql/mysql/jds_ref_entity_field.sql")
    }

    override fun createBindEntityEnums(connection: Connection) {
        executeSqlFromFile(connection, "sql/mysql/jds_ref_entity_enum.sql")
    }

    override fun createRefEntityOverview(connection: Connection) {
        executeSqlFromFile(connection, "sql/mysql/jds_entity_overview.sql")
    }

    override fun createBindEntityBinding(connection: Connection) {
        executeSqlFromFile(connection, "sql/mysql/jds_entity_binding.sql")
    }

    override fun createRefInheritance(connection: Connection) {
        executeSqlFromFile(connection, "sql/mysql/jds_ref_entity_inheritance.sql")
    }

    override fun getNativeDataTypeFloat(): String {
        return "FLOAT"
    }

    override fun getNativeDataTypeDouble(): String {
        return "DOUBLE"
    }

    override fun getNativeDataTypeZonedDateTime(): String {
        return "TIMESTAMP"
    }

    override fun getNativeDataTypeTime(): String {
        return "TIME"
    }

    override fun getNativeDataTypeBlob(max: Int): String {
        return "BLOB"
    }

    override fun getNativeDataTypeInteger(): String {
        return "INT"
    }

    override fun getNativeDataTypeDate(): String {
        return "DATE"
    }

    override fun getNativeDataTypeDateTime(): String {
        return "DATETIME"
    }

    override fun getNativeDataTypeLong(): String {
        return "BIGINT"
    }

    override fun getNativeDataTypeString(max: Int): String {
        return if (max == 0)
            "TEXT"
        else
            "VARCHAR($max)"
    }

    override fun getNativeDataTypeBoolean(): String {
        return "BOOLEAN"
    }

    override fun getDbCreateIndexSyntax(tableName: String, columnName: String, indexName: String): String {
        return "CREATE INDEX $indexName ON $tableName($columnName);"
    }

    override fun createOrAlterProc(procedureName: String,
                                   tableName: String,
                                   columns: Map<String, String>,
                                   uniqueColumns: Collection<String>,
                                   doNothingOnConflict: Boolean): String {
        val sqlBuilder = StringBuilder()

        sqlBuilder.append("CREATE PROCEDURE $procedureName(")
        val inputParameters = StringJoiner(", ")
        columns.forEach { column, type -> inputParameters.add("IN p_$column $type") }
        sqlBuilder.append(inputParameters)
        sqlBuilder.append(")\n")

        sqlBuilder.append("BEGIN\n")
        if (doNothingOnConflict || (columns.count() == uniqueColumns.count()))
            sqlBuilder.append("\tINSERT IGNORE INTO $tableName(")
        else
            sqlBuilder.append("\tINSERT INTO $tableName(")
        val columnNames = StringJoiner(", ")
        columns.forEach { column, _ -> columnNames.add(column) }
        sqlBuilder.append(columnNames)
        sqlBuilder.append(")\n")

        sqlBuilder.append("\tVALUES")
        sqlBuilder.append("(")
        val parameterNames = StringJoiner(", ")
        columns.forEach { column, _ -> parameterNames.add("p_$column") }
        sqlBuilder.append(parameterNames)
        sqlBuilder.append(")\n")

        if (!doNothingOnConflict && columns.count() > uniqueColumns.count()) {
            sqlBuilder.append("\tON DUPLICATE KEY ")
            sqlBuilder.append("UPDATE ")
            val updateStatements = StringJoiner(", ")
            columns.forEach { column, _ ->
                if (!uniqueColumns.contains(column))//dont update unique columns
                    updateStatements.add("$column = p_$column")
            }
            sqlBuilder.append(updateStatements)
        }

        sqlBuilder.append(";\n")

        sqlBuilder.append("END;\n")
        return sqlBuilder.toString()
    }
}