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
import io.github.subiyacryolite.jds.events.JdsSaveListener;
import io.github.subiyacryolite.jds.events.OnPostSaveEventArguments;
import io.github.subiyacryolite.jds.events.OnPreSaveEventArguments;
import javafx.beans.property.*;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * This class is responsible for persisting on or more {@link JdsEntity JdsEntities}
 */
public class JdsSave implements Callable<Boolean> {

    private final JdsDb jdsDb;
    private final int batchSize;
    private final Connection connection;
    private final Collection<? extends JdsEntity> entities;
    private final boolean recursiveInnerCall;
    private final OnPreSaveEventArguments onPreSaveEventArguments;
    private final OnPostSaveEventArguments onPostSaveEventArguments;

    /**
     * @param jdsDb
     * @param entities
     */
    public JdsSave(final JdsDb jdsDb, final Collection<? extends JdsEntity> entities) throws SQLException, ClassNotFoundException {
        this(jdsDb, jdsDb.getConnection(), 0, entities, false);
    }

    /**
     * @param jdsDb
     * @param batchSize
     * @param entities
     */
    public JdsSave(final JdsDb jdsDb, final int batchSize, final Collection<? extends JdsEntity> entities) throws SQLException, ClassNotFoundException {
        this(jdsDb, jdsDb.getConnection(), batchSize, entities, false);
    }

    private JdsSave(final JdsDb jdsDb, Connection connection, final int batchSize, final Collection<? extends JdsEntity> entities, boolean recursiveInnerCall) {
        this(jdsDb, connection, batchSize, entities, recursiveInnerCall, new OnPreSaveEventArguments(connection), new OnPostSaveEventArguments(connection));
    }

    private JdsSave(final JdsDb jdsDb, Connection connection, final int batchSize, final Collection<? extends JdsEntity> entities, boolean recursiveInnerCall, final OnPreSaveEventArguments onPreSaveEventArguments, final OnPostSaveEventArguments onPostSaveEventArguments) {
        this.jdsDb = jdsDb;
        this.batchSize = batchSize;
        this.entities = entities;
        this.connection = connection;
        this.recursiveInnerCall = recursiveInnerCall;
        this.onPreSaveEventArguments = onPreSaveEventArguments;
        this.onPostSaveEventArguments = onPostSaveEventArguments;
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public Boolean call() throws Exception {
        JdsSaveContainer saveContainer = new JdsSaveContainer();
        List<Collection<JdsEntity>> batchEntities = new ArrayList<>();
        setupBatches(batchSize, entities, saveContainer, batchEntities);
        int step = 0;
        int stepsRequired = batchEntities.size() + 1;
        for (Collection<JdsEntity> current : batchEntities) {
            saveInner(jdsDb, current, saveContainer, step);
            step++;
            if (jdsDb.isPrintingOutput())
                System.out.printf("Processed batch [%s of %s]\n", step, stepsRequired);
        }
        return true;
    }

    /**
     * @param batchSize
     * @param entities
     * @param container
     * @param batchEntities
     */
    private void setupBatches(int batchSize, Collection<? extends JdsEntity> entities, final JdsSaveContainer container, List<Collection<JdsEntity>> batchEntities) {
        //create batches
        int currentBatch = 0;
        //default bach is 0 or -1 which means one large chunk. Anything above is a single batch
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
    private void createBatchCollection(final JdsSaveContainer saveContainer, final List<Collection<JdsEntity>> batchEntities) {
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
        //blob
        saveContainer.blobs.add(new HashMap<>());
        //arrays
        saveContainer.stringArrays.add(new HashMap<>());
        saveContainer.dateTimeArrays.add(new HashMap<>());
        saveContainer.floatArrays.add(new HashMap<>());
        saveContainer.doubleArrays.add(new HashMap<>());
        saveContainer.longArrays.add(new HashMap<>());
        saveContainer.integerArrays.add(new HashMap<>());
        //enums
        saveContainer.enums.add(new HashMap<>());
        saveContainer.enumCollections.add(new HashMap<>());
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
    private void saveInner(final JdsDb database, final Collection<JdsEntity> entities, final JdsSaveContainer saveContainer, final int step) throws Exception {
        //fire
        int sequence = 0;
        for (final JdsEntity entity : entities) {
            if (entity == null) continue;
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
            //assign blobs
            saveContainer.blobs.get(step).put(entity.getEntityGuid(), entity.blobProperties);
            //assign lists
            saveContainer.stringArrays.get(step).put(entity.getEntityGuid(), entity.stringArrayProperties);
            saveContainer.dateTimeArrays.get(step).put(entity.getEntityGuid(), entity.dateTimeArrayProperties);
            saveContainer.floatArrays.get(step).put(entity.getEntityGuid(), entity.floatArrayProperties);
            saveContainer.doubleArrays.get(step).put(entity.getEntityGuid(), entity.doubleArrayProperties);
            saveContainer.longArrays.get(step).put(entity.getEntityGuid(), entity.longArrayProperties);
            saveContainer.integerArrays.get(step).put(entity.getEntityGuid(), entity.integerArrayProperties);
            //assign enums
            saveContainer.enums.get(step).put(entity.getEntityGuid(), entity.enumProperties);
            saveContainer.enumCollections.get(step).put(entity.getEntityGuid(), entity.enumCollectionProperties);
            //assign objects
            saveContainer.objectArrays.get(step).put(entity.getEntityGuid(), entity.objectArrayProperties);
            saveContainer.objects.get(step).put(entity.getEntityGuid(), entity.objectProperties);
            sequence++;
        }
        //share one connection for raw saves, helps with performance
        try {
            boolean writeToPrimaryDataTables = jdsDb.isWritingToPrimaryDataTables();
            //always save overviews
            saveOverviews(connection, saveContainer.overviews.get(step));
            //ensure that overviews are submitted before handing over to listeners

            for (final JdsEntity entity : entities) {
                if (entity instanceof JdsSaveListener) {
                    ((JdsSaveListener) entity).onPreSave(onPreSaveEventArguments);
                }
            }
            if (!recursiveInnerCall)
                onPreSaveEventArguments.executeBatches();

            //properties
            saveBooleans(writeToPrimaryDataTables, saveContainer.booleans.get(step));
            saveStrings(writeToPrimaryDataTables, saveContainer.strings.get(step));
            saveDatesAndDateTimes(writeToPrimaryDataTables, saveContainer.localDateTimes.get(step), saveContainer.localDates.get(step));
            saveZonedDateTimes(writeToPrimaryDataTables, saveContainer.zonedDateTimes.get(step));
            saveTimes(writeToPrimaryDataTables, saveContainer.localTimes.get(step));
            saveLongs(writeToPrimaryDataTables, saveContainer.longs.get(step));
            saveDoubles(writeToPrimaryDataTables, saveContainer.doubles.get(step));
            saveIntegers(writeToPrimaryDataTables, saveContainer.integers.get(step));
            saveFloats(writeToPrimaryDataTables, saveContainer.floats.get(step));
            //blobs
            saveBlobs(writeToPrimaryDataTables, saveContainer.blobs.get(step));
            //array properties [NOTE arrays have old entries deleted first, for cases where a user reduced the amount of entries in the collection]
            saveArrayDates(writeToPrimaryDataTables, saveContainer.dateTimeArrays.get(step));
            saveArrayStrings(writeToPrimaryDataTables, saveContainer.stringArrays.get(step));
            saveArrayLongs(writeToPrimaryDataTables, saveContainer.longArrays.get(step));
            saveArrayDoubles(writeToPrimaryDataTables, saveContainer.doubleArrays.get(step));
            saveArrayIntegers(writeToPrimaryDataTables, saveContainer.integerArrays.get(step));
            saveArrayFloats(writeToPrimaryDataTables, saveContainer.floatArrays.get(step));
            //enums
            saveEnums(writeToPrimaryDataTables, saveContainer.enums.get(step));
            saveEnumCollections(writeToPrimaryDataTables, saveContainer.enumCollections.get(step));
            //objects and object arrays
            //object entity overviews and entity bindings are ALWAYS persisted
            saveAndBindObjects(connection, saveContainer.objects.get(step));
            saveAndBindObjectArrays(connection, saveContainer.objectArrays.get(step));

            for (final JdsEntity entity : entities) {
                if (entity instanceof JdsSaveListener) {
                    ((JdsSaveListener) entity).onPostSave(onPostSaveEventArguments);
                }
            }
            if (!recursiveInnerCall)
                onPostSaveEventArguments.executeBatches();

        } catch (Exception ex) {
            throw ex;
        } finally {
            if (!recursiveInnerCall)
                connection.close();
        }
    }

    /**
     * @param overviews
     */
    private void saveOverviews(Connection connection, final HashSet<JdsEntityOverview> overviews) throws SQLException {
        int record = 0;
        int recordTotal = overviews.size();
        try (INamedStatement upsert = jdsDb.supportsStatements() ? new NamedCallableStatement(connection, jdsDb.saveOverview()) : new NamedPreparedStatement(connection, jdsDb.saveOverview());
             INamedStatement inheritance = jdsDb.supportsStatements() ? new NamedCallableStatement(connection, jdsDb.saveOverviewInheritance()) : new NamedPreparedStatement(connection, jdsDb.saveOverviewInheritance())) {
            for (JdsEntityOverview overview : overviews) {
                record++;
                //Entity Overview
                upsert.setString("entityGuid", overview.getEntityGuid());
                upsert.setTimestamp("dateCreated", Timestamp.valueOf(overview.getDateCreated()));
                upsert.setTimestamp("dateModified", Timestamp.valueOf(LocalDateTime.now())); //always update date modified!!!
                upsert.addBatch();
                //Entity Inheritance
                inheritance.setString("entityGuid", overview.getEntityGuid());
                inheritance.setLong("entityId", overview.getEntityId());
                inheritance.addBatch();
                if (jdsDb.isPrintingOutput())
                    System.out.printf("Saving Overview [%s of %s]\n", record, recordTotal);
            }
            upsert.executeBatch();
            inheritance.executeBatch();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param blobProperties
     */
    private void saveBlobs(boolean writeToPrimaryDataTables, final Map<String, Map<Long, SimpleBlobProperty>> blobProperties) {
        int record = 0;
        //log byte array as text???
        try {
            INamedStatement upsert = jdsDb.supportsStatements() ? onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveBlob()) : onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveBlob());
            INamedStatement log = jdsDb.isOracleDb() ? onPostSaveEventArguments.getOrAddNamedCall(jdsDb.getSaveOldBlobValues()) : onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.getSaveOldBlobValues());
            for (Map.Entry<String, Map<Long, SimpleBlobProperty>> entry : blobProperties.entrySet()) {
                record++;
                int innerRecord = 0;
                int innerRecordSize = entry.getValue().size();
                if (innerRecordSize == 0) continue;
                String entityGuid = entry.getKey();
                for (Map.Entry<Long, SimpleBlobProperty> recordEntry : entry.getValue().entrySet()) {
                    innerRecord++;
                    long fieldId = recordEntry.getKey();
                    if (writeToPrimaryDataTables) {
                        upsert.setString("entityGuid", entityGuid);
                        upsert.setLong("fieldId", fieldId);
                        upsert.setBytes("value", recordEntry.getValue().get());
                        upsert.addBatch();
                    }
                    if (jdsDb.isPrintingOutput())
                        System.out.printf("Updating record [%s]. Blob field [%s of %s]\n", record, innerRecord, innerRecordSize);
                    if (!jdsDb.isLoggingEdits()) continue;
                    log.setString("entityGuid", entityGuid);
                    log.setLong("fieldId", fieldId);
                    log.setBytes("value", recordEntry.getValue().get());
                    log.setInt("sequence", 0);
                    log.addBatch();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param booleanProperties
     * @implNote Booleans are saved as integers behind the scenes
     */
    private void saveBooleans(boolean writeToPrimaryDataTables, final Map<String, Map<Long, SimpleBooleanProperty>> booleanProperties) {
        int record = 0;
        try {
            INamedStatement upsert = jdsDb.supportsStatements() ? onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveInteger()) : onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveInteger());
            INamedStatement log = jdsDb.isOracleDb() ? onPostSaveEventArguments.getOrAddNamedCall(jdsDb.getSaveOldIntegerValues()) : onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.getSaveOldIntegerValues());
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
                    if (writeToPrimaryDataTables) {
                        upsert.setString("entityGuid", entityGuid);
                        upsert.setLong("fieldId", fieldId);
                        upsert.setInt("value", value);
                        upsert.addBatch();
                    }
                    if (jdsDb.isPrintingOutput())
                        System.out.printf("Updating record [%s]. Boolean field [%s of %s]\n", record, innerRecord, innerRecordSize);
                    if (!jdsDb.isLoggingEdits()) continue;
                    log.setString("entityGuid", entityGuid);
                    log.setLong("fieldId", fieldId);
                    log.setInt("value", value);
                    log.setInt("sequence", 0);
                    log.addBatch();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param integerProperties
     */
    private void saveIntegers(boolean writeToPrimaryDataTables, final Map<String, Map<Long, SimpleIntegerProperty>> integerProperties) {
        int record = 0;
        try {
            INamedStatement upsert = jdsDb.supportsStatements() ? onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveInteger()) : onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveInteger());
            INamedStatement log = jdsDb.isOracleDb() ? onPostSaveEventArguments.getOrAddNamedCall(jdsDb.getSaveOldIntegerValues()) : onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.getSaveOldIntegerValues());
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
                    if (writeToPrimaryDataTables) {
                        upsert.setString("entityGuid", entityGuid);
                        upsert.setLong("fieldId", fieldId);
                        upsert.setInt("value", value);
                        upsert.addBatch();
                    }
                    if (jdsDb.isPrintingOutput())
                        System.out.printf("Updating record [%s]. Integer field [%s of %s]\n", record, innerRecord, innerRecordSize);
                    if (!jdsDb.isLoggingEdits()) continue;
                    log.setString("entityGuid", entityGuid);
                    log.setLong("fieldId", fieldId);
                    log.setInt("value", value);
                    log.setInt("sequence", 0);
                    log.addBatch();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param floatProperties
     */
    private void saveFloats(boolean writeToPrimaryDataTables, final Map<String, Map<Long, SimpleFloatProperty>> floatProperties) {
        int record = 0;
        try {
            INamedStatement upsert = jdsDb.supportsStatements() ? onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveFloat()) : onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveFloat());
            INamedStatement log = jdsDb.isOracleDb() ? onPostSaveEventArguments.getOrAddNamedCall(jdsDb.getSaveOldFloatValues()) : onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.getSaveOldFloatValues());
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
                    if (writeToPrimaryDataTables) {
                        upsert.setString("entityGuid", entityGuid);
                        upsert.setLong("fieldId", fieldId);
                        upsert.setFloat("value", value);
                        upsert.addBatch();
                    }
                    if (jdsDb.isPrintingOutput())
                        System.out.printf("Updating record [%s]. Float field [%s of %s]\n", record, innerRecord, innerRecordSize);
                    if (!jdsDb.isLoggingEdits()) continue;
                    log.setString("entityGuid", entityGuid);
                    log.setLong("fieldId", fieldId);
                    log.setFloat("value", value);
                    log.setInt("sequence", 0);
                    log.addBatch();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param doubleProperties
     */
    private void saveDoubles(boolean writeToPrimaryDataTables, final Map<String, Map<Long, SimpleDoubleProperty>> doubleProperties) {
        int record = 0;
        try {
            INamedStatement upsert = jdsDb.supportsStatements() ? onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveDouble()) : onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveDouble());
            INamedStatement log = jdsDb.isOracleDb() ? onPostSaveEventArguments.getOrAddNamedCall(jdsDb.getSaveOldDoubleValues()) : onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.getSaveOldDoubleValues());
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
                    if (writeToPrimaryDataTables) {
                        upsert.setString("entityGuid", entityGuid);
                        upsert.setLong("fieldId", fieldId);
                        upsert.setDouble("value", value);
                        upsert.addBatch();
                    }
                    if (jdsDb.isPrintingOutput())
                        System.out.printf("Updating record [%s]. Double field [%s of %s]\n", record, innerRecord, innerRecordSize);
                    if (!jdsDb.isLoggingEdits()) continue;
                    log.setString("entityGuid", entityGuid);
                    log.setLong("fieldId", fieldId);
                    log.setDouble("value", value);
                    log.setInt("sequence", 0);
                    log.addBatch();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param longProperties
     */
    private void saveLongs(boolean writeToPrimaryDataTables, final Map<String, Map<Long, SimpleLongProperty>> longProperties) {
        int record = 0;
        try {
            INamedStatement upsert = jdsDb.supportsStatements() ? onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveLong()) : onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveLong());
            INamedStatement log = jdsDb.isOracleDb() ? onPostSaveEventArguments.getOrAddNamedCall(jdsDb.getSaveOldLongValues()) : onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.getSaveOldLongValues());
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
                    if (writeToPrimaryDataTables) {
                        upsert.setString("entityGuid", entityGuid);
                        upsert.setLong("fieldId", fieldId);
                        upsert.setLong("value", value);
                        upsert.addBatch();
                    }
                    if (jdsDb.isPrintingOutput())
                        System.out.printf("Updating record [%s]. Long field [%s of %s]\n", record, innerRecord, innerRecordSize);
                    if (!jdsDb.isLoggingEdits()) continue;
                    log.setString("entityGuid", entityGuid);
                    log.setLong("fieldId", fieldId);
                    log.setLong("value", value);
                    log.setInt("sequence", 0);
                    log.addBatch();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param stringProperties
     */
    private void saveStrings(boolean writeToPrimaryDataTables, final Map<String, Map<Long, SimpleStringProperty>> stringProperties) {
        int record = 0;
        try {
            INamedStatement upsert = jdsDb.supportsStatements() ? onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveString()) : onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveString());
            INamedStatement log = jdsDb.isOracleDb() ? onPostSaveEventArguments.getOrAddNamedCall(jdsDb.getSaveOldTextValues()) : onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.getSaveOldTextValues());
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
                    if (writeToPrimaryDataTables) {
                        upsert.setString("entityGuid", entityGuid);
                        upsert.setLong("fieldId", fieldId);
                        upsert.setString("value", value);
                        upsert.addBatch();
                    }
                    if (jdsDb.isPrintingOutput())
                        System.out.printf("Updating record [%s]. Text field [%s of %s]\n", record, innerRecord, innerRecordSize);
                    if (!jdsDb.isLoggingEdits()) continue;
                    log.setString("entityGuid", entityGuid);
                    log.setLong("fieldId", fieldId);
                    log.setString("value", value);
                    log.setInt("sequence", 0);
                    log.addBatch();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param localDateTimeProperties
     * @param localDateProperties
     */
    private void saveDatesAndDateTimes(boolean writeToPrimaryDataTables, final Map<String, Map<Long, SimpleObjectProperty<Temporal>>> localDateTimeProperties, final Map<String, Map<Long, SimpleObjectProperty<Temporal>>> localDateProperties) {
        int record = 0;
        try {
            INamedStatement upsert = jdsDb.supportsStatements() ? onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveDateTime()) : onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveDateTime());
            INamedStatement log = jdsDb.isOracleDb() ? onPostSaveEventArguments.getOrAddNamedCall(jdsDb.getSaveOldDateTimeValues()) : onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.getSaveOldDateTimeValues());
            for (Map.Entry<String, Map<Long, SimpleObjectProperty<Temporal>>> entry : localDateTimeProperties.entrySet()) {
                record++;
                int innerRecord = 0;
                int innerRecordSize = entry.getValue().size();
                if (innerRecordSize == 0) continue;
                String entityGuid = entry.getKey();
                for (Map.Entry<Long, SimpleObjectProperty<Temporal>> recordEntry : entry.getValue().entrySet()) {
                    innerRecord++;
                    long fieldId = recordEntry.getKey();
                    LocalDateTime localDateTime = (LocalDateTime) recordEntry.getValue().get();
                    if (writeToPrimaryDataTables) {
                        upsert.setString("entityGuid", entityGuid);
                        upsert.setLong("fieldId", fieldId);
                        upsert.setTimestamp("value", Timestamp.valueOf(localDateTime));
                        upsert.addBatch();
                    }
                    if (jdsDb.isPrintingOutput())
                        System.out.printf("Updating record [%s]. LocalDateTime field [%s of %s]\n", record, innerRecord, innerRecordSize);
                    if (!jdsDb.isLoggingEdits()) continue;
                    log.setString("entityGuid", entityGuid);
                    log.setLong("fieldId", fieldId);
                    log.setTimestamp("value", Timestamp.valueOf(localDateTime));
                    log.setInt("sequence", 0);
                    log.addBatch();
                }
            }
            for (Map.Entry<String, Map<Long, SimpleObjectProperty<Temporal>>> entry : localDateProperties.entrySet()) {
                record++;
                int innerRecord = 0;
                int innerRecordSize = entry.getValue().size();
                if (innerRecordSize == 0) continue;
                String entityGuid = entry.getKey();
                for (Map.Entry<Long, SimpleObjectProperty<Temporal>> recordEntry : entry.getValue().entrySet()) {
                    innerRecord++;
                    long fieldId = recordEntry.getKey();
                    LocalDate localDate = (LocalDate) recordEntry.getValue().get();
                    upsert.setString("entityGuid", entityGuid);
                    upsert.setLong("fieldId", fieldId);
                    upsert.setTimestamp("value", Timestamp.valueOf(localDate.atStartOfDay()));
                    upsert.addBatch();
                    if (jdsDb.isPrintingOutput())
                        System.out.printf("Updating record [%s]. LocalDate field [%s of %s]\n", record, innerRecord, innerRecordSize);
                    if (!jdsDb.isLoggingEdits()) continue;
                    log.setString("entityGuid", entityGuid);
                    log.setLong("fieldId", fieldId);
                    log.setTimestamp("value", Timestamp.valueOf(localDate.atStartOfDay()));
                    log.setInt("sequence", 0);
                    log.addBatch();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param localTimeProperties
     */
    private void saveTimes(boolean writeToPrimaryDataTables, final Map<String, Map<Long, SimpleObjectProperty<Temporal>>> localTimeProperties) {
        int record = 0;
        try {
            INamedStatement upsert = jdsDb.supportsStatements() ? onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveTime()) : onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveTime());
            INamedStatement log = jdsDb.isOracleDb() ? onPostSaveEventArguments.getOrAddNamedCall(jdsDb.getSaveOldIntegerValues()) : onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.getSaveOldIntegerValues());
            for (Map.Entry<String, Map<Long, SimpleObjectProperty<Temporal>>> entry : localTimeProperties.entrySet()) {
                record++;
                int innerRecord = 0;
                int innerRecordSize = entry.getValue().size();
                if (innerRecordSize == 0) continue;
                String entityGuid = entry.getKey();
                for (Map.Entry<Long, SimpleObjectProperty<Temporal>> recordEntry : entry.getValue().entrySet()) {
                    innerRecord++;
                    long fieldId = recordEntry.getKey();
                    LocalTime localTime = (LocalTime) recordEntry.getValue().get();
                    int secondOfDay = localTime.toSecondOfDay();
                    if (writeToPrimaryDataTables) {
                        upsert.setString("entityGuid", entityGuid);
                        upsert.setLong("fieldId", fieldId);
                        upsert.setInt("value", secondOfDay);
                        upsert.addBatch();
                    }
                    if (jdsDb.isPrintingOutput())
                        System.out.printf("Updating record [%s]. LocalTime field [%s of %s]\n", record, innerRecord, innerRecordSize);
                    if (!jdsDb.isLoggingEdits()) continue;
                    log.setString("entityGuid", entityGuid);
                    log.setLong("fieldId", fieldId);
                    log.setInt("value", secondOfDay);
                    log.setInt("sequence", 0);
                    log.addBatch();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param zonedDateProperties
     */
    private void saveZonedDateTimes(boolean writeToPrimaryDataTables, final Map<String, Map<Long, SimpleObjectProperty<Temporal>>> zonedDateProperties) {
        int record = 0;
        try {
            INamedStatement upsert = jdsDb.supportsStatements() ? onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveZonedDateTime()) : onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveZonedDateTime());
            INamedStatement log = jdsDb.isOracleDb() ? onPostSaveEventArguments.getOrAddNamedCall(jdsDb.getSaveOldLongValues()) : onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.getSaveOldLongValues());
            for (Map.Entry<String, Map<Long, SimpleObjectProperty<Temporal>>> entry : zonedDateProperties.entrySet()) {
                record++;
                int innerRecord = 0;
                int innerRecordSize = entry.getValue().size();
                if (innerRecordSize == 0) continue;
                String entityGuid = entry.getKey();
                for (Map.Entry<Long, SimpleObjectProperty<Temporal>> recordEntry : entry.getValue().entrySet()) {
                    innerRecord++;
                    long fieldId = recordEntry.getKey();
                    ZonedDateTime zonedDateTime = (ZonedDateTime) recordEntry.getValue().get();
                    if (writeToPrimaryDataTables) {
                        upsert.setString("entityGuid", entityGuid);
                        upsert.setLong("fieldId", fieldId);
                        upsert.setLong("value", zonedDateTime.toEpochSecond());
                        upsert.addBatch();
                    }
                    if (jdsDb.isPrintingOutput())
                        System.out.printf("Updating record [%s]. ZonedDateTime field [%s of %s]\n", record, innerRecord, innerRecordSize);
                    if (!jdsDb.isLoggingEdits()) continue;
                    log.setString("entityGuid", entityGuid);
                    log.setLong("fieldId", fieldId);
                    log.setLong("value", zonedDateTime.toEpochSecond());
                    log.setInt("sequence", 0);
                    log.addBatch();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param enums
     */
    public void saveEnums(boolean writeToPrimaryDataTables, final Map<String, Map<JdsFieldEnum, SimpleObjectProperty<Enum>>> enums) {
        int record = 0;
        try {
            INamedStatement upsert = jdsDb.supportsStatements() ? onPostSaveEventArguments.getOrAddNamedCall(jdsDb.saveInteger()) : onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.saveInteger());
            INamedStatement log = jdsDb.isOracleDb() ? onPostSaveEventArguments.getOrAddNamedCall(jdsDb.getSaveOldIntegerValues()) : onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.getSaveOldIntegerValues());
            for (Map.Entry<String, Map<JdsFieldEnum, SimpleObjectProperty<Enum>>> entry : enums.entrySet()) {
                record++;
                int innerRecord = 0;
                int innerRecordSize = entry.getValue().size();
                if (innerRecordSize == 0) continue;
                String entityGuid = entry.getKey();
                for (Map.Entry<JdsFieldEnum, SimpleObjectProperty<Enum>> recordEntry : entry.getValue().entrySet()) {
                    innerRecord++;
                    JdsFieldEnum jdsFieldEnum = recordEntry.getKey();
                    Enum value = recordEntry.getValue().get();
                    if (writeToPrimaryDataTables) {
                        upsert.setString("entityGuid", entityGuid);
                        upsert.setLong("fieldId", jdsFieldEnum.getField().getId());
                        upsert.setInt("value", jdsFieldEnum.indexOf(value));
                        upsert.addBatch();
                    }
                    if (jdsDb.isPrintingOutput())
                        System.out.printf("Updating record [%s]. Enum field [%s of %s]\n", record, innerRecord, innerRecordSize);
                    if (!jdsDb.isLoggingEdits()) continue;
                    log.setString("entityGuid", entityGuid);
                    log.setLong("fieldId", jdsFieldEnum.getField().getId());
                    log.setInt("value", jdsFieldEnum.indexOf(value));
                    log.setInt("sequence", 0);
                    log.addBatch();
                }
            }
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
    private void saveArrayDates(boolean writeToPrimaryDataTables, final Map<String, Map<Long, SimpleListProperty<LocalDateTime>>> dateTimeArrayProperties) {
        String deleteSql = "DELETE FROM JdsStoreDateTimeArray WHERE FieldId = ? AND EntityGuid = ?";
        String insertSql = "INSERT INTO JdsStoreDateTimeArray (Sequence,Value,FieldId,EntityGuid) VALUES (?,?,?,?)";
        try {
            INamedStatement log = jdsDb.isOracleDb() ? onPostSaveEventArguments.getOrAddNamedCall(jdsDb.getSaveOldDateTimeValues()) : onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.getSaveOldDateTimeValues());
            PreparedStatement delete = onPostSaveEventArguments.getOrAddStatement(deleteSql);
            PreparedStatement insert = onPostSaveEventArguments.getOrAddStatement(insertSql);
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
                        if (jdsDb.isLoggingEdits()) {
                            log.setString("entityGuid", entityGuid);
                            log.setLong("fieldId", fieldId);
                            log.setTimestamp("value", Timestamp.valueOf(value));
                            log.setInt("sequence", index.get());
                            log.addBatch();
                        }
                        if (writeToPrimaryDataTables) {
                            delete.setLong(1, fieldId);
                            delete.setString(2, entityGuid);
                            delete.addBatch();
                            //insert
                            insert.setInt(1, index.get());
                            insert.setTimestamp(2, Timestamp.valueOf(value));
                            insert.setLong(3, fieldId);
                            insert.setString(4, entityGuid);
                            insert.addBatch();
                        }
                        index.set(index.get() + 1);
                        if (jdsDb.isPrintingOutput())
                            System.out.printf("Inserting array record [%s]. DateTime field [%s of %s]\n", record, innerRecord, innerTotal);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param floatArrayProperties
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5]to[3,4]
     */
    private void saveArrayFloats(boolean writeToPrimaryDataTables, final Map<String, Map<Long, SimpleListProperty<Float>>> floatArrayProperties) {
        String deleteSql = "DELETE FROM JdsStoreFloatArray WHERE FieldId = ? AND EntityGuid = ?";
        String insertSql = "INSERT INTO JdsStoreFloatArray (FieldId,EntityGuid,Value,Sequence) VALUES (?,?,?,?)";
        try {
            INamedStatement log = jdsDb.isOracleDb() ? onPostSaveEventArguments.getOrAddNamedCall(jdsDb.getSaveOldFloatValues()) : onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.getSaveOldFloatValues());
            PreparedStatement delete = onPostSaveEventArguments.getOrAddStatement(deleteSql);
            PreparedStatement insert = onPostSaveEventArguments.getOrAddStatement(insertSql);
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
                        if (jdsDb.isLoggingEdits()) {
                            log.setString("entityGuid", entityGuid);
                            log.setLong("fieldId", fieldId);
                            log.setFloat("value", value);
                            log.setInt("sequence", index.get());
                            log.addBatch();
                        }
                        if (writeToPrimaryDataTables) {//delete
                            delete.setLong(1, fieldId);
                            delete.setString(2, entityGuid);
                            delete.addBatch();
                            //insert
                            insert.setInt(1, index.get());
                            insert.setFloat(2, value);
                            insert.setLong(3, fieldId);
                            insert.setString(4, entityGuid);
                            insert.addBatch();
                        }
                        index.set(index.get() + 1);
                        if (jdsDb.isPrintingOutput())
                            System.out.printf("Inserting array record [%s]. Float field [%s of %s]\n", record, innerRecord, innerTotal);

                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param integerArrayProperties
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5] to [3,4]
     */
    private void saveArrayIntegers(boolean writeToPrimaryDataTables, final Map<String, Map<Long, SimpleListProperty<Integer>>> integerArrayProperties) {
        String deleteSql = "DELETE FROM JdsStoreIntegerArray WHERE FieldId = :fieldId AND EntityGuid = :entityGuid";
        String insertSql = "INSERT INTO JdsStoreIntegerArray (FieldId,EntityGuid,Sequence,Value) VALUES (:fieldId, :entityGuid, :sequence, :value)";
        try {
            INamedStatement log = jdsDb.isOracleDb() ? onPostSaveEventArguments.getOrAddNamedCall(jdsDb.getSaveOldIntegerValues()) : onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.getSaveOldIntegerValues());
            INamedStatement delete = onPostSaveEventArguments.getOrAddNamedStatement(deleteSql);
            INamedStatement insert = onPostSaveEventArguments.getOrAddNamedStatement(insertSql);
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
                        if (jdsDb.isLoggingEdits()) {
                            log.setString("entityGuid", entityGuid);
                            log.setLong("fieldId", fieldId);
                            log.setInt("value", value);
                            log.setInt("sequence", index.get());
                            log.addBatch();
                        }
                        if (writeToPrimaryDataTables) {
                            //delete
                            delete.setLong("fieldId", fieldId);
                            delete.setString("entityGuid", entityGuid);
                            delete.addBatch();
                            //insert
                            insert.setInt("sequence", index.get());
                            insert.setInt("value", value);
                            insert.setLong("fieldId", fieldId);
                            insert.setString("entityGuid", entityGuid);
                            insert.addBatch();
                        }
                        index.set(index.get() + 1);
                        if (jdsDb.isPrintingOutput())
                            System.out.printf("Inserting array record [%s]. Integer field [%s of %s]\n", record, innerRecord, innerTotal);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param doubleArrayProperties
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5]to[3,4]
     */
    private void saveArrayDoubles(boolean writeToPrimaryDataTables, final Map<String, Map<Long, SimpleListProperty<Double>>> doubleArrayProperties) {
        String deleteSql = "DELETE FROM JdsStoreDoubleArray WHERE FieldId = :fieldId AND EntityGuid = :entityGuid";
        String insertSql = "INSERT INTO JdsStoreDoubleArray (FieldId,EntityGuid,Sequence,Value) VALUES (:fieldId, :entityGuid, :sequence, :value)";
        try {
            INamedStatement log = jdsDb.isOracleDb() ? onPostSaveEventArguments.getOrAddNamedCall(jdsDb.getSaveOldDoubleValues()) : onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.getSaveOldDoubleValues());
            INamedStatement delete = onPostSaveEventArguments.getOrAddNamedStatement(deleteSql);
            INamedStatement insert = onPostSaveEventArguments.getOrAddNamedStatement(insertSql);
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
                        if (jdsDb.isLoggingEdits()) {
                            log.setString("entityGuid", entityGuid);
                            log.setLong("fieldId", fieldId);
                            log.setDouble("value", value);
                            log.setInt("sequence", index.get());
                            log.addBatch();
                        }
                        if (writeToPrimaryDataTables) {
                            //delete
                            delete.setLong("fieldId", fieldId);
                            delete.setString("entityGuid", entityGuid);
                            delete.addBatch();
                            //insert
                            insert.setInt("fieldId", index.get());
                            insert.setDouble("entityGuid", value);
                            insert.setLong("sequence", fieldId);
                            insert.setString("value", entityGuid);
                            insert.addBatch();
                        }
                        index.set(index.get() + 1);
                        if (jdsDb.isPrintingOutput())
                            System.out.printf("Inserting array record [%s]. Double field [%s of %s]\n", record, innerRecord, innerTotal);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param longArrayProperties
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5]to[3,4]
     */
    private void saveArrayLongs(boolean writeToPrimaryDataTables, final Map<String, Map<Long, SimpleListProperty<Long>>> longArrayProperties) {
        String deleteSql = "DELETE FROM JdsStoreDoubleArray WHERE FieldId = ? AND EntityGuid = ?";
        String insertSql = "INSERT INTO JdsStoreDoubleArray (FieldId,EntityGuid,Sequence,Value) VALUES (?,?,?,?)";
        try {
            INamedStatement log = jdsDb.isOracleDb() ? onPostSaveEventArguments.getOrAddNamedCall(jdsDb.getSaveOldLongValues()) : onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.getSaveOldLongValues());
            PreparedStatement delete = onPostSaveEventArguments.getOrAddStatement(deleteSql);
            PreparedStatement insert = onPostSaveEventArguments.getOrAddStatement(insertSql);
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
                        if (jdsDb.isLoggingEdits()) {
                            log.setString("entityGuid", entityGuid);
                            log.setLong("fieldId", fieldId);
                            log.setLong("value", value);
                            log.setInt("sequence", index.get());
                            log.addBatch();
                        }
                        if (writeToPrimaryDataTables) {//delete
                            delete.setLong(1, fieldId);
                            delete.setString(2, entityGuid);
                            delete.addBatch();
                            //insert
                            insert.setInt(1, index.get());
                            insert.setLong(2, value);
                            insert.setLong(3, fieldId);
                            insert.setString(4, entityGuid);
                            insert.addBatch();
                        }
                        index.set(index.get() + 1);
                        if (jdsDb.isPrintingOutput())
                            System.out.printf("Inserting array record [%s]. Long field [%s of %s]\n", record, innerRecord, innerTotal);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param stringArrayProperties
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5]to[3,4]
     */
    private void saveArrayStrings(boolean writeToPrimaryDataTables, final Map<String, Map<Long, SimpleListProperty<String>>> stringArrayProperties) {
        String deleteSql = "DELETE FROM JdsStoreTextArray WHERE FieldId = :fieldId AND EntityGuid = :entityGuid";
        String insertSql = "INSERT INTO JdsStoreTextArray (FieldId,EntityGuid,Sequence,Value) VALUES (:fieldId, :entityGuid, :sequence, :value)";
        try {
            INamedStatement log = jdsDb.isOracleDb() ? onPostSaveEventArguments.getOrAddNamedCall(jdsDb.getSaveOldTextValues()) : onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.getSaveOldTextValues());
            INamedStatement delete = onPostSaveEventArguments.getOrAddNamedStatement(deleteSql);
            INamedStatement insert = onPostSaveEventArguments.getOrAddNamedStatement(insertSql);
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
                        if (jdsDb.isLoggingEdits()) {
                            log.setString("entityGuid", entityGuid);
                            log.setLong("fieldId", fieldId);
                            log.setString("value", value);
                            log.setInt("sequence", index.get());
                            log.addBatch();
                        }
                        if (writeToPrimaryDataTables) {
                            //delete
                            delete.setLong("fieldId", fieldId);
                            delete.setString("entityGuid", entityGuid);
                            delete.addBatch();
                            //insert
                            insert.setInt("fieldId", index.get());
                            insert.setString("entityGuid", value);
                            insert.setLong("sequence", fieldId);
                            insert.setString("value", entityGuid);
                            insert.addBatch();
                        }
                        index.set(index.get() + 1);
                        if (jdsDb.isPrintingOutput())
                            System.out.printf("Inserting array record [%s]. String field [%s of %s]\n", record, innerRecord, innerTotal);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param enumStrings
     * @apiNote Enums are actually saved as index based integer arrays
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5]to[3,4]
     */
    private void saveEnumCollections(boolean writeToPrimaryDataTables, final Map<String, Map<JdsFieldEnum, SimpleListProperty<Enum>>> enumStrings) {
        int record = 0;
        int recordTotal = enumStrings.size();
        String deleteSql = "DELETE FROM JdsStoreIntegerArray WHERE FieldId = :fieldId AND EntityGuid = :entityGuid";
        String insertSql = "INSERT INTO JdsStoreIntegerArray (FieldId,EntityGuid,Sequence,Value) VALUES (:fieldId, :entityGuid, :sequence, :value)";
        try {
            INamedStatement log = jdsDb.isOracleDb() ? onPostSaveEventArguments.getOrAddNamedCall(jdsDb.getSaveOldIntegerValues()) : onPostSaveEventArguments.getOrAddNamedStatement(jdsDb.getSaveOldIntegerValues());
            INamedStatement delete = onPostSaveEventArguments.getOrAddNamedStatement(deleteSql);
            INamedStatement insert = onPostSaveEventArguments.getOrAddNamedStatement(insertSql);
            for (Map.Entry<String, Map<JdsFieldEnum, SimpleListProperty<Enum>>> entry : enumStrings.entrySet()) {
                record++;
                String entityGuid = entry.getKey();
                for (Map.Entry<JdsFieldEnum, SimpleListProperty<Enum>> fieldEnums : entry.getValue().entrySet()) {
                    int sequence = 0;
                    JdsFieldEnum jdsFieldEnum = fieldEnums.getKey();
                    ObservableList<? extends Enum> textValues = fieldEnums.getValue().get();
                    if (textValues.size() == 0) continue;
                    for (Enum anEnum : textValues) {
                        if (jdsDb.isLoggingEdits()) {
                            log.setString("entityGuid", entityGuid);
                            log.setLong("fieldId", jdsFieldEnum.getField().getId());
                            log.setInt("value", jdsFieldEnum.indexOf(anEnum));
                            log.setInt("sequence", sequence);
                            log.addBatch();
                        }
                        if (writeToPrimaryDataTables) {
                            //delete
                            delete.setLong("fieldId", jdsFieldEnum.getField().getId());
                            delete.setString("entityGuid", entityGuid);
                            delete.addBatch();
                            //insert
                            insert.setLong("fieldId", jdsFieldEnum.getField().getId());
                            insert.setString("entityGuid", entityGuid);
                            insert.setInt("sequence", sequence);
                            insert.setInt("value", jdsFieldEnum.indexOf(anEnum));
                            insert.addBatch();
                        }
                        if (jdsDb.isPrintingOutput())
                            System.out.printf("Updating enum [%s]. Object field [%s of %s]\n", sequence, record, recordTotal);
                        sequence++;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param connection
     * @param objectArrayProperties
     * @implNote Arrays have old entries deleted first. This for cases where a user may have reduced the amount of entries in the collection i.e [3,4,5]to[3,4]
     * @implNote For the love of Christ don't use parallel stream here
     */
    private void saveAndBindObjectArrays(final Connection connection, final Map<String, Map<Long, SimpleListProperty<JdsEntity>>> objectArrayProperties) throws Exception {
        if (objectArrayProperties.isEmpty()) return;
        final Collection<JdsEntity> jdsEntities = new ArrayList<>();
        final Collection<JdsParentEntityBinding> parentEntityBindings = new ArrayList<>();
        final Collection<JdsParentChildBinding> parentChildBindings = new ArrayList<>();
        final IntegerProperty record = new SimpleIntegerProperty(0);
        final BooleanProperty changesMade = new SimpleBooleanProperty(false);
        for (Map.Entry<String, Map<Long, SimpleListProperty<JdsEntity>>> serviceCodeEntities : objectArrayProperties.entrySet()) {
            String parentGuid = serviceCodeEntities.getKey();
            for (Map.Entry<Long, SimpleListProperty<JdsEntity>> serviceCodeEntity : serviceCodeEntities.getValue().entrySet()) {
                record.set(0);
                changesMade.set(false);
                serviceCodeEntity.getValue().stream().filter(jdsEntity -> jdsEntity != null).forEach(jdsEntity -> {
                    if (!changesMade.get()) {
                        //only clear if changes are made. else you wipe out old bindings regardless
                        changesMade.set(true);
                        JdsParentEntityBinding parentEntityBinding = new JdsParentEntityBinding();
                        parentEntityBinding.parentGuid = parentGuid;
                        parentEntityBinding.entityId = serviceCodeEntity.getKey();
                        parentEntityBindings.add(parentEntityBinding);

                    }
                    JdsParentChildBinding parentChildBinding = new JdsParentChildBinding();
                    parentChildBinding.parentGuid = parentGuid;
                    parentChildBinding.childGuid = jdsEntity.getEntityGuid();
                    parentChildBindings.add(parentChildBinding);
                    jdsEntities.add(jdsEntity);
                    record.set(record.get() + 1);
                    if (jdsDb.isPrintingOutput())
                        System.out.printf("Binding array object %s\n", record.get());
                });
            }
        }
        //save children first
        new JdsSave(jdsDb, connection, -1, jdsEntities, true, onPreSaveEventArguments, onPostSaveEventArguments).call();

        //bind children below
        try {
            INamedStatement clearOldBindings = onPostSaveEventArguments.getOrAddNamedStatement("DELETE FROM JdsStoreEntityBinding WHERE ParentEntityGuid = :parentEntityGuid AND ChildEntityId = :childEntityId");
            INamedStatement writeNewBindings = onPostSaveEventArguments.getOrAddNamedStatement("INSERT INTO JdsStoreEntityBinding(ParentEntityGuid,ChildEntityGuid,ChildEntityId) Values(:parentEntityGuid, :childEntityGuid, :childEntityId)");
            for (JdsParentEntityBinding parentEntityBinding : parentEntityBindings) {
                clearOldBindings.setString("parentEntityGuid", parentEntityBinding.parentGuid);
                clearOldBindings.setLong("childEntityId", parentEntityBinding.entityId);
                clearOldBindings.addBatch();
            }
            for (JdsEntity jdsEntity : jdsEntities) {
                writeNewBindings.setString("parentEntityGuid", getParent(parentChildBindings, jdsEntity.getEntityGuid()));
                writeNewBindings.setString("childEntityGuid", jdsEntity.getEntityGuid());
                writeNewBindings.setLong("childEntityId", jdsEntity.getEntityCode());
                writeNewBindings.addBatch();
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param connection
     * @param objectProperties
     * @implNote For the love of Christ don't use parallel stream here
     */
    private void saveAndBindObjects(final Connection connection, final Map<String, Map<Long, SimpleObjectProperty<JdsEntity>>> objectProperties) throws Exception {
        if (objectProperties.isEmpty()) return;//prevent stack overflow :)
        final IntegerProperty record = new SimpleIntegerProperty(0);
        final BooleanProperty changesMade = new SimpleBooleanProperty(false);
        final Collection<JdsParentEntityBinding> parentEntityBindings = new ArrayList<>();
        final Collection<JdsParentChildBinding> parentChildBindings = new ArrayList<>();
        final Collection<JdsEntity> jdsEntities = new ArrayList<>();
        for (Map.Entry<String, Map<Long, SimpleObjectProperty<JdsEntity>>> entry : objectProperties.entrySet()) {
            String parentGuid = entry.getKey();
            for (Map.Entry<Long, SimpleObjectProperty<JdsEntity>> recordEntry : entry.getValue().entrySet()) {
                record.set(0);
                JdsEntity jdsEntity = recordEntry.getValue().get();
                changesMade.set(false);
                if (jdsEntity != null) {
                    if (!changesMade.get()) {
                        changesMade.set(true);
                        JdsParentEntityBinding parentEntityBinding = new JdsParentEntityBinding();
                        parentEntityBinding.parentGuid = parentGuid;
                        parentEntityBinding.entityId = recordEntry.getKey();
                        parentEntityBindings.add(parentEntityBinding);
                    }
                    jdsEntities.add(jdsEntity);
                    JdsParentChildBinding parentChildBinding = new JdsParentChildBinding();
                    parentChildBinding.parentGuid = parentGuid;
                    parentChildBinding.childGuid = jdsEntity.getEntityGuid();
                    parentChildBindings.add(parentChildBinding);
                    record.set(record.get() + 1);
                    if (jdsDb.isPrintingOutput())
                        System.out.printf("Binding object %s\n", record.get());
                }
            }
        }
        //save children first
        new JdsSave(jdsDb, connection, -1, jdsEntities, true, onPreSaveEventArguments, onPostSaveEventArguments).call();

        //bind children below
        try {
            INamedStatement clearOldBindings = onPostSaveEventArguments.getOrAddNamedStatement("DELETE FROM JdsStoreEntityBinding WHERE ParentEntityGuid = :parentEntityGuid AND ChildEntityId = :childEntityId");
            INamedStatement writeNewBindings = onPostSaveEventArguments.getOrAddNamedStatement("INSERT INTO JdsStoreEntityBinding(ParentEntityGuid,ChildEntityGuid,ChildEntityId) Values(:parentEntityGuid, :childEntityGuid, :childEntityId)");
            for (JdsParentEntityBinding parentEntityBinding : parentEntityBindings) {
                clearOldBindings.setString("parentEntityGuid", parentEntityBinding.parentGuid);
                clearOldBindings.setLong("childEntityId", parentEntityBinding.entityId);
                clearOldBindings.addBatch();
            }
            for (JdsEntity jdsEntity : jdsEntities) {
                writeNewBindings.setString("parentEntityGuid", getParent(parentChildBindings, jdsEntity.getEntityGuid()));
                writeNewBindings.setString("childEntityGuid", jdsEntity.getEntityGuid());
                writeNewBindings.setLong("childEntityId", jdsEntity.getEntityCode());
                writeNewBindings.addBatch();
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param jdsParentChildBindings
     * @param childGuid
     * @return
     */
    private String getParent(final Collection<JdsParentChildBinding> jdsParentChildBindings, final String childGuid) {
        Optional<JdsParentChildBinding> any = jdsParentChildBindings.stream().filter(parentChildBinding -> parentChildBinding.childGuid.equals(childGuid)).findAny();
        return any.isPresent() ? any.get().parentGuid : "";
    }

    /**
     * @param jdsDb
     * @param batchSize
     * @param entities
     * @deprecated please refer to <a href="https://github.com/SubiyaCryolite/Jenesis-Data-Store"> the readme</a> for the most up to date CRUD approach
     */
    public static void save(final JdsDb jdsDb, final int batchSize, final Collection<? extends JdsEntity> entities) throws Exception {
        new JdsSave(jdsDb, batchSize, entities).call();
    }

    /**
     * @param jdsDb
     * @param batchSize
     * @param entities
     * @deprecated please refer to <a href="https://github.com/SubiyaCryolite/Jenesis-Data-Store"> the readme</a> for the most up to date CRUD approach
     */
    public static void save(final JdsDb jdsDb, final int batchSize, final JdsEntity... entities) throws Exception {
        save(jdsDb, batchSize, Arrays.asList(entities));
    }
}
