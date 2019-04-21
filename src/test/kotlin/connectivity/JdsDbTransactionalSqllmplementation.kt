/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectivity

import io.github.subiyacryolite.jds.JdsDbTransactionalSql
import java.io.File
import java.io.FileInputStream
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

class JdsDbTransactionalSqllmplementation : JdsDbTransactionalSql() {

    override val connection: Connection
        get () {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
            val properties = Properties()
            FileInputStream(File("db.tsql.properties")).use { properties.load(it) }
            //return DriverManager.getConnection("jdbc:sqlserver://BR7INDANA\\SMARTCARE40;databaseName=jds", properties)
            return DriverManager.getConnection("jdbc:sqlserver://DESKTOP-64C7FRP\\JDSINSTANCE;databaseName=jds", properties);
        }
}
