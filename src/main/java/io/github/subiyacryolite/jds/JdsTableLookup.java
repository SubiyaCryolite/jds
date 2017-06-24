package io.github.subiyacryolite.jds;

import io.github.subiyacryolite.jds.enums.JdsComponent;
import io.github.subiyacryolite.jds.enums.JdsFieldType;

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
            case BOOLEAN:
                return JdsComponent.StoreInteger.getName();
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
}
