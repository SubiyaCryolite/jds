package io.github.subiyacryolite.jds.embedded

import io.github.subiyacryolite.jds.JdsEntity
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.MonthDay
import java.time.Period
import java.time.YearMonth

//blobs
class JdsBlobValues(val id: Long, val v: ByteArray?)
//primitives
class JdsBooleanValues(val id: Long, val v: Int?)
class JdsDoubleValues(val id: Long, val v: Double?)
class JdsIntegerValues(val id: Long, val v: Int?)
class JdsLongValues(val id: Long, val v: Long?)
class JdsTextValues(val id: Long, val v: String?)
class JdsFloatValues(val id: Long, val v: Float?)
//time constructs
class JdsLocalDateTimeValues(val id: Long, val v: Timestamp?)
class JdsTimeValues(val id: Long, val v: Int?)
class JdsZonedDateTimeValues(val id: Long, val v: Long?)
class JdsLocalDateValues(val id: Long, val v: Timestamp?)
class JdsDurationValues(val id: Long, val v: Long?)
class JdsMonthDayValues(val id: Long, val v: String?)
class JdsYearMonthValues(val id: Long, val v: String?)
class JdsPeriodValues(val id: Long, val v: String?)

//enums
class JdsEnumValues(val i: Long, val v: Int?)
//arrays
class JdsDateCollections(val i: Long, val s: Int, val v: Timestamp?)
class JdsDoubleCollections(val i: Long, val s: Int, val v: Double?)
class JdsIntegerCollections(val i: Long, val s: Int, val v: Int?)
class JdsLongCollections(val i: Long, val s: Int, val v: Long?)
class JdsTextCollections(val i: Long, val s: Int, val v: String?)
class JdsFloatCollections(val i: Long, val s: Int, val v: Float?)
class JdsEnumCollections(val i: Long, val s: Int, val v: Int?)

class JdsStoreEntityOverview(val uuid: String, val id: Long, val l: Boolean, val v: Long, val dc: LocalDateTime, val dm: LocalDateTime)
class JdsStoreEntityBinding(val p: String, val c: String, val f: Long, val i: Long)

class JdsEmbeddedContainer(sources: Collection<JdsEntity>) {
    /**
     * Embedded objects
     */
    val e: MutableList<JdsEmbeddedObject> = ArrayList()
    init {
        sources.forEach {
            e.add(JdsEmbeddedObject(it))
        }
    }
}