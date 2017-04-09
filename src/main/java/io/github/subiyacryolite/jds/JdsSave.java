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

import javafx.beans.property.*;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.util.*;

/**
 * Created by ifunga on 12/02/2017.
 */
public class JdsSave {

    /**
     * @param database
     * @param batchSize
     * @param entities
     */
    public static void save(final JdsDatabase database, final int batchSize, final Collection<? extends JdsEntity> entities) {
        JdsSaveContainer saveContainer = new JdsSaveContainer();
        List<Collection<JdsEntity>> batchEntities = new ArrayList<>();
        setupBatches(batchSize, entities, saveContainer, batchEntities);
        //process batches
        int step = 0;
        int stepsRequired = batchEntities.size() + 1;
        for (Collection<JdsEntity> current : batchEntities) {
            saveInner(database, current, saveContainer, step);
            step++;
            if (database.printOutput())
                System.out.printf("Processed batch [%s of %s]\n", step, stepsRequired);
        }
    }

    /**
     * @param batchSize
     * @param entities
     * @param container
     * @param batchEntities
     */
    private static void setupBatches(int batchSize, Collection<? extends JdsEntity> entities, final JdsSaveContainer container, List<Collection<JdsEntity>> batchEntities) {
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
                    createBatchCollection(container, batchEntities);
                }
                batchEntities.get(currentBatch).add(jdsEntity);
                iteration++;
            }
        } else {
            //single large batch, good luck
            createBatchCollection(container, batchEntities);
            for (JdsEntity jdsEntity : entities) {
                batchEntities.get(0).add(jdsEntity);
            }
        }
    }

    /**
     * @param saveContainer
     * @param batchEntities
     */
    private static void createBatchCollection(final JdsSaveContainer saveContainer, final List<Collection<JdsEntity>> batchEntities) {
        batchEntities.add(new ArrayList<>());
        saveContainer.overviews.add(new HashSet<>());
        //primitives
        saveContainer.localDateTimes.add(new HashMap<>());
        saveContainer.zonedDateTimes.add(new HashMap<>());
        saveContainer.localTimes.add(new HashMap<>());
        saveContainer.localDates.add(new HashMap<>());
        saveContainer.strings.add(new HashMap<>());
        saveContainer.booleans.add(new HashMap<>());
        saveContainer.floats.add(new HashMap<>());
        saveContainer.doubles.add(new HashMap<>());
        saveContainer.longs.add(new HashMap<>());
        saveContainer.integers.add(new HashMap<>());
        //arrays
        saveContainer.stringArrays.add(new HashMap<>());
        saveContainer.dateTimeArrays.add(new HashMap<>());
        saveContainer.floatArrays.add(new HashMap<>());
        saveContainer.doubleArrays.add(new HashMap<>());
        saveContainer.longArrays.add(new HashMap<>());
        saveContainer.integerArrays.add(new HashMap<>());
        //enums
        saveContainer.enums.add(new HashMap<>());
        //objects
        saveContainer.objects.add(new HashMap<>());
        //object arrays
        saveContainer.objectArrays.add(new HashMap<>());
    }

    /**
     * @param database
     * @param entities
     * @param saveContainer
     * @param step
     */
    private static void saveInner(final JdsDatabase database, final Collection<? extends JdsEntity> entities, final JdsSaveContainer saveContainer, final int step) {
        //fire
        for (final JdsEntity entity : entities) {
            if (entity == null) continue;
            mapEntity(database, entity);
            //update the modified date to time of commit
            entity.setDateModified(LocalDateTime.now());
            saveContainer.overviews.get(step).add(entity.getOverview());
            //assign properties
            saveContainer.booleans.get(step).put(entity.getEntityGuid(), entity.booleanProperties);
            saveContainer.localDateTimes.get(step).put(entity.getEntityGuid(), entity.localDateTimeProperties);
            saveContainer.zonedDateTimes.get(step).put(entity.getEntityGuid(), entity.zonedDateTimeProperties);
            saveContainer.localTimes.get(step).put(entity.getEntityGuid(), entity.localTimeProperties);
            saveContainer.localDates.get(step).put(entity.getEntityGuid(), entity.localDateProperties);
            saveContainer.strings.get(step).put(entity.getEntityGuid(), entity.stringProperties);
            saveContainer.floats.get(step).put(entity.getEntityGuid(), entity.floatProperties);
            saveContainer.doubles.get(step).put(entity.getEntityGuid(), entity.doubleProperties);
            saveContainer.longs.get(step).put(entity.getEntityGuid(), entity.longProperties);
            saveContainer.integers.get(step).put(entity.getEntityGuid(), entity.integerProperties);
            //assign lists
            saveContainer.stringArrays.get(step).put(entity.getEntityGuid(), entity.stringArrayProperties);
            saveContainer.dateTimeArrays.get(step).put(entity.getEntityGuid(), entity.dateTimeArrayProperties);
            saveContainer.floatArrays.get(step).put(entity.getEntityGuid(), entity.floatArrayProperties);
            saveContainer.doubleArrays.get(step).put(entity.getEntityGuid(), entity.doubleArrayProperties);
            saveContainer.longArrays.get(step).put(entity.getEntityGuid(), entity.longArrayProperties);
            saveContainer.integerArrays.get(step).put(entity.getEntityGuid(), entity.integerArrayProperties);
            //assign enums
            saveContainer.enums.get(step).put(entity.getEntityGuid(), entity.enumProperties);
            //assign objects
            saveContainer.objectArrays.get(step).put(entity.getEntityGuid(), entity.objectArrayProperties);
            saveContainer.objects.get(step).put(entity.getEntityGuid(), entity.objectProperties);
        }
        saveOverviews(database, saveContainer.overviews.get(step));
        //properties
        saveBooleans(database, saveContainer.booleans.get(step));
        saveZonedDateTimes(database, saveContainer.zonedDateTimes.get(step));
        saveStrings(database, saveContainer.strings.get(step));
        saveDates(database, saveContainer.localDateTimes.get(step));
        saveLongs(database, saveContainer.longs.get(step));
        saveDoubles(database, saveContainer.doubles.get(step));
        saveIntegers(database, saveContainer.integers.get(step));
        saveFloats(database, saveContainer.floats.get(step));
        //array properties [NOTE arrays have old entries deleted first, for cases where a user reduced the amount of entries in the collection]
        saveArrayDates(database, saveContainer.dateTimeArrays.get(step));
        saveArrayStrings(database, saveContainer.stringArrays.get(step));
        saveArrayLongs(database, saveContainer.longArrays.get(step));
        saveArrayDoubles(database, saveContainer.doubleArrays.get(step));
        saveArrayIntegers(database, saveContainer.integerArrays.get(step));
        saveArrayFloats(database, saveContainer.floatArrays.get(step));
        //enums
        saveEnums(database, saveContainer.enums.get(step));
        //objects and object arrays
        saveArrayObjects(database, saveContainer.objectArrays.get(step));
        bindAndSaveInnerObjects(database, saveContainer.objects.get(step));
    }

    /**
     * @param database
     * @param batchSize
     * @param entities
     */
    public static void save(final JdsDatabase database, final int batchSize, final JdsEntity... entities) {
        save(database, batchSize, Arrays.asList(entities));
    }

    /**
     * @param jdsDatabase
     * @param overviews
     */
    private static void saveOverviews(final JdsDatabase jdsDatabase, final HashSet<JdsEntityOverview> overviews) {
        int record = 0;
        int recordTotal = overviews.size();
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement upsert = jdsDatabase.supportsStatements() ? connection.prepareCall(jdsDatabase.saveOverview()) : connection.prepareStatement(jdsDatabase.saveOverview())) {
            connection.setAutoCommit(false);
            for (JdsEntityOverview overview : overviews) {
                record++;
                //EntityGuid,ParentEntityGuid,DateCreated,DateModified,EntityId
                upsert.setString(1, overview.getEntityGuid());
                upsert.setTimestamp(2, Timestamp.valueOf(overview.getDateCreated()));
                upsert.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                upsert.setLong(4, overview.getEntityCode());
                upsert.addBatch();
                if (jdsDatabase.printOutput())
                    System.out.printf("Saving Overview [%s of %s]\n", record, recordTotal);
            }
            upsert.executeBatch();
            connection.commit();
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
            jdsDatabase.mapClassFields(jdsEntity.getEntityCode(), jdsEntity.properties);
            jdsDatabase.mapClassEnums(jdsEntity.getEntityCode(), jdsEntity.allEnums);
            if (jdsDatabase.printOutput())
                System.out.printf("Mapped Entity [%s]\n", jdsEntity.getEntityName());
        }
    }

    /**
     * @param jdsDatabase
     * @param booleanProperties
     * @implNote Booleans are saved as integers behind the scenes
     */
    private static void saveBooleans(final JdsDatabase jdsDatabase, final Map<String, Map<Long, SimpleBooleanProperty>> booleanProperties) {
        int record = 0;
        String logSql = "INSERT INTO JdsStoreOldFieldValues(EntityGuid,FieldId,IntegerValue) VALUES(?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement upsert = jdsDatabase.supportsStatements() ? connection.prepareCall(jdsDatabase.saveInteger()) : connection.prepareStatement(jdsDatabase.saveInteger());
             PreparedStatement log = connection.prepareStatement(logSql)) {
            connection.setAutoCommit(false);
            for (Map.Entry<String, Map<Long, SimpleBooleanProperty>> entry : booleanProperties.entrySet()) {
                record++;
                int innerRecord = 0;
                int innerRecordSize = entry.getValue().size();
                if (innerRecordSize == 0) continue;
                String entityGuid = entry.getKey();
                for (Map.Entry<Long, SimpleBooleanProperty> recordEntry : entry.getValue().entrySet()) {
                    innerRecord++;
                    long fieldId = recordEntry.getKey();
                    int value = recordEntry.getValue().get() ? 1 : 0;
                    upsert.setString(1, entityGuid);
                    upsert.setLong(2, fieldId);
                    upsert.setInt(3, value);
                    upsert.addBatch();
                    if (jdsDatabase.printOutput())
                        System.out.printf("Updating record [%s]. Boolean field [%s of %s]\n", record, innerRecord, innerRecordSize);
                    if (!jdsDatabase.logEdits()) continue;
                    log.setString(1, entityGuid);
                    log.setLong(2, fieldId);
                    log.setInt(3, value);
                    log.addBatch();
                }
            }
            upsert.executeBatch();
            log.executeBatch();
            connection.commit();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param jdsDatabase
     * @param integerProperties
     */
    private static void saveIntegers(final JdsDatabase jdsDatabase, final Map<String, Map<Long, SimpleIntegerProperty>> integerProperties) {
        int record = 0;
        String logSql = "INSERT INTO JdsStoreOldFieldValues(EntityGuid,FieldId,IntegerValue) VALUES(?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement upsert = jdsDatabase.supportsStatements() ? connection.prepareCall(jdsDatabase.saveInteger()) : connection.prepareStatement(jdsDatabase.saveInteger());
             PreparedStatement log = connection.prepareStatement(logSql)) {
            connection.setAutoCommit(false);
            for (Map.Entry<String, Map<Long, SimpleIntegerProperty>> entry : integerProperties.entrySet()) {
                record++;
                int innerRecord = 0;
                int innerRecordSize = entry.getValue().size();
                if (innerRecordSize == 0) continue;
                String entityGuid = entry.getKey();
                for (Map.Entry<Long, SimpleIntegerProperty> recordEntry : entry.getValue().entrySet()) {
                    innerRecord++;
                    long fieldId = recordEntry.getKey();
                    int value = recordEntry.getValue().get();
                    upsert.setString(1, entityGuid);
                    upsert.setLong(2, fieldId);
                    upsert.setInt(3, value);
                    upsert.addBatch();
                    if (jdsDatabase.printOutput())
                        System.out.printf("Updating record [%s]. Integer field [%s of %s]\n", record, innerRecord, innerRecordSize);
                    if (!jdsDatabase.logEdits()) continue;
                    log.setString(1, entityGuid);
                    log.setLong(2, fieldId);
                    log.setInt(3, value);
                    log.addBatch();
                }
            }
            upsert.executeBatch();
            log.executeBatch();
            connection.commit();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param jdsDatabase
     * @param floatProperties
     */
    private static void saveFloats(final JdsDatabase jdsDatabase, final Map<String, Map<Long, SimpleFloatProperty>> floatProperties) {
        int record = 0;
        String logSql = "INSERT INTO JdsStoreOldFieldValues(EntityGuid,FieldId,FloatValue) VALUES(?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement upsert = jdsDatabase.supportsStatements() ? connection.prepareCall(jdsDatabase.saveFloat()) : connection.prepareStatement(jdsDatabase.saveFloat());
             PreparedStatement log = connection.prepareStatement(logSql)) {
            connection.setAutoCommit(false);
            for (Map.Entry<String, Map<Long, SimpleFloatProperty>> entry : floatProperties.entrySet()) {
                record++;
                int innerRecord = 0;
                int innerRecordSize = entry.getValue().size();
                if (innerRecordSize == 0) continue;
                String entityGuid = entry.getKey();
                for (Map.Entry<Long, SimpleFloatProperty> recordEntry : entry.getValue().entrySet()) {
                    innerRecord++;
                    long fieldId = recordEntry.getKey();
                    float value = recordEntry.getValue().get();
                    upsert.setString(1, entityGuid);
                    upsert.setLong(2, fieldId);
                    upsert.setFloat(3, value);
                    upsert.addBatch();
                    if (jdsDatabase.printOutput())
                        System.out.printf("Updating record [%s]. Float field [%s of %s]\n", record, innerRecord, innerRecordSize);
                    if (!jdsDatabase.logEdits()) continue;
                    log.setString(1, entityGuid);
                    log.setLong(2, fieldId);
                    log.setFloat(3, value);
                    log.addBatch();
                }
            }
            upsert.executeBatch();
            log.executeBatch();
            connection.commit();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param jdsDatabase
     * @param doubleProperties
     */
    private static void saveDoubles(final JdsDatabase jdsDatabase, final Map<String, Map<Long, SimpleDoubleProperty>> doubleProperties) {
        int record = 0;
        String logSql = "INSERT INTO JdsStoreOldFieldValues(EntityGuid,FieldId,DoubleValue) VALUES(?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement upsert = jdsDatabase.supportsStatements() ? connection.prepareCall(jdsDatabase.saveDouble()) : connection.prepareStatement(jdsDatabase.saveDouble());
             PreparedStatement log = connection.prepareStatement(logSql)) {
            connection.setAutoCommit(false);
            for (Map.Entry<String, Map<Long, SimpleDoubleProperty>> entry : doubleProperties.entrySet()) {
                record++;
                int innerRecord = 0;
                int innerRecordSize = entry.getValue().size();
                if (innerRecordSize == 0) continue;
                String entityGuid = entry.getKey();
                for (Map.Entry<Long, SimpleDoubleProperty> recordEntry : entry.getValue().entrySet()) {
                    innerRecord++;
                    long fieldId = recordEntry.getKey();
                    double value = recordEntry.getValue().get();
                    upsert.setString(1, entityGuid);
                    upsert.setLong(2, fieldId);
                    upsert.setDouble(3, value);
                    upsert.addBatch();
                    if (jdsDatabase.printOutput())
                        System.out.printf("Updating record [%s]. Double field [%s of %s]\n", record, innerRecord, innerRecordSize);
                    if (!jdsDatabase.logEdits()) continue;
                    log.setString(1, entityGuid);
                    log.setLong(2, fieldId);
                    log.setDouble(3, value);
                    log.addBatch();
                }
            }
            upsert.executeBatch();
            log.executeBatch();
            connection.commit();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param jdsDatabase
     * @param longProperties
     */
    private static void saveLongs(final JdsDatabase jdsDatabase, final Map<String, Map<Long, SimpleLongProperty>> longProperties) {
        int record = 0;
        String logSql = "INSERT INTO JdsStoreOldFieldValues(EntityGuid,FieldId,LongValue) VALUES(?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement upsert = jdsDatabase.supportsStatements() ? connection.prepareCall(jdsDatabase.saveLong()) : connection.prepareStatement(jdsDatabase.saveLong());
             PreparedStatement log = connection.prepareStatement(logSql)) {
            connection.setAutoCommit(false);
            for (Map.Entry<String, Map<Long, SimpleLongProperty>> entry : longProperties.entrySet()) {
                record++;
                int innerRecord = 0;
                int innerRecordSize = entry.getValue().size();
                if (innerRecordSize == 0) continue;
                String entityGuid = entry.getKey();
                for (Map.Entry<Long, SimpleLongProperty> recordEntry : entry.getValue().entrySet()) {
                    innerRecord++;
                    long fieldId = recordEntry.getKey();
                    long value = recordEntry.getValue().get();
                    upsert.setString(1, entityGuid);
                    upsert.setLong(2, fieldId);
                    upsert.setLong(3, value);
                    upsert.addBatch();
                    if (jdsDatabase.printOutput())
                        System.out.printf("Updating record [%s]. Long field [%s of %s]\n", record, innerRecord, innerRecordSize);
                    if (!jdsDatabase.logEdits()) continue;
                    log.setString(1, entityGuid);
                    log.setLong(2, fieldId);
                    log.setLong(3, value);
                    log.addBatch();
                }
            }
            upsert.executeBatch();
            log.executeBatch();
            connection.commit();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param jdsDatabase
     * @param stringProperties
     */
    private static void saveStrings(final JdsDatabase jdsDatabase, final Map<String, Map<Long, SimpleStringProperty>> stringProperties) {
        int record = 0;
        String logSql = "INSERT INTO JdsStoreOldFieldValues(EntityGuid,FieldId,TextValue) VALUES(?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement upsert = jdsDatabase.supportsStatements() ? connection.prepareCall(jdsDatabase.saveString()) : connection.prepareStatement(jdsDatabase.saveString());
             PreparedStatement log = connection.prepareStatement(logSql)) {
            connection.setAutoCommit(false);
            for (Map.Entry<String, Map<Long, SimpleStringProperty>> entry : stringProperties.entrySet()) {
                record++;
                int innerRecord = 0;
                int innerRecordSize = entry.getValue().size();
                if (innerRecordSize == 0) continue;
                String entityGuid = entry.getKey();
                for (Map.Entry<Long, SimpleStringProperty> recordEntry : entry.getValue().entrySet()) {
                    innerRecord++;
                    long fieldId = recordEntry.getKey();
                    String value = recordEntry.getValue().get();
                    upsert.setString(1, entityGuid);
                    upsert.setLong(2, fieldId);
                    upsert.setString(3, value);
                    upsert.addBatch();
                    if (jdsDatabase.printOutput())
                        System.out.printf("Updating record [%s]. Text field [%s of %s]\n", record, innerRecord, innerRecordSize);
                    if (!jdsDatabase.logEdits()) continue;
                    log.setString(1, entityGuid);
                    log.setLong(2, fieldId);
                    log.setString(3, value);
                    log.addBatch();
                }
            }
            upsert.executeBatch();
            log.executeBatch();
            connection.commit();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param jdsDatabase
     * @param dateProperties
     */
    private static void saveDates(final JdsDatabase jdsDatabase, final Map<String, Map<Long, SimpleObjectProperty<Temporal>>> dateProperties) {
        int record = 0;
        String logSql = "INSERT INTO JdsStoreOldFieldValues(EntityGuid,FieldId,DateTimeValue) VALUES(?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement upsert = jdsDatabase.supportsStatements() ? connection.prepareCall(jdsDatabase.saveDateTime()) : connection.prepareStatement(jdsDatabase.saveDateTime());
             PreparedStatement log = connection.prepareStatement(logSql)) {
            connection.setAutoCommit(false);
            for (Map.Entry<String, Map<Long, SimpleObjectProperty<Temporal>>> entry : dateProperties.entrySet()) {
                record++;
                int innerRecord = 0;
                int innerRecordSize = entry.getValue().size();
                if (innerRecordSize == 0) continue;
                String entityGuid = entry.getKey();
                for (Map.Entry<Long, SimpleObjectProperty<Temporal>> recordEntry : entry.getValue().entrySet()) {
                    innerRecord++;
                    long fieldId = recordEntry.getKey();
                    Temporal temporal = recordEntry.getValue().get();
                    long longValue = temporal.getLong(ChronoField.INSTANT_SECONDS);
                    LocalDateTime localDateTime = Instant.ofEpochMilli(longValue).atZone(ZoneId.systemDefault()).toLocalDateTime();
                    upsert.setString(1, entityGuid);
                    upsert.setLong(2, fieldId);
                    upsert.setTimestamp(3, Timestamp.valueOf(localDateTime));
                    upsert.addBatch();
                    if (jdsDatabase.printOutput())
                        System.out.printf("Updating record [%s]. DateTime field [%s of %s]\n", record, innerRecord, innerRecordSize);
                    if (!jdsDatabase.logEdits()) continue;
                    log.setString(1, entityGuid);
                    log.setLong(2, fieldId);
                    log.setTimestamp(3, Timestamp.valueOf(localDateTime));
                    log.addBatch();
                }
            }
            upsert.executeBatch();
            log.executeBatch();
            connection.commit();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param jdsDatabase
     * @param zonedDateProperties
     */
    private static void saveZonedDateTimes(final JdsDatabase jdsDatabase, final Map<String, Map<Long, SimpleObjectProperty<Temporal>>> zonedDateProperties) {
        int record = 0;
        String logSql = "INSERT INTO JdsStoreOldFieldValues(EntityGuid,FieldId,LongValue) VALUES(?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement upsert = jdsDatabase.supportsStatements() ? connection.prepareCall(jdsDatabase.saveZonedDateTime()) : connection.prepareStatement(jdsDatabase.saveZonedDateTime());
             PreparedStatement log = connection.prepareStatement(logSql)) {
            connection.setAutoCommit(false);
            for (Map.Entry<String, Map<Long, SimpleObjectProperty<Temporal>>> entry : zonedDateProperties.entrySet()) {
                record++;
                int innerRecord = 0;
                int innerRecordSize = entry.getValue().size();
                if (innerRecordSize == 0) continue;
                String entityGuid = entry.getKey();
                for (Map.Entry<Long, SimpleObjectProperty<Temporal>> recordEntry : entry.getValue().entrySet()) {
                    innerRecord++;
                    long fieldId = recordEntry.getKey();
                    Temporal value = recordEntry.getValue().get();
                    upsert.setString(1, entityGuid);
                    upsert.setLong(2, fieldId);
                    upsert.setLong(3, value.getLong(ChronoField.INSTANT_SECONDS));
                    upsert.addBatch();
                    if (jdsDatabase.printOutput())
                        System.out.printf("Updating record [%s]. ZonedDateTime field [%s of %s]\n", record, innerRecord, innerRecordSize);
                    if (!jdsDatabase.logEdits()) continue;
                    log.setString(1, entityGuid);
                    log.setLong(2, fieldId);
                    log.setLong(3, value.getLong(ChronoField.INSTANT_SECONDS));
                    log.addBatch();
                }
            }
            upsert.executeBatch();
            log.executeBatch();
            connection.commit();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * Save all dates in one go
     *
     * @param dateTimeArrayProperties
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5]to[3,4]
     */
    private static void saveArrayDates(final JdsDatabase jdsDatabase, final Map<String, Map<Long, SimpleListProperty<LocalDateTime>>> dateTimeArrayProperties) {
        String logSql = "INSERT INTO JdsStoreOldFieldValues(EntityGuid,FieldId,DateTimeValue,Sequence) VALUES(?,?,?,?)";
        String deleteSql = "DELETE FROM JdsStoreDateTimeArray WHERE FieldId = ? AND EntityGuid = ?";
        String insertSql = "INSERT INTO JdsStoreDateTimeArray (Sequence,Value,FieldId,EntityGuid) VALUES (?,?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement log = connection.prepareStatement(logSql);
             PreparedStatement delete = connection.prepareStatement(deleteSql);
             PreparedStatement insert = connection.prepareStatement(insertSql)) {
            connection.setAutoCommit(false);
            int record = 0;
            for (Map.Entry<String, Map<Long, SimpleListProperty<LocalDateTime>>> entry : dateTimeArrayProperties.entrySet()) {
                record++;
                String entityGuid = entry.getKey();
                final SimpleIntegerProperty index = new SimpleIntegerProperty(0);
                for (Map.Entry<Long, SimpleListProperty<LocalDateTime>> it : entry.getValue().entrySet()) {
                    Long fieldId = it.getKey();
                    int innerRecord = 0;
                    int innerTotal = it.getValue().get().size();
                    for (LocalDateTime value : it.getValue().get()) {
                        if (jdsDatabase.logEdits()) {
                            log.setString(1, entityGuid);
                            log.setLong(2, fieldId);
                            log.setTimestamp(3, Timestamp.valueOf(value));
                            log.setInt(4, index.get());
                            log.addBatch();
                        }
                        delete.setLong(1, fieldId);
                        delete.setString(2, entityGuid);
                        delete.addBatch();
                        //insert
                        insert.setInt(1, index.get());
                        insert.setTimestamp(2, Timestamp.valueOf(value));
                        insert.setLong(3, fieldId);
                        insert.setString(4, entityGuid);
                        insert.addBatch();
                        index.set(index.get() + 1);
                        if (jdsDatabase.printOutput())
                            System.out.printf("Inserting array record [%s]. DateTime field [%s of %s]\n", record, innerRecord, innerTotal);
                    }
                }
            }
            log.executeBatch();
            delete.executeBatch();
            insert.executeBatch();
            connection.commit();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param jdsDatabase
     * @param floatArrayProperties
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5]to[3,4]
     */
    private static void saveArrayFloats(final JdsDatabase jdsDatabase, final Map<String, Map<Long, SimpleListProperty<Float>>> floatArrayProperties) {
        String logSql = "INSERT INTO JdsStoreOldFieldValues(EntityGuid,FieldId,FloatValue,Sequence) VALUES(?,?,?,?)";
        String deleteSql = "DELETE FROM JdsStoreFloatArray WHERE FieldId = ? AND EntityGuid = ?";
        String insertSql = "INSERT INTO JdsStoreFloatArray (FieldId,EntityGuid,Value,Sequence) VALUES (?,?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement log = connection.prepareStatement(logSql);
             PreparedStatement delete = connection.prepareStatement(deleteSql);
             PreparedStatement insert = connection.prepareStatement(insertSql)) {
            connection.setAutoCommit(false);
            int record = 0;
            for (Map.Entry<String, Map<Long, SimpleListProperty<Float>>> entry : floatArrayProperties.entrySet()) {
                record++;
                String entityGuid = entry.getKey();
                final SimpleIntegerProperty index = new SimpleIntegerProperty(0);
                for (Map.Entry<Long, SimpleListProperty<Float>> it : entry.getValue().entrySet()) {
                    Long fieldId = it.getKey();
                    int innerRecord = 0;
                    int innerTotal = it.getValue().get().size();
                    for (Float value : it.getValue().get()) {
                        if (jdsDatabase.logEdits()) {
                            log.setString(1, entityGuid);
                            log.setLong(2, fieldId);
                            log.setFloat(3, value);
                            log.setInt(4, index.get());
                            log.addBatch();
                        }
                        //delete
                        delete.setLong(1, fieldId);
                        delete.setString(2, entityGuid);
                        delete.addBatch();
                        //insert
                        insert.setInt(1, index.get());
                        insert.setFloat(2, value);
                        insert.setLong(3, fieldId);
                        insert.setString(4, entityGuid);
                        insert.addBatch();
                        index.set(index.get() + 1);
                        if (jdsDatabase.printOutput())
                            System.out.printf("Inserting array record [%s]. Float field [%s of %s]\n", record, innerRecord, innerTotal);

                    }
                }
            }
            log.executeBatch();
            delete.executeBatch();
            insert.executeBatch();
            connection.commit();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param jdsDatabase
     * @param integerArrayProperties
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5] to [3,4]
     */
    private static void saveArrayIntegers(final JdsDatabase jdsDatabase, final Map<String, Map<Long, SimpleListProperty<Integer>>> integerArrayProperties) {
        String logSql = "INSERT INTO JdsStoreOldFieldValues(EntityGuid,FieldId,IntegerValue,Sequence) VALUES(?,?,?,?)";
        String deleteSql = "DELETE FROM JdsStoreIntegerArray WHERE FieldId = ? AND EntityGuid = ?";
        String insertSql = "INSERT INTO JdsStoreIntegerArray (FieldId,EntityGuid,Sequence,Value) VALUES (?,?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement log = connection.prepareStatement(logSql);
             PreparedStatement delete = connection.prepareStatement(deleteSql);
             PreparedStatement insert = connection.prepareStatement(insertSql)) {
            connection.setAutoCommit(false);
            int record = 0;
            for (Map.Entry<String, Map<Long, SimpleListProperty<Integer>>> entry : integerArrayProperties.entrySet()) {
                record++;
                String entityGuid = entry.getKey();
                final SimpleIntegerProperty index = new SimpleIntegerProperty(0);
                for (Map.Entry<Long, SimpleListProperty<Integer>> it : entry.getValue().entrySet()) {
                    Long fieldId = it.getKey();
                    int innerRecord = 0;
                    int innerTotal = it.getValue().get().size();
                    for (Integer value : it.getValue().get()) {
                        if (jdsDatabase.logEdits()) {
                            log.setString(1, entityGuid);
                            log.setLong(2, fieldId);
                            log.setInt(3, value);
                            log.setInt(4, index.get());
                            log.addBatch();
                        }
                        //delete
                        delete.setLong(1, fieldId);
                        delete.setString(2, entityGuid);
                        delete.addBatch();
                        //insert
                        insert.setInt(1, index.get());
                        insert.setInt(2, value);
                        insert.setLong(3, fieldId);
                        insert.setString(4, entityGuid);
                        insert.addBatch();
                        index.set(index.get() + 1);
                        if (jdsDatabase.printOutput())
                            System.out.printf("Inserting array record [%s]. Integer field [%s of %s]\n", record, innerRecord, innerTotal);
                    }
                }
            }
            log.executeBatch();
            delete.executeBatch();
            insert.executeBatch();
            connection.commit();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param jdsDatabase
     * @param doubleArrayProperties
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5]to[3,4]
     */
    private static void saveArrayDoubles(final JdsDatabase jdsDatabase, final Map<String, Map<Long, SimpleListProperty<Double>>> doubleArrayProperties) {
        String logSql = "INSERT INTO JdsStoreOldFieldValues(EntityGuid,FieldId,DoubleValue,Sequence) VALUES(?,?,?,?)";
        String deleteSql = "DELETE FROM JdsStoreDoubleArray WHERE FieldId = ? AND EntityGuid = ?";
        String insertSql = "INSERT INTO JdsStoreDoubleArray (FieldId,EntityGuid,Sequence,Value) VALUES (?,?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement log = connection.prepareStatement(logSql);
             PreparedStatement delete = connection.prepareStatement(deleteSql);
             PreparedStatement insert = connection.prepareStatement(insertSql)) {
            connection.setAutoCommit(false);
            int record = 0;
            for (Map.Entry<String, Map<Long, SimpleListProperty<Double>>> entry : doubleArrayProperties.entrySet()) {
                record++;
                String entityGuid = entry.getKey();
                final SimpleIntegerProperty index = new SimpleIntegerProperty(0);
                for (Map.Entry<Long, SimpleListProperty<Double>> it : entry.getValue().entrySet()) {
                    Long fieldId = it.getKey();
                    int innerRecord = 0;
                    int innerTotal = it.getValue().get().size();
                    for (Double value : it.getValue().get()) {
                        if (jdsDatabase.logEdits()) {
                            log.setString(1, entityGuid);
                            log.setLong(2, fieldId);
                            log.setDouble(3, value);
                            log.setInt(4, index.get());
                            log.addBatch();
                        }
                        //delete
                        delete.setLong(1, fieldId);
                        delete.setString(2, entityGuid);
                        delete.addBatch();
                        //insert
                        insert.setInt(1, index.get());
                        insert.setDouble(2, value);
                        insert.setLong(3, fieldId);
                        insert.setString(4, entityGuid);
                        insert.addBatch();
                        index.set(index.get() + 1);
                        if (jdsDatabase.printOutput())
                            System.out.printf("Inserting array record [%s]. Double field [%s of %s]\n", record, innerRecord, innerTotal);
                    }
                }
            }
            log.executeBatch();
            delete.executeBatch();
            insert.executeBatch();
            connection.commit();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param jdsDatabase
     * @param longArrayProperties
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5]to[3,4]
     */
    private static void saveArrayLongs(final JdsDatabase jdsDatabase, final Map<String, Map<Long, SimpleListProperty<Long>>> longArrayProperties) {
        String logSql = "INSERT INTO JdsStoreOldFieldValues(EntityGuid,FieldId,LongValue,Sequence) VALUES(?,?,?,?)";
        String deleteSql = "DELETE FROM JdsStoreDoubleArray WHERE FieldId = ? AND EntityGuid = ?";
        String insertSql = "INSERT INTO JdsStoreDoubleArray (FieldId,EntityGuid,Sequence,Value) VALUES (?,?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement log = connection.prepareStatement(logSql);
             PreparedStatement delete = connection.prepareStatement(deleteSql);
             PreparedStatement insert = connection.prepareStatement(insertSql)) {
            connection.setAutoCommit(false);
            int record = 0;
            for (Map.Entry<String, Map<Long, SimpleListProperty<Long>>> entry : longArrayProperties.entrySet()) {
                record++;
                String entityGuid = entry.getKey();
                final SimpleIntegerProperty index = new SimpleIntegerProperty(0);
                for (Map.Entry<Long, SimpleListProperty<Long>> it : entry.getValue().entrySet()) {
                    Long fieldId = it.getKey();
                    int innerRecord = 0;
                    int innerTotal = it.getValue().get().size();
                    for (Long value : it.getValue().get()) {
                        if (jdsDatabase.logEdits()) {
                            log.setString(1, entityGuid);
                            log.setLong(2, fieldId);
                            log.setLong(3, value);
                            log.setInt(4, index.get());
                            log.addBatch();
                        }
                        //delete
                        delete.setLong(1, fieldId);
                        delete.setString(2, entityGuid);
                        delete.addBatch();
                        //insert
                        insert.setInt(1, index.get());
                        insert.setLong(2, value);
                        insert.setLong(3, fieldId);
                        insert.setString(4, entityGuid);
                        insert.addBatch();
                        index.set(index.get() + 1);
                        if (jdsDatabase.printOutput())
                            System.out.printf("Inserting array record [%s]. Long field [%s of %s]\n", record, innerRecord, innerTotal);
                    }
                }
            }
            log.executeBatch();
            delete.executeBatch();
            insert.executeBatch();
            connection.commit();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param jdsDatabase
     * @param stringArrayProperties
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5]to[3,4]
     */
    private static void saveArrayStrings(final JdsDatabase jdsDatabase, final Map<String, Map<Long, SimpleListProperty<String>>> stringArrayProperties) {
        String logSql = "INSERT INTO JdsStoreOldFieldValues(EntityGuid,FieldId,TextValue,Sequence) VALUES(?,?,?,?)";
        String deleteSql = "DELETE FROM JdsStoreTextArray WHERE FieldId = ? AND EntityGuid = ?";
        String insertSql = "INSERT INTO JdsStoreTextArray (FieldId,EntityGuid,Sequence,Value) VALUES (?,?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement log = connection.prepareStatement(logSql);
             PreparedStatement delete = connection.prepareStatement(deleteSql);
             PreparedStatement insert = connection.prepareStatement(insertSql)) {
            connection.setAutoCommit(false);
            int record = 0;
            for (Map.Entry<String, Map<Long, SimpleListProperty<String>>> entry : stringArrayProperties.entrySet()) {
                record++;
                String entityGuid = entry.getKey();
                final SimpleIntegerProperty index = new SimpleIntegerProperty(0);
                for (Map.Entry<Long, SimpleListProperty<String>> it : entry.getValue().entrySet()) {
                    Long fieldId = it.getKey();
                    int innerRecord = 0;
                    int innerTotal = it.getValue().get().size();
                    for (String value : it.getValue().get()) {
                        if (jdsDatabase.logEdits()) {
                            log.setString(1, entityGuid);
                            log.setLong(2, fieldId);
                            log.setString(3, value);
                            log.setInt(4, index.get());
                            log.addBatch();
                        }
                        //delete
                        delete.setLong(1, fieldId);
                        delete.setString(2, entityGuid);
                        delete.addBatch();
                        //insert
                        insert.setInt(1, index.get());
                        insert.setString(2, value);
                        insert.setLong(3, fieldId);
                        insert.setString(4, entityGuid);
                        insert.addBatch();
                        index.set(index.get() + 1);
                        if (jdsDatabase.printOutput())
                            System.out.printf("Inserting array record [%s]. String field [%s of %s]\n", record, innerRecord, innerTotal);
                    }
                }
            }
            log.executeBatch();
            delete.executeBatch();
            insert.executeBatch();
            connection.commit();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param jdsDatabase
     * @param enumStrings
     * @apiNote Enums are actually saved as index based integer arrays
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5]to[3,4]
     */
    private static void saveEnums(final JdsDatabase jdsDatabase, final Map<String, Map<JdsFieldEnum, SimpleListProperty<String>>> enumStrings) {
        int record = 0;
        int recordTotal = enumStrings.size();
        String logSql = "INSERT INTO JdsStoreOldFieldValues(EntityGuid,FieldId,IntegerValue,Sequence) VALUES(?,?,?,?)";
        String deleteSql = "DELETE FROM JdsStoreIntegerArray WHERE FieldId = ? AND EntityGuid = ?";
        String insertSql = "INSERT INTO JdsStoreIntegerArray (FieldId,EntityGuid,Sequence,Value) VALUES (?,?,?,?)";
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement log = connection.prepareStatement(logSql);
             PreparedStatement delete = connection.prepareStatement(deleteSql);
             PreparedStatement insert = connection.prepareStatement(insertSql)) {
            connection.setAutoCommit(false);
            for (Map.Entry<String, Map<JdsFieldEnum, SimpleListProperty<String>>> entry : enumStrings.entrySet()) {
                record++;
                String entityGuid = entry.getKey();
                for (Map.Entry<JdsFieldEnum, SimpleListProperty<String>> fieldEnums : entry.getValue().entrySet()) {
                    int sequence = 0;
                    JdsFieldEnum fieldId = fieldEnums.getKey();
                    ObservableList<String> textValues = fieldEnums.getValue().get();
                    if (textValues.size() == 0) continue;
                    for (String enumText : textValues) {
                        if (jdsDatabase.logEdits()) {
                            log.setString(1, entityGuid);
                            log.setLong(2, fieldId.getField().getId());
                            log.setInt(3, fieldId.getIndex(enumText));
                            log.setInt(4, sequence);
                            log.addBatch();
                        }
                        //delete
                        delete.setLong(1, fieldId.getField().getId());
                        delete.setString(2, entityGuid);
                        delete.addBatch();
                        //insert
                        insert.setLong(1, fieldId.getField().getId());
                        insert.setString(2, entityGuid);
                        insert.setInt(3, sequence);
                        insert.setInt(4, fieldId.getIndex(enumText));
                        insert.addBatch();
                        if (jdsDatabase.printOutput())
                            System.out.printf("Updating enum [%s]. Object field [%s of %s]\n", sequence, record, recordTotal);
                        sequence++;
                    }
                }
            }
            log.executeBatch();
            delete.executeBatch();
            insert.executeBatch();
            connection.commit();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param jdsDatabase
     * @param objectArrayProperties
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5]to[3,4]
     * @implNote For the love of Christ don't use parallel stream here
     */
    private static void saveArrayObjects(final JdsDatabase jdsDatabase, final Map<String, Map<Long, SimpleListProperty<? extends JdsEntity>>> objectArrayProperties) {
        if (objectArrayProperties.size() == 0) return;
        final Collection<JdsEntity> jdsEntities = new ArrayList<>();
        final Collection<JdsParentEntityBinding> parentEntityBindings = new ArrayList<>();
        final Collection<JdsParentChildBinding> parentChildBindings = new ArrayList<>();
        final IntegerProperty record = new SimpleIntegerProperty(0);
        final BooleanProperty changesMade = new SimpleBooleanProperty(false);
        for (Map.Entry<String, Map<Long, SimpleListProperty<? extends JdsEntity>>> serviceCodeEntities : objectArrayProperties.entrySet()) {
            String parentGuid = serviceCodeEntities.getKey();
            for (Map.Entry<Long, SimpleListProperty<? extends JdsEntity>> serviceCodeEntity : serviceCodeEntities.getValue().entrySet()) {
                record.set(0);
                changesMade.set(false);
                serviceCodeEntity.getValue().stream().filter(jdsEntity -> jdsEntity != null).forEach(jdsEntity -> {
                    if (!changesMade.get()) {
                        //only clear if changes are made. else you wipe out old bindings regardless
                        changesMade.set(true);
                        JdsParentEntityBinding parentEntityBinding = new JdsParentEntityBinding();
                        parentEntityBinding.parentGuid = parentGuid;
                        parentEntityBinding.EntityId = serviceCodeEntity.getKey();
                        parentEntityBindings.add(parentEntityBinding);

                    }
                    JdsParentChildBinding parentChildBinding = new JdsParentChildBinding();
                    parentChildBinding.parentGuid = parentGuid;
                    parentChildBinding.childGuid = jdsEntity.getEntityGuid();
                    parentChildBindings.add(parentChildBinding);
                    jdsEntities.add(jdsEntity);
                    record.set(record.get() + 1);
                    System.out.printf("Binding array object %s\n", record.get());
                });
            }
        }
        //save children first
        save(jdsDatabase, -1, jdsEntities);

        //bind children below
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement clearOldBindings = connection.prepareStatement("DELETE FROM JdsStoreEntityBinding WHERE ParentEntityGuid = ? AND ChildEntityId = ?");
             PreparedStatement writeNewBindings = connection.prepareStatement("INSERT INTO JdsStoreEntityBinding(ParentEntityGuid,ChildEntityGuid,ChildEntityId) Values(?,?,?)")) {
            connection.setAutoCommit(false);
            for (JdsParentEntityBinding parentEntityBinding : parentEntityBindings) {
                clearOldBindings.setString(1, parentEntityBinding.parentGuid);
                clearOldBindings.setLong(2, parentEntityBinding.EntityId);
                clearOldBindings.addBatch();
            }
            for (JdsEntity jdsEntity : jdsEntities) {
                writeNewBindings.setString(1, getParent(parentChildBindings, jdsEntity.getEntityGuid()));
                writeNewBindings.setString(2, jdsEntity.getEntityGuid());
                writeNewBindings.setLong(3, jdsEntity.getEntityCode());
                writeNewBindings.addBatch();
            }
            int[] res2 = clearOldBindings.executeBatch();
            int[] res3 = writeNewBindings.executeBatch();
            connection.commit();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param jdsDatabase
     * @param objectProperties
     * @implNote For the love of Christ don't use parallel stream here
     */
    private static void bindAndSaveInnerObjects(final JdsDatabase jdsDatabase, final Map<String, Map<Long, SimpleObjectProperty<? extends JdsEntity>>> objectProperties) {
        if (objectProperties.size() == 0) return;//prevent stack overflow :)
        final IntegerProperty record = new SimpleIntegerProperty(0);
        final BooleanProperty changesMade = new SimpleBooleanProperty(false);
        final Collection<JdsParentEntityBinding> parentEntityBindings = new ArrayList<>();
        final Collection<JdsParentChildBinding> parentChildBindings = new ArrayList<>();
        final Collection<JdsEntity> jdsEntities = new ArrayList<>();
        for (Map.Entry<String, Map<Long, SimpleObjectProperty<? extends JdsEntity>>> entry : objectProperties.entrySet()) {
            String parentGuid = entry.getKey();
            for (Map.Entry<Long, SimpleObjectProperty<? extends JdsEntity>> recordEntry : entry.getValue().entrySet()) {
                record.set(0);
                JdsEntity jdsEntity = recordEntry.getValue().get();
                changesMade.set(false);
                if (jdsEntity != null) {
                    if (!changesMade.get()) {
                        changesMade.set(true);
                        JdsParentEntityBinding parentEntityBinding = new JdsParentEntityBinding();
                        parentEntityBinding.parentGuid = parentGuid;
                        parentEntityBinding.EntityId = recordEntry.getKey();
                        parentEntityBindings.add(parentEntityBinding);
                    }
                    jdsEntities.add(jdsEntity);
                    JdsParentChildBinding parentChildBinding = new JdsParentChildBinding();
                    parentChildBinding.parentGuid = parentGuid;
                    parentChildBinding.childGuid = jdsEntity.getEntityGuid();
                    parentChildBindings.add(parentChildBinding);
                    record.set(record.get() + 1);
                    System.out.printf("Binding object %s\n", record.get());
                }
            }
        }
        //save children first
        save(jdsDatabase, -1, jdsEntities);

        //bind children below
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement clearOldBindings = connection.prepareStatement("DELETE FROM JdsStoreEntityBinding WHERE ParentEntityGuid = ? AND ChildEntityId = ?");
             PreparedStatement writeNewBindings = connection.prepareStatement("INSERT INTO JdsStoreEntityBinding(ParentEntityGuid,ChildEntityGuid,ChildEntityId) Values(?,?,?)")) {
            connection.setAutoCommit(false);
            for (JdsParentEntityBinding parentEntityBinding : parentEntityBindings) {
                clearOldBindings.setString(1, parentEntityBinding.parentGuid);
                clearOldBindings.setLong(2, parentEntityBinding.EntityId);
                clearOldBindings.addBatch();
            }
            for (JdsEntity jdsEntity : jdsEntities) {
                writeNewBindings.setString(1, getParent(parentChildBindings, jdsEntity.getEntityGuid()));
                writeNewBindings.setString(2, jdsEntity.getEntityGuid());
                writeNewBindings.setLong(3, jdsEntity.getEntityCode());
                writeNewBindings.addBatch();
            }
            int[] res2 = clearOldBindings.executeBatch();
            int[] res3 = writeNewBindings.executeBatch();
            connection.commit();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param jdsParentChildBindings
     * @param childGuid
     * @return
     */
    private static String getParent(final Collection<JdsParentChildBinding> jdsParentChildBindings, final String childGuid) {
        Optional<JdsParentChildBinding> any = jdsParentChildBindings.stream().filter(parentChildBinding -> parentChildBinding.childGuid.equals(childGuid)).findAny();
        return any.isPresent() ? any.get().parentGuid : "";
    }
}
