package io.github.subiyacryolite.jds;

import com.javaworld.NamedPreparedStatement;
import io.github.subiyacryolite.jds.enums.JdsComponent;
import io.github.subiyacryolite.jds.enums.JdsComponentType;
import io.github.subiyacryolite.jds.enums.JdsImplementation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by ifunga on 14/07/2017.
 */
public abstract class JdsDbOracle extends JdsDb {

    public JdsDbOracle() {
        supportsStatements = true;
        implementation = JdsImplementation.ORACLE;
    }

    @Override
    public int tableExists(Connection connection, String tableName) {
        int toReturn = 0;
        String sql = "SELECT COUNT(*) AS Result FROM all_objects WHERE object_type IN ('TABLE') AND object_name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tableName.toUpperCase());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    toReturn = resultSet.getInt("Result");
                }
            }
        } catch (Exception ex) {
            toReturn = 0;
            ex.printStackTrace(System.err);
        }
        return toReturn;
    }

    @Override
    public int viewExists(Connection connection, String viewName) {
        int toReturn = 0;
        String sql = "SELECT COUNT(*) AS Result FROM all_objects WHERE object_type IN ('VIEW') AND object_name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, viewName.toUpperCase());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    toReturn = resultSet.getInt("Result");
                }
            }
        } catch (Exception ex) {
            toReturn = 0;
            ex.printStackTrace(System.err);
        }
        return toReturn;
    }

    @Override
    public int procedureExists(Connection connection, String procedureName) {
        int toReturn = 0;
        String sql = "SELECT COUNT(*) AS Result FROM all_objects WHERE object_type IN ('PROCEDURE') AND object_name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, procedureName.toUpperCase());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    toReturn = resultSet.getInt("Result");
                }
            }
        } catch (Exception ex) {
            toReturn = 0;
            ex.printStackTrace(System.err);
        }
        return toReturn;
    }

    @Override
    public int columnExists(Connection connection, String tableName, String columnName) {
        int toReturn = 0;
        String sql = "SELECT COUNT(COLUMN_NAME) AS Result FROM ALL_TAB_COLUMNS WHERE TABLE_NAME = :tableName AND COLUMN_NAME = :columnName";
        try (NamedPreparedStatement preparedStatement = new NamedPreparedStatement(connection, sql)) {
            preparedStatement.setString("tableName", tableName.toUpperCase());
            preparedStatement.setString("columnName", columnName.toUpperCase());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    toReturn = resultSet.getInt("Result");
                }
            }
        } catch (Exception ex) {
            toReturn = 0;
            ex.printStackTrace(System.err);
        }
        return toReturn;
    }

    public String createOrAlterView(String viewName, String viewSql) {
        StringBuilder sb = new StringBuilder("CREATE VIEW\t");
        sb.append(viewName);
        sb.append("\tAS\t");
        sb.append(viewSql);
        String toExecute = sb.toString();
        return toExecute;
    }

    @Override
    protected void createStoreEntityInheritance(Connection connection) {
        executeSqlFromFile(connection, "sql/oracle/createStoreEntityInheritance.sql");
    }

    @Override
    protected void createStoreText(Connection connection) {
        executeSqlFromFile(connection, "sql/oracle/createStoreText.sql");
    }

    @Override
    protected void createStoreDateTime(Connection connection) {
        executeSqlFromFile(connection, "sql/oracle/createStoreDateTime.sql");
    }

    @Override
    protected void createStoreZonedDateTime(Connection connection) {
        executeSqlFromFile(connection, "sql/oracle/createStoreZonedDateTime.sql");
    }

    @Override
    protected void createStoreInteger(Connection connection) {
        executeSqlFromFile(connection, "sql/oracle/createStoreInteger.sql");
    }

    @Override
    protected void createStoreFloat(Connection connection) {
        executeSqlFromFile(connection, "sql/oracle/createStoreFloat.sql");
    }

    @Override
    protected void createStoreDouble(Connection connection) {
        executeSqlFromFile(connection, "sql/oracle/createStoreDouble.sql");
    }

    @Override
    protected void createStoreLong(Connection connection) {
        executeSqlFromFile(connection, "sql/oracle/createStoreLong.sql");
    }

    @Override
    protected void createStoreTextArray(Connection connection) {
        executeSqlFromFile(connection, "sql/oracle/createStoreTextArray.sql");
    }

    @Override
    protected void createStoreDateTimeArray(Connection connection) {
        executeSqlFromFile(connection, "sql/oracle/createStoreDateTimeArray.sql");
    }

    @Override
    protected void createStoreIntegerArray(Connection connection) {
        executeSqlFromFile(connection, "sql/oracle/createStoreIntegerArray.sql");
    }

    @Override
    protected void createStoreFloatArray(Connection connection) {
        executeSqlFromFile(connection, "sql/oracle/createStoreFloatArray.sql");
    }

    @Override
    protected void createStoreDoubleArray(Connection connection) {
        executeSqlFromFile(connection, "sql/oracle/createStoreDoubleArray.sql");
    }

    @Override
    protected void createStoreLongArray(Connection connection) {
        executeSqlFromFile(connection, "sql/oracle/createStoreLongArray.sql");
    }

    @Override
    protected void createStoreEntities(Connection connection) {
        executeSqlFromFile(connection, "sql/oracle/createRefEntities.sql");
    }

    @Override
    protected void createRefEnumValues(Connection connection) {
        executeSqlFromFile(connection, "sql/oracle/createRefEnumValues.sql");
    }

    @Override
    protected void createRefFields(Connection connection) {
        executeSqlFromFile(connection, "sql/oracle/createRefFields.sql");
    }

    @Override
    protected void createRefFieldTypes(Connection connection) {
        executeSqlFromFile(connection, "sql/oracle/createRefFieldTypes.sql");
    }

    @Override
    protected void createBindEntityFields(Connection connection) {
        executeSqlFromFile(connection, "sql/oracle/createBindEntityFields.sql");
    }

    @Override
    protected void createBindEntityEnums(Connection connection) {
        executeSqlFromFile(connection, "sql/oracle/createBindEntityEnums.sql");
    }

    @Override
    protected void createRefEntityOverview(Connection connection) {
        executeSqlFromFile(connection, "sql/oracle/createStoreEntityOverview.sql");
    }

    @Override
    protected void createRefOldFieldValues(Connection connection) {
        executeSqlFromFile(connection, "sql/oracle/createStoreOldFieldValues.sql");
        //allow multiple leaves you open to SLQ injection. Thus manually add these indexes here unless you want to add more files
        //oracle jdbc hates semi-colons
        executeSqlFromString(connection, "CREATE INDEX IntegerValues  ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, IntegerValue)");
        executeSqlFromString(connection, "CREATE INDEX FloatValues    ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, FloatValue)");
        executeSqlFromString(connection, "CREATE INDEX DoubleValues   ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, DoubleValue)");
        executeSqlFromString(connection, "CREATE INDEX LongValues     ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, LongValue)");
        executeSqlFromString(connection, "CREATE INDEX DateTimeValues ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, DateTimeValue)");
        executeSqlFromString(connection, "CREATE INDEX TextBlobValues ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence)");
    }

    @Override
    protected void createStoreEntityBinding(Connection connection) {
        executeSqlFromFile(connection, "sql/oracle/createStoreEntityBinding.sql");
    }

    protected void createStoreTime(Connection connection) {
        executeSqlFromFile(connection, "sql/oracle/createStoreTime.sql");
    }

    protected void createStoreBlob(Connection connection) {
        executeSqlFromFile(connection, "sql/oracle/createStoreBlob.sql");
    }

    @Override
    protected void createRefInheritance(Connection connection) {
        executeSqlFromFile(connection, "sql/oracle/createRefInheritance.sql");
    }

    @Override
    protected void prepareCustomDatabaseComponents(Connection connection) {
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_BLOB);
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_TEXT);
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_LONG);
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_INTEGER);
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_FLOAT);
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_DOUBLE);
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_DATE_TIME);
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_TIME);
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_ZONED_DATE_TIME);
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_ENTITY_V_2);
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.MAP_ENTITY_FIELDS);
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.MAP_ENTITY_ENUMS);
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.MAP_CLASS_NAME);
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.MAP_ENUM_VALUES);
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.MAP_FIELD_NAMES);
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.MAP_FIELD_TYPES);
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.MAP_ENTITY_INHERITANCE);
        prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.SAVE_ENTITY_INHERITANCE);
    }

    @Override
    protected void prepareCustomDatabaseComponents(Connection connection, JdsComponent jdsComponent) {
        switch (jdsComponent) {
            case SAVE_ENTITY_V_2:
                executeSqlFromFile(connection, "sql/oracle/procedures/procStoreEntityOverviewV2.sql");
                break;
            case SAVE_ENTITY_INHERITANCE:
                executeSqlFromFile(connection, "sql/oracle/procedures/procStoreEntityInheritance.sql");
                break;
            case MAP_FIELD_NAMES:
                executeSqlFromFile(connection, "sql/oracle/procedures/procBindFieldNames.sql");
                break;
            case MAP_FIELD_TYPES:
                executeSqlFromFile(connection, "sql/oracle/procedures/procBindFieldTypes.sql");
                break;
            case SAVE_BLOB:
                executeSqlFromFile(connection, "sql/oracle/procedures/procStoreBlob.sql");
                break;
            case SAVE_TIME:
                executeSqlFromFile(connection, "sql/oracle/procedures/procStoreTime.sql");
                break;
            case SAVE_TEXT:
                executeSqlFromFile(connection, "sql/oracle/procedures/procStoreText.sql");
                break;
            case SAVE_LONG:
                executeSqlFromFile(connection, "sql/oracle/procedures/procStoreLong.sql");
                break;
            case SAVE_INTEGER:
                executeSqlFromFile(connection, "sql/oracle/procedures/procStoreInteger.sql");
                break;
            case SAVE_FLOAT:
                executeSqlFromFile(connection, "sql/oracle/procedures/procStoreFloat.sql");
                break;
            case SAVE_DOUBLE:
                executeSqlFromFile(connection, "sql/oracle/procedures/procStoreDouble.sql");
                break;
            case SAVE_DATE_TIME:
                executeSqlFromFile(connection, "sql/oracle/procedures/procStoreDateTime.sql");
                break;
            case SAVE_ZONED_DATE_TIME:
                executeSqlFromFile(connection, "sql/oracle/procedures/procStoreZonedDateTime.sql");
                break;
            case SAVE_ENTITY:
                executeSqlFromFile(connection, "sql/oracle/procedures/procStoreEntityOverview.sql");
                break;
            case MAP_ENTITY_FIELDS:
                executeSqlFromFile(connection, "sql/oracle/procedures/procBindEntityFields.sql");
                break;
            case MAP_ENTITY_ENUMS:
                executeSqlFromFile(connection, "sql/oracle/procedures/procBindEntityEnums.sql");
                break;
            case MAP_CLASS_NAME:
                executeSqlFromFile(connection, "sql/oracle/procedures/procRefEntities.sql");
                break;
            case MAP_ENUM_VALUES:
                executeSqlFromFile(connection, "sql/oracle/procedures/procRefEnumValues.sql");
                break;
            case MAP_ENTITY_INHERITANCE:
                executeSqlFromFile(connection, "sql/oracle/procedures/procBindParentToChild.sql");
                break;
        }
    }
}
