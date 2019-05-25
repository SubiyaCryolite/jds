package io.github.subiyacryolite.jds.tests

import io.github.subiyacryolite.jds.tests.common.BaseTestConfig
import io.github.subiyacryolite.jds.tests.common.TestData
import io.github.subiyacryolite.jds.tests.entities.TimeConstruct
import io.github.subiyacryolite.jds.JdsDb
import io.github.subiyacryolite.jds.JdsLoad
import io.github.subiyacryolite.jds.JdsSave
import io.github.subiyacryolite.jds.enums.JdsFilterBy
import java.util.*

class TimeConstructs : BaseTestConfig("Time constructs") {

    @Throws(Exception::class)
    override fun testImpl(jdsDb: JdsDb) {
        save(jdsDb)
        load(jdsDb)
    }

    @Throws(Exception::class)
    private fun save(jdsDb: JdsDb) {
        val timeConstruct = TestData.timeConstruct
        JdsSave(jdsDb, Arrays.asList(timeConstruct)).call()
        println("saved entityVersions [$timeConstruct]")
    }

    @Throws(Exception::class)
    private fun load(jdsDb: JdsDb) {
        val list = JdsLoad(jdsDb, TimeConstruct::class.java, JdsFilterBy.UUID, setOf("timeConstruct")).call() //load all entityVersions of type AddressBook with Entity Guids in range
        println("loaded entityVersions [$list]")
    }
}