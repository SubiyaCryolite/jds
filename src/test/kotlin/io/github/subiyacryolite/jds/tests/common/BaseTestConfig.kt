package io.github.subiyacryolite.jds.tests.common

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.github.subiyacryolite.jds.JdsDb
import io.github.subiyacryolite.jds.tests.connectivity.*
import io.github.subiyacryolite.jds.tests.entities.*
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.io.File


/**
 * Created by ifunga on 08/04/2017.
 */
abstract class BaseTestConfig(private val testName: String) {

    companion object {
        const val DOUBLE_DELTA = 1e-15
        const val FLOAT_DELTA = 1e-2f

        fun initialiseSqLiteBackend(): JdsDb {
            val jdsDb = JdsDbSqliteImplementation()
            initJds(jdsDb)
            return jdsDb
        }

        fun initialisePostgeSqlBackend(): JdsDb {
            val jdsDb = JdsDbPostgreSqlmplementation()
            initJds(jdsDb)
            return jdsDb
        }

        fun initialiseTSqlBackend(): JdsDb {
            val jdsDb = JdsDbTransactionalSqllmplementation()
            initJds(jdsDb)
            return jdsDb
        }

        fun initialiseMysqlBackend(): JdsDb {
            val jdsDb = JdsDbMySqlImplementation()
            initJds(jdsDb)
            return jdsDb
        }

        fun initialiseMariaDbBackend(): JdsDb {
            val jdsDb = JdsDbMariaImplementation()
            initJds(jdsDb)
            return jdsDb
        }

        fun initialiseOracleBackend(): JdsDb {
            val jdsDb = JdsDbOracleImplementation()
            initJds(jdsDb)
            return jdsDb
        }

        private fun initialiseJdsClasses(jdsDb: JdsDb) {
            jdsDb.map(EntityA::class.java)
            jdsDb.map(EntityB::class.java)
            jdsDb.map(EntityC::class.java)
            jdsDb.map(Example::class.java)
            jdsDb.map(Address::class.java)
            jdsDb.map(AddressBook::class.java)
            jdsDb.map(TimeConstruct::class.java)
            jdsDb.map(Login::class.java)
        }

        private fun initJds(jdsDb: JdsDb) {
            jdsDb.init()
            jdsDb.options.logOutput = true
            initialiseJdsClasses(jdsDb)
        }

        private val logger = LoggerFactory.getLogger(BaseTestConfig::class.java)
    }

   init {

        val tsqlConfigFile = File("db.tsql.properties")
        if (!tsqlConfigFile.exists()) {
            Thread.currentThread().contextClassLoader.getResourceAsStream("tsql.src.properties").use { inputStream ->
                inputStream.reader().use { inputStreamReader -> tsqlConfigFile.writeText(inputStreamReader.readText()) }
            }
        }

        val pgConfigFile = File("db.pg.properties")
        if (!pgConfigFile.exists()) {
            Thread.currentThread().contextClassLoader.getResourceAsStream("pg.src.properties").use { inputStream ->
                inputStream.reader().use { inputStreamReader -> pgConfigFile.writeText(inputStreamReader.readText()) }
            }
        }

        val oraConfigFile = File("db.ora.properties")
        if (!oraConfigFile.exists()) {
            Thread.currentThread().contextClassLoader.getResourceAsStream("ora.src.properties").use { inputStream ->
                inputStream.reader().use { inputStreamReader -> oraConfigFile.writeText(inputStreamReader.readText()) }
            }
        }

        val mysqlConfigFile = File("db.mysql.properties")
        if (!mysqlConfigFile.exists()) {
            Thread.currentThread().contextClassLoader.getResourceAsStream("mysql.src.properties").use { inputStream ->
                inputStream.reader().use { inputStreamReader -> mysqlConfigFile.writeText(inputStreamReader.readText()) }
            }
        }

        val mariaConfigFile = File("db.mariadb.properties")
        if (!mariaConfigFile.exists()) {
            Thread.currentThread().contextClassLoader.getResourceAsStream("mysql.src.properties").use { inputStream ->
                inputStream.reader().use { inputStreamReader -> mariaConfigFile.writeText(inputStreamReader.readText()) }
            }
        }
    }

    fun test(jdsDb: JdsDb) {
        logger.info("JDS (${jdsDb.implementation}) :: $testName")
        testImpl(jdsDb)
    }

    protected abstract fun testImpl(jdsDb: JdsDb)

    @Test
    @Throws(Exception::class)
    fun testPostgreSql() = test(initialisePostgeSqlBackend())

    @Test
    @Throws(Exception::class)
    fun testOracle() = test(initialiseOracleBackend())

    @Test
    @Throws(Exception::class)
    fun testTransactionalSql() = test(initialiseTSqlBackend())

    @Test
    @Throws(Exception::class)
    fun testSqLite() = test(initialiseSqLiteBackend())

    @Test
    @Throws(Exception::class)
    fun testMySql() = test(initialiseMysqlBackend())

    @Test
    @Throws(Exception::class)
    fun testMariaDb() = test(initialiseMariaDbBackend())

    @Test
    @Throws(Exception::class)
    fun testAll() {
        testSqLite()
        testTransactionalSql()
        testPostgreSql()
        testMySql()
        testOracle()
        testMariaDb()
    }

    protected val objectMapper: ObjectMapper
        get() {
            val objectMapper = ObjectMapper()
            objectMapper.registerModule(JavaTimeModule())
            objectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            objectMapper.enable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
            objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX)
            objectMapper.enable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
            return objectMapper;
        }
}
