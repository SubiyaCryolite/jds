package tests

import common.BaseTestConfig
import entities.EntityA
import io.github.subiyacryolite.jds.JdsLoad
import io.github.subiyacryolite.jds.JdsSave
import org.junit.jupiter.api.Test
import java.util.*
import java.util.concurrent.Executors

class BulkSaveAndLoad : BaseTestConfig() {

    @Throws(Exception::class)
    private fun save() {
        val memObjects = ArrayList<EntityA>()
        for (i in 0..9999) {
            val entry = EntityA()
            entry.overview.uuid = String.format("guidBulk%s", i)
            memObjects.add(entry)
        }
        val save = JdsSave(jdsDb, memObjects,1024)
        val process= Executors.newSingleThreadExecutor().submit(save)
        if(!process.isDone)
            Thread.sleep(16)
        println("Successfully saved $memObjects")
    }

    @Throws(Exception::class)
    private fun load() {
        val entityAs = JdsLoad(jdsDb, EntityA::class.java)
        println("All A's [${entityAs.call()}]")
    }

    @Throws(Exception::class)
    private fun saveAndLoad() {
        save()
        load()
    }

    @Test
    @Throws(Exception::class)
    fun testPostgreSql() {
        initialisePostgeSqlBackend()//13s 175ms
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
        initialiseTSqlBackend()//8s 326ms
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
    fun allImplementations() {
        testSqLite()
        testTransactionalSql()
        testPostgreSql()
        testMySql()
        testOracle()
    }
}
