package io.github.subiyacryolite.jds.events;

import java.sql.Connection;

/**
 * Created by ifunga on 13/05/2017.
 */
public class OnPostLoadEvent {
    private final String entityGuid;
    private final Connection connection;

    public OnPostLoadEvent(Connection connection, String entityGuid) {
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
