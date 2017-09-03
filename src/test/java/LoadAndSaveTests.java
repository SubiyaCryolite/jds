import common.BaseTestConfig;
import entities.JdsExample;
import io.github.subiyacryolite.jds.JdsDelete;
import io.github.subiyacryolite.jds.JdsLoad;
import io.github.subiyacryolite.jds.JdsSave;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Created by ifunga on 12/04/2017.
 */
public class LoadAndSaveTests extends BaseTestConfig {

    @Test
    public void save() throws Exception {
        List<JdsExample> collection = getCollection();
        Callable<Boolean> save = new JdsSave(jdsDb, collection);
        FutureTask<Boolean> saving = new FutureTask(save);
        new Thread(saving).start();
        while (!saving.isDone())
            System.out.println("Waiting for operation 1 to complete");
        System.out.printf("Saved? %s\n", saving.get());
    }

    @Test
    public void bulkSave() throws Exception {
        System.out.printf("=========== %s ===========\n", jdsDb.getImplementation());
        List<JdsExample> collection = new ArrayList<>();
        for (int i = 0; i < 5000; i++) {
            JdsExample jdsExample = new JdsExample();
            jdsExample.getOverview().setEntityGuid("guid_" + i);
            jdsExample.setIntField(i);
            jdsExample.setFloatField(i + 1);
            jdsExample.setDoubleField(i + 2);
            jdsExample.setLongField(i + 3);
            collection.add(jdsExample);
        }
        Callable<Boolean> save = new JdsSave(jdsDb, collection);
        FutureTask<Boolean> saving = new FutureTask(save);
        new Thread(saving).start();

        while (!saving.isDone())
            System.out.println("Waiting for operation 1 to complete");
        System.out.printf("Saved? %s\n", saving.get());
    }

    @Test
    public void loadNonExisting() throws Exception {
        Callable<List<JdsExample>> loadNonExistingCallable = new JdsLoad(jdsDb, JdsExample.class,"DOES_NOT_EXIST");
        FutureTask<List<JdsExample>> loadNonExistingTask = new FutureTask(loadNonExistingCallable);

        new Thread(loadNonExistingTask).start();

        while (!loadNonExistingTask.isDone())
            System.out.println("Waiting for operation 1 to complete");
        List<JdsExample> loadNonExistingResult = loadNonExistingTask.get();
        System.out.println(loadNonExistingResult);
    }

    @Test
    public void load() throws ExecutionException, InterruptedException {
        Callable<List<JdsExample>> loadAllInstances = new JdsLoad(jdsDb, JdsExample.class);
        Callable<List<JdsExample>> loadSpecificInstance = new JdsLoad(jdsDb, JdsExample.class, "instance3");
        Callable<List<JdsExample>> loadSortedInstances = new JdsLoad(jdsDb, JdsExample.class);

        FutureTask<List<JdsExample>> loadingAllInstances = new FutureTask(loadAllInstances);
        FutureTask<List<JdsExample>> loadingSpecificInstance = new FutureTask(loadSpecificInstance);
        FutureTask<List<JdsExample>> loadingSortedInstances = new FutureTask(loadSortedInstances);

        new Thread(loadingAllInstances).start();
        new Thread(loadingSpecificInstance).start();
        new Thread(loadingSortedInstances).start();

        while (!loadingAllInstances.isDone())
            System.out.println("Waiting for operation 1 to complete");
        while (!loadingSpecificInstance.isDone())
            System.out.println("Waiting for operation 2 to complete");
        while (!loadingSortedInstances.isDone())
            System.out.println("Waiting for operation 3 to complete");

        List<JdsExample> allInstances = loadingAllInstances.get();
        List<JdsExample> specificInstance = loadingSpecificInstance.get();
        List<JdsExample> sortedInstances = loadingSortedInstances.get();

        System.out.println(allInstances);
        System.out.println(specificInstance);
        System.out.println(sortedInstances);

        System.out.println("DONE");
    }

    @Test
    public void isolatedDelete() throws ExecutionException, InterruptedException {
        Callable<Boolean> delete = new JdsDelete(jdsDb, "instance2");

        FutureTask<Boolean> deleting = new FutureTask(delete);
        new Thread(deleting).start();

        while (!deleting.isDone())
            System.out.println("Waiting for operation to complete");
        System.out.println("Deleted? " + deleting.get());
    }

    @Test
    public void callableOracleBulkSave() throws Exception {
        initialiseOracleBackend();
        bulkSave();
    }

    @Test
    public void callableOracleBulkLoad() throws ExecutionException, InterruptedException {
        initialiseOracleBackend();
        load();
    }

    @Test
    public void callableSqlLiteBulkSave() throws Exception {
        initialiseTSqlBackend();
        bulkSave();
    }

    @Test
    public void callableSqlLiteBulkLoad() throws ExecutionException, InterruptedException {
        initialiseMysqlBackend();
        load();
    }

    @Test
    public void callableSqlLiteBulkLoadSave() throws Exception {
        initialiseSqlLiteBackend();
        bulkSave();
        load();
    }

    @Test
    public void callableSqlLiteLoadSave() throws Exception {
        initialiseSqlLiteBackend();
        save();
        load();
    }

    @Test
    public void callableSqlLiteLoadNoExisting() throws Exception {
        initialiseSqlLiteBackend();
        save();
        load();
    }

    @Test
    public void callableMysqlLoadSave() throws Exception {
        initialiseMysqlBackend();
        save();
        load();
    }

    @Test
    public void callablePostgeSqlLoadSave() throws Exception {
        initialisePostgeSqlBackend();
        save();
        load();
    }

    @Test
    public void callableTSqlLoadSave() throws Exception {
        initialiseTSqlBackend();
        save();
        load();
    }

    @Test
    public void callableTSqlBulkLoad() throws ExecutionException, InterruptedException {
        initialiseTSqlBackend();
        load();
    }
}
