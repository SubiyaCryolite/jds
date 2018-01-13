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

import io.github.subiyacryolite.jds.events.JdsDeleteListener
import io.github.subiyacryolite.jds.events.OnDeleteEventArguments
import java.sql.Connection
import java.sql.SQLException
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * This class is responsible for deleting [JdsEntities][JdsEntity] in the [JdsDataBase][JdsDb]
 */
/**
 * @param jdsDb
 * @param uuids
 */
class JdsDelete(private val jdsDb: JdsDb, uuids: List<CharSequence>) : Callable<Boolean> {

    private val entities: Collection<CharSequence>
    private val alternateConnections: ConcurrentMap<Int, Connection> = ConcurrentHashMap()

    init {
        this.entities = uuids
    }

    /**
     * @param jdsDb
     * @param entities
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @Throws(SQLException::class, ClassNotFoundException::class)
    constructor(jdsDb: JdsDb, entities: Collection<JdsEntity>) : this(jdsDb, entities.map({ it.overview.uuid })) {

        jdsDb.getConnection().use { connection ->
            val args = OnDeleteEventArguments(jdsDb, connection, alternateConnections)
            entities.forEach { entity ->
                if (entity is JdsDeleteListener)
                    entity.onDelete(args)
                jdsDb.tables.forEach { it.deleteRecord(jdsDb, args, connection, entity) }
            }
        }

        //close alternate connections
        alternateConnections.forEach { it.value.close() }
    }

    /**
     * @param jdsDb
     * @param entities
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @Throws(SQLException::class, ClassNotFoundException::class)
    constructor(jdsDb: JdsDb, vararg entities: JdsEntity) : this(jdsDb, Arrays.asList<JdsEntity>(*entities))

    /**
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    override fun call(): Boolean? {
        jdsDb.getConnection().use { connection ->
            connection.prepareStatement(DELETE_SQL).use { statement ->
                connection.autoCommit = false
                for (entity in entities) {
                    statement.setString(1, entity.toString())
                    statement.addBatch()
                }
                statement.executeBatch()
                connection.commit()
            }
        }
        return true
    }


    companion object {
        private val DELETE_SQL = "DELETE FROM jds_entity_overview WHERE UUID = ?"

        /**
         * @param jdsDb
         * @param entities
         * @throws Exception
         */
        @Deprecated("please refer to <a href=\"https://github.com/SubiyaCryolite/Jenesis-Data-Store\"> the readme</a> for the most up to date CRUD approach", ReplaceWith("JdsDelete(jdsDb, entityVersions).call()", "io.github.subiyacryolite.jds.JdsDelete"))
        @Throws(Exception::class)
        fun delete(jdsDb: JdsDb, entities: Collection<JdsEntity>) {
            JdsDelete(jdsDb, entities).call()
        }

        /**
         * @param jdsDb
         * @param entities
         * @throws Exception
         */
        @Deprecated("please refer to <a href=\"https://github.com/SubiyaCryolite/Jenesis-Data-Store\"> the readme</a> for the most up to date CRUD approach", ReplaceWith("delete(jdsDb, Arrays.asList(*entityVersions))", "io.github.subiyacryolite.jds.JdsDelete.Companion.delete", "java.util.Arrays"))
        @Throws(Exception::class)
        fun delete(jdsDb: JdsDb, vararg entities: JdsEntity) {
            delete(jdsDb, Arrays.asList(*entities))
        }
    }
}