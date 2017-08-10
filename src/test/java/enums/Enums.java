package enums;

import io.github.subiyacryolite.jds.JdsFieldEnum;
import fields.Fields;

/**
 * Created by ifunga on 18/02/2017.
 */
public class Enums {

    public final static JdsFieldEnum PRIMARY_ADDRESS_ENUM = new JdsFieldEnum(PrimaryAddress.class, Fields.PRIMARY_ADDRESS_ENUM, PrimaryAddress.values());
    public final static JdsFieldEnum SEX_ENUM = new JdsFieldEnum(Sex.class, Fields.SEX_ENUM, Sex.values());
}