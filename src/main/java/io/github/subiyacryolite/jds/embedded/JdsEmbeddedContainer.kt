package io.github.subiyacryolite.jds.embedded

import io.github.subiyacryolite.jds.JdsEntity
import java.sql.Timestamp
import java.time.LocalDateTime

class JdsStoreBlob(val id: Long, val `val`: ByteArray?)
class JdsStoreBoolean(val id: Long, val `val`: Int?)
class JdsStoreDateTime(val id: Long, val `val`: Timestamp?)
class JdsStoreDouble(val id: Long, val `val`: Double?)
class JdsStoreInteger(val id: Long, val `val`: Int?)
class JdsStoreLong(val id: Long, val `val`: Long?)
class JdsStoreText(val id: Long, val `val`: String?)
class JdsStoreFloat(val id: Long, val `val`: Float?)
class JdsStoreTime(val id: Long, val `val`: Int?)
class JdsStoreZonedDateTime(val id: Long, val `val`: Long?)
class JdsStoreLocalDate(val id: Long, val `val`: Timestamp?)
class JdsStoreEnum(val id: Long, val `val`: Int?)//ordinal

class JdsStoreDateTimeArray(val id: Long, val index: Int, val `val`: Timestamp?)
class JdsStoreDoubleArray(val id: Long, val index: Int, val `val`: Double?)
class JdsStoreIntegerArray(val id: Long, val index: Int, val `val`: Int?)
class JdsStoreLongArray(val id: Long, val index: Int, val `val`: Long?)
class JdsStoreTextArray(val id: Long, val index: Int, val `val`: String?)
class JdsStoreFloatArray(val id: Long, val index: Int, val `val`: Float?)
class JdsStoreEnumArray(val id: Long, val index: Int, val `val`: Int?)//ordinal

class JdsStoreEntityOverview(val uuid: String, val id: Long, val l: Boolean, val dc: LocalDateTime, val dm: LocalDateTime)
class JdsStoreEntityBinding(val pid: String, val uid: String, val id: Long, val cd: Int)

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