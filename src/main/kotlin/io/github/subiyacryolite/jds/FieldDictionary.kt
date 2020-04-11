package io.github.subiyacryolite.jds

import io.github.subiyacryolite.jds.context.DbContext
import java.sql.Connection
import java.util.concurrent.ConcurrentHashMap

object FieldDictionary {

    private val dictionary: ConcurrentHashMap<Int, HashSet<Pair<Int, String>>> = ConcurrentHashMap()

    fun addEntityField(entityId: Int, fieldId: Int, propertyName: String) {
        if (Entity.initialising) {
            addEntityField(entityId, Pair(fieldId, propertyName))
        }
    }

    private fun addEntityField(entityId: Int, pair: Pair<Int, String>) {
        if (Entity.initialising) {
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