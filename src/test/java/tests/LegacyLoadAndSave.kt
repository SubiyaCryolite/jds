package tests

import common.BaseTestConfig
import entities.Address
import io.github.subiyacryolite.jds.JdsLoad
import io.github.subiyacryolite.jds.JdsSave
import org.junit.jupiter.api.Test

/**
 * Created by ifunga on 18/02/2017.
 */
class LegacyLoadAndSave : BaseTestConfig() {

    @Throws(Exception::class)
    private fun save() {
        val addressBook = addressBook
        JdsSave(jdsDb, 1, addressBook).call()
        System.out.printf("Saved %s\n", addressBook)
    }

    @Throws(Exception::class)
    private fun load() {
        val comparator = kotlin.Comparator { o1: Address, o2: Address -> o1.provinceOrState.compareTo(o2.provinceOrState) }
        val allAddressBooks = JdsLoad(jdsDb, Address::class.java, comparator).call() //load all entityVersions of type AddressBook
        val specificAddressBook = JdsLoad(jdsDb, Address::class.java, comparator, "primaryAddress").call() //load all entityVersions of type AddressBook with Entity Guids in range
        System.out.printf("All entityVersions [%s]\n", allAddressBooks)
        System.out.printf("Specific entityVersions [%s]\n", specificAddressBook)
    }

    @Throws(Exception::class)
    private fun saveAndLoad() {
        save()
        load()
    }

    @Test
    @Throws(Exception::class)
    fun postreSqlImplementation() {
        initialisePostgeSqlBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun tSqlImplementation() {
        initialiseTSqlBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun sqliteImplementation() {
        initialiseSqlLiteBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun mySqlImplementation() {
        initialiseMysqlBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun oracleImplementation() {
        initialiseOracleBackend()
        saveAndLoad()
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