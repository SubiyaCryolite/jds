package tests

import common.BaseTestConfig
import io.github.subiyacryolite.jds.JdsDelete
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors

class Deletes : BaseTestConfig() {

    @Throws(Exception::class)
    private fun delete() {
        val delete = JdsDelete(jdsDb, addressBook)
        val process = Executors.newSingleThreadExecutor().submit(delete)
        while (!process.isDone)
            Thread.sleep(16)
        println("Deleted successfully?  ${process.get()}")
    }

    @Test
    @Throws(Exception::class)
    fun sqLiteImplementation() {
        initialiseSqLiteBackend()
        delete()
    }

    @Test
    @Throws(Exception::class)
    fun mysqlImplementation() {
        initialiseMysqlBackend()
        delete()
    }

    @Test
    @Throws(Exception::class)
    fun mariaDbImplementation() {
        initialiseMariaDbBackend()
        delete()
    }

    @Test
    @Throws(Exception::class)
    fun postgreSqlImplementation() {
        initialisePostgeSqlBackend()
        delete()
    }

    @Test
    @Throws(Exception::class)
    fun tSqlImplementation() {
        initialiseTSqlBackend()
        delete()
    }

    @Test
    @Throws(Exception::class)
    fun oracleImplementation() {
        initialiseOracleBackend()
        delete()
    }

    @Test
    @Throws(Exception::class)
    fun allImplementations() {
        mysqlImplementation()
        oracleImplementation()
        postgreSqlImplementation()
        tSqlImplementation()
        sqLiteImplementation()
        mariaDbImplementation()
    }
}