/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.subiyacryolite.jds.connectivity;

import io.github.subiyacryolite.jds.JdsDbTransactionalSql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author indana
 */
public class JdsDbTransactionalSqllmplementation extends JdsDbTransactionalSql {

    @Override
    public Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        //return DriverManager.getConnection("jdbc:sqlserver://ICT-INDANA\\JDSINSTANCE;databaseName=jds", "sa", "p@nkP#55W0rd");
        return DriverManager.getConnection("jdbc:sqlserver://DESKTOP-64C7FRP\\JDSINSTANCE;databaseName=jds", "sa", "p@nkP#55W0rd");
    }
}
