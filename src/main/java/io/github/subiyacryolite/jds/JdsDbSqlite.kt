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
abstract class JdsDbSqlite : JdsDb {

    constructor() {
        implementation = JdsImplementation.SQLITE
        supportsStatements = false
    }

    override fun tableExists(connection: Connection, tableName: String): Int {
        var toReturn = 0
        val sql = "SELECT COUNT(name) AS Result FROM sqlite_master WHERE type='table' AND name=?;"
        try {
            connection.prepareStatement(sql).use { preparedStatement ->
                preparedStatement.setString(1, tableName)
                preparedStatement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        toReturn = resultSet.getInt("Result")
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
        return toReturn
    }

    override fun columnExists(connection: Connection, tableName: String, columnName: String): Int {
        val sql = String.format("PRAGMA table_info('%s')", tableName)
        try {
            NamedPreparedStatement(connection, sql).use { preparedStatement ->
                preparedStatement.executeQuery().use { resultSet ->
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

    override fun createStoreEntityInheritance(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsStoreEntityInheritance.sql")
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
        executeSqlFromFile(connection, "sql/sqlite/JdsRefEntities.sql")
    }

    override fun createRefEnumValues(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsRefEnumValues.sql")
    }

    override fun createRefFields(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsRefFields.sql")
    }

    override fun createRefFieldTypes(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsRefFieldTypes.sql")
    }

    override fun createBindEntityFields(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsBindEntityFields.sql")
    }

    override fun createBindEntityEnums(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsBindEntityEnums.sql")
    }

    override fun createRefEntityOverview(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsStoreEntityOverview.sql")
    }

    override fun createRefOldFieldValues(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsStoreOldFieldValues.sql")
        executeSqlFromString(connection, "CREATE INDEX IntegerValues  ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, IntegerValue);")
        executeSqlFromString(connection, "CREATE INDEX FloatValues    ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, FloatValue);")
        executeSqlFromString(connection, "CREATE INDEX DoubleValues   ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, DoubleValue);")
        executeSqlFromString(connection, "CREATE INDEX LongValues     ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, LongValue);")
        executeSqlFromString(connection, "CREATE INDEX DateTimeValues ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, DateTimeValue);")
        executeSqlFromString(connection, "CREATE INDEX TextBlobValues ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence);")
    }

    override fun createStoreEntityBinding(connection: Connection) {
        executeSqlFromFile(connection, "sql/sqlite/JdsStoreEntityBinding.sql")
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
        return "INSERT OR REPLACE INTO JdsStoreText(EntityGuid, FieldId, Value) VALUES(:entityGuid, :fieldId, :value);"
    }

    override fun saveLong(): String {
        return "INSERT OR REPLACE INTO JdsStoreLong(EntityGuid, FieldId, Value) VALUES(:entityGuid, :fieldId, :value);"
    }

    override fun saveDouble(): String {
        return "INSERT OR REPLACE INTO JdsStoreDouble(EntityGuid, FieldId, Value) VALUES(:entityGuid, :fieldId, :value);"
    }

    override fun saveFloat(): String {
        return "INSERT OR REPLACE INTO JdsStoreFloat(EntityGuid, FieldId, Value) VALUES(:entityGuid, :fieldId, :value);"
    }

    override fun saveInteger(): String {
        return "INSERT OR REPLACE INTO JdsStoreInteger(EntityGuid, FieldId, Value) VALUES(:entityGuid, :fieldId, :value);"
    }

    override fun saveDateTime(): String {
        return "INSERT OR REPLACE INTO JdsStoreDateTime(EntityGuid, FieldId, Value) VALUES(:entityGuid, :fieldId, :value);"
    }

    override fun saveTime(): String {
        return "INSERT OR REPLACE INTO JdsStoreTime(EntityGuid, FieldId, Value) VALUES(:entityGuid, :fieldId, :value);"
    }

    override fun saveBlob(): String {
        return "INSERT OR REPLACE INTO JdsStoreBlob(EntityGuid, FieldId, Value) VALUES(:entityGuid, :fieldId, :value);"
    }

    override fun saveZonedDateTime(): String {
        return "INSERT OR REPLACE INTO JdsStoreZonedDateTime(EntityGuid, FieldId, Value) VALUES(:entityGuid, :fieldId, :value);"
    }

    override fun saveOverview(): String {
        return "INSERT OR REPLACE INTO JdsStoreEntityOverview(EntityGuid, DateCreated, DateModified, Live, Version) VALUES(:entityGuid, :dateCreated, :dateModified, :live, :version);"
    }

    override fun saveOverviewInheritance(): String {
        return "INSERT OR REPLACE INTO JdsStoreEntityInheritance(EntityGuid, EntityId) VALUES(:entityGuid, :entityId);"
    }

    override fun mapClassFields(): String {
        return "INSERT OR REPLACE INTO JdsBindEntityFields(EntityId, FieldId) VALUES(:entityId, :fieldId);"
    }

    override fun mapFieldNames(): String {
        return "INSERT OR REPLACE INTO JdsRefFields(FieldId, FieldName, FieldDescription) VALUES(:fieldId, :fieldName, :fieldDescription);"
    }

    override fun mapFieldTypes(): String {
        return "INSERT OR REPLACE INTO JdsRefFieldTypes(TypeId, TypeName) VALUES(:typeId, :typeName);"
    }

    override fun mapClassEnumsImplementation(): String {
        return "INSERT OR REPLACE INTO JdsBindEntityEnums(EntityId, FieldId) VALUES(?,?);"
    }

    override fun mapClassName(): String {
        return "INSERT OR REPLACE INTO JdsRefEntities(EntityId, EntityName) VALUES(?,?);"
    }

    override fun mapEnumValues(): String {
        return "INSERT OR REPLACE INTO JdsRefEnumValues(FieldId, EnumSeq, EnumValue) VALUES(?,?,?);"
    }

    /**
     * Map parents to child entities
     *
     * @return
     */
    override fun mapParentToChild(): String {
        return "INSERT OR REPLACE INTO JdsRefEntityInheritance(ParentEntityCode,ChildEntityCode) VALUES(?,?);"
    }

    override fun createOrAlterView(viewName: String, viewSql: String): String {
        return ""
    }

    override fun getSqlAddColumn(): String {
        return "ALTER TABLE %s ADD COLUMN %s %s"
    }

    override fun getSqlTypeFloat(): String {
        return "REAL"
    }

    override fun getSqlTypeDouble(): String {
        return "DOUBLE"
    }

    override fun getSqlTypeZonedDateTime(): String {
        return "BIGINT"
    }

    override fun getSqlTypeTime(): String {
        return "INTEGER"
    }

    override fun getSqlTypeBlob(max: Int): String {
        return "BLOB"
    }

    override fun getSqlTypeInteger(): String {
        return "INTEGER"
    }

    override fun getSqlTypeDateTime(): String {
        return "TIMESTAMP"
    }

    override fun getSqlTypeLong(): String {
        return "BIGINT"
    }

    override fun getSqlTypeText(max: Int): String {
        return "TEXT"
    }
}
