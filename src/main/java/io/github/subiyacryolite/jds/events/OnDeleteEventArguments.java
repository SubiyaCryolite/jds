package io.github.subiyacryolite.jds.events;

import com.javaworld.INamedStatement;
import com.javaworld.NamedCallableStatement;
import com.javaworld.NamedPreparedStatement;
import io.github.subiyacryolite.jds.JdsDb;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by ifunga on 16/07/2017.
 */
public class OnDeleteEventArguments {
    private final String entityGuid;
    private final JdsDb jdsDb;
    private final Connection connection;
    private final LinkedHashMap<String, Statement> statements;

    public OnDeleteEventArguments(JdsDb jdsDb, Connection connection, String entityGuid) {
        this.jdsDb = jdsDb;
        this.entityGuid = entityGuid;
        this.connection = connection;
        this.statements = new LinkedHashMap<>();
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
        return (PreparedStatement)statements.get(key);
    }

    public synchronized CallableStatement getOrAddCall(String key) throws SQLException {
        if (!statements.containsKey(key))
            statements.put(key, connection.prepareCall(key));
        return (CallableStatement) statements.get(key);
    }

    public synchronized INamedStatement getOrAddNamedStatement(String key) throws SQLException {
        if (!statements.containsKey(key))
            statements.put(key, new NamedPreparedStatement(connection, key));
        return (INamedStatement) statements.get(key);
    }

    public synchronized INamedStatement getOrAddNamedCall(String key) throws SQLException {
        if (!statements.containsKey(key))
            statements.put(key, new NamedCallableStatement(connection, key));
        return (INamedStatement) statements.get(key);
    }

    public void executeBatches() throws SQLException {
        connection.setAutoCommit(false);
        for (Statement preparedStatement : statements.values()) {
            preparedStatement.executeBatch();
            preparedStatement.close();
        }
        connection.commit();
        connection.setAutoCommit(true);
    }
}
