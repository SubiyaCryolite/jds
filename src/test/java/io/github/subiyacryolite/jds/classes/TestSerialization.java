package io.github.subiyacryolite.jds.classes;

import io.github.subiyacryolite.jds.DataTests;
import io.github.subiyacryolite.jds.JdsEntity;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by indana on 5/10/2017.
 */
public class TestSerialization extends DataTests {

    @Test
    public void testSerialization() {
        List<JdsEntity> entities = new ArrayList<>();
        entities.add(getSimpleAddressBook());
        for (JdsEntity ent : entities) {
            String fName = ent.getClass().getCanonicalName();
            serialize(ent, fName);
            deserialize(fName, ent.getClass());
        }
    }
}
