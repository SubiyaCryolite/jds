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
     * Retrieve the table that stores the requested field type
     *
     * @param fieldType the requested field type
     * @return the table that stores the requested field type
     */
    fun getTable(fieldType: JdsFieldType): String {
        when (fieldType) {
            JdsFieldType.FLOAT -> return STORE_FLOAT.componentName
            JdsFieldType.INT, JdsFieldType.BOOLEAN -> return STORE_INTEGER.componentName
            JdsFieldType.DOUBLE -> return STORE_DOUBLE.componentName
            JdsFieldType.LONG -> return STORE_LONG.componentName
            JdsFieldType.TEXT -> return STORE_TEXT.componentName
            JdsFieldType.DATE_TIME -> return STORE_DATE_TIME.componentName
            JdsFieldType.ARRAY_FLOAT -> return STORE_FLOAT_ARRAY.componentName
            JdsFieldType.ARRAY_INT -> return STORE_INTEGER_ARRAY.componentName
            JdsFieldType.ARRAY_DOUBLE -> return STORE_DOUBLE_ARRAY.componentName
            JdsFieldType.ARRAY_LONG -> return STORE_LONG_ARRAY.componentName
            JdsFieldType.ARRAY_TEXT -> return STORE_TEXT_ARRAY.componentName
            JdsFieldType.ARRAY_DATE_TIME -> return STORE_DATE_TIME_ARRAY.componentName
            JdsFieldType.ENUM_COLLECTION -> return STORE_INTEGER_ARRAY.componentName
            JdsFieldType.ENUM -> return STORE_INTEGER.componentName
            JdsFieldType.ZONED_DATE_TIME -> return STORE_ZONED_DATE_TIME.componentName
            JdsFieldType.DATE -> return STORE_DATE_TIME.componentName
            JdsFieldType.TIME -> return STORE_TIME.componentName
            JdsFieldType.BLOB -> return STORE_BLOB.componentName
            else -> return "INVALID"
        }
    }

    /**
     * Get the short version of the table that holds the requested field type
     *
     * @param jdsFieldType the requested field type
     * @return the short version of the table that holds the requested field type
     */
    fun getTablePrefix(jdsFieldType: JdsFieldType): String {
        when (jdsFieldType) {
            JdsFieldType.TEXT -> return STORE_TEXT.prefix
            JdsFieldType.BLOB -> return STORE_BLOB.prefix
            JdsFieldType.INT, JdsFieldType.BOOLEAN -> return STORE_INTEGER.prefix
            JdsFieldType.DOUBLE -> return STORE_DOUBLE.prefix
            JdsFieldType.FLOAT -> return STORE_FLOAT.prefix
            JdsFieldType.LONG -> return STORE_LONG.prefix
            JdsFieldType.DATE, JdsFieldType.DATE_TIME -> return STORE_DATE_TIME.prefix
            JdsFieldType.ZONED_DATE_TIME -> return STORE_ZONED_DATE_TIME.prefix
            JdsFieldType.TIME -> return STORE_TIME.prefix
            JdsFieldType.ARRAY_TEXT -> return STORE_TEXT_ARRAY.prefix
            JdsFieldType.ARRAY_INT, JdsFieldType.ENUM_COLLECTION -> return STORE_INTEGER_ARRAY.prefix
            JdsFieldType.ARRAY_DOUBLE -> return STORE_DOUBLE_ARRAY.prefix
            JdsFieldType.ARRAY_FLOAT -> return STORE_FLOAT_ARRAY.prefix
            JdsFieldType.ARRAY_LONG -> return STORE_LONG_ARRAY.prefix
            JdsFieldType.ARRAY_DATE_TIME -> return STORE_DATE_TIME_ARRAY.prefix
        }
        return "INVALID"
    }
}
