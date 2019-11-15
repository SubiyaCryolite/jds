package io.github.subiyacryolite.jds

import io.github.subiyacryolite.jds.enums.JdsFieldType

object JdsSchema {

    /**
     * @param jdsDb
     * @param tableName
     * @return
     */
    fun generateTable(jdsDb: JdsDb, tableName: String): String {
        val uuidDataType = getDbDataType(jdsDb, JdsFieldType.String, 36)
        val uuidLocationVersionDataType = getDbDataType(jdsDb, JdsFieldType.Int)
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
        //sort fields by ID to ensure correct insertion
        fields.sortedBy { it.id }.filterNot { isIgnoredType(it.type) }.sortedBy { it.name }.forEach {
            when (it.type) {
                JdsFieldType.EnumCollection -> JdsFieldEnum.enums[it.id]!!.values.forEachIndexed { _, enum ->
                    val columnName = "${it.name}_${enum.ordinal}"
                    val columnDataType = getDbDataType(jdsDb, JdsFieldType.Boolean)
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
        JdsFieldType.Blob,
        JdsFieldType.EntityCollection,
        JdsFieldType.FloatCollection,
        JdsFieldType.IntCollection,
        JdsFieldType.DoubleCollection,
        JdsFieldType.LongCollection,
        JdsFieldType.StringCollection,
        JdsFieldType.EnumStringCollection,
        JdsFieldType.DateTimeCollection,
        JdsFieldType.Entity -> true
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
        JdsFieldType.Entity -> jdsDb.getDataType(JdsFieldType.String, 36)//act as a FK if you will
        JdsFieldType.Float -> jdsDb.getDataType(JdsFieldType.Float)
        JdsFieldType.Double -> jdsDb.getDataType(JdsFieldType.Double)
        JdsFieldType.ZonedDateTime -> jdsDb.getDataType(JdsFieldType.ZonedDateTime)
        JdsFieldType.Time -> jdsDb.getDataType(JdsFieldType.Time)
        JdsFieldType.Blob -> jdsDb.getDataType(JdsFieldType.Blob, max)
        JdsFieldType.EnumCollection, JdsFieldType.Boolean -> jdsDb.getDataType(JdsFieldType.Boolean)
        JdsFieldType.Enum, JdsFieldType.Int -> jdsDb.getDataType(JdsFieldType.Int)
        JdsFieldType.DateTime -> jdsDb.getDataType(JdsFieldType.DateTime)
        JdsFieldType.Date -> jdsDb.getDataType(JdsFieldType.Date)
        JdsFieldType.Long, JdsFieldType.Duration -> jdsDb.getDataType(JdsFieldType.Long)
        JdsFieldType.Period, JdsFieldType.String, JdsFieldType.YearMonth, JdsFieldType.MonthDay, JdsFieldType.EnumString -> jdsDb.getDataType(JdsFieldType.String, max)
        else -> "invalid"
    }


    fun generateIndex(jdsDb: JdsDb, tableName: String, column: String): String {
        return jdsDb.getDbCreateIndexSyntax(tableName, column, "${tableName}_ix_$column")
    }
}