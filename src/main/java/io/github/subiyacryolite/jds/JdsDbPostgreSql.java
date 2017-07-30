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
package io.github.subiyacryolite.jds;

import io.github.subiyacryolite.jds.enums.JdsComponent;
import io.github.subiyacryolite.jds.enums.JdsComponentType;
import io.github.subiyacryolite.jds.enums.JdsImplementation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * The PostgreSQL implementation of {@link JdsDb JdsDataBase}
 */
public abstract class JdsDbPostgreSql extends JdsDb {

    public JdsDbPostgreSql() {
        supportsStatements = true;
        implementation = JdsImplementation.POSTGRES;
    }

    @Override
    public int tableExists(String tableName) {
        int toReturn = 0;
        String sql = "SELECT COUNT(*) AS Result FROM information_schema.tables WHERE table_catalog = ? AND table_name = ?";
        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, connection.getCatalog());
            preparedStatement.setString(2, tableName.toLowerCase());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                toReturn = resultSet.getInt("Result");
            }
        } catch (Exception ex) {
            toReturn = 0;
            ex.printStackTrace(System.err);
        }
        return toReturn;
    }

    @Override
    public int procedureExists(String procedureName) {
        int toReturn = 0;
        String sql = "select COUNT(*) AS Result from information_schema.routines where routine_catalog = ? and routine_name = ?";
        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, connection.getCatalog());
            preparedStatement.setString(2, procedureName.toLowerCase());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                toReturn = resultSet.getInt("Result");
            }
        } catch (Exception ex) {
            toReturn = 0;
            ex.printStackTrace(System.err);
        }
        return toReturn;
    }

    @Override
    public int viewExists(String viewName) {
        int toReturn = 0;
        String sql = "select COUNT(*) AS Result from information_schema.views where table_catalog = ? and table_name = ?";
        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, connection.getCatalog());
            preparedStatement.setString(2, viewName.toLowerCase());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                toReturn = resultSet.getInt("Result");
            }
        } catch (Exception ex) {
            toReturn = 0;
            ex.printStackTrace(System.err);
        }
        return toReturn;
    }

    @Override
    public int columnExists(String tableName, String columnName) {
        int toReturn = 0;
        String sql = "SELECT COUNT(COLUMN_NAME) AS Result FROM information_schema.COLUMNS WHERE TABLE_CATALOG = :tableCatalog AND TABLE_NAME = :tableName AND COLUMN_NAME = :columnName";
        toReturn = columnExistsCommonImpl(tableName.toLowerCase(), columnName.toLowerCase(), toReturn, sql);
        return toReturn;
    }

    @Override
    protected void createStoreEntityInheritance() {
        executeSqlFromFile("sql/postgresql/createStoreEntityInheritance.sql");
    }

    @Override
    protected void createStoreText() {
        executeSqlFromFile("sql/postgresql/createStoreText.sql");
    }

    @Override
    protected void createStoreDateTime() {
        executeSqlFromFile("sql/postgresql/createStoreDateTime.sql");
    }

    @Override
    protected void createStoreZonedDateTime() {
        executeSqlFromFile("sql/postgresql/createStoreZonedDateTime.sql");
    }

    @Override
    protected void createStoreInteger() {
        executeSqlFromFile("sql/postgresql/createStoreInteger.sql");
    }

    @Override
    protected void createStoreFloat() {
        executeSqlFromFile("sql/postgresql/createStoreFloat.sql");
    }

    @Override
    protected void createStoreDouble() {
        executeSqlFromFile("sql/postgresql/createStoreDouble.sql");
    }

    @Override
    protected void createStoreLong() {
        executeSqlFromFile("sql/postgresql/createStoreLong.sql");
    }

    @Override
    protected void createStoreTextArray() {
        executeSqlFromFile("sql/postgresql/createStoreTextArray.sql");
    }

    @Override
    protected void createStoreDateTimeArray() {
        executeSqlFromFile("sql/postgresql/createStoreDateTimeArray.sql");
    }

    @Override
    protected void createStoreIntegerArray() {
        executeSqlFromFile("sql/postgresql/createStoreIntegerArray.sql");
    }

    @Override
    protected void createStoreFloatArray() {
        executeSqlFromFile("sql/postgresql/createStoreFloatArray.sql");
    }

    @Override
    protected void createStoreDoubleArray() {
        executeSqlFromFile("sql/postgresql/createStoreDoubleArray.sql");
    }

    @Override
    protected void createStoreLongArray() {
        executeSqlFromFile("sql/postgresql/createStoreLongArray.sql");
    }

    @Override
    protected void createStoreEntities() {
        executeSqlFromFile("sql/postgresql/createRefEntities.sql");
    }

    @Override
    protected void createRefEnumValues() {
        executeSqlFromFile("sql/postgresql/createRefEnumValues.sql");
    }

    @Override
    protected void createRefFields() {
        executeSqlFromFile("sql/postgresql/createRefFields.sql");
    }

    @Override
    protected void createRefFieldTypes() {
        executeSqlFromFile("sql/postgresql/createRefFieldTypes.sql");
    }

    @Override
    protected void createBindEntityFields() {
        executeSqlFromFile("sql/postgresql/createBindEntityFields.sql");
    }

    @Override
    protected void createBindEntityEnums() {
        executeSqlFromFile("sql/postgresql/createBindEntityEnums.sql");
    }

    @Override
    protected void createRefEntityOverview() {
        executeSqlFromFile("sql/postgresql/createStoreEntityOverview.sql");
    }

    @Override
    protected void createRefOldFieldValues() {
        executeSqlFromFile("sql/postgresql/createStoreOldFieldValues.sql");
    }

    @Override
    protected void createStoreEntityBinding() {
        executeSqlFromFile("sql/postgresql/createStoreEntityBinding.sql");
    }

    protected void createStoreTime() {
        executeSqlFromFile("sql/postgresql/createStoreTime.sql");
    }

    protected void createStoreBlob() {
        executeSqlFromFile("sql/postgresql/createStoreBlob.sql");
    }

    @Override
    protected void createRefInheritance() {
        executeSqlFromFile("sql/postgresql/createRefInheritance.sql");
    }

    @Override
    protected void prepareCustomDatabaseComponents() {
        prepareDatabaseComponent(JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_BLOB);
        prepareDatabaseComponent(JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_TEXT);
        prepareDatabaseComponent(JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_LONG);
        prepareDatabaseComponent(JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_INTEGER);
        prepareDatabaseComponent(JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_FLOAT);
        prepareDatabaseComponent(JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_DOUBLE);
        prepareDatabaseComponent(JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_DATE_TIME);
        prepareDatabaseComponent(JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_TIME);
        prepareDatabaseComponent(JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_ZONED_DATE_TIME);
        prepareDatabaseComponent(JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_ENTITY_V_2);
        prepareDatabaseComponent(JdsComponentType.STORED_PROCEDURE, JdsComponent.MAP_ENTITY_FIELDS);
        prepareDatabaseComponent(JdsComponentType.STORED_PROCEDURE, JdsComponent.MAP_ENTITY_ENUMS);
        prepareDatabaseComponent(JdsComponentType.STORED_PROCEDURE, JdsComponent.MAP_CLASS_NAME);
        prepareDatabaseComponent(JdsComponentType.STORED_PROCEDURE, JdsComponent.MAP_ENUM_VALUES);
        prepareDatabaseComponent(JdsComponentType.STORED_PROCEDURE, JdsComponent.MAP_FIELD_NAMES);
        prepareDatabaseComponent(JdsComponentType.STORED_PROCEDURE, JdsComponent.MAP_FIELD_TYPES);
        prepareDatabaseComponent(JdsComponentType.STORED_PROCEDURE, JdsComponent.MAP_ENTITY_INHERITANCE);
        prepareDatabaseComponent(JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_ENTITY_INHERITANCE);
    }

    @Override
    protected void prepareCustomDatabaseComponents(JdsComponent jdsComponent) {
        switch (jdsComponent) {
            case SAVE_ENTITY_V_2:
                executeSqlFromFile("sql/postgresql/procedures/procStoreEntityOverviewV2.sql");
                break;
            case SAVE_ENTITY_INHERITANCE:
                executeSqlFromFile("sql/postgresql/procedures/procStoreEntityInheritance.sql");
                break;
            case MAP_FIELD_NAMES:
                executeSqlFromFile("sql/postgresql/procedures/procBindFieldNames.sql");
                break;
            case MAP_FIELD_TYPES:
                executeSqlFromFile("sql/postgresql/procedures/procBindFieldTypes.sql");
                break;
            case SAVE_BLOB:
                executeSqlFromFile("sql/postgresql/procedures/procStoreBlob.sql");
                break;
            case SAVE_TIME:
                executeSqlFromFile("sql/postgresql/procedures/procStoreTime.sql");
                break;
            case SAVE_TEXT:
                executeSqlFromFile("sql/postgresql/procedures/procStoreText.sql");
                break;
            case SAVE_LONG:
                executeSqlFromFile("sql/postgresql/procedures/procStoreLong.sql");
                break;
            case SAVE_INTEGER:
                executeSqlFromFile("sql/postgresql/procedures/procStoreInteger.sql");
                break;
            case SAVE_FLOAT:
                executeSqlFromFile("sql/postgresql/procedures/procStoreFloat.sql");
                break;
            case SAVE_DOUBLE:
                executeSqlFromFile("sql/postgresql/procedures/procStoreDouble.sql");
                break;
            case SAVE_DATE_TIME:
                executeSqlFromFile("sql/postgresql/procedures/procStoreDateTime.sql");
                break;
            case SAVE_ZONED_DATE_TIME:
                executeSqlFromFile("sql/postgresql/procedures/procStoreZonedDateTime.sql");
                break;
            case SAVE_ENTITY:
                executeSqlFromFile("sql/postgresql/procedures/procStoreEntityOverview.sql");
                break;
            case MAP_ENTITY_FIELDS:
                executeSqlFromFile("sql/postgresql/procedures/procBindEntityFields.sql");
                break;
            case MAP_ENTITY_ENUMS:
                executeSqlFromFile("sql/postgresql/procedures/procBindEntityEnums.sql");
                break;
            case MAP_CLASS_NAME:
                executeSqlFromFile("sql/postgresql/procedures/procRefEntities.sql");
                break;
            case MAP_ENUM_VALUES:
                executeSqlFromFile("sql/postgresql/procedures/procRefEnumValues.sql");
                break;
            case MAP_ENTITY_INHERITANCE:
                executeSqlFromFile("sql/postgresql/procedures/procBindParentToChild.sql");
                break;
        }
    }

    public String createOrAlterView(String viewName, String viewSql) {
        StringBuilder sb = new StringBuilder("CREATE VIEW\t");
        sb.append(viewName);
        sb.append("\tAS\t");
        sb.append(viewSql);
        String toExecute = sb.toString();
        return toExecute;
    }
}
