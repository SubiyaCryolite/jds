package io.github.subiyacryolite.jds.tests

import io.github.subiyacryolite.jds.tests.common.BaseTestConfig
import io.github.subiyacryolite.jds.tests.entities.EntityA
import io.github.subiyacryolite.jds.JdsDb
import io.github.subiyacryolite.jds.JdsLoad
import io.github.subiyacryolite.jds.JdsSave
import java.util.*
import java.util.concurrent.Executors

class BulkSaveAndLoad : BaseTestConfig("Bulk insert and load (10,000 EAV records)") {

    @Throws(Exception::class)
    override fun testImpl(jdsDb: JdsDb){
        save(jdsDb)
        load(jdsDb)
    }

    @Throws(Exception::class)
    private fun save(jdsDb: JdsDb) {
        val memObjects = ArrayList<EntityA>()
        for (i in 0..9999) {
            val entry = EntityA()
            entry.overview.uuid = "guidBulk$i"
            memObjects.add(entry)
        }
        val process = Executors.newSingleThreadExecutor().submit(JdsSave(jdsDb, memObjects))
        while (!process.isDone)
            Thread.sleep(16)
        println("Successfully saved $memObjects")
    }

    @Throws(Exception::class)
    private fun load(jdsDb: JdsDb) {
        val entityAs = JdsLoad(jdsDb, EntityA::class.java)
        println("All A's [${entityAs.call()}]")
    }
}
