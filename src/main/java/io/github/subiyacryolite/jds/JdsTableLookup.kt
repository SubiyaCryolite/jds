package io.github.subiyacryolite.jds

import io.github.subiyacryolite.jds.enums.JdsComponent.*
import io.github.subiyacryolite.jds.enums.JdsFieldType

/**
 * Class used to look up the datastore of Jds Field Types
 * Used in dynamic view creation, filtering and other operations
 * Created by ifunga on 24/06/2017.
 */
object JdsTableLookup {

    /**
     * Retrieve the table that stores the requested fieldEntity type
     *
     * @param fieldType the requested fieldEntity type
     * @return the table that stores the requested fieldEntity type
     */
    fun getTable(fieldType: JdsFieldType): String {
        when (fieldType) {
            JdsFieldType.FLOAT -> return STORE_FLOAT.component
            JdsFieldType.DOUBLE -> return STORE_DOUBLE.component
            JdsFieldType.ARRAY_FLOAT -> return STORE_FLOAT_ARRAY.component
            JdsFieldType.ARRAY_INT -> return STORE_INTEGER_ARRAY.component
            JdsFieldType.ARRAY_DOUBLE -> return STORE_DOUBLE_ARRAY.component
            JdsFieldType.ARRAY_LONG -> return STORE_LONG_ARRAY.component
            JdsFieldType.ARRAY_TEXT -> return STORE_TEXT_ARRAY.component
            JdsFieldType.ARRAY_DATE_TIME -> return STORE_DATE_TIME_ARRAY.component
            JdsFieldType.ENUM_COLLECTION -> return STORE_INTEGER_ARRAY.component
            JdsFieldType.ZONED_DATE_TIME -> return STORE_ZONED_DATE_TIME.component
            JdsFieldType.TIME -> return STORE_TIME.component
            JdsFieldType.BLOB -> return STORE_BLOB.component
            JdsFieldType.ENUM, JdsFieldType.INT, JdsFieldType.BOOLEAN -> return STORE_INTEGER.component
            JdsFieldType.DATE, JdsFieldType.DATE_TIME -> return STORE_DATE_TIME.component
            JdsFieldType.LONG, JdsFieldType.DURATION -> return STORE_LONG.component
            JdsFieldType.PERIOD, JdsFieldType.TEXT, JdsFieldType.YEAR_MONTH, JdsFieldType.MONTH_DAY -> return STORE_TEXT.component
            else -> return "INVALID"
        }
    }

    /**
     * Get the short version of the table that holds the requested fieldEntity type
     *
     * @param jdsFieldType the requested fieldEntity type
     * @return the short version of the table that holds the requested fieldEntity type
     */
    fun getTablePrefix(jdsFieldType: JdsFieldType): String {
        when (jdsFieldType) {
            JdsFieldType.FLOAT -> return STORE_FLOAT.prefix
            JdsFieldType.DOUBLE -> return STORE_DOUBLE.prefix
            JdsFieldType.ARRAY_FLOAT -> return STORE_FLOAT_ARRAY.prefix
            JdsFieldType.ARRAY_INT -> return STORE_INTEGER_ARRAY.prefix
            JdsFieldType.ARRAY_DOUBLE -> return STORE_DOUBLE_ARRAY.prefix
            JdsFieldType.ARRAY_LONG -> return STORE_LONG_ARRAY.prefix
            JdsFieldType.ARRAY_TEXT -> return STORE_TEXT_ARRAY.prefix
            JdsFieldType.ARRAY_DATE_TIME -> return STORE_DATE_TIME_ARRAY.prefix
            JdsFieldType.ENUM_COLLECTION -> return STORE_INTEGER_ARRAY.prefix
            JdsFieldType.ZONED_DATE_TIME -> return STORE_ZONED_DATE_TIME.prefix
            JdsFieldType.TIME -> return STORE_TIME.prefix
            JdsFieldType.BLOB -> return STORE_BLOB.prefix
            JdsFieldType.ENUM, JdsFieldType.INT, JdsFieldType.BOOLEAN -> return STORE_INTEGER.prefix
            JdsFieldType.DATE, JdsFieldType.DATE_TIME -> return STORE_DATE_TIME.prefix
            JdsFieldType.LONG, JdsFieldType.DURATION -> return STORE_LONG.prefix
            JdsFieldType.PERIOD, JdsFieldType.TEXT, JdsFieldType.YEAR_MONTH, JdsFieldType.MONTH_DAY -> return STORE_TEXT.prefix
        }
        return "INVALID"
    }
}
