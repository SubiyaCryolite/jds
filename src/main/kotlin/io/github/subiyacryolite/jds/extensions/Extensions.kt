/**
 * Jenesis Data Store Copyright (c) 2017 Ifunga Ndana. All rights reserved.
 *
 * 1. Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 2. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 3. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * Neither the name Jenesis Data Store nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package io.github.subiyacryolite.jds.extensions

import io.github.subiyacryolite.jds.Entity
import io.github.subiyacryolite.jds.Field
import io.github.subiyacryolite.jds.IEntity
import io.github.subiyacryolite.jds.beans.property.WritableProperty
import io.github.subiyacryolite.jds.context.DbContext
import io.github.subiyacryolite.jds.enums.Implementation
import java.nio.ByteBuffer
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.Temporal
import java.util.*

private fun ZonedDateTime.toSqlStringFormat(): String = this.format(Extensions.ZonedDateTimeFormat)

private fun LocalTime.toSqlStringFormat(): String = this.format(Extensions.LocalTimeFormat)

private fun LocalDate.toSqlStringFormat(): String = this.format(Extensions.LocalDateFormat)

fun String.toZonedDateTime(): ZonedDateTime = ZonedDateTime.parse(this, Extensions.ZonedDateTimeFormat)

fun String.toLocalTime(): LocalTime = LocalTime.parse(this, Extensions.LocalTimeFormatReadOnly)

fun String.toLocalDate(): LocalDate = LocalDate.parse(this, Extensions.LocalDateFormat)

fun ByteArray?.toUuid(): UUID? = if (this == null) {
    null
} else {
    val byteBuffer = ByteBuffer.wrap(this)
    UUID(byteBuffer.long, byteBuffer.long)
}

fun UUID?.toByteArray(): ByteArray? = if (this == null) {
    null
} else {
    val byteBuffer = ByteBuffer.wrap(ByteArray(16))
    byteBuffer.putLong(this.mostSignificantBits)
    byteBuffer.putLong(this.leastSignificantBits)
    byteBuffer.array()
}

/**
 * @param value
 * @param input
 * @param dbContext
 */
fun PreparedStatement.setZonedDateTime(value: Int, input: ZonedDateTime?, dbContext: DbContext) = when (dbContext.implementation) {
    Implementation.TSql -> this.setString(value, input?.toSqlStringFormat())
    Implementation.Postgres -> this.setObject(value, input?.toOffsetDateTime())
    Implementation.MySql, Implementation.Oracle, Implementation.MariaDb -> this.setTimestamp(value, if (input != null) Timestamp.from(input.toInstant()) else null)
    else -> this.setObject(value, input?.toInstant()?.toEpochMilli())
}

/**
 * @param column
 * @param dbContext
 * @return
 */
fun ResultSet.getZonedDateTime(column: String, dbContext: DbContext): Any = when (dbContext.implementation) {
    Implementation.TSql -> this.getString(column)
    Implementation.Postgres -> this.getObject(column, java.time.OffsetDateTime::class.java)
    Implementation.MySql, Implementation.Oracle, Implementation.MariaDb -> this.getTimestamp(column)
    else -> this.getLong(column)
}

/**
 * @param value
 * @param input
 * @param dbContext
 */
fun PreparedStatement.setLocalTime(value: Int, input: LocalTime?, dbContext: DbContext) = when (dbContext.implementation) {
    Implementation.TSql, Implementation.MySql, Implementation.MariaDb -> this.setString(value, input?.toSqlStringFormat())
    Implementation.Postgres -> this.setObject(value, input)
    else -> this.setObject(value, input?.toNanoOfDay())
}

/**
 * @param column
 * @param dbContext
 */
fun ResultSet.getLocalTime(column: String, dbContext: DbContext): Any = when (dbContext.implementation) {
    Implementation.TSql, Implementation.MySql, Implementation.MariaDb -> this.getString(column)
    Implementation.Postgres -> this.getObject(column, LocalTime::class.java)
    else -> this.getLong(column)
}


/**
 * @param value
 * @param input
 * @param dbContext
 */
fun PreparedStatement.setLocalDate(value: Int, input: LocalDate?, dbContext: DbContext) = when (dbContext.implementation) {
    Implementation.TSql, Implementation.MySql, Implementation.MariaDb -> this.setString(value, input?.toSqlStringFormat())
    Implementation.Postgres -> this.setObject(value, input)
    else -> this.setTimestamp(value, if (input != null) Timestamp.valueOf(input.atStartOfDay()) else null) //Oracle, Sqlite
}

/**
 * @param column
 * @param dbContext
 */
fun ResultSet.getLocalDate(column: String, dbContext: DbContext): Any = when (dbContext.implementation) {
    Implementation.TSql -> this.getString(column)
    Implementation.Postgres -> this.getObject(column, LocalDate::class.java)
    else -> this.getTimestamp(column)//Oracle, Sqlite, maria,sql
}

/**
 * Extension method which filters the map of fields which are persisted to disk.
 * When sensitive data is not being saved, fields marked as sensitive will be excluded
 */
@JvmName("filterTemporalValue")
internal fun Map<Int, WritableProperty<out Temporal?>>.filterSensitiveFields(dbContext: DbContext): Map<Int, WritableProperty<out Temporal?>> = if (dbContext.options.saveSensitiveData) {
    this
} else {
    this.filter { !Field.values[it.key]!!.sensitive }//only save fields which aren't sensitive
}

/**
 * Extension method which filters the map of fields which are persisted to disk.
 * When sensitive data is not being saved, fields marked as sensitive will be excluded
 */
@JvmName("filterValue")
internal fun <T> Map<Int, WritableProperty<T>>.filterSensitiveFields(dbContext: DbContext): Map<Int, WritableProperty<T>> = if (dbContext.options.saveSensitiveData) {
    this
} else {
    this.filter { !Field.values[it.key]!!.sensitive }//only save fields which aren't sensitive
}

/**
 * Extension method which filters the map of fields which are persisted to disk.
 * When sensitive data is not being saved, fields marked as sensitive will be excluded
 */
@JvmName("filterCollection")
internal fun <T> Map<Int, MutableCollection<T>>.filterSensitiveFields(dbContext: DbContext): Map<Int, MutableCollection<T>> = if (dbContext.options.saveSensitiveData) {
    this
} else {
    this.filter { !Field.values[it.key]!!.sensitive }
}

object Extensions {

    val LocalDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val ZonedDateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS xxx")
    val LocalTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSSS")
    val LocalTimeFormatReadOnly: DateTimeFormatter = DateTimeFormatter.ofPattern("[HH:mm:ss.SSSSSSS][HH:mm:ss]")

    /**
     * @param entity
     * @param parentEntities
     */
    fun determineParents(entity: Class<out IEntity>, parentEntities: MutableCollection<Int>) {
        addAllToList(entity.superclass, parentEntities)
    }

    /**
     * @param superclass
     * @param parentEntities
     */
    private fun addAllToList(superclass: Class<*>?, parentEntities: MutableCollection<Int>) {
        if (superclass == null) return
        val annotation = Entity.getEntityAnnotation(superclass)
        if (annotation != null) {
            parentEntities.add(annotation.id)
            addAllToList(superclass.superclass, parentEntities)
        }
    }
}