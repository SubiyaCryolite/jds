/*
* Jenesis Data Store Copyright (c) 2017 Ifunga Ndana. All rights reserved.
*
* 1. Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
*
* 2. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
*
* 3. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
*
* Neither the name Jenesis Data Store nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package io.github.subiyacryolite.jds;

import com.javaworld.INamedStatement;
import com.javaworld.NamedCallableStatement;
import com.javaworld.NamedPreparedStatement;
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation;
import io.github.subiyacryolite.jds.enums.JdsComponent;
import io.github.subiyacryolite.jds.enums.JdsComponentType;
import io.github.subiyacryolite.jds.enums.JdsImplementation;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

/**
 * This class is responsible for the setup of SQL connections, default database
 * write statements, as well as the initialization of core and custom components
 * that will support JDS on the underlying Database implementation
 */
public abstract class JdsDb implements JdsDbContract {

    private final HashMap<Long, Class<? extends JdsEntity>> classes = new HashMap<>();

    /**
     * A value indicating whether the underlying database implementation
     * supports callable statements (Stored Procedures)
     */
    protected boolean supportsStatements;
    /**
     * The underlying database implementation
     */
    protected JdsImplementation implementation;
    /**
     * A value indicating whether JDS should log every write in the system
     */
    private boolean loggingEdits;
    /**
     * A value indicating whether JDS should print internal log information
     */
    private boolean printingOutput;
    /**
     * If you want to use this library to only persist changes without storing to the primary datatables
     */
    private boolean writingToPrimaryDataTables = true;

    /**
     * Initialise JDS base tables
     */
    public void init() {
        prepareDatabaseComponents();
        //===========================================
        JdsUpdateHelper.v1Tov2DropColumnStoreEntityOverview(this);
        JdsUpdateHelper.v1Tov2AddColumnStoreOldFieldValues(this);
        JdsUpdateHelper.v1Tov2AddColumnStoreEntityBindings(this);
        //===========================================
        prepareCustomDatabaseComponents();
        //===========================================
        JdsUpdateHelper.v1ToV2MigrateData(this);
    }


    /**
     * Initialise core database components
     */
    private void prepareDatabaseComponents() {
        prepareDatabaseComponent(JdsComponentType.TABLE, JdsComponent.RefEntities);
        prepareDatabaseComponent(JdsComponentType.TABLE, JdsComponent.StoreEntityOverview);
        prepareDatabaseComponent(JdsComponentType.TABLE, JdsComponent.StoreEntityInheritance);
        prepareDatabaseComponent(JdsComponentType.TABLE, JdsComponent.StoreEntityBinding);
        prepareDatabaseComponent(JdsComponentType.TABLE, JdsComponent.RefEnumValues);
        prepareDatabaseComponent(JdsComponentType.TABLE, JdsComponent.RefFields);
        prepareDatabaseComponent(JdsComponentType.TABLE, JdsComponent.RefFieldTypes);
        prepareDatabaseComponent(JdsComponentType.TABLE, JdsComponent.StoreTextArray);
        prepareDatabaseComponent(JdsComponentType.TABLE, JdsComponent.StoreFloatArray);
        prepareDatabaseComponent(JdsComponentType.TABLE, JdsComponent.StoreIntegerArray);
        prepareDatabaseComponent(JdsComponentType.TABLE, JdsComponent.StoreLongArray);
        prepareDatabaseComponent(JdsComponentType.TABLE, JdsComponent.StoreDoubleArray);
        prepareDatabaseComponent(JdsComponentType.TABLE, JdsComponent.StoreDateTimeArray);
        prepareDatabaseComponent(JdsComponentType.TABLE, JdsComponent.StoreText);
        prepareDatabaseComponent(JdsComponentType.TABLE, JdsComponent.StoreBlob);
        prepareDatabaseComponent(JdsComponentType.TABLE, JdsComponent.StoreFloat);
        prepareDatabaseComponent(JdsComponentType.TABLE, JdsComponent.StoreInteger);
        prepareDatabaseComponent(JdsComponentType.TABLE, JdsComponent.StoreLong);
        prepareDatabaseComponent(JdsComponentType.TABLE, JdsComponent.StoreDouble);
        prepareDatabaseComponent(JdsComponentType.TABLE, JdsComponent.StoreDateTime);
        prepareDatabaseComponent(JdsComponentType.TABLE, JdsComponent.StoreZonedDateTime);
        prepareDatabaseComponent(JdsComponentType.TABLE, JdsComponent.StoreTime);
        prepareDatabaseComponent(JdsComponentType.TABLE, JdsComponent.StoreOldFieldValues);
        prepareDatabaseComponent(JdsComponentType.TABLE, JdsComponent.BindEntityFields);
        prepareDatabaseComponent(JdsComponentType.TABLE, JdsComponent.BindEntityEnums);
        prepareDatabaseComponent(JdsComponentType.TABLE, JdsComponent.RefInheritance);
    }

    /**
     * Indicates the underlying implementation of this JDS Database instance
     *
     * @return the underlying implementation of this JDS Database instance
     */
    public JdsImplementation getImplementation() {
        return this.implementation;
    }

    /**
     * Delegates the creation of custom database components depending on the
     * underlying JDS Database implementation
     *
     * @param databaseComponent the type of database component to create
     * @param jdsComponent      an enum that maps to the components concrete
     *                          implementation details
     */
    protected final void prepareDatabaseComponent(JdsComponentType databaseComponent, JdsComponent jdsComponent) {
        switch (databaseComponent) {
            case TABLE:
                if (!doesTableExist(jdsComponent.getName())) {
                    initiateDatabaseComponent(jdsComponent);
                }
                break;
            case STORED_PROCEDURE:
                if (!doesProcedureExist(jdsComponent.getName())) {
                    initiateDatabaseComponent(jdsComponent);
                }
                break;
            case TRIGGER:
                if (!doesTriggerExist(jdsComponent.getName())) {
                    initiateDatabaseComponent(jdsComponent);
                }
                break;
        }
    }

    /**
     * Initialises core JDS Database components
     *
     * @param jdsComponent an enum that maps to the components concrete
     *                     implementation details
     */
    private final void initiateDatabaseComponent(JdsComponent jdsComponent) {
        switch (jdsComponent) {
            case StoreEntityInheritance:
                createStoreEntityInheritance();
                break;
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
            case StoreBlob:
                createStoreBlob();
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
            case StoreZonedDateTime:
                createStoreZonedDateTime();
                break;
            case StoreTime:
                createStoreTime();
                break;
            case RefEntities:
                createStoreEntities();
                break;
            case RefEnumValues:
                createRefEnumValues();
                break;
            case RefInheritance:
                createRefInheritance();
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
        prepareCustomDatabaseComponents(jdsComponent);
    }

    /**
     * Initialises custom JDS Database components
     *
     * @param jdsComponent an enum that maps to the components concrete
     *                     implementation details
     */
    protected void prepareCustomDatabaseComponents(JdsComponent jdsComponent) {
    }

    /**
     * Checks if the specified table exists the the database
     *
     * @param tableName the table to look up
     * @return true if the specified table exists the the database
     */
    private final boolean doesTableExist(String tableName) {
        int answer = tableExists(tableName);
        return answer == 1;
    }

    /**
     * Checks if the specified procedure exists the the database
     *
     * @param procedureName the procedure to look up
     * @return true if the specified procedure exists the the database
     */
    private final boolean doesProcedureExist(String procedureName) {
        int answer = procedureExists(procedureName);
        return answer == 1;
    }

    /**
     * Checks if the specified trigger exists the the database
     *
     * @param triggerName the trigger to look up
     * @return true if the specified trigger exists the the database
     */
    private final boolean doesTriggerExist(String triggerName) {
        int answer = triggerExists(triggerName);
        return answer == 1;
    }

    /**
     * Checks if the specified index exists the the database
     *
     * @param indexName the index to look up
     * @return true if the specified index exists the the database
     */
    private final boolean doesIndexExist(String indexName) {
        int answer = indexExists(indexName);
        return answer == 1;
    }

    /**
     * Checks if the specified index exists the the database
     *
     * @param columnName the column to look up
     * @param tableName  the table to inspect
     * @return true if the specified index exists the the database
     */
    private final boolean doesColumnExist(String tableName, String columnName) {
        int answer = columnExists(tableName, columnName);
        return answer == 1;
    }

    protected int columnExistsCommonImpl(String tableName, String columnName, int toReturn, String sql) {
        try (Connection connection = getConnection(); NamedPreparedStatement preparedStatement = new NamedPreparedStatement(connection, sql)) {
            preparedStatement.setString("tableName", tableName);
            preparedStatement.setString("columnName", columnName);
            preparedStatement.setString("tableCatalog", connection.getCatalog());
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

    /**
     * Executes SQL found in the specified file. We recommend having one
     * statement per file.
     *
     * @param fileName the file containing SQL to find
     */
    protected final void executeSqlFromFile(String fileName) {
        try (InputStream rs = this.getClass().getClassLoader().getResourceAsStream(fileName)) {
            String innerSql = fileToString(rs);
            executeSqlFromString(innerSql);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    protected final void executeSqlFromString(String innerSql) {
        try (Connection connection = getConnection(); Statement innerStmt = connection.createStatement();) {
            innerStmt.executeUpdate(innerSql);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * Method to read contents of a file to a String variable
     *
     * @param inputStream the stream containing a files contents
     * @return the contents of a file contained in the input stream
     * @throws Exception
     */
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

    /**
     * Override this method with custom implementations of {@link #prepareDatabaseComponent(JdsComponentType, JdsComponent) prepareDatabaseComponents}
     * {@link #prepareDatabaseComponent(JdsComponentType, JdsComponent) prepareDatabaseComponents}
     * delegates the creation of custom database components depending on the
     * underlying JDS Database implementation
     */
    protected void prepareCustomDatabaseComponents() {
    }

    /**
     * Database specific check to see if the specified table exists in the
     * database
     *
     * @param tableName the table to look up
     * @return 1 if the specified table exists in the database
     */
    public abstract int tableExists(String tableName);

    /**
     * Database specific check to see if the specified procedure exists in the
     * database
     *
     * @param procedureName the procedure to look up
     * @return 1 if the specified procedure exists in the database
     */
    public int procedureExists(String procedureName) {
        return 0;
    }

    /**
     * Database specific check to see if the specified trigger exists in the
     * database
     *
     * @param triggerName the trigger to look up
     * @return 1 if the specified trigger exists in the database
     */
    public int triggerExists(String triggerName) {
        return 0;
    }

    /**
     * Database specific check to see if the specified index exists in the
     * database
     *
     * @param indexName the trigger to look up
     * @return 1 if the specified index exists in the database
     */
    public int indexExists(String indexName) {
        return 0;
    }

    /**
     * Database specific check to see if the specified column exists in the
     * database
     *
     * @param tableName
     * @param columnName
     * @return
     */
    public int columnExists(String tableName, String columnName) {
        return 0;
    }

    /**
     * Database specific SQL used to create the schema that stores text values
     */
    abstract void createStoreText();

    /**
     * Database specific SQL used to create the schema that stores blob
     * values
     */
    abstract void createStoreBlob();

    /**
     * Database specific SQL used to create the schema that stores datetime
     * values
     */
    abstract void createStoreDateTime();

    /**
     * Database specific SQL used to create the schema that stores zoned
     * datetime values
     */
    abstract void createStoreZonedDateTime();

    /**
     * Database specific SQL used to create the schema that stores time values
     */
    abstract void createStoreTime();

    /**
     * Database specific SQL used to create the schema that stores integer
     * values
     */
    abstract void createStoreInteger();

    /**
     * Database specific SQL used to create the schema that stores float values
     */
    abstract void createStoreFloat();

    /**
     * Database specific SQL used to create the schema that stores double values
     */
    abstract void createStoreDouble();

    /**
     * Database specific SQL used to create the schema that stores long values
     */
    abstract void createStoreLong();

    /**
     * Database specific SQL used to create the schema that stores text array
     * values
     */
    abstract void createStoreTextArray();

    /**
     * Database specific SQL used to create the schema that stores datetime
     * array values
     */
    abstract void createStoreDateTimeArray();

    /**
     * Database specific SQL used to create the schema that stores integer array
     * values
     */
    abstract void createStoreIntegerArray();

    /**
     * Database specific SQL used to create the schema that stores float array
     * values
     */
    abstract void createStoreFloatArray();

    /**
     * Database specific SQL used to create the schema that stores double array
     * values
     */
    abstract void createStoreDoubleArray();

    /**
     * Database specific SQL used to create the schema that stores long array
     * values
     */
    abstract void createStoreLongArray();

    /**
     * Database specific SQL used to create the schema that stores entity
     * definitions
     */
    abstract void createStoreEntities();

    /**
     * Database specific SQL used to create the schema that stores enum
     * definitions
     */
    abstract void createRefEnumValues();

    /**
     * Database specific SQL used to create the schema that stores inheritance information
     */
    abstract void createRefInheritance();

    /**
     * Database specific SQL used to create the schema that stores field
     * definitions
     */
    abstract void createRefFields();

    /**
     * Database specific SQL used to create the schema that stores field type
     * definitions
     */
    abstract void createRefFieldTypes();

    /**
     * Database specific SQL used to create the schema that stores entity
     * binding information
     */
    abstract void createBindEntityFields();

    /**
     * Database specific SQL used to create the schema that stores entity to
     * enum binding information
     */
    abstract void createBindEntityEnums();

    /**
     * Database specific SQL used to create the schema that stores entity
     * overview
     */
    abstract void createRefEntityOverview();

    /**
     * Database specific SQL used to create the schema that stores old field
     * values of every type
     */
    abstract void createRefOldFieldValues();

    /**
     * Database specific SQL used to create the schema that stores entity to
     * entity bindings
     */
    abstract void createStoreEntityBinding();

    /**
     * Database specific SQL used to create the schema that stores entity to type
     * bindings
     */
    abstract void createStoreEntityInheritance();

    /**
     * Binds all the fields attached to an entity, updates the fields dictionary
     *
     * @param connection the SQL connection to use for DB operations
     * @param entityId   the value representing the entity
     * @param fields     the values representing the entity's fields
     */
    public final synchronized void mapClassFields(final Connection connection, final long entityId, final Map<Long, String> fields) {
        try (INamedStatement mapClassFields = supportsStatements() ? new NamedCallableStatement(connection, mapClassFields()) : new NamedPreparedStatement(connection, mapClassFields());
             INamedStatement mapFieldNames = supportsStatements() ? new NamedCallableStatement(connection, mapFieldNames()) : new NamedPreparedStatement(connection, mapFieldNames())) {
            for (Map.Entry<Long, String> field : fields.entrySet()) {
                //1. map this field ID to the entity type
                mapClassFields.setLong("entityId", entityId);
                mapClassFields.setLong("fieldId", field.getKey());
                mapClassFields.addBatch();
                //2. map this field to the field dictionary
                mapFieldNames.setLong("fieldId", field.getKey());
                mapFieldNames.setString("fieldName", field.getValue());
                mapFieldNames.addBatch();
            }
            mapClassFields.executeBatch();
            mapFieldNames.executeBatch();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * Binds all the field types and updates reference tables
     *
     * @param connection the SQL connection to use for DB operations
     * @param entityId   the value representing the entity
     * @param fieldTypes the values representing the entity's field types
     */
    public final synchronized void mapClassFieldTypes(final Connection connection, final long entityId, final Map<Long, String> fieldTypes) {
        try (INamedStatement mapFieldTypes = supportsStatements() ? new NamedCallableStatement(connection, mapFieldTypes()) : new NamedPreparedStatement(connection, mapFieldTypes())) {
            for (Map.Entry<Long, String> fieldType : fieldTypes.entrySet()) {
                mapFieldTypes.setLong("typeId", fieldType.getKey());
                mapFieldTypes.setString("typeName", fieldType.getValue());
                mapFieldTypes.addBatch();
            }
            mapFieldTypes.executeBatch();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * Binds all the enums attached to an entity
     *
     * @param entityId the value representing the entity
     * @param fields   the entity's enums
     */
    public final synchronized void mapClassEnums(final Connection connection, final long entityId, final Set<JdsFieldEnum> fields) {
        mapEnumValues(connection, fields);
        mapClassEnumsImplementation(connection, entityId, fields);
        if (isPrintingOutput())
            System.out.printf("Mapped Enums for Entity[%s]\n", entityId);
    }

    /**
     * @param connection     the SQL connection to use for DB operations
     * @param parentEntities a collection of parent classes
     * @param entityCode     the value representing the entity
     */
    private void mapParentEntities(Connection connection, List<Long> parentEntities, long entityCode) {
        if (parentEntities.isEmpty()) return;
        try (PreparedStatement statement = supportsStatements() ? connection.prepareCall(mapParentToChild()) : connection.prepareStatement(mapParentToChild())) {
            for (long parentEntitiy : parentEntities) {
                statement.setLong(1, parentEntitiy);
                statement.setLong(2, entityCode);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * Binds all the enums attached to an entity
     *
     * @param connection the SQL connection to use for DB operations
     * @param entityId   the value representing the entity
     * @param fields     the entity's enums
     */
    private final synchronized void mapClassEnumsImplementation(final Connection connection, final long entityId, final Set<JdsFieldEnum> fields) {
        try (PreparedStatement statement = supportsStatements() ? connection.prepareCall(mapClassEnumsImplementation()) : connection.prepareStatement(mapClassEnumsImplementation())) {
            for (JdsFieldEnum field : fields) {
                for (int index = 0; index < field.getSequenceValues().length; index++) {
                    statement.setLong(1, entityId);
                    statement.setLong(2, field.getField().getId());
                    statement.addBatch();
                }
            }
            statement.executeBatch();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * Binds all the values attached to an enum
     *
     * @param connection the SQL connection to use for DB operations
     * @param fieldEnums the field enum
     */
    private final synchronized void mapEnumValues(final Connection connection, final Set<JdsFieldEnum> fieldEnums) {
        try (PreparedStatement statement = supportsStatements() ? connection.prepareCall(mapEnumValues()) : connection.prepareStatement(mapEnumValues())) {
            for (JdsFieldEnum field : fieldEnums) {
                for (int index = 0; index < field.getSequenceValues().length; index++) {
                    statement.setLong(1, field.getField().getId());
                    statement.setInt(2, index);
                    statement.setString(3, field.getSequenceValues()[index].toString());
                    statement.addBatch();
                }
            }
            statement.executeBatch();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * Maps an entity's name to its id
     *
     * @param connection the SQL connection to use for DB operations
     * @param entityId   the entity's id
     * @param entityName the entity's name
     */
    public final synchronized void mapClassName(final Connection connection, final long entityId, final String entityName) {
        try (PreparedStatement statement = supportsStatements() ? connection.prepareCall(mapClassName()) : connection.prepareStatement(mapClassName())) {
            statement.setLong(1, entityId);
            statement.setString(2, entityName);
            statement.executeUpdate();
            if (printingOutput)
                System.out.printf("Mapped Entity [%S - %s]\n", entityName, entityId);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    public synchronized void map(Class<? extends JdsEntity> entity) {
        if (entity.isAnnotationPresent(JdsEntityAnnotation.class)) {
            JdsEntityAnnotation entityAnnotation = entity.getAnnotation(JdsEntityAnnotation.class);
            if (!classes.containsKey(entityAnnotation.entityId())) {
                classes.put(entityAnnotation.entityId(), entity);
                //do the thing
                try (Connection connection = getConnection()) {
                    connection.setAutoCommit(false);
                    List<Long> parentEntities = new ArrayList<>();
                    JdsEntity jdsEntity = entity.newInstance();
                    determineParents(entity, parentEntities);
                    mapClassName(connection, jdsEntity.getEntityCode(), jdsEntity.getEntityName());
                    mapClassFields(connection, jdsEntity.getEntityCode(), jdsEntity.properties);
                    mapClassFieldTypes(connection, jdsEntity.getEntityCode(), jdsEntity.types);
                    mapClassEnums(connection, jdsEntity.getEntityCode(), jdsEntity.allEnums);
                    mapParentEntities(connection, parentEntities, jdsEntity.getEntityCode());
                    connection.commit();
                    jdsEntity = null;
                    if (isPrintingOutput())
                        System.out.printf("Mapped Entity [%s]\n", jdsEntity.getEntityName());
                } catch (Exception ex) {
                    ex.printStackTrace(System.err);
                }
            } else
                throw new RuntimeException("Duplicate service code for class [" + entity.getCanonicalName() + "] - [" + entityAnnotation.entityId() + "]");
        } else
            throw new RuntimeException("You must annotate the class [" + entity.getCanonicalName() + "] with [" + JdsEntityAnnotation.class + "]");
    }

    public Collection<Class<? extends JdsEntity>> getMappedClasses() {
        return classes.values();
    }

    private void determineParents(Class<? extends JdsEntity> entity, List<Long> parentEntities) {
        addAllToList(entity.getSuperclass(), parentEntities);
    }

    private void addAllToList(Class<?> superclass, List<Long> parentEntities) {
        if (superclass == null) return;
        if (superclass.isAnnotationPresent(JdsEntityAnnotation.class)) {
            JdsEntityAnnotation annotation = superclass.getAnnotation(JdsEntityAnnotation.class);
            parentEntities.add(annotation.entityId());
            addAllToList(superclass.getSuperclass(), parentEntities);
        }
    }


    public Class<? extends JdsEntity> getBoundClass(long serviceCode) {
        return classes.get(serviceCode);
    }

    /**
     * A value indicating whether JDS is logging every write in the system
     *
     * @return true if JDS is logging every write in the system
     */
    public final boolean isLoggingEdits() {
        return loggingEdits;
    }

    /**
     * A value indicating whether JDS is only persisting changes to fields without affecting the primary datastores
     *
     * @return true if JDS is only persisting changes to fields without affecting the primary datastores
     */
    public final boolean isWritingToPrimaryDataTables() {
        return writingToPrimaryDataTables;
    }

    /**
     * @param value
     */
    public final void isWritingToPrimaryDataTables(boolean value) {
        this.writingToPrimaryDataTables = value;
    }

    /**
     * Determine whether JDS should log every write in the system
     *
     * @param value whether JDS should log every write in the system
     */
    public final void isLoggingEdits(boolean value) {
        this.loggingEdits = value;
    }

    /**
     * A value indicating whether JDS is printing internal log information
     *
     * @return true if JDS is printing internal log information
     */
    public final boolean isPrintingOutput() {
        return printingOutput;
    }

    /**
     * Determine whether JDS should print internal log information
     *
     * @param value whether JDS should print internal log information
     */
    public final void isPrintingOutput(boolean value) {
        this.printingOutput = value;
    }

    /**
     * A value indicating whether the underlying database implementation
     * supports callable statements (Stored Procedures)
     *
     * @return true if the underlying database implementation supports callable
     * statements (stored procedures)
     */
    public final boolean supportsStatements() {
        return supportsStatements;
    }

    /**
     * SQL call to save text values
     *
     * @return the default or overridden SQL statement for this operation
     */
    public String saveString() {
        return "{call procStoreText(:entityGuid,:fieldId,:value)}";
    }

    /**
     * SQL call to save long values
     *
     * @return the default or overridden SQL statement for this operation
     */
    public String saveLong() {
        return "{call procStoreLong(:entityGuid,:fieldId,:value)}";
    }

    /**
     * SQL call to save double values
     *
     * @return the default or overridden SQL statement for this operation
     */
    public String saveDouble() {
        return "{call procStoreDouble(:entityGuid,:fieldId,:value)}";
    }

    /**
     * SQL call to save blob values
     *
     * @return the default or overridden SQL statement for this operation
     */
    public String saveBlob() {
        return "{call procStoreBlob(:entityGuid,:fieldId,:value)}";
    }

    /**
     * SQL call to save float values
     *
     * @return the default or overridden SQL statement for this operation
     */
    public String saveFloat() {
        return "{call procStoreFloat(:entityGuid,:fieldId,:value)}";
    }

    /**
     * SQL call to save integer values
     *
     * @return the default or overridden SQL statement for this operation
     */
    public String saveInteger() {
        return "{call procStoreInteger(:entityGuid,:fieldId,:value)}";
    }

    /**
     * SQL call to save datetime values
     *
     * @return the default or overridden SQL statement for this operation
     */
    public String saveDateTime() {
        return "{call procStoreDateTime(:entityGuid,:fieldId,:value)}";
    }

    /**
     * SQL call to save datetime values
     *
     * @return the default or overridden SQL statement for this operation
     */
    public String saveZonedDateTime() {
        return "{call procStoreZonedDateTime(:entityGuid,:fieldId,:value)}";
    }

    /**
     * SQL call to save date values
     *
     * @return the default or overridden SQL statement for this operation
     */
    public String saveDate() {
        return "{call procStoreDate(:entityGuid,:fieldId,:value)}";
    }

    /**
     * SQL call to save time values
     *
     * @return the default or overridden SQL statement for this operation
     */
    public String saveTime() {
        return "{call procStoreTime(:entityGuid,:fieldId,:value)}";
    }

    /**
     * SQL call to save entity overview values
     *
     * @return the default or overridden SQL statement for this operation
     */
    public String saveOverview() {
        return "{call procStoreEntityOverviewV2(:entityGuid,:dateCreated,:dateModified)}";
    }

    /**
     * SQL call to save entity overview values
     *
     * @return the default or overridden SQL statement for this operation
     */
    public String saveOverviewInheritance() {
        return "{call procStoreEntityInheritance(:entityGuid, :entityId)}";
    }

    /**
     * SQL call to bind fields to entities
     *
     * @return the default or overridden SQL statement for this operation
     */
    public String mapClassFields() {
        return "{call procBindEntityFields(:entityId, :fieldId)}";
    }

    public String mapFieldNames() {
        return "{call procBindFieldNames(:fieldId, :fieldName)}";
    }

    public String mapFieldTypes() {
        return "{call procBindFieldTypes(:typeId, :typeName)}";
    }

    /**
     * SQL call to bind enums to entities
     *
     * @return the default or overridden SQL statement for this operation
     */
    public String mapClassEnumsImplementation() {
        return "{call procBindEntityEnums(?,?)}";
    }

    /**
     * Map parents to child entities
     *
     * @return
     */
    public String mapParentToChild() {
        return "{call procBindParentToChild(?,?)}";
    }

    /**
     * SQL call to map class names
     *
     * @return the default or overridden SQL statement for this operation
     */
    public String mapClassName() {
        return "{call procRefEntities(?,?)}";
    }

    /**
     * SQL call to save reference enum values
     *
     * @return the default or overridden SQL statement for this operation
     */
    public String mapEnumValues() {
        return "{call procRefEnumValues(?,?,?)}";
    }

    public abstract String createOrAlterView(String viewName, String viewSql);
}
