package constants

import io.github.subiyacryolite.jds.JdsField
import io.github.subiyacryolite.jds.enums.JdsFieldType

/**
 * Created by ifunga on 18/02/2017.
 */
object Fields {
    val STREET_NAME = JdsField(1, "street_name", JdsFieldType.STRING)
    val PLOT_NUMBER = JdsField(2, "plot_number", JdsFieldType.INT)
    val AREA_NAME = JdsField(3, "area_name", JdsFieldType.STRING)
    val PROVINCE_NAME = JdsField(4, "province_name", JdsFieldType.STRING)
    val CITY_NAME = JdsField(5, "city_name", JdsFieldType.STRING)
    val SEX_ENUM = JdsField(6, "sex_enum", JdsFieldType.ENUM)
    val COUNTRY_NAME = JdsField(7, "country_name", JdsFieldType.STRING)
    val PRIMARY_ADDRESS_ENUM = JdsField(8, "primary_address_enum", JdsFieldType.ENUM)
    val LOCAL_DATE_OF_REGISTRATION = JdsField(9, "local_date_of_registration", JdsFieldType.DATE_TIME)
    val ZONED_DATE_OF_REGISTRATION = JdsField(10, "zoned_date_of_registration", JdsFieldType.ZONED_DATE_TIME)
    val STRING_FIELD = JdsField(11, "string_field", JdsFieldType.STRING)
    val TIME_FIELD = JdsField(12, "time_field", JdsFieldType.TIME)
    val DATE_FIELD = JdsField(13, "date_field", JdsFieldType.DATE)
    val DATE_TIME_FIELD = JdsField(14, "date_time_field", JdsFieldType.DATE_TIME)
    val ZONED_DATE_TIME_FIELD = JdsField(15, "zoned_date_time_field", JdsFieldType.ZONED_DATE_TIME)
    val LONG_FIELD = JdsField(16, "long_field", JdsFieldType.LONG)
    val INT_FIELD = JdsField(17, "int_field", JdsFieldType.INT)
    val DOUBLE_FIELD = JdsField(18, "double_field", JdsFieldType.DOUBLE)
    val FLOAT_FIELD = JdsField(19, "float_field", JdsFieldType.FLOAT)
    val BOOLEAN_FIELD = JdsField(20, "boolean_field", JdsFieldType.BOOLEAN)
    val BLOB_FIELD = JdsField(21, "blob_field", JdsFieldType.BLOB)
    val ADDRESSES = JdsField(22, "addresses", JdsFieldType.ENTITY_COLLECTION)
    val PERIOD = JdsField(23, "period", JdsFieldType.PERIOD)
    val DURATION = JdsField(24, "duration", JdsFieldType.DURATION)
    val MONTH_DAY = JdsField(25, "month_day", JdsFieldType.MONTH_DAY)
    val YEAR_MONTH = JdsField(26, "year_month", JdsFieldType.YEAR_MONTH)
    val RIGHTS= JdsField(27, "rights", JdsFieldType.ENUM_COLLECTION)
}
