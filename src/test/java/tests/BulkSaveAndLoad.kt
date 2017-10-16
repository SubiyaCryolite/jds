package tests

import common.BaseTestConfig
import entities.EntityA
import io.github.subiyacryolite.jds.JdsLoad
import io.github.subiyacryolite.jds.JdsSave
import org.junit.jupiter.api.Test
import java.util.*

class BulkSaveAndLoad : BaseTestConfig() {

    @Throws(Exception::class)
    override fun save() {
        val memObjects = ArrayList<EntityA>()
        for (i in 0..9999) {
            val entry = EntityA()
            entry.overview.entityGuid = String.format("guidBulk%s", i)
            memObjects.add(entry)
        }
        val jdsSave = JdsSave(jdsDb, 1024, memObjects)
        jdsSave.call()
        System.out.printf("Saved %s\n", memObjects)
    }

    @Throws(Exception::class)
    override fun load() {
        val entityAs = JdsLoad.load(jdsDb, EntityA::class.java)
        System.out.printf("All A s [%s]\n", entityAs)
    }

    @Test
    @Throws(Exception::class)
    fun saveAndLoadPostreSqlImplementation() {
        initialisePostgeSqlBackend()//13s 175ms
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun saveAndLoadOracleSqlImplementation() {
        initialiseOracleBackend()//11s 79ms
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun saveAndLoadTsqlImplementation() {
        initialiseTSqlBackend()//8s 326ms
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun saveAndLoadSqliteImplementation() {
        initialiseSqlLiteBackend()//12s 501ms
        saveAndLoad()
    }

    @Test
    @Throws(Exception::class)
    fun saveAndLoadMySqlImplementation() {
        initialiseMysqlBackend()
        saveAndLoad()//8s 126ms
    }

    @Test
    @Throws(Exception::class)
    fun saveAndLoadAllImplementations() {
        saveAndLoadSqliteImplementation()
        saveAndLoadTsqlImplementation()
        saveAndLoadPostreSqlImplementation()
        saveAndLoadMySqlImplementation()
        saveAndLoadOracleSqlImplementation()
    }
}
