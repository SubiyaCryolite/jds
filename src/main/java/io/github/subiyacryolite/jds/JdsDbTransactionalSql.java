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
 * The TSQL implementation of {@link JdsDb JdsDataBase}
 */
public abstract class JdsDbTransactionalSql extends JdsDb {

    protected JdsDbTransactionalSql() {
        supportsStatements = true;
        implementation = JdsImplementation.TSQL;
    }

    @Override
    public int tableExists(Connection connection, String tableName) {
        int toReturn = 0;
        String sql = "IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?)) SELECT 1 AS Result ELSE SELECT 0 AS Result ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tableName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    toReturn = resultSet.getInt("Result");
                }
            }
        } catch (Exception ex) {
            toReturn = 0;
            ex.printStackTrace(System.err);
        }
        return toReturn;
    }

    @Override
    public int procedureExists(Connection connection, String procedureName) {
        int toReturn = 0;
        String sql = "IF (EXISTS (SELECT * FROM sysobjects WHERE NAME = ? and XTYPE = ?)) SELECT 1 AS Result ELSE SELECT 0 AS Result ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, procedureName);
            preparedStatement.setString(2, "P");
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    toReturn = resultSet.getInt("Result");
                }
            }
        } catch (Exception ex) {
            toReturn = 0;
            ex.printStackTrace(System.err);
        }
        return toReturn;
    }

    @Override
    public int triggerExists(Connection connection, String triggerName) {
        int toReturn = 0;
        String sql = "IF (EXISTS (SELECT * FROM sysobjects WHERE NAME = ? and XTYPE = ?)) SELECT 1 AS Result ELSE SELECT 0 AS Result ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, triggerName);
            preparedStatement.setString(2, "TR");
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    toReturn = resultSet.getInt("Result");
                }
            }
        } catch (Exception ex) {
            toReturn = 0;
            ex.printStackTrace(System.err);
        }
        return toReturn;
    }

    @Override
    public int viewExists(Connection connection, String viewName) {
        int toReturn = 0;
        String sql = "IF (EXISTS (SELECT * FROM sysobjects WHERE NAME = ? and XTYPE = ?)) SELECT 1 AS Result ELSE SELECT 0 AS Result ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, viewName);
            preparedStatement.setString(2, "V");
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    toReturn = resultSet.getInt("Result");
                }
            }
        } catch (Exception ex) {
            toReturn = 0;
            ex.printStackTrace(System.err);
        }
        return toReturn;
    }

    @Override
    public int columnExists(Connection connection, String tableName, String columnName) {
        int toReturn = 0;
        String sql = "SELECT COUNT(COLUMN_NAME) AS Result from INFORMATION_SCHEMA.columns WHERE TABLE_CATALOG = :tableCatalog and TABLE_NAME = :tableName and COLUMN_NAME = :columnName";
        toReturn = columnExistsCommonImpl(connection, tableName, columnName, toReturn, sql);
        return toReturn;
    }

    @Override
    protected void createStoreEntityInheritance(Connection connection) {
        executeSqlFromFile(connection, "sql/tsql/createStoreEntityInheritance.sql");
    }

    @Override
    protected void createStoreText(Connection connection) {
        executeSqlFromFile(connection, "sql/tsql/createStoreText.sql");
    }

    @Override
    protected void createStoreDateTime(Connection connection) {
        executeSqlFromFile(connection, "sql/tsql/createStoreDateTime.sql");
    }

    @Override
    protected void createStoreZonedDateTime(Connection connection) {
        executeSqlFromFile(connection, "sql/tsql/createStoreZonedDateTime.sql");
    }

    @Override
    protected void createStoreInteger(Connection connection) {
        executeSqlFromFile(connection, "sql/tsql/createStoreInteger.sql");
    }

    @Override
    protected void createStoreFloat(Connection connection) {
        executeSqlFromFile(connection, "sql/tsql/createStoreFloat.sql");
    }

    @Override
    protected void createStoreDouble(Connection connection) {
        executeSqlFromFile(connection, "sql/tsql/createStoreDouble.sql");
    }

    @Override
    protected void createStoreLong(Connection connection) {
        executeSqlFromFile(connection, "sql/tsql/createStoreLong.sql");
    }

    @Override
    protected void createStoreTextArray(Connection connection) {
        executeSqlFromFile(connection, "sql/tsql/createStoreTextArray.sql");
    }

    @Override
    protected void createStoreDateTimeArray(Connection connection) {
        executeSqlFromFile(connection, "sql/tsql/createStoreDateTimeArray.sql");
    }

    @Override
    protected void createStoreIntegerArray(Connection connection) {
        executeSqlFromFile(connection, "sql/tsql/createStoreIntegerArray.sql");
    }

    @Override
    protected void createStoreFloatArray(Connection connection) {
        executeSqlFromFile(connection, "sql/tsql/createStoreFloatArray.sql");
    }

    @Override
    protected void createStoreDoubleArray(Connection connection) {
        executeSqlFromFile(connection, "sql/tsql/createStoreDoubleArray.sql");
    }

    @Override
    protected void createStoreLongArray(Connection connection) {
        executeSqlFromFile(connection, "sql/tsql/createStoreLongArray.sql");
    }

    @Override
    protected void createStoreEntities(Connection connection) {
        executeSqlFromFile(connection, "sql/tsql/createRefEntities.sql");
    }

    @Override
    protected void createRefEnumValues(Connection connection) {
        executeSqlFromFile(connection, "sql/tsql/createRefEnumValues.sql");
    }

    @Override
    protected void createRefFields(Connection connection) {
        executeSqlFromFile(connection, "sql/tsql/createRefFields.sql");
    }

    @Override
    protected void createRefFieldTypes(Connection connection) {
        executeSqlFromFile(connection, "sql/tsql/createRefFieldTypes.sql");
    }

    @Override
    protected void createBindEntityFields(Connection connection) {
        executeSqlFromFile(connection, "sql/tsql/createBindEntityFields.sql");
    }

    @Override
    protected void createBindEntityEnums(Connection connection) {
        executeSqlFromFile(connection, "sql/tsql/createBindEntityEnums.sql");
    }

    @Override
    protected void createRefEntityOverview(Connection connection) {
        executeSqlFromFile(connection, "sql/tsql/createStoreEntityOverview.sql");
    }

    @Override
    protected void createRefOldFieldValues(Connection connection) {
        executeSqlFromFile(connection, "sql/tsql/createStoreOldFieldValues.sql");
    }

    @Override
    protected void createStoreEntityBinding(Connection connection) {
        executeSqlFromFile(connection, "sql/tsql/createStoreEntityBinding.sql");
    }

    protected void createStoreTime(Connection connection) {
        executeSqlFromFile(connection, "sql/tsql/createStoreTime.sql");
    }

    protected void createStoreBlob(Connection connection) {
        executeSqlFromFile(connection, "sql/tsql/createStoreBlob.sql");
    }

    @Override
    protected void createRefInheritance(Connection connection) {
        executeSqlFromFile(connection, "sql/tsql/createRefInheritance.sql");
    }

    @Override
    protected void prepareCustomDatabaseComponents(Connection connection) {
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_BLOB);
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_TEXT);
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_LONG);
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_INTEGER);
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_FLOAT);
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_DOUBLE);
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_DATE_TIME);
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_TIME);
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_ZONED_DATE_TIME);
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_ENTITY_V_2);
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.MAP_ENTITY_FIELDS);
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.MAP_ENTITY_ENUMS);
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.MAP_CLASS_NAME);
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.MAP_ENUM_VALUES);
        prepareDatabaseComponent(connection, JdsComponentType.TRIGGER, JdsComponent.TSQL_CASCADE_ENTITY_BINDING);
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.MAP_FIELD_NAMES);
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.MAP_FIELD_TYPES);
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.MAP_ENTITY_INHERITANCE);
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_ENTITY_INHERITANCE);
    }

    @Override
    protected void prepareCustomDatabaseComponents(Connection connection, JdsComponent jdsComponent) {
        switch (jdsComponent) {
            case SAVE_ENTITY_V_2:
                executeSqlFromFile(connection, "sql/tsql/procedures/procStoreEntityOverviewV2.sql");
                break;
            case SAVE_ENTITY_INHERITANCE:
                executeSqlFromFile(connection, "sql/tsql/procedures/procStoreEntityInheritance.sql");
                break;
            case MAP_FIELD_NAMES:
                executeSqlFromFile(connection, "sql/tsql/procedures/procBindFieldNames.sql");
                break;
            case MAP_FIELD_TYPES:
                executeSqlFromFile(connection, "sql/tsql/procedures/procBindFieldTypes.sql");
                break;
            case SAVE_BLOB:
                executeSqlFromFile(connection, "sql/tsql/procedures/procStoreBlob.sql");
                break;
            case SAVE_TIME:
                executeSqlFromFile(connection, "sql/tsql/procedures/procStoreTime.sql");
                break;
            case SAVE_TEXT:
                executeSqlFromFile(connection, "sql/tsql/procedures/procStoreText.sql");
                break;
            case SAVE_LONG:
                executeSqlFromFile(connection, "sql/tsql/procedures/procStoreLong.sql");
                break;
            case SAVE_INTEGER:
                executeSqlFromFile(connection, "sql/tsql/procedures/procStoreInteger.sql");
                break;
            case SAVE_FLOAT:
                executeSqlFromFile(connection, "sql/tsql/procedures/procStoreFloat.sql");
                break;
            case SAVE_DOUBLE:
                executeSqlFromFile(connection, "sql/tsql/procedures/procStoreDouble.sql");
                break;
            case SAVE_DATE_TIME:
                executeSqlFromFile(connection, "sql/tsql/procedures/procStoreDateTime.sql");
                break;
            case SAVE_ZONED_DATE_TIME:
                executeSqlFromFile(connection, "sql/tsql/procedures/procStoreZonedDateTime.sql");
                break;
            case SAVE_ENTITY:
                executeSqlFromFile(connection, "sql/tsql/procedures/procStoreEntityOverview.sql");
                break;
            case MAP_ENTITY_FIELDS:
                executeSqlFromFile(connection, "sql/tsql/procedures/procBindEntityFields.sql");
                break;
            case MAP_ENTITY_ENUMS:
                executeSqlFromFile(connection, "sql/tsql/procedures/procBindEntityEnums.sql");
                break;
            case MAP_CLASS_NAME:
                executeSqlFromFile(connection, "sql/tsql/procedures/procRefEntities.sql");
                break;
            case MAP_ENUM_VALUES:
                executeSqlFromFile(connection, "sql/tsql/procedures/procRefEnumValues.sql");
                break;
            case TSQL_CASCADE_ENTITY_BINDING:
                executeSqlFromFile(connection, "sql/tsql/triggers/createEntityBindingCascade.sql");
                break;
            case MAP_ENTITY_INHERITANCE:
                executeSqlFromFile(connection, "sql/tsql/procedures/procBindParentToChild.sql");
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
