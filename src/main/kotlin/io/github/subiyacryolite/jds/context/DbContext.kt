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
        prepareJdsComponent(connection, Table.EntityBinding)
        prepareJdsComponent(connection, Table.EntityField)
        prepareJdsComponent(connection, Table.FieldEntity)
        prepareJdsComponent(connection, Table.EntityEnum)
        prepareJdsComponent(connection, Table.FieldDictionary)
        prepareJdsComponent(connection, Table.FieldTag)
        prepareJdsComponent(connection, Table.FieldAlternateCode)
        prepareJdsComponent(connection, Table.EntityTag)
        if (supportsStatements) {
            prepareJdsComponent(connection, Procedure.EntityBinding)
            prepareJdsComponent(connection, Procedure.EntityField)
            prepareJdsComponent(connection, Procedure.FieldEntity)
            prepareJdsComponent(connection, Procedure.EntityEnum)
            prepareJdsComponent(connection, Procedure.Entity)
            prepareJdsComponent(connection, Procedure.Enum)
            prepareJdsComponent(connection, Procedure.Field)
            prepareJdsComponent(connection, Procedure.EntityInheritance)
            prepareJdsComponent(connection, Procedure.FieldDictionary)
            prepareJdsComponent(connection, Procedure.FieldTag)
            prepareJdsComponent(connection, Procedure.EntityTag)
            prepareJdsComponent(connection, Procedure.FieldAlternateCode)
        }
    }

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
            Table.EntityTag -> executeSqlFromString(connection, createEntityTag())
            Table.FieldAlternateCode -> executeSqlFromString(connection, createFieldAlternateCode())
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
            Procedure.EntityTag -> executeSqlFromString(connection, createPopEntityTag())
        }
    }

    internal fun getCallOrStatement(connection: Connection, sql: String): PreparedStatement {
        return if (supportsStatements) {
            connection.prepareCall(sql)
        } else {
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

    /**
     * @param table
     * @param columns LinkedHashMap<columnName -> columnType>
     * @param uniqueColumns LinkedHashMap<constraintName -> constraintColumns>
     * @param primaryKeys LinkedHashMap<constraintName -> constraintColumns>
     * @param foreignKeys LinkedHashMap<constraintName -> LinkedHashMap< LocalColumns -> ReferenceTable(ReferenceColumns)>>
     * @param indexes LinkedHashMap<constraintName -> index columns>
     */
    private fun createTable(
            table: Table,
            columns: Map<String, String>,
            uniqueColumns: Map<String, String> = HashMap(),
            primaryKeys: Map<String, String> = HashMap(),
            foreignKeys: LinkedHashMap<String, LinkedHashMap<String, String>> = LinkedHashMap(),
            indexes: Map<String, String> = HashMap()
    ): String {
        return createTable(getName(table), columns, uniqueColumns, primaryKeys, foreignKeys, indexes)
    }

    /**
     * @param tableName
     * @param columns LinkedHashMap<columnName -> columnType>
     * @param uniqueColumns LinkedHashMap<constraintName -> constraintColumns>
     * @param primaryKeys LinkedHashMap<constraintName -> constraintColumns>
     * @param foreignKeys LinkedHashMap<constraintName -> LinkedHashMap< LocalColumns -> ReferenceTable(ReferenceColumns)>>
     * @param indexes LinkedHashMap<constraintName -> index columns>
     */
    private fun createTable(
            tableName: String,
            columns: Map<String, String>,
            uniqueColumns: Map<String, String> = HashMap(),
            primaryKeys: Map<String, String> = HashMap(),
            foreignKeys: LinkedHashMap<String, LinkedHashMap<String, String>> = LinkedHashMap(),
            indexes: Map<String, String> = HashMap()
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

        uniqueColumns.forEach { (constraintName, constrainColumns) ->
            endingComponents.add("CONSTRAINT $constraintName UNIQUE ($constrainColumns)")
        }

        foreignKeys.forEach { (constraintName, columnBinding) ->
            columnBinding.forEach { (localColumns, remoteTableColumn) ->
                endingComponents.add("CONSTRAINT $constraintName FOREIGN KEY ($localColumns) REFERENCES $remoteTableColumn ON DELETE CASCADE")
            }
        }

        primaryKeys.forEach { (_, constraintColumns) ->
            endingComponents.add("PRIMARY KEY ($constraintColumns)")
        }

        sqlBuilder.append(endingComponents)

        sqlBuilder.append(");\n")

        val indexJoiner = StringJoiner("\n")
        if (implementation != Implementation.MySql && implementation != Implementation.MariaDb) {
            //create index on foreign key columns
            foreignKeys.forEach { (_, columnBinding) ->
                columnBinding.forEach { (localColumns, _) ->
                    if (!localColumns.contains(",")) {
                        indexJoiner.add("CREATE INDEX ${tableName.replace("jds.", "")}_$localColumns ON $tableName($localColumns);")
                    }
                }
            }
        }

        indexes.forEach { (t, u) ->
            indexJoiner.add("CREATE INDEX $t ON $u;")
        }

        sqlBuilder.append(indexJoiner)

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
     * @param tags a description of the entity
     */
    private fun populateRefEntity(connection: Connection, id: Int, name: String, description: String, tags: Array<String>) = try {
        connection.prepareStatement("DELETE FROM ${getName(Table.EntityTag)} WHERE entity_id = ?").use { clearEntityTag ->
            getCallOrStatement(connection, populateEntityTag()).use { populateEntityTag ->
                getCallOrStatement(connection, populateEntity()).use { statement ->
                    statement.setInt(1, id)
                    statement.setString(2, name)
                    statement.setString(3, description)
                    statement.executeUpdate()
                    if (options.logOutput)
                        println("Mapped Entity [$name - $id]")

                    clearEntityTag.setInt(1, id)
                    clearEntityTag.executeUpdate()

                    tags.forEach { tag ->
                        populateEntityTag.setInt(1, id)
                        populateEntityTag.setString(2, tag)
                        populateEntityTag.addBatch()
                    }
                    populateEntityTag.executeBatch()
                }
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
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
                        populateRefEntity(connection, jdsEntity.overview.entityId, entityAnnotation.name, entityAnnotation.description, entityAnnotation.tags)
                        jdsEntity.populateRefFieldRefEntityField(this, connection)
                        jdsEntity.populateRefEnumRefEntityEnum(this, connection)
                        FieldDictionary.update(this, connection, jdsEntity.overview.entityId)
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
     * SQL call to update the field tag table
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateFieldTag() = "{call ${getName(Procedure.FieldTag)}(?, ?)}"

    /**
     * SQL call to update the entity tag table
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateEntityTag() = "{call ${getName(Procedure.EntityTag)}(?, ?)}"

    /**
     * SQL call to update the field dictionary property
     * @return the default or overridden SQL statement for this operation
     */
    internal open fun populateFieldAlternateCode() = "{call ${getName(Procedure.FieldAlternateCode)}(?, ?, ?)}"

    internal fun getName(table: Table): String {
        return "${objectPrefix}${table.table}"
    }

    internal fun getName(procedure: Procedure): String {
        return "${objectPrefix}${procedure.procedure}"
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
        columns["property_name"] = getDataType(FieldType.String, 64)
        return createOrAlterProc(Procedure.FieldDictionary, Table.FieldDictionary, columns, uniqueColumns, false)
    }

    private fun createPopFieldAlternateCode(): String {
        val uniqueColumns = setOf("field_id", "alternate_code")
        val columns = LinkedHashMap<String, String>()
        columns["field_id"] = getDataType(FieldType.Int)
        columns["alternate_code"] = getDataType(FieldType.String, 16)
        columns["value"] = getDataType(FieldType.String, 64)
        return createOrAlterProc(Procedure.FieldAlternateCode, Table.FieldAlternateCode, columns, uniqueColumns, false)
    }

    private fun createPopFieldTag(): String {
        val uniqueColumns = setOf("field_id", "tag")
        val columns = LinkedHashMap<String, String>()
        columns["field_id"] = getDataType(FieldType.Int)
        columns["tag"] = getDataType(FieldType.String, 16)
        return createOrAlterProc(Procedure.FieldTag, Table.FieldTag, columns, uniqueColumns, true)
    }

    private fun createPopEntityTag(): String {
        val uniqueColumns = setOf("entity_id", "tag")
        val columns = LinkedHashMap<String, String>()
        columns["entity_id"] = getDataType(FieldType.Int)
        columns["tag"] = getDataType(FieldType.String, 16)
        return createOrAlterProc(Procedure.EntityTag, Table.EntityTag, columns, uniqueColumns, true)
    }

    private fun getDimensionTableFk(objectName: String): LinkedHashMap<String, LinkedHashMap<String, String>> {
        return linkedMapOf("${objectName}_jds_fk" to linkedMapOf("id, edit_version" to "$dimensionTable(id, edit_version)"))
    }

    private fun createFieldDictionary(): String {
        val table = Table.FieldDictionary
        val objectName = table.table
        val columns = linkedMapOf(
                "entity_id" to getDataTypeImpl(FieldType.Int),
                "field_id" to getDataTypeImpl(FieldType.Int),
                "property_name" to getDataTypeImpl(FieldType.String, 64)
        )
        val uniqueColumns = linkedMapOf("${objectName}_u" to "entity_id, field_id")
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${objectName}_jds_fk_1"] = linkedMapOf("entity_id" to "${getName(Table.Entity)} (id)")
        foreignKeys["${objectName}_jds_fk_2"] = linkedMapOf("field_id" to "${getName(Table.Field)} (id)")
        return createTable(table, columns, uniqueColumns, emptyMap(), foreignKeys)
    }

    private fun createFieldTag(): String {
        val table = Table.FieldTag
        val objectName = table.table
        val columns = linkedMapOf(
                "field_id" to getDataTypeImpl(FieldType.Int),
                "tag" to getDataTypeImpl(FieldType.String, 16)
        )
        val uniqueColumns = linkedMapOf("${objectName}_u" to "field_id, tag")
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${objectName}_jds_fk_2"] = linkedMapOf("field_id" to "${getName(Table.Field)} (id)")
        val indexes = mapOf("${objectName}_tag" to "${getName(table)}(tag)")
        return createTable(table, columns, uniqueColumns, emptyMap(), foreignKeys, indexes)
    }

    private fun createEntityTag(): String {
        val table = Table.EntityTag
        val objectName = table.table
        val columns = linkedMapOf(
                "entity_id" to getDataTypeImpl(FieldType.Int),
                "tag" to getDataTypeImpl(FieldType.String, 16)
        )
        val primaryKey = linkedMapOf("${objectName}_pk" to "entity_id, tag")
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${objectName}_jds_fk_2"] = linkedMapOf("entity_id" to "${getName(Table.Entity)} (id)")
        val indexes = mapOf("${objectName}_tag" to "${getName(table)}(tag)")
        return createTable(table, columns, HashMap(), primaryKey, foreignKeys, indexes)
    }

    private fun createFieldAlternateCode(): String {
        val objectName = Table.FieldAlternateCode.table
        val columns = linkedMapOf(
                "field_id" to getDataTypeImpl(FieldType.Int),
                "alternate_code" to getDataTypeImpl(FieldType.String, 16),
                "value" to getDataTypeImpl(FieldType.String, 64)
        )
        val primaryKey = linkedMapOf("${objectName}_pk" to "field_id, alternate_code")
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${objectName}_jds_fk_1"] = linkedMapOf("field_id" to "${getName(Table.Field)} (id)")
        return createTable(Table.FieldAlternateCode, columns, HashMap(), primaryKey, foreignKeys)
    }

    private fun createStoreEntities(): String {
        val objectName = Table.Entity.table
        val columns = linkedMapOf(
                "id" to getDataTypeImpl(FieldType.Int),
                "name" to getDataTypeImpl(FieldType.String, 64),
                "description" to getDataTypeImpl(FieldType.String, 256)
        )
        val primaryKey = linkedMapOf("${objectName}_pk" to "id")
        return createTable(Table.Entity, columns, HashMap(), primaryKey, LinkedHashMap())
    }

    private fun createRefEnumValues(): String {
        val objectName = Table.Enum.table
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
        val objectName = Table.Field.table
        val columns = linkedMapOf(
                "id" to getDataTypeImpl(FieldType.Int),
                "caption" to getDataTypeImpl(FieldType.String, 64),
                "description" to getDataTypeImpl(FieldType.String, 256),
                "field_type_ordinal" to getDataTypeImpl(FieldType.Int)
        )
        val primaryKey = linkedMapOf("${objectName}_pk" to "id")
        val foreignKeys = LinkedHashMap<String, LinkedHashMap<String, String>>()
        foreignKeys["${objectName}_jds_fk_1"] = linkedMapOf("field_type_ordinal" to "${objectPrefix}${Table.FieldType.table} (ordinal)")
        return createTable(Table.Field, columns, HashMap(), primaryKey, foreignKeys)
    }

    private fun createRefFieldTypes(): String {
        val objectName = Table.FieldType.table
        val columns = linkedMapOf(
                "ordinal" to getDataTypeImpl(FieldType.Int),
                "caption" to getDataTypeImpl(FieldType.String, 64)
        )
        val primaryKey = linkedMapOf("${objectName}_pk" to "ordinal")
        return createTable(Table.FieldType, columns, HashMap(), primaryKey, LinkedHashMap())
    }

    private fun createBindEntityFields(): String {
        val objectName = Table.EntityField.table
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
        val objectName = Table.FieldEntity.table
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
        val objectName = Table.EntityEnum.table
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
        val objectName = Table.EntityInheritance.table
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

    override fun getDataType(fieldType: FieldType) = getDataType(fieldType, 0)

    override fun getDataType(fieldType: FieldType, max: Int) = getDataTypeImpl(fieldType, max)

    /**
     * Gets the underlying database type of the supplied [io.github.subiyacryolite.jds.Field]
     * @param fieldType the supplied [io.github.subiyacryolite.jds.Field]
     * @param max the maximum length of the database type, applied against [io.github.subiyacryolite.jds.enums.FieldType.String] and [io.github.subiyacryolite.jds.enums.FieldType.Blob] types
     * @return the underlying database type of the supplied [io.github.subiyacryolite.jds.Field]
     */
    protected abstract fun getDataTypeImpl(fieldType: FieldType, max: Int = 0): String
}
