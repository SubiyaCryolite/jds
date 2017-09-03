package io.github.subiyacryolite.jds


import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation
import io.github.subiyacryolite.jds.enums.JdsFieldType
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
                val cleanName = cleanViewName(jdsEntityAnnotation.entityName)
                val viewName = getMainViewName(cleanName)
                val inheritanceHierarchy = ArrayList<Long>()
                inheritanceHierarchy.add(entityId)
                try {
                    jdsDb.getConnection().use { connection ->
                        populateChildEntities(connection, inheritanceHierarchy, entityId)
                        val arrayFloatView = innerView(connection, jdsDb, JdsFieldType.ARRAY_FLOAT, inheritanceHierarchy, cleanName)
                        val arrayIntView = innerView(connection, jdsDb, JdsFieldType.ARRAY_INT, inheritanceHierarchy, cleanName)
                        val arrayDoubleView = innerView(connection, jdsDb, JdsFieldType.ARRAY_DOUBLE, inheritanceHierarchy, cleanName)
                        val arrayLongView = innerView(connection, jdsDb, JdsFieldType.ARRAY_LONG, inheritanceHierarchy, cleanName)
                        val arrayTextView = innerView(connection, jdsDb, JdsFieldType.ARRAY_TEXT, inheritanceHierarchy, cleanName)
                        val arrayDateTimeView = innerView(connection, jdsDb, JdsFieldType.ARRAY_DATE_TIME, inheritanceHierarchy, cleanName)
                        val booleanView = innerView(connection, jdsDb, JdsFieldType.BOOLEAN, inheritanceHierarchy, cleanName)
                        //String blobView = innerView(connection, jdsDb, JdsFieldType.BLOB, entityAndChildren, name);, problem with PG implementation
                        val dateTimeView = innerView(connection, jdsDb, JdsFieldType.DATE_TIME, inheritanceHierarchy, cleanName)
                        val dateView = innerView(connection, jdsDb, JdsFieldType.DATE, inheritanceHierarchy, cleanName)
                        val doubleView = innerView(connection, jdsDb, JdsFieldType.DOUBLE, inheritanceHierarchy, cleanName)
                        val enumView = innerView(connection, jdsDb, JdsFieldType.ENUM, inheritanceHierarchy, cleanName)
                        val arrayEnumView = innerView(connection, jdsDb, JdsFieldType.ENUM_COLLECTION, inheritanceHierarchy, cleanName)
                        val floatView = innerView(connection, jdsDb, JdsFieldType.FLOAT, inheritanceHierarchy, cleanName)
                        val intView = innerView(connection, jdsDb, JdsFieldType.INT, inheritanceHierarchy, cleanName)
                        val longView = innerView(connection, jdsDb, JdsFieldType.LONG, inheritanceHierarchy, cleanName)
                        val timeView = innerView(connection, jdsDb, JdsFieldType.TIME, inheritanceHierarchy, cleanName)
                        val textView = innerView(connection, jdsDb, JdsFieldType.TEXT, inheritanceHierarchy, cleanName)
                        val zonedDateTimeView = innerView(connection, jdsDb, JdsFieldType.ZONED_DATE_TIME, inheritanceHierarchy, cleanName)
                        val unused = arrayOf(arrayFloatView, arrayIntView, arrayDoubleView, arrayLongView, arrayTextView, arrayDateTimeView, arrayEnumView)
                        createMainView(connection,
                                jdsDb,
                                inheritanceHierarchy,
                                viewName,
                                arrayOf(booleanView,
                                        //blobView, problem with PG implementation
                                        enumView, dateTimeView, dateView, doubleView, floatView, intView, longView, timeView, textView, zonedDateTimeView))
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
                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        inheritanceHierarchy.add(rs.getLong("ChildEntityCode"))
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
            val sql = String.format("SELECT field.FieldName FROM \n" +
                    "JdsRefEntities entity\n" +
                    "LEFT JOIN JdsBindEntityFields bound\n" +
                    "ON entity.EntityId = bound.EntityId\n" +
                    "LEFT JOIN JdsRefFields field \n" +
                    "ON bound.FieldId = field.FieldId\n" +
                    "LEFT join JdsRefFieldTypes type\n" +
                    "ON field.FieldId = type.TypeId\n" +
                    "WHERE %s NOT IN ('BLOB','ARRAY_FLOAT', 'ARRAY_INT', 'ARRAY_DOUBLE', 'ARRAY_LONG', 'ARRAY_TEXT', 'ARRAY_DATE_TIME','ENUM_COLLECTION') AND entity.EntityId = ?\n" +
                    "ORDER BY field.FieldName", if (jdsDb.isOracleDb) "dbms_lob.substr(type.TypeName, dbms_lob.getlength(type.TypeName), 1)" else "type.TypeName")
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
                ex.printStackTrace(System.err)
            }

            val stringBuilder = StringBuilder("SELECT overview.EntityGuid, overview.DateCreated, overview.DateModified")
            if (!fieldNames.isEmpty()) {
                stringBuilder.append(", ")
                val stringJoiner = StringJoiner(", ")
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
                val name = cleanViewName(je.entityName)
                val viewName = getMainViewName(name)
                jdsDb.getConnection().use { connection ->
                    dropView(jdsDb, viewName)
                    dropView(jdsDb, getViewName(JdsFieldType.ARRAY_FLOAT, name))
                    dropView(jdsDb, getViewName(JdsFieldType.ARRAY_INT, name))
                    dropView(jdsDb, getViewName(JdsFieldType.ARRAY_DOUBLE, name))
                    dropView(jdsDb, getViewName(JdsFieldType.ARRAY_LONG, name))
                    dropView(jdsDb, getViewName(JdsFieldType.ARRAY_TEXT, name))
                    dropView(jdsDb, getViewName(JdsFieldType.ARRAY_DATE_TIME, name))
                    dropView(jdsDb, getViewName(JdsFieldType.BOOLEAN, name))
                    //dropView(connection, getViewName(JdsFieldType.BLOB, name));
                    dropView(jdsDb, getViewName(JdsFieldType.DATE_TIME, name))
                    dropView(jdsDb, getViewName(JdsFieldType.DATE, name))
                    dropView(jdsDb, getViewName(JdsFieldType.DOUBLE, name))
                    dropView(jdsDb, getViewName(JdsFieldType.ENUM_COLLECTION, name))
                    dropView(jdsDb, getViewName(JdsFieldType.ENUM, name))
                    dropView(jdsDb, getViewName(JdsFieldType.FLOAT, name))
                    dropView(jdsDb, getViewName(JdsFieldType.INT, name))
                    dropView(jdsDb, getViewName(JdsFieldType.LONG, name))
                    dropView(jdsDb, getViewName(JdsFieldType.TIME, name))
                    dropView(jdsDb, getViewName(JdsFieldType.TEXT, name))
                    dropView(jdsDb, getViewName(JdsFieldType.ZONED_DATE_TIME, name))
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
            val sql = String.format("select distinct field.FieldName from \n" +
                    "JdsRefEntities entity\n" +
                    "left join JdsBindEntityFields bound\n" +
                    "on entity.EntityId = bound.EntityId\n" +
                    "left join JdsRefFields field \n" +
                    "on bound.FieldId = field.FieldId\n" +
                    "left join JdsRefFieldTypes type\n" +
                    "on field.FieldId = type.TypeId\n" +
                    "where %s = ? and entity.EntityId = ?\n" +
                    "order by field.FieldName", if (jdsDb.isOracleDb) "dbms_lob.substr(type.TypeName, dbms_lob.getlength(type.TypeName), 1)" else "type.TypeName")
            //handle oracle nclobs
            //omit ; as oracle jdbc is a pain
            val fieldNames = ArrayList<String>()
            try {
                connection.prepareStatement(sql).use { ps ->
                    ps.setString(1, fieldType.toString())
                    ps.setLong(2, inheritanceHierarchy[0])
                    val rs = ps.executeQuery()
                    while (rs.next()) {
                        fieldNames.add(rs.getString("FieldName"))
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
                fieldsOfInterest.add(String.format("MAX(CASE WHEN t.FieldName = '%s' THEN t.Value ELSE NULL END) AS %s", entry, entry))
            }
            stringBuilder.append(fieldsOfInterest)
            stringBuilder.append("\nFROM\t\n")
            stringBuilder.append("\t(\n")
            stringBuilder.append("\t\tSELECT src.EntityGuid, field.FieldName, src.Value\n")
            stringBuilder.append("\t\tFROM ")
            stringBuilder.append(JdsTableLookup.getTable(fieldType))
            stringBuilder.append("\tsrc\n")
            stringBuilder.append("\t\t JOIN JdsStoreEntityOverview ov ON ov.EntityGuid = src.EntityGuid")
            val entityHierarchy = StringJoiner(",")
            for (id in inheritanceHierarchy)
                entityHierarchy.add("" + id)
            stringBuilder.append(String.format(" AND ov.EntityGuid IN (SELECT DISTINCT EntityGuid FROM JdsStoreEntityInheritance eh WHERE eh.EntityId IN (%s))\n", entityHierarchy))
            stringBuilder.append("\t\tJOIN JdsRefFields field on src.FieldId = field.FieldId\n")
            stringBuilder.append("\t\tJOIN JdsRefFieldTypes fieldType on field.FieldId = fieldType.TypeId \n")
            stringBuilder.append(String.format("\t\tWHERE src.FieldId IN (SELECT DISTINCT ef.FieldId FROM JdsBindEntityFields ef where ef.EntityId in (%s) and %s = '%s')\n",
                    entityHierarchy,
                    if (jdsDb.isOracleDb) "dbms_lob.substr(fieldType.TypeName, dbms_lob.getlength(fieldType.TypeName), 1)" else "fieldType.TypeName", //nclob magic
                    fieldType))
            stringBuilder.append("\t) t\n")
            stringBuilder.append("\tGROUP BY t.EntityGuid")//oracle jdbc hates ; terminators

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
            return String.format("vw_%s_%s", entityName, cleanViewName(fieldType.toString()))
        }

        /**
         * Generate a string that will be used as a view name
         *
         * @param entityName
         * @return
         */
        private fun getMainViewName(entityName: String): String {
            return String.format("_%s", entityName)
        }

        /**
         * Executes a view drop
         *
         * @param jdsDb the SQL connection that will be used for this operation
         * @param name  the view to drop
         * @return whether the action completed successfully
         */
        private fun dropView(jdsDb: JdsDb, name: String): Boolean {
            try {
                jdsDb.getConnection().use { connection -> connection.prepareStatement(String.format("DROP VIEW %s%s", name, if (jdsDb.isPosgreSqlDb) " CASCADE" else "")).use { preparedStatement -> return preparedStatement.execute() } }
            } catch (e: Exception) {
                return false
            }

        }
    }
}