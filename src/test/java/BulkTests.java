import common.BaseTestConfig;
import entities.EntityA;
import io.github.subiyacryolite.jds.JdsLoad;
import io.github.subiyacryolite.jds.JdsSave;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class BulkTests extends BaseTestConfig {

    public void save() throws Exception {
        List<EntityA> memObjects = new ArrayList<>();
        for (int i = 0; i < 10_000; i++) {
            EntityA entry = new EntityA();
            entry.getOverview().setEntityGuid(String.format("guidBulk%s", i));
            memObjects.add(entry);
        }
        JdsSave jdsSave = new JdsSave(jdsDb, 1024, memObjects);
        jdsSave.call();
        System.out.printf("Saved %s\n", memObjects);
    }

    public void load() throws Exception {
        List<EntityA> entityAs = JdsLoad.Companion.load(jdsDb, EntityA.class);
        System.out.printf("All A s [%s]\n", entityAs);
    }

    @Test
    public void saveAndLoadPostreSqlImplementation() throws Exception {
        initialisePostgeSqlBackend();//13s 175ms
        saveAndLoad();
    }

    @Test
    public void saveAndLoadOracleSqlImplementation() throws Exception {
        initialiseOracleBackend();//11s 79ms
        saveAndLoad();
    }

    @Test
    public void saveAndLoadTsqlImplementation() throws Exception {
        initialiseTSqlBackend();//8s 326ms
        saveAndLoad();
    }

    @Test
    public void saveAndLoadSqliteImplementation() throws Exception {
        initialiseSqlLiteBackend();//12s 501ms
        saveAndLoad();
    }

    @Test
    public void saveAndLoadMySqlImplementation() throws Exception {
        initialiseMysqlBackend();
        saveAndLoad();//8s 126ms
    }

    @Test
    public void saveAndLoadAllImplementations() throws Exception {
        saveAndLoadSqliteImplementation();
        saveAndLoadTsqlImplementation();
        saveAndLoadPostreSqlImplementation();
        saveAndLoadMySqlImplementation();
        saveAndLoadOracleSqlImplementation();
    }
}
