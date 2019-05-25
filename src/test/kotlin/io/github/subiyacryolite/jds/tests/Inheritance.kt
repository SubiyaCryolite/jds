package io.github.subiyacryolite.jds.tests

import io.github.subiyacryolite.jds.tests.common.BaseTestConfig
import io.github.subiyacryolite.jds.tests.common.TestData
import io.github.subiyacryolite.jds.tests.entities.EntityA
import io.github.subiyacryolite.jds.tests.entities.EntityB
import io.github.subiyacryolite.jds.tests.entities.EntityC
import io.github.subiyacryolite.jds.JdsDb
import io.github.subiyacryolite.jds.JdsLoad
import io.github.subiyacryolite.jds.JdsSave

/**
 * Created by ifunga on 01/07/2017.
 */
class Inheritance : BaseTestConfig("Inheritance") {

    @Throws(Exception::class)
    override fun testImpl(jdsDb: JdsDb) {
        save(jdsDb)
        load(jdsDb)
    }

    @Throws(Exception::class)
    private fun save(jdsDb: JdsDb) {
        val save = JdsSave(jdsDb, TestData.inheritanceCollection)
        save.call()
    }

    @Throws(Exception::class)
    private fun load(jdsDb: JdsDb) {
        val entityAs = JdsLoad(jdsDb, EntityA::class.java)
        val entityBs = JdsLoad(jdsDb, EntityB::class.java)
        val entityCs = JdsLoad(jdsDb, EntityC::class.java)
        System.out.printf("All A s [%s]\n", entityAs.call())
        System.out.printf("All B s [%s]\n", entityBs.call())
        System.out.printf("All C s [%s]\n", entityCs.call())
    }
}
