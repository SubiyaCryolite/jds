package tests

import common.BaseTestConfig
import io.github.subiyacryolite.jds.JdsSave
import io.github.subiyacryolite.jds.JdsTable
import org.junit.jupiter.api.Test
import java.util.*


class CustomReportJson : BaseTestConfig("Custom reports from json") {

    @Throws(Exception::class)
    private fun test() {
        val xmlString = Thread.currentThread().contextClassLoader.getResourceAsStream("CustomReport.json").use { it.bufferedReader().readText() }
        val customTable = objectMapper.readValue(xmlString, JdsTable::class.java)
        jdsDb.mapTable(customTable)
        jdsDb.prepareTables()


        val jdsSave = JdsSave(jdsDb, Arrays.asList(addressBook))
        jdsSave.call()
    }

    @Test
    @Throws(Exception::class)
    fun testPostgreSql() {
        initialisePostgeSqlBackend()
        test()
    }

    @Test
    @Throws(Exception::class)
    fun testTransactionalSql() {
        initialiseTSqlBackend()
        test()
    }

    @Test
    @Throws(Exception::class)
    fun testSqLite() {
        initialiseSqLiteBackend()
        test()
    }

    @Test
    @Throws(Exception::class)
    fun testMySql() {
        initialiseMysqlBackend()
        test()
    }

    @Test
    @Throws(Exception::class)
    fun testMariaDb() {
        initialiseMariaDbBackend()
        test()
    }

    @Test
    @Throws(Exception::class)
    fun testOracle() {
        initialiseOracleBackend()
        test()
    }

    @Test
    @Throws(Exception::class)
    fun allImplementations() {
        testTransactionalSql()
        testPostgreSql()
        testMySql()
        testOracle()
        testSqLite()
        testMariaDb()
    }

}