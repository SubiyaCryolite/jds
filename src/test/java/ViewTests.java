import io.github.subiyacryolite.jds.JdsEntity;
import io.github.subiyacryolite.jds.JdsView;
import common.BaseTestConfig;
import org.junit.Test;

import java.sql.SQLException;

/**
 * Created by ifunga on 24/06/2017.
 */
public class ViewTests extends BaseTestConfig {


    @Test
    public void createFlatTableAllImplementations() throws SQLException, ClassNotFoundException {
        createFlatTableMysql();
        createFlatTablePostgres();
        createFlatTableTSql();
        createFlatTableOracle();
    }

    @Test
    public void createFlatTableOracle() throws SQLException, ClassNotFoundException {
        initialiseOracleBackend();
        for (Class<? extends JdsEntity> entry : jdsDb.getMappedClasses()) {
            boolean delete = JdsView.delete(entry, jdsDb);
            boolean create = JdsView.create(entry, jdsDb);
        }
    }

    @Test
    public void createFlatTableTSql() throws SQLException, ClassNotFoundException {
        initialiseTSqlBackend();
        for (Class<? extends JdsEntity> entry : jdsDb.getMappedClasses()) {
            boolean delete = JdsView.delete(entry, jdsDb);
            boolean create = JdsView.create(entry, jdsDb);
        }
    }

    @Test
    public void createFlatTablePostgres() throws SQLException, ClassNotFoundException {
        initialisePostgeSqlBackend();
        for (Class<? extends JdsEntity> entry : jdsDb.getMappedClasses()) {
            boolean delete = JdsView.delete(entry, jdsDb);
            boolean create = JdsView.create(entry, jdsDb);
        }
    }

    @Test
    public void createFlatTableMysql() throws SQLException, ClassNotFoundException {
        initialiseMysqlBackend();
        for (Class<? extends JdsEntity> entry : jdsDb.getMappedClasses()) {
            boolean delete = JdsView.delete(entry, jdsDb);
            boolean create = JdsView.create(entry, jdsDb);
        }
    }

}
