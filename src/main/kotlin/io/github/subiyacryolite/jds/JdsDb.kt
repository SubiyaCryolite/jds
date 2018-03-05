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

import com.javaworld.NamedPreparedStatement
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
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap

/**
 * This class is responsible for the setup of SQL connections, default database
 * write statements, as well as the initialization of core and custom components
 * that will support JDS on the underlying Database implementation
 * @param implementation
 * @param supportsStatements
 */
abstract class JdsDb(var implementation: JdsImplementation, var supportsStatements: Boolean) : IJdsDb, Serializable {

    val classes = ConcurrentHashMap<Long, Class<out JdsEntity>>()
    val tables = HashSet<JdsTable>()
    val options = JdsOptions()

    /**
     * Initialise JDS base tables
     */
    fun init() {
        try {
            getConnection().use { connection ->
                prepareDatabaseComponents(connection)
                prepareCustomDatabaseComponents(connection)
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
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_ENTITY_OVERVIEW)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_ENTITY_BINDING)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.BIND_ENTITY_FIELDS)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.BIND_ENTITY_ENUMS)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_TEXT)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_TEXT_COLLECTION)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_BLOB)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_ENUM)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_ENUM_COLLECTION)
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
    internal fun prepareDatabaseComponent(connection: Connection, databaseComponent: JdsComponentType, jdsComponent: JdsComponent) {
        when (databaseComponent) {
            JdsComponentType.TABLE -> if (!doesTableExist(connection, jdsComponent.component)) {
                initiateDatabaseComponent(connection, jdsComponent)
            }
            JdsComponentType.STORED_PROCEDURE -> if (!doesProcedureExist(connection, jdsComponent.component)) {
                initiateDatabaseComponent(connection, jdsComponent)
            }
            JdsComponentType.TRIGGER -> if (!doesTriggerExist(connection, jdsComponent.component)) {
                initiateDatabaseComponent(connection, jdsComponent)
            }
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
            JdsComponent.STORE_BOOLEAN -> executeSqlFromString(connection, createStoreBoolean())
            JdsComponent.STORE_BLOB -> executeSqlFromString(connection, createStoreBlob())
            JdsComponent.STORE_TEXT -> executeSqlFromString(connection, createStoreText())
            JdsComponent.STORE_TEXT_COLLECTION -> executeSqlFromString(connection, createStoreTextCollection())
            JdsComponent.STORE_PERIOD -> executeSqlFromString(connection, createStorePeriod())
            JdsComponent.STORE_DURATION -> executeSqlFromString(connection, createStoreDuration())
            JdsComponent.STORE_YEAR_MONTH -> executeSqlFromString(connection, createStoreYearMonth())
            JdsComponent.STORE_MONTH_DAY -> executeSqlFromString(connection, createStoreMonthDay())
            JdsComponent.STORE_ENUM -> executeSqlFromString(connection, createStoreEnum())
            JdsComponent.STORE_ENUM_COLLECTION -> executeSqlFromString(connection, createStoreEnumCollection())
            JdsComponent.STORE_FLOAT -> executeSqlFromString(connection, createStoreFloat())
            JdsComponent.STORE_FLOAT -> executeSqlFromString(connection, createStoreFloatCollection())
            JdsComponent.STORE_INTEGER -> executeSqlFromString(connection, createStoreInteger())
            JdsComponent.STORE_INTEGER_COLLECTION -> executeSqlFromString(connection, createStoreIntegerCollection())
            JdsComponent.STORE_LONG -> executeSqlFromString(connection, createStoreLong())
            JdsComponent.STORE_LONG_COLLECTION -> executeSqlFromString(connection, createStoreLongCollection())
            JdsComponent.STORE_DOUBLE -> executeSqlFromString(connection, createStoreDouble())
            JdsComponent.STORE_DOUBLE -> executeSqlFromString(connection, createStoreDoubleCollection())
            JdsComponent.STORE_DATE -> executeSqlFromString(connection, createStoreDate())
            JdsComponent.STORE_DATE_TIME -> executeSqlFromString(connection, createStoreDateTime())
            JdsComponent.STORE_DATE_TIME_COLLECTION -> executeSqlFromString(connection, createStoreDateTimeCollection())
            JdsComponent.STORE_ZONED_DATE_TIME -> executeSqlFromString(connection, createStoreZonedDateTime())
            JdsComponent.STORE_TIME -> executeSqlFromString(connection, createStoreTime())
            JdsComponent.REF_FIELDS -> createRefFields(connection)
            JdsComponent.REF_ENTITIES -> createStoreEntities(connection)
            JdsComponent.REF_ENUM_VALUES -> createRefEnumValues(connection)
            JdsComponent.REF_INHERITANCE -> createRefInheritance(connection)
            JdsComponent.BIND_ENTITY_FIELDS -> createBindEntityFields(connection)
            JdsComponent.BIND_ENTITY_ENUMS -> createBindEntityEnums(connection)
            JdsComponent.STORE_ENTITY_BINDING -> createBindEntityBinding(connection)
        //====================================================================================
            JdsComponent.STORE_ENTITY_OVERVIEW -> {
                createRefEntityOverview(connection)
                createIndexes(connection)
            }
            JdsComponent.REF_FIELD_TYPES -> {
                createRefFieldTypes(connection)
                populateFieldTypes(connection)
            }
            else -> {
            }
        }
        prepareCustomDatabaseComponents(connection, jdsComponent)
    }

    fun createIndexes(connection: Connection) = try {
        //executeSqlFromString(connection, getDbCreateIndexSyntax("jds_entity_overview", "uuid, uuid_location, uuid_version", "jds_entity_overview_ix_uuid"))
        //executeSqlFromString(connection, getDbCreateIndexSyntax("jds_entity_overview", "parent_composite_key", "jds_entity_overview_ix_parent_composite_key"))
        //executeSqlFromString(connection, getDbCreateIndexSyntax("jds_entity_overview", "parent_uuid", "jds_entity_overview_ix_parent_uuid"))
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    fun dropIndexes(connection: Connection) = try {
        //connection.prepareStatement("DROP INDEX jds_entity_overview_ix_uuid").use { it.executeUpdate() }
        //connection.prepareStatement("DROP INDEX jds_entity_overview_ix_parent_composite_key").use { it.executeUpdate() }
        //connection.prepareStatement("DROP INDEX jds_entity_overview_ix_parent_uuid").use { it.executeUpdate() }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * Initialises custom JDS Database components
     *
     * @param connection   The SQL connection to use
     * @param jdsComponent an enum that maps to the components concrete
     * implementation details
     */
    internal open fun prepareCustomDatabaseComponents(connection: Connection, jdsComponent: JdsComponent) {}

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

    override fun doesIndexExist(connection: Connection, indexName: String): Boolean {
        val answer = indexExists(connection, indexName)
        return answer == 1
    }

    override fun doesColumnExist(connection: Connection, tableName: String, columnName: String): Boolean {
        val answer = columnExists(connection, tableName, columnName)
        return answer == 1
    }

    internal fun columnExistsCommonImpl(connection: Connection, tableName: String, columnName: String, toReturn: Int, sql: String): Int {
        var toReturn = toReturn
        try {
            NamedPreparedStatement(connection, sql).use {
                it.setString("tableName", tableName)
                it.setString("columnName", columnName)
                it.setString("tableCatalog", connection.catalog)
                it.executeQuery().use {
                    while (it.next()) {
                        toReturn = it.getInt("Result")
                    }
                }
            }
        } catch (ex: Exception) {
            toReturn = 0
            ex.printStackTrace(System.err)
        }
        return toReturn
    }

    /**
     * Executes SQL found in the specified file. We recommend having one
     * statement per file.
     *
     * @param fileName the file containing SQL to find
     */
    internal fun executeSqlFromFile(connection: Connection, fileName: String) {
        try {
            Thread.currentThread().contextClassLoader.getResourceAsStream(fileName).use {
                val innerSql = fileToString(it)
                executeSqlFromString(connection, innerSql)
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    @JvmOverloads
    fun executeSqlFromString(connection: Connection, sql: String, update: Boolean = false) {
        try {
            connection.prepareStatement(sql).use { statement ->
                when (update) {
                    true -> statement.executeUpdate()
                    false -> statement.execute()
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
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
     * Override this method with custom implementations of [prepareDatabaseComponents][.prepareDatabaseComponent]
     * [prepareDatabaseComponents][.prepareDatabaseComponent]
     * delegates the creation of custom database components depending on the
     * underlying JDS Database implementation
     *
     * @param connection the SQL connection to use
     */
    internal open fun prepareCustomDatabaseComponents(connection: Connection) {}

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
                         columns: java.util.LinkedHashMap<String, String>,
                         uniqueColumns: java.util.LinkedHashMap<String, String>,
                         primaryKeys: java.util.LinkedHashMap<String, String>,
                         foreignKeys: java.util.LinkedHashMap<String, java.util.LinkedHashMap<String, String>>): String {
        val sqlBuilder = StringBuilder()
        sqlBuilder.append("CREATE TABLE $tableName(\n")

        val endingComponents = StringJoiner(",\n\t")
        sqlBuilder.append("\t")
        val columnJoiner = StringJoiner(",\n\t")
        columns.forEach { column, type ->
            columnJoiner.add("$column $type")
        }
        endingComponents.add(columnJoiner.toString())

        if (!uniqueColumns.isEmpty()) {
            uniqueColumns.forEach { constraintName, constrainColumns ->
                endingComponents.add("CONSTRAINT $constraintName UNIQUE ($constrainColumns)")
            }
        }

        if (!foreignKeys.isEmpty()) {
            foreignKeys.forEach { constraintName, localColumnsRemoteTableColumns ->
                localColumnsRemoteTableColumns.forEach { localColumns, remoteTableColumns ->
                    endingComponents.add("CONSTRAINT $constraintName FOREIGN KEY ($localColumns) REFERENCES $remoteTableColumns ON DELETE CASCADE")
                }
            }
        }

        if (!primaryKeys.isEmpty()) {
            primaryKeys.forEach { constraintName, constraintColumns ->
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
    open fun procedureExists(connection: Connection, procedureName: String): Int {
        return 0
    }

    /**
     * Internal checks to see if the specified view exists in the database
     *
     * @param connection the connection to use
     * @param viewName the view to look up
     * @return 1 if the specified procedure exists in the database
     */
    open fun viewExists(connection: Connection, viewName: String): Int {
        return 0
    }

    /**
     * Internal checks to see if the specified trigger exists in the database
     *
     * @param connection the connection to use
     * @param triggerName the trigger to look up
     * @return 1 if the specified trigger exists in the database
     */
    open fun triggerExists(connection: Connection, triggerName: String): Int {
        return 0
    }

    /**
     * Internal checks to see if the specified index exists in the database
     *
     * @param connection the connection to use
     * @param indexName the trigger to look up
     * @return 1 if the specified index exists in the database
     */
    fun indexExists(connection: Connection, indexName: String): Int {
        return 0
    }

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
     * @param parent
     */
    private fun populateRefEntity(connection: Connection, id: Long, name: String, caption: String, description: String) = try {
        (if (supportsStatements) connection.prepareCall(populateRefEntity()) else connection.prepareStatement(populateRefEntity())).use { statement ->
            statement.setLong(1, id)
            statement.setString(2, name)
            statement.setString(3, caption)
            statement.setString(4, description)
            statement.executeUpdate()
            if (options.isPrintingOutput)
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
        //pool connections so that there's no wastage
        val connectionPool = HashMap<Int, Connection>()
        tables.forEach { it.forceGenerateOrUpdateSchema(this, connectionPool) }
        connectionPool.forEach { it.value.close() }
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
                    getConnection().use { connection ->
                        connection.autoCommit = false
                        val parentEntities = ArrayList<Long>()
                        var jdsEntity = entity.newInstance()
                        parentEntities.add(jdsEntity.overview.entityId)//add this own entity to the chain
                        JdsExtensions.determineParents(entity, parentEntities)
                        populateRefEntity(connection, jdsEntity.overview.entityId, entityAnnotation.name, entityAnnotation.caption, entityAnnotation.description)
                        jdsEntity.populateRefFieldRefEntityField(this, connection, jdsEntity.overview.entityId)
                        jdsEntity.populateRefEnumRefEntityEnum(this, connection, jdsEntity.overview.entityId)
                        mapParentEntities(connection, parentEntities, jdsEntity.overview.entityId)
                        connection.commit()
                        if (options.isPrintingOutput)
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


    internal open fun saveMonthDay() = "{call jds_pop_store_month_day(?, ?, ?, ?, ?)}"

    internal open fun saveYearMonth() = "{call jds_pop_store_year_month(?, ?, ?, ?, ?)}"


    internal open fun savePeriod() = "{call jds_pop_store_period(?, ?, ?, ?, ?)}"

    internal open fun saveDuration() = "{call jds_pop_store_duration(?, ?, ?, ?, ?)}"

    /**
     * SQL call to save blob values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveBlob() = "{call jds_pop_store_blob(?, ?, ?, ?, ?)}"

    /**
     * SQL call to save boolean values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveBoolean() = "{call jds_pop_store_boolean(?, ?, ?, ?, ?)}"

    /**
     * SQL call to save datetime values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveDateTime() = "{call jds_pop_store_date_time(?, ?, ?, ?, ?)}"

    /**
     * SQL call to save datetime values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveDateTimeCollection() = "{call jds_pop_store_date_time_collection(?, ?, ?, ?, ?, ?)}"

    /**
     * SQL call to save double values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveDouble() = "{call jds_pop_store_double(?, ?, ?, ?, ?)}"

    /**
     * SQL call to save double values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveDoubleCollection() = "{call jds_pop_store_double_collection(?, ?, ?, ?, ?, ?)}"

    /**
     * SQL call to save float values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveFloat() = "{call jds_pop_store_float(?, ?, ?, ?, ?)}"

    /**
     * SQL call to save float values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveFloatCollection() = "{call jds_pop_store_float_collection(?, ?, ?, ?, ?, ?)}"

    /**
     * SQL call to save integer values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveInteger() = "{call jds_pop_store_integer(?, ?, ?, ?, ?)}"

    /**
     * SQL call to save integer values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveIntegerCollection() = "{call jds_pop_store_integer_collection(?, ?, ?, ?, ?, ?)}"

    /**
     * SQL call to save long values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveLong() = "{call jds_pop_store_long(?, ?, ?, ?, ?)}"

    /**
     * SQL call to save long values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveLongCollection() = "{call jds_pop_store_long_collection(?, ?, ?, ?, ?, ?)}"

    /**
     * SQL call to save text values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveString() = "{call jds_pop_store_text(?, ?, ?, ?, ?)}"

    /**
     * SQL call to save text values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveStringCollection() = "{call jds_pop_store_text_collection(?, ?, ?, ?, ?, ?)}"

    /**
     * SQL call to save time values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveTime() = "{call jds_pop_store_time(?, ?, ?, ?, ?)}"

    /**
     * SQL call to save datetime values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveZonedDateTime() = "{call jds_pop_store_zoned_date_time(?, ?, ?, ?, ?)}"

    /**
     * SQL call to save datetime values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveEnum() = "{call jds_pop_store_enum(?, ?, ?, ?, ?)}"

    /**
     * SQL call to save datetime values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveEnumCollection() = "{call jds_pop_store_enum_collection(?, ?, ?, ?, ?, ?)}"

    /**
     * SQL call to save date values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveDate() = "{call jds_pop_store_date(?, ?, ?, ?, ?, ?)}"

    /**
     * SQL call to save entity overview values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveOverview() = "{call jds_pop_entity_overview(?, ?, ?, ?, ?, ?, ?)}"

    /**
     * SQL call to save entity overview values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveEntityBindings() = "{call jds_pop_entity_binding(?, ?, ?, ?, ?, ?, ?)}"

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
     * @param targetConnection a custom flag to access a particular connection
     * @throws ClassNotFoundException when JDBC driver is not configured correctly
     * @throws SQLException when a standard SQL Exception occurs
     * @return a custom connection to a database
     */
    @Throws(ClassNotFoundException::class, SQLException::class)
    override fun getConnection(targetConnection: Int): Connection {
        return getConnection()
    }

    /**
     * @param fieldId
     */
    internal fun typeOfField(fieldId: Long): JdsFieldType = when (JdsField.values.containsKey(fieldId)) {
        true -> JdsField.values[fieldId]!!.type
        false -> JdsFieldType.UNKNOWN
    }

    protected fun createPopJdsEntityBinding(): String {
        val uniqueColumns = setOf("parent_uuid", "parent_uuid_location", "parent_uuid_version", "child_uuid", "child_uuid_location", "child_uuid_version")
        val columns = LinkedHashMap<String, String>()
        columns["parent_uuid"] = getNativeDataTypeString(64)
        columns["parent_uuid_location"] = getNativeDataTypeString(45)
        columns["parent_uuid_version"] = getNativeDataTypeInteger()
        columns["child_uuid"] = getNativeDataTypeString(64)
        columns["child_uuid_location"] = getNativeDataTypeString(45)
        columns["child_uuid_version"] = getNativeDataTypeInteger()
        columns["child_attribute_id"] = getNativeDataTypeLong()
        return createOrAlterProc("jds_pop_entity_binding", "jds_entity_binding", columns, uniqueColumns, false)
    }

    protected fun createPopJdsEntityOverview(): String {
        val uniqueColumns = setOf("uuid", "uuid_location", "uuid_version")
        val columns = LinkedHashMap<String, String>()
        columns["uuid"] = getNativeDataTypeString(64)
        columns["uuid_location"] = getNativeDataTypeString(45)
        columns["uuid_version"] = getNativeDataTypeInteger()
        columns["entity_id"] = getNativeDataTypeLong()
        columns["entity_version"] = getNativeDataTypeLong()
        columns["live"] = getNativeDataTypeBoolean()
        columns["last_edit"] = getNativeDataTypeDateTime()
        return createOrAlterProc("jds_pop_entity_overview", "jds_entity_overview", columns, uniqueColumns, false)
    }

    protected fun createPopJdsRefEntity(): String {
        val uniqueColumns = setOf("id")
        val columns = LinkedHashMap<String, String>()
        columns["id"] = getNativeDataTypeLong()
        columns["name"] = getNativeDataTypeString(256)
        columns["caption"] = getNativeDataTypeString(256)
        columns["description"] = getNativeDataTypeString(256)
        return createOrAlterProc("jds_pop_ref_entity", "jds_ref_entity", columns, uniqueColumns, false)
    }

    protected fun createPopJdsRefEntityEnum(): String {
        val uniqueColumns = setOf("entity_id", "field_id")
        val columns = LinkedHashMap<String, String>()
        columns["entity_id"] = getNativeDataTypeLong()
        columns["field_id"] = getNativeDataTypeLong()
        return createOrAlterProc("jds_pop_ref_entity_enum", "jds_ref_entity_enum", columns, uniqueColumns, true)
    }

    protected fun createPopJdsRefEntityField(): String {
        val uniqueColumns = setOf("entity_id", "field_id")
        val columns = LinkedHashMap<String, String>()
        columns["entity_id"] = getNativeDataTypeLong()
        columns["field_id"] = getNativeDataTypeLong()
        return createOrAlterProc("jds_pop_ref_entity_field", "jds_ref_entity_field", columns, uniqueColumns, true)
    }

    protected fun createPopJdsRefEntityInheritance(): String {
        val uniqueColumns = setOf("parent_entity_id", "child_entity_id")
        val columns = LinkedHashMap<String, String>()
        columns["parent_entity_id"] = getNativeDataTypeLong()
        columns["child_entity_id"] = getNativeDataTypeLong()
        return createOrAlterProc("jds_pop_ref_entity_inheritance", "jds_ref_entity_inheritance", columns, uniqueColumns, true)
    }

    protected fun createPopJdsRefEnum(): String {
        val uniqueColumns = setOf("field_id", "seq")
        val columns = LinkedHashMap<String, String>()
        columns["field_id"] = getNativeDataTypeLong()
        columns["seq"] = getNativeDataTypeInteger()
        columns["caption"] = getNativeDataTypeString(0)
        return createOrAlterProc("jds_pop_ref_enum", "jds_ref_enum", columns, uniqueColumns, false)
    }

    protected fun createPopJdsRefField(): String {
        val uniqueColumns = setOf("id")
        val columns = LinkedHashMap<String, String>()
        columns["id"] = getNativeDataTypeLong()
        columns["caption"] = getNativeDataTypeString(128)
        columns["description"] = getNativeDataTypeString(256)
        columns["type_ordinal"] = getNativeDataTypeInteger()
        return createOrAlterProc("jds_pop_ref_field", "jds_ref_field", columns, uniqueColumns, false)
    }

    private val storeUniqueColumns = setOf("uuid", "uuid_location", "uuid_version", "field_id")
    private val storeUniqueColumnsInclSequence = setOf("uuid", "uuid_location", "uuid_version", "field_id", "sequence")

    private val storeCommonColumns: LinkedHashMap<String, String>
        get() {
            val columns = LinkedHashMap<String, String>()
            columns["uuid"] = getNativeDataTypeString(64)
            columns["uuid_location"] = getNativeDataTypeString(45)
            columns["uuid_version"] = getNativeDataTypeInteger()
            columns["field_id"] = getNativeDataTypeLong()
            return columns
        }

    private val storeCommonColumnsInclSequence: LinkedHashMap<String, String>
        get() {
            val columns = storeCommonColumns
            columns["sequence"] = getNativeDataTypeInteger()
            return columns
        }

    protected fun createPopJdsStoreBlob(): String {
        val columns = storeCommonColumns
        columns["value"] = getNativeDataTypeBlob(0)
        return createOrAlterProc("jds_pop_store_blob", "jds_store_blob", columns, storeUniqueColumns, false)
    }

    protected fun createPopJdsStoreBoolean(): String {
        val columns = storeCommonColumns
        columns["value"] = getNativeDataTypeBoolean()
        return createOrAlterProc("jds_pop_store_boolean", "jds_store_boolean", columns, storeUniqueColumns, false)
    }

    protected fun createPopJdsStoreDateTime(): String {
        val columns = storeCommonColumns
        columns["value"] = getNativeDataTypeDateTime()
        return createOrAlterProc("jds_pop_store_date_time", "jds_store_date_time", columns, storeUniqueColumns, false)
    }

    protected fun createPopJdsStoreDateTimeCollection(): String {
        val columns = storeCommonColumnsInclSequence
        columns["value"] = getNativeDataTypeDateTime()
        return createOrAlterProc("jds_pop_store_date_time_collection", "jds_store_date_time_collection", columns, storeUniqueColumnsInclSequence, false)
    }

    protected fun createPopJdsStoreDouble(): String {
        val columns = storeCommonColumns
        columns["value"] = getNativeDataTypeDouble()
        return createOrAlterProc("jds_pop_store_double", "jds_store_double", columns, storeUniqueColumns, false)
    }

    protected fun createPopJdsStoreDoubleCollection(): String {
        val columns = storeCommonColumnsInclSequence
        columns["value"] = getNativeDataTypeDouble()
        return createOrAlterProc("jds_pop_store_double_collection", "jds_store_double_collection", columns, storeUniqueColumnsInclSequence, false)
    }

    protected fun createPopJdsStoreFloat(): String {
        val columns = storeCommonColumns
        columns["value"] = getNativeDataTypeFloat()
        return createOrAlterProc("jds_pop_store_float", "jds_store_float", columns, storeUniqueColumns, false)
    }

    protected fun createPopJdsStoreFloatCollection(): String {
        val columns = storeCommonColumnsInclSequence
        columns["value"] = getNativeDataTypeFloat()
        return createOrAlterProc("jds_pop_store_float_collection", "jds_store_float_collection", columns, storeUniqueColumnsInclSequence, false)
    }

    protected fun createPopJdsStoreInteger(): String {
        val columns = storeCommonColumns
        columns["value"] = getNativeDataTypeInteger()
        return createOrAlterProc("jds_pop_store_integer", "jds_store_integer", columns, storeUniqueColumns, false)
    }

    protected fun createPopJdsStoreIntegerCollection(): String {
        val columns = storeCommonColumnsInclSequence
        columns["value"] = getNativeDataTypeInteger()
        return createOrAlterProc("jds_pop_store_integer_collection", "jds_store_integer_collection", columns, storeUniqueColumnsInclSequence, false)
    }

    protected fun createPopJdsStoreLong(): String {
        val columns = storeCommonColumns
        columns["value"] = getNativeDataTypeLong()
        return createOrAlterProc("jds_pop_store_long", "jds_store_long", columns, storeUniqueColumns, false)
    }

    protected fun createPopJdsStoreLongCollection(): String {
        val columns = storeCommonColumnsInclSequence
        columns["value"] = getNativeDataTypeLong()
        return createOrAlterProc("jds_pop_store_long_collection", "jds_store_long_collection", columns, storeUniqueColumnsInclSequence, false)
    }

    protected fun createPopJdsStoreText(): String {
        val columns = storeCommonColumns
        columns["value"] = getNativeDataTypeString(0)
        return createOrAlterProc("jds_pop_store_text", "jds_store_text", columns, storeUniqueColumns, false)
    }

    protected fun createPopJdsStoreTextCollection(): String {
        val columns = storeCommonColumnsInclSequence
        columns["value"] = getNativeDataTypeString(0)
        return createOrAlterProc("jds_pop_store_text_collection", "jds_store_text_collection", columns, storeUniqueColumnsInclSequence, false)
    }

    protected fun createPopJdsStoreEnum(): String {
        val columns = storeCommonColumns
        columns["value"] = getNativeDataTypeString(0)
        return createOrAlterProc("jds_pop_store_enum", "jds_store_enum", columns, storeUniqueColumns, false)
    }

    protected fun createPopJdsStoreEnumCollection(): String {
        val columns = storeCommonColumnsInclSequence
        columns["value"] = getNativeDataTypeString(0)
        return createOrAlterProc("jds_pop_store_enum_collection", "jds_store_enum_collection", columns, storeUniqueColumnsInclSequence, false)
    }

    protected fun createPopJdsStoreTime(): String {
        val columns = storeCommonColumns
        columns["value"] = getNativeDataTypeTime()
        return createOrAlterProc("jds_pop_store_time", "jds_store_time", columns, storeUniqueColumns, false)
    }

    protected fun createPopJdsStoreDate(): String {
        val columns = storeCommonColumns
        columns["value"] = getNativeDataTypeTime()
        return createOrAlterProc("jds_pop_store_date", "jds_store_date", columns, storeUniqueColumns, false)
    }

    protected fun createPopJdsStoreDuration(): String {
        val columns = storeCommonColumns
        columns["value"] = getNativeDataTypeTime()
        return createOrAlterProc("jds_pop_store_duration", "jds_store_duration", columns, storeUniqueColumns, false)
    }

    protected fun createPopJdsStorePeriod(): String {
        val columns = storeCommonColumns
        columns["value"] = getNativeDataTypeTime()
        return createOrAlterProc("jds_pop_store_period", "jds_store_period", columns, storeUniqueColumns, false)
    }

    protected fun createPopJdsMonthYear(): String {
        val columns = storeCommonColumns
        columns["value"] = getNativeDataTypeTime()
        return createOrAlterProc("jds_pop_store_month_year", "jds_store_month_year", columns, storeUniqueColumns, false)
    }

    protected fun createPopJdsYearMonth(): String {
        val columns = storeCommonColumns
        columns["value"] = getNativeDataTypeTime()
        return createOrAlterProc("jds_pop_store_year_month", "jds_store_year_month", columns, storeUniqueColumns, false)
    }

    protected fun createPopJdsStoreZonedDateTime(): String {
        val columns = storeCommonColumns
        columns["value"] = getNativeDataTypeZonedDateTime()
        return createOrAlterProc("jds_pop_store_zoned_date_time", "jds_store_zoned_date_time", columns, storeUniqueColumns, false)
    }

    private fun createStoreBlob(): String {
        val tableName = "jds_store_blob"
        val columns = storeCommonColumns
        columns["value"] = getNativeDataTypeBlob(0)
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_uc"] = "uuid, uuid_location, uuid_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_fk"] = linkedMapOf("uuid, uuid_location, uuid_version" to "jds_entity_overview(uuid, uuid_location, uuid_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreBoolean(): String {
        val tableName = "jds_store_boolean"
        val columns = storeCommonColumns
        columns["value"] = getNativeDataTypeBoolean()
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_uc"] = "uuid, uuid_location, uuid_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_fk"] = linkedMapOf("uuid, uuid_location, uuid_version" to "jds_entity_overview(uuid, uuid_location, uuid_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    protected fun createStoreDate(): String {
        val tableName = "jds_store_date"
        val columns = storeCommonColumns
        columns["value"] = getNativeDataTypeDateTime()
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_uc"] = "uuid, uuid_location, uuid_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_fk"] = linkedMapOf("uuid, uuid_location, uuid_version" to "jds_entity_overview(uuid, uuid_location, uuid_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreDateTime(): String {
        val tableName = "jds_store_date_time"
        val columns = storeCommonColumns
        columns["value"] = getNativeDataTypeDateTime()
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_uc"] = "uuid, uuid_location, uuid_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_fk"] = linkedMapOf("uuid, uuid_location, uuid_version" to "jds_entity_overview(uuid, uuid_location, uuid_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreDateTimeCollection(): String {
        val tableName = "jds_store_date_time_collection"
        val columns = storeCommonColumnsInclSequence
        columns["value"] = getNativeDataTypeDateTime()
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_uc"] = "uuid, uuid_location, uuid_version, field_id, sequence"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_fk"] = linkedMapOf("uuid, uuid_location, uuid_version" to "jds_entity_overview(uuid, uuid_location, uuid_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreDouble(): String {
        val tableName = "jds_store_double"
        val columns = storeCommonColumns
        columns["value"] = getNativeDataTypeDouble()
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_uc"] = "uuid, uuid_location, uuid_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_fk"] = linkedMapOf("uuid, uuid_location, uuid_version" to "jds_entity_overview(uuid, uuid_location, uuid_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreDoubleCollection(): String {
        val tableName = "jds_store_double_collection"
        val columns = storeCommonColumnsInclSequence
        columns["value"] = getNativeDataTypeDouble()
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_uc"] = "uuid, uuid_location, uuid_version, field_id, sequence"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_fk"] = linkedMapOf("uuid, uuid_location, uuid_version" to "jds_entity_overview(uuid, uuid_location, uuid_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreDuration(): String {
        val tableName = "jds_store_duration"
        val columns = storeCommonColumns
        columns["value"] = getNativeDataTypeLong()
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_uc"] = "uuid, uuid_location, uuid_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_fk"] = linkedMapOf("uuid, uuid_location, uuid_version" to "jds_entity_overview(uuid, uuid_location, uuid_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreEnum(): String {
        val tableName = "jds_store_enum"
        val columns = storeCommonColumns
        columns["value"] = getNativeDataTypeInteger()
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_uc"] = "uuid, uuid_location, uuid_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_fk"] = linkedMapOf("uuid, uuid_location, uuid_version" to "jds_entity_overview(uuid, uuid_location, uuid_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreEnumCollection(): String {
        val tableName = "jds_store_enum_collection"
        val columns = storeCommonColumnsInclSequence
        columns["value"] = getNativeDataTypeInteger()
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_uc"] = "uuid, uuid_location, uuid_version, field_id, sequence"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_fk"] = linkedMapOf("uuid, uuid_location, uuid_version" to "jds_entity_overview(uuid, uuid_location, uuid_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreFloat(): String {
        val tableName = "jds_store_float"
        val columns = storeCommonColumns
        columns["value"] = getNativeDataTypeFloat()
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_uc"] = "uuid, uuid_location, uuid_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_fk"] = linkedMapOf("uuid, uuid_location, uuid_version" to "jds_entity_overview(uuid, uuid_location, uuid_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreFloatCollection(): String {
        val tableName = "jds_store_float_collection"
        val columns = storeCommonColumnsInclSequence
        columns["value"] = getNativeDataTypeFloat()
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_uc"] = "uuid, uuid_location, uuid_version, field_id, sequence"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_fk"] = linkedMapOf("uuid, uuid_location, uuid_version" to "jds_entity_overview(uuid, uuid_location, uuid_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreInteger(): String {
        val tableName = "jds_store_integer"
        val columns = storeCommonColumns
        columns["value"] = getNativeDataTypeInteger()
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_uc"] = "uuid, uuid_location, uuid_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_fk"] = linkedMapOf("uuid, uuid_location, uuid_version" to "jds_entity_overview(uuid, uuid_location, uuid_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreIntegerCollection(): String {
        val tableName = "jds_store_integer_collection"
        val columns = storeCommonColumnsInclSequence
        columns["value"] = getNativeDataTypeInteger()
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_uc"] = "uuid, uuid_location, uuid_version, field_id, sequence"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_fk"] = linkedMapOf("uuid, uuid_location, uuid_version" to "jds_entity_overview(uuid, uuid_location, uuid_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreLong(): String {
        val tableName = "jds_store_long"
        val columns = storeCommonColumns
        columns["value"] = getNativeDataTypeLong()
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_uc"] = "uuid, uuid_location, uuid_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_fk"] = linkedMapOf("uuid, uuid_location, uuid_version" to "jds_entity_overview(uuid, uuid_location, uuid_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreLongCollection(): String {
        val tableName = "jds_store_long_collection"
        val columns = storeCommonColumnsInclSequence
        columns["value"] = getNativeDataTypeLong()
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_uc"] = "uuid, uuid_location, uuid_version, field_id, sequence"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_fk"] = linkedMapOf("uuid, uuid_location, uuid_version" to "jds_entity_overview(uuid, uuid_location, uuid_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    protected fun createStoreMonthDay(): String {
        val tableName = "jds_store_month_day"
        val columns = storeCommonColumns
        columns["value"] = getNativeDataTypeString(0)
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_uc"] = "uuid, uuid_location, uuid_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_fk"] = linkedMapOf("uuid, uuid_location, uuid_version" to "jds_entity_overview(uuid, uuid_location, uuid_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStorePeriod(): String {
        val tableName = "jds_store_period"
        val columns = storeCommonColumns
        columns["value"] = getNativeDataTypeString(0)
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_uc"] = "uuid, uuid_location, uuid_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_fk"] = linkedMapOf("uuid, uuid_location, uuid_version" to "jds_entity_overview(uuid, uuid_location, uuid_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreText(): String {
        val tableName = "jds_store_text"
        val columns = storeCommonColumns
        columns["value"] = getNativeDataTypeString(0)
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_uc"] = "uuid, uuid_location, uuid_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_fk"] = linkedMapOf("uuid, uuid_location, uuid_version" to "jds_entity_overview(uuid, uuid_location, uuid_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreTextCollection(): String {
        val tableName = "jds_store_text_collection"
        val columns = storeCommonColumnsInclSequence
        columns["value"] = getNativeDataTypeString(0)
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_uc"] = "uuid, uuid_location, uuid_version, field_id, sequence"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_fk"] = linkedMapOf("uuid, uuid_location, uuid_version" to "jds_entity_overview(uuid, uuid_location, uuid_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreTime(): String {
        val tableName = "jds_store_time"
        val columns = storeCommonColumns
        columns["value"] = getNativeDataTypeTime()
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_uc"] = "uuid, uuid_location, uuid_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_fk"] = linkedMapOf("uuid, uuid_location, uuid_version" to "jds_entity_overview(uuid, uuid_location, uuid_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreYearMonth(): String {
        val tableName = "jds_store_year_month"
        val columns = storeCommonColumns
        columns["value"] = getNativeDataTypeString(0)
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_uc"] = "uuid, uuid_location, uuid_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_fk"] = linkedMapOf("uuid, uuid_location, uuid_version" to "jds_entity_overview(uuid, uuid_location, uuid_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreZonedDateTime(): String {
        val tableName = "jds_store_zoned_date_time"
        val columns = storeCommonColumns
        columns["value"] = getNativeDataTypeZonedDateTime()
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_uc"] = "uuid, uuid_location, uuid_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_fk"] = linkedMapOf("uuid, uuid_location, uuid_version" to "jds_entity_overview(uuid, uuid_location, uuid_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }
}
