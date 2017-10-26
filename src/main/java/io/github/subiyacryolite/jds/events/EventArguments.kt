package io.github.subiyacryolite.jds.events

import com.javaworld.INamedStatement
import com.javaworld.NamedCallableStatement
import com.javaworld.NamedPreparedStatement
import io.github.subiyacryolite.jds.IJdsDb
import java.sql.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

abstract class EventArguments(val jdsDb: IJdsDb, val connection: Connection, private val alternateConnection: ConcurrentMap<Int, Connection>) {

    protected val statements: LinkedHashMap<String, Statement> = LinkedHashMap()

    @Synchronized
    @Throws(SQLException::class)
    fun getOrAddStatement(query: String): PreparedStatement {
        if (!statements.containsKey(query))
            statements.put(query, connection.prepareStatement(query))
        return statements[query] as PreparedStatement
    }

    @Synchronized
    @Throws(SQLException::class)
    fun getOrAddCall(query: String): CallableStatement {
        if (!statements.containsKey(query))
            statements.put(query, connection.prepareCall(query))
        return statements[query] as CallableStatement
    }

    @Synchronized
    @Throws(SQLException::class)
    fun getOrAddNamedStatement(query: String): INamedStatement {
        if (!statements.containsKey(query))
            statements.put(query, NamedPreparedStatement(connection, query))
        return statements[query] as INamedStatement
    }

    @Synchronized
    @Throws(SQLException::class)
    fun getOrAddNamedCall(query: String): INamedStatement {
        if (!statements.containsKey(query))
            statements.put(query, NamedCallableStatement(connection, query))
        return statements[query] as INamedStatement
    }

    @Synchronized
    @Throws(SQLException::class)
    fun getOrAddStatement(targetConnection: Int, query: String): PreparedStatement {
        prepareConnection(targetConnection)
        if (!statements.containsKey(query))
            statements.put(query, alternateConnection(targetConnection).prepareStatement(query))
        return statements[query] as PreparedStatement
    }

    @Synchronized
    @Throws(SQLException::class)
    fun getOrAddCall(targetConnection: Int, query: String): CallableStatement {
        prepareConnection(targetConnection)
        if (!statements.containsKey(query))
            statements.put(query, alternateConnection(targetConnection).prepareCall(query))
        return statements[query] as CallableStatement
    }

    @Synchronized
    @Throws(SQLException::class)
    fun getOrAddNamedStatement(targetConnection: Int, query: String): INamedStatement {
        prepareConnection(targetConnection)
        if (!statements.containsKey(query))
            statements.put(query, NamedPreparedStatement(alternateConnection(targetConnection), query))
        return statements[query] as INamedStatement
    }

    @Synchronized
    @Throws(SQLException::class)
    fun getOrAddNamedCall(targetConnection: Int, query: String): INamedStatement {
        prepareConnection(targetConnection)
        if (!statements.containsKey(query))
            statements.put(query, NamedCallableStatement(alternateConnection(targetConnection), query))
        return statements[query] as INamedStatement
    }

    private fun prepareConnection(targetConnection: Int) {
        if (!alternateConnection.containsKey(targetConnection)) {
            val connection = jdsDb.getConnection(targetConnection)
            connection.autoCommit = false
            alternateConnection.put(targetConnection, connection)
        }
    }

    @Throws(SQLException::class)
    open fun executeBatches() {
        connection.autoCommit = false
        for (preparedStatement in statements.values) {
            preparedStatement.executeBatch()
            preparedStatement.close()
        }
        connection.commit()
        connection.autoCommit = true
    }

    private fun alternateConnection(targetConnection: Int): Connection {
        return alternateConnection[targetConnection]!!
    }
}