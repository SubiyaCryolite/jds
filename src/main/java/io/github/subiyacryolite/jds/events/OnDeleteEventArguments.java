package io.github.subiyacryolite.jds.events;

import com.javaworld.INamedStatement;
import com.javaworld.NamedCallableStatement;
import com.javaworld.NamedPreparedStatement;
import io.github.subiyacryolite.jds.JdsDb;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by ifunga on 16/07/2017.
 */
public class OnDeleteEventArguments {
    private final String entityGuid;
    private final JdsDb jdsDb;
    private final Connection connection;
    private final LinkedHashMap<String, PreparedStatement> statements;
    private final LinkedHashMap<String, CallableStatement> calls;
    private final LinkedHashMap<String, INamedStatement> namedStatements;
    private final LinkedHashMap<String, INamedStatement> namedCalls;

    public OnDeleteEventArguments(JdsDb jdsDb, Connection connection, String entityGuid) {
        this.jdsDb = jdsDb;
        this.entityGuid = entityGuid;
        this.connection = connection;
        this.statements = new LinkedHashMap<>();
        this.calls = new LinkedHashMap<>();
        this.namedStatements = new LinkedHashMap<>();
        this.namedCalls = new LinkedHashMap<>();
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

    public synchronized INamedStatement getOrAddNamedStatement(String key) throws SQLException {
        if (!namedStatements.containsKey(key))
            namedStatements.put(key, new NamedPreparedStatement(connection, key));
        return namedStatements.get(key);
    }

    public synchronized INamedStatement getOrAddNamedCall(String key) throws SQLException {
        if (!namedCalls.containsKey(key))
            namedCalls.put(key, new NamedCallableStatement(connection, key));
        return namedCalls.get(key);
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
        for (INamedStatement preparedStatement : namedStatements.values()) {
            preparedStatement.executeBatch();
            preparedStatement.close();
        }
        for (INamedStatement callableStatement : namedCalls.values()) {
            callableStatement.executeBatch();
            callableStatement.close();
        }
    }
}
