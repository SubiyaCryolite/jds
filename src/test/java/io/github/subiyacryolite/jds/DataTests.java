package io.github.subiyacryolite.jds;

import io.github.subiyacryolite.jds.classes.SimpleAddress;
import io.github.subiyacryolite.jds.classes.SimpleAddressBook;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Comparator;
import java.util.List;

import static io.github.subiyacryolite.jds.classes.TestEnums.PRIMARY_ADDRESS_ENUM;

/**
 * Created by ifunga on 18/02/2017.
 */
public class DataTests extends BaseTest {
    @Test
    @Override
    public void saveAndLoad() {
        saveObject();
        testLoads();
    }

    @Test
    @Override
    public void saveObject() {
        SimpleAddressBook simpleAddressBook = getSimpleAddressBook();
        JdsSave.save(jdsDataBase, 1, simpleAddressBook);
        System.out.printf("Saved %s\n", simpleAddressBook);
    }

    @Test
    @Override
    public void testLoads() {
        List<SimpleAddressBook> allAddressBooks = JdsLoad.load(jdsDataBase, SimpleAddressBook.class); //load all entities of type SimpleAddressBook
        List<SimpleAddressBook> specificAddressBook = JdsLoad.load(jdsDataBase, SimpleAddressBook.class, "testGuid0001"); //load all entities of type SimpleAddressBook with Entity Guids in range
        System.out.printf("All entities [%s]\n", allAddressBooks);
        System.out.printf("Specific entities [%s]\n", specificAddressBook);
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
    public void testSortedLoads() {
        Comparator<SimpleAddressBook> comparator = Comparator.comparing(SimpleAddressBook::getDateCreated);
        List<SimpleAddressBook> allAddressBooks = JdsLoad.load(jdsDataBase, SimpleAddressBook.class, comparator); //load all entities of type SimpleAddressBook
        List<SimpleAddressBook> specificAddressBook = JdsLoad.load(jdsDataBase, SimpleAddressBook.class, comparator, "testGuid0001"); //load all entities of type SimpleAddressBook with Entity Guids in range
        System.out.printf("All entities [%s]\n", allAddressBooks);
        System.out.printf("Specific entities [%s]\n", specificAddressBook);
    }

    @Test
    public void deleteUsingStrings() {
        JdsDelete.delete(jdsDataBase, "primaryAddress1");
    }

    @Test
    public void deleteUsingObjectOrCollection() {
        SimpleAddressBook simpleAddressBook = getSimpleAddressBook();
        JdsDelete.delete(jdsDataBase, simpleAddressBook);
    }

    private SimpleAddressBook getSimpleAddressBook() {
        SimpleAddress primaryAddress = new SimpleAddress();
        primaryAddress.setEntityGuid("primaryAddress"); //setting a custom Entity Guid
        primaryAddress.setDateModified(LocalDateTime.of(2012, Month.APRIL, 12, 13, 49));
        primaryAddress.setArea("Norte Broad");
        primaryAddress.setCity("Livingstone");
        primaryAddress.setCountry("Zambia");
        primaryAddress.setPlotNumber(23);
        primaryAddress.setProvinceOrState("Southern");
        primaryAddress.setStreetName("East Street");
        primaryAddress.getPrimaryAddress().add(PRIMARY_ADDRESS_ENUM.getValue(0));

        SimpleAddress secondAddress = new SimpleAddress();
        secondAddress.setEntityGuid("secondAddress"); //setting a custom Entity Guid
        secondAddress.setDateModified(LocalDateTime.of(2009, Month.OCTOBER, 16, 03, 34));
        secondAddress.setArea("Roma");
        secondAddress.setCity("Lusaka");
        secondAddress.setCountry("Zambia");
        secondAddress.setPlotNumber(2);
        secondAddress.setProvinceOrState("Lusaka");
        secondAddress.setStreetName("West Street");
        secondAddress.getPrimaryAddress().add(PRIMARY_ADDRESS_ENUM.getValue(1));

        SimpleAddress thirdAddress = new SimpleAddress();
        thirdAddress.setEntityGuid("thirdAddress"); //setting a custom Entity Guid
        thirdAddress.setDateModified(LocalDateTime.of(2007, Month.JULY, 04, 05, 10));
        thirdAddress.setArea("Riverdale");
        thirdAddress.setCity("Ndola");
        thirdAddress.setCountry("Zambia");
        thirdAddress.setPlotNumber(9);
        thirdAddress.setProvinceOrState("Copperbelt");
        thirdAddress.setStreetName("West Street");
        thirdAddress.getPrimaryAddress().add(PRIMARY_ADDRESS_ENUM.getValue(1));

        SimpleAddressBook simpleAddressBook = new SimpleAddressBook();
        simpleAddressBook.setEntityGuid("testGuid0001"); //setting a custom Entity Guid
        simpleAddressBook.getAddresses().add(primaryAddress);
        simpleAddressBook.getAddresses().add(secondAddress);
        simpleAddressBook.getAddresses().add(thirdAddress);
        return simpleAddressBook;
    }
}
