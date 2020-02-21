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
import java.sql.Connection

/**
 * The SQLite implementation of [io.github.subiyacryolite.jds.DbContext]
 */
abstract class SqLiteDbContext : DbContext(Implementation.SqLite, false) {

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

    override fun saveEntityLiveVersion() = "INSERT INTO ${objectPrefix}entity_live_version(id) VALUES(?) ON CONFLICT(id) DO NOTHING;"

    override fun saveString() = "INSERT INTO ${objectPrefix}str_text(id, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(id, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveBoolean() = "INSERT INTO ${objectPrefix}str_boolean(id, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(id, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveLong() = "INSERT INTO ${objectPrefix}str_long(id, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(id, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveDouble() = "INSERT INTO ${objectPrefix}str_double(id, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(id, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveFloat() = "INSERT INTO ${objectPrefix}str_float(id, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(id, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveShort() = "INSERT INTO ${objectPrefix}str_short(id, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(id, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveUuid() = "INSERT INTO ${objectPrefix}str_uuid(id, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(id, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveInteger() = "INSERT INTO ${objectPrefix}str_integer(id, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(id, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveDateTime() = "INSERT INTO ${objectPrefix}str_date_time(id, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(id, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveTime() = "INSERT INTO ${objectPrefix}str_time(id, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(id, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveBlob() = "INSERT INTO ${objectPrefix}str_blob(id, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(id, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveZonedDateTime() = "INSERT INTO ${objectPrefix}str_zoned_date_time(id, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(id, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveDate() = "INSERT INTO ${objectPrefix}str_date(id, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(id, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveEnum() = "INSERT INTO ${objectPrefix}str_enum(id, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(id, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveEnumString() = "INSERT INTO ${objectPrefix}str_enum_string(id, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(id, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveMonthDay() = "INSERT INTO ${objectPrefix}str_month_day(id, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(id, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveYearMonth() = "INSERT INTO ${objectPrefix}str_year_month(id, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(id, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun savePeriod() = "INSERT INTO ${objectPrefix}str_period(id, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(id, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveDuration() = "INSERT INTO ${objectPrefix}str_duration(id, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(id, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveEnumCollections() = "INSERT INTO ${objectPrefix}str_enum_col(id, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(id, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveEnumStringCollections() = "INSERT INTO ${objectPrefix}str_enum_string_col(id, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(id, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveDateTimeCollections() = "INSERT INTO ${objectPrefix}str_date_time_col(id, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(id, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveFloatCollections() = "INSERT INTO ${objectPrefix}str_float_col(id, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(id, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveIntegerCollections() = "INSERT INTO ${objectPrefix}str_integer_col(id, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(id, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveDoubleCollections() = "INSERT INTO ${objectPrefix}str_double_col(id, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(id, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveLongCollections() = "INSERT INTO ${objectPrefix}str_long_col(id, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(id, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveStringCollections() = "INSERT INTO ${objectPrefix}str_text_col(id, edit_version, field_id, value) VALUES(?, ?, ?, ?) ON CONFLICT(id, edit_version, field_id) DO UPDATE SET value = EXCLUDED.value;"

    override fun saveOverview() = "INSERT INTO ${objectPrefix}entity_overview(id, edit_version, entity_id) VALUES(?, ?, ?) ON CONFLICT(id, edit_version) DO UPDATE SET entity_id = EXCLUDED.entity_id"

    override fun saveEntityBindings() = "INSERT INTO ${objectPrefix}entity_binding(parent_id, parent_edit_version, child_id, child_edit_version, child_attribute_id) VALUES(?, ?, ?, ?, ?) ON CONFLICT(parent_id, parent_edit_version, child_id, child_edit_version) DO UPDATE SET child_attribute_id = EXCLUDED.child_attribute_id"

    override fun populateRefEntityField() = "INSERT INTO ${objectPrefix}ref_entity_field(entity_id, field_id) VALUES(?, ?) ON CONFLICT(entity_id, field_id) DO NOTHING"

    override fun populateRefFieldEntity() = "INSERT INTO ${objectPrefix}ref_field_entity(field_id, entity_id) VALUES(?, ?) ON CONFLICT(field_id, entity_id) DO NOTHING"

    override fun populateRefField() = "INSERT INTO ${objectPrefix}ref_field(id, caption, description, field_type_ordinal) VALUES(?, ?, ?, ?) ON CONFLICT(id) DO UPDATE SET caption = EXCLUDED.caption, description = EXCLUDED.description, field_type_ordinal = EXCLUDED.field_type_ordinal"

    override fun populateRefEntityEnum() = "INSERT INTO ${objectPrefix}ref_entity_enum(entity_id, field_id) VALUES(?, ?) ON CONFLICT(entity_id, field_id) DO NOTHING"

    override fun populateRefEntity() = "INSERT INTO ${objectPrefix}ref_entity(id, name, caption, description) VALUES(?, ?, ?, ?) ON CONFLICT(id) DO UPDATE SET name = EXCLUDED.name, caption = EXCLUDED.caption, description = EXCLUDED.description"

    override fun populateRefEnum() = "INSERT INTO ${objectPrefix}ref_enum(field_id, seq, name, caption) VALUES(?, ?, ?, ?) ON CONFLICT(field_id, seq) DO UPDATE SET name = EXCLUDED.name, caption = EXCLUDED.caption"

    override fun populateFieldDictionary() = "INSERT INTO ${objectPrefix}field_dictionary(entity_id, field_id, property_name) VALUES(?, ?, ?) ON CONFLICT(entity_id, field_id) DO UPDATE SET property_name = EXCLUDED.property_name"

    override fun mapParentToChild() = "INSERT INTO ${objectPrefix}ref_entity_inheritance(parent_entity_id, child_entity_id) VALUES(?, ?) ON CONFLICT(parent_entity_id, child_entity_id) DO NOTHING"

    override fun getDataTypeImpl(fieldType: FieldType, max: Int): String = when (fieldType) {
        FieldType.Float -> "REAL"
        FieldType.Double -> "DOUBLE"
        FieldType.ZonedDateTime -> "BIGINT"
        FieldType.Time -> "INTEGER"
        FieldType.Blob -> "BLOB"
        FieldType.Int -> "INTEGER"
        FieldType.Short -> "INTEGER"
        FieldType.Uuid -> "TEXT"
        FieldType.Date -> "TIMESTAMP"
        FieldType.DateTime -> "TIMESTAMP"
        FieldType.Long -> "BIGINT"
        FieldType.String -> "TEXT"
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
        return ""
    }
}