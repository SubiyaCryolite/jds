package io.github.subiyacryolite.jds;

import io.github.subiyacryolite.jds.enums.JdsFieldType;

import static io.github.subiyacryolite.jds.enums.JdsComponent.*;

/**
 * Class used to look up the datastore of Jds Field Types
 * Used in dynamic view creation, filtering and other operations
 * Created by ifunga on 24/06/2017.
 */
class JdsTableLookup {

    /**
     * Retrieve the table that stores the requested field type
     *
     * @param fieldType the requested field type
     * @return the table that stores the requested field type
     */
    public static String getTable(JdsFieldType fieldType) {
        switch (fieldType) {
            case FLOAT:
                return STORE_FLOAT.getName();
            case INT:
            case BOOLEAN:
                return STORE_INTEGER.getName();
            case DOUBLE:
                return STORE_DOUBLE.getName();
            case LONG:
                return STORE_LONG.getName();
            case TEXT:
                return STORE_TEXT.getName();
            case DATE_TIME:
                return STORE_DATE_TIME.getName();
            case ARRAY_FLOAT:
                return STORE_FLOAT_ARRAY.getName();
            case ARRAY_INT:
                return STORE_INTEGER_ARRAY.getName();
            case ARRAY_DOUBLE:
                return STORE_DOUBLE_ARRAY.getName();
            case ARRAY_LONG:
                return STORE_LONG_ARRAY.getName();
            case ARRAY_TEXT:
                return STORE_TEXT_ARRAY.getName();
            case ARRAY_DATE_TIME:
                return STORE_DATE_TIME_ARRAY.getName();
            case ENUM_COLLECTION:
                return STORE_INTEGER_ARRAY.getName();
            case ENUM:
                return STORE_INTEGER.getName();
            case ZONED_DATE_TIME:
                return STORE_ZONED_DATE_TIME.getName();
            case DATE:
                return STORE_DATE_TIME.getName();
            case TIME:
                return STORE_TIME.getName();
            case BLOB:
                return STORE_BLOB.getName();
            default:
                return "INVALID";
        }
    }

    /**
     * Get the short version of the table that holds the requested field type
     *
     * @param jdsFieldType the requested field type
     * @return the short version of the table that holds the requested field type
     */
    public static String getTablePrefix(JdsFieldType jdsFieldType) {
        switch (jdsFieldType) {
            case TEXT:
                return STORE_TEXT.getPrefix();
            case BLOB:
                return STORE_BLOB.getPrefix();
            case INT:
            case BOOLEAN:
                return STORE_INTEGER.getPrefix();
            case DOUBLE:
                return STORE_DOUBLE.getPrefix();
            case FLOAT:
                return STORE_FLOAT.getPrefix();
            case LONG:
                return STORE_LONG.getPrefix();
            case DATE:
            case DATE_TIME:
                return STORE_DATE_TIME.getPrefix();
            case ZONED_DATE_TIME:
                return STORE_ZONED_DATE_TIME.getPrefix();
            case TIME:
                return STORE_TIME.getPrefix();
            case ARRAY_TEXT:
                return STORE_TEXT_ARRAY.getPrefix();
            case ARRAY_INT:
            case ENUM_COLLECTION:
                return STORE_INTEGER_ARRAY.getPrefix();
            case ARRAY_DOUBLE:
                return STORE_DOUBLE_ARRAY.getPrefix();
            case ARRAY_FLOAT:
                return STORE_FLOAT_ARRAY.getPrefix();
            case ARRAY_LONG:
                return STORE_LONG_ARRAY.getPrefix();
            case ARRAY_DATE_TIME:
                return STORE_DATE_TIME_ARRAY.getPrefix();
        }
        return "INVALID";
    }
}
