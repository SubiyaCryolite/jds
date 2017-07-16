package io.github.subiyacryolite.jds.enums;

import io.github.subiyacryolite.jds.JdsFieldEnum;
import io.github.subiyacryolite.jds.fields.SimpleAddressFields;

/**
 * Created by ifunga on 18/02/2017.
 */
public class SimpleAddressEnums {

    public final static JdsFieldEnum PRIMARY_ADDRESS_ENUM = new JdsFieldEnum(PrimaryAddress.class, SimpleAddressFields.PRIMARY_ADDRESS_ENUM, PrimaryAddress.values());
    public final static JdsFieldEnum SEX_ENUM = new JdsFieldEnum(Sex.class, SimpleAddressFields.SEX_ENUM, Sex.values());
}