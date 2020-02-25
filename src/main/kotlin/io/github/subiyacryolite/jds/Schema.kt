package io.github.subiyacryolite.jds

import io.github.subiyacryolite.jds.context.DbContext
import io.github.subiyacryolite.jds.context.IDbContext
import io.github.subiyacryolite.jds.enums.FieldType

object Schema {

    /**
     * @param dbContext
     * @param tableName
     * @return
     */
    fun generateTable(dbContext: DbContext, tableName: String): String {
        val idDataType = getDbDataType(dbContext, FieldType.String, 36)
        val editVersionDataType = getDbDataType(dbContext, FieldType.Int)
        val stringBuilder = StringBuilder()
        stringBuilder.append("CREATE TABLE ")
        stringBuilder.append(tableName)
        stringBuilder.append("(id $idDataType, edit_version $editVersionDataType,\n")
        stringBuilder.append("CONSTRAINT ${tableName}_uc_composite UNIQUE (id, edit_version),\n")
        stringBuilder.append("FOREIGN KEY (id, edit_version) REFERENCES ${dbContext.dimensionTable}(id, edit_version) ON DELETE CASCADE)")
        return stringBuilder.toString()
    }

    /**
     * @param dbContext
     * @param fields
     * @param columnToFieldMap
     * @param enumOrdinals
     * @return
     */
    fun generateColumns(dbContext: IDbContext, fields: Collection<Field>, columnToFieldMap: LinkedHashMap<String, Field>, enumOrdinals: HashMap<String, Int>): LinkedHashMap<String, String> {
        val collection = LinkedHashMap<String, String>()
        //sort fields by ID to ensure correct insertion
        fields.sortedBy { it.id }.filterNot { isIgnoredType(it.type) }.sortedBy { it.name }.forEach {
            when (it.type) {
                FieldType.EnumCollection -> FieldEnum.enums[it.id]!!.values.forEachIndexed { _, enum ->
                    val columnName = "${it.name}_${enum.ordinal}"
                    val columnDataType = getDbDataType(dbContext, FieldType.Boolean)
                    collection[columnName] = "$columnName $columnDataType"
                    columnToFieldMap[columnName] = it
                    enumOrdinals[columnName] = enum.ordinal
                }
                else -> {
                    collection[it.name] = generateColumn(dbContext, it)
                    columnToFieldMap[it.name] = it
                }
            }
        }
        return collection
    }

    fun isIgnoredType(type: FieldType) = when (type) {
        FieldType.Blob,
        FieldType.EntityCollection,
        FieldType.FloatCollection,
        FieldType.IntCollection,
        FieldType.UuidCollection,
        FieldType.DoubleCollection,
        FieldType.LongCollection,
        FieldType.StringCollection,
        FieldType.EnumStringCollection,
        FieldType.DateTimeCollection,
        FieldType.Entity -> true
        else -> false
    }

    fun isIgnoredType(type: Int): Boolean {
        val actualType = Field.values[type] ?: return true
        return isIgnoredType(actualType.type)
    }

    /**
     * @param dbContext
     * @param field
     * @param max
     * @return
     */
    private fun generateColumn(dbContext: IDbContext, field: Field, max: Int = 0): String {
        val columnName = field.name
        val columnType = getDbDataType(dbContext, field.type, max)
        return "$columnName $columnType"
    }

    /**
     * @param dbContext
     * @param fieldType
     * @param max
     * @return
     */
    @JvmOverloads
    fun getDbDataType(dbContext: IDbContext, fieldType: FieldType, max: Int = 0): String = when (fieldType) {
        FieldType.Entity -> dbContext.getDataType(FieldType.String, 36)//act as a FK if you will
        FieldType.Float -> dbContext.getDataType(FieldType.Float)
        FieldType.Double -> dbContext.getDataType(FieldType.Double)
        FieldType.Uuid -> dbContext.getDataType(FieldType.Uuid)
        FieldType.ZonedDateTime -> dbContext.getDataType(FieldType.ZonedDateTime)
        FieldType.Time -> dbContext.getDataType(FieldType.Time)
        FieldType.Blob -> dbContext.getDataType(FieldType.Blob, max)
        FieldType.EnumCollection, FieldType.Boolean -> dbContext.getDataType(FieldType.Boolean)
        FieldType.Enum, FieldType.Int -> dbContext.getDataType(FieldType.Int)
        FieldType.DateTime -> dbContext.getDataType(FieldType.DateTime)
        FieldType.Date -> dbContext.getDataType(FieldType.Date)
        FieldType.Short -> dbContext.getDataType(FieldType.Short)
        FieldType.Long, FieldType.Duration -> dbContext.getDataType(FieldType.Long)
        FieldType.Period, FieldType.String, FieldType.YearMonth, FieldType.MonthDay, FieldType.EnumString -> dbContext.getDataType(FieldType.String, max)
        else -> "invalid"
    }


    fun generateIndex(dbContext: DbContext, tableName: String, column: String): String {
        return dbContext.getDbCreateIndexSyntax(tableName, column, "${tableName}_ix_$column")
    }
}