package io.github.subiyacryolite.jds

import com.javaworld.INamedStatement
import io.github.subiyacryolite.jds.enums.JdsImplementation
import java.sql.PreparedStatement
import java.sql.Timestamp
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


object JdsExtensions {

    private val tSqlDateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS xxx")

    fun ZonedDateTime.toTsqlOffsetDateTime(): String {
        return this.format(tSqlDateTimeFormat)
    }

    fun String.fromTsqlOffsetDateTime(): ZonedDateTime {
        return ZonedDateTime.parse(this, tSqlDateTimeFormat)
    }

    fun INamedStatement.setTimestamp(value: String, input: ZonedDateTime, jdsDb: JdsDb) {
        when (jdsDb.implementation) {
            JdsImplementation.TSQL -> this.setString(value, input.toTsqlOffsetDateTime())
            JdsImplementation.POSTGRES -> this.setObject(value, input.toOffsetDateTime())
            JdsImplementation.MYSQL, JdsImplementation.ORACLE -> this.setTimestamp(value, Timestamp.from(input.toInstant()))
            else -> this.setLong(value, input.toInstant().toEpochMilli())
        }
    }

    fun PreparedStatement.setTimestamp(value: Int, input: ZonedDateTime, jdsDb: JdsDb) {
        when (jdsDb.implementation) {
            JdsImplementation.TSQL -> this.setString(value, input.toTsqlOffsetDateTime())
            JdsImplementation.POSTGRES -> this.setObject(value, input.toOffsetDateTime())
            JdsImplementation.MYSQL, JdsImplementation.ORACLE -> this.setTimestamp(value, Timestamp.from(input.toInstant()))
            else -> this.setLong(value, input.toInstant().toEpochMilli())
        }
    }
}