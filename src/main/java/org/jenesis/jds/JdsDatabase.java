package org.jenesis.jds;


import org.jenesis.jds.enums.JdsEnumTable;
import org.jenesis.jds.enums.JdsImplementation;
import org.jenesis.jds.enums.JdsSqlType;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import java.util.Set;


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
    protected JdsImplementation implementation;

    public final void init() {
        init(JdsSqlType.TABLE, JdsEnumTable.RefEntities);
        init(JdsSqlType.TABLE, JdsEnumTable.StoreEntityOverview);
        init(JdsSqlType.TABLE, JdsEnumTable.StoreEntityBinding);
        init(JdsSqlType.TABLE, JdsEnumTable.RefEnumValues);
        init(JdsSqlType.TABLE, JdsEnumTable.RefFields);
        init(JdsSqlType.TABLE, JdsEnumTable.RefFieldTypes);
        init(JdsSqlType.TABLE, JdsEnumTable.StoreTextArray);
        init(JdsSqlType.TABLE, JdsEnumTable.StoreFloatArray);
        init(JdsSqlType.TABLE, JdsEnumTable.StoreIntegerArray);
        init(JdsSqlType.TABLE, JdsEnumTable.StoreLongArray);
        init(JdsSqlType.TABLE, JdsEnumTable.StoreDoubleArray);
        init(JdsSqlType.TABLE, JdsEnumTable.StoreDateTimeArray);
        init(JdsSqlType.TABLE, JdsEnumTable.StoreText);
        init(JdsSqlType.TABLE, JdsEnumTable.StoreFloat);
        init(JdsSqlType.TABLE, JdsEnumTable.StoreInteger);
        init(JdsSqlType.TABLE, JdsEnumTable.StoreLong);
        init(JdsSqlType.TABLE, JdsEnumTable.StoreDouble);
        init(JdsSqlType.TABLE, JdsEnumTable.StoreDateTime);
        init(JdsSqlType.TABLE, JdsEnumTable.StoreOldFieldValues);
        init(JdsSqlType.TABLE, JdsEnumTable.BindEntityFields);
        init(JdsSqlType.TABLE, JdsEnumTable.BindEntityEnums);
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

    public JdsImplementation getImplementation() {
        return this.implementation;
    }

    public final static JdsDatabase getImplementation(JdsImplementation implementation) {
        switch (implementation) {
            case SQLITE:
                return new JdsDatabaseSqlite();
            case POSTGRES:
                return new JdsDatabasePostgres();
            case TSQL:
                return new JdsDatabaseTransactionalSql();
            case MYSQL:
                return new JdsDatabaseMySql();
        }
        return null;
    }

    protected final void init(JdsSqlType type, JdsEnumTable jdsEnumTable) {
        switch (type) {
            case TABLE:
                if (!doesTableExist(jdsEnumTable.getName()))
                    initialise(jdsEnumTable);
                break;
            case STORED_PROCEDURE:
                if (!doesProcedureExist(jdsEnumTable.getName()))
                    initialise(jdsEnumTable);
                break;
            case TRIGGER:
                if (!doesTriggerExist(jdsEnumTable.getName()))
                    initialise(jdsEnumTable);
                break;
        }
    }

    private final void initialise(JdsEnumTable jdsEnumTable) {
        switch (jdsEnumTable) {
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
            case StoreEntityBinding:
                createStoreEntityBinding();
                break;
        }
        initialiseExtra(jdsEnumTable);
    }

    protected void initialiseExtra(JdsEnumTable jdsEnumTable) {
    }

    private final boolean doesTableExist(String tableName) {
        int answer = tableExists(tableName);
        return answer == 1;
    }

    private final boolean doesProcedureExist(String procedureName) {
        int answer = procedureExists(procedureName);
        return answer == 1;
    }

    private final boolean doesTriggerExist(String triggerName) {
        int answer = triggerExists(triggerName);
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

    public int triggerExists(String triggerName) {
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

    protected final void createStoreEntitySubclass() {
    }

    abstract void createRefEnumValues();

    abstract void createRefFields();

    abstract void createRefFieldTypes();

    abstract void createBindEntityFields();

    abstract void createBindEntityEnums();

    abstract void createRefEntityOverview();

    abstract void createRefOldFieldValues();

    abstract void createStoreEntityBinding();

    public final synchronized void mapClassFields(final long entityId, final Set<Long> listenerHashMap) {
        try (Connection connection = getConnection();
             PreparedStatement statement = supportsStatements() ? connection.prepareCall(mapClassFields()) : connection.prepareStatement(mapClassFields())) {
            connection.setAutoCommit(false);
            for (Long fieldId : listenerHashMap) {
                statement.setLong(1, entityId);
                statement.setLong(2, fieldId);
                statement.addBatch();
            }
            statement.executeBatch();
            connection.commit();
            System.out.printf("Mapped Fields for Entity[%s]\n", entityId);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    public final synchronized void mapClassEnums(final long entityCode, final Set<JdsFieldEnum> fields) {
        mapEnumValues(fields);
        mapEntityEnums(entityCode, fields);
        System.out.printf("Mapped Enums for Entity[%s]\n", entityCode);
    }

    private final synchronized void mapEntityEnums(final long entityId, final Set<JdsFieldEnum> fields) {
        try (Connection connection = getConnection();
             PreparedStatement statement = supportsStatements() ? connection.prepareCall(mapEntityEnums()) : connection.prepareStatement(mapEntityEnums())) {
            connection.setAutoCommit(false);
            for (JdsFieldEnum field : fields)
                for (int index = 0; index < field.getSequenceValues().size(); index++) {
                    statement.setLong(1, entityId);
                    statement.setLong(2, field.getField().getId());
                    statement.addBatch();
                }
            statement.executeBatch();
            connection.commit();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    private final synchronized void mapEnumValues(final Set<JdsFieldEnum> fields) {
        try (Connection connection = getConnection(); PreparedStatement statement = supportsStatements() ? connection.prepareCall(mapEnumValues()) : connection.prepareStatement(mapEnumValues())) {
            connection.setAutoCommit(false);
            for (JdsFieldEnum field : fields) {
                for (int index = 0; index < field.getSequenceValues().size(); index++) {
                    statement.setLong(1, field.getField().getId());
                    statement.setInt(2, index);
                    statement.setString(3, field.getSequenceValues().get(index));
                    statement.addBatch();
                }
            }
            statement.executeBatch();
            connection.commit();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    public final synchronized void mapClassName(final long entityId, final String entityName) {
        try (Connection connection = getConnection();
             PreparedStatement statement = supportsStatements() ? connection.prepareCall(mapClassName()) : connection.prepareStatement(mapClassName())) {
            statement.setLong(1, entityId);
            statement.setString(2, entityName);
            statement.executeUpdate();
            System.out.printf("Mapped Entity [%S - %s]\n", entityName, entityId);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
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
        return "{call procStoreEntityOverview(?,?,?,?)}";
    }

    public String mapClassFields() {
        return "{call procBindEntityFields(?,?)}";
    }

    public String mapEntityEnums() {
        return "{call procBindEntityEnums(?,?)}";
    }

    public String mapClassName() {
        return "{call procRefEntities(?,?)}";
    }

    public String mapEnumValues() {
        return "{call procRefEnumValues(?,?,?)}";
    }

    public final boolean supportsStatements() {
        return supportsStatements;
    }
}
