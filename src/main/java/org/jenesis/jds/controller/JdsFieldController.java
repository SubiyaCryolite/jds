package org.jenesis.jds.controller;

import javafx.beans.property.*;
import org.jenesis.jds.entities.JdsDatabase;
import org.jenesis.jds.entities.JdsEntity;
import org.jenesis.jds.entities.JdsEntityClasses;
import org.jenesis.jds.entities.JdsFieldEnum;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ifunga on 09/02/2017.
 */
public class JdsFieldController {
    private static JdsFieldController instance;

    public static synchronized JdsFieldController instance() {
        if (instance == null)
            instance = new JdsFieldController();
        return instance;
    }

    public void populateStringArrays(final String actionId, final HashMap<Long, SimpleListProperty<String>> enumProperties) {
        String sql = "SELECT v.Value, v.FieldId FROM JdsStoreTextArray v WHERE v.ActionId = ?";
        try (Connection connection = JdsDatabase.instance().getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, actionId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                long key = resultSet.getLong("FieldId");
                if (enumProperties.containsKey(key))
                    enumProperties.get(key).add(resultSet.getString("Value"));
            }
            resultSet.close();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    public void populateEnumStrings(final String actionId, final HashMap<JdsFieldEnum, SimpleListProperty<String>> enumProperties) {
        String sql = "SELECT FieldId,ActionId,EnumIndex,Value FROM JdsStoreIntegerArray WHERE ActionId = ? AND FieldId = ?";
        try (Connection connection = JdsDatabase.instance().getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (Map.Entry<JdsFieldEnum, SimpleListProperty<String>> current : enumProperties.entrySet()) {
                SimpleListProperty<String> propertyArray = current.getValue();
                preparedStatement.clearParameters();
                preparedStatement.setString(1, actionId);
                preparedStatement.setLong(2, current.getKey().getField().getId());
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    if (current.getKey().getField().getId() == resultSet.getLong("FieldId"))
                        propertyArray.add(current.getKey().getValue(resultSet.getInt("Value")));
                }
                resultSet.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }


    public void populateStrings(final String actionId, final HashMap<Long, SimpleStringProperty> stringProperties) {
        String sql = "select v.Value, v.FieldId from JdsStoreText v where v.ActionId =  ?";
        try (Connection connection = JdsDatabase.instance().getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, actionId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                long key = resultSet.getLong("FieldId");
                if (stringProperties.containsKey(key))
                    stringProperties.get(key).set(resultSet.getString("Value"));
            }
            resultSet.close();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    public void populateIntegers(final String actionId, final HashMap<Long, SimpleIntegerProperty> properties) {
        String sql = "select v.Value, v.FieldId from JdsStoreInteger v where v.ActionId = ?";
        try (Connection connection = JdsDatabase.instance().getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, actionId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                long key = resultSet.getLong("FieldId");
                if (properties.containsKey(key))
                    properties.get(key).set(resultSet.getInt("Value"));
            }
            resultSet.close();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    public void populateDoubles(final String actionId, final HashMap<Long, SimpleDoubleProperty> properties) {
        String sql = "select v.Value, v.FieldId from JdsStoreDouble v where v.ActionId = ?";
        try (Connection connection = JdsDatabase.instance().getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, actionId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                long key = resultSet.getLong("FieldId");
                if (properties.containsKey(key))
                    properties.get(key).set(resultSet.getDouble("Value"));
            }
            resultSet.close();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    public void populateFloats(final String actionId, final HashMap<Long, SimpleFloatProperty> properties) {
        String sql = "select v.Value, v.FieldId from JdsStoreFloat v where v.ActionId = ?";
        try (Connection connection = JdsDatabase.instance().getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, actionId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                long key = resultSet.getLong("FieldId");
                if (properties.containsKey(key))
                    properties.get(key).set(resultSet.getFloat("Value"));
            }
            resultSet.close();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    public void populateLongs(final String actionId, final HashMap<Long, SimpleLongProperty> properties) {
        String sql = "select v.Value, v.FieldId from JdsStoreLong v where v.ActionId = ?";
        try (Connection connection = JdsDatabase.instance().getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, actionId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                long key = resultSet.getLong("FieldId");
                if (properties.containsKey(key))
                    properties.get(key).set(resultSet.getLong("Value"));
            }
            resultSet.close();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    public void populateDates(final String actionId, final HashMap<Long, SimpleObjectProperty<LocalDateTime>> properties) {
        String sql = "select v.Value, v.FieldId from JdsStoreDateTime v where v.ActionId = ?";
        try (Connection connection = JdsDatabase.instance().getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, actionId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                long key = resultSet.getLong("FieldId");
                if (properties.containsKey(key))
                    properties.get(key).set(resultSet.getTimestamp("Value").toLocalDateTime());
            }
            resultSet.close();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    public void populateObjectLists(final String actionId, final HashMap<Long, SimpleListProperty<? extends JdsEntity>> properties) {
        for (Map.Entry<Long, SimpleListProperty<? extends JdsEntity>> current : properties.entrySet()) {
            SimpleListProperty<JdsEntity> propertyList = (SimpleListProperty<JdsEntity>) current.getValue();
            long serviceCode = current.getKey();
            List<String> actionIds = getSecondaryIdsForClass(actionId, serviceCode);
            actionIds.stream().filter(innerActionId -> JdsEntityClasses.hasClass(serviceCode)).forEach(innerActionId -> {
                Class<JdsEntity> actionRef = JdsEntityClasses.getBoundClass(serviceCode);
                try {
                    JdsEntity action = actionRef.newInstance();
                    action.setActionId(innerActionId);
                    action.populate();
                    propertyList.get().add(action);
                } catch (Exception ex) {
                    ex.printStackTrace(System.err);
                }
            });
        }
    }

    public void populateObjects(final String actionId, final HashMap<Long, SimpleObjectProperty<? extends JdsEntity>> objects) {
        for (Map.Entry<Long, SimpleObjectProperty<? extends JdsEntity>> current : objects.entrySet()) {
            SimpleObjectProperty<JdsEntity> property = ((SimpleObjectProperty<JdsEntity>) current.getValue());
            long serviceCode = current.getKey();
            List<String> actionIds = getSecondaryIdsForClass(actionId, serviceCode);
            actionIds.stream().filter(innerActionId -> JdsEntityClasses.hasClass(serviceCode)).forEach(innerActionId -> {
                Class<JdsEntity> actionRef = JdsEntityClasses.getBoundClass(serviceCode);
                try {
                    JdsEntity action = actionRef.newInstance();
                    action.setActionId(innerActionId);
                    action.populate();
                    property.set(action);
                } catch (Exception ex) {
                    ex.printStackTrace(System.err);
                }
            });
        }
    }

    private List<String> getSecondaryIdsForClass(final String actionId, final long entityCode) {
        String sql = "SELECT SubActionId FROM JdsStoreEntitySubclass WHERE ActionId = ? AND EntityId = ?";
        List<String> toReturn = new ArrayList<>();
        try (Connection connection = JdsDatabase.instance().getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, actionId);
            preparedStatement.setLong(2, entityCode);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                toReturn.add(resultSet.getString("SubActionId"));
            }
            resultSet.close();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
        return toReturn;
    }
}
