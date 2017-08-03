import common.BaseTestConfig;
import entities.SimpleAddress;
import fields.SimpleAddressFields;
import io.github.subiyacryolite.jds.JdsFilter;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by ifunga on 05/03/2017.
 */
public class FilterTests extends BaseTestConfig {

    @Test
    public void querySqlite() throws Exception {
        initialiseSqlLiteBackend();
        basicQuery();
    }

    @Test
    public void queryMysql() throws Exception {
        initialiseMysqlBackend();
        basicQuery();
    }

    @Test
    public void queryPostgres() throws Exception {
        initialisePostgeSqlBackend();
        basicQuery();
    }

    @Test
    public void queryTsql() throws Exception {
        initialiseTSqlBackend();
        basicQuery();
    }

    @Test
    public void queryOracle() throws Exception {
        initialiseOracleBackend();
        basicQuery();
    }

    @Test
    public void queryAll() throws Exception {
        queryMysql();
        queryOracle();
        queryPostgres();
        querySqlite();
        queryTsql();
    }

    private void basicQuery() throws Exception {
        System.out.printf("=========== %s ===========\n", jdsDb.getImplementation());
        JdsFilter filter = new JdsFilter(jdsDb, SimpleAddress.class).between(SimpleAddressFields.PLOT_NUMBER, 1, 2).like(SimpleAddressFields.COUNTRY_NAME, "Zam").or().equals(SimpleAddressFields.PROVINCE_NAME, "Copperbelt");
        List<SimpleAddress> output = filter.call();
        Assert.assertNotNull(output);
        System.out.println(output);
    }
}
