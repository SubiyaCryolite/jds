package io.github.subiyacryolite.jds.tests

import io.github.subiyacryolite.jds.tests.common.BaseTestConfig
import io.github.subiyacryolite.jds.JdsDb
import org.junit.jupiter.api.Test

class AllTests {

    private val bulkSaveAndLoad = BulkSaveAndLoad()
    private val customReport = CustomReport()
    private val customReportJson = CustomReportJson()
    private val inheritance = Inheritance()
    private val legacyValidation = LegacyValidation()
    private val legacyLoadAndSave = LegacyLoadAndSave()
    private val nonExisting = NonExisting()
    private val timeConstructs = TimeConstructs()
    private val loadAndSaveTests = LoadAndSaveTests()

    private fun runAllTests(jdsDb: JdsDb)
    {
        bulkSaveAndLoad.test(jdsDb)
        //customReport.test(jdsDb)
        //customReportJson.test(jdsDb)
        inheritance.test(jdsDb)
        legacyValidation.test(jdsDb)
        legacyLoadAndSave.test(jdsDb)
        nonExisting.test(jdsDb)
        timeConstructs.test(jdsDb)
        loadAndSaveTests.test(jdsDb)
    }

    @Test
    fun testAll() {
        testTransactionalSql()
        testMySql()
        testOracle()
        testPostgreSql()
        testSqLite()
        testMariaDb()
    }

    @Test
    fun testTransactionalSql() {
        runAllTests(BaseTestConfig.initialiseTSqlBackend())
    }

    @Test
    fun testMySql() {
        runAllTests(BaseTestConfig.initialiseMysqlBackend())
    }

    @Test
    fun testMariaDb() {
        runAllTests(BaseTestConfig.initialiseMariaDbBackend())
    }

    @Test
    fun testOracle() {
        runAllTests(BaseTestConfig.initialiseOracleBackend())
    }

    @Test
    fun testPostgreSql() {
        runAllTests(BaseTestConfig.initialisePostgeSqlBackend())
    }

    @Test
    fun testSqLite() {
        runAllTests(BaseTestConfig.initialiseSqLiteBackend())
    }
}