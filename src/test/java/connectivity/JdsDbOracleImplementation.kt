package connectivity

import io.github.subiyacryolite.jds.JdsDbOracle
import java.io.File
import java.io.FileInputStream
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

/**
 * @author indana
 */
class JdsDbOracleImplementation : JdsDbOracle() {

    override val connection: Connection
        get () {
            Class.forName("oracle.jdbc.driver.OracleDriver")
            val properties = Properties()
            FileInputStream(File("db.ora.properties")).use { properties.load(it) }
            return DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", properties)
        }
}
