/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.subiyacryolite.jds.tests.connectivity

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.subiyacryolite.jds.JdsDbTransactionalSql
import java.io.File
import java.io.FileInputStream
import java.util.*
import javax.sql.DataSource

class JdsDbTransactionalSqllmplementation : JdsDbTransactionalSql() {

    private val properties: Properties = Properties()
    private val hikariDataSource: DataSource

    init {
        FileInputStream(File("db.tsql.properties")).use { properties.load(it) }

        val hikariConfig = HikariConfig()
        hikariConfig.driverClassName = properties["driverClassName"].toString()
        hikariConfig.maximumPoolSize = properties["maximumPoolSize"].toString().toInt()
        hikariConfig.username = properties["username"].toString()
        hikariConfig.password = properties["password"].toString()
        hikariConfig.dataSourceProperties = properties //additional props
        hikariConfig.jdbcUrl = "jdbc:sqlserver://${properties["dbUrl"]}\\${properties["dbInstance"]};databaseName=${properties["dbName"]}"
        hikariDataSource = HikariDataSource(hikariConfig)
    }

    override val dataSource: DataSource
        get () = hikariDataSource
}
