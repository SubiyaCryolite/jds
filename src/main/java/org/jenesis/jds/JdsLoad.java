package org.jenesis.jds;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.jenesis.jds.annotations.JdsEntityAnnotation;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by ifung on 14/02/2017.
 */
public class JdsLoad {

    final private static int batchSize = 1000; //Java supports up to 1000 prepared statements depending on the driver

    public static <T extends JdsEntity> List<T> load(final JdsDatabase jdsDatabase, final Class<T> referenceType, final String... suppliedActionIds) {
        JdsEntityAnnotation annotation = referenceType.getAnnotation(JdsEntityAnnotation.class);
        long code = annotation.entityCode();
        List<T> collections = new ArrayList<>();
        List<List<String>> allBatches = new ArrayList<>(new ArrayList<>());
        List<JdsEntity> castCollection = (List<JdsEntity>) collections;
        prepareActionBatches(jdsDatabase, batchSize, code, allBatches, suppliedActionIds);

        boolean initialiseInnerContent = true;
        for (List<String> currentBatch : allBatches) {
            populateInner(jdsDatabase, referenceType, castCollection, initialiseInnerContent, currentBatch);
        }
        return collections;
    }

    private static <T extends JdsEntity> void populateInner(final JdsDatabase jdsDatabase, Class<T> referenceType, final Collection<JdsEntity> jdsEntities, final boolean initialiseInnerContent, final Collection<String> entityActionIds) {
        String questionsString = getQuestions(entityActionIds.size());
        //primitives
        String sqlTextValues = String.format("SELECT ActionId, Value, FieldId FROM JdsStoreText WHERE ActionId IN (%s) ORDER BY ActionId", questionsString);
        String sqlLongValues = String.format("SELECT ActionId, Value, FieldId FROM JdsStoreLong WHERE ActionId IN (%s) ORDER BY ActionId", questionsString);
        String sqlIntegerValues = String.format("SELECT ActionId, Value, FieldId FROM JdsStoreInteger WHERE ActionId IN (%s) ORDER BY ActionId", questionsString);
        String sqlFloatValues = String.format("SELECT ActionId, Value, FieldId FROM JdsStoreFloat WHERE ActionId IN (%s) ORDER BY ActionId", questionsString);
        String sqlDoubleValues = String.format("SELECT ActionId, Value, FieldId FROM JdsStoreDouble WHERE ActionId IN (%s) ORDER BY ActionId", questionsString);
        String sqlDateTimeValues = String.format("SELECT ActionId, Value, FieldId FROM JdsStoreDateTime WHERE ActionId IN (%s) ORDER BY ActionId", questionsString);
        //array
        String sqlTextArrayValues = String.format("SELECT ActionId, Value, FieldId, Sequence FROM JdsStoreTextArray WHERE ActionId IN (%s) ORDER BY ActionId", questionsString);
        String sqlIntegerArrayValues = String.format("SELECT ActionId, Value, FieldId, Sequence FROM JdsStoreIntegerArray WHERE ActionId IN (%s) ORDER BY ActionId", questionsString);
        String sqlLongArrayValues = String.format("SELECT ActionId, Value, FieldId, Sequence FROM JdsStoreLongArray WHERE ActionId IN (%s) ORDER BY ActionId", questionsString);
        String sqlFloatArrayValues = String.format("SELECT ActionId, Value, FieldId, Sequence FROM JdsStoreFloatArray WHERE ActionId IN (%s) ORDER BY ActionId", questionsString);
        String sqlDoubleArrayValues = String.format("SELECT ActionId, Value, FieldId, Sequence FROM JdsStoreDoubleArray WHERE ActionId IN (%s) ORDER BY ActionId", questionsString);
        String sqlDateTimeArrayValues = String.format("SELECT ActionId, Value, FieldId, Sequence FROM JdsStoreDateTimeArray WHERE ActionId IN (%s) ORDER BY ActionId", questionsString);
        String sqlEmbeddedAndArrayObjects = String.format("SELECT ActionId, SubActionId, EntityId FROM JdsStoreEntitySubclass WHERE ActionId IN (%s) ORDER BY ActionId", questionsString);
        try (Connection connection = jdsDatabase.getConnection();
             PreparedStatement strings = connection.prepareStatement(sqlTextValues);
             PreparedStatement longs = connection.prepareStatement(sqlLongValues);
             PreparedStatement integers = connection.prepareStatement(sqlIntegerValues);
             PreparedStatement floats = connection.prepareStatement(sqlFloatValues);
             PreparedStatement doubles = connection.prepareStatement(sqlDoubleValues);
             PreparedStatement dateTimes = connection.prepareStatement(sqlDateTimeValues);
             PreparedStatement textArrays = connection.prepareStatement(sqlTextArrayValues);
             PreparedStatement integerArrays = connection.prepareStatement(sqlIntegerArrayValues);
             PreparedStatement longArrays = connection.prepareStatement(sqlLongArrayValues);
             PreparedStatement floatArrays = connection.prepareStatement(sqlFloatArrayValues);
             PreparedStatement doubleArrays = connection.prepareStatement(sqlDoubleArrayValues);
             PreparedStatement dateTimeArrays = connection.prepareStatement(sqlDateTimeArrayValues);
             PreparedStatement embeddedAndArrayObjects = connection.prepareStatement(sqlEmbeddedAndArrayObjects)) {
            //work in batches to not break prepared statement
            int index = 1;
            for (String actionId : entityActionIds) {
                if (initialiseInnerContent) {
                    JdsEntity instance = referenceType.newInstance();
                    instance.setActionId(actionId);
                    jdsEntities.add(instance);
                }
                //primitives
                setParameterForStatement(strings, index, actionId);
                setParameterForStatement(integers, index, actionId);
                setParameterForStatement(longs, index, actionId);
                setParameterForStatement(floats, index, actionId);
                setParameterForStatement(doubles, index, actionId);
                setParameterForStatement(dateTimes, index, actionId);
                //array
                setParameterForStatement(textArrays, index, actionId);
                setParameterForStatement(longArrays, index, actionId);
                setParameterForStatement(floatArrays, index, actionId);
                setParameterForStatement(doubleArrays, index, actionId);
                setParameterForStatement(dateTimeArrays, index, actionId);
                //integer array and enums
                setParameterForStatement(integerArrays, index, actionId);
                //object
                setParameterForStatement(embeddedAndArrayObjects, index, actionId);
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
            populateIntegerArraysAndEnums(jdsEntities, integerArrays);
            //other arrays
            //objects
            populateObjectEntriesAndObjectArrays(jdsDatabase, jdsEntities, embeddedAndArrayObjects);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    private static void populateIntegerArraysAndEnums(Collection<JdsEntity> jdsEntities, PreparedStatement preparedStatement) throws SQLException {
        JdsEntity currentEntity = null;
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String actionId = resultSet.getString("ActionId");
            int value = resultSet.getInt("Value");
            long fieldId = resultSet.getLong("FieldId");
            currentEntity = optimalEntityLookup(jdsEntities, currentEntity, actionId);
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
        Queue<String> innerActionIds = new ConcurrentLinkedQueue<>();
        Queue<JdsEntity> innerObjects = new ConcurrentLinkedQueue<>();
        JdsEntity currentEntity = null;
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String actionId = resultSet.getString("ActionId");
            String subActionId = resultSet.getString("SubActionId");
            long entityId = resultSet.getLong("EntityId");
            currentEntity = optimalEntityLookup(jdsEntities, currentEntity, actionId);
            if (currentEntity == null) continue;
            try {
                if (currentEntity.objectArrayProperties.containsKey(entityId)) {
                    SimpleListProperty<JdsEntity> propertyList = (SimpleListProperty<JdsEntity>) currentEntity.objectArrayProperties.get(entityId);
                    Class<JdsEntity> jdsEntityClass = JdsEntityClasses.getBoundClass(entityId);
                    JdsEntity action = jdsEntityClass.newInstance();
                    //
                    action.setActionId(subActionId);
                    innerActionIds.add(subActionId);
                    propertyList.get().add(action);
                    innerObjects.add(action);
                } else if (currentEntity.objectProperties.containsKey(entityId)) {
                    SimpleObjectProperty<JdsEntity> property = ((SimpleObjectProperty<JdsEntity>) currentEntity.objectProperties.get(entityId));
                    Class<JdsEntity> jdsEntityClass = JdsEntityClasses.getBoundClass(entityId);
                    JdsEntity action = jdsEntityClass.newInstance();
                    //
                    action.setActionId(subActionId);
                    innerActionIds.add(subActionId);
                    property.set(action);
                    innerObjects.add(action);
                }
            } catch (Exception ex) {
                ex.printStackTrace(System.err);
            }
        }
        List<List<String>> batches = createProcessingBatches(innerActionIds);
        batches.parallelStream().forEach(batch -> {
            populateInner(jdsDatabase, null, innerObjects, false, batch);
            jdsDatabase.toString();
        });
    }

    private static JdsEntity optimalEntityLookup(final Collection<JdsEntity> jdsEntities, JdsEntity currentEntity, final String actionId) {
        if (currentEntity == null || !currentEntity.getActionId().equals(actionId)) {
            Optional<JdsEntity> optional = jdsEntities.parallelStream().filter(ent -> ent.getActionId().equals(actionId)).findAny();
            if (optional.isPresent())
                currentEntity = optional.get();
        }
        return currentEntity;
    }

    private static List<List<String>> createProcessingBatches(Queue<String> innerActionIds) {
        List<List<String>> batches = new ArrayList<>();
        int index = 0;
        int batch = 0;
        for (String val : innerActionIds) {
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

    private static void populateDateTime(final Collection<JdsEntity> jdsEntities, final PreparedStatement preparedStatement) throws SQLException {
        JdsEntity currentEntity = null;
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String actionId = resultSet.getString("ActionId");
            Timestamp value = resultSet.getTimestamp("Value");
            long fieldId = resultSet.getLong("FieldId");
            currentEntity = optimalEntityLookup(jdsEntities, currentEntity, actionId);
            if (currentEntity == null) continue;
            if (currentEntity.dateProperties.containsKey(fieldId))
                currentEntity.dateProperties.get(fieldId).set(value.toLocalDateTime());
        }
    }

    private static void populateDouble(final Collection<JdsEntity> jdsEntities, final PreparedStatement preparedStatement) throws SQLException {
        JdsEntity currentEntity = null;
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String actionId = resultSet.getString("ActionId");
            double value = resultSet.getDouble("Value");
            long fieldId = resultSet.getLong("FieldId");
            currentEntity = optimalEntityLookup(jdsEntities, currentEntity, actionId);
            if (currentEntity == null) continue;
            if (currentEntity.doubleProperties.containsKey(fieldId))
                currentEntity.doubleProperties.get(fieldId).set(value);
        }
    }

    private static void populateInteger(final Collection<JdsEntity> jdsEntities, final PreparedStatement preparedStatement) throws SQLException {
        JdsEntity currentEntity = null;
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String actionId = resultSet.getString("ActionId");
            int value = resultSet.getInt("Value");
            long fieldId = resultSet.getLong("FieldId");
            currentEntity = optimalEntityLookup(jdsEntities, currentEntity, actionId);
            if (currentEntity == null) continue;
            if (currentEntity.integerProperties.containsKey(fieldId))
                currentEntity.integerProperties.get(fieldId).set(value);

        }
    }

    private static void populateFloat(final Collection<JdsEntity> jdsEntities, final PreparedStatement preparedStatement) throws SQLException {
        JdsEntity currentEntity = null;
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String actionId = resultSet.getString("ActionId");
            float value = resultSet.getFloat("Value");
            long fieldId = resultSet.getLong("FieldId");
            currentEntity = optimalEntityLookup(jdsEntities, currentEntity, actionId);
            if (currentEntity == null) continue;
            if (currentEntity.floatProperties.containsKey(fieldId))
                currentEntity.floatProperties.get(fieldId).set(value);

        }
    }

    private static void populateLong(final Collection<JdsEntity> jdsEntities, final PreparedStatement preparedStatement) throws SQLException {
        JdsEntity currentEntity = null;
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String actionId = resultSet.getString("ActionId");
            long value = resultSet.getLong("Value");
            long fieldId = resultSet.getLong("FieldId");
            currentEntity = optimalEntityLookup(jdsEntities, currentEntity, actionId);
            if (currentEntity == null) continue;
            if (currentEntity.longProperties.containsKey(fieldId))
                currentEntity.longProperties.get(fieldId).set(value);
        }
    }

    private static void populateText(final Collection<JdsEntity> jdsEntities, final PreparedStatement preparedStatement) throws SQLException {
        JdsEntity currentEntity = null;
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String actionId = resultSet.getString("ActionId");
            String value = resultSet.getString("Value");
            long fieldId = resultSet.getLong("FieldId");
            currentEntity = optimalEntityLookup(jdsEntities, currentEntity, actionId);
            if (currentEntity == null) continue;
            if (currentEntity.stringProperties.containsKey(fieldId))
                currentEntity.stringProperties.get(fieldId).set(value);

        }
        resultSet.close();
    }

    private static void prepareActionBatches(final JdsDatabase jdsDatabase, final int batchSize, final long code, final List<List<String>> allBatches, final String[] suppliedActionIds) {
        int batchIndex = 0;
        int batchContents = 0;
        //if no ids supplied we are looking for all instances of the entity
        String sql1 = "SELECT ActionId FROM JdsRefEntityOverview WHERE EntityId = ?";
        try (Connection connection = jdsDatabase.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql1)) {
            if (suppliedActionIds.length == 0) {
                preparedStatement.setLong(1, code);
                ResultSet rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    if (batchContents == batchSize) {
                        batchIndex++;
                        batchContents = 0;
                    }
                    if (batchContents == 0)
                        allBatches.add(new ArrayList<>());
                    allBatches.get(batchIndex).add(rs.getString("ActionId"));
                    batchContents++;
                }

            } else {
                for (String actionId : suppliedActionIds) {
                    if (batchContents == batchSize) {
                        batchIndex++;
                        batchContents = 0;
                    }
                    if (batchContents == 0)
                        allBatches.add(new ArrayList<>());
                    allBatches.get(batchIndex).add(actionId);
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

    private static void setParameterForStatement(final PreparedStatement textStatement, final int dex, final String actionId) throws SQLException {
        textStatement.setString(dex, actionId);
    }
}
