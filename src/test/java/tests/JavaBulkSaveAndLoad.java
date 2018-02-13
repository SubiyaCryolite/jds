package tests;

import common.BaseTestConfig;
import entities.EntityA;
import io.github.subiyacryolite.jds.JdsLoad;
import io.github.subiyacryolite.jds.JdsSave;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class JavaBulkSaveAndLoad extends BaseTestConfig {

    public JavaBulkSaveAndLoad() {
        super("Java - Bulk Load Save");
    }

    private void save() throws SQLException, ClassNotFoundException, InterruptedException {
        ArrayList<EntityA> memObjects = new ArrayList<EntityA>();
        for (int i = 0; i < 10000; i++) {
            EntityA entry = new EntityA();
            entry.getOverview().setUuid(String.format("guidBulk%s", i));
            memObjects.add(entry);
        }
        JdsSave save = new JdsSave(jdsDb, memObjects);
        Future<Boolean> process = Executors.newSingleThreadExecutor().submit(save);
        if (!process.isDone())
            Thread.sleep(16);
        System.out.println("Successfully saved $memObjects");
    }

    private void load() throws Exception {
        JdsLoad<EntityA> entityAs = new JdsLoad(jdsDb, EntityA.class);
        System.out.println("All A's [${entityAs.call()}]");
    }

    public void saveAndLoad() throws Exception {
        save();
        load();
    }

    @Test
    public void testPostgreSql() throws Exception {
        initialisePostgeSqlBackend();//6s 948ms
        saveAndLoad();
    }

    @Test
    public void testOracle() throws Exception {
        initialiseOracleBackend();//11s 79ms
        saveAndLoad();
    }

    @Test
    public void testTransactionalSql() throws Exception {
        initialiseTSqlBackend();//4s 532ms
        saveAndLoad();
    }

    @Test
    public void testSqLite() throws Exception {
        initialiseSqLiteBackend();//12s 501ms
        saveAndLoad();
    }

    @Test
    public void testMySql() throws Exception {
        initialiseMysqlBackend();
        saveAndLoad();//8s 126ms
    }

    @Test
    public void testMariaDb() throws Exception {
        initialiseMariaDbBackend();
        saveAndLoad();//8s 126ms
    }

    @Test
    public void allImplementations() throws Exception {
        testSqLite();
        testTransactionalSql();
        testPostgreSql();
        testMySql();
        testOracle();
        testMariaDb();
    }
}
