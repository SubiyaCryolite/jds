package io.github.subiyacryolite.jds.tests

import io.github.subiyacryolite.jds.tests.common.BaseTestConfig
import io.github.subiyacryolite.jds.tests.common.TestData
import io.github.subiyacryolite.jds.JdsDb
import io.github.subiyacryolite.jds.JdsDelete
import java.util.concurrent.Executors

class Deletes : BaseTestConfig("Deletes") {

    @Throws(Exception::class)
    override fun testImpl(jdsDb: JdsDb) {
        val delete = JdsDelete(jdsDb, TestData.addressBook)
        val process = Executors.newSingleThreadExecutor().submit(delete)
        while (!process.isDone)
            Thread.sleep(16)
        println("Deleted successfully?  ${process.get()}")
    }
}