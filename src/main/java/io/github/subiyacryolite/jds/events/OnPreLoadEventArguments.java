package io.github.subiyacryolite.jds.events;

import java.sql.Connection;

/**
 * Created by ifunga on 13/05/2017.
 */
public class OnPreLoadEventArguments {
    private final String entityGuid;
    private final int batchSequence;
    private final int batchSize;
    private final Connection connection;

    public OnPreLoadEventArguments(Connection connection, String entityGuid, int batchSequence, int batchSize) {
        this.entityGuid = entityGuid;
        this.batchSequence = batchSequence;
        this.batchSize = batchSize;
        this.connection = connection;
    }

    public String getEntityGuid() {
        return entityGuid;
    }

    public int getBatchSequence() {
        return batchSequence;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public Connection getConnection() {
        return connection;
    }

}
