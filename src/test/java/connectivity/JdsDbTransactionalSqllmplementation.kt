/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectivity

import io.github.subiyacryolite.jds.JdsDbTransactionalSql

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.Properties

/**
 * @author indana
 */
class JdsDbTransactionalSqllmplementation : JdsDbTransactionalSql() {

    @Throws(ClassNotFoundException::class, SQLException::class)
    override fun getConnection(): Connection {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
        val properties = Properties()
        properties.put("user", "sa")
        properties.put("password", "p@nkP#55W0rd")
        return DriverManager.getConnection("jdbc:sqlserver://ICT-INDANA\\JDSINSTANCE;databaseName=jds", properties)
        //return DriverManager.getConnection("jdbc:sqlserver://DESKTOP-64C7FRP\\JDSINSTANCE;databaseName=jds", properties);
    }
}
