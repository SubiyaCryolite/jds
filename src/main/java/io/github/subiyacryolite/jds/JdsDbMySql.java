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
 * The MySQL implementation of {@link JdsDb JdsDataBase}
 */
public abstract class JdsDbMySql extends JdsDb {

    protected JdsDbMySql() {
        supportsStatements = true;
        implementation = JdsImplementation.MYSQL;
    }

    @Override
    public int tableExists(String tableName) {
        int toReturn = 0;
        String sql = "SELECT COUNT(table_schema) AS Result FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ? AND TABLE_SCHEMA = ?";
        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tableName);
            preparedStatement.setString(2, connection.getCatalog());
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

    public int procedureExists(String procedureName) {
        int toReturn = 0;
        String sql = "SELECT COUNT(ROUTINE_NAME) AS Result FROM INFORMATION_SCHEMA.ROUTINES WHERE ROUTINE_TYPE='PROCEDURE' AND ROUTINE_NAME = ? AND ROUTINE_SCHEMA = ?";
        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, procedureName);
            preparedStatement.setString(2, connection.getCatalog());
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

    public int viewExists(String viewName) {
        int toReturn = 0;
        String sql = "SELECT COUNT(ROUTINE_NAME) AS Result FROM INFORMATION_SCHEMA.VIEWS WHERE AND TABLE_NAME = ? AND TABLE_SCHEMA = ?";
        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, viewName);
            preparedStatement.setString(2, connection.getCatalog());
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
    protected void createStoreText() {
        executeSqlFromFile("sql/mysql/createStoreText.sql");
    }

    @Override
    protected void createStoreDateTime() {
        executeSqlFromFile("sql/mysql/createStoreDateTime.sql");
    }

    @Override
    protected void createStoreZonedDateTime() {
        executeSqlFromFile("sql/mysql/createStoreZonedDateTime.sql");
    }

    @Override
    protected void createStoreInteger() {
        executeSqlFromFile("sql/mysql/createStoreInteger.sql");
    }

    @Override
    protected void createStoreFloat() {
        executeSqlFromFile("sql/mysql/createStoreFloat.sql");
    }

    @Override
    protected void createStoreDouble() {
        executeSqlFromFile("sql/mysql/createStoreDouble.sql");
    }

    @Override
    protected void createStoreLong() {
        executeSqlFromFile("sql/mysql/createStoreLong.sql");
    }

    @Override
    protected void createStoreTextArray() {
        executeSqlFromFile("sql/mysql/createStoreTextArray.sql");
    }

    @Override
    protected void createStoreDateTimeArray() {
        executeSqlFromFile("sql/mysql/createStoreDateTimeArray.sql");
    }

    @Override
    protected void createStoreIntegerArray() {
        executeSqlFromFile("sql/mysql/createStoreIntegerArray.sql");
    }

    @Override
    protected void createStoreFloatArray() {
        executeSqlFromFile("sql/mysql/createStoreFloatArray.sql");
    }

    @Override
    protected void createStoreDoubleArray() {
        executeSqlFromFile("sql/mysql/createStoreDoubleArray.sql");
    }

    @Override
    protected void createStoreLongArray() {
        executeSqlFromFile("sql/mysql/createStoreLongArray.sql");
    }

    @Override
    protected void createStoreEntities() {
        executeSqlFromFile("sql/mysql/createRefEntities.sql");
    }

    @Override
    protected void createRefEnumValues() {
        executeSqlFromFile("sql/mysql/createRefEnumValues.sql");
    }

    @Override
    protected void createRefFields() {
        executeSqlFromFile("sql/mysql/createRefFields.sql");
    }

    @Override
    protected void createRefFieldTypes() {
        executeSqlFromFile("sql/mysql/createRefFieldTypes.sql");
    }

    @Override
    protected void createBindEntityFields() {
        executeSqlFromFile("sql/mysql/createBindEntityFields.sql");
    }

    @Override
    protected void createBindEntityEnums() {
        executeSqlFromFile("sql/mysql/createBindEntityEnums.sql");
    }

    @Override
    protected void createRefEntityOverview() {
        executeSqlFromFile("sql/mysql/createStoreEntityOverview.sql");
    }

    @Override
    protected void createRefOldFieldValues() {
        executeSqlFromFile("sql/mysql/createStoreOldFieldValues.sql");
    }

    @Override
    protected void createStoreEntityBinding() {
        executeSqlFromFile("sql/mysql/createStoreEntityBinding.sql");
    }

    protected void createStoreTime() {
        executeSqlFromFile("sql/mysql/createStoreTime.sql");
    }

    protected void createStoreBlob() {
        executeSqlFromFile("sql/mysql/createStoreBlob.sql");
    }

    @Override
    protected void prepareCustomDatabaseComponents() {
        prepareDatabaseComponent(JdsComponentType.STORED_PROCEDURE, JdsComponent.SaveBlob);
        prepareDatabaseComponent(JdsComponentType.STORED_PROCEDURE, JdsComponent.SaveText);
        prepareDatabaseComponent(JdsComponentType.STORED_PROCEDURE, JdsComponent.SaveLong);
        prepareDatabaseComponent(JdsComponentType.STORED_PROCEDURE, JdsComponent.SaveInteger);
        prepareDatabaseComponent(JdsComponentType.STORED_PROCEDURE, JdsComponent.SaveFloat);
        prepareDatabaseComponent(JdsComponentType.STORED_PROCEDURE, JdsComponent.SaveDouble);
        prepareDatabaseComponent(JdsComponentType.STORED_PROCEDURE, JdsComponent.SaveDateTime);
        prepareDatabaseComponent(JdsComponentType.STORED_PROCEDURE, JdsComponent.SaveTime);
        prepareDatabaseComponent(JdsComponentType.STORED_PROCEDURE, JdsComponent.SaveZonedDateTime);
        prepareDatabaseComponent(JdsComponentType.STORED_PROCEDURE, JdsComponent.SaveEntity);
        prepareDatabaseComponent(JdsComponentType.STORED_PROCEDURE, JdsComponent.MapEntityFields);
        prepareDatabaseComponent(JdsComponentType.STORED_PROCEDURE, JdsComponent.MapEntityEnums);
        prepareDatabaseComponent(JdsComponentType.STORED_PROCEDURE, JdsComponent.MapClassName);
        prepareDatabaseComponent(JdsComponentType.STORED_PROCEDURE, JdsComponent.MapEnumValues);
        prepareDatabaseComponent(JdsComponentType.STORED_PROCEDURE, JdsComponent.MapFieldNames);
        prepareDatabaseComponent(JdsComponentType.STORED_PROCEDURE, JdsComponent.MapFieldTypes);
    }

    @Override
    protected void prepareCustomDatabaseComponents(JdsComponent jdsComponent) {
        switch (jdsComponent) {
            case MapFieldNames:
                executeSqlFromFile("sql/mysql/procedures/procBindFieldNames.sql");
                break;
            case MapFieldTypes:
                executeSqlFromFile("sql/mysql/procedures/procBindFieldTypes.sql");
                break;
            case SaveBlob:
                executeSqlFromFile("sql/mysql/procedures/procStoreBlob.sql");
                break;
            case SaveTime:
                executeSqlFromFile("sql/mysql/procedures/procStoreTime.sql");
                break;
            case SaveText:
                executeSqlFromFile("sql/mysql/procedures/procStoreText.sql");
                break;
            case SaveLong:
                executeSqlFromFile("sql/mysql/procedures/procStoreLong.sql");
                break;
            case SaveInteger:
                executeSqlFromFile("sql/mysql/procedures/procStoreInteger.sql");
                break;
            case SaveFloat:
                executeSqlFromFile("sql/mysql/procedures/procStoreFloat.sql");
                break;
            case SaveDouble:
                executeSqlFromFile("sql/mysql/procedures/procStoreDouble.sql");
                break;
            case SaveDateTime:
                executeSqlFromFile("sql/mysql/procedures/procStoreDateTime.sql");
                break;
            case SaveZonedDateTime:
                executeSqlFromFile("sql/mysql/procedures/procStoreZonedDateTime.sql");
                break;
            case SaveEntity:
                executeSqlFromFile("sql/mysql/procedures/procStoreEntityOverview.sql");
                break;
            case MapEntityFields:
                executeSqlFromFile("sql/mysql/procedures/procBindEntityFields.sql");
                break;
            case MapEntityEnums:
                executeSqlFromFile("sql/mysql/procedures/procBindEntityEnums.sql");
                break;
            case MapClassName:
                executeSqlFromFile("sql/mysql/procedures/procRefEntities.sql");
                break;
            case MapEnumValues:
                executeSqlFromFile("sql/mysql/procedures/procRefEnumValues.sql");
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