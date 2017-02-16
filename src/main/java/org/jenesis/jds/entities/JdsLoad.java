package org.jenesis.jds.entities;

import org.jenesis.jds.annotations.JdsEntityAnnotation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ifung on 14/02/2017.
 */
public class JdsLoad {

    public static <T extends JdsEntity> List<T> load(final JdsDatabase jdsDatabase, final Class<T> referenceType, final String... suppliedActionIds) {
        final int batchSize = 1000; //Java supports up to 1000 prepared statements depending on the driver
        JdsEntityAnnotation annotation = referenceType.getAnnotation(JdsEntityAnnotation.class);
        long code = annotation.entityCode();
        List<T> collections = new ArrayList<>();
        List<List<String>> allBatches = new ArrayList<>(new ArrayList<>());
        List<JdsEntity> castCollection = (List<JdsEntity>) collections;
        prepareActionBatches(jdsDatabase, batchSize, code, allBatches, suppliedActionIds);
        for (List<String> currentBatch : allBatches) {
            String questionsString = getQuestions(currentBatch.size());
            String sqlTextValues = String.format("SELECT ActionId, Value, FieldId FROM JdsStoreText WHERE ActionId IN (%s) ORDER BY ActionId", questionsString);
            String sqlLongValues = String.format("SELECT ActionId, Value, FieldId FROM JdsStoreLong WHERE ActionId IN (%s) ORDER BY ActionId", questionsString);
            String sqlFloatValues = String.format("SELECT ActionId, Value, FieldId FROM JdsStoreFloat WHERE ActionId IN (%s) ORDER BY ActionId", questionsString);
            String sqlDoubleValues = String.format("SELECT ActionId, Value, FieldId FROM JdsStoreDouble WHERE ActionId IN (%s) ORDER BY ActionId", questionsString);
            String sqlDateTimeValues = String.format("SELECT ActionId, Value, FieldId FROM JdsStoreDateTime WHERE ActionId IN (%s) ORDER BY ActionId", questionsString);
            try (Connection connection = jdsDatabase.getConnection();
                 PreparedStatement textStatement = connection.prepareStatement(sqlTextValues);
                 PreparedStatement longStatement = connection.prepareStatement(sqlLongValues);
                 PreparedStatement floatStatement = connection.prepareStatement(sqlFloatValues);
                 PreparedStatement doubleStatement = connection.prepareStatement(sqlDoubleValues);
                 PreparedStatement dateTimeStatement = connection.prepareStatement(sqlDateTimeValues)) {
                //work in batches to not break prepared statement
                int dex = 1;
                for (String actionId : currentBatch) {
                    JdsEntity instance = referenceType.newInstance();
                    instance.setActionId(actionId);
                    castCollection.add(instance);
                    setParameterForStatement(textStatement, dex, actionId);
                    setParameterForStatement(longStatement, dex, actionId);
                    setParameterForStatement(floatStatement, dex, actionId);
                    setParameterForStatement(doubleStatement, dex, actionId);
                    setParameterForStatement(dateTimeStatement, dex, actionId);
                    dex++;
                }
                populateText(castCollection, textStatement);
                populateLong(castCollection, longStatement);
                populateFloat(castCollection, floatStatement);
                populateDouble(castCollection, doubleStatement);
                populateDateTime(castCollection, dateTimeStatement);
                //populateObject(castCollection, textStatement);

            } catch (Exception ex) {
                ex.printStackTrace(System.err);
            }
        }
        return collections;
    }

    private static void populateDateTime(final List<JdsEntity> castCollection, final PreparedStatement dateTimeStatement) throws SQLException {
        ResultSet resultSet = dateTimeStatement.executeQuery();
        while (resultSet.next()) {
            String actionId = resultSet.getString("ActionId");
            Timestamp value = resultSet.getTimestamp("Value");
            long fieldId = resultSet.getLong("FieldId");
            castCollection.parallelStream().filter(jdsEntity -> jdsEntity.getActionId().equals(actionId)).forEach(jdsEntity -> {
                if (jdsEntity.dateProperties.containsKey(fieldId)) {
                    jdsEntity.dateProperties.get(fieldId).set(value.toLocalDateTime());
                }
            });
        }
    }

    private static void populateDouble(final List<JdsEntity> castCollection, final PreparedStatement doubleStatement) throws SQLException {
        ResultSet resultSet = doubleStatement.executeQuery();
        while (resultSet.next()) {
            String actionId = resultSet.getString("ActionId");
            double value = resultSet.getDouble("Value");
            long fieldId = resultSet.getLong("FieldId");
            castCollection.parallelStream().filter(jdsEntity -> jdsEntity.getActionId().equals(actionId)).forEach(jdsEntity -> {
                if (jdsEntity.doubleProperties.containsKey(fieldId)) {
                    jdsEntity.doubleProperties.get(fieldId).set(value);
                }
            });
        }
    }

    private static void populateFloat(final List<JdsEntity> castCollection, final PreparedStatement floatStatement) throws SQLException {
        ResultSet resultSet = floatStatement.executeQuery();
        while (resultSet.next()) {
            String actionId = resultSet.getString("ActionId");
            float value = resultSet.getFloat("Value");
            long fieldId = resultSet.getLong("FieldId");
            castCollection.parallelStream().filter(jdsEntity -> jdsEntity.getActionId().equals(actionId)).forEach(jdsEntity -> {
                if (jdsEntity.floatProperties.containsKey(fieldId)) {
                    jdsEntity.floatProperties.get(fieldId).set(value);
                }
            });
        }
    }

    private static void populateLong(final List<JdsEntity> castCollection, final PreparedStatement longStatement) throws SQLException {
        ResultSet resultSet = longStatement.executeQuery();
        while (resultSet.next()) {
            String actionId = resultSet.getString("ActionId");
            long value = resultSet.getLong("Value");
            long fieldId = resultSet.getLong("FieldId");
            castCollection.parallelStream().filter(jdsEntity -> jdsEntity.getActionId().equals(actionId)).forEach(jdsEntity -> {
                if (jdsEntity.longProperties.containsKey(fieldId)) {
                    jdsEntity.longProperties.get(fieldId).set(value);
                }
            });
        }
    }

    private static void populateText(final List<JdsEntity> castCollection, final PreparedStatement textStatement) throws SQLException {
        ResultSet resultSet = textStatement.executeQuery();
        while (resultSet.next()) {
            String actionId = resultSet.getString("ActionId");
            String value = resultSet.getString("Value");
            long fieldId = resultSet.getLong("FieldId");
            castCollection.parallelStream().filter(jdsEntity -> jdsEntity.getActionId().equals(actionId)).forEach(jdsEntity -> {
                if (jdsEntity.stringProperties.containsKey(fieldId)) {
                    jdsEntity.stringProperties.get(fieldId).set(value);
                }
            });
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
