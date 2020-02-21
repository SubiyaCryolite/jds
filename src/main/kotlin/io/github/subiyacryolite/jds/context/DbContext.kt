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
import io.github.subiyacryolite.jds.FieldDictionary
import io.github.subiyacryolite.jds.Options
import io.github.subiyacryolite.jds.annotations.EntityAnnotation
import io.github.subiyacryolite.jds.enums.FieldType
import io.github.subiyacryolite.jds.enums.Implementation
import io.github.subiyacryolite.jds.enums.Procedure
import io.github.subiyacryolite.jds.enums.Table
import io.github.subiyacryolite.jds.extensions.Extensions
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.Serializable
import java.sql.Connection
import java.sql.PreparedStatement
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
abstract class DbContext(
        val implementation: Implementation,
        val supportsStatements: Boolean,
        val objectPrefix: String = "jds_",
        val schema: String = ""
) : IDbContext, Serializable {

    val classes = ConcurrentHashMap<Int, Class<out Entity>>()
    val tables = HashSet<io.github.subiyacryolite.jds.Table>()
    val options = Options()
    var dimensionTable = ""

    /**
     * Initialise JDS base tables
     */
    @JvmOverloads
    fun init(dimensionTable: String = "${objectPrefix}entity_overview") {
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
        prepareImplementation(connection)
        prepareJdsComponent(connection, Table.Entity)
        prepareJdsComponent(connection, Table.FieldType)
        prepareJdsComponent(connection, Table.Field)
        prepareJdsComponent(connection, Table.Enum)
        prepareJdsComponent(connection, Table.EntityInheritance)
        prepareJdsComponent(connection, Table.EntityOverview)
        prepareJdsComponent(connection, Table.EntityBinding)
        prepareJdsComponent(connection, Table.EntityField)
        prepareJdsComponent(connection, Table.FieldEntity)
        prepareJdsComponent(connection, Table.EntityEnum)
        prepareJdsComponent(connection, Table.FieldDictionary)
        prepareJdsComponent(connection, Table.StoreText)
        prepareJdsComponent(connection, Table.StoreTextCollection)
        prepareJdsComponent(connection, Table.StoreBlob)
        prepareJdsComponent(connection, Table.StoreEnum)
        prepareJdsComponent(connection, Table.StoreEnumString)
        prepareJdsComponent(connection, Table.StoreEnumCollection)
        prepareJdsComponent(connection, Table.StoreEnumStringCollection)
        prepareJdsComponent(connection, Table.StoreShort)
        prepareJdsComponent(connection, Table.StoreFloat)
        prepareJdsComponent(connection, Table.StoreFloatCollection)
        prepareJdsComponent(connection, Table.StoreInteger)
        prepareJdsComponent(connection, Table.StoreIntegerCollection)
        prepareJdsComponent(connection, Table.StoreDate)
        prepareJdsComponent(connection, Table.StoreLong)
        prepareJdsComponent(connection, Table.StoreLongCollection)
        prepareJdsComponent(connection, Table.StoreDouble)
        prepareJdsComponent(connection, Table.StoreDoubleCollection)
        prepareJdsComponent(connection, Table.StoreDateTime)
        prepareJdsComponent(connection, Table.StoreDateTimeCollection)
        prepareJdsComponent(connection, Table.StoreZonedDateTime)
        prepareJdsComponent(connection, Table.StoreTime)
        prepareJdsComponent(connection, Table.StorePeriod)
        prepareJdsComponent(connection, Table.StoreDuration)
        prepareJdsComponent(connection, Table.StoreYearMonth)
        prepareJdsComponent(connection, Table.StoreMonthDay)
        prepareJdsComponent(connection, Table.StoreBoolean)
        prepareJdsComponent(connection, Table.StoreUuid)
        prepareJdsComponent(connection, Table.EntityLive)
        prepareJdsComponent(connection, Table.FieldTag)
        prepareJdsComponent(connection, Table.FieldAlternateCode)
        if (supportsStatements) {
            prepareJdsComponent(connection, Procedure.StoreBoolean)
            prepareJdsComponent(connection, Procedure.StoreBlob)
            prepareJdsComponent(connection, Procedure.StoreText)
            prepareJdsComponent(connection, Procedure.StoreLong)
            prepareJdsComponent(connection, Procedure.StoreInteger)
            prepareJdsComponent(connection, Procedure.StoreFloat)
            prepareJdsComponent(connection, Procedure.StoreShort)
            prepareJdsComponent(connection, Procedure.StoreUuid)
            prepareJdsComponent(connection, Procedure.StoreDouble)
            prepareJdsComponent(connection, Procedure.StoreDateTime)
            prepareJdsComponent(connection, Procedure.StoreTime)
            prepareJdsComponent(connection, Procedure.StoreDate)
            prepareJdsComponent(connection, Procedure.StoreDuration)
            prepareJdsComponent(connection, Procedure.StorePeriod)
            prepareJdsComponent(connection, Procedure.StoreMonthDay)
            prepareJdsComponent(connection, Procedure.StoreYearMonth)
            prepareJdsComponent(connection, Procedure.StoreEnum)
            prepareJdsComponent(connection, Procedure.StoreEnumString)
            prepareJdsComponent(connection, Procedure.StoreZonedDateTime)
            prepareJdsComponent(connection, Procedure.EntityOverview)
            prepareJdsComponent(connection, Procedure.EntityBinding)
            prepareJdsComponent(connection, Procedure.EntityField)
            prepareJdsComponent(connection, Procedure.FieldEntity)
            prepareJdsComponent(connection, Procedure.EntityEnum)
            prepareJdsComponent(connection, Procedure.Entity)
            prepareJdsComponent(connection, Procedure.Enum)
            prepareJdsComponent(connection, Procedure.Field)
            prepareJdsComponent(connection, Procedure.EntityInheritance)
            prepareJdsComponent(connection, Procedure.EntityLive)
            prepareJdsComponent(connection, Procedure.StoreTextCollection)
            prepareJdsComponent(connection, Procedure.StoreEnumCollection)
            prepareJdsComponent(connection, Procedure.StoreEnumStringCollection)
            prepareJdsComponent(connection, Procedure.StoreDateTimeCollection)
            prepareJdsComponent(connection, Procedure.StoreFloatCollection)
            prepareJdsComponent(connection, Procedure.StoreIntegerCollection)
            prepareJdsComponent(connection, Procedure.StoreLongCollection)
            prepareJdsComponent(connection, Procedure.StoreDoubleCollection)
            prepareJdsComponent(connection, Procedure.FieldDictionary)
            prepareJdsComponent(connection, Procedure.FieldTag)
            prepareJdsComponent(connection, Procedure.FieldAlternateCode)
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

    val isPostGreSqlDb: Boolean
        get() = implementation === Implementation.PostGreSql

    /**
     * Allow each unique DB implementation to perform unique steps
     * For example the creation of Schemas in Postgres or Namespaces in SQL server
     */
    open internal fun prepareImplementation(connection: Connection) {

    }

    /**
     * Delegates the creation of custom database components depending on the
     * underlying JDS Database implementation
     * @param connection the connection to use for this operation
     * @param table an enum that maps to the components concrete
     * implementation details
     */
    private fun prepareJdsComponent(connection: Connection, table: Table) {
        val tableExists = doesTableExist(connection, table)
        if (!tableExists) {
            initiateDatabaseComponent(connection, table)
        }
    }

    /**
     * Delegates the creation of custom database components depending on the
     * underlying JDS Database implementation
     * @param connection the connection to use for this operation
     * @param procedure an enum that maps to the components concrete
     * implementation details
     */
    private fun prepareJdsComponent(connection: Connection, procedure: Procedure) {
        if (!doesProcedureExist(connection, procedure)) {
            initiateDatabaseComponent(connection, procedure)
        }
    }

    /**
     * Initialises core JDS Database components
     *
     * @param connection the SQL connection top use for this operation
     * @param table an enum that maps to the components concrete implementation
     */
    private fun initiateDatabaseComponent(connection: Connection, table: Table) {
        when (table) {
            Table.EntityBinding -> executeSqlFromString(connection, createEntityBinding())
            Table.EntityLive -> executeSqlFromString(connection, createEntityLive())
            Table.EntityOverview -> executeSqlFromString(connection, createRefEntityOverview())
            Table.Entity -> executeSqlFromString(connection, createStoreEntities())
            Table.EntityEnum -> executeSqlFromString(connection, createBindEntityEnums())
            Table.EntityField -> executeSqlFromString(connection, createBindEntityFields())
            Table.FieldEntity -> executeSqlFromString(connection, createBindFieldEntities())
            Table.Enum -> executeSqlFromString(connection, createRefEnumValues())
            Table.FieldType -> {
                executeSqlFromString(connection, createRefFieldTypes())
                populateFieldTypes(connection)
            }
            Table.Field -> executeSqlFromString(connection, createRefFields())
            Table.EntityInheritance -> executeSqlFromString(connection, createRefInheritance())
            Table.FieldDictionary -> executeSqlFromString(connection, createFieldDictionary())
            Table.FieldTag -> executeSqlFromString(connection, createFieldTag())
            Table.FieldAlternateCode -> executeSqlFromString(connection, createFieldAlternateCode())
            Table.StoreBlob -> executeSqlFromString(connection, createStoreBlob())
            Table.StoreBoolean -> executeSqlFromString(connection, createStoreBoolean())
            Table.StoreDate -> executeSqlFromString(connection, createStoreDate())
            Table.StoreDateTime -> executeSqlFromString(connection, createStoreDateTime())
            Table.StoreDateTimeCollection -> executeSqlFromString(connection, createStoreDateTimeCollection())
            Table.StoreDouble -> executeSqlFromString(connection, createStoreDouble())
            Table.StoreDoubleCollection -> executeSqlFromString(connection, createStoreDoubleCollection())
            Table.StoreDuration -> executeSqlFromString(connection, createStoreDuration())
            Table.StoreEnum -> executeSqlFromString(connection, createStoreEnum())
            Table.StoreEnumString -> executeSqlFromString(connection, createStoreEnumString())
            Table.StoreEnumCollection -> executeSqlFromString(connection, createStoreEnumCollection())
            Table.StoreEnumStringCollection -> executeSqlFromString(connection, createStoreEnumStringCollection())
            Table.StoreFloat -> executeSqlFromString(connection, createStoreFloat())
            Table.StoreShort -> executeSqlFromString(connection, createStoreShort())
            Table.StoreUuid -> executeSqlFromString(connection, createStoreUuid())
            Table.StoreFloatCollection -> executeSqlFromString(connection, createStoreFloatCollection())
            Table.StoreInteger -> executeSqlFromString(connection, createStoreInteger())
            Table.StoreIntegerCollection -> executeSqlFromString(connection, createStoreIntegerCollection())
            Table.StoreLong -> executeSqlFromString(connection, createStoreLong())
            Table.StoreLongCollection -> executeSqlFromString(connection, createStoreLongCollection())
            Table.StoreMonthDay -> executeSqlFromString(connection, createStoreMonthDay())
            Table.StorePeriod -> executeSqlFromString(connection, createStorePeriod())
            Table.StoreText -> executeSqlFromString(connection, createStoreText())
            Table.StoreTextCollection -> executeSqlFromString(connection, createStoreTextCollection())
            Table.StoreTime -> executeSqlFromString(connection, createStoreTime())
            Table.StoreYearMonth -> executeSqlFromString(connection, createStoreYearMonth())
            Table.StoreZonedDateTime -> executeSqlFromString(connection, createStoreZonedDateTime())
        }
    }

    /**
     * Initialises core JDS Database components
     *
     * @param connection   the SQL connection top use for this operation
     * @param procedure an enum that maps to the components concrete implementation
     */
    private fun initiateDatabaseComponent(connection: Connection, procedure: Procedure) {
        when (procedure) {
            Procedure.EntityBinding -> executeSqlFromString(connection, createPopEntityBinding())
            Procedure.EntityLive -> executeSqlFromString(connection, createPopEntityLiveVersion())
            Procedure.EntityOverview -> executeSqlFromString(connection, createPopEntityOverview())
            Procedure.Entity -> executeSqlFromString(connection, createPopEntity())
            Procedure.EntityEnum -> executeSqlFromString(connection, createPopEntityEnum())
            Procedure.EntityField -> executeSqlFromString(connection, createPopEntityField())
            Procedure.FieldEntity -> executeSqlFromString(connection, createPopFieldEntity())
            Procedure.EntityInheritance -> executeSqlFromString(connection, createPopEntityInheritance())
            Procedure.Enum -> executeSqlFromString(connection, createPopEnum())
            Procedure.Field -> executeSqlFromString(connection, createPopField())
            Procedure.FieldDictionary -> executeSqlFromString(connection, createPopFieldDictionary())
            Procedure.FieldAlternateCode -> executeSqlFromString(connection, createPopFieldAlternateCode())
            Procedure.FieldTag -> executeSqlFromString(connection, createPopFieldTag())
            Procedure.StoreBlob -> executeSqlFromString(connection, createPopStoreBlob())
            Procedure.StoreBoolean -> executeSqlFromString(connection, createPopStoreBoolean())
            Procedure.StoreDate -> executeSqlFromString(connection, createPopStoreDate())
            Procedure.StoreDateTime -> executeSqlFromString(connection, createPopStoreDateTime())
            Procedure.StoreDouble -> executeSqlFromString(connection, createPopStoreDouble())
            Procedure.StoreDoubleCollection -> executeSqlFromString(connection, createPopDoubleCollection())
            Procedure.StoreDuration -> executeSqlFromString(connection, createPopStoreDuration())
            Procedure.StoreEnum -> executeSqlFromString(connection, createPopStoreEnum())
            Procedure.StoreEnumString -> executeSqlFromString(connection, createPopStoreEnumString())
            Procedure.StoreEnumCollection -> executeSqlFromString(connection, createPopEnumCollection())
            Procedure.StoreEnumStringCollection -> executeSqlFromString(connection, createPopEnumStringCollection())
            Procedure.StoreFloat -> executeSqlFromString(connection, createPopStoreFloat())
            Procedure.StoreShort -> executeSqlFromString(connection, createPopStoreShort())
            Procedure.StoreUuid -> executeSqlFromString(connection, createPopStoreUuid())
            Procedure.StoreFloatCollection -> executeSqlFromString(connection, createPopFloatCollection())
            Procedure.StoreInteger -> executeSqlFromString(connection, createPopStoreInteger())
            Procedure.StoreIntegerCollection -> executeSqlFromString(connection, createPopIntegerCollection())
            Procedure.StoreLong -> executeSqlFromString(connection, createPopStoreLong())
            Procedure.StoreLongCollection -> executeSqlFromString(connection, createPopLongCollection())
            Procedure.StoreMonthDay -> executeSqlFromString(connection, createPopMonthDay())
            Procedure.StorePeriod -> executeSqlFromString(connection, createPopStorePeriod())
            Procedure.StoreText -> executeSqlFromString(connection, createPopStoreText())
            Procedure.StoreTextCollection -> executeSqlFromString(connection, createPopTextCollection())
            Procedure.StoreTime -> executeSqlFromString(connection, createPopStoreTime())
            Procedure.StoreYearMonth -> executeSqlFromString(connection, createPopYearMonth())
            Procedure.StoreZonedDateTime -> executeSqlFromString(connection, createPopStoreZonedDateTime())
        }
    }

    internal fun getCallOrStatement(connection: Connection,sql: String):PreparedStatement{
        return if(supportsStatements){
            connection.prepareCall(sql)
        }else{
            connection.prepareStatement(sql)
        }
    }

    override fun doesTableExist(connection: Connection, table: Table): Boolean {
        return tableExists(connection, table) > 0
    }

    override fun doesTableExist(connection: Connection, tableName: String): Boolean {
        return tableExists(connection, tableName) > 0
    }

    override fun doesProcedureExist(connection: Connection, procedure: Procedure): Boolean {
        return procedureExists(connection, procedure) > 0
    }

    override fun doesProcedureExist(connection: Connection, procedureName: String): Boolean {
        return procedureExists(connection, procedureName) > 0
    }

    override fun doesColumnExist(connection: Connection, tableName: String, columnName: String): Boolean {
        return columnExists(connection, "${objectPrefix}${tableName}", columnName) > 0
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

    private fun executeSqlFromString(connection: Connection, sql: String) {
        try {
            connection.autoCommit = false
            connection.prepareStatement(sql).use { statement -> statement.executeUpdate() }
            connection.commit()
        } catch (ex: Exception) {
            connection.rollback()
            ex.printStackTrace(System.err)
        } finally {
            connection.autoCommit = true
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
     * @param table the table to look up
     * @return 1 if the specified table exists the the database
     */
    abstract fun tableExists(connection: Connection, table: Table): Int

    abstract fun tableExists(connection: Connection, tableName: String): Int

    fun createOrAlterProc(
            procedure: Procedure,
            table: Table,
            columns: Map<String, String>,
            uniqueColumns: Collection<String>,
            doNothingOnConflict: Boolean
    ): String {
        return createOrAlterProc(getName(procedure), getName(table), columns, uniqueColumns, doNothingOnConflict)
    }

    /**
     * @param procedureName
     * @param tableName
     * @param columns
     * @param uniqueColumns
     * @param doNothingOnConflict
     */
    abstract override fun createOrAlterProc(
            procedureName: String,
            tableName: String,
            columns: Map<String, String>,
            uniqueColumns: Collection<String>,
            doNothingOnConflict: Boolean
    ): String

    private fun createTable(
            table: Table,
            columns: HashMap<String, String>,
            uniqueColumns: HashMap<String, String> = HashMap(),
            primaryKeys: HashMap<String, String> = HashMap(),
            foreignKeys: LinkedHashMap<String, LinkedHashMap<String, String>> = LinkedHashMap()
    ): String {
        return createTable(getName(table), columns, uniqueColumns, primaryKeys, foreignKeys)
    }

    /**
     * @param tableName
     * @param columns LinkedHashMap<columnName -> columnType>
     * @param uniqueColumns LinkedHashMap<constraintName -> constraintColumns>
     * @param primaryKeys LinkedHashMap<constraintName -> constraintColumns>
     * @param foreignKeys LinkedHashMap<constraintName -> LinkedHashMap< LocalColumns -> ReferenceTable(ReferenceColumns)>>
     */
    private fun createTable(
            tableName: String,
            columns: HashMap<String, String>,
            uniqueColumns: HashMap<String, String> = HashMap(),
            primaryKeys: HashMap<String, String> = HashMap(),
            foreignKeys: LinkedHashMap<String, LinkedHashMap<String, String>> = LinkedHashMap()
    ): String {
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
     * @param procedure the procedure to look up
     * @return 1 if the specified procedure exists in the database
     */
    open fun procedureExists(connection: Connection, procedure: Procedure): Int = 0

    /**
     * Internal checks to see if the specified procedure exists in the database
     *
     * @param connection the connection to use
     * @param procedureName the procedure to look up
     * @return 1 if the specified procedure exists in the database
     */
    open fun procedureExists(connection: Connection, procedureName: String): Int = 0

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
        (if (supportsStatements) connection.prepareCall(populateEntityInheritance()) else connection.prepareStatement(populateEntityInheritance())).use { statement ->
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
        (if (supportsStatements) connection.prepareCall(populateEntity()) else connection.prepareStatement(populateEntity())).use { statement ->
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

    fun mapTable(vararg table: io.github.subiyacryolite.jds.Table) {
        tables.addAll(table)
    }

    @Throws(Exception::class)
    fun prepareTables() {
        dataSource.connection.use { connection ->
            tables.forEach { it.forceGenerateOrUpdateSchema(this, connection) }
        }
    }

    fun map(entity: Class<out Entity>) {
        val entityAnnotation = Entity.getEntityAnnotation(entity)
        if (entityAnnotation != null) {
            if (!classes.containsKey(entityAnnotation.id)) {
                classes[entityAnnotation.id] = entity
                //do the thing
                try {
                    Entity.initialising = true
                    dataSource.connection.use { connection ->
                        connection.autoCommit = false
                        val parentEntities = HashSet<Int>()
                        val jdsEntity = entity.getDeclaredConstructor().newInstance()
                        parentEntities.add(jdsEntity.overview.entityId)//add this own entity to the chain
                        Extensions.determineParents(entity, parentEntities)
                        populateRefEntity(connection, jdsEntity.overview.entityId, entityAnnotation.name, entityAnnotation.description)
                        jdsEntity.populateRefFieldRefEntityField(this, connection, jdsEntity.overview.entityId)
                        jdsEntity.populateRefEnumRefEntityEnum(this, connection, jdsEntity.overview.entityId)
                        FieldDictionary.update(this, connection)
                        mapParentEntities(connection, parentEntities, jdsEntity.overview.entityId)
                        connection.commit()
                        connection.autoCommit = true
                        if (options.logOutput)
                            println("Mapped Entity [${entityAnnotation.name}]")
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace(System.err)
                } finally {
                    Entity.initialising = false
                }
            }
        } else
            throw RuntimeException("You must annotate the class [${entity.canonicalName}] or its parent with [${EntityAnnotation::class.java}]")
    }

    private fun populateFieldTypes(connection: Connection) {
        connection.prepareStatement("INSERT INTO ${getName(Table.FieldType)}(ordinal, caption) VALUES(?,?)").use { statement ->
            FieldType.values().forEach { fieldType ->
                statement.setInt(1, fieldType.ordinal)
                statement.setString(2, fieldType.name)
                statement.addBatch()
            }
            statement.executeBatch()
        }
    }

    internal open fun populateEntityLive() = "{call ${getName(Procedure.EntityLive)}(?)}"

    internal open fun populateStoreMonthDay() = "{call ${getName(Procedure.StoreMonthDay)}(?, ?, ?, ?)}"

    internal open fun populateStoreYearMonth() = "{call ${getName(Procedure.StoreYearMonth)}(?, ?, ?, ?)}"

    internal open fun populateStorePeriod() = "{call ${getName(Procedure.StorePeriod)}(?, ?, ?, ?)}"

    internal open fun populateStoreDuration() = "{call ${getName(Procedure.StoreDuration)}(?, ?, ?, ?)}"

    /**
     * SQL call to save blob values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateStoreBlob() = "{call ${getName(Procedure.StoreBlob)}(?, ?, ?, ?)}"

    /**
     * SQL call to save boolean values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateStoreBoolean() = "{call ${getName(Procedure.StoreBoolean)}(?, ?, ?, ?)}"

    /**
     * SQL call to save datetime values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateStoreDateTime() = "{call ${getName(Procedure.StoreDateTime)}(?, ?, ?, ?)}"

    /**
     * SQL call to save double values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateStoreDouble() = "{call ${getName(Procedure.StoreDouble)}(?, ?, ?, ?)}"

    /**
     * SQL call to save float values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateStoreFloat() = "{call ${getName(Procedure.StoreFloat)}(?, ?, ?, ?)}"

    /**
     * SQL call to save short values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateStoreShort() = "{call ${getName(Procedure.StoreShort)}(?, ?, ?, ?)}"

    /**
     * SQL call to save short values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateStoreUuid() = "{call ${getName(Procedure.StoreUuid)}(?, ?, ?, ?)}"

    /**
     * SQL call to save integer values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateStoreInteger() = "{call ${getName(Procedure.StoreInteger)}(?, ?, ?, ?)}"

    /**
     * SQL call to save long values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateStoreLong() = "{call ${getName(Procedure.StoreLong)}(?, ?, ?, ?)}"


    /**
     * SQL call to save text values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateStoreText() = "{call ${getName(Procedure.StoreText)}(?, ?, ?, ?)}"

    /**
     * SQL call to save time values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateStoreTime() = "{call ${getName(Procedure.StoreTime)}(?, ?, ?, ?)}"

    /**
     * SQL call to save datetime values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateStoreZonedDateTime() = "{call ${getName(Procedure.StoreZonedDateTime)}(?, ?, ?, ?)}"

    /**
     * SQL call to save enum values as ordinal int values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateStoreEnum() = "{call ${getName(Procedure.StoreEnum)}(?, ?, ?, ?)}"

    /**
     * QL call to save enum values as string values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateStoreEnumString() = "{call ${getName(Procedure.StoreEnumString)}(?, ?, ?, ?)}"

    /**
     * SQL call to save date values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateStoreDate() = "{call ${getName(Procedure.StoreDate)}(?, ?, ?, ?)}"

    /**
     * SQL call to save enum collections
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateStoreEnumCollection() = "{call ${getName(Procedure.StoreEnumCollection)}(?, ?, ?, ?)}"

    /**
     * SQL call to save enum string collections
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateStoreEnumStringCollection() = "{call ${getName(Procedure.StoreEnumStringCollection)}(?, ?, ?, ?)}"

    /**
     * SQL call to save date time collections
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateStoreDateTimeCollection() = "{call ${getName(Procedure.StoreDateTimeCollection)}(?, ?, ?, ?)}"

    /**
     * SQL call to save float collections
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateStoreFloatCollection() = "{call ${getName(Procedure.StoreFloatCollection)}(?, ?, ?, ?)}"

    /**
     * SQL call to save integer collections
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateStoreIntegerCollection() = "{call ${getName(Procedure.StoreIntegerCollection)}(?, ?, ?, ?)}"

    /**
     * SQL call to save double collections
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateStoreDoubleCollection() = "{call ${getName(Procedure.StoreDoubleCollection)}(?, ?, ?, ?)}"

    /**
     * SQL call to save long collections
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateStoreLongCollection() = "{call ${getName(Procedure.StoreLongCollection)}(?, ?, ?, ?)}"

    /**
     * SQL call to save string collections
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateStoreTextCollection() = "{call ${getName(Procedure.StoreTextCollection)}(?, ?, ?, ?)}"

    /**
     * SQL call to save entity overview values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateEntityOverview() = "{call ${getName(Procedure.EntityOverview)}(?, ?, ?)}"

    /**
     * SQL call to save entity overview values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateEntityBinding() = "{call ${getName(Procedure.EntityBinding)}(?, ?, ?, ?, ?)}"

    /**
     * SQL call to bind fieldIds to entityVersions
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateEntityField() = "{call ${getName(Procedure.EntityField)}(?, ?)}"

    /**
     *
     */
    internal open fun populateFieldEntity() = "{call ${getName(Procedure.FieldEntity)}(?, ?)}"

    /**
     * SQL call to map field names and descriptions
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateField() = "{call ${getName(Procedure.Field)}(?, ?, ?, ?)}"

    /**
     * SQL call to bind enumProperties to entityVersions
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateEntityEnum() = "{call ${getName(Procedure.EntityEnum)}(?,?)}"

    /**
     * SQL call to map parents to child entities
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateEntityInheritance() = "{call ${getName(Procedure.EntityInheritance)}(?, ?)}"

    /**
     * SQL call to map class names
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateEntity() = "{call ${getName(Procedure.Entity)}(?, ?, ?)}"

    /**
     * SQL call to save reference enum values
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateEnum() = "{call ${getName(Procedure.Enum)}(?, ?, ?, ?)}"

    /**
     * SQL call to update the field dictionary property
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateFieldDictionary() = "{call ${getName(Procedure.FieldDictionary)}(?, ?, ?)}"

    /**
     * SQL call to update the field dictionary property
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateFieldTag() = "{call ${getName(Procedure.FieldTag)}(?, ?)}"

    /**
     * SQL call to update the field dictionary property
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateFieldAlternateCode() = "{call ${getName(Procedure.FieldAlternateCode)}(?, ?, ?)}"

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

    internal fun getName(table: Table): String {
        return "${objectPrefix}${table.component}"
    }

    internal fun getName(procedure: Procedure): String {
        return "${objectPrefix}${procedure.component}"
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

    private fun createPopEntityBinding(): String {
        val uniqueColumns = setOf("parent_id", "parent_edit_version", "child_id", "child_edit_version")
        val columns = LinkedHashMap<String, String>()
        columns["parent_id"] = getDataType(FieldType.String, 36)
        columns["parent_edit_version"] = getDataType(FieldType.Int)
        columns["child_id"] = getDataType(FieldType.String, 36)
        columns["child_edit_version"] = getDataType(FieldType.Int)
        columns["child_attribute_id"] = getDataType(FieldType.Long)
        return createOrAlterProc(Procedure.EntityBinding, Table.EntityBinding, columns, uniqueColumns, false)
    }

    private fun createPopEntityOverview(): String {
        val uniqueColumns = setOf("id", "edit_version")
        val columns = LinkedHashMap<String, String>()
        columns["id"] = getDataType(FieldType.String, 36)
        columns["edit_version"] = getDataType(FieldType.Int)
        columns["entity_id"] = getDataType(FieldType.Int)
        return createOrAlterProc(Procedure.EntityOverview, Table.EntityOverview, columns, uniqueColumns, false)
    }

    private fun createPopEntity(): String {
        val uniqueColumns = setOf("id")
        val columns = LinkedHashMap<String, String>()
        columns["id"] = getDataType(FieldType.Int)
        columns["name"] = getDataType(FieldType.String, 64)
        columns["description"] = getDataType(FieldType.String, 256)
        return createOrAlterProc(Procedure.Entity, Table.Entity, columns, uniqueColumns, false)
    }

    private fun createPopEntityEnum(): String {
        val uniqueColumns = setOf("entity_id", "field_id")
        val columns = LinkedHashMap<String, String>()
        columns["entity_id"] = getDataType(FieldType.Int)
        columns["field_id"] = getDataType(FieldType.Int)
        return createOrAlterProc(Procedure.EntityEnum, Table.EntityEnum, columns, uniqueColumns, true)
    }

    private fun createPopEntityField(): String {
        val uniqueColumns = setOf("entity_id", "field_id")
        val columns = LinkedHashMap<String, String>()
        columns["entity_id"] = getDataType(FieldType.Int)
        columns["field_id"] = getDataType(FieldType.Int)
        return createOrAlterProc(Procedure.EntityField, Table.EntityField, columns, uniqueColumns, true)
    }

    private fun createPopFieldEntity(): String {
        val uniqueColumns = setOf("field_id", "entity_id")
        val columns = LinkedHashMap<String, String>()
        columns["field_id"] = getDataType(FieldType.Int)
        columns["entity_id"] = getDataType(FieldType.Int)
        return createOrAlterProc(Procedure.FieldEntity, Table.FieldEntity, columns, uniqueColumns, true)
    }

    private fun createPopEntityInheritance(): String {
        val uniqueColumns = setOf("parent_entity_id", "child_entity_id")
        val columns = LinkedHashMap<String, String>()
        columns["parent_entity_id"] = getDataType(FieldType.Int)
        columns["child_entity_id"] = getDataType(FieldType.Int)
        return createOrAlterProc(Procedure.EntityInheritance, Table.EntityInheritance, columns, uniqueColumns, true)
    }

    private fun createPopEnum(): String {
        val uniqueColumns = setOf("field_id", "seq")
        val columns = LinkedHashMap<String, String>()
        columns["field_id"] = getDataType(FieldType.Int)
        columns["seq"] = getDataType(FieldType.Int)
        columns["name"] = getDataType(FieldType.String, 128)
        columns["caption"] = getDataType(FieldType.String, 128)
        return createOrAlterProc(Procedure.Enum, Table.Enum, columns, uniqueColumns, false)
    }

    private fun createPopField(): String {
        val uniqueColumns = setOf("id")
        val columns = LinkedHashMap<String, String>()
        columns["id"] = getDataType(FieldType.Int)
        columns["caption"] = getDataType(FieldType.String, 64)
        columns["description"] = getDataType(FieldType.String, 256)
        columns["field_type_ordinal"] = getDataType(FieldType.Int)
        return createOrAlterProc(Procedure.Field, Table.Field, columns, uniqueColumns, false)
    }

    private fun createPopFieldDictionary(): String {
        val uniqueColumns = setOf("entity_id", "field_id")
        val columns = LinkedHashMap<String, String>()
        columns["entity_id"] = getDataType(FieldType.Int)
        columns["field_id"] = getDataType(FieldType.Int)
        columns["property_name"] = getDataType(FieldType.String, 48)
        return createOrAlterProc(Procedure.FieldDictionary, Table.FieldDictionary, columns, uniqueColumns, false)
    }

    private fun createPopFieldAlternateCode(): String {
        val uniqueColumns = setOf("field_id", "alternate_code")
        val columns = LinkedHashMap<String, String>()
        columns["field_id"] = getDataType(FieldType.Int)
        columns["alternate_code"] = getDataType(FieldType.String,16)
        columns["value"] = getDataType(FieldType.String, 48)
        return createOrAlterProc(Procedure.FieldAlternateCode, Table.FieldAlternateCode, columns, uniqueColumns, false)
    }

    private fun createPopFieldTag(): String {
        val uniqueColumns = setOf("field_id")
        val columns = LinkedHashMap<String, String>()
        columns["field_id"] = getDataType(FieldType.Int)
        columns["tag"] = getDataType(FieldType.String,16)
        return createOrAlterProc(Procedure.FieldTag, Table.FieldTag, columns, uniqueColumns, false)
    }

    private fun createPopStoreBlob(): String {
        return createOrAlterProc(Procedure.StoreBlob, Table.StoreBlob, getStoreColumns("value" to getDataType(FieldType.Blob)), storeUniqueColumns, false)
    }

    private fun createPopStoreBoolean(): String {
        return createOrAlterProc(Procedure.StoreBoolean, Table.StoreBoolean, getStoreColumns("value" to getDataType(FieldType.Boolean)), storeUniqueColumns, false)
    }

    private fun createPopStoreDateTime(): String {
        return createOrAlterProc(Procedure.StoreDateTime, Table.StoreDateTime, getStoreColumns("value" to getDataType(FieldType.DateTime)), storeUniqueColumns, false)
    }

    private fun createPopStoreDouble(): String {
        return createOrAlterProc(Procedure.StoreDouble, Table.StoreDouble, getStoreColumns("value" to getDataType(FieldType.Double)), storeUniqueColumns, false)
    }

    private fun createPopDoubleCollection(): String {
        return createOrAlterProc(Procedure.StoreDoubleCollection, Table.StoreDoubleCollection, getStoreColumns("value" to getDataType(FieldType.Double)), storeUniqueColumns, false)
    }

    private fun createPopStoreFloat(): String {
        return createOrAlterProc(Procedure.StoreFloat, Table.StoreFloat, getStoreColumns("value" to getDataType(FieldType.Float)), storeUniqueColumns, false)
    }

    private fun createPopStoreShort(): String {
        return createOrAlterProc(Procedure.StoreShort, Table.StoreShort, getStoreColumns("value" to getDataType(FieldType.Short)), storeUniqueColumns, false)
    }

    private fun createPopStoreUuid(): String {
        return createOrAlterProc(Procedure.StoreUuid, Table.StoreUuid, getStoreColumns("value" to getDataType(FieldType.Uuid)), storeUniqueColumns, false)
    }

    private fun createPopFloatCollection(): String {
        return createOrAlterProc(Procedure.StoreFloatCollection, Table.StoreFloatCollection, getStoreColumns("value" to getDataType(FieldType.Float)), storeUniqueColumns, false)
    }

    private fun createPopStoreInteger(): String {
        return createOrAlterProc(Procedure.StoreInteger, Table.StoreInteger, getStoreColumns("value" to getDataType(FieldType.Int)), storeUniqueColumns, false)
    }

    private fun createPopIntegerCollection(): String {
        return createOrAlterProc(Procedure.StoreIntegerCollection, Table.StoreIntegerCollection, getStoreColumns("value" to getDataType(FieldType.Int)), storeUniqueColumns, false)
    }

    private fun createPopStoreLong(): String {
        return createOrAlterProc(Procedure.StoreLong, Table.StoreLong, getStoreColumns("value" to getDataType(FieldType.Long)), storeUniqueColumns, false)
    }

    private fun createPopLongCollection(): String {
        return createOrAlterProc(Procedure.StoreLongCollection, Table.StoreLongCollection, getStoreColumns("value" to getDataType(FieldType.Long)), storeUniqueColumns, false)
    }

    private fun createPopStoreText(): String {
        return createOrAlterProc(Procedure.StoreText, Table.StoreText, getStoreColumns("value" to getDataType(FieldType.String)), storeUniqueColumns, false)
    }

    private fun createPopTextCollection(): String {
        return createOrAlterProc(Procedure.StoreTextCollection, Table.StoreTextCollection, getStoreColumns("value" to getDataType(FieldType.String)), storeUniqueColumns, false)
    }

    private fun createPopStoreEnum(): String {
        return createOrAlterProc(Procedure.StoreEnum, Table.StoreEnum, getStoreColumns("value" to getDataType(FieldType.Int)), storeUniqueColumns, false)
    }

    private fun createPopStoreEnumString(): String {
        return createOrAlterProc(Procedure.StoreEnumString, Table.StoreEnumString, getStoreColumns("value" to getDataType(FieldType.String)), storeUniqueColumns, false)
    }

    private fun createPopEnumCollection(): String {
        return createOrAlterProc(Procedure.StoreEnumCollection, Table.StoreEnumCollection, getStoreColumns("value" to getDataType(FieldType.Int)), storeUniqueColumns, false)
    }

    private fun createPopEnumStringCollection(): String {
        return createOrAlterProc(Procedure.StoreEnumStringCollection, Table.StoreDateTime, getStoreColumns("value" to getDataType(FieldType.String)), storeUniqueColumns, false)
    }

    private fun createPopStoreTime(): String {
        return createOrAlterProc(Procedure.StoreTime, Table.StoreTime, getStoreColumns("value" to getDataType(FieldType.Time)), storeUniqueColumns, false)
    }

    private fun createPopStoreDate(): String {
        return createOrAlterProc(Procedure.StoreDate, Table.StoreDate, getStoreColumns("value" to getDataType(FieldType.Date)), storeUniqueColumns, false)
    }

    private fun createPopStoreDuration(): String {
        return createOrAlterProc(Procedure.StoreDuration, Table.StoreDuration, getStoreColumns("value" to getDataType(FieldType.Long)), storeUniqueColumns, false)
    }

    private fun createPopStorePeriod(): String {
        return createOrAlterProc(Procedure.StorePeriod, Table.StorePeriod, getStoreColumns("value" to getDataType(FieldType.String)), storeUniqueColumns, false)
    }

    private fun createPopMonthDay(): String {
        return createOrAlterProc(Procedure.StoreMonthDay, Table.StoreMonthDay, getStoreColumns("value" to getDataType(FieldType.String)), storeUniqueColumns, false)
    }

    private fun createPopYearMonth(): String {
        return createOrAlterProc(Procedure.StoreYearMonth, Table.StoreYearMonth, getStoreColumns("value" to getDataType(FieldType.String)), storeUniqueColumns, false)
    }

    private fun createPopStoreZonedDateTime(): String {
        return createOrAlterProc(Procedure.StoreZonedDateTime, Table.StoreZonedDateTime, getStoreColumns("value" to getDataType(FieldType.ZonedDateTime)), storeUniqueColumns, false)
    }

    private fun createPopEntityLiveVersion(): String {
        val columns = LinkedHashMap<String, String>()
        columns["id"] = getDataType(FieldType.String, 36)
        //don't include edit_version column, a separate SQL statement updates that column
        return createOrAlterProc(Procedure.EntityLive, Table.EntityLive, columns, setOf("id"), false)
    }

    private fun getDimensionTableFk(objectName: String): LinkedHashMap<String, LinkedHashMap<String, String>> {
        return linkedMapOf("${objectName}_jds_fk" to linkedMapOf("id, edit_version" to "$dimensionTable(id, edit_version)"))
    }

    private fun createEntityLive(): String {
        val objectName = Table.EntityLive.component
        val columns = LinkedHashMap<String, String>()
        columns["id"] = getDataType(FieldType.String, 36)
        columns["edit_version"] = getDataType(FieldType.Int)
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${objectName}_jds_uk"] = "id"
        return createTable(Table.EntityLive, columns, uniqueColumns, LinkedHashMap(), getDimensionTableFk(objectName))
    }

    private fun createStoreBlob(): String {
        val objectName = Table.StoreBlob.component
        return createTable(Table.StoreBlob, getStoreColumns("value" to getDataType(FieldType.Blob)), getStoreUniqueColumns(objectName), LinkedHashMap(), getDimensionTableFk(objectName))
    }

    private fun createStoreBoolean(): String {
        val objectName = Table.StoreBoolean.component
        return createTable(Table.StoreBoolean, getStoreColumns("value" to getDataType(FieldType.Boolean)), getStoreUniqueColumns(objectName), LinkedHashMap(), getDimensionTableFk(objectName))
    }

    private fun createStoreDate(): String {
        val objectName = Table.StoreDate.component
        return createTable(Table.StoreDate, getStoreColumns("value" to getDataType(FieldType.Date)), getStoreUniqueColumns(objectName), LinkedHashMap(), getDimensionTableFk(objectName))
    }

    private fun createStoreDateTime(): String {
        val objectName = Table.StoreDateTime.component
        return createTable(Table.StoreDateTime, getStoreColumns("value" to getDataType(FieldType.DateTime)), getStoreUniqueColumns(objectName), LinkedHashMap(), getDimensionTableFk(objectName))
    }

    private fun createStoreDateTimeCollection(): String {
        val objectName = Table.StoreDateTimeCollection.component
        return createTable(Table.StoreDateTimeCollection, getStoreColumns("value" to getDataType(FieldType.DateTime)), getStoreUniqueColumns(objectName), LinkedHashMap(), getDimensionTableFk(objectName))
    }

    private fun createStoreDouble(): String {
        val objectName = Table.StoreDouble.component
        return createTable(Table.StoreDouble, getStoreColumns("value" to getDataType(FieldType.Double)), getStoreUniqueColumns(objectName), LinkedHashMap(), getDimensionTableFk(objectName))
    }

    private fun createStoreDoubleCollection(): String {
        val objectName = Table.StoreDoubleCollection.component
        return createTable(Table.StoreDoubleCollection, getStoreColumns("value" to getDataType(FieldType.Double)), getStoreUniqueColumns(objectName), LinkedHashMap(), getDimensionTableFk(objectName))
    }

    private fun createStoreDuration(): String {
        val objectName = Table.StoreDuration.component
        return createTable(Table.StoreDuration, getStoreColumns("value" to getDataType(FieldType.Long)), getStoreUniqueColumns(objectName), LinkedHashMap(), getDimensionTableFk(objectName))
    }

    private fun createStoreEnum(): String {
        val objectName = Table.StoreEnum.component
        return createTable(Table.StoreEnum, getStoreColumns("value" to getDataType(FieldType.Int)), getStoreUniqueColumns(objectName), LinkedHashMap(), getDimensionTableFk(objectName))
    }

    private fun createStoreEnumString(): String {
        val objectName = Table.StoreEnumString.component
        return createTable(Table.StoreEnumString, getStoreColumns("value" to getDataType(FieldType.String)), getStoreUniqueColumns(objectName), LinkedHashMap(), getDimensionTableFk(objectName))
    }

    private fun createStoreEnumCollection(): String {
        val objectName = Table.StoreEnumCollection.component
        return createTable(Table.StoreEnumCollection, getStoreColumns("value" to getDataType(FieldType.Int)), getStoreUniqueColumns(objectName), LinkedHashMap(), getDimensionTableFk(objectName))
    }

    private fun createStoreEnumStringCollection(): String {
        val objectName = Table.StoreEnumStringCollection.component
        val uniqueColumns = LinkedHashMap<String, String>()
        uniqueColumns["${objectName}_u"] = "id, edit_version, field_id"
        return createTable(Table.StoreEnumStringCollection, getStoreColumns("value" to getDataType(FieldType.String)), uniqueColumns, LinkedHashMap(), getDimensionTableFk(objectName))
    }

    private fun createStoreFloat(): String {
        val objectName = Table.StoreFloat.component
        return createTable(Table.StoreFloat, getStoreColumns("value" to getDataType(FieldType.Float)), getStoreUniqueColumns(objectName), LinkedHashMap(), getDimensionTableFk(objectName))
    }

    private fun createStoreShort(): String {
        val objectName = Table.StoreShort.component
        return createTable(Table.StoreShort, getStoreColumns("value" to getDataType(FieldType.Short)), getStoreUniqueColumns(objectName), LinkedHashMap(), getDimensionTableFk(objectName))
    }

    private fun createStoreUuid(): String {
        val objectName = Table.StoreUuid.component
        return createTable(Table.StoreUuid, getStoreColumns("value" to getDataType(FieldType.Uuid)), getStoreUniqueColumns(objectName), LinkedHashMap(), getDimensionTableFk(objectName))
    }

    private fun createStoreFloatCollection(): String {
        val objectName = Table.StoreFloatCollection.component
        return createTable(Table.StoreFloatCollection, getStoreColumns("value" to getDataType(FieldType.Float)), getStoreUniqueColumns(objectName), LinkedHashMap(), getDimensionTableFk(objectName))
    }

    private fun createStoreInteger(): String {
        val objectName = Table.StoreInteger.component
        return createTable(Table.StoreInteger, getStoreColumns("value" to getDataType(FieldType.Int)), getStoreUniqueColumns(objectName), LinkedHashMap(), getDimensionTableFk(objectName))
    }

    private fun createStoreIntegerCollection(): String {
        val objectName = Table.StoreIntegerCollection.component
        return createTable(Table.StoreIntegerCollection, getStoreColumns("value" to getDataType(FieldType.Int)), getStoreUniqueColumns(objectName), LinkedHashMap(), getDimensionTableFk(objectName))
    }

    private fun createStoreLong(): String {
        val objectName = Table.StoreLong.component
        return createTable(Table.StoreLong, getStoreColumns("value" to getDataType(FieldType.Long)), getStoreUniqueColumns(objectName), LinkedHashMap(), getDimensionTableFk(objectName))
    }

    private fun createStoreLongCollection(): String {
        val objectName = Table.StoreLongCollection.component
        return createTable(Table.StoreLongCollection, getStoreColumns("value" to getDataType(FieldType.Long)), getStoreUniqueColumns(objectName), LinkedHashMap(), getDimensionTableFk(objectName))
    }

    private fun createStoreMonthDay(): String {
        val objectName = Table.StoreMonthDay.component
        return createTable(Table.StoreMonthDay, getStoreColumns("value" to getDataType(FieldType.String)), getStoreUniqueColumns(objectName), LinkedHashMap(), getDimensionTableFk(objectName))
    }

    private fun createStorePeriod(): String {
        val objectName = Table.StorePeriod.component
        return createTable(Table.StorePeriod, getStoreColumns("value" to getDataType(FieldType.String)), getStoreUniqueColumns(objectName), LinkedHashMap(), getDimensionTableFk(objectName))
    }

    private fun createStoreText(): String {
        val objectName = Table.StoreText.component
        return createTable(Table.StoreText, getStoreColumns("value" to getDataType(FieldType.String)), getStoreUniqueColumns(objectName), LinkedHashMap(), getDimensionTableFk(objectName))
    }

    private fun createStoreTextCollection(): String {
        val objectName = Table.StoreTextCollection.component
        return createTable(Table.StoreTextCollection, getStoreColumns("value" to getDataType(FieldType.String)), getStoreUniqueColumns(objectName), LinkedHashMap(), getDimensionTableFk(objectName))
    }

    private fun createStoreTime(): String {
        val objectName = Table.StoreTime.component
        return createTable(Table.StoreTime, getStoreColumns("value" to getDataType(FieldType.Time)), getStoreUniqueColumns(objectName), LinkedHashMap(), getDimensionTableFk(objectName))
    }

    private fun createStoreYearMonth(): String {
        val objectName = Table.StoreYearMonth.component
        return createTable(Table.StoreYearMonth, getStoreColumns("value" to getDataType(FieldType.String)), getStoreUniqueColumns(objectName), LinkedHashMap(), getDimensionTableFk(objectName))
    }

    private fun createStoreZonedDateTime(): String {
        val objectName = Table.StoreZonedDateTime.component
        return createTable(Table.StoreZonedDateTime, getStoreColumns("value" to getDataType(FieldType.ZonedDateTime)), getStoreUniqueColumns(objectName), LinkedHashMap(), getDimensionTableFk(objectName))
    }

    private fun createEntityBinding(): String {
        val objectName = Table.EntityBinding.component
        val columns = linkedMapOf(
                "parent_id" to getDataTypeImpl(FieldType.String, 36),
                "parent_edit_version" to getDataTypeImpl(FieldType.Int),
                "child_id" to getDataTypeImpl(FieldType.String, 36),
                "child_edit_version" to getDataTypeImpl(FieldType.Int),
                "child_attribute_id" to getDataTypeImpl(FieldType.Int)
        )
        val uniqueColumns = linkedMapOf("${objectName}_uk" to "parent_id, parent_edit_version, child_id, child_edit_version")
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        if (implementation != Implementation.TSql) {
            foreignKeys["${objectName}_jds_fk_1"] = linkedMapOf("parent_id, parent_edit_version" to "${getName(Table.EntityOverview)} (id, edit_version)")
            foreignKeys["${objectName}_jds_fk_2"] = linkedMapOf("child_id, child_edit_version" to "${getName(Table.EntityOverview)} (id, edit_version)")
        }
        return createTable(Table.EntityBinding, columns, uniqueColumns, HashMap(), foreignKeys)
    }

    private fun createRefEntityOverview(): String {
        val objectName = Table.EntityOverview.component
        val columns = linkedMapOf(
                "id" to getDataTypeImpl(FieldType.String, 36),
                "edit_version" to getDataTypeImpl(FieldType.Int),
                "entity_id" to getDataTypeImpl(FieldType.Int)
        )
        val primaryKey = linkedMapOf("${objectName}_pk" to "id, edit_version")
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${objectName}_jds_fk_1"] = linkedMapOf("entity_id" to "${getName(Table.Entity)} (id)")
        return createTable(Table.EntityOverview, columns, HashMap(), primaryKey, foreignKeys)
    }

    private fun createFieldDictionary(): String {
        val objectName = Table.FieldDictionary.component
        val columns = linkedMapOf(
                "entity_id" to getDataTypeImpl(FieldType.Int),
                "field_id" to getDataTypeImpl(FieldType.Int),
                "property_name" to getDataTypeImpl(FieldType.String, 48)
        )
        val primaryKey = linkedMapOf("${objectName}_pk" to "entity_id, field_id")
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${objectName}_jds_fk_1"] = linkedMapOf("entity_id" to "${getName(Table.Entity)} (id)")
        foreignKeys["${objectName}_jds_fk_2"] = linkedMapOf("field_id" to "${getName(Table.Field)} (id)")
        return createTable(Table.FieldDictionary, columns, HashMap(), primaryKey, foreignKeys)
    }

    private fun createFieldTag(): String {
        val objectName = Table.FieldTag.component
        val columns = linkedMapOf(
                "field_id" to getDataTypeImpl(FieldType.Int),
                "tag" to getDataTypeImpl(FieldType.String, 16)
        )
        val primaryKey = linkedMapOf("${objectName}_pk" to "field_id")
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${objectName}_jds_fk_2"] = linkedMapOf("field_id" to "${getName(Table.Field)} (id)")
        return createTable(Table.FieldTag, columns, HashMap(), primaryKey, foreignKeys)
    }

    private fun createFieldAlternateCode(): String {
        val objectName = Table.FieldAlternateCode.component
        val columns = linkedMapOf(
                "field_id" to getDataTypeImpl(FieldType.Int),
                "alternate_code" to getDataTypeImpl(FieldType.String, 16),
                "value" to getDataTypeImpl(FieldType.String, 48)
        )
        val primaryKey = linkedMapOf("${objectName}_pk" to "field_id, alternate_code")
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${objectName}_jds_fk_1"] = linkedMapOf("field_id" to "${getName(Table.Field)} (id)")
        return createTable(Table.FieldAlternateCode, columns, HashMap(), primaryKey, foreignKeys)
    }

    private fun createStoreEntities(): String {
        val objectName = Table.Entity.component
        val columns = linkedMapOf(
                "id" to getDataTypeImpl(FieldType.Int),
                "name" to getDataTypeImpl(FieldType.String, 64),
                "description" to getDataTypeImpl(FieldType.String, 256)
        )
        val primaryKey = linkedMapOf("${objectName}_pk" to "id")
        return createTable(Table.Entity, columns, HashMap(), primaryKey, LinkedHashMap())
    }

    private fun createRefEnumValues(): String {
        val objectName = Table.Enum.component
        val columns = linkedMapOf(
                "field_id" to getDataTypeImpl(FieldType.Int),
                "seq" to getDataTypeImpl(FieldType.Int),
                "name" to getDataTypeImpl(FieldType.String, 128),
                "caption" to getDataTypeImpl(FieldType.String, 128)
        )
        val primaryKey = linkedMapOf("${objectName}_pk" to "field_id, seq")
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${objectName}_jds_fk_1"] = linkedMapOf("field_id" to "${getName(Table.Field)} (id)")
        return createTable(Table.Enum, columns, HashMap(), primaryKey, foreignKeys)
    }

    private fun createRefFields(): String {
        val objectName = Table.Field.component
        val columns = linkedMapOf(
                "id" to getDataTypeImpl(FieldType.Int),
                "caption" to getDataTypeImpl(FieldType.String, 64),
                "description" to getDataTypeImpl(FieldType.String, 256),
                "field_type_ordinal" to getDataTypeImpl(FieldType.Int)
        )
        val primaryKey = linkedMapOf("${objectName}_pk" to "id")
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${objectName}_jds_fk_1"] = linkedMapOf("field_type_ordinal" to "${objectPrefix}${Table.FieldType.component} (ordinal)")
        return createTable(Table.Field, columns, HashMap(), primaryKey, foreignKeys)
    }

    private fun createRefFieldTypes(): String {
        val objectName = Table.FieldType.component
        val columns = linkedMapOf(
                "ordinal" to getDataTypeImpl(FieldType.Int),
                "caption" to getDataTypeImpl(FieldType.String, 64)
        )
        val primaryKey = linkedMapOf("${objectName}_pk" to "ordinal")
        return createTable(Table.FieldType, columns, HashMap(), primaryKey, LinkedHashMap())
    }

    private fun createBindEntityFields(): String {
        val objectName = Table.EntityField.component
        val columns = linkedMapOf(
                "entity_id" to getDataTypeImpl(FieldType.Int),
                "field_id" to getDataTypeImpl(FieldType.Int)
        )
        val primaryKey = linkedMapOf("${objectName}_pk" to "entity_id, field_id")
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${objectName}_jds_fk_1"] = linkedMapOf("entity_id" to "${getName(Table.Entity)} (id)")
        foreignKeys["${objectName}_jds_fk_2"] = linkedMapOf("field_id" to "${getName(Table.Field)} (id)")
        return createTable(Table.EntityField, columns, HashMap(), primaryKey, foreignKeys)
    }

    private fun createBindFieldEntities(): String {
        val objectName = Table.FieldEntity.component
        val columns = linkedMapOf(
                "field_id" to getDataTypeImpl(FieldType.Int),
                "entity_id" to getDataTypeImpl(FieldType.Int)
        )
        val primaryKey = linkedMapOf("${objectName}_pk" to "entity_id, field_id")
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${objectName}_jds_fk_1"] = linkedMapOf("entity_id" to "${getName(Table.Entity)} (id)")
        foreignKeys["${objectName}_jds_fk_2"] = linkedMapOf("field_id" to "${getName(Table.Field)} (id)")
        return createTable(Table.FieldEntity, columns, HashMap(), primaryKey, foreignKeys)
    }

    private fun createBindEntityEnums(): String {
        val objectName = Table.EntityEnum.component
        val columns = linkedMapOf(
                "entity_id" to getDataTypeImpl(FieldType.Int),
                "field_id" to getDataTypeImpl(FieldType.Int)
        )
        val primaryKey = linkedMapOf("${objectName}_pk" to "entity_id, field_id")
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${objectName}_jds_fk_1"] = linkedMapOf("entity_id" to "${getName(Table.Entity)} (id)")
        foreignKeys["${objectName}_jds_fk_2"] = linkedMapOf("field_id" to "${getName(Table.Field)} (id)")
        return createTable(Table.EntityEnum, columns, HashMap(), primaryKey, foreignKeys)
    }

    private fun createRefInheritance(): String {
        val objectName = Table.EntityInheritance.component
        val columns = linkedMapOf(
                "parent_entity_id" to getDataTypeImpl(FieldType.Int),
                "child_entity_id" to getDataTypeImpl(FieldType.Int)
        )
        val primaryKey = linkedMapOf("${objectName}_pk" to "parent_entity_id, child_entity_id")
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        if (implementation != Implementation.TSql) {
            foreignKeys["${objectName}_jds_fk_1"] = linkedMapOf("parent_entity_id" to "${getName(Table.Entity)} (id)")
            foreignKeys["${objectName}_jds_fk_2"] = linkedMapOf("child_entity_id" to "${getName(Table.Entity)} (id)")
        }
        return createTable(Table.EntityInheritance, columns, HashMap(), primaryKey, foreignKeys)
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
