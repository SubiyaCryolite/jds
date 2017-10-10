/*
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

import io.github.subiyacryolite.jds.enums.JdsComponent
import io.github.subiyacryolite.jds.enums.JdsComponentType
import io.github.subiyacryolite.jds.enums.JdsImplementation

import java.sql.Connection

/**
 * The PostgreSQL implementation of [JdsDataBase][JdsDb]
 */
abstract class JdsDbPostgreSql : JdsDb() {
    init {
        supportsStatements = true
        implementation = JdsImplementation.POSTGRES
    }

    override fun tableExists(connection: Connection, tableName: String): Int {
        var toReturn = 0
        val sql = "SELECT COUNT(*) AS Result FROM information_schema.tables WHERE table_catalog = ? AND table_name = ?"
        try {
            connection.prepareStatement(sql).use { preparedStatement ->
                preparedStatement.setString(1, connection.catalog)
                preparedStatement.setString(2, tableName.toLowerCase())
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
        val sql = "select COUNT(*) AS Result from information_schema.routines where routine_catalog = ? and routine_name = ?"
        try {
            connection.prepareStatement(sql).use { preparedStatement ->
                preparedStatement.setString(1, connection.catalog)
                preparedStatement.setString(2, procedureName.toLowerCase())
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
        val sql = "select COUNT(*) AS Result from information_schema.views where table_catalog = ? and table_name = ?"
        try {
            connection.prepareStatement(sql).use { preparedStatement ->
                preparedStatement.setString(1, connection.catalog)
                preparedStatement.setString(2, viewName.toLowerCase())
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
        val sql = "SELECT COUNT(COLUMN_NAME) AS Result FROM information_schema.COLUMNS WHERE TABLE_CATALOG = :tableCatalog AND TABLE_NAME = :tableName AND COLUMN_NAME = :columnName"
        toReturn = columnExistsCommonImpl(connection, tableName.toLowerCase(), columnName.toLowerCase(), toReturn, sql)
        return toReturn
    }

    override fun createStoreEntityInheritance(connection: Connection) {
        executeSqlFromFile(connection, "sql/postgresql/createStoreEntityInheritance.sql")
    }

    override fun createStoreText(connection: Connection) {
        executeSqlFromFile(connection, "sql/postgresql/createStoreText.sql")
    }

    override fun createStoreDateTime(connection: Connection) {
        executeSqlFromFile(connection, "sql/postgresql/createStoreDateTime.sql")
    }

    override fun createStoreZonedDateTime(connection: Connection) {
        executeSqlFromFile(connection, "sql/postgresql/createStoreZonedDateTime.sql")
    }

    override fun createStoreInteger(connection: Connection) {
        executeSqlFromFile(connection, "sql/postgresql/createStoreInteger.sql")
    }

    override fun createStoreFloat(connection: Connection) {
        executeSqlFromFile(connection, "sql/postgresql/createStoreFloat.sql")
    }

    override fun createStoreDouble(connection: Connection) {
        executeSqlFromFile(connection, "sql/postgresql/createStoreDouble.sql")
    }

    override fun createStoreLong(connection: Connection) {
        executeSqlFromFile(connection, "sql/postgresql/createStoreLong.sql")
    }

    override fun createStoreTextArray(connection: Connection) {
        executeSqlFromFile(connection, "sql/postgresql/createStoreTextArray.sql")
    }

    override fun createStoreDateTimeArray(connection: Connection) {
        executeSqlFromFile(connection, "sql/postgresql/createStoreDateTimeArray.sql")
    }

    override fun createStoreIntegerArray(connection: Connection) {
        executeSqlFromFile(connection, "sql/postgresql/createStoreIntegerArray.sql")
    }

    override fun createStoreFloatArray(connection: Connection) {
        executeSqlFromFile(connection, "sql/postgresql/createStoreFloatArray.sql")
    }

    override fun createStoreDoubleArray(connection: Connection) {
        executeSqlFromFile(connection, "sql/postgresql/createStoreDoubleArray.sql")
    }

    override fun createStoreLongArray(connection: Connection) {
        executeSqlFromFile(connection, "sql/postgresql/createStoreLongArray.sql")
    }

    override fun createStoreEntities(connection: Connection) {
        executeSqlFromFile(connection, "sql/postgresql/createRefEntities.sql")
    }

    override fun createRefEnumValues(connection: Connection) {
        executeSqlFromFile(connection, "sql/postgresql/createRefEnumValues.sql")
    }

    override fun createRefFields(connection: Connection) {
        executeSqlFromFile(connection, "sql/postgresql/createRefFields.sql")
    }

    override fun createRefFieldTypes(connection: Connection) {
        executeSqlFromFile(connection, "sql/postgresql/createRefFieldTypes.sql")
    }

    override fun createBindEntityFields(connection: Connection) {
        executeSqlFromFile(connection, "sql/postgresql/createBindEntityFields.sql")
    }

    override fun createBindEntityEnums(connection: Connection) {
        executeSqlFromFile(connection, "sql/postgresql/createBindEntityEnums.sql")
    }

    override fun createRefEntityOverview(connection: Connection) {
        executeSqlFromFile(connection, "sql/postgresql/createStoreEntityOverview.sql")
    }

    override fun createRefOldFieldValues(connection: Connection) {
        executeSqlFromFile(connection, "sql/postgresql/createStoreOldFieldValues.sql")
    }

    override fun createStoreEntityBinding(connection: Connection) {
        executeSqlFromFile(connection, "sql/postgresql/createStoreEntityBinding.sql")
    }

    override fun createStoreTime(connection: Connection) {
        executeSqlFromFile(connection, "sql/postgresql/createStoreTime.sql")
    }

    override fun createStoreBlob(connection: Connection) {
        executeSqlFromFile(connection, "sql/postgresql/createStoreBlob.sql")
    }

    override fun createRefInheritance(connection: Connection) {
        executeSqlFromFile(connection, "sql/postgresql/createRefInheritance.sql")
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
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_ENTITY_V_2)
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
            JdsComponent.SAVE_ENTITY_V_2 -> executeSqlFromFile(connection, "sql/postgresql/procedures/procStoreEntityOverviewV2.sql")
            JdsComponent.SAVE_ENTITY_V_3 -> executeSqlFromFile(connection, "sql/postgresql/procedures/procStoreEntityOverviewV3.sql")
            JdsComponent.SAVE_ENTITY_INHERITANCE -> executeSqlFromFile(connection, "sql/postgresql/procedures/procStoreEntityInheritance.sql")
            JdsComponent.MAP_FIELD_NAMES -> executeSqlFromFile(connection, "sql/postgresql/procedures/procBindFieldNames.sql")
            JdsComponent.MAP_FIELD_TYPES -> executeSqlFromFile(connection, "sql/postgresql/procedures/procBindFieldTypes.sql")
            JdsComponent.SAVE_BLOB -> executeSqlFromFile(connection, "sql/postgresql/procedures/procStoreBlob.sql")
            JdsComponent.SAVE_TIME -> executeSqlFromFile(connection, "sql/postgresql/procedures/procStoreTime.sql")
            JdsComponent.SAVE_TEXT -> executeSqlFromFile(connection, "sql/postgresql/procedures/procStoreText.sql")
            JdsComponent.SAVE_LONG -> executeSqlFromFile(connection, "sql/postgresql/procedures/procStoreLong.sql")
            JdsComponent.SAVE_INTEGER -> executeSqlFromFile(connection, "sql/postgresql/procedures/procStoreInteger.sql")
            JdsComponent.SAVE_FLOAT -> executeSqlFromFile(connection, "sql/postgresql/procedures/procStoreFloat.sql")
            JdsComponent.SAVE_DOUBLE -> executeSqlFromFile(connection, "sql/postgresql/procedures/procStoreDouble.sql")
            JdsComponent.SAVE_DATE_TIME -> executeSqlFromFile(connection, "sql/postgresql/procedures/procStoreDateTime.sql")
            JdsComponent.SAVE_ZONED_DATE_TIME -> executeSqlFromFile(connection, "sql/postgresql/procedures/procStoreZonedDateTime.sql")
            JdsComponent.SAVE_ENTITY -> executeSqlFromFile(connection, "sql/postgresql/procedures/procStoreEntityOverview.sql")
            JdsComponent.MAP_ENTITY_FIELDS -> executeSqlFromFile(connection, "sql/postgresql/procedures/procBindEntityFields.sql")
            JdsComponent.MAP_ENTITY_ENUMS -> executeSqlFromFile(connection, "sql/postgresql/procedures/procBindEntityEnums.sql")
            JdsComponent.MAP_CLASS_NAME -> executeSqlFromFile(connection, "sql/postgresql/procedures/procRefEntities.sql")
            JdsComponent.MAP_ENUM_VALUES -> executeSqlFromFile(connection, "sql/postgresql/procedures/procRefEnumValues.sql")
            JdsComponent.MAP_ENTITY_INHERITANCE -> executeSqlFromFile(connection, "sql/postgresql/procedures/procBindParentToChild.sql")
            else -> {
            }
        }
    }

    override fun createOrAlterView(viewName: String, viewSql: String): String {
        val sb = StringBuilder("CREATE VIEW\t")
        sb.append(viewName)
        sb.append("\tAS\t")
        sb.append(viewSql)
        return sb.toString()
    }
}
