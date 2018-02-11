package tests

import common.BaseTestConfig
import entities.EntityA
import io.github.subiyacryolite.jds.JdsLoad
import io.github.subiyacryolite.jds.JdsSave
import org.junit.jupiter.api.Test
import java.util.*
import java.util.concurrent.Executors

class BulkSaveAndLoad : BaseTestConfig("Bulk save and loads") {

    private fun save() {
        val memObjects = ArrayList<EntityA>()
        for (i in 0..9999) {
            val entry = EntityA()
            entry.overview.uuid = String.format("guidBulk%s", i)
            memObjects.add(entry)
        }
        val save = JdsSave(jdsDb, memObjects)
        val process = Executors.newSingleThreadExecutor().submit(save)
        if (!process.isDone)
            Thread.sleep(16)
        println("Successfully saved $memObjects")
    }

    @Throws(Exception::class)
    private fun load() {
        val entityAs = JdsLoad(jdsDb, EntityA::class.java)
        println("All A's [${entityAs.call()}]")
    }

    private fun saveAndLoad() {
        try {
            save()
            load()
        } catch (ex: Exception) {
            ex.toString()
        }
    }

    @Test
    @Throws(Exception::class)
    fun testPostgreSql() {
        initialisePostgeSqlBackend()//6s 948ms
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun testOracle() {
        initialiseOracleBackend()//11s 79ms
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun testTransactionalSql() {
        initialiseTSqlBackend()//4s 532ms
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun testSqLite() {
        initialiseSqLiteBackend()//12s 501ms
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun testMySql() {
        initialiseMysqlBackend()
        saveAndLoad()//8s 126ms
    }

    @Test
    @Throws(Exception::class)
    fun testMariaDb() {
        initialiseMariaDbBackend()
        saveAndLoad()//8s 126ms
    }

    @Test
    @Throws(Exception::class)
    fun allImplementations() {
        testSqLite()
        testTransactionalSql()
        testPostgreSql()
        testMySql()
        testOracle()
        testMariaDb()
    }
}
