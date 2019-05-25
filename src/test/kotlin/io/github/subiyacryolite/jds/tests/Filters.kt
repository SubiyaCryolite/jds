package io.github.subiyacryolite.jds.tests

import io.github.subiyacryolite.jds.tests.common.BaseTestConfig
import io.github.subiyacryolite.jds.tests.constants.Fields
import io.github.subiyacryolite.jds.tests.entities.Address
import io.github.subiyacryolite.jds.JdsDb
import io.github.subiyacryolite.jds.JdsFilter
import java.util.concurrent.Executors

/**
 * Created by ifunga on 05/03/2017.
 */
class Filters : BaseTestConfig("Load filters") {

    @Throws(Exception::class)
    override fun testImpl(jdsDb: JdsDb) {
        val filter = JdsFilter(jdsDb, Address::class.java).between(Fields.PLOT_NUMBER, 1, 2).like(Fields.COUNTRY_NAME, "Zam").or().equals(Fields.PROVINCE_NAME, "Copperbelt")
        val process = Executors.newSingleThreadExecutor().submit(filter)
        while (!process.isDone)
            Thread.sleep(16)
        println(process.get())
    }
}
