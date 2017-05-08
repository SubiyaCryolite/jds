/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.subiyacryolite.jds;

import org.sqlite.SQLiteConfig;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author indana
 */
public class JdsDbSqliteImplementation extends JdsDbSqlite {

    @Override
    public Connection getConnection() throws ClassNotFoundException, SQLException {
        String url = "jdbc:sqlite:" + getDatabaseFile();
        SQLiteConfig sqLiteConfig = new SQLiteConfig();
        sqLiteConfig.enforceForeignKeys(true); //You must enable foreign keys in SQLite
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection(url, sqLiteConfig.toProperties());
    }

    public String getDatabaseFile() {
        File path = new File(System.getProperty("user.home") + File.separator + ".jdstest" + File.separator + "jds.db");
        if (!path.exists()) {
            File directory = path.getParentFile();
            if (!directory.exists()) {
                directory.mkdirs();
            }
        }
        String absolutePath = path.getAbsolutePath();
        return absolutePath;
    }
}
