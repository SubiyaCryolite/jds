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
 * The TSQL implementation of [io.github.subiyacryolite.jds.context.DbContext]
 */
abstract class TransactionalSqlContext : DbContext(Implementation.TSql, true, "jds.", "jds") {

    override fun prepareImplementation(connection: Connection) {
        createSchemaIfNotExists(connection)
    }

    private fun createSchemaIfNotExists(connection: Connection) {
        if (!doesSchemaExist(connection)) {
            createSchema(connection)
        }
    }

    private fun doesSchemaExist(connection: Connection): Boolean {
        connection.prepareStatement("SELECT COUNT(schema_name) AS Result FROM information_schema.schemata WHERE schema_name = ? AND catalog_name= ?").use { statement ->
            statement.setString(1, schema)
            statement.setString(2, connection.catalog)
            statement.executeQuery().use { resultSet ->
                while (resultSet.next()) {
                    return resultSet.getInt(1) > 0
                }
            }
        }
        return false
    }

    private fun createSchema(connection: Connection) {
        connection.prepareStatement("CREATE SCHEMA $schema").use { statement ->
            statement.executeUpdate()
        }
    }

    override fun tableExists(connection: Connection, table: Table): Int {
        return tableExists(connection, table.table)
    }

    override fun tableExists(connection: Connection, tableName: String): Int {
        val sql = "SELECT COUNT(TABLE_NAME) AS Result FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_CATALOG = ? AND TABLE_SCHEMA = ? AND TABLE_NAME = ?"
        return getResult(connection, sql, arrayOf(connection.catalog, schema, tableName))
    }

    override fun procedureExists(connection: Connection, procedure: Procedure): Int {
        return procedureExists(connection, procedure.procedure)
    }

    override fun procedureExists(connection: Connection, procedureName: String): Int {
        val sql = "SELECT COUNT(ROUTINE_NAME) AS Result FROM information_schema.routines WHERE ROUTINE_CATALOG = ? and ROUTINE_SCHEMA = ? and ROUTINE_NAME = ?"
        return getResult(connection, sql, arrayOf(connection.catalog, schema, procedureName))
    }

    override fun columnExists(connection: Connection, tableName: String, columnName: String): Int {
        val sql = "SELECT COUNT(COLUMN_NAME) AS Result from INFORMATION_SCHEMA.columns WHERE TABLE_CATALOG = ? and TABLE_SCHEMA = ? AND TABLE_NAME = ? and COLUMN_NAME = ?"
        return getResult(connection, sql, arrayOf(connection.catalog, schema, tableName, columnName))
    }

    override fun getDataTypeImpl(fieldType: FieldType, max: Int): String = when (fieldType) {
        FieldType.Float -> "REAL"
        FieldType.Double -> "FLOAT"
        FieldType.ZonedDateTime -> "DATETIMEOFFSET(7)"
        FieldType.Time -> "TIME(7)"
        FieldType.Blob -> if (max == 0) "VARBINARY(MAX)" else "VARBINARY($max)"
        FieldType.Int -> "INTEGER"
        FieldType.Short -> "SMALLINT"
        FieldType.Uuid -> "UNIQUEIDENTIFIER"
        FieldType.Date -> "DATE"
        FieldType.DateTime -> "DATETIME"
        FieldType.Long -> "BIGINT"
        FieldType.String -> if (max == 0) "NVARCHAR(MAX)" else "NVARCHAR($max)"
        FieldType.Boolean -> "BIT"
        else -> ""
    }

    override fun getDbCreateIndexSyntax(tableName: String, columnName: String, indexName: String): String {
        return "CREATE INDEX $indexName ON $tableName($columnName);"
    }

    override fun createOrAlterProc(
            procedureName: String,
            tableName: String,
            columns: Map<String, String>,
            uniqueColumns: Collection<String>,
            doNothingOnConflict: Boolean
    ): String {
        val sqlBuilder = StringBuilder()

        sqlBuilder.append("CREATE OR ALTER PROCEDURE $procedureName(")
        val inputParameters = StringJoiner(", ")
        columns.forEach { (column, type) -> inputParameters.add("@p_$column $type") }
        sqlBuilder.append(inputParameters)
        sqlBuilder.append(")\n")

        sqlBuilder.append("\t AS\n")
        sqlBuilder.append("\t\tBEGIN\n")
        sqlBuilder.append("\t\t\tMERGE  $tableName AS dest\n")

        sqlBuilder.append("\t\t\tUSING (VALUES(")
        val parameterColumns = StringJoiner(", ")
        columns.forEach { (column, _) -> parameterColumns.add("@p_$column") }
        sqlBuilder.append(parameterColumns)
        sqlBuilder.append("))")

        sqlBuilder.append("AS src(")
        val targetColumns = StringJoiner(", ")
        columns.forEach { (column, _) -> targetColumns.add(column) }
        sqlBuilder.append(targetColumns)
        sqlBuilder.append(")\n")

        sqlBuilder.append("\t\t\tON (")
        val comparisonColumns = StringJoiner(" AND ")
        uniqueColumns.forEach { column -> comparisonColumns.add("src.$column = dest.$column") }
        sqlBuilder.append(comparisonColumns)
        sqlBuilder.append(")\n")

        sqlBuilder.append("\t\t\tWHEN NOT MATCHED THEN INSERT (")
        val notMatchedColumns = StringJoiner(", ")
        columns.forEach { (column, _) -> notMatchedColumns.add(column) }
        sqlBuilder.append(notMatchedColumns)
        sqlBuilder.append(")")

        sqlBuilder.append(" VALUES(")
        val notMatchedColumnsSrc = StringJoiner(", ")
        columns.forEach { (column, _) -> notMatchedColumnsSrc.add("src.$column") }
        sqlBuilder.append(notMatchedColumnsSrc)
        sqlBuilder.append(")\n")

        if (!doNothingOnConflict && columns.count() > uniqueColumns.count()) {
            sqlBuilder.append("\t\t\tWHEN MATCHED THEN UPDATE SET ")
            val updateColumns = StringJoiner(", ")
            columns.forEach { (column, _) ->
                if (!uniqueColumns.contains(column)) //dont update unique columns
                    updateColumns.add("dest.$column = src.$column")
            }
            sqlBuilder.append(updateColumns)
        }
        sqlBuilder.append("\t\t\t;\n")
        sqlBuilder.append("\t\tEND")
        return sqlBuilder.toString()
    }
}
