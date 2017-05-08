package io.github.subiyacryolite.jds;

import io.github.subiyacryolite.jds.classes.JdsDbSqliteImplementation;
import io.github.subiyacryolite.jds.classes.SimpleAddress;
import io.github.subiyacryolite.jds.classes.SimpleAddressBook;
import io.github.subiyacryolite.jds.classes.TypeClass;
import io.github.subiyacryolite.jds.enums.JdsImplementation;
import org.junit.Before;
import org.junit.Test;
import org.sqlite.SQLiteConfig;

import java.io.File;

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
        jdsDataBase = JdsDb.getImplementation(JdsImplementation.POSTGRES);
        jdsDataBase.setConnectionProperties("org.postgresql.Driver", "jdbc:postgresql://127.0.0.1:5432/jds", "postgres", "");
        jdsDataBase.init();
        jdsDataBase.logEdits(true);
    }

    @Test
    public void initialiseTSqlBackend() {
        jdsDataBase = JdsDb.getImplementation(JdsImplementation.TSQL);
        jdsDataBase.setConnectionProperties("com.microsoft.sqlserver.jdbc.SQLServerDriver", "jdbc:sqlserver://DESKTOP-64C7FRP\\JDSINSTANCE;databaseName=jds", "sa", "p@nkP#55W0rd");
        jdsDataBase.init();
        jdsDataBase.logEdits(true);
    }

    @Test
    public void initialiseMysqlBackend() {
        jdsDataBase = JdsDb.getImplementation(JdsImplementation.MYSQL);
        jdsDataBase.setConnectionProperties("com.mysql.cj.jdbc.Driver", "jdbc:mysql://localhost:3306/jds?autoReconnect=true&useSSL=false", "root", "");
        jdsDataBase.init();
        jdsDataBase.logEdits(true);
    }
}
