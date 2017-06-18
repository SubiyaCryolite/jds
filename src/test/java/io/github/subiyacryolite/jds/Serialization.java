package io.github.subiyacryolite.jds;

import io.github.subiyacryolite.jds.common.BaseTestConfig;
import javafx.beans.property.SimpleBlobProperty;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by indana on 5/10/2017.
 */
public class Serialization extends BaseTestConfig {

    @Test
    public void testSerialization() {
        List<JdsEntity> entities = new ArrayList<>();
        entities.add(getSimpleAddressBook());
        for (JdsEntity jdsEntity : entities) {
            String canonicalName = jdsEntity.getClass().getCanonicalName();
            serialize(jdsEntity, canonicalName);
            deserialize(canonicalName, jdsEntity.getClass());
        }
    }

    @Test
    public void testBlobSerialization() {
        SimpleBlobProperty in = new SimpleBlobProperty(new byte[]{1, 0, 1, 1, 1, 1, 0});
        String canonicalName = in.getClass().getCanonicalName();
        serialize(in, canonicalName);
        SimpleBlobProperty out = deserialize(canonicalName, in.getClass());
        System.out.printf("pre %s\n",Arrays.toString(in.get()));
        System.out.printf("post %s\n",Arrays.toString(out.get()));
    }
}
