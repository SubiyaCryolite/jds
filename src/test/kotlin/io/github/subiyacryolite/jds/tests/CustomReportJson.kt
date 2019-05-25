package io.github.subiyacryolite.jds.tests

import io.github.subiyacryolite.jds.tests.common.BaseTestConfig
import io.github.subiyacryolite.jds.tests.common.TestData
import io.github.subiyacryolite.jds.JdsDb
import io.github.subiyacryolite.jds.JdsSave
import io.github.subiyacryolite.jds.JdsTable


class CustomReportJson : BaseTestConfig("Custom reports from json") {

    @Throws(Exception::class)
    override fun testImpl(jdsDb: JdsDb) {
        val xmlString = Thread.currentThread().contextClassLoader.getResourceAsStream("CustomReport.json").use { it.bufferedReader().readText() }
        val customTable = objectMapper.readValue(xmlString, JdsTable::class.java)
        jdsDb.mapTable(customTable)
        jdsDb.prepareTables()

        val jdsSave = JdsSave(jdsDb, TestData.addressBook)
        jdsSave.call()
    }
}