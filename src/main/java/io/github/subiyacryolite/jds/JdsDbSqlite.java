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

import com.javaworld.NamedParameterStatement;
import io.github.subiyacryolite.jds.enums.JdsImplementation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * The SQLite implementation of {@link JdsDb JdsDataBase}
 */
public abstract class JdsDbSqlite extends JdsDb {

    protected JdsDbSqlite() {
        implementation = JdsImplementation.SQLITE;
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

    public int columnExists(String tableName, String columnName) {
        String sql = String.format("PRAGMA table_info('%s')", tableName);
        try (Connection connection = getConnection(); NamedParameterStatement preparedStatement = new NamedParameterStatement(connection, sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String column = resultSet.getString("name");
                if (column.equalsIgnoreCase(columnName))
                    return 1; //does exist
            }
            return 0;//doesn't exist
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            return 0;//doesn't exist
        }
    }

    @Override
    protected void createStoreEntityInheritance() {
        executeSqlFromFile("sql/sqlite/createStoreEntityInheritance.sql");
    }

    @Override
    protected void createStoreText() {
        executeSqlFromFile("sql/sqlite/createStoreText.sql");
    }

    @Override
    protected void createStoreDateTime() {
        executeSqlFromFile("sql/sqlite/createStoreDateTime.sql");
    }

    @Override
    protected void createStoreZonedDateTime() {
        executeSqlFromFile("sql/sqlite/createStoreZonedDateTime.sql");
    }

    @Override
    protected void createStoreInteger() {
        executeSqlFromFile("sql/sqlite/createStoreInteger.sql");
    }

    @Override
    protected void createStoreFloat() {
        executeSqlFromFile("sql/sqlite/createStoreFloat.sql");
    }

    @Override
    protected void createStoreDouble() {
        executeSqlFromFile("sql/sqlite/createStoreDouble.sql");
    }

    @Override
    protected void createStoreLong() {
        executeSqlFromFile("sql/sqlite/createStoreLong.sql");
    }

    @Override
    protected void createStoreTextArray() {
        executeSqlFromFile("sql/sqlite/createStoreTextArray.sql");
    }

    @Override
    protected void createStoreDateTimeArray() {
        executeSqlFromFile("sql/sqlite/createStoreDateTimeArray.sql");
    }

    @Override
    protected void createStoreIntegerArray() {
        executeSqlFromFile("sql/sqlite/createStoreIntegerArray.sql");
    }

    @Override
    protected void createStoreFloatArray() {
        executeSqlFromFile("sql/sqlite/createStoreFloatArray.sql");
    }

    @Override
    protected void createStoreDoubleArray() {
        executeSqlFromFile("sql/sqlite/createStoreDoubleArray.sql");
    }

    @Override
    protected void createStoreLongArray() {
        executeSqlFromFile("sql/sqlite/createStoreLongArray.sql");
    }

    @Override
    protected void createStoreEntities() {
        executeSqlFromFile("sql/sqlite/createRefEntities.sql");
    }

    @Override
    protected void createRefEnumValues() {
        executeSqlFromFile("sql/sqlite/createRefEnumValues.sql");
    }

    @Override
    protected void createRefFields() {
        executeSqlFromFile("sql/sqlite/createRefFields.sql");
    }

    @Override
    protected void createRefFieldTypes() {
        executeSqlFromFile("sql/sqlite/createRefFieldTypes.sql");
    }

    @Override
    protected void createBindEntityFields() {
        executeSqlFromFile("sql/sqlite/createBindEntityFields.sql");
    }

    @Override
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

    @Override
    protected void createStoreTime() {
        executeSqlFromFile("sql/sqlite/createStoreTime.sql");
    }

    protected void createStoreBlob() {
        executeSqlFromFile("sql/sqlite/createStoreBlob.sql");
    }

    @Override
    protected void createRefInheritance() {
        executeSqlFromFile("sql/sqlite/createRefInheritance.sql");
    }


    @Override
    public String saveString() {
        return "INSERT OR REPLACE INTO JdsStoreText(EntityGuid,FieldId,Value) VALUES(?,?,?);";
    }

    @Override
    public String saveLong() {
        return "INSERT OR REPLACE INTO JdsStoreLong(EntityGuid,FieldId,Value) VALUES(?,?,?);";
    }

    @Override
    public String saveDouble() {
        return "INSERT OR REPLACE INTO JdsStoreDouble(EntityGuid,FieldId,Value) VALUES(?,?,?);";
    }

    @Override
    public String saveFloat() {
        return "INSERT OR REPLACE INTO JdsStoreFloat(EntityGuid,FieldId,Value) VALUES(?,?,?);";
    }

    @Override
    public String saveInteger() {
        return "INSERT OR REPLACE INTO JdsStoreInteger(EntityGuid,FieldId,Value) VALUES(?,?,?);";
    }

    @Override
    public String saveDateTime() {
        return "INSERT OR REPLACE INTO JdsStoreDateTime(EntityGuid,FieldId,Value) VALUES(?,?,?);";
    }

    @Override
    public String saveTime() {
        return "INSERT OR REPLACE INTO JdsStoreTime(EntityGuid,FieldId,Value) VALUES(?,?,?);";
    }

    @Override
    public String saveBlob() {
        return "INSERT OR REPLACE INTO JdsStoreBlob(EntityGuid,FieldId,Value) VALUES(?,?,?);";
    }

    @Override
    public String saveZonedDateTime() {
        return "INSERT OR REPLACE INTO JdsStoreZonedDateTime(EntityGuid,FieldId,Value) VALUES(?,?,?);";
    }

    @Override
    public String saveOverview() {
        return "INSERT OR REPLACE INTO JdsStoreEntityOverview(EntityGuid,DateCreated,DateModified) VALUES(?,?,?)";
    }

    @Override
    public String saveOverviewInheritance() {
        return "INSERT OR REPLACE INTO JdsStoreEntityInheritance(EntityGuid,EntityId) VALUES(?,?)";
    }

    @Override
    public String mapClassFields() {
        return "INSERT OR REPLACE INTO JdsBindEntityFields(EntityId,FieldId) VALUES(?,?);";
    }

    public String mapFieldNames() {
        return "INSERT OR REPLACE INTO JdsRefFields(FieldId,FieldName) VALUES(?,?);";
    }

    public String mapFieldTypes() {
        return "INSERT OR REPLACE INTO JdsRefFieldTypes(TypeId,TypeName) VALUES(?,?);";
    }

    @Override
    public String mapClassEnumsImplementation() {
        return "INSERT OR REPLACE INTO JdsBindEntityEnums(EntityId,FieldId) VALUES(?,?);";
    }

    @Override
    public String mapClassName() {
        return "INSERT OR REPLACE INTO JdsRefEntities(EntityId,EntityName) VALUES(?,?);";
    }

    @Override
    public String mapEnumValues() {
        return "INSERT OR REPLACE INTO JdsRefEnumValues(FieldId,EnumSeq,EnumValue) VALUES(?,?,?);";
    }

    /**
     * Map parents to child entities
     *
     * @return
     */
    public String mapParentToChild() {
        return "INSERT OR REPLACE INTO JdsRefEntityInheritance(ParentEntityCode,ChildEntityCode) VALUES(?,?);";
    }

    public String createOrAlterView(String viewName, String viewSql) {
        return "";
    }
}
