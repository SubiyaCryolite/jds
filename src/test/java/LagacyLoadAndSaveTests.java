import io.github.subiyacryolite.jds.JdsDelete;
import io.github.subiyacryolite.jds.JdsLoad;
import io.github.subiyacryolite.jds.JdsSave;
import common.BaseTestConfig;
import entities.SimpleAddress;
import entities.SimpleAddressBook;
import org.junit.Test;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Created by ifunga on 18/02/2017.
 */
public class LagacyLoadAndSaveTests extends BaseTestConfig {

    @Test
    @Override
    public void save() throws Exception {
        System.out.printf("=========== %s ===========\n", jdsDb.getImplementation());
        SimpleAddressBook simpleAddressBook = getSimpleAddressBook();
        JdsSave.save(jdsDb, 1, simpleAddressBook);
        System.out.printf("Saved %s\n", simpleAddressBook);
    }


    @Test
    @Override
    public void load() throws Exception {
        List<SimpleAddress> allAddressBooks = new JdsLoad(jdsDb, SimpleAddress.class).call(); //load all entities of type SimpleAddressBook
        List<SimpleAddress> specificAddressBook = new JdsLoad(jdsDb, SimpleAddress.class, "primaryAddress").call(); //load all entities of type SimpleAddressBook with Entity Guids in range
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
    public void saveAndLoadOracleImplementation() throws Exception {
        initialiseOracleBackend();
        saveAndLoad();
    }

    @Test
    public void saveAndLoadAllImplementations() throws Exception {
        saveAndLoadTsqlImplementation();
        saveAndLoadPostreSqlImplementation();
        saveAndLoadMySqlImplementation();
        saveAndLoadSqliteImplementation();
        saveAndLoadOracleImplementation();
    }

    @Test
    public void testSortedLoads() throws Exception {
        Comparator<SimpleAddressBook> comparator = Comparator.comparing(SimpleAddressBook::getDateCreated);
        List<SimpleAddressBook> allAddressBooks = new JdsLoad(jdsDb, SimpleAddressBook.class, comparator).call(); //load all entities of type SimpleAddressBook
        List<SimpleAddressBook> specificAddressBook = JdsLoad.load(jdsDb, SimpleAddressBook.class, comparator, "testGuid0001"); //load all entities of type SimpleAddressBook with Entity Guids in range
        System.out.printf("All entities [%s]\n", allAddressBooks);
        System.out.printf("Specific entities [%s]\n", specificAddressBook);
    }

    @Test
    public void deleteUsingStrings() throws ExecutionException, InterruptedException {
        initialiseSqlLiteBackend();
        Boolean result = new FutureTask<>(new JdsDelete(jdsDb, "primaryAddress1")).get();
        System.out.print("Completed " + result);
    }

    @Test
    public void deleteUsingObjectOrCollection() throws Exception {
        SimpleAddressBook simpleAddressBook = getSimpleAddressBook();
        JdsDelete.delete(jdsDb, simpleAddressBook);
    }
}
