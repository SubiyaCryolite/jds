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
import io.github.subiyacryolite.jds.enums.JdsImplementation
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.sql.Connection
import java.sql.SQLException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap

/**
 * This class is responsible for the setup of SQL connections, default database
 * write statements, as well as the initialization of core and custom components
 * that will support JDS on the underlying Database implementation
 * @param implementation
 * @param supportsStatements
 */
abstract class JdsDb(var implementation: JdsImplementation, var supportsStatements: Boolean) : IJdsDb {

    val classes = ConcurrentHashMap<Long, Class<out JdsEntity>>()
    val tables = HashSet<JdsTable>()


    /**
     * A value indicating whether JDS should log every write in the system
     */
    var isLoggingEdits: Boolean = false
    /**
     * A value indicating whether JDS should print internal log information
     */
    var isPrintingOutput: Boolean = false
    /**
     * Indicate whether JDS is persisting to the primary data tables
     */
    var isWritingToPrimaryDataTables = true
    /**
     * Indicate whether the log table will have unique entries or will append only
     */
    var isLoggingAppendOnly = false

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
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_ENTITY_OVERVIEW)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_ENTITY_INHERITANCE)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_ENTITY_BINDING)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.REF_ENUM_VALUES)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.REF_FIELDS)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.REF_FIELD_TYPES)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_TEXT_ARRAY)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_FLOAT_ARRAY)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_INTEGER_ARRAY)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_LONG_ARRAY)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_DOUBLE_ARRAY)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_DATE_TIME_ARRAY)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_TEXT)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_BLOB)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_FLOAT)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_INTEGER)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_LONG)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_DOUBLE)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_DATE_TIME)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_ZONED_DATE_TIME)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_TIME)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_BOOLEAN)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.STORE_OLD_FIELD_VALUES)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.BIND_ENTITY_FIELDS)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.BIND_ENTITY_ENUMS)
        prepareDatabaseComponent(connection, JdsComponentType.TABLE, JdsComponent.REF_INHERITANCE)
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
            JdsComponent.STORE_ENTITY_INHERITANCE -> createStoreEntityInheritance(connection)
            JdsComponent.STORE_TEXT_ARRAY -> createStoreTextArray(connection)
            JdsComponent.STORE_FLOAT_ARRAY -> createStoreFloatArray(connection)
            JdsComponent.STORE_INTEGER_ARRAY -> createStoreIntegerArray(connection)
            JdsComponent.STORE_LONG_ARRAY -> createStoreLongArray(connection)
            JdsComponent.STORE_DOUBLE_ARRAY -> createStoreDoubleArray(connection)
            JdsComponent.STORE_DATE_TIME_ARRAY -> createStoreDateTimeArray(connection)
            JdsComponent.STORE_BOOLEAN -> createStoreBoolean(connection)
            JdsComponent.STORE_BLOB -> createStoreBlob(connection)
            JdsComponent.STORE_TEXT -> createStoreText(connection)
            JdsComponent.STORE_FLOAT -> createStoreFloat(connection)
            JdsComponent.STORE_INTEGER -> createStoreInteger(connection)
            JdsComponent.STORE_LONG -> createStoreLong(connection)
            JdsComponent.STORE_DOUBLE -> createStoreDouble(connection)
            JdsComponent.STORE_DATE_TIME -> createStoreDateTime(connection)
            JdsComponent.STORE_ZONED_DATE_TIME -> createStoreZonedDateTime(connection)
            JdsComponent.STORE_TIME -> createStoreTime(connection)
            JdsComponent.REF_ENTITIES -> createStoreEntities(connection)
            JdsComponent.REF_ENUM_VALUES -> createRefEnumValues(connection)
            JdsComponent.REF_INHERITANCE -> createRefInheritance(connection)
            JdsComponent.REF_FIELDS -> createRefFields(connection)
            JdsComponent.REF_FIELD_TYPES -> createRefFieldTypes(connection)
            JdsComponent.BIND_ENTITY_FIELDS -> createBindEntityFields(connection)
            JdsComponent.BIND_ENTITY_ENUMS -> createBindEntityEnums(connection)
            JdsComponent.STORE_ENTITY_OVERVIEW -> createRefEntityOverview(connection)
            JdsComponent.STORE_OLD_FIELD_VALUES -> createRefOldFieldValues(connection)
            JdsComponent.STORE_ENTITY_BINDING -> createStoreEntityBinding(connection)
            else -> {
            }
        }
        prepareCustomDatabaseComponents(connection, jdsComponent)
    }

    /**
     * Initialises custom JDS Database components
     *
     * @param connection   The SQL connection to use
     * @param jdsComponent an enum that maps to the components concrete
     * implementation details
     */
    protected open fun prepareCustomDatabaseComponents(connection: Connection, jdsComponent: JdsComponent) {}

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
            NamedPreparedStatement(connection, sql).use { preparedStatement ->
                preparedStatement.setString("tableName", tableName)
                preparedStatement.setString("columnName", columnName)
                preparedStatement.setString("tableCatalog", connection.catalog)
                preparedStatement.executeQuery().use({ resultSet ->
                    while (resultSet.next()) {
                        toReturn = resultSet.getInt("Result")
                    }
                })
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
            Thread.currentThread().contextClassLoader.getResourceAsStream(fileName).use { rs ->
                val innerSql = fileToString(rs)
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
    protected open fun prepareCustomDatabaseComponents(connection: Connection) {}

    /**
     * Internal checks to see if the specified table exists the the database
     *
     * @param connection the connection to use
     * @param tableName the table to look up
     * @return 1 if the specified table exists the the database
     */
    abstract fun tableExists(connection: Connection, tableName: String): Int

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
     * Database specific SQL used to create the schema that stores text values
     */
    protected abstract fun createStoreBoolean(connection: Connection)

    /**
     * Database specific SQL used to create the schema that stores text values
     */
    protected abstract fun createStoreText(connection: Connection)

    /**
     * Database specific SQL used to create the schema that stores blob
     * values
     */
    protected abstract fun createStoreBlob(connection: Connection)

    /**
     * Database specific SQL used to create the schema that stores datetime
     * values
     */
    protected abstract fun createStoreDateTime(connection: Connection)

    /**
     * Database specific SQL used to create the schema that stores zoned
     * datetime values
     */
    protected abstract fun createStoreZonedDateTime(connection: Connection)

    /**
     * Database specific SQL used to create the schema that stores time values
     */
    protected abstract fun createStoreTime(connection: Connection)

    /**
     * Database specific SQL used to create the schema that stores integer
     * values
     */
    protected abstract fun createStoreInteger(connection: Connection)

    /**
     * Database specific SQL used to create the schema that stores float values
     */
    protected abstract fun createStoreFloat(connection: Connection)

    /**
     * Database specific SQL used to create the schema that stores double values
     */
    protected abstract fun createStoreDouble(connection: Connection)

    /**
     * Database specific SQL used to create the schema that stores long values
     */
    protected abstract fun createStoreLong(connection: Connection)

    /**
     * Database specific SQL used to create the schema that stores text array
     * values
     */
    protected abstract fun createStoreTextArray(connection: Connection)

    /**
     * Database specific SQL used to create the schema that stores datetime
     * array values
     */
    protected abstract fun createStoreDateTimeArray(connection: Connection)

    /**
     * Database specific SQL used to create the schema that stores integer array
     * values
     */
    protected abstract fun createStoreIntegerArray(connection: Connection)

    /**
     * Database specific SQL used to create the schema that stores float array
     * values
     */
    protected abstract fun createStoreFloatArray(connection: Connection)

    /**
     * Database specific SQL used to create the schema that stores double array
     * values
     */
    protected abstract fun createStoreDoubleArray(connection: Connection)

    /**
     * Database specific SQL used to create the schema that stores long array
     * values
     */
    protected abstract fun createStoreLongArray(connection: Connection)

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
     * Database specific SQL used to create the schema that stores old field
     * values of every type
     */
    protected abstract fun createRefOldFieldValues(connection: Connection)

    /**
     * Database specific SQL used to create the schema that stores entity to
     * entity bindings
     */
    protected abstract fun createStoreEntityBinding(connection: Connection)

    /**
     * Database specific SQL used to create the schema that stores entity to type
     * bindings
     */
    protected abstract fun createStoreEntityInheritance(connection: Connection)

    /**
     * @param connection     the SQL connection to use for DB operations
     * @param parentEntities a collection of parent classes
     * @param entityCode     the value representing the entity
     */
    private fun mapParentEntities(connection: Connection, parentEntities: List<Long>, entityCode: Long) {
        if (parentEntities.isEmpty()) return
        try {
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
    }

    /**
     * Maps an entity's name to its id
     *
     * @param connection the SQL connection to use for DB operations
     * @param entityId   the entity's id
     * @param entityName the entity's name
     */
    private fun mapClassName(connection: Connection, entityId: Long, entityName: String) {
        try {
            (if (supportsStatements) connection.prepareCall(mapClassName()) else connection.prepareStatement(mapClassName())).use { statement ->
                statement.setLong(1, entityId)
                statement.setString(2, entityName)
                statement.executeUpdate()
                if (isPrintingOutput)
                    println("Mapped Entity [$entityName - $entityId]")
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    fun mapTable(table: JdsTable) {
        tables.add(table)
    }

    fun prepareTables() {
        //pool connections so that there's no wastage
        val connectionPool = HashMap<Int, Connection>()
        tables.forEach { it.forceGenerateOrUpdateSchema(this, connectionPool) }
        connectionPool.forEach { it.value.close() }
    }

    fun map(entity: Class<out JdsEntity>) {
        if (entity.isAnnotationPresent(JdsEntityAnnotation::class.java)) {
            val entityAnnotation = entity.getAnnotation(JdsEntityAnnotation::class.java)
            if (!classes.containsKey(entityAnnotation.entityId)) {
                classes.put(entityAnnotation.entityId, entity)
                //do the thing
                try {
                    getConnection().use { connection ->
                        connection.autoCommit = false
                        val parentEntities = ArrayList<Long>()
                        var jdsEntity = entity.newInstance()
                        JdsExtensions.determineParents(entity, parentEntities)
                        mapClassName(connection, jdsEntity.overview.entityId, entityAnnotation.entityName)
                        jdsEntity.mapClassFields(this, connection, jdsEntity.overview.entityId)
                        jdsEntity.mapClassFieldTypes(this, connection)
                        jdsEntity.mapClassEnums(this, connection, jdsEntity.overview.entityId)
                        mapParentEntities(connection, parentEntities, jdsEntity.overview.entityId)
                        connection.commit()
                        if (isPrintingOutput)
                            println("Mapped Entity [${entityAnnotation.entityName}]")
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace(System.err)
                }
            } else
                throw RuntimeException("Duplicate service code for class [${entity.canonicalName}] - [${entityAnnotation.entityId}]")
        } else
            throw RuntimeException("You must annotate the class [${entity.canonicalName}] with [${JdsEntityAnnotation::class.java}]")
    }

    /**
     * SQL call to save text values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveString(): String {
        return "{call procStoreText(:uuid,:fieldId,:value)}"
    }

    /**
     * SQL call to save boolean values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveBoolean(): String {
        return "{call procStoreBoolean(:uuid,:fieldId,:value)}"
    }

    /**
     * SQL call to save long values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveLong(): String {
        return "{call procStoreLong(:uuid,:fieldId,:value)}"
    }

    /**
     * SQL call to save double values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveDouble(): String {
        return "{call procStoreDouble(:uuid,:fieldId,:value)}"
    }

    /**
     * SQL call to save blob values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveBlob(): String {
        return "{call procStoreBlob(:uuid,:fieldId,:value)}"
    }


    /**
     * SQL call to save float values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveFloat(): String {
        return "{call procStoreFloat(:uuid,:fieldId,:value)}"
    }

    /**
     * SQL call to save integer values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveInteger(): String {
        return "{call procStoreInteger(:uuid,:fieldId,:value)}"
    }

    /**
     * SQL call to save datetime values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveDateTime(): String {
        return "{call procStoreDateTime(:uuid,:fieldId,:value)}"
    }

    /**
     * SQL call to save datetime values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveZonedDateTime(): String {
        return "{call procStoreZonedDateTime(:uuid,:fieldId,:value)}"
    }

    /**
     * SQL call to save date values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveDate(): String {
        return "{call procStoreDate(:uuid,:fieldId,:value)}"
    }

    /**
     * SQL call to save time values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveTime(): String {
        return "{call procStoreTime(:uuid,:fieldId,:value)}"
    }

    /**
     * SQL call to save entity overview values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveOverview(): String {
        return "{call procStoreEntityOverviewV3(:uuid,:dateCreated,:dateModified,:live,:version)}"
    }

    /**
     * SQL call to save entity overview values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveOverviewInheritance(): String {
        return "{call procStoreEntityInheritance(:uuid, :entityId)}"
    }

    /**
     * SQL call to bind fieldIds to entityVersions
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun mapClassFields(): String {
        return "{call procBindEntityFields(:entityId, :fieldId)}"
    }

    /**
     * SQL call to map field names and descriptions
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun mapFieldNames(): String {
        return "{call procBindFieldNames(:fieldId, :fieldName, :fieldDescription)}"
    }

    /**
     * SQL call to map field types
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun mapFieldTypes(): String {
        return "{call procBindFieldTypes(:typeId, :typeName)}"
    }

    /**
     * SQL call to bind enumProperties to entityVersions
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun mapClassEnumsImplementation(): String {
        return "{call procBindEntityEnums(?,?)}"
    }

    /**
     * SQL call to map parents to child entities
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun mapParentToChild(): String {
        return "{call procBindParentToChild(?,?)}"
    }

    /**
     * SQL call to map class names
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun mapClassName(): String {
        return "{call procRefEntities(?,?)}"
    }

    /**
     * SQL call to save reference enum values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun mapEnumValues(): String {
        return "{call procRefEnumValues(?,?,?)}"
    }

    /**
     * Variable to facilitate in-line rows in Oracle
     */
    private val logSqlSource = when (isOracleDb) {
        true -> "SELECT ?, ?, ?, ? FROM DUAL"
        else -> "SELECT ?, ?, ?, ?"
    }

    /**
     * Variable to facilitate lookups for strings in Oracle
     */
    private val oldStringValue = when (isOracleDb) {
        true -> "dbms_lob.substr(StringValue, dbms_lob.getlength(StringValue), 1)"
        else -> "StringValue"
    }

    /**
     * SQL executed in order to log String values
     * @return SQL executed in order to log String values
     */
    internal fun saveOldStringValues(): String {
        return when (isLoggingAppendOnly) {
            true -> "INSERT INTO JdsStoreOldFieldValues(Uuid, FieldId, Sequence, StringValue) VALUES(?, ?, ?, ?)"
            false -> "INSERT INTO JdsStoreOldFieldValues(Uuid, FieldId, Sequence, StringValue) $logSqlSource " +
                    "WHERE NOT EXISTS(SELECT 1 FROM JdsStoreOldFieldValues WHERE Uuid = ? AND FieldId = ? AND Sequence = ? AND $oldStringValue = ?)"
        }
    }

    /**
     * SQL executed in order to log Double values
     * @return SQL executed in order to log Double values
     */
    internal fun saveOldDoubleValues(): String {
        return when (isLoggingAppendOnly) {
            true -> "INSERT INTO JdsStoreOldFieldValues(Uuid, FieldId, Sequence, DoubleValue) VALUES(?, ?, ?, ?)"
            false -> "INSERT INTO JdsStoreOldFieldValues(Uuid, FieldId, Sequence, DoubleValue) $logSqlSource " +
                    "WHERE NOT EXISTS(SELECT 1 FROM JdsStoreOldFieldValues WHERE Uuid = ? AND FieldId = ? AND Sequence = ? AND DoubleValue = ?)"
        }
    }

    /**
     * SQL executed in order to log Long values
     * @return SQL executed in order to log Long values
     */
    internal fun saveOldLongValues(): String {
        return when (isLoggingAppendOnly) {
            true -> "INSERT INTO JdsStoreOldFieldValues(Uuid, FieldId, Sequence, LongValue) VALUES(?, ?, ?, ?)"
            false -> "INSERT INTO JdsStoreOldFieldValues(Uuid, FieldId, Sequence, LongValue) $logSqlSource " +
                    "WHERE NOT EXISTS(SELECT 1 FROM JdsStoreOldFieldValues WHERE Uuid = ? AND FieldId = ? AND Sequence = ? AND LongValue = ?)"
        }
    }

    /**
     * SQL executed in order to log Integer values
     * @return SQL executed in order to log Integer values
     */
    internal fun saveOldIntegerValues(): String {
        return when (isLoggingAppendOnly) {
            true -> "INSERT INTO JdsStoreOldFieldValues(Uuid, FieldId, Sequence, IntegerValue) VALUES(?, ?, ?, ?)"
            false -> "INSERT INTO JdsStoreOldFieldValues(Uuid, FieldId, Sequence, IntegerValue) $logSqlSource " +
                    "WHERE NOT EXISTS(SELECT 1 FROM JdsStoreOldFieldValues WHERE Uuid = ? AND FieldId = ? AND Sequence = ? AND IntegerValue = ?)"
        }
    }

    /**
     * SQL executed in order to log Float values
     * @return SQL executed in order to log Float values
     */
    internal fun saveOldFloatValues(): String {
        return when (isLoggingAppendOnly) {
            true -> "INSERT INTO JdsStoreOldFieldValues(Uuid, FieldId, Sequence, FloatValue) VALUES(?, ?, ?, ?)"
            false -> "INSERT INTO JdsStoreOldFieldValues(Uuid, FieldId, Sequence, FloatValue) $logSqlSource " +
                    "WHERE NOT EXISTS(SELECT 1 FROM JdsStoreOldFieldValues WHERE Uuid = ? AND FieldId = ? AND Sequence = ? AND FloatValue = ?)"
        }
    }

    /**
     * SQL executed in order to log DateTime values
     * @return SQL executed in order to log DateTime values
     */
    internal fun saveOldDateTimeValues(): String {
        return when (isLoggingAppendOnly) {
            true -> "INSERT INTO JdsStoreOldFieldValues(Uuid, FieldId, Sequence, DateTimeValue) VALUES(?, ?, ?, ?)"
            false -> "INSERT INTO JdsStoreOldFieldValues(Uuid, FieldId, Sequence, DateTimeValue) $logSqlSource " +
                    "WHERE NOT EXISTS(SELECT 1 FROM JdsStoreOldFieldValues WHERE Uuid = ? AND FieldId = ? AND Sequence = ? AND DateTimeValue = ?)"
        }
    }

    /**
     * SQL executed in order to log ZonedDateTime values
     * @return SQL executed in order to log ZonedDateTime values
     */
    internal fun saveOldZonedDateTimeValues(): String {
        return when (isLoggingAppendOnly) {
            true -> "INSERT INTO JdsStoreOldFieldValues(Uuid, FieldId, Sequence, ZonedDateTimeValue) VALUES(?, ?, ?, ?)"
            false -> "INSERT INTO JdsStoreOldFieldValues(Uuid, FieldId, Sequence, ZonedDateTimeValue) $logSqlSource " +
                    "WHERE NOT EXISTS(SELECT 1 FROM JdsStoreOldFieldValues WHERE Uuid = ? AND FieldId = ? AND Sequence = ? AND ZonedDateTimeValue = ?)"
        }
    }

    /**
     * SQL executed in order to log Time values
     * @return SQL executed in order to log Time values
     */
    internal fun saveOldTimeValues(): String {
        return when (isLoggingAppendOnly) {
            true -> "INSERT INTO JdsStoreOldFieldValues(Uuid, FieldId, Sequence, TimeValue) VALUES(?, ?, ?, ?)"
            false -> "INSERT INTO JdsStoreOldFieldValues(Uuid, FieldId, Sequence, TimeValue) $logSqlSource " +
                    "WHERE NOT EXISTS(SELECT 1 FROM JdsStoreOldFieldValues WHERE Uuid = ? AND FieldId = ? AND Sequence = ? AND TimeValue = ?)"
        }
    }

    /**
     * SQL executed in order to log Boolean values
     * @return SQL executed in order to log Boolean values
     */
    internal fun saveOldBooleanValues(): String {
        return when (isLoggingAppendOnly) {
            true -> "INSERT INTO JdsStoreOldFieldValues(Uuid, FieldId, Sequence, BooleanValue) VALUES(?, ?, ?, ?)"
            false -> "INSERT INTO JdsStoreOldFieldValues(Uuid, FieldId, Sequence, BooleanValue) $logSqlSource " +
                    "WHERE NOT EXISTS(SELECT 1 FROM JdsStoreOldFieldValues WHERE Uuid = ? AND FieldId = ? AND Sequence = ? AND BooleanValue = ?)"
        }
    }

    /**
     * SQL executed in order to log Blob values
     * @return SQL executed in order to log Blob values
     */
    internal fun saveOldBlobValues(): String {
        return "INSERT INTO JdsStoreOldFieldValues(Uuid, FieldId, Sequence, BlobValue) VALUES(?, ?, ?, ?)"
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
}
