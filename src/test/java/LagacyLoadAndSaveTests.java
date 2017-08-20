import common.BaseTestConfig;
import entities.Address;
import entities.AddressBook;
import io.github.subiyacryolite.jds.JdsDelete;
import io.github.subiyacryolite.jds.JdsLoad;
import io.github.subiyacryolite.jds.JdsSave;
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
        AddressBook addressBook = getSimpleAddressBook();
        JdsSave.save(jdsDb, 1, addressBook);
        System.out.printf("Saved %s\n", addressBook);
    }


    @Test
    @Override
    public void load() throws Exception {
        List<Address> allAddressBooks = new JdsLoad(jdsDb, Address.class).call(); //load all entities of type AddressBook
        List<Address> specificAddressBook = new JdsLoad(jdsDb, Address.class, "primaryAddress").call(); //load all entities of type AddressBook with Entity Guids in range
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
        Comparator<AddressBook> comparator = Comparator.comparing(entry -> entry.getOverview().getDateCreated());
        List<AddressBook> allAddressBooks = new JdsLoad(jdsDb, AddressBook.class, comparator).call(); //load all entities of type AddressBook
        List<AddressBook> specificAddressBook = JdsLoad.load(jdsDb, AddressBook.class, comparator, "testGuid0001"); //load all entities of type AddressBook with Entity Guids in range
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
        AddressBook addressBook = getSimpleAddressBook();
        JdsDelete.delete(jdsDb, addressBook);
    }
}
