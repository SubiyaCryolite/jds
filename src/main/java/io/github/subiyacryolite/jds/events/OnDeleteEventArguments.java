package io.github.subiyacryolite.jds.events;

import io.github.subiyacryolite.jds.JdsDb;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * Created by ifunga on 16/07/2017.
 */
public class OnDeleteEventArguments {
    private final String entityGuid;
    private final JdsDb jdsDb;
    private final Connection connection;
    private final HashMap<String, PreparedStatement> statements;
    private final HashMap<String, CallableStatement> calls;

    public OnDeleteEventArguments(JdsDb jdsDb, Connection connection, String entityGuid) {
        this.jdsDb = jdsDb;
        this.entityGuid = entityGuid;
        this.connection = connection;
        this.statements = new HashMap<>();
        this.calls = new HashMap<>();
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

    public PreparedStatement getOrAddStatement(String key) throws SQLException {
        return statements.getOrDefault(key, connection.prepareStatement(key));
    }

    public PreparedStatement getOrAddCall(String key) throws SQLException {
        return calls.getOrDefault(key, connection.prepareCall(key));
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
