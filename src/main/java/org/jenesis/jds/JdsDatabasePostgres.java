package org.jenesis.jds;

import org.jenesis.jds.enums.JdsEnumTable;
import org.jenesis.jds.enums.JdsImplementation;
import org.jenesis.jds.enums.JdsSqlType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by ifunga on 12/02/2017.
 */
public class JdsDatabasePostgres extends JdsDatabase {

    public JdsDatabasePostgres() {
        supportsStatements = true;
        implementation= JdsImplementation.POSTGRES;
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
        createTableFromFile("sql/postgresql/createStoreEntityOverview.sql");
    }

    @Override
    protected void createRefOldFieldValues() {
        createTableFromFile("sql/postgresql/createStoreOldFieldValues.sql");
    }

    @Override
    protected void createStoreEntityBinding() {
        createTableFromFile("sql/postgresql/createStoreEntityBinding.sql");
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
    }

    @Override
    protected void initialiseExtra(JdsEnumTable jdsEnumTable) {
        switch (jdsEnumTable) {
            case SaveText:
                createTableFromFile("sql/postgresql/procedures/procStoreText.sql");
                break;
            case SaveLong:
                createTableFromFile("sql/postgresql/procedures/procStoreLong.sql");
                break;
            case SaveInteger:
                createTableFromFile("sql/postgresql/procedures/procStoreInteger.sql");
                break;
            case SaveFloat:
                createTableFromFile("sql/postgresql/procedures/procStoreFloat.sql");
                break;
            case SaveDouble:
                createTableFromFile("sql/postgresql/procedures/procStoreDouble.sql");
                break;
            case SaveDateTime:
                createTableFromFile("sql/postgresql/procedures/procStoreDateTime.sql");
                break;
            case SaveEntity:
                createTableFromFile("sql/postgresql/procedures/procStoreEntityOverview.sql");
                break;
            case MapEntityFields:
                createTableFromFile("sql/postgresql/procedures/procBindEntityFields.sql");
                break;
            case MapEntityEnums:
                createTableFromFile("sql/postgresql/procedures/procBindEntityEnums.sql");
                break;
            case MapClassName:
                createTableFromFile("sql/postgresql/procedures/procRefEntities.sql");
                break;
            case MapEnumValues:
                createTableFromFile("sql/postgresql/procedures/procRefEnumValues.sql");
                break;
        }
    }
}
