import io.github.subiyacryolite.jds.JdsEntity;
import io.github.subiyacryolite.jds.JdsLoad;
import io.github.subiyacryolite.jds.JdsSave;
import io.github.subiyacryolite.jds.common.BaseTestConfig;
import io.github.subiyacryolite.jds.entities.EntityA;
import io.github.subiyacryolite.jds.entities.EntityB;
import io.github.subiyacryolite.jds.entities.EntityC;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ifung on 01/07/2017.
 */
public class Ihneritance extends BaseTestConfig {

    private List<JdsEntity> getInheritanceCollection() {
        List<JdsEntity> collection = new ArrayList<>();

        EntityA entitya = new EntityA();
        entitya.setEntityGuid("entityA");//constant
        entitya.setEntityAValue("entity A - ValueA");

        EntityB entityb = new EntityB();
        entityb.setEntityGuid("entityB");//constant
        entityb.setEntityAValue("entity B - Value A");
        entityb.setEntityBValue("entity B - Value B");

        EntityC entityc = new EntityC();
        entityc.setEntityGuid("entityC");//constant
        entityc.setEntityAValue("entity C - Value A");
        entityc.setEntityBValue("entity C - Value B");
        entityc.setEntityCValue("entity C - Value C");

        collection.add(entitya);
        collection.add(entityb);
        collection.add(entityc);

        return collection;
    }

    @Test
    public void testIheritanceSqlite() throws Exception {
        initialiseSqlLiteBackend();
        JdsSave save = new JdsSave(jdsDb, getInheritanceCollection());
        save.call();
        jdsDb.toString();
    }

    @Test
    public void testIheritanceMySql() throws Exception {
        initialiseMysqlBackend();
        JdsSave save = new JdsSave(jdsDb, getInheritanceCollection());
        save.call();
        jdsDb.toString();
    }

    @Test
    public void testIheritancePostgreSQL() throws Exception {
        initialisePostgeSqlBackend();
        JdsSave save = new JdsSave(jdsDb, getInheritanceCollection());
        save.call();
        jdsDb.toString();
    }

    @Test
    public void testIheritanceTSql() throws Exception {
        initialiseTSqlBackend();
        JdsSave save = new JdsSave(jdsDb, getInheritanceCollection());
        save.call();
        jdsDb.toString();
    }

    @Test
    public void testAllInitialilization() throws Exception {
        testIheritanceSqlite();
        testIheritanceMySql();
        testIheritancePostgreSQL();
        testIheritanceTSql();
    }

    @Test
    public void loadInheritedTsql() throws Exception {
        initialiseTSqlBackend();
        List<EntityA> entityAs = JdsLoad.load(jdsDb, EntityA.class);
        List<EntityB> entityBs = JdsLoad.load(jdsDb, EntityB.class);
        List<EntityC> entityCs = JdsLoad.load(jdsDb, EntityC.class);
        System.out.printf("All A s [%s]\n", entityAs);
        System.out.printf("All B s [%s]\n", entityBs);
        System.out.printf("All C s [%s]\n", entityCs);
    }

    @Test
    public void loadInheritedMysSql() throws Exception {
        initialiseMysqlBackend();
        List<EntityA> entityAs = JdsLoad.load(jdsDb, EntityA.class);
        List<EntityB> entityBs = JdsLoad.load(jdsDb, EntityB.class);
        List<EntityC> entityCs = JdsLoad.load(jdsDb, EntityC.class);
        System.out.printf("All A s [%s]\n", entityAs);
        System.out.printf("All B s [%s]\n", entityBs);
        System.out.printf("All C s [%s]\n", entityCs);
    }

    @Test
    public void loadInheritedPostgresSql() throws Exception {
        initialisePostgeSqlBackend();
        List<EntityA> entityAs = JdsLoad.load(jdsDb, EntityA.class);
        List<EntityB> entityBs = JdsLoad.load(jdsDb, EntityB.class);
        List<EntityC> entityCs = JdsLoad.load(jdsDb, EntityC.class);
        System.out.printf("All A s [%s]\n", entityAs);
        System.out.printf("All B s [%s]\n", entityBs);
        System.out.printf("All C s [%s]\n", entityCs);
    }

    @Test
    public void loadInheritedSqLite() throws Exception {
        initialiseSqlLiteBackend();
        List<EntityA> entityAs = JdsLoad.load(jdsDb, EntityA.class);
        List<EntityB> entityBs = JdsLoad.load(jdsDb, EntityB.class);
        List<EntityC> entityCs = JdsLoad.load(jdsDb, EntityC.class);
        System.out.printf("All A s [%s]\n", entityAs);
        System.out.printf("All B s [%s]\n", entityBs);
        System.out.printf("All C s [%s]\n", entityCs);
    }

    @Test
    public void loadInheritedAll() throws Exception {
        loadInheritedTsql();
        loadInheritedSqLite();
        loadInheritedMysSql();
        loadInheritedPostgresSql();
    }

}
