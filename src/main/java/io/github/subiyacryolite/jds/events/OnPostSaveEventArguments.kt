package io.github.subiyacryolite.jds.events

import com.javaworld.INamedStatement
import com.javaworld.NamedCallableStatement
import com.javaworld.NamedPreparedStatement
import java.sql.*
import java.util.*

/**
 * Created by ifunga on 13/05/2017.
 */
class OnPostSaveEventArguments {
    val connection: Connection
    private val statements: LinkedHashMap<String, Statement>

    constructor(connection: Connection) {
        this.connection = connection
        this.statements = LinkedHashMap()
    }

    @Throws(SQLException::class)
    @Synchronized
    fun getOrAddStatement(key: String): PreparedStatement {
        if (!statements.containsKey(key))
            statements.put(key, connection.prepareStatement(key))
        return statements[key] as PreparedStatement
    }

    @Throws(SQLException::class)
    @Synchronized
    fun getOrAddCall(key: String): CallableStatement {
        if (!statements.containsKey(key))
            statements.put(key, connection.prepareCall(key))
        return statements[key] as CallableStatement
    }

    @Throws(SQLException::class)
    @Synchronized
    fun getOrAddNamedStatement(key: String): INamedStatement {
        if (!statements.containsKey(key))
            statements.put(key, NamedPreparedStatement(connection, key))
        return statements[key] as INamedStatement
    }

    @Throws(SQLException::class)
    @Synchronized
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
