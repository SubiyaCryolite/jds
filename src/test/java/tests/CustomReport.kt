package tests

import common.BaseTestConfig
import constants.Fields
import constants.Rights
import entities.*
import io.github.subiyacryolite.jds.JdsSave
import io.github.subiyacryolite.jds.JdsTable
import org.junit.jupiter.api.Test
import java.util.*

class CustomReport : BaseTestConfig() {

    @Throws(Exception::class)
    private fun test() {
        val customTable = JdsTable()
        customTable.uniqueEntries = false
        customTable.name = "CrtAddressSpecific"
        customTable.registerEntities(Address::class.java)
        customTable.registerField(Fields.AREA_NAME)
        customTable.registerField(Fields.CITY_NAME)

        val crtAddress = JdsTable(Address::class.java, true)
        val crtAddressBook = JdsTable(AddressBook::class.java, true)
        val crtLogin = JdsTable(Login::class.java, true)

        val crtA = JdsTable(EntityA::class.java, true)
        val crtB = JdsTable(EntityB::class.java, true)
        val crtC = JdsTable(EntityC::class.java, true)

        jdsDb.mapTable(customTable)
        jdsDb.mapTable(crtAddress)
        jdsDb.mapTable(crtAddressBook)
        jdsDb.mapTable(crtLogin)
        jdsDb.mapTable(crtA)
        jdsDb.mapTable(crtB)
        jdsDb.mapTable(crtC)
        jdsDb.prepareTables()

        val login = Login()
        login.rights.add(Rights.CAN_LOGIN)
        login.rights.add(Rights.CAN_CREATE_USER)

        val sav = JdsSave(jdsDb, Arrays.asList(sampleAddressBook))
        sav.call()
    }

    @Test
    @Throws(Exception::class)
    fun postreSqlImplementation() {
        initialisePostgeSqlBackend()
        test()
    }

    @Test
    @Throws(Exception::class)
    fun tSqlImplementation() {
        initialiseTSqlBackend()
        test()
    }

    @Test
    @Throws(Exception::class)
    fun sqliteImplementation() {
        initialiseSqlLiteBackend()
        test()
    }

    @Test
    @Throws(Exception::class)
    fun mySqlImplementation() {
        initialiseMysqlBackend()
        test()
    }

    @Test
    @Throws(Exception::class)
    fun oracleImplementation() {
        initialiseOracleBackend()
        test()
    }

    @Test
    @Throws(Exception::class)
    fun allImplementations() {
        tSqlImplementation()
        postreSqlImplementation()
        mySqlImplementation()
        sqliteImplementation()
        oracleImplementation()
    }

}