package io.github.subiyacryolite.jds.tests.constants

import io.github.subiyacryolite.jds.JdsField
import io.github.subiyacryolite.jds.enums.JdsFieldType

/**
 * Created by ifunga on 18/02/2017.
 */
object Fields {
    val STREET_NAME = JdsField(1, "street_name", JdsFieldType.String)
    val PLOT_NUMBER = JdsField(2, "plot_number", JdsFieldType.Short)
    val AREA_NAME = JdsField(3, "area_name", JdsFieldType.String)
    val PROVINCE_NAME = JdsField(4, "province_name", JdsFieldType.String)
    val CITY_NAME = JdsField(5, "city_name", JdsFieldType.String)
    val SEX_ENUM = JdsField(6, "sex_enum", JdsFieldType.Enum)
    val COUNTRY_NAME = JdsField(7, "country_name", JdsFieldType.String)
    val PRIMARY_ADDRESS_ENUM = JdsField(8, "primary_address_enum", JdsFieldType.Enum)
    val TIME_OF_ENTRY = JdsField(9, "time_of_entry", JdsFieldType.Time)
    val LOCAL_DATE_OF_REGISTRATION = JdsField(10, "local_date_of_registration", JdsFieldType.DateTime)
    val ZONED_DATE_OF_REGISTRATION = JdsField(11, "zoned_date_of_registration", JdsFieldType.ZonedDateTime)
    val STRING_FIELD = JdsField(12, "string_field", JdsFieldType.String)
    val TIME_FIELD = JdsField(13, "time_field", JdsFieldType.Time)
    val DATE_FIELD = JdsField(14, "date_field", JdsFieldType.Date)
    val DATE_TIME_FIELD = JdsField(15, "date_time_field", JdsFieldType.DateTime)
    val ZONED_DATE_TIME_FIELD = JdsField(16, "zoned_date_time_field", JdsFieldType.ZonedDateTime)
    val LONG_FIELD = JdsField(17, "long_field", JdsFieldType.Long)
    val INT_FIELD = JdsField(18, "int_field", JdsFieldType.Int)
    val DOUBLE_FIELD = JdsField(19, "double_field", JdsFieldType.Double)
    val FLOAT_FIELD = JdsField(20, "float_field", JdsFieldType.Float)
    val BOOLEAN_FIELD = JdsField(21, "boolean_field", JdsFieldType.Boolean)
    val BLOB_FIELD = JdsField(22, "blob_field", JdsFieldType.Blob)
    val ADDRESSES = JdsField(23, "addresses", JdsFieldType.EntityCollection)
    val PERIOD = JdsField(24, "period", JdsFieldType.Period)
    val DURATION = JdsField(25, "duration", JdsFieldType.Duration)
    val MONTH_DAY = JdsField(26, "month_day", JdsFieldType.MonthDay)
    val YEAR_MONTH = JdsField(27, "year_month", JdsFieldType.YearMonth)
    val RIGHTS = JdsField(28, "rights", JdsFieldType.EnumCollection)
    val PRIMARY_ADDRESS_ENUM_STRING = JdsField(29, "primary_address_enum_str", JdsFieldType.EnumString)
    val ENTRY_UUID = JdsField(30, "entry_uuid", JdsFieldType.Uuid)
}
