package io.github.subiyacryolite.jds.fields;

import io.github.subiyacryolite.jds.JdsField;
import io.github.subiyacryolite.jds.enums.JdsFieldType;

/**
 * Created by ifunga on 18/02/2017.
 */
public class SimpleAddressFields {
    public static final JdsField STREET_NAME = new JdsField(1, "street_name", JdsFieldType.TEXT);
    public static final JdsField PLOT_NUMBER = new JdsField(2, "plot_number", JdsFieldType.INT);
    public static final JdsField AREA_NAME = new JdsField(3, "area_name", JdsFieldType.TEXT);
    public static final JdsField PROVINCE_NAME = new JdsField(4, "province_name", JdsFieldType.TEXT);
    public static final JdsField CITY_NAME = new JdsField(5, "city_name", JdsFieldType.TEXT);
    public static final JdsField SEX_ENUM = new JdsField(6, "sex_enum", JdsFieldType.ENUM);
    public static final JdsField COUNTRY_NAME = new JdsField(7, "country_name", JdsFieldType.TEXT);
    public static final JdsField PRIMARY_ADDRESS_ENUM = new JdsField(8, "primary_address_enum", JdsFieldType.ENUM_COLLECTION);
    public static final JdsField LOCAL_DATE_OF_REGISTRATION = new JdsField(9, "local_date_of_registration", JdsFieldType.DATE_TIME);
    public static final JdsField ZONED_DATE_OF_REGISTRATION = new JdsField(10, "zoned_date_of_registration", JdsFieldType.ZONED_DATE_TIME);
}
