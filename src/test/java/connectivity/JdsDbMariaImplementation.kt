/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectivity

import io.github.subiyacryolite.jds.JdsDbMaria
import java.io.File
import java.io.FileInputStream
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

/**
 * @author indana
 */
class JdsDbMariaImplementation : JdsDbMaria() {

    override val connection: Connection
        get () {
            Class.forName("org.mariadb.jdbc.Driver")
            val properties = Properties()
            FileInputStream(File("db.maria.properties")).use { properties.load(it) }
            return DriverManager.getConnection("jdbc:mariadb://localhost:3307/jds", properties)
        }
}