/*
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
import java.util.*
import java.util.concurrent.Callable


/**
 * This class is used to perform basic searches based on defined parameters
 */
class JdsFilter<T : JdsEntity>
/**
 * @param jdsDb
 * @param referenceType
 */
(private val jdsDb: JdsDb, private val referenceType: Class<T>) : AutoCloseable, Callable<List<T>> {


    private val blockParameters: LinkedList<LinkedList<Any>> = LinkedList()
    private val blockStrings: LinkedList<LinkedList<String>> = LinkedList()
    private val blockSwitches: LinkedList<String> = LinkedList()
    private val tablesToJoin: HashSet<JdsFieldType> = HashSet()
    private var currentStrings: LinkedList<String> = LinkedList()
    private var currentValues: LinkedList<Any> = LinkedList()
    private var entityId: Long = 0

    init {
        if (referenceType.isAnnotationPresent(JdsEntityAnnotation::class.java)) {
            val je = referenceType.getAnnotation(JdsEntityAnnotation::class.java)
            entityId = je.entityId
        } else
            throw IllegalArgumentException("You must annotate the class [" + referenceType.canonicalName + "] with [" + JdsEntityAnnotation::class.java + "]")
        //==================================
        blockStrings.add(currentStrings)
        blockParameters.add(currentValues)
        blockSwitches.add("")
    }

    fun or(): JdsFilter<*> {
        currentStrings = LinkedList()
        blockStrings.add(currentStrings)
        currentValues = LinkedList()
        blockParameters.add(currentValues)
        blockSwitches.add(" OR ")
        return this
    }

    fun and(): JdsFilter<*> {
        currentStrings = LinkedList()
        blockStrings.add(currentStrings)
        currentValues = LinkedList()
        blockParameters.add(currentValues)
        blockSwitches.add(" AND ")
        return this
    }

    @Throws(Exception::class)
    override fun close() {
        blockParameters.clear()
        blockStrings.clear()
        blockSwitches.clear()
        tablesToJoin.clear()
        currentStrings!!.clear()
        currentValues!!.clear()
    }

    @Throws(Exception::class)
    override fun call(): List<T> {
        val matchingGuids = ArrayList<String>()
        val sql = this.toQuery()
        try {
            jdsDb.getConnection().use { connection ->
                connection.prepareStatement(sql).use { ps ->
                    var parameterIndex = 1
                    for (parameters in blockParameters) {
                        for (paramter in parameters) {
                            ps.setObject(parameterIndex, paramter)
                            parameterIndex++
                        }
                    }
                    val rs = ps.executeQuery()
                    while (rs.next()) {
                        matchingGuids.add(rs.getString("EntityGuid"))
                    }
                    rs.close()
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }

        return if (matchingGuids.isEmpty()) {
            //no results. return empty collection
            //if you pass empty collection to jds load it will assume load EVERYTHING
            ArrayList()
        } else JdsLoad(jdsDb, referenceType, *matchingGuids.toTypedArray()).call()
    }


    fun toQuery(): String {
        val main = StringBuilder()
        main.append("SELECT DISTINCT eo.EntityGuid FROM JdsStoreEntityInheritance eo\n")
        main.append("JOIN JdsRefEntities entity ON eo.EntityId = entity.EntityId")
        if (tablesToJoin.size > 0) {
            main.append(" JOIN ")
            main.append(createLeftJoins(tablesToJoin))
        }
        main.append("\nWHERE entity.EntityId = ")
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

    private fun createLeftJoins(tablesToJoin: HashSet<JdsFieldType>): String {
        val tables = ArrayList<String>()
        for (ft in tablesToJoin) {
            tables.add(String.format("%s %s on %s.EntityGuid = eo.EntityGuid", JdsTableLookup.getTable(ft), JdsTableLookup.getTablePrefix(ft), JdsTableLookup.getTablePrefix(ft)))
        }
        return tables.joinToString(" JOIN\n")
    }

    override fun toString(): String {
        return toQuery()
    }

    //========================================================CONDITIONS START HERE

    private fun isLob(jdsField: JdsField): Boolean {
        return jdsField.type === JdsFieldType.TEXT || jdsField.type === JdsFieldType.BLOB
    }

    fun isNotNull(jdsField: JdsField): JdsFilter<*> {
        tablesToJoin.add(jdsField.type)
        val builder = String.format("(%s.FieldId = %s AND %s IS NOT NULL)",
                JdsTableLookup.getTablePrefix(jdsField.type),
                jdsField.id,
                (if (jdsDb.isOracleDb && isLob(jdsField)) "dbms_lob.substr(PLACE_HOLD.Value, dbms_lob.getlength(PLACE_HOLD.Value), 1)" else "PLACE_HOLD.Value").replace("PLACE_HOLD".toRegex(), JdsTableLookup.getTablePrefix(jdsField.type)))
        currentStrings!!.add(builder)
        currentValues!!.add("")
        return this
    }

    fun isNull(jdsField: JdsField): JdsFilter<*> {
        tablesToJoin.add(jdsField.type)
        val builder = String.format("(%s.FieldId = %s AND %s IS NULL)",
                JdsTableLookup.getTablePrefix(jdsField.type),
                jdsField.id,
                (if (jdsDb.isOracleDb && isLob(jdsField)) "dbms_lob.substr(PLACE_HOLD.Value, dbms_lob.getlength(PLACE_HOLD.Value), 1)" else "PLACE_HOLD.Value").replace("PLACE_HOLD".toRegex(), JdsTableLookup.getTablePrefix(jdsField.type)))
        currentStrings!!.add(builder)
        currentValues!!.add("")
        return this
    }

    fun between(jdsField: JdsField, value1: Any, value2: Any): JdsFilter<*> {
        tablesToJoin.add(jdsField.type)
        val builder = String.format("(%s.FieldId = %s AND (%s BETWEEN ? AND ?) )",
                JdsTableLookup.getTablePrefix(jdsField.type),
                jdsField.id,
                (if (jdsDb.isOracleDb && isLob(jdsField)) "dbms_lob.substr(PLACE_HOLD.Value, dbms_lob.getlength(PLACE_HOLD.Value), 1)" else "PLACE_HOLD.Value").replace("PLACE_HOLD".toRegex(), JdsTableLookup.getTablePrefix(jdsField.type)))
        currentStrings!!.add(builder)
        currentValues!!.add(value1)
        currentValues!!.add(value2)
        return this
    }

    fun notLessThan(jdsField: JdsField, value: Any): JdsFilter<*> {
        tablesToJoin.add(jdsField.type)
        val builder = String.format("(%s.FieldId = %s AND %s !< ?)",
                JdsTableLookup.getTablePrefix(jdsField.type), jdsField.id,
                (if (jdsDb.isOracleDb && isLob(jdsField)) "dbms_lob.substr(PLACE_HOLD.Value, dbms_lob.getlength(PLACE_HOLD.Value), 1)" else "PLACE_HOLD.Value").replace("PLACE_HOLD".toRegex(), JdsTableLookup.getTablePrefix(jdsField.type)))
        currentStrings!!.add(builder)
        currentValues!!.add(value)
        return this
    }

    fun lessThan(jdsField: JdsField, value: Any): JdsFilter<*> {
        tablesToJoin.add(jdsField.type)
        val builder = String.format("(%s.FieldId = %s AND %s < ?)",
                JdsTableLookup.getTablePrefix(jdsField.type),
                jdsField.id,
                (if (jdsDb.isOracleDb && isLob(jdsField)) "dbms_lob.substr(PLACE_HOLD.Value, dbms_lob.getlength(PLACE_HOLD.Value), 1)" else "PLACE_HOLD.Value").replace("PLACE_HOLD".toRegex(), JdsTableLookup.getTablePrefix(jdsField.type)))
        currentStrings!!.add(builder)
        currentValues!!.add(value)
        return this
    }

    fun lessThanOrEqualTo(jdsField: JdsField, value: Any): JdsFilter<*> {
        tablesToJoin.add(jdsField.type)
        val builder = String.format("(%s.FieldId = %s AND %s < ?)",
                JdsTableLookup.getTablePrefix(jdsField.type),
                jdsField.id,
                (if (jdsDb.isOracleDb && isLob(jdsField)) "dbms_lob.substr(PLACE_HOLD.Value, dbms_lob.getlength(PLACE_HOLD.Value), 1)" else "PLACE_HOLD.Value").replace("PLACE_HOLD".toRegex(), JdsTableLookup.getTablePrefix(jdsField.type)))
        currentStrings!!.add(builder)
        currentValues!!.add(value)
        return this
    }

    fun notGreaterThan(jdsField: JdsField, value: Any): JdsFilter<*> {
        tablesToJoin.add(jdsField.type)
        val builder = String.format("(%s.FieldId = %s AND %s !> ?)",
                JdsTableLookup.getTablePrefix(jdsField.type),
                jdsField.id,
                (if (jdsDb.isOracleDb && isLob(jdsField)) "dbms_lob.substr(PLACE_HOLD.Value, dbms_lob.getlength(PLACE_HOLD.Value), 1)" else "PLACE_HOLD.Value").replace("PLACE_HOLD".toRegex(), JdsTableLookup.getTablePrefix(jdsField.type)))
        currentStrings!!.add(builder)
        currentValues!!.add(value)
        return this
    }

    fun greaterThan(jdsField: JdsField, value: Any): JdsFilter<*> {
        tablesToJoin.add(jdsField.type)
        val builder = String.format("(%s.FieldId = %s AND %s > ?)",
                JdsTableLookup.getTablePrefix(jdsField.type),
                jdsField.id,
                (if (jdsDb.isOracleDb && isLob(jdsField)) "dbms_lob.substr(PLACE_HOLD.Value, dbms_lob.getlength(PLACE_HOLD.Value), 1)" else "PLACE_HOLD.Value").replace("PLACE_HOLD".toRegex(), JdsTableLookup.getTablePrefix(jdsField.type)))
        currentStrings!!.add(builder)
        currentValues!!.add(value)
        return this
    }

    fun greaterThanOrEqualTo(jdsField: JdsField, value: Any): JdsFilter<*> {
        tablesToJoin.add(jdsField.type)
        val builder = String.format("(%s.FieldId = %s AND %s >= ?)",
                JdsTableLookup.getTablePrefix(jdsField.type),
                jdsField.id,
                (if (jdsDb.isOracleDb && isLob(jdsField)) "dbms_lob.substr(PLACE_HOLD.Value, dbms_lob.getlength(PLACE_HOLD.Value), 1)" else "PLACE_HOLD.Value").replace("PLACE_HOLD".toRegex(), JdsTableLookup.getTablePrefix(jdsField.type)))
        currentStrings!!.add(builder)
        currentValues!!.add(value)
        return this
    }

    fun equals(jdsField: JdsField, value: Any): JdsFilter<*> {
        tablesToJoin.add(jdsField.type)
        val builder = String.format("(%s.FieldId = %s AND %s = ?)",
                JdsTableLookup.getTablePrefix(jdsField.type),
                jdsField.id,
                (if (jdsDb.isOracleDb && isLob(jdsField)) "dbms_lob.substr(PLACE_HOLD.Value, dbms_lob.getlength(PLACE_HOLD.Value), 1)" else "PLACE_HOLD.Value").replace("PLACE_HOLD".toRegex(), JdsTableLookup.getTablePrefix(jdsField.type)))
        currentStrings!!.add(builder)
        currentValues!!.add(value)
        return this
    }

    fun notEquals(jdsField: JdsField, value: Any): JdsFilter<*> {
        tablesToJoin.add(jdsField.type)
        val builder = String.format("(%s.FieldId = %s AND %s <> ?)",
                JdsTableLookup.getTablePrefix(jdsField.type),
                jdsField.id,
                (if (jdsDb.isOracleDb && isLob(jdsField)) "dbms_lob.substr(PLACE_HOLD.Value, dbms_lob.getlength(PLACE_HOLD.Value), 1)" else "PLACE_HOLD.Value").replace("PLACE_HOLD".toRegex(), JdsTableLookup.getTablePrefix(jdsField.type)))
        currentStrings!!.add(builder)
        currentValues!!.add(value)
        return this
    }

    fun like(jdsField: JdsField, value: Any): JdsFilter<*> {
        tablesToJoin.add(jdsField.type)
        val builder = String.format("(%s.FieldId = %s AND %s LIKE ?)",
                JdsTableLookup.getTablePrefix(jdsField.type),
                jdsField.id,
                (if (jdsDb.isOracleDb && isLob(jdsField)) "dbms_lob.substr(PLACE_HOLD.Value, dbms_lob.getlength(PLACE_HOLD.Value), 1)" else "PLACE_HOLD.Value").replace("PLACE_HOLD".toRegex(), JdsTableLookup.getTablePrefix(jdsField.type)))
        currentStrings!!.add(builder)
        currentValues!!.add(value)
        return this
    }

    fun startsLike(jdsField: JdsField, value: Any): JdsFilter<*> {
        tablesToJoin.add(jdsField.type)
        val builder = String.format("(%s.FieldId = %s AND %s LIKE ?%)",
                JdsTableLookup.getTablePrefix(jdsField.type),
                jdsField.id,
                (if (jdsDb.isOracleDb && isLob(jdsField)) "dbms_lob.substr(PLACE_HOLD.Value, dbms_lob.getlength(PLACE_HOLD.Value), 1)" else "PLACE_HOLD.Value").replace("PLACE_HOLD".toRegex(), JdsTableLookup.getTablePrefix(jdsField.type)))
        currentStrings!!.add(builder)
        currentValues!!.add(value)
        return this
    }

    fun endsLike(jdsField: JdsField, value: Any): JdsFilter<*> {
        tablesToJoin.add(jdsField.type)
        val builder = String.format("(%s.FieldId = %s AND %s LIKE %?)",
                JdsTableLookup.getTablePrefix(jdsField.type),
                jdsField.id,
                (if (jdsDb.isOracleDb && isLob(jdsField)) "dbms_lob.substr(PLACE_HOLD.Value, dbms_lob.getlength(PLACE_HOLD.Value), 1)" else "PLACE_HOLD.Value").replace("PLACE_HOLD".toRegex(), JdsTableLookup.getTablePrefix(jdsField.type)))
        currentStrings!!.add(builder)
        currentValues!!.add(value)
        return this
    }

    fun notLike(jdsField: JdsField, value: Any): JdsFilter<*> {
        tablesToJoin.add(jdsField.type)
        val builder = String.format("(%s.FieldId = %s AND %s NOT LIKE %)",
                JdsTableLookup.getTablePrefix(jdsField.type),
                jdsField.id,
                (if (jdsDb.isOracleDb && isLob(jdsField)) "dbms_lob.substr(PLACE_HOLD.Value, dbms_lob.getlength(PLACE_HOLD.Value), 1)" else "PLACE_HOLD.Value").replace("PLACE_HOLD".toRegex(), JdsTableLookup.getTablePrefix(jdsField.type)))
        currentStrings!!.add(builder)
        currentValues!!.add(value)
        return this
    }

    fun `in`(jdsField: JdsField, value: Any): JdsFilter<*> {
        tablesToJoin.add(jdsField.type)
        val builder = String.format("(%s.FieldId = %s AND %s IN (?))",
                JdsTableLookup.getTablePrefix(jdsField.type),
                jdsField.id,
                (if (jdsDb.isOracleDb && isLob(jdsField)) "dbms_lob.substr(PLACE_HOLD.Value, dbms_lob.getlength(PLACE_HOLD.Value), 1)" else "PLACE_HOLD.Value").replace("PLACE_HOLD".toRegex(), JdsTableLookup.getTablePrefix(jdsField.type)))
        currentStrings!!.add(builder)
        currentValues!!.add(value)
        currentValues!!.add(value)
        return this
    }

    fun notIn(jdsField: JdsField, value: Any): JdsFilter<*> {
        tablesToJoin.add(jdsField.type)
        val builder = String.format("(%s.FieldId = %s AND %s NOT IN (?))",
                JdsTableLookup.getTablePrefix(jdsField.type),
                jdsField.id,
                (if (jdsDb.isOracleDb && isLob(jdsField)) "dbms_lob.substr(PLACE_HOLD.Value, dbms_lob.getlength(PLACE_HOLD.Value), 1)" else "PLACE_HOLD.Value").replace("PLACE_HOLD".toRegex(), JdsTableLookup.getTablePrefix(jdsField.type)))
        currentStrings!!.add(builder)
        currentValues!!.add(value)
        currentValues!!.add(value)
        return this
    }
}

