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
import java.sql.*
import java.util.*

/**
 * Event arguments to handle this listener invocation.
 * This class supports batching via the use of the {@link #getOrAddCall(String) getOrAddCall},
 * {@link #getOrAddStatement(String) getOrAddStatement}, {@link #getOrAddNamedCall(String) getOrAddNamedCall} and
 * {@link #getOrAddNamedStatement(String) getOrAddNamedStatement} methods.
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
