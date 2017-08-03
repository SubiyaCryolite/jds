/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectivity;

import io.github.subiyacryolite.jds.JdsDbMySql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 * @author indana
 */
public class JdsDbMySqlImplementation extends JdsDbMySql {

    @Override
    public Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Properties properties = new Properties();
        properties.put("user", "root");
        properties.put("password", "p@nkP#55W0rd");
        //info.put("password", "");
        properties.put("autoReconnect","true");
        properties.put("allowMultiQueries","true");
        properties.put("useSSL","false");
        properties.put("rewriteBatchedStatements","true");//known to cause problems with saves
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/jds?", properties);
    }
}
