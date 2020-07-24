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
 * The MySQL implementation of [io.github.subiyacryolite.jds.context.DbContext]
 */
abstract class MySqlContext : DbContext {

    constructor(implementation: Implementation, supportsStatements: Boolean) : super(implementation, supportsStatements)

    constructor() : this(Implementation.MySql, true)

    override fun tableExists(connection: Connection, table: Table): Int {
        return tableExists(connection, "$objectPrefix${table.table}")
    }

    override fun tableExists(connection: Connection, tableName: String): Int {
        val sql = "SELECT 1 AS Result FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?"
        return getResult(connection, sql, arrayOf(connection.catalog, tableName))
    }

    override fun procedureExists(connection: Connection, procedure: Procedure): Int {
        val sql = "SELECT 1 AS Result FROM INFORMATION_SCHEMA.ROUTINES WHERE ROUTINE_SCHEMA = ? AND ROUTINE_TYPE='PROCEDURE' AND ROUTINE_NAME = ?"
        return getResult(connection, sql, arrayOf(connection.catalog, "$objectPrefix${procedure.procedure}"))
    }

    override fun columnExists(connection: Connection, tableName: String, columnName: String): Int {
        val sql = "SELECT 1 AS Result FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND COLUMN_NAME = ?"
        return getResult(connection, sql, arrayOf(connection.catalog, "$objectPrefix$tableName", columnName))
    }

    override fun getDataTypeImpl(fieldType: FieldType, max: Int): String = when (fieldType) {
        FieldType.Float -> "FLOAT"
        FieldType.Double -> "DOUBLE"
        FieldType.ZonedDateTime -> "TIMESTAMP"
        FieldType.Time -> "TIME"
        FieldType.Blob -> "BLOB"
        FieldType.Int -> "INT"
        FieldType.Date -> "DATE"
        FieldType.DateTime -> "DATETIME"
        FieldType.Short -> "SMALLINT"
        FieldType.Uuid -> "BINARY(16)"
        FieldType.Long -> "BIGINT"
        FieldType.String -> if (max == 0) "TEXT" else "VARCHAR($max)"
        FieldType.Boolean -> "BOOLEAN"
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

        sqlBuilder.append("CREATE PROCEDURE $procedureName(")
        val inputParameters = StringJoiner(", ")
        columns.forEach { (column, type) -> inputParameters.add("IN p_$column $type") }
        sqlBuilder.append(inputParameters)
        sqlBuilder.append(")\n")

        sqlBuilder.append("BEGIN\n")
        if (doNothingOnConflict || (columns.count() == uniqueColumns.count()))
            sqlBuilder.append("\tINSERT IGNORE INTO $tableName(")
        else
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

        if (!doNothingOnConflict && columns.count() > uniqueColumns.count()) {
            sqlBuilder.append("\tON DUPLICATE KEY ")
            sqlBuilder.append("UPDATE ")
            val updateStatements = StringJoiner(", ")
            columns.forEach { (column, _) ->
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