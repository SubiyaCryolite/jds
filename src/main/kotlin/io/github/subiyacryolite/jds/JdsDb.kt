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
import io.github.subiyacryolite.jds.enums.JdsFieldType
import io.github.subiyacryolite.jds.enums.JdsImplementation
import io.github.subiyacryolite.jds.enums.JdsProcedureComponent
import io.github.subiyacryolite.jds.enums.JdsTableComponent
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
        prepareJdsComponent(connection, JdsTableComponent.RefEntities)
        prepareJdsComponent(connection, JdsTableComponent.RefFieldTypes)
        prepareJdsComponent(connection, JdsTableComponent.RedFields)
        prepareJdsComponent(connection, JdsTableComponent.RefEnumValues)
        prepareJdsComponent(connection, JdsTableComponent.RefInheritance)
        prepareJdsComponent(connection, JdsTableComponent.EntityOverview)
        prepareJdsComponent(connection, JdsTableComponent.EntityBinding)
        prepareJdsComponent(connection, JdsTableComponent.RefEntityField)
        prepareJdsComponent(connection, JdsTableComponent.RefEntityEnums)
        prepareJdsComponent(connection, JdsTableComponent.StoreText)
        prepareJdsComponent(connection, JdsTableComponent.StoreTextCollection)
        prepareJdsComponent(connection, JdsTableComponent.StoreBlob)
        prepareJdsComponent(connection, JdsTableComponent.StoreEnum)
        prepareJdsComponent(connection, JdsTableComponent.StoreEnumString)
        prepareJdsComponent(connection, JdsTableComponent.StoreEnumCollection)
        prepareJdsComponent(connection, JdsTableComponent.StoreEnumStringCollection)
        prepareJdsComponent(connection, JdsTableComponent.StoreShort)
        prepareJdsComponent(connection, JdsTableComponent.StoreFloat)
        prepareJdsComponent(connection, JdsTableComponent.StoreFloatCollection)
        prepareJdsComponent(connection, JdsTableComponent.StoreInteger)
        prepareJdsComponent(connection, JdsTableComponent.StoreIntegerCollection)
        prepareJdsComponent(connection, JdsTableComponent.StoreDate)
        prepareJdsComponent(connection, JdsTableComponent.StoreLong)
        prepareJdsComponent(connection, JdsTableComponent.StoreLongCollection)
        prepareJdsComponent(connection, JdsTableComponent.StoreDouble)
        prepareJdsComponent(connection, JdsTableComponent.StoreDoubleCollection)
        prepareJdsComponent(connection, JdsTableComponent.StoreDateTime)
        prepareJdsComponent(connection, JdsTableComponent.StoreDateTimeCollection)
        prepareJdsComponent(connection, JdsTableComponent.StoreZonedDateTime)
        prepareJdsComponent(connection, JdsTableComponent.StoreTime)
        prepareJdsComponent(connection, JdsTableComponent.StorePeriod)
        prepareJdsComponent(connection, JdsTableComponent.StoreDuration)
        prepareJdsComponent(connection, JdsTableComponent.StoreYearMonth)
        prepareJdsComponent(connection, JdsTableComponent.StoreMonthDay)
        prepareJdsComponent(connection, JdsTableComponent.StoreBoolean)
        prepareJdsComponent(connection, JdsTableComponent.StoreUuid)
        prepareJdsComponent(connection, JdsTableComponent.EntityLiveVersion)
        if (supportsStatements) {
            prepareJdsComponent(connection, JdsProcedureComponent.PopStoreBoolean)
            prepareJdsComponent(connection, JdsProcedureComponent.PopStoreBlob)
            prepareJdsComponent(connection, JdsProcedureComponent.PopStoreText)
            prepareJdsComponent(connection, JdsProcedureComponent.PopStoreLong)
            prepareJdsComponent(connection, JdsProcedureComponent.PopStoreInteger)
            prepareJdsComponent(connection, JdsProcedureComponent.PopStoreFloat)
            prepareJdsComponent(connection, JdsProcedureComponent.PopStoreShort)
            prepareJdsComponent(connection, JdsProcedureComponent.PopStoreUuid)
            prepareJdsComponent(connection, JdsProcedureComponent.PopStoreDouble)
            prepareJdsComponent(connection, JdsProcedureComponent.PopStoreDateTime)
            prepareJdsComponent(connection, JdsProcedureComponent.PopStoreTime)
            prepareJdsComponent(connection, JdsProcedureComponent.PopStoreDate)
            prepareJdsComponent(connection, JdsProcedureComponent.PopStoreDuration)
            prepareJdsComponent(connection, JdsProcedureComponent.PopStorePeriod)
            prepareJdsComponent(connection, JdsProcedureComponent.PopStoreMonthYear)
            prepareJdsComponent(connection, JdsProcedureComponent.PopStoreMonthDay)
            prepareJdsComponent(connection, JdsProcedureComponent.PopStoreYearMonth)
            prepareJdsComponent(connection, JdsProcedureComponent.PopStoreEnum)
            prepareJdsComponent(connection, JdsProcedureComponent.PopStoreEnumString)
            prepareJdsComponent(connection, JdsProcedureComponent.PopStoreZonedDateTime)
            prepareJdsComponent(connection, JdsProcedureComponent.PopEntityOverview)
            prepareJdsComponent(connection, JdsProcedureComponent.PopEntityBinding)
            prepareJdsComponent(connection, JdsProcedureComponent.PopRefEntityField)
            prepareJdsComponent(connection, JdsProcedureComponent.PopRefEntityEnum)
            prepareJdsComponent(connection, JdsProcedureComponent.PopRefEntity)
            prepareJdsComponent(connection, JdsProcedureComponent.PopRefEnum)
            prepareJdsComponent(connection, JdsProcedureComponent.PopRefField)
            prepareJdsComponent(connection, JdsProcedureComponent.PopRefEntityInheritance)
            prepareJdsComponent(connection, JdsProcedureComponent.PopEntityLiveVersion)
            prepareJdsComponent(connection, JdsProcedureComponent.PopStoreTextCollection)
            prepareJdsComponent(connection, JdsProcedureComponent.PopStoreEnumCollection)
            prepareJdsComponent(connection, JdsProcedureComponent.PopStoreEnumStringCollection)
            prepareJdsComponent(connection, JdsProcedureComponent.PopStoreFloatCollection)
            prepareJdsComponent(connection, JdsProcedureComponent.PopStoreIntegerCollection)
            prepareJdsComponent(connection, JdsProcedureComponent.PopStoreLongCollection)
            prepareJdsComponent(connection, JdsProcedureComponent.PopStoreDoubleCollection)
        }
    }

    val isOracleDb: Boolean
        get() = implementation === JdsImplementation.Oracle

    val isTransactionalSqlDb: Boolean
        get() = implementation === JdsImplementation.TSql

    val isMySqlDb: Boolean
        get() = implementation === JdsImplementation.MySql || implementation === JdsImplementation.MariaDb

    val isSqLiteDb: Boolean
        get() = implementation === JdsImplementation.SqLite

    val isPosgreSqlDb: Boolean
        get() = implementation === JdsImplementation.Postgres

    /**
     * Delegates the creation of custom database components depending on the
     * underlying JDS Database implementation
     * @param connection the connection to use for this operation
     * @param jdsTableComponent an enum that maps to the components concrete
     * implementation details
     */
    private fun prepareJdsComponent(connection: Connection, jdsTableComponent: JdsTableComponent) {
        if (!doesTableExist(connection, jdsTableComponent.component)) {
            initiateDatabaseComponent(connection, jdsTableComponent)
        }
    }

    /**
     * Delegates the creation of custom database components depending on the
     * underlying JDS Database implementation
     * @param connection the connection to use for this operation
     * @param jdsProcedureComponent an enum that maps to the components concrete
     * implementation details
     */
    private fun prepareJdsComponent(connection: Connection, jdsProcedureComponent: JdsProcedureComponent) {
        if (!doesProcedureExist(connection, jdsProcedureComponent.component)) {
            initiateDatabaseComponent(connection, jdsProcedureComponent)
        }
    }

    /**
     * Initialises core JDS Database components
     *
     * @param connection the SQL connection top use for this operation
     * @param jdsTableComponent an enum that maps to the components concrete implementation
     */
    private fun initiateDatabaseComponent(connection: Connection, jdsTableComponent: JdsTableComponent) {
        when (jdsTableComponent) {
            JdsTableComponent.EntityBinding -> createBindEntityBinding(connection)
            JdsTableComponent.EntityLiveVersion -> executeSqlFromString(connection, createEntityLiveVersionTable())
            JdsTableComponent.EntityOverview -> createRefEntityOverview(connection)
            JdsTableComponent.RefEntities -> createStoreEntities(connection)
            JdsTableComponent.RefEntityEnums -> createBindEntityEnums(connection)
            JdsTableComponent.RefEntityField -> createBindEntityFields(connection)
            JdsTableComponent.RefEnumValues -> createRefEnumValues(connection)
            JdsTableComponent.RefFieldTypes -> {
                createRefFieldTypes(connection)
                populateFieldTypes(connection)
            }
            JdsTableComponent.RedFields -> createRefFields(connection)
            JdsTableComponent.RefInheritance -> createRefInheritance(connection)
            JdsTableComponent.StoreBlob -> executeSqlFromString(connection, createStoreBlob())
            JdsTableComponent.StoreBoolean -> executeSqlFromString(connection, createStoreBoolean())
            JdsTableComponent.StoreDate -> executeSqlFromString(connection, createStoreDate())
            JdsTableComponent.StoreDateTime -> executeSqlFromString(connection, createStoreDateTime())
            JdsTableComponent.StoreDateTimeCollection -> executeSqlFromString(connection, createStoreDateTimeCollection())
            JdsTableComponent.StoreDouble -> executeSqlFromString(connection, createStoreDouble())
            JdsTableComponent.StoreDoubleCollection -> executeSqlFromString(connection, createStoreDoubleCollection())
            JdsTableComponent.StoreDuration -> executeSqlFromString(connection, createStoreDuration())
            JdsTableComponent.StoreEnum -> executeSqlFromString(connection, createStoreEnum())
            JdsTableComponent.StoreEnumString -> executeSqlFromString(connection, createStoreEnumString())
            JdsTableComponent.StoreEnumCollection -> executeSqlFromString(connection, createStoreEnumCollection())
            JdsTableComponent.StoreEnumStringCollection -> executeSqlFromString(connection, createStoreEnumStringCollection())
            JdsTableComponent.StoreFloat -> executeSqlFromString(connection, createStoreFloat())
            JdsTableComponent.StoreShort -> executeSqlFromString(connection, createStoreShort())
            JdsTableComponent.StoreUuid -> executeSqlFromString(connection, createStoreUuid())
            JdsTableComponent.StoreFloatCollection -> executeSqlFromString(connection, createStoreFloatCollection())
            JdsTableComponent.StoreInteger -> executeSqlFromString(connection, createStoreInteger())
            JdsTableComponent.StoreIntegerCollection -> executeSqlFromString(connection, createStoreIntegerCollection())
            JdsTableComponent.StoreLong -> executeSqlFromString(connection, createStoreLong())
            JdsTableComponent.StoreLongCollection -> executeSqlFromString(connection, createStoreLongCollection())
            JdsTableComponent.StoreMonthDay -> executeSqlFromString(connection, createStoreMonthDay())
            JdsTableComponent.StorePeriod -> executeSqlFromString(connection, createStorePeriod())
            JdsTableComponent.StoreText -> executeSqlFromString(connection, createStoreText())
            JdsTableComponent.StoreTextCollection -> executeSqlFromString(connection, createStoreTextCollection())
            JdsTableComponent.StoreTime -> executeSqlFromString(connection, createStoreTime())
            JdsTableComponent.StoreYearMonth -> executeSqlFromString(connection, createStoreYearMonth())
            JdsTableComponent.StoreZonedDateTime -> executeSqlFromString(connection, createStoreZonedDateTime())
        }
    }

    /**
     * Initialises core JDS Database components
     *
     * @param connection   the SQL connection top use for this operation
     * @param jdsTableComponent an enum that maps to the components concrete implementation
     */
    private fun initiateDatabaseComponent(connection: Connection, jdsTableComponent: JdsProcedureComponent) {
        when (jdsTableComponent) {
            JdsProcedureComponent.PopEntityBinding -> executeSqlFromString(connection, createPopJdsEntityBinding())
            JdsProcedureComponent.PopEntityLiveVersion -> executeSqlFromString(connection, createPopEntityLiveVersion())
            JdsProcedureComponent.PopEntityOverview -> executeSqlFromString(connection, createPopJdsEntityOverview())
            JdsProcedureComponent.PopRefEntity -> executeSqlFromString(connection, createPopJdsRefEntity())
            JdsProcedureComponent.PopRefEntityEnum -> executeSqlFromString(connection, createPopJdsRefEntityEnum())
            JdsProcedureComponent.PopRefEntityField -> executeSqlFromString(connection, createPopJdsRefEntityField())
            JdsProcedureComponent.PopRefEntityInheritance -> executeSqlFromString(connection, createPopJdsRefEntityInheritance())
            JdsProcedureComponent.PopRefEnum -> executeSqlFromString(connection, createPopJdsRefEnum())
            JdsProcedureComponent.PopRefField -> executeSqlFromString(connection, createPopJdsRefField())
            JdsProcedureComponent.PopStoreBlob -> executeSqlFromString(connection, createPopJdsStoreBlob())
            JdsProcedureComponent.PopStoreBoolean -> executeSqlFromString(connection, createPopJdsStoreBoolean())
            JdsProcedureComponent.PopStoreDate -> executeSqlFromString(connection, createPopJdsStoreDate())
            JdsProcedureComponent.PopStoreDateTime -> executeSqlFromString(connection, createPopJdsStoreDateTime())
            JdsProcedureComponent.PopStoreDouble -> executeSqlFromString(connection, createPopJdsStoreDouble())
            JdsProcedureComponent.PopStoreDoubleCollection -> executeSqlFromString(connection, createPopDoubleCollection())
            JdsProcedureComponent.PopStoreDuration -> executeSqlFromString(connection, createPopJdsStoreDuration())
            JdsProcedureComponent.PopStoreEnum -> executeSqlFromString(connection, createPopJdsStoreEnum())
            JdsProcedureComponent.PopStoreEnumString -> executeSqlFromString(connection, createPopJdsStoreEnumString())
            JdsProcedureComponent.PopStoreEnumCollection -> executeSqlFromString(connection, createPopEnumCollection())
            JdsProcedureComponent.PopStoreEnumStringCollection -> executeSqlFromString(connection, createPopEnumStringCollection())
            JdsProcedureComponent.PopStoreFloat -> executeSqlFromString(connection, createPopJdsStoreFloat())
            JdsProcedureComponent.PopStoreShort -> executeSqlFromString(connection, createPopJdsStoreShort())
            JdsProcedureComponent.PopStoreUuid -> executeSqlFromString(connection, createPopJdsStoreUuid())
            JdsProcedureComponent.PopStoreFloatCollection -> executeSqlFromString(connection, createPopFloatCollection())
            JdsProcedureComponent.PopStoreInteger -> executeSqlFromString(connection, createPopJdsStoreInteger())
            JdsProcedureComponent.PopStoreIntegerCollection -> executeSqlFromString(connection, createPopIntegerCollection())
            JdsProcedureComponent.PopStoreLong -> executeSqlFromString(connection, createPopJdsStoreLong())
            JdsProcedureComponent.PopStoreLongCollection -> executeSqlFromString(connection, createPopLongCollection())
            JdsProcedureComponent.PopStoreMonthDay -> executeSqlFromString(connection, createPopJdsMonthDay())
            JdsProcedureComponent.PopStoreMonthYear -> executeSqlFromString(connection, createPopJdsMonthYear())
            JdsProcedureComponent.PopStorePeriod -> executeSqlFromString(connection, createPopJdsStorePeriod())
            JdsProcedureComponent.PopStoreText -> executeSqlFromString(connection, createPopJdsStoreText())
            JdsProcedureComponent.PopStoreTextCollection -> executeSqlFromString(connection, createPopTextCollection())
            JdsProcedureComponent.PopStoreTime -> executeSqlFromString(connection, createPopJdsStoreTime())
            JdsProcedureComponent.PopStoreYearMonth -> executeSqlFromString(connection, createPopJdsYearMonth())
            JdsProcedureComponent.PopStoreZonedDateTime -> executeSqlFromString(connection, createPopJdsStoreZonedDateTime())
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
                if (parentEntity != entityCode) {
                    statement.setLong(1, parentEntity)
                    statement.setLong(2, entityCode)
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
            if (options.logOutput)
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
                        if (options.logOutput)
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
    internal open fun populateRefEnum() = "{call jds_pop_ref_enum(?,?,?,?)}"

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
        false -> JdsFieldType.Unknown
    }

    private fun getStoreUniqueColumns(tableName: String) = linkedMapOf("${tableName}_u" to "uuid, edit_version, field_id")

    private fun getStoreColumns(kvp: Pair<String, String>): LinkedHashMap<String, String> {
        return linkedMapOf(
                "uuid" to getDataType(JdsFieldType.String, 36),
                "edit_version" to getDataType(JdsFieldType.Int),
                "field_id" to getDataType(JdsFieldType.Long),
                kvp.first to kvp.second
        )
    }

    private fun createPopJdsEntityBinding(): String {
        val uniqueColumns = setOf("parent_uuid", "parent_edit_version", "child_uuid", "child_edit_version")
        val columns = LinkedHashMap<String, String>()
        columns["parent_uuid"] = getDataType(JdsFieldType.String, 36)
        columns["parent_edit_version"] = getDataType(JdsFieldType.Int)
        columns["child_uuid"] = getDataType(JdsFieldType.String, 36)
        columns["child_edit_version"] = getDataType(JdsFieldType.Int)
        columns["child_attribute_id"] = getDataType(JdsFieldType.Long)
        return createOrAlterProc("jds_pop_entity_binding", "jds_entity_binding", columns, uniqueColumns, false)
    }

    private fun createPopJdsEntityOverview(): String {
        val uniqueColumns = setOf("uuid", "edit_version")
        val columns = LinkedHashMap<String, String>()
        columns["uuid"] = getDataType(JdsFieldType.String, 36)
        columns["edit_version"] = getDataType(JdsFieldType.Int)
        columns["entity_id"] = getDataType(JdsFieldType.Long)
        return createOrAlterProc("jds_pop_entity_overview", "jds_entity_overview", columns, uniqueColumns, false)
    }

    private fun createPopJdsRefEntity(): String {
        val uniqueColumns = setOf("id")
        val columns = LinkedHashMap<String, String>()
        columns["id"] = getDataType(JdsFieldType.Long)
        columns["name"] = getDataType(JdsFieldType.String, 64)
        columns["caption"] = getDataType(JdsFieldType.String, 64)
        columns["description"] = getDataType(JdsFieldType.String, 256)
        return createOrAlterProc("jds_pop_ref_entity", "jds_ref_entity", columns, uniqueColumns, false)
    }

    private fun createPopJdsRefEntityEnum(): String {
        val uniqueColumns = setOf("entity_id", "field_id")
        val columns = LinkedHashMap<String, String>()
        columns["entity_id"] = getDataType(JdsFieldType.Long)
        columns["field_id"] = getDataType(JdsFieldType.Long)
        return createOrAlterProc("jds_pop_ref_entity_enum", "jds_ref_entity_enum", columns, uniqueColumns, true)
    }

    private fun createPopJdsRefEntityField(): String {
        val uniqueColumns = setOf("entity_id", "field_id")
        val columns = LinkedHashMap<String, String>()
        columns["entity_id"] = getDataType(JdsFieldType.Long)
        columns["field_id"] = getDataType(JdsFieldType.Long)
        return createOrAlterProc("jds_pop_ref_entity_field", "jds_ref_entity_field", columns, uniqueColumns, true)
    }

    private fun createPopJdsRefEntityInheritance(): String {
        val uniqueColumns = setOf("parent_entity_id", "child_entity_id")
        val columns = LinkedHashMap<String, String>()
        columns["parent_entity_id"] = getDataType(JdsFieldType.Long)
        columns["child_entity_id"] = getDataType(JdsFieldType.Long)
        return createOrAlterProc("jds_pop_ref_entity_inheritance", "jds_ref_entity_inheritance", columns, uniqueColumns, true)
    }

    private fun createPopJdsRefEnum(): String {
        val uniqueColumns = setOf("field_id", "seq")
        val columns = LinkedHashMap<String, String>()
        columns["field_id"] = getDataType(JdsFieldType.Long)
        columns["seq"] = getDataType(JdsFieldType.Int)
        columns["name"] = getDataType(JdsFieldType.String, 128)
        columns["caption"] = getDataType(JdsFieldType.String, 128)
        return createOrAlterProc("jds_pop_ref_enum", "jds_ref_enum", columns, uniqueColumns, false)
    }

    private fun createPopJdsRefField(): String {
        val uniqueColumns = setOf("id")
        val columns = LinkedHashMap<String, String>()
        columns["id"] = getDataType(JdsFieldType.Long)
        columns["caption"] = getDataType(JdsFieldType.String, 64)
        columns["description"] = getDataType(JdsFieldType.String, 256)
        columns["type_ordinal"] = getDataType(JdsFieldType.Int)
        return createOrAlterProc("jds_pop_ref_field", "jds_ref_field", columns, uniqueColumns, false)
    }

    private fun createPopJdsStoreBlob(): String {
        return createOrAlterProc("jds_pop_blob", "jds_str_blob", getStoreColumns("value" to getDataType(JdsFieldType.Blob)), storeUniqueColumns, false)
    }

    private fun createPopJdsStoreBoolean(): String {
        return createOrAlterProc("jds_pop_boolean", "jds_str_boolean", getStoreColumns("value" to getDataType(JdsFieldType.Boolean)), storeUniqueColumns, false)
    }

    private fun createPopJdsStoreDateTime(): String {
        return createOrAlterProc("jds_pop_date_time", "jds_str_date_time", getStoreColumns("value" to getDataType(JdsFieldType.DateTime)), storeUniqueColumns, false)
    }

    private fun createPopJdsStoreDouble(): String {
        return createOrAlterProc("jds_pop_double", "jds_str_double", getStoreColumns("value" to getDataType(JdsFieldType.Double)), storeUniqueColumns, false)
    }

    private fun createPopDoubleCollection(): String {
        return createOrAlterProc("jds_pop_double_col", "jds_str_double_col", getStoreColumns("value" to getDataType(JdsFieldType.Double)), storeUniqueColumns, false)
    }

    private fun createPopJdsStoreFloat(): String {
        return createOrAlterProc("jds_pop_float", "jds_str_float", getStoreColumns("value" to getDataType(JdsFieldType.Float)), storeUniqueColumns, false)
    }

    private fun createPopJdsStoreShort(): String {
        return createOrAlterProc("jds_pop_short", "jds_str_short", getStoreColumns("value" to getDataType(JdsFieldType.Short)), storeUniqueColumns, false)
    }

    private fun createPopJdsStoreUuid(): String {
        return createOrAlterProc("jds_pop_uuid", "jds_str_uuid", getStoreColumns("value" to getDataType(JdsFieldType.Uuid)), storeUniqueColumns, false)
    }

    private fun createPopFloatCollection(): String {
        return createOrAlterProc("jds_pop_float_col", "jds_str_float_col", getStoreColumns("value" to getDataType(JdsFieldType.Float)), storeUniqueColumns, false)
    }

    private fun createPopJdsStoreInteger(): String {
        return createOrAlterProc("jds_pop_integer", "jds_str_integer", getStoreColumns("value" to getDataType(JdsFieldType.Int)), storeUniqueColumns, false)
    }

    private fun createPopIntegerCollection(): String {
        return createOrAlterProc("jds_pop_integer_col", "jds_str_integer_col", getStoreColumns("value" to getDataType(JdsFieldType.Int)), storeUniqueColumns, false)
    }

    private fun createPopJdsStoreLong(): String {
        return createOrAlterProc("jds_pop_long", "jds_str_long", getStoreColumns("value" to getDataType(JdsFieldType.Long)), storeUniqueColumns, false)
    }

    private fun createPopLongCollection(): String {
        return createOrAlterProc("jds_pop_long_col", "jds_str_long_col", getStoreColumns("value" to getDataType(JdsFieldType.Long)), storeUniqueColumns, false)
    }

    private fun createPopJdsStoreText(): String {
        return createOrAlterProc("jds_pop_text", "jds_str_text", getStoreColumns("value" to getDataType(JdsFieldType.String)), storeUniqueColumns, false)
    }

    private fun createPopTextCollection(): String {
        return createOrAlterProc("jds_pop_text_col", "jds_str_text_col", getStoreColumns("value" to getDataType(JdsFieldType.String)), storeUniqueColumns, false)
    }

    private fun createPopJdsStoreEnum(): String {
        return createOrAlterProc("jds_pop_enum", "jds_str_enum", getStoreColumns("value" to getDataType(JdsFieldType.Int)), storeUniqueColumns, false)
    }

    private fun createPopJdsStoreEnumString(): String {
        return createOrAlterProc("jds_pop_enum_string", "jds_str_enum_string", getStoreColumns("value" to getDataType(JdsFieldType.String)), storeUniqueColumns, false)
    }

    private fun createPopEnumCollection(): String {
        return createOrAlterProc("jds_pop_enum_col", "jds_str_enum_col", getStoreColumns("value" to getDataType(JdsFieldType.Int)), storeUniqueColumns, false)
    }

    private fun createPopEnumStringCollection(): String {
        return createOrAlterProc("jds_pop_enum_string_col", "jds_str_enum_string_col", getStoreColumns("value" to getDataType(JdsFieldType.String)), storeUniqueColumns, false)
    }

    private fun createPopJdsStoreTime(): String {
        return createOrAlterProc("jds_pop_time", "jds_str_time", getStoreColumns("value" to getDataType(JdsFieldType.Time)), storeUniqueColumns, false)
    }

    private fun createPopJdsStoreDate(): String {
        return createOrAlterProc("jds_pop_date", "jds_str_date", getStoreColumns("value" to getDataType(JdsFieldType.Date)), storeUniqueColumns, false)
    }

    private fun createPopJdsStoreDuration(): String {
        return createOrAlterProc("jds_pop_duration", "jds_str_duration", getStoreColumns("value" to getDataType(JdsFieldType.Long)), storeUniqueColumns, false)
    }

    private fun createPopJdsStorePeriod(): String {
        return createOrAlterProc("jds_pop_period", "jds_str_period", getStoreColumns("value" to getDataType(JdsFieldType.String)), storeUniqueColumns, false)
    }

    private fun createPopJdsMonthYear(): String {
        return createOrAlterProc("jds_pop_month_year", "jds_str_month_year", getStoreColumns("value" to getDataType(JdsFieldType.String)), storeUniqueColumns, false)
    }

    private fun createPopJdsMonthDay(): String {
        return createOrAlterProc("jds_pop_month_day", "jds_str_month_day", getStoreColumns("value" to getDataType(JdsFieldType.String)), storeUniqueColumns, false)
    }

    private fun createPopJdsYearMonth(): String {
        return createOrAlterProc("jds_pop_year_month", "jds_str_year_month", getStoreColumns("value" to getDataType(JdsFieldType.String)), storeUniqueColumns, false)
    }

    private fun createPopJdsStoreZonedDateTime(): String {
        return createOrAlterProc("jds_pop_zoned_date_time", "jds_str_zoned_date_time", getStoreColumns("value" to getDataType(JdsFieldType.ZonedDateTime)), storeUniqueColumns, false)
    }

    private fun createPopEntityLiveVersion(): String {
        val columns = LinkedHashMap<String, String>()
        columns["uuid"] = getDataType(JdsFieldType.String, 36)
        //don't include edit_version column, a separate SQL statement updates that column
        return createOrAlterProc("jds_pop_entity_live_version", "jds_entity_live_version", columns, setOf("uuid"), false)
    }

    private fun createEntityLiveVersionTable(): String {
        val tableName = "jds_entity_live_version"
        val columns = LinkedHashMap<String, String>()
        columns["uuid"] = getDataType(JdsFieldType.String, 36)
        columns["edit_version"] = getDataType(JdsFieldType.Int)
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_u"] = "uuid"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, columns, uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreBlob(): String {
        val tableName = "jds_str_blob"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(JdsFieldType.Blob)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreBoolean(): String {
        val tableName = "jds_str_boolean"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(JdsFieldType.Boolean)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreDate(): String {
        val tableName = "jds_str_date"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(JdsFieldType.Date)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreDateTime(): String {
        val tableName = "jds_str_date_time"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(JdsFieldType.DateTime)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreDateTimeCollection(): String {
        val tableName = "jds_str_date_time_col"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(JdsFieldType.DateTime)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreDouble(): String {
        val tableName = "jds_str_double"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(JdsFieldType.Double)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreDoubleCollection(): String {
        val tableName = "jds_str_double_col"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(JdsFieldType.Double)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreDuration(): String {
        val tableName = "jds_str_duration"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(JdsFieldType.Long)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreEnum(): String {
        val tableName = "jds_str_enum"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(JdsFieldType.Int)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreEnumString(): String {
        val tableName = "jds_str_enum_string"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(JdsFieldType.String)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreEnumCollection(): String {
        val tableName = "jds_str_enum_col"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(JdsFieldType.Int)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreEnumStringCollection(): String {
        val tableName = "jds_str_enum_string_col"//Oracle length
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${tableName}_u"] = "uuid, edit_version, field_id"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(JdsFieldType.String)), uniqueColumns, LinkedHashMap(), foreignKeys)
    }

    private fun createStoreFloat(): String {
        val tableName = "jds_str_float"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(JdsFieldType.Float)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreShort(): String {
        val tableName = "jds_str_short"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(JdsFieldType.Short)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreUuid(): String {
        val tableName = "jds_str_uuid"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(JdsFieldType.Uuid)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreFloatCollection(): String {
        val tableName = "jds_str_float_col"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(JdsFieldType.Float)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreInteger(): String {
        val tableName = "jds_str_integer"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(JdsFieldType.Int)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreIntegerCollection(): String {
        val tableName = "jds_str_integer_col"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(JdsFieldType.Int)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreLong(): String {
        val tableName = "jds_str_long"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(JdsFieldType.Long)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreLongCollection(): String {
        val tableName = "jds_str_long_col"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(JdsFieldType.Long)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreMonthDay(): String {
        val tableName = "jds_str_month_day"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(JdsFieldType.String)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStorePeriod(): String {
        val tableName = "jds_str_period"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(JdsFieldType.String)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreText(): String {
        val tableName = "jds_str_text"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(JdsFieldType.String)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreTextCollection(): String {
        val tableName = "jds_str_text_col"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(JdsFieldType.String)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreTime(): String {
        val tableName = "jds_str_time"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(JdsFieldType.Time)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreYearMonth(): String {
        val tableName = "jds_str_year_month"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(JdsFieldType.String)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
    }

    private fun createStoreZonedDateTime(): String {
        val tableName = "jds_str_zoned_date_time"
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${tableName}_f"] = linkedMapOf("uuid, edit_version" to "$dimensionTable(uuid, edit_version)")
        return createTable(tableName, getStoreColumns("value" to getDataType(JdsFieldType.ZonedDateTime)), getStoreUniqueColumns(tableName), LinkedHashMap(), foreignKeys)
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
     * @param max the maximum length of the database type, applied against [io.github.subiyacryolite.jds.enums.JdsFieldType.String] and [io.github.subiyacryolite.jds.enums.JdsFieldType.Blob] types
     * @return the underlying database type of the supplied [io.github.subiyacryolite.jds.JdsField]
     */
    protected abstract fun getDataTypeImpl(fieldType: JdsFieldType, max: Int = 0): String

    companion object {

        private val storeUniqueColumns = setOf("uuid", "edit_version", "field_id")
    }
}
