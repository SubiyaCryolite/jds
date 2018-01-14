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

    override fun createStoreEntityInheritance(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsEntityInstance.sql")
    }

    override fun createStoreBoolean(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsStoreBoolean.sql")
    }

    override fun createStoreText(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsStoreText.sql")
    }

    override fun createStoreDateTime(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsStoreDateTime.sql")
    }

    override fun createStoreZonedDateTime(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsStoreZonedDateTime.sql")
    }

    override fun createStoreInteger(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsStoreInteger.sql")
    }

    override fun createStoreFloat(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsStoreFloat.sql")
    }

    override fun createStoreDouble(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsStoreDouble.sql")
    }

    override fun createStoreLong(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsStoreLong.sql")
    }

    override fun createStoreTextArray(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsStoreTextArray.sql")
    }

    override fun createStoreDateTimeArray(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsStoreDateTimeArray.sql")
    }

    override fun createStoreIntegerArray(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsStoreIntegerArray.sql")
    }

    override fun createStoreFloatArray(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsStoreFloatArray.sql")
    }

    override fun createStoreDoubleArray(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsStoreDoubleArray.sql")
    }

    override fun createStoreLongArray(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsStoreLongArray.sql")
    }

    override fun createStoreEntities(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsRefEntity.sql")
    }

    override fun createRefEnumValues(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsRefEnum.sql")
    }

    override fun createRefFields(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsRefField.sql")
    }

    override fun createRefFieldTypes(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsRefFieldType.sql")
    }

    override fun createBindEntityFields(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsRefEntityField.sql")
    }

    override fun createBindEntityEnums(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsRefEntityEnum.sql")
    }

    override fun createRefEntityOverview(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsEntityoverview.sql")
    }

    override fun createRefOldFieldValues(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsStoreOldFieldValue.sql")
        executeSqlFromString(connection, "CREATE INDEX indx_jds_old_integer          ON jds_store_old_field_value (uuid, field_id, sequence, integer_value)")
        executeSqlFromString(connection, "CREATE INDEX indx_jds_old_float            ON jds_store_old_field_value (uuid, field_id, sequence, float_value)")
        executeSqlFromString(connection, "CREATE INDEX indx_jds_old_double           ON jds_store_old_field_value (uuid, field_id, sequence, double_value)")
        executeSqlFromString(connection, "CREATE INDEX indx_jds_old_long             ON jds_store_old_field_value (uuid, field_id, sequence, long_value)")
        executeSqlFromString(connection, "CREATE INDEX indx_jds_old_date_time_value        ON jds_store_old_field_value (uuid, field_id, sequence, date_time_value)")
        executeSqlFromString(connection, "CREATE INDEX indx_jds_old_time             ON jds_store_old_field_value (uuid, field_id, sequence, time_value)")
        executeSqlFromString(connection, "CREATE INDEX indx_jds_old_boolean          ON jds_store_old_field_value (uuid, field_id, sequence, boolean_value)")
        executeSqlFromString(connection, "CREATE INDEX indx_jds_old_zoned_date_time  ON jds_store_old_field_value (uuid, field_id, sequence, zoned_date_time_value)")
        executeSqlFromString(connection, "CREATE INDEX indx_jds_old_blob_text        ON jds_store_old_field_value (uuid, field_id, sequence)")
    }

    override fun createStoreEntityBinding(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsEntityBinding.sql")
    }

    override fun createStoreTime(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsStoreTime.sql")
    }

    override fun createStoreBlob(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsStoreBlob.sql")
    }

    override fun createRefInheritance(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsRefEntityInheritance.sql")
    }

    override fun saveString(): String {
        return "INSERT OR REPLACE INTO jds_store_text(uuid, field_id, value) VALUES(:uuid, :fieldId, :value)"
    }

    override fun saveBoolean(): String {
        return "INSERT OR REPLACE INTO jds_store_boolean(uuid, field_id, value) VALUES(:uuid, :fieldId, :value)"
    }

    override fun saveLong(): String {
        return "INSERT OR REPLACE INTO jds_store_long(uuid, field_id, value) VALUES(:uuid, :fieldId, :value)"
    }

    override fun saveDouble(): String {
        return "INSERT OR REPLACE INTO jds_store_double(uuid, field_id, value) VALUES(:uuid, :fieldId, :value)"
    }

    override fun saveFloat(): String {
        return "INSERT OR REPLACE INTO jds_store_float(uuid, field_id, value) VALUES(:uuid, :fieldId, :value)"
    }

    override fun saveInteger(): String {
        return "INSERT OR REPLACE INTO jds_store_integer(uuid, field_id, value) VALUES(:uuid, :fieldId, :value)"
    }

    override fun saveDateTime(): String {
        return "INSERT OR REPLACE INTO jds_store_date_time(uuid, field_id, value) VALUES(:uuid, :fieldId, :value)"
    }

    override fun saveTime(): String {
        return "INSERT OR REPLACE INTO jds_store_time(uuid, field_id, value) VALUES(:uuid, :fieldId, :value)"
    }

    override fun saveBlob(): String {
        return "INSERT OR REPLACE INTO jds_store_blob(uuid, field_id, value) VALUES(:uuid, :fieldId, :value)"
    }

    override fun saveZonedDateTime(): String {
        return "INSERT OR REPLACE INTO jds_store_zoned_date_time(uuid, field_id, value) VALUES(:uuid, :fieldId, :value)"
    }

    override fun saveOverview(): String {
        return "INSERT OR REPLACE INTO jds_entity_overview(uuid, date_created, date_modified, live, version) VALUES(:uuid, :dateCreated, :dateModified, :live, :version)"
    }

    override fun saveOverviewInheritance(): String {
        return "INSERT OR REPLACE INTO jds_entity_instance(entity_uuid, entity_id) VALUES(:uuid, :entityId)"
    }

    override fun mapClassFields(): String {
        return "INSERT OR REPLACE INTO jds_ref_entity_field(entity_id, field_id) VALUES(:entityId, :fieldId)"
    }

    override fun mapFieldName(): String {
        return "INSERT OR REPLACE INTO jds_ref_field(id, caption, description, type_ordinal) VALUES(:fieldId, :fieldName, :fieldDescription, :typeOrdinal)"
    }

    override fun mapClassEnumsImplementation(): String {
        return "INSERT OR REPLACE INTO jds_ref_entity_enum(entity_id, field_id) VALUES(?,?)"
    }

    override fun mapClassName(): String {
        return "INSERT OR REPLACE INTO jds_ref_entity(id, caption) VALUES(?,?)"
    }

    override fun mapEnumValues(): String {
        return "INSERT OR REPLACE INTO jds_ref_enum(field_id, seq, caption) VALUES(?,?,?)"
    }

    override fun mapParentToChild(): String {
        return "INSERT OR REPLACE INTO jds_ref_entity_inheritance(parent_entity_id, child_entity_id) VALUES(?,?)"
    }

    override fun getDbAddColumnSyntax(): String {
        return "ALTER TABLE %s ADD COLUMN %s %s"
    }

    override fun getDbFloatDataType(): String {
        return "REAL"
    }

    override fun getDbDoubleDataType(): String {
        return "DOUBLE"
    }

    override fun getDbZonedDateTimeDataType(): String {
        return "BIGINT"
    }

    override fun getDbTimeDataType(): String {
        return "INTEGER"
    }

    override fun getDbBlobDataType(max: Int): String {
        return "BLOB"
    }

    override fun getDbIntegerDataType(): String {
        return "INTEGER"
    }

    override fun getDbDateTimeDataType(): String {
        return "TIMESTAMP"
    }

    override fun getDbLongDataType(): String {
        return "BIGINT"
    }

    override fun getDbStringDataType(max: Int): String {
        return "TEXT"
    }

    override fun getDbBooleanDataType(): String {
        return "BOOLEAN"
    }
}