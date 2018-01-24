package common

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import connectivity.*
import constants.PrimaryAddress
import entities.*
import io.github.subiyacryolite.jds.JdsDb
import io.github.subiyacryolite.jds.JdsEntity
import java.io.*
import java.time.*
import java.util.*

/**
 * Created by ifunga on 08/04/2017.
 */
abstract class BaseTestConfig {

    protected val DOUBLE_DELTA = 1e-15
    protected val FLOAT_DELTA = 1e-2f
    protected lateinit var jdsDb: JdsDb

    protected val addressBook: AddressBook
        get() {
            val primaryAddress = Address()
            primaryAddress.overview.uuid = "primaryAddress"
            primaryAddress.overview.dateModified = LocalDateTime.of(2012, Month.APRIL, 12, 13, 49)
            primaryAddress.area = "Norte Broad"
            primaryAddress.city = "Livingstone"
            primaryAddress.country = "Zambia"
            primaryAddress.plotNumber = null
            primaryAddress.provinceOrState = "Southern"
            primaryAddress.streetName = "East Street"
            primaryAddress.timeOfEntry = ZonedDateTime.now()
            primaryAddress.time = primaryAddress.timeOfEntry.toLocalTime()
            primaryAddress.primaryAddress = PrimaryAddress.YES

            val secondAddress = Address()
            secondAddress.overview.uuid = "secondAddress"
            secondAddress.overview.dateModified = LocalDateTime.of(2009, Month.OCTOBER, 16, 3, 34)
            secondAddress.area = "Roma"
            secondAddress.city = "Lusaka"
            secondAddress.country = "Zambia"
            secondAddress.plotNumber = 2
            secondAddress.provinceOrState = "Lusaka"
            secondAddress.streetName = "West Street"
            secondAddress.timeOfEntry = ZonedDateTime.now().minusMonths(2)
            secondAddress.time = secondAddress.timeOfEntry.toLocalTime()
            secondAddress.primaryAddress = PrimaryAddress.NO

            val thirdAddress = Address()
            thirdAddress.overview.uuid = "thirdAddress"
            thirdAddress.overview.dateModified = LocalDateTime.of(2007, Month.JULY, 4, 5, 10)
            thirdAddress.area = "Riverdale"
            thirdAddress.city = "Ndola"
            thirdAddress.country = "Zambia"
            thirdAddress.plotNumber = 9
            thirdAddress.provinceOrState = "Copperbelt"
            thirdAddress.streetName = "West Street"
            thirdAddress.timeOfEntry = ZonedDateTime.now().minusDays(3)
            thirdAddress.time = thirdAddress.timeOfEntry.toLocalTime()
            thirdAddress.primaryAddress = PrimaryAddress.NO

            val addressBook = AddressBook()
            addressBook.overview.uuid = "testGuid0001"
            addressBook.addresses.add(primaryAddress)
            addressBook.addresses.add(secondAddress)
            addressBook.addresses.add(thirdAddress)
            return addressBook
        }

    protected val timeConstruct: TimeConstruct
        get() {
            val timeConstruct = TimeConstruct()
            timeConstruct.overview.uuid = "timeConstruct"
            timeConstruct.duration = Duration.ofDays(2).minusHours(4)
            timeConstruct.monthDay = MonthDay.of(Month.JULY, 1)
            timeConstruct.yearMonth = YearMonth.of(1991, Month.OCTOBER)
            timeConstruct.period = Period.ofYears(4).minusMonths(4).minusDays(12)
            return timeConstruct;
        }

    protected val inheritanceCollection: List<JdsEntity>
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

    protected val collection: List<Example>
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

    protected val objectMapper: ObjectMapper
        get() {
            val _objectMapper = ObjectMapper()
            _objectMapper.registerModule(JavaTimeModule())
            _objectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            _objectMapper.enable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
            _objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX)
            _objectMapper.enable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
            return _objectMapper;
        }

    private fun initialiseJdsClasses() {
        jdsDb.map(EntityA::class.java)
        jdsDb.map(EntityB::class.java)
        jdsDb.map(EntityC::class.java)
        jdsDb.map(Example::class.java)
        jdsDb.map(Address::class.java)
        jdsDb.map(AddressBook::class.java)
        jdsDb.map(TimeConstruct::class.java)
        jdsDb.map(Login::class.java)
    }

    private fun initJds() {
        jdsDb.init()
        jdsDb.isLoggingEdits = true
        jdsDb.isLoggingAppendOnly = false
        initialiseJdsClasses()
        System.out.printf("=========== %s ===========\n", jdsDb.implementation)
    }

    fun initialiseSqlLiteBackend() {
        jdsDb = JdsDbSqliteImplementation()
        initJds()
    }

    fun initialisePostgeSqlBackend() {
        jdsDb = JdsDbPostgreSqlmplementation()
        initJds()
    }

    fun initialiseTSqlBackend() {
        jdsDb = JdsDbTransactionalSqllmplementation()
        initJds()
    }

    fun initialiseMysqlBackend() {
        jdsDb = JdsDbMariaImplementation()
        initJds()
    }

    fun initialiseOracleBackend() {
        jdsDb = JdsDbOracleImplementation()
        initJds()
    }

    protected fun <T> serialize(objectToSerialize: T?, fileName: String?) {
        if (fileName == null) {
            throw IllegalArgumentException(
                    "Name of file to which to serialize object to cannot be null.")
        }
        if (objectToSerialize == null) {
            throw IllegalArgumentException("Object to be serialized cannot be null.")
        }
        try {
            FileOutputStream(fileName).use { fos ->
                ObjectOutputStream(fos).use { oos ->
                    oos.writeObject(objectToSerialize)
                    println("Serialization of completed: " + objectToSerialize)
                }
            }
        } catch (ioException: IOException) {
            ioException.printStackTrace(System.err)
        }
    }

    protected fun <T> deserialize(fileToDeserialize: String?, classBeingDeserialized: Class<T>?): T? {
        if (fileToDeserialize == null) {
            throw IllegalArgumentException("Cannot deserialize from a null filename.")
        }
        if (classBeingDeserialized == null) {
            throw IllegalArgumentException("Type of class to be deserialized cannot be null.")
        }
        var objectOut: T? = null
        try {
            FileInputStream(fileToDeserialize).use { fis ->
                ObjectInputStream(fis).use { ois ->
                    objectOut = ois.readObject() as T
                    println("Deserialization of completed: " + objectOut!!)
                }
            }
        } catch (exception: IOException) {
            exception.printStackTrace(System.err)
        } catch (exception: ClassNotFoundException) {
            exception.printStackTrace(System.err)
        }
        return objectOut
    }
}
