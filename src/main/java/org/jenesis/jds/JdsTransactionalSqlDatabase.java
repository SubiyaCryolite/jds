package org.jenesis.jds;

import org.jenesis.jds.enums.JdsTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by ifunga on 12/02/2017.
 */
public class JdsTransactionalSqlDatabase extends JdsDatabase {

    protected JdsTransactionalSqlDatabase() {
        supportsStatements = true;
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
        String sql = "IF (EXISTS (SELECT * FROM sysobjects WHERE NAME = ?)) SELECT 1 AS Result ELSE SELECT 0 AS Result ";
        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, procedureName);
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
    protected void createStoreEntitySubclass() {
        createTableFromFile("sql/tsql/createStoreEntitySubclass.sql");
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
        createTableFromFile("sql/tsql/createRefEntityOverview.sql");
    }

    @Override
    protected void createRefOldFieldValues() {
        createTableFromFile("sql/tsql/createStoreOldFieldValues.sql");
    }


    @Override
    protected void initExtra() {
        init(false, JdsTable.SaveText);
        init(false, JdsTable.SaveLong);
        init(false, JdsTable.SaveInteger);
        init(false, JdsTable.SaveFloat);
        init(false, JdsTable.SaveDouble);
        init(false, JdsTable.SaveDateTime);
        init(false, JdsTable.SaveOverview);
    }

    @Override
    protected void createTableExtra(JdsTable jdsTable) {
        switch (jdsTable) {
            case SaveText:
                createTableFromFile("sql/tsql/procedures/procJdsStoreText.sql");
                break;
            case SaveLong:
                createTableFromFile("sql/tsql/procedures/procJdsStoreLong.sql");
                break;
            case SaveInteger:
                createTableFromFile("sql/tsql/procedures/procJdsStoreInteger.sql");
                break;
            case SaveFloat:
                createTableFromFile("sql/tsql/procedures/procJdsStoreFloat.sql");
                break;
            case SaveDouble:
                createTableFromFile("sql/tsql/procedures/procJdsStoreDouble.sql");
                break;
            case SaveDateTime:
                createTableFromFile("sql/tsql/procedures/procJdsStoreDateTime.sql");
                break;
            case SaveOverview:
                createTableFromFile("sql/tsql/procedures/procJdsRefEntityOverview.sql");
                break;
        }
    }
}
