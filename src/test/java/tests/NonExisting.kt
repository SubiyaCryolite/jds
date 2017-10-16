package tests

import common.BaseTestConfig
import entities.Example
import io.github.subiyacryolite.jds.JdsLoad
import org.junit.jupiter.api.Test
import java.util.concurrent.FutureTask

class NonExisting : BaseTestConfig() {

    @Throws(Exception::class)
    fun loadNonExisting() {
        val loadNonExistingCallable = JdsLoad(jdsDb, Example::class.java, "DOES_NOT_EXIST")
        val loadNonExistingTask = FutureTask(loadNonExistingCallable)

        Thread(loadNonExistingTask).start()
        while (!loadNonExistingTask.isDone)
            println("Loading...")
        val loadNonExistingResult = loadNonExistingTask.get()
        println(loadNonExistingResult)
    }

    @Test
    @Throws(Exception::class)
    fun sqlLiteImplementation() {
        initialiseSqlLiteBackend()
        loadNonExisting()
    }


    @Test
    @Throws(Exception::class)
    fun mysqlImplementation() {
        initialiseMysqlBackend()
        loadNonExisting()
    }

    @Test
    @Throws(Exception::class)
    fun postgeSqlImplementation() {
        initialisePostgeSqlBackend()
        loadNonExisting()
    }

    @Test
    @Throws(Exception::class)
    fun tSqlImplementation() {
        initialiseTSqlBackend()
        loadNonExisting()
    }

    @Test
    @Throws(Exception::class)
    fun oracleImplementation() {
        initialiseOracleBackend()
        loadNonExisting()
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