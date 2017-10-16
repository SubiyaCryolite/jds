package tests

import common.BaseTestConfig
import entities.Address
import entities.TimeConstruct
import io.github.subiyacryolite.jds.JdsLoad
import io.github.subiyacryolite.jds.JdsSave
import org.junit.jupiter.api.Test
import java.time.*
import java.util.*

class TimeConstructs : BaseTestConfig() {
    @Throws(Exception::class)
    override fun save() {
        val timeConstruct = TimeConstruct()
        timeConstruct.overview.entityGuid = "timeConstruct"
        timeConstruct.duration = Duration.ofDays(2).minusHours(4)
        timeConstruct.monthDay = MonthDay.of(Month.JULY, 1)
        timeConstruct.yearMonth = YearMonth.of(1991, Month.OCTOBER)
        timeConstruct.period = Period.ofYears(4).minusMonths(4).minusDays(12)
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
        initialiseSqlLiteBackend()
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