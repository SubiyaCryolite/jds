package io.github.subiyacryolite.jds.tests

import io.github.subiyacryolite.jds.tests.common.BaseTestConfig
import io.github.subiyacryolite.jds.tests.common.TestData
import io.github.subiyacryolite.jds.tests.constants.Fields
import io.github.subiyacryolite.jds.tests.constants.Rights
import io.github.subiyacryolite.jds.tests.entities.*
import io.github.subiyacryolite.jds.JdsDb
import io.github.subiyacryolite.jds.JdsSave
import io.github.subiyacryolite.jds.JdsTable

class CustomReport : BaseTestConfig("Custom reports and shared connections") {

    @Throws(Exception::class)
    override fun testImpl(jdsDb: JdsDb) {
        val customTable = JdsTable()
        customTable.name = "address_specific"
        customTable.registerEntity(Address::class.java)
        customTable.registerFields(Fields.AREA_NAME, Fields.CITY_NAME)

        val crtAddress = JdsTable(Address::class.java)

        val crtAddressBook = JdsTable(AddressBook::class.java)
        val crtLogin = JdsTable(Login::class.java)
        val crtA = JdsTable(EntityA::class.java)
        val crtB = JdsTable(EntityB::class.java)
        val crtC = JdsTable(EntityC::class.java)
        val exampleType = JdsTable(Example::class.java)

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

        val saveCollection = JdsSave(jdsDb, TestData.collection)
        saveCollection.call()

        val saveLogins = JdsSave(jdsDb, listOf(login1, login2))
        saveLogins.call()

        val saveAddressBook = JdsSave(jdsDb, listOf(TestData.addressBook))
        saveAddressBook.call()

        val saveInheritedObjects = JdsSave(jdsDb, TestData.inheritanceCollection)
        saveInheritedObjects.call()
    }
}