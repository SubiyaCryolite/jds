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
package io.github.subiyacryolite.jds.tests.common

import io.github.subiyacryolite.jds.tests.entities.*
import io.github.subiyacryolite.jds.Entity
import java.time.*
import java.util.ArrayList

object TestData {
    val addressBook: AddressBook
        get() {
            val primaryAddress = Address()
            primaryAddress.overview.id = "primaryAddress" //custom uuid
            primaryAddress.area = "Norte Broad"
            primaryAddress.city = "Livingstone"
            primaryAddress.country = "Zambia"
            primaryAddress.plotNumber = null
            primaryAddress.provinceOrState = "Southern"
            primaryAddress.streetName = "East Street"
            primaryAddress.timeOfEntry = LocalDateTime.now()
            primaryAddress.primaryAddress = true

            val secondAddress = Address()
            secondAddress.overview.id = "secondAddress"
            secondAddress.area = "Roma"
            secondAddress.city = "Lusaka"
            secondAddress.country = "Zambia"
            secondAddress.plotNumber = 2
            secondAddress.provinceOrState = "Lusaka"
            secondAddress.streetName = "West Street"
            secondAddress.timeOfEntry = LocalDateTime.now().minusMonths(2)
            secondAddress.primaryAddress = false

            val thirdAddress = Address()
            thirdAddress.overview.id = "thirdAddress"
            thirdAddress.area = "Riverdale"
            thirdAddress.city = "Ndola"
            thirdAddress.country = "Zambia"
            thirdAddress.plotNumber = 9
            thirdAddress.provinceOrState = "Copperbelt"
            thirdAddress.streetName = "West Street"
            thirdAddress.timeOfEntry = LocalDateTime.now().minusDays(3)
            thirdAddress.primaryAddress = null

            val addressBook = AddressBook()
            addressBook.overview.id = "testGuid0001"
            addressBook.addresses.add(primaryAddress)
            addressBook.addresses.add(secondAddress)
            addressBook.addresses.add(thirdAddress)
            return addressBook
        }

    val timeConstruct: TimeConstruct
        get() {
            val timeConstruct = TimeConstruct()
            timeConstruct.overview.id = "timeConstruct"
            timeConstruct.duration = Duration.ofDays(2).minusHours(4)
            timeConstruct.monthDay = MonthDay.of(Month.JULY, 1)
            timeConstruct.yearMonth = YearMonth.of(1991, Month.OCTOBER)
            timeConstruct.period = Period.ofYears(4).minusMonths(4).minusDays(12)
            return timeConstruct;
        }

    val inheritanceCollection: List<Entity>
        get() {
            val collection = ArrayList<Entity>()

            val entitya = EntityA()
            entitya.overview.id = "entityA"
            entitya.fieldA = "entity A - ValueA"

            val entityb = EntityB()
            entityb.overview.id = "entityB"
            entityb.fieldA = "entity B - Value A"
            entityb.fieldB = "entity B - Value B"

            val entityc = EntityC()
            entityc.overview.id = "entityC"
            entityc.fieldA = "entity C - Value A"
            entityc.fieldB = "entity C - Value B"
            entityc.fieldC = "entity C - Value C"

            collection.add(entitya)
            collection.add(entityb)
            collection.add(entityc)

            return collection
        }

    val collection: List<Example>
        get() {
            val instance1 = Example()
            instance1.stringField = "One"
            instance1.timeField = LocalTime.of(15, 24)
            instance1.dateField = LocalDate.of(2012, 8, 26)
            instance1.dateTimeField = LocalDateTime.of(1991, 7, 1, 8, 33, 12)
            instance1.zonedDateTimeField = ZonedDateTime.of(LocalDateTime.now().minusMonths(3), ZoneId.systemDefault())
            instance1.intField = 99
            instance1.longField = 888
            instance1.doubleField = 777.666
            instance1.floatField = 5555.4444f
            instance1.booleanField = true
            instance1.overview.id = "instance1"

            val instance2 = Example()
            instance2.stringField = "Two"
            instance2.timeField = LocalTime.of(19, 24)
            instance2.dateField = LocalDate.of(2011, 4, 2)
            instance2.dateTimeField = LocalDateTime.of(1999, 2, 21, 11, 13, 43)
            instance2.zonedDateTimeField = ZonedDateTime.of(LocalDateTime.now().minusMonths(7), ZoneId.systemDefault())
            instance2.intField = 66
            instance2.longField = 555
            instance2.doubleField = 444.333
            instance2.floatField = 2222.1111f
            instance2.booleanField = false
            instance2.overview.id = "instance2"

            val instance3 = Example()
            instance3.stringField = "Three"
            instance3.timeField = LocalTime.of(3, 14)
            instance3.dateField = LocalDate.of(2034, 6, 14)
            instance3.dateTimeField = LocalDateTime.of(1987, 7, 24, 13, 22, 45)
            instance3.zonedDateTimeField = ZonedDateTime.of(LocalDateTime.now().plusDays(3), ZoneId.systemDefault())
            instance3.intField = 22
            instance3.longField = 333
            instance3.doubleField = 444.555
            instance3.floatField = 5555.6666f
            instance3.booleanField = true
            instance3.overview.id = "instance3"

            val instance4 = Example()
            instance4.stringField = "Four"
            instance4.timeField = LocalTime.of(12, 44)
            instance4.dateField = LocalDate.of(3034, 12, 1)
            instance4.dateTimeField = LocalDateTime.of(1964, 10, 24, 2, 12, 14)
            instance4.zonedDateTimeField = ZonedDateTime.of(LocalDateTime.now().minusDays(3), ZoneId.systemDefault())
            instance4.intField = 10
            instance4.longField = 100
            instance4.doubleField = 100.22
            instance4.floatField = 1000.0f
            instance4.booleanField = false
            instance4.overview.id = "instance4"

            val allInstances = ArrayList<Example>()
            allInstances.add(instance1)
            allInstances.add(instance2)
            allInstances.add(instance3)
            allInstances.add(instance4)
            return allInstances
        }
}