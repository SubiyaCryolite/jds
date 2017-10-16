package tests

import common.BaseTestConfig
import constants.Fields
import entities.Address
import io.github.subiyacryolite.jds.JdsFilter
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * Created by ifunga on 05/03/2017.
 */
class Filters : BaseTestConfig() {

    @Test
    @Throws(Exception::class)
    fun querySqlite() {
        initialiseSqlLiteBackend()
        basicQuery()
    }

    @Test
    @Throws(Exception::class)
    fun queryMysql() {
        initialiseMysqlBackend()
        basicQuery()
    }

    @Test
    @Throws(Exception::class)
    fun queryPostgres() {
        initialisePostgeSqlBackend()
        basicQuery()
    }

    @Test
    @Throws(Exception::class)
    fun queryTsql() {
        initialiseTSqlBackend()
        basicQuery()
    }

    @Test
    @Throws(Exception::class)
    fun queryOracle() {
        initialiseOracleBackend()
        basicQuery()
    }

    @Test
    @Throws(Exception::class)
    fun queryAll() {
        queryMysql()
        queryOracle()
        queryPostgres()
        querySqlite()
        queryTsql()
    }

    @Throws(Exception::class)
    private fun basicQuery() {
        System.out.printf("=========== %s ===========\n", jdsDb.implementation)
        val filter = JdsFilter(jdsDb, Address::class.java).between(Fields.PLOT_NUMBER, 1, 2).like(Fields.COUNTRY_NAME, "Zam").or().equals(Fields.PROVINCE_NAME, "Copperbelt")
        val output = filter.call()
        assertNotNull(output)
        println(output)
    }
}
