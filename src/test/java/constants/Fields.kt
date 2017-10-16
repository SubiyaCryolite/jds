package constants

import io.github.subiyacryolite.jds.JdsField
import io.github.subiyacryolite.jds.enums.JdsFieldType

/**
 * Created by ifunga on 18/02/2017.
 */
object Fields {
    val STREET_NAME = JdsField(1, "street_name", JdsFieldType.TEXT)
    val PLOT_NUMBER = JdsField(2, "plot_number", JdsFieldType.INT)
    val AREA_NAME = JdsField(3, "area_name", JdsFieldType.TEXT)
    val PROVINCE_NAME = JdsField(4, "province_name", JdsFieldType.TEXT)
    val CITY_NAME = JdsField(5, "city_name", JdsFieldType.TEXT)
    val SEX_ENUM = JdsField(6, "sex_enum", JdsFieldType.ENUM)
    val COUNTRY_NAME = JdsField(7, "country_name", JdsFieldType.TEXT)
    val PRIMARY_ADDRESS_ENUM = JdsField(8, "primary_address_enum", JdsFieldType.ENUM)
    val LOCAL_DATE_OF_REGISTRATION = JdsField(9, "local_date_of_registration", JdsFieldType.DATE_TIME)
    val ZONED_DATE_OF_REGISTRATION = JdsField(10, "zoned_date_of_registration", JdsFieldType.ZONED_DATE_TIME)
    val STRING_FIELD = JdsField(11, "STRING_FIELD", JdsFieldType.TEXT)
    val TIME_FIELD = JdsField(12, "TIME_FIELD", JdsFieldType.TIME)
    val DATE_FIELD = JdsField(13, "DATE_FIELD", JdsFieldType.DATE)
    val DATE_TIME_FIELD = JdsField(14, "DATE_TIME_FIELD", JdsFieldType.DATE_TIME)
    val ZONED_DATE_TIME_FIELD = JdsField(15, "ZONED_DATE_TIME_FIELD", JdsFieldType.ZONED_DATE_TIME)
    val LONG_FIELD = JdsField(16, "LONG_FIELD", JdsFieldType.LONG)
    val INT_FIELD = JdsField(17, "INT_FIELD", JdsFieldType.INT)
    val DOUBLE_FIELD = JdsField(18, "DOUBLE_FIELD", JdsFieldType.DOUBLE)
    val FLOAT_FIELD = JdsField(19, "FLOAT_FIELD", JdsFieldType.FLOAT)
    val BOOLEAN_FIELD = JdsField(20, "BOOLEAN_FIELD", JdsFieldType.BOOLEAN)
    val BLOB_FIELD = JdsField(21, "BLOB_FIELD", JdsFieldType.BLOB)
    val ADDRESS_FIELD = JdsField(22, "ADDRESS_FIELD", JdsFieldType.CLASS)
    val PERIOD = JdsField(23, "PERIOD", JdsFieldType.PERIOD)
    val DURATION = JdsField(24, "DURATION", JdsFieldType.DURATION)
    val MONTH_DAY = JdsField(25, "MONTH_DAY", JdsFieldType.MONTH_DAY)
    val YEAR_MONTH = JdsField(26, "YEAR_MONTH", JdsFieldType.YEAR_MONTH)
}
