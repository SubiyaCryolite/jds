package io.github.subiyacryolite.jds.tests

import io.github.subiyacryolite.jds.tests.common.BaseTestConfig
import io.github.subiyacryolite.jds.tests.common.TestData
import io.github.subiyacryolite.jds.tests.entities.Address
import io.github.subiyacryolite.jds.JdsDb
import io.github.subiyacryolite.jds.JdsLoad
import io.github.subiyacryolite.jds.JdsSave
import io.github.subiyacryolite.jds.enums.JdsFilterBy

/**
 * Created by ifunga on 18/02/2017.
 */
class LegacyLoadAndSave : BaseTestConfig("Legacy load and save") {

    @Throws(Exception::class)
    override fun testImpl(jdsDb: JdsDb) {
        save(jdsDb)
        load(jdsDb)
    }

    @Throws(Exception::class)
    private fun save(jdsDb: JdsDb) {
        val addressBook = TestData.addressBook
        JdsSave(jdsDb, addressBook).call()
        System.out.printf("Saved %s\n", addressBook)
    }

    @Throws(Exception::class)
    private fun load(jdsDb: JdsDb) {
        val allAddressBooks = JdsLoad(jdsDb, Address::class.java).call() //load all entityVersions of type AddressBook
        val specificAddressBook = JdsLoad(jdsDb, Address::class.java, JdsFilterBy.Uuid, setOf("primaryAddress")).call() //load all entityVersions of type AddressBook with Entity Guids in range
        System.out.printf("All entityVersions [%s]\n", allAddressBooks)
        System.out.printf("Specific entityVersions [%s]\n", specificAddressBook)
    }
}
