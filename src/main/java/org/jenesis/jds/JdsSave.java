package org.jenesis.jds;

import javafx.beans.property.*;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Created by ifunga on 12/02/2017.
 */
public class JdsSave {

    public static void save(final JdsDatabase database, final int batchSize, final Collection<? extends JdsEntity> entities) {
        //strings, dates and numerics
        List<HashMap<String, HashMap<Long, SimpleObjectProperty<LocalDateTime>>>> dateTimeProperties = new ArrayList<>();
        List<HashMap<String, HashMap<Long, SimpleStringProperty>>> stringProperties = new ArrayList<>();
        List<HashMap<String, HashMap<Long, SimpleFloatProperty>>> floatProperties = new ArrayList<>();
        List<HashMap<String, HashMap<Long, SimpleDoubleProperty>>> doubleProperties = new ArrayList<>();
        List<HashMap<String, HashMap<Long, SimpleLongProperty>>> longProperties = new ArrayList<>();
        List<HashMap<String, HashMap<Long, SimpleIntegerProperty>>> integerProperties = new ArrayList<>();
        //arrays
        List<HashMap<String, HashMap<Long, SimpleListProperty<? extends JdsEntity>>>> objectArrayProperties = new ArrayList<>();
        List<HashMap<String, HashMap<Long, SimpleListProperty<String>>>> stringArrayProperties = new ArrayList<>();
        List<HashMap<String, HashMap<Long, SimpleListProperty<LocalDateTime>>>> dateTimeArrayProperties = new ArrayList<>();
        List<HashMap<String, HashMap<Long, SimpleListProperty<Float>>>> floatArrayProperties = new ArrayList<>();
        List<HashMap<String, HashMap<Long, SimpleListProperty<Double>>>> doubleArrayProperties = new ArrayList<>();
        List<HashMap<String, HashMap<Long, SimpleListProperty<Long>>>> longArrayProperties = new ArrayList<>();
        List<HashMap<String, HashMap<Long, SimpleListProperty<Integer>>>> integerArrayProperties = new ArrayList<>();
        //enums
        List<HashMap<String, HashMap<JdsFieldEnum, SimpleListProperty<String>>>> enumProperties = new ArrayList<>();
        //objectProperties
        List<HashMap<String, HashMap<Long, SimpleObjectProperty<? extends JdsEntity>>>> objectProperties = new ArrayList<>();
        List<HashSet<JdsEntityOverview>> overviews = new LinkedList<>();
        List<Collection<JdsEntity>> batchEntities = new ArrayList<>();
        //create batches
        int currentBatch = 0;
        int iteration = 0;
        if (batchSize > 0) {
            for (JdsEntity jdsEntity : entities) {
                if (currentBatch == batchSize) {
                    currentBatch++;
                    iteration = 0;
                }
                if (iteration == 0) {
                    createBatchCollection(dateTimeProperties, stringProperties, floatProperties, doubleProperties, longProperties, integerProperties, objectArrayProperties, stringArrayProperties, dateTimeArrayProperties, floatArrayProperties, doubleArrayProperties, longArrayProperties, integerArrayProperties, enumProperties, objectProperties, overviews, batchEntities);
                }
                batchEntities.get(currentBatch).add(jdsEntity);
                iteration++;
            }
        } else {
            //single large batch, good luck
            currentBatch = 1;
            createBatchCollection(dateTimeProperties, stringProperties, floatProperties, doubleProperties, longProperties, integerProperties, objectArrayProperties, stringArrayProperties, dateTimeArrayProperties, floatArrayProperties, doubleArrayProperties, longArrayProperties, integerArrayProperties, enumProperties, objectProperties, overviews, batchEntities);
            for (JdsEntity jdsEntity : entities) {
                batchEntities.get(0).add(jdsEntity);
            }
        }

        //process batches
        for (int batch = 0; batch < currentBatch; batch++) {
            saveInner(database, batchEntities.get(batch), dateTimeProperties.get(batch), stringProperties.get(batch), floatProperties.get(batch), doubleProperties.get(batch), longProperties.get(batch), integerProperties.get(batch), objectArrayProperties.get(batch), stringArrayProperties.get(batch), dateTimeArrayProperties.get(batch), floatArrayProperties.get(batch), doubleArrayProperties.get(batch), longArrayProperties.get(batch), integerArrayProperties.get(batch), enumProperties.get(batch), objectProperties.get(batch), overviews.get(batch));
            System.out.printf("Processed batch [%s of %s]\n", (batch + 1), currentBatch);
        }
    }

    private static void createBatchCollection(List<HashMap<String, HashMap<Long, SimpleObjectProperty<LocalDateTime>>>> dateTimeProperties, List<HashMap<String, HashMap<Long, SimpleStringProperty>>> stringProperties, List<HashMap<String, HashMap<Long, SimpleFloatProperty>>> floatProperties, List<HashMap<String, HashMap<Long, SimpleDoubleProperty>>> doubleProperties, List<HashMap<String, HashMap<Long, SimpleLongProperty>>> longProperties, List<HashMap<String, HashMap<Long, SimpleIntegerProperty>>> integerProperties, List<HashMap<String, HashMap<Long, SimpleListProperty<? extends JdsEntity>>>> objectArrayProperties, List<HashMap<String, HashMap<Long, SimpleListProperty<String>>>> stringArrayProperties, List<HashMap<String, HashMap<Long, SimpleListProperty<LocalDateTime>>>> dateTimeArrayProperties, List<HashMap<String, HashMap<Long, SimpleListProperty<Float>>>> floatArrayProperties, List<HashMap<String, HashMap<Long, SimpleListProperty<Double>>>> doubleArrayProperties, List<HashMap<String, HashMap<Long, SimpleListProperty<Long>>>> longArrayProperties, List<HashMap<String, HashMap<Long, SimpleListProperty<Integer>>>> integerArrayProperties, List<HashMap<String, HashMap<JdsFieldEnum, SimpleListProperty<String>>>> enumProperties, List<HashMap<String, HashMap<Long, SimpleObjectProperty<? extends JdsEntity>>>> objectProperties, List<HashSet<JdsEntityOverview>> overviews, List<Collection<JdsEntity>> batchEntities) {
        batchEntities.add(new ArrayList<>());
        overviews.add(new HashSet<>());
        //primitives
        dateTimeProperties.add(new HashMap<>());
        stringProperties.add(new HashMap<>());
        floatProperties.add(new HashMap<>());
        doubleProperties.add(new HashMap<>());
        longProperties.add(new HashMap<>());
        integerProperties.add(new HashMap<>());
        //arrays
        stringArrayProperties.add(new HashMap<>());
        dateTimeArrayProperties.add(new HashMap<>());
        floatArrayProperties.add(new HashMap<>());
        doubleArrayProperties.add(new HashMap<>());
        longArrayProperties.add(new HashMap<>());
        integerArrayProperties.add(new HashMap<>());
        //enums
        enumProperties.add(new HashMap<>());
        //objects
        objectProperties.add(new HashMap<>());
        //object arrays
        objectArrayProperties.add(new HashMap<>());
    }

    /**
     * @param database
     * @param entities
     * @param dateTimeProperties
     * @param stringProperties
     * @param floatProperties
     * @param doubleProperties
     * @param longProperties
     * @param integerProperties
     * @param objectArrayProperties
     * @param stringArrayProperties
     * @param dateTimeArrayProperties
     * @param floatArrayProperties
     * @param doubleArrayProperties
     * @param longArrayProperties
     * @param integerArrayProperties
     * @param enumProperties
     * @param objectProperties
     * @param overviews
     */
    private static void saveInner(final JdsDatabase database,
                                  final Collection<? extends JdsEntity> entities,
                                  final HashMap<String, HashMap<Long, SimpleObjectProperty<LocalDateTime>>> dateTimeProperties,
                                  final HashMap<String, HashMap<Long, SimpleStringProperty>> stringProperties,
                                  final HashMap<String, HashMap<Long, SimpleFloatProperty>> floatProperties,
                                  final HashMap<String, HashMap<Long, SimpleDoubleProperty>> doubleProperties,
                                  final HashMap<String, HashMap<Long, SimpleLongProperty>> longProperties,
                                  final HashMap<String, HashMap<Long, SimpleIntegerProperty>> integerProperties,
                                  final HashMap<String, HashMap<Long, SimpleListProperty<? extends JdsEntity>>> objectArrayProperties,
                                  final HashMap<String, HashMap<Long, SimpleListProperty<String>>> stringArrayProperties,
                                  final HashMap<String, HashMap<Long, SimpleListProperty<LocalDateTime>>> dateTimeArrayProperties,
                                  final HashMap<String, HashMap<Long, SimpleListProperty<Float>>> floatArrayProperties,
                                  final HashMap<String, HashMap<Long, SimpleListProperty<Double>>> doubleArrayProperties,
                                  final HashMap<String, HashMap<Long, SimpleListProperty<Long>>> longArrayProperties,
                                  final HashMap<String, HashMap<Long, SimpleListProperty<Integer>>> integerArrayProperties,
                                  final HashMap<String, HashMap<JdsFieldEnum, SimpleListProperty<String>>> enumProperties,
                                  final HashMap<String, HashMap<Long, SimpleObjectProperty<? extends JdsEntity>>> objectProperties,
                                  final HashSet<JdsEntityOverview> overviews) {
        //fire
        for (final JdsEntity entity : entities) {
            if (entity == null) continue;
            mapEntity(database, entity);
            entity.setDateModified(LocalDateTime.now());//update the modified date to time of commit
            overviews.add(entity.getOverview());
            dateTimeProperties.put(entity.getEntityGuid(), entity.dateProperties);
            stringProperties.put(entity.getEntityGuid(), entity.stringProperties);
            floatProperties.put(entity.getEntityGuid(), entity.floatProperties);
            doubleProperties.put(entity.getEntityGuid(), entity.doubleProperties);
            longProperties.put(entity.getEntityGuid(), entity.longProperties);
            integerProperties.put(entity.getEntityGuid(), entity.integerProperties);
            objectArrayProperties.put(entity.getEntityGuid(), entity.objectArrayProperties);
            stringArrayProperties.put(entity.getEntityGuid(), entity.stringArrayProperties);
            dateTimeArrayProperties.put(entity.getEntityGuid(), entity.dateTimeArrayProperties);
            floatArrayProperties.put(entity.getEntityGuid(), entity.floatArrayProperties);
            doubleArrayProperties.put(entity.getEntityGuid(), entity.doubleArrayProperties);
            longArrayProperties.put(entity.getEntityGuid(), entity.longArrayProperties);
            integerArrayProperties.put(entity.getEntityGuid(), entity.integerArrayProperties);
            enumProperties.put(entity.getEntityGuid(), entity.enumProperties);
            objectProperties.put(entity.getEntityGuid(), entity.objectProperties);
        }
        saveOverviews(database, overviews);
        //properties
        saveStrings(database, stringProperties);
        saveDates(database, dateTimeProperties);
        saveLongs(database, longProperties);
        saveDoubles(database, doubleProperties);
        saveIntegers(database, integerProperties);
        saveFloats(database, floatProperties);
        //array properties [NOTE arrays have old entries deleted first, for cases where a user reduced the amount of entries in the collection]
        saveArrayDates(database, dateTimeArrayProperties);
        saveArrayStrings(database, stringArrayProperties);
        saveArrayLongs(database, longArrayProperties);
        saveArrayDoubles(database, doubleArrayProperties);
        saveArrayIntegers(database, integerArrayProperties);
        saveArrayFloats(database, floatArrayProperties);
        //enums
        saveEnums(database, enumProperties);
        //objects and object arrays
        saveArrayObjects(database, objectArrayProperties);
        saveObjects(database, objectProperties);
    }

    public static void save(final JdsDatabase database, final int batchSize, final JdsEntity... entities) {
        save(database, batchSize, Arrays.asList(entities));
    }

    private static void saveOverviews(final JdsDatabase jdsDatabase, final HashSet<JdsEntityOverview> overviews) {
        int record = 0;
        int recordTotal = overviews.size();
        String updateSql = "UPDATE JdsRefEntityOverview SET DateModified = ? WHERE ActionId = ? AND EntityId = ?;";
        String insertSql = "INSERT INTO JdsRefEntityOverview(ActionId,DateCreated,DateModified,EntityId) VALUES (?,?,?,?);";
        try (Connection outerConnection = jdsDatabase.getConnection();
             PreparedStatement update = outerConnection.prepareStatement(updateSql);
             PreparedStatement insert = outerConnection.prepareStatement(insertSql)) {
            for (JdsEntityOverview overview : overviews) {
                record++;
                update.clearParameters();
                update.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                update.setString(2, overview.getEntityGuid());
                update.setLong(3, overview.getEntityCode());
                if (update.executeUpdate() == 0) {
                    insert.clearParameters();
                    insert.setString(1, overview.getEntityGuid());
                    insert.setTimestamp(2, Timestamp.valueOf(overview.getDateCreated()));
                    insert.setTimestamp(3, Timestamp.valueOf(overview.getDateModified()));
                    insert.setLong(4, overview.getEntityCode());
                    insert.executeUpdate();
                    System.out.printf("Saving Overview [%s of %s]", record, recordTotal);
                    System.out.println();
                } else {
                    System.out.printf("Updating Overview [%s of %s]", record, recordTotal);
                    System.out.println();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param jdsEntity
     */
    private static void mapEntity(final JdsDatabase jdsDatabase, final JdsEntity jdsEntity) {
        if (!JdsEntity.map.contains(jdsEntity.getEntityCode())) {
            JdsEntity.map.add(jdsEntity.getEntityCode());
            jdsDatabase.mapClassName(jdsEntity.getEntityCode(), jdsEntity.getEntityName());
            jdsDatabase.mapClassFields(jdsEntity.getEntityCode(), jdsEntity.allFields);
            jdsDatabase.mapClassEnums(jdsEntity.getEntityCode(), jdsEntity.allEnums);
            System.out.printf("Mapped Entity [%s]\n", jdsEntity.getEntityName());
        }
    }

    /**
     * @param jdsDatabase
     * @param objectProperties
     */
    private static void saveObjects(final JdsDatabase jdsDatabase, final HashMap<String, HashMap<Long, SimpleObjectProperty<? extends JdsEntity>>> objectProperties) {
        if (objectProperties.size() == 0) return;//prevent stack overflow :)
        int record = 0;
        Collection<JdsEntity> collection = new ArrayList<>();
        String sql = "INSERT INTO JdsStoreEntitySubclass (ActionId,SubActionId,EntityId) VALUES (?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (Map.Entry<String, HashMap<Long, SimpleObjectProperty<? extends JdsEntity>>> entry : objectProperties.entrySet()) {
                String actionId = entry.getKey();
                int innerRecord = 0;
                int innerRecordSize = entry.getValue().size();
                if (innerRecordSize == 0) continue;
                for (Map.Entry<Long, SimpleObjectProperty<? extends JdsEntity>> recordEntry : entry.getValue().entrySet()) {
                    long entityCode = recordEntry.getKey();
                    JdsEntity value = recordEntry.getValue().get();
                    if (value == null) continue;
                    preparedStatement.clearParameters();
                    preparedStatement.setString(1, actionId);
                    preparedStatement.setString(2, value.getEntityGuid());
                    preparedStatement.setLong(3, entityCode);
                    preparedStatement.executeUpdate();
                    collection.add(value);
                    innerRecord++;
                    System.out.printf("Saving inner Object [%s]. Object field [%s of %s]", record, innerRecord, innerRecordSize);
                    System.out.println();
                }
                record++;
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
        if (collection.size() == 0) return;
        save(jdsDatabase, -1, collection);
    }

    /**
     * @param jdsDatabase
     * @param integerProperties
     */
    private static void saveIntegers(final JdsDatabase jdsDatabase, final HashMap<String, HashMap<Long, SimpleIntegerProperty>> integerProperties) {
        int record = 0;
        String updateSql = "UPDATE JdsStoreInteger SET Value = ? WHERE FieldId =? AND ActionId = ?";
        String insertSql = "INSERT INTO JdsStoreInteger (FieldId,ActionId,Value) VALUES (?,?,?)";
        String logSql = "INSERT INTO JdsStoreOldFieldValues(ActionId,FieldId,IntegerValue) VALUES(?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement update = connection.prepareStatement(updateSql);
             PreparedStatement insert = connection.prepareStatement(insertSql);
             PreparedStatement log = connection.prepareStatement(logSql)) {
            for (Map.Entry<String, HashMap<Long, SimpleIntegerProperty>> entry : integerProperties.entrySet()) {
                record++;
                int innerRecord = 0;
                int innerRecordSize = entry.getValue().size();
                if (innerRecordSize == 0) continue;
                String actionId = entry.getKey();
                for (Map.Entry<Long, SimpleIntegerProperty> recordEntry : entry.getValue().entrySet()) {
                    long fieldId = recordEntry.getKey();
                    int value = recordEntry.getValue().get();
                    innerRecord++;
                    update.clearParameters();
                    update.setInt(1, value);
                    update.setLong(2, fieldId);
                    update.setString(3, actionId);
                    if (update.executeUpdate() == 0) {
                        insert.clearParameters();
                        insert.setLong(1, fieldId);
                        insert.setString(2, actionId);
                        insert.setInt(3, value);
                        insert.executeUpdate();
                        System.out.printf("Saving record [%s]. Text field [%s of %s]\n", record, innerRecord, innerRecordSize);
                    } else {
                        System.out.printf("Updating record [%s]. Text field [%s of %s]\n", record, innerRecord, innerRecordSize);
                    }
                    if (!jdsDatabase.logEdits()) continue;
                    log.clearParameters();
                    log.setString(1, actionId);
                    log.setLong(2, fieldId);
                    log.setInt(3, value);
                    log.executeUpdate();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param jdsDatabase
     * @param floatProperties
     */
    private static void saveFloats(final JdsDatabase jdsDatabase, final HashMap<String, HashMap<Long, SimpleFloatProperty>> floatProperties) {
        int record = 0;
        String updateSql = "UPDATE JdsStoreFloat SET Value = ? WHERE FieldId =? AND ActionId = ?";
        String insertSql = "INSERT INTO JdsStoreFloat (FieldId,ActionId,Value) VALUES (?,?,?)";
        String logSql = "INSERT INTO JdsStoreOldFieldValues(ActionId,FieldId,FloatValue) VALUES(?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement update = connection.prepareStatement(updateSql);
             PreparedStatement insert = connection.prepareStatement(insertSql);
             PreparedStatement log = connection.prepareStatement(logSql)) {
            for (Map.Entry<String, HashMap<Long, SimpleFloatProperty>> entry : floatProperties.entrySet()) {
                record++;
                int innerRecord = 0;
                int innerRecordSize = entry.getValue().size();
                if (innerRecordSize == 0) continue;
                String actionId = entry.getKey();
                for (Map.Entry<Long, SimpleFloatProperty> recordEntry : entry.getValue().entrySet()) {
                    long fieldId = recordEntry.getKey();
                    float value = recordEntry.getValue().get();
                    innerRecord++;
                    update.clearParameters();
                    update.setFloat(1, value);
                    update.setLong(2, fieldId);
                    update.setString(3, actionId);
                    if (update.executeUpdate() == 0) {
                        insert.clearParameters();
                        insert.setLong(1, fieldId);
                        insert.setString(2, actionId);
                        insert.setFloat(3, value);
                        insert.executeUpdate();
                        System.out.printf("Updating record [%s]. Text field [%s of %s]\n", record, innerRecord, innerRecordSize);
                    } else {
                        System.out.printf("Saving record [%s]. Text field [%s of %s]\n", record, innerRecord, innerRecordSize);
                    }
                    if (!jdsDatabase.logEdits()) continue;
                    log.clearParameters();
                    log.setString(1, actionId);
                    log.setLong(2, fieldId);
                    log.setFloat(3, value);
                    log.executeUpdate();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param jdsDatabase
     * @param doubleProperties
     */
    private static void saveDoubles(final JdsDatabase jdsDatabase, final HashMap<String, HashMap<Long, SimpleDoubleProperty>> doubleProperties) {
        int record = 0;
        String updateSql = "UPDATE JdsStoreDouble SET Value = ? WHERE FieldId =? AND ActionId = ?";
        String insertSql = "INSERT INTO JdsStoreDouble (FieldId,ActionId,Value) VALUES (?,?,?)";
        String logSql = "INSERT INTO JdsStoreOldFieldValues(ActionId,FieldId,DoubleValue) VALUES(?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement update = connection.prepareStatement(updateSql);
             PreparedStatement insert = connection.prepareStatement(insertSql);
             PreparedStatement log = connection.prepareStatement(logSql)) {
            for (Map.Entry<String, HashMap<Long, SimpleDoubleProperty>> entry : doubleProperties.entrySet()) {
                record++;
                int innerRecord = 0;
                int innerRecordSize = entry.getValue().size();
                if (innerRecordSize == 0) continue;
                String actionId = entry.getKey();
                for (Map.Entry<Long, SimpleDoubleProperty> recordEntry : entry.getValue().entrySet()) {
                    long fieldId = recordEntry.getKey();
                    double value = recordEntry.getValue().get();
                    innerRecord++;
                    update.clearParameters();
                    update.setDouble(1, value);
                    update.setLong(2, fieldId);
                    update.setString(3, actionId);
                    if (update.executeUpdate() == 0) {
                        insert.clearParameters();
                        insert.setLong(1, fieldId);
                        insert.setString(2, actionId);
                        insert.setDouble(3, value);
                        insert.executeUpdate();
                        System.out.printf("Updating record [%s]. Text field [%s of %s]\n", record, innerRecord, innerRecordSize);
                    } else {
                        System.out.printf("Saving record [%s]. Text field [%s of %s]\n", record, innerRecord, innerRecordSize);
                    }
                    if (!jdsDatabase.logEdits()) continue;
                    log.clearParameters();
                    log.setString(1, actionId);
                    log.setLong(2, fieldId);
                    log.setDouble(3, value);
                    log.executeUpdate();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param jdsDatabase
     * @param longProperties
     */
    private static void saveLongs(final JdsDatabase jdsDatabase, final HashMap<String, HashMap<Long, SimpleLongProperty>> longProperties) {
        int record = 0;
        String updateSql = "UPDATE JdsStoreLong SET Value = ? WHERE FieldId =? AND ActionId = ?";
        String insertSql = "INSERT INTO JdsStoreLong (FieldId,ActionId,Value) VALUES (?,?,?)";
        String logSql = "INSERT INTO JdsStoreOldFieldValues(ActionId,FieldId,LongValue) VALUES(?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement update = connection.prepareStatement(updateSql);
             PreparedStatement insert = connection.prepareStatement(insertSql);
             PreparedStatement log = connection.prepareStatement(logSql)) {
            for (Map.Entry<String, HashMap<Long, SimpleLongProperty>> entry : longProperties.entrySet()) {
                record++;
                int innerRecord = 0;
                int innerRecordSize = entry.getValue().size();
                if (innerRecordSize == 0) continue;
                String actionId = entry.getKey();
                for (Map.Entry<Long, SimpleLongProperty> recordEntry : entry.getValue().entrySet()) {
                    long fieldId = recordEntry.getKey();
                    long value = recordEntry.getValue().get();
                    innerRecord++;
                    update.clearParameters();
                    update.setLong(1, value);
                    update.setLong(2, fieldId);
                    update.setString(3, actionId);
                    if (update.executeUpdate() == 0) {
                        insert.clearParameters();
                        insert.setLong(1, fieldId);
                        insert.setString(2, actionId);
                        insert.setLong(3, value);
                        insert.executeUpdate();
                        System.out.printf("Updating record [%s]. Long field [%s of %s]\n", record, innerRecord, innerRecordSize);
                    } else {
                        System.out.printf("Saving record [%s]. Long field [%s of %s]\n", record, innerRecord, innerRecordSize);
                    }
                    if (!jdsDatabase.logEdits()) continue;
                    log.clearParameters();
                    log.setString(1, actionId);
                    log.setLong(2, fieldId);
                    log.setLong(3, value);
                    log.executeUpdate();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param jdsDatabase
     * @param stringProperties
     */
    private static void saveStrings(final JdsDatabase jdsDatabase, final HashMap<String, HashMap<Long, SimpleStringProperty>> stringProperties) {
        int record = 0;
        String updateSql = "UPDATE JdsStoreText SET Value = ? WHERE FieldId =? AND ActionId = ?";
        String insertSql = "INSERT INTO JdsStoreText (FieldId,ActionId,Value) VALUES (?,?,?)";
        String logSql = "INSERT INTO JdsStoreOldFieldValues(ActionId,FieldId,TextValue) VALUES(?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement update = connection.prepareStatement(updateSql);
             PreparedStatement insert = connection.prepareStatement(insertSql);
             PreparedStatement log = connection.prepareStatement(logSql)) {
            for (Map.Entry<String, HashMap<Long, SimpleStringProperty>> entry : stringProperties.entrySet()) {
                record++;
                int innerRecord = 0;
                int innerRecordSize = entry.getValue().size();
                if (innerRecordSize == 0) continue;
                String actionId = entry.getKey();
                for (Map.Entry<Long, SimpleStringProperty> recordEntry : entry.getValue().entrySet()) {
                    innerRecord++;
                    long fieldId = recordEntry.getKey();
                    String value = recordEntry.getValue().get();
                    update.clearParameters();
                    update.setString(1, value);
                    update.setLong(2, fieldId);
                    update.setString(3, actionId);
                    if (update.executeUpdate() == 0) {
                        insert.clearParameters();
                        insert.setLong(1, fieldId);
                        insert.setString(2, actionId);
                        insert.setString(3, value);
                        insert.executeUpdate();
                        System.out.printf("Saving record [%s]. Text field [%s of %s]\n", record, innerRecord, innerRecordSize);
                    } else {
                        System.out.printf("Updating record [%s]. Text field [%s of %s]\n", record, innerRecord, innerRecordSize);
                    }
                    if (!jdsDatabase.logEdits()) continue;
                    log.clearParameters();
                    log.setString(1, actionId);
                    log.setLong(2, fieldId);
                    log.setString(3, value);
                    log.executeUpdate();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param jdsDatabase
     * @param dateProperties
     */
    private static void saveDates(final JdsDatabase jdsDatabase, final HashMap<String, HashMap<Long, SimpleObjectProperty<LocalDateTime>>> dateProperties) {
        int record = 0;
        String updateSql = "UPDATE JdsStoreDateTime SET Value = ? WHERE FieldId =? AND ActionId = ?";
        String insertSql = "INSERT INTO JdsStoreDateTime (FieldId,ActionId,Value) VALUES (?,?,?)";
        String logSql = "INSERT INTO JdsStoreOldFieldValues(ActionId,FieldId,DateTimeValue) VALUES(?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement update = connection.prepareStatement(updateSql);
             PreparedStatement insert = connection.prepareStatement(insertSql);
             PreparedStatement log = connection.prepareStatement(logSql)) {
            for (Map.Entry<String, HashMap<Long, SimpleObjectProperty<LocalDateTime>>> entry : dateProperties.entrySet()) {
                record++;
                int innerRecord = 0;
                int innerRecordSize = entry.getValue().size();
                if (innerRecordSize == 0) continue;
                String actionId = entry.getKey();
                for (Map.Entry<Long, SimpleObjectProperty<LocalDateTime>> recordEntry : entry.getValue().entrySet()) {
                    long fieldId = recordEntry.getKey();
                    LocalDateTime value = recordEntry.getValue().get();
                    innerRecord++;
                    update.clearParameters();
                    update.setTimestamp(1, Timestamp.valueOf(value));
                    update.setLong(2, fieldId);
                    update.setString(3, actionId);
                    if (update.executeUpdate() == 0) {
                        insert.clearParameters();
                        insert.setLong(1, fieldId);
                        insert.setString(2, actionId);
                        insert.setTimestamp(3, Timestamp.valueOf(value));
                        insert.executeUpdate();
                        System.out.printf("Updating record [%s]. Text field [%s of %s]\n", record, innerRecord, innerRecordSize);
                    } else {
                        System.out.printf("Saving record [%s]. Text field [%s of %s]\n", record, innerRecord, innerRecordSize);
                    }
                    if (!jdsDatabase.logEdits()) continue;
                    log.clearParameters();
                    log.setString(1, actionId);
                    log.setLong(2, fieldId);
                    log.setTimestamp(3, Timestamp.valueOf(value));
                    log.executeUpdate();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * Save all dates in one do
     *
     * @param dateTimeArrayProperties
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5]->[3,4]
     */
    private static void saveArrayDates(final JdsDatabase jdsDatabase, final HashMap<String, HashMap<Long, SimpleListProperty<LocalDateTime>>> dateTimeArrayProperties) {
        String deleteSql = "DELETE FROM JdsStoreDateTimeArray WHERE FieldId = ? AND ActionId = ?";
        String insertSql = "INSERT INTO JdsStoreDateTimeArray (Sequence,Value,FieldId,ActionId) VALUES (?,?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement delete = connection.prepareStatement(deleteSql);
             PreparedStatement insert = connection.prepareStatement(insertSql)) {
            int record = 0;
            for (Map.Entry<String, HashMap<Long, SimpleListProperty<LocalDateTime>>> entry : dateTimeArrayProperties.entrySet()) {
                record++;
                String actionId = entry.getKey();
                final SimpleIntegerProperty index = new SimpleIntegerProperty(0);
                for (Map.Entry<Long, SimpleListProperty<LocalDateTime>> it : entry.getValue().entrySet()) {
                    Long fieldId = it.getKey();
                    int innerRecord = 0;
                    int innerTotal = it.getValue().get().size();
                    for (LocalDateTime value : it.getValue().get()) {
                        delete.clearParameters();
                        delete.setLong(1, fieldId);
                        delete.setString(2, actionId);
                        delete.executeUpdate();
                        //insert
                        insert.clearParameters();
                        insert.setInt(1, index.get());
                        insert.setTimestamp(2, Timestamp.valueOf(value));
                        insert.setLong(3, fieldId);
                        insert.setString(4, actionId);
                        insert.executeUpdate();
                        index.set(index.get() + 1);
                        System.out.printf("Inserting array record [%s]. DateTime field [%s of %s]\n", record, innerRecord, innerTotal);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param jdsDatabase
     * @param floatArrayProperties
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5]->[3,4]
     */
    private static void saveArrayFloats(final JdsDatabase jdsDatabase, final HashMap<String, HashMap<Long, SimpleListProperty<Float>>> floatArrayProperties) {
        String deleteSql = "DELETE FROM JdsStoreFloatArray WHERE FieldId = ? AND ActionId = ?";
        String insertSql = "INSERT INTO JdsStoreFloatArray (FieldId,ActionId,Sequence,Value) VALUES (?,?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement delete = connection.prepareStatement(deleteSql);
             PreparedStatement insert = connection.prepareStatement(insertSql)) {
            int record = 0;
            for (Map.Entry<String, HashMap<Long, SimpleListProperty<Float>>> entry : floatArrayProperties.entrySet()) {
                record++;
                String actionId = entry.getKey();
                final SimpleIntegerProperty index = new SimpleIntegerProperty(0);
                for (Map.Entry<Long, SimpleListProperty<Float>> it : entry.getValue().entrySet()) {
                    Long fieldId = it.getKey();
                    int innerRecord = 0;
                    int innerTotal = it.getValue().get().size();
                    for (Float value : it.getValue().get()) {
                        delete.clearParameters();
                        delete.setLong(1, fieldId);
                        delete.setString(2, actionId);
                        delete.executeUpdate();
                        //insert
                        insert.clearParameters();
                        insert.setInt(1, index.get());
                        insert.setFloat(2, value);
                        insert.setLong(3, fieldId);
                        insert.setString(4, actionId);
                        insert.executeUpdate();
                        index.set(index.get() + 1);
                        System.out.printf("Inserting array record [%s]. Float field [%s of %s]\n", record, innerRecord, innerTotal);

                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param jdsDatabase
     * @param integerArrayProperties
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5]->[3,4]
     */
    private static void saveArrayIntegers(final JdsDatabase jdsDatabase, final HashMap<String, HashMap<Long, SimpleListProperty<Integer>>> integerArrayProperties) {
        String deleteSql = "DELETE FROM JdsStoreIntegerArray WHERE FieldId = ? AND ActionId = ?";
        String insertSql = "INSERT INTO JdsStoreIntegerArray (FieldId,ActionId,Sequence,Value) VALUES (?,?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement delete = connection.prepareStatement(deleteSql);
             PreparedStatement insert = connection.prepareStatement(insertSql)) {
            int record = 0;
            for (Map.Entry<String, HashMap<Long, SimpleListProperty<Integer>>> entry : integerArrayProperties.entrySet()) {
                record++;
                String actionId = entry.getKey();
                final SimpleIntegerProperty index = new SimpleIntegerProperty(0);
                for (Map.Entry<Long, SimpleListProperty<Integer>> it : entry.getValue().entrySet()) {
                    Long fieldId = it.getKey();
                    int innerRecord = 0;
                    int innerTotal = it.getValue().get().size();
                    for (Integer value : it.getValue().get()) {
                        delete.clearParameters();
                        delete.setLong(1, fieldId);
                        delete.setString(2, actionId);
                        delete.executeUpdate();
                        //insert
                        insert.clearParameters();
                        insert.setInt(1, index.get());
                        insert.setInt(2, value);
                        insert.setLong(3, fieldId);
                        insert.setString(4, actionId);
                        insert.executeUpdate();
                        index.set(index.get() + 1);
                        System.out.printf("Inserting array record [%s]. Integer field [%s of %s]\n", record, innerRecord, innerTotal);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param jdsDatabase
     * @param doubleArrayProperties
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5]->[3,4]
     */
    private static void saveArrayDoubles(final JdsDatabase jdsDatabase, final HashMap<String, HashMap<Long, SimpleListProperty<Double>>> doubleArrayProperties) {
        String deleteSql = "DELETE FROM JdsStoreDoubleArray WHERE FieldId = ? AND ActionId = ?";
        String insertSql = "INSERT INTO JdsStoreDoubleArray (FieldId,ActionId,Sequence,Value) VALUES (?,?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement delete = connection.prepareStatement(deleteSql);
             PreparedStatement insert = connection.prepareStatement(insertSql)) {
            int record = 0;
            for (Map.Entry<String, HashMap<Long, SimpleListProperty<Double>>> entry : doubleArrayProperties.entrySet()) {
                record++;
                String actionId = entry.getKey();
                final SimpleIntegerProperty index = new SimpleIntegerProperty(0);
                for (Map.Entry<Long, SimpleListProperty<Double>> it : entry.getValue().entrySet()) {
                    Long fieldId = it.getKey();
                    int innerRecord = 0;
                    int innerTotal = it.getValue().get().size();
                    for (Double value : it.getValue().get()) {
                        delete.clearParameters();
                        delete.setLong(1, fieldId);
                        delete.setString(2, actionId);
                        delete.executeUpdate();
                        //insert
                        insert.clearParameters();
                        insert.setInt(1, index.get());
                        insert.setDouble(2, value);
                        insert.setLong(3, fieldId);
                        insert.setString(4, actionId);
                        insert.executeUpdate();
                        index.set(index.get() + 1);
                        System.out.printf("Inserting array record [%s]. Double field [%s of %s]\n", record, innerRecord, innerTotal);

                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param jdsDatabase
     * @param longArrayProperties
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5]->[3,4]
     */
    private static void saveArrayLongs(final JdsDatabase jdsDatabase, final HashMap<String, HashMap<Long, SimpleListProperty<Long>>> longArrayProperties) {
        String deleteSql = "DELETE FROM JdsStoreDoubleArray WHERE FieldId = ? AND ActionId = ?";
        String insertSql = "INSERT INTO JdsStoreDoubleArray (FieldId,ActionId,Sequence,Value) VALUES (?,?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement delete = connection.prepareStatement(deleteSql);
             PreparedStatement insert = connection.prepareStatement(insertSql)) {
            int record = 0;
            for (Map.Entry<String, HashMap<Long, SimpleListProperty<Long>>> entry : longArrayProperties.entrySet()) {
                record++;
                String actionId = entry.getKey();
                final SimpleIntegerProperty index = new SimpleIntegerProperty(0);
                for (Map.Entry<Long, SimpleListProperty<Long>> it : entry.getValue().entrySet()) {
                    Long fieldId = it.getKey();
                    int innerRecord = 0;
                    int innerTotal = it.getValue().get().size();
                    for (Long value : it.getValue().get()) {
                        delete.clearParameters();
                        delete.setLong(1, fieldId);
                        delete.setString(2, actionId);
                        delete.executeUpdate();
                        //insert
                        insert.clearParameters();
                        insert.setInt(1, index.get());
                        insert.setLong(2, value);
                        insert.setLong(3, fieldId);
                        insert.setString(4, actionId);
                        insert.executeUpdate();
                        index.set(index.get() + 1);
                        System.out.printf("Inserting array record [%s]. Long field [%s of %s]\n", record, innerRecord, innerTotal);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param jdsDatabase
     * @param stringArrayProperties
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5]->[3,4]
     */
    private static void saveArrayStrings(final JdsDatabase jdsDatabase, final HashMap<String, HashMap<Long, SimpleListProperty<String>>> stringArrayProperties) {
        String deleteSql = "DELETE FROM JdsStoreTextArray WHERE FieldId = ? AND ActionId = ?";
        String insertSql = "INSERT INTO JdsStoreTextArray (FieldId,ActionId,Sequence,Value) VALUES (?,?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement delete = connection.prepareStatement(deleteSql);
             PreparedStatement insert = connection.prepareStatement(insertSql)) {
            int record = 0;
            for (Map.Entry<String, HashMap<Long, SimpleListProperty<String>>> entry : stringArrayProperties.entrySet()) {
                record++;
                String actionId = entry.getKey();
                final SimpleIntegerProperty index = new SimpleIntegerProperty(0);
                for (Map.Entry<Long, SimpleListProperty<String>> it : entry.getValue().entrySet()) {
                    Long fieldId = it.getKey();
                    int innerRecord = 0;
                    int innerTotal = it.getValue().get().size();
                    for (String value : it.getValue().get()) {
                        delete.clearParameters();
                        delete.setLong(1, fieldId);
                        delete.setString(2, actionId);
                        delete.executeUpdate();
                        //insert
                        insert.clearParameters();
                        insert.setInt(1, index.get());
                        insert.setString(2, value);
                        insert.setLong(3, fieldId);
                        insert.setString(4, actionId);
                        insert.executeUpdate();
                        index.set(index.get() + 1);
                        System.out.printf("Inserting array record [%s]. String field [%s of %s]\n", record, innerRecord, innerTotal);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param jdsDatabase
     * @param objectArrayProperties
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5]->[3,4]
     */
    private static void saveArrayObjects(final JdsDatabase jdsDatabase, final HashMap<String, HashMap<Long, SimpleListProperty<? extends JdsEntity>>> objectArrayProperties) {
        Collection<Jds_StoreEntitySubclass> bindings = new ArrayList<>();
        Collection<JdsEntity> jdsEntities = new ArrayList<>();
        for (Map.Entry<String, HashMap<Long, SimpleListProperty<? extends JdsEntity>>> serviceCodeEntities : objectArrayProperties.entrySet()) {
            String upperActionId = serviceCodeEntities.getKey();
            for (Map.Entry<Long, SimpleListProperty<? extends JdsEntity>> serviceCodeEntity : serviceCodeEntities.getValue().entrySet()) {
                long serviceCode = serviceCodeEntity.getKey();
                serviceCodeEntity.getValue().filtered(jdsEntity -> jdsEntity != null).parallelStream().forEach(jdsEntity -> {
                    jdsEntities.add(jdsEntity);
                    Jds_StoreEntitySubclass binding = new Jds_StoreEntitySubclass();
                    binding.setActionId(upperActionId);
                    binding.setSubActionId(jdsEntity.getEntityGuid());
                    binding.setEntityId(serviceCode);
                    bindings.add(binding);
                });
            }
        }
        int record = 0;
        String deleteSql = "DELETE FROM JdsStoreEntitySubclass WHERE ActionId = ? AND SubActionId = ? AND EntityId = ?";
        String insertSql = "INSERT INTO JdsStoreEntitySubclass (ActionId,SubActionId,EntityId) VALUES (?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement delete = connection.prepareStatement(deleteSql);
             PreparedStatement insert = connection.prepareStatement(insertSql)) {
            for (Jds_StoreEntitySubclass bind : bindings) {
                if (bind == null) continue;
                if (bind.getActionId() == null) continue;
                delete.clearParameters();
                delete.setString(1, bind.getActionId());
                delete.setString(2, bind.getSubActionId());
                delete.setLong(3, bind.getEntityId());
                delete.executeUpdate();
                record++;
                insert.clearParameters();
                insert.setString(1, bind.getActionId());
                insert.setString(2, bind.getSubActionId());
                insert.setLong(3, bind.getEntityId());
                insert.executeUpdate();
                System.out.printf("Binding object array. [%s]", record);
                System.out.println();
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
        if (jdsEntities.size() == 0) return;
        save(jdsDatabase, -1, jdsEntities);
    }

    /**
     * @param database
     * @param enumStrings
     * @apiNote Enums are actually saved as index based integer arrays
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5]->[3,4]
     */
    private static void saveEnums(final JdsDatabase database, final HashMap<String, HashMap<JdsFieldEnum, SimpleListProperty<String>>> enumStrings) {
        int record = 0;
        int recordTotal = enumStrings.size();
        String updateSql = "UPDATE JdsStoreIntegerArray SET Value = ? WHERE FieldId = ? AND ActionId = ? AND Sequence = ?";
        String insertSql = "INSERT INTO JdsStoreIntegerArray (FieldId,ActionId,Sequence,Value) VALUES (?,?,?,?)";
        try (Connection outerConnection = database.getConnection();
             PreparedStatement insert = outerConnection.prepareStatement(insertSql);
             PreparedStatement update = outerConnection.prepareStatement(updateSql)) {
            for (Map.Entry<String, HashMap<JdsFieldEnum, SimpleListProperty<String>>> entry : enumStrings.entrySet()) {
                record++;
                String actionId = entry.getKey();
                for (Map.Entry<JdsFieldEnum, SimpleListProperty<String>> fieldEnums : entry.getValue().entrySet()) {
                    int dex = 0;
                    JdsFieldEnum fieldId = fieldEnums.getKey();
                    ObservableList<String> textValues = fieldEnums.getValue().get();
                    if (textValues.size() == 0) continue;
                    for (String enumText : textValues) {
                        update.clearParameters();
                        update.setInt(1, fieldId.getIndex(enumText));
                        update.setLong(2, fieldId.getField().getId());
                        update.setString(3, actionId);
                        update.setInt(4, dex);
                        if (update.executeUpdate() == 0) {
                            insert.clearParameters();
                            insert.setLong(1, fieldId.getField().getId());
                            insert.setString(2, actionId);
                            insert.setInt(3, dex);
                            insert.setInt(4, fieldId.getIndex(enumText));
                            insert.executeUpdate();
                            System.out.printf("Saving enum [%s]. Object field [%s of %s]\n", dex, record, recordTotal);
                        } else {
                            System.out.printf("Updating enum [%s]. Object field [%s of %s]\n", dex, record, recordTotal);
                        }
                        dex++;

                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }
}
