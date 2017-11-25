package tests

import common.BaseTestConfig
import entities.Example
import io.github.subiyacryolite.jds.JdsLoad
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors

class NonExisting : BaseTestConfig() {

    @Throws(Exception::class)
    fun loadNonExisting() {
        val load = JdsLoad(jdsDb, Example::class.java, "DOES_NOT_EXIST")
        val process = Executors.newSingleThreadExecutor().submit(load)
        while (!process.isDone)
            Thread.sleep(16)
        println(process.get())
    }

    @Test
    @Throws(Exception::class)
    fun sqlLiteImplementation() {
        initialiseSqlLiteBackend()
        loadNonExisting()
    }


    @Test
    @Throws(Exception::class)
    fun mysqlImplementation() {
        initialiseMysqlBackend()
        loadNonExisting()
    }

    @Test
    @Throws(Exception::class)
    fun postgeSqlImplementation() {
        initialisePostgeSqlBackend()
        loadNonExisting()
    }

    @Test
    @Throws(Exception::class)
    fun tSqlImplementation() {
        initialiseTSqlBackend()
        loadNonExisting()
    }

    @Test
    @Throws(Exception::class)
    fun oracleImplementation() {
        initialiseOracleBackend()
        loadNonExisting()
    }

    @Test
    @Throws(Exception::class)
    fun allImplementations() {
        sqlLiteImplementation()
        mysqlImplementation()
        postgeSqlImplementation()
        tSqlImplementation()
        oracleImplementation()
    }
}