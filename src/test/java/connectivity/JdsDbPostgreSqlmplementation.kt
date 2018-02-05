package connectivity

import io.github.subiyacryolite.jds.JdsDbPostgreSql

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
        properties["user"] = "postgres"
        properties["password"] = "postgres"
        //properties["password"] = "m7r@n$4mAz"
        properties["rewriteBatchedStatements"] = "true"
        return DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/jds", properties)
    }
}
