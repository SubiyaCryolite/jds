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

import io.github.subiyacryolite.jds.enums.JdsImplementation
import java.sql.Connection
import java.util.*

/**
 * The PostgreSQL implementation of [JdsDataBase][JdsDb]
 */
abstract class JdsDbPostgreSql : JdsDb(JdsImplementation.POSTGRES, true) {
    override fun tableExists(connection: Connection, tableName: String): Int {
        var toReturn = 0
        val sql = "SELECT COUNT(*) AS Result FROM information_schema.tables WHERE table_catalog = ? AND table_name = ?"
        try {
            connection.prepareStatement(sql).use {
                it.setString(1, connection.catalog)
                it.setString(2, tableName.toLowerCase())
                it.executeQuery().use {
                    while (it.next())
                        toReturn = it.getInt("Result")
                }
            }
        } catch (ex: Exception) {
            toReturn = 0
            ex.printStackTrace(System.err)
        }
        return toReturn
    }

    override fun procedureExists(connection: Connection, procedureName: String): Int {
        var toReturn = 0
        val sql = "select COUNT(*) AS Result from information_schema.routines where routine_catalog = ? and routine_name = ?"
        try {
            connection.prepareStatement(sql).use {
                it.setString(1, connection.catalog)
                it.setString(2, procedureName.toLowerCase())
                it.executeQuery().use {
                    while (it.next())
                        toReturn = it.getInt("Result")
                }
            }
        } catch (ex: Exception) {
            toReturn = 0
            ex.printStackTrace(System.err)
        }
        return toReturn
    }

    override fun viewExists(connection: Connection, viewName: String): Int {
        var toReturn = 0
        val sql = "select COUNT(*) AS Result from information_schema.views where table_catalog = ? and table_name = ?"
        try {
            connection.prepareStatement(sql).use {
                it.setString(1, connection.catalog)
                it.setString(2, viewName.toLowerCase())
                it.executeQuery().use {
                    while (it.next()) {
                        toReturn = it.getInt("Result")
                    }
                }
            }
        } catch (ex: Exception) {
            toReturn = 0
            ex.printStackTrace(System.err)
        }
        return toReturn
    }

    override fun columnExists(connection: Connection, tableName: String, columnName: String): Int {
        val sql = "SELECT COUNT(COLUMN_NAME) AS Result FROM information_schema.COLUMNS WHERE TABLE_CATALOG = :tableCatalog AND TABLE_NAME = :tableName AND COLUMN_NAME = :columnName"
        return  columnExistsCommonImpl(connection, tableName.toLowerCase(), columnName.toLowerCase(),  sql)
    }

    override fun createStoreEntities(connection: Connection) {
        executeSqlFromFile(connection, "sql/postgresql/jds_ref_entity.sql")
    }

    override fun createRefEnumValues(connection: Connection) {
        executeSqlFromFile(connection, "sql/postgresql/jds_ref_enum.sql")
    }

    override fun createRefFields(connection: Connection) {
        executeSqlFromFile(connection, "sql/postgresql/jds_ref_field.sql")
    }

    override fun createRefFieldTypes(connection: Connection) {
        executeSqlFromFile(connection, "sql/postgresql/jds_ref_field_type.sql")
    }

    override fun createBindEntityFields(connection: Connection) {
        executeSqlFromFile(connection, "sql/postgresql/jds_ref_entity_field.sql")
    }

    override fun createBindEntityEnums(connection: Connection) {
        executeSqlFromFile(connection, "sql/postgresql/jds_ref_entity_enum.sql")
    }

    override fun createRefEntityOverview(connection: Connection) {
        executeSqlFromFile(connection, "sql/postgresql/jds_entity_overview.sql")
    }

    override fun createBindEntityBinding(connection: Connection) {
        executeSqlFromFile(connection, "sql/postgresql/jds_entity_binding.sql")
    }

    override fun createRefInheritance(connection: Connection) {
        executeSqlFromFile(connection, "sql/postgresql/jds_ref_entity_inheritance.sql")
    }

    override fun getNativeDataTypeFloat(): String {
        return "REAL"
    }

    override fun getNativeDataTypeDouble(): String {
        return "FLOAT"
    }

    override fun getNativeDataTypeZonedDateTime(): String {
        return "TIMESTAMP WITH TIME ZONE"
    }

    override fun getNativeDataTypeTime(): String {
        return "TIME WITHOUT TIME ZONE"
    }

    override fun getNativeDataTypeBlob(max: Int): String {
        return "BYTEA"
    }

    override fun getNativeDataTypeInteger(): String {
        return "INTEGER"
    }

    override fun getNativeDataTypeDate(): String {
        return "DATE"
    }

    override fun getNativeDataTypeDateTime(): String {
        return "TIMESTAMP WITHOUT TIME ZONE"
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

        sqlBuilder.append("CREATE OR REPLACE FUNCTION $procedureName(")
        val inputParameters = StringJoiner(", ")
        columns.forEach { column, type -> inputParameters.add("p_$column $type") }
        sqlBuilder.append(inputParameters)
        sqlBuilder.append(")\n")

        sqlBuilder.append("\tRETURNS VOID AS \$\$\n")
        sqlBuilder.append("BEGIN\n")

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

        sqlBuilder.append("\tON CONFLICT (")
        val uniqueConstraints = StringJoiner(", ")
        uniqueColumns.forEach { uniqueConstraints.add(it) }
        sqlBuilder.append(uniqueConstraints)
        sqlBuilder.append(")\n")

        if (doNothingOnConflict || (columns.count() == uniqueColumns.count())) {
            sqlBuilder.append("\t\tDO NOTHING;\n")
        } else {
            sqlBuilder.append("\t\tDO UPDATE SET ")
            val updateStatements = StringJoiner(", ")
            columns.forEach { column, _ ->
                if (!uniqueColumns.contains(column))//dont update unique columns
                    updateStatements.add("$column = p_$column")
            }
            sqlBuilder.append(updateStatements)
            sqlBuilder.append(";\n")
        }

        sqlBuilder.append("END;\n")
        sqlBuilder.append("\$\$\n")
        sqlBuilder.append("LANGUAGE plpgsql;")
        return sqlBuilder.toString()
    }
}
