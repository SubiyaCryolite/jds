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
        createTableFromFile("sql/sqlite/createStoreText.sql");
    }

    protected void createStoreDateTime() {
        createTableFromFile("sql/sqlite/createStoreDateTime.sql");
    }

    protected void createStoreInteger() {
        createTableFromFile("sql/sqlite/createStoreInteger.sql");
    }

    protected void createStoreFloat() {
        createTableFromFile("sql/sqlite/createStoreFloat.sql");
    }

    protected void createStoreDouble() {
        createTableFromFile("sql/sqlite/createStoreDouble.sql");
    }

    protected void createStoreLong() {
        createTableFromFile("sql/sqlite/createStoreLong.sql");
    }

    protected void createStoreTextArray() {
        createTableFromFile("sql/sqlite/createStoreTextArray.sql");
    }

    protected void createStoreDateTimeArray() {
        createTableFromFile("sql/sqlite/createStoreDateTimeArray.sql");
    }

    protected void createStoreIntegerArray() {
        createTableFromFile("sql/sqlite/createStoreIntegerArray.sql");
    }

    protected void createStoreFloatArray() {
        createTableFromFile("sql/sqlite/createStoreFloatArray.sql");
    }

    protected void createStoreDoubleArray() {
        createTableFromFile("sql/sqlite/createStoreDoubleArray.sql");
    }

    protected void createStoreLongArray() {
        createTableFromFile("sql/sqlite/createStoreLongArray.sql");
    }

    protected void createStoreEntities() {
        createTableFromFile("sql/sqlite/createRefEntities.sql");
    }

    protected void createRefEnumValues() {
        createTableFromFile("sql/sqlite/createRefEnumValues.sql");
    }

    protected void createRefFields() {
        createTableFromFile("sql/sqlite/createRefFields.sql");
    }

    protected void createRefFieldTypes() {
        createTableFromFile("sql/sqlite/createRefFieldTypes.sql");
    }

    protected void createBindEntityFields() {
        createTableFromFile("sql/sqlite/createBindEntityFields.sql");
    }

    protected void createBindEntityEnums() {
        createTableFromFile("sql/sqlite/createBindEntityEnums.sql");
    }

    protected void createRefEntityOverview() {
        createTableFromFile("sql/sqlite/createStoreEntityOverview.sql");
    }

    @Override
    void createRefOldFieldValues() {
        createTableFromFile("sql/sqlite/createStoreOldFieldValues.sql");
    }

    @Override
    protected void createStoreEntityBinding() {
        createTableFromFile("sql/sqlite/createStoreEntityBinding.sql");
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

    public String saveOverview() {
        return "INSERT OR REPLACE INTO JdsStoreEntityOverview(EntityGuid,DateCreated,DateModified,EntityId) VALUES(?,?,?,?)";
    }

    public String mapClassFields() {
        return "INSERT OR REPLACE INTO JdsBindEntityFields(EntityId,FieldId) VALUES(?,?);";
    }

    public String mapEntityEnums() {
        return "INSERT OR REPLACE INTO JdsBindEntityEnums(EntityId,FieldId) VALUES(?,?);";
    }

    public String mapClassName() {
        return "INSERT OR REPLACE INTO JdsRefEntities(EntityId,EntityName) VALUES(?,?);";
    }

    public String mapEnumValues() {
        return "INSERT OR REPLACE INTO JdsRefEnumValues(FieldId,EnumSeq,EnumValue) VALUES(?,?,?);";
    }
}
