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

/**
 * The SQLite implementation of [JdsDataBase][JdsDb]
 */
abstract class JdsDbSqlite : JdsDb(JdsImplementation.SQLITE, false) {

    override fun tableExists(connection: Connection, tableName: String): Int {
        var toReturn = 0
        val sql = "SELECT COUNT(name) AS Result FROM sqlite_master WHERE type='table' AND name=?;"
        try {
            connection.prepareStatement(sql).use {
                it.setString(1, tableName)
                it.executeQuery().use {
                    while (it.next())
                        toReturn = it.getInt("Result")
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
        return toReturn
    }

    override fun columnExists(connection: Connection, tableName: String, columnName: String): Int {
        val sql = "PRAGMA table_info('$tableName')"
        try {
            NamedPreparedStatement(connection, sql).use {
                it.executeQuery().use {
                    while (it.next()) {
                        val column = it.getString("name")
                        if (column.equals(columnName, ignoreCase = true))
                            return 1 //does exist
                    }
                }
                return 0//doesn't exist
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
            return 0//doesn't exist
        }
    }

    override fun createStoreEntities(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/jds_ref_entity.sql")
    }

    override fun createRefEnumValues(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/jds_ref_enum.sql")
    }

    override fun createRefFields(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/jds_ref_field.sql")
    }

    override fun createRefFieldTypes(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/jds_ref_field_type.sql")
    }

    override fun createBindEntityFields(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/jds_ref_entity_field.sql")
    }

    override fun createBindEntityEnums(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/jds_ref_entity_enum.sql")
    }

    override fun createRefEntityOverview(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/jds_entity_overview.sql")
    }

    override fun createBindEntityBinding(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/jds_entity_binding.sql")
    }

    override fun createRefInheritance(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/jds_ref_entity_inheritance.sql")
    }

    override fun saveString() = "INSERT OR REPLACE INTO jds_store_text(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?)"

    override fun saveBoolean() = "INSERT OR REPLACE INTO jds_store_boolean(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?)"

    override fun saveLong() = "INSERT OR REPLACE INTO jds_store_long(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?)"

    override fun saveDouble() = "INSERT OR REPLACE INTO jds_store_double(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?)"

    override fun saveFloat() = "INSERT OR REPLACE INTO jds_store_float(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?)"

    override fun saveInteger() = "INSERT OR REPLACE INTO jds_store_integer(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?)"

    override fun saveDateTime() = "INSERT OR REPLACE INTO jds_store_date_time(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?)"

    override fun saveTime() = "INSERT OR REPLACE INTO jds_store_time(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?)"

    override fun saveBlob() = "INSERT OR REPLACE INTO jds_store_blob(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?)"

    override fun saveZonedDateTime() = "INSERT OR REPLACE INTO jds_store_zoned_date_time(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?)"

    override fun saveDate() = "INSERT OR REPLACE INTO jds_store_date(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?)"

    override fun saveEnum() = "INSERT OR REPLACE INTO jds_store_enum(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?)"

    override fun saveMonthDay() = "INSERT OR REPLACE INTO jds_store_month_day(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?)"

    override fun saveYearMonth() = "INSERT OR REPLACE INTO jds_store_year_month(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?)"

    override fun savePeriod() = "INSERT OR REPLACE INTO jds_store_period(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?)"

    override fun saveDuration() = "INSERT OR REPLACE INTO jds_store_duration(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?)"

    override fun saveOverview() = "INSERT OR REPLACE INTO jds_entity_overview(uuid, edit_version, entity_id, entity_version) VALUES(?, ?, ?, ?)"

    override fun saveEntityBindings() = "INSERT OR REPLACE INTO jds_entity_binding(parent_uuid, parent_edit_version, child_uuid, child_edit_version, child_attribute_id) VALUES(?, ?, ?, ?, ?)"

    override fun populateRefEntityField() = "INSERT OR REPLACE INTO jds_ref_entity_field(entity_id, field_id) VALUES(?, ?)"

    override fun populateRefField() = "INSERT OR REPLACE INTO jds_ref_field(id, caption, description, type_ordinal) VALUES(:fieldId, :fieldName, :fieldDescription, :typeOrdinal)"

    override fun populateRefEntityEnum() = "INSERT OR REPLACE INTO jds_ref_entity_enum(entity_id, field_id) VALUES(?, ?)"

    override fun populateRefEntity() = "INSERT OR REPLACE INTO jds_ref_entity(id, caption, caption, description, parent) VALUES(?, ?, ?, ?, ?)"

    override fun populateRefEnum() = "INSERT OR REPLACE INTO jds_ref_enum(field_id, seq, caption) VALUES(?,?,?)"

    override fun mapParentToChild() = "INSERT OR REPLACE INTO jds_ref_entity_inheritance(parent_entity_id, child_entity_id) VALUES(?, ?)"

    override fun getNativeDataTypeFloat() = "REAL"

    override fun getNativeDataTypeDouble() = "DOUBLE"

    override fun getNativeDataTypeZonedDateTime() = "BIGINT"

    override fun getNativeDataTypeTime() = "INTEGER"

    override fun getNativeDataTypeBlob(max: Int) = "BLOB"

    override fun getNativeDataTypeInteger() = "INTEGER"

    override fun getNativeDataTypeDate() = "TIMESTAMP"

    override fun getNativeDataTypeDateTime() = "TIMESTAMP"

    override fun getNativeDataTypeLong() = "BIGINT"

    override fun getNativeDataTypeString(max: Int) = "TEXT"

    override fun getNativeDataTypeBoolean() = "BOOLEAN"

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
}