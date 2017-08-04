package connectivity;

import io.github.subiyacryolite.jds.JdsDbPostgreSql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author indana
 */
public class JdsDbPostgreSqlmplementation extends JdsDbPostgreSql {

    @Override
    public Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        Properties properties = new Properties();
        properties.put("user", "postgres");
        properties.put("password", "");
        return DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/jds", properties);
    }
}
