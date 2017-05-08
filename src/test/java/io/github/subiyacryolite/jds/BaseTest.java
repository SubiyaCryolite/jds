package io.github.subiyacryolite.jds;

import io.github.subiyacryolite.jds.classes.SimpleAddress;
import io.github.subiyacryolite.jds.classes.SimpleAddressBook;
import io.github.subiyacryolite.jds.classes.TypeClass;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by ifung on 08/04/2017.
 */
public abstract class BaseTest {

    protected final double DELTA = 1e-15;
    protected JdsDb jdsDataBase;

    public void saveAndLoad() {
    }

    public void saveObject() {
    }

    public void testLoads() {
    }

    @Before
    public void beforeAll() {
        initialiseJdsClasses();
    }

    @Test
    public void initialiseJdsClasses() {
        JdsEntityClasses.map(TypeClass.class);
        JdsEntityClasses.map(SimpleAddress.class);
        JdsEntityClasses.map(SimpleAddressBook.class);
    }


    @Test
    public void initialiseSqlLiteBackend() {
        jdsDataBase = new JdsDbSqliteImplementation();
        jdsDataBase.init();
        jdsDataBase.logEdits(true);
    }

    @Test
    public void initialisePostgeSqlBackend() {
        jdsDataBase = new JdsDbPostgreSqlmplementation();
        jdsDataBase.init();
        jdsDataBase.logEdits(true);
    }

    @Test
    public void initialiseTSqlBackend() {
        jdsDataBase = new JdsDbTransactionalSqllmplementation();
        jdsDataBase.init();
        jdsDataBase.logEdits(true);
    }

    @Test
    public void initialiseMysqlBackend() {
        jdsDataBase = new JdsDbMySqlImplementation();
        jdsDataBase.init();
        jdsDataBase.logEdits(true);
    }
}
