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
import java.sql.SQLException

/**
 *
 * @author indana
 */
class JdsDbSqliteImplementation : JdsDbSqlite() {

    val databaseFile: String
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

    @Throws(ClassNotFoundException::class, SQLException::class)
    override fun getConnection(): Connection {
        val url = "jdbc:sqlite:" + databaseFile
        val sqLiteConfig = SQLiteConfig()
        sqLiteConfig.enforceForeignKeys(true) //You must enable foreign keys in SQLite
        Class.forName("org.sqlite.JDBC")
        return DriverManager.getConnection(url, sqLiteConfig.toProperties())
    }
}
