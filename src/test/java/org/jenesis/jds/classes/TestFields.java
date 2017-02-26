package org.jenesis.jds.classes;

import org.jenesis.jds.JdsField;
import org.jenesis.jds.enums.JdsFieldType;

/**
 * Created by ifung on 18/02/2017.
 */
public class TestFields {
    public static final JdsField STREET_NAME = new JdsField(1, "street_name", JdsFieldType.TEXT);
    public static final JdsField PLOT_NUMBER = new JdsField(2, "plot_number", JdsFieldType.INT);
    public static final JdsField AREA_NAME = new JdsField(3, "area_name", JdsFieldType.TEXT);
    public static final JdsField PROVINCE_NAME = new JdsField(4, "province_name", JdsFieldType.TEXT);
    public static final JdsField CITY_NAME = new JdsField(5, "city_name", JdsFieldType.TEXT);
    public static final JdsField SEX_ENUM = new JdsField(6, "sex_enum", JdsFieldType.ENUM_TEXT);
    public static final JdsField COUNTRY_NAME = new JdsField(7, "country_name", JdsFieldType.TEXT);
}
