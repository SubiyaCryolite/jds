package io.github.subiyacryolite.jds

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


object JdsExtensions {

    val TsqlDateTimeFormat = "YYYY-MM-DD hh:mm:ss[.nnnnnnn] [+|-]hh:mm"

    fun ZonedDateTime.toIsoOffsetDateTime(): String {
        return this.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }

    fun String.fromIsoOffsetDateTime(): ZonedDateTime {
        return ZonedDateTime.parse(this, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }
}