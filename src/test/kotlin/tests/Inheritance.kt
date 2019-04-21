package tests

import common.BaseTestConfig
import entities.EntityA
import entities.EntityB
import entities.EntityC
import io.github.subiyacryolite.jds.JdsLoad
import io.github.subiyacryolite.jds.JdsSave
import org.junit.jupiter.api.Test

/**
 * Created by ifunga on 01/07/2017.
 */
class Inheritance : BaseTestConfig("Inheritance") {

    @Throws(Exception::class)
    private fun save() {
        val save = JdsSave(jdsDb, inheritanceCollection)
        save.call()
    }

    @Throws(Exception::class)
    private fun load() {
        val entityAs = JdsLoad(jdsDb, EntityA::class.java)
        val entityBs = JdsLoad(jdsDb, EntityB::class.java)
        val entityCs = JdsLoad(jdsDb, EntityC::class.java)
        System.out.printf("All A s [%s]\n", entityAs.call())
        System.out.printf("All B s [%s]\n", entityBs.call())
        System.out.printf("All C s [%s]\n", entityCs.call())
    }

    @Throws(Exception::class)
    private fun saveAndLoad() {
        save()
        load()
    }

    @Test
    @Throws(Exception::class)
    fun testTransactionalSql() {
        initialiseTSqlBackend()
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
    fun testSqLite() {
        initialiseSqLiteBackend()
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
        testTransactionalSql()
        testSqLite()
        testMySql()
        testPostgreSql()
        testOracle()
        testMariaDb()
    }
}
