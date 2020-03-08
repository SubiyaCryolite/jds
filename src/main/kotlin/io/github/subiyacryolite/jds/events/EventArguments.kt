package io.github.subiyacryolite.jds.events

import java.io.Closeable
import java.sql.Connection
import java.sql.PreparedStatement

/**
 * @param connection the connection to use when invoking [execute]
 * @param closeConnection indicates if the connection should be closed when invoking [close]
 */
class EventArguments(
        private val connection: Connection,
        private val closeConnection: Boolean = true
) : Closeable {

    private val statements: LinkedHashMap<String, PreparedStatement> = LinkedHashMap()

    /**
     * @param query
     */
    @Throws(Exception::class)
    fun getOrAddStatement(query: String): PreparedStatement {
        if (!statements.containsKey(query)) {
            statements[query] = connection.prepareStatement(query)
        }
        return statements[query]!!
    }

    /**
     * @param query
     */
    @Throws(Exception::class)
    fun getOrAddCall(query: String): PreparedStatement {
        if (!statements.containsKey(query)) {
            statements[query] = connection.prepareCall(query)
        }
        return statements[query]!!
    }

    /**
     * @param query
     * @param connection
     */
    @Throws(Exception::class)
    fun getOrAddStatement(connection: Connection, query: String): PreparedStatement {
        if (!statements.containsKey(query)) {
            statements[query] = connection.prepareStatement(query)
        }
        return statements[query]!!
    }

    /**
     *
     */
    fun execute() = try {
        connection.autoCommit = false
        for (statement in statements.values) {
            statement.executeBatch()
            statement.clearBatch()
        }
        connection.commit()
    } catch (exception: Exception) {
        connection.rollback()
        exception.printStackTrace(System.err)
    } finally {
        connection.autoCommit = true
    }

    override fun close() {
        for (statement in statements.values) {
            statement.close()
        }
        if (closeConnection) {
            connection.close()
        }
    }
}