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
 * The Oracle implementation of [io.github.subiyacryolite.jds.DbContext]
 */
abstract class OracleContext : DbContext(Implementation.Oracle, true) {

    override fun tableExists(connection: Connection, table: Table): Int {
        return tableExists(connection, "$objectPrefix${table.component}")
    }

    override fun tableExists(connection: Connection, tableName: String): Int {
        val sql = "SELECT COUNT(*) AS Result FROM all_objects WHERE object_type IN ('TABLE') AND object_name = ?"
        return getResult(connection, sql, arrayOf(connection.catalog, tableName))
    }

    override fun procedureExists(connection: Connection, procedure: Procedure): Int {
        val sql = "SELECT COUNT(*) AS Result FROM all_objects WHERE object_type IN ('PROCEDURE') AND object_name = ?"
        return getResult(connection, sql, arrayOf("$objectPrefix${procedure.component}"))
    }

    override fun columnExists(connection: Connection, tableName: String, columnName: String): Int {
        val sql = "SELECT COUNT(COLUMN_NAME) AS Result FROM ALL_TAB_COLUMNS WHERE TABLE_NAME = ? AND COLUMN_NAME = ?"
        return getResult(connection, sql, arrayOf("$objectPrefix$tableName", columnName))
    }

    override fun getDataTypeImpl(fieldType: FieldType, max: Int): String = when (fieldType) {
        FieldType.Float -> "BINARY_FLOAT"
        FieldType.Double -> "BINARY_DOUBLE"
        FieldType.ZonedDateTime -> "TIMESTAMP WITH TIME ZONE"
        FieldType.Time -> "NUMBER(19)"
        FieldType.Blob -> "BLOB"
        FieldType.Int -> "NUMBER(10)"
        FieldType.Short -> "NUMBER(5)"
        FieldType.Uuid -> "RAW(16)"
        FieldType.Date -> "DATE"
        FieldType.DateTime -> "TIMESTAMP"
        FieldType.Long -> "NUMBER(19)"
        FieldType.String -> if (max == 0) "NCLOB" else "NVARCHAR2($max)"
        FieldType.Boolean -> "NUMBER(1,0)"
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

        sqlBuilder.append("CREATE OR REPLACE PROCEDURE $procedureName(")
        val inputParameters = StringJoiner(", ")
        columns.forEach { (column, type) ->
            var shortType = type
            val containsBrackets = type.contains("(")
            if (containsBrackets)
                shortType = shortType.substring(0, shortType.indexOf('('))
            inputParameters.add("p_$column IN $shortType")
        }
        sqlBuilder.append(inputParameters)
        sqlBuilder.append(")\n")

        sqlBuilder.append("\t AS\n")
        sqlBuilder.append("\t\tBEGIN\n")
        sqlBuilder.append("\t\t\tMERGE  INTO $tableName dest\n")

        sqlBuilder.append("\t\t\tUSING DUAL ON (")
        val comparisonColumns = StringJoiner(" AND ")
        uniqueColumns.forEach { column -> comparisonColumns.add("$column = p_$column") }
        sqlBuilder.append(comparisonColumns)
        sqlBuilder.append(")\n")

        sqlBuilder.append("\t\t\tWHEN NOT MATCHED THEN INSERT (")
        val notMatchedColumns = StringJoiner(", ")
        columns.forEach { (column, _) -> notMatchedColumns.add(column) }
        sqlBuilder.append(notMatchedColumns)
        sqlBuilder.append(")")

        sqlBuilder.append(" VALUES(")
        val notMatchedColumnsSrc = StringJoiner(", ")
        columns.forEach { (column, _) -> notMatchedColumnsSrc.add("p_$column") }
        sqlBuilder.append(notMatchedColumnsSrc)
        sqlBuilder.append(")\n")

        if (!doNothingOnConflict && columns.count() > uniqueColumns.count()) {
            sqlBuilder.append("\t\t\tWHEN MATCHED THEN UPDATE SET ")
            val updateColumns = StringJoiner(", ")
            columns.forEach { (column, _) ->
                if (!uniqueColumns.contains(column)) //dont update unique columns
                    updateColumns.add("$column = p_$column")
            }
            sqlBuilder.append(updateColumns)
        }
        sqlBuilder.append("\t\t\t;\n")
        sqlBuilder.append("\t\tEND $procedureName;")
        return sqlBuilder.toString()
    }
}
