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

/**
 * Created by ifunga on 14/07/2017.
 */
abstract class JdsDbOracle : JdsDb() {
    init {
        supportsStatements = true
        implementation = JdsImplementation.ORACLE
    }

    override fun tableExists(connection: Connection, tableName: String): Int {
        var toReturn = 0
        val sql = "SELECT COUNT(*) AS Result FROM all_objects WHERE object_type IN ('TABLE') AND object_name = ?"
        try {
            connection.prepareStatement(sql).use { preparedStatement ->
                preparedStatement.setString(1, tableName.toUpperCase())
                preparedStatement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        toReturn = resultSet.getInt("Result")
                    }
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
            connection.prepareStatement(sql).use { preparedStatement ->
                preparedStatement.setString(1, viewName.toUpperCase())
                preparedStatement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        toReturn = resultSet.getInt("Result")
                    }
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
            connection.prepareStatement(sql).use { preparedStatement ->
                preparedStatement.setString(1, procedureName.toUpperCase())
                preparedStatement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        toReturn = resultSet.getInt("Result")
                    }
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
            NamedPreparedStatement(connection, sql).use { preparedStatement ->
                preparedStatement.setString("tableName", tableName.toUpperCase())
                preparedStatement.setString("columnName", columnName.toUpperCase())
                preparedStatement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        toReturn = resultSet.getInt("Result")
                    }
                }
            }
        } catch (ex: Exception) {
            toReturn = 0
            ex.printStackTrace(System.err)
        }
        return toReturn
    }

    override fun createOrAlterView(viewName: String, viewSql: String): String {
        val sb = StringBuilder("CREATE VIEW\t")
        sb.append(viewName)
        sb.append("\tAS\t")
        sb.append(viewSql)
        return sb.toString()
    }

    override fun createStoreEntityInheritance(connection: Connection) {
        executeSqlFromFile(connection, "sql/oracle/JdsStoreEntityInheritance.sql")
    }

    override fun createStoreText(connection: Connection) {
        executeSqlFromFile(connection, "sql/oracle/JdsStoreText.sql")
    }

    override fun createStoreDateTime(connection: Connection) {
        executeSqlFromFile(connection, "sql/oracle/JdsStoreDateTime.sql")
    }

    override fun createStoreZonedDateTime(connection: Connection) {
        executeSqlFromFile(connection, "sql/oracle/JdsStoreZonedDateTime.sql")
    }

    override fun createStoreInteger(connection: Connection) {
        executeSqlFromFile(connection, "sql/oracle/JdsStoreInteger.sql")
    }

    override fun createStoreFloat(connection: Connection) {
        executeSqlFromFile(connection, "sql/oracle/JdsStoreFloat.sql")
    }

    override fun createStoreDouble(connection: Connection) {
        executeSqlFromFile(connection, "sql/oracle/JdsStoreDouble.sql")
    }

    override fun createStoreLong(connection: Connection) {
        executeSqlFromFile(connection, "sql/oracle/JdsStoreLong.sql")
    }

    override fun createStoreTextArray(connection: Connection) {
        executeSqlFromFile(connection, "sql/oracle/JdsStoreTextArray.sql")
    }

    override fun createStoreDateTimeArray(connection: Connection) {
        executeSqlFromFile(connection, "sql/oracle/JdsStoreDateTimeArray.sql")
    }

    override fun createStoreIntegerArray(connection: Connection) {
        executeSqlFromFile(connection, "sql/oracle/JdsStoreIntegerArray.sql")
    }

    override fun createStoreFloatArray(connection: Connection) {
        executeSqlFromFile(connection, "sql/oracle/JdsStoreFloatArray.sql")
    }

    override fun createStoreDoubleArray(connection: Connection) {
        executeSqlFromFile(connection, "sql/oracle/JdsStoreDoubleArray.sql")
    }

    override fun createStoreLongArray(connection: Connection) {
        executeSqlFromFile(connection, "sql/oracle/JdsStoreLongArray.sql")
    }

    override fun createStoreEntities(connection: Connection) {
        executeSqlFromFile(connection, "sql/oracle/JdsRefEntities.sql")
    }

    override fun createRefEnumValues(connection: Connection) {
        executeSqlFromFile(connection, "sql/oracle/JdsRefEnumValues.sql")
    }

    override fun createRefFields(connection: Connection) {
        executeSqlFromFile(connection, "sql/oracle/JdsRefFields.sql")
    }

    override fun createRefFieldTypes(connection: Connection) {
        executeSqlFromFile(connection, "sql/oracle/JdsRefFieldTypes.sql")
    }

    override fun createBindEntityFields(connection: Connection) {
        executeSqlFromFile(connection, "sql/oracle/JdsBindEntityFields.sql")
    }

    override fun createBindEntityEnums(connection: Connection) {
        executeSqlFromFile(connection, "sql/oracle/JdsBindEntityEnums.sql")
    }

    override fun createRefEntityOverview(connection: Connection) {
        executeSqlFromFile(connection, "sql/oracle/JdsStoreEntityOverview.sql")
    }

    override fun createRefOldFieldValues(connection: Connection) {
        executeSqlFromFile(connection, "sql/oracle/JdsStoreOldFieldValues.sql")
        //allow multiple leaves you open to SLQ injection. Thus manually add these indexes here unless you want to add more files
        //oracle jdbc hates semi-colons
        executeSqlFromString(connection, "CREATE INDEX IntegerValues  ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, IntegerValue)")
        executeSqlFromString(connection, "CREATE INDEX FloatValues    ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, FloatValue)")
        executeSqlFromString(connection, "CREATE INDEX DoubleValues   ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, DoubleValue)")
        executeSqlFromString(connection, "CREATE INDEX LongValues     ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, LongValue)")
        executeSqlFromString(connection, "CREATE INDEX DateTimeValues ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, DateTimeValue)")
        executeSqlFromString(connection, "CREATE INDEX TextBlobValues ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence)")
    }

    override fun createStoreEntityBinding(connection: Connection) {
        executeSqlFromFile(connection, "sql/oracle/JdsStoreEntityBinding.sql")
    }

    override fun createStoreTime(connection: Connection) {
        executeSqlFromFile(connection, "sql/oracle/JdsStoreTime.sql")
    }

    override fun createStoreBlob(connection: Connection) {
        executeSqlFromFile(connection, "sql/oracle/JdsStoreBlob.sql")
    }

    override fun createRefInheritance(connection: Connection) {
        executeSqlFromFile(connection, "sql/oracle/JdsRefEntityInheritance.sql")
    }

    override fun prepareCustomDatabaseComponents(connection: Connection) {
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_BLOB)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_TEXT)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_LONG)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_INTEGER)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_FLOAT)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_DOUBLE)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_DATE_TIME)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_TIME)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_ZONED_DATE_TIME)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_ENTITY_V_3)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.MAP_ENTITY_FIELDS)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.MAP_ENTITY_ENUMS)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.MAP_CLASS_NAME)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.MAP_ENUM_VALUES)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.MAP_FIELD_NAMES)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.MAP_FIELD_TYPES)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.MAP_ENTITY_INHERITANCE)
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_ENTITY_INHERITANCE)
    }

    override fun prepareCustomDatabaseComponents(connection: Connection, jdsComponent: JdsComponent) {
        when (jdsComponent) {
            JdsComponent.SAVE_ENTITY_V_3 -> executeSqlFromFile(connection, "sql/oracle/procedures/procStoreEntityOverviewV3.sql")
            JdsComponent.SAVE_ENTITY_INHERITANCE -> executeSqlFromFile(connection, "sql/oracle/procedures/procStoreEntityInheritance.sql")
            JdsComponent.MAP_FIELD_NAMES -> executeSqlFromFile(connection, "sql/oracle/procedures/procBindFieldNames.sql")
            JdsComponent.MAP_FIELD_TYPES -> executeSqlFromFile(connection, "sql/oracle/procedures/procBindFieldTypes.sql")
            JdsComponent.SAVE_BLOB -> executeSqlFromFile(connection, "sql/oracle/procedures/procStoreBlob.sql")
            JdsComponent.SAVE_TIME -> executeSqlFromFile(connection, "sql/oracle/procedures/procStoreTime.sql")
            JdsComponent.SAVE_TEXT -> executeSqlFromFile(connection, "sql/oracle/procedures/procStoreText.sql")
            JdsComponent.SAVE_LONG -> executeSqlFromFile(connection, "sql/oracle/procedures/procStoreLong.sql")
            JdsComponent.SAVE_INTEGER -> executeSqlFromFile(connection, "sql/oracle/procedures/procStoreInteger.sql")
            JdsComponent.SAVE_FLOAT -> executeSqlFromFile(connection, "sql/oracle/procedures/procStoreFloat.sql")
            JdsComponent.SAVE_DOUBLE -> executeSqlFromFile(connection, "sql/oracle/procedures/procStoreDouble.sql")
            JdsComponent.SAVE_DATE_TIME -> executeSqlFromFile(connection, "sql/oracle/procedures/procStoreDateTime.sql")
            JdsComponent.SAVE_ZONED_DATE_TIME -> executeSqlFromFile(connection, "sql/oracle/procedures/procStoreZonedDateTime.sql")
            JdsComponent.MAP_ENTITY_FIELDS -> executeSqlFromFile(connection, "sql/oracle/procedures/procBindEntityFields.sql")
            JdsComponent.MAP_ENTITY_ENUMS -> executeSqlFromFile(connection, "sql/oracle/procedures/procBindEntityEnums.sql")
            JdsComponent.MAP_CLASS_NAME -> executeSqlFromFile(connection, "sql/oracle/procedures/procRefEntities.sql")
            JdsComponent.MAP_ENUM_VALUES -> executeSqlFromFile(connection, "sql/oracle/procedures/procRefEnumValues.sql")
            JdsComponent.MAP_ENTITY_INHERITANCE -> executeSqlFromFile(connection, "sql/oracle/procedures/procBindParentToChild.sql")
            else -> {
            }
        }
    }
}
