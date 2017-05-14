package io.github.subiyacryolite.jds;

import io.github.subiyacryolite.jds.classes.SimpleAddress;
import io.github.subiyacryolite.jds.classes.SimpleAddressBook;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import static io.github.subiyacryolite.jds.classes.TestEnums.PRIMARY_ADDRESS_ENUM;

/**
 * Created by ifunga on 18/02/2017.
 */
public class DataTests extends BaseTest {
    @Test
    @Override
    public void saveAndLoad() throws Exception {
        saveObject();
        testLoads();
    }

    @Test
    @Override
    public void saveObject() throws Exception {
        SimpleAddressBook simpleAddressBook = getSimpleAddressBook();
        JdsSave.save(jdsDataBase, 1, simpleAddressBook);
        System.out.printf("Saved %s\n", simpleAddressBook);
    }

    @Test
    @Override
    public void testLoads() throws Exception {
        FutureTask<List<SimpleAddressBook>> futureTask = new FutureTask<List<SimpleAddressBook>>(new JdsLoad(jdsDataBase, SimpleAddressBook.class));
        futureTask.run();
        List<SimpleAddressBook> allAddressBooks = futureTask.get(); //load all entities of type SimpleAddressBook
        List<SimpleAddressBook> specificAddressBook = new JdsLoad(jdsDataBase, SimpleAddressBook.class, "testGuid0001").call(); //load all entities of type SimpleAddressBook with Entity Guids in range
        System.out.printf("All entities [%s]\n", allAddressBooks);
        System.out.printf("Specific entities [%s]\n", specificAddressBook);
    }

    @Test
    public void saveAndLoadPostreSqlImplementation() throws Exception {
        initialisePostgeSqlBackend();
        saveAndLoad();
    }

    @Test
    public void saveAndLoadTsqlImplementation() throws Exception {
        initialiseTSqlBackend();
        saveAndLoad();
    }

    @Test
    public void saveAndLoadSqliteImplementation() throws Exception {
        initialiseSqlLiteBackend();
        saveAndLoad();
    }

    @Test
    public void saveAndLoadMySqlImplementation() throws Exception {
        initialiseMysqlBackend();
        saveAndLoad();
    }

    @Test
    public void saveAndLoadAllImplementations() throws Exception {
        saveAndLoadSqliteImplementation();
        saveAndLoadTsqlImplementation();
        saveAndLoadPostreSqlImplementation();
        saveAndLoadMySqlImplementation();
    }

    @Test
    public void testSortedLoads() throws Exception {
        Comparator<SimpleAddressBook> comparator = Comparator.comparing(SimpleAddressBook::getDateCreated);
        List<SimpleAddressBook> allAddressBooks = new JdsLoad(jdsDataBase, SimpleAddressBook.class, comparator).call(); //load all entities of type SimpleAddressBook
        List<SimpleAddressBook> specificAddressBook = JdsLoad.load(jdsDataBase, SimpleAddressBook.class, comparator, "testGuid0001"); //load all entities of type SimpleAddressBook with Entity Guids in range
        System.out.printf("All entities [%s]\n", allAddressBooks);
        System.out.printf("Specific entities [%s]\n", specificAddressBook);
    }

    @Test
    public void deleteUsingStrings() throws ExecutionException, InterruptedException {
        initialiseSqlLiteBackend();
        Boolean result = new FutureTask<>(new JdsDelete(jdsDataBase, "primaryAddress1")).get();
        System.out.print("Completed " + result);
    }

    @Test
    public void deleteUsingObjectOrCollection() throws Exception {
        SimpleAddressBook simpleAddressBook = getSimpleAddressBook();
        JdsDelete.delete(jdsDataBase, simpleAddressBook);
    }

    protected SimpleAddressBook getSimpleAddressBook() {
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
