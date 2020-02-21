/**
 * Jenesis Data Store Copyright (c) 2017 Ifunga Ndana. All rights reserved.
 *
 * 1. Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 2. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 3. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * Neither the name Jenesis Data Store nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package io.github.subiyacryolite.jds.tests

import io.github.subiyacryolite.jds.tests.common.BaseTestConfig
import io.github.subiyacryolite.jds.tests.common.TestData
import io.github.subiyacryolite.jds.tests.entities.Example
import io.github.subiyacryolite.jds.context.DbContext
import io.github.subiyacryolite.jds.Load
import io.github.subiyacryolite.jds.Save
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LegacyValidation : BaseTestConfig("Legacy validation") {

    @Throws(Exception::class)
    override fun testImpl(dbContext: DbContext) {
        val memObjects = TestData.collection
        Save(dbContext, memObjects).call()
        System.out.printf("saved objects %s\n", memObjects)
        val savObjects = Load(dbContext, Example::class.java).call() //load all entityVersions of type AddressBook
        System.out.printf("loaded objects [%s]\n", savObjects)

        val srcA = savObjects.firstOrNull { it.overview.id == "instance1" }
        val srcB = memObjects.firstOrNull { it.overview.id == "instance1" }

        val srcC = savObjects.firstOrNull { it.overview.id == "instance2" }
        val srcD = memObjects.firstOrNull { it.overview.id == "instance2" }

        val srcE = savObjects.firstOrNull { it.overview.id == "instance3" }
        val srcF = memObjects.firstOrNull { it.overview.id == "instance3" }

        val srcG = savObjects.firstOrNull { it.overview.id == "instance4" }
        val srcH = memObjects.firstOrNull { it.overview.id == "instance4" }

        match(dbContext, srcA, srcB)
        match(dbContext, srcC, srcD)
        match(dbContext, srcE, srcF)
        match(dbContext, srcG, srcH)
    }

    private fun match(dbContext: DbContext, srcA: Example?, srcB: Example?) {
        if (srcA == null) return
        if (srcB == null) return
        //date, localTime, zonedDateTime and localDateTime is accurate up to seconds [not nanoseconds]
        assertEquals(srcA.dateField.year, srcB.dateField.year, "DateField::Year " + srcA.overview.id)
        assertEquals(srcA.dateField.month, srcB.dateField.month, "DateField::Month " + srcA.overview.id)
        assertEquals(srcA.dateField.dayOfMonth, srcB.dateField.dayOfMonth, "DateField::DayOfMonth " + srcA.overview.id)

        assertEquals(srcA.timeField.hour, srcB.timeField.hour, "TimeField::Hour " + srcA.overview.id)
        assertEquals(srcA.timeField.minute, srcB.timeField.minute, "TimeField::Minute " + srcA.overview.id)
        assertEquals(srcA.timeField.second, srcB.timeField.second, "TimeField::Second " + srcA.overview.id)

        assertEquals(srcA.dateTimeField.year, srcB.dateTimeField.year, "DateTimeField::Year " + srcA.overview.id)
        assertEquals(srcA.dateTimeField.month, srcB.dateTimeField.month, "DateTimeField::Month " + srcA.overview.id)
        assertEquals(srcA.dateTimeField.dayOfMonth, srcB.dateTimeField.dayOfMonth, "DateTimeField::DayOfMonth " + srcA.overview.id)
        assertEquals(srcA.dateTimeField.hour, srcB.dateTimeField.hour, "DateTimeField::Hour " + srcA.overview.id)
        assertEquals(srcA.dateTimeField.minute, srcB.dateTimeField.minute, "DateTimeField::Minute " + srcA.overview.id)
        assertEquals(srcA.dateTimeField.second, srcB.dateTimeField.second, "DateTimeField::Second " + srcA.overview.id)

        assertEquals(srcA.zonedDateTimeField.year, srcB.zonedDateTimeField.year, "ZonedDateTimeField::Year " + srcA.overview.id)
        assertEquals(srcA.zonedDateTimeField.month, srcB.zonedDateTimeField.month, "ZonedDateTimeField::Month " + srcA.overview.id)
        assertEquals(srcA.zonedDateTimeField.dayOfMonth, srcB.zonedDateTimeField.dayOfMonth, "ZonedDateTimeField::DayOfMonth " + srcA.overview.id)
        assertEquals(srcA.zonedDateTimeField.hour, srcB.zonedDateTimeField.hour, "ZonedDateTimeField::Hour " + srcA.overview.id)
        assertEquals(srcA.zonedDateTimeField.minute, srcB.zonedDateTimeField.minute, "ZonedDateTimeField::Minute " + srcA.overview.id)
        if (!dbContext.isMySqlDb)//MYSQL rounds of nanos to millis. Thus seconds may fail
            assertEquals(srcA.zonedDateTimeField.second, srcB.zonedDateTimeField.second, "ZonedDateTimeField::Second " + srcA.overview.id)
        assertEquals(srcA.zonedDateTimeField.offset, srcB.zonedDateTimeField.offset, "ZonedDateTimeField::Offset " + srcA.overview.id)

        assertEquals(srcA.stringField, srcB.stringField, "StringField " + srcA.overview.id)
        assertEquals(srcA.intField, srcB.intField, "IntField " + srcA.overview.id)
        assertEquals(srcA.floatField, srcB.floatField, FLOAT_DELTA, "FloatField " + srcA.overview.id)
        assertEquals(srcA.doubleField, srcB.doubleField, DOUBLE_DELTA, "DoubleField " + srcA.overview.id)
        assertEquals(srcA.longField, srcB.longField, "LongField " + srcA.overview.id)
        assertEquals(srcA.booleanField, srcB.booleanField, "BooleanField " + srcA.overview.id)
    }

    @Test
    fun postGreSql() {
        testPostgreSql()
    }

    @Test
    fun sqlLite() {
        testSqLite()
    }

    @Test
    fun mariaDb() {
        testMariaDb()
    }

    @Test
    fun mySql() {
        testMySql()
    }

    @Test
    fun oracle() {
        testOracle()
    }

    @Test
    fun transactionalSql() {
        testTransactionalSql()
    }
}
