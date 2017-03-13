package org.jenesis.jds;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by ifunga on 13/03/2017.
 */
public class JdsDelete {
    private final static String DELETE_SQL ="DELETE FROM JdsStoreEntityOverview WHERE EntityGuid = ?";

    public static void delete(final JdsDatabase jdsDatabase, final Collection<? extends JdsEntity> entities) {
        try (Connection connection = jdsDatabase.getConnection(); PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            connection.setAutoCommit(false);
            for (JdsEntity entity : entities) {
                statement.setString(1, entity.getEntityGuid());
                statement.addBatch();
            }
            statement.executeBatch();
            connection.commit();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    public static void delete(final JdsDatabase jdsDatabase, final JdsEntity... entities) {
        delete(jdsDatabase, Arrays.asList(entities));
    }

    public static void delete(final JdsDatabase jdsDatabase, final String... entityGuids) {
        try (Connection connection = jdsDatabase.getConnection(); PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            connection.setAutoCommit(false);
            for (String entityGuid : entityGuids) {
                statement.setString(1, entityGuid);
                statement.addBatch();
            }
            statement.executeBatch();
            connection.commit();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }
}
