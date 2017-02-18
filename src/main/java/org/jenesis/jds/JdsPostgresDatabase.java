package org.jenesis.jds;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by ifunga on 12/02/2017.
 */
public class JdsPostgresDatabase extends JdsDatabase {
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

    protected void createStoreText() {
        createTableFromFile("sql/postgres/createStoreText.sql");
    }

    protected void createStoreDateTime() {
        createTableFromFile("sql/postgres/createStoreDateTime.sql");
    }

    protected void createStoreInteger() {
        createTableFromFile("sql/postgres/createStoreInteger.sql");
    }

    protected void createStoreFloat() {
        createTableFromFile("sql/postgres/createStoreFloat.sql");
    }

    protected void createStoreDouble() {
        createTableFromFile("sql/postgres/createStoreDouble.sql");
    }

    protected void createStoreLong() {
        createTableFromFile("sql/postgres/createStoreLong.sql");
    }

    protected void createStoreTextArray() {
        createTableFromFile("sql/postgres/createStoreTextArray.sql");
    }

    protected void createStoreDateTimeArray() {
        createTableFromFile("sql/postgres/createStoreDateTimeArray.sql");
    }

    protected void createStoreIntegerArray() {
        createTableFromFile("sql/postgres/createStoreIntegerArray.sql");
    }

    protected void createStoreFloatArray() {
        createTableFromFile("sql/postgres/createStoreFloatArray.sql");
    }

    protected void createStoreDoubleArray() {
        createTableFromFile("sql/postgres/createStoreDoubleArray.sql");
    }

    protected void createStoreLongArray() {
        createTableFromFile("sql/postgres/createStoreLongArray.sql");
    }

    protected void createStoreEntities() {
        createTableFromFile("sql/postgres/createRefEntities.sql");
    }

    protected void createStoreEntitySubclass() {
        createTableFromFile("sql/postgres/createStoreEntitySubclass.sql");
    }

    protected void createRefEnumValues() {
        createTableFromFile("sql/postgres/createRefEnumValues.sql");
    }

    protected void createRefFields() {
        createTableFromFile("sql/postgres/createRefFields.sql");
    }

    protected void createRefFieldTypes() {
        createTableFromFile("sql/postgres/createRefFieldTypes.sql");
    }

    protected void createBindEntityFields() {
        createTableFromFile("sql/postgres/createBindEntityFields.sql");
    }

    protected void createBindEntityEnums() {
        createTableFromFile("sql/postgres/createBindEntityEnums.sql");
    }

    protected void createRefEntityOverview() {
        createTableFromFile("sql/postgres/createRefEntityOverview.sql");
    }

    @Override
    void createRefOldFieldValues() {
        createTableFromFile("sql/postgres/createStoreOldFieldValues.sql");
    }
}
