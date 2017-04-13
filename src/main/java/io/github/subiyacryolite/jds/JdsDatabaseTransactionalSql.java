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

import io.github.subiyacryolite.jds.enums.JdsDatabaseComponent;
import io.github.subiyacryolite.jds.enums.JdsEnumTable;
import io.github.subiyacryolite.jds.enums.JdsImplementation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by ifunga on 12/02/2017.
 */
public class JdsDatabaseTransactionalSql extends JdsDatabase {

    protected JdsDatabaseTransactionalSql() {
        supportsStatements = true;
        implementation = JdsImplementation.TSQL;
    }

    @Override
    public int tableExists(String tableName) {
        int toReturn = 0;
        String sql = "IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?)) SELECT 1 AS Result ELSE SELECT 0 AS Result ";
        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tableName);
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
        String sql = "IF (EXISTS (SELECT * FROM sysobjects WHERE NAME = ? and XTYPE = ?)) SELECT 1 AS Result ELSE SELECT 0 AS Result ";
        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, procedureName);
            preparedStatement.setString(2, "P");
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

    public int triggerExists(String triggerName) {
        int toReturn = 0;
        String sql = "IF (EXISTS (SELECT * FROM sysobjects WHERE NAME = ? and XTYPE = ?)) SELECT 1 AS Result ELSE SELECT 0 AS Result ";
        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, triggerName);
            preparedStatement.setString(2, "TR");
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
        executeSqlFromFile("sql/tsql/createStoreText.sql");
    }

    @Override
    protected void createStoreDateTime() {
        executeSqlFromFile("sql/tsql/createStoreDateTime.sql");
    }

    @Override
    protected void createStoreZonedDateTime() {
        executeSqlFromFile("sql/tsql/createStoreZonedDateTime.sql");
    }

    @Override
    protected void createStoreInteger() {
        executeSqlFromFile("sql/tsql/createStoreInteger.sql");
    }

    @Override
    protected void createStoreFloat() {
        executeSqlFromFile("sql/tsql/createStoreFloat.sql");
    }

    @Override
    protected void createStoreDouble() {
        executeSqlFromFile("sql/tsql/createStoreDouble.sql");
    }

    @Override
    protected void createStoreLong() {
        executeSqlFromFile("sql/tsql/createStoreLong.sql");
    }

    @Override
    protected void createStoreTextArray() {
        executeSqlFromFile("sql/tsql/createStoreTextArray.sql");
    }

    @Override
    protected void createStoreDateTimeArray() {
        executeSqlFromFile("sql/tsql/createStoreDateTimeArray.sql");
    }

    @Override
    protected void createStoreIntegerArray() {
        executeSqlFromFile("sql/tsql/createStoreIntegerArray.sql");
    }

    @Override
    protected void createStoreFloatArray() {
        executeSqlFromFile("sql/tsql/createStoreFloatArray.sql");
    }

    @Override
    protected void createStoreDoubleArray() {
        executeSqlFromFile("sql/tsql/createStoreDoubleArray.sql");
    }

    @Override
    protected void createStoreLongArray() {
        executeSqlFromFile("sql/tsql/createStoreLongArray.sql");
    }

    @Override
    protected void createStoreEntities() {
        executeSqlFromFile("sql/tsql/createRefEntities.sql");
    }

    @Override
    protected void createRefEnumValues() {
        executeSqlFromFile("sql/tsql/createRefEnumValues.sql");
    }

    @Override
    protected void createRefFields() {
        executeSqlFromFile("sql/tsql/createRefFields.sql");
    }

    @Override
    protected void createRefFieldTypes() {
        executeSqlFromFile("sql/tsql/createRefFieldTypes.sql");
    }

    @Override
    protected void createBindEntityFields() {
        executeSqlFromFile("sql/tsql/createBindEntityFields.sql");
    }

    @Override
    protected void createBindEntityEnums() {
        executeSqlFromFile("sql/tsql/createBindEntityEnums.sql");
    }

    @Override
    protected void createRefEntityOverview() {
        executeSqlFromFile("sql/tsql/createStoreEntityOverview.sql");
    }

    @Override
    protected void createRefOldFieldValues() {
        executeSqlFromFile("sql/tsql/createStoreOldFieldValues.sql");
    }

    @Override
    protected void createStoreEntityBinding() {
        executeSqlFromFile("sql/tsql/createStoreEntityBinding.sql");
    }

    protected void createStoreTime() {
        executeSqlFromFile("sql/tsql/createStoreTime.sql");
    }


    @Override
    protected void prepareCustomDatabaseComponents() {
        prepareDatabaseComponent(JdsDatabaseComponent.STORED_PROCEDURE, JdsEnumTable.SaveText);
        prepareDatabaseComponent(JdsDatabaseComponent.STORED_PROCEDURE, JdsEnumTable.SaveLong);
        prepareDatabaseComponent(JdsDatabaseComponent.STORED_PROCEDURE, JdsEnumTable.SaveInteger);
        prepareDatabaseComponent(JdsDatabaseComponent.STORED_PROCEDURE, JdsEnumTable.SaveFloat);
        prepareDatabaseComponent(JdsDatabaseComponent.STORED_PROCEDURE, JdsEnumTable.SaveDouble);
        prepareDatabaseComponent(JdsDatabaseComponent.STORED_PROCEDURE, JdsEnumTable.SaveDateTime);
        prepareDatabaseComponent(JdsDatabaseComponent.STORED_PROCEDURE, JdsEnumTable.SaveTime);
        prepareDatabaseComponent(JdsDatabaseComponent.STORED_PROCEDURE, JdsEnumTable.SaveZonedDateTime);
        prepareDatabaseComponent(JdsDatabaseComponent.STORED_PROCEDURE, JdsEnumTable.SaveEntity);
        prepareDatabaseComponent(JdsDatabaseComponent.STORED_PROCEDURE, JdsEnumTable.MapEntityFields);
        prepareDatabaseComponent(JdsDatabaseComponent.STORED_PROCEDURE, JdsEnumTable.MapEntityEnums);
        prepareDatabaseComponent(JdsDatabaseComponent.STORED_PROCEDURE, JdsEnumTable.MapClassName);
        prepareDatabaseComponent(JdsDatabaseComponent.STORED_PROCEDURE, JdsEnumTable.MapEnumValues);
        prepareDatabaseComponent(JdsDatabaseComponent.TRIGGER, JdsEnumTable.CascadeEntityBinding);
    }

    @Override
    protected void prepareCustomDatabaseComponents(JdsEnumTable jdsEnumTable) {
        switch (jdsEnumTable) {
            case SaveTime:
                executeSqlFromFile("sql/tsql/procedures/procStoreTime.sql");
                break;
            case SaveText:
                executeSqlFromFile("sql/tsql/procedures/procStoreText.sql");
                break;
            case SaveLong:
                executeSqlFromFile("sql/tsql/procedures/procStoreLong.sql");
                break;
            case SaveInteger:
                executeSqlFromFile("sql/tsql/procedures/procStoreInteger.sql");
                break;
            case SaveFloat:
                executeSqlFromFile("sql/tsql/procedures/procStoreFloat.sql");
                break;
            case SaveDouble:
                executeSqlFromFile("sql/tsql/procedures/procStoreDouble.sql");
                break;
            case SaveDateTime:
                executeSqlFromFile("sql/tsql/procedures/procStoreDateTime.sql");
                break;
            case SaveZonedDateTime:
                executeSqlFromFile("sql/tsql/procedures/procStoreZonedDateTime.sql");
                break;
            case SaveEntity:
                executeSqlFromFile("sql/tsql/procedures/procStoreEntityOverview.sql");
                break;
            case MapEntityFields:
                executeSqlFromFile("sql/tsql/procedures/procBindEntityFields.sql");
                break;
            case MapEntityEnums:
                executeSqlFromFile("sql/tsql/procedures/procBindEntityEnums.sql");
                break;
            case MapClassName:
                executeSqlFromFile("sql/tsql/procedures/procRefEntities.sql");
                break;
            case MapEnumValues:
                executeSqlFromFile("sql/tsql/procedures/procRefEnumValues.sql");
                break;
            case CascadeEntityBinding:
                executeSqlFromFile("sql/tsql/triggers/createEntityBindingCascade.sql");
                break;
        }
    }
}
