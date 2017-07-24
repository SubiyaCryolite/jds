package io.github.subiyacryolite.jds.events;

import java.sql.Connection;

/**
 * Created by ifunga on 13/05/2017.
 */
public class OnPostLoadEventArguments {
    private final String entityGuid;
    private final Connection connection;

    public OnPostLoadEventArguments(Connection connection, String entityGuid) {
        this.entityGuid = entityGuid;
        this.connection = connection;
    }

    public String getEntityGuid() {
        return entityGuid;
    }

    public Connection getConnection() {
        return connection;
    }

}
