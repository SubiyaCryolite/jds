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

import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation;
import io.github.subiyacryolite.jds.enums.JdsFieldType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import static io.github.subiyacryolite.jds.JdsTableLookup.getTablePrefix;
import static io.github.subiyacryolite.jds.JdsTableLookup.getTable;

/**
 * This class is used to perform basic searches based on defined parameters
 */
public class JdsFilter<T extends JdsEntity> implements AutoCloseable, Callable<List<T>> {


    private final LinkedList<LinkedList<Object>> blockParamaters;
    private final LinkedList<LinkedList<String>> blockStrings;
    private final LinkedList<String> blockSwitches;
    private final HashSet<JdsFieldType> tablesToJoin;
    private final JdsDb jdsDb;
    private final Class<T> referenceType;
    private LinkedList<String> currentStrings;
    private LinkedList<Object> currentValues;
    private long entityId;

    /**
     * @param jdsDb
     * @param referenceType
     */
    public JdsFilter(JdsDb jdsDb, final Class<T> referenceType) {
        this.jdsDb = jdsDb;
        this.referenceType = referenceType;
        if (referenceType.isAnnotationPresent(JdsEntityAnnotation.class)) {
            JdsEntityAnnotation je = referenceType.getAnnotation(JdsEntityAnnotation.class);
            entityId = je.entityId();
        } else
            throw new IllegalArgumentException("You must annotate the class [" + referenceType.getCanonicalName() + "] with [" + JdsEntityAnnotation.class + "]");
        //////////
        currentStrings = new LinkedList<>();
        currentValues = new LinkedList<>();
        //==================================
        blockStrings = new LinkedList<>();
        blockParamaters = new LinkedList<>();
        //==================================
        tablesToJoin = new HashSet<>();
        blockSwitches = new LinkedList<>();
        //==================================
        blockStrings.add(currentStrings);
        blockParamaters.add(currentValues);
        blockSwitches.add("");
    }

    public JdsFilter or() {
        currentStrings = new LinkedList<>();
        blockStrings.add(currentStrings);
        currentValues = new LinkedList<>();
        blockParamaters.add(currentValues);
        blockSwitches.add(" OR ");
        return this;
    }

    public JdsFilter and() {
        currentStrings = new LinkedList<>();
        blockStrings.add(currentStrings);
        currentValues = new LinkedList<>();
        blockParamaters.add(currentValues);
        blockSwitches.add(" AND ");
        return this;
    }

    @Override
    public void close() throws Exception {
        blockParamaters.clear();
        blockStrings.clear();
        blockSwitches.clear();
        tablesToJoin.clear();
        currentStrings.clear();
        currentValues.clear();
    }

    @Override
    public List<T> call() throws Exception {
        List<String> matchingGuids = new ArrayList<>();
        String sql = this.toQuery();
        try (Connection connection = jdsDb.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            for (LinkedList<Object> parameters : blockParamaters) {
                for (Object paramter : parameters) {
                    ps.setObject(parameterIndex, paramter);
                    parameterIndex++;
                }
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                matchingGuids.add(rs.getString("EntityGuid"));
            }
            rs.close();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
        if (matchingGuids.isEmpty()) {
            //no results. return empty collection
            //if you pass empty collection to jds load it will assume load EVERYTHING
            return new ArrayList<>();
        }
        return new JdsLoad(jdsDb, referenceType, matchingGuids.toArray(new String[0])).call();
    }


    public String toQuery() {
        StringBuilder main = new StringBuilder();
        main.append("SELECT DISTINCT eo.EntityGuid FROM JdsStoreEntityInheritance eo\n");
        main.append("JOIN JdsRefEntities entity ON eo.EntityId = entity.EntityId");
        if (tablesToJoin.size() > 0) {
            main.append(" JOIN ");
            main.append(createLeftJoins(tablesToJoin));
        }
        main.append("\nWHERE entity.EntityId = ");
        main.append(entityId);
        if (blockStrings.size() > 0) {
            main.append(" AND ");
        }
        for (int chunk = 0; chunk < blockStrings.size(); chunk++) {
            StringBuilder inner = new StringBuilder();
            main.append(blockSwitches.get(chunk));
            inner.append(String.join(" AND\n", blockStrings.get(chunk)));
            main.append("(");
            main.append(inner);
            main.append(")");
        }
        return main.toString();
    }

    private String createLeftJoins(HashSet<JdsFieldType> tablesToJoin) {
        List<String> tables = new ArrayList<>();
        for (JdsFieldType ft : tablesToJoin) {
            tables.add(String.format("%s %s on %s.EntityGuid = eo.EntityGuid", getTable(ft), getTablePrefix(ft), getTablePrefix(ft)));
        }
        return String.join(" JOIN\n", tables);
    }

    public String toString() {
        return toQuery();
    }

    //========================================================CONDITIONS START HERE
    public JdsFilter isNotNull(JdsField jdsField) {
        tablesToJoin.add(jdsField.getType());
        String builder = String.format("(%s.FieldId = %s AND %s.Value IS NOT NULL)", getTablePrefix(jdsField.getType()), jdsField.getId(), getTablePrefix(jdsField.getType()));
        currentStrings.add(builder);
        currentValues.add("");
        return this;
    }

    public JdsFilter isNull(JdsField jdsField) {
        tablesToJoin.add(jdsField.getType());
        String builder = String.format("(%s.FieldId = %s AND %s.Value IS NULL)", getTablePrefix(jdsField.getType()), jdsField.getId(), getTablePrefix(jdsField.getType()));
        currentStrings.add(builder);
        currentValues.add("");
        return this;
    }

    public JdsFilter between(JdsField jdsField, Object value1, Object value2) {
        tablesToJoin.add(jdsField.getType());
        String builder = String.format("(%s.FieldId = %s AND (%s.Value BETWEEN ? AND ?) )", getTablePrefix(jdsField.getType()), jdsField.getId(), getTablePrefix(jdsField.getType()));
        currentStrings.add(builder);
        currentValues.add(value1);
        currentValues.add(value2);
        return this;
    }

    public JdsFilter notLessThan(JdsField jdsField, Object value) {
        tablesToJoin.add(jdsField.getType());
        String builder = String.format("(%s.FieldId = %s AND %s.Value !< ?)", getTablePrefix(jdsField.getType()), jdsField.getId(), getTablePrefix(jdsField.getType()));
        currentStrings.add(builder);
        currentValues.add(value);
        return this;
    }

    public JdsFilter lessThan(JdsField jdsField, Object value) {
        tablesToJoin.add(jdsField.getType());
        String builder = String.format("(%s.FieldId = %s AND %s.Value < ?)", getTablePrefix(jdsField.getType()), jdsField.getId(), getTablePrefix(jdsField.getType()));
        currentStrings.add(builder);
        currentValues.add(value);
        return this;
    }

    public JdsFilter lessThanOrEqualTo(JdsField jdsField, Object value) {
        tablesToJoin.add(jdsField.getType());
        String builder = String.format("(%s.FieldId = %s AND %s.Value < ?)", getTablePrefix(jdsField.getType()), jdsField.getId(), getTablePrefix(jdsField.getType()));
        currentStrings.add(builder);
        currentValues.add(value);
        return this;
    }

    public JdsFilter notGreaterThan(JdsField jdsField, Object value) {
        tablesToJoin.add(jdsField.getType());
        String builder = String.format("(%s.FieldId = %s AND %s.Value !> ?)", getTablePrefix(jdsField.getType()), jdsField.getId(), getTablePrefix(jdsField.getType()));
        currentStrings.add(builder);
        currentValues.add(value);
        return this;
    }

    public JdsFilter greaterThan(JdsField jdsField, Object value) {
        tablesToJoin.add(jdsField.getType());
        String builder = String.format("(%s.FieldId = %s AND %s.Value > ?)", getTablePrefix(jdsField.getType()), jdsField.getId(), getTablePrefix(jdsField.getType()));
        currentStrings.add(builder);
        currentValues.add(value);
        return this;
    }

    public JdsFilter greaterThanOrEqualTo(JdsField jdsField, Object value) {
        tablesToJoin.add(jdsField.getType());
        String builder = String.format("(%s.FieldId = %s AND %s.Value >= ?)", getTablePrefix(jdsField.getType()), jdsField.getId(), getTablePrefix(jdsField.getType()));
        currentStrings.add(builder);
        currentValues.add(value);
        return this;
    }

    public JdsFilter equals(JdsField jdsField, Object value) {
        tablesToJoin.add(jdsField.getType());
        String builder = String.format("(%s.FieldId = %s AND %s.Value = ?)", getTablePrefix(jdsField.getType()), jdsField.getId(), getTablePrefix(jdsField.getType()));
        currentStrings.add(builder);
        currentValues.add(value);
        return this;
    }

    public JdsFilter notEquals(JdsField jdsField, Object value) {
        tablesToJoin.add(jdsField.getType());
        String builder = String.format("(%s.FieldId = %s AND %s.Value <> ?)", getTablePrefix(jdsField.getType()), jdsField.getId(), getTablePrefix(jdsField.getType()));
        currentStrings.add(builder);
        currentValues.add(value);
        return this;
    }

    public JdsFilter like(JdsField jdsField, Object value) {
        tablesToJoin.add(jdsField.getType());
        String builder = String.format("(%s.FieldId = %s AND %s.Value LIKE ?)", getTablePrefix(jdsField.getType()), jdsField.getId(), getTablePrefix(jdsField.getType()));
        currentStrings.add(builder);
        currentValues.add(value);
        return this;
    }

    public JdsFilter startsLike(JdsField jdsField, Object value) {
        tablesToJoin.add(jdsField.getType());
        String builder = String.format("(%s.FieldId = %s AND %s.Value LIKE ?%)", getTablePrefix(jdsField.getType()), jdsField.getId(), getTablePrefix(jdsField.getType()));
        currentStrings.add(builder);
        currentValues.add(value);
        return this;
    }

    public JdsFilter endsLike(JdsField jdsField, Object value) {
        tablesToJoin.add(jdsField.getType());
        String builder = String.format("(%s.FieldId = %s AND %s.Value LIKE %?)", getTablePrefix(jdsField.getType()), jdsField.getId(), getTablePrefix(jdsField.getType()));
        currentStrings.add(builder);
        currentValues.add(value);
        return this;
    }

    public JdsFilter notLike(JdsField jdsField, Object value) {
        tablesToJoin.add(jdsField.getType());
        String builder = String.format("(%s.FieldId = %s AND %s.Value NOT LIKE %)", getTablePrefix(jdsField.getType()), jdsField.getId(), getTablePrefix(jdsField.getType()));
        currentStrings.add(builder);
        currentValues.add(value);
        return this;
    }

    public JdsFilter in(JdsField jdsField, Object value) {
        tablesToJoin.add(jdsField.getType());
        String builder = String.format("(%s.FieldId = %s AND %s.Value IN (?))", getTablePrefix(jdsField.getType()), jdsField.getId(), getTablePrefix(jdsField.getType()));
        currentStrings.add(builder);
        currentValues.add(value);
        currentValues.add(value);
        return this;
    }

    public JdsFilter notIn(JdsField jdsField, Object value) {
        tablesToJoin.add(jdsField.getType());
        String builder = String.format("(%s.FieldId = %s AND %s.Value NOT IN (?))", getTablePrefix(jdsField.getType()), jdsField.getId(), getTablePrefix(jdsField.getType()));
        currentStrings.add(builder);
        currentValues.add(value);
        currentValues.add(value);
        return this;
    }
}

