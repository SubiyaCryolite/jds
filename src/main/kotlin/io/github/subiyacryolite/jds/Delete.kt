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
package io.github.subiyacryolite.jds

import io.github.subiyacryolite.jds.context.DbContext
import io.github.subiyacryolite.jds.events.EventArguments
import io.github.subiyacryolite.jds.events.DeleteListener
import java.sql.Connection
import java.sql.SQLException
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * This class is responsible for deleting [JdsEntities][Entity] in the [DbContext][DbContext]
 */
/**
 * @param dbContext
 * @param uuids
 */
class Delete(private val dbContext: DbContext, private val ids: Iterable<String>) : Callable<Boolean> {

    private val alternateConnections: ConcurrentMap<Int, Connection> = ConcurrentHashMap()

    /**
     * @param dbContext
     * @param entities
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @Throws(SQLException::class, ClassNotFoundException::class)
    constructor(dbContext: DbContext, entities: Collection<Entity>) : this(dbContext, entities.map { it.overview.id }) {

        dbContext.dataSource.connection.use { connection ->
            val args = EventArguments(connection)
            entities.forEach { entity ->
                if (entity is DeleteListener)
                    entity.onDelete(args)
            }
        }
        //close alternate connections, leave main one as is
        alternateConnections.forEach { it.value.close() }
    }

    /**
     * @param dbContext
     * @param entities
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @Throws(SQLException::class, ClassNotFoundException::class)
    constructor(dbContext: DbContext, vararg entities: Entity) : this(dbContext, listOf(*entities))

    /**
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    override fun call(): Boolean? {
        dbContext.dataSource.connection.use { connection ->
            connection.prepareStatement(DELETE_SQL).use { statement ->
                connection.autoCommit = false
                for (id in ids) {
                    statement.setString(1, id)
                    statement.addBatch()
                }
                statement.executeBatch()
                connection.commit()
            }
        }
        return true
    }


    companion object {
        private const val DELETE_SQL = "DELETE FROM jds_entity_overview WHERE id = ?"
    }
}