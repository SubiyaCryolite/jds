package io.github.subiyacryolite.jds.entities;

import io.github.subiyacryolite.jds.JdsField;
import io.github.subiyacryolite.jds.enums.JdsFieldType;

/**
 * Created by ifunga on 12/04/2017.
 */
public class JdsExampleFields {
    public static final JdsField STRING_FIELD = new JdsField(1000, "STRING_FIELD", JdsFieldType.TEXT);
    public static final JdsField TIME_FIELD = new JdsField(1009, "TIME_FIELD", JdsFieldType.TIME);
    public static final JdsField DATE_FIELD = new JdsField(1001, "DATE_FIELD", JdsFieldType.DATE);
    public static final JdsField DATE_TIME_FIELD = new JdsField(1002, "DATE_TIME_FIELD", JdsFieldType.DATE_TIME);
    public static final JdsField ZONED_DATE_TIME_FIELD = new JdsField(1003, "ZONED_DATE_TIME_FIELD", JdsFieldType.ZONED_DATE_TIME);
    public static final JdsField LONG_FIELD = new JdsField(1004, "LONG_FIELD", JdsFieldType.LONG);
    public static final JdsField INT_FIELD = new JdsField(1005, "INT_FIELD", JdsFieldType.INT);
    public static final JdsField DOUBLE_FIELD = new JdsField(1006, "DOUBLE_FIELD", JdsFieldType.DOUBLE);
    public static final JdsField FLOAT_FIELD = new JdsField(1007, "FLOAT_FIELD", JdsFieldType.FLOAT);
    public static final JdsField BOOLEAN_FIELD = new JdsField(1008, "BOOLEAN_FIELD", JdsFieldType.BOOLEAN);
}
