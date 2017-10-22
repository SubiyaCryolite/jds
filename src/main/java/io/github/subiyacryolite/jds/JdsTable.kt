package io.github.subiyacryolite.jds

import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation
import io.github.subiyacryolite.jds.enums.JdsFieldType
import io.github.subiyacryolite.jds.events.OnPostSaveEventArguments
import java.io.Serializable
import java.sql.Connection
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet


open class JdsTable : Serializable {
    var name: String = ""
    var uniqueEntries = false
    var fieldIds = ArrayList<Long>()
    var entityVersions = HashMap<Long, MutableSet<Long>>()//EntityId.Versions

    private val columnToFieldMap = LinkedHashMap<String,JdsField>()
    private val columnNames = LinkedList<String>()
    private val insertColumns = StringJoiner(",")
    private val insertParameters = StringJoiner(",")
    private val updates = StringJoiner(",")

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
            entityVersions.putIfAbsent(annotation.entityId, HashSet())
            entityVersions[annotation.entityId]!!.add(annotation.version)
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
        fieldIds.add(field.id)
    }

    /**
     *
     * @param jdsEntity
     * @param onPostSaveEventArguments
     * @throws Exception
     */
    @Throws(Exception::class)
    fun executeSave(jdsEntity: JdsEntity, onPostSaveEventArguments: OnPostSaveEventArguments) {
        val satisfied = satisfiesCondition(jdsEntity)
        if (satisfied) {
            if (uniqueEntries) {
                //tricky, need to build UPSERT syntax for all
                //do it the lazy way
                //update else
                //insert?
            } else {
                //so easy, just insert into
                val query = "INSERT INTO $name ($insertColumns) VALUES ($insertParameters)"

                val preparedStatement = onPostSaveEventArguments.getOrAddStatement(query)
                preparedStatement.setObject(1, jdsEntity.overview.entityGuid)

                //order will be maintained by linked list
                columnNames.forEachIndexed { dex, column ->
                    val field = columnToFieldMap[column]!!
                    when (field.type) {
                        JdsFieldType.ENUM_COLLECTION, JdsFieldType.ENUM -> {
                            JdsFieldEnum[field.id]!!.sequenceValues.forEach {
                                val value = jdsEntity.getReportAtomicValue(field.id, it!!.ordinal)
                                preparedStatement.setObject(dex + 2, value ?: null)
                            }
                        }
                        else -> {
                            val value = jdsEntity.getReportAtomicValue(field.id, 0)
                            preparedStatement.setObject(dex + 2, value ?: null)
                        }
                    }
                }
                preparedStatement.addBatch()
            }
        }
    }

    /**
     * @param jdsDb
     * @param connection
     */
    internal fun generateOrUpdateSchema(jdsDb: JdsDb, connection: Connection) {
        val tableFields = JdsField.values.filter { fieldIds.contains(it.value.id) }.map { it.value }
        val tableSql = JdsSchema.generateTable(jdsDb, name, uniqueEntries)
        val columnSql = JdsSchema.generateColumns(jdsDb, name, tableFields, columnToFieldMap)
        val primaryKey = JdsSchema.getPrimaryKey();

        if (!jdsDb.doesTableExist(connection, name)) {
            connection.prepareStatement(tableSql).use {
                it.executeUpdate()
                println("Created table $name")
            }
        }

        //mandatory primary key
        insertColumns.add(primaryKey)
        insertParameters.add("?")

        columnSql.forEach { columnName, sql ->
            if (!jdsDb.doesColumnExist(connection, name, columnName)) {
                connection.prepareStatement(sql).use {
                    it.executeUpdate()
                    println("Created column $columnName")
                }
            }
            //regardless of column existing add to the query
            columnNames.add(columnName)
            insertColumns.add(columnName)
            insertParameters.add("?")
            updates.add("columnName = ?")
        }
    }

    /**
     *
     * @param jdsEntity
     */
    private fun satisfiesCondition(jdsEntity: JdsEntity): Boolean {
        val notLimitedToSpecificEntity = entityVersions.isEmpty()
        val matchesSpecificEntity = !notLimitedToSpecificEntity && entityVersions.containsKey(jdsEntity.overview.entityId)
        val matchesSpecificVersion = matchesSpecificEntity && entityVersions[jdsEntity.overview.entityId]!!.contains(jdsEntity.overview.version)
        return notLimitedToSpecificEntity || (matchesSpecificEntity && matchesSpecificVersion)
    }
}