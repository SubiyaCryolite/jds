/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.subiyacryolite.jds.connectivity;

import io.github.subiyacryolite.jds.JdsDbMySql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author indana
 */
public class JdsDbMySqlImplementation extends JdsDbMySql {

    @Override
    public Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/jds?autoReconnect=true&useSSL=false", "root", "p@nkP#55W0rd");//p@nkP#55W0rd
    }
}
