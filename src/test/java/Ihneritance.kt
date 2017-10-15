

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
        System.out.printf("=========== %s ===========\n", jdsDb.implementation)
        val entityAs = JdsLoad.load(jdsDb, EntityA::class.java)
        val entityBs = JdsLoad.load(jdsDb, EntityB::class.java)
        val entityCs = JdsLoad.load(jdsDb, EntityC::class.java)
        System.out.printf("All A s [%s]\n", entityAs)
        System.out.printf("All B s [%s]\n", entityBs)
        System.out.printf("All C s [%s]\n", entityCs)
    }

    @Test
    @Throws(Exception::class)
    fun testIheritanceOracle() {
        initialiseOracleBackend()
        save()
        jdsDb.toString()
    }

    @Test
    @Throws(Exception::class)
    fun testIheritanceSqlite() {
        initialiseSqlLiteBackend()
        save()
        jdsDb.toString()
    }

    @Test
    @Throws(Exception::class)
    fun testIheritanceMySql() {
        initialiseMysqlBackend()
        save()
        jdsDb.toString()
    }

    @Test
    @Throws(Exception::class)
    fun testIheritancePostgreSQL() {
        initialisePostgeSqlBackend()
        save()
        jdsDb.toString()
    }

    @Test
    @Throws(Exception::class)
    fun testIheritanceTSql() {
        initialiseTSqlBackend()
        save()
        jdsDb.toString()
    }

    @Test
    @Throws(Exception::class)
    fun testAllInitialilization() {
        testIheritanceSqlite()
        testIheritancePostgreSQL()
        testIheritanceTSql()
        testIheritanceMySql()
        testIheritanceOracle()
    }

    @Test
    @Throws(Exception::class)
    fun loadInheritedTsql() {
        initialiseTSqlBackend()
        load()
    }

    @Test
    @Throws(Exception::class)
    fun loadInheritedMysSql() {
        initialiseMysqlBackend()
        load()
    }

    @Test
    @Throws(Exception::class)
    fun loadInheritedPostgresSql() {
        initialisePostgeSqlBackend()
        load()
    }

    @Test
    @Throws(Exception::class)
    fun loadInheritedSqLite() {
        initialiseSqlLiteBackend()
        load()
    }

    @Test
    @Throws(Exception::class)
    fun loadInheritedOracle() {
        initialiseOracleBackend()
        load()
    }

    @Test
    @Throws(Exception::class)
    fun loadInheritedAll() {
        loadInheritedTsql()
        loadInheritedSqLite()
        loadInheritedMysSql()
        loadInheritedPostgresSql()
        loadInheritedOracle()
    }

    private //constant
            //constant
            //constant
    val inheritanceCollection: List<JdsEntity>
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
