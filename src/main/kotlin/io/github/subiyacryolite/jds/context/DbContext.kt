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
package io.github.subiyacryolite.jds.context

import io.github.subiyacryolite.jds.Entity
import io.github.subiyacryolite.jds.Field
import io.github.subiyacryolite.jds.Options
import io.github.subiyacryolite.jds.Table
import io.github.subiyacryolite.jds.annotations.EntityAnnotation
import io.github.subiyacryolite.jds.enums.FieldType
import io.github.subiyacryolite.jds.enums.Implementation
import io.github.subiyacryolite.jds.enums.ProcedureComponent
import io.github.subiyacryolite.jds.enums.TableComponent
import io.github.subiyacryolite.jds.extensions.Extensions
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.Serializable
import java.sql.Connection
import java.sql.SQLException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.sql.DataSource
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap

/**
 * This class is responsible for the setup of DataSources as well as the initialization of core and custom components
 * that will support JDS on the underlying Database implementation
 * @param implementation
 * @param supportsStatements
 */
abstract class DbContext(val implementation: Implementation, val supportsStatements: Boolean) : IDbContext, Serializable {

    val classes = ConcurrentHashMap<Int, Class<out Entity>>()
    val tables = HashSet<Table>()
    val options = Options()
    var dimensionTable = ""

    /**
     * Initialise JDS base tables
     */
    @JvmOverloads
    fun init(dimensionTable: String = "jds_entity_overview") {
        try {
            this.dimensionTable = dimensionTable
            dataSource.connection.use { connection ->
                prepareJdsComponents(connection)
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }


    /**
     * Initialise core database components
     */
    private fun prepareJdsComponents(connection: Connection) {
        prepareJdsComponent(connection, TableComponent.RefEntities)
        prepareJdsComponent(connection, TableComponent.RefFieldTypes)
        prepareJdsComponent(connection, TableComponent.RedFields)
        prepareJdsComponent(connection, TableComponent.RefEnumValues)
        prepareJdsComponent(connection, TableComponent.RefInheritance)
        prepareJdsComponent(connection, TableComponent.EntityOverview)
        prepareJdsComponent(connection, TableComponent.EntityBinding)
        prepareJdsComponent(connection, TableComponent.RefEntityField)
        prepareJdsComponent(connection, TableComponent.RefEntityEnums)
        prepareJdsComponent(connection, TableComponent.StoreText)
        prepareJdsComponent(connection, TableComponent.StoreTextCollection)
        prepareJdsComponent(connection, TableComponent.StoreBlob)
        prepareJdsComponent(connection, TableComponent.StoreEnum)
        prepareJdsComponent(connection, TableComponent.StoreEnumString)
        prepareJdsComponent(connection, TableComponent.StoreEnumCollection)
        prepareJdsComponent(connection, TableComponent.StoreEnumStringCollection)
        prepareJdsComponent(connection, TableComponent.StoreShort)
        prepareJdsComponent(connection, TableComponent.StoreFloat)
        prepareJdsComponent(connection, TableComponent.StoreFloatCollection)
        prepareJdsComponent(connection, TableComponent.StoreInteger)
        prepareJdsComponent(connection, TableComponent.StoreIntegerCollection)
        prepareJdsComponent(connection, TableComponent.StoreDate)
        prepareJdsComponent(connection, TableComponent.StoreLong)
        prepareJdsComponent(connection, TableComponent.StoreLongCollection)
        prepareJdsComponent(connection, TableComponent.StoreDouble)
        prepareJdsComponent(connection, TableComponent.StoreDoubleCollection)
        prepareJdsComponent(connection, TableComponent.StoreDateTime)
        prepareJdsComponent(connection, TableComponent.StoreDateTimeCollection)
        prepareJdsComponent(connection, TableComponent.StoreZonedDateTime)
        prepareJdsComponent(connection, TableComponent.StoreTime)
        prepareJdsComponent(connection, TableComponent.StorePeriod)
        prepareJdsComponent(connection, TableComponent.StoreDuration)
        prepareJdsComponent(connection, TableComponent.StoreYearMonth)
        prepareJdsComponent(connection, TableComponent.StoreMonthDay)
        prepareJdsComponent(connection, TableComponent.StoreBoolean)
        prepareJdsComponent(connection, TableComponent.StoreUuid)
        prepareJdsComponent(connection, TableComponent.EntityLiveVersion)
        if (supportsStatements) {
            prepareJdsComponent(connection, ProcedureComponent.PopStoreBoolean)
            prepareJdsComponent(connection, ProcedureComponent.PopStoreBlob)
            prepareJdsComponent(connection, ProcedureComponent.PopStoreText)
            prepareJdsComponent(connection, ProcedureComponent.PopStoreLong)
            prepareJdsComponent(connection, ProcedureComponent.PopStoreInteger)
            prepareJdsComponent(connection, ProcedureComponent.PopStoreFloat)
            prepareJdsComponent(connection, ProcedureComponent.PopStoreShort)
            prepareJdsComponent(connection, ProcedureComponent.PopStoreUuid)
            prepareJdsComponent(connection, ProcedureComponent.PopStoreDouble)
            prepareJdsComponent(connection, ProcedureComponent.PopStoreDateTime)
            prepareJdsComponent(connection, ProcedureComponent.PopStoreTime)
            prepareJdsComponent(connection, ProcedureComponent.PopStoreDate)
            prepareJdsComponent(connection, ProcedureComponent.PopStoreDuration)
            prepareJdsComponent(connection, ProcedureComponent.PopStorePeriod)
            prepareJdsComponent(connection, ProcedureComponent.PopStoreMonthYear)
            prepareJdsComponent(connection, ProcedureComponent.PopStoreMonthDay)
            prepareJdsComponent(connection, ProcedureComponent.PopStoreYearMonth)
            prepareJdsComponent(connection, ProcedureComponent.PopStoreEnum)
            prepareJdsComponent(connection, ProcedureComponent.PopStoreEnumString)
            prepareJdsComponent(connection, ProcedureComponent.PopStoreZonedDateTime)
            prepareJdsComponent(connection, ProcedureComponent.PopEntityOverview)
            prepareJdsComponent(connection, ProcedureComponent.PopEntityBinding)
            prepareJdsComponent(connection, ProcedureComponent.PopRefEntityField)
            prepareJdsComponent(connection, ProcedureComponent.PopRefEntityEnum)
            prepareJdsComponent(connection, ProcedureComponent.PopRefEntity)
            prepareJdsComponent(connection, ProcedureComponent.PopRefEnum)
            prepareJdsComponent(connection, ProcedureComponent.PopRefField)
            prepareJdsComponent(connection, ProcedureComponent.PopRefEntityInheritance)
            prepareJdsComponent(connection, ProcedureComponent.PopEntityLiveVersion)
            prepareJdsComponent(connection, ProcedureComponent.PopStoreTextCollection)
            prepareJdsComponent(connection, ProcedureComponent.PopStoreEnumCollection)
            prepareJdsComponent(connection, ProcedureComponent.PopStoreEnumStringCollection)
            prepareJdsComponent(connection, ProcedureComponent.PopStoreFloatCollection)
            prepareJdsComponent(connection, ProcedureComponent.PopStoreIntegerCollection)
            prepareJdsComponent(connection, ProcedureComponent.PopStoreLongCollection)
            prepareJdsComponent(connection, ProcedureComponent.PopStoreDoubleCollection)
        }
    }

    val isOracleDb: Boolean
        get() = implementation === Implementation.Oracle

    val isTransactionalSqlDb: Boolean
        get() = implementation === Implementation.TSql

    val isMySqlDb: Boolean
        get() = implementation === Implementation.MySql || implementation === Implementation.MariaDb

    val isSqLiteDb: Boolean
        get() = implementation === Implementation.SqLite

    val isPosgreSqlDb: Boolean
        get() = implementation === Implementation.Postgres

    /**
     * Delegates the creation of custom database components depending on the
     * underlying JDS Database implementation
     * @param connection the connection to use for this operation
     * @param tableComponent an enum that maps to the components concrete
     * implementation details
     */
    private fun prepareJdsComponent(connection: Connection, tableComponent: TableComponent) {
        if (!doesTableExist(connection, tableComponent.component)) {
            initiateDatabaseComponent(connection, tableComponent)
        }
    }

    /**
     * Delegates the creation of custom database components depending on the
     * underlying JDS Database implementation
     * @param connection the connection to use for this operation
     * @param procedureComponent an enum that maps to the components concrete
     * implementation details
     */
    private fun prepareJdsComponent(connection: Connection, procedureComponent: ProcedureComponent) {
        if (!doesProcedureExist(connection, procedureComponent.component)) {
            initiateDatabaseComponent(connection, procedureComponent)
        }
    }

    /**
     * Initialises core JDS Database components
     *
     * @param connection the SQL connection top use for this operation
     * @param tableComponent an enum that maps to the components concrete implementation
     */
    private fun initiateDatabaseComponent(connection: Connection, tableComponent: TableComponent) {
        when (tableComponent) {
            TableComponent.EntityBinding -> executeSqlFromString(connection, createEntityBinding())
            TableComponent.EntityLiveVersion -> executeSqlFromString(connection, createEntityLiveVersionTable())
            TableComponent.EntityOverview -> executeSqlFromString(connection, createRefEntityOverview())
            TableComponent.RefEntities -> executeSqlFromString(connection, createStoreEntities())
            TableComponent.RefEntityEnums -> executeSqlFromString(connection, createBindEntityEnums())
            TableComponent.RefEntityField -> executeSqlFromString(connection, createBindEntityFields())
            TableComponent.RefEnumValues -> executeSqlFromString(connection, createRefEnumValues())
            TableComponent.RefFieldTypes -> {
                executeSqlFromString(connection, createRefFieldTypes())
                populateFieldTypes(connection)
            }
            TableComponent.RedFields -> executeSqlFromString(connection, createRefFields())
            TableComponent.RefInheritance -> executeSqlFromString(connection, createRefInheritance())
            TableComponent.StoreBlob -> executeSqlFromString(connection, createStoreBlob())
            TableComponent.StoreBoolean -> executeSqlFromString(connection, createStoreBoolean())
            TableComponent.StoreDate -> executeSqlFromString(connection, createStoreDate())
            TableComponent.StoreDateTime -> executeSqlFromString(connection, createStoreDateTime())
            TableComponent.StoreDateTimeCollection -> executeSqlFromString(connection, createStoreDateTimeCollection())
            TableComponent.StoreDouble -> executeSqlFromString(connection, createStoreDouble())
            TableComponent.StoreDoubleCollection -> executeSqlFromString(connection, createStoreDoubleCollection())
            TableComponent.StoreDuration -> executeSqlFromString(connection, createStoreDuration())
            TableComponent.StoreEnum -> executeSqlFromString(connection, createStoreEnum())
            TableComponent.StoreEnumString -> executeSqlFromString(connection, createStoreEnumString())
            TableComponent.StoreEnumCollection -> executeSqlFromString(connection, createStoreEnumCollection())
            TableComponent.StoreEnumStringCollection -> executeSqlFromString(connection, createStoreEnumStringCollection())
            TableComponent.StoreFloat -> executeSqlFromString(connection, createStoreFloat())
            TableComponent.StoreShort -> executeSqlFromString(connection, createStoreShort())
            TableComponent.StoreUuid -> executeSqlFromString(connection, createStoreUuid())
            TableComponent.StoreFloatCollection -> executeSqlFromString(connection, createStoreFloatCollection())
            TableComponent.StoreInteger -> executeSqlFromString(connection, createStoreInteger())
            TableComponent.StoreIntegerCollection -> executeSqlFromString(connection, createStoreIntegerCollection())
            TableComponent.StoreLong -> executeSqlFromString(connection, createStoreLong())
            TableComponent.StoreLongCollection -> executeSqlFromString(connection, createStoreLongCollection())
            TableComponent.StoreMonthDay -> executeSqlFromString(connection, createStoreMonthDay())
            TableComponent.StorePeriod -> executeSqlFromString(connection, createStorePeriod())
            TableComponent.StoreText -> executeSqlFromString(connection, createStoreText())
            TableComponent.StoreTextCollection -> executeSqlFromString(connection, createStoreTextCollection())
            TableComponent.StoreTime -> executeSqlFromString(connection, createStoreTime())
            TableComponent.StoreYearMonth -> executeSqlFromString(connection, createStoreYearMonth())
            TableComponent.StoreZonedDateTime -> executeSqlFromString(connection, createStoreZonedDateTime())
        }
    }

    /**
     * Initialises core JDS Database components
     *
     * @param connection   the SQL connection top use for this operation
     * @param jdsTableComponent an enum that maps to the components concrete implementation
     */
    private fun initiateDatabaseComponent(connection: Connection, jdsTableComponent: ProcedureComponent) {
        when (jdsTableComponent) {
            ProcedureComponent.PopEntityBinding -> executeSqlFromString(connection, createPopJdsEntityBinding())
            ProcedureComponent.PopEntityLiveVersion -> executeSqlFromString(connection, createPopEntityLiveVersion())
            ProcedureComponent.PopEntityOverview -> executeSqlFromString(connection, createPopJdsEntityOverview())
            ProcedureComponent.PopRefEntity -> executeSqlFromString(connection, createPopJdsRefEntity())
            ProcedureComponent.PopRefEntityEnum -> executeSqlFromString(connection, createPopJdsRefEntityEnum())
            ProcedureComponent.PopRefEntityField -> executeSqlFromString(connection, createPopJdsRefEntityField())
            ProcedureComponent.PopRefEntityInheritance -> executeSqlFromString(connection, createPopJdsRefEntityInheritance())
            ProcedureComponent.PopRefEnum -> executeSqlFromString(connection, createPopJdsRefEnum())
            ProcedureComponent.PopRefField -> executeSqlFromString(connection, createPopJdsRefField())
            ProcedureComponent.PopStoreBlob -> executeSqlFromString(connection, createPopJdsStoreBlob())
            ProcedureComponent.PopStoreBoolean -> executeSqlFromString(connection, createPopJdsStoreBoolean())
            ProcedureComponent.PopStoreDate -> executeSqlFromString(connection, createPopJdsStoreDate())
            ProcedureComponent.PopStoreDateTime -> executeSqlFromString(connection, createPopJdsStoreDateTime())
            ProcedureComponent.PopStoreDouble -> executeSqlFromString(connection, createPopJdsStoreDouble())
            ProcedureComponent.PopStoreDoubleCollection -> executeSqlFromString(connection, createPopDoubleCollection())
            ProcedureComponent.PopStoreDuration -> executeSqlFromString(connection, createPopJdsStoreDuration())
            ProcedureComponent.PopStoreEnum -> executeSqlFromString(connection, createPopJdsStoreEnum())
            ProcedureComponent.PopStoreEnumString -> executeSqlFromString(connection, createPopJdsStoreEnumString())
            ProcedureComponent.PopStoreEnumCollection -> executeSqlFromString(connection, createPopEnumCollection())
            ProcedureComponent.PopStoreEnumStringCollection -> executeSqlFromString(connection, createPopEnumStringCollection())
            ProcedureComponent.PopStoreFloat -> executeSqlFromString(connection, createPopJdsStoreFloat())
            ProcedureComponent.PopStoreShort -> executeSqlFromString(connection, createPopJdsStoreShort())
            ProcedureComponent.PopStoreUuid -> executeSqlFromString(connection, createPopJdsStoreUuid())
            ProcedureComponent.PopStoreFloatCollection -> executeSqlFromString(connection, createPopFloatCollection())
            ProcedureComponent.PopStoreInteger -> executeSqlFromString(connection, createPopJdsStoreInteger())
            ProcedureComponent.PopStoreIntegerCollection -> executeSqlFromString(connection, createPopIntegerCollection())
            ProcedureComponent.PopStoreLong -> executeSqlFromString(connection, createPopJdsStoreLong())
            ProcedureComponent.PopStoreLongCollection -> executeSqlFromString(connection, createPopLongCollection())
            ProcedureComponent.PopStoreMonthDay -> executeSqlFromString(connection, createPopJdsMonthDay())
            ProcedureComponent.PopStoreMonthYear -> executeSqlFromString(connection, createPopJdsMonthYear())
            ProcedureComponent.PopStorePeriod -> executeSqlFromString(connection, createPopJdsStorePeriod())
            ProcedureComponent.PopStoreText -> executeSqlFromString(connection, createPopJdsStoreText())
            ProcedureComponent.PopStoreTextCollection -> executeSqlFromString(connection, createPopTextCollection())
            ProcedureComponent.PopStoreTime -> executeSqlFromString(connection, createPopJdsStoreTime())
            ProcedureComponent.PopStoreYearMonth -> executeSqlFromString(connection, createPopJdsYearMonth())
            ProcedureComponent.PopStoreZonedDateTime -> executeSqlFromString(connection, createPopJdsStoreZonedDateTime())
        }
    }

    override fun doesTableExist(connection: Connection, name: String): Boolean {
        return tableExists(connection, name) == 1
    }

    override fun doesProcedureExist(connection: Connection, name: String): Boolean {
        return procedureExists(connection, name) == 1
    }

    override fun doesColumnExist(connection: Connection, tableName: String, columnName: String): Boolean {
        return columnExists(connection, tableName, columnName) == 1
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
        Thread.currentThread().contextClassLoader.getResourceAsStream(fileName).use { inputStream ->
            executeSqlFromString(connection, fileToString(inputStream))
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
    private fun fileToString(inputStream: InputStream?): String {
        BufferedInputStream(inputStream!!).use { bufferedInputStream ->
            ByteArrayOutputStream().use { byteArrayOutputStream ->
                var result = bufferedInputStream.read()
                while (result != -1) {
                    byteArrayOutputStream.write(result.toByte().toInt())
                    result = bufferedInputStream.read()
                }
                return byteArrayOutputStream.toString()
            }
        }
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
     * @param connection     the SQL connection to use for DB operations
     * @param parentEntities a collection of parent classes
     * @param entityCode     the value representing the entity
     */
    private fun mapParentEntities(connection: Connection, parentEntities: Collection<Int>, entityCode: Int) = try {
        (if (supportsStatements) connection.prepareCall(mapParentToChild()) else connection.prepareStatement(mapParentToChild())).use { statement ->
            for (parentEntity in parentEntities) {
                if (parentEntity != entityCode) {
                    statement.setInt(1, parentEntity)
                    statement.setInt(2, entityCode)
                    statement.addBatch()
                }
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
     * @param description a description of the entity
     */
    private fun populateRefEntity(connection: Connection, id: Int, name: String, description: String) = try {
        (if (supportsStatements) connection.prepareCall(populateRefEntity()) else connection.prepareStatement(populateRefEntity())).use { statement ->
            statement.setInt(1, id)
            statement.setString(2, name)
            statement.setString(3, description)
            statement.executeUpdate()
            if (options.logOutput)
                println("Mapped Entity [$name - $id]")
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    fun mapTable(vararg table: Table) {
        tables.addAll(table)
    }

    @Throws(Exception::class)
    fun prepareTables() {
        dataSource.connection.use { connection ->
            tables.forEach { it.forceGenerateOrUpdateSchema(this, connection) }
        }
    }

    fun map(entity: Class<out Entity>) {
        val classHasAnnotation = entity.isAnnotationPresent(EntityAnnotation::class.java)
        val superclassHasAnnotation = entity.superclass.isAnnotationPresent(EntityAnnotation::class.java)
        if (classHasAnnotation || superclassHasAnnotation) {
            val entityAnnotation = when (classHasAnnotation) {
                true -> entity.getAnnotation(EntityAnnotation::class.java)
                false -> entity.superclass.getAnnotation(EntityAnnotation::class.java)
            }
            if (!classes.containsKey(entityAnnotation.id)) {
                classes[entityAnnotation.id] = entity
                //do the thing
                try {
                    dataSource.connection.use { connection ->
                        connection.autoCommit = false
                        val parentEntities = HashSet<Int>()
                        val jdsEntity = entity.getDeclaredConstructor().newInstance()
                        parentEntities.add(jdsEntity.overview.entityId)//add this own entity to the chain
                        Extensions.determineParents(entity, parentEntities)
                        populateRefEntity(connection, jdsEntity.overview.entityId, entityAnnotation.name, entityAnnotation.description)
                        jdsEntity.populateRefFieldRefEntityField(this, connection, jdsEntity.overview.entityId)
                        jdsEntity.populateRefEnumRefEntityEnum(this, connection, jdsEntity.overview.entityId)
                        mapParentEntities(connection, parentEntities, jdsEntity.overview.entityId)
                        connection.commit()
                        connection.autoCommit = true
                        if (options.logOutput)
                            println("Mapped Entity [${entityAnnotation.name}]")
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace(System.err)
                }
            }
        } else
            throw RuntimeException("You must annotate the class [${entity.canonicalName}] or its parent with [${EntityAnnotation::class.java}]")
    }

    private fun populateFieldTypes(connection: Connection) {
        connection.prepareStatement("INSERT INTO jds_ref_field_type(ordinal, caption) VALUES(?,?)").use { statement ->
            FieldType.values().forEach { fieldType ->
                statement.setInt(1, fieldType.ordinal)
                statement.setString(2, fieldType.name)
                statement.addBatch()
            }
            statement.executeBatch()
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
     * SQL call to save short values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveShort() = "{call jds_pop_short(?, ?, ?, ?)}"

    /**
     * SQL call to save short values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun saveUuid() = "{call jds_pop_uuid(?, ?, ?, ?)}"

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
    internal open fun mapParentToChild() = "{call jds_pop_ref_entity_inheritance(?, ?)}"

    /**
     * SQL call to map class names
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateRefEntity() = "{call jds_pop_ref_entity(?, ?, ?)}"

    /**
     * SQL call to save reference enum values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateRefEnum() = "{call jds_pop_ref_enum(?, ?, ?, ?)}"

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
    internal fun typeOfField(fieldId: Int): FieldType = when (Field.values.containsKey(fieldId)) {
        true -> Field.values[fieldId]!!.type
        false -> FieldType.Unknown
    }

    private fun getStoreUniqueColumns(tableName: String) = linkedMapOf("${tableName}_u" to "id, edit_version, field_id")

    private fun getStoreColumns(kvp: Pair<String, String>): LinkedHashMap<String, String> {
        return linkedMapOf(
                "id" to getDataType(FieldType.String, 36),
                "edit_version" to getDataType(FieldType.Int),
                "field_id" to getDataType(FieldType.Int),
                kvp.first to kvp.second
        )
    }

    private fun createPopJdsEntityBinding(): String {
        val uniqueColumns = setOf("parent_id", "parent_edit_version", "child_id", "child_edit_version")
        val columns = LinkedHashMap<String, String>()
        columns["parent_id"] = getDataType(FieldType.String, 36)
        columns["parent_edit_version"] = getDataType(FieldType.Int)
        columns["child_id"] = getDataType(FieldType.String, 36)
        columns["child_edit_version"] = getDataType(FieldType.Int)
        columns["child_attribute_id"] = getDataType(FieldType.Long)
        return createOrAlterProc("jds_pop_entity_binding", "jds_entity_binding", columns, uniqueColumns, false)
    }

    private fun createPopJdsEntityOverview(): String {
        val uniqueColumns = setOf("id", "edit_version")
        val columns = LinkedHashMap<String, String>()
        columns["id"] = getDataType(FieldType.String, 36)
        columns["edit_version"] = getDataType(FieldType.Int)
        columns["entity_id"] = getDataType(FieldType.Int)
        return createOrAlterProc("jds_pop_entity_overview", "jds_entity_overview", columns, uniqueColumns, false)
    }

    private fun createPopJdsRefEntity(): String {
        val uniqueColumns = setOf("id")
        val columns = LinkedHashMap<String, String>()
        columns["id"] = getDataType(FieldType.Int)
        columns["name"] = getDataType(FieldType.String, 64)
        columns["description"] = getDataType(FieldType.String, 256)
        return createOrAlterProc("jds_pop_ref_entity", "jds_ref_entity", columns, uniqueColumns, false)
    }

    private fun createPopJdsRefEntityEnum(): String {
        val uniqueColumns = setOf("entity_id", "field_id")
        val columns = LinkedHashMap<String, String>()
        columns["entity_id"] = getDataType(FieldType.Int)
        columns["field_id"] = getDataType(FieldType.Int)
        return createOrAlterProc("jds_pop_ref_entity_enum", "jds_ref_entity_enum", columns, uniqueColumns, true)
    }

    private fun createPopJdsRefEntityField(): String {
        val uniqueColumns = setOf("entity_id", "field_id")
        val columns = LinkedHashMap<String, String>()
        columns["entity_id"] = getDataType(FieldType.Int)
        columns["field_id"] = getDataType(FieldType.Int)
        return createOrAlterProc("jds_pop_ref_entity_field", "jds_ref_entity_field", columns, uniqueColumns, true)
    }

    private fun createPopJdsRefEntityInheritance(): String {
        val uniqueColumns = setOf("parent_entity_id", "child_entity_id")
        val columns = LinkedHashMap<String, String>()
        columns["parent_entity_id"] = getDataType(FieldType.Int)
        columns["child_entity_id"] = getDataType(FieldType.Int)
        return createOrAlterProc("jds_pop_ref_entity_inheritance", "jds_ref_entity_inheritance", columns, uniqueColumns, true)
    }

    private fun createPopJdsRefEnum(): String {
        val uniqueColumns = setOf("field_id", "seq")
        val columns = LinkedHashMap<String, String>()
        columns["field_id"] = getDataType(FieldType.Int)
        columns["seq"] = getDataType(FieldType.Int)
        columns["name"] = getDataType(FieldType.String, 128)
        columns["caption"] = getDataType(FieldType.String, 128)
        return createOrAlterProc("jds_pop_ref_enum", "jds_ref_enum", columns, uniqueColumns, false)
    }

    private fun createPopJdsRefField(): String {
        val uniqueColumns = setOf("id")
        val columns = LinkedHashMap<String, String>()
        columns["id"] = getDataType(FieldType.Int)
        columns["caption"] = getDataType(FieldType.String, 64)
        columns["description"] = getDataType(FieldType.String, 256)
        columns["field_type_ordinal"] = getDataType(FieldType.Int)
        return createOrAlterProc("jds_pop_ref_field", "jds_ref_field", columns, uniqueColumns, false)
    }

    private fun createPopJdsStoreBlob(): String {
        return createOrAlterProc("jds_pop_blob", "jds_str_blob", getStoreColumns("value" to getDataType(FieldType.Blob)), storeUniqueColumns, false)
    }

    private fun createPopJdsStoreBoolean(): String {
        return createOrAlterProc("jds_pop_boolean", "jds_str_boolean", getStoreColumns("value" to getDataType(FieldType.Boolean)), storeUniqueColumns, false)
    }

    private fun createPopJdsStoreDateTime(): String {
        return createOrAlterProc("jds_pop_date_time", "jds_str_date_time", getStoreColumns("value" to getDataType(FieldType.DateTime)), storeUniqueColumns, false)
    }

    private fun createPopJdsStoreDouble(): String {
        return createOrAlterProc("jds_pop_double", "jds_str_double", getStoreColumns("value" to getDataType(FieldType.Double)), storeUniqueColumns, false)
    }

    private fun createPopDoubleCollection(): String {
        return createOrAlterProc("jds_pop_double_col", "jds_str_double_col", getStoreColumns("value" to getDataType(FieldType.Double)), storeUniqueColumns, false)
    }

    private fun createPopJdsStoreFloat(): String {
        return createOrAlterProc("jds_pop_float", "jds_str_float", getStoreColumns("value" to getDataType(FieldType.Float)), storeUniqueColumns, false)
    }

    private fun createPopJdsStoreShort(): String {
        return createOrAlterProc("jds_pop_short", "jds_str_short", getStoreColumns("value" to getDataType(FieldType.Short)), storeUniqueColumns, false)
    }

    private fun createPopJdsStoreUuid(): String {
        return createOrAlterProc("jds_pop_uuid", "jds_str_uuid", getStoreColumns("value" to getDataType(FieldType.Uuid)), storeUniqueColumns, false)
    }

    private fun createPopFloatCollection(): String {
        return createOrAlterProc("jds_pop_float_col", "jds_str_float_col", getStoreColumns("value" to getDataType(FieldType.Float)), storeUniqueColumns, false)
    }

    private fun createPopJdsStoreInteger(): String {
        return createOrAlterProc("jds_pop_integer", "jds_str_integer", getStoreColumns("value" to getDataType(FieldType.Int)), storeUniqueColumns, false)
    }

    private fun createPopIntegerCollection(): String {
        return createOrAlterProc("jds_pop_integer_col", "jds_str_integer_col", getStoreColumns("value" to getDataType(FieldType.Int)), storeUniqueColumns, false)
    }

    private fun createPopJdsStoreLong(): String {
        return createOrAlterProc("jds_pop_long", "jds_str_long", getStoreColumns("value" to getDataType(FieldType.Long)), storeUniqueColumns, false)
    }

    private fun createPopLongCollection(): String {
        return createOrAlterProc("jds_pop_long_col", "jds_str_long_col", getStoreColumns("value" to getDataType(FieldType.Long)), storeUniqueColumns, false)
    }

    private fun createPopJdsStoreText(): String {
        return createOrAlterProc("jds_pop_text", "jds_str_text", getStoreColumns("value" to getDataType(FieldType.String)), storeUniqueColumns, false)
    }

    private fun createPopTextCollection(): String {
        return createOrAlterProc("jds_pop_text_col", "jds_str_text_col", getStoreColumns("value" to getDataType(FieldType.String)), storeUniqueColumns, false)
    }

    private fun createPopJdsStoreEnum(): String {
        return createOrAlterProc("jds_pop_enum", "jds_str_enum", getStoreColumns("value" to getDataType(FieldType.Int)), storeUniqueColumns, false)
    }

    private fun createPopJdsStoreEnumString(): String {
        return createOrAlterProc("jds_pop_enum_string", "jds_str_enum_string", getStoreColumns("value" to getDataType(FieldType.String)), storeUniqueColumns, false)
    }

    private fun createPopEnumCollection(): String {
        return createOrAlterProc("jds_pop_enum_col", "jds_str_enum_col", getStoreColumns("value" to getDataType(FieldType.Int)), storeUniqueColumns, false)
    }

    private fun createPopEnumStringCollection(): String {
        return createOrAlterProc("jds_pop_enum_string_col", "jds_str_enum_string_col", getStoreColumns("value" to getDataType(FieldType.String)), storeUniqueColumns, false)
    }

    private fun createPopJdsStoreTime(): String {
        return createOrAlterProc("jds_pop_time", "jds_str_time", getStoreColumns("value" to getDataType(FieldType.Time)), storeUniqueColumns, false)
    }

    private fun createPopJdsStoreDate(): String {
        return createOrAlterProc("jds_pop_date", "jds_str_date", getStoreColumns("value" to getDataType(FieldType.Date)), storeUniqueColumns, false)
    }

    private fun createPopJdsStoreDuration(): String {
        return createOrAlterProc("jds_pop_duration", "jds_str_duration", getStoreColumns("value" to getDataType(FieldType.Long)), storeUniqueColumns, false)
    }

    private fun createPopJdsStorePeriod(): String {
        return createOrAlterProc("jds_pop_period", "jds_str_period", getStoreColumns("value" to getDataType(FieldType.String)), storeUniqueColumns, false)
    }

    private fun createPopJdsMonthYear(): String {
        return createOrAlterProc("jds_pop_month_year", "jds_str_month_year", getStoreColumns("value" to getDataType(FieldType.String)), storeUniqueColumns, false)
    }

    private fun createPopJdsMonthDay(): String {
        return createOrAlterProc("jds_pop_month_day", "jds_str_month_day", getStoreColumns("value" to getDataType(FieldType.String)), storeUniqueColumns, false)
    }

    private fun createPopJdsYearMonth(): String {
        return createOrAlterProc("jds_pop_year_month", "jds_str_year_month", getStoreColumns("value" to getDataType(FieldType.String)), storeUniqueColumns, false)
    }

    private fun createPopJdsStoreZonedDateTime(): String {
        return createOrAlterProc("jds_pop_zoned_date_time", "jds_str_zoned_date_time", getStoreColumns("value" to getDataType(FieldType.ZonedDateTime)), storeUniqueColumns, false)
    }

    private fun createPopEntityLiveVersion(): String {
        val columns = LinkedHashMap<String, String>()
        columns["id"] = getDataType(FieldType.String, 36)
        //don't include edit_version column, a separate SQL statement updates that column
        return createOrAlterProc("jds_pop_entity_live_version", "jds_entity_live_version", columns, setOf("id"), false)
    }

    private fun createEntityLiveVersionTable(): String {
        val tableName = "jds_entity_live_version"
        val columns = LinkedHashMap<String, String>()
        columns["id"] = getDataType(FieldType.String, 36)
        columns["edit_version"] = getDataType(FieldType.Int)
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_u"] = "id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("id, edit_version" to "$dimensionTable(id, edit_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreBlob(): String {
        val tableName = "jds_str_blob"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("id, edit_version" to "$dimensionTable(id, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(FieldType.Blob)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreBoolean(): String {
        val tableName = "jds_str_boolean"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("id, edit_version" to "$dimensionTable(id, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(FieldType.Boolean)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreDate(): String {
        val tableName = "jds_str_date"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("id, edit_version" to "$dimensionTable(id, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(FieldType.Date)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreDateTime(): String {
        val tableName = "jds_str_date_time"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("id, edit_version" to "$dimensionTable(id, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(FieldType.DateTime)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreDateTimeCollection(): String {
        val tableName = "jds_str_date_time_col"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("id, edit_version" to "$dimensionTable(id, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(FieldType.DateTime)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreDouble(): String {
        val tableName = "jds_str_double"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("id, edit_version" to "$dimensionTable(id, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(FieldType.Double)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreDoubleCollection(): String {
        val tableName = "jds_str_double_col"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("id, edit_version" to "$dimensionTable(id, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(FieldType.Double)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreDuration(): String {
        val tableName = "jds_str_duration"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("id, edit_version" to "$dimensionTable(id, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(FieldType.Long)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreEnum(): String {
        val tableName = "jds_str_enum"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("id, edit_version" to "$dimensionTable(id, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(FieldType.Int)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreEnumString(): String {
        val tableName = "jds_str_enum_string"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("id, edit_version" to "$dimensionTable(id, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(FieldType.String)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreEnumCollection(): String {
        val tableName = "jds_str_enum_col"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("id, edit_version" to "$dimensionTable(id, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(FieldType.Int)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreEnumStringCollection(): String {
        val tableName = "jds_str_enum_string_col"//Oracle length
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_u"] = "id, edit_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("id, edit_version" to "$dimensionTable(id, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(FieldType.String)), uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreFloat(): String {
        val tableName = "jds_str_float"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("id, edit_version" to "$dimensionTable(id, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(FieldType.Float)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreShort(): String {
        val tableName = "jds_str_short"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("id, edit_version" to "$dimensionTable(id, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(FieldType.Short)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreUuid(): String {
        val tableName = "jds_str_uuid"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("id, edit_version" to "$dimensionTable(id, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(FieldType.Uuid)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreFloatCollection(): String {
        val tableName = "jds_str_float_col"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("id, edit_version" to "$dimensionTable(id, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(FieldType.Float)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreInteger(): String {
        val tableName = "jds_str_integer"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("id, edit_version" to "$dimensionTable(id, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(FieldType.Int)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreIntegerCollection(): String {
        val tableName = "jds_str_integer_col"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("id, edit_version" to "$dimensionTable(id, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(FieldType.Int)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreLong(): String {
        val tableName = "jds_str_long"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("id, edit_version" to "$dimensionTable(id, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(FieldType.Long)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreLongCollection(): String {
        val tableName = "jds_str_long_col"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("id, edit_version" to "$dimensionTable(id, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(FieldType.Long)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreMonthDay(): String {
        val tableName = "jds_str_month_day"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("id, edit_version" to "$dimensionTable(id, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(FieldType.String)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStorePeriod(): String {
        val tableName = "jds_str_period"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("id, edit_version" to "$dimensionTable(id, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(FieldType.String)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreText(): String {
        val tableName = "jds_str_text"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("id, edit_version" to "$dimensionTable(id, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(FieldType.String)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreTextCollection(): String {
        val tableName = "jds_str_text_col"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("id, edit_version" to "$dimensionTable(id, edit_version)")
        val sql=createTable(tableName, getStoreColumns("value" to getDataType(FieldType.String)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
        return sql
    }

    private fun createStoreTime(): String {
        val tableName = "jds_str_time"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("id, edit_version" to "$dimensionTable(id, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(FieldType.Time)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreYearMonth(): String {
        val tableName = "jds_str_year_month"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("id, edit_version" to "$dimensionTable(id, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(FieldType.String)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreZonedDateTime(): String {
        val tableName = "jds_str_zoned_date_time"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("id, edit_version" to "$dimensionTable(id, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(FieldType.ZonedDateTime)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createEntityBinding(): String {
        val tableName = "jds_entity_binding"
        val columns = linkedMapOf(
                "parent_id" to getDataTypeImpl(FieldType.String, 36),
                "parent_edit_version" to getDataTypeImpl(FieldType.Int),
                "child_id" to getDataTypeImpl(FieldType.String, 36),
                "child_edit_version" to getDataTypeImpl(FieldType.Int),
                "child_attribute_id" to getDataTypeImpl(FieldType.Int)
        )
        val uniqueColumns = linkedMapOf("${tableName}_uk" to "parent_id, parent_edit_version, child_id, child_edit_version")
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        if (implementation != Implementation.TSql) {
            foreignKeys["${tableName}_fk_1"] = linkedMapOf("parent_id, parent_edit_version" to "jds_entity_overview (id, edit_version)")
            foreignKeys["${tableName}_fk_2"] = linkedMapOf("child_id, child_edit_version" to "jds_entity_overview (id, edit_version)")
        }
        return createTable(tableName, columns, uniqueColumns, HashMap(), foreignKeys)
    }

    private fun createRefEntityOverview(): String {
        val tableName = "jds_entity_overview"
        val columns = linkedMapOf(
                "id" to getDataTypeImpl(FieldType.String, 36),
                "edit_version" to getDataTypeImpl(FieldType.Int),
                "entity_id" to getDataTypeImpl(FieldType.Int)
        )
        val primaryKey = linkedMapOf("jds_entity_overview_uk" to "id, edit_version")
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_fk_1"] = linkedMapOf("entity_id" to "jds_ref_entity (id)")
        return createTable(tableName, columns, HashMap(), primaryKey, foreignKeys)
    }

    private fun createStoreEntities(): String {
        val tableName = "jds_ref_entity"
        val columns = linkedMapOf(
                "id" to getDataTypeImpl(FieldType.Int),
                "name" to getDataTypeImpl(FieldType.String, 64),
                "description" to getDataTypeImpl(FieldType.String, 256)
        )
        val primaryKey = linkedMapOf("jds_ref_entity_pk" to "id")
        return createTable(tableName, columns, HashMap(), primaryKey, LinkedHashMap())
    }

    private fun createRefEnumValues(): String {
        val tableName = "jds_ref_enum"
        val columns = linkedMapOf(
                "field_id" to getDataTypeImpl(FieldType.Int),
                "seq" to getDataTypeImpl(FieldType.Int),
                "name" to getDataTypeImpl(FieldType.String, 128),
                "caption" to getDataTypeImpl(FieldType.String, 128)
        )
        val primaryKey = linkedMapOf("jds_ref_enum_pk" to "field_id, seq")
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_fk_1"] = linkedMapOf("field_id" to "jds_ref_field (id)")
        return createTable(tableName, columns, HashMap(), primaryKey, foreignKeys)
    }

    private fun createRefFields(): String {
        val tableName = "jds_ref_field"
        val columns = linkedMapOf(
                "id" to getDataTypeImpl(FieldType.Int),
                "caption" to getDataTypeImpl(FieldType.String, 64),
                "description" to getDataTypeImpl(FieldType.String, 256),
                "field_type_ordinal" to getDataTypeImpl(FieldType.Int)
        )
        val primaryKey = linkedMapOf("jds_ref_field_pk" to "id")
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_fk_1"] = linkedMapOf("field_type_ordinal" to "jds_ref_field_type (ordinal)")
        return createTable(tableName, columns, HashMap(), primaryKey, foreignKeys)
    }

    private fun createRefFieldTypes(): String {
        val tableName = "jds_ref_field_type"
        val columns = linkedMapOf(
                "ordinal" to getDataTypeImpl(FieldType.Int),
                "caption" to getDataTypeImpl(FieldType.String, 64)
        )
        val primaryKey = linkedMapOf("jds_ref_field_type_pk" to "ordinal")
        return createTable(tableName, columns, HashMap(), primaryKey, LinkedHashMap())
    }

    private fun createBindEntityFields(): String {
        val tableName = "jds_ref_entity_field"
        val columns = linkedMapOf(
                "entity_id" to getDataTypeImpl(FieldType.Int),
                "field_id" to getDataTypeImpl(FieldType.Int)
        )
        val primaryKey = linkedMapOf("jds_ref_entity_field_pk" to "entity_id, field_id")
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_fk_1"] = linkedMapOf("entity_id" to "jds_ref_entity (id)")
        foreignKeys["${tableName}_fk_2"] = linkedMapOf("field_id" to "jds_ref_field (id)")
        return createTable(tableName, columns, HashMap(), primaryKey, foreignKeys)
    }

    private fun createBindEntityEnums(): String {
        val tableName = "jds_ref_entity_enum"
        val columns = linkedMapOf(
                "entity_id" to getDataTypeImpl(FieldType.Int),
                "field_id" to getDataTypeImpl(FieldType.Int)
        )
        val primaryKey = linkedMapOf("jds_ref_entity_enum_pk" to "entity_id, field_id")
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_fk_1"] = linkedMapOf("entity_id" to "jds_ref_entity (id)")
        foreignKeys["${tableName}_fk_2"] = linkedMapOf("field_id" to "jds_ref_field (id)")
        return createTable(tableName, columns, HashMap(), primaryKey, foreignKeys)
    }

    private fun createRefInheritance(): String {
        val tableName = "jds_ref_entity_inheritance"
        val columns = linkedMapOf(
                "parent_entity_id" to getDataTypeImpl(FieldType.Int),
                "child_entity_id" to getDataTypeImpl(FieldType.Int)
        )
        val primaryKey = linkedMapOf("jds_ref_entity_inheritance_pk" to "parent_entity_id, child_entity_id")
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        if (implementation != Implementation.TSql) {
            foreignKeys["${tableName}_fk_1"] = linkedMapOf("parent_entity_id" to "jds_ref_entity (id)")
            foreignKeys["${tableName}_fk_2"] = linkedMapOf("child_entity_id" to "jds_ref_entity (id)")
        }
        return createTable(tableName, columns, HashMap(), primaryKey, foreignKeys)
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

    override fun getDataType(fieldType: FieldType) = getDataType(fieldType, 0)

    override fun getDataType(fieldType: FieldType, max: Int) = getDataTypeImpl(fieldType, max)

    /**
     * Gets the underlying database type of the supplied [io.github.subiyacryolite.jds.Field]
     * @param fieldType the supplied [io.github.subiyacryolite.jds.Field]
     * @param max the maximum length of the database type, applied against [io.github.subiyacryolite.jds.enums.FieldType.String] and [io.github.subiyacryolite.jds.enums.FieldType.Blob] types
     * @return the underlying database type of the supplied [io.github.subiyacryolite.jds.Field]
     */
    protected abstract fun getDataTypeImpl(fieldType: FieldType, max: Int = 0): String

    companion object {

        private val storeUniqueColumns = setOf("id", "edit_version", "field_id")
    }
}
