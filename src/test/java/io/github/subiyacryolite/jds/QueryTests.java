package io.github.subiyacryolite.jds;

import io.github.subiyacryolite.jds.classes.SimpleAddress;
import io.github.subiyacryolite.jds.classes.TestFields;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by ifung on 05/03/2017.
 */
public class QueryTests extends BaseTest {

    public QueryTests() {
        initialiseSqlLiteBackend();
    }

    @Test
    public void TestQuery() {
        JdsFilter query = new JdsFilter().equals(TestFields.AREA_NAME, "Riverdale").like(TestFields.COUNTRY_NAME, "Zam").or().equals(TestFields.PROVINCE_NAME, "Copperbelt");
        List<SimpleAddress> output = query.find(jdsDatabase, SimpleAddress.class);
        Assert.assertNotNull(output);
    }
}
