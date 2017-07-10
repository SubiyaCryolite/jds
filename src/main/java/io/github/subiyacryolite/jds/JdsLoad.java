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

import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation;
import io.github.subiyacryolite.jds.events.JdsPostLoadListener;
import io.github.subiyacryolite.jds.events.JdsPreLoadListener;
import io.github.subiyacryolite.jds.events.OnPostLoadEvent;
import io.github.subiyacryolite.jds.events.OnPreLoadEventArguments;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.IOException;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * This class is responsible for loading an {@link JdsEntity entities} {@link JdsField fields}
 */
public class JdsLoad<T extends JdsEntity> implements Callable<List<T>> {

    /**
     * Java supports up to 1000 prepared supportsStatements depending on the driver
     */
    public static final int MAX_BATCH_SIZE = 1000;
    private final List<T> collections = new ArrayList<>();
    private final JdsDb jdsDb;
    private final Class<T> referenceType;
    private final String[] entityGuids;
    private Comparator<T> comparator;

    /**
     * @param jdsDb
     * @param referenceType
     * @param comparator
     * @param entityGuids
     */
    public JdsLoad(final JdsDb jdsDb, final Class<T> referenceType, Comparator<T> comparator, final String... entityGuids) {
        this.jdsDb = jdsDb;
        this.referenceType = referenceType;
        this.entityGuids = entityGuids;
        this.comparator = comparator;
    }

    /**
     * @param jdsDb
     * @param referenceType
     * @param entityGuids
     */
    public JdsLoad(final JdsDb jdsDb, final Class<T> referenceType, final String... entityGuids) {
        this.jdsDb = jdsDb;
        this.referenceType = referenceType;
        this.entityGuids = entityGuids;
    }


    /**
     * @param jdsDb
     * @param referenceType
     * @param entities
     * @param initialisePrimitives
     * @param initialiseDatesAndTimes
     * @param initialiseObjects
     * @param entityGuids
     * @param <T>
     */

    private <T extends JdsEntity> void populateInner(final JdsDb jdsDb,
                                                     Class<T> referenceType,
                                                     final Collection<T> entities,
                                                     final boolean initialisePrimitives,
                                                     final boolean initialiseDatesAndTimes,
                                                     final boolean initialiseObjects,
                                                     final Collection<String> entityGuids) {
        String questionsString = getQuestions(entityGuids.size());
        //primitives
        String sqlTextValues = String.format("SELECT EntityGuid, Value, FieldId FROM JdsStoreText WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        String sqlLongValues = String.format("SELECT EntityGuid, Value, FieldId FROM JdsStoreLong WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        String sqlIntegerValues = String.format("SELECT EntityGuid, Value, FieldId FROM JdsStoreInteger WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        String sqlFloatValues = String.format("SELECT EntityGuid, Value, FieldId FROM JdsStoreFloat WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        String sqlDoubleValues = String.format("SELECT EntityGuid, Value, FieldId FROM JdsStoreDouble WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        String sqlDateTimeValues = String.format("SELECT EntityGuid, Value, FieldId FROM JdsStoreDateTime WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        String sqlTimeValues = String.format("SELECT EntityGuid, Value, FieldId FROM JdsStoreTime WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        String sqlZonedDateTimeValues = String.format("SELECT EntityGuid, Value, FieldId FROM JdsStoreZonedDateTime WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        //blobs
        String sqlBlobs = String.format("SELECT EntityGuid, Value, FieldId FROM JdsStoreBlob WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        //array
        String sqlTextArrayValues = String.format("SELECT EntityGuid, Value, FieldId, Sequence FROM JdsStoreTextArray WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        String sqlIntegerArrayAndEnumValues = String.format("SELECT EntityGuid, Value, FieldId, Sequence FROM JdsStoreIntegerArray WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        String sqlLongArrayValues = String.format("SELECT EntityGuid, Value, FieldId, Sequence FROM JdsStoreLongArray WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        String sqlFloatArrayValues = String.format("SELECT EntityGuid, Value, FieldId, Sequence FROM JdsStoreFloatArray WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        String sqlDoubleArrayValues = String.format("SELECT EntityGuid, Value, FieldId, Sequence FROM JdsStoreDoubleArray WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        String sqlDateTimeArrayValues = String.format("SELECT EntityGuid, Value, FieldId, Sequence FROM JdsStoreDateTimeArray WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        String sqlEmbeddedAndArrayObjects = String.format("SELECT ChildEntityGuid, ParentEntityGuid, ChildEntityId FROM JdsStoreEntityBinding WHERE ParentEntityGuid IN (%s) ORDER BY ParentEntityGuid", questionsString);
        //overviews
        String sqlOverviews = String.format("SELECT EntityGuid, DateCreated, DateModified, EntityId FROM JdsStoreEntityOverview WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        try (Connection connection = jdsDb.getConnection();
             PreparedStatement blobs = connection.prepareStatement(sqlBlobs);
             PreparedStatement strings = connection.prepareStatement(sqlTextValues);
             PreparedStatement longs = connection.prepareStatement(sqlLongValues);
             PreparedStatement integers = connection.prepareStatement(sqlIntegerValues);
             PreparedStatement floats = connection.prepareStatement(sqlFloatValues);
             PreparedStatement doubles = connection.prepareStatement(sqlDoubleValues);
             PreparedStatement dateTimes = connection.prepareStatement(sqlDateTimeValues);
             PreparedStatement times = connection.prepareStatement(sqlTimeValues);
             PreparedStatement zonedDateTimes = connection.prepareStatement(sqlZonedDateTimeValues);
             PreparedStatement textArrays = connection.prepareStatement(sqlTextArrayValues);
             PreparedStatement integerArraysAndEnums = connection.prepareStatement(sqlIntegerArrayAndEnumValues);
             PreparedStatement longArrays = connection.prepareStatement(sqlLongArrayValues);
             PreparedStatement floatArrays = connection.prepareStatement(sqlFloatArrayValues);
             PreparedStatement doubleArrays = connection.prepareStatement(sqlDoubleArrayValues);
             PreparedStatement dateTimeArrays = connection.prepareStatement(sqlDateTimeArrayValues);
             PreparedStatement embeddedAndArrayObjects = connection.prepareStatement(sqlEmbeddedAndArrayObjects);
             PreparedStatement overviews = connection.prepareStatement(sqlOverviews)) {
            //work in batches to not break prepared statement
            int batchSequence = 1;
            for (String entityGuid : entityGuids) {
                if (referenceType != null && (initialisePrimitives || initialiseDatesAndTimes || initialiseObjects)) {
                    //sometimes the entities would already have been instanciated, thus we only need to populate
                    T entity = referenceType.newInstance();
                    entity.setEntityGuid(entityGuid);
                    entities.add(entity);
                    if (entity instanceof JdsPreLoadListener)
                        ((JdsPreLoadListener) entity).onPreLoad(new OnPreLoadEventArguments(entityGuid, batchSequence, entityGuids.size()));
                }
                //primitives
                setParameterForStatement(batchSequence, entityGuid, strings);
                setParameterForStatement(batchSequence, entityGuid, integers);
                setParameterForStatement(batchSequence, entityGuid, longs);
                setParameterForStatement(batchSequence, entityGuid, floats);
                setParameterForStatement(batchSequence, entityGuid, doubles);
                setParameterForStatement(batchSequence, entityGuid, dateTimes);
                setParameterForStatement(batchSequence, entityGuid, times);
                setParameterForStatement(batchSequence, entityGuid, zonedDateTimes);
                //blobs
                setParameterForStatement(batchSequence, entityGuid, blobs);
                //array
                setParameterForStatement(batchSequence, entityGuid, textArrays);
                setParameterForStatement(batchSequence, entityGuid, longArrays);
                setParameterForStatement(batchSequence, entityGuid, floatArrays);
                setParameterForStatement(batchSequence, entityGuid, doubleArrays);
                setParameterForStatement(batchSequence, entityGuid, dateTimeArrays);
                setParameterForStatement(batchSequence, entityGuid, integerArraysAndEnums);
                //object and object arrays
                setParameterForStatement(batchSequence, entityGuid, embeddedAndArrayObjects);
                //overview
                setParameterForStatement(batchSequence, entityGuid, overviews);
                batchSequence++;
            }
            if (initialisePrimitives) {
                //primitives
                populateText(entities, strings);
                populateLong(entities, longs);
                populateIntegerAndBoolean(entities, integers);
                populateFloat(entities, floats);
                populateDouble(entities, doubles);
                //integer arrays and enums
                populateIntegerArraysAndEnums(entities, integerArraysAndEnums);
                populateFloatArrays(entities, floatArrays);
                populateLongArrays(entities, longArrays);
                populateStringArrays(entities, textArrays);
                populateDoubleArrays(entities, doubleArrays);

            }
            if (initialiseDatesAndTimes) {
                populateZonedDateTime(entities, zonedDateTimes);
                populateDateTimeAndDate(entities, dateTimes);
                populateTimes(entities, times);
                populateDateTimeArrays(entities, dateTimeArrays);
            }
            if (initialiseObjects) {
                //blobs
                populateBlobs(entities, blobs);
                populateObjectEntriesAndObjectArrays(jdsDb, entities, embeddedAndArrayObjects, initialisePrimitives, initialiseDatesAndTimes, initialiseObjects);
            }
            populateOverviews(entities, overviews);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }


    /**
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    private <T extends JdsEntity> void populateFloatArrays(Collection<T> jdsEntities, PreparedStatement preparedStatement) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String entityGuid = resultSet.getString("EntityGuid");
                float value = resultSet.getFloat("Value");
                long fieldId = resultSet.getLong("FieldId");
                optimalEntityLookup(jdsEntities, entityGuid).stream().filter(jdsEntity -> jdsEntity.floatArrayProperties.containsKey(fieldId)).forEach(currentEntity -> {
                    SimpleListProperty<Float> property = currentEntity.floatArrayProperties.get(fieldId);
                    property.add(value);
                });
            }
        }
    }

    /**
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    private <T extends JdsEntity> void populateDoubleArrays(Collection<T> jdsEntities, PreparedStatement preparedStatement) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String entityGuid = resultSet.getString("EntityGuid");
                double value = resultSet.getDouble("Value");
                long fieldId = resultSet.getLong("FieldId");
                optimalEntityLookup(jdsEntities, entityGuid).stream().filter(jdsEntity -> jdsEntity.doubleArrayProperties.containsKey(fieldId)).forEach(currentEntity -> {
                    SimpleListProperty<Double> property = currentEntity.doubleArrayProperties.get(fieldId);
                    property.add(value);
                });
            }
        }
    }

    /**
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    private <T extends JdsEntity> void populateLongArrays(Collection<T> jdsEntities, PreparedStatement preparedStatement) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String entityGuid = resultSet.getString("EntityGuid");
                long value = resultSet.getLong("Value");
                long fieldId = resultSet.getLong("FieldId");
                optimalEntityLookup(jdsEntities, entityGuid).stream().filter(jdsEntity -> jdsEntity.longArrayProperties.containsKey(fieldId)).forEach(currentEntity -> {
                    SimpleListProperty<Long> property = currentEntity.longArrayProperties.get(fieldId);
                    property.add(value);
                });
            }
        }
    }

    /**
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    private <T extends JdsEntity> void populateDateTimeArrays(Collection<T> jdsEntities, PreparedStatement preparedStatement) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String entityGuid = resultSet.getString("EntityGuid");
                Timestamp value = resultSet.getTimestamp("Value");
                long fieldId = resultSet.getLong("FieldId");
                optimalEntityLookup(jdsEntities, entityGuid).stream().filter(jdsEntity -> jdsEntity.dateTimeArrayProperties.containsKey(fieldId)).forEach(currentEntity -> {
                    SimpleListProperty<LocalDateTime> property = currentEntity.dateTimeArrayProperties.get(fieldId);
                    property.add(value.toLocalDateTime());
                });
            }
        }
    }

    /**
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    private <T extends JdsEntity> void populateStringArrays(Collection<T> jdsEntities, PreparedStatement preparedStatement) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String entityGuid = resultSet.getString("EntityGuid");
                String value = resultSet.getString("Value");
                long fieldId = resultSet.getLong("FieldId");
                optimalEntityLookup(jdsEntities, entityGuid).stream().filter(jdsEntity -> jdsEntity.stringArrayProperties.containsKey(fieldId)).forEach(currentEntity -> {
                    SimpleListProperty<String> property = currentEntity.stringArrayProperties.get(fieldId);
                    property.add(value);
                });
            }
        }
    }

    /**
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    private <T extends JdsEntity> void populateIntegerArraysAndEnums(Collection<T> jdsEntities, PreparedStatement preparedStatement) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String entityGuid = resultSet.getString("EntityGuid");
                int value = resultSet.getInt("Value");
                long fieldId = resultSet.getLong("FieldId");
                for (JdsEntity jdsEntity : optimalEntityLookup(jdsEntities, entityGuid)) {
                    if (jdsEntity.integerArrayProperties.containsKey(fieldId)) {
                        SimpleListProperty<Integer> property = jdsEntity.integerArrayProperties.get(fieldId);
                        property.add(value);
                    }else {
                        Optional<JdsFieldEnum> fieldEnum = jdsEntity.enumProperties.keySet().stream().filter(entry -> entry.getField().getId() == fieldId).findAny();
                        if (fieldEnum.isPresent()) {
                            JdsFieldEnum jdsFieldEnum = fieldEnum.get();
                            SimpleListProperty<Enum> property = jdsEntity.enumProperties.get(jdsFieldEnum);
                            Object[] enumValues = jdsFieldEnum.getEnumType().getEnumConstants();
                            if (value < enumValues.length) {
                                property.add((Enum) enumValues[value]);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @param jdsDb
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    private <T extends JdsEntity> void populateObjectEntriesAndObjectArrays(JdsDb jdsDb, Collection<T> jdsEntities,
                                                                            PreparedStatement preparedStatement,
                                                                            final boolean initialisePrimitives,
                                                                            final boolean initialiseDatesAndTimes,
                                                                            final boolean initialiseObjects) throws SQLException {
        HashSet<String> entityGuids = new HashSet<>();//ids should be unique
        Queue<JdsEntity> innerObjects = new ConcurrentLinkedQueue<>();//can be multiple copies of the same object however
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String parentEntityGuid = resultSet.getString("ParentEntityGuid");
                String entityGuid = resultSet.getString("ChildEntityGuid");
                long entityId = resultSet.getLong("ChildEntityId");
                for (JdsEntity parentEntity : optimalEntityLookup(jdsEntities, parentEntityGuid)) {
                    try {
                        if (parentEntity.objectArrayProperties.containsKey(entityId)) {
                            SimpleListProperty<JdsEntity> propertyList = parentEntity.objectArrayProperties.get(entityId);
                            Class<? extends JdsEntity> jdsEntityClass = jdsDb.getBoundClass(entityId);
                            JdsEntity jdsEntity = jdsEntityClass.newInstance();
                            //
                            jdsEntity.setEntityGuid(entityGuid);
                            entityGuids.add(entityGuid);
                            propertyList.get().add(jdsEntity);
                            innerObjects.add(jdsEntity);
                        } else if (parentEntity.objectProperties.containsKey(entityId)) {
                            SimpleObjectProperty<JdsEntity> property = parentEntity.objectProperties.get(entityId);
                            Class<? extends JdsEntity> jdsEntityClass = jdsDb.getBoundClass(entityId);
                            JdsEntity jdsEntity = jdsEntityClass.newInstance();
                            //
                            jdsEntity.setEntityGuid(entityGuid);
                            entityGuids.add(entityGuid);
                            property.set(jdsEntity);
                            innerObjects.add(jdsEntity);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace(System.err);
                    }
                }
            }
        }
        List<List<String>> batches = createProcessingBatches(entityGuids);
        batches.stream().forEach(batch -> {
            populateInner(jdsDb, null, innerObjects, initialisePrimitives, initialiseDatesAndTimes, initialiseObjects, batch);
        });
    }

    /**
     * @param jdsEntities
     * @param entityGuid
     * @return
     */
    private <T extends JdsEntity> Collection<T> optimalEntityLookup(final Collection<T> jdsEntities, final String entityGuid) {
        return jdsEntities.parallelStream().filter(entryPredicate -> entryPredicate.getEntityGuid().equals(entityGuid)).collect(Collectors.toList());
    }

    /**
     * @param entityGuids
     * @return
     */
    private List<List<String>> createProcessingBatches(Collection<String> entityGuids) {
        List<List<String>> batches = new ArrayList<>();
        int index = 0;
        int batch = 0;
        for (String entityGuid : entityGuids) {
            if (index == MAX_BATCH_SIZE) {
                batch++;
                index = 0;
            }
            if (index == 0)
                batches.add(new ArrayList<>());
            batches.get(batch).add(entityGuid);
            index++;
        }
        return batches;
    }

    /**
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    private <T extends JdsEntity> void populateOverviews(final Collection<T> jdsEntities, final PreparedStatement preparedStatement) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String entityGuid = resultSet.getString("EntityGuid");
                Timestamp dateCreated = resultSet.getTimestamp("DateCreated");
                Timestamp dateModified = resultSet.getTimestamp("DateModified");
                for (JdsEntity entity : optimalEntityLookup(jdsEntities, entityGuid)) {
                    entity.setDateModified(dateModified.toLocalDateTime());
                    entity.setDateCreated(dateCreated.toLocalDateTime());
                    if (entity instanceof JdsPostLoadListener)
                        ((JdsPostLoadListener) entity).onPostLoad(new OnPostLoadEvent(entity.getEntityGuid()));
                }
            }
        }
    }

    /**
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    private <T extends JdsEntity> void populateDateTimeAndDate(final Collection<T> jdsEntities, final PreparedStatement preparedStatement) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String entityGuid = resultSet.getString("EntityGuid");
                Timestamp value = resultSet.getTimestamp("Value");
                long fieldId = resultSet.getLong("FieldId");
                optimalEntityLookup(jdsEntities, entityGuid).parallelStream().filter(entity -> entity.localDateTimeProperties.containsKey(fieldId)).forEach(entity -> {
                    entity.localDateTimeProperties.get(fieldId).set(value.toLocalDateTime());
                });
                optimalEntityLookup(jdsEntities, entityGuid).parallelStream().filter(entity -> entity.localDateProperties.containsKey(fieldId)).forEach(entity -> {
                    entity.localDateProperties.get(fieldId).set(value.toLocalDateTime().toLocalDate());
                });
            }
        }
    }

    /**
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    private <T extends JdsEntity> void populateDouble(final Collection<T> jdsEntities, final PreparedStatement preparedStatement) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String entityGuid = resultSet.getString("EntityGuid");
                double value = resultSet.getDouble("Value");
                long fieldId = resultSet.getLong("FieldId");
                optimalEntityLookup(jdsEntities, entityGuid).parallelStream().filter(entity -> entity.doubleProperties.containsKey(fieldId)).forEach(entity -> {
                    entity.doubleProperties.get(fieldId).set(value);
                });
            }
        }
    }

    /**
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    private <T extends JdsEntity> void populateBlobs(final Collection<T> jdsEntities, final PreparedStatement preparedStatement) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String entityGuid = resultSet.getString("EntityGuid");
                Blob value = resultSet.getBlob("Value");
                long fieldId = resultSet.getLong("FieldId");
                optimalEntityLookup(jdsEntities, entityGuid).parallelStream().filter(entity -> entity.blobProperties.containsKey(fieldId)).forEach(entity -> {
                    try {
                        entity.blobProperties.get(fieldId).set(value.getBinaryStream());
                    } catch (IOException | SQLException e) {
                        e.printStackTrace(System.err);
                    }
                });
            }
        }
    }

    /**
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    private <T extends JdsEntity> void populateIntegerAndBoolean(final Collection<T> jdsEntities, final PreparedStatement preparedStatement) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String entityGuid = resultSet.getString("EntityGuid");
                int value = resultSet.getInt("Value");
                long fieldId = resultSet.getLong("FieldId");
                optimalEntityLookup(jdsEntities, entityGuid).parallelStream().filter(entity -> entity.integerProperties.containsKey(fieldId)).forEach(entity -> {
                    entity.integerProperties.get(fieldId).set(value);
                });
                optimalEntityLookup(jdsEntities, entityGuid).parallelStream().filter(entity -> entity.booleanProperties.containsKey(fieldId)).forEach(entity -> {
                    entity.booleanProperties.get(fieldId).set(value == 1);
                });
            }
        }
    }

    /**
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    private <T extends JdsEntity> void populateTimes(final Collection<T> jdsEntities, final PreparedStatement preparedStatement) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String entityGuid = resultSet.getString("EntityGuid");
                int value = resultSet.getInt("Value");
                long fieldId = resultSet.getLong("FieldId");
                optimalEntityLookup(jdsEntities, entityGuid).parallelStream().filter(entity -> entity.localTimeProperties.containsKey(fieldId)).forEach(entity -> {
                    LocalTime localTime = LocalTime.ofSecondOfDay(value);
                    entity.localTimeProperties.get(fieldId).set(localTime);
                });
            }
        }
    }

    /**
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    private <T extends JdsEntity> void populateFloat(final Collection<T> jdsEntities, final PreparedStatement preparedStatement) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String entityGuid = resultSet.getString("EntityGuid");
                float value = resultSet.getFloat("Value");
                long fieldId = resultSet.getLong("FieldId");
                optimalEntityLookup(jdsEntities, entityGuid).parallelStream().filter(entity -> entity.floatProperties.containsKey(fieldId)).forEach(entity -> {
                    entity.floatProperties.get(fieldId).set(value);
                });
            }
        }
    }

    /**
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    private <T extends JdsEntity> void populateLong(final Collection<T> jdsEntities, final PreparedStatement preparedStatement) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String entityGuid = resultSet.getString("EntityGuid");
                long value = resultSet.getLong("Value");
                long fieldId = resultSet.getLong("FieldId");
                optimalEntityLookup(jdsEntities, entityGuid).parallelStream().filter(entity -> entity.longProperties.containsKey(fieldId)).forEach(entity -> {
                    entity.longProperties.get(fieldId).set(value);
                });
            }
        }
    }

    /**
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    private <T extends JdsEntity> void populateZonedDateTime(final Collection<T> jdsEntities, final PreparedStatement preparedStatement) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String entityGuid = resultSet.getString("EntityGuid");
                long value = resultSet.getLong("Value");
                long fieldId = resultSet.getLong("FieldId");
                optimalEntityLookup(jdsEntities, entityGuid).parallelStream().filter(entity -> entity.zonedDateTimeProperties.containsKey(fieldId)).forEach(entity -> {
                    Instant instant = Instant.ofEpochSecond(value);
                    ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
                    entity.zonedDateTimeProperties.get(fieldId).set(zonedDateTime);
                });
            }
        }
    }

    /**
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    private <T extends JdsEntity> void populateText(final Collection<T> jdsEntities, final PreparedStatement preparedStatement) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String entityGuid = resultSet.getString("EntityGuid");
                String value = resultSet.getString("Value");
                long fieldId = resultSet.getLong("FieldId");
                optimalEntityLookup(jdsEntities, entityGuid).parallelStream().filter(entity -> entity.stringProperties.containsKey(fieldId)).forEach(entity -> {
                    entity.stringProperties.get(fieldId).set(value);
                });
            }
        }
    }

    /**
     * @param jdsDataBase
     * @param batchSize
     * @param entityId
     * @param allBatches
     * @param suppliedEntityGuids
     */
    private void prepareActionBatches(final JdsDb jdsDataBase, final int batchSize, final long entityId, final List<List<String>> allBatches, final String[] suppliedEntityGuids) throws SQLException, ClassNotFoundException {
        int batchIndex = 0;
        int batchContents = 0;

        List<Long> entityAndChildren = new ArrayList<>();
        entityAndChildren.add(entityId);

        try (Connection connection = jdsDataBase.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT ChildEntityCode FROM JdsRefEntityInheritance WHERE ParentEntityCode = ?")) {
                preparedStatement.setLong(1, entityAndChildren.get(0));
                try (ResultSet rs = preparedStatement.executeQuery()) {
                    while (rs.next()) {
                        entityAndChildren.add(rs.getLong("ChildEntityCode"));
                    }
                }
            }

            StringJoiner entityHeirarchy = new StringJoiner(",");
            for (Long id : entityAndChildren)
                entityHeirarchy.add(id + "");

            //if no ids supplied we are looking for all instances of the entity
            String rawSql = "SELECT DISTINCT EntityGuid FROM JdsStoreEntityInheritance WHERE EntityId IN (%s)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(String.format(rawSql, entityHeirarchy))) {
                if (suppliedEntityGuids.length == 0) {
                    try (ResultSet rs = preparedStatement.executeQuery()) {
                        while (rs.next()) {
                            if (batchContents == batchSize) {
                                batchIndex++;
                                batchContents = 0;
                            }
                            if (batchContents == 0)
                                allBatches.add(new ArrayList<>());
                            allBatches.get(batchIndex).add(rs.getString("EntityGuid"));
                            batchContents++;
                        }
                    }
                } else {
                    for (String EntityGuid : suppliedEntityGuids) {
                        if (batchContents == batchSize) {
                            batchIndex++;
                            batchContents = 0;
                        }
                        if (batchContents == 0)
                            allBatches.add(new ArrayList<>());
                        allBatches.get(batchIndex).add(EntityGuid);
                        batchContents++;
                    }
                }
            }
        }
    }

    /**
     * @param size
     * @return
     */
    private String getQuestions(final int size) {
        String[] questionArray = new String[size];
        for (int index = 0; index < size; index++) {
            questionArray[index] = "?";
        }
        return String.join(",", questionArray);
    }

    /**
     * @param preparedStatement
     * @param index
     * @param entityGuid
     * @throws SQLException
     */
    private void setParameterForStatement(final int index, final String entityGuid, final PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(index, entityGuid);
    }

    @Override
    public List<T> call() throws Exception {
        JdsEntityAnnotation annotation = referenceType.getAnnotation(JdsEntityAnnotation.class);
        long entityId = annotation.entityId();
        List<List<String>> allBatches = new ArrayList<>(new ArrayList<>());
        List<T> castCollection = collections;
        prepareActionBatches(jdsDb, MAX_BATCH_SIZE, entityId, allBatches, entityGuids);
        boolean initialisePrimitives = true;
        boolean initialiseDatesAndTimes = true;
        boolean initialiseObjects = true;
        for (List<String> currentBatch : allBatches) {
            populateInner(jdsDb, referenceType, castCollection, initialisePrimitives, initialiseDatesAndTimes, initialiseObjects, currentBatch);
        }
        if (comparator != null)
            collections.sort(comparator);
        return collections;
    }

    /**
     * @param jdsDb
     * @param referenceType
     * @param comparator
     * @param entityGuids
     * @param <T>
     * @return
     * @deprecated please refer to <a href="https://github.com/SubiyaCryolite/Jenesis-Data-Store"> the readme</a> for the most up to date CRUD approach
     */
    public static <T extends JdsEntity> List<T> load(final JdsDb jdsDb, final Class<T> referenceType, Comparator<T> comparator, final String... entityGuids) throws Exception {
        List<T> collections = new JdsLoad(jdsDb, referenceType, comparator, entityGuids).call();
        return collections;
    }

    /**
     * @param jdsDb
     * @param referenceType
     * @param entityGuids
     * @param <T>
     * @return
     * @deprecated please refer to <a href="https://github.com/SubiyaCryolite/Jenesis-Data-Store"> the readme</a> for the most up to date CRUD approach
     */
    public static <T extends JdsEntity> List<T> load(final JdsDb jdsDb, final Class<T> referenceType, final String... entityGuids) throws Exception {
        List<T> collections = new JdsLoad(jdsDb, referenceType, entityGuids).call();
        return collections;
    }
}
