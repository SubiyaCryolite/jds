/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectivity

import io.github.subiyacryolite.jds.JdsDbMySql
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*

/**
 * @author indana
 */
class JdsDbMySqlImplementation : JdsDbMySql() {

    @Throws(ClassNotFoundException::class, SQLException::class)
    override fun getConnection(): Connection {
        Class.forName("com.mysql.cj.jdbc.Driver")
        val properties = Properties()
        properties.put("user", "root")
        //properties.put("password", "p@nkP#55W0rd");
        properties.put("password", "")
        properties.put("autoReconnect", "true")
        properties.put("allowMultiQueries", "false")
        properties.put("useSSL", "false")
        properties.put("rewriteBatchedStatements", "true")
        properties.put("continueBatchOnError", "true")
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/jds", properties)
    }
}
