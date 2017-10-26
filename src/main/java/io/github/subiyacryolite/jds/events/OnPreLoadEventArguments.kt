/**
 * Jenesis Data Store Copyright (c) 2017 Ifunga Ndana. All rights reserved.
 *
 * 1. Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 2. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 3. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * Neither the name Jenesis Data Store nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package io.github.subiyacryolite.jds.events

import com.javaworld.INamedStatement
import com.javaworld.NamedCallableStatement
import com.javaworld.NamedPreparedStatement
import io.github.subiyacryolite.jds.IJdsDb
import java.sql.*
import java.util.*

/**
 * Event arguments to handle this listener invocation.
 * This class supports batching via the use of the {@link #getOrAddCall(String) getOrAddCall},
 * {@link #getOrAddStatement(String) getOrAddStatement}, {@link #getOrAddNamedCall(String) getOrAddNamedCall} and
 * {@link #getOrAddNamedStatement(String) getOrAddNamedStatement} methods.
 */
class OnPreLoadEventArguments {
    val uuid: String
    val batchSequence: Int
    val batchSize: Int
    val jdsDb: IJdsDb
    val connection: Connection
    val alternateConnection: LinkedHashMap<Int, Connection>
    private val statements: LinkedHashMap<String, Statement>
    private val alternateStatements: LinkedHashMap<Int, LinkedHashMap<String, Statement>>

    constructor(jdsDb: IJdsDb,connection: Connection, uuid: String, batchSequence: Int, batchSize: Int) {
        this.uuid = uuid
        this.batchSequence = batchSequence
        this.batchSize = batchSize
        this.jdsDb = jdsDb
        this.connection = connection
        this.alternateConnection = LinkedHashMap()
        this.statements = LinkedHashMap()
        this.alternateStatements = LinkedHashMap()
    }

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
        if (!alternateStatements[targetConnection]!!.containsKey(query))
            alternateStatements[targetConnection]!!.put(query, alternateConnection(targetConnection).prepareStatement(query))
        return alternateStatements[targetConnection]!![query] as PreparedStatement
    }

    @Synchronized
    @Throws(SQLException::class)
    fun getOrAddCall(targetConnection: Int, query: String): CallableStatement {
        prepareConnection(targetConnection)
        if (!alternateStatements[targetConnection]!!.containsKey(query))
            alternateStatements[targetConnection]!!.put(query, alternateConnection(targetConnection).prepareCall(query))
        return alternateStatements[targetConnection]!![query] as CallableStatement
    }

    @Synchronized
    @Throws(SQLException::class)
    fun getOrAddNamedStatement(targetConnection: Int, query: String): INamedStatement {
        prepareConnection(targetConnection)
        if (!alternateStatements[targetConnection]!!.containsKey(query))
            alternateStatements[targetConnection]!!.put(query, NamedPreparedStatement(alternateConnection(targetConnection), query))
        return alternateStatements[targetConnection]!![query] as INamedStatement
    }

    @Synchronized
    @Throws(SQLException::class)
    fun getOrAddNamedCall(targetConnection: Int, query: String): INamedStatement {
        prepareConnection(targetConnection)
        if (!alternateStatements[targetConnection]!!.containsKey(query))
            alternateStatements[targetConnection]!!.put(query, NamedCallableStatement(alternateConnection(targetConnection), query))
        return statements[query] as INamedStatement
    }

    private fun prepareConnection(targetConnection: Int) {
        if (!alternateConnection.containsKey(targetConnection))
            alternateConnection.put(targetConnection, jdsDb.getConnection(targetConnection))
        if (!alternateStatements.containsKey(targetConnection))
            alternateStatements.put(targetConnection, LinkedHashMap<String, Statement>())
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

        alternateConnection.forEach { targetConnection, con ->
            con.autoCommit = false
            executeStatementsOnConnection(targetConnection)
            con.commit()
            con.autoCommit = true

            //close alternate connections too as they were created internally
            con.close();
        }
    }

    private fun executeStatementsOnConnection(targetConnection: Int) {
        alternateStatements.filter { it.key == targetConnection }.forEach {
            it.value.forEach {
                it.value.executeBatch()
                it.value.close()
            }
        }
    }

    private fun alternateConnection(targetConnection: Int): Connection {
        if (!alternateConnection.containsKey(targetConnection))
            alternateConnection.put(targetConnection, jdsDb.getConnection(targetConnection))
        return alternateConnection[targetConnection]!!
    }
}
