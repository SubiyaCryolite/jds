package org.jenesis.jds;

import org.jenesis.jds.enums.JdsTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by ifunga on 12/02/2017.
 */
public class JdsPostgresDatabase extends JdsDatabase {

    public JdsPostgresDatabase() {
        supportsStatements = true;
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
        createTableFromFile("sql/postgresql/createStoreText.sql");
    }

    @Override
    protected void createStoreDateTime() {
        createTableFromFile("sql/postgresql/createStoreDateTime.sql");
    }

    @Override
    protected void createStoreInteger() {
        createTableFromFile("sql/postgresql/createStoreInteger.sql");
    }

    @Override
    protected void createStoreFloat() {
        createTableFromFile("sql/postgresql/createStoreFloat.sql");
    }

    @Override
    protected void createStoreDouble() {
        createTableFromFile("sql/postgresql/createStoreDouble.sql");
    }

    @Override
    protected void createStoreLong() {
        createTableFromFile("sql/postgresql/createStoreLong.sql");
    }

    @Override
    protected void createStoreTextArray() {
        createTableFromFile("sql/postgresql/createStoreTextArray.sql");
    }

    @Override
    protected void createStoreDateTimeArray() {
        createTableFromFile("sql/postgresql/createStoreDateTimeArray.sql");
    }

    @Override
    protected void createStoreIntegerArray() {
        createTableFromFile("sql/postgresql/createStoreIntegerArray.sql");
    }

    @Override
    protected void createStoreFloatArray() {
        createTableFromFile("sql/postgresql/createStoreFloatArray.sql");
    }

    @Override
    protected void createStoreDoubleArray() {
        createTableFromFile("sql/postgresql/createStoreDoubleArray.sql");
    }

    @Override
    protected void createStoreLongArray() {
        createTableFromFile("sql/postgresql/createStoreLongArray.sql");
    }

    @Override
    protected void createStoreEntities() {
        createTableFromFile("sql/postgresql/createRefEntities.sql");
    }

    @Override
    protected void createStoreEntitySubclass() {
        createTableFromFile("sql/postgresql/createStoreEntitySubclass.sql");
    }

    @Override
    protected void createRefEnumValues() {
        createTableFromFile("sql/postgresql/createRefEnumValues.sql");
    }

    @Override
    protected void createRefFields() {
        createTableFromFile("sql/postgresql/createRefFields.sql");
    }

    @Override
    protected void createRefFieldTypes() {
        createTableFromFile("sql/postgresql/createRefFieldTypes.sql");
    }

    @Override
    protected void createBindEntityFields() {
        createTableFromFile("sql/postgresql/createBindEntityFields.sql");
    }

    @Override
    protected void createBindEntityEnums() {
        createTableFromFile("sql/postgresql/createBindEntityEnums.sql");
    }

    @Override
    protected void createRefEntityOverview() {
        createTableFromFile("sql/postgresql/createRefEntityOverview.sql");
    }

    @Override
    protected void createRefOldFieldValues() {
        createTableFromFile("sql/postgresql/createStoreOldFieldValues.sql");
    }

    @Override
    protected void initExtra() {
        init(false, JdsTable.SaveText);
        init(false, JdsTable.SaveLong);
        init(false, JdsTable.SaveInteger);
        init(false, JdsTable.SaveFloat);
        init(false, JdsTable.SaveDouble);
        init(false, JdsTable.SaveDateTime);
    }

    @Override
    protected void createTableExtra(JdsTable jdsTable) {
        switch (jdsTable) {
            case SaveText:
                createTableFromFile("sql/postgresql/procedures/procJdsStoreText.sql");
                break;
            case SaveLong:
                createTableFromFile("sql/postgresql/procedures/procJdsStoreLong.sql");
                break;
            case SaveInteger:
                createTableFromFile("sql/postgresql/procedures/procJdsStoreInteger.sql");
                break;
            case SaveFloat:
                createTableFromFile("sql/postgresql/procedures/procJdsStoreFloat.sql");
                break;
            case SaveDouble:
                createTableFromFile("sql/postgresql/procedures/procJdsStoreDouble.sql");
                break;
            case SaveDateTime:
                createTableFromFile("sql/postgresql/procedures/procJdsStoreDateTime.sql");
                break;
        }
    }
}
