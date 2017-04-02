package io.github.subiyacryolite.jds;

import io.github.subiyacryolite.jds.enums.JdsFieldType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static io.github.subiyacryolite.jds.enums.JdsEnumTable.*;

/**
 * Created by ifunga on 03/03/2017.
 */
public class JdsQuery {


    private final LinkedList<LinkedList<Object>> sessionValues;
    private final LinkedList<LinkedList<String>> sessionStrings;
    private final LinkedList<String> sessionSwitches;
    private final HashSet<JdsFieldType> tablesToJoin;
    private LinkedList<String> currentStrings;
    private LinkedList<Object> currentValues;


    public JdsQuery() {
        currentStrings = new LinkedList<>();
        currentValues = new LinkedList<>();
        //==================================
        sessionStrings = new LinkedList<>();
        sessionValues = new LinkedList<>();
        //==================================
        tablesToJoin = new HashSet<>();
        sessionSwitches = new LinkedList<>();
        //==================================
        sessionStrings.add(currentStrings);
        sessionValues.add(currentValues);
        sessionSwitches.add("");
    }

    public JdsQuery or() {
        currentStrings = new LinkedList<>();
        sessionStrings.add(currentStrings);
        currentValues = new LinkedList<>();
        sessionValues.add(currentValues);
        sessionSwitches.add(" OR ");
        return this;
    }

    public JdsQuery and() {
        currentStrings = new LinkedList<>();
        sessionStrings.add(currentStrings);
        currentValues = new LinkedList<>();
        sessionValues.add(currentValues);
        sessionSwitches.add(" AND ");
        return this;
    }

    public JdsQuery equals(JdsField jdsField, Object value) {
        tablesToJoin.add(jdsField.getType());
        String builder = String.format("(%s.FieldId = %s AND %s.Value = ?)", getTablePrefix(jdsField.getType()), jdsField.getId(),getTablePrefix(jdsField.getType()));
        currentStrings.add(builder);
        currentValues.add(value);
        return this;
    }

    public JdsQuery like(JdsField jdsField, Object value) {
        tablesToJoin.add(jdsField.getType());
        String builder = String.format("(%s.FieldId = %s AND %s.Value LIKE ?)", getTablePrefix(jdsField.getType()), jdsField.getId(),getTablePrefix(jdsField.getType()));
        currentStrings.add(builder);
        currentValues.add(value);
        return this;
    }

    public JdsQuery startsLike(JdsField jdsField, Object value) {
        tablesToJoin.add(jdsField.getType());
        String builder = String.format("(%s.FieldId = %s AND %s.Value LIKE ?%)", getTablePrefix(jdsField.getType()), jdsField.getId(),getTablePrefix(jdsField.getType()));
        currentStrings.add(builder);
        currentValues.add(value);
        return this;
    }

    public JdsQuery endsLike(JdsField jdsField, Object value) {
        tablesToJoin.add(jdsField.getType());
        String builder = String.format("(%s.FieldId = %s AND %s.Value LIKE %?)", getTablePrefix(jdsField.getType()), jdsField.getId(),getTablePrefix(jdsField.getType()));
        currentStrings.add(builder);
        currentValues.add(value);
        return this;
    }

    public JdsQuery notLike(JdsField jdsField, Object value) {
        tablesToJoin.add(jdsField.getType());
        String builder = String.format("(%s.FieldId = %s AND %s.Value NOT LIKE %)", getTablePrefix(jdsField.getType()), jdsField.getId(),getTablePrefix(jdsField.getType()));
        currentStrings.add(builder);
        currentValues.add(value);
        return this;
    }

    public JdsQuery in(JdsField jdsField, Object value) {
        tablesToJoin.add(jdsField.getType());
        String builder = String.format("(%s.FieldId = %s AND %s.Value IN (?))", getTablePrefix(jdsField.getType()), jdsField.getId(),getTablePrefix(jdsField.getType()));
        currentStrings.add(builder);
        currentValues.add(value);
        currentValues.add(value);
        return this;
    }

    public JdsQuery notIn(JdsField jdsField, Object value) {
        tablesToJoin.add(jdsField.getType());
        String builder = String.format("(%s.FieldId = %s AND %s.Value NOT IN (?))", getTablePrefix(jdsField.getType()), jdsField.getId(),getTablePrefix(jdsField.getType()));
        currentStrings.add(builder);
        currentValues.add(value);
        currentValues.add(value);
        return this;
    }

    public String toQuery() {
        StringBuilder main = new StringBuilder();
        main.append("SELECT * FROM JdsStoreEntityOverview eo");
        if (tablesToJoin.size() > 0) {
            main.append(" JOIN ");
            main.append(createLeftJoins(tablesToJoin));
        }
        if (sessionStrings.size() > 0) {
            main.append(" WHERE ");
        }
        for (int chunk = 0; chunk < sessionStrings.size(); chunk++) {
            StringBuilder inner = new StringBuilder();
            main.append(sessionSwitches.get(chunk));
            inner.append(String.join(" AND ", sessionStrings.get(chunk)));
            main.append("(");
            main.append(inner);
            main.append(")");
        }
        return main.toString();
    }

    private String createLeftJoins(HashSet<JdsFieldType> tablesToJoin) {
        List<String> tables = new ArrayList<>();
        for (JdsFieldType ft : tablesToJoin) {
            tables.add(String.format("%s %s on %s.EntityGuid = eo.EntityGuid", getTableName(ft), getTablePrefix(ft), getTablePrefix(ft)));
        }
        return String.join(" JOIN ", tables);
    }

    private String getTableName(JdsFieldType jdsFieldType) {
        switch (jdsFieldType) {
            case TEXT:
                return StoreText.getName();
            case INT:
                return StoreInteger.getName();
            case DOUBLE:
                return StoreDouble.getName();
            case FLOAT:
                return StoreFloat.getName();
            case LONG:
                return StoreLong.getName();
            case DATE_TIME:
                return StoreDateTime.getName();
            case ARRAY_TEXT:
                return StoreTextArray.getName();
            case ARRAY_INT:
            case ENUM_TEXT:
                return StoreIntegerArray.getName();
            case ARRAY_DOUBLE:
                return StoreDoubleArray.getName();
            case ARRAY_FLOAT:
                return StoreFloatArray.getName();
            case ARRAY_LONG:
                return StoreLongArray.getName();
            case ARRAY_DATE_TIME:
                return StoreDateTimeArray.getName();
        }
        return "undefined";
    }

    private String getTablePrefix(JdsFieldType jdsFieldType) {
        switch (jdsFieldType) {
            case TEXT:
                return StoreText.getPrefix();
            case INT:
                return StoreInteger.getPrefix();
            case DOUBLE:
                return StoreDouble.getPrefix();
            case FLOAT:
                return StoreFloat.getPrefix();
            case LONG:
                return StoreLong.getPrefix();
            case DATE_TIME:
                return StoreDateTime.getPrefix();
            case ARRAY_TEXT:
                return StoreTextArray.getPrefix();
            case ARRAY_INT:
            case ENUM_TEXT:
                return StoreIntegerArray.getPrefix();
            case ARRAY_DOUBLE:
                return StoreDoubleArray.getPrefix();
            case ARRAY_FLOAT:
                return StoreFloatArray.getPrefix();
            case ARRAY_LONG:
                return StoreLongArray.getPrefix();
            case ARRAY_DATE_TIME:
                return StoreDateTimeArray.getPrefix();
        }
        return "undefined";
    }
}

