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

    fun ZonedDateTime.toZonedDateTimeSqlFormat(): String {
        return this.format(tSqlDateTimeFormat)
    }

    fun String.toZonedDateTime(): ZonedDateTime {
        return ZonedDateTime.parse(this, tSqlDateTimeFormat)
    }

    fun LocalTime.localTimeFormat(): String {
        return this.format(localTimeFormat)
    }

    fun String.toLocalTimeSqlFormat(): LocalTime {
        return LocalTime.parse(this, localTimeFormat)
    }

    fun INamedStatement.setZonedDateTime(value: String, input: ZonedDateTime, jdsDb: JdsDb) {
        when (jdsDb.implementation) {
            JdsImplementation.TSQL -> this.setString(value, input.toZonedDateTimeSqlFormat())
            JdsImplementation.POSTGRES -> this.setObject(value, input.toOffsetDateTime())
            JdsImplementation.MYSQL, JdsImplementation.ORACLE -> this.setTimestamp(value, Timestamp.from(input.toInstant()))
            else -> this.setLong(value, input.toInstant().toEpochMilli())
        }
    }

    fun PreparedStatement.setZonedDateTime(value: Int, input: ZonedDateTime, jdsDb: JdsDb) {
        when (jdsDb.implementation) {
            JdsImplementation.TSQL -> this.setString(value, input.toZonedDateTimeSqlFormat())
            JdsImplementation.POSTGRES -> this.setObject(value, input.toOffsetDateTime())
            JdsImplementation.MYSQL, JdsImplementation.ORACLE -> this.setTimestamp(value, Timestamp.from(input.toInstant()))
            else -> this.setLong(value, input.toInstant().toEpochMilli())
        }
    }

    fun ResultSet.getZonedDateTime(column: String, jdsDb: JdsDb): Any {
        return when (jdsDb.implementation) {
            JdsImplementation.TSQL -> this.getString(column)
            JdsImplementation.POSTGRES -> this.getObject(column)
            JdsImplementation.MYSQL, JdsImplementation.ORACLE -> this.getTimestamp(column)
            else -> this.getLong(column)
        }
    }

    fun INamedStatement.setLocalTime(value: String, input: LocalTime, jdsDb: JdsDb) {
        when (jdsDb.implementation) {
            JdsImplementation.TSQL, JdsImplementation.MYSQL -> this.setString(value, input.localTimeFormat())
            JdsImplementation.POSTGRES -> this.setObject(value, input)
            else -> this.setLong(value, input.toNanoOfDay())
        }
    }

    fun PreparedStatement.setLocalTime(value: Int, input: LocalTime, jdsDb: JdsDb) {
        when (jdsDb.implementation) {
            JdsImplementation.TSQL, JdsImplementation.MYSQL -> this.setString(value, input.localTimeFormat())
            JdsImplementation.POSTGRES -> this.setObject(value, input)
            else -> this.setLong(value, input.toNanoOfDay())
        }
    }

    fun ResultSet.getTime(column: String, jdsDb: JdsDb): Any {
        return when (jdsDb.implementation) {
            JdsImplementation.TSQL, JdsImplementation.MYSQL -> this.getString(column)
            JdsImplementation.POSTGRES -> this.getObject(column)
            else -> this.getLong(column)
        }
    }

    fun determineParents(entity: Class<out IJdsEntity>, parentEntities: MutableList<Long>) {
        addAllToList(entity.superclass, parentEntities)
    }

    private fun addAllToList(superclass: Class<*>?, parentEntities: MutableList<Long>) {
        if (superclass == null) return
        if (superclass.isAnnotationPresent(JdsEntityAnnotation::class.java)) {
            val annotation = superclass.getAnnotation(JdsEntityAnnotation::class.java)
            parentEntities.add(annotation.entityId)
            addAllToList(superclass.superclass, parentEntities)
        }
    }
}