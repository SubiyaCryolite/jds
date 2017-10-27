package tests

import common.BaseTestConfig
import entities.EntityA
import entities.EntityB
import entities.EntityC
import io.github.subiyacryolite.jds.JdsEntity
import io.github.subiyacryolite.jds.JdsLoad
import io.github.subiyacryolite.jds.JdsSave
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Created by ifunga on 01/07/2017.
 */
class Ihneritance : BaseTestConfig() {

    @Throws(Exception::class)
    override fun save() {
        val save = JdsSave(jdsDb, inheritanceCollection)
        save.call()
    }

    @Throws(Exception::class)
    override fun load() {
        val entityAs = JdsLoad.load(jdsDb, EntityA::class.java)
        val entityBs = JdsLoad.load(jdsDb, EntityB::class.java)
        val entityCs = JdsLoad.load(jdsDb, EntityC::class.java)
        System.out.printf("All A s [%s]\n", entityAs)
        System.out.printf("All B s [%s]\n", entityBs)
        System.out.printf("All C s [%s]\n", entityCs)
    }

    @Test
    @Throws(Exception::class)
    fun tSqlImplementation() {
        initialiseTSqlBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun mysSqlImplementation() {
        initialiseMysqlBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun postgresSqlImplementation() {
        initialisePostgeSqlBackend()
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun sqLiteImplementation() {
        initialiseSqlLiteBackend()
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
        tSqlImplementation()
        sqLiteImplementation()
        mysSqlImplementation()
        postgresSqlImplementation()
        oracleImplementation()
    }
}
