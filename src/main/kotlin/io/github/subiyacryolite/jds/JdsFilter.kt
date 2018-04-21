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
import io.github.subiyacryolite.jds.enums.JdsFilterBy
import java.util.*
import java.util.concurrent.Callable


/**
 * This class is used to perform basic searches based on defined parameters
 * @param jdsDb
 * @param referenceType
 */
class JdsFilter<T : JdsEntity>(private val jdsDb: JdsDb, private val referenceType: Class<T>, private val filterBy: JdsFilterBy) : AutoCloseable, Callable<MutableList<T>> {

    private val blockParameters: LinkedList<LinkedList<Any>> = LinkedList()
    private val blockStrings: LinkedList<LinkedList<String>> = LinkedList()
    private val blockSwitches: LinkedList<String> = LinkedList()
    private val tablesToJoin: HashSet<JdsFieldType> = HashSet()
    private var currentStrings: LinkedList<String> = LinkedList()
    private var currentValues: LinkedList<Any> = LinkedList()
    private var entityId = 0L
    private val filterColumn: String
        get() {
            return when (filterBy) {
                JdsFilterBy.UUID -> "uuid"
                JdsFilterBy.UUID_LOCATION -> "uuid_location"
            }
        }

    constructor(jdsDb: JdsDb, referenceType: Class<T>) : this(jdsDb, referenceType, JdsFilterBy.UUID)

    init {
        val classHasAnnotation = referenceType.isAnnotationPresent(JdsEntityAnnotation::class.java)
        val superclassHasAnnotation = referenceType.superclass.isAnnotationPresent(JdsEntityAnnotation::class.java)
        if (classHasAnnotation || superclassHasAnnotation) {
            val je = when (classHasAnnotation) {
                true -> referenceType.getAnnotation(JdsEntityAnnotation::class.java)
                false -> referenceType.superclass.getAnnotation(JdsEntityAnnotation::class.java)
            }
            entityId = je.id
        } else
            throw IllegalArgumentException("You must annotate the class [" + referenceType.canonicalName + "] or its parent with [" + JdsEntityAnnotation::class.java + "]")
        //==================================
        blockStrings.add(currentStrings)
        blockParameters.add(currentValues)
        blockSwitches.add("")
    }

    /**
     *
     */
    fun or(): JdsFilter<*> {
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
    fun and(): JdsFilter<*> {
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
    override fun call(): MutableList<T> {
        val matchingFilterIds = ArrayList<String>()
        val sql = this.toQuery()
        try {
            jdsDb.connection.use {
                it.prepareStatement(sql).use {
                    var parameterIndex = 1
                    for (parameters in blockParameters)
                        for (parameter in parameters) {
                            it.setObject(parameterIndex, parameter)
                            parameterIndex++
                        }
                    it.executeQuery().use {
                        while (it.next())
                            matchingFilterIds.add(it.getString("UUID"))
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
        } else JdsLoad(jdsDb, referenceType, filterBy, matchingFilterIds).call()
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
    private fun createLeftJoins(tablesToJoin: HashSet<JdsFieldType>): String {
        val tables = ArrayList<String>()
        for (ft in tablesToJoin) {
            tables.add(String.format("%s %s on %s.composite_key = eo.composite_key", JdsTableLookup.getTableForFieldType(ft), JdsTableLookup.getTableAliasForFieldType(ft), JdsTableLookup.getTableAliasForFieldType(ft)))
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
    private fun isLob(field: JdsField): Boolean {
        return field.type === JdsFieldType.STRING || field.type === JdsFieldType.BLOB
    }

    /**
     * @param field
     * @param value
     */
    fun isNotNull(field: JdsField): JdsFilter<*> {
        tablesToJoin.add(field.type)
        val builder = String.format("(%s.field_id = %s AND %s IS NOT NULL)",
                JdsTableLookup.getTableAliasForFieldType(field.type),
                field.id,
                (if (jdsDb.isOracleDb && isLob(field)) "dbms_lob.substr(PLACE_HOLD.value, dbms_lob.getlength(PLACE_HOLD.value), 1)" else "PLACE_HOLD.value").replace("PLACE_HOLD".toRegex(), JdsTableLookup.getTableAliasForFieldType(field.type)))
        currentStrings.add(builder)
        currentValues.add("")
        return this
    }

    /**
     * @param field
     * @param value
     */
    fun isNull(field: JdsField): JdsFilter<*> {
        tablesToJoin.add(field.type)
        val builder = String.format("(%s.field_id = %s AND %s IS NULL)",
                JdsTableLookup.getTableAliasForFieldType(field.type),
                field.id,
                (if (jdsDb.isOracleDb && isLob(field)) "dbms_lob.substr(PLACE_HOLD.value, dbms_lob.getlength(PLACE_HOLD.value), 1)" else "PLACE_HOLD.value").replace("PLACE_HOLD".toRegex(), JdsTableLookup.getTableAliasForFieldType(field.type)))
        currentStrings.add(builder)
        currentValues.add("")
        return this
    }

    /**
     * @param field
     * @param value
     */
    fun between(field: JdsField, value1: Any, value2: Any): JdsFilter<*> {
        tablesToJoin.add(field.type)
        val builder = String.format("(%s.field_id = %s AND (%s BETWEEN ? AND ?) )",
                JdsTableLookup.getTableAliasForFieldType(field.type),
                field.id,
                (if (jdsDb.isOracleDb && isLob(field)) "dbms_lob.substr(PLACE_HOLD.value, dbms_lob.getlength(PLACE_HOLD.value), 1)" else "PLACE_HOLD.value").replace("PLACE_HOLD".toRegex(), JdsTableLookup.getTableAliasForFieldType(field.type)))
        currentStrings.add(builder)
        currentValues.add(value1)
        currentValues.add(value2)
        return this
    }

    /**
     * @param field
     * @param value
     */
    fun notLessThan(field: JdsField, value: Any): JdsFilter<*> {
        tablesToJoin.add(field.type)
        val builder = String.format("(%s.field_id = %s AND %s !< ?)",
                JdsTableLookup.getTableAliasForFieldType(field.type), field.id,
                (if (jdsDb.isOracleDb && isLob(field)) "dbms_lob.substr(PLACE_HOLD.value, dbms_lob.getlength(PLACE_HOLD.value), 1)" else "PLACE_HOLD.value").replace("PLACE_HOLD".toRegex(), JdsTableLookup.getTableAliasForFieldType(field.type)))
        currentStrings.add(builder)
        currentValues.add(value)
        return this
    }

    /**
     * @param field
     * @param value
     */
    fun lessThan(field: JdsField, value: Any): JdsFilter<*> {
        tablesToJoin.add(field.type)
        val builder = String.format("(%s.field_id = %s AND %s < ?)",
                JdsTableLookup.getTableAliasForFieldType(field.type),
                field.id,
                (if (jdsDb.isOracleDb && isLob(field)) "dbms_lob.substr(PLACE_HOLD.value, dbms_lob.getlength(PLACE_HOLD.value), 1)" else "PLACE_HOLD.value").replace("PLACE_HOLD".toRegex(), JdsTableLookup.getTableAliasForFieldType(field.type)))
        currentStrings.add(builder)
        currentValues.add(value)
        return this
    }

    /**
     * @param field
     * @param value
     */
    fun lessThanOrEqualTo(field: JdsField, value: Any): JdsFilter<*> {
        tablesToJoin.add(field.type)
        val builder = String.format("(%s.field_id = %s AND %s < ?)",
                JdsTableLookup.getTableAliasForFieldType(field.type),
                field.id,
                (if (jdsDb.isOracleDb && isLob(field)) "dbms_lob.substr(PLACE_HOLD.value, dbms_lob.getlength(PLACE_HOLD.value), 1)" else "PLACE_HOLD.value").replace("PLACE_HOLD".toRegex(), JdsTableLookup.getTableAliasForFieldType(field.type)))
        currentStrings.add(builder)
        currentValues.add(value)
        return this
    }

    /**
     * @param field
     * @param value
     */
    fun notGreaterThan(field: JdsField, value: Any): JdsFilter<*> {
        tablesToJoin.add(field.type)
        val builder = String.format("(%s.field_id = %s AND %s !> ?)",
                JdsTableLookup.getTableAliasForFieldType(field.type),
                field.id,
                (if (jdsDb.isOracleDb && isLob(field)) "dbms_lob.substr(PLACE_HOLD.value, dbms_lob.getlength(PLACE_HOLD.value), 1)" else "PLACE_HOLD.value").replace("PLACE_HOLD".toRegex(), JdsTableLookup.getTableAliasForFieldType(field.type)))
        currentStrings.add(builder)
        currentValues.add(value)
        return this
    }

    /**
     * @param field
     * @param value
     */
    fun greaterThan(field: JdsField, value: Any): JdsFilter<*> {
        tablesToJoin.add(field.type)
        val builder = String.format("(%s.field_id = %s AND %s > ?)",
                JdsTableLookup.getTableAliasForFieldType(field.type),
                field.id,
                (if (jdsDb.isOracleDb && isLob(field)) "dbms_lob.substr(PLACE_HOLD.value, dbms_lob.getlength(PLACE_HOLD.value), 1)" else "PLACE_HOLD.value").replace("PLACE_HOLD".toRegex(), JdsTableLookup.getTableAliasForFieldType(field.type)))
        currentStrings.add(builder)
        currentValues.add(value)
        return this
    }

    /**
     * @param field
     * @param value
     */
    fun greaterThanOrEqualTo(field: JdsField, value: Any): JdsFilter<*> {
        tablesToJoin.add(field.type)
        val builder = String.format("(%s.field_id = %s AND %s >= ?)",
                JdsTableLookup.getTableAliasForFieldType(field.type),
                field.id,
                (if (jdsDb.isOracleDb && isLob(field)) "dbms_lob.substr(PLACE_HOLD.value, dbms_lob.getlength(PLACE_HOLD.value), 1)" else "PLACE_HOLD.value").replace("PLACE_HOLD".toRegex(), JdsTableLookup.getTableAliasForFieldType(field.type)))
        currentStrings.add(builder)
        currentValues.add(value)
        return this
    }

    /**
     * @param field
     * @param value
     */
    fun equals(field: JdsField, value: Any): JdsFilter<*> {
        tablesToJoin.add(field.type)
        val builder = String.format("(%s.field_id = %s AND %s = ?)",
                JdsTableLookup.getTableAliasForFieldType(field.type),
                field.id,
                (if (jdsDb.isOracleDb && isLob(field)) "dbms_lob.substr(PLACE_HOLD.value, dbms_lob.getlength(PLACE_HOLD.value), 1)" else "PLACE_HOLD.value").replace("PLACE_HOLD".toRegex(), JdsTableLookup.getTableAliasForFieldType(field.type)))
        currentStrings.add(builder)
        currentValues.add(value)
        return this
    }

    /**
     * @param field
     * @param value
     */
    fun notEquals(field: JdsField, value: Any): JdsFilter<*> {
        tablesToJoin.add(field.type)
        val builder = String.format("(%s.field_id = %s AND %s <> ?)",
                JdsTableLookup.getTableAliasForFieldType(field.type),
                field.id,
                (if (jdsDb.isOracleDb && isLob(field)) "dbms_lob.substr(PLACE_HOLD.value, dbms_lob.getlength(PLACE_HOLD.value), 1)" else "PLACE_HOLD.value").replace("PLACE_HOLD".toRegex(), JdsTableLookup.getTableAliasForFieldType(field.type)))
        currentStrings.add(builder)
        currentValues.add(value)
        return this
    }

    /**
     * @param field
     * @param value
     */
    fun like(field: JdsField, value: Any): JdsFilter<*> {
        tablesToJoin.add(field.type)
        val builder = String.format("(%s.field_id = %s AND %s LIKE ?)",
                JdsTableLookup.getTableAliasForFieldType(field.type),
                field.id,
                (if (jdsDb.isOracleDb && isLob(field)) "dbms_lob.substr(PLACE_HOLD.value, dbms_lob.getlength(PLACE_HOLD.value), 1)" else "PLACE_HOLD.value").replace("PLACE_HOLD".toRegex(), JdsTableLookup.getTableAliasForFieldType(field.type)))
        currentStrings.add(builder)
        currentValues.add(value)
        return this
    }

    /**
     * @param field
     * @param value
     */
    fun startsLike(field: JdsField, value: Any): JdsFilter<*> {
        tablesToJoin.add(field.type)
        val builder = String.format("(%s.field_id = %s AND %s LIKE ?%)",
                JdsTableLookup.getTableAliasForFieldType(field.type),
                field.id,
                (if (jdsDb.isOracleDb && isLob(field)) "dbms_lob.substr(PLACE_HOLD.value, dbms_lob.getlength(PLACE_HOLD.value), 1)" else "PLACE_HOLD.value").replace("PLACE_HOLD".toRegex(), JdsTableLookup.getTableAliasForFieldType(field.type)))
        currentStrings.add(builder)
        currentValues.add(value)
        return this
    }

    /**
     * @param field
     * @param value
     */
    fun endsLike(field: JdsField, value: Any): JdsFilter<*> {
        tablesToJoin.add(field.type)
        val builder = String.format("(%s.field_id = %s AND %s LIKE %?)",
                JdsTableLookup.getTableAliasForFieldType(field.type),
                field.id,
                (if (jdsDb.isOracleDb && isLob(field)) "dbms_lob.substr(PLACE_HOLD.value, dbms_lob.getlength(PLACE_HOLD.value), 1)" else "PLACE_HOLD.value").replace("PLACE_HOLD".toRegex(), JdsTableLookup.getTableAliasForFieldType(field.type)))
        currentStrings.add(builder)
        currentValues.add(value)
        return this
    }

    /**
     * @param field
     * @param value
     */
    fun notLike(field: JdsField, value: Any): JdsFilter<*> {
        tablesToJoin.add(field.type)
        val builder = String.format("(%s.field_id = %s AND %s NOT LIKE %)",
                JdsTableLookup.getTableAliasForFieldType(field.type),
                field.id,
                (if (jdsDb.isOracleDb && isLob(field)) "dbms_lob.substr(PLACE_HOLD.value, dbms_lob.getlength(PLACE_HOLD.value), 1)" else "PLACE_HOLD.value").replace("PLACE_HOLD".toRegex(), JdsTableLookup.getTableAliasForFieldType(field.type)))
        currentStrings.add(builder)
        currentValues.add(value)
        return this
    }

    /**
     * @param field
     * @param value
     */
    fun `in`(field: JdsField, value: Any): JdsFilter<*> {
        tablesToJoin.add(field.type)
        val builder = String.format("(%s.field_id = %s AND %s IN (?))",
                JdsTableLookup.getTableAliasForFieldType(field.type),
                field.id,
                (if (jdsDb.isOracleDb && isLob(field)) "dbms_lob.substr(PLACE_HOLD.value, dbms_lob.getlength(PLACE_HOLD.value), 1)" else "PLACE_HOLD.value").replace("PLACE_HOLD".toRegex(), JdsTableLookup.getTableAliasForFieldType(field.type)))
        currentStrings.add(builder)
        currentValues.add(value)
        currentValues.add(value)
        return this
    }

    /**
     * @param field
     * @param value
     */
    fun notIn(field: JdsField, value: Any): JdsFilter<*> {
        tablesToJoin.add(field.type)
        val builder = String.format("(%s.field_id = %s AND %s NOT IN (?))",
                JdsTableLookup.getTableAliasForFieldType(field.type),
                field.id,
                (if (jdsDb.isOracleDb && isLob(field)) "dbms_lob.substr(PLACE_HOLD.value, dbms_lob.getlength(PLACE_HOLD.value), 1)" else "PLACE_HOLD.value").replace("PLACE_HOLD".toRegex(), JdsTableLookup.getTableAliasForFieldType(field.type)))
        currentStrings.add(builder)
        currentValues.add(value)
        currentValues.add(value)
        return this
    }
}

