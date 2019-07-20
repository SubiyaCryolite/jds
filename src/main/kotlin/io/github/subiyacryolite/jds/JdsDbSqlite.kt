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

import io.github.subiyacryolite.jds.enums.JdsFieldType
import io.github.subiyacryolite.jds.enums.JdsImplementation
import java.sql.Connection

/**
 * The SQLite implementation of [io.github.subiyacryolite.jds.JdsDb]
 */
abstract class JdsDbSqlite : JdsDb(JdsImplementation.SQLITE, false) {

    override fun tableExists(connection: Connection, tableName: String): Int {
        val sql = "SELECT COUNT(name) AS Result FROM sqlite_master WHERE type='table' AND name=?;"
        return getResult(connection, sql, arrayOf(tableName))
    }

    override fun columnExists(connection: Connection, tableName: String, columnName: String): Int {
        val sql = "PRAGMA table_info('$tableName')"
        try {
            connection.prepareStatement(sql).use { statement ->
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        val column = resultSet.getString("name")
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

    override fun saveEntityLiveVersion() = "INSERT INTO jds_entity_live_version(uuid) VALUES(?) ON CONFLICT(uuid) DO NOTHING;"

    override fun saveString() = "INSERT INTO jds_str_text(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(uuid, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveBoolean() = "INSERT INTO jds_str_boolean(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(uuid, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveLong() = "INSERT INTO jds_str_long(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(uuid, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveDouble() = "INSERT INTO jds_str_double(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(uuid, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveFloat() = "INSERT INTO jds_str_float(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(uuid, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveShort() = "INSERT INTO jds_str_short(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(uuid, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveUuid() = "INSERT INTO jds_str_uuid(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(uuid, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveInteger() = "INSERT INTO jds_str_integer(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(uuid, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveDateTime() = "INSERT INTO jds_str_date_time(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(uuid, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveTime() = "INSERT INTO jds_str_time(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(uuid, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveBlob() = "INSERT INTO jds_str_blob(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(uuid, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveZonedDateTime() = "INSERT INTO jds_str_zoned_date_time(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(uuid, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveDate() = "INSERT INTO jds_str_date(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(uuid, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveEnum() = "INSERT INTO jds_str_enum(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(uuid, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveEnumString() = "INSERT INTO jds_str_enum_string(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(uuid, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveMonthDay() = "INSERT INTO jds_str_month_day(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(uuid, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveYearMonth() = "INSERT INTO jds_str_year_month(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(uuid, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun savePeriod() = "INSERT INTO jds_str_period(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(uuid, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveDuration() = "INSERT INTO jds_str_duration(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(uuid, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveEnumCollections() = "INSERT INTO jds_str_enum_col(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(uuid, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveEnumStringCollections() = "INSERT INTO jds_str_enum_string_col(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(uuid, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveDateTimeCollections() = "INSERT INTO jds_str_date_time_col(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(uuid, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveFloatCollections() = "INSERT INTO jds_str_float_col(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(uuid, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveIntegerCollections() = "INSERT INTO jds_str_integer_col(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(uuid, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveDoubleCollections() = "INSERT INTO jds_str_double_col(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(uuid, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveLongCollections() = "INSERT INTO jds_str_long_col(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(uuid, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveStringCollections() = "INSERT INTO jds_str_text_col(uuid, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(uuid, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveOverview() = "INSERT INTO jds_entity_overview(uuid, edit_version, entity_id) VALUES(?, ?, ?) ON CONFLICT(uuid, edit_version) DO UPDATE SET entity_id = EXCLUDED.entity_id"

    override fun saveEntityBindings() = "INSERT INTO jds_entity_binding(parent_uuid, parent_edit_version, child_uuid, child_edit_version, child_attribute_id) VALUES(?, ?, ?, ?, ?) ON CONFLICT(parent_uuid, parent_edit_version, child_uuid, child_edit_version) DO UPDATE SET child_attribute_id = EXCLUDED.child_attribute_id"

    override fun populateRefEntityField() = "INSERT INTO jds_ref_entity_field(entity_id, field_id) VALUES(?, ?) ON CONFLICT(entity_id, field_id) DO NOTHING"

    override fun populateRefField() = "INSERT INTO jds_ref_field(id, caption, description, type_ordinal) VALUES(?, ?, ?, ?) ON CONFLICT(id) DO UPDATE SET caption = EXCLUDED.caption, description = EXCLUDED.description, type_ordinal = EXCLUDED.type_ordinal"

    override fun populateRefEntityEnum() = "INSERT INTO jds_ref_entity_enum(entity_id, field_id) VALUES(?, ?) ON CONFLICT(entity_id, field_id) DO NOTHING"

    override fun populateRefEntity() = "INSERT INTO jds_ref_entity(id, name, caption, description) VALUES(?, ?, ?, ?) ON CONFLICT(id) DO UPDATE SET name = EXCLUDED.name, caption = EXCLUDED.caption, description = EXCLUDED.description"

    override fun populateRefEnum() = "INSERT INTO jds_ref_enum(field_id, seq, name, caption) VALUES(?, ?, ?, ?) ON CONFLICT(field_id, seq) DO UPDATE SET name = EXCLUDED.name, caption = EXCLUDED.caption"

    override fun mapParentToChild() = "INSERT INTO jds_ref_entity_inheritance(parent_entity_id, child_entity_id) VALUES(?, ?) ON CONFLICT(parent_entity_id, child_entity_id) DO NOTHING"

    override fun getDataTypeImpl(fieldType: JdsFieldType, max: Int): String = when (fieldType) {
        JdsFieldType.FLOAT -> "REAL"
        JdsFieldType.DOUBLE -> "DOUBLE"
        JdsFieldType.ZONED_DATE_TIME -> "BIGINT"
        JdsFieldType.TIME -> "INTEGER"
        JdsFieldType.BLOB -> "BLOB"
        JdsFieldType.INT -> "INTEGER"
        JdsFieldType.SHORT -> "INTEGER"
        JdsFieldType.UUID -> "TEXT"
        JdsFieldType.DATE -> "TIMESTAMP"
        JdsFieldType.DATE_TIME -> "TIMESTAMP"
        JdsFieldType.LONG -> "BIGINT"
        JdsFieldType.STRING -> "TEXT"
        JdsFieldType.BOOLEAN -> "BOOLEAN"
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
        return ""
    }
}