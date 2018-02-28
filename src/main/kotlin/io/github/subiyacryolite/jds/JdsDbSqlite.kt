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

    override fun createStoreBoolean(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/jds_store_boolean.sql")
    }

    override fun createStoreText(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/jds_store_text.sql")
    }

    override fun createStoreDateTime(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/jds_store_date_time.sql")
    }

    override fun createStoreZonedDateTime(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/jds_store_zoned_date_time.sql")
    }

    override fun createStoreInteger(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/jds_store_integer.sql")
    }

    override fun createStoreFloat(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/jds_store_float.sql")
    }

    override fun createStoreDouble(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/jds_store_double.sql")
    }

    override fun createStoreLong(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/jds_store_long.sql")
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

    override fun createRefEntityOverviewLight(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/jds_entity_overview_light.sql")
    }

    override fun createStoreTime(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/jds_store_time.sql")
    }

    override fun createStoreBlob(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/jds_store_blob.sql")
    }

    override fun createRefInheritance(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/jds_ref_entity_inheritance.sql")
    }

    override fun saveString(): String {
        return "INSERT OR REPLACE INTO jds_store_text(composite_key, field_id, sequence, value) VALUES(:uuid, :fieldId, :sequence, :value)"
    }

    override fun saveBoolean(): String {
        return "INSERT OR REPLACE INTO jds_store_boolean(composite_key, field_id, value) VALUES(:uuid, :fieldId, :value)"
    }

    override fun saveLong(): String {
        return "INSERT OR REPLACE INTO jds_store_long(composite_key, field_id, sequence, value) VALUES(:uuid, :fieldId, :sequence, :value)"
    }

    override fun saveDouble(): String {
        return "INSERT OR REPLACE INTO jds_store_double(composite_key, field_id, sequence, value) VALUES(:uuid, :fieldId, :sequence, :value)"
    }

    override fun saveFloat(): String {
        return "INSERT OR REPLACE INTO jds_store_float(composite_key, field_id, sequence, value) VALUES(:uuid, :fieldId, :sequence, :value)"
    }

    override fun saveInteger(): String {
        return "INSERT OR REPLACE INTO jds_store_integer(composite_key, field_id, sequence, value) VALUES(:uuid, :fieldId, :sequence, :value)"
    }

    override fun saveDateTime(): String {
        return "INSERT OR REPLACE INTO jds_store_date_time(composite_key, field_id, sequence, value) VALUES(:uuid, :fieldId, :sequence, :value)"
    }

    override fun saveTime(): String {
        return "INSERT OR REPLACE INTO jds_store_time(composite_key, field_id, value) VALUES(:uuid, :fieldId, :value)"
    }

    override fun saveBlob(): String {
        return "INSERT OR REPLACE INTO jds_store_blob(composite_key, field_id, value) VALUES(:uuid, :fieldId, :value)"
    }

    override fun saveZonedDateTime(): String {
        return "INSERT OR REPLACE INTO jds_store_zoned_date_time(composite_key, field_id, value) VALUES(:uuid, :fieldId, :value)"
    }

    override fun saveOverview(): String {
        ////:compositeKey, :uuid, :uuidLocation, :uuidLocationVersion, :parentUuid, :parentCompositeKey, :entityId, :live, :entityVersion, :lastEdit
        return "INSERT OR REPLACE INTO jds_entity_overview(composite_key, uuid, uuid_location, uuid_location_version, parent_uuid, parent_composite_key, entity_id, live, entity_version, last_edit) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
    }

    override fun populateRefEntityField(): String {
        return "INSERT OR REPLACE INTO jds_ref_entity_field(entity_id, field_id) VALUES(?, ?)"
    }

    override fun populateRefField(): String {
        return "INSERT OR REPLACE INTO jds_ref_field(id, caption, description, type_ordinal) VALUES(:fieldId, :fieldName, :fieldDescription, :typeOrdinal)"
    }

    override fun populateRefEntityEnum(): String {
        return "INSERT OR REPLACE INTO jds_ref_entity_enum(entity_id, field_id) VALUES(?,?)"
    }

    override fun populateRefEntity(): String {
        return "INSERT OR REPLACE INTO jds_ref_entity(id, caption, caption, description, parent) VALUES(?,?,?,?,?)"
    }

    override fun populateRefEnum(): String {
        return "INSERT OR REPLACE INTO jds_ref_enum(field_id, seq, caption) VALUES(?,?,?)"
    }

    override fun mapParentToChild(): String {
        return "INSERT OR REPLACE INTO jds_ref_entity_inheritance(parent_entity_id, child_entity_id) VALUES(?,?)"
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

    override fun getDbCreateIndexSyntax(tableName: String, columnName: String, indexName: String): String {
        return "CREATE INDEX $indexName ON $tableName($columnName);"
    }
}