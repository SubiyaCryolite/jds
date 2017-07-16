package io.github.subiyacryolite.jds.connectivity;

import io.github.subiyacryolite.jds.JdsDbOracle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author indana
 */
public class JdsDbOracleSqlmplementation extends JdsDbOracle {

    @Override
    public Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        return DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "sys as sysdba", "p@nkP#55W0rd");
    }
}
