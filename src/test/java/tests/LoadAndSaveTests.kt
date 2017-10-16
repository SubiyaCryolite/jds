package tests

import common.BaseTestConfig
import entities.Example
import io.github.subiyacryolite.jds.JdsDelete
import io.github.subiyacryolite.jds.JdsLoad
import io.github.subiyacryolite.jds.JdsSave
import org.junit.jupiter.api.Test
import java.util.*
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
    @Throws(Exception::class)
    fun bulkSave() {
        System.out.printf("=========== %s ===========\n", jdsDb.implementation)
        val collection = ArrayList<Example>()
        for (i in 0..4999) {
            val example = Example()
            example.overview.entityGuid = "guid_" + i
            example.intField = i
            example.floatField = (i + 1).toFloat()
            example.doubleField = (i + 2).toDouble()
            example.longField = (i + 3).toLong()
            collection.add(example)
        }
        val save = JdsSave(jdsDb, collection)
        val saving = FutureTask(save)
        Thread(saving).start()

        while (!saving.isDone)
            println("Waiting for operation 1 to complete")
        System.out.printf("Saved? %s\n", saving.get())
    }

    @Test
    @Throws(Exception::class)
    fun loadNonExisting() {
        val loadNonExistingCallable = JdsLoad(jdsDb, Example::class.java, "DOES_NOT_EXIST")
        val loadNonExistingTask = FutureTask(loadNonExistingCallable)

        Thread(loadNonExistingTask).start()

        while (!loadNonExistingTask.isDone)
            println("Waiting for operation 1 to complete")
        val loadNonExistingResult = loadNonExistingTask.get()
        println(loadNonExistingResult)
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
    @Throws(ExecutionException::class, InterruptedException::class)
    fun isolatedDelete() {
        val delete = JdsDelete(jdsDb, "instance2")

        val deleting = FutureTask(delete)
        Thread(deleting).start()

        while (!deleting.isDone)
            println("Waiting for operation to complete")
        println("Deleted? " + deleting.get())
    }

    @Test
    @Throws(Exception::class)
    fun callableOracleBulkSave() {
        initialiseOracleBackend()
        bulkSave()
    }

    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun callableOracleBulkLoad() {
        initialiseOracleBackend()
        load()
    }

    @Test
    @Throws(Exception::class)
    fun callableSqlLiteBulkSave() {
        initialiseTSqlBackend()
        bulkSave()
    }

    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun callableSqlLiteBulkLoad() {
        initialiseMysqlBackend()
        load()
    }

    @Test
    @Throws(Exception::class)
    fun callableSqlLiteBulkLoadSave() {
        initialiseSqlLiteBackend()
        bulkSave()
        load()
    }

    @Test
    @Throws(Exception::class)
    fun callableSqlLiteLoadSave() {
        initialiseSqlLiteBackend()
        save()
        load()
    }

    @Test
    @Throws(Exception::class)
    fun callableSqlLiteLoadNoExisting() {
        initialiseSqlLiteBackend()
        save()
        load()
    }

    @Test
    @Throws(Exception::class)
    fun callableMysqlLoadSave() {
        initialiseMysqlBackend()
        save()
        load()
    }

    @Test
    @Throws(Exception::class)
    fun callablePostgeSqlLoadSave() {
        initialisePostgeSqlBackend()
        save()
        load()
    }

    @Test
    @Throws(Exception::class)
    fun callableTSqlLoadSave() {
        initialiseTSqlBackend()
        save()
        load()
    }

    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun callableTSqlBulkLoad() {
        initialiseTSqlBackend()
        load()
    }
}
