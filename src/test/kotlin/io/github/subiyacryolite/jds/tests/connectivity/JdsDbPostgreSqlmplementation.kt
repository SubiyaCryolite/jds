package io.github.subiyacryolite.jds.tests.connectivity

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.subiyacryolite.jds.JdsDbPostgreSql
import java.io.File
import java.io.FileInputStream
import java.util.*
import javax.sql.DataSource

class JdsDbPostgreSqlmplementation : JdsDbPostgreSql() {

    private val properties: Properties = Properties()
    private val hikariDataSource: DataSource

    init {
        FileInputStream(File("db.pg.properties")).use { properties.load(it) }

        val hikariConfig = HikariConfig()
        hikariConfig.driverClassName = properties["driverClassName"].toString()
        hikariConfig.maximumPoolSize = properties["maximumPoolSize"].toString().toInt()
        hikariConfig.username = properties["username"].toString()
        hikariConfig.password = properties["password"].toString()
        hikariConfig.dataSourceProperties = properties //additional props
        hikariConfig.jdbcUrl = "jdbc:postgresql://${properties["dbUrl"]}:${properties["dbPort"]}/${properties["dbName"]}"
        hikariDataSource = HikariDataSource(hikariConfig)
    }

    override val dataSource: DataSource
        get () = hikariDataSource
}