package tests

import common.BaseTestConfig
import io.github.subiyacryolite.jds.JdsSave
import io.github.subiyacryolite.jds.JdsTable
import org.junit.jupiter.api.Test
import java.util.*


class CustomReportJson : BaseTestConfig() {

    @Throws(Exception::class)
    private fun test() {
        val xmlString = Thread.currentThread().contextClassLoader.getResourceAsStream("CustomReport.json").use { it.bufferedReader().readText() }
        val customTable = objectMapper.readValue(xmlString, JdsTable::class.java)
        jdsDb.mapTable(customTable)
        jdsDb.prepareTables()


        val sav = JdsSave(jdsDb, Arrays.asList(sampleAddressBook))
        sav.call()
    }

    @Test
    @Throws(Exception::class)
    fun postreSqlImplementation() {
        initialisePostgeSqlBackend()
        test()
    }

    @Test
    @Throws(Exception::class)
    fun tSqlImplementation() {
        initialiseTSqlBackend()
        test()
    }

    @Test
    @Throws(Exception::class)
    fun sqliteImplementation() {
        initialiseSqlLiteBackend()
        test()
    }

    @Test
    @Throws(Exception::class)
    fun mySqlImplementation() {
        initialiseMysqlBackend()
        test()
    }

    @Test
    @Throws(Exception::class)
    fun oracleImplementation() {
        initialiseOracleBackend()
        test()
    }

    @Test
    @Throws(Exception::class)
    fun allImplementations() {
        tSqlImplementation()
        postreSqlImplementation()
        mySqlImplementation()
        sqliteImplementation()
        oracleImplementation()
    }

}