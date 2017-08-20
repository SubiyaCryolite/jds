import common.BaseTestConfig;
import entities.EntityA;
import entities.EntityB;
import entities.EntityC;
import io.github.subiyacryolite.jds.JdsEntity;
import io.github.subiyacryolite.jds.JdsLoad;
import io.github.subiyacryolite.jds.JdsSave;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ifunga on 01/07/2017.
 */
public class Ihneritance extends BaseTestConfig {

    @Override
    public void save() throws Exception {
        JdsSave save = new JdsSave(jdsDb, getInheritanceCollection());
        save.call();
    }

    @Override
    public void load() throws Exception {
        System.out.printf("=========== %s ===========\n", jdsDb.getImplementation());
        List<EntityA> entityAs = JdsLoad.load(jdsDb, EntityA.class);
        List<EntityB> entityBs = JdsLoad.load(jdsDb, EntityB.class);
        List<EntityC> entityCs = JdsLoad.load(jdsDb, EntityC.class);
        System.out.printf("All A s [%s]\n", entityAs);
        System.out.printf("All B s [%s]\n", entityBs);
        System.out.printf("All C s [%s]\n", entityCs);
    }

    @Test
    public void testIheritanceOracle() throws Exception {
        initialiseOracleBackend();
        save();
        jdsDb.toString();
    }

    @Test
    public void testIheritanceSqlite() throws Exception {
        initialiseSqlLiteBackend();
        save();
        jdsDb.toString();
    }

    @Test
    public void testIheritanceMySql() throws Exception {
        initialiseMysqlBackend();
        save();
        jdsDb.toString();
    }

    @Test
    public void testIheritancePostgreSQL() throws Exception {
        initialisePostgeSqlBackend();
        save();
        jdsDb.toString();
    }

    @Test
    public void testIheritanceTSql() throws Exception {
        initialiseTSqlBackend();
        save();
        jdsDb.toString();
    }

    @Test
    public void testAllInitialilization() throws Exception {
        testIheritanceSqlite();
        testIheritancePostgreSQL();
        testIheritanceTSql();
        testIheritanceMySql();
        testIheritanceOracle();
    }

    @Test
    public void loadInheritedTsql() throws Exception {
        initialiseTSqlBackend();
        load();
    }

    @Test
    public void loadInheritedMysSql() throws Exception {
        initialiseMysqlBackend();
        load();
    }

    @Test
    public void loadInheritedPostgresSql() throws Exception {
        initialisePostgeSqlBackend();
        load();
    }

    @Test
    public void loadInheritedSqLite() throws Exception {
        initialiseSqlLiteBackend();
        load();
    }

    @Test
    public void loadInheritedOracle() throws Exception {
        initialiseOracleBackend();
        load();
    }

    @Test
    public void loadInheritedAll() throws Exception {
        loadInheritedTsql();
        loadInheritedSqLite();
        loadInheritedMysSql();
        loadInheritedPostgresSql();
        loadInheritedOracle();
    }

    private List<JdsEntity> getInheritanceCollection() {
        List<JdsEntity> collection = new ArrayList<>();

        EntityA entitya = new EntityA();
        entitya.getOverview().setEntityGuid("entityA");//constant
        entitya.setEntityAValue("entity A - ValueA");

        EntityB entityb = new EntityB();
        entityb.getOverview().setEntityGuid("entityB");//constant
        entityb.setEntityAValue("entity B - Value A");
        entityb.setEntityBValue("entity B - Value B");

        EntityC entityc = new EntityC();
        entityc.getOverview().setEntityGuid("entityC");//constant
        entityc.setEntityAValue("entity C - Value A");
        entityc.setEntityBValue("entity C - Value B");
        entityc.setEntityCValue("entity C - Value C");

        collection.add(entitya);
        collection.add(entityb);
        collection.add(entityc);

        return collection;
    }


}
