import io.github.subiyacryolite.jds.JdsFilter;
import io.github.subiyacryolite.jds.common.BaseTestConfig;
import io.github.subiyacryolite.jds.entities.SimpleAddress;
import io.github.subiyacryolite.jds.fields.SimpleAddressFields;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by ifunga on 05/03/2017.
 */
public class FilterTests extends BaseTestConfig {

    public FilterTests() {
        initialiseSqlLiteBackend();
    }

    @Test
    public void basicQuery() throws Exception {
        JdsFilter filter = new JdsFilter(jdsDb, SimpleAddress.class).between(SimpleAddressFields.PLOT_NUMBER, 1, 2).like(SimpleAddressFields.COUNTRY_NAME, "Zam").or().equals(SimpleAddressFields.PROVINCE_NAME, "Copperbelt");
        List<SimpleAddress> output = filter.call();
        Assert.assertNotNull(output);
        System.out.println(output);
    }
}
