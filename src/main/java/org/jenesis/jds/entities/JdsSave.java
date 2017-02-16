package org.jenesis.jds.entities;

import javafx.beans.property.*;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Created by ifunga on 12/02/2017.
 */
public class JdsSave {

    public static void save(final JdsDatabase database, final int batchSize, final Collection<? extends JdsEntity> entities) {
        //stringProperties and dateProperties
        HashMap<String, HashMap<Long, SimpleObjectProperty<LocalDateTime>>> dates = new HashMap<>();
        HashMap<String, HashMap<Long, SimpleStringProperty>> strings = new HashMap<>();
        //numeric
        HashMap<String, HashMap<Long, SimpleFloatProperty>> floats = new HashMap<>();
        HashMap<String, HashMap<Long, SimpleDoubleProperty>> doubles = new HashMap<>();
        HashMap<String, HashMap<Long, SimpleLongProperty>> longs = new HashMap<>();
        HashMap<String, HashMap<Long, SimpleIntegerProperty>> integers = new HashMap<>();
        //arrays
        HashMap<String, HashMap<Long, SimpleListProperty<? extends JdsEntity>>> arraysObjects = new HashMap<>();
        HashMap<String, HashMap<Long, SimpleListProperty<String>>> arrayStrings = new HashMap<>();
        HashMap<String, HashMap<Long, SimpleListProperty<LocalDateTime>>> arrayDates = new HashMap<>();
        HashMap<String, HashMap<Long, SimpleListProperty<Float>>> arrayFloats = new HashMap<>();
        HashMap<String, HashMap<Long, SimpleListProperty<Double>>> arrayDoubles = new HashMap<>();
        HashMap<String, HashMap<Long, SimpleListProperty<Long>>> arrayLongs = new HashMap<>();
        HashMap<String, HashMap<Long, SimpleListProperty<Integer>>> arrayIntegers = new HashMap<>();
        //enums
        HashMap<String, HashMap<JdsFieldEnum, SimpleListProperty<String>>> enumStrings = new HashMap<>();
        //objectProperties
        HashMap<String, HashMap<Long, SimpleObjectProperty<? extends JdsEntity>>> objects = new HashMap<>();
        HashSet<JdsEntityOverview> overviews = new HashSet<>();
        //fire
        for (final JdsEntity entity : entities) {
            mapEntity(entity);
            overviews.add(entity.getOverview());
            dates.put(entity.getActionId(), entity.dateProperties);
            strings.put(entity.getActionId(), entity.stringProperties);
            floats.put(entity.getActionId(), entity.floatProperties);
            doubles.put(entity.getActionId(), entity.doubleProperties);
            longs.put(entity.getActionId(), entity.longProperties);
            integers.put(entity.getActionId(), entity.integerProperties);
            arraysObjects.put(entity.getActionId(), entity.objectArrayProperties);
            arrayStrings.put(entity.getActionId(), entity.stringArrayProperties);
            arrayDates.put(entity.getActionId(), entity.jdsArrayDates);
            arrayFloats.put(entity.getActionId(), entity.jdsArrayFloats);
            arrayDoubles.put(entity.getActionId(), entity.jdsArrayDoubles);
            arrayLongs.put(entity.getActionId(), entity.jdsArrayLongs);
            arrayIntegers.put(entity.getActionId(), entity.jdsArrayIntegers);
            enumStrings.put(entity.getActionId(), entity.enumProperties);
            objects.put(entity.getActionId(), entity.objectProperties);
        }
        saveOverviews(database, overviews); //actionId, dateCreated, dateModified, serviceId
        //single values
        saveStrings(database, strings);
        saveDates(database, dates);
        saveLongs(database, longs);
        saveDoubles(database, doubles);
        saveIntegers(database, integers);
        saveFloats(database, floats);
        //array values
        saveArrayDates(database, arrayDates);
        saveArrayStrings(database, arrayStrings);
        saveArrayLongs(database, arrayLongs);
        saveArrayDoubles(database, arrayDoubles);
        saveArrayIntegers(database, arrayIntegers);
        saveArrayFloats(database, arrayFloats);
        //SLOWEST PARTS
        saveEnums(database, enumStrings);
        saveArrayObjects(database, arraysObjects);
        //NOT BAD
        saveObjects(database, objects);
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
                update.setString(2, overview.getActionId());
                update.setLong(3, overview.getEntityCode());
                if (update.executeUpdate() == 0) {
                    insert.clearParameters();
                    insert.setString(1, overview.getActionId());
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

    private static void mapEntity(final JdsEntity jdsEntity) {
        if (!JdsEntity.map.contains(jdsEntity.getEntityCode())) {
            JdsEntity.map.add(jdsEntity.getEntityCode());
            JdsDatabase.instance().mapClassName(jdsEntity.getEntityCode(), jdsEntity.getEntityName());
            JdsDatabase.instance().mapClassFields(jdsEntity.getEntityCode(), jdsEntity.allFields);
            JdsDatabase.instance().mapClassEnums(jdsEntity.getEntityCode(), jdsEntity.allEnums);
            System.out.printf("Mapped Entity [%s]\n", jdsEntity.getEntityName());
        }
    }

    private static void saveEnums(final JdsDatabase database, final HashMap<String, HashMap<JdsFieldEnum, SimpleListProperty<String>>> enumStrings) {
        int record = 0;
        int recordTotal = enumStrings.size();
        String updateSql = "UPDATE JdsStoreIntegerArray SET Value = ? WHERE FieldId = ? AND ActionId = ? AND EnumIndex = ?";
        String insertSql = "INSERT INTO JdsStoreIntegerArray (FieldId,ActionId,EnumIndex,Value) VALUES (?,?,?,?)";
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
                        update.setString(4, actionId);
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

    private static void saveObjects(final JdsDatabase jdsDatabase, final HashMap<String, HashMap<Long, SimpleObjectProperty<? extends JdsEntity>>> objects) {
        if (objects.size() == 0) return;//prevent stack overflow :)
        int record = 0;
        Collection<JdsEntity> collection = new ArrayList<>();
        String sql = "INSERT INTO JdsStoreEntitySubclass (ActionId,SubActionId,EntityId) VALUES (?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (Map.Entry<String, HashMap<Long, SimpleObjectProperty<? extends JdsEntity>>> entry : objects.entrySet()) {
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
                    preparedStatement.setString(2, value.getActionId());
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

    private static void saveIntegers(final JdsDatabase jdsDatabase, final HashMap<String, HashMap<Long, SimpleIntegerProperty>> integers) {
        int record = 0;
        String updateSql = "UPDATE JdsStoreInteger SET Value = ? WHERE FieldId =? AND ActionId = ?";
        String insertSql = "INSERT INTO JdsStoreInteger (FieldId,ActionId,Value) VALUES (?,?,?)";
        String logSql = "INSERT INTO JdsRefOldFieldValues(ActionId,FieldId,IntegerValue) VALUES(?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement update = connection.prepareStatement(updateSql);
             PreparedStatement insert = connection.prepareStatement(insertSql);
             PreparedStatement log = connection.prepareStatement(logSql)) {
            for (Map.Entry<String, HashMap<Long, SimpleIntegerProperty>> entry : integers.entrySet()) {
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
                        System.out.printf("Updating record [%s]. Text field [%s of %s]\n", record, innerRecord, innerRecordSize);
                    } else {
                        System.out.printf("Saving record [%s]. Text field [%s of %s]\n", record, innerRecord, innerRecordSize);
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

    private static void saveFloats(final JdsDatabase jdsDatabase, final HashMap<String, HashMap<Long, SimpleFloatProperty>> floats) {
        int record = 0;
        String updateSql = "UPDATE JdsStoreFloat SET Value = ? WHERE FieldId =? AND ActionId = ?";
        String insertSql = "INSERT INTO JdsStoreFloat (FieldId,ActionId,Value) VALUES (?,?,?)";
        String logSql = "INSERT INTO JdsRefOldFieldValues(ActionId,FieldId,FloatValue) VALUES(?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement update = connection.prepareStatement(updateSql);
             PreparedStatement insert = connection.prepareStatement(insertSql);
             PreparedStatement log = connection.prepareStatement(logSql)) {
            for (Map.Entry<String, HashMap<Long, SimpleFloatProperty>> entry : floats.entrySet()) {
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

    private static void saveDoubles(final JdsDatabase jdsDatabase, final HashMap<String, HashMap<Long, SimpleDoubleProperty>> doubles) {
        int record = 0;
        String updateSql = "UPDATE JdsStoreDouble SET Value = ? WHERE FieldId =? AND ActionId = ?";
        String insertSql = "INSERT INTO JdsStoreDouble (FieldId,ActionId,Value) VALUES (?,?,?)";
        String logSql = "INSERT INTO JdsRefOldFieldValues(ActionId,FieldId,DoubleValue) VALUES(?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement update = connection.prepareStatement(updateSql);
             PreparedStatement insert = connection.prepareStatement(insertSql);
             PreparedStatement log = connection.prepareStatement(logSql)) {
            for (Map.Entry<String, HashMap<Long, SimpleDoubleProperty>> entry : doubles.entrySet()) {
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

    private static void saveLongs(final JdsDatabase jdsDatabase, final HashMap<String, HashMap<Long, SimpleLongProperty>> longs) {
        int record = 0;
        String updateSql = "UPDATE JdsStoreLong SET Value = ? WHERE FieldId =? AND ActionId = ?";
        String insertSql = "INSERT INTO JdsStoreLong (FieldId,ActionId,Value) VALUES (?,?,?)";
        String logSql = "INSERT INTO JdsRefOldFieldValues(ActionId,FieldId,LongValue) VALUES(?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement update = connection.prepareStatement(updateSql);
             PreparedStatement insert = connection.prepareStatement(insertSql);
             PreparedStatement log = connection.prepareStatement(logSql)) {
            for (Map.Entry<String, HashMap<Long, SimpleLongProperty>> entry : longs.entrySet()) {
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

    private static void saveStrings(final JdsDatabase jdsDatabase, final HashMap<String, HashMap<Long, SimpleStringProperty>> strings) {
        int record = 0;
        String updateSql = "UPDATE JdsStoreText SET Value = ? WHERE FieldId =? AND ActionId = ?";
        String insertSql = "INSERT INTO JdsStoreText (FieldId,ActionId,Value) VALUES (?,?,?)";
        String logSql = "INSERT INTO JdsRefOldFieldValues(ActionId,FieldId,TextValue) VALUES(?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement update = connection.prepareStatement(updateSql);
             PreparedStatement insert = connection.prepareStatement(insertSql);
             PreparedStatement log = connection.prepareStatement(logSql)) {
            for (Map.Entry<String, HashMap<Long, SimpleStringProperty>> entry : strings.entrySet()) {
                record++;
                int innerRecord = 0;
                int innerRecordSize = entry.getValue().size();
                if (innerRecordSize == 0) continue;
                String actionId = entry.getKey();
                for (Map.Entry<Long, SimpleStringProperty> recordEntry : entry.getValue().entrySet()) {
                    long fieldId = recordEntry.getKey();
                    String value = recordEntry.getValue().get();
                    if (value.isEmpty()) continue;
                    innerRecord++;
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
                        System.out.printf("Updating record [%s]. Text field [%s of %s]\n", record, innerRecord, innerRecordSize);
                    } else {
                        System.out.printf("Saving record [%s]. Text field [%s of %s]\n", record, innerRecord, innerRecordSize);
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

    private static void saveDates(final JdsDatabase jdsDatabase, final HashMap<String, HashMap<Long, SimpleObjectProperty<LocalDateTime>>> strings) {
        int record = 0;
        String updateSql = "UPDATE JdsStoreDateTime SET Value = ? WHERE FieldId =? AND ActionId = ?";
        String insertSql = "INSERT INTO JdsStoreDateTime (FieldId,ActionId,Value) VALUES (?,?,?)";
        String logSql = "INSERT INTO JdsRefOldFieldValues(ActionId,FieldId,DateTimeValue) VALUES(?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement update = connection.prepareStatement(updateSql);
             PreparedStatement insert = connection.prepareStatement(insertSql);
             PreparedStatement log = connection.prepareStatement(logSql)) {
            for (Map.Entry<String, HashMap<Long, SimpleObjectProperty<LocalDateTime>>> entry : strings.entrySet()) {
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
     * @param arrayDates
     */
    private static void saveArrayDates(final JdsDatabase jdsDatabase, final HashMap<String, HashMap<Long, SimpleListProperty<LocalDateTime>>> arrayDates) {
        String updateSql = "UPDATE JdsStoreDateTimeArray SET EnumIndex =?, Value = ? WHERE FieldId = ? AND ActionId = ?";
        String insertSql = "INSERT INTO JdsStoreDateTimeArray (EnumIndex,Value,FieldId,ActionId) VALUES (?,?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement update = connection.prepareStatement(updateSql);
             PreparedStatement insert = connection.prepareStatement(insertSql)) {
            int record = 0;
            for (Map.Entry<String, HashMap<Long, SimpleListProperty<LocalDateTime>>> entry : arrayDates.entrySet()) {
                record++;
                String actionId = entry.getKey();
                final SimpleIntegerProperty index = new SimpleIntegerProperty(0);
                for (Map.Entry<Long, SimpleListProperty<LocalDateTime>> it : entry.getValue().entrySet()) {
                    Long fieldId = it.getKey();
                    int innerRecord = 0;
                    int innerTotal = it.getValue().get().size();
                    for (LocalDateTime value : it.getValue().get()) {
                        update.clearParameters();
                        update.setInt(1, index.get());
                        update.setTimestamp(2, Timestamp.valueOf(value));
                        update.setLong(3, fieldId);
                        update.setString(4, actionId);
                        if (update.executeUpdate() == 0) {
                            insert.clearParameters();
                            insert.setInt(1, index.get());
                            insert.setTimestamp(2, Timestamp.valueOf(value));
                            insert.setLong(3, fieldId);
                            insert.setString(4, actionId);
                            insert.executeUpdate();
                            index.set(index.get() + 1);
                            System.out.printf("Inserting array record [%s]. DateTime field [%s of %s]\n", record, innerRecord, innerTotal);
                        } else {
                            System.out.printf("Updating array record [%s]. DateTime field [%s of %s]\n", record, innerRecord, innerTotal);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    private static void saveArrayFloats(final JdsDatabase jdsDatabase, final HashMap<String, HashMap<Long, SimpleListProperty<Float>>> arrayFloats) {
        String updateSql = "UPDATE JdsStoreFloatArray SET EnumIndex =?, Value = ? WHERE FieldId = ? AND ActionId = ?";
        String insertSql = "INSERT INTO JdsStoreFloatArray (FieldId,ActionId,EnumIndex,Value) VALUES (?,?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement update = connection.prepareStatement(updateSql);
             PreparedStatement insert = connection.prepareStatement(insertSql)) {
            int record = 0;
            for (Map.Entry<String, HashMap<Long, SimpleListProperty<Float>>> entry : arrayFloats.entrySet()) {
                record++;
                String actionId = entry.getKey();
                final SimpleIntegerProperty index = new SimpleIntegerProperty(0);
                for (Map.Entry<Long, SimpleListProperty<Float>> it : entry.getValue().entrySet()) {
                    Long fieldId = it.getKey();
                    int innerRecord = 0;
                    int innerTotal = it.getValue().get().size();
                    for (Float value : it.getValue().get()) {
                        update.clearParameters();
                        update.setInt(1, index.get());
                        update.setFloat(2, value);
                        update.setLong(3, fieldId);
                        update.setString(4, actionId);
                        if (update.executeUpdate() == 0) {
                            insert.clearParameters();
                            insert.setInt(1, index.get());
                            insert.setFloat(2, value);
                            insert.setLong(3, fieldId);
                            insert.setString(4, actionId);
                            insert.executeUpdate();
                            index.set(index.get() + 1);
                            System.out.printf("Inserting array record [%s]. Float field [%s of %s]\n", record, innerRecord, innerTotal);
                        } else {
                            System.out.printf("Updating array record [%s]. DateTime field [%s of %s]\n", record, innerRecord, innerTotal);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    private static void saveArrayIntegers(final JdsDatabase jdsDatabase, final HashMap<String, HashMap<Long, SimpleListProperty<Integer>>> arrayIntegers) {
        String updateSql = "UPDATE JdsStoreIntegerArray SET EnumIndex =?, Value = ? WHERE FieldId = ? AND ActionId = ?";
        String insertSql = "INSERT INTO JdsStoreIntegerArray (FieldId,ActionId,EnumIndex,Value) VALUES (?,?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement update = connection.prepareStatement(updateSql);
             PreparedStatement insert = connection.prepareStatement(insertSql)) {
            int record = 0;
            for (Map.Entry<String, HashMap<Long, SimpleListProperty<Integer>>> entry : arrayIntegers.entrySet()) {
                record++;
                String actionId = entry.getKey();
                final SimpleIntegerProperty index = new SimpleIntegerProperty(0);
                for (Map.Entry<Long, SimpleListProperty<Integer>> it : entry.getValue().entrySet()) {
                    Long fieldId = it.getKey();
                    int innerRecord = 0;
                    int innerTotal = it.getValue().get().size();
                    for (Integer value : it.getValue().get()) {
                        update.clearParameters();
                        update.setInt(1, index.get());
                        update.setInt(2, value);
                        update.setLong(3, fieldId);
                        update.setString(4, actionId);
                        if (update.executeUpdate() == 0) {
                            insert.clearParameters();
                            insert.setInt(1, index.get());
                            insert.setInt(2, value);
                            insert.setLong(3, fieldId);
                            insert.setString(4, actionId);
                            insert.executeUpdate();
                            index.set(index.get() + 1);
                            System.out.printf("Inserting array record [%s]. Integer field [%s of %s]\n", record, innerRecord, innerTotal);
                        } else {
                            System.out.printf("Updating array record [%s]. Integer field [%s of %s]\n", record, innerRecord, innerTotal);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    private static void saveArrayDoubles(final JdsDatabase jdsDatabase, final HashMap<String, HashMap<Long, SimpleListProperty<Double>>> arrayDoubles) {
        String updateSql = "UPDATE JdsStoreDoubleArray SET EnumIndex =?, Value = ? WHERE FieldId = ? AND ActionId = ?";
        String insertSql = "INSERT INTO JdsStoreDoubleArray (FieldId,ActionId,EnumIndex,Value) VALUES (?,?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement update = connection.prepareStatement(updateSql);
             PreparedStatement insert = connection.prepareStatement(insertSql)) {
            int record = 0;
            for (Map.Entry<String, HashMap<Long, SimpleListProperty<Double>>> entry : arrayDoubles.entrySet()) {
                record++;
                String actionId = entry.getKey();
                final SimpleIntegerProperty index = new SimpleIntegerProperty(0);
                for (Map.Entry<Long, SimpleListProperty<Double>> it : entry.getValue().entrySet()) {
                    Long fieldId = it.getKey();
                    int innerRecord = 0;
                    int innerTotal = it.getValue().get().size();
                    for (Double value : it.getValue().get()) {
                        update.clearParameters();
                        update.setInt(1, index.get());
                        update.setDouble(2, value);
                        update.setLong(3, fieldId);
                        update.setString(4, actionId);
                        if (update.executeUpdate() == 0) {
                            insert.clearParameters();
                            insert.setInt(1, index.get());
                            insert.setDouble(2, value);
                            insert.setLong(3, fieldId);
                            insert.setString(4, actionId);
                            insert.executeUpdate();
                            index.set(index.get() + 1);
                            System.out.printf("Inserting array record [%s]. Double field [%s of %s]\n", record, innerRecord, innerTotal);
                        } else {
                            System.out.printf("Updating array record [%s]. Double field [%s of %s]\n", record, innerRecord, innerTotal);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    private static void saveArrayLongs(final JdsDatabase jdsDatabase, final HashMap<String, HashMap<Long, SimpleListProperty<Long>>> arrayLongs) {
        String updateSql = "UPDATE JdsStoreDoubleArray SET EnumIndex =?, Value = ? WHERE FieldId = ? AND ActionId = ?";
        String insertSql = "INSERT INTO JdsStoreDoubleArray (FieldId,ActionId,EnumIndex,Value) VALUES (?,?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement update = connection.prepareStatement(updateSql);
             PreparedStatement insert = connection.prepareStatement(insertSql)) {
            int record = 0;
            for (Map.Entry<String, HashMap<Long, SimpleListProperty<Long>>> entry : arrayLongs.entrySet()) {
                record++;
                String actionId = entry.getKey();
                final SimpleIntegerProperty index = new SimpleIntegerProperty(0);
                for (Map.Entry<Long, SimpleListProperty<Long>> it : entry.getValue().entrySet()) {
                    Long fieldId = it.getKey();
                    int innerRecord = 0;
                    int innerTotal = it.getValue().get().size();
                    for (Long value : it.getValue().get()) {
                        update.clearParameters();
                        update.setInt(1, index.get());
                        update.setLong(2, value);
                        update.setLong(3, fieldId);
                        update.setString(4, actionId);
                        if (update.executeUpdate() == 0) {
                            insert.clearParameters();
                            insert.setInt(1, index.get());
                            insert.setLong(2, value);
                            insert.setLong(3, fieldId);
                            insert.setString(4, actionId);
                            insert.executeUpdate();
                            index.set(index.get() + 1);
                            System.out.printf("Inserting array record [%s]. Long field [%s of %s]\n", record, innerRecord, innerTotal);
                        } else {
                            System.out.printf("Updating array record [%s]. Long field [%s of %s]\n", record, innerRecord, innerTotal);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    private static void saveArrayStrings(final JdsDatabase jdsDatabase, final HashMap<String, HashMap<Long, SimpleListProperty<String>>> arrayStrings) {
        String updateSql = "UPDATE JdsStoreTextArray SET EnumIndex =?, Value = ? WHERE FieldId = ? AND ActionId = ?";
        String insertSql = "INSERT INTO JdsStoreTextArray (FieldId,ActionId,EnumIndex,Value) VALUES (?,?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement update = connection.prepareStatement(updateSql);
             PreparedStatement insert = connection.prepareStatement(insertSql)) {
            int record = 0;
            for (Map.Entry<String, HashMap<Long, SimpleListProperty<String>>> entry : arrayStrings.entrySet()) {
                record++;
                String actionId = entry.getKey();
                final SimpleIntegerProperty index = new SimpleIntegerProperty(0);
                for (Map.Entry<Long, SimpleListProperty<String>> it : entry.getValue().entrySet()) {
                    Long fieldId = it.getKey();
                    int innerRecord = 0;
                    int innerTotal = it.getValue().get().size();
                    for (String value : it.getValue().get()) {
                        update.clearParameters();
                        update.setInt(1, index.get());
                        update.setString(2, value);
                        update.setLong(3, fieldId);
                        update.setString(4, actionId);
                        if (update.executeUpdate() == 0) {
                            insert.clearParameters();
                            insert.setInt(1, index.get());
                            insert.setString(2, value);
                            insert.setLong(3, fieldId);
                            insert.setString(4, actionId);
                            insert.executeUpdate();
                            index.set(index.get() + 1);
                            System.out.printf("Inserting array record [%s]. String field [%s of %s]\n", record, innerRecord, innerTotal);
                        } else {
                            System.out.printf("Updating array record [%s]. String field [%s of %s]\n", record, innerRecord, innerTotal);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    private static void saveArrayObjects(final JdsDatabase jdsDatabase, final HashMap<String, HashMap<Long, SimpleListProperty<? extends JdsEntity>>> arraysObjects) {
        Collection<Jds_StoreEntitySubclass> bindings = new ArrayList<>();
        Collection<JdsEntity> jdsEntities = new ArrayList<>();
        for (Map.Entry<String, HashMap<Long, SimpleListProperty<? extends JdsEntity>>> serviceCodeEntities : arraysObjects.entrySet()) {
            String upperActionId = serviceCodeEntities.getKey();
            for (Map.Entry<Long, SimpleListProperty<? extends JdsEntity>> serviceCodeEntity : serviceCodeEntities.getValue().entrySet()) {
                long serviceCode = serviceCodeEntity.getKey();
                serviceCodeEntity.getValue().filtered(jdsEntity -> jdsEntity != null).parallelStream().forEach(jdsEntity -> {
                    jdsEntities.add(jdsEntity);
                    Jds_StoreEntitySubclass binding = new Jds_StoreEntitySubclass();
                    binding.setActionId(upperActionId);
                    binding.setSubActionId(jdsEntity.getActionId());
                    binding.setEntityId(serviceCode);
                    bindings.add(binding);
                });
            }
        }
        int record = 0;
        String selectSql = "SELECT SubActionId FROM JdsStoreEntitySubclass WHERE ActionId = ? AND SubActionId = ? AND EntityId = ?";
        String insertSql = "INSERT INTO JdsStoreEntitySubclass (ActionId,SubActionId,EntityId) VALUES (?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement select = connection.prepareStatement(selectSql);
             PreparedStatement insert = connection.prepareStatement(insertSql)) {
            for (Jds_StoreEntitySubclass bind : bindings) {
                select.clearParameters();
                select.setString(1, bind.getActionId());
                select.setString(2, bind.getSubActionId());
                select.setLong(3, bind.getEntityId());
                ResultSet resultSet = select.executeQuery();
                if (!resultSet.next()) {
                    //only proceed if binding doesn't already exist, much faster than comparing count result
                    record++;
                    insert.clearParameters();
                    insert.setString(1, bind.getActionId());
                    insert.setString(2, bind.getSubActionId());
                    insert.setLong(3, bind.getEntityId());
                    insert.executeUpdate();
                    System.out.printf("Binding object array. [%s]", record);
                    System.out.println();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
        if (jdsEntities.size() == 0) return;
        save(jdsDatabase, -1, jdsEntities);
    }
}
