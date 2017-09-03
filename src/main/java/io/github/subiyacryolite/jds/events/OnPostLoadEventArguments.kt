package io.github.subiyacryolite.jds.events

import com.javaworld.INamedStatement
import com.javaworld.NamedCallableStatement
import com.javaworld.NamedPreparedStatement
import java.sql.*
import java.util.*

/**
 * Created by ifunga on 13/05/2017.
 */
class OnPostLoadEventArguments {
    val entityGuid: String
    val connection: Connection
    private val statements: MutableMap<String, Statement>

    constructor(connection: Connection, entityGuid: String) {
        this.entityGuid = entityGuid
        this.connection = connection
        this.statements = LinkedHashMap<String, Statement>()
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
    fun getOrAddCall(key: String): CallableStatement {
        if (!statements.containsKey(key))
            statements.put(key, connection.prepareCall(key))
        return statements[key] as CallableStatement
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
            preparedStatement.close()
        }
        connection.commit()
        connection.autoCommit = true
    }
}
