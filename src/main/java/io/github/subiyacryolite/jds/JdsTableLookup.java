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
                return StoreFloat.getName();
            case INT:
            case BOOLEAN:
                return StoreInteger.getName();
            case DOUBLE:
                return StoreDouble.getName();
            case LONG:
                return StoreLong.getName();
            case TEXT:
                return StoreText.getName();
            case DATE_TIME:
                return StoreDateTime.getName();
            case ARRAY_FLOAT:
                return StoreFloatArray.getName();
            case ARRAY_INT:
                return StoreIntegerArray.getName();
            case ARRAY_DOUBLE:
                return StoreDoubleArray.getName();
            case ARRAY_LONG:
                return StoreLongArray.getName();
            case ARRAY_TEXT:
                return StoreTextArray.getName();
            case ARRAY_DATE_TIME:
                return StoreDateTimeArray.getName();
            case ENUM_COLLECTION:
                return StoreIntegerArray.getName();
            case ENUM:
                return StoreInteger.getName();
            case ZONED_DATE_TIME:
                return StoreZonedDateTime.getName();
            case DATE:
                return StoreDateTime.getName();
            case TIME:
                return StoreTime.getName();
            case BLOB:
                return StoreBlob.getName();
            default:
                return "INVALID";
        }
    }

    /**
     * Get the short version of the table that holds the requested field type
     * @param jdsFieldType the requested field type
     * @return the short version of the table that holds the requested field type
     */
    public static String getTablePrefix(JdsFieldType jdsFieldType) {
        switch (jdsFieldType) {
            case TEXT:
                return StoreText.getPrefix();
            case BLOB:
                return StoreBlob.getPrefix();
            case INT:
            case BOOLEAN:
                return StoreInteger.getPrefix();
            case DOUBLE:
                return StoreDouble.getPrefix();
            case FLOAT:
                return StoreFloat.getPrefix();
            case LONG:
                return StoreLong.getPrefix();
            case DATE:
            case DATE_TIME:
                return StoreDateTime.getPrefix();
            case ZONED_DATE_TIME:
                return StoreZonedDateTime.getPrefix();
            case TIME:
                return StoreTime.getPrefix();
            case ARRAY_TEXT:
                return StoreTextArray.getPrefix();
            case ARRAY_INT:
            case ENUM_COLLECTION:
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
        return "INVALID";
    }
}
