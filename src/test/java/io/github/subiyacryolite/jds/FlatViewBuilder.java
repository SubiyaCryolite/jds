package io.github.subiyacryolite.jds;

import com.sun.javaws.exceptions.InvalidArgumentException;
import io.github.subiyacryolite.jds.common.BaseTestConfig;
import io.github.subiyacryolite.jds.entities.JdsExample;
import io.github.subiyacryolite.jds.entities.SimpleAddress;
import io.github.subiyacryolite.jds.entities.SimpleAddressBook;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ifung on 24/06/2017.
 */
public class FlatViewBuilder extends BaseTestConfig {

    public List<Class<? extends JdsEntity>> getEntities() {
        List<Class<? extends JdsEntity>> list = new ArrayList<>();
        list.add(SimpleAddress.class);
        list.add(JdsExample.class);
        list.add(SimpleAddressBook.class);
        return list;
    }

    @Test
    public void createFlatTableAllImplementations()
    {
        createFlatTableMysql();
        createFlatTablePostgres();
        createFlatTableTSql();
    }

    @Test
    public void createFlatTableTSql() {
        initialiseTSqlBackend();
        try {
            for (Class<? extends JdsEntity> entry : getEntities()) {
                boolean delete = JdsView.delete(entry, jdsDb);
                boolean create = JdsView.create(entry, jdsDb);
            }
        } catch (InvalidArgumentException e) {
            e.printStackTrace(System.err);
        }
    }

    @Test
    public void createFlatTablePostgres() {
        initialisePostgeSqlBackend();
        try {
            for (Class<? extends JdsEntity> entry : getEntities()) {
                boolean delete = JdsView.delete(entry, jdsDb);
                boolean create = JdsView.create(entry, jdsDb);
            }
        } catch (InvalidArgumentException e) {
            e.printStackTrace(System.err);
        }
    }

    @Test
    public void createFlatTableMysql() {
        initialiseMysqlBackend();
        try {
            for (Class<? extends JdsEntity> entry : getEntities()) {
                boolean delete = JdsView.delete(entry, jdsDb);
                boolean create = JdsView.create(entry, jdsDb);
            }
        } catch (InvalidArgumentException e) {
            e.printStackTrace(System.err);
        }
    }

}
