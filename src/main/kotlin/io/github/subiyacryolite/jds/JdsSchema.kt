package io.github.subiyacryolite.jds

import io.github.subiyacryolite.jds.enums.JdsFieldType

object JdsSchema {

    /**
     * @param jdsDb
     * @param tableName
     * @return
     */
    fun generateTable(jdsDb: JdsDb, tableName: String): String {
        val uuidDataType = getDbDataType(jdsDb, JdsFieldType.STRING, 36)
        val uuidLocationVersionDataType = getDbDataType(jdsDb, JdsFieldType.INT)
        val stringBuilder = StringBuilder()
        stringBuilder.append("CREATE TABLE ")
        stringBuilder.append(tableName)
        stringBuilder.append("(uuid $uuidDataType, edit_version $uuidLocationVersionDataType,\n")
        stringBuilder.append("CONSTRAINT ${tableName}_uc_composite UNIQUE (uuid, edit_version),\n")
        stringBuilder.append("FOREIGN KEY (uuid, edit_version) REFERENCES ${jdsDb.dimensionTable}(uuid, edit_version) ON DELETE CASCADE)")
        return stringBuilder.toString()
    }

    /**
     * @param jdsDb
     * @param fields
     * @param columnToFieldMap
     * @param enumOrdinals
     * @return
     */
    fun generateColumns(jdsDb: IJdsDb, fields: Collection<JdsField>, columnToFieldMap: LinkedHashMap<String, JdsField>, enumOrdinals: HashMap<String, Int>): LinkedHashMap<String, String> {
        val collection = LinkedHashMap<String, String>()
        fields.filterNot { isIgnoredType(it.type) }.sortedBy { it.name }.forEach {
            when (it.type) {
                JdsFieldType.ENUM_COLLECTION -> JdsFieldEnum.enums[it.id]!!.values.forEachIndexed { _, enum ->
                    val columnName = "${it.name}_${enum.ordinal}"
                    val columnDataType = getDbDataType(jdsDb, JdsFieldType.BOOLEAN)
                    collection[columnName] = "$columnName $columnDataType"
                    columnToFieldMap[columnName] = it
                    enumOrdinals[columnName] = enum.ordinal
                }
                else -> {
                    collection[it.name] = generateColumn(jdsDb, it)
                    columnToFieldMap[it.name] = it
                }
            }
        }
        return collection
    }

    fun isIgnoredType(type: JdsFieldType) = when (type) {
        JdsFieldType.BLOB,
        JdsFieldType.ENTITY_COLLECTION,
        JdsFieldType.FLOAT_COLLECTION,
        JdsFieldType.INT_COLLECTION,
        JdsFieldType.DOUBLE_COLLECTION,
        JdsFieldType.LONG_COLLECTION,
        JdsFieldType.STRING_COLLECTION,
        JdsFieldType.ENUM_STRING_COLLECTION,
        JdsFieldType.DATE_TIME_COLLECTION,
        JdsFieldType.ENTITY -> true
        else -> false
    }

    fun isIgnoredType(type: Long): Boolean {
        val actualType = JdsField.values[type] ?: return true
        return isIgnoredType(actualType.type)
    }

    /**
     * @param jdsDb
     * @param field
     * @param max
     * @return
     */
    private fun generateColumn(jdsDb: IJdsDb, field: JdsField, max: Int = 0): String {
        val columnName = field.name
        val columnType = getDbDataType(jdsDb, field.type, max)
        return "$columnName $columnType"
    }

    /**
     * @param jdsDb
     * @param fieldType
     * @param max
     * @return
     */
    @JvmOverloads
    fun getDbDataType(jdsDb: IJdsDb, fieldType: JdsFieldType, max: Int = 0): String = when (fieldType) {
        JdsFieldType.ENTITY -> jdsDb.getDataType(JdsFieldType.STRING, 36)//act as a FK if you will
        JdsFieldType.FLOAT -> jdsDb.getDataType(JdsFieldType.FLOAT)
        JdsFieldType.DOUBLE -> jdsDb.getDataType(JdsFieldType.DOUBLE)
        JdsFieldType.ZONED_DATE_TIME -> jdsDb.getDataType(JdsFieldType.ZONED_DATE_TIME)
        JdsFieldType.TIME -> jdsDb.getDataType(JdsFieldType.TIME)
        JdsFieldType.BLOB -> jdsDb.getDataType(JdsFieldType.BLOB, max)
        JdsFieldType.ENUM_COLLECTION, JdsFieldType.BOOLEAN -> jdsDb.getDataType(JdsFieldType.BOOLEAN)
        JdsFieldType.ENUM, JdsFieldType.INT -> jdsDb.getDataType(JdsFieldType.INT)
        JdsFieldType.DATE_TIME -> jdsDb.getDataType(JdsFieldType.DATE_TIME)
        JdsFieldType.DATE -> jdsDb.getDataType(JdsFieldType.DATE)
        JdsFieldType.LONG, JdsFieldType.DURATION -> jdsDb.getDataType(JdsFieldType.LONG)
        JdsFieldType.PERIOD, JdsFieldType.STRING, JdsFieldType.YEAR_MONTH, JdsFieldType.MONTH_DAY, JdsFieldType.ENUM_STRING -> jdsDb.getDataType(JdsFieldType.STRING, max)
        else -> "invalid"
    }


    fun generateIndex(jdsDb: JdsDb, tableName: String, column: String): String {
        return jdsDb.getDbCreateIndexSyntax(tableName, column, "${tableName}_ix_$column")
    }
}