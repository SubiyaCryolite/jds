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
    fun testSqLite() {
        initialiseSqlLiteBackend()
        loadNonExisting()
    }


    @Test
    @Throws(Exception::class)
    fun testMySql() {
        initialiseMysqlBackend()
        loadNonExisting()
    }

    @Test
    @Throws(Exception::class)
    fun testPostgreSql() {
        initialisePostgeSqlBackend()
        loadNonExisting()
    }

    @Test
    @Throws(Exception::class)
    fun testTransactionalSql() {
        initialiseTSqlBackend()
        loadNonExisting()
    }

    @Test
    @Throws(Exception::class)
    fun testOracle() {
        initialiseOracleBackend()
        loadNonExisting()
    }

    @Test
    @Throws(Exception::class)
    fun allImplementations() {
        testSqLite()
        testMySql()
        testPostgreSql()
        testTransactionalSql()
        testOracle()
    }
}