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
package io.github.subiyacryolite.jds.context

import io.github.subiyacryolite.jds.enums.FieldType
import io.github.subiyacryolite.jds.enums.Implementation
import io.github.subiyacryolite.jds.enums.Procedure
import io.github.subiyacryolite.jds.enums.Table
import java.sql.Connection
import java.util.*

/**
 * The PostgreSQL implementation of [io.github.subiyacryolite.jds.DbContext]
 */
abstract class PostGreSqlContext : DbContext(Implementation.PostGreSql, true, "jds.", "jds") {

    override fun prepareImplementation(connection: Connection) {
        createSchemaIfNotExists(connection)
    }

    private fun createSchemaIfNotExists(connection: Connection) {
        if (!doesSchemaExist(connection)) {
            createSchema(connection)
        }
    }

    private fun doesSchemaExist(connection: Connection): Boolean {
        connection.prepareStatement("SELECT EXISTS(SELECT meta.schema_name FROM information_schema.schemata meta WHERE meta.schema_name = ? AND meta.catalog_name=?)").use { statement ->
            statement.setString(1, schema)
            statement.setString(2, connection.catalog)
            statement.executeQuery().use { resultSet ->
                while (resultSet.next()) {
                    return resultSet.getBoolean(1)
                }
            }
        }
        return false
    }

    private fun createSchema(connection: Connection) {
        connection.prepareStatement("CREATE SCHEMA $schema").use { statement ->
            statement.executeUpdate()
        }
        connection.prepareStatement("COMMENT ON SCHEMA $schema IS 'Holds all jds DB objects';").use { statement ->
            statement.executeUpdate()
        }
    }

    override fun tableExists(connection: Connection, table: Table): Int {
        return tableExists(connection, table.component)
    }

    override fun tableExists(connection: Connection, tableName: String): Int {
        val sql = "SELECT COUNT(*) AS Result FROM information_schema.tables WHERE table_catalog = ? AND table_schema = ? AND table_name = ?"
        return getResult(connection, sql, arrayOf(connection.catalog, schema, tableName))
    }

    override fun procedureExists(connection: Connection, procedure: Procedure): Int {
        val sql = "select COUNT(*) AS Result from information_schema.routines where routine_catalog = ? AND routine_schema = ? AND routine_name = ?"
        return getResult(connection, sql, arrayOf(connection.catalog, schema, procedure.component))
    }

    override fun columnExists(connection: Connection, tableName: String, columnName: String): Int {
        val sql = "SELECT COUNT(COLUMN_NAME) AS Result FROM information_schema.columns WHERE table_catalog = ? AND table_schema = ? AND table_name = ? AND column_name = ?"
        return getResult(connection, sql, arrayOf(connection.catalog, schema, tableName, columnName))
    }

    override fun getDataTypeImpl(fieldType: FieldType, max: Int): String = when (fieldType) {
        FieldType.Float -> "REAL"
        FieldType.Double -> "FLOAT"
        FieldType.ZonedDateTime -> "TIMESTAMP WITH TIME ZONE"
        FieldType.Time -> "TIME WITHOUT TIME ZONE"
        FieldType.Blob -> "BYTEA"
        FieldType.Int -> "INTEGER"
        FieldType.Short -> "SMALLINT"
        FieldType.Uuid -> "UUID"
        FieldType.Date -> "DATE"
        FieldType.DateTime -> "TIMESTAMP WITHOUT TIME ZONE"
        FieldType.Long -> "BIGINT"
        FieldType.String -> if (max == 0) "TEXT" else "VARCHAR($max)"
        FieldType.Boolean -> "BOOLEAN"
        else -> ""
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
        columns.forEach { (column, type) -> inputParameters.add("p_$column $type") }
        sqlBuilder.append(inputParameters)
        sqlBuilder.append(")\n")

        sqlBuilder.append("\tRETURNS VOID AS \$\$\n")
        sqlBuilder.append("BEGIN\n")

        sqlBuilder.append("\tINSERT INTO $tableName(")
        val columnNames = StringJoiner(", ")
        columns.forEach { (column, _) -> columnNames.add(column) }
        sqlBuilder.append(columnNames)
        sqlBuilder.append(")\n")

        sqlBuilder.append("\tVALUES")
        sqlBuilder.append("(")
        val parameterNames = StringJoiner(", ")
        columns.forEach { (column, _) -> parameterNames.add("p_$column") }
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
            columns.forEach { (column, _) ->
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
