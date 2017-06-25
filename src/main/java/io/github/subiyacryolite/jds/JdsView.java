package io.github.subiyacryolite.jds;


import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation;
import io.github.subiyacryolite.jds.enums.JdsFieldType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
            String name = cleanViewName(je.entityName());
            String viewName = getMainViewName(name);
            try (Connection connection = jdsDb.getConnection()) {
                String arrayFloatView = innerView(connection, jdsDb, JdsFieldType.ARRAY_FLOAT, id, name);
                String arrayIntView = innerView(connection, jdsDb, JdsFieldType.ARRAY_INT, id, name);
                String arrayDoubleView = innerView(connection, jdsDb, JdsFieldType.ARRAY_DOUBLE, id, name);
                String arrayLongView = innerView(connection, jdsDb, JdsFieldType.ARRAY_LONG, id, name);
                String arrayTextView = innerView(connection, jdsDb, JdsFieldType.ARRAY_TEXT, id, name);
                String arrayDateTimeView = innerView(connection, jdsDb, JdsFieldType.ARRAY_DATE_TIME, id, name);
                String booleanView = innerView(connection, jdsDb, JdsFieldType.BOOLEAN, id, name);
                //String blobView = innerView(connection, jdsDb, JdsFieldType.BLOB, id, name);, problem with PG implementation
                String dateTimeView = innerView(connection, jdsDb, JdsFieldType.DATE_TIME, id, name);
                String dateView = innerView(connection, jdsDb, JdsFieldType.DATE, id, name);
                String doubleView = innerView(connection, jdsDb, JdsFieldType.DOUBLE, id, name);
                String enumView = innerView(connection, jdsDb, JdsFieldType.ENUM_TEXT, id, name);
                String floatView = innerView(connection, jdsDb, JdsFieldType.FLOAT, id, name);
                String intView = innerView(connection, jdsDb, JdsFieldType.INT, id, name);
                String longView = innerView(connection, jdsDb, JdsFieldType.LONG, id, name);
                String timeView = innerView(connection, jdsDb, JdsFieldType.TIME, id, name);
                String textView = innerView(connection, jdsDb, JdsFieldType.TEXT, id, name);
                String zonedDateTimeView = innerView(connection, jdsDb, JdsFieldType.ZONED_DATE_TIME, id, name);
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
                        id,
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

    /**
     * @param connection the SQL connection that will be used for this operation
     * @param viewName   the final name of this view
     * @param tables     the tables that will make up this view
     */
    private static void createMainView(Connection connection, JdsDb jdsDb, long entityId, String viewName, String[] tables) {
        String sql = "SELECT field.FieldName FROM \n" +
                "JdsRefEntities entity\n" +
                "LEFT JOIN JdsBindEntityFields bound\n" +
                "ON entity.EntityId = bound.EntityId\n" +
                "LEFT JOIN JdsRefFields field \n" +
                "ON bound.FieldId = field.FieldId\n" +
                "LEFT join JdsRefFieldTypes type\n" +
                "ON field.FieldId = type.TypeId\n" +
                "WHERE type.TypeName NOT IN ('BLOB','ARRAY_FLOAT', 'ARRAY_INT', 'ARRAY_DOUBLE', 'ARRAY_LONG', 'ARRAY_TEXT', 'ARRAY_DATE_TIME','ENUM_TEXT') AND entity.EntityId = ?\n" +
                "ORDER BY field.FieldName";
        List<String> fieldNames = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, entityId);
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
        stringBuilder.append("\nWHERE overview.EntityId = ");
        stringBuilder.append(entityId);

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
    public static boolean delete(final Class<? extends JdsEntity> target, final JdsDb jdsDb) throws IllegalArgumentException {
        if (!jdsDb.supportsStatements())
            throw new IllegalArgumentException("The underlying database does not support the creation of views");
        if (target.isAnnotationPresent(JdsEntityAnnotation.class)) {
            JdsEntityAnnotation je = target.getAnnotation(JdsEntityAnnotation.class);
            String name = cleanViewName(je.entityName());
            String viewName = getMainViewName(name);
            try (Connection connection = jdsDb.getConnection()) {
                dropView(connection, viewName);
                dropView(connection, getViewName(JdsFieldType.ARRAY_FLOAT, name));
                dropView(connection, getViewName(JdsFieldType.ARRAY_INT, name));
                dropView(connection, getViewName(JdsFieldType.ARRAY_DOUBLE, name));
                dropView(connection, getViewName(JdsFieldType.ARRAY_LONG, name));
                dropView(connection, getViewName(JdsFieldType.ARRAY_TEXT, name));
                dropView(connection, getViewName(JdsFieldType.ARRAY_DATE_TIME, name));
                dropView(connection, getViewName(JdsFieldType.BOOLEAN, name));
                dropView(connection, getViewName(JdsFieldType.BLOB, name));
                dropView(connection, getViewName(JdsFieldType.DATE_TIME, name));
                dropView(connection, getViewName(JdsFieldType.DATE, name));
                dropView(connection, getViewName(JdsFieldType.DOUBLE, name));
                dropView(connection, getViewName(JdsFieldType.ENUM_TEXT, name));
                dropView(connection, getViewName(JdsFieldType.FLOAT, name));
                dropView(connection, getViewName(JdsFieldType.INT, name));
                dropView(connection, getViewName(JdsFieldType.LONG, name));
                dropView(connection, getViewName(JdsFieldType.TIME, name));
                dropView(connection, getViewName(JdsFieldType.TEXT, name));
                dropView(connection, getViewName(JdsFieldType.ZONED_DATE_TIME, name));
            } catch (Exception ex) {
                ex.printStackTrace(System.err);
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
     * @param entityId   the id code of the JdsEntity
     * @param entityName the raw entity name
     * @return the created inner view name
     */
    private static String innerView(final Connection connection, final JdsDb jdsDb, final JdsFieldType fieldType, final long entityId, final String entityName) {
        String sql = "select field.FieldName from \n" +
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
            ps.setLong(2, entityId);
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
        StringJoiner sj = new StringJoiner(",\n");
        for (String entry : fieldNames) {
            sj.add(String.format("MAX(CASE WHEN t.FieldName = '%s' THEN t.Value ELSE NULL END) AS %s", entry, entry));
        }
        stringBuilder.append(sj.toString());
        stringBuilder.append("\nFROM\t\n");
        stringBuilder.append("\t(\n");
        stringBuilder.append("\t\tselect src.EntityGuid, sField.FieldName, src.Value\n");
        stringBuilder.append("\t\tfrom ");
        stringBuilder.append(JdsTableLookup.tableFor(fieldType));
        stringBuilder.append("\tsrc\n");
        stringBuilder.append("\t\tleft join JdsRefFields sField\n");
        stringBuilder.append("\t\ton src.FieldId = sField.FieldId\n");
        stringBuilder.append("\t\tleft join JdsRefFieldTypes sFieldType\n");
        stringBuilder.append("\t\ton sField.FieldId = sFieldType.TypeId \n");
        stringBuilder.append(String.format("\t\twhere src.FieldId IN (SELECT ef.FieldId FROM JdsBindEntityFields EF where ef.EntityId = %s and sFieldType.TypeName = '%s')\n", entityId, fieldType));
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
        return String.format("vw_%s", entityName);
    }

    /**
     * Executes a view drop
     *
     * @param connection the SQL connection that will be used for this operation
     * @param name       the view to drop
     * @return whether the action completed successfully
     */
    private static boolean dropView(final Connection connection, final String name) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("DROP VIEW %s", name))) {
            return preparedStatement.execute();
        } catch (Exception ex) {
            return false;
        }
    }
}
