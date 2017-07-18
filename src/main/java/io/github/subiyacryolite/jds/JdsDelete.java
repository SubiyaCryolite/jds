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
package io.github.subiyacryolite.jds;

import io.github.subiyacryolite.jds.events.JdsDeleteListener;
import io.github.subiyacryolite.jds.events.OnDeleteEventArguments;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is responsible for deleting {@link JdsEntity JdsEntities} in the {@link JdsDb JdsDataBase}
 */
public class JdsDelete implements Callable<Boolean> {
    private final static String DELETE_SQL = "DELETE FROM JdsStoreEntityOverview WHERE EntityGuid = ?";
    private final JdsDb jdsDb;
    private final Collection<String> entities;

    public Boolean call() throws Exception {
        try (Connection connection = jdsDb.getConnection(); PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            connection.setAutoCommit(false);
            for (String entity : entities) {
                statement.setString(1, entity);
                statement.addBatch();
            }
            statement.executeBatch();
            connection.commit();
        }
        return true;
    }

    public JdsDelete(final JdsDb jdsDb, final Collection<JdsEntity> entities) {
        this.jdsDb = jdsDb;
        this.entities = entities.stream().map(x -> x.getEntityGuid()).collect(Collectors.toList());
    }

    public JdsDelete(final JdsDb jdsDb, final JdsEntity... entities) {
        this.jdsDb = jdsDb;
        Stream<JdsEntity> entityStream = Arrays.stream(entities);
        entityStream.forEach(entity -> {
            if(entity instanceof JdsDeleteListener)
            ((JdsDeleteListener) entity).onDelete(new OnDeleteEventArguments(this.jdsDb, entity.getEntityGuid()));
        });
        this.entities = Arrays.stream(entities).map(x -> x.getEntityGuid()).collect(Collectors.toList());
    }

    public JdsDelete(final JdsDb jdsDb, final String... entityGuids) {
        this.jdsDb = jdsDb;
        this.entities = Arrays.stream(entityGuids).collect(Collectors.toList());
    }

    /**
     * @param jdsDb
     * @param entities
     * @throws Exception
     * @deprecated please refer to <a href="https://github.com/SubiyaCryolite/Jenesis-Data-Store"> the readme</a> for the most up to date CRUD approach
     */
    public static void delete(final JdsDb jdsDb, final Collection<JdsEntity> entities) throws Exception {
        new JdsDelete(jdsDb, entities).call();
    }

    /**
     * @param jdsDb
     * @param entities
     * @throws Exception
     * @deprecated please refer to <a href="https://github.com/SubiyaCryolite/Jenesis-Data-Store"> the readme</a> for the most up to date CRUD approach
     */
    public static void delete(final JdsDb jdsDb, final JdsEntity... entities) throws Exception {
        delete(jdsDb, Arrays.asList(entities));
    }

    /**
     * @param jdsDb
     * @param entityGuids
     * @throws Exception
     * @deprecated please refer to <a href="https://github.com/SubiyaCryolite/Jenesis-Data-Store"> the readme</a> for the most up to date CRUD approach
     */
    public static void delete(final JdsDb jdsDb, final String... entityGuids) throws Exception {
        new JdsDelete(jdsDb, entityGuids).call();
    }
}
