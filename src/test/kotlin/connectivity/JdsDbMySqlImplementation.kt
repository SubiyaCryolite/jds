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
import java.util.*

class JdsDbMySqlImplementation : JdsDbMySql() {

    private val properties: Properties = Properties()
    private val connectionString: String

    init {
        FileInputStream(File("db.mysql.properties")).use { properties.load(it) }
        connectionString = "jdbc:mysql://${properties["dbUrl"]}:${properties["dbPort"]}/${properties["dbName"]}"
    }

    override val connection: Connection
        get () {
            Class.forName("com.mysql.cj.jdbc.Driver")
            return DriverManager.getConnection(connectionString, properties)
        }
}