package tests

import common.BaseTestConfig
import io.github.subiyacryolite.jds.JdsDelete
import org.junit.jupiter.api.Test

class Deletes : BaseTestConfig() {

    @Test
    @Throws(Exception::class)
    fun deleteUsingStrings() {
        initialiseSqlLiteBackend()
        val result = JdsDelete(jdsDb, "primaryAddress1").call()
        print("Completed " + result!!)
    }

    @Test
    @Throws(Exception::class)
    fun sqLiteImplementation() {
        initialiseSqlLiteBackend()
        deleteUsingStrings()
    }

    @Test
    @Throws(Exception::class)
    fun mysqlImplementation() {
        initialiseMysqlBackend()
        deleteUsingStrings()
    }

    @Test
    @Throws(Exception::class)
    fun postgreSqlImplementation() {
        initialisePostgeSqlBackend()
        deleteUsingStrings()
    }

    @Test
    @Throws(Exception::class)
    fun tSqlImplementation() {
        initialiseTSqlBackend()
        deleteUsingStrings()
    }

    @Test
    @Throws(Exception::class)
    fun oracleImplementation() {
        initialiseOracleBackend()
        deleteUsingStrings()
    }

    @Test
    @Throws(Exception::class)
    fun allImplementations() {
        mysqlImplementation()
        oracleImplementation()
        postgreSqlImplementation()
        sqLiteImplementation()
        tSqlImplementation()
    }
}