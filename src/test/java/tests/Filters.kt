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

    @Throws(Exception::class)
    private fun filter() {
        val filter = JdsFilter(jdsDb, Address::class.java).between(Fields.PLOT_NUMBER, 1, 2).like(Fields.COUNTRY_NAME, "Zam").or().equals(Fields.PROVINCE_NAME, "Copperbelt")
        val process = Executors.newSingleThreadExecutor().submit(filter)
        while (!process.isDone)
            Thread.sleep(16)
        println(process.get())
    }

    @Test
    @Throws(Exception::class)
    fun sqLiteImplementation() {
        initialiseSqLiteBackend()
        filter()
    }

    @Test
    @Throws(Exception::class)
    fun mysqlImplementation() {
        initialiseMysqlBackend()
        filter()
    }

    @Test
    @Throws(Exception::class)
    fun mariaDbImplementation() {
        initialiseMariaDbBackend()
        filter()
    }

    @Test
    @Throws(Exception::class)
    fun postgreSqlImplementation() {
        initialisePostgeSqlBackend()
        filter()
    }

    @Test
    @Throws(Exception::class)
    fun tSqlImplementation() {
        initialiseTSqlBackend()
        filter()
    }

    @Test
    @Throws(Exception::class)
    fun oracleImplementation() {
        initialiseOracleBackend()
        filter()
    }

    @Test
    @Throws(Exception::class)
    fun allImplementations() {
        mysqlImplementation()
        oracleImplementation()
        postgreSqlImplementation()
        sqLiteImplementation()
        tSqlImplementation()
        mariaDbImplementation()
    }
}
