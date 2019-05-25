package io.github.subiyacryolite.jds.tests

import io.github.subiyacryolite.jds.tests.common.BaseTestConfig
import io.github.subiyacryolite.jds.tests.entities.Example
import io.github.subiyacryolite.jds.JdsDb
import io.github.subiyacryolite.jds.JdsLoad
import io.github.subiyacryolite.jds.enums.JdsFilterBy
import java.util.concurrent.Executors

class NonExisting : BaseTestConfig("Load non-existing items") {

    @Throws(Exception::class)
    override fun testImpl(jdsDb: JdsDb) {
        val load = JdsLoad(jdsDb, Example::class.java, JdsFilterBy.UUID, setOf("DOES_NOT_EXIST"))
        val process = Executors.newSingleThreadExecutor().submit(load)
        while (!process.isDone)
            Thread.sleep(16)
        println(process.get())
    }
}