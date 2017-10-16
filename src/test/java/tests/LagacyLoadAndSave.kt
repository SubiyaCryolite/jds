package tests

import common.BaseTestConfig
import entities.Address
import io.github.subiyacryolite.jds.JdsLoad
import io.github.subiyacryolite.jds.JdsSave
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Created by ifunga on 18/02/2017.
 */
class LagacyLoadAndSave : BaseTestConfig() {

    @Test
    @Throws(Exception::class)
    override fun save() {
        val addressBook = sampleAddressBook
        JdsSave(jdsDb, 1, Arrays.asList(addressBook)).call()
        System.out.printf("Saved %s\n", addressBook)
    }

    @Test
    @Throws(Exception::class)
    override fun load() {
        val comparator = Comparator.comparing<Address, String> { entry -> entry.provinceOrState }
        val allAddressBooks = JdsLoad(jdsDb, Address::class.java, comparator).call() //load all entities of type AddressBook
        val specificAddressBook = JdsLoad(jdsDb, Address::class.java, comparator, "primaryAddress").call() //load all entities of type AddressBook with Entity Guids in range
        System.out.printf("All entities [%s]\n", allAddressBooks)
        System.out.printf("Specific entities [%s]\n", specificAddressBook)
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
