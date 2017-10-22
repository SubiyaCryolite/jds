package io.github.subiyacryolite.jds

import io.github.subiyacryolite.jds.enums.JdsFieldType

object JdsSchema {
    fun generateTable(jdsDb: IJdsDb, reportName: String, appendOnly: Boolean): String {
        val guidDataType = dataType(jdsDb, JdsFieldType.TEXT, 48)
        val sb = StringBuilder()
        sb.append("CREATE TABLE ")
        sb.append(reportName)
        sb.append("( ${getPrimaryKey()} $guidDataType ${when (appendOnly) {
            true -> "PRIMARY KEY"
            else -> ""
        }})")
        return sb.toString()
    }

    fun generateColumns(jdsDb: IJdsDb, reportName: String, fields: Collection<JdsField>, columnToFieldMap: LinkedHashMap<String, JdsField>): LinkedHashMap<String, String> {
        val collection = LinkedHashMap<String, String>()
        fields.forEach { field ->
            when (field.type) {
                JdsFieldType.CLASS,
                JdsFieldType.ARRAY_FLOAT,
                JdsFieldType.ARRAY_INT,
                JdsFieldType.ARRAY_DOUBLE,
                JdsFieldType.ARRAY_LONG,
                JdsFieldType.ARRAY_TEXT,
                JdsFieldType.ARRAY_DATE_TIME -> {
                }
                JdsFieldType.ENUM_COLLECTION, JdsFieldType.ENUM -> JdsFieldEnum[field.id]!!.sequenceValues.forEach {
                    val columnName = "${field.name}_${it!!.ordinal}"
                    val columnDefinition = dataType(jdsDb, JdsFieldType.INT)
                    collection.put(columnName, String.format(jdsDb.getSqlAddColumn(), reportName, columnName, columnDefinition))
                    columnToFieldMap.put(columnName, field)
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
            JdsFieldType.FLOAT -> return jdsDb.getSqlTypeFloat()
            JdsFieldType.DOUBLE -> return jdsDb.getSqlTypeDouble()
            JdsFieldType.ZONED_DATE_TIME -> return jdsDb.getSqlTypeZonedDateTime()
            JdsFieldType.TIME -> return jdsDb.getSqlTypeTime()
            JdsFieldType.BLOB -> return jdsDb.getSqlTypeBlob(max)
            JdsFieldType.ENUM, JdsFieldType.INT, JdsFieldType.BOOLEAN -> return jdsDb.getSqlTypeInteger()
            JdsFieldType.DATE, JdsFieldType.DATE_TIME -> return jdsDb.getSqlTypeDateTime()
            JdsFieldType.LONG, JdsFieldType.DURATION -> return jdsDb.getSqlTypeLong()
            JdsFieldType.PERIOD, JdsFieldType.TEXT, JdsFieldType.YEAR_MONTH, JdsFieldType.MONTH_DAY -> return jdsDb.getSqlTypeText(max)
        }
        return "invalid"
    }

    fun getPrimaryKey(): String {
        return "uuid"
    }
}