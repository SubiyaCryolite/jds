/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.subiyacryolite.jds.tests.connectivity

import io.github.subiyacryolite.jds.JdsDbSqlite
import org.sqlite.SQLiteConfig
import org.sqlite.SQLiteDataSource
import java.io.File
import java.sql.DriverManager
import javax.sql.DataSource


class JdsDbSqliteImplementation : JdsDbSqlite() {

    private val sqLiteDataSource: DataSource

    init {
        val path = File(System.getProperty("user.home") + File.separator + ".jdstest" + File.separator + "jds.db")
        if (!path.exists())
            if (!path.parentFile.exists())
                path.parentFile.mkdirs()

        val sqLiteConfig = SQLiteConfig()
        sqLiteConfig.enforceForeignKeys(true) //You must enable foreign keys in SQLite
        sqLiteDataSource = SQLiteDataSource(sqLiteConfig)
        sqLiteDataSource.url = "jdbc:sqlite:${path.absolutePath}"
    }

    override val dataSource: DataSource
        get () {
            Class.forName("org.sqlite.JDBC")
            return sqLiteDataSource
        }
}
