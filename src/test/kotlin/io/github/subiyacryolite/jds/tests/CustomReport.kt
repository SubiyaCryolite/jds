package io.github.subiyacryolite.jds.tests

import io.github.subiyacryolite.jds.tests.common.BaseTestConfig
import io.github.subiyacryolite.jds.tests.common.TestData
import io.github.subiyacryolite.jds.tests.constants.Fields
import io.github.subiyacryolite.jds.tests.enums.Right
import io.github.subiyacryolite.jds.tests.entities.*
import io.github.subiyacryolite.jds.context.DbContext
import io.github.subiyacryolite.jds.Save
import io.github.subiyacryolite.jds.Table
import org.junit.jupiter.api.Test

class CustomReport : BaseTestConfig("Custom reports and shared connections") {

    @Throws(Exception::class)
    override fun testImpl(dbContext: DbContext) {
        val customTable = Table()
        customTable.name = "address_specific"
        customTable.registerEntity(Address::class.java,false)
        customTable.registerFields(Fields.ResidentialArea, Fields.City)

        val crtAddress = Table(Address::class.java)

        val crtAddressBook = Table(AddressBook::class.java)
        val crtLogin = Table(Login::class.java)
        val crtA = Table(EntityA::class.java)
        val crtB = Table(EntityB::class.java)
        val crtC = Table(EntityC::class.java)
        val exampleType = Table(Example::class.java)

        dbContext.mapTable(customTable, crtAddress, crtAddressBook, crtLogin, crtA, crtB, crtC, exampleType)
        dbContext.prepareTables()

        val login1 = Login()
        login1.overview.id = "login1"
        login1.rights.add(Right.Login)
        login1.rights.add(Right.CreateUser)

        val login2 = Login()
        login2.overview.id = "login2"
        login2.rights.add(Right.Login)
        login2.rights.add(Right.DeleteRecord)

        val saveCollection = Save(dbContext, TestData.collection)
        saveCollection.call()

        val saveLogins = Save(dbContext, listOf(login1, login2))
        saveLogins.call()

        val saveAddressBook = Save(dbContext, listOf(TestData.addressBook))
        saveAddressBook.call()

        val saveInheritedObjects = Save(dbContext, TestData.inheritanceCollection)
        saveInheritedObjects.call()
    }

    @Test
    fun postGreSql() {
        testPostgreSql()
    }

    @Test
    fun sqlLite() {
        testSqLite()
    }

    @Test
    fun mariaDb() {
        testMariaDb()
    }

    @Test
    fun mySql() {
        testMySql()
    }

    @Test
    fun oracle() {
        testOracle()
    }

    @Test
    fun transactionalSql() {
        testTransactionalSql()
    }
}