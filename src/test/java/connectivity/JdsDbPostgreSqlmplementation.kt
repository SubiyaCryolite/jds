package connectivity

import io.github.subiyacryolite.jds.JdsDbPostgreSql
import java.io.File
import java.io.FileInputStream
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

/**
 * @author indana
 */
class JdsDbPostgreSqlmplementation : JdsDbPostgreSql() {

    override val connection: Connection
        get () {
            Class.forName("org.postgresql.Driver")
            val properties = Properties()
            FileInputStream(File("db.pg.properties")).use { properties.load(it) }
            return DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/jds", properties)
        }
}
