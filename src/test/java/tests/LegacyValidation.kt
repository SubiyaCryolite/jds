package tests

import common.BaseTestConfig
import entities.Example
import io.github.subiyacryolite.jds.JdsLoad
import io.github.subiyacryolite.jds.JdsSave
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Created by indana on 5/17/2017.
 */
class LegacyValidation : BaseTestConfig() {

    @Throws(Exception::class)
    private fun saveAndLoad() {
        val memObjects = collection
        JdsSave(jdsDb, memObjects).call()
        System.out.printf("saved objects %s\n", memObjects)
        val savObjects = JdsLoad(jdsDb, Example::class.java).call() //load all entityVersions of type AddressBook
        System.out.printf("loaded objects [%s]\n", savObjects)

        val srcA = savObjects.firstOrNull { it.overview.uuid == "instance1" }
        val srcB = memObjects.firstOrNull { it.overview.uuid == "instance1" }

        val srcC = savObjects.firstOrNull { it.overview.uuid == "instance2" }
        val srcD = memObjects.firstOrNull { it.overview.uuid == "instance2" }

        val srcE = savObjects.firstOrNull { it.overview.uuid == "instance3" }
        val srcF = memObjects.firstOrNull { it.overview.uuid == "instance3" }

        val srcG = savObjects.firstOrNull { it.overview.uuid == "instance4" }
        val srcH = memObjects.firstOrNull { it.overview.uuid == "instance4" }

        match(srcA, srcB)
        match(srcC, srcD)
        match(srcE, srcF)
        match(srcG, srcH)
    }

    private fun match(srcA: Example?, srcB: Example?) {
        if (srcA == null) return
        if (srcB == null) return
        //date, localTime, zonedDateTime and localDateTime is accurate up to seconds [not nanoseconds]
        assertEquals(srcA.dateField.year, srcB.dateField.year, "DateField::Year " + srcA.overview.uuid)
        assertEquals(srcA.dateField.month, srcB.dateField.month, "DateField::Month " + srcA.overview.uuid)
        assertEquals(srcA.dateField.dayOfMonth, srcB.dateField.dayOfMonth, "DateField::DayOfMonth " + srcA.overview.uuid)

        assertEquals(srcA.timeField.hour, srcB.timeField.hour, "TimeField::Hour " + srcA.overview.uuid)
        assertEquals(srcA.timeField.minute, srcB.timeField.minute, "TimeField::Minute " + srcA.overview.uuid)
        assertEquals(srcA.timeField.second, srcB.timeField.second, "TimeField::Second " + srcA.overview.uuid)

        assertEquals(srcA.dateTimeField.year, srcB.dateTimeField.year, "DateTimeField::Year " + srcA.overview.uuid)
        assertEquals(srcA.dateTimeField.month, srcB.dateTimeField.month, "DateTimeField::Month " + srcA.overview.uuid)
        assertEquals(srcA.dateTimeField.dayOfMonth, srcB.dateTimeField.dayOfMonth, "DateTimeField::DayOfMonth " + srcA.overview.uuid)
        assertEquals(srcA.dateTimeField.hour, srcB.dateTimeField.hour, "DateTimeField::Hour " + srcA.overview.uuid)
        assertEquals(srcA.dateTimeField.minute, srcB.dateTimeField.minute, "DateTimeField::Minute " + srcA.overview.uuid)
        assertEquals(srcA.dateTimeField.second, srcB.dateTimeField.second, "DateTimeField::Second " + srcA.overview.uuid)

        assertEquals(srcA.zonedDateTimeField.year, srcB.zonedDateTimeField.year, "ZonedDateTimeField::Year " + srcA.overview.uuid)
        assertEquals(srcA.zonedDateTimeField.month, srcB.zonedDateTimeField.month, "ZonedDateTimeField::Month " + srcA.overview.uuid)
        assertEquals(srcA.zonedDateTimeField.dayOfMonth, srcB.zonedDateTimeField.dayOfMonth, "ZonedDateTimeField::DayOfMonth " + srcA.overview.uuid)
        assertEquals(srcA.zonedDateTimeField.hour, srcB.zonedDateTimeField.hour, "ZonedDateTimeField::Hour " + srcA.overview.uuid)
        assertEquals(srcA.zonedDateTimeField.minute, srcB.zonedDateTimeField.minute, "ZonedDateTimeField::Minute " + srcA.overview.uuid)
        if (!jdsDb.isMySqlDb)//MYSQL rounds of nanos to millis. Thus seconds may fail
            assertEquals(srcA.zonedDateTimeField.second, srcB.zonedDateTimeField.second, "ZonedDateTimeField::Second " + srcA.overview.uuid)
        assertEquals(srcA.zonedDateTimeField.offset, srcB.zonedDateTimeField.offset, "ZonedDateTimeField::Offset " + srcA.overview.uuid)

        assertEquals(srcA.stringField, srcB.stringField, "StringField " + srcA.overview.uuid)
        assertEquals(srcA.intField, srcB.intField, "IntField " + srcA.overview.uuid)
        assertEquals(srcA.floatField!!, srcB.floatField!!, FLOAT_DELTA, "FloatField " + srcA.overview.uuid)
        assertEquals(srcA.doubleField!!, srcB.doubleField!!, DOUBLE_DELTA, "DoubleField " + srcA.overview.uuid)
        assertEquals(srcA.longField, srcB.longField, "LongField " + srcA.overview.uuid)
        assertEquals(srcA.booleanField, srcB.booleanField, "BooleanField " + srcA.overview.uuid)
    }

    @Test
    @Throws(Exception::class)
    fun testPostgreSql() {
        initialisePostgeSqlBackend()
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
    fun testSqLite() {
        initialiseSqLiteBackend()
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
    fun testOracle() {
        initialiseOracleBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun allImplementations() {
        testSqLite()
        testTransactionalSql()
        testOracle()
        testMySql()
        testPostgreSql()
    }
}
