package fields;

import io.github.subiyacryolite.jds.JdsField;
import io.github.subiyacryolite.jds.enums.JdsFieldType;

/**
 * Created by ifunga on 18/02/2017.
 */
public class Fields {
    public static final JdsField STREET_NAME = new JdsField(1, "street_name", JdsFieldType.TEXT);
    public static final JdsField PLOT_NUMBER = new JdsField(2, "plot_number", JdsFieldType.INT);
    public static final JdsField AREA_NAME = new JdsField(3, "area_name", JdsFieldType.TEXT);
    public static final JdsField PROVINCE_NAME = new JdsField(4, "province_name", JdsFieldType.TEXT);
    public static final JdsField CITY_NAME = new JdsField(5, "city_name", JdsFieldType.TEXT);
    public static final JdsField SEX_ENUM = new JdsField(6, "sex_enum", JdsFieldType.ENUM);
    public static final JdsField COUNTRY_NAME = new JdsField(7, "country_name", JdsFieldType.TEXT);
    public static final JdsField PRIMARY_ADDRESS_ENUM = new JdsField(8, "primary_address_enum", JdsFieldType.ENUM);
    public static final JdsField LOCAL_DATE_OF_REGISTRATION = new JdsField(9, "local_date_of_registration", JdsFieldType.DATE_TIME);
    public static final JdsField ZONED_DATE_OF_REGISTRATION = new JdsField(10, "zoned_date_of_registration", JdsFieldType.ZONED_DATE_TIME);
    public static final JdsField STRING_FIELD = new JdsField(1000, "STRING_FIELD", JdsFieldType.TEXT);
    public static final JdsField TIME_FIELD = new JdsField(1009, "TIME_FIELD", JdsFieldType.TIME);
    public static final JdsField DATE_FIELD = new JdsField(1001, "DATE_FIELD", JdsFieldType.DATE);
    public static final JdsField DATE_TIME_FIELD = new JdsField(1002, "DATE_TIME_FIELD", JdsFieldType.DATE_TIME);
    public static final JdsField ZONED_DATE_TIME_FIELD = new JdsField(1003, "ZONED_DATE_TIME_FIELD", JdsFieldType.ZONED_DATE_TIME);
    public static final JdsField LONG_FIELD = new JdsField(1004, "LONG_FIELD", JdsFieldType.LONG);
    public static final JdsField INT_FIELD = new JdsField(1005, "INT_FIELD", JdsFieldType.INT);
    public static final JdsField DOUBLE_FIELD = new JdsField(1006, "DOUBLE_FIELD", JdsFieldType.DOUBLE);
    public static final JdsField FLOAT_FIELD = new JdsField(1007, "FLOAT_FIELD", JdsFieldType.FLOAT);
    public static final JdsField BOOLEAN_FIELD = new JdsField(1008, "BOOLEAN_FIELD", JdsFieldType.BOOLEAN);
    public static final JdsField BLOB_FIELD = new JdsField(1010, "BLOB_FIELD", JdsFieldType.BLOB);
}
