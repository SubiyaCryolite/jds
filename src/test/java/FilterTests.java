import common.BaseTestConfig;
import entities.Address;
import fields.Fields;
import io.github.subiyacryolite.jds.JdsFilter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        JdsFilter filter = new JdsFilter(jdsDb, Address.class).between(Fields.PLOT_NUMBER, 1, 2).like(Fields.COUNTRY_NAME, "Zam").or().equals(Fields.PROVINCE_NAME, "Copperbelt");
        List<Address> output = filter.call();
        assertNotNull(output);
        System.out.println(output);
    }
}
