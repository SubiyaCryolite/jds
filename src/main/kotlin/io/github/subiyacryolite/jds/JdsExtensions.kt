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
package io.github.subiyacryolite.jds

import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation
import io.github.subiyacryolite.jds.enums.JdsImplementation
import javafx.beans.value.WritableValue
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


object JdsExtensions {

    private val localDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val zonedDateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS xxx")
    private val localTimeFormat = DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSSS")
    private val localTimeFormatReadOnly = DateTimeFormatter.ofPattern("[HH:mm:ss.SSSSSSS][HH:mm:ss]")

    private fun ZonedDateTime.toSqlStringFormat(): String = this.format(zonedDateTimeFormat)

    private fun LocalTime.toSqlStringFormat(): String = this.format(localTimeFormat)

    private fun LocalDate.toSqlStringFormat(): String = this.format(localDateFormat)

    fun String.toZonedDateTime(): ZonedDateTime = ZonedDateTime.parse(this, zonedDateTimeFormat)

    fun String.toLocalTime(): LocalTime = LocalTime.parse(this, localTimeFormatReadOnly)

    fun String.toLocalDate(): LocalDate = LocalDate.parse(this, localDateFormat)

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
     * @param jdsDb
     */
    fun PreparedStatement.setZonedDateTime(value: Int, input: ZonedDateTime?, jdsDb: JdsDb) = when (jdsDb.implementation) {
        JdsImplementation.TSql -> this.setString(value, input?.toSqlStringFormat())
        JdsImplementation.Postgres -> this.setObject(value, input?.toOffsetDateTime())
        JdsImplementation.MySql, JdsImplementation.Oracle, JdsImplementation.MariaDb -> this.setTimestamp(value, if (input != null) Timestamp.from(input.toInstant()) else null)
        else -> this.setObject(value, input?.toInstant()?.toEpochMilli())
    }

    /**
     * @param column
     * @param jdsDb
     * @return
     */
    fun ResultSet.getZonedDateTime(column: String, jdsDb: JdsDb): Any = when (jdsDb.implementation) {
        JdsImplementation.TSql -> this.getString(column)
        JdsImplementation.Postgres -> this.getObject(column, java.time.OffsetDateTime::class.java)
        JdsImplementation.MySql, JdsImplementation.Oracle, JdsImplementation.MariaDb -> this.getTimestamp(column)
        else -> this.getLong(column)
    }

    /**
     * @param value
     * @param input
     * @param jdsDb
     */
    fun PreparedStatement.setLocalTime(value: Int, input: LocalTime?, jdsDb: JdsDb) = when (jdsDb.implementation) {
        JdsImplementation.TSql, JdsImplementation.MySql, JdsImplementation.MariaDb -> this.setString(value, input?.toSqlStringFormat())
        JdsImplementation.Postgres -> this.setObject(value, input)
        else -> this.setObject(value, input?.toNanoOfDay())
    }

    /**
     * @param column
     * @param jdsDb
     */
    fun ResultSet.getLocalTime(column: String, jdsDb: JdsDb): Any = when (jdsDb.implementation) {
        JdsImplementation.TSql, JdsImplementation.MySql, JdsImplementation.MariaDb -> this.getString(column)
        JdsImplementation.Postgres -> this.getObject(column, LocalTime::class.java)
        else -> this.getLong(column)
    }


    /**
     * @param value
     * @param input
     * @param jdsDb
     */
    fun PreparedStatement.setLocalDate(value: Int, input: LocalDate?, jdsDb: JdsDb) = when (jdsDb.implementation) {
        JdsImplementation.TSql, JdsImplementation.MySql, JdsImplementation.MariaDb -> this.setString(value, input?.toSqlStringFormat())
        JdsImplementation.Postgres -> this.setObject(value, input)
        else -> this.setTimestamp(value, if (input != null) Timestamp.valueOf(input.atStartOfDay()) else null) //Oracle, Sqlite
    }

    /**
     * @param column
     * @param jdsDb
     */
    fun ResultSet.getLocalDate(column: String, jdsDb: JdsDb): Any = when (jdsDb.implementation) {
        JdsImplementation.TSql -> this.getString(column)
        JdsImplementation.Postgres -> this.getObject(column, LocalDate::class.java)
        else -> this.getTimestamp(column)//Oracle, Sqlite, maria,sql
    }

    /**
     * Extension method which filters the map of fields which are persisted to disk.
     * When sensitive data is not being saved, fields marked as sensitive will be excluded
     */
    @JvmName("filterTemporalValue")
    internal fun Map<Long, WritableValue<out Temporal?>>.filterSensitiveFields(jdsDb: JdsDb): Map<Long, WritableValue<out Temporal?>> = if (jdsDb.options.saveSensitiveData) {
        this
    } else {
        this.filter { !JdsField.values[it.key]!!.sensitive }//only save fields which aren't sensitive
    }

    /**
     * Extension method which filters the map of fields which are persisted to disk.
     * When sensitive data is not being saved, fields marked as sensitive will be excluded
     */
    @JvmName("filterValue")
    internal fun <T> Map<Long, WritableValue<T>>.filterSensitiveFields(jdsDb: JdsDb): Map<Long, WritableValue<T>> = if (jdsDb.options.saveSensitiveData) {
        this
    } else {
        this.filter { !JdsField.values[it.key]!!.sensitive }//only save fields which aren't sensitive
    }

    /**
     * Extension method which filters the map of fields which are persisted to disk.
     * When sensitive data is not being saved, fields marked as sensitive will be excluded
     */
    @JvmName("filterCollection")
    internal fun <T> Map<Long, MutableCollection<T>>.filterSensitiveFields(jdsDb: JdsDb): Map<Long, MutableCollection<T>> = if (jdsDb.options.saveSensitiveData) {
        this
    } else {
        this.filter { !JdsField.values[it.key]!!.sensitive }
    }

    /**
     * @param entity
     * @param parentEntities
     */
    fun determineParents(entity: Class<out IJdsEntity>, parentEntities: MutableCollection<Long>) {
        addAllToList(entity.superclass, parentEntities)
    }

    /**
     * @param superclass
     * @param parentEntities
     */
    private fun addAllToList(superclass: Class<*>?, parentEntities: MutableCollection<Long>) {
        if (superclass == null) return
        if (superclass.isAnnotationPresent(JdsEntityAnnotation::class.java)) {
            val annotation = superclass.getAnnotation(JdsEntityAnnotation::class.java)
            parentEntities.add(annotation.id)
            addAllToList(superclass.superclass, parentEntities)
        }
    }
}