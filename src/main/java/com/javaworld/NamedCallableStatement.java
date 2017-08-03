package com.javaworld;

import java.io.InputStream;
import java.sql.*;
import java.util.*;

/*
* @author adam_crume
*/
public class NamedCallableStatement implements INamedStatement {
    /**
     * The statement this object is wrapping.
     */
    private final CallableStatement callableStatement;

    /**
     * Maps parameter names to arrays of ints which are the parameter indices.
     */
    private final Map indexMap;


    /**
     * Creates a NamedPreparedStatement.  Wraps a call to
     * c.{@link Connection#prepareStatement(String)
     * prepareStatement}.
     *
     * @param connection the database connection
     * @param query      the parameterized query
     * @throws SQLException if the statement could not be created
     */
    public NamedCallableStatement(Connection connection, String query) throws SQLException {
        indexMap = new HashMap();
        String parsedQuery = parse(query, indexMap);
        callableStatement = connection.prepareCall(parsedQuery);
    }


    /**
     * Parses a query with named parameters.  The parameter-index mappings are
     * put into the map, and the
     * parsed query is returned.  DO NOT CALL FROM CLIENT CODE.  This
     * method is non-private so JUnit code can
     * test it.
     *
     * @param query    query to parse
     * @param paramMap map to hold parameter-index mappings
     * @return the parsed query
     */
    static final String parse(String query, Map paramMap) {
        // I was originally using regular expressions, but they didn't work well for ignoring
        // parameter-like strings inside quotes.
        int length = query.length();
        StringBuffer parsedQuery = new StringBuffer(length);
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        int index = 1;

        for (int i = 0; i < length; i++) {
            char c = query.charAt(i);
            if (inSingleQuote) {
                if (c == '\'') {
                    inSingleQuote = false;
                }
            } else if (inDoubleQuote) {
                if (c == '"') {
                    inDoubleQuote = false;
                }
            } else {
                if (c == '\'') {
                    inSingleQuote = true;
                } else if (c == '"') {
                    inDoubleQuote = true;
                } else if (c == ':' && i + 1 < length &&
                        Character.isJavaIdentifierStart(query.charAt(i + 1))) {
                    int j = i + 2;
                    while (j < length && Character.isJavaIdentifierPart(query.charAt(j))) {
                        j++;
                    }
                    String name = query.substring(i + 1, j);
                    c = '?'; // replace the parameter with a question mark
                    i += name.length(); // skip past the end if the parameter
                    List indexList = (List) paramMap.get(name);
                    if (indexList == null) {
                        indexList = new LinkedList();
                        paramMap.put(name, indexList);
                    }
                    indexList.add(new Integer(index));
                    index++;
                }
            }
            parsedQuery.append(c);
        }

        // replace the lists of Integer objects with arrays of ints
        for (Iterator itr = paramMap.entrySet().iterator(); itr.hasNext(); ) {
            Map.Entry entry = (Map.Entry) itr.next();
            List list = (List) entry.getValue();
            int[] indexes = new int[list.size()];
            int i = 0;
            for (Iterator itr2 = list.iterator(); itr2.hasNext(); ) {
                Integer x = (Integer) itr2.next();
                indexes[i++] = x.intValue();
            }
            entry.setValue(indexes);
        }
        return parsedQuery.toString();
    }


    /**
     * Returns the indexes for a parameter.
     *
     * @param name parameter name
     * @return parameter indexes
     * @throws IllegalArgumentException if the parameter does not exist
     */
    private int[] getIndexes(String name) {
        int[] indexes = (int[]) indexMap.get(name);
        if (indexes == null) {
            throw new IllegalArgumentException("Parameter not found: " + name);
        }
        return indexes;
    }


    /**
     * Sets a parameter.
     *
     * @param name  parameter name
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setObject(int, Object)
     */
    public void setObject(String name, Object value) throws SQLException {
        int[] indexes = getIndexes(name);
        for (int i = 0; i < indexes.length; i++) {
            callableStatement.setObject(indexes[i], value);
        }
    }

    /**
     * Sets a parameter.
     *
     * @param name  parameter name
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setObject(int, Object)
     */
    public void setBytes(String name, byte[] value) throws SQLException {
        int[] indexes = getIndexes(name);
        for (int i = 0; i < indexes.length; i++) {
            callableStatement.setBytes(indexes[i], value);
        }
    }

    /**
     * Sets a parameter.
     *
     * @param name  parameter name
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setObject(int, Object)
     */
    public void setBlob(String name, InputStream value) throws SQLException {
        int[] indexes = getIndexes(name);
        for (int i = 0; i < indexes.length; i++) {
            callableStatement.setBlob(indexes[i], value);
        }
    }

    /**
     * Sets a parameter.
     *
     * @param name  parameter name
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setObject(int, Object)
     */
    public void setNull(String name, int value) throws SQLException {
        int[] indexes = getIndexes(name);
        for (int i = 0; i < indexes.length; i++) {
            callableStatement.setNull(indexes[i], value);
        }
    }


    /**
     * Sets a parameter.
     *
     * @param name  parameter name
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setString(int, String)
     */
    public void setString(String name, String value) throws SQLException {
        int[] indexes = getIndexes(name);
        for (int i = 0; i < indexes.length; i++) {
            callableStatement.setString(indexes[i], value);
        }
    }


    /**
     * Sets a parameter.
     *
     * @param name  parameter name
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setInt(int, int)
     */
    public void setInt(String name, int value) throws SQLException {
        int[] indexes = getIndexes(name);
        for (int i = 0; i < indexes.length; i++) {
            callableStatement.setInt(indexes[i], value);
        }
    }


    /**
     * Sets a parameter.
     *
     * @param name  parameter name
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setInt(int, int)
     */
    public void setLong(String name, long value) throws SQLException {
        int[] indexes = getIndexes(name);
        for (int i = 0; i < indexes.length; i++) {
            callableStatement.setLong(indexes[i], value);
        }
    }

    /**
     * Sets a parameter.
     *
     * @param name  parameter name
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setInt(int, int)
     */
    public void setFloat(String name, float value) throws SQLException {
        int[] indexes = getIndexes(name);
        for (int i = 0; i < indexes.length; i++) {
            callableStatement.setFloat(indexes[i], value);
        }
    }

    /**
     * Sets a parameter.
     *
     * @param name  parameter name
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setInt(int, int)
     */
    public void setDouble(String name, double value) throws SQLException {
        int[] indexes = getIndexes(name);
        for (int i = 0; i < indexes.length; i++) {
            callableStatement.setDouble(indexes[i], value);
        }
    }


    /**
     * Sets a parameter.
     *
     * @param name  parameter name
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setTimestamp(int, Timestamp)
     */
    public void setTimestamp(String name, Timestamp value) throws SQLException {
        int[] indexes = getIndexes(name);
        for (int i = 0; i < indexes.length; i++) {
            callableStatement.setTimestamp(indexes[i], value);
        }
    }


    /**
     * Returns the underlying statement.
     *
     * @return the statement
     */
    public PreparedStatement getStatement() {
        return callableStatement;
    }


    /**
     * Executes the statement.
     *
     * @return true if the first result is a {@link ResultSet}
     * @throws SQLException if an error occurred
     * @see PreparedStatement#execute()
     */
    public boolean execute() throws SQLException {
        return callableStatement.execute();
    }


    /**
     * Executes the statement, which must be a query.
     *
     * @return the query results
     * @throws SQLException if an error occurred
     * @see PreparedStatement#executeQuery()
     */
    public ResultSet executeQuery() throws SQLException {
        return callableStatement.executeQuery();
    }


    /**
     * Executes the statement, which must be an SQL INSERT, UPDATE or DELETE
     * statement;
     * or an SQL statement that returns nothing, such as a DDL statement.
     *
     * @return number of rows affected
     * @throws SQLException if an error occurred
     * @see PreparedStatement#executeUpdate()
     */
    public int executeUpdate() throws SQLException {
        return callableStatement.executeUpdate();
    }

    //==================================================================================================================

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        return callableStatement.executeQuery(sql);
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        return callableStatement.executeUpdate(sql);
    }

    /**
     * Closes the statement.
     *
     * @throws SQLException if an error occurred
     * @see Statement#close()
     */
    public void close() throws SQLException {
        callableStatement.close();
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return callableStatement.getMaxFieldSize();
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        callableStatement.setMaxFieldSize(max);
    }

    @Override
    public int getMaxRows() throws SQLException {
        return callableStatement.getMaxRows();
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        callableStatement.setMaxRows(max);
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        callableStatement.setEscapeProcessing(enable);
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return callableStatement.getQueryTimeout();
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        callableStatement.setQueryTimeout(seconds);
    }

    @Override
    public void cancel() throws SQLException {
        callableStatement.cancel();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return callableStatement.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        callableStatement.clearWarnings();
    }

    @Override
    public void setCursorName(String name) throws SQLException {
        callableStatement.setCursorName(name);
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        return callableStatement.execute(sql);
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return callableStatement.getResultSet();
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return callableStatement.getUpdateCount();
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return callableStatement.getMoreResults();
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        callableStatement.setFetchDirection(direction);
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return callableStatement.getFetchDirection();
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        callableStatement.setFetchSize(rows);
    }

    @Override
    public int getFetchSize() throws SQLException {
        return callableStatement.getFetchSize();
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return callableStatement.getResultSetConcurrency();
    }

    @Override
    public int getResultSetType() throws SQLException {
        return callableStatement.getResultSetType();
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        callableStatement.addBatch(sql);
    }

    @Override
    public void clearBatch() throws SQLException {
        callableStatement.clearBatch();
    }

    /**
     * Adds the current set of parameters as a batch entry.
     *
     * @throws SQLException if something went wrong
     */
    public void addBatch() throws SQLException {
        callableStatement.addBatch();
    }


    /**
     * Executes all of the batched statements.
     * <p>
     * See {@link Statement#executeBatch()} for details.
     *
     * @return update counts for each statement
     * @throws SQLException if something went wrong
     */
    public int[] executeBatch() throws SQLException {
        return callableStatement.executeBatch();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return callableStatement.getConnection();
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return callableStatement.getMoreResults(current);
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return callableStatement.getResultSet();
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return callableStatement.executeUpdate(sql, autoGeneratedKeys);
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return callableStatement.executeUpdate(sql, columnIndexes);
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return callableStatement.executeUpdate(sql, columnNames);
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return callableStatement.execute(sql, autoGeneratedKeys);
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return callableStatement.execute(sql, columnIndexes);
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        return callableStatement.execute(sql, columnNames);
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return callableStatement.getResultSetHoldability();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return callableStatement.isClosed();
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        callableStatement.setPoolable(poolable);
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return callableStatement.isPoolable();
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        callableStatement.closeOnCompletion();
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return callableStatement.isCloseOnCompletion();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return callableStatement.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return callableStatement.isWrapperFor(iface);
    }
}