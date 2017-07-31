package io.github.subiyacryolite.jds.events;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * Created by ifunga on 13/05/2017.
 */
public class OnPreLoadEventArguments {
    private final String entityGuid;
    private final int batchSequence;
    private final int batchSize;
    private final Connection connection;
    private final HashMap<String, PreparedStatement> statements;
    private final HashMap<String, CallableStatement> calls;

    public OnPreLoadEventArguments(Connection connection, String entityGuid, int batchSequence, int batchSize) {
        this.entityGuid = entityGuid;
        this.batchSequence = batchSequence;
        this.batchSize = batchSize;
        this.connection = connection;
        this.statements = new HashMap<>();
        this.calls = new HashMap<>();
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

    public synchronized PreparedStatement getOrAddStatement(String key) throws SQLException {
        if (!statements.containsKey(key))
            statements.put(key, connection.prepareStatement(key));
        return statements.get(key);
    }

    public synchronized CallableStatement getOrAddCall(String key) throws SQLException {
        if (!calls.containsKey(key))
            calls.put(key, connection.prepareCall(key));
        return calls.get(key);
    }

    public void executeBatches() throws SQLException {
        for (PreparedStatement preparedStatement : statements.values()) {
            preparedStatement.executeBatch();
            preparedStatement.close();
        }
        for (CallableStatement callableStatement : calls.values()) {
            callableStatement.executeBatch();
            callableStatement.close();
        }
    }
}
