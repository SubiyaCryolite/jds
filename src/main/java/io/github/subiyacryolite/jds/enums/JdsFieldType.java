package io.github.subiyacryolite.jds.enums;

/**
 * Created by ifunga on 09/02/2017.
 */
public enum JdsFieldType {
    FLOAT(0),
    INT(1),
    DOUBLE(2),
    LONG(3),
    TEXT(4),
    DATE_TIME(5),
    ARRAY_FLOAT(6),
    ARRAY_INT(7),
    ARRAY_DOUBLE(8),
    ARRAY_LONG(9),
    ARRAY_TEXT(10),
    ARRAY_DATE_TIME(11),
    ENUM_TEXT(12),
    BOOLEAN(13);

    private final int type;

    JdsFieldType(final int type)
    {
        this.type=type;
    }

    public int getType()
    {
        return type;
    }
}
