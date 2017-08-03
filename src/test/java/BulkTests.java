import common.BaseTestConfig;
import entities.EntityA;
import io.github.subiyacryolite.jds.JdsLoad;
import io.github.subiyacryolite.jds.JdsSave;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class BulkTests extends BaseTestConfig {

    public void save() throws Exception {
        List<EntityA> memObjects = new ArrayList<>();
        for (int i = 0; i < 10_000; i++) {
            EntityA entry = new EntityA();
            entry.setEntityGuid(String.format("guidBulk%s", i));
            memObjects.add(entry);
        }
        JdsSave jdsSave = new JdsSave(jdsDb, 400, memObjects);
        jdsSave.call();
        System.out.printf("Saved %s\n", memObjects);
    }

    public void load() throws Exception {
        List<EntityA> entityAs = JdsLoad.load(jdsDb, EntityA.class);
        System.out.printf("All A s [%s]\n", entityAs);
    }

    @Test
    public void saveAndLoadPostreSqlImplementation() throws Exception {
        initialisePostgeSqlBackend();//??
        saveAndLoad();
    }

    @Test
    public void saveAndLoadOracleSqlImplementation() throws Exception {
        initialiseOracleBackend();//??
        saveAndLoad();
    }

    @Test
    public void saveAndLoadTsqlImplementation() throws Exception {
        initialiseTSqlBackend();//4s64ms
        saveAndLoad();
    }

    @Test
    public void saveAndLoadSqliteImplementation() throws Exception {
        initialiseSqlLiteBackend();//8s958ms
        saveAndLoad();
    }

    @Test
    public void saveAndLoadMySqlImplementation() throws Exception {
        initialiseMysqlBackend();
        saveAndLoad();//40s383ms
    }

    @Test
    public void saveAndLoadAllImplementations() throws Exception {
        saveAndLoadSqliteImplementation();
        saveAndLoadTsqlImplementation();
        saveAndLoadPostreSqlImplementation();
        saveAndLoadMySqlImplementation();
    }
}
