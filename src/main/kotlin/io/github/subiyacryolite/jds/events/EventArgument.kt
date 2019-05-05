package io.github.subiyacryolite.jds.events

import io.github.subiyacryolite.jds.IJdsDb
import java.sql.*
import java.util.concurrent.ConcurrentMap

/**
 * @property jdsDb
 * @property connection
 * @property alternateConnections
 */
abstract class EventArgument(val jdsDb: IJdsDb, val connection: Connection, protected val alternateConnections: ConcurrentMap<Int, Connection>) {

    protected val statements: LinkedHashMap<String, Statement> = LinkedHashMap()

    /**
     * @param query
     */
    @Synchronized
    @Throws(SQLException::class)
    fun getOrAddStatement(query: String): PreparedStatement {
        if (!statements.containsKey(query))
            statements[query] = connection.prepareStatement(query)
        return statements[query] as PreparedStatement
    }

    /**
     * @param query
     */
    @Synchronized
    @Throws(SQLException::class)
    fun getOrAddCall(query: String): CallableStatement {
        if (!statements.containsKey(query))
            statements[query] = connection.prepareCall(query)
        return statements[query] as CallableStatement
    }

    /**
     * @param query
     * @param targetConnection
     */
    @Synchronized
    @Throws(SQLException::class)
    fun getOrAddStatement(targetConnection: Int, query: String): PreparedStatement {
        prepareConnection(targetConnection)
        if (!statements.containsKey(query))
            statements[query] = alternateConnection(targetConnection).prepareStatement(query)
        return statements[query] as PreparedStatement
    }

    /**
     * @param query
     * @param targetConnection
     */
    @Synchronized
    @Throws(SQLException::class)
    fun getOrAddCall(targetConnection: Int, query: String): CallableStatement {
        prepareConnection(targetConnection)
        if (!statements.containsKey(query))
            statements[query] = alternateConnection(targetConnection).prepareCall(query)
        return statements[query] as CallableStatement
    }

    /**
     * @param query
     * @param connection
     */
    @Synchronized
    @Throws(SQLException::class)
    fun getOrAddStatement(connection: Connection, query: String): PreparedStatement {
        if (!statements.containsKey(query))
            statements[query] = connection.prepareStatement(query)
        return statements[query] as PreparedStatement
    }

    /**
     * @param query
     * @param connection
     */
    @Synchronized
    @Throws(SQLException::class)
    fun getOrAddCall(connection: Connection, query: String): CallableStatement {
        if (!statements.containsKey(query))
            statements[query] = connection.prepareCall(query)
        return statements[query] as CallableStatement
    }

    /**
     * @param query
     * @param connection
     */
    private fun prepareConnection(targetConnection: Int) {
        if (!alternateConnections.containsKey(targetConnection)) {
            val connection = jdsDb.getConnection(targetConnection)
            alternateConnections[targetConnection] = connection
        }
    }

    /**
     * @param query
     * @param connection
     */
    @Throws(SQLException::class)
    open fun executeBatches() {
        connection.autoCommit = false
        for (statement in statements.values) {
            statement.executeBatch()
            statement.close()
        }
        connection.commit()
        connection.autoCommit = true
    }

    /**
     * @param targetConnection
     */
    private fun alternateConnection(targetConnection: Int): Connection {
        return alternateConnections[targetConnection]!!
    }
}