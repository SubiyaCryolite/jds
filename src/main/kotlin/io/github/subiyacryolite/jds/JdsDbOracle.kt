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
import io.github.subiyacryolite.jds.enums.JdsComponent
import io.github.subiyacryolite.jds.enums.JdsComponentType
import io.github.subiyacryolite.jds.enums.JdsImplementation
import java.sql.Connection
import java.util.*

/**
 * Created by ifunga on 14/07/2017.
 */
abstract class JdsDbOracle : JdsDb(JdsImplementation.ORACLE, true) {

    override fun tableExists(connection: Connection, tableName: String): Int {
        var toReturn = 0
        val sql = "SELECT COUNT(*) AS Result FROM all_objects WHERE object_type IN ('TABLE') AND object_name = ?"
        try {
            connection.prepareStatement(sql).use {
                it.setString(1, tableName.toUpperCase())
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
        val sql = "SELECT COUNT(*) AS Result FROM all_objects WHERE object_type IN ('VIEW') AND object_name = ?"
        try {
            connection.prepareStatement(sql).use {
                it.setString(1, viewName.toUpperCase())
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
        val sql = "SELECT COUNT(*) AS Result FROM all_objects WHERE object_type IN ('PROCEDURE') AND object_name = ?"
        try {
            connection.prepareStatement(sql).use {
                it.setString(1, procedureName.toUpperCase())
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

    override fun columnExists(connection: Connection, tableName: String, columnName: String): Int {
        var toReturn = 0
        val sql = "SELECT COUNT(COLUMN_NAME) AS Result FROM ALL_TAB_COLUMNS WHERE TABLE_NAME = :tableName AND COLUMN_NAME = :columnName"
        try {
            NamedPreparedStatement(connection, sql).use {
                it.setString("tableName", tableName.toUpperCase())
                it.setString("columnName", columnName.toUpperCase())
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

    override fun createStoreEntities(connection: Connection) {
        executeSqlFromFile(connection, "sql/oracle/jds_ref_entity.sql")
    }

    override fun createRefEnumValues(connection: Connection) {
        executeSqlFromFile(connection, "sql/oracle/jds_ref_enum.sql")
    }

    override fun createRefFields(connection: Connection) {
        executeSqlFromFile(connection, "sql/oracle/jds_ref_field.sql")
    }

    override fun createRefFieldTypes(connection: Connection) {
        executeSqlFromFile(connection, "sql/oracle/jds_ref_field_type.sql")
    }

    override fun createBindEntityFields(connection: Connection) {
        executeSqlFromFile(connection, "sql/oracle/jds_ref_entity_field.sql")
    }

    override fun createBindEntityEnums(connection: Connection) {
        executeSqlFromFile(connection, "sql/oracle/jds_ref_entity_enum.sql")
    }

    override fun createRefEntityOverview(connection: Connection) {
        executeSqlFromFile(connection, "sql/oracle/jds_entity_overview.sql")
    }

    override fun createBindEntityBinding(connection: Connection) {
        executeSqlFromFile(connection, "sql/oracle/jds_entity_binding.sql")
    }

    override fun createRefInheritance(connection: Connection) {
        executeSqlFromFile(connection, "sql/oracle/jds_ref_entity_inheritance.sql")
    }

    override fun prepareCustomDatabaseComponents(connection: Connection) {
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_STORE_BOOLEAN)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_STORE_BLOB)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_STORE_TEXT)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_STORE_LONG)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_STORE_INTEGER)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_STORE_FLOAT)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_STORE_DOUBLE)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_STORE_DATE_TIME)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_STORE_TIME)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_STORE_DATE)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_STORE_DURATION)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_STORE_PERIOD)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_STORE_MONTH_YEAR)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_STORE_MONTH_DAY)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_STORE_YEAR_MONTH)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_STORE_ENUM)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_STORE_ZONED_DATE_TIME)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_ENTITY_OVERVIEW)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_ENTITY_BINDING)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_REF_ENTITY_FIELD)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_REF_ENTITY_ENUM)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_REF_ENTITY)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_REF_ENUM)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_REF_FIELD)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_REF_ENTITY_INHERITANCE)
    }

    override fun prepareCustomDatabaseComponents(connection: Connection, jdsComponent: JdsComponent) {
        when (jdsComponent) {
            JdsComponent.POP_ENTITY_BINDING -> executeSqlFromString(connection, createPopJdsEntityBinding())
            JdsComponent.POP_ENTITY_OVERVIEW -> executeSqlFromString(connection, createPopJdsEntityOverview())
            JdsComponent.PROC_REF_ENTITY -> executeSqlFromString(connection, createPopJdsRefEntity())
            JdsComponent.PROC_REF_ENTITY_ENUM -> executeSqlFromString(connection, createPopJdsRefEntityEnum())
            JdsComponent.PROC_REF_ENTITY_FIELD -> executeSqlFromString(connection, createPopJdsRefEntityField())
            JdsComponent.PROC_REF_ENTITY_INHERITANCE -> executeSqlFromString(connection, createPopJdsRefEntityInheritance())
            JdsComponent.PROC_REF_ENUM -> executeSqlFromString(connection, createPopJdsRefEnum())
            JdsComponent.PROC_REF_FIELD -> executeSqlFromString(connection, createPopJdsRefField())
            JdsComponent.PROC_STORE_BLOB -> executeSqlFromString(connection, createPopJdsStoreBlob())
            JdsComponent.PROC_STORE_BOOLEAN -> executeSqlFromString(connection, createPopJdsStoreBoolean())
            JdsComponent.PROC_STORE_DATE -> executeSqlFromString(connection, createPopJdsStoreDate())
            JdsComponent.PROC_STORE_DATE_TIME -> executeSqlFromString(connection, createPopJdsStoreDateTime())
            JdsComponent.PROC_STORE_DOUBLE -> executeSqlFromString(connection, createPopJdsStoreDouble())
            JdsComponent.PROC_STORE_DURATION -> executeSqlFromString(connection, createPopJdsStoreDuration())
            JdsComponent.PROC_STORE_ENUM -> executeSqlFromString(connection, createPopJdsStoreEnum())
            JdsComponent.PROC_STORE_FLOAT -> executeSqlFromString(connection, createPopJdsStoreFloat())
            JdsComponent.PROC_STORE_INTEGER -> executeSqlFromString(connection, createPopJdsStoreInteger())
            JdsComponent.PROC_STORE_LONG -> executeSqlFromString(connection, createPopJdsStoreLong())
            JdsComponent.PROC_STORE_MONTH_YEAR -> executeSqlFromString(connection, createPopJdsMonthYear())
            JdsComponent.PROC_STORE_MONTH_DAY -> executeSqlFromString(connection, createPopJdsMonthDay())
            JdsComponent.PROC_STORE_PERIOD -> executeSqlFromString(connection, createPopJdsStorePeriod())
            JdsComponent.PROC_STORE_TEXT -> executeSqlFromString(connection, createPopJdsStoreText())
            JdsComponent.PROC_STORE_TIME -> executeSqlFromString(connection, createPopJdsStoreTime())
            JdsComponent.PROC_STORE_YEAR_MONTH -> executeSqlFromString(connection, createPopJdsYearMonth())
            JdsComponent.PROC_STORE_ZONED_DATE_TIME -> executeSqlFromString(connection, createPopJdsStoreZonedDateTime())
            else -> {
            }
        }
    }

    override fun getNativeDataTypeFloat(): String {
        return "BINARY_FLOAT"
    }

    override fun getNativeDataTypeDouble(): String {
        return "BINARY_DOUBLE"
    }

    override fun getNativeDataTypeZonedDateTime(): String {
        return "TIMESTAMP WITH TIME ZONE"
    }

    override fun getNativeDataTypeTime(): String {
        return "NUMBER(19)"
    }

    override fun getNativeDataTypeBlob(max: Int): String {
        return "BLOB"
    }

    override fun getNativeDataTypeInteger(): String {
        return "NUMBER(10)"
    }

    override fun getNativeDataTypeDate(): String{
        return "DATE"
    }

    override fun getNativeDataTypeDateTime(): String {
        return "TIMESTAMP"
    }

    override fun getNativeDataTypeLong(): String {
        return "NUMBER(19)"
    }

    override fun getNativeDataTypeString(max: Int): String {
        return if (max == 0) "NCLOB" else "NVARCHAR2($max)"
    }

    override fun getNativeDataTypeBoolean(): String {
        return "SMALLINT"
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

        sqlBuilder.append("CREATE OR ALTER PROCEDURE $procedureName(")
        val inputParameters = StringJoiner(", ")
        columns.forEach { column, type -> inputParameters.add("p_$column IN $type") }
        sqlBuilder.append(inputParameters)
        sqlBuilder.append(")\n")

        sqlBuilder.append("\t AS\n")
        sqlBuilder.append("\t\tBEGIN\n")
        sqlBuilder.append("\t\t\tMERGE  INTO $tableName dest\n")

        sqlBuilder.append("\t\t\tUSING DUAL\n")

        sqlBuilder.append("\t\t\tON (")
        val comparisonColumns = StringJoiner(" AND ")
        uniqueColumns.forEach { column -> comparisonColumns.add("$column = p_$column") }
        sqlBuilder.append(comparisonColumns)
        sqlBuilder.append(")\n")

        sqlBuilder.append("\t\t\tWHEN NOT MATCHED THEN INSERT (")
        val notMatchedColumns = StringJoiner(", ")
        columns.forEach { column, _ -> notMatchedColumns.add(column) }
        sqlBuilder.append(notMatchedColumns)
        sqlBuilder.append(")")

        sqlBuilder.append(" VALUES(")
        val notMatchedColumnsSrc = StringJoiner(", ")
        columns.forEach { column, _ -> notMatchedColumnsSrc.add("src.$column") }
        sqlBuilder.append(notMatchedColumnsSrc)
        sqlBuilder.append(")\n")

        if (!doNothingOnConflict) {
            sqlBuilder.append("\t\t\tWHEN MATCHED THEN UPDATE SET ")
            val comparisonColumns = StringJoiner(", ")
            columns.forEach { column, _ ->
                if (!uniqueColumns.contains(column)) //dont update unique columns
                    comparisonColumns.add("dest.$column = src.$column")
            }
            sqlBuilder.append(comparisonColumns)
        }
        sqlBuilder.append("\t\t\t;\n")
        sqlBuilder.append("\t\tEND $procedureName")
        return sqlBuilder.toString()
    }
}
