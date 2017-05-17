package io.github.subiyacryolite.jds;

import io.github.subiyacryolite.jds.common.BaseTestConfig;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by indana on 5/10/2017.
 */
public class Serialization extends BaseTestConfig {

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
