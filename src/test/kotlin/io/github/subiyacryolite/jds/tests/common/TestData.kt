package io.github.subiyacryolite.jds.tests.common

import io.github.subiyacryolite.jds.tests.constants.PrimaryAddress
import io.github.subiyacryolite.jds.tests.entities.*
import io.github.subiyacryolite.jds.JdsEntity
import java.time.*
import java.util.ArrayList

object TestData {
    val addressBook: AddressBook
        get() {
            val primaryAddress = Address()
            primaryAddress.overview.uuid = "primaryAddress" //custom uuid
            primaryAddress.area = "Norte Broad"
            primaryAddress.city = "Livingstone"
            primaryAddress.country = "Zambia"
            primaryAddress.plotNumber = null
            primaryAddress.provinceOrState = "Southern"
            primaryAddress.streetName = "East Street"
            primaryAddress.timeOfEntry = LocalTime.now()
            primaryAddress.primaryAddress = PrimaryAddress.YES

            val secondAddress = Address()
            secondAddress.overview.uuid = "secondAddress"
            secondAddress.area = "Roma"
            secondAddress.city = "Lusaka"
            secondAddress.country = "Zambia"
            secondAddress.plotNumber = 2
            secondAddress.provinceOrState = "Lusaka"
            secondAddress.streetName = "West Street"
            secondAddress.timeOfEntry = ZonedDateTime.now().minusMonths(2).toLocalTime()
            secondAddress.primaryAddress = PrimaryAddress.NO

            val thirdAddress = Address()
            thirdAddress.overview.uuid = "thirdAddress"
            thirdAddress.area = "Riverdale"
            thirdAddress.city = "Ndola"
            thirdAddress.country = "Zambia"
            thirdAddress.plotNumber = 9
            thirdAddress.provinceOrState = "Copperbelt"
            thirdAddress.streetName = "West Street"
            thirdAddress.timeOfEntry = ZonedDateTime.now().minusDays(3).toLocalTime()
            thirdAddress.primaryAddress = null

            val addressBook = AddressBook()
            addressBook.overview.uuid = "testGuid0001"
            addressBook.addresses.add(primaryAddress)
            addressBook.addresses.add(secondAddress)
            addressBook.addresses.add(thirdAddress)
            return addressBook
        }

    val timeConstruct: TimeConstruct
        get() {
            val timeConstruct = TimeConstruct()
            timeConstruct.overview.uuid = "timeConstruct"
            timeConstruct.duration = Duration.ofDays(2).minusHours(4)
            timeConstruct.monthDay = MonthDay.of(Month.JULY, 1)
            timeConstruct.yearMonth = YearMonth.of(1991, Month.OCTOBER)
            timeConstruct.period = Period.ofYears(4).minusMonths(4).minusDays(12)
            return timeConstruct;
        }

    val inheritanceCollection: List<JdsEntity>
        get() {
            val collection = ArrayList<JdsEntity>()

            val entitya = EntityA()
            entitya.overview.uuid = "entityA"
            entitya.entityAValue = "entity A - ValueA"

            val entityb = EntityB()
            entityb.overview.uuid = "entityB"
            entityb.entityAValue = "entity B - Value A"
            entityb.entityBValue = "entity B - Value B"

            val entityc = EntityC()
            entityc.overview.uuid = "entityC"
            entityc.entityAValue = "entity C - Value A"
            entityc.entityBValue = "entity C - Value B"
            entityc.entityCValue = "entity C - Value C"

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
            instance1.overview.uuid = "instance1"

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
            instance2.overview.uuid = "instance2"

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
            instance3.overview.uuid = "instance3"

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
            instance4.overview.uuid = "instance4"

            val allInstances = ArrayList<Example>()
            allInstances.add(instance1)
            allInstances.add(instance2)
            allInstances.add(instance3)
            allInstances.add(instance4)
            return allInstances
        }
}