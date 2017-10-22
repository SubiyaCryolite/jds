package tests

import common.BaseTestConfig
import constants.Fields
import entities.Address
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

        val crtAddress = JdsTable()
        crtAddress.uniqueEntries = false
        crtAddress.name = "CrtAddress"
        crtAddress.registerEntities(Address::class.java, true)

        jdsDb.mapTable(customTable)
        jdsDb.mapTable(crtAddress)
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