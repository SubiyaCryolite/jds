package com.javaworld;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Created by ifunga on 20/07/2017.
 */
public interface INamedStatement extends AutoCloseable {

    void setString(String name, String value) throws SQLException;

    void setInt(String name, int value) throws SQLException;

    void setLong(String name, long value) throws SQLException;

    void setBytes(String name, byte[] value) throws SQLException;

    void setBlob(String name, InputStream value) throws SQLException;

    void setFloat(String name, float value) throws SQLException;

    void setNull(String name, int value) throws SQLException;

    void setDouble(String name, double value) throws SQLException;

    void setTimestamp(String name, Timestamp value) throws SQLException;

    PreparedStatement getStatement();

    boolean execute() throws SQLException;

    ResultSet executeQuery() throws SQLException;

    int executeUpdate() throws SQLException;

    void close() throws SQLException;

    void addBatch() throws SQLException;

    int[] executeBatch() throws SQLException;
}
