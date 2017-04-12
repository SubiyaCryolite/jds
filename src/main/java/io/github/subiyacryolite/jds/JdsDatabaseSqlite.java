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

import io.github.subiyacryolite.jds.enums.JdsImplementation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by ifunga on 12/02/2017.
 */
public class JdsDatabaseSqlite extends JdsDatabase {

    public JdsDatabaseSqlite() {
        implementation= JdsImplementation.SQLITE;
        supportsStatements = false;
    }

    @Override
    public int tableExists(String tableName) {
        int toReturn = 0;
        String sql = "SELECT COUNT(name) AS Result FROM sqlite_master WHERE type='table' AND name=?;";
        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tableName);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                toReturn = resultSet.getInt("Result");
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
        return toReturn;
    }

    protected void createStoreText() {
        executeSqlFromFile("sql/sqlite/createStoreText.sql");
    }

    protected void createStoreDateTime() {
        executeSqlFromFile("sql/sqlite/createStoreDateTime.sql");
    }

    @Override
    protected void createStoreZonedDateTime() {
        executeSqlFromFile("sql/sqlite/createStoreZonedDateTime.sql");
    }

    protected void createStoreInteger() {
        executeSqlFromFile("sql/sqlite/createStoreInteger.sql");
    }

    protected void createStoreFloat() {
        executeSqlFromFile("sql/sqlite/createStoreFloat.sql");
    }

    protected void createStoreDouble() {
        executeSqlFromFile("sql/sqlite/createStoreDouble.sql");
    }

    protected void createStoreLong() {
        executeSqlFromFile("sql/sqlite/createStoreLong.sql");
    }

    protected void createStoreTextArray() {
        executeSqlFromFile("sql/sqlite/createStoreTextArray.sql");
    }

    protected void createStoreDateTimeArray() {
        executeSqlFromFile("sql/sqlite/createStoreDateTimeArray.sql");
    }

    protected void createStoreIntegerArray() {
        executeSqlFromFile("sql/sqlite/createStoreIntegerArray.sql");
    }

    protected void createStoreFloatArray() {
        executeSqlFromFile("sql/sqlite/createStoreFloatArray.sql");
    }

    protected void createStoreDoubleArray() {
        executeSqlFromFile("sql/sqlite/createStoreDoubleArray.sql");
    }

    protected void createStoreLongArray() {
        executeSqlFromFile("sql/sqlite/createStoreLongArray.sql");
    }

    protected void createStoreEntities() {
        executeSqlFromFile("sql/sqlite/createRefEntities.sql");
    }

    protected void createRefEnumValues() {
        executeSqlFromFile("sql/sqlite/createRefEnumValues.sql");
    }

    protected void createRefFields() {
        executeSqlFromFile("sql/sqlite/createRefFields.sql");
    }

    protected void createRefFieldTypes() {
        executeSqlFromFile("sql/sqlite/createRefFieldTypes.sql");
    }

    protected void createBindEntityFields() {
        executeSqlFromFile("sql/sqlite/createBindEntityFields.sql");
    }

    protected void createBindEntityEnums() {
        executeSqlFromFile("sql/sqlite/createBindEntityEnums.sql");
    }

    protected void createRefEntityOverview() {
        executeSqlFromFile("sql/sqlite/createStoreEntityOverview.sql");
    }

    @Override
    void createRefOldFieldValues() {
        executeSqlFromFile("sql/sqlite/createStoreOldFieldValues.sql");
    }

    @Override
    protected void createStoreEntityBinding() {
        executeSqlFromFile("sql/sqlite/createStoreEntityBinding.sql");
    }

    protected void createStoreTime() {
        executeSqlFromFile("sql/sqlite/createStoreTime.sql");
    }


    public String saveString() {
        return "INSERT OR REPLACE INTO JdsStoreText(EntityGuid,FieldId,Value) VALUES(?,?,?);";
    }

    public String saveLong() {
        return "INSERT OR REPLACE INTO JdsStoreLong(EntityGuid,FieldId,Value) VALUES(?,?,?);";
    }

    public String saveDouble() {
        return "INSERT OR REPLACE INTO JdsStoreDouble(EntityGuid,FieldId,Value) VALUES(?,?,?);";
    }

    public String saveFloat() {
        return "INSERT OR REPLACE INTO JdsStoreFloat(EntityGuid,FieldId,Value) VALUES(?,?,?);";
    }

    public String saveInteger() {
        return "INSERT OR REPLACE INTO JdsStoreInteger(EntityGuid,FieldId,Value) VALUES(?,?,?);";
    }

    public String saveDateTime() {
        return "INSERT OR REPLACE INTO JdsStoreDateTime(EntityGuid,FieldId,Value) VALUES(?,?,?);";
    }

    public String saveTime() {
        return "INSERT OR REPLACE INTO JdsStoreTime(EntityGuid,FieldId,Value) VALUES(?,?,?);";
    }

    public String saveZonedDateTime() {
        return "INSERT OR REPLACE INTO JdsStoreZonedDateTime(EntityGuid,FieldId,Value) VALUES(?,?,?);";
    }

    public String saveOverview() {
        return "INSERT OR REPLACE INTO JdsStoreEntityOverview(EntityGuid,DateCreated,DateModified,EntityId) VALUES(?,?,?,?)";
    }

    public String mapClassFields() {
        return "INSERT OR REPLACE INTO JdsBindEntityFields(EntityId,FieldId) VALUES(?,?);";
    }

    public String mapClassEnumsImplementation() {
        return "INSERT OR REPLACE INTO JdsBindEntityEnums(EntityId,FieldId) VALUES(?,?);";
    }

    public String mapClassName() {
        return "INSERT OR REPLACE INTO JdsRefEntities(EntityId,EntityName) VALUES(?,?);";
    }

    public String mapEnumValues() {
        return "INSERT OR REPLACE INTO JdsRefEnumValues(FieldId,EnumSeq,EnumValue) VALUES(?,?,?);";
    }
}
