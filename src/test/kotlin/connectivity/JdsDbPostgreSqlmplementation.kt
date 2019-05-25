package connectivity

import io.github.subiyacryolite.jds.JdsDbPostgreSql
import java.io.File
import java.io.FileInputStream
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

class JdsDbPostgreSqlmplementation : JdsDbPostgreSql() {

    private val properties: Properties = Properties()
    private val connectionString: String

    init {
        FileInputStream(File("db.pg.properties")).use { properties.load(it) }
        connectionString = "jdbc:postgresql://${properties["dbUrl"]}:${properties["dbPort"]}/${properties["dbName"]}"
    }

    override val connection: Connection
        get () {
            Class.forName("org.postgresql.Driver")
            return DriverManager.getConnection(connectionString, properties)
        }
}