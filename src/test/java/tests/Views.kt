package tests

import common.BaseTestConfig
import io.github.subiyacryolite.jds.JdsView
import org.junit.jupiter.api.Test
import java.sql.SQLException

/**
 * Created by ifunga on 24/06/2017.
 */
class Views : BaseTestConfig() {

    @Test
    @Throws(SQLException::class, ClassNotFoundException::class)
    fun allImplementation() {
        mysqlImplementation()
        postgresImplementation()
        tSqlImplementation()
        oracleImplementation()
    }

    @Test
    @Throws(SQLException::class, ClassNotFoundException::class)
    fun oracleImplementation() {
        initialiseOracleBackend()
        dropAndRecreateViews()
    }

    @Test
    @Throws(SQLException::class, ClassNotFoundException::class)
    fun tSqlImplementation() {
        initialiseTSqlBackend()
        dropAndRecreateViews()
    }

    @Test
    @Throws(SQLException::class, ClassNotFoundException::class)
    fun postgresImplementation() {
        initialisePostgeSqlBackend()
        dropAndRecreateViews()
    }

    @Test
    @Throws(SQLException::class, ClassNotFoundException::class)
    fun mysqlImplementation() {
        initialiseMysqlBackend()
        dropAndRecreateViews()
    }

    @Throws(SQLException::class, ClassNotFoundException::class)
    private fun dropAndRecreateViews() {
        for (entry in jdsDb.mappedClasses) {
            val delete = JdsView.delete(entry, jdsDb)
            val create = JdsView.create(entry, jdsDb)
        }
        println("Created views :)")
    }
}