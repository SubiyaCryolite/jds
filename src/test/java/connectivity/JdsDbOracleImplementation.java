package connectivity;

import io.github.subiyacryolite.jds.JdsDbOracle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author indana
 */
public class JdsDbOracleImplementation extends JdsDbOracle {

    @Override
    public Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Properties properties = new Properties();
        properties.put("user", "jdsx");
        properties.put("password", "jdsx");
        return DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", properties);
    }
}
