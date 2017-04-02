package io.github.subiyacryolite.jds;

import io.github.subiyacryolite.jds.classes.SimpleAddress;
import io.github.subiyacryolite.jds.classes.SimpleAddressBook;
import io.github.subiyacryolite.jds.enums.JdsImplementation;
import org.junit.Test;
import org.sqlite.SQLiteConfig;

import java.io.File;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Comparator;
import java.util.List;

/**
 * Created by ifunga on 18/02/2017.
 */
public class DataStorageTests {

    private JdsDatabase jdsDatabase;

    public DataStorageTests() {
        initialiseJdsClasses();
    }

    @Test
    public void initialiseJdsClasses() {
        JdsEntityClasses.map(SimpleAddress.class);
        JdsEntityClasses.map(SimpleAddressBook.class);
    }

    @Test
    public void initialiseSqlLiteBackend() {
        String url = "jdbc:sqlite:" + getDatabaseFile();
        jdsDatabase = JdsDatabase.getImplementation(JdsImplementation.SQLITE);
        SQLiteConfig sqLiteConfig = new SQLiteConfig();
        sqLiteConfig.enforceForeignKeys(true); //You must enable foreign keys in SQLite
        jdsDatabase.setConnectionProperties(url, sqLiteConfig.toProperties());
        jdsDatabase.init();
    }

    @Test
    public void initialisePostgeSqlBackend() {
        jdsDatabase = JdsDatabase.getImplementation(JdsImplementation.POSTGRES);
        jdsDatabase.setConnectionProperties("org.postgresql.Driver", "jdbc:postgresql://127.0.0.1:5432/jds", "postgres", "");
        jdsDatabase.init();
    }

    @Test
    public void initialiseTSqlBackend() {
        jdsDatabase = JdsDatabase.getImplementation(JdsImplementation.TSQL);
        jdsDatabase.setConnectionProperties("com.microsoft.sqlserver.jdbc.SQLServerDriver", "jdbc:sqlserver://DESKTOP-64C7FRP\\JDSINSTANCE;databaseName=jds", "sa", "p@nkP#55W0rd");
        jdsDatabase.init();
    }

    @Test
    public void initialiseMysqlBackend() {
        jdsDatabase = JdsDatabase.getImplementation(JdsImplementation.MYSQL);
        jdsDatabase.setConnectionProperties("com.mysql.cj.jdbc.Driver", "jdbc:mysql://localhost:3306/jds?autoReconnect=true&useSSL=false", "root", "");
        jdsDatabase.init();
    }

    @Test
    public void saveAndLoad() {
        saveObject();
        testLoads();
    }

    @Test
    public void saveAndLoadPostreSqlImplementation() {
        initialisePostgeSqlBackend();
        saveAndLoad();
    }

    @Test
    public void saveAndLoadTsqlImplementation() {
        initialiseTSqlBackend();
        saveAndLoad();
    }

    @Test
    public void saveAndLoadSqliteImplementation() {
        initialiseSqlLiteBackend();
        saveAndLoad();
    }

    @Test
    public void saveAndLoadMySqlImplementation() {
        initialiseMysqlBackend();
        saveAndLoad();
    }

    @Test
    public void saveAndLoadAllImplementations() {
        saveAndLoadSqliteImplementation();
        saveAndLoadTsqlImplementation();
        saveAndLoadPostreSqlImplementation();
        saveAndLoadMySqlImplementation();
    }

    @Test
    public void saveObject() {
        SimpleAddressBook simpleAddressBook = getSimpleAddressBook();
        JdsSave.save(jdsDatabase, 1, simpleAddressBook);
        System.out.printf("Saved %s\n", simpleAddressBook);
    }

    @Test
    public void testLoads() {
        List<SimpleAddressBook> allAddressBooks = JdsLoad.load(jdsDatabase, SimpleAddressBook.class); //load all entities of type SimpleAddressBook
        List<SimpleAddressBook> specificAddressBook = JdsLoad.load(jdsDatabase, SimpleAddressBook.class, "testGuid0001"); //load all entities of type SimpleAddressBook with Entity Guids in range
        System.out.printf("All entities [%s]\n", allAddressBooks);
        System.out.printf("Specific entities [%s]\n", specificAddressBook);
    }

    @Test
    public void testSortedLoads() {
        Comparator<SimpleAddressBook> comparator = Comparator.comparing(SimpleAddressBook::getDateCreated);
        List<SimpleAddressBook> allAddressBooks = JdsLoad.load(jdsDatabase, SimpleAddressBook.class, comparator); //load all entities of type SimpleAddressBook
        List<SimpleAddressBook> specificAddressBook = JdsLoad.load(jdsDatabase, SimpleAddressBook.class, comparator, "testGuid0001"); //load all entities of type SimpleAddressBook with Entity Guids in range
        System.out.printf("All entities [%s]\n", allAddressBooks);
        System.out.printf("Specific entities [%s]\n", specificAddressBook);
    }

    @Test
    public void deleteUsingStrings() {
        JdsDelete.delete(jdsDatabase, "primaryAddress1");
    }

    @Test
    public void deleteUsingObjectOrCollection() {
        SimpleAddressBook simpleAddressBook = getSimpleAddressBook();
        JdsDelete.delete(jdsDatabase, simpleAddressBook);
    }

    private SimpleAddressBook getSimpleAddressBook() {
        SimpleAddress primaryAddress1 = new SimpleAddress();
        primaryAddress1.setEntityGuid("primaryAddress1"); //setting a custom Entity Guid
        primaryAddress1.setDateModified(LocalDateTime.of(2012, Month.APRIL, 12, 13, 49));
        primaryAddress1.setArea("Norte Broad");
        primaryAddress1.setCity("Livingstone");
        primaryAddress1.setCountry("Zambia");
        primaryAddress1.setPlotNumber(23);
        primaryAddress1.setProvinceOrState("Southern");
        primaryAddress1.setStreetName("East Street");

        SimpleAddress primaryAddress2 = new SimpleAddress();
        primaryAddress2.setEntityGuid("primaryAddress2"); //setting a custom Entity Guid
        primaryAddress2.setDateModified(LocalDateTime.of(2009, Month.OCTOBER, 16, 03, 34));
        primaryAddress2.setArea("Roma");
        primaryAddress2.setCity("Lusaka");
        primaryAddress2.setCountry("Zambia");
        primaryAddress2.setPlotNumber(2);
        primaryAddress2.setProvinceOrState("Lusaka");
        primaryAddress2.setStreetName("West Street");

        SimpleAddress primaryAddress3 = new SimpleAddress();
        primaryAddress3.setEntityGuid("primaryAddress3"); //setting a custom Entity Guid
        primaryAddress3.setDateModified(LocalDateTime.of(2007, Month.JULY, 04, 05, 10));
        primaryAddress3.setArea("Riverdale");
        primaryAddress3.setCity("Ndola");
        primaryAddress3.setCountry("Zambia");
        primaryAddress3.setPlotNumber(9);
        primaryAddress3.setProvinceOrState("Copperbelt");
        primaryAddress3.setStreetName("West Street");

        SimpleAddressBook simpleAddressBook = new SimpleAddressBook();
        simpleAddressBook.setEntityGuid("testGuid0001"); //setting a custom Entity Guid
        simpleAddressBook.getAddresses().add(primaryAddress1);
        simpleAddressBook.getAddresses().add(primaryAddress2);
        simpleAddressBook.getAddresses().add(primaryAddress3);
        return simpleAddressBook;
    }

    public String getDatabaseFile() {
        File path = new File(System.getProperty("user.home") + File.separator + ".jdstest" + File.separator + "jds.db");
        if (!path.exists()) {
            File directory = path.getParentFile();
            if (!directory.exists()) {
                directory.mkdirs();
            }
        }
        String absolutePath = path.getAbsolutePath();
        System.err.printf("You're test database is available here [%s]\n", absolutePath);
        return absolutePath;
    }
}
