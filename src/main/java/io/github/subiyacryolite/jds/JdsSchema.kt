package io.github.subiyacryolite.jds

import io.github.subiyacryolite.jds.enums.JdsFieldType

object JdsSchema {
    fun generateTable(jdsDb: IJdsDb, reportName: String, appendOnly: Boolean): String {
        val guidDataType = dataType(jdsDb, JdsFieldType.STRING, 48)
        val sb = StringBuilder()
        sb.append("CREATE TABLE ")
        sb.append(reportName)
        sb.append("( ${getPrimaryKey()} $guidDataType ${when (appendOnly) {
            true -> "PRIMARY KEY"
            else -> ""
        }})")
        return sb.toString()
    }

    fun generateColumns(jdsDb: IJdsDb, reportName: String, fields: Collection<JdsField>, columnToFieldMap: LinkedHashMap<String, JdsField>, enumOrdinals: HashMap<String, Int>): LinkedHashMap<String, String> {
        val collection = LinkedHashMap<String, String>()
        fields.sortedBy { it.name }.forEach { field ->
            when (field.type) {
                JdsFieldType.ENTITY_COLLECTION,
                JdsFieldType.FLOAT_COLLECTION,
                JdsFieldType.INT_COLLECTION,
                JdsFieldType.DOUBLE_COLLECTION,
                JdsFieldType.LONG_COLLECTION,
                JdsFieldType.STRING_COLLECTION,
                JdsFieldType.DATE_TIME_COLLECTION -> {
                }
                JdsFieldType.ENUM_COLLECTION -> JdsFieldEnum[field.id]!!.values.forEachIndexed { index, enum ->
                    val columnName = "${field.name}_${enum!!.ordinal}"
                    val columnDefinition = dataType(jdsDb, JdsFieldType.INT)
                    collection.put(columnName, String.format(jdsDb.getSqlAddColumn(), reportName, columnName, columnDefinition))
                    columnToFieldMap.put(columnName, field)

                    enumOrdinals.put(columnName, enum!!.ordinal)
                }
                else -> {
                    collection.put(field.name, generateColumn(jdsDb, reportName, field))
                    columnToFieldMap.put(field.name, field)
                }
            }
        }
        return collection
    }

    @JvmOverloads
    private fun generateColumn(jdsDb: IJdsDb, reportName: String, field: JdsField, max: Int = 0): String {
        val columnName = field.name
        val columnDefinition = dataType(jdsDb, field.type, max)
        return String.format(jdsDb.getSqlAddColumn(), reportName, columnName, columnDefinition)
    }

    @JvmOverloads
    fun dataType(jdsDb: IJdsDb, fieldType: JdsFieldType, max: Int = 0): String {
        when (fieldType) {
            JdsFieldType.ENTITY -> return jdsDb.getSqlTypeText(48)//act as a fk if you will
            JdsFieldType.FLOAT -> return jdsDb.getSqlTypeFloat()
            JdsFieldType.DOUBLE -> return jdsDb.getSqlTypeDouble()
            JdsFieldType.ZONED_DATE_TIME -> return jdsDb.getSqlTypeZonedDateTime()
            JdsFieldType.TIME -> return jdsDb.getSqlTypeTime()
            JdsFieldType.BLOB -> return jdsDb.getSqlTypeBlob(max)
            JdsFieldType.BOOLEAN -> return jdsDb.getSqlTypeBoolean()
            JdsFieldType.ENUM, JdsFieldType.INT -> return jdsDb.getSqlTypeInteger()
            JdsFieldType.DATE, JdsFieldType.DATE_TIME -> return jdsDb.getSqlTypeDateTime()
            JdsFieldType.LONG, JdsFieldType.DURATION -> return jdsDb.getSqlTypeLong()
            JdsFieldType.PERIOD, JdsFieldType.STRING, JdsFieldType.YEAR_MONTH, JdsFieldType.MONTH_DAY -> return jdsDb.getSqlTypeText(max)
        }
        return "invalid"
    }

    fun getPrimaryKey(): String {
        return "uuid"
    }
}