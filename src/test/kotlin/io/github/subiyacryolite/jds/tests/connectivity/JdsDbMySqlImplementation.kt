/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.subiyacryolite.jds.tests.connectivity

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.subiyacryolite.jds.JdsDbMySql
import java.io.File
import java.io.FileInputStream
import java.util.*
import javax.sql.DataSource

class JdsDbMySqlImplementation : JdsDbMySql() {

    private val properties: Properties = Properties()
    private val hikariDataSource: DataSource

    init {
        FileInputStream(File("db.mysql.properties")).use { properties.load(it) }

        val hikariConfig = HikariConfig()
        hikariConfig.driverClassName = properties["driverClassName"].toString()
        hikariConfig.maximumPoolSize = properties["maximumPoolSize"].toString().toInt()
        hikariConfig.username = properties["username"].toString()
        hikariConfig.password = properties["password"].toString()
        hikariConfig.dataSourceProperties = properties //additional props
        hikariConfig.jdbcUrl = "jdbc:mysql://${properties["dbUrl"]}:${properties["dbPort"]}/${properties["dbName"]}"
        hikariDataSource = HikariDataSource(hikariConfig)
    }

    override val dataSource: DataSource
        get () = hikariDataSource
}