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

    private val inheritanceCollection: List<JdsEntity>
        get() {
            val collection = ArrayList<JdsEntity>()

            val entitya = EntityA()
            entitya.overview.entityGuid = "entityA"
            entitya.entityAValue = "entity A - ValueA"

            val entityb = EntityB()
            entityb.overview.entityGuid = "entityB"
            entityb.entityAValue = "entity B - Value A"
            entityb.entityBValue = "entity B - Value B"

            val entityc = EntityC()
            entityc.overview.entityGuid = "entityC"
            entityc.entityAValue = "entity C - Value A"
            entityc.entityBValue = "entity C - Value B"
            entityc.entityCValue = "entity C - Value C"

            collection.add(entitya)
            collection.add(entityb)
            collection.add(entityc)

            return collection
        }


}
