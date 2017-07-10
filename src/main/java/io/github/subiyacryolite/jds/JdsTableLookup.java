package io.github.subiyacryolite.jds;

import io.github.subiyacryolite.jds.enums.JdsComponent;
import io.github.subiyacryolite.jds.enums.JdsFieldType;

import static io.github.subiyacryolite.jds.enums.JdsComponent.*;

/**
 * Class used to look up the datastore of Jds Field Types
 * Created by ifunga on 24/06/2017.
 */
class JdsTableLookup {
    public static String tableFor(JdsFieldType fieldType) {
        switch (fieldType) {
            case FLOAT:
                return JdsComponent.StoreFloat.getName();
            case INT:
            case BOOLEAN:
                return JdsComponent.StoreInteger.getName();
            case DOUBLE:
                return JdsComponent.StoreDouble.getName();
            case LONG:
                return JdsComponent.StoreLong.getName();
            case TEXT:
                return JdsComponent.StoreText.getName();
            case DATE_TIME:
                return JdsComponent.StoreDateTime.getName();
            case ARRAY_FLOAT:
                return JdsComponent.StoreFloatArray.getName();
            case ARRAY_INT:
                return JdsComponent.StoreIntegerArray.getName();
            case ARRAY_DOUBLE:
                return JdsComponent.StoreDoubleArray.getName();
            case ARRAY_LONG:
                return JdsComponent.StoreLongArray.getName();
            case ARRAY_TEXT:
                return JdsComponent.StoreTextArray.getName();
            case ARRAY_DATE_TIME:
                return JdsComponent.StoreDateTimeArray.getName();
            case ENUM_TEXT:
                return JdsComponent.StoreTextArray.getName();
            case ZONED_DATE_TIME:
                return JdsComponent.StoreZonedDateTime.getName();
            case DATE:
                return JdsComponent.StoreDateTime.getName();
            case TIME:
                return JdsComponent.StoreTime.getName();
            case BLOB:
                return JdsComponent.StoreBlob.getName();
            default:
                return "INVALID";
        }
    }


    public static String getTableName(JdsFieldType jdsFieldType) {
        switch (jdsFieldType) {
            case TEXT:
                return StoreText.getName();
            case BLOB:
                return StoreBlob.getName();
            case INT:
            case BOOLEAN:
                return StoreInteger.getName();
            case DOUBLE:
                return StoreDouble.getName();
            case FLOAT:
                return StoreFloat.getName();
            case LONG:
                return StoreLong.getName();
            case DATE:
            case DATE_TIME:
                return StoreDateTime.getName();
            case ZONED_DATE_TIME:
                return StoreZonedDateTime.getName();
            case TIME:
                return StoreTime.getName();
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
