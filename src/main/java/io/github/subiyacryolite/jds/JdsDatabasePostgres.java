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
public class JdsDatabasePostgres extends JdsDatabase {

    public JdsDatabasePostgres() {
        supportsStatements = true;
        implementation = JdsImplementation.POSTGRES;
    }

    @Override
    public int tableExists(String tableName) {
        int toReturn = 0;
        String sql = "SELECT COUNT(*) AS Result FROM information_schema.tables where table_name = ?";
        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tableName.toLowerCase());
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
        String sql = "SELECT COUNT(*) AS Result FROM pg_proc WHERE proname = ?";
        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, procedureName.toLowerCase());
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

    @Override
    protected void prepareCustomDatabaseComponents() {
        prepareDatabaseComponent(JdsDatabaseComponent.STORED_PROCEDURE, JdsEnumTable.SaveText);
        prepareDatabaseComponent(JdsDatabaseComponent.STORED_PROCEDURE, JdsEnumTable.SaveLong);
        prepareDatabaseComponent(JdsDatabaseComponent.STORED_PROCEDURE, JdsEnumTable.SaveInteger);
        prepareDatabaseComponent(JdsDatabaseComponent.STORED_PROCEDURE, JdsEnumTable.SaveFloat);
        prepareDatabaseComponent(JdsDatabaseComponent.STORED_PROCEDURE, JdsEnumTable.SaveDouble);
        prepareDatabaseComponent(JdsDatabaseComponent.STORED_PROCEDURE, JdsEnumTable.SaveDateTime);
        prepareDatabaseComponent(JdsDatabaseComponent.STORED_PROCEDURE, JdsEnumTable.SaveZonedDateTime);
        prepareDatabaseComponent(JdsDatabaseComponent.STORED_PROCEDURE, JdsEnumTable.SaveEntity);
        prepareDatabaseComponent(JdsDatabaseComponent.STORED_PROCEDURE, JdsEnumTable.MapEntityFields);
        prepareDatabaseComponent(JdsDatabaseComponent.STORED_PROCEDURE, JdsEnumTable.MapEntityEnums);
        prepareDatabaseComponent(JdsDatabaseComponent.STORED_PROCEDURE, JdsEnumTable.MapClassName);
        prepareDatabaseComponent(JdsDatabaseComponent.STORED_PROCEDURE, JdsEnumTable.MapEnumValues);
    }

    @Override
    protected void prepareCustomDatabaseComponents(JdsEnumTable jdsEnumTable) {
        switch (jdsEnumTable) {
            case SaveText:
                executeSqlFromFile("sql/postgresql/procedures/procStoreText.sql");
                break;
            case SaveLong:
                executeSqlFromFile("sql/postgresql/procedures/procStoreLong.sql");
                break;
            case SaveInteger:
                executeSqlFromFile("sql/postgresql/procedures/procStoreInteger.sql");
                break;
            case SaveFloat:
                executeSqlFromFile("sql/postgresql/procedures/procStoreFloat.sql");
                break;
            case SaveDouble:
                executeSqlFromFile("sql/postgresql/procedures/procStoreDouble.sql");
                break;
            case SaveDateTime:
                executeSqlFromFile("sql/postgresql/procedures/procStoreDateTime.sql");
                break;
            case SaveZonedDateTime:
                executeSqlFromFile("sql/postgresql/procedures/procStoreZonedDateTime.sql");
                break;
            case SaveEntity:
                executeSqlFromFile("sql/postgresql/procedures/procStoreEntityOverview.sql");
                break;
            case MapEntityFields:
                executeSqlFromFile("sql/postgresql/procedures/procBindEntityFields.sql");
                break;
            case MapEntityEnums:
                executeSqlFromFile("sql/postgresql/procedures/procBindEntityEnums.sql");
                break;
            case MapClassName:
                executeSqlFromFile("sql/postgresql/procedures/procRefEntities.sql");
                break;
            case MapEnumValues:
                executeSqlFromFile("sql/postgresql/procedures/procRefEnumValues.sql");
                break;
        }
    }
}
