package org.jenesis.jds;

import org.jenesis.jds.classes.TestFields;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by ifung on 05/03/2017.
 */
public class TestJdsQuery {
    public TestJdsQuery() {
    }

    @Test
    public void TestQuery() {
        JdsQuery query = new JdsQuery()
                .equals(TestFields.AREA_NAME, "Chilenje")
                .like(TestFields.CITY_NAME, "Lus")
                .or()
                .equals(TestFields.AREA_NAME,"Woodlands");
        String output = query.toQuery();
        Assert.assertNotNull(output);
    }
}
