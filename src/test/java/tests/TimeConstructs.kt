package tests

import common.BaseTestConfig
import entities.TimeConstruct
import io.github.subiyacryolite.jds.JdsLoad
import io.github.subiyacryolite.jds.JdsSave
import org.junit.jupiter.api.Test
import java.util.*

class TimeConstructs : BaseTestConfig() {
    @Throws(Exception::class)
    override fun save() {
        val timeConstruct = timeConstruct;
        JdsSave(jdsDb, Arrays.asList(timeConstruct)).call()
        println("saved entities [$timeConstruct]")
    }

    @Throws(Exception::class)
    override fun load() {
        val list = JdsLoad(jdsDb, TimeConstruct::class.java, "timeConstruct").call() //load all entities of type AddressBook with Entity Guids in range
        println("loaded entities [$list]")
    }


    @Test
    @Throws(Exception::class)
    fun testSqlite() {
        initialiseSqlLiteBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun testTsql() {
        initialiseTSqlBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun testMysSql() {
        initialiseMysqlBackend()
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
        testMysSql()
        testOracle()
        testPostgreSql()
        testSqlite()
        testTsql()
    }
}