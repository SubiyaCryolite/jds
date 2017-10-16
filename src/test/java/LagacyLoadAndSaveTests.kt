import common.BaseTestConfig
import entities.Address
import entities.AddressBook
import io.github.subiyacryolite.jds.JdsDelete
import io.github.subiyacryolite.jds.JdsLoad
import io.github.subiyacryolite.jds.JdsSave
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

import java.util.Arrays
import java.util.Comparator

/**
 * Created by ifunga on 18/02/2017.
 */
class LagacyLoadAndSaveTests : BaseTestConfig() {

    @Test
    @Throws(Exception::class)
    override fun save() {
        System.out.printf("=========== %s ===========\n", jdsDb.implementation)
        val addressBook = sampleAddressBook
        JdsSave(jdsDb, 1, Arrays.asList(addressBook)).call()
        System.out.printf("Saved %s\n", addressBook)
    }

    @Test
    @Throws(Exception::class)
    override fun load() {
        val allAddressBooks = JdsLoad(jdsDb, Address::class.java).call() //load all entities of type AddressBook
        val specificAddressBook = JdsLoad(jdsDb, Address::class.java, "primaryAddress").call() //load all entities of type AddressBook with Entity Guids in range
        System.out.printf("All entities [%s]\n", allAddressBooks)
        System.out.printf("Specific entities [%s]\n", specificAddressBook)
    }

    @Test
    @Throws(Exception::class)
    fun saveAndLoadPostreSqlImplementation() {
        initialisePostgeSqlBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun saveAndLoadTsqlImplementation() {
        initialiseTSqlBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun saveAndLoadSqliteImplementation() {
        initialiseSqlLiteBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun saveAndLoadMySqlImplementation() {
        initialiseMysqlBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun saveAndLoadOracleImplementation() {
        initialiseOracleBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun saveAndLoadAllImplementations() {
        saveAndLoadTsqlImplementation()
        saveAndLoadPostreSqlImplementation()
        saveAndLoadMySqlImplementation()
        saveAndLoadSqliteImplementation()
        saveAndLoadOracleImplementation()
    }

    @Test
    @Throws(Exception::class)
    fun testSortedLoads() {
        val comparator = Comparator.comparing<AddressBook, LocalDateTime> { entry -> entry.overview.dateCreated }
        val allAddressBooks = JdsLoad(jdsDb, AddressBook::class.java, comparator).call() //load all entities of type AddressBook
        val specificAddressBook = JdsLoad(jdsDb, AddressBook::class.java, comparator, "testGuid0001").call() //load all entities of type AddressBook with Entity Guids in range
        System.out.printf("All entities [%s]\n", allAddressBooks)
        System.out.printf("Specific entities [%s]\n", specificAddressBook)
    }

    @Test
    @Throws(Exception::class)
    fun deleteUsingStrings() {
        initialiseSqlLiteBackend()
        val result = JdsDelete(jdsDb, "primaryAddress1").call()
        print("Completed " + result!!)
    }

    @Test
    @Throws(Exception::class)
    fun deleteUsingObjectOrCollection() {
        val addressBook = sampleAddressBook
        JdsDelete.delete(jdsDb, addressBook)
    }
}
