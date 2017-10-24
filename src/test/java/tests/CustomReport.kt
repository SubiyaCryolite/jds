package tests

import common.BaseTestConfig
import constants.Fields
import entities.Address
import entities.AddressBook
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

        val crtAddress = JdsTable(Address::class.java,true)
        val crtAddressBook = JdsTable(AddressBook::class.java,true)

        jdsDb.mapTable(customTable)
        jdsDb.mapTable(crtAddress)
        jdsDb.mapTable(crtAddressBook)
        jdsDb.prepareTables()

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
        jdsDb.isLoggingEdits = false
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