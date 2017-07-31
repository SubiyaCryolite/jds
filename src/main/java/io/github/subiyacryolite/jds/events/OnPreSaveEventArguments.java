package io.github.subiyacryolite.jds.events;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * Created by ifunga on 13/05/2017.
 */
public class OnPreSaveEventArguments {
    private final int outerBatchStep;
    private final int batchSize;
    private final Connection connection;
    private final HashMap<String, PreparedStatement> statements;
    private final HashMap<String, CallableStatement> calls;

    public OnPreSaveEventArguments(Connection connection, int outerBatchStep, int batchSize) {
        this.outerBatchStep = outerBatchStep;
        this.batchSize = batchSize;
        this.connection = connection;
        this.statements = new HashMap<>();
        this.calls = new HashMap<>();
    }

    public Connection getConnection() {
        return connection;
    }

    public int getOuterBatchStep() {
        return outerBatchStep;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public PreparedStatement getOrAddStatement(String key) throws SQLException {
        return statements.getOrDefault(key, connection.prepareStatement(key));
    }

    public CallableStatement getOrAddCall(String key) throws SQLException {
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
