package io.github.subiyacryolite.jds.events;

import io.github.subiyacryolite.jds.JdsDb;

import java.sql.Connection;

/**
 * Created by ifunga on 16/07/2017.
 */
public class OnDeleteEventArguments {
    private final String entityGuid;
    private final JdsDb jdsDb;
    private final Connection connection;

    public OnDeleteEventArguments(JdsDb jdsDb, Connection connection, String entityGuid) {
        this.jdsDb = jdsDb;
        this.entityGuid = entityGuid;
        this.connection = connection;
    }

    public String getEntityGuid() {
        return entityGuid;
    }

    public JdsDb getJdsDb() {
        return jdsDb;
    }

    public Connection getConnection() {
        return connection;
    }

}
