package connectivity

import io.github.subiyacryolite.jds.JdsDbPostgreSql
import java.io.File
import java.io.FileInputStream

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.Properties

/**
 * @author indana
 */
class JdsDbPostgreSqlmplementation : JdsDbPostgreSql() {

    @Throws(ClassNotFoundException::class, SQLException::class)
    override fun getConnection(): Connection {
        Class.forName("org.postgresql.Driver")
        val properties = Properties()
        FileInputStream(File("dbsettings.properties")).use { properties.load(it) }
        return DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/jds", properties)
    }
}
