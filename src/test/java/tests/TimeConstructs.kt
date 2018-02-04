package tests

import common.BaseTestConfig
import entities.TimeConstruct
import io.github.subiyacryolite.jds.JdsLoad
import io.github.subiyacryolite.jds.JdsSave
import org.junit.jupiter.api.Test
import java.util.*

class TimeConstructs : BaseTestConfig() {
    @Throws(Exception::class)
    private fun save() {
        val timeConstruct = timeConstruct;
        JdsSave(jdsDb, Arrays.asList(timeConstruct)).call()
        println("saved entityVersions [$timeConstruct]")
    }

    @Throws(Exception::class)
    private fun load() {
        val list = JdsLoad(jdsDb, TimeConstruct::class.java, setOf("timeConstruct")).call() //load all entityVersions of type AddressBook with Entity Guids in range
        println("loaded entityVersions [$list]")
    }

    @Throws(Exception::class)
    private fun saveAndLoad() {
        save()
        load()
    }

    @Test
    @Throws(Exception::class)
    fun testSqLite() {
        initialiseSqLiteBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun testTransactionalSql() {
        initialiseTSqlBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun testMySql() {
        initialiseMysqlBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun testMariaDb() {
        initialiseMariaDbBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun testPostgreSql() {
        initialisePostgeSqlBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun testOracle() {
        initialiseOracleBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun testAllInitialilization() {
        testMySql()
        testOracle()
        testPostgreSql()
        testSqLite()
        testTransactionalSql()
        testMariaDb()
    }
}