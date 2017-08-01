package io.github.subiyacryolite.jds.events;

import com.javaworld.INamedStatement;
import com.javaworld.NamedCallableStatement;
import com.javaworld.NamedPreparedStatement;

import java.sql.*;
import java.util.LinkedHashMap;

/**
 * Created by ifunga on 13/05/2017.
 */
public class OnPreSaveEventArguments {
    private final Connection connection;
    private final LinkedHashMap<String, Statement> statements;
    private final LinkedHashMap<String, INamedStatement> namedStatements;

    public OnPreSaveEventArguments(Connection connection) {
        this.connection = connection;
        this.statements = new LinkedHashMap<>();
        this.namedStatements = new LinkedHashMap<>();
    }

    public Connection getConnection() {
        return connection;
    }

    public synchronized Statement getOrAddStatement(String key) throws SQLException {
        if (!statements.containsKey(key))
            statements.put(key, connection.prepareStatement(key));
        return statements.get(key);
    }

    public synchronized Statement getOrAddCall(String key) throws SQLException {
        if (!statements.containsKey(key))
            statements.put(key, connection.prepareCall(key));
        return statements.get(key);
    }

    public synchronized INamedStatement getOrAddNamedStatement(String key) throws SQLException {
        if (!namedStatements.containsKey(key))
            namedStatements.put(key, new NamedPreparedStatement(connection, key));
        return namedStatements.get(key);
    }

    public synchronized INamedStatement getOrAddNamedCall(String key) throws SQLException {
        if (!namedStatements.containsKey(key))
            namedStatements.put(key, new NamedCallableStatement(connection, key));
        return namedStatements.get(key);
    }

    public void executeBatches() throws SQLException {
        //start with named statements
        for (INamedStatement preparedStatement : namedStatements.values()) {
            preparedStatement.executeBatch();
            preparedStatement.close();
        }
        for (Statement preparedStatement : statements.values()) {
            preparedStatement.executeBatch();
            preparedStatement.close();
        }
    }
}
