package tests

import common.BaseTestConfig
import constants.Fields
import entities.Address
import io.github.subiyacryolite.jds.JdsFilter
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors

/**
 * Created by ifunga on 05/03/2017.
 */
class Filters : BaseTestConfig() {

    @Test
    @Throws(Exception::class)
    fun sqLiteImplementation() {
        initialiseSqlLiteBackend()
        basicQuery()
    }

    @Test
    @Throws(Exception::class)
    fun mysqlImplementation() {
        initialiseMysqlBackend()
        basicQuery()
    }

    @Test
    @Throws(Exception::class)
    fun postgreSqlImplementation() {
        initialisePostgeSqlBackend()
        basicQuery()
    }

    @Test
    @Throws(Exception::class)
    fun tSqlImplementation() {
        initialiseTSqlBackend()
        basicQuery()
    }

    @Test
    @Throws(Exception::class)
    fun oracleImplementation() {
        initialiseOracleBackend()
        basicQuery()
    }

    @Test
    @Throws(Exception::class)
    fun allImplementations() {
        mysqlImplementation()
        oracleImplementation()
        postgreSqlImplementation()
        sqLiteImplementation()
        tSqlImplementation()
    }

    @Throws(Exception::class)
    private fun basicQuery() {
        val filter = JdsFilter(jdsDb, Address::class.java).between(Fields.PLOT_NUMBER, 1, 2).like(Fields.COUNTRY_NAME, "Zam").or().equals(Fields.PROVINCE_NAME, "Copperbelt")
        val process = Executors.newSingleThreadExecutor().submit(filter)
        while (!process.isDone)
            Thread.sleep(16)
        println(process.get())
    }
}
