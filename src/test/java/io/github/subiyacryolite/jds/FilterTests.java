package io.github.subiyacryolite.jds;

import io.github.subiyacryolite.jds.common.BaseTestConfig;
import io.github.subiyacryolite.jds.entities.SimpleAddress;
import io.github.subiyacryolite.jds.fields.SimpleAddressFields;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Created by ifunga on 05/03/2017.
 */
public class FilterTests extends BaseTestConfig {

    public FilterTests() {
        initialiseSqlLiteBackend();
    }

    @Test
    public void basicQuery() throws ExecutionException, InterruptedException {
        JdsFilter filter = new JdsFilter(jdsDb, SimpleAddress.class).equals(SimpleAddressFields.AREA_NAME, "Riverdale").like(SimpleAddressFields.COUNTRY_NAME, "Zam").or().equals(SimpleAddressFields.PROVINCE_NAME, "Copperbelt");
        List<SimpleAddress> output = new FutureTask<List<SimpleAddress>>(filter).get();
        Assert.assertNotNull(output);
    }
}
