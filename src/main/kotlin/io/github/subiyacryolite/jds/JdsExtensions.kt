package io.github.subiyacryolite.jds

import com.javaworld.INamedStatement
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation
import io.github.subiyacryolite.jds.enums.JdsImplementation
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


object JdsExtensions {

    private val tSqlDateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS xxx")
    private val localTimeFormat = DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSSS")
    private val localTimeFormatReadOnly = DateTimeFormatter.ofPattern("[HH:mm:ss.SSSSSSS][HH:mm:ss]")

    /**
     * @return
     */
    fun ZonedDateTime.toZonedDateTimeSqlFormat(): String = this.format(tSqlDateTimeFormat)

    /**
     * @return
     */
    fun String.toZonedDateTime(): ZonedDateTime = ZonedDateTime.parse(this, tSqlDateTimeFormat)

    /**
     * @return
     */
    fun LocalTime.localTimeFormat(): String = this.format(localTimeFormat)

    /**
     * @return
     */
    fun String.toLocalTimeSqlFormat(): LocalTime = LocalTime.parse(this, localTimeFormatReadOnly)

    /**
     * @param value
     * @param input
     * @param jdsDb
     */
    fun INamedStatement.setZonedDateTime(value: String, input: ZonedDateTime, jdsDb: JdsDb) {
        when (jdsDb.implementation) {
            JdsImplementation.TSQL -> this.setString(value, input.toZonedDateTimeSqlFormat())
            JdsImplementation.POSTGRES -> this.setObject(value, input.toOffsetDateTime())
            JdsImplementation.MYSQL, JdsImplementation.ORACLE -> this.setTimestamp(value, Timestamp.from(input.toInstant()))
            else -> this.setLong(value, input.toInstant().toEpochMilli())
        }
    }

    /**
     * @param value
     * @param input
     * @param jdsDb
     */
    fun PreparedStatement.setZonedDateTime(value: Int, input: ZonedDateTime, jdsDb: JdsDb) {
        when (jdsDb.implementation) {
            JdsImplementation.TSQL -> this.setString(value, input.toZonedDateTimeSqlFormat())
            JdsImplementation.POSTGRES -> this.setObject(value, input.toOffsetDateTime())
            JdsImplementation.MYSQL, JdsImplementation.ORACLE -> this.setTimestamp(value, Timestamp.from(input.toInstant()))
            else -> this.setLong(value, input.toInstant().toEpochMilli())
        }
    }

    /**
     * @param column
     * @param jdsDb
     * @return
     */
    fun ResultSet.getZonedDateTime(column: String, jdsDb: JdsDb): Any = when (jdsDb.implementation) {
        JdsImplementation.TSQL -> this.getString(column)
        JdsImplementation.POSTGRES -> this.getObject(column, java.time.OffsetDateTime::class.java)
        JdsImplementation.MYSQL, JdsImplementation.ORACLE -> this.getTimestamp(column)
        else -> this.getLong(column)
    }

    /**
     * @param value
     * @param input
     * @param jdsDb
     */
    fun INamedStatement.setLocalTime(value: String, input: LocalTime, jdsDb: JdsDb) {
        when (jdsDb.implementation) {
            JdsImplementation.TSQL, JdsImplementation.MYSQL -> this.setString(value, input.localTimeFormat())
            JdsImplementation.POSTGRES -> this.setObject(value, input)
            else -> this.setLong(value, input.toNanoOfDay())
        }
    }

    /**
     * @param value
     * @param input
     * @param jdsDb
     */
    fun PreparedStatement.setLocalTime(value: Int, input: LocalTime, jdsDb: JdsDb) {
        when (jdsDb.implementation) {
            JdsImplementation.TSQL, JdsImplementation.MYSQL -> this.setString(value, input.localTimeFormat())
            JdsImplementation.POSTGRES -> this.setObject(value, input)
            else -> this.setLong(value, input.toNanoOfDay())
        }
    }

    /**
     * @param column
     * @param jdsDb
     */
    fun ResultSet.getLocalTime(column: String, jdsDb: JdsDb): Any = when (jdsDb.implementation) {
        JdsImplementation.TSQL, JdsImplementation.MYSQL -> this.getString(column)
        JdsImplementation.POSTGRES -> this.getObject(column, LocalTime::class.java)
        else -> this.getLong(column)
    }

    /**
     * @param entity
     * @param parentEntities
     */
    fun determineParents(entity: Class<out IJdsEntity>, parentEntities: MutableList<Long>) {
        addAllToList(entity.superclass, parentEntities)
    }

    /**
     * @param superclass
     * @param parentEntities
     */
    private fun addAllToList(superclass: Class<*>?, parentEntities: MutableList<Long>) {
        if (superclass == null) return
        if (superclass.isAnnotationPresent(JdsEntityAnnotation::class.java)) {
            val annotation = superclass.getAnnotation(JdsEntityAnnotation::class.java)
            parentEntities.add(annotation.entityId)
            addAllToList(superclass.superclass, parentEntities)
        }
    }
}