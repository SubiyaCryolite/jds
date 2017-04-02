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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by ifunga on 13/03/2017.
 */
public class JdsDelete {
    private final static String DELETE_SQL ="DELETE FROM JdsStoreEntityOverview WHERE EntityGuid = ?";

    public static void delete(final JdsDatabase jdsDatabase, final Collection<? extends JdsEntity> entities) {
        try (Connection connection = jdsDatabase.getConnection(); PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            connection.setAutoCommit(false);
            for (JdsEntity entity : entities) {
                statement.setString(1, entity.getEntityGuid());
                statement.addBatch();
            }
            statement.executeBatch();
            connection.commit();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    public static void delete(final JdsDatabase jdsDatabase, final JdsEntity... entities) {
        delete(jdsDatabase, Arrays.asList(entities));
    }

    public static void delete(final JdsDatabase jdsDatabase, final String... entityGuids) {
        try (Connection connection = jdsDatabase.getConnection(); PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            connection.setAutoCommit(false);
            for (String entityGuid : entityGuids) {
                statement.setString(1, entityGuid);
                statement.addBatch();
            }
            statement.executeBatch();
            connection.commit();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }
}
