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

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.github.subiyacryolite.jds.context.DbContext
import io.github.subiyacryolite.jds.tests.connectivity.*
import io.github.subiyacryolite.jds.tests.entities.*
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.io.File

abstract class BaseTestConfig(private val testName: String) {

    companion object {
        const val DOUBLE_DELTA = 1e-15
        const val FLOAT_DELTA = 1e-2f

        fun initialiseSqLiteBackend(): DbContext {
            val jdsDb = SqLiteDbContextImplementation()
            initJds(jdsDb)
            return jdsDb
        }

        fun initialisePostgeSqlBackend(): DbContext {
            val jdsDb = PostGreSqlContextImplementation()
            initJds(jdsDb)
            return jdsDb
        }

        fun initialiseTSqlBackend(): DbContext {
            val jdsDb = TransactionalSqlContextImplementation()
            initJds(jdsDb)
            return jdsDb
        }

        fun initialiseMysqlBackend(): DbContext {
            val jdsDb = MySqlContextImplementation()
            initJds(jdsDb)
            return jdsDb
        }

        fun initialiseMariaDbBackend(): DbContext {
            val jdsDb = MariaDbContextImplementation()
            initJds(jdsDb)
            return jdsDb
        }

        fun initialiseOracleBackend(): DbContext {
            val jdsDb = OracleContextImplementation()
            initJds(jdsDb)
            return jdsDb
        }

        private fun initialiseJdsClasses(dbContext: DbContext) {
            dbContext.map(EntityA::class.java)
            dbContext.map(EntityB::class.java)
            dbContext.map(EntityC::class.java)
            dbContext.map(Example::class.java)
            dbContext.map(Address::class.java)
            dbContext.map(AddressBook::class.java)
            dbContext.map(TimeConstruct::class.java)
            dbContext.map(Login::class.java)
        }

        private fun initJds(dbContext: DbContext) {
            dbContext.init()
            dbContext.options.logOutput = true
            initialiseJdsClasses(dbContext)
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

    fun test(dbContext: DbContext) {
        logger.info("JDS (${dbContext.implementation}) :: $testName")
        testImpl(dbContext)
    }

    open fun testImpl(dbContext: DbContext){}

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
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)//Optimize
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)//Optimize
            objectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            objectMapper.enable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
            objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX)
            objectMapper.enable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
            return objectMapper;
        }
}
