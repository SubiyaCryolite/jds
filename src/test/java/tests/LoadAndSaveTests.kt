package tests

import common.BaseTestConfig
import entities.Example
import io.github.subiyacryolite.jds.JdsLoad
import io.github.subiyacryolite.jds.JdsSave
import org.junit.jupiter.api.Test
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask

/**
 * Created by ifunga on 12/04/2017.
 */
class LoadAndSaveTests : BaseTestConfig() {

    @Test
    @Throws(Exception::class)
    override fun save() {
        val collection = collection
        val save = JdsSave(jdsDb, collection)
        val saving = FutureTask(save)
        val thread = Thread(saving)
        thread.start()
        while (!saving.isDone)
            println("Waiting for operation 1 to complete")
        System.out.printf("Saved? %s\n", saving.get())
    }

    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    override fun load() {
        val loadAllInstances = JdsLoad(jdsDb, Example::class.java)
        val loadSpecificInstance = JdsLoad(jdsDb, Example::class.java, "instance3")
        val loadSortedInstances = JdsLoad(jdsDb, Example::class.java)

        val loadingAllInstances = FutureTask(loadAllInstances)
        val loadingSpecificInstance = FutureTask(loadSpecificInstance)
        val loadingSortedInstances = FutureTask(loadSortedInstances)

        Thread(loadingAllInstances).start()
        Thread(loadingSpecificInstance).start()
        Thread(loadingSortedInstances).start()

        while (!loadingAllInstances.isDone)
            println("Waiting for operation 1 to complete")
        while (!loadingSpecificInstance.isDone)
            println("Waiting for operation 2 to complete")
        while (!loadingSortedInstances.isDone)
            println("Waiting for operation 3 to complete")

        val allInstances = loadingAllInstances.get()
        val specificInstance = loadingSpecificInstance.get()
        val sortedInstances = loadingSortedInstances.get()

        println(allInstances)
        println(specificInstance)
        println(sortedInstances)

        println("DONE")
    }

    @Test
    @Throws(Exception::class)
    fun sqlLiteImplementation() {
        initialiseSqlLiteBackend()
        saveAndLoad()
    }


    @Test
    @Throws(Exception::class)
    fun mysqlImplementation() {
        initialiseMysqlBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun postgeSqlImplementation() {
        initialisePostgeSqlBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun tSqlImplementation() {
        initialiseTSqlBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun oracleImplementation() {
        initialiseOracleBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun allImplementations() {
        sqlLiteImplementation()
        mysqlImplementation()
        postgeSqlImplementation()
        tSqlImplementation()
        oracleImplementation()
    }
}
