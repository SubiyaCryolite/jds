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

/**
 * The SQLite implementation of [io.github.subiyacryolite.jds.DbContext]
 */
abstract class SqLiteDbContext : DbContext(Implementation.SqLite, false) {

    override fun tableExists(connection: Connection, table: Table): Int {
        return tableExists(connection, "$objectPrefix${table.table}")
    }

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

    override fun populateEntityBinding() = "INSERT INTO ${getName(Table.EntityBinding)}(parent_id, parent_edit_version, child_id, child_edit_version, child_attribute_id) VALUES(?, ?, ?, ?, ?) ON CONFLICT(parent_id, parent_edit_version, child_id, child_edit_version) DO UPDATE SET child_attribute_id = EXCLUDED.child_attribute_id"

    override fun populateEntityField() = "INSERT INTO ${getName(Table.EntityField)}(entity_id, field_id) VALUES(?, ?) ON CONFLICT(entity_id, field_id) DO NOTHING"

    override fun populateFieldEntity() = "INSERT INTO ${getName(Table.FieldEntity)}(field_id, entity_id) VALUES(?, ?) ON CONFLICT(field_id, entity_id) DO NOTHING"

    override fun populateField() = "INSERT INTO ${getName(Table.Field)}(id, caption, description, field_type_ordinal) VALUES(?, ?, ?, ?) ON CONFLICT(id) DO UPDATE SET caption = EXCLUDED.caption, description = EXCLUDED.description, field_type_ordinal = EXCLUDED.field_type_ordinal"

    override fun populateEntityEnum() = "INSERT INTO ${getName(Table.EntityEnum)}(entity_id, field_id) VALUES(?, ?) ON CONFLICT(entity_id, field_id) DO NOTHING"

    override fun populateEntity() = "INSERT INTO ${getName(Table.Entity)}(id, name, description) VALUES(?, ?, ?) ON CONFLICT(id) DO UPDATE SET name = EXCLUDED.name, description = EXCLUDED.description"

    override fun populateEnum() = "INSERT INTO ${getName(Table.Enum)}(field_id, seq, name, caption) VALUES(?, ?, ?, ?) ON CONFLICT(field_id, seq) DO UPDATE SET name = EXCLUDED.name, caption = EXCLUDED.caption"

    override fun populateFieldType() =  "INSERT INTO ${getName(Table.FieldType)}(ordinal, caption) VALUES(?, ?) ON CONFLICT(ordinal) DO UPDATE SET caption = EXCLUDED.caption"

    override fun populateFieldDictionary() = "INSERT INTO ${getName(Table.FieldDictionary)}(entity_id, field_id, property_name) VALUES(?, ?, ?) ON CONFLICT(entity_id, field_id) DO UPDATE SET property_name = EXCLUDED.property_name"

    override fun populateEntityInheritance() = "INSERT INTO ${getName(Table.EntityInheritance)}(parent_entity_id, child_entity_id) VALUES(?, ?) ON CONFLICT(parent_entity_id, child_entity_id) DO NOTHING"

    override fun populateFieldTag() = "INSERT INTO ${getName(Table.FieldTag)}(field_id, tag) VALUES(?, ?) ON CONFLICT(field_id, tag) DO NOTHING"

    override fun populateEntityTag() = "INSERT INTO ${getName(Table.EntityTag)}(entity_id, tag) VALUES(?, ?) ON CONFLICT(entity_id, tag) DO NOTHING"

    override fun populateFieldAlternateCode() = "INSERT INTO ${getName(Table.FieldAlternateCode)}(field_id, alternate_code, value) VALUES(?, ?, ?) ON CONFLICT(field_id, alternate_code) DO UPDATE SET value = EXCLUDED.value"

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