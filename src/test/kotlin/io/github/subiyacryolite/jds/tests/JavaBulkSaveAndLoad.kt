package io.github.subiyacryolite.jds.tests

import io.github.subiyacryolite.jds.tests.common.BaseTestConfig
import io.github.subiyacryolite.jds.tests.entities.EntityA
import io.github.subiyacryolite.jds.JdsDb
import io.github.subiyacryolite.jds.JdsLoad
import io.github.subiyacryolite.jds.JdsSave

import java.sql.SQLException
import java.util.ArrayList
import java.util.concurrent.Executors

class JavaBulkSaveAndLoad : BaseTestConfig("Java - Bulk Load Save") {

    @Throws(Exception::class)
    override fun testImpl(jdsDb: JdsDb) {
        save(jdsDb)
        load(jdsDb)
    }

    @Throws(SQLException::class, ClassNotFoundException::class, InterruptedException::class)
    private fun save(jdsDb: JdsDb) {
        val memObjects = ArrayList<EntityA>()
        for (i in 0..9999) {
            val entry = EntityA()
            entry.overview.uuid = String.format("guidBulk%s", i)
            memObjects.add(entry)
        }
        val save = JdsSave(jdsDb, memObjects, null)
        val process = Executors.newSingleThreadExecutor().submit(save)
        if (!process.isDone)
            Thread.sleep(16)
        println("Successfully saved $memObjects")
    }

    @Throws(Exception::class)
    private fun load(jdsDb: JdsDb) {
        val entityAs = JdsLoad(jdsDb, EntityA::class.java)
        println("All A's [" + entityAs.call() + "]")
    }
}
