package org.jenesis.jds;

import org.jenesis.jds.classes.SimpleAddress;
import org.jenesis.jds.classes.SimpleAddressBook;
import org.jenesis.jds.enums.JdsImplementation;
import org.junit.Test;
import org.sqlite.SQLiteConfig;

import java.io.File;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

/**
 * Created by ifunga on 18/02/2017.
 */
public class TestClass {

    private JdsDatabase jdsDatabase;

    public TestClass() {
        initialiseTSqlBackend();
        initialiseJdsClasses();
        jdsDatabase.logEdits(false);
    }

    public void initialiseSqlLiteBackend() {
        String url = "jdbc:sqlite:" + getDatabaseFile();
        jdsDatabase = JdsDatabase.getImplementation(JdsImplementation.SQLITE);
        SQLiteConfig sqLiteConfig = new SQLiteConfig();
        sqLiteConfig.enforceForeignKeys(true); //You must enable foreign keys in SQLite
        jdsDatabase.setConnectionProperties(url, sqLiteConfig.toProperties());
        jdsDatabase.init();
        jdsDatabase.logEdits(false);
    }

    public void initialisePostgeSqlBackend() {
        jdsDatabase = JdsDatabase.getImplementation(JdsImplementation.POSTGRES);
        jdsDatabase.setConnectionProperties("org.postgresql.Driver", "jdbc:postgresql://127.0.0.1:5432/jdstest", "postgres", "");
        jdsDatabase.init();
        jdsDatabase.logEdits(false);
    }

    public void initialiseTSqlBackend() {
        jdsDatabase = JdsDatabase.getImplementation(JdsImplementation.TSQL);
        jdsDatabase.setConnectionProperties("com.microsoft.sqlserver.jdbc.SQLServerDriver", "jdbc:sqlserver://DESKTOP-64C7FRP\\JDSINSTANCE;databaseName=jdstest", "sa", "p@nkP#55W0rd");
        jdsDatabase.init();
        jdsDatabase.logEdits(false);
    }

    /**
     * Initialise JDS Entity Classes
     */
    @Test
    public void initialiseJdsClasses() {
        JdsEntityClasses.map(SimpleAddress.class);
        JdsEntityClasses.map(SimpleAddressBook.class);
    }

    @Test
    public void testSaveAndLoad() {
        testSaves();
        testLoads();
    }

    /**
     *
     */
    @Test
    public void testSaves() {
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

    public String getDatabaseFile() {
        File path = new File(System.getProperty("user.home") + File.separator + ".jdstest" + File.separator + "database_001d.db");
        if (!path.exists()) {
            File directory = path.getParentFile();
            if (!directory.exists()) {
                directory.mkdirs();
            }
        }
        String absolutePath = path.getAbsolutePath();
        System.err.printf("You test database is available here [%s]\n", absolutePath);
        return absolutePath;
    }
}
