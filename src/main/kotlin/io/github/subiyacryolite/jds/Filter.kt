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

import io.github.subiyacryolite.jds.annotations.EntityAnnotation
import io.github.subiyacryolite.jds.context.DbContext
import io.github.subiyacryolite.jds.enums.FieldType
import io.github.subiyacryolite.jds.enums.FilterBy
import java.util.*
import java.util.concurrent.Callable


/**
 * This class is used to perform basic searches based on defined parameters
 * @param dbContext
 * @param referenceType
 */
class Filter<T : Entity>(private val dbContext: DbContext, private val referenceType: Class<T>, private val filterBy: FilterBy) : AutoCloseable, Callable<MutableCollection<T>> {

    private val blockParameters: LinkedList<LinkedList<Any>> = LinkedList()
    private val blockStrings: LinkedList<LinkedList<String>> = LinkedList()
    private val blockSwitches: LinkedList<String> = LinkedList()
    private val tablesToJoin: HashSet<FieldType> = HashSet()
    private var currentStrings: LinkedList<String> = LinkedList()
    private var currentValues: LinkedList<Any> = LinkedList()
    private var entityId:Int = 0
    private val filterColumn: String
        get() {
            return when (filterBy) {
                FilterBy.Id -> "id"
                FilterBy.IdLocation -> "uuid_location"
            }
        }

    constructor(dbContext: DbContext, referenceType: Class<T>) : this(dbContext, referenceType, FilterBy.Id)

    init {
        val je = Entity.getEntityAnnotation(referenceType)
        if (je!=null) {
            entityId = je.id
        } else
            throw IllegalArgumentException("You must annotate the class [" + referenceType.canonicalName + "] or its parent with [" + EntityAnnotation::class.java + "]")
        //==================================
        blockStrings.add(currentStrings)
        blockParameters.add(currentValues)
        blockSwitches.add("")
    }

    /**
     *
     */
    fun or(): Filter<*> {
        currentStrings = LinkedList()
        blockStrings.add(currentStrings)
        currentValues = LinkedList()
        blockParameters.add(currentValues)
        blockSwitches.add(" OR ")
        return this
    }

    /**
     *
     */
    fun and(): Filter<*> {
        currentStrings = LinkedList()
        blockStrings.add(currentStrings)
        currentValues = LinkedList()
        blockParameters.add(currentValues)
        blockSwitches.add(" AND ")
        return this
    }

    /**
     * @throws Exception
     */
    @Throws(Exception::class)
    override fun close() {
        blockParameters.clear()
        blockStrings.clear()
        blockSwitches.clear()
        tablesToJoin.clear()
        currentStrings.clear()
        currentValues.clear()
    }

    /**
     * @throws Exception
     * @return
     */
    @Throws(Exception::class)
    override fun call(): MutableCollection<T> {
        val matchingFilterIds = ArrayList<String>()
        val sql = this.toQuery()
        try {
            dbContext.dataSource.connection.use {
                it.prepareStatement(sql).use { statement ->
                    var parameterIndex = 1
                    for (parameters in blockParameters)
                        for (parameter in parameters) {
                            statement.setObject(parameterIndex, parameter)
                            parameterIndex++
                        }
                    statement.executeQuery().use { resultSet ->
                        while (resultSet.next())
                            matchingFilterIds.add(resultSet.getString("UUID"))
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }

        return if (matchingFilterIds.isEmpty()) {
            //no results. return empty collection
            //if you pass empty collection to jds load it will assume load EVERYTHING
            ArrayList()
        } else Load(dbContext, referenceType, filterBy, matchingFilterIds).call()
    }

    /**
     *
     */
    private fun toQuery(): String {
        val main = StringBuilder()
        main.append("SELECT DISTINCT eo.$filterColumn FROM jds_entity_overview eo ")
        main.append("JOIN jds_ref_entity entity ON eb.entity_id = entity.entity_id")
        if (tablesToJoin.size > 0) {
            main.append(" JOIN ")
            main.append(createLeftJoins(tablesToJoin))
        }
        main.append("\nWHERE entity.entity_id = ")
        main.append(entityId)
        if (blockStrings.size > 0) {
            main.append(" AND ")
        }
        for (chunk in blockStrings.indices) {
            val inner = StringBuilder()
            main.append(blockSwitches[chunk])
            inner.append(blockStrings[chunk].joinToString(" AND\n"))
            main.append("(")
            main.append(inner)
            main.append(")")
        }
        return main.toString()
    }

    /**
     * @param tablesToJoin
     * @return
     */
    private fun createLeftJoins(tablesToJoin: HashSet<FieldType>): String {
        val tables = ArrayList<String>()
        for (ft in tablesToJoin) {
            tables.add(String.format("%s %s on %s.composite_key = eo.composite_key", TableLookup.getTableForFieldType(ft), TableLookup.getTableAliasForFieldType(ft), TableLookup.getTableAliasForFieldType(ft)))
        }
        return tables.joinToString(" JOIN\n")
    }

    override fun toString(): String {
        return toQuery()
    }

    //========================================================CONDITIONS START HERE
    /**
     * @param field
     * @param value
     */
    private fun isLob(field: Field): Boolean {
        return field.type === FieldType.String || field.type === FieldType.Blob
    }

    /**
     * @param field
     * @param value
     */
    fun isNotNull(field: Field): Filter<*> {
        tablesToJoin.add(field.type)
        val builder = String.format("(%s.field_id = %s AND %s IS NOT NULL)",
                TableLookup.getTableAliasForFieldType(field.type),
                field.id,
                (if (dbContext.isOracleDb && isLob(field)) "dbms_lob.substr(PLACE_HOLD.value, dbms_lob.getlength(PLACE_HOLD.value), 1)" else "PLACE_HOLD.value").replace("PLACE_HOLD".toRegex(), TableLookup.getTableAliasForFieldType(field.type)))
        currentStrings.add(builder)
        currentValues.add("")
        return this
    }

    /**
     * @param field
     * @param value
     */
    fun isNull(field: Field): Filter<*> {
        tablesToJoin.add(field.type)
        val builder = String.format("(%s.field_id = %s AND %s IS NULL)",
                TableLookup.getTableAliasForFieldType(field.type),
                field.id,
                (if (dbContext.isOracleDb && isLob(field)) "dbms_lob.substr(PLACE_HOLD.value, dbms_lob.getlength(PLACE_HOLD.value), 1)" else "PLACE_HOLD.value").replace("PLACE_HOLD".toRegex(), TableLookup.getTableAliasForFieldType(field.type)))
        currentStrings.add(builder)
        currentValues.add("")
        return this
    }

    /**
     * @param field
     * @param value
     */
    fun between(field: Field, value1: Any, value2: Any): Filter<*> {
        tablesToJoin.add(field.type)
        val builder = String.format("(%s.field_id = %s AND (%s BETWEEN ? AND ?) )",
                TableLookup.getTableAliasForFieldType(field.type),
                field.id,
                (if (dbContext.isOracleDb && isLob(field)) "dbms_lob.substr(PLACE_HOLD.value, dbms_lob.getlength(PLACE_HOLD.value), 1)" else "PLACE_HOLD.value").replace("PLACE_HOLD".toRegex(), TableLookup.getTableAliasForFieldType(field.type)))
        currentStrings.add(builder)
        currentValues.add(value1)
        currentValues.add(value2)
        return this
    }

    /**
     * @param field
     * @param value
     */
    fun notLessThan(field: Field, value: Any): Filter<*> {
        tablesToJoin.add(field.type)
        val builder = String.format("(%s.field_id = %s AND %s !< ?)",
                TableLookup.getTableAliasForFieldType(field.type), field.id,
                (if (dbContext.isOracleDb && isLob(field)) "dbms_lob.substr(PLACE_HOLD.value, dbms_lob.getlength(PLACE_HOLD.value), 1)" else "PLACE_HOLD.value").replace("PLACE_HOLD".toRegex(), TableLookup.getTableAliasForFieldType(field.type)))
        currentStrings.add(builder)
        currentValues.add(value)
        return this
    }

    /**
     * @param field
     * @param value
     */
    fun lessThan(field: Field, value: Any): Filter<*> {
        tablesToJoin.add(field.type)
        val builder = String.format("(%s.field_id = %s AND %s < ?)",
                TableLookup.getTableAliasForFieldType(field.type),
                field.id,
                (if (dbContext.isOracleDb && isLob(field)) "dbms_lob.substr(PLACE_HOLD.value, dbms_lob.getlength(PLACE_HOLD.value), 1)" else "PLACE_HOLD.value").replace("PLACE_HOLD".toRegex(), TableLookup.getTableAliasForFieldType(field.type)))
        currentStrings.add(builder)
        currentValues.add(value)
        return this
    }

    /**
     * @param field
     * @param value
     */
    fun lessThanOrEqualTo(field: Field, value: Any): Filter<*> {
        tablesToJoin.add(field.type)
        val builder = String.format("(%s.field_id = %s AND %s < ?)",
                TableLookup.getTableAliasForFieldType(field.type),
                field.id,
                (if (dbContext.isOracleDb && isLob(field)) "dbms_lob.substr(PLACE_HOLD.value, dbms_lob.getlength(PLACE_HOLD.value), 1)" else "PLACE_HOLD.value").replace("PLACE_HOLD".toRegex(), TableLookup.getTableAliasForFieldType(field.type)))
        currentStrings.add(builder)
        currentValues.add(value)
        return this
    }

    /**
     * @param field
     * @param value
     */
    fun notGreaterThan(field: Field, value: Any): Filter<*> {
        tablesToJoin.add(field.type)
        val builder = String.format("(%s.field_id = %s AND %s !> ?)",
                TableLookup.getTableAliasForFieldType(field.type),
                field.id,
                (if (dbContext.isOracleDb && isLob(field)) "dbms_lob.substr(PLACE_HOLD.value, dbms_lob.getlength(PLACE_HOLD.value), 1)" else "PLACE_HOLD.value").replace("PLACE_HOLD".toRegex(), TableLookup.getTableAliasForFieldType(field.type)))
        currentStrings.add(builder)
        currentValues.add(value)
        return this
    }

    /**
     * @param field
     * @param value
     */
    fun greaterThan(field: Field, value: Any): Filter<*> {
        tablesToJoin.add(field.type)
        val builder = String.format("(%s.field_id = %s AND %s > ?)",
                TableLookup.getTableAliasForFieldType(field.type),
                field.id,
                (if (dbContext.isOracleDb && isLob(field)) "dbms_lob.substr(PLACE_HOLD.value, dbms_lob.getlength(PLACE_HOLD.value), 1)" else "PLACE_HOLD.value").replace("PLACE_HOLD".toRegex(), TableLookup.getTableAliasForFieldType(field.type)))
        currentStrings.add(builder)
        currentValues.add(value)
        return this
    }

    /**
     * @param field
     * @param value
     */
    fun greaterThanOrEqualTo(field: Field, value: Any): Filter<*> {
        tablesToJoin.add(field.type)
        val builder = String.format("(%s.field_id = %s AND %s >= ?)",
                TableLookup.getTableAliasForFieldType(field.type),
                field.id,
                (if (dbContext.isOracleDb && isLob(field)) "dbms_lob.substr(PLACE_HOLD.value, dbms_lob.getlength(PLACE_HOLD.value), 1)" else "PLACE_HOLD.value").replace("PLACE_HOLD".toRegex(), TableLookup.getTableAliasForFieldType(field.type)))
        currentStrings.add(builder)
        currentValues.add(value)
        return this
    }

    /**
     * @param field
     * @param value
     */
    fun equals(field: Field, value: Any): Filter<*> {
        tablesToJoin.add(field.type)
        val builder = String.format("(%s.field_id = %s AND %s = ?)",
                TableLookup.getTableAliasForFieldType(field.type),
                field.id,
                (if (dbContext.isOracleDb && isLob(field)) "dbms_lob.substr(PLACE_HOLD.value, dbms_lob.getlength(PLACE_HOLD.value), 1)" else "PLACE_HOLD.value").replace("PLACE_HOLD".toRegex(), TableLookup.getTableAliasForFieldType(field.type)))
        currentStrings.add(builder)
        currentValues.add(value)
        return this
    }

    /**
     * @param field
     * @param value
     */
    fun notEquals(field: Field, value: Any): Filter<*> {
        tablesToJoin.add(field.type)
        val builder = String.format("(%s.field_id = %s AND %s <> ?)",
                TableLookup.getTableAliasForFieldType(field.type),
                field.id,
                (if (dbContext.isOracleDb && isLob(field)) "dbms_lob.substr(PLACE_HOLD.value, dbms_lob.getlength(PLACE_HOLD.value), 1)" else "PLACE_HOLD.value").replace("PLACE_HOLD".toRegex(), TableLookup.getTableAliasForFieldType(field.type)))
        currentStrings.add(builder)
        currentValues.add(value)
        return this
    }

    /**
     * @param field
     * @param value
     */
    fun like(field: Field, value: Any): Filter<*> {
        tablesToJoin.add(field.type)
        val builder = String.format("(%s.field_id = %s AND %s LIKE ?)",
                TableLookup.getTableAliasForFieldType(field.type),
                field.id,
                (if (dbContext.isOracleDb && isLob(field)) "dbms_lob.substr(PLACE_HOLD.value, dbms_lob.getlength(PLACE_HOLD.value), 1)" else "PLACE_HOLD.value").replace("PLACE_HOLD".toRegex(), TableLookup.getTableAliasForFieldType(field.type)))
        currentStrings.add(builder)
        currentValues.add(value)
        return this
    }

    /**
     * @param field
     * @param value
     */
    fun startsLike(field: Field, value: Any): Filter<*> {
        tablesToJoin.add(field.type)
        val builder = String.format("(%s.field_id = %s AND %s LIKE ?%)",
                TableLookup.getTableAliasForFieldType(field.type),
                field.id,
                (if (dbContext.isOracleDb && isLob(field)) "dbms_lob.substr(PLACE_HOLD.value, dbms_lob.getlength(PLACE_HOLD.value), 1)" else "PLACE_HOLD.value").replace("PLACE_HOLD".toRegex(), TableLookup.getTableAliasForFieldType(field.type)))
        currentStrings.add(builder)
        currentValues.add(value)
        return this
    }

    /**
     * @param field
     * @param value
     */
    fun endsLike(field: Field, value: Any): Filter<*> {
        tablesToJoin.add(field.type)
        val builder = String.format("(%s.field_id = %s AND %s LIKE %?)",
                TableLookup.getTableAliasForFieldType(field.type),
                field.id,
                (if (dbContext.isOracleDb && isLob(field)) "dbms_lob.substr(PLACE_HOLD.value, dbms_lob.getlength(PLACE_HOLD.value), 1)" else "PLACE_HOLD.value").replace("PLACE_HOLD".toRegex(), TableLookup.getTableAliasForFieldType(field.type)))
        currentStrings.add(builder)
        currentValues.add(value)
        return this
    }

    /**
     * @param field
     * @param value
     */
    fun notLike(field: Field, value: Any): Filter<*> {
        tablesToJoin.add(field.type)
        val builder = String.format("(%s.field_id = %s AND %s NOT LIKE %)",
                TableLookup.getTableAliasForFieldType(field.type),
                field.id,
                (if (dbContext.isOracleDb && isLob(field)) "dbms_lob.substr(PLACE_HOLD.value, dbms_lob.getlength(PLACE_HOLD.value), 1)" else "PLACE_HOLD.value").replace("PLACE_HOLD".toRegex(), TableLookup.getTableAliasForFieldType(field.type)))
        currentStrings.add(builder)
        currentValues.add(value)
        return this
    }

    /**
     * @param field
     * @param value
     */
    fun `in`(field: Field, value: Any): Filter<*> {
        tablesToJoin.add(field.type)
        val builder = String.format("(%s.field_id = %s AND %s IN (?))",
                TableLookup.getTableAliasForFieldType(field.type),
                field.id,
                (if (dbContext.isOracleDb && isLob(field)) "dbms_lob.substr(PLACE_HOLD.value, dbms_lob.getlength(PLACE_HOLD.value), 1)" else "PLACE_HOLD.value").replace("PLACE_HOLD".toRegex(), TableLookup.getTableAliasForFieldType(field.type)))
        currentStrings.add(builder)
        currentValues.add(value)
        currentValues.add(value)
        return this
    }

    /**
     * @param field
     * @param value
     */
    fun notIn(field: Field, value: Any): Filter<*> {
        tablesToJoin.add(field.type)
        val builder = String.format("(%s.field_id = %s AND %s NOT IN (?))",
                TableLookup.getTableAliasForFieldType(field.type),
                field.id,
                (if (dbContext.isOracleDb && isLob(field)) "dbms_lob.substr(PLACE_HOLD.value, dbms_lob.getlength(PLACE_HOLD.value), 1)" else "PLACE_HOLD.value").replace("PLACE_HOLD".toRegex(), TableLookup.getTableAliasForFieldType(field.type)))
        currentStrings.add(builder)
        currentValues.add(value)
        currentValues.add(value)
        return this
    }
}

