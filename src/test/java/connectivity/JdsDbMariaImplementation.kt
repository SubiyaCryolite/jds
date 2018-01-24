/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectivity

import io.github.subiyacryolite.jds.JdsDbMaria
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*

/**
 * @author indana
 */
class JdsDbMariaImplementation : JdsDbMaria() {

    @Throws(ClassNotFoundException::class, SQLException::class)
    override fun getConnection(): Connection {
        Class.forName("org.mariadb.jdbc.Driver")
        val properties = Properties()
        properties["user"] = "root"
        properties["password"] = "p@nkP#55W0rd";
        properties["autoReconnect"] = "true"
        properties["allowMultiQueries"] = "false"
        properties["useSSL"] = "false"
        properties["rewriteBatchedStatements"] = "true"
        properties["continueBatchOnError"] = "true"
        return DriverManager.getConnection("jdbc:mariadb://localhost:3307/jds", properties)
    }
}