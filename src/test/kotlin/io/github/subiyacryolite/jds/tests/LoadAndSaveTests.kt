package io.github.subiyacryolite.jds.tests

import io.github.subiyacryolite.jds.tests.common.BaseTestConfig
import io.github.subiyacryolite.jds.tests.common.TestData
import io.github.subiyacryolite.jds.tests.entities.Example
import io.github.subiyacryolite.jds.JdsDb
import io.github.subiyacryolite.jds.JdsLoad
import io.github.subiyacryolite.jds.JdsSave
import io.github.subiyacryolite.jds.enums.JdsFilterBy
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors


/**
 * Created by ifunga on 12/04/2017.
 */
class LoadAndSaveTests : BaseTestConfig("Load and Save tests") {

    @Throws(Exception::class)
    override fun testImpl(jdsDb: JdsDb) {
        save(jdsDb)
        load(jdsDb)
    }

    @Throws(Exception::class)
    private fun save(jdsDb: JdsDb) {
        val save = JdsSave(jdsDb, TestData.collection)
        val process = Executors.newSingleThreadExecutor().submit(save)
        while (!process.isDone)
            Thread.sleep(16)
        System.out.printf("Saved? %s\n", process.get())
    }

    @Throws(ExecutionException::class, InterruptedException::class)
    private fun load(jdsDb: JdsDb) {
        val loadAllInstances = JdsLoad(jdsDb, Example::class.java)
        val loadSpecificInstance = JdsLoad(jdsDb, Example::class.java, JdsFilterBy.Uuid, setOf("instance3"))
        val loadSortedInstances = JdsLoad(jdsDb, Example::class.java)

        val executorService = Executors.newFixedThreadPool(3)
        val loadingAllInstances = executorService.submit(loadAllInstances)
        val loadingSpecificInstance = executorService.submit(loadSpecificInstance)
        val loadingSortedInstances = executorService.submit(loadSortedInstances)

        while (!loadingAllInstances.isDone)
            Thread.sleep(16)
        while (!loadingSpecificInstance.isDone)
            Thread.sleep(16)
        while (!loadingSortedInstances.isDone)
            Thread.sleep(16)

        val allInstances = loadingAllInstances.get()
        val specificInstance = loadingSpecificInstance.get()
        val sortedInstances = loadingSortedInstances.get()

        println(allInstances)
        println(specificInstance)
        println(sortedInstances)

        println("DONE")
    }
}
