/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.subiyacryolite.jds;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author indana
 */
public interface JdsDbContract {

    /**
     * Acquire standard connection to the database
     *
     * @return standard connection to the database
     * @throws ClassNotFoundException when JDBC driver is not configured
     *                                correctly
     * @throws SQLException           when a standard SQL Exception occurs
     */
    Connection getConnection() throws ClassNotFoundException, SQLException;
}
