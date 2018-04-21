/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectivity

import io.github.subiyacryolite.jds.JdsDbSqlite
import org.sqlite.SQLiteConfig
import java.io.File
import java.sql.Connection
import java.sql.DriverManager

/**
 *
 * @author indana
 */
class JdsDbSqliteImplementation : JdsDbSqlite() {

    private val fileLocation: String
        get() {
            val path = File(System.getProperty("user.home") + File.separator + ".jdstest" + File.separator + "jds.db")
            if (!path.exists()) {
                val directory = path.parentFile
                if (!directory.exists()) {
                    directory.mkdirs()
                }
            }
            return path.absolutePath
        }

    override val connection: Connection
        get () {
            val url = "jdbc:sqlite:$fileLocation"
            val sqLiteConfig = SQLiteConfig()
            sqLiteConfig.enforceForeignKeys(true) //You must enable foreign keys in SQLite
            Class.forName("org.sqlite.JDBC")
            return DriverManager.getConnection(url, sqLiteConfig.toProperties())
        }
}
