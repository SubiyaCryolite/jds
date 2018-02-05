/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectivity

import io.github.subiyacryolite.jds.JdsDbMySql
import java.io.File
import java.io.FileInputStream
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
        FileInputStream(File("dbsettings.properties")).use { properties.load(it) }
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/jds", properties)
    }
}