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
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * This class is responsible for loading an {@link JdsEntity entities} {@link JdsField fields}
 */
public class JdsLoad {

    /**
     * Java supports up to 1000 prepared supportsStatements depending on the driver
     */
    final private static int batchSize = 1000;

    /**
     * @param jdsDataBase
     * @param referenceType
     * @param comparator
     * @param suppliedEntityGuids
     * @param <T>
     * @return
     */
    public static <T extends JdsEntity> List<T> load(final JdsDb jdsDataBase, final Class<T> referenceType, Comparator<T> comparator, final String... suppliedEntityGuids) {
        List<T> collections = new ArrayList<>();
        loadImplementation(jdsDataBase, referenceType, (List<JdsEntity>) collections, suppliedEntityGuids);
        collections.sort(comparator);
        return collections;
    }

    /**
     * @param jdsDataBase
     * @param referenceType
     * @param suppliedEntityGuids
     * @param <T>
     * @return
     */
    public static <T extends JdsEntity> List<T> load(final JdsDb jdsDataBase, final Class<T> referenceType, final String... suppliedEntityGuids) {
        List<T> collections = new ArrayList<>();
        loadImplementation(jdsDataBase, referenceType, (List<JdsEntity>) collections, suppliedEntityGuids);
        return collections;
    }

    /**
     * @param jdsDataBase
     * @param referenceType
     * @param collections
     * @param suppliedEntityGuids
     * @param <T>
     */
    private static <T extends JdsEntity> void loadImplementation(JdsDb jdsDataBase, Class<T> referenceType, List<JdsEntity> collections, String[] suppliedEntityGuids) {
        JdsEntityAnnotation annotation = referenceType.getAnnotation(JdsEntityAnnotation.class);
        long code = annotation.entityId();
        List<List<String>> allBatches = new ArrayList<>(new ArrayList<>());
        List<JdsEntity> castCollection = collections;
        prepareActionBatches(jdsDataBase, batchSize, code, allBatches, suppliedEntityGuids);

        boolean initialiseInnerContent = true;
        for (List<String> currentBatch : allBatches) {
            populateInner(jdsDataBase, referenceType, castCollection, initialiseInnerContent, currentBatch);
        }
    }

    /**
     * @param jdsDataBase
     * @param referenceType
     * @param jdsEntities
     * @param initialiseInnerContent
     * @param entityEntityGuids
     * @param <T>
     */
    private static <T extends JdsEntity> void populateInner(final JdsDb jdsDataBase, Class<T> referenceType, final Collection<JdsEntity> jdsEntities, final boolean initialiseInnerContent, final Collection<String> entityEntityGuids) {
        String questionsString = getQuestions(entityEntityGuids.size());
        //primitives
        String sqlTextValues = String.format("SELECT EntityGuid, Value, FieldId FROM JdsStoreText WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        String sqlLongValues = String.format("SELECT EntityGuid, Value, FieldId FROM JdsStoreLong WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        String sqlIntegerValues = String.format("SELECT EntityGuid, Value, FieldId FROM JdsStoreInteger WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        String sqlFloatValues = String.format("SELECT EntityGuid, Value, FieldId FROM JdsStoreFloat WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        String sqlDoubleValues = String.format("SELECT EntityGuid, Value, FieldId FROM JdsStoreDouble WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        String sqlDateTimeValues = String.format("SELECT EntityGuid, Value, FieldId FROM JdsStoreDateTime WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        String sqlTimeValues = String.format("SELECT EntityGuid, Value, FieldId FROM JdsStoreTime WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        String sqlZonedDateTimeValues = String.format("SELECT EntityGuid, Value, FieldId FROM JdsStoreZonedDateTime WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
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
        try (Connection connection = jdsDataBase.getConnection();
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
            int index = 1;
            for (String EntityGuid : entityEntityGuids) {
                if (initialiseInnerContent) {
                    JdsEntity instance = referenceType.newInstance();
                    instance.setEntityGuid(EntityGuid);
                    jdsEntities.add(instance);
                }
                //primitives
                setParameterForStatement(strings, index, EntityGuid);
                setParameterForStatement(integers, index, EntityGuid);
                setParameterForStatement(longs, index, EntityGuid);
                setParameterForStatement(floats, index, EntityGuid);
                setParameterForStatement(doubles, index, EntityGuid);
                setParameterForStatement(dateTimes, index, EntityGuid);
                setParameterForStatement(times, index, EntityGuid);
                setParameterForStatement(zonedDateTimes, index, EntityGuid);
                //array
                setParameterForStatement(textArrays, index, EntityGuid);
                setParameterForStatement(longArrays, index, EntityGuid);
                setParameterForStatement(floatArrays, index, EntityGuid);
                setParameterForStatement(doubleArrays, index, EntityGuid);
                setParameterForStatement(dateTimeArrays, index, EntityGuid);
                setParameterForStatement(integerArraysAndEnums, index, EntityGuid);
                //object and object arrays
                setParameterForStatement(embeddedAndArrayObjects, index, EntityGuid);
                //overview
                setParameterForStatement(overviews, index, EntityGuid);
                index++;
            }
            //primitives
            populateText(jdsEntities, strings);
            populateLong(jdsEntities, longs);
            populateZonedDateTime(jdsEntities, zonedDateTimes);
            populateIntegerAndBoolean(jdsEntities, integers);
            populateFloat(jdsEntities, floats);
            populateDouble(jdsEntities, doubles);
            populateDateTimeAndDate(jdsEntities, dateTimes);
            populateTimes(jdsEntities, times);

            //integer arrays and enums
            populateIntegerArraysAndEnums(jdsEntities, integerArraysAndEnums);
            populateFloatArrays(jdsEntities, floatArrays);
            populateLongArrays(jdsEntities, longArrays);
            populateStringArrays(jdsEntities, textArrays);
            populateDoubleArrays(jdsEntities, doubleArrays);
            populateDateTimeArrays(jdsEntities, dateTimeArrays);
            //objects
            populateObjectEntriesAndObjectArrays(jdsDataBase, jdsEntities, embeddedAndArrayObjects);
            populateOverviews(jdsEntities, overviews);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    private static void populateFloatArrays(Collection<JdsEntity> jdsEntities, PreparedStatement preparedStatement) throws SQLException {
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
    private static void populateDoubleArrays(Collection<JdsEntity> jdsEntities, PreparedStatement preparedStatement) throws SQLException {
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
    private static void populateLongArrays(Collection<JdsEntity> jdsEntities, PreparedStatement preparedStatement) throws SQLException {
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
    private static void populateDateTimeArrays(Collection<JdsEntity> jdsEntities, PreparedStatement preparedStatement) throws SQLException {
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
    private static void populateStringArrays(Collection<JdsEntity> jdsEntities, PreparedStatement preparedStatement) throws SQLException {
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
    private static void populateIntegerArraysAndEnums(Collection<JdsEntity> jdsEntities, PreparedStatement preparedStatement) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String entityGuid = resultSet.getString("EntityGuid");
                int value = resultSet.getInt("Value");
                long fieldId = resultSet.getLong("FieldId");
                for (JdsEntity jdsEntity : optimalEntityLookup(jdsEntities, entityGuid)) {
                    if (jdsEntity.integerArrayProperties.containsKey(fieldId)) {
                        SimpleListProperty<Integer> property = jdsEntity.integerArrayProperties.get(fieldId);
                        property.add(value);
                    } else {
                        Optional<JdsFieldEnum> fieldEnum = jdsEntity.enumProperties.keySet().stream().filter(entry -> entry.getField().getId() == fieldId).findAny();
                        if (fieldEnum.isPresent()) {
                            JdsFieldEnum jdsFieldEnum = fieldEnum.get();
                            SimpleListProperty<String> property = jdsEntity.enumProperties.get(jdsFieldEnum);
                            property.add(jdsFieldEnum.getValue(value));
                        }
                    }
                }
            }
        }
    }

    /**
     * @param jdsDataBase
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    private static void populateObjectEntriesAndObjectArrays(JdsDb jdsDataBase, Collection<JdsEntity> jdsEntities, PreparedStatement preparedStatement) throws SQLException {
        HashSet<String> innerEntityGuids = new HashSet<>();//ids should be unique
        Queue<JdsEntity> innerObjects = new ConcurrentLinkedQueue<>();//can be multiple copies of the same object however
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String parentEntityGuid = resultSet.getString("ParentEntityGuid");
                String entityGuid = resultSet.getString("ChildEntityGuid");
                long entityId = resultSet.getLong("ChildEntityId");
                for (JdsEntity parentEntity : optimalEntityLookup(jdsEntities, parentEntityGuid)) {
                    try {
                        if (parentEntity.objectArrayProperties.containsKey(entityId)) {
                            SimpleListProperty<JdsEntity> propertyList = (SimpleListProperty<JdsEntity>) parentEntity.objectArrayProperties.get(entityId);
                            Class<JdsEntity> jdsEntityClass = JdsEntityClasses.getBoundClass(entityId);
                            JdsEntity jdsEntity = jdsEntityClass.newInstance();
                            //
                            jdsEntity.setEntityGuid(entityGuid);
                            innerEntityGuids.add(entityGuid);
                            propertyList.get().add(jdsEntity);
                            innerObjects.add(jdsEntity);
                        } else if (parentEntity.objectProperties.containsKey(entityId)) {
                            SimpleObjectProperty<JdsEntity> property = ((SimpleObjectProperty<JdsEntity>) parentEntity.objectProperties.get(entityId));
                            Class<JdsEntity> jdsEntityClass = JdsEntityClasses.getBoundClass(entityId);
                            JdsEntity jdsEntity = jdsEntityClass.newInstance();
                            //
                            jdsEntity.setEntityGuid(entityGuid);
                            innerEntityGuids.add(entityGuid);
                            property.set(jdsEntity);
                            innerObjects.add(jdsEntity);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace(System.err);
                    }
                }
            }
        }
        List<List<String>> batches = createProcessingBatches(innerEntityGuids);
        batches.stream().forEach(batch -> {
            populateInner(jdsDataBase, null, innerObjects, false, batch);
        });
    }

    /**
     * @param jdsEntities
     * @param entityGuid
     * @return
     */
    private static Collection<JdsEntity> optimalEntityLookup(final Collection<JdsEntity> jdsEntities, final String entityGuid) {
        return jdsEntities.parallelStream().filter(entryPredicate -> entryPredicate.getEntityGuid().equals(entityGuid)).collect(Collectors.toList());
    }

    /**
     * @param innerEntityGuids
     * @return
     */
    private static List<List<String>> createProcessingBatches(Collection<String> innerEntityGuids) {
        List<List<String>> batches = new ArrayList<>();
        int index = 0;
        int batch = 0;
        for (String val : innerEntityGuids) {
            if (index == batchSize) {
                batch++;
                index = 0;
            }
            if (index == 0)
                batches.add(new ArrayList<>());
            batches.get(batch).add(val);
            index++;
        }
        return batches;
    }

    /**
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    private static void populateOverviews(final Collection<JdsEntity> jdsEntities, final PreparedStatement preparedStatement) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String entityGuid = resultSet.getString("EntityGuid");
                Timestamp dateCreated = resultSet.getTimestamp("DateCreated");
                Timestamp dateModified = resultSet.getTimestamp("DateModified");
                for (JdsEntity entity : optimalEntityLookup(jdsEntities, entityGuid)) {
                    entity.setDateModified(dateModified.toLocalDateTime());
                    entity.setDateCreated(dateCreated.toLocalDateTime());
                }
            }
        }
    }

    /**
     * @param jdsEntities
     * @param preparedStatement
     * @throws SQLException
     */
    private static void populateDateTimeAndDate(final Collection<JdsEntity> jdsEntities, final PreparedStatement preparedStatement) throws SQLException {
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
    private static void populateDouble(final Collection<JdsEntity> jdsEntities, final PreparedStatement preparedStatement) throws SQLException {
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
    private static void populateIntegerAndBoolean(final Collection<JdsEntity> jdsEntities, final PreparedStatement preparedStatement) throws SQLException {
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
    private static void populateTimes(final Collection<JdsEntity> jdsEntities, final PreparedStatement preparedStatement) throws SQLException {
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
    private static void populateFloat(final Collection<JdsEntity> jdsEntities, final PreparedStatement preparedStatement) throws SQLException {
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
    private static void populateLong(final Collection<JdsEntity> jdsEntities, final PreparedStatement preparedStatement) throws SQLException {
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
    private static void populateZonedDateTime(final Collection<JdsEntity> jdsEntities, final PreparedStatement preparedStatement) throws SQLException {
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
    private static void populateText(final Collection<JdsEntity> jdsEntities, final PreparedStatement preparedStatement) throws SQLException {
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
     * @param code
     * @param allBatches
     * @param suppliedEntityGuids
     */
    private static void prepareActionBatches(final JdsDb jdsDataBase, final int batchSize, final long code, final List<List<String>> allBatches, final String[] suppliedEntityGuids) {
        int batchIndex = 0;
        int batchContents = 0;
        //if no ids supplied we are looking for all instances of the entity
        String sql1 = "SELECT EntityGuid FROM JdsStoreEntityOverview WHERE EntityId = ?";
        try (Connection connection = jdsDataBase.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql1)) {
            if (suppliedEntityGuids.length == 0) {
                preparedStatement.setLong(1, code);
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
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param size
     * @return
     */
    private static String getQuestions(final int size) {
        String[] questionArray = new String[size];
        for (int index = 0; index < size; index++) {
            questionArray[index] = "?";
        }
        return String.join(",", questionArray);
    }

    /**
     * @param textStatement
     * @param dex
     * @param entityGuid
     * @throws SQLException
     */
    private static void setParameterForStatement(final PreparedStatement textStatement, final int dex, final String entityGuid) throws SQLException {
        textStatement.setString(dex, entityGuid);
    }
}
