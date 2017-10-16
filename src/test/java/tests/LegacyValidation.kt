package tests

import common.BaseTestConfig
import entities.Example
import io.github.subiyacryolite.jds.JdsLoad
import io.github.subiyacryolite.jds.JdsSave
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Created by indana on 5/17/2017.
 */
class LegacyValidation : BaseTestConfig() {
    @Test
    @Throws(Exception::class)
    override fun saveAndLoad() {
        val memObjects = collection
        JdsSave.save(jdsDb, 1, memObjects)
        System.out.printf("Saved %s\n", memObjects)

        val savObjects = JdsLoad.load(jdsDb, Example::class.java) //load all entities of type AddressBook
        val specificObject = JdsLoad.load(jdsDb, Example::class.java, "instance4") //load all entities of type AddressBook with Entity Guids in range
        System.out.printf("All entities [%s]\n", savObjects)
        System.out.printf("Specific entities [%s]\n", specificObject)

        val srcA = savObjects.parallelStream().filter { it.overview.entityGuid == "instance1" }.findAny()
        val srcB = memObjects.parallelStream().filter { it.overview.entityGuid == "instance1" }.findAny()
        val srcC = savObjects.parallelStream().filter { it.overview.entityGuid == "instance2" }.findAny()
        val srcD = memObjects.parallelStream().filter { it.overview.entityGuid == "instance2" }.findAny()
        val srcE = savObjects.parallelStream().filter { it.overview.entityGuid == "instance3" }.findAny()
        val srcF = memObjects.parallelStream().filter { it.overview.entityGuid == "instance3" }.findAny()
        val srcG = savObjects.parallelStream().filter { it.overview.entityGuid == "instance4" }.findAny()
        val srcH = memObjects.parallelStream().filter { it.overview.entityGuid == "instance4" }.findAny()

        match(srcA, srcB)
        match(srcC, srcD)
        match(srcE, srcF)
        match(srcG, srcH)
    }

    private fun match(srcA: Optional<Example>, srcB: Optional<Example>) {
        assertTrue(srcA.isPresent, "srcA is not present")
        assertTrue(srcB.isPresent, "srcB is not present")
        //date, localTime, zonedDateTime and localDateTime is accurate up to seconds [not nanoseconds]
        assertEquals(srcA.get().dateField.year, srcB.get().dateField.year, "DateField getYear " + srcA.get().overview.entityGuid)
        assertEquals(srcA.get().dateField.month, srcB.get().dateField.month, "DateField getMonth " + srcA.get().overview.entityGuid)
        assertEquals(srcA.get().dateField.dayOfMonth, srcB.get().dateField.dayOfMonth, "DateField getDayOfMonth " + srcA.get().overview.entityGuid)

        assertEquals(srcA.get().timeField.hour, srcB.get().timeField.hour, "TimeField getHour " + srcA.get().overview.entityGuid)
        assertEquals(srcA.get().timeField.minute, srcB.get().timeField.minute, "TimeField getMinute " + srcA.get().overview.entityGuid)
        assertEquals(srcA.get().timeField.second, srcB.get().timeField.second, "TimeField getSecond " + srcA.get().overview.entityGuid)

        assertEquals(srcA.get().dateTimeField.year, srcB.get().dateTimeField.year, "DateTimeField getYear " + srcA.get().overview.entityGuid)
        assertEquals(srcA.get().dateTimeField.month, srcB.get().dateTimeField.month, "DateTimeField getMonth " + srcA.get().overview.entityGuid)
        assertEquals(srcA.get().dateTimeField.dayOfMonth, srcB.get().dateTimeField.dayOfMonth, "DateTimeField getDayOfMonth " + srcA.get().overview.entityGuid)
        assertEquals(srcA.get().dateTimeField.hour, srcB.get().dateTimeField.hour, "DateTimeField getHour " + srcA.get().overview.entityGuid)
        assertEquals(srcA.get().dateTimeField.minute, srcB.get().dateTimeField.minute, "DateTimeField getMinute " + srcA.get().overview.entityGuid)
        assertEquals(srcA.get().dateTimeField.second, srcB.get().dateTimeField.second, "DateTimeField getSecond " + srcA.get().overview.entityGuid)

        assertEquals(srcA.get().zonedDateTimeField.year, srcB.get().zonedDateTimeField.year, "ZonedDateTimeField getYear " + srcA.get().overview.entityGuid)
        assertEquals(srcA.get().zonedDateTimeField.month, srcB.get().zonedDateTimeField.month, "ZonedDateTimeField getMonth " + srcA.get().overview.entityGuid)
        assertEquals(srcA.get().zonedDateTimeField.dayOfMonth, srcB.get().zonedDateTimeField.dayOfMonth, "ZonedDateTimeField getDayOfMonth " + srcA.get().overview.entityGuid)
        assertEquals(srcA.get().zonedDateTimeField.hour, srcB.get().zonedDateTimeField.hour, "ZonedDateTimeField getHour " + srcA.get().overview.entityGuid)
        assertEquals(srcA.get().zonedDateTimeField.minute, srcB.get().zonedDateTimeField.minute, "ZonedDateTimeField getMinute " + srcA.get().overview.entityGuid)
        assertEquals(srcA.get().zonedDateTimeField.second, srcB.get().zonedDateTimeField.second, "ZonedDateTimeField getSecond " + srcA.get().overview.entityGuid)
        assertEquals(srcA.get().zonedDateTimeField.zone, srcB.get().zonedDateTimeField.zone, "ZonedDateTimeField getZone " + srcA.get().overview.entityGuid)
        assertEquals(srcA.get().zonedDateTimeField.offset, srcB.get().zonedDateTimeField.offset, "ZonedDateTimeField getOffset " + srcA.get().overview.entityGuid)

        assertEquals(srcA.get().stringField, srcB.get().stringField, "StringField " + srcA.get().overview.entityGuid)
        assertEquals(srcA.get().intField, srcB.get().intField, "IntField " + srcA.get().overview.entityGuid)
        assertEquals(srcA.get().floatField, srcB.get().floatField, FLOAT_DELTA, "FloatField " + srcA.get().overview.entityGuid)
        assertEquals(srcA.get().doubleField, srcB.get().doubleField, DOUBLE_DELTA, "DoubleField " + srcA.get().overview.entityGuid)
        assertEquals(srcA.get().longField, srcB.get().longField, "LongField " + srcA.get().overview.entityGuid)
        assertEquals(srcA.get().booleanField, srcB.get().booleanField, "BooleanField " + srcA.get().overview.entityGuid)
    }

    @Test
    @Throws(Exception::class)
    fun postreSqlImplementation() {
        initialisePostgeSqlBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun tsqlImplementation() {
        initialiseTSqlBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun sqliteImplementation() {
        initialiseSqlLiteBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun mySqlImplementation() {
        initialiseMysqlBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun oracleImplementation() {
        initialiseOracleBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun saveAndLoadAllImplementations() {
        sqliteImplementation()
        tsqlImplementation()
        postreSqlImplementation()
        oracleImplementation()
        mySqlImplementation()
    }
}
