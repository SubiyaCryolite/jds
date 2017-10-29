package io.github.subiyacryolite.jds

import io.github.subiyacryolite.jds.JdsExtensions.setLocalTime
import io.github.subiyacryolite.jds.JdsExtensions.setZonedDateTime
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation
import io.github.subiyacryolite.jds.enums.JdsFieldType
import io.github.subiyacryolite.jds.events.OnPostSaveEventArguments
import java.io.Serializable
import java.sql.Connection
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet


open class JdsTable() : Serializable {
    var name: String = ""
    var uniqueEntries = false
    var onlyLiveRecords = false
    var onlyDeprecatedRecords = false
    var entities = HashSet<Long>()
    var fields = HashSet<Long>()

    private var targetConnection = -1
    private val columnToFieldMap = LinkedHashMap<String, JdsField>()
    private val enumOrdinals = HashMap<String, Int>()
    private val columnNames = LinkedList<String>()
    private val insertColumns = StringJoiner(",")
    private val insertParameters = StringJoiner(",")
    private var deleteSql = ""
    private var insertSql = ""
    private var generatedOrUpdatedSchema = false

    @JvmOverloads
    constructor(entity: Class<out IJdsEntity>, uniqueEntries: Boolean = false) : this() {
        if (entity.isAnnotationPresent(JdsEntityAnnotation::class.java)) {
            val annotation = entity.getAnnotation(JdsEntityAnnotation::class.java)
            name = annotation.entityName
            this.uniqueEntries = uniqueEntries
            registerEntities(entity, true)
        }
    }

    /**
     * If you are creating a JdsTable in memory
     * Register entityVersions here
     * @param entityClass
     * @param registerFields
     */
    @JvmOverloads
    fun registerEntities(entityClass: Class<out IJdsEntity>, registerFields: Boolean = false) {
        if (entityClass.isAnnotationPresent(JdsEntityAnnotation::class.java)) {
            val annotation = entityClass.getAnnotation(JdsEntityAnnotation::class.java)
            entities.add(annotation.entityId)
            if (registerFields) {
                val entity = entityClass.newInstance()
                entity.registerFields(this)
            }
        }
    }

    /**
     * If you are creating a JdsTable in memory
     * Register fieldIds here
     * @param field
     */
    fun registerField(field: JdsField) {
        fields.add(field.id)
    }

    /**
     *
     * @param jdsDb an instance of JdsDb, used to lookup mapped classes and determine SQL types based on implementation
     * @param jdsEntity an entity that may have properties of interest
     * @param onPostSaveEventArguments event arguments that will hold batched SQL queries for execution
     * @throws Exception General IO errors
     */
    @Throws(Exception::class)
    fun executeSave(jdsDb: JdsDb, jdsEntity: JdsEntity, onPostSaveEventArguments: OnPostSaveEventArguments) {
        if (!generatedOrUpdatedSchema)
            throw ExceptionInInitializerError("You must call forceGenerateOrUpdateSchema()")

        val satisfied = satisfiesConditions(jdsDb, jdsEntity)
        if (satisfied) {
            if (uniqueEntries) {
                //if unique delete old entries
                val deleteStatement = onPostSaveEventArguments.getOrAddStatement(targetConnection, deleteSql)
                deleteStatement.setString(1, jdsEntity.overview.uuid)
                deleteStatement.addBatch()
            }
            val insertStatement = onPostSaveEventArguments.getOrAddStatement(targetConnection, insertSql)
            insertStatement.setObject(1, jdsEntity.overview.uuid)

            //order will be maintained by linked list
            columnNames.forEachIndexed { columnIndex, columnName ->
                val field = columnToFieldMap[columnName]!!
                val value = when (field.type == JdsFieldType.ENUM_COLLECTION) {
                    true -> jdsEntity.getReportAtomicValue(field.id, enumOrdinals[columnName]!!)
                    false -> jdsEntity.getReportAtomicValue(field.id, 0)
                }
                when (value) {
                    is ZonedDateTime -> insertStatement.setZonedDateTime(columnIndex + 2, value, jdsDb)
                    is LocalTime -> insertStatement.setLocalTime(columnIndex + 2, value, jdsDb)
                    else -> insertStatement.setObject(columnIndex + 2, value ?: null)
                }
            }
            insertStatement.addBatch()
        }
    }

    /**
     * @param jdsDb an instance of JdsDb, used determine SQL types based on implementation
     * @param pool a shared connection pool to quickly query the target schemas
     */
    internal fun forceGenerateOrUpdateSchema(jdsDb: JdsDb, pool: HashMap<Int, Connection>) {

        if (!pool.containsKey(targetConnection))
            pool.put(targetConnection, jdsDb.getConnection(targetConnection))

        val connection = pool[targetConnection]!!

        val tableFields = JdsField.values.filter { fields.contains(it.value.id) }.map { it.value }
        val tableSql = JdsSchema.generateTable(jdsDb, name, uniqueEntries)
        val columnSql = JdsSchema.generateColumns(jdsDb, name, tableFields, columnToFieldMap, enumOrdinals)
        val primaryKey = JdsSchema.getPrimaryKey()


        if (!jdsDb.doesTableExist(connection, name))
            connection.prepareStatement(tableSql).use {
                it.executeUpdate()
                if (jdsDb.isPrintingOutput)
                    println("Created $name")
            }

        //mandatory primary key
        insertColumns.add(primaryKey)
        insertParameters.add("?")

        columnSql.forEach { columnName, sql ->
            if (!jdsDb.doesColumnExist(connection, name, columnName)) {
                connection.prepareStatement(sql).use {
                    it.executeUpdate()
                    if (jdsDb.isPrintingOutput)
                        println("Created $name.$columnName")
                }
            }
            //regardless of column existing add to the query
            columnNames.add(columnName)
            insertColumns.add(columnName)
            insertParameters.add("?")
        }

        deleteSql = "DELETE FROM $name WHERE $primaryKey = ?"
        insertSql = "INSERT INTO $name ($insertColumns) VALUES ($insertParameters)"

        generatedOrUpdatedSchema = true
    }

    /**
     * Determine if this entity should have its properties persisted to this table
     * @param jdsDb an instance of JdsDb, used to lookup mapped classes
     * @param jdsEntity an entity that may have properties of interest
     */
    private fun satisfiesConditions(jdsDb: JdsDb, jdsEntity: JdsEntity): Boolean {

        if (onlyLiveRecords && !jdsEntity.overview.live)
            return false

        if (onlyDeprecatedRecords && jdsEntity.overview.live)
            return false

        if (entities.isEmpty())
            return true

        entities.forEach { entityCode ->
            val entityType = jdsDb.classes[entityCode]!!
            if (entityType.isInstance(jdsEntity))
                return true
        }
        return false
    }

    /**
     * @param targetConnection
     */
    fun setTargetConnection(targetConnection: Int) {
        this.targetConnection = targetConnection
    }
}