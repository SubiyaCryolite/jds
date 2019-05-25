/**
 * Jenesis Data Store Copyright (c) 2017 Ifunga Ndana. All rights reserved.
 *
 * 1. Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 2. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 3. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * Neither the name Jenesis Data Store nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package io.github.subiyacryolite.jds

import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation
import io.github.subiyacryolite.jds.enums.JdsComponent
import io.github.subiyacryolite.jds.enums.JdsComponentType
import io.github.subiyacryolite.jds.enums.JdsFieldType
import io.github.subiyacryolite.jds.enums.JdsImplementation
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.Serializable
import java.sql.Connection
import java.sql.SQLException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.sql.DataSource
import kotlin.collections.LinkedHashMap

/**
 * This class is responsible for the setup of SQL connections, default database
 * write statements, as well as the initialization of core and custom components
 * that will support JDS on the underlying Database implementation
 * @param implementation
 * @param supportsStatements
 */
abstract class JdsDb(val implementation: JdsImplementation, val supportsStatements: Boolean) : IJdsDb, Serializable {

    val classes = ConcurrentHashMap<Long, Class<out JdsEntity>>()
    val tables = HashSet<JdsTable>()
    val options = JdsOptions()
    var dimensionTable = ""

    /**
     * Initialise JDS base tables
     */
    @JvmOverloads
    fun init(dimensionTable: String = "jds_entity_overview") {
        try {
            this.dimensionTable = dimensionTable
            dataSource.connection.use { connection ->
                prepareDatabaseComponents(connection)
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }


    /**
     * Initialise core database components
     */
    private fun prepareDatabaseComponents(connection: Connection) {
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.REF_ENTITIES)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.REF_FIELD_TYPES)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.REF_FIELDS)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.REF_ENUM_VALUES)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.REF_INHERITANCE)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.ENTITY_OVERVIEW)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.ENTITY_BINDING)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.REF_ENTITY_FIELD)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.REF_ENTITY_ENUMS)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_TEXT)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_TEXT_COLLECTION)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_BLOB)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_ENUM)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_ENUM_STRING)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_ENUM_COLLECTION)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_ENUM_STRING_COLLECTION)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_FLOAT)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_FLOAT_COLLECTION)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_INTEGER)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_INTEGER_COLLECTION)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_DATE)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_LONG)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_LONG_COLLECTION)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_DOUBLE)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_DOUBLE_COLLECTION)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_DATE_TIME)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_DATE_TIME_COLLECTION)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_ZONED_DATE_TIME)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_TIME)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_PERIOD)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_DURATION)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_YEAR_MONTH)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_MONTH_DAY)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_BOOLEAN)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.ENTITY_LIVE_VERSION)
        if (supportsStatements) {
            prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_STORE_BOOLEAN)
            prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_STORE_BLOB)
            prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_STORE_TEXT)
            prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_STORE_LONG)
            prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_STORE_INTEGER)
            prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_STORE_FLOAT)
            prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_STORE_DOUBLE)
            prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_STORE_DATE_TIME)
            prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_STORE_TIME)
            prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_STORE_DATE)
            prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_STORE_DURATION)
            prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_STORE_PERIOD)
            prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_STORE_MONTH_YEAR)
            prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_STORE_MONTH_DAY)
            prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_STORE_YEAR_MONTH)
            prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_STORE_ENUM)
            prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_STORE_ENUM_STRING)
            prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_STORE_ZONED_DATE_TIME)
            prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_ENTITY_OVERVIEW)
            prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_ENTITY_BINDING)
            prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_REF_ENTITY_FIELD)
            prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_REF_ENTITY_ENUM)
            prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_REF_ENTITY)
            prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_REF_ENUM)
            prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_REF_FIELD)
            prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_REF_ENTITY_INHERITANCE)
            prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_ENTITY_LIVE_VERSION)
            prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_STORE_TEXT_COLLECTION)
            prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_STORE_ENUM_COLLECTION)
            prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_STORE_ENUM_STRING_COLLECTION)
            prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_STORE_FLOAT_COLLECTION)
            prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_STORE_INTEGER_COLLECTION)
            prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_STORE_LONG_COLLECTION)
            prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_STORE_DOUBLE_COLLECTION)
            prepareDatabaseComponent(connection, JdsComponentType.STORED_PROCEDURE, JdsComponent.POP_STORE_DATE_TIME_COLLECTION)
        }
    }

    val isOracleDb: Boolean
        get() = implementation === JdsImplementation.ORACLE

    val isTransactionalSqlDb: Boolean
        get() = implementation === JdsImplementation.TSQL

    val isMySqlDb: Boolean
        get() = implementation === JdsImplementation.MYSQL

    val isSqLiteDb: Boolean
        get() = implementation === JdsImplementation.SQLITE

    val isPosgreSqlDb: Boolean
        get() = implementation === JdsImplementation.POSTGRES

    /**
     * Delegates the creation of custom database components depending on the
     * underlying JDS Database implementation
     *
     * @param connection        the connection to use for this operation
     * @param databaseComponent the type of database component to create
     * @param jdsComponent      an enum that maps to the components concrete
     * implementation details
     */
    private fun prepareDatabaseComponent(connection: Connection, databaseComponent: JdsComponentType, jdsComponent: JdsComponent) {
        when (databaseComponent) {
            JdsComponentType.TABLE -> if (!doesTableExist(connection, jdsComponent.component))
                initiateDatabaseComponent(connection, jdsComponent)

            JdsComponentType.STORED_PROCEDURE -> if (!doesProcedureExist(connection, jdsComponent.component))
                initiateDatabaseComponent(connection, jdsComponent)

            JdsComponentType.TRIGGER -> if (!doesTriggerExist(connection, jdsComponent.component))
                initiateDatabaseComponent(connection, jdsComponent)
        }
    }

    /**
     * Initialises core JDS Database components
     *
     * @param connection   the SQL connection top use for this operation
     * @param jdsComponent an enum that maps to the components concrete
     * implementation details
     */
    private fun initiateDatabaseComponent(connection: Connection, jdsComponent: JdsComponent) {
        when (jdsComponent) {
            JdsComponent.ENTITY_BINDING -> createBindEntityBinding(connection)
            JdsComponent.ENTITY_LIVE_VERSION -> executeSqlFromString(connection, createEntityLiveVersionTable())
            JdsComponent.ENTITY_OVERVIEW -> {
                createRefEntityOverview(connection)
            }
            JdsComponent.REF_ENTITIES -> createStoreEntities(connection)
            JdsComponent.REF_ENTITY_ENUMS -> createBindEntityEnums(connection)
            JdsComponent.REF_ENTITY_FIELD -> createBindEntityFields(connection)
            JdsComponent.REF_ENUM_VALUES -> createRefEnumValues(connection)
            JdsComponent.REF_FIELD_TYPES -> {
                createRefFieldTypes(connection)
                populateFieldTypes(connection)
            }
            JdsComponent.REF_FIELDS -> createRefFields(connection)
            JdsComponent.REF_INHERITANCE -> createRefInheritance(connection)
            JdsComponent.STORE_BLOB -> executeSqlFromString(connection, createStoreBlob())
            JdsComponent.STORE_BOOLEAN -> executeSqlFromString(connection, createStoreBoolean())
            JdsComponent.STORE_DATE -> executeSqlFromString(connection, createStoreDate())
            JdsComponent.STORE_DATE_TIME -> executeSqlFromString(connection, createStoreDateTime())
            JdsComponent.STORE_DATE_TIME_COLLECTION -> executeSqlFromString(connection, createStoreDateTimeCollection())
            JdsComponent.STORE_DOUBLE -> executeSqlFromString(connection, createStoreDouble())
            JdsComponent.STORE_DOUBLE_COLLECTION -> executeSqlFromString(connection, createStoreDoubleCollection())
            JdsComponent.STORE_DURATION -> executeSqlFromString(connection, createStoreDuration())
            JdsComponent.STORE_ENUM -> executeSqlFromString(connection, createStoreEnum())
            JdsComponent.STORE_ENUM_STRING -> executeSqlFromString(connection, createStoreEnumString())
            JdsComponent.STORE_ENUM_COLLECTION -> executeSqlFromString(connection, createStoreEnumCollection())
            JdsComponent.STORE_ENUM_STRING_COLLECTION -> executeSqlFromString(connection, createStoreEnumStringCollection())
            JdsComponent.STORE_FLOAT -> executeSqlFromString(connection, createStoreFloat())
            JdsComponent.STORE_FLOAT_COLLECTION -> executeSqlFromString(connection, createStoreFloatCollection())
            JdsComponent.STORE_INTEGER -> executeSqlFromString(connection, createStoreInteger())
            JdsComponent.STORE_INTEGER_COLLECTION -> executeSqlFromString(connection, createStoreIntegerCollection())
            JdsComponent.STORE_LONG -> executeSqlFromString(connection, createStoreLong())
            JdsComponent.STORE_LONG_COLLECTION -> executeSqlFromString(connection, createStoreLongCollection())
            JdsComponent.STORE_MONTH_DAY -> executeSqlFromString(connection, createStoreMonthDay())
            JdsComponent.STORE_PERIOD -> executeSqlFromString(connection, createStorePeriod())
            JdsComponent.STORE_TEXT -> executeSqlFromString(connection, createStoreText())
            JdsComponent.STORE_TEXT_COLLECTION -> executeSqlFromString(connection, createStoreTextCollection())
            JdsComponent.STORE_TIME -> executeSqlFromString(connection, createStoreTime())
            JdsComponent.STORE_YEAR_MONTH -> executeSqlFromString(connection, createStoreYearMonth())
            JdsComponent.STORE_ZONED_DATE_TIME -> executeSqlFromString(connection, createStoreZonedDateTime())
            /********************************************************
             * Directives that create stored procedures follow below
             ********************************************************/
            JdsComponent.POP_ENTITY_BINDING -> executeSqlFromString(connection, createPopJdsEntityBinding())
            JdsComponent.POP_ENTITY_LIVE_VERSION -> executeSqlFromString(connection, createPopEntityLiveVersion())
            JdsComponent.POP_ENTITY_OVERVIEW -> executeSqlFromString(connection, createPopJdsEntityOverview())
            JdsComponent.POP_REF_ENTITY -> executeSqlFromString(connection, createPopJdsRefEntity())
            JdsComponent.POP_REF_ENTITY_ENUM -> executeSqlFromString(connection, createPopJdsRefEntityEnum())
            JdsComponent.POP_REF_ENTITY_FIELD -> executeSqlFromString(connection, createPopJdsRefEntityField())
            JdsComponent.POP_REF_ENTITY_INHERITANCE -> executeSqlFromString(connection, createPopJdsRefEntityInheritance())
            JdsComponent.POP_REF_ENUM -> executeSqlFromString(connection, createPopJdsRefEnum())
            JdsComponent.POP_REF_FIELD -> executeSqlFromString(connection, createPopJdsRefField())
            JdsComponent.POP_STORE_BLOB -> executeSqlFromString(connection, createPopJdsStoreBlob())
            JdsComponent.POP_STORE_BOOLEAN -> executeSqlFromString(connection, createPopJdsStoreBoolean())
            JdsComponent.POP_STORE_DATE -> executeSqlFromString(connection, createPopJdsStoreDate())
            JdsComponent.POP_STORE_DATE_TIME -> executeSqlFromString(connection, createPopJdsStoreDateTime())
            JdsComponent.POP_STORE_DATE_TIME_COLLECTION -> executeSqlFromString(connection, createPopDateTimeCollection())
            JdsComponent.POP_STORE_DOUBLE -> executeSqlFromString(connection, createPopJdsStoreDouble())
            JdsComponent.POP_STORE_DOUBLE_COLLECTION -> executeSqlFromString(connection, createPopDoubleCollection())
            JdsComponent.POP_STORE_DURATION -> executeSqlFromString(connection, createPopJdsStoreDuration())
            JdsComponent.POP_STORE_ENUM -> executeSqlFromString(connection, createPopJdsStoreEnum())
            JdsComponent.POP_STORE_ENUM_STRING -> executeSqlFromString(connection, createPopJdsStoreEnumString())
            JdsComponent.POP_STORE_ENUM_COLLECTION -> executeSqlFromString(connection, createPopEnumCollection())
            JdsComponent.POP_STORE_ENUM_STRING_COLLECTION -> executeSqlFromString(connection, createPopEnumStringCollection())
            JdsComponent.POP_STORE_FLOAT -> executeSqlFromString(connection, createPopJdsStoreFloat())
            JdsComponent.POP_STORE_FLOAT_COLLECTION -> executeSqlFromString(connection, createPopFloatCollection())
            JdsComponent.POP_STORE_INTEGER -> executeSqlFromString(connection, createPopJdsStoreInteger())
            JdsComponent.POP_STORE_INTEGER_COLLECTION -> executeSqlFromString(connection, createPopIntegerCollection())
            JdsComponent.POP_STORE_LONG -> executeSqlFromString(connection, createPopJdsStoreLong())
            JdsComponent.POP_STORE_LONG_COLLECTION -> executeSqlFromString(connection, createPopLongCollection())
            JdsComponent.POP_STORE_MONTH_DAY -> executeSqlFromString(connection, createPopJdsMonthDay())
            JdsComponent.POP_STORE_MONTH_YEAR -> executeSqlFromString(connection, createPopJdsMonthYear())
            JdsComponent.POP_STORE_PERIOD -> executeSqlFromString(connection, createPopJdsStorePeriod())
            JdsComponent.POP_STORE_TEXT -> executeSqlFromString(connection, createPopJdsStoreText())
            JdsComponent.POP_STORE_TEXT_COLLECTION -> executeSqlFromString(connection, createPopTextCollection())
            JdsComponent.POP_STORE_TIME -> executeSqlFromString(connection, createPopJdsStoreTime())
            JdsComponent.POP_STORE_YEAR_MONTH -> executeSqlFromString(connection, createPopJdsYearMonth())
            JdsComponent.POP_STORE_ZONED_DATE_TIME -> executeSqlFromString(connection, createPopJdsStoreZonedDateTime())
            else -> {
            }
        }
    }

    override fun doesTableExist(connection: Connection, tableName: String): Boolean {
        val answer = tableExists(connection, tableName)
        return answer == 1
    }

    override fun doesProcedureExist(connection: Connection, procedureName: String): Boolean {
        val answer = procedureExists(connection, procedureName)
        return answer == 1
    }

    override fun doesTriggerExist(connection: Connection, triggerName: String): Boolean {
        val answer = triggerExists(connection, triggerName)
        return answer == 1
    }

    override fun doesColumnExist(connection: Connection, tableName: String, columnName: String): Boolean {
        val answer = columnExists(connection, tableName, columnName)
        return answer == 1
    }

    internal fun getResult(connection: Connection, sql: String, params: Array<String>): Int {
        try {
            connection.prepareStatement(sql).use { statement ->
                params.forEachIndexed { index, param ->
                    statement.setString(index + 1, param)
                }
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        return resultSet.getInt("Result")
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
        return 0
    }

    /**
     * Executes SQL found in the specified file. We recommend having one
     * statement per file.
     *
     * @param fileName the file containing SQL to find
     */
    internal fun executeSqlFromFile(connection: Connection, fileName: String) = try {
        Thread.currentThread().contextClassLoader.getResourceAsStream(fileName).use {
            val innerSql = fileToString(it)
            executeSqlFromString(connection, innerSql)
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    private fun executeSqlFromString(connection: Connection, sql: String) = try {
        connection.prepareStatement(sql).use { it.executeUpdate() }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }


    /**
     * Method to read contents of a file to a String variable
     *
     * @param inputStream the stream containing a files contents
     * @return the contents of a file contained in the input stream
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun fileToString(inputStream: InputStream): String {
        val bufferedInputStream = BufferedInputStream(inputStream)
        val byteArrayOutputStream = ByteArrayOutputStream()
        var result = bufferedInputStream.read()
        while (result != -1) {
            byteArrayOutputStream.write(result.toByte().toInt())
            result = bufferedInputStream.read()
        }
        byteArrayOutputStream.close()
        bufferedInputStream.close()
        return byteArrayOutputStream.toString()
    }

    /**
     * Internal checks to see if the specified table exists the the database
     *
     * @param connection the connection to use
     * @param tableName the table to look up
     * @return 1 if the specified table exists the the database
     */
    abstract fun tableExists(connection: Connection, tableName: String): Int

    /**
     * @param procedureName
     * @param tableName
     * @param columns
     * @param uniqueColumns
     * @param doNothingOnConflict
     */
    abstract override fun createOrAlterProc(procedureName: String,
                                            tableName: String,
                                            columns: Map<String, String>,
                                            uniqueColumns: Collection<String>,
                                            doNothingOnConflict: Boolean): String

    /**
     * @param tableName
     * @param columns LinkedHashMap<columnName -> columnType>
     * @param uniqueColumns LinkedHashMap<constrainName -> constraintColumns>
     * @param primaryKeys LinkedHashMap<constrainName -> constraintColumns>
     * @param foreignKeys LinkedHashMap<constraintName -> LinkedHashMap< LocalColumns -> ReferenceTable(ReferenceColumns)>>
     */
    open fun createTable(tableName: String,
                         columns: HashMap<String, String>,
                         uniqueColumns: HashMap<String, String>,
                         primaryKeys: HashMap<String, String>,
                         foreignKeys: LinkedHashMap<String, LinkedHashMap<String, String>>): String {
        val sqlBuilder = StringBuilder()
        sqlBuilder.append("CREATE TABLE $tableName(\n")

        val endingComponents = StringJoiner(",\n\t")
        sqlBuilder.append("\t")
        val columnJoiner = StringJoiner(",\n\t")
        columns.forEach { (column, type) ->
            columnJoiner.add("$column $type")
        }
        endingComponents.add(columnJoiner.toString())

        if (uniqueColumns.isNotEmpty()) {
            uniqueColumns.forEach { (constraintName, constrainColumns) ->
                endingComponents.add("CONSTRAINT $constraintName UNIQUE ($constrainColumns)")
            }
        }

        if (foreignKeys.isNotEmpty()) {
            foreignKeys.forEach { (constraintName, localColumnsRemoteTableColumns) ->
                localColumnsRemoteTableColumns.forEach { (localColumns, remoteTableColumns) ->
                    endingComponents.add("CONSTRAINT $constraintName FOREIGN KEY ($localColumns) REFERENCES $remoteTableColumns ON DELETE CASCADE")
                }
            }
        }

        if (primaryKeys.isNotEmpty()) {
            primaryKeys.forEach { (_, constraintColumns) ->
                endingComponents.add("PRIMARY KEY ($constraintColumns)")
            }
        }

        sqlBuilder.append(endingComponents)

        sqlBuilder.append(")")
        return sqlBuilder.toString()
    }

    /**
     * Internal checks to see if the specified procedure exists in the database
     *
     * @param connection the connection to use
     * @param procedureName the procedure to look up
     * @return 1 if the specified procedure exists in the database
     */
    open fun procedureExists(connection: Connection, procedureName: String): Int = 0

    /**
     * Internal checks to see if the specified view exists in the database
     *
     * @param connection the connection to use
     * @param viewName the view to look up
     * @return 1 if the specified procedure exists in the database
     */
    open fun viewExists(connection: Connection, viewName: String): Int = 0

    /**
     * Internal checks to see if the specified trigger exists in the database
     *
     * @param connection the connection to use
     * @param triggerName the trigger to look up
     * @return 1 if the specified trigger exists in the database
     */
    open fun triggerExists(connection: Connection, triggerName: String): Int = 0

    /**
     * Internal checks to see if the specified column exists in the database
     *
     * @param connection the connection to use
     * @param tableName the table to look-up
     * @param columnName the column to look-up
     * @return 1 if the specified column exists in the database
     */
    open fun columnExists(connection: Connection, tableName: String, columnName: String): Int {
        return 0
    }

    /**
     * Database specific SQL used to create the schema that stores long values
     */
    protected fun createStoreLong(connection: Connection) = executeSqlFromString(connection, createStoreLong())

    /**
     * Database specific SQL used to create the schema that stores entity
     * definitions
     */
    protected abstract fun createStoreEntities(connection: Connection)

    /**
     * Database specific SQL used to create the schema that stores enum
     * definitions
     */
    protected abstract fun createRefEnumValues(connection: Connection)

    /**
     * Database specific SQL used to create the schema that stores inheritance information
     */
    protected abstract fun createRefInheritance(connection: Connection)

    /**
     * Database specific SQL used to create the schema that stores field
     * definitions
     */
    protected abstract fun createRefFields(connection: Connection)

    /**
     * Database specific SQL used to create the schema that stores field type
     * definitions
     */
    protected abstract fun createRefFieldTypes(connection: Connection)

    /**
     * Database specific SQL used to create the schema that stores entity
     * binding information
     */
    protected abstract fun createBindEntityFields(connection: Connection)

    /**
     * Database specific SQL used to create the schema that stores entity to
     * enum binding information
     */
    protected abstract fun createBindEntityEnums(connection: Connection)

    /**
     * Database specific SQL used to create the schema that stores entity
     * overview
     */
    protected abstract fun createRefEntityOverview(connection: Connection)

    /**
     * Database specific SQL used to create the schema that stores entity
     * overview
     */
    protected abstract fun createBindEntityBinding(connection: Connection)

    /**
     * @param connection     the SQL connection to use for DB operations
     * @param parentEntities a collection of parent classes
     * @param entityCode     the value representing the entity
     */
    private fun mapParentEntities(connection: Connection, parentEntities: List<Long>, entityCode: Long) = try {
        (if (supportsStatements) connection.prepareCall(mapParentToChild()) else connection.prepareStatement(mapParentToChild())).use { statement ->
            for (parentEntity in parentEntities) {
                statement.setLong(1, parentEntity)
                statement.setLong(2, entityCode)
                statement.addBatch()
            }
            statement.executeBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * Maps an entity's name to its id
     *
     * @param connection the SQL connection to use for DB operations
     * @param id   the entity's id
     * @param name the entity's name
     * @param caption
     * @param description
     */
    private fun populateRefEntity(connection: Connection, id: Long, name: String, caption: String, description: String) = try {
        (if (supportsStatements) connection.prepareCall(populateRefEntity()) else connection.prepareStatement(populateRefEntity())).use { statement ->
            statement.setLong(1, id)
            statement.setString(2, name)
            statement.setString(3, caption)
            statement.setString(4, description)
            statement.executeUpdate()
            if (options.isLoggingOutput)
                println("Mapped Entity [$name - $id]")
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    fun mapTable(vararg table: JdsTable) {
        tables.addAll(table)
    }

    @Throws(Exception::class)
    fun prepareTables() {
        dataSource.connection.use { connection ->
            tables.forEach { it.forceGenerateOrUpdateSchema(this, connection) }
        }
    }

    fun map(entity: Class<out JdsEntity>) {
        val classHasAnnotation = entity.isAnnotationPresent(JdsEntityAnnotation::class.java)
        val superclassHasAnnotation = entity.superclass.isAnnotationPresent(JdsEntityAnnotation::class.java)
        if (classHasAnnotation || superclassHasAnnotation) {
            val entityAnnotation = when (classHasAnnotation) {
                true -> entity.getAnnotation(JdsEntityAnnotation::class.java)
                false -> entity.superclass.getAnnotation(JdsEntityAnnotation::class.java)
            }
            if (!classes.containsKey(entityAnnotation.id)) {
                classes[entityAnnotation.id] = entity
                //do the thing
                try {
                    dataSource.connection.use { connection ->
                        connection.autoCommit = false
                        val parentEntities = ArrayList<Long>()
                        val jdsEntity = entity.getDeclaredConstructor().newInstance()
                        parentEntities.add(jdsEntity.overview.entityId)//add this own entity to the chain
                        JdsExtensions.determineParents(entity, parentEntities)
                        populateRefEntity(connection, jdsEntity.overview.entityId, entityAnnotation.name, entityAnnotation.caption, entityAnnotation.description)
                        jdsEntity.populateRefFieldRefEntityField(this, connection, jdsEntity.overview.entityId)
                        jdsEntity.populateRefEnumRefEntityEnum(this, connection, jdsEntity.overview.entityId)
                        mapParentEntities(connection, parentEntities, jdsEntity.overview.entityId)
                        connection.commit()
                        connection.autoCommit = true
                        if (options.isLoggingOutput)
                            println("Mapped Entity [${entityAnnotation.name}]")
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace(System.err)
                }
            }
        } else
            throw RuntimeException("You must annotate the class [${entity.canonicalName}] or its parent with [${JdsEntityAnnotation::class.java}]")
    }

    private fun populateFieldTypes(connection: Connection) {
        connection.prepareStatement("INSERT INTO jds_ref_field_type(ordinal, caption) VALUES(?,?)").use {
            JdsFieldType.values().forEach { ft ->
                it.setInt(1, ft.ordinal)
                it.setString(2, ft.name)
                it.addBatch()
            }
            it.executeBatch()
        }
    }

    internal open fun saveEntityLiveVersion() = "{call jds_pop_entity_live_version(?)}"

    internal open fun saveMonthDay() = "{call jds_pop_month_day(?, ?, ?, ?)}"

    internal open fun saveYearMonth() = "{call jds_pop_year_month(?, ?, ?, ?)}"

    internal open fun savePeriod() = "{call jds_pop_period(?, ?, ?, ?)}"

    internal open fun saveDuration() = "{call jds_pop_duration(?, ?, ?, ?)}"

    /**
     * SQL call to save blob values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveBlob() = "{call jds_pop_blob(?, ?, ?, ?)}"

    /**
     * SQL call to save boolean values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveBoolean() = "{call jds_pop_boolean(?, ?, ?, ?)}"

    /**
     * SQL call to save datetime values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveDateTime() = "{call jds_pop_date_time(?, ?, ?, ?)}"

    /**
     * SQL call to save datetime values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveDateTimeCollection() = "{call jds_pop_date_time_col(?, ?, ?, ?, ?)}"

    /**
     * SQL call to save double values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveDouble() = "{call jds_pop_double(?, ?, ?, ?)}"

    /**
     * SQL call to save double values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveDoubleCollection() = "{call jds_pop_double_col(?, ?, ?, ?, ?)}"

    /**
     * SQL call to save float values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveFloat() = "{call jds_pop_float(?, ?, ?, ?)}"

    /**
     * SQL call to save float values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveFloatCollection() = "{call jds_pop_float_col(?, ?, ?, ?, ?)}"

    /**
     * SQL call to save integer values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveInteger() = "{call jds_pop_integer(?, ?, ?, ?)}"

    /**
     * SQL call to save integer values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveIntegerCollection() = "{call jds_pop_integer_col(?, ?, ?, ?, ?)}"

    /**
     * SQL call to save long values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveLong() = "{call jds_pop_long(?, ?, ?, ?)}"

    /**
     * SQL call to save long values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveLongCollection() = "{call jds_pop_long_col(?, ?, ?, ?, ?)}"

    /**
     * SQL call to save text values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveString() = "{call jds_pop_text(?, ?, ?, ?)}"

    /**
     * SQL call to save text values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveStringCollection() = "{call jds_pop_text_col(?, ?, ?, ?, ?)}"

    /**
     * SQL call to save time values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveTime() = "{call jds_pop_time(?, ?, ?, ?)}"

    /**
     * SQL call to save datetime values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveZonedDateTime() = "{call jds_pop_zoned_date_time(?, ?, ?, ?)}"

    /**
     * SQL call to save enum values as ordinal int values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveEnum() = "{call jds_pop_enum(?, ?, ?, ?)}"

    /**
     * QL call to save enum values as string values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveEnumString() = "{call jds_pop_enum_string(?, ?, ?, ?)}"

    /**
     * SQL call to save date values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveDate() = "{call jds_pop_date(?, ?, ?, ?)}"

    /**
     * SQL call to save enum collections
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveEnumCollections() = "{call jds_pop_enum_col(?, ?, ?, ?)}"

    /**
     * SQL call to save enum string collections
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveEnumStringCollections() = "{call jds_pop_enum_string_col(?, ?, ?, ?)}"

    /**
     * SQL call to save date time collections
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveDateTimeCollections() = "{call jds_pop_date_time_col(?, ?, ?, ?)}"

    /**
     * SQL call to save float collections
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveFloatCollections() = "{call jds_pop_float_col(?, ?, ?, ?)}"

    /**
     * SQL call to save integer collections
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveIntegerCollections() = "{call jds_pop_integer_col(?, ?, ?, ?)}"

    /**
     * SQL call to save double collections
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveDoubleCollections() = "{call jds_pop_double_col(?, ?, ?, ?)}"

    /**
     * SQL call to save long collections
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveLongCollections() = "{call jds_pop_long_col(?, ?, ?, ?)}"

    /**
     * SQL call to save string collections
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveStringCollections() = "{call jds_pop_text_col(?, ?, ?, ?)}"

    /**
     * SQL call to save entity overview values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveOverview() = "{call jds_pop_entity_overview(?, ?, ?)}"

    /**
     * SQL call to save entity overview values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveEntityBindings() = "{call jds_pop_entity_binding(?, ?, ?, ?, ?)}"

    /**
     * SQL call to bind fieldIds to entityVersions
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateRefEntityField() = "{call jds_pop_ref_entity_field(?, ?)}"

    /**
     * SQL call to map field names and descriptions
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateRefField() = "{call jds_pop_ref_field(?, ?, ?, ?)}"

    /**
     * SQL call to bind enumProperties to entityVersions
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateRefEntityEnum() = "{call jds_pop_ref_entity_enum(?,?)}"

    /**
     * SQL call to map parents to child entities
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun mapParentToChild() = "{call jds_pop_ref_entity_inheritance(?,?)}"

    /**
     * SQL call to map class names
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateRefEntity() = "{call jds_pop_ref_entity(?,?,?,?)}"

    /**
     * SQL call to save reference enum values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateRefEnum() = "{call jds_pop_ref_enum(?,?,?)}"

    /**
     * Variable to facilitate in-line rows in Oracle
     */
    protected val logSqlSource = when (isOracleDb) {
        true -> "SELECT ?, ?, ?, ? FROM DUAL"
        else -> "SELECT ?, ?, ?, ?"
    }

    /**
     * Variable to facilitate lookups for strings in Oracle
     */
    protected val oldStringValue = when (isOracleDb) {
        true -> "dbms_lob.substr(string_value, dbms_lob.getlength(string_value), 1)"
        else -> "string_value"
    }

    /**
     * Acquire a custom connection to a database
     * @param targetDataSource a custom flag to access a particular connection
     * @throws ClassNotFoundException when JDBC driver is not configured correctly
     * @throws SQLException when a standard SQL Exception occurs
     * @return a custom connection to a database
     */
    @Throws(ClassNotFoundException::class, SQLException::class)
    override fun getDataSource(targetDataSource: Int): DataSource {
        return dataSource
    }

    /**
     * @param fieldId
     */
    internal fun typeOfField(fieldId: Long): JdsFieldType = when (JdsField.values.containsKey(fieldId)) {
        true -> JdsField.values[fieldId]!!.type
        false -> JdsFieldType.UNKNOWN
    }

    private val storeCommonColumns: LinkedHashMap<String, String>
        get() {
            val columns = LinkedHashMap<String, String>()
            columns["uuid"] = getDataType(JdsFieldType.STRING, 36)
            columns["edit_version"] = getDataType(JdsFieldType.INT)
            columns["field_id"] = getDataType(JdsFieldType.LONG)
            return columns
        }

    private fun createPopJdsEntityBinding(): String {
        val uniqueColumns = setOf("parent_uuid", "parent_edit_version", "child_uuid", "child_edit_version")
        val columns = LinkedHashMap<String, String>()
        columns["parent_uuid"] = getDataType(JdsFieldType.STRING, 36)
        columns["parent_edit_version"] = getDataType(JdsFieldType.INT)
        columns["child_uuid"] = getDataType(JdsFieldType.STRING, 36)
        columns["child_edit_version"] = getDataType(JdsFieldType.INT)
        columns["child_attribute_id"] = getDataType(JdsFieldType.LONG)
        return createOrAlterProc("jds_pop_entity_binding", "jds_entity_binding", columns, uniqueColumns, false)
    }

    private fun createPopJdsEntityOverview(): String {
        val uniqueColumns = setOf("uuid", "edit_version")
        val columns = LinkedHashMap<String, String>()
        columns["uuid"] = getDataType(JdsFieldType.STRING, 36)
        columns["edit_version"] = getDataType(JdsFieldType.INT)
        columns["entity_id"] = getDataType(JdsFieldType.LONG)
        return createOrAlterProc("jds_pop_entity_overview", "jds_entity_overview", columns, uniqueColumns, false)
    }

    private fun createPopJdsRefEntity(): String {
        val uniqueColumns = setOf("id")
        val columns = LinkedHashMap<String, String>()
        columns["id"] = getDataType(JdsFieldType.LONG)
        columns["name"] = getDataType(JdsFieldType.STRING, 64)
        columns["caption"] = getDataType(JdsFieldType.STRING, 64)
        columns["description"] = getDataType(JdsFieldType.STRING, 256)
        return createOrAlterProc("jds_pop_ref_entity", "jds_ref_entity", columns, uniqueColumns, false)
    }

    private fun createPopJdsRefEntityEnum(): String {
        val uniqueColumns = setOf("entity_id", "field_id")
        val columns = LinkedHashMap<String, String>()
        columns["entity_id"] = getDataType(JdsFieldType.LONG)
        columns["field_id"] = getDataType(JdsFieldType.LONG)
        return createOrAlterProc("jds_pop_ref_entity_enum", "jds_ref_entity_enum", columns, uniqueColumns, true)
    }

    private fun createPopJdsRefEntityField(): String {
        val uniqueColumns = setOf("entity_id", "field_id")
        val columns = LinkedHashMap<String, String>()
        columns["entity_id"] = getDataType(JdsFieldType.LONG)
        columns["field_id"] = getDataType(JdsFieldType.LONG)
        return createOrAlterProc("jds_pop_ref_entity_field", "jds_ref_entity_field", columns, uniqueColumns, true)
    }

    private fun createPopJdsRefEntityInheritance(): String {
        val uniqueColumns = setOf("parent_entity_id", "child_entity_id")
        val columns = LinkedHashMap<String, String>()
        columns["parent_entity_id"] = getDataType(JdsFieldType.LONG)
        columns["child_entity_id"] = getDataType(JdsFieldType.LONG)
        return createOrAlterProc("jds_pop_ref_entity_inheritance", "jds_ref_entity_inheritance", columns, uniqueColumns, true)
    }

    private fun createPopJdsRefEnum(): String {
        val uniqueColumns = setOf("field_id", "seq")
        val columns = LinkedHashMap<String, String>()
        columns["field_id"] = getDataType(JdsFieldType.LONG)
        columns["seq"] = getDataType(JdsFieldType.INT)
        columns["caption"] = getDataType(JdsFieldType.STRING, 64)
        return createOrAlterProc("jds_pop_ref_enum", "jds_ref_enum", columns, uniqueColumns, false)
    }

    private fun createPopJdsRefField(): String {
        val uniqueColumns = setOf("id")
        val columns = LinkedHashMap<String, String>()
        columns["id"] = getDataType(JdsFieldType.LONG)
        columns["caption"] = getDataType(JdsFieldType.STRING, 64)
        columns["description"] = getDataType(JdsFieldType.STRING, 256)
        columns["type_ordinal"] = getDataType(JdsFieldType.INT)
        return createOrAlterProc("jds_pop_ref_field", "jds_ref_field", columns, uniqueColumns, false)
    }

    private fun createPopJdsStoreBlob(): String {
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.BLOB)
        return createOrAlterProc("jds_pop_blob", "jds_str_blob", columns, storeUniqueColumns, false)
    }

    private fun createPopJdsStoreBoolean(): String {
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.BOOLEAN)
        return createOrAlterProc("jds_pop_boolean", "jds_str_boolean", columns, storeUniqueColumns, false)
    }

    private fun createPopJdsStoreDateTime(): String {
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.DATE_TIME)
        return createOrAlterProc("jds_pop_date_time", "jds_str_date_time", columns, storeUniqueColumns, false)
    }

    private fun createPopDateTimeCollection(): String {
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.DATE_TIME)
        return createOrAlterProc("jds_pop_date_time_col", "jds_str_date_time_col", columns, storeUniqueColumns, false)
    }

    private fun createPopJdsStoreDouble(): String {
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.DOUBLE)
        return createOrAlterProc("jds_pop_double", "jds_str_double", columns, storeUniqueColumns, false)
    }

    private fun createPopDoubleCollection(): String {
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.DOUBLE)
        return createOrAlterProc("jds_pop_double_col", "jds_str_double_col", columns, storeUniqueColumns, false)
    }

    private fun createPopJdsStoreFloat(): String {
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.FLOAT)
        return createOrAlterProc("jds_pop_float", "jds_str_float", columns, storeUniqueColumns, false)
    }

    private fun createPopFloatCollection(): String {
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.FLOAT)
        return createOrAlterProc("jds_pop_float_col", "jds_str_float_col", columns, storeUniqueColumns, false)
    }

    private fun createPopJdsStoreInteger(): String {
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.INT)
        return createOrAlterProc("jds_pop_integer", "jds_str_integer", columns, storeUniqueColumns, false)
    }

    private fun createPopIntegerCollection(): String {
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.INT)
        return createOrAlterProc("jds_pop_integer_col", "jds_str_integer_col", columns, storeUniqueColumns, false)
    }

    private fun createPopJdsStoreLong(): String {
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.LONG)
        return createOrAlterProc("jds_pop_long", "jds_str_long", columns, storeUniqueColumns, false)
    }

    private fun createPopLongCollection(): String {
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.LONG)
        return createOrAlterProc("jds_pop_long_col", "jds_str_long_col", columns, storeUniqueColumns, false)
    }

    private fun createPopJdsStoreText(): String {
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.STRING)
        return createOrAlterProc("jds_pop_text", "jds_str_text", columns, storeUniqueColumns, false)
    }

    private fun createPopTextCollection(): String {
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.STRING)
        return createOrAlterProc("jds_pop_text_col", "jds_str_text_col", columns, storeUniqueColumns, false)
    }

    private fun createPopJdsStoreEnum(): String {
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.INT)
        return createOrAlterProc("jds_pop_enum", "jds_str_enum", columns, storeUniqueColumns, false)
    }

    private fun createPopJdsStoreEnumString(): String {
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.STRING)
        return createOrAlterProc("jds_pop_enum_string", "jds_str_enum_string", columns, storeUniqueColumns, false)
    }

    private fun createPopEnumCollection(): String {
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.INT)
        return createOrAlterProc("jds_pop_enum_col", "jds_str_enum_col", columns, storeUniqueColumns, false)
    }

    private fun createPopEnumStringCollection(): String {
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.STRING)
        return createOrAlterProc("jds_pop_enum_string_col", "jds_str_enum_string_col", columns, storeUniqueColumns, false)
    }

    private fun createPopJdsStoreTime(): String {
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.TIME)
        return createOrAlterProc("jds_pop_time", "jds_str_time", columns, storeUniqueColumns, false)
    }

    private fun createPopJdsStoreDate(): String {
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.DATE)
        return createOrAlterProc("jds_pop_date", "jds_str_date", columns, storeUniqueColumns, false)
    }

    private fun createPopJdsStoreDuration(): String {
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.LONG)
        return createOrAlterProc("jds_pop_duration", "jds_str_duration", columns, storeUniqueColumns, false)
    }

    private fun createPopJdsStorePeriod(): String {
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.STRING)
        return createOrAlterProc("jds_pop_period", "jds_str_period", columns, storeUniqueColumns, false)
    }

    private fun createPopJdsMonthYear(): String {
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.STRING)
        return createOrAlterProc("jds_pop_month_year", "jds_str_month_year", columns, storeUniqueColumns, false)
    }

    private fun createPopJdsMonthDay(): String {
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.STRING)
        return createOrAlterProc("jds_pop_month_day", "jds_str_month_day", columns, storeUniqueColumns, false)
    }

    private fun createPopJdsYearMonth(): String {
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.STRING)
        return createOrAlterProc("jds_pop_year_month", "jds_str_year_month", columns, storeUniqueColumns, false)
    }

    private fun createPopJdsStoreZonedDateTime(): String {
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.ZONED_DATE_TIME)
        return createOrAlterProc("jds_pop_zoned_date_time", "jds_str_zoned_date_time", columns, storeUniqueColumns, false)
    }

    private fun createPopEntityLiveVersion(): String {
        val columns = LinkedHashMap<String, String>()
        columns["uuid"] = getDataType(JdsFieldType.STRING, 36)
        //don't include edit_version column, a separate SQL statement updates that column
        return createOrAlterProc("jds_pop_entity_live_version", "jds_entity_live_version", columns, setOf("uuid"), false)
    }

    private fun createEntityLiveVersionTable(): String {
        val tableName = "jds_entity_live_version"
        val columns = LinkedHashMap<String, String>()
        columns["uuid"] = getDataType(JdsFieldType.STRING, 36)
        columns["edit_version"] = getDataType(JdsFieldType.INT)
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_u"] = "uuid"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreBlob(): String {
        val tableName = "jds_str_blob"
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.BLOB)
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_u"] = "uuid, edit_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreBoolean(): String {
        val tableName = "jds_str_boolean"
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.BOOLEAN)
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_u"] = "uuid, edit_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreDate(): String {
        val tableName = "jds_str_date"
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.DATE)
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_u"] = "uuid, edit_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreDateTime(): String {
        val tableName = "jds_str_date_time"
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.DATE_TIME)
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_u"] = "uuid, edit_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreDateTimeCollection(): String {
        val tableName = "jds_str_date_time_col"
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.DATE_TIME)
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_u"] = "uuid, edit_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreDouble(): String {
        val tableName = "jds_str_double"
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.DOUBLE)
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_u"] = "uuid, edit_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreDoubleCollection(): String {
        val tableName = "jds_str_double_col"
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.DOUBLE)
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_u"] = "uuid, edit_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreDuration(): String {
        val tableName = "jds_str_duration"
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.LONG)
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_u"] = "uuid, edit_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreEnum(): String {
        val tableName = "jds_str_enum"
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.INT)
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_u"] = "uuid, edit_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreEnumString(): String {
        val tableName = "jds_str_enum_string"
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.STRING)
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_u"] = "uuid, edit_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreEnumCollection(): String {
        val tableName = "jds_str_enum_col"
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.INT)
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_u"] = "uuid, edit_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreEnumStringCollection(): String {
        val tableName = "jds_str_enum_string_col"//Oracle length
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.STRING)
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_u"] = "uuid, edit_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreFloat(): String {
        val tableName = "jds_str_float"
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.FLOAT)
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_u"] = "uuid, edit_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreFloatCollection(): String {
        val tableName = "jds_str_float_col"
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.FLOAT)
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_u"] = "uuid, edit_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreInteger(): String {
        val tableName = "jds_str_integer"
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.INT)
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_u"] = "uuid, edit_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreIntegerCollection(): String {
        val tableName = "jds_str_integer_col"
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.INT)
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_u"] = "uuid, edit_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreLong(): String {
        val tableName = "jds_str_long"
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.LONG)
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_u"] = "uuid, edit_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreLongCollection(): String {
        val tableName = "jds_str_long_col"
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.LONG)
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_u"] = "uuid, edit_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreMonthDay(): String {
        val tableName = "jds_str_month_day"
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.STRING)
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_u"] = "uuid, edit_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStorePeriod(): String {
        val tableName = "jds_str_period"
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.STRING)
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_u"] = "uuid, edit_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreText(): String {
        val tableName = "jds_str_text"
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.STRING)
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_u"] = "uuid, edit_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreTextCollection(): String {
        val tableName = "jds_str_text_col"
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.STRING)
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_u"] = "uuid, edit_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreTime(): String {
        val tableName = "jds_str_time"
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.TIME)
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_u"] = "uuid, edit_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreYearMonth(): String {
        val tableName = "jds_str_year_month"
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.STRING)
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_u"] = "uuid, edit_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreZonedDateTime(): String {
        val tableName = "jds_str_zoned_date_time"
        val columns = storeCommonColumns
        columns["value"] = getDataType(JdsFieldType.ZONED_DATE_TIME)
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_u"] = "uuid, edit_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    fun deleteOldDataFromReportTables(connection: Connection) {
        tables.forEach {
            if (it.isStoringLiveRecordsOnly) {
                connection.prepareStatement(it.deleteOldRecords(this)).use { statement ->
                    statement.executeUpdate()
                }
            }
        }
    }

    override fun getDataType(fieldType: JdsFieldType) = getDataType(fieldType, 0)

    override fun getDataType(fieldType: JdsFieldType, max: Int) = getDataTypeImpl(fieldType, max)

    /**
     * Gets the underlying database type of the supplied [io.github.subiyacryolite.jds.JdsField]
     * @param fieldType the supplied [io.github.subiyacryolite.jds.JdsField]
     * @param max the maximum length of the database type, applied against [io.github.subiyacryolite.jds.enums.JdsFieldType.STRING] and [io.github.subiyacryolite.jds.enums.JdsFieldType.BLOB] types
     * @return the underlying database type of the supplied [io.github.subiyacryolite.jds.JdsField]
     */
    protected abstract fun getDataTypeImpl(fieldType: JdsFieldType, max: Int = 0): String

    companion object {

        private val storeUniqueColumns = setOf("uuid", "edit_version", "field_id")
    }
}
