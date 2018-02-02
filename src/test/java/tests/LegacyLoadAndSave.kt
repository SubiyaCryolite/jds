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
        jdsDb.getConnection().use {
            val jds = JdsSave(jdsDb, setOf(addressBook),it)
            jds.closeConnection = false
            jds.call()
        }
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
    fun testPostgreSql() {
        initialisePostgeSqlBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun testTransactionalSql() {
        initialiseTSqlBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun testSqLite() {
        initialiseSqLiteBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun testMySql() {
        initialiseMysqlBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun testOracle() {
        initialiseOracleBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun allImplementations() {
        testTransactionalSql()
        testPostgreSql()
        testMySql()
        testSqLite()
        testOracle()
    }
}
