package org.jenesis.jds;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.jenesis.jds.annotations.JdsEntityAnnotation;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by ifung on 14/02/2017.
 */
public class JdsLoad {

    final private static int batchSize = 1000; //Java supports up to 1000 prepared statements depending on the driver

    public static <T extends JdsEntity> List<T> load(final JdsDatabase jdsDatabase, final Class<T> referenceType, final String... suppliedEntityGuids) {
        JdsEntityAnnotation annotation = referenceType.getAnnotation(JdsEntityAnnotation.class);
        long code = annotation.entityCode();
        List<T> collections = new ArrayList<>();
        List<List<String>> allBatches = new ArrayList<>(new ArrayList<>());
        List<JdsEntity> castCollection = (List<JdsEntity>) collections;
        prepareActionBatches(jdsDatabase, batchSize, code, allBatches, suppliedEntityGuids);

        boolean initialiseInnerContent = true;
        for (List<String> currentBatch : allBatches) {
            populateInner(jdsDatabase, referenceType, castCollection, initialiseInnerContent, currentBatch);
        }
        return collections;
    }

    private static <T extends JdsEntity> void populateInner(final JdsDatabase jdsDatabase, Class<T> referenceType, final Collection<JdsEntity> jdsEntities, final boolean initialiseInnerContent, final Collection<String> entityEntityGuids) {
        String questionsString = getQuestions(entityEntityGuids.size());
        //primitives
        String sqlTextValues = String.format("SELECT EntityGuid, Value, FieldId FROM JdsStoreText WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        String sqlLongValues = String.format("SELECT EntityGuid, Value, FieldId FROM JdsStoreLong WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        String sqlIntegerValues = String.format("SELECT EntityGuid, Value, FieldId FROM JdsStoreInteger WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        String sqlFloatValues = String.format("SELECT EntityGuid, Value, FieldId FROM JdsStoreFloat WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        String sqlDoubleValues = String.format("SELECT EntityGuid, Value, FieldId FROM JdsStoreDouble WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        String sqlDateTimeValues = String.format("SELECT EntityGuid, Value, FieldId FROM JdsStoreDateTime WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        //array
        String sqlTextArrayValues = String.format("SELECT EntityGuid, Value, FieldId, Sequence FROM JdsStoreTextArray WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        String sqlIntegerArrayAndEnumValues = String.format("SELECT EntityGuid, Value, FieldId, Sequence FROM JdsStoreIntegerArray WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        String sqlLongArrayValues = String.format("SELECT EntityGuid, Value, FieldId, Sequence FROM JdsStoreLongArray WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        String sqlFloatArrayValues = String.format("SELECT EntityGuid, Value, FieldId, Sequence FROM JdsStoreFloatArray WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        String sqlDoubleArrayValues = String.format("SELECT EntityGuid, Value, FieldId, Sequence FROM JdsStoreDoubleArray WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        String sqlDateTimeArrayValues = String.format("SELECT EntityGuid, Value, FieldId, Sequence FROM JdsStoreDateTimeArray WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        String sqlEmbeddedAndArrayObjects = String.format("SELECT EntityGuid, SubEntityGuid, EntityId FROM JdsStoreEntitySubclass WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        //overviews
        String sqlOverviews = String.format("SELECT EntityGuid, DateCreated, DateModified, EntityId FROM JdsRefEntityOverview WHERE EntityGuid IN (%s) ORDER BY EntityGuid", questionsString);
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement strings = connection.prepareStatement(sqlTextValues);
             PreparedStatement longs = connection.prepareStatement(sqlLongValues);
             PreparedStatement integers = connection.prepareStatement(sqlIntegerValues);
             PreparedStatement floats = connection.prepareStatement(sqlFloatValues);
             PreparedStatement doubles = connection.prepareStatement(sqlDoubleValues);
             PreparedStatement dateTimes = connection.prepareStatement(sqlDateTimeValues);
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
            populateInteger(jdsEntities, integers);
            populateFloat(jdsEntities, floats);
            populateDouble(jdsEntities, doubles);
            populateDateTime(jdsEntities, dateTimes);

            //integer arrays and enums
            populateIntegerArraysAndEnums(jdsEntities, integerArraysAndEnums);
            populateFloatArrays(jdsEntities, floatArrays);
            populateLongArrays(jdsEntities, longArrays);
            populateStringArrays(jdsEntities, textArrays);
            populateDoubleArrays(jdsEntities, doubleArrays);
            populateDateTimeArrays(jdsEntities, dateTimeArrays);
            //objects
            populateObjectEntriesAndObjectArrays(jdsDatabase, jdsEntities, embeddedAndArrayObjects);
            populateOverviews(jdsEntities, overviews);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    private static void populateFloatArrays(Collection<JdsEntity> jdsEntities, PreparedStatement preparedStatement) throws SQLException {
        JdsEntity currentEntity = null;
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String EntityGuid = resultSet.getString("EntityGuid");
            float value = resultSet.getFloat("Value");
            long fieldId = resultSet.getLong("FieldId");
            currentEntity = optimalEntityLookup(jdsEntities, currentEntity, EntityGuid);
            if (currentEntity == null) continue;
            if (currentEntity.floatArrayProperties.containsKey(fieldId)) {
                SimpleListProperty<Float> property = currentEntity.floatArrayProperties.get(fieldId);
                property.add(value);
            }
        }
    }

    private static void populateDoubleArrays(Collection<JdsEntity> jdsEntities, PreparedStatement preparedStatement) throws SQLException {
        JdsEntity currentEntity = null;
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String EntityGuid = resultSet.getString("EntityGuid");
            double value = resultSet.getDouble("Value");
            long fieldId = resultSet.getLong("FieldId");
            currentEntity = optimalEntityLookup(jdsEntities, currentEntity, EntityGuid);
            if (currentEntity == null) continue;
            if (currentEntity.doubleArrayProperties.containsKey(fieldId)) {
                SimpleListProperty<Double> property = currentEntity.doubleArrayProperties.get(fieldId);
                property.add(value);
            }
        }
    }

    private static void populateLongArrays(Collection<JdsEntity> jdsEntities, PreparedStatement preparedStatement) throws SQLException {
        JdsEntity currentEntity = null;
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String EntityGuid = resultSet.getString("EntityGuid");
            long value = resultSet.getLong("Value");
            long fieldId = resultSet.getLong("FieldId");
            currentEntity = optimalEntityLookup(jdsEntities, currentEntity, EntityGuid);
            if (currentEntity == null) continue;
            if (currentEntity.longArrayProperties.containsKey(fieldId)) {
                SimpleListProperty<Long> property = currentEntity.longArrayProperties.get(fieldId);
                property.add(value);
            }
        }
    }

    private static void populateDateTimeArrays(Collection<JdsEntity> jdsEntities, PreparedStatement preparedStatement) throws SQLException {
        JdsEntity currentEntity = null;
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String EntityGuid = resultSet.getString("EntityGuid");
            Timestamp value = resultSet.getTimestamp("Value");
            long fieldId = resultSet.getLong("FieldId");
            currentEntity = optimalEntityLookup(jdsEntities, currentEntity, EntityGuid);
            if (currentEntity == null) continue;
            if (currentEntity.dateTimeArrayProperties.containsKey(fieldId)) {
                SimpleListProperty<LocalDateTime> property = currentEntity.dateTimeArrayProperties.get(fieldId);
                property.add(value.toLocalDateTime());
            }
        }
    }

    private static void populateStringArrays(Collection<JdsEntity> jdsEntities, PreparedStatement preparedStatement) throws SQLException {
        JdsEntity currentEntity = null;
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String EntityGuid = resultSet.getString("EntityGuid");
            String value = resultSet.getString("Value");
            long fieldId = resultSet.getLong("FieldId");
            currentEntity = optimalEntityLookup(jdsEntities, currentEntity, EntityGuid);
            if (currentEntity == null) continue;
            if (currentEntity.stringArrayProperties.containsKey(fieldId)) {
                SimpleListProperty<String> property = currentEntity.stringArrayProperties.get(fieldId);
                property.add(value);
            }
        }
    }

    private static void populateIntegerArraysAndEnums(Collection<JdsEntity> jdsEntities, PreparedStatement preparedStatement) throws SQLException {
        JdsEntity currentEntity = null;
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String EntityGuid = resultSet.getString("EntityGuid");
            int value = resultSet.getInt("Value");
            long fieldId = resultSet.getLong("FieldId");
            currentEntity = optimalEntityLookup(jdsEntities, currentEntity, EntityGuid);
            if (currentEntity == null) continue;
            if (currentEntity.integerArrayProperties.containsKey(fieldId)) {
                SimpleListProperty<Integer> property = currentEntity.integerArrayProperties.get(fieldId);
                property.add(value);
            } else {
                Optional<JdsFieldEnum> fx = currentEntity.enumProperties.keySet().stream().filter(entry -> entry.getField().getId() == fieldId).findAny();
                if (fx.isPresent()) {
                    JdsFieldEnum fe = fx.get();
                    SimpleListProperty<String> property = currentEntity.enumProperties.get(fe);
                    property.add(fe.getValue(value));
                }
            }
        }
    }

    private static void populateObjectEntriesAndObjectArrays(JdsDatabase jdsDatabase, Collection<JdsEntity> jdsEntities, PreparedStatement preparedStatement) throws SQLException {
        Queue<String> innerEntityGuids = new ConcurrentLinkedQueue<>();
        Queue<JdsEntity> innerObjects = new ConcurrentLinkedQueue<>();
        JdsEntity currentEntity = null;
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String EntityGuid = resultSet.getString("EntityGuid");
            String subEntityGuid = resultSet.getString("SubEntityGuid");
            long entityId = resultSet.getLong("EntityId");
            currentEntity = optimalEntityLookup(jdsEntities, currentEntity, EntityGuid);
            if (currentEntity == null) continue;
            try {
                if (currentEntity.objectArrayProperties.containsKey(entityId)) {
                    SimpleListProperty<JdsEntity> propertyList = (SimpleListProperty<JdsEntity>) currentEntity.objectArrayProperties.get(entityId);
                    Class<JdsEntity> jdsEntityClass = JdsEntityClasses.getBoundClass(entityId);
                    JdsEntity action = jdsEntityClass.newInstance();
                    //
                    action.setEntityGuid(subEntityGuid);
                    innerEntityGuids.add(subEntityGuid);
                    propertyList.get().add(action);
                    innerObjects.add(action);
                } else if (currentEntity.objectProperties.containsKey(entityId)) {
                    SimpleObjectProperty<JdsEntity> property = ((SimpleObjectProperty<JdsEntity>) currentEntity.objectProperties.get(entityId));
                    Class<JdsEntity> jdsEntityClass = JdsEntityClasses.getBoundClass(entityId);
                    JdsEntity action = jdsEntityClass.newInstance();
                    //
                    action.setEntityGuid(subEntityGuid);
                    innerEntityGuids.add(subEntityGuid);
                    property.set(action);
                    innerObjects.add(action);
                }
            } catch (Exception ex) {
                ex.printStackTrace(System.err);
            }
        }
        List<List<String>> batches = createProcessingBatches(innerEntityGuids);
        batches.parallelStream().forEach(batch -> {
            populateInner(jdsDatabase, null, innerObjects, false, batch);
            jdsDatabase.toString();
        });
    }

    private static JdsEntity optimalEntityLookup(final Collection<JdsEntity> jdsEntities, JdsEntity currentEntity, final String EntityGuid) {
        if (currentEntity == null || !currentEntity.getEntityGuid().equals(EntityGuid)) {
            Optional<JdsEntity> optional = jdsEntities.parallelStream().filter(ent -> ent.getEntityGuid().equals(EntityGuid)).findAny();
            if (optional.isPresent())
                currentEntity = optional.get();
        }
        return currentEntity;
    }

    private static List<List<String>> createProcessingBatches(Queue<String> innerEntityGuids) {
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

    private static void populateOverviews(final Collection<JdsEntity> jdsEntities, final PreparedStatement preparedStatement) throws SQLException {
        JdsEntity currentEntity = null;
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String EntityGuid = resultSet.getString("EntityGuid");
            Timestamp dateCreated = resultSet.getTimestamp("DateCreated");
            Timestamp dateModified = resultSet.getTimestamp("DateModified");
            currentEntity = optimalEntityLookup(jdsEntities, currentEntity, EntityGuid);
            if (currentEntity == null) continue;
            currentEntity.setDateModified(dateModified.toLocalDateTime());
            currentEntity.setDateCreated(dateCreated.toLocalDateTime());
        }
    }

    private static void populateDateTime(final Collection<JdsEntity> jdsEntities, final PreparedStatement preparedStatement) throws SQLException {
        JdsEntity currentEntity = null;
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String EntityGuid = resultSet.getString("EntityGuid");
            Timestamp value = resultSet.getTimestamp("Value");
            long fieldId = resultSet.getLong("FieldId");
            currentEntity = optimalEntityLookup(jdsEntities, currentEntity, EntityGuid);
            if (currentEntity == null) continue;
            if (currentEntity.dateProperties.containsKey(fieldId))
                currentEntity.dateProperties.get(fieldId).set(value.toLocalDateTime());
        }
    }

    private static void populateDouble(final Collection<JdsEntity> jdsEntities, final PreparedStatement preparedStatement) throws SQLException {
        JdsEntity currentEntity = null;
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String EntityGuid = resultSet.getString("EntityGuid");
            double value = resultSet.getDouble("Value");
            long fieldId = resultSet.getLong("FieldId");
            currentEntity = optimalEntityLookup(jdsEntities, currentEntity, EntityGuid);
            if (currentEntity == null) continue;
            if (currentEntity.doubleProperties.containsKey(fieldId))
                currentEntity.doubleProperties.get(fieldId).set(value);
        }
    }

    private static void populateInteger(final Collection<JdsEntity> jdsEntities, final PreparedStatement preparedStatement) throws SQLException {
        JdsEntity currentEntity = null;
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String EntityGuid = resultSet.getString("EntityGuid");
            int value = resultSet.getInt("Value");
            long fieldId = resultSet.getLong("FieldId");
            currentEntity = optimalEntityLookup(jdsEntities, currentEntity, EntityGuid);
            if (currentEntity == null) continue;
            if (currentEntity.integerProperties.containsKey(fieldId))
                currentEntity.integerProperties.get(fieldId).set(value);

        }
    }

    private static void populateFloat(final Collection<JdsEntity> jdsEntities, final PreparedStatement preparedStatement) throws SQLException {
        JdsEntity currentEntity = null;
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String EntityGuid = resultSet.getString("EntityGuid");
            float value = resultSet.getFloat("Value");
            long fieldId = resultSet.getLong("FieldId");
            currentEntity = optimalEntityLookup(jdsEntities, currentEntity, EntityGuid);
            if (currentEntity == null) continue;
            if (currentEntity.floatProperties.containsKey(fieldId))
                currentEntity.floatProperties.get(fieldId).set(value);

        }
    }

    private static void populateLong(final Collection<JdsEntity> jdsEntities, final PreparedStatement preparedStatement) throws SQLException {
        JdsEntity currentEntity = null;
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String EntityGuid = resultSet.getString("EntityGuid");
            long value = resultSet.getLong("Value");
            long fieldId = resultSet.getLong("FieldId");
            currentEntity = optimalEntityLookup(jdsEntities, currentEntity, EntityGuid);
            if (currentEntity == null) continue;
            if (currentEntity.longProperties.containsKey(fieldId))
                currentEntity.longProperties.get(fieldId).set(value);
        }
    }

    private static void populateText(final Collection<JdsEntity> jdsEntities, final PreparedStatement preparedStatement) throws SQLException {
        JdsEntity currentEntity = null;
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String EntityGuid = resultSet.getString("EntityGuid");
            String value = resultSet.getString("Value");
            long fieldId = resultSet.getLong("FieldId");
            currentEntity = optimalEntityLookup(jdsEntities, currentEntity, EntityGuid);
            if (currentEntity == null) continue;
            if (currentEntity.stringProperties.containsKey(fieldId))
                currentEntity.stringProperties.get(fieldId).set(value);

        }
        resultSet.close();
    }

    private static void prepareActionBatches(final JdsDatabase jdsDatabase, final int batchSize, final long code, final List<List<String>> allBatches, final String[] suppliedEntityGuids) {
        int batchIndex = 0;
        int batchContents = 0;
        //if no ids supplied we are looking for all instances of the entity
        String sql1 = "SELECT EntityGuid FROM JdsRefEntityOverview WHERE EntityId = ?";
        try (Connection connection = jdsDatabase.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql1)) {
            if (suppliedEntityGuids.length == 0) {
                preparedStatement.setLong(1, code);
                ResultSet rs = preparedStatement.executeQuery();
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

    private static String getQuestions(final int size) {
        String[] questionArray = new String[size];
        for (int index = 0; index < size; index++) {
            questionArray[index] = "?";
        }
        return String.join(",", questionArray);
    }

    private static void setParameterForStatement(final PreparedStatement textStatement, final int dex, final String EntityGuid) throws SQLException {
        textStatement.setString(dex, EntityGuid);
    }
}
