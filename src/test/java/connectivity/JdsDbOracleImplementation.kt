package connectivity

import io.github.subiyacryolite.jds.JdsDbOracle

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.Properties

/**
 * @author indana
 */
class JdsDbOracleImplementation : JdsDbOracle() {

    @Throws(ClassNotFoundException::class, SQLException::class)
    override fun getConnection(): Connection {
        Class.forName("oracle.jdbc.driver.OracleDriver")
        val properties = Properties()
        properties.put("user", "jdsx")
        properties.put("password", "jdsx")
        return DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", properties)
    }
}
