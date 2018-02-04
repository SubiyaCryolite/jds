package tests

import common.BaseTestConfig
import constants.Fields
import constants.Rights
import entities.*
import io.github.subiyacryolite.jds.JdsSave
import io.github.subiyacryolite.jds.JdsTable
import org.junit.jupiter.api.Test

class CustomReport : BaseTestConfig() {

    @Throws(Exception::class)
    private fun test() {
        val customTable = JdsTable()
        customTable.uniqueEntries = false
        customTable.name = "CrtAddressSpecific"
        customTable.registerEntity(Address::class.java)
        customTable.registerField(Fields.AREA_NAME)
        customTable.registerField(Fields.CITY_NAME)

        val crtAddress = JdsTable(Address::class.java, true)
        val crtAddressBook = JdsTable(AddressBook::class.java, true)
        val crtLogin = JdsTable(Login::class.java, true)
        val crtA = JdsTable(EntityA::class.java, true)
        val crtB = JdsTable(EntityB::class.java, true)
        val crtC = JdsTable(EntityC::class.java, true)
        val exampleType = JdsTable(Example::class.java, true)

        jdsDb.mapTable(customTable, crtAddress, crtAddressBook, crtLogin, crtA, crtB, crtC, exampleType)
        jdsDb.prepareTables()

        val login1 = Login()
        login1.overview.uuid = "login1"
        login1.rights.add(Rights.CAN_LOGIN)
        login1.rights.add(Rights.CAN_CREATE_USER)

        val login2 = Login()
        login2.overview.uuid = "login2"
        login2.rights.add(Rights.CAN_LOGIN)
        login2.rights.add(Rights.CAN_DELETE_RECORD)

        val jdsSave1 = JdsSave(jdsDb, collection)
        jdsSave1.call()

        val jdsSave2 = JdsSave(jdsDb, listOf(login1, login2))
        jdsSave2.call()

        val jdsSave3 = JdsSave(jdsDb, listOf(addressBook))
        jdsSave3.call()

        val jdsSave4 = JdsSave(jdsDb, inheritanceCollection)
        jdsSave4.call()
    }

    @Test
    @Throws(Exception::class)
    fun testPostgreSql() {
        initialisePostgeSqlBackend()
        test()
    }

    @Test
    @Throws(Exception::class)
    fun testTransactionalSql() {
        initialiseTSqlBackend()
        test()
    }

    @Test
    @Throws(Exception::class)
    fun testSqLite() {
        initialiseSqLiteBackend()
        test()
    }

    @Test
    @Throws(Exception::class)
    fun testMySql() {
        initialiseMysqlBackend()
        test()
    }

    @Test
    @Throws(Exception::class)
    fun testMariaDb() {
        initialiseMariaDbBackend()
        test()
    }

    @Test
    @Throws(Exception::class)
    fun testOracle() {
        initialiseOracleBackend()
        test()
    }

    @Test
    @Throws(Exception::class)
    fun allImplementations() {
        testTransactionalSql()
        testPostgreSql()
        testMySql()
        testOracle()
        testSqLite()
        testMariaDb()
    }
}