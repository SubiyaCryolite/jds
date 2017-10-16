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
    fun createFlatTableAllImplementations() {
        createFlatTableMysql()
        createFlatTablePostgres()
        createFlatTableTSql()
        createFlatTableOracle()
    }

    @Test
    @Throws(SQLException::class, ClassNotFoundException::class)
    fun createFlatTableOracle() {
        initialiseOracleBackend()
        for (entry in jdsDb.mappedClasses) {
            val delete = JdsView.delete(entry, jdsDb)
            val create = JdsView.create(entry, jdsDb)
        }
    }

    @Test
    @Throws(SQLException::class, ClassNotFoundException::class)
    fun createFlatTableTSql() {
        initialiseTSqlBackend()
        for (entry in jdsDb.mappedClasses) {
            val delete = JdsView.delete(entry, jdsDb)
            val create = JdsView.create(entry, jdsDb)
        }
    }

    @Test
    @Throws(SQLException::class, ClassNotFoundException::class)
    fun createFlatTablePostgres() {
        initialisePostgeSqlBackend()
        for (entry in jdsDb.mappedClasses) {
            val delete = JdsView.delete(entry, jdsDb)
            val create = JdsView.create(entry, jdsDb)
        }
    }

    @Test
    @Throws(SQLException::class, ClassNotFoundException::class)
    fun createFlatTableMysql() {
        initialiseMysqlBackend()
        for (entry in jdsDb.mappedClasses) {
            val delete = JdsView.delete(entry, jdsDb)
            val create = JdsView.create(entry, jdsDb)
        }
    }
}