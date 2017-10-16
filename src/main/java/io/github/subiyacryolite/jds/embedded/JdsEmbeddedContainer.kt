package io.github.subiyacryolite.jds.embedded

import io.github.subiyacryolite.jds.JdsEntity
import java.sql.Timestamp
import java.time.LocalDateTime

class JdsStoreBlob(val id: Long, val v: ByteArray?)
class JdsStoreBoolean(val id: Long, val v: Int?)
class JdsStoreDateTime(val id: Long, val v: Timestamp?)
class JdsStoreDouble(val id: Long, val v: Double?)
class JdsStoreInteger(val id: Long, val v: Int?)
class JdsStoreLong(val id: Long, val v: Long?)
class JdsStoreText(val id: Long, val v: String?)
class JdsStoreFloat(val id: Long, val v: Float?)
class JdsStoreTime(val id: Long, val v: Int?)
class JdsStoreMonthDay(val id: Long, val v: Int?)
class JdsStoreZonedDateTime(val id: Long, val v: Long?)
class JdsStoreLocalDate(val id: Long, val v: Timestamp?)
class JdsStoreEnum(val i: Long, val v: Int?)

class JdsStoreDateTimeArray(val i: Long, val s: Int, val v: Timestamp?)
class JdsStoreDoubleArray(val i: Long, val s: Int, val v: Double?)
class JdsStoreIntegerArray(val i: Long, val s: Int, val v: Int?)
class JdsStoreLongArray(val i: Long, val s: Int, val v: Long?)
class JdsStoreTextArray(val i: Long, val s: Int, val v: String?)
class JdsStoreFloatArray(val i: Long, val s: Int, val v: Float?)
class JdsStoreEnumArray(val i: Long, val s: Int, val v: Int?)

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