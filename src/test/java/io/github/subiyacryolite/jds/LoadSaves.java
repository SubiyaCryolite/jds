package io.github.subiyacryolite.jds;

import io.github.subiyacryolite.jds.common.BaseTestConfig;
import io.github.subiyacryolite.jds.entities.JdsExample;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Created by ifung on 12/04/2017.
 */
public class LoadSaves extends BaseTestConfig {

    @Test
    public void callableSqlLiteLoadSave() throws ExecutionException, InterruptedException {
        initialiseSqlLiteBackend();
        save();
        load();
    }

    @Test
    public void callableMysqlLoadSave() throws ExecutionException, InterruptedException {
        initialiseMysqlBackend();
        save();
        load();
    }

    @Test
    public void callablePostgeSqlLoadSave() throws ExecutionException, InterruptedException {
        initialisePostgeSqlBackend();
        save();
        load();
    }

    @Test
    public void callableTSqlLoadSave() throws ExecutionException, InterruptedException {
        initialiseTSqlBackend();
        save();
        load();
    }

    @Test
    public void save() throws ExecutionException, InterruptedException {
        List<JdsExample> collection = getCollection();
        Callable<Boolean> save = new JdsSave(jdsDb, 0, collection);
        FutureTask<Boolean> saving = new FutureTask(save);
        saving.run();
        while (!saving.isDone())
            System.out.println("Waiting for operation 1 to complete");
        System.out.printf("Saved? %s\n", saving.get());
    }

    @Test
    public void load() throws ExecutionException, InterruptedException {
        Callable<List<JdsExample>> loadAllInstances = new JdsLoad(jdsDb, JdsExample.class);
        Callable<List<JdsExample>> loadSpecificInstance = new JdsLoad(jdsDb, JdsExample.class, "instance3");
        Callable<List<JdsExample>> loadSortedInstances = new JdsLoad(jdsDb, JdsExample.class);

        FutureTask<List<JdsExample>> loadingAllInstances = new FutureTask(loadAllInstances);
        FutureTask<List<JdsExample>> loadingSpecificInstance = new FutureTask(loadSpecificInstance);
        FutureTask<List<JdsExample>> loadingSortedInstances = new FutureTask(loadSortedInstances);
        loadingAllInstances.run();
        loadingSpecificInstance.run();
        loadingSortedInstances.run();

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
        deleting.run();
        while(!deleting.isDone())
            System.out.println("Waiting for operation to complete");
        System.out.println("Deleted? "+ deleting.get());
    }
}
