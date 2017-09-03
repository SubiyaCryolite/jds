/*
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
import java.sql.SQLException
import java.util.*
import java.util.concurrent.Callable
import kotlin.streams.toList

/**
 * This class is responsible for deleting [JdsEntities][JdsEntity] in the [JdsDataBase][JdsDb]
 */
/**
 * @param jdsDb
 * @param entityGuids
 */
class JdsDelete(private val jdsDb: JdsDb, entityGuids: List<CharSequence>) : Callable<Boolean> {

    private val entities: Collection<CharSequence>

    init {
        this.entities = entityGuids
    }

    /**
     * @param jdsDb
     * @param entityGuids
     */
    constructor(jdsDb: JdsDb, vararg entityGuids: String) : this(jdsDb, Arrays.asList<String>(*entityGuids)) {}

    /**
     * @param jdsDb
     * @param entities
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @Throws(SQLException::class, ClassNotFoundException::class)
    constructor(jdsDb: JdsDb, entities: Collection<JdsEntity>) : this(jdsDb, entities.stream().map({it.overview.entityGuid}).toList()) {
        val connection = jdsDb.getConnection()
        entities.forEach { entity ->
            if (entity is JdsDeleteListener)
                (entity as JdsDeleteListener).onDelete(OnDeleteEventArguments(this.jdsDb, connection, entity.overview.entityGuid))
        }
    }

    /**
     * @param jdsDb
     * @param entities
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @Throws(SQLException::class, ClassNotFoundException::class)
    constructor(jdsDb: JdsDb, vararg entities: JdsEntity) : this(jdsDb, Arrays.asList<JdsEntity>(*entities)) {
    }

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
        private val DELETE_SQL = "DELETE FROM JdsStoreEntityOverview WHERE EntityGuid = ?"

        /**
         * @param jdsDb
         * @param entities
         * @throws Exception
         */
        @Deprecated("please refer to <a href=\"https://github.com/SubiyaCryolite/Jenesis-Data-Store\"> the readme</a> for the most up to date CRUD approach", ReplaceWith("JdsDelete(jdsDb, entities).call()", "io.github.subiyacryolite.jds.JdsDelete"))
        @Throws(Exception::class)
        fun delete(jdsDb: JdsDb, entities: Collection<JdsEntity>) {
            JdsDelete(jdsDb, entities).call()
        }

        /**
         * @param jdsDb
         * @param entities
         * @throws Exception
         */
        @Deprecated("please refer to <a href=\"https://github.com/SubiyaCryolite/Jenesis-Data-Store\"> the readme</a> for the most up to date CRUD approach", ReplaceWith("delete(jdsDb, Arrays.asList(*entities))", "io.github.subiyacryolite.jds.JdsDelete.Companion.delete", "java.util.Arrays"))
        @Throws(Exception::class)
        fun delete(jdsDb: JdsDb, vararg entities: JdsEntity) {
            delete(jdsDb, Arrays.asList(*entities))
        }

        /**
         * @param jdsDb
         * @param entityGuids
         * @throws Exception
         */
        @Deprecated("please refer to <a href=\"https://github.com/SubiyaCryolite/Jenesis-Data-Store\"> the readme</a> for the most up to date CRUD approach", ReplaceWith("JdsDelete(jdsDb, *entityGuids).call()", "io.github.subiyacryolite.jds.JdsDelete"))
        @Throws(Exception::class)
        fun delete(jdsDb: JdsDb, vararg entityGuids: String) {
            JdsDelete(jdsDb, *entityGuids).call()
        }
    }
}