package org.jenesis.jds.entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by ifunga on 12/02/2017.
 */
public class JdsTransactionalSqlDatabase extends JdsDatabase {

    protected JdsTransactionalSqlDatabase()
    {}

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

    protected void createStoreText() {
        createTableFromFile("sql/tsql/createStoreText.sql");
    }

    protected void createStoreDateTime() {
        createTableFromFile("sql/tsql/createStoreDateTime.sql");
    }

    protected void createStoreInteger() {
        createTableFromFile("sql/tsql/createStoreInteger.sql");
    }

    protected void createStoreFloat() {
        createTableFromFile("sql/tsql/createStoreFloat.sql");
    }

    protected void createStoreDouble() {
        createTableFromFile("sql/tsql/createStoreDouble.sql");
    }

    protected void createStoreLong() {
        createTableFromFile("sql/tsql/createStoreLong.sql");
    }

    protected void createStoreTextArray() {
        createTableFromFile("sql/tsql/createStoreTextArray.sql");
    }

    protected void createStoreDateTimeArray() {
        createTableFromFile("sql/tsql/createStoreDateTimeArray.sql");
    }

    protected void createStoreIntegerArray() {
        createTableFromFile("sql/tsql/createStoreIntegerArray.sql");
    }

    protected void createStoreFloatArray() {
        createTableFromFile("sql/tsql/createStoreFloatArray.sql");
    }

    protected void createStoreDoubleArray() {
        createTableFromFile("sql/tsql/createStoreDoubleArray.sql");
    }

    protected void createStoreLongArray() {
        createTableFromFile("sql/tsql/createStoreLongArray.sql");
    }

    protected void createStoreEntities() {
        createTableFromFile("sql/tsql/createStoreEntities.sql");
    }

    protected void createStoreEntitySubclass() {
        createTableFromFile("sql/tsql/createStoreEntitySubclass.sql");
    }

    protected void createRefEnumValues() {
        createTableFromFile("sql/tsql/createRefEnumValues.sql");
    }

    protected void createRefFields() {
        createTableFromFile("sql/tsql/createRefFields.sql");
    }

    protected void createRefFieldTypes() {
        createTableFromFile("sql/tsql/createRefFieldTypes.sql");
    }

    protected void createBindEntityFields() {
        createTableFromFile("sql/tsql/createBindEntityFields.sql");
    }

    protected void createBindEntityEnums() {
        createTableFromFile("sql/tsql/createBindEntityEnums.sql");
    }

    protected void createRefEntityOverview() {
        createTableFromFile("sql/tsql/createRefEntityOverview.sql");
    }

    @Override
    void createRefOldFieldValues() {
        createTableFromFile("sql/tsql/createRefOldFieldValues.sql");
    }
}
