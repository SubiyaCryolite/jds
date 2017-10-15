package constants;

import entities.Address;
import io.github.subiyacryolite.jds.JdsFieldEntity;

public class Entities {
    public static final JdsFieldEntity<Address> ADDRESS_BROOK = new JdsFieldEntity(Address.class, Fields.ADDRESS_BROOK);
}
