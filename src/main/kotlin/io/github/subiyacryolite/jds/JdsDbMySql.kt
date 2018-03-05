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
 * The MySQL implementation of [JdsDataBase][JdsDb]
 */
abstract class JdsDbMySql : JdsDb {

    constructor(implementation: JdsImplementation, supportsStatements: Boolean) : super(implementation, supportsStatements)

    constructor() : this(JdsImplementation.MYSQL, true)

    override fun tableExists(connection: Connection, tableName: String): Int {
        var toReturn = 0
        val sql = "SELECT 1 AS Result FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = :tableSchema AND TABLE_NAME = :tableName"
        try {
            NamedPreparedStatement(connection, sql).use {
                it.setString("tableName", tableName)
                it.setString("tableSchema", connection.catalog)
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
        val sql = "SELECT 1 AS Result FROM INFORMATION_SCHEMA.ROUTINES WHERE ROUTINE_SCHEMA = :procedureSchema AND ROUTINE_TYPE='PROCEDURE' AND ROUTINE_NAME = :procedureName"
        try {
            NamedPreparedStatement(connection, sql).use {
                it.setString("procedureName", procedureName)
                it.setString("procedureSchema", connection.catalog)
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
        val sql = "SELECT 1 AS Result FROM INFORMATION_SCHEMA.VIEWS WHERE TABLE_SCHEMA = :viewSchema AND TABLE_NAME = :viewName"
        try {
            NamedPreparedStatement(connection, sql).use {
                it.setString("viewName", viewName)
                it.setString("viewSchema", connection.catalog)
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
        val sql = "SELECT 1 AS Result FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = :tableCatalog AND TABLE_NAME = :tableName AND COLUMN_NAME = :columnName"
        return columnExistsCommonImpl(connection, tableName, columnName, 0, sql)
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
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_STORE_TEXT_COLLECTION)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_STORE_LONG_COLLECTION)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_STORE_INTEGER_COLLECTION)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_STORE_FLOAT_COLLECTION)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_STORE_DOUBLE_COLLECTION)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_STORE_DATE_TIME_COLLECTION)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_STORE_DATE)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_STORE_DURATION)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_STORE_PERIOD)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_STORE_MONTH_YEAR)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_STORE_YEAR_MONTH)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_STORE_ENUM)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_STORE_ENUM_COLLECTION)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.PROC_STORE_TIME)
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
            JdsComponent.PROC_STORE_DATE_TIME_COLLECTION -> executeSqlFromString(connection, createPopJdsStoreDateTimeCollection())
            JdsComponent.PROC_STORE_DOUBLE -> executeSqlFromString(connection, createPopJdsStoreDouble())
            JdsComponent.PROC_STORE_DOUBLE_COLLECTION -> executeSqlFromString(connection, createPopJdsStoreDoubleCollection())
            JdsComponent.PROC_STORE_DURATION -> executeSqlFromString(connection, createPopJdsStoreDuration())
            JdsComponent.PROC_STORE_ENUM -> executeSqlFromString(connection, createPopJdsStoreEnum())
            JdsComponent.PROC_STORE_ENUM_COLLECTION -> executeSqlFromString(connection, createPopJdsStoreEnumCollection())
            JdsComponent.PROC_STORE_FLOAT -> executeSqlFromString(connection, createPopJdsStoreFloat())
            JdsComponent.PROC_STORE_FLOAT_COLLECTION -> executeSqlFromString(connection, createPopJdsStoreFloatCollection())
            JdsComponent.PROC_STORE_INTEGER -> executeSqlFromString(connection, createPopJdsStoreInteger())
            JdsComponent.PROC_STORE_INTEGER_COLLECTION -> executeSqlFromString(connection, createPopJdsStoreIntegerCollection())
            JdsComponent.PROC_STORE_LONG -> executeSqlFromString(connection, createPopJdsStoreLong())
            JdsComponent.PROC_STORE_LONG_COLLECTION -> executeSqlFromString(connection, createPopJdsStoreLongCollection())
            JdsComponent.PROC_STORE_MONTH_YEAR -> executeSqlFromString(connection, createPopJdsMonthYear())
            JdsComponent.PROC_STORE_PERIOD -> executeSqlFromString(connection, createPopJdsStorePeriod())
            JdsComponent.PROC_STORE_TEXT -> executeSqlFromString(connection, createPopJdsStoreText())
            JdsComponent.PROC_STORE_TEXT_COLLECTION -> executeSqlFromString(connection, createPopJdsStoreTextCollection())
            JdsComponent.PROC_STORE_TIME -> executeSqlFromString(connection, createPopJdsStoreTime())
            JdsComponent.PROC_STORE_YEAR_MONTH -> executeSqlFromString(connection, createPopJdsYearMonth())
            JdsComponent.PROC_STORE_ZONED_DATE_TIME -> executeSqlFromString(connection, createPopJdsStoreZonedDateTime())
            else -> {
            }
        }
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
        return ""
    }

    override fun createTable(tableName: String,
                             columns: LinkedHashMap<String, String>,
                             uniqueColumns: LinkedHashMap<String, String>,
                             primaryKeys: LinkedHashMap<String, String>,
                             foreignKeys: LinkedHashMap<String, LinkedHashMap<String, String>>): String {
        return ""
    }
}