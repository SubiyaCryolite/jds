package io.github.subiyacryolite.jds.events;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by ifunga on 13/05/2017.
 */
public class OnPostSaveEventArguments {
    private final int batchSize;
    private final Connection connection;
    private final LinkedHashMap <String, PreparedStatement> statements;
    private final LinkedHashMap <String, CallableStatement> calls;

    public OnPostSaveEventArguments(Connection connection, int batchSize) {
        this.batchSize = batchSize;
        this.connection = connection;
        this.statements = new LinkedHashMap<>();
        this.calls = new LinkedHashMap <>();
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
