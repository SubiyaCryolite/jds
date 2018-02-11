package tests

import common.BaseTestConfig
import entities.Example
import io.github.subiyacryolite.jds.JdsLoad
import io.github.subiyacryolite.jds.JdsSave
import io.github.subiyacryolite.jds.enums.JdsFilterBy
import org.junit.jupiter.api.Test
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors


/**
 * Created by ifunga on 12/04/2017.
 */
class LoadAndSaveTests : BaseTestConfig("Load and Save tests") {

    @Throws(Exception::class)
    private fun save() {
        val save = JdsSave(jdsDb, collection)
        val process = Executors.newSingleThreadExecutor().submit(save)
        while (!process.isDone)
            Thread.sleep(16)
        System.out.printf("Saved? %s\n", process.get())
    }

    @Throws(ExecutionException::class, InterruptedException::class)
    private fun load() {
        val loadAllInstances = JdsLoad(jdsDb, Example::class.java)
        val loadSpecificInstance = JdsLoad(jdsDb, Example::class.java, JdsFilterBy.UUID, setOf("instance3"))
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

    @Throws(ExecutionException::class, InterruptedException::class)
    private fun saveAndLoad() {
        save()
        load()
    }

    @Test
    @Throws(Exception::class)
    fun testSqLite() {
        initialiseSqLiteBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun testMySql() {
        initialiseMysqlBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun testMariaDb() {
        initialiseMariaDbBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun testPostgreSql() {
        initialisePostgeSqlBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun testTransactionalSql() {
        initialiseTSqlBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun testOracle() {
        initialiseOracleBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun allImplementations() {
        testSqLite()
        testMySql()
        testMariaDb()
        testPostgreSql()
        testTransactionalSql()
        testOracle()
    }
}
