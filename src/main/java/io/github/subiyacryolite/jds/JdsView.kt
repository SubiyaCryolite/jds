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
import io.github.subiyacryolite.jds.enums.JdsComponent
import io.github.subiyacryolite.jds.enums.JdsFieldType
import io.github.subiyacryolite.jds.enums.JdsImplementation
import java.sql.Connection
import java.sql.SQLException
import java.util.*

/**
 * This class is responsible for creating and deleting flat views of [JdsEntities][JdsEntity]
 * Created by ifunga on 24/06/2017.
 */
class JdsView {

    companion object {
        /**
         * Create flat tables that will be representative of a JdsEntity
         *
         * @param target the [JdsEntity]
         * @param jdsDb  an instance of [JdsDb]
         * @return whether the operation completed successfully
         * @throws IllegalArgumentException
         */
        @Throws(IllegalArgumentException::class)
        fun create(target: Class<out JdsEntity>, jdsDb: JdsDb): Boolean {
            if (!jdsDb.supportsStatements())
                throw IllegalArgumentException("The underlying database does not support the creation of views")
            if (target.isAnnotationPresent(JdsEntityAnnotation::class.java)) {
                val jdsEntityAnnotation = target.getAnnotation(JdsEntityAnnotation::class.java)
                val entityId = jdsEntityAnnotation.entityId
                val entityViewName = cleanViewName(jdsEntityAnnotation.entityName)
                val viewName = getMainViewName(entityViewName)
                val inheritanceHierarchy = ArrayList<Long>()
                inheritanceHierarchy.add(entityId)
                try {
                    jdsDb.getConnection().use { connection ->
                        populateChildEntities(connection, inheritanceHierarchy, entityId)
                        val arrayFloatView = innerView(connection, jdsDb, JdsFieldType.ARRAY_FLOAT, inheritanceHierarchy, entityViewName)
                        val arrayIntView = innerView(connection, jdsDb, JdsFieldType.ARRAY_INT, inheritanceHierarchy, entityViewName)
                        val arrayDoubleView = innerView(connection, jdsDb, JdsFieldType.ARRAY_DOUBLE, inheritanceHierarchy, entityViewName)
                        val arrayLongView = innerView(connection, jdsDb, JdsFieldType.ARRAY_LONG, inheritanceHierarchy, entityViewName)
                        val arrayTextView = innerView(connection, jdsDb, JdsFieldType.ARRAY_TEXT, inheritanceHierarchy, entityViewName)
                        val arrayDateTimeView = innerView(connection, jdsDb, JdsFieldType.ARRAY_DATE_TIME, inheritanceHierarchy, entityViewName)
                        val booleanView = innerView(connection, jdsDb, JdsFieldType.BOOLEAN, inheritanceHierarchy, entityViewName)
                        //String blobView = innerView(connection, jdsDb, JdsFieldType.BLOB, entityAndChildren, name);, problem with PG implementation
                        val dateTimeView = innerView(connection, jdsDb, JdsFieldType.DATE_TIME, inheritanceHierarchy, entityViewName)
                        val dateView = innerView(connection, jdsDb, JdsFieldType.DATE, inheritanceHierarchy, entityViewName)
                        val doubleView = innerView(connection, jdsDb, JdsFieldType.DOUBLE, inheritanceHierarchy, entityViewName)
                        val enumView = innerView(connection, jdsDb, JdsFieldType.ENUM, inheritanceHierarchy, entityViewName)
                        val arrayEnumView = innerView(connection, jdsDb, JdsFieldType.ENUM_COLLECTION, inheritanceHierarchy, entityViewName)
                        val floatView = innerView(connection, jdsDb, JdsFieldType.FLOAT, inheritanceHierarchy, entityViewName)
                        val intView = innerView(connection, jdsDb, JdsFieldType.INT, inheritanceHierarchy, entityViewName)
                        val longView = innerView(connection, jdsDb, JdsFieldType.LONG, inheritanceHierarchy, entityViewName)
                        val timeView = innerView(connection, jdsDb, JdsFieldType.TIME, inheritanceHierarchy, entityViewName)
                        val textView = innerView(connection, jdsDb, JdsFieldType.TEXT, inheritanceHierarchy, entityViewName)
                        val zonedDateTimeView = innerView(connection, jdsDb, JdsFieldType.ZONED_DATE_TIME, inheritanceHierarchy, entityViewName)
                        val periodView = innerView(connection, jdsDb, JdsFieldType.PERIOD, inheritanceHierarchy, entityViewName)
                        val durationView = innerView(connection, jdsDb, JdsFieldType.DURATION, inheritanceHierarchy, entityViewName)
                        val yearMonthView = innerView(connection, jdsDb, JdsFieldType.YEAR_MONTH, inheritanceHierarchy, entityViewName)
                        val monthDayView = innerView(connection, jdsDb, JdsFieldType.MONTH_DAY, inheritanceHierarchy, entityViewName)
                        val unused = arrayOf(arrayFloatView, arrayIntView, arrayDoubleView, arrayLongView, arrayTextView, arrayDateTimeView, arrayEnumView)
                        createMainView(connection,
                                jdsDb,
                                inheritanceHierarchy,
                                viewName,
                                arrayOf(booleanView,
                                        //blobView, problem with PG implementation
                                        enumView, dateTimeView, dateView, doubleView, floatView, intView, longView, timeView, textView, zonedDateTimeView, periodView, durationView, yearMonthView, monthDayView))
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace(System.err)
                }
                return true
            } else {
                throw IllegalArgumentException("You must annotate the class [" + target.canonicalName + "] with [" + JdsEntityAnnotation::class.java + "]")
            }
        }

        /**
         * Determine the inheritance hierarchy of the supplied Entity Id
         *
         * @param connection           the shared SQL connection to use
         * @param inheritanceHierarchy the inheritance hierarchy of the supplied Entity type
         * @param entityId             the id of the Entity type
         * @throws SQLException a possible SQL exception
         */
        @Throws(SQLException::class)
        private fun populateChildEntities(connection: Connection, inheritanceHierarchy: MutableList<Long>, entityId: Long) {
            val sql = "SELECT ChildEntityCode FROM JdsRefEntityInheritance WHERE ParentEntityCode = ?"
            connection.prepareStatement(sql).use { ps ->
                ps.setLong(1, entityId)
                ps.executeQuery().use {
                    while (it.next()) {
                        inheritanceHierarchy.add(it.getLong("ChildEntityCode"))
                    }
                }
            }
        }

        /**
         * @param connection the SQL connection that will be used for this operation
         * @param viewName   the final name of this view
         * @param tables     the tables that will make up this view
         */
        private fun createMainView(connection: Connection, jdsDb: JdsDb, entityId: List<Long>, viewName: String, tables: Array<String>) {
            val condition = if (jdsDb.isOracleDb) "dbms_lob.substr(${JdsComponent.REF_FIELD_TYPES.prefix}.TypeName, dbms_lob.getlength(${JdsComponent.REF_FIELD_TYPES.prefix}.TypeName), 1)" else "${JdsComponent.REF_FIELD_TYPES.prefix}.TypeName"
            val orderBy = if (jdsDb.isOracleDb)"ORDER BY dbms_lob.substr(${JdsComponent.REF_FIELDS.prefix}.FieldName, dbms_lob.getlength(${JdsComponent.REF_FIELDS.prefix}.FieldName), 1)" else "ORDER BY ${JdsComponent.REF_FIELDS.prefix}.FieldName"
            val sql = "SELECT ${JdsComponent.REF_FIELDS.prefix}.FieldName FROM ${JdsComponent.REF_ENTITIES.component} ${JdsComponent.REF_ENTITIES.prefix}\n" +
                    "LEFT JOIN ${JdsComponent.BIND_ENTITY_FIELDS.component} ${JdsComponent.BIND_ENTITY_FIELDS.prefix} ON ${JdsComponent.REF_ENTITIES.prefix}.EntityId = ${JdsComponent.BIND_ENTITY_FIELDS.prefix}.EntityId\n" +
                    "LEFT JOIN ${JdsComponent.REF_FIELDS.component} ${JdsComponent.REF_FIELDS.prefix} ON ${JdsComponent.BIND_ENTITY_FIELDS.prefix}.FieldId = ${JdsComponent.REF_FIELDS.prefix}.FieldId\n" +
                    "LEFT join ${JdsComponent.REF_FIELD_TYPES.component} ${JdsComponent.REF_FIELD_TYPES.prefix} ON ${JdsComponent.REF_FIELDS.prefix}.FieldId = ${JdsComponent.REF_FIELD_TYPES.prefix}.TypeId\n" +
                    "WHERE $condition NOT IN ('BLOB','ARRAY_FLOAT', 'ARRAY_INT', 'ARRAY_DOUBLE', 'ARRAY_LONG', 'ARRAY_TEXT', 'ARRAY_DATE_TIME','ENUM_COLLECTION') AND ${JdsComponent.REF_ENTITIES.prefix}.EntityId = ?\n" +
                    "$orderBy"
            //handle oracle nclobs
            //omit ; as oracle jdbc is a pain
            val fieldNames = ArrayList<String>()
            try {
                connection.prepareStatement(sql).use { preparedStatement ->
                    preparedStatement.setLong(1, entityId[0])
                    val resultSet = preparedStatement.executeQuery()
                    while (resultSet.next()) {
                        fieldNames.add(resultSet.getString("FieldName"))
                    }
                }
            } catch (ex: Exception) {
                System.err.println(sql)
                ex.printStackTrace(System.err)
            }

            val stringBuilder = StringBuilder("SELECT overview.EntityGuid, overview.DateCreated, overview.DateModified")
            if (!fieldNames.isEmpty()) {
                stringBuilder.append(", ")
                val stringJoiner = when (jdsDb.isMySqlDb) {
                    true -> StringJoiner(", ", "'", "'")
                    false -> StringJoiner(",")
                }
                for (entry in fieldNames) {
                    stringJoiner.add(entry)
                }
                stringBuilder.append(stringJoiner.toString())
            }
            stringBuilder.append("\n")
            stringBuilder.append("FROM JdsStoreEntityOverview overview\n")
            val stringJoiner = StringJoiner("\n")
            for (index in tables.indices)
                stringJoiner.add(String.format("LEFT JOIN %s vwn%s ON overview.EntityGuid = vwn%s.EntityGuid", tables[index], index, index))
            stringBuilder.append(stringJoiner.toString())
            stringBuilder.append("\nWHERE overview.EntityGuid IN (SELECT DISTINCT EntityGuid FROM JdsStoreEntityInheritance eh WHERE eh.EntityId IN (")
            val entityHeiarchy = StringJoiner(",")
            for (l in entityId)
                entityHeiarchy.add(l.toString() + "")
            stringBuilder.append(entityHeiarchy)
            stringBuilder.append("))")//oracle jdbc hates ; terminators

            val viewSql = jdsDb.createOrAlterView(viewName, stringBuilder.toString())
            try {
                connection.prepareStatement(viewSql).use { preparedStatement -> preparedStatement.execute() }
            } catch (ex: Exception) {
                System.err.println(viewSql)
                ex.printStackTrace(System.err)
            }
        }

        /**
         * Delete all flat tables bound to a JdsEntity
         *
         * @param target the [JdsEntity]
         * @param jdsDb  an instance of [JdsDb]
         * @return whether the operation completed successfully
         * @throws IllegalArgumentException
         */
        @Throws(IllegalArgumentException::class, SQLException::class, ClassNotFoundException::class)
        fun delete(target: Class<out JdsEntity>, jdsDb: JdsDb): Boolean {
            if (!jdsDb.supportsStatements())
                throw IllegalArgumentException("The underlying database does not support the creation of views")
            if (target.isAnnotationPresent(JdsEntityAnnotation::class.java)) {
                val je = target.getAnnotation(JdsEntityAnnotation::class.java)
                val cleanViewName = cleanViewName(je.entityName)
                val viewName = getMainViewName(cleanViewName)
                jdsDb.getConnection().use { connection ->
                    dropView(jdsDb, connection, viewName)
                    dropView(jdsDb, connection, getViewName(JdsFieldType.ARRAY_FLOAT, cleanViewName))
                    dropView(jdsDb, connection, getViewName(JdsFieldType.ARRAY_INT, cleanViewName))
                    dropView(jdsDb, connection, getViewName(JdsFieldType.ARRAY_DOUBLE, cleanViewName))
                    dropView(jdsDb, connection, getViewName(JdsFieldType.ARRAY_LONG, cleanViewName))
                    dropView(jdsDb, connection, getViewName(JdsFieldType.ARRAY_TEXT, cleanViewName))
                    dropView(jdsDb, connection, getViewName(JdsFieldType.ARRAY_DATE_TIME, cleanViewName))
                    dropView(jdsDb, connection, getViewName(JdsFieldType.BOOLEAN, cleanViewName))
                    //dropView(connection, getViewName(JdsFieldType.BLOB, name));
                    dropView(jdsDb, connection, getViewName(JdsFieldType.DATE_TIME, cleanViewName))
                    dropView(jdsDb, connection, getViewName(JdsFieldType.DATE, cleanViewName))
                    dropView(jdsDb, connection, getViewName(JdsFieldType.DOUBLE, cleanViewName))
                    dropView(jdsDb, connection, getViewName(JdsFieldType.ENUM_COLLECTION, cleanViewName))
                    dropView(jdsDb, connection, getViewName(JdsFieldType.ENUM, cleanViewName))
                    dropView(jdsDb, connection, getViewName(JdsFieldType.FLOAT, cleanViewName))
                    dropView(jdsDb, connection, getViewName(JdsFieldType.INT, cleanViewName))
                    dropView(jdsDb, connection, getViewName(JdsFieldType.LONG, cleanViewName))
                    dropView(jdsDb, connection, getViewName(JdsFieldType.TIME, cleanViewName))
                    dropView(jdsDb, connection, getViewName(JdsFieldType.TEXT, cleanViewName))
                    dropView(jdsDb, connection, getViewName(JdsFieldType.ZONED_DATE_TIME, cleanViewName))
                    dropView(jdsDb, connection, getViewName(JdsFieldType.PERIOD, cleanViewName))
                    dropView(jdsDb, connection, getViewName(JdsFieldType.DURATION, cleanViewName))
                    dropView(jdsDb, connection, getViewName(JdsFieldType.YEAR_MONTH, cleanViewName))
                    dropView(jdsDb, connection, getViewName(JdsFieldType.MONTH_DAY, cleanViewName))
                }
                return true
            } else {
                throw IllegalArgumentException("You must annotate the class [" + target.canonicalName + "] with [" + JdsEntityAnnotation::class.java + "]")
            }
        }

        /**
         * Create names that are suitable for use in the database
         *
         * @param rawViewName raw version of the proposed view name
         * @return value that is suitable for use in the database
         */
        private fun cleanViewName(rawViewName: String): String {
            return rawViewName.replace("\\s+".toRegex(), "_").toLowerCase()
        }

        /**
         * Create a view of a specific data-type for a JdsEntity
         *
         * @param connection           the SQL connection that will be used for this operation
         * @param jdsDb                an instance of JdsDb
         * @param fieldType            the data-type of this view
         * @param inheritanceHierarchy the id codes of the JdsEntity and its children
         * @param entityName           the raw entity name
         * @return the created inner view name
         */
        private fun innerView(connection: Connection, jdsDb: JdsDb, fieldType: JdsFieldType, inheritanceHierarchy: List<Long>, entityName: String): String {
            val prefix = if (jdsDb.isOracleDb) "dbms_lob.substr(field.FieldName, dbms_lob.getlength(field.FieldName), 1)" else "field.FieldName"
            val sql = String.format("SELECT DISTINCT $prefix fieldNameValue FROM \n" +
                    "JdsRefEntities entity\n" +
                    "LEFT JOIN JdsBindEntityFields bound\n" +
                    "ON entity.EntityId = bound.EntityId\n" +
                    "LEFT JOIN JdsRefFields field \n" +
                    "ON bound.FieldId = field.FieldId\n" +
                    "LEFT JOIN JdsRefFieldTypes type\n" +
                    "ON field.FieldId = type.TypeId\n" +
                    "WHERE %s = ? AND entity.EntityId = ?\n" +
                    "ORDER BY fieldNameValue", if (jdsDb.isOracleDb) "dbms_lob.substr(type.TypeName, dbms_lob.getlength(type.TypeName), 1)" else "type.TypeName")
            val fieldNames = ArrayList<String>()
            try {
                connection.prepareStatement(sql).use { ps ->
                    ps.setString(1, fieldType.toString())
                    ps.setLong(2, inheritanceHierarchy[0])
                    val rs = ps.executeQuery()
                    while (rs.next()) {
                        fieldNames.add(rs.getString("fieldNameValue"))
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace(System.err)
            }

            val viewName = getViewName(fieldType, entityName)

            val stringBuilder = StringBuilder("SELECT t.EntityGuid AS EntityGuid")
            if (!fieldNames.isEmpty()) {
                stringBuilder.append(",\n")
            }
            val fieldsOfInterest = StringJoiner(",\n")
            for (entry in fieldNames) {
                when {
                    jdsDb.isMySqlDb -> fieldsOfInterest.add("MAX(CASE WHEN t.FieldName = '$entry' THEN t.Value ELSE NULL END) AS '$entry'")
                    jdsDb.isOracleDb -> when (JdsTableLookup.getComponent(fieldType)) {
                        JdsComponent.STORE_TEXT -> fieldsOfInterest.add("MAX(CASE WHEN dbms_lob.substr(t.FieldName, dbms_lob.getlength(t.FieldName), 1) = '$entry' THEN dbms_lob.substr(t.Value, dbms_lob.getlength(t.Value), 1) ELSE NULL END) AS $entry")
                        else -> fieldsOfInterest.add("MAX(CASE WHEN dbms_lob.substr(t.FieldName, dbms_lob.getlength(t.FieldName), 1) = '$entry' THEN t.Value ELSE NULL END) AS $entry")
                    }
                    else -> fieldsOfInterest.add("MAX(CASE WHEN t.FieldName = '$entry' THEN t.Value ELSE NULL END) AS $entry")
                }
            }
            stringBuilder.append(fieldsOfInterest)
            stringBuilder.append("\nFROM\t\n")
            stringBuilder.append("\t(\n")
            stringBuilder.append("\t\tSELECT src.EntityGuid, ${JdsComponent.REF_FIELDS.prefix}.FieldName, src.Value\n")
            stringBuilder.append("\t\tFROM ")
            stringBuilder.append(JdsTableLookup.getComponentTable(fieldType))
            stringBuilder.append("\tsrc\n")
            stringBuilder.append("\t\tJOIN ${JdsComponent.STORE_ENTITY_OVERVIEW.component} ${JdsComponent.STORE_ENTITY_OVERVIEW.prefix} ON ${JdsComponent.STORE_ENTITY_OVERVIEW.prefix}.EntityGuid = src.EntityGuid")
            val entityHierarchy = StringJoiner(",")
            for (id in inheritanceHierarchy)
                entityHierarchy.add("" + id)
            stringBuilder.append(" AND ${JdsComponent.STORE_ENTITY_OVERVIEW.prefix}.EntityGuid IN (SELECT DISTINCT EntityGuid FROM ${JdsComponent.STORE_ENTITY_INHERITANCE.component} ${JdsComponent.STORE_ENTITY_INHERITANCE.prefix} WHERE ${JdsComponent.STORE_ENTITY_INHERITANCE.prefix}.EntityId IN ($entityHierarchy))\n")
            stringBuilder.append("\t\tJOIN ${JdsComponent.REF_FIELDS.component} ${JdsComponent.REF_FIELDS.prefix} on src.FieldId = ${JdsComponent.REF_FIELDS.prefix}.FieldId\n")
            stringBuilder.append("\t\tJOIN ${JdsComponent.REF_FIELD_TYPES.component} ${JdsComponent.REF_FIELD_TYPES.prefix} on ${JdsComponent.REF_FIELDS.prefix}.FieldId = ${JdsComponent.REF_FIELD_TYPES.prefix}.TypeId \n")
            val cond = if (jdsDb.isOracleDb) "dbms_lob.substr(${JdsComponent.REF_FIELD_TYPES.prefix}.TypeName, dbms_lob.getlength(${JdsComponent.REF_FIELD_TYPES.prefix}.TypeName), 1)" else "${JdsComponent.REF_FIELD_TYPES.prefix}.TypeName" //nclob magic
            stringBuilder.append("\t\tWHERE src.FieldId IN (SELECT DISTINCT ${JdsComponent.BIND_ENTITY_FIELDS.prefix}.FieldId FROM ${JdsComponent.BIND_ENTITY_FIELDS.component} ${JdsComponent.BIND_ENTITY_FIELDS.prefix} where ${JdsComponent.BIND_ENTITY_FIELDS.prefix}.EntityId in ($entityHierarchy) and $cond = '$fieldType')\n")
            stringBuilder.append("\t) t\n")
            stringBuilder.append("GROUP BY EntityGuid")//oracle jdbc hates ; terminators

            val sqlToExecute = jdsDb.createOrAlterView(viewName, stringBuilder.toString())
            try {
                connection.prepareStatement(sqlToExecute).use { preparedStatement -> preparedStatement.execute() }
            } catch (ex: Exception) {
                System.err.println(sqlToExecute)
                ex.printStackTrace(System.err)
            }
            return viewName
        }

        /**
         * Generate a string that will be used as a view name
         *
         * @param fieldType
         * @param entityName
         * @return
         */
        private fun getViewName(fieldType: JdsFieldType, entityName: String): String {
            return String.format("vw_%s_%s", entityName, cleanViewName(fieldType.shortCode))
        }

        /**
         * Generate a string that will be used as a view name
         *
         * @param entityName
         * @return
         */
        private fun getMainViewName(entityName: String): String {
            return String.format("v_%s", entityName)
        }

        /**
         * Executes a view drop
         *
         * @param jdsDb the SQL connection that will be used for this operation
         * @param name  the view to drop
         * @return whether the action completed successfully
         */
        private fun dropView(jdsDb: JdsDb, connection: Connection, name: String): Boolean {
            val pre = when (jdsDb.isOracleDb) {
                true -> name
                else -> name
            }
            val post = when (jdsDb.implementation) {
                JdsImplementation.POSTGRES -> " CASCADE"
                else -> ""
            }
            val src = "DROP VIEW $pre$post"
            try {
                connection.prepareStatement(src).use { preparedStatement -> return preparedStatement.execute() }
            } catch (e: Exception) {
                return false
            }
        }
    }
}