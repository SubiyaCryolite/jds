package org.jenesis.jds;


import javafx.beans.property.SimpleIntegerProperty;
import org.jenesis.jds.enums.JdsImplementation;
import org.jenesis.jds.enums.JdsTable;
import org.jenesis.jds.listeners.BaseListener;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;


/**
 * Created by ifunga on 12/02/2017.
 */
public abstract class JdsDatabase {

    private String className;
    private String url;
    private String userName;
    private String passWord;
    private boolean logEdits;
    private boolean printOutput;
    private boolean propertiesSet;
    protected boolean supportsStatements, deleteAsFunction;
    private Properties properties;

    public final void init() {
        init(true, JdsTable.RefEntities);
        init(true, JdsTable.StoreEntityOverview);
        init(true, JdsTable.RefEnumValues);
        init(true, JdsTable.RefFields);
        init(true, JdsTable.RefFieldTypes);
        init(true, JdsTable.StoreTextArray);
        init(true, JdsTable.StoreFloatArray);
        init(true, JdsTable.StoreIntegerArray);
        init(true, JdsTable.StoreLongArray);
        init(true, JdsTable.StoreDoubleArray);
        init(true, JdsTable.StoreDateTimeArray);
        init(true, JdsTable.StoreText);
        init(true, JdsTable.StoreFloat);
        init(true, JdsTable.StoreInteger);
        init(true, JdsTable.StoreLong);
        init(true, JdsTable.StoreDouble);
        init(true, JdsTable.StoreDateTime);
        init(true, JdsTable.StoreOldFieldValues);
        init(true, JdsTable.BindEntityFields);
        init(true, JdsTable.BindEntityEnums);
        initExtra();
    }

    public final void setConnectionProperties(String className, String url, String userName, String passWord) {
        if (className == null || url == null || userName == null || passWord == null)
            throw new RuntimeException("Please supply valid values. Nulls not permitted");
        this.className = className;
        this.url = url;
        this.userName = userName;
        this.passWord = passWord;
        this.propertiesSet = true;
    }

    public void setConnectionProperties(String url, java.util.Properties properties) {
        if (url == null)
            throw new RuntimeException("Please supply valid values. Nulls not permitted");
        this.url = url;
        this.propertiesSet = true;
        this.properties = properties;
    }

    public final synchronized Connection getConnection() throws ClassNotFoundException, SQLException {
        if (!propertiesSet)
            throw new RuntimeException("Please set connection properties before requesting a database connection");
        if (userName != null && passWord != null) {
            Class.forName(className);
            return DriverManager.getConnection(url, userName, passWord);
        } else {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection(url, properties);
        }
    }

    public final static JdsDatabase getImplementation(JdsImplementation implementation) {
        switch (implementation) {
            case SQLITE:
                return new JdsDatabaseSqlite();
            case POSTGRES:
                return new JdsDatabasePostgres();
            case TSQL:
                return new JdsDatabaseTransactionalSql();
        }
        return null;
    }

    protected final void init(boolean isTable, JdsTable jdsTable) {
        boolean tableExists = isTable ? doesTableExist(jdsTable.getName()) : doesProcedureExist(jdsTable.getName());
        if (!tableExists)
            createTable(jdsTable);
    }

    private final void createTable(JdsTable jdsTable) {
        switch (jdsTable) {
            case StoreTextArray:
                createStoreTextArray();
                break;
            case StoreFloatArray:
                createStoreFloatArray();
                break;
            case StoreIntegerArray:
                createStoreIntegerArray();
                break;
            case StoreLongArray:
                createStoreLongArray();
                break;
            case StoreDoubleArray:
                createStoreDoubleArray();
                break;
            case StoreDateTimeArray:
                createStoreDateTimeArray();
                break;
            case StoreText:
                createStoreText();
                break;
            case StoreFloat:
                createStoreFloat();
                break;
            case StoreInteger:
                createStoreInteger();
                break;
            case StoreLong:
                createStoreLong();
                break;
            case StoreDouble:
                createStoreDouble();
                break;
            case StoreDateTime:
                createStoreDateTime();
                break;
            case RefEntities:
                createStoreEntities();
                break;
            case RefEnumValues:
                createRefEnumValues();
                break;
            case RefFields:
                createRefFields();
                break;
            case RefFieldTypes:
                createRefFieldTypes();
                break;
            case BindEntityFields:
                createBindEntityFields();
                break;
            case BindEntityEnums:
                createBindEntityEnums();
                break;
            case StoreEntityOverview:
                createRefEntityOverview();
                break;
            case StoreOldFieldValues:
                createRefOldFieldValues();
                break;
        }
        createTableExtra(jdsTable);
    }

    protected void createTableExtra(JdsTable jdsTable) {
    }

    private final boolean doesTableExist(String tableName) {
        int answer = tableExists(tableName);
        return answer == 1;
    }

    private final boolean doesProcedureExist(String tableName) {
        int answer = procedureExists(tableName);
        return answer == 1;
    }

    protected final void createTableFromFile(String fileName) {
        try (Connection connection = getConnection(); Statement innerStmt = connection.createStatement();) {
            String innerSql = fileToString(this.getClass().getClassLoader().getResourceAsStream(fileName));
            innerStmt.executeUpdate(innerSql);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    private String fileToString(InputStream inputStream) throws Exception {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int result = bufferedInputStream.read();
        while (result != -1) {
            byteArrayOutputStream.write((byte) result);
            result = bufferedInputStream.read();
        }
        byteArrayOutputStream.close();
        bufferedInputStream.close();
        return byteArrayOutputStream.toString();
    }

    protected void initExtra() {
    }

    public abstract int tableExists(String tableName);

    public int procedureExists(String procedureName) {
        return 0;
    }

    abstract void createStoreText();

    abstract void createStoreDateTime();

    abstract void createStoreInteger();

    abstract void createStoreFloat();

    abstract void createStoreDouble();

    abstract void createStoreLong();

    abstract void createStoreTextArray();

    abstract void createStoreDateTimeArray();

    abstract void createStoreIntegerArray();

    abstract void createStoreFloatArray();

    abstract void createStoreDoubleArray();

    abstract void createStoreLongArray();

    abstract void createStoreEntities();

    protected final void createStoreEntitySubclass(){}

    abstract void createRefEnumValues();

    abstract void createRefFields();

    abstract void createRefFieldTypes();

    abstract void createBindEntityFields();

    abstract void createBindEntityEnums();

    abstract void createRefEntityOverview();

    abstract void createRefOldFieldValues();

    public final synchronized boolean mapClassFields(final long entityCode, final HashMap<Long, BaseListener> listenerHashMap) {
        int rowsWritten = 0;
        String checkSql = "SELECT COUNT(*) AS Result FROM JdsBindEntityFields WHERE EntityId = ? AND FieldId =?";
        String insertSql = "INSERT INTO JdsBindEntityFields (EntityId,FieldId) VALUES (?,?)";
        try (Connection connection = getConnection();
             PreparedStatement check = connection.prepareStatement(checkSql);
             PreparedStatement insert = connection.prepareStatement(insertSql)) {
            for (Map.Entry<Long, BaseListener> fieldId : listenerHashMap.entrySet()) {
                check.clearParameters();
                check.setLong(1, entityCode);
                check.setLong(2, fieldId.getKey());
                ResultSet resultSet = check.executeQuery();
                while (resultSet.next()) {
                    if (resultSet.getInt("Result") == 0) {
                        insert.clearParameters();
                        insert.setLong(1, entityCode);
                        insert.setLong(2, fieldId.getKey());
                        rowsWritten += insert.executeUpdate();
                    }
                }
                resultSet.close();
            }
            System.out.printf("Mapped Fields for Entity[%s]\n", entityCode);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            return false;
        }
        return rowsWritten > 0;
    }

    public final synchronized boolean mapClassEnums(final long entityCode, final HashSet<JdsFieldEnum> fields) {
        SimpleIntegerProperty firstResult = new SimpleIntegerProperty(0);
        SimpleIntegerProperty secondResult = new SimpleIntegerProperty(0);
        if (mapEnumValues(fields, firstResult)) return false;
        if (mapEntityEnums(entityCode, fields, secondResult)) return false;
        System.out.printf("Mapped Enums for Entity[%s]\n", entityCode);
        return firstResult.get() > 0 && secondResult.get() > 0;
    }

    private final synchronized boolean mapEntityEnums(long entityCode, HashSet<JdsFieldEnum> fields, SimpleIntegerProperty secondResult) {
        String checkSql = "SELECT COUNT(*) AS Result FROM JdsBindEntityEnums WHERE EntityId=? AND FieldId=?";
        String insertSql = "INSERT INTO JdsBindEntityEnums(EntityId,FieldId) VALUES (?,?)";
        try (Connection connection = getConnection();
             PreparedStatement check = connection.prepareStatement(checkSql);
             PreparedStatement insert = connection.prepareStatement(insertSql)) {
            for (JdsFieldEnum field : fields)
                for (int index = 0; index < field.getSequenceValues().size(); index++) {
                    check.clearParameters();
                    check.setLong(1, entityCode);
                    check.setLong(2, field.getField().getId());
                    ResultSet resultSet = check.executeQuery();
                    while (resultSet.next()) {
                        if (resultSet.getInt("Result") == 0) {
                            insert.clearParameters();
                            insert.setLong(1, entityCode);
                            insert.setLong(2, field.getField().getId());
                            secondResult.set(secondResult.get() + insert.executeUpdate());
                        }
                    }
                    resultSet.close();
                }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            return true;
        }
        return false;
    }

    private final synchronized boolean mapEnumValues(HashSet<JdsFieldEnum> fields, SimpleIntegerProperty firstResult) {
        String checkSql = "SELECT COUNT(*) AS Result FROM JdsRefEnumValues WHERE FieldId=? AND EnumSeq=? AND EnumValue=?";
        String insertSql = "INSERT INTO JdsRefEnumValues (FieldId,EnumSeq,EnumValue) VALUES (?,?,?)";
        try (Connection connection = getConnection(); PreparedStatement check = connection.prepareStatement(checkSql); PreparedStatement insert = connection.prepareStatement(insertSql)) {
            for (JdsFieldEnum field : fields) {
                for (int index = 0; index < field.getSequenceValues().size(); index++) {
                    check.clearParameters();
                    check.setLong(1, field.getField().getId());
                    check.setInt(2, index);
                    check.setString(3, field.getSequenceValues().get(index));
                    ResultSet resultSet = check.executeQuery();
                    while (resultSet.next()) {
                        if (resultSet.getInt("Result") == 0) {
                            insert.clearParameters();
                            insert.setLong(1, field.getField().getId());
                            insert.setInt(2, index);
                            insert.setString(3, field.getSequenceValues().get(index));
                            firstResult.set(firstResult.get() + insert.executeUpdate());
                        }
                    }
                    resultSet.close();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            return true;
        }
        return false;
    }

    public final synchronized boolean mapClassName(final long entityCode, final String entityName) {
        int result = 0;
        String checkSql = "SELECT COUNT(*) AS Result FROM JdsRefEntities WHERE EntityId = ? AND EntityName = ?";
        String insertSql = "INSERT INTO JdsRefEntities(EntityId,EntityName) VALUES (?,?)";
        try (Connection connection = getConnection();
             PreparedStatement check = connection.prepareStatement(checkSql);
             PreparedStatement insert = connection.prepareStatement(insertSql)) {
            check.setLong(1, entityCode);
            check.setString(2, entityName);
            ResultSet resultSet = check.executeQuery();
            while (resultSet.next()) {
                if (resultSet.getInt("Result") == 0) {
                    insert.clearParameters();
                    insert.setLong(1, entityCode);
                    insert.setString(2, entityName);
                    result += insert.executeUpdate();
                }
            }
            System.out.printf("Mapped Entity [%S - %s]", entityName, entityCode);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
        return result > 0;
    }

    public final boolean logEdits() {
        return logEdits;
    }

    public final void logEdits(boolean logEdits) {
        this.logEdits = logEdits;
    }

    public final boolean printOutput() {
        return printOutput;
    }

    public final void printOutput(boolean printOutput) {
        this.printOutput = printOutput;
    }

    public String saveString() {
        return "{call procStoreText(?,?,?)}";
    }

    public String saveLong() {
        return "{call procStoreLong(?,?,?)}";
    }

    public String saveDouble() {
        return "{call procStoreDouble(?,?,?)}";
    }

    public String saveFloat() {
        return "{call procStoreFloat(?,?,?)}";
    }

    public String saveInteger() {
        return "{call procStoreInteger(?,?,?)}";
    }

    public String saveDateTime() {
        return "{call procStoreDateTime(?,?,?)}";
    }

    public String saveOverview() {
        return "{call procStoreEntityOverview(?,?,?,?,?)}";
    }

    public final boolean supportsStatements() {
        return supportsStatements;
    }
}
