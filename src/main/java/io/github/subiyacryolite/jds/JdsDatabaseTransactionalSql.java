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

import io.github.subiyacryolite.jds.enums.JdsEnumTable;
import io.github.subiyacryolite.jds.enums.JdsImplementation;
import io.github.subiyacryolite.jds.enums.JdsSqlType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by ifunga on 12/02/2017.
 */
public class JdsDatabaseTransactionalSql extends JdsDatabase {

    protected JdsDatabaseTransactionalSql() {
        supportsStatements = true;
        deleteAsFunction = true;
        implementation= JdsImplementation.TSQL;
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
        createTableFromFile("sql/tsql/createStoreText.sql");
    }

    @Override
    protected void createStoreDateTime() {
        createTableFromFile("sql/tsql/createStoreDateTime.sql");
    }

    @Override
    protected void createStoreInteger() {
        createTableFromFile("sql/tsql/createStoreInteger.sql");
    }

    @Override
    protected void createStoreFloat() {
        createTableFromFile("sql/tsql/createStoreFloat.sql");
    }

    @Override
    protected void createStoreDouble() {
        createTableFromFile("sql/tsql/createStoreDouble.sql");
    }

    @Override
    protected void createStoreLong() {
        createTableFromFile("sql/tsql/createStoreLong.sql");
    }

    @Override
    protected void createStoreTextArray() {
        createTableFromFile("sql/tsql/createStoreTextArray.sql");
    }

    @Override
    protected void createStoreDateTimeArray() {
        createTableFromFile("sql/tsql/createStoreDateTimeArray.sql");
    }

    @Override
    protected void createStoreIntegerArray() {
        createTableFromFile("sql/tsql/createStoreIntegerArray.sql");
    }

    @Override
    protected void createStoreFloatArray() {
        createTableFromFile("sql/tsql/createStoreFloatArray.sql");
    }

    @Override
    protected void createStoreDoubleArray() {
        createTableFromFile("sql/tsql/createStoreDoubleArray.sql");
    }

    @Override
    protected void createStoreLongArray() {
        createTableFromFile("sql/tsql/createStoreLongArray.sql");
    }

    @Override
    protected void createStoreEntities() {
        createTableFromFile("sql/tsql/createRefEntities.sql");
    }

    @Override
    protected void createRefEnumValues() {
        createTableFromFile("sql/tsql/createRefEnumValues.sql");
    }

    @Override
    protected void createRefFields() {
        createTableFromFile("sql/tsql/createRefFields.sql");
    }

    @Override
    protected void createRefFieldTypes() {
        createTableFromFile("sql/tsql/createRefFieldTypes.sql");
    }

    @Override
    protected void createBindEntityFields() {
        createTableFromFile("sql/tsql/createBindEntityFields.sql");
    }

    @Override
    protected void createBindEntityEnums() {
        createTableFromFile("sql/tsql/createBindEntityEnums.sql");
    }

    @Override
    protected void createRefEntityOverview() {
        createTableFromFile("sql/tsql/createStoreEntityOverview.sql");
    }

    @Override
    protected void createRefOldFieldValues() {
        createTableFromFile("sql/tsql/createStoreOldFieldValues.sql");
    }

    @Override
    protected void createStoreEntityBinding() {
        createTableFromFile("sql/tsql/createStoreEntityBinding.sql");
    }

    @Override
    protected void initExtra() {
        init(JdsSqlType.STORED_PROCEDURE, JdsEnumTable.SaveText);
        init(JdsSqlType.STORED_PROCEDURE, JdsEnumTable.SaveLong);
        init(JdsSqlType.STORED_PROCEDURE, JdsEnumTable.SaveInteger);
        init(JdsSqlType.STORED_PROCEDURE, JdsEnumTable.SaveFloat);
        init(JdsSqlType.STORED_PROCEDURE, JdsEnumTable.SaveDouble);
        init(JdsSqlType.STORED_PROCEDURE, JdsEnumTable.SaveDateTime);
        init(JdsSqlType.STORED_PROCEDURE, JdsEnumTable.SaveEntity);
        init(JdsSqlType.STORED_PROCEDURE, JdsEnumTable.MapEntityFields);
        init(JdsSqlType.STORED_PROCEDURE, JdsEnumTable.MapEntityEnums);
        init(JdsSqlType.STORED_PROCEDURE, JdsEnumTable.MapClassName);
        init(JdsSqlType.STORED_PROCEDURE, JdsEnumTable.MapEnumValues);
        init(JdsSqlType.TRIGGER, JdsEnumTable.CascadeEntityBinding);
    }

    @Override
    protected void initialiseExtra(JdsEnumTable jdsEnumTable) {
        switch (jdsEnumTable) {
            case SaveText:
                createTableFromFile("sql/tsql/procedures/procStoreText.sql");
                break;
            case SaveLong:
                createTableFromFile("sql/tsql/procedures/procStoreLong.sql");
                break;
            case SaveInteger:
                createTableFromFile("sql/tsql/procedures/procStoreInteger.sql");
                break;
            case SaveFloat:
                createTableFromFile("sql/tsql/procedures/procStoreFloat.sql");
                break;
            case SaveDouble:
                createTableFromFile("sql/tsql/procedures/procStoreDouble.sql");
                break;
            case SaveDateTime:
                createTableFromFile("sql/tsql/procedures/procStoreDateTime.sql");
                break;
            case SaveEntity:
                createTableFromFile("sql/tsql/procedures/procStoreEntityOverview.sql");
                break;
            case MapEntityFields:
                createTableFromFile("sql/tsql/procedures/procBindEntityFields.sql");
                break;
            case MapEntityEnums:
                createTableFromFile("sql/tsql/procedures/procBindEntityEnums.sql");
                break;
            case MapClassName:
                createTableFromFile("sql/tsql/procedures/procRefEntities.sql");
                break;
            case MapEnumValues:
                createTableFromFile("sql/tsql/procedures/procRefEnumValues.sql");
                break;
            case CascadeEntityBinding:
                createTableFromFile("sql/tsql/triggers/createEntityBindingCascade.sql");
                break;
        }
    }
}
