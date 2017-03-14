package org.jenesis.jds;

import org.jenesis.jds.enums.JdsEnumTable;
import org.jenesis.jds.enums.JdsImplementation;
import org.jenesis.jds.enums.JdsSqlType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by ifunga on 14/03/2017.
 */
public class JdsDatabaseMySql extends JdsDatabase {

    protected JdsDatabaseMySql() {
        supportsStatements = true;
        implementation= JdsImplementation.MYSQL;
    }

    @Override
    public int tableExists(String tableName) {
        int toReturn = 0;
        String sql = "SELECT COUNT(table_schema) AS Result FROM information_schema.tables WHERE table_name = ?";
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
        String sql = "SELECT COUNT(ROUTINE_NAME) AS Result FROM INFORMATION_SCHEMA.ROUTINES WHERE ROUTINE_TYPE='PROCEDURE' AND ROUTINE_NAME = ?";
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
        createTableFromFile("sql/mysql/createStoreText.sql");
    }

    @Override
    protected void createStoreDateTime() {
        createTableFromFile("sql/mysql/createStoreDateTime.sql");
    }

    @Override
    protected void createStoreInteger() {
        createTableFromFile("sql/mysql/createStoreInteger.sql");
    }

    @Override
    protected void createStoreFloat() {
        createTableFromFile("sql/mysql/createStoreFloat.sql");
    }

    @Override
    protected void createStoreDouble() {
        createTableFromFile("sql/mysql/createStoreDouble.sql");
    }

    @Override
    protected void createStoreLong() {
        createTableFromFile("sql/mysql/createStoreLong.sql");
    }

    @Override
    protected void createStoreTextArray() {
        createTableFromFile("sql/mysql/createStoreTextArray.sql");
    }

    @Override
    protected void createStoreDateTimeArray() {
        createTableFromFile("sql/mysql/createStoreDateTimeArray.sql");
    }

    @Override
    protected void createStoreIntegerArray() {
        createTableFromFile("sql/mysql/createStoreIntegerArray.sql");
    }

    @Override
    protected void createStoreFloatArray() {
        createTableFromFile("sql/mysql/createStoreFloatArray.sql");
    }

    @Override
    protected void createStoreDoubleArray() {
        createTableFromFile("sql/mysql/createStoreDoubleArray.sql");
    }

    @Override
    protected void createStoreLongArray() {
        createTableFromFile("sql/mysql/createStoreLongArray.sql");
    }

    @Override
    protected void createStoreEntities() {
        createTableFromFile("sql/mysql/createRefEntities.sql");
    }

    @Override
    protected void createRefEnumValues() {
        createTableFromFile("sql/mysql/createRefEnumValues.sql");
    }

    @Override
    protected void createRefFields() {
        createTableFromFile("sql/mysql/createRefFields.sql");
    }

    @Override
    protected void createRefFieldTypes() {
        createTableFromFile("sql/mysql/createRefFieldTypes.sql");
    }

    @Override
    protected void createBindEntityFields() {
        createTableFromFile("sql/mysql/createBindEntityFields.sql");
    }

    @Override
    protected void createBindEntityEnums() {
        createTableFromFile("sql/mysql/createBindEntityEnums.sql");
    }

    @Override
    protected void createRefEntityOverview() {
        createTableFromFile("sql/mysql/createStoreEntityOverview.sql");
    }

    @Override
    protected void createRefOldFieldValues() {
        createTableFromFile("sql/mysql/createStoreOldFieldValues.sql");
    }

    @Override
    protected void createStoreEntityBinding() {
        createTableFromFile("sql/mysql/createStoreEntityBinding.sql");
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
                createTableFromFile("sql/mysql/procedures/procStoreText.sql");
                break;
            case SaveLong:
                createTableFromFile("sql/mysql/procedures/procStoreLong.sql");
                break;
            case SaveInteger:
                createTableFromFile("sql/mysql/procedures/procStoreInteger.sql");
                break;
            case SaveFloat:
                createTableFromFile("sql/mysql/procedures/procStoreFloat.sql");
                break;
            case SaveDouble:
                createTableFromFile("sql/mysql/procedures/procStoreDouble.sql");
                break;
            case SaveDateTime:
                createTableFromFile("sql/mysql/procedures/procStoreDateTime.sql");
                break;
            case SaveEntity:
                createTableFromFile("sql/mysql/procedures/procStoreEntityOverview.sql");
                break;
            case MapEntityFields:
                createTableFromFile("sql/mysql/procedures/procBindEntityFields.sql");
                break;
            case MapEntityEnums:
                createTableFromFile("sql/mysql/procedures/procBindEntityEnums.sql");
                break;
            case MapClassName:
                createTableFromFile("sql/mysql/procedures/procRefEntities.sql");
                break;
            case MapEnumValues:
                createTableFromFile("sql/mysql/procedures/procRefEnumValues.sql");
                break;
            case CascadeEntityBinding:
                createTableFromFile("sql/mysql/triggers/createEntityBindingCascade.sql");
                break;
        }
    }
}