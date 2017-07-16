package io.github.subiyacryolite.jds;


import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation;
import io.github.subiyacryolite.jds.enums.JdsFieldType;
import io.github.subiyacryolite.jds.enums.JdsImplementation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * This class is responsible for creating and deleting flat views of {@link JdsEntity JdsEntities}
 * Created by ifunga on 24/06/2017.
 */
public class JdsView {

    /**
     * Create flat tables that will be representative of a JdsEntity
     *
     * @param target the {@link JdsEntity JdsEntity}
     * @param jdsDb  an instance of {@link JdsDb JdsDb}
     * @return whether the operation completed successfully
     * @throws IllegalArgumentException
     */
    public static boolean create(final Class<? extends JdsEntity> target, final JdsDb jdsDb) throws IllegalArgumentException {
        if (!jdsDb.supportsStatements())
            throw new IllegalArgumentException("The underlying database does not support the creation of views");
        if (target.isAnnotationPresent(JdsEntityAnnotation.class)) {
            JdsEntityAnnotation je = target.getAnnotation(JdsEntityAnnotation.class);
            long id = je.entityId();
            String cleanName = cleanViewName(je.entityName());
            String viewName = getMainViewName(cleanName);
            List<Long> entityAndChildren = new ArrayList<>();
            entityAndChildren.add(id);
            try (Connection connection = jdsDb.getConnection()) {
                populateChildEntities(connection, entityAndChildren, id);
                String arrayFloatView = innerView(connection, jdsDb, JdsFieldType.ARRAY_FLOAT, entityAndChildren, cleanName);
                String arrayIntView = innerView(connection, jdsDb, JdsFieldType.ARRAY_INT, entityAndChildren, cleanName);
                String arrayDoubleView = innerView(connection, jdsDb, JdsFieldType.ARRAY_DOUBLE, entityAndChildren, cleanName);
                String arrayLongView = innerView(connection, jdsDb, JdsFieldType.ARRAY_LONG, entityAndChildren, cleanName);
                String arrayTextView = innerView(connection, jdsDb, JdsFieldType.ARRAY_TEXT, entityAndChildren, cleanName);
                String arrayDateTimeView = innerView(connection, jdsDb, JdsFieldType.ARRAY_DATE_TIME, entityAndChildren, cleanName);
                String booleanView = innerView(connection, jdsDb, JdsFieldType.BOOLEAN, entityAndChildren, cleanName);
                //String blobView = innerView(connection, jdsDb, JdsFieldType.BLOB, entityAndChildren, name);, problem with PG implementation
                String dateTimeView = innerView(connection, jdsDb, JdsFieldType.DATE_TIME, entityAndChildren, cleanName);
                String dateView = innerView(connection, jdsDb, JdsFieldType.DATE, entityAndChildren, cleanName);
                String doubleView = innerView(connection, jdsDb, JdsFieldType.DOUBLE, entityAndChildren, cleanName);
                String enumView = innerView(connection, jdsDb, JdsFieldType.ENUM_COLLECTION, entityAndChildren, cleanName);
                String floatView = innerView(connection, jdsDb, JdsFieldType.FLOAT, entityAndChildren, cleanName);
                String intView = innerView(connection, jdsDb, JdsFieldType.INT, entityAndChildren, cleanName);
                String longView = innerView(connection, jdsDb, JdsFieldType.LONG, entityAndChildren, cleanName);
                String timeView = innerView(connection, jdsDb, JdsFieldType.TIME, entityAndChildren, cleanName);
                String textView = innerView(connection, jdsDb, JdsFieldType.TEXT, entityAndChildren, cleanName);
                String zonedDateTimeView = innerView(connection, jdsDb, JdsFieldType.ZONED_DATE_TIME, entityAndChildren, cleanName);
                String[] unused = new String[]{
                        arrayFloatView,
                        arrayIntView,
                        arrayDoubleView,
                        arrayLongView,
                        arrayTextView,
                        arrayDateTimeView,
                        enumView};
                createMainView(connection,
                        jdsDb,
                        entityAndChildren,
                        viewName,
                        new String[]{
                                booleanView,
                                //blobView, problem with PG implementation
                                dateTimeView,
                                dateView,
                                doubleView,
                                floatView,
                                intView,
                                longView,
                                timeView,
                                textView,
                                zonedDateTimeView});
            } catch (Exception ex) {
                ex.printStackTrace(System.err);
            }
            return true;
        } else {
            throw new IllegalArgumentException("You must annotate the class [" + target.getCanonicalName() + "] with [" + JdsEntityAnnotation.class + "]");
        }
    }

    private static void populateChildEntities(Connection connection, List<Long> entityAndChildren, long id) throws SQLException {
        String sql = "SELECT ChildEntityCode FROM JdsRefEntityInheritance WHERE ParentEntityCode = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    entityAndChildren.add(rs.getLong("ChildEntityCode"));
                }
            }
        }
    }

    /**
     * @param connection the SQL connection that will be used for this operation
     * @param viewName   the final name of this view
     * @param tables     the tables that will make up this view
     */
    private static void createMainView(Connection connection, JdsDb jdsDb, List<Long> entityId, String viewName, String[] tables) {
        String sql = "SELECT field.FieldName FROM \n" +
                "JdsRefEntities entity\n" +
                "LEFT JOIN JdsBindEntityFields bound\n" +
                "ON entity.EntityId = bound.EntityId\n" +
                "LEFT JOIN JdsRefFields field \n" +
                "ON bound.FieldId = field.FieldId\n" +
                "LEFT join JdsRefFieldTypes type\n" +
                "ON field.FieldId = type.TypeId\n" +
                "WHERE type.TypeName NOT IN ('BLOB','ARRAY_FLOAT', 'ARRAY_INT', 'ARRAY_DOUBLE', 'ARRAY_LONG', 'ARRAY_TEXT', 'ARRAY_DATE_TIME','ENUM_COLLECTION') AND entity.EntityId = ?\n" +
                "ORDER BY field.FieldName";
        List<String> fieldNames = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, entityId.get(0));
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                fieldNames.add(resultSet.getString("FieldName"));
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }

        StringBuilder stringBuilder = new StringBuilder("SELECT overview.EntityGuid, overview.DateCreated, overview.DateModified");
        if (!fieldNames.isEmpty()) {
            stringBuilder.append(", ");
            StringJoiner stringJoiner = new StringJoiner(", ");
            for (String entry : fieldNames) {
                stringJoiner.add(entry);
            }
            stringBuilder.append(stringJoiner.toString());
        }
        stringBuilder.append("\n");
        stringBuilder.append("FROM JdsStoreEntityOverview overview\n");
        StringJoiner stringJoiner = new StringJoiner("\n");
        for (int index = 0; index < tables.length; index++)
            stringJoiner.add(String.format("LEFT JOIN %s vwn%s ON overview.EntityGuid = vwn%s.EntityGuid", tables[index], index, index));
        stringBuilder.append(stringJoiner.toString());
        stringBuilder.append("\nWHERE overview.EntityGuid IN (SELECT DISTINCT EntityGuid FROM JdsStoreEntityInheritance eh WHERE eh.EntityId IN (");
        StringJoiner entityHeiarchy = new StringJoiner(",");
        for (Long l : entityId)
            entityHeiarchy.add(l + "");
        stringBuilder.append(entityHeiarchy);
        stringBuilder.append("))");

        String viewSql = jdsDb.createOrAlterView(viewName, stringBuilder.toString());
        try (PreparedStatement preparedStatement = connection.prepareStatement(viewSql)) {
            preparedStatement.execute();
        } catch (Exception ex) {
            System.err.println(viewSql);
            ex.printStackTrace(System.err);
        }
    }

    /**
     * Delete all flat tables bound to a JdsEntity
     *
     * @param target the {@link JdsEntity JdsEntity}
     * @param jdsDb  an instance of {@link JdsDb JdsDb}
     * @return whether the operation completed successfully
     * @throws IllegalArgumentException
     */
    public static boolean delete(final Class<? extends JdsEntity> target, final JdsDb jdsDb) throws IllegalArgumentException, SQLException, ClassNotFoundException {
        if (!jdsDb.supportsStatements())
            throw new IllegalArgumentException("The underlying database does not support the creation of views");
        if (target.isAnnotationPresent(JdsEntityAnnotation.class)) {
            JdsEntityAnnotation je = target.getAnnotation(JdsEntityAnnotation.class);
            String name = cleanViewName(je.entityName());
            String viewName = getMainViewName(name);
            try (Connection connection = jdsDb.getConnection()) {
                dropView(jdsDb, viewName);
                dropView(jdsDb, getViewName(JdsFieldType.ARRAY_FLOAT, name));
                dropView(jdsDb, getViewName(JdsFieldType.ARRAY_INT, name));
                dropView(jdsDb, getViewName(JdsFieldType.ARRAY_DOUBLE, name));
                dropView(jdsDb, getViewName(JdsFieldType.ARRAY_LONG, name));
                dropView(jdsDb, getViewName(JdsFieldType.ARRAY_TEXT, name));
                dropView(jdsDb, getViewName(JdsFieldType.ARRAY_DATE_TIME, name));
                dropView(jdsDb, getViewName(JdsFieldType.BOOLEAN, name));
                //dropView(connection, getViewName(JdsFieldType.BLOB, name));
                dropView(jdsDb, getViewName(JdsFieldType.DATE_TIME, name));
                dropView(jdsDb, getViewName(JdsFieldType.DATE, name));
                dropView(jdsDb, getViewName(JdsFieldType.DOUBLE, name));
                dropView(jdsDb, getViewName(JdsFieldType.ENUM_COLLECTION, name));
                dropView(jdsDb, getViewName(JdsFieldType.FLOAT, name));
                dropView(jdsDb, getViewName(JdsFieldType.INT, name));
                dropView(jdsDb, getViewName(JdsFieldType.LONG, name));
                dropView(jdsDb, getViewName(JdsFieldType.TIME, name));
                dropView(jdsDb, getViewName(JdsFieldType.TEXT, name));
                dropView(jdsDb, getViewName(JdsFieldType.ZONED_DATE_TIME, name));
            }
            return true;
        } else {
            throw new IllegalArgumentException("You must annotate the class [" + target.getCanonicalName() + "] with [" + JdsEntityAnnotation.class + "]");
        }
    }

    /**
     * Create names that are suitable for use in the database
     *
     * @param rawViewName raw version of the proposed view name
     * @return value that is suitable for use in the database
     */
    private static String cleanViewName(final String rawViewName) {
        return rawViewName.replaceAll("\\s+", "_").toLowerCase();
    }

    /**
     * Create a view of a specific data-type for a JdsEntity
     *
     * @param connection the SQL connection that will be used for this operation
     * @param jdsDb      an instance of JdsDb
     * @param fieldType  the data-type of this view
     * @param entityIds  the id codes of the JdsEntity and its children
     * @param entityName the raw entity name
     * @return the created inner view name
     */
    private static String innerView(final Connection connection, final JdsDb jdsDb, final JdsFieldType fieldType, final List<Long> entityIds, final String entityName) {
        String sql = "select distinct field.FieldName from \n" +
                "JdsRefEntities entity\n" +
                "left join JdsBindEntityFields bound\n" +
                "on entity.EntityId = bound.EntityId\n" +
                "left join JdsRefFields field \n" +
                "on bound.FieldId = field.FieldId\n" +
                "left join JdsRefFieldTypes type\n" +
                "on field.FieldId = type.TypeId\n" +
                "where type.TypeName = ? and entity.EntityId = ?\n" +
                "order by field.FieldName";

        List<String> fieldNames = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, fieldType.toString());
            ps.setLong(2, entityIds.get(0));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                fieldNames.add(rs.getString("FieldName"));
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
        String viewName = getViewName(fieldType, entityName);

        StringBuilder stringBuilder = new StringBuilder("SELECT t.EntityGuid AS EntityGuid");
        if (!fieldNames.isEmpty()) {
            stringBuilder.append(",\n");
        }
        StringJoiner fieldsOfInterest = new StringJoiner(",\n");
        for (String entry : fieldNames) {
            fieldsOfInterest.add(String.format("MAX(CASE WHEN t.FieldName = '%s' THEN t.Value ELSE NULL END) AS %s", entry, entry));
        }
        stringBuilder.append(fieldsOfInterest);
        stringBuilder.append("\nFROM\t\n");
        stringBuilder.append("\t(\n");
        stringBuilder.append("\t\tSELECT src.EntityGuid, sField.FieldName, src.Value\n");
        stringBuilder.append("\t\tFROM ");
        stringBuilder.append(JdsTableLookup.tableFor(fieldType));
        stringBuilder.append("\tsrc\n");
        stringBuilder.append("\t\t JOIN JdsStoreEntityOverview ov ON ov.EntityGuid = src.EntityGuid");
        StringJoiner entityHierarchy = new StringJoiner(",");
        for (Long id : entityIds)
            entityHierarchy.add("" + id);
        stringBuilder.append(String.format(" AND ov.EntityGuid IN (SELECT DISTINCT EntityGuid FROM JdsStoreEntityInheritance eh WHERE eh.EntityId IN (%s))\n", entityHierarchy));
        stringBuilder.append("\t\tJOIN JdsRefFields sField on src.FieldId = sField.FieldId\n");
        stringBuilder.append("\t\tJOIN JdsRefFieldTypes sFieldType on sField.FieldId = sFieldType.TypeId \n");
        stringBuilder.append(String.format("\t\tWHERE src.FieldId IN (SELECT DISTINCT ef.FieldId FROM JdsBindEntityFields EF where ef.EntityId in (%s) and sFieldType.TypeName = '%s')\n", entityHierarchy, fieldType));
        stringBuilder.append("\t) AS t\n");
        stringBuilder.append("\tGROUP BY t.EntityGuid");

        String sqlToExecute = jdsDb.createOrAlterView(viewName, stringBuilder.toString());
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlToExecute)) {
            preparedStatement.execute();
        } catch (Exception ex) {
            System.err.println(sqlToExecute);
            ex.printStackTrace(System.err);
        }
        return viewName;
    }

    /**
     * Generate a string that will be used as a view name
     *
     * @param fieldType
     * @param entityName
     * @return
     */
    private static String getViewName(final JdsFieldType fieldType, final String entityName) {
        return String.format("vw_%s_%s", entityName, cleanViewName(fieldType.toString()));
    }

    /**
     * Generate a string that will be used as a view name
     *
     * @param entityName
     * @return
     */
    private static String getMainViewName(final String entityName) {
        return String.format("_%s", entityName);
    }

    /**
     * Executes a view drop
     *
     * @param jdsDb the SQL connection that will be used for this operation
     * @param name  the view to drop
     * @return whether the action completed successfully
     */
    private static boolean dropView(final JdsDb jdsDb, final String name) {
        try (Connection connection = jdsDb.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(String.format("DROP VIEW %s%s", name, jdsDb.implementation == JdsImplementation.POSTGRES ? " CASCADE" : ""))) {
            return preparedStatement.execute();
        } catch (Exception e) {
            return false;
        }
    }
}
