/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectivity

import io.github.subiyacryolite.jds.JdsDbTransactionalSql
import java.io.File
import java.io.FileInputStream
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*

/**
 * @author indana
 */
class JdsDbTransactionalSqllmplementation : JdsDbTransactionalSql() {

    @Throws(ClassNotFoundException::class, SQLException::class)
    override fun getConnection(): Connection {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
        val properties = Properties()
        FileInputStream(File("dbsettings.properties")).use { properties.load(it) }
        //return DriverManager.getConnection("jdbc:sqlserver://BROADREACH4\\SMARTCARE40;databaseName=jds", properties)
        return DriverManager.getConnection("jdbc:sqlserver://DESKTOP-64C7FRP\\JDSINSTANCE;databaseName=jds", properties);
    }
}
