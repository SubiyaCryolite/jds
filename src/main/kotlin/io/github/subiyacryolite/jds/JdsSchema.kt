package io.github.subiyacryolite.jds

import io.github.subiyacryolite.jds.enums.JdsFieldType
import io.github.subiyacryolite.jds.enums.JdsImplementation

object JdsSchema {

    /**
     * @param jdsDb
     * @param reportName
     * @param appendOnly
     * @return
     */
    fun generateTable(jdsDb: JdsDb, reportName: String, appendOnly: Boolean): String {
        val compositeKeyDataType = getDbDataType(jdsDb, JdsFieldType.STRING, 128)
        val stringBuilder = StringBuilder()
        stringBuilder.append("CREATE TABLE ")
        stringBuilder.append(reportName)
        stringBuilder.append("( $compositeKeyColumn $compositeKeyDataType ${when (appendOnly) {
            true -> ", FOREIGN KEY ($compositeKeyColumn) REFERENCES jds_entity_overview($compositeKeyColumn) ON DELETE CASCADE"
            else -> ""
        }})")
        return stringBuilder.toString()
    }

    /**
     * @param jdsDb
     * @param reportName
     * @param fields
     * @param columnToFieldMap
     * @param enumOrdinals
     * @return
     */
    fun generateColumns(jdsDb: IJdsDb, fields: Collection<JdsField>, columnToFieldMap: LinkedHashMap<String, JdsField>, enumOrdinals: HashMap<String, Int>): LinkedHashMap<String, String> {
        val collection = LinkedHashMap<String, String>()
        fields.sortedBy { it.name }.forEach {
            when (it.type) {
                JdsFieldType.BLOB,
                JdsFieldType.ENTITY_COLLECTION,
                JdsFieldType.FLOAT_COLLECTION,
                JdsFieldType.INT_COLLECTION,
                JdsFieldType.DOUBLE_COLLECTION,
                JdsFieldType.LONG_COLLECTION,
                JdsFieldType.STRING_COLLECTION,
                JdsFieldType.DATE_TIME_COLLECTION -> {
                    //necessary
                }
                JdsFieldType.ENUM_COLLECTION -> JdsFieldEnum.enums[it.id]!!.values.forEachIndexed { _, enum ->
                    val columnName = "${it.name}_${enum!!.ordinal}"
                    val columnDataType = getDbDataType(jdsDb, JdsFieldType.BOOLEAN)
                    collection[columnName] = "$columnName $columnDataType"
                    columnToFieldMap[columnName] = it
                    enumOrdinals[columnName] = enum!!.ordinal
                }
                else -> {
                    collection[it.name] = generateColumn(jdsDb, it)
                    columnToFieldMap[it.name] = it
                }
            }
        }
        return collection
    }

    /**
     * @param jdsDb
     * @param reportName
     * @param field
     * @param max
     * @return
     */
    @JvmOverloads
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
        JdsFieldType.ENTITY -> jdsDb.getDbStringDataType(195)//act as a FK if you will
        JdsFieldType.FLOAT -> jdsDb.getDbFloatDataType()
        JdsFieldType.DOUBLE -> jdsDb.getDbDoubleDataType()
        JdsFieldType.ZONED_DATE_TIME -> jdsDb.getDbZonedDateTimeDataType()
        JdsFieldType.TIME -> jdsDb.getDbTimeDataType()
        JdsFieldType.BLOB -> jdsDb.getDbBlobDataType(max)
        JdsFieldType.BOOLEAN -> jdsDb.getDbBooleanDataType()
        JdsFieldType.ENUM, JdsFieldType.INT -> jdsDb.getDbIntegerDataType()
        JdsFieldType.DATE, JdsFieldType.DATE_TIME -> jdsDb.getDbDateTimeDataType()
        JdsFieldType.LONG, JdsFieldType.DURATION -> jdsDb.getDbLongDataType()
        JdsFieldType.PERIOD, JdsFieldType.STRING, JdsFieldType.YEAR_MONTH, JdsFieldType.MONTH_DAY -> jdsDb.getDbStringDataType(max)
        else -> "invalid"
    }


    fun generateIndex(jdsDb: JdsDb, tableName: String, column: String): String {
        return jdsDb.getDbCreateIndexSyntax(tableName, column, "${tableName}_ix_$column")
    }

    /**
     * @return
     */
    val compositeKeyColumn = "composite_key"
}