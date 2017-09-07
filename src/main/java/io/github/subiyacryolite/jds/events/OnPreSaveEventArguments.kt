package io.github.subiyacryolite.jds.events

import com.javaworld.INamedStatement
import com.javaworld.NamedCallableStatement
import com.javaworld.NamedPreparedStatement
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.sql.Statement
import java.util.*

/**
 * Event arguments to handle this listener invocation.
 * This class supports batching via the use of the {@link #getOrAddCall(String) getOrAddCall},
 * {@link #getOrAddStatement(String) getOrAddStatement}, {@link #getOrAddNamedCall(String) getOrAddNamedCall} and
 * {@link #getOrAddNamedStatement(String) getOrAddNamedStatement} methods.
 */
class OnPreSaveEventArguments {
    val connection: Connection
    private val statements: LinkedHashMap<String, Statement>

    constructor(connection: Connection) {
        this.connection = connection
        this.statements = LinkedHashMap()
    }

    @Synchronized
    @Throws(SQLException::class)
    fun getOrAddStatement(key: String): PreparedStatement {
        if (!statements.containsKey(key))
            statements.put(key, connection.prepareStatement(key))
        return statements[key] as PreparedStatement
    }

    @Synchronized
    @Throws(SQLException::class)
    fun getOrAddCall(key: String): PreparedStatement {
        if (!statements.containsKey(key))
            statements.put(key, connection.prepareCall(key))
        return statements[key] as PreparedStatement
    }

    @Synchronized
    @Throws(SQLException::class)
    fun getOrAddNamedStatement(key: String): INamedStatement {
        if (!statements.containsKey(key))
            statements.put(key, NamedPreparedStatement(connection, key))
        return statements[key] as INamedStatement
    }

    @Synchronized
    @Throws(SQLException::class)
    fun getOrAddNamedCall(key: String): INamedStatement {
        if (!statements.containsKey(key))
            statements.put(key, NamedCallableStatement(connection, key))
        return statements[key] as INamedStatement
    }

    @Throws(SQLException::class)
    fun executeBatches() {
        connection.autoCommit = false
        for (preparedStatement in statements.values) {
            preparedStatement.executeBatch()
        }
        connection.commit()
        connection.autoCommit = true
    }

    @Throws(SQLException::class)
    fun closeBatches() {
        for (preparedStatement in statements.values) {
            preparedStatement.close()
        }
        connection.autoCommit = true
    }
}
