package io.github.subiyacryolite.jds

import io.github.subiyacryolite.jds.context.DbContext
import java.sql.Connection
import java.util.concurrent.ConcurrentHashMap

object FieldDictionary {

    private val dictionary: ConcurrentHashMap<Int, HashSet<Pair<Int, String>>> = ConcurrentHashMap()

    /**
     * Only performed during initialization (map stage)
     * [propertyName] may unique per entity, thu must be called once for each
     */
    fun registerField(entityId: Int, fieldId: Int, propertyName: String) {
        if (DbContext.initialising) {
            registerField(entityId, Pair(fieldId, propertyName))
        }
    }

    /**
     * Only performed during initialization (map stage)
     * [pair] may contain a unique name per entity, thu must be called once for each
     */
    private fun registerField(entityId: Int, pair: Pair<Int, String>) {
        if (DbContext.initialising) {
            val dict = dictionary.getOrPut(entityId) { HashSet() }
            dict.add(pair)
        }
    }

    fun update(dbContext: DbContext, connection: Connection, entityId: Int) {
        dbContext.getCallOrStatement(connection, dbContext.populateFieldDictionary()).use { statement ->
            dictionary[entityId]?.forEach { (fieldId, propertyName) ->
                statement.setInt(1, entityId)
                statement.setInt(2, fieldId)
                statement.setString(3, propertyName)
                statement.addBatch()
            }
            statement.executeBatch()
        }
    }
}