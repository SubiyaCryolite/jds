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

import com.fasterxml.jackson.annotation.JsonIgnore
import io.github.subiyacryolite.jds.JdsExtensions.toByteArray
import io.github.subiyacryolite.jds.JdsExtensions.toLocalDate
import io.github.subiyacryolite.jds.JdsExtensions.toLocalTime
import io.github.subiyacryolite.jds.JdsExtensions.toUuid
import io.github.subiyacryolite.jds.JdsExtensions.toZonedDateTime
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation
import io.github.subiyacryolite.jds.beans.property.*
import io.github.subiyacryolite.jds.embedded.*
import io.github.subiyacryolite.jds.enums.JdsFieldType
import io.github.subiyacryolite.jds.utility.DeepCopy
import javafx.beans.property.*
import javafx.beans.value.WritableValue
import java.io.Externalizable
import java.io.ObjectInput
import java.io.ObjectOutput
import java.io.Serializable
import java.math.BigDecimal
import java.sql.Connection
import java.sql.Timestamp
import java.time.*
import java.time.temporal.Temporal
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.collections.ArrayList

/**
 * This class allows for all mapping operations in JDS, it also uses
 * [IJdsOverview] to store overview data
 */
abstract class JdsEntity : IJdsEntity, Serializable {
    @set:JsonIgnore
    @get:JsonIgnore
    final override var overview: IJdsOverview = JdsOverview()
    //time constructs
    @get:JsonIgnore
    internal val localDateTimeValues: HashMap<Long, WritableValue<out Temporal?>> = HashMap()
    @get:JsonIgnore
    internal val zonedDateTimeValues: HashMap<Long, WritableValue<out Temporal?>> = HashMap()
    @get:JsonIgnore
    internal val localDateValues: HashMap<Long, WritableValue<out Temporal?>> = HashMap()
    @get:JsonIgnore
    internal val localTimeValues: HashMap<Long, WritableValue<out Temporal?>> = HashMap()
    @get:JsonIgnore
    internal val monthDayValues: HashMap<Long, WritableValue<MonthDay?>> = HashMap()
    @get:JsonIgnore
    internal val yearMonthValues: HashMap<Long, WritableValue<out Temporal?>> = HashMap()
    @get:JsonIgnore
    internal val periodValues: HashMap<Long, WritableValue<Period?>> = HashMap()
    @get:JsonIgnore
    internal val durationValues: HashMap<Long, WritableValue<Duration?>> = HashMap()
    //strings
    @get:JsonIgnore
    internal val stringValues: HashMap<Long, WritableValue<String?>> = HashMap()
    //boolean
    @get:JsonIgnore
    internal val booleanValues: HashMap<Long, WritableValue<Boolean?>> = HashMap()
    //numeric
    @get:JsonIgnore
    internal val shortValues: HashMap<Long, WritableValue<Short?>> = HashMap()
    @get:JsonIgnore
    internal val floatValues: HashMap<Long, WritableValue<Float?>> = HashMap()
    @get:JsonIgnore
    internal val doubleValues: HashMap<Long, WritableValue<Double?>> = HashMap()
    @get:JsonIgnore
    internal val longValues: HashMap<Long, WritableValue<Long?>> = HashMap()
    @get:JsonIgnore
    internal val integerValues: HashMap<Long, WritableValue<Int?>> = HashMap()
    @get:JsonIgnore
    internal val uuidValues: HashMap<Long, WritableValue<UUID?>> = HashMap()
    //arrays
    @get:JsonIgnore
    override val objectCollections: HashMap<JdsFieldEntity<*>, MutableCollection<IJdsEntity>> = HashMap()
    @get:JsonIgnore
    internal val stringCollections: HashMap<Long, MutableCollection<String>> = HashMap()
    @get:JsonIgnore
    internal val dateTimeCollections: HashMap<Long, MutableCollection<LocalDateTime>> = HashMap()
    @get:JsonIgnore
    internal val floatCollections: HashMap<Long, MutableCollection<Float>> = HashMap()
    @get:JsonIgnore
    internal val doubleCollections: HashMap<Long, MutableCollection<Double>> = HashMap()
    @get:JsonIgnore
    internal val longCollections: HashMap<Long, MutableCollection<Long>> = HashMap()
    @get:JsonIgnore
    internal val integerCollections: HashMap<Long, MutableCollection<Int>> = HashMap()
    //enumValues - enums can be null, enum collections cannot (skip unknown entries)
    @get:JsonIgnore
    internal val enumValues: HashMap<Long, WritableValue<Enum<*>?>> = HashMap()
    @get:JsonIgnore
    internal val stringEnumValues: HashMap<Long, WritableValue<Enum<*>?>> = HashMap()
    @get:JsonIgnore
    internal val enumCollections: HashMap<Long, MutableCollection<Enum<*>>> = HashMap()
    @get:JsonIgnore
    internal val enumStringCollections: HashMap<Long, MutableCollection<Enum<*>>> = HashMap()
    //objects
    @get:JsonIgnore
    override val objectValues: HashMap<JdsFieldEntity<*>, WritableValue<out IJdsEntity>> = HashMap()
    //blobs
    @get:JsonIgnore
    internal val blobValues: HashMap<Long, WritableValue<ByteArray?>> = HashMap()

    init {
        val classHasAnnotation = javaClass.isAnnotationPresent(JdsEntityAnnotation::class.java)
        val superclassHasAnnotation = javaClass.superclass.isAnnotationPresent(JdsEntityAnnotation::class.java)
        if (classHasAnnotation || superclassHasAnnotation) {
            val entityAnnotation = when (classHasAnnotation) {
                true -> javaClass.getAnnotation(JdsEntityAnnotation::class.java)
                false -> javaClass.superclass.getAnnotation(JdsEntityAnnotation::class.java)
            }
            overview.entityId = entityAnnotation.id
        } else {
            throw RuntimeException("You must annotate the class [" + javaClass.canonicalName + "] or its parent with [" + JdsEntityAnnotation::class.java + "]")
        }
    }

    @JvmName("mapShort")
    protected fun map(field: JdsField, value: Short?) = map(field, SimpleObjectProperty(value))

    @JvmName("mapShort")
    protected fun map(field: JdsField, property: WritableValue<Short?>): WritableValue<Short?> {
        if (field.type != JdsFieldType.Short) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        shortValues[mapField(overview.entityId, field.bind())] = property
        return property
    }

    @JvmName("mapDouble")
    protected fun map(field: JdsField, value: Double?) = map(field, SimpleObjectProperty(value))

    @JvmName("mapDouble")
    protected fun map(field: JdsField, value: DoubleProperty) = map(field, value as WritableValue<Double?>)

    @JvmName("mapDouble")
    protected fun map(field: JdsField, property: WritableValue<Double?>): WritableValue<Double?> {
        if (field.type != JdsFieldType.Double) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        doubleValues[mapField(overview.entityId, field.bind())] = property
        return property
    }

    @JvmName("mapInt")
    protected fun map(field: JdsField, value: Int?) = map(field, SimpleObjectProperty(value))

    @JvmName("mapInt")
    protected fun map(field: JdsField, value: IntegerProperty) = map(field, value as WritableValue<Int?>)

    @JvmName("mapInt")
    protected fun map(field: JdsField, property: WritableValue<Int?>): WritableValue<Int?> {
        if (field.type != JdsFieldType.Int) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        integerValues[mapField(overview.entityId, field.bind())] = property
        return property
    }

    @JvmName("mapLong")
    protected fun map(field: JdsField, value: Long?) = map(field, SimpleObjectProperty(value))

    @JvmName("mapLong")
    protected fun map(field: JdsField, value: LongProperty) = map(field, value as WritableValue<Long?>)

    @JvmName("mapLong")
    protected fun map(field: JdsField, property: WritableValue<Long?>): WritableValue<Long?> {
        if (field.type != JdsFieldType.Long) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        longValues[mapField(overview.entityId, field.bind())] = property
        return property
    }

    @JvmName("mapFloat")
    protected fun map(field: JdsField, value: Float?) = map(field, SimpleObjectProperty(value))

    @JvmName("mapFloat")
    protected fun map(field: JdsField, value: FloatProperty) = map(field, value as WritableValue<Float?>)

    @JvmName("mapFloat")
    protected fun map(field: JdsField, property: WritableValue<Float?>): WritableValue<Float?> {
        if (field.type != JdsFieldType.Float) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        floatValues[mapField(overview.entityId, field.bind())] = property
        return property
    }

    @JvmName("mapBoolean")
    protected fun map(field: JdsField, value: Boolean?) = map(field, SimpleObjectProperty(value))

    @JvmName("mapBoolean")
    protected fun map(field: JdsField, property: WritableValue<Boolean?>): WritableValue<Boolean?> {
        if (field.type != JdsFieldType.Boolean) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        booleanValues[mapField(overview.entityId, field.bind())] = property
        return property
    }

    @JvmName("mapUuid")
    protected fun map(field: JdsField, value: UUID?) = map(field, SimpleObjectProperty(value))

    @JvmName("mapUuid")
    protected fun map(field: JdsField, property: WritableValue<UUID?>): WritableValue<UUID?> {
        if (field.type != JdsFieldType.Uuid) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        uuidValues[mapField(overview.entityId, field.bind())] = property
        return property
    }

    @JvmName("mapString")
    protected fun map(field: JdsField, value: String?) = map(field, SimpleStringProperty(value))

    @JvmName("mapString")
    protected fun map(field: JdsField, property: WritableValue<String?>): WritableValue<String?> {
        if (field.type != JdsFieldType.String) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        stringValues[mapField(overview.entityId, field.bind())] = property
        return property
    }

    @JvmName("mapDateTime")
    protected fun map(field: JdsField, value: LocalDateTime?) = map(field, SimpleObjectProperty(value))

    @JvmName("mapDateTime")
    protected fun map(field: JdsField, property: WritableValue<LocalDateTime?>): WritableValue<LocalDateTime?> {
        if (field.type != JdsFieldType.DateTime) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        localDateTimeValues[mapField(overview.entityId, field.bind())] = property
        return property
    }

    @JvmName("mapZonedDateTime")
    protected fun map(field: JdsField, value: ZonedDateTime?) = map(field, SimpleObjectProperty(value))

    @JvmName("mapZonedDateTime")
    protected fun map(field: JdsField, property: WritableValue<ZonedDateTime?>): WritableValue<ZonedDateTime?> {
        if (field.type != JdsFieldType.ZonedDateTime) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        zonedDateTimeValues[mapField(overview.entityId, field.bind())] = property
        return property
    }

    @JvmName("mapDate")
    protected fun map(field: JdsField, value: LocalDate?) = map(field, SimpleObjectProperty(value))

    @JvmName("mapDate")
    protected fun map(field: JdsField, property: WritableValue<LocalDate?>): WritableValue<LocalDate?> {
        if (field.type != JdsFieldType.Date) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        localDateValues[mapField(overview.entityId, field.bind())] = property
        return property
    }

    @JvmName("mapTime")
    protected fun map(field: JdsField, value: LocalTime?) = map(field, SimpleObjectProperty(value))

    @JvmName("mapTime")
    protected fun map(field: JdsField, property: WritableValue<LocalTime?>): WritableValue<LocalTime?> {
        if (field.type != JdsFieldType.Time) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        localTimeValues[mapField(overview.entityId, field.bind())] = property
        return property
    }

    @JvmName("mapBlob")
    protected fun map(field: JdsField, value: ByteArray?) = map(field, SimpleObjectProperty(value))

    @JvmName("mapBlob")
    protected fun map(field: JdsField, property: WritableValue<ByteArray?>): WritableValue<ByteArray?> {
        if (field.type != JdsFieldType.Blob) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        blobValues[mapField(overview.entityId, field.bind())] = property
        return property
    }

    @JvmName("mapMonthDay")
    protected fun map(field: JdsField, value: MonthDay?) = map(field, SimpleObjectProperty(value))

    @JvmName("mapMonthDay")
    protected fun map(field: JdsField, property: WritableValue<MonthDay?>): WritableValue<MonthDay?> {
        if (field.type != JdsFieldType.MonthDay) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        monthDayValues[mapField(overview.entityId, field.bind())] = property
        return property
    }

    @JvmName("mapYearMonth")
    protected fun map(field: JdsField, value: YearMonth?) = map(field, SimpleObjectProperty(value))

    @JvmName("mapYearMonth")
    protected fun map(field: JdsField, property: WritableValue<YearMonth?>): WritableValue<YearMonth?> {
        if (field.type != JdsFieldType.YearMonth) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        yearMonthValues[mapField(overview.entityId, field.bind())] = property
        return property
    }

    @JvmName("mapPeriod")
    protected fun map(field: JdsField, value: Period?) = map(field, SimpleObjectProperty(value))

    @JvmName("mapPeriod")
    protected fun map(field: JdsField, property: WritableValue<Period?>): WritableValue<Period?> {
        if (field.type != JdsFieldType.Period) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        periodValues[mapField(overview.entityId, field.bind())] = property
        return property
    }

    @JvmName("mapDuration")
    protected fun map(field: JdsField, value: Duration?) = map(field, SimpleObjectProperty(value))

    @JvmName("mapDuration")
    protected fun map(field: JdsField, property: WritableValue<Duration?>): WritableValue<Duration?> {
        if (field.type != JdsFieldType.Duration) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        durationValues[mapField(overview.entityId, field.bind())] = property
        return property
    }

    @JvmName("mapEnum")
    protected fun <T : Enum<T>> map(fieldEnum: JdsFieldEnum<T>, value: T?) = map(fieldEnum, SimpleObjectProperty(value))

    @JvmName("mapEnum")
    protected fun <T : Enum<T>> map(fieldEnum: JdsFieldEnum<T>, property: WritableValue<T?>): WritableValue<T?> {
        if (fieldEnum.field.type != JdsFieldType.Enum && fieldEnum.field.type != JdsFieldType.EnumString) {
            throw RuntimeException("Incorrect type supplied for field [$fieldEnum.field]")
        }
        if (fieldEnum.field.type == JdsFieldType.Enum) {
            enumValues[fieldEnum.field.id] = property as WritableValue<Enum<*>?>
        } else {
            stringEnumValues[fieldEnum.field.id] = property as WritableValue<Enum<*>?>
        }
        fieldEnum.field.bind()
        mapField(overview.entityId, fieldEnum.field.id)
        mapEnums(overview.entityId, fieldEnum.field.id)
        return property
    }

    @JvmName("mapStrings")
    protected fun map(field: JdsField, collection: MutableCollection<String>): MutableCollection<String> {
        if (field.type != JdsFieldType.StringCollection) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        stringCollections[mapField(overview.entityId, field.bind())] = collection
        return collection
    }

    @JvmName("mapDateTimes")
    protected fun map(field: JdsField, collection: MutableCollection<LocalDateTime>): MutableCollection<LocalDateTime> {
        if (field.type != JdsFieldType.DateTimeCollection) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        dateTimeCollections[mapField(overview.entityId, field.bind())] = collection
        return collection
    }

    @JvmName("mapFloats")
    protected fun map(field: JdsField, collection: MutableCollection<Float>): MutableCollection<Float> {
        if (field.type != JdsFieldType.FloatCollection) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        floatCollections[mapField(overview.entityId, field.bind())] = collection
        return collection
    }

    @JvmName("mapIntegers")
    protected fun map(field: JdsField, collection: MutableCollection<Int>): MutableCollection<Int> {
        if (field.type != JdsFieldType.IntCollection) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        integerCollections[mapField(overview.entityId, field.bind())] = collection
        return collection
    }

    @JvmName("mapDoubles")
    protected fun map(field: JdsField, collection: MutableCollection<Double>): MutableCollection<Double> {
        if (field.type != JdsFieldType.DoubleCollection) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        doubleCollections[mapField(overview.entityId, field.bind())] = collection
        return collection
    }

    @JvmName("mapLongs")
    protected fun map(field: JdsField, collection: MutableCollection<Long>): MutableCollection<Long> {
        if (field.type != JdsFieldType.LongCollection) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        longCollections[mapField(overview.entityId, field.bind())] = collection
        return collection
    }

    @JvmName("mapEnums")
    protected fun <T : Enum<T>> map(fieldEnum: JdsFieldEnum<T>, collection: MutableCollection<T>): MutableCollection<T> {
        if (fieldEnum.field.type != JdsFieldType.EnumCollection && fieldEnum.field.type != JdsFieldType.EnumStringCollection) {
            throw RuntimeException("Incorrect type supplied for field [$fieldEnum.field]")
        }
        if (fieldEnum.field.type == JdsFieldType.EnumCollection) {
            enumCollections[fieldEnum.field.id] = collection as MutableCollection<Enum<*>>
        } else {
            enumStringCollections[fieldEnum.field.id] = collection as MutableCollection<Enum<*>>
        }
        fieldEnum.field.bind()
        mapField(overview.entityId, fieldEnum.field.id)
        mapEnums(overview.entityId, fieldEnum.field.id)
        return collection
    }

    protected fun <T : IJdsEntity> map(fieldEntity: JdsFieldEntity<T>, entity: T): WritableValue<T> {
        return map(fieldEntity, SimpleObjectProperty(entity))
    }

    protected fun <T : IJdsEntity> map(fieldEntity: JdsFieldEntity<T>, property: WritableValue<T>): WritableValue<T> {
        if (fieldEntity.field.type != JdsFieldType.Entity) {
            throw RuntimeException("Please assign the correct type to field [$fieldEntity]")
        }
        if (!objectCollections.containsKey(fieldEntity) && !objectValues.containsKey(fieldEntity)) {
            fieldEntity.field.bind()
            objectValues[fieldEntity] = property
            mapField(overview.entityId, fieldEntity.field.id)
        } else {
            throw RuntimeException("You can only bind a class to one property. This class is already bound to one object or object array")
        }
        return property
    }

    /**
     * @param fieldEntity
     * @param collection
     */
    protected fun <T : IJdsEntity> map(fieldEntity: JdsFieldEntity<T>, collection: MutableCollection<T>): MutableCollection<T> {
        if (fieldEntity.field.type != JdsFieldType.EntityCollection) {
            throw RuntimeException("Please supply a valid type for JdsFieldEntity")
        }
        if (!objectCollections.containsKey(fieldEntity)) {
            fieldEntity.field.bind()
            objectCollections[fieldEntity] = collection as MutableCollection<IJdsEntity>
            mapField(overview.entityId, fieldEntity.field.id)
        } else {
            throw RuntimeException("You can only bind a class to one property. This class is already bound to one object or object array")
        }
        return collection
    }

    /**
     * Copy values from matching fieldIds found in both objects
     *
     * @param source The entity to copy values from
     * @param <T>    A valid JDSEntity
    </T> */
    fun <T : JdsEntity> copy(source: T) {
        this.overview.uuid = source.overview.uuid
        this.overview.editVersion = source.overview.editVersion
        this.overview.entityId = source.overview.entityId

        localDateTimeValues.clear()
        localDateTimeValues.putAll(DeepCopy.clone(source.localDateTimeValues)!!)

        zonedDateTimeValues.clear()
        zonedDateTimeValues.putAll(DeepCopy.clone(source.zonedDateTimeValues)!!)

        localDateValues.clear()
        localDateValues.putAll(DeepCopy.clone(source.localDateValues)!!)

        localTimeValues.clear()
        localTimeValues.putAll(DeepCopy.clone(source.localTimeValues)!!)

        monthDayValues.clear()
        monthDayValues.putAll(DeepCopy.clone(source.monthDayValues)!!)

        yearMonthValues.clear()
        yearMonthValues.putAll(DeepCopy.clone(source.yearMonthValues)!!)

        periodValues.clear()
        periodValues.putAll(DeepCopy.clone(source.periodValues)!!)

        durationValues.clear()
        durationValues.putAll(DeepCopy.clone(source.durationValues)!!)

        stringValues.clear()
        stringValues.putAll(DeepCopy.clone(source.stringValues)!!)

        booleanValues.clear()
        booleanValues.putAll(DeepCopy.clone(source.booleanValues)!!)

        floatValues.clear()
        floatValues.putAll(DeepCopy.clone(source.floatValues)!!)

        doubleValues.clear()
        doubleValues.putAll(DeepCopy.clone(source.doubleValues)!!)

        shortValues.clear()
        shortValues.putAll(DeepCopy.clone(source.shortValues)!!)

        longValues.clear()
        longValues.putAll(DeepCopy.clone(source.longValues)!!)

        integerValues.clear()
        integerValues.putAll(DeepCopy.clone(source.integerValues)!!)

        uuidValues.clear()
        uuidValues.putAll(DeepCopy.clone(source.uuidValues)!!)

        objectCollections.clear()
        objectCollections.putAll(DeepCopy.clone(source.objectCollections)!!)

        stringCollections.clear()
        stringCollections.putAll(DeepCopy.clone(source.stringCollections)!!)

        dateTimeCollections.clear()
        dateTimeCollections.putAll(DeepCopy.clone(source.dateTimeCollections)!!)

        floatCollections.clear()
        floatCollections.putAll(DeepCopy.clone(source.floatCollections)!!)

        doubleCollections.clear()
        doubleCollections.putAll(DeepCopy.clone(source.doubleCollections)!!)

        longCollections.clear()
        longCollections.putAll(DeepCopy.clone(source.longCollections)!!)

        integerCollections.clear()
        integerCollections.putAll(DeepCopy.clone(source.integerCollections)!!)

        enumValues.clear()
        enumValues.putAll(DeepCopy.clone(source.enumValues)!!)

        stringEnumValues.clear()
        stringEnumValues.putAll(DeepCopy.clone(source.stringEnumValues)!!)

        enumCollections.clear()
        enumCollections.putAll(DeepCopy.clone(source.enumCollections)!!)

        enumStringCollections.clear()
        enumStringCollections.putAll(DeepCopy.clone(source.enumStringCollections)!!)

        objectValues.clear()
        objectValues.putAll(DeepCopy.clone(source.objectValues)!!)

        blobValues.clear()
        blobValues.putAll(DeepCopy.clone(source.blobValues)!!)
    }

    /**
     * Implementation ignores null values by default on the assumption that nullable values have default values of null
     * @param jdsEmbeddedObject
     */
    @Throws(Exception::class)
    override fun assign(jdsEmbeddedObject: JdsEmbeddedObject) {
        //==============================================
        //PRIMITIVES, also saved to array struct to streamline json
        //==============================================
        booleanValues.entries.forEach {
            val input = when (it.value.value) {
                true -> 1
                false -> 0
                else -> null
            }
            jdsEmbeddedObject.booleanValues.add(JdsStoreBoolean(it.key, input))
        }
        stringValues.entries.forEach { jdsEmbeddedObject.stringValues.add(JdsStoreString(it.key, it.value.value)) }
        floatValues.entries.forEach { jdsEmbeddedObject.floatValue.add(JdsStoreFloat(it.key, it.value.value)) }
        doubleValues.entries.forEach { jdsEmbeddedObject.doubleValues.add(JdsStoreDouble(it.key, it.value.value)) }
        shortValues.entries.forEach { jdsEmbeddedObject.shortValues.add(JdsStoreShort(it.key, it.value.value)) }
        longValues.entries.forEach { jdsEmbeddedObject.longValues.add(JdsStoreLong(it.key, it.value.value)) }
        integerValues.entries.forEach { jdsEmbeddedObject.integerValues.add(JdsStoreInteger(it.key, it.value.value)) }
        uuidValues.entries.forEach { jdsEmbeddedObject.uuidValues.add(JdsStoreUuid(it.key, it.value.value.toByteArray())) }
        //==============================================
        //Dates & Time
        //==============================================
        zonedDateTimeValues.entries.forEach { jdsEmbeddedObject.zonedDateTimeValues.add(JdsStoreZonedDateTime(it.key, (it.value.value as ZonedDateTime?)?.toInstant()?.toEpochMilli())) }
        localTimeValues.entries.forEach { jdsEmbeddedObject.timeValues.add(JdsStoreTime(it.key, (it.value.value as LocalTime?)?.toNanoOfDay())) }
        durationValues.entries.forEach { jdsEmbeddedObject.durationValues.add(JdsStoreDuration(it.key, it.value.value?.toNanos())) }
        localDateTimeValues.entries.forEach { jdsEmbeddedObject.dateTimeValues.add(JdsStoreDateTime(it.key, safeLocalDateTime(it.value.value))) }
        localDateValues.entries.forEach { jdsEmbeddedObject.dateValues.add(JdsStoreDate(it.key, safeLocalDate(it.value.value))) }
        monthDayValues.entries.forEach { jdsEmbeddedObject.monthDayValues.add(JdsStoreMonthDay(it.key, it.value.value?.toString())) }
        yearMonthValues.entries.forEach { jdsEmbeddedObject.yearMonthValues.add(JdsStoreYearMonth(it.key, (it.value.value as YearMonth?)?.toString())) }
        periodValues.entries.forEach { jdsEmbeddedObject.periodValues.add(JdsStorePeriod(it.key, it.value.value?.toString())) }
        //==============================================
        //BLOB
        //==============================================
        blobValues.entries.forEach {
            jdsEmbeddedObject.blobValues.add(JdsStoreBlob(it.key, it.value.value ?: ByteArray(0)))
        }
        //==============================================
        //Enums
        //==============================================
        enumValues.entries.forEach { jdsEmbeddedObject.enumValues.add(JdsStoreEnum(it.key, it.value.value?.ordinal)) }
        stringEnumValues.entries.forEach { jdsEmbeddedObject.enumStringValues.add(JdsStoreEnumString(it.key, it.value.value?.name)) }
        enumCollections.entries.forEach { jdsEmbeddedObject.enumCollections.add(JdsStoreEnumCollection(it.key, toIntCollection(it.value))) }
        enumStringCollections.entries.forEach { jdsEmbeddedObject.enumStringCollections.add(JdsStoreEnumStringCollection(it.key, toStringCollection(it.value))) }
        //==============================================
        //ARRAYS
        //==============================================
        stringCollections.entries.forEach { jdsEmbeddedObject.stringCollections.add(JdsStoreStringCollection(it.key, it.value)) }
        dateTimeCollections.entries.forEach { jdsEmbeddedObject.dateTimeCollection.add(JdsStoreDateTimeCollection(it.key, toTimeStampCollection(it.value))) }
        floatCollections.entries.forEach { jdsEmbeddedObject.floatCollections.add(JdsStoreFloatCollection(it.key, it.value)) }
        doubleCollections.entries.forEach { jdsEmbeddedObject.doubleCollections.add(JdsStoreDoubleCollection(it.key, it.value)) }
        longCollections.entries.forEach { jdsEmbeddedObject.longCollections.add(JdsStoreLongCollection(it.key, it.value)) }
        integerCollections.entries.forEach { jdsEmbeddedObject.integerCollections.add(JdsStoreIntegerCollection(it.key, it.value)) }
        //==============================================
        //EMBEDDED OBJECTS
        //==============================================
        objectCollections.forEach { (fieldEntity, mutableCollection) ->
            mutableCollection.forEach { iJdsEntity ->
                val embeddedObject = JdsEmbeddedObject()
                embeddedObject.fieldId = fieldEntity.field.id
                embeddedObject.init(iJdsEntity)
                jdsEmbeddedObject.entityOverviews.add(embeddedObject)
            }
        }
        objectValues.forEach { (fieldEntity, objectProperty) ->
            val embeddedObject = JdsEmbeddedObject()
            embeddedObject.fieldId = fieldEntity.field.id
            embeddedObject.init(objectProperty.value)
            jdsEmbeddedObject.entityOverviews.add(embeddedObject)
        }
    }

    private fun safeLocalDateTime(value: Temporal?): Timestamp? {
        val localDateTime = value as LocalDateTime?
        if (localDateTime != null) {
            return Timestamp.valueOf(localDateTime)
        }
        return null
    }

    private fun safeLocalDate(value: Temporal?): Timestamp? {
        val localDate = value as LocalDate?
        if (localDate != null) {
            return Timestamp.valueOf(localDate.atStartOfDay())
        }
        return null
    }

    /**
     * @param fieldType
     * @param fieldId
     * @param value
     */
    internal fun populateProperties(fieldType: JdsFieldType, fieldId: Long, value: Any?) {
        //what happens when you supply an unknown field ID? Create an empty container and throw it in regardless, this way compatibility isnt broken
        initBackingPropertyIfNotDefined(fieldType, fieldId)
        when (fieldType) {

            JdsFieldType.Float -> floatValues[fieldId]?.value = when (value) {
                is Double -> value.toFloat()
                else -> value as Float?
            }

            JdsFieldType.Double -> doubleValues[fieldId]?.value = value as Double?

            JdsFieldType.Short -> shortValues[fieldId]?.value = when (value) {
                is Double -> value.toShort()
                is Int -> value.toShort()
                is Short? -> value
                else -> null
            }

            JdsFieldType.Long -> longValues[fieldId]?.value = when (value) {
                is Long? -> value
                is BigDecimal -> value.toLong() //Oracle
                is Int -> value.toLong()
                else -> null
            }

            JdsFieldType.Int -> integerValues[fieldId]?.value = when (value) {
                is Int? -> value
                is BigDecimal -> value.toInt() //Oracle
                else -> null
            }

            JdsFieldType.Uuid -> uuidValues[fieldId]?.value = when (value) {
                is ByteArray? -> value.toUuid()
                is String -> UUID.fromString(value)
                is UUID? -> value
                else -> null
            }

            JdsFieldType.Boolean -> booleanValues[fieldId]?.value = when (value) {
                is Int -> value == 1
                is Boolean? -> value
                is BigDecimal -> value.intValueExact() == 1 //Oracle
                else -> null
            }

            JdsFieldType.DoubleCollection -> doubleCollections[fieldId]?.add(value as Double)

            JdsFieldType.FloatCollection -> floatCollections[fieldId]?.add(value as Float)

            JdsFieldType.LongCollection -> longCollections[fieldId]?.add(value as Long)

            JdsFieldType.IntCollection -> integerCollections[fieldId]?.add(value as Int)

            //Enums may be null
            JdsFieldType.Enum -> enumValues.filter { it.key == fieldId }.forEach {
                val fieldEnum = JdsFieldEnum.enums[it.key]
                if (fieldEnum != null) {
                    if (value != null) {
                        it.value.value = (when (value) {
                            is BigDecimal -> fieldEnum.valueOf(value.intValueExact())
                            else -> fieldEnum.valueOf(value as Int)
                        })
                    }
                }
            }

            //Enums may be null
            JdsFieldType.EnumString -> enumValues.filter { it.key == fieldId }.forEach {
                val fieldEnum = JdsFieldEnum.enums[it.key]
                if (fieldEnum != null && value is String)
                    it.value.value = fieldEnum.valueOf(value)
            }

            //Enum collections should NOT accept nulls. Unknown collection should be skipped
            JdsFieldType.EnumCollection -> enumCollections.filter { it.key == fieldId }.forEach {
                val fieldEnum = JdsFieldEnum.enums[it.key]
                if (fieldEnum != null) {
                    val enumValues = fieldEnum.values
                    val ordinal = when (value) {
                        is Int -> value
                        is BigDecimal -> value.intValueExact()
                        else -> enumValues.size
                    }
                    if (ordinal < enumValues.size) {
                        it.value.add(enumValues.find { enumValue -> enumValue.ordinal == ordinal }!!)
                    }
                }
            }

            //Enum collections should NOT accept nulls. Unknown collection should be skipped
            JdsFieldType.EnumStringCollection -> enumStringCollections.filter { it.key == fieldId }.forEach {
                val fieldEnum = JdsFieldEnum.enums[it.key]
                if (fieldEnum != null) {
                    val enumStr = value.toString()
                    val enumVal = fieldEnum.valueOf(enumStr)
                    if (enumVal != null)
                        it.value.add(enumVal)
                }
            }

            JdsFieldType.String -> stringValues[fieldId]?.value = (value as String?)

            JdsFieldType.StringCollection -> stringCollections[fieldId]?.add(value as String)

            JdsFieldType.ZonedDateTime -> when (value) {
                is Long -> zonedDateTimeValues[fieldId]?.value = ZonedDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault())
                is Timestamp -> zonedDateTimeValues[fieldId]?.value = ZonedDateTime.ofInstant(value.toInstant(), ZoneOffset.systemDefault())
                is String -> zonedDateTimeValues[fieldId]?.value = value.toZonedDateTime()
                is OffsetDateTime -> zonedDateTimeValues[fieldId]?.value = value.atZoneSameInstant(ZoneId.systemDefault())
            }

            JdsFieldType.Date -> when (value) {
                is Timestamp -> localDateValues[fieldId]?.value = (value.toLocalDateTime().toLocalDate())
                is LocalDate -> localDateValues[fieldId]?.value = (value)
                is String -> localDateValues[fieldId]?.value = (value.toLocalDate())
                else -> localDateValues[fieldId]?.value = null
            }

            JdsFieldType.Time -> when (value) {
                is Long -> localTimeValues[fieldId]?.value = (LocalTime.MIN.plusNanos(value))
                is LocalTime -> localTimeValues[fieldId]?.value = (value)
                is String -> localTimeValues[fieldId]?.value = (value.toLocalTime())
            }

            JdsFieldType.Duration -> durationValues[fieldId]?.value = (when (value) {
                is BigDecimal -> Duration.ofNanos(value.longValueExact())//Oracle
                else -> Duration.ofNanos(value as Long)
            })

            JdsFieldType.MonthDay -> monthDayValues[fieldId]?.value = if (value is String) MonthDay.parse(value) else null

            JdsFieldType.YearMonth -> yearMonthValues[fieldId]?.value = if (value is String) YearMonth.parse(value) else null

            JdsFieldType.Period -> periodValues[fieldId]?.value = if (value is String) Period.parse(value) else null

            JdsFieldType.DateTime -> localDateTimeValues[fieldId]?.value = if (value is Timestamp) value.toLocalDateTime() else null

            JdsFieldType.DateTimeCollection -> dateTimeCollections[fieldId]?.add((value as Timestamp).toLocalDateTime())

            JdsFieldType.Blob -> when (value) {
                is ByteArray -> blobValues[fieldId]?.value = value
                null -> blobValues[fieldId]?.value = ByteArray(0)//Oracle
            }

            else -> {
            }
        }
    }

    /**
     * This method enforces forward compatibility by ensuring that every property is present even if the field is not defined or known locally
     */
    private fun initBackingPropertyIfNotDefined(fieldType: JdsFieldType, fieldId: Long) {
        when (fieldType) {

            JdsFieldType.String -> if (!stringValues.containsKey(fieldId))
                stringValues[fieldId] = SimpleStringProperty("")

            JdsFieldType.DoubleCollection -> if (!doubleCollections.containsKey(fieldId))
                doubleCollections[fieldId] = ArrayList()

            JdsFieldType.FloatCollection -> if (!floatCollections.containsKey(fieldId))
                floatCollections[fieldId] = ArrayList()

            JdsFieldType.LongCollection -> if (!longCollections.containsKey(fieldId))
                longCollections[fieldId] = ArrayList()

            JdsFieldType.IntCollection -> if (!integerCollections.containsKey(fieldId))
                integerCollections[fieldId] = ArrayList()

            JdsFieldType.StringCollection -> if (!stringCollections.containsKey(fieldId))
                stringCollections[fieldId] = ArrayList()

            JdsFieldType.DateTimeCollection -> if (!dateTimeCollections.containsKey(fieldId))
                dateTimeCollections[fieldId] = ArrayList()

            JdsFieldType.EnumCollection -> if (!enumCollections.containsKey(fieldId))
                enumCollections[fieldId] = ArrayList()

            JdsFieldType.ZonedDateTime -> if (!zonedDateTimeValues.containsKey(fieldId))
                zonedDateTimeValues[fieldId] = SimpleObjectProperty<Temporal?>()

            JdsFieldType.Date -> if (!localDateValues.containsKey(fieldId))
                localDateValues[fieldId] = SimpleObjectProperty<Temporal?>()

            JdsFieldType.Time -> if (!localTimeValues.containsKey(fieldId))
                localTimeValues[fieldId] = SimpleObjectProperty<Temporal?>()

            JdsFieldType.Duration -> if (!durationValues.containsKey(fieldId))
                durationValues[fieldId] = SimpleObjectProperty<Duration?>()

            JdsFieldType.MonthDay -> if (!monthDayValues.containsKey(fieldId))
                monthDayValues[fieldId] = SimpleObjectProperty<MonthDay?>()

            JdsFieldType.YearMonth -> if (!yearMonthValues.containsKey(fieldId))
                yearMonthValues[fieldId] = SimpleObjectProperty<Temporal?>()

            JdsFieldType.Period -> if (!periodValues.containsKey(fieldId))
                periodValues[fieldId] = SimpleObjectProperty<Period?>()

            JdsFieldType.DateTime -> if (!localDateTimeValues.containsKey(fieldId))
                localDateTimeValues[fieldId] = SimpleObjectProperty<Temporal>()

            JdsFieldType.Blob -> if (!blobValues.containsKey(fieldId))
                blobValues[fieldId] = SimpleBlobProperty(byteArrayOf())

            JdsFieldType.Enum -> if (!enumValues.containsKey(fieldId))
                enumValues[fieldId] = SimpleObjectProperty()

            JdsFieldType.Float -> if (!floatValues.containsKey(fieldId))
                floatValues[fieldId] = NullableFloatProperty()

            JdsFieldType.Double -> if (!doubleValues.containsKey(fieldId))
                doubleValues[fieldId] = NullableDoubleProperty()

            JdsFieldType.Short -> if (!shortValues.containsKey(fieldId))
                shortValues[fieldId] = NullableShortProperty()

            JdsFieldType.Long -> if (!longValues.containsKey(fieldId))
                longValues[fieldId] = NullableLongProperty()

            JdsFieldType.Int -> if (!integerValues.containsKey(fieldId))
                integerValues[fieldId] = NullableIntegerProperty()

            JdsFieldType.Uuid -> if (!uuidValues.containsKey(fieldId))
                uuidValues[fieldId] = SimpleObjectProperty<UUID?>()

            JdsFieldType.Boolean -> if (!booleanValues.containsKey(fieldId))
                booleanValues[fieldId] = NullableBooleanProperty()
        }

    }

    /**
     * @param jdsDb
     * @param fieldId
     * @param entityId
     * @param uuid
     * @param innerObjects
     * @param uuids
     */
    internal fun populateObjects(
            jdsDb: JdsDb,
            fieldId: Long?,
            entityId: Long,
            uuid: String,
            editVersion: Int,
            innerObjects: ConcurrentLinkedQueue<JdsEntity>,
            uuids: MutableCollection<JdsEntityComposite>
    ) {
        try {
            if (fieldId == null) return
            objectCollections.filter { it.key.field.id == fieldId }.forEach { kvp ->
                val entity = jdsDb.classes[entityId]!!.getDeclaredConstructor().newInstance()
                entity.overview.uuid = uuid
                entity.overview.editVersion = editVersion

                if (entity is IJdsEntity) {
                    kvp.value.add(entity)
                }
                innerObjects.add(entity)

                uuids.add(JdsEntityComposite(uuid, editVersion))
            }
            objectValues.filter { it.key.field.id == fieldId }.forEach {
                if (it.value.value == null) {
                    it.value.value = jdsDb.classes[entityId]!!.getDeclaredConstructor().newInstance()
                }
                it.value.value.overview.uuid = uuid
                it.value.value.overview.editVersion = editVersion

                val jdsEntity = it.value.value as JdsEntity
                innerObjects.add(jdsEntity)

                uuids.add(JdsEntityComposite(uuid, editVersion))
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }


    /**
     * Binds all the fieldIds attached to an entity, updates the fieldIds dictionary
     * @param connection the SQL connection to use for DB operations
     * @param entityId   the value representing the entity
     */
    internal fun populateRefFieldRefEntityField(
            jdsDb: JdsDb,
            connection: Connection,
            entityId: Long
    ) = try {
        (if (jdsDb.supportsStatements) connection.prepareCall(jdsDb.populateRefField()) else connection.prepareStatement(jdsDb.populateRefField())).use { populateRefField ->
            (if (jdsDb.supportsStatements) connection.prepareCall(jdsDb.populateRefEntityField()) else connection.prepareStatement(jdsDb.populateRefEntityField())).use { populateRefEntityField ->
                getFields(overview.entityId).forEach {
                    val lookup = JdsField.values[it]!!
                    //1. map this jdsField to the jdsField dictionary
                    populateRefField.setLong(1, lookup.id)
                    populateRefField.setString(2, lookup.name)
                    populateRefField.setString(3, lookup.description)
                    populateRefField.setInt(4, lookup.type.ordinal)
                    populateRefField.addBatch()
                    //2. map this jdsField ID to the entity type
                    populateRefEntityField.setLong(1, entityId)
                    populateRefEntityField.setLong(2, lookup.id)
                    populateRefEntityField.addBatch()
                }
                populateRefField.executeBatch()
                populateRefEntityField.executeBatch()
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * Binds all the enumValues attached to an entity
     * @param connection
     * @param entityId
     * @param jdsDb
     */
    @Synchronized
    internal fun populateRefEnumRefEntityEnum(
            jdsDb: JdsDb,
            connection: Connection,
            entityId: Long
    ) {
        populateRefEnum(jdsDb, connection, getEnums(overview.entityId))
        populateRefEntityEnum(jdsDb, connection, entityId, getEnums(overview.entityId))
        if (jdsDb.options.isLoggingOutput) {
            System.out.printf("Mapped Enums for Entity[%s]\n", entityId)
        }
    }

    /**
     * Binds all the enumValues attached to an entity
     * @param jdsDb
     * @param connection the SQL connection to use for DB operations
     * @param entityId the value representing the entity
     * @param fieldIds the entity's enumValues
     */
    @Synchronized
    private fun populateRefEntityEnum(
            jdsDb: JdsDb,
            connection: Connection,
            entityId: Long,
            fieldIds: Set<Long>
    ) = try {
        (if (jdsDb.supportsStatements) connection.prepareCall(jdsDb.populateRefEntityEnum()) else connection.prepareStatement(jdsDb.populateRefEntityEnum())).use {
            for (fieldId in fieldIds) {
                val jdsFieldEnum = JdsFieldEnum.enums[fieldId]!!
                it.setLong(1, entityId)
                it.setLong(2, jdsFieldEnum.field.id)
                it.addBatch()
            }
            it.executeBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * Binds all the values attached to an enum
     * @param jdsDb
     * @param connection the SQL connection to use for DB operations
     * @param fieldIds the jdsField enum
     */
    @Synchronized
    private fun populateRefEnum(jdsDb: JdsDb, connection: Connection, fieldIds: Set<Long>) = try {
        (if (jdsDb.supportsStatements) connection.prepareCall(jdsDb.populateRefEnum()) else connection.prepareStatement(jdsDb.populateRefEnum())).use {
            for (fieldId in fieldIds) {
                val jdsFieldEnum = JdsFieldEnum.enums[fieldId]!!
                jdsFieldEnum.values.forEach { enum ->
                    it.setLong(1, jdsFieldEnum.field.id)
                    it.setInt(2, enum.ordinal)
                    it.setString(3, enum.name)
                    it.setString(4, enum.toString())
                    it.addBatch()
                }
            }
            it.executeBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * @param fieldId
     * @param ordinal
     * @return
     */
    fun getReportAtomicValue(fieldId: Long, ordinal: Int): Any? {
        if (localDateTimeValues.containsKey(fieldId)) {
            return localDateTimeValues[fieldId]!!.value as LocalDateTime?
        }
        if (zonedDateTimeValues.containsKey(fieldId)) {
            return zonedDateTimeValues[fieldId]!!.value as ZonedDateTime?
        }
        if (localDateValues.containsKey(fieldId)) {
            return localDateValues[fieldId]!!.value as LocalDate?
        }
        if (localTimeValues.containsKey(fieldId)) {
            return localTimeValues[fieldId]!!.value as LocalTime?
        }
        if (monthDayValues.containsKey(fieldId)) {
            return (monthDayValues[fieldId]!!.value as MonthDay).toString()
        }
        if (yearMonthValues.containsKey(fieldId)) {
            return yearMonthValues[fieldId]!!.value.toString()
        }
        if (periodValues.containsKey(fieldId)) {
            return periodValues[fieldId]!!.value.toString()
        }
        if (durationValues.containsKey(fieldId)) {
            return durationValues[fieldId]!!.value?.toNanos()
        }
        if (stringValues.containsKey(fieldId)) {
            return stringValues[fieldId]!!.value
        }
        if (floatValues.containsKey(fieldId)) {
            return floatValues[fieldId]?.value?.toFloat()
        }
        if (doubleValues.containsKey(fieldId)) {
            return doubleValues[fieldId]?.value?.toDouble()
        }
        if (shortValues.containsKey(fieldId)) {
            return shortValues[fieldId]?.value?.toShort()
        }
        if (booleanValues.containsKey(fieldId)) {
            return booleanValues[fieldId]?.value
        }
        if (longValues.containsKey(fieldId)) {
            return longValues[fieldId]?.value?.toLong()
        }
        if (stringEnumValues.containsKey(fieldId)) {
            return stringEnumValues[fieldId]?.value.toString()
        }
        if (integerValues.containsKey(fieldId)) {
            return integerValues[fieldId]?.value?.toInt()
        }
        if (uuidValues.containsKey(fieldId)) {
            return uuidValues[fieldId]?.value
        }
        if (enumValues.containsKey(fieldId)) {
            return enumValues[fieldId]?.value?.ordinal
        }
        enumCollections.filter { it.key == fieldId }.forEach { it -> it.value.filter { it.ordinal == ordinal }.forEach { _ -> return true } }
        objectValues.filter { it.key.field.id == fieldId }.forEach { return it.value.value.overview.uuid }
        return null
    }

    /**
     * @param jdsTable
     */
    override fun registerFields(jdsTable: JdsTable) {
        getFields(overview.entityId).forEach {
            if (!JdsSchema.isIgnoredType(it)) {
                jdsTable.registerField(it)
            }
        }
    }

    /**
     * Ensures child entities have ids that link them to their parent.
     * For frequent refreshes/imports from different sources this is necessary to prevent duplicate entries of the same data
     * @param pattern The pattern to set for each nested entity
     */
    @JvmOverloads
    fun standardizeUUIDs(pattern: String = overview.uuid) {
        standardizeObjectUuids(pattern, objectValues.values)
        standardizeCollectionUuids(pattern, objectCollections.values)
    }

    /**
     * Ensures child entities have edit versions that match their parents
     * For frequent refreshes/imports from different sources this is necessary to prevent duplicate entries of the same data
     * @param version The version to set for each nested entity
     */
    @JvmOverloads
    fun standardizeEditVersion(version: Int = overview.editVersion) {
        standardizeObjectEditVersion(version, objectValues.values)
        standardizeCollectionEditVersion(version, objectCollections.values)
    }

    /**
     * Ensures child entities have ids that link them to their parent.
     * @param parentUuid
     * @param objectArrayCollection
     */
    private fun standardizeCollectionUuids(parentUuid: String, objectArrayCollection: Iterable<MutableCollection<out IJdsEntity>>) {
        objectArrayCollection.forEach {
            it.forEachIndexed { sequence, entry ->
                entry.overview.uuid = standardiseLength(parentUuid, ":${entry.overview.entityId}:$sequence")
                standardizeObjectUuids(entry.overview.uuid, entry.objectValues.values)
                standardizeCollectionUuids(entry.overview.uuid, entry.objectCollections.values)
            }
        }
    }

    /**
     * Ensures child entities have ids that link them to their parent.
     * @param parentUuid
     * @param objectCollection
     */
    private fun standardizeObjectUuids(parentUuid: String, objectCollection: Iterable<WritableValue<out IJdsEntity>>) {
        objectCollection.forEach { entry ->
            entry.value.overview.uuid = standardiseLength(parentUuid, ":${entry.value.overview.entityId}")
            standardizeObjectUuids(entry.value.overview.uuid, entry.value.objectValues.values)
            standardizeCollectionUuids(entry.value.overview.uuid, entry.value.objectCollections.values)
        }
    }

    /**
     * Ensures child entities have ids that link them to their parent.
     * @param version
     * @param objectArrayCollection
     */
    private fun standardizeCollectionEditVersion(version: Int, objectArrayCollection: Iterable<MutableCollection<out IJdsEntity>>) {
        objectArrayCollection.forEach {
            it.forEach { entry ->
                entry.overview.editVersion = version
                standardizeObjectEditVersion(version, entry.objectValues.values)
                standardizeCollectionEditVersion(version, entry.objectCollections.values)
            }
        }
    }

    /**
     * Ensures child entities have ids that link them to their parent.
     * @param version
     * @param objectCollection
     */
    private fun standardizeObjectEditVersion(version: Int, objectCollection: Iterable<WritableValue<out IJdsEntity>>) {
        objectCollection.forEach { objectProperty ->
            objectProperty.value.overview.editVersion = version
            standardizeObjectEditVersion(version, objectProperty.value.objectValues.values)
            standardizeCollectionEditVersion(version, objectProperty.value.objectCollections.values)
        }
    }

    /**
     * Internal helper function that works with all nested objects
     */
    override fun getNestedEntities(includeThisEntity: Boolean): Sequence<JdsEntity> = sequence {
        if (includeThisEntity) {
            yield(this@JdsEntity)
        }
        objectValues.values.forEach { objectProperty -> yieldAll(objectProperty.value.getNestedEntities()) }
        objectCollections.values.forEach { objectCollection -> objectCollection.forEach { entity -> yieldAll(entity.getNestedEntities()) } }
    }

    /**
     * Internal helper function that works with all nested objects
     */
    override fun getNestedEntities(collection: MutableCollection<JdsEntity>, includeThisEntity: Boolean) {
        if (includeThisEntity) {
            collection.add(this@JdsEntity)
        }
        objectValues.values.forEach { it.value.getNestedEntities(collection) }
        objectCollections.values.forEach { it -> it.forEach { it.getNestedEntities(collection) } }
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun toString(): String {
        return "JdsEntity(uuid=${overview.uuid},editVersion=${overview.editVersion},entityId=${overview.entityId})"
    }

    companion object : Externalizable {

        private const val serialVersionUID = 20180106_2125L
        private val allFields = ConcurrentHashMap<Long, LinkedHashSet<Long>>()
        private val allEnums = ConcurrentHashMap<Long, LinkedHashSet<Long>>()

        private fun standardiseLength(dest: String, append: String): String {
            val appendLength = append.length //e.g 5
            val substringIndex = dest.length - appendLength //e.g 36
            return "${dest.subSequence(0, substringIndex)}$append"
        }

        override fun readExternal(objectInput: ObjectInput) {
            allFields.clear()
            allFields.putAll(objectInput.readObject() as Map<Long, LinkedHashSet<Long>>)
            allEnums.clear()
            allEnums.putAll(objectInput.readObject() as Map<Long, LinkedHashSet<Long>>)
        }

        override fun writeExternal(objectOutput: ObjectOutput) {
            objectOutput.writeObject(allFields)
            objectOutput.writeObject(allEnums)
        }

        protected fun mapField(entityId: Long, fieldId: Long): Long {
            getFields(entityId).add(fieldId)
            return fieldId
        }

        protected fun mapEnums(entityId: Long, fieldId: Long): Long {
            getEnums(entityId).add(fieldId)
            return fieldId
        }

        private fun toTimeStampCollection(values: MutableCollection<LocalDateTime>) = values.map { Timestamp.valueOf(it) }.toMutableList()

        private fun toIntCollection(values: MutableCollection<Enum<*>>) = values.map { it.ordinal }.toMutableList()

        private fun toStringCollection(values: MutableCollection<Enum<*>>) = values.map { it.name }.toMutableList()

        private fun getFields(entityId: Long) = allFields.getOrPut(entityId) { LinkedHashSet() }

        private fun getEnums(entityId: Long) = allEnums.getOrPut(entityId) { LinkedHashSet() }

        /**
         * Convenience method to help with [WritableValue] containers.
         * Exposes [WritableValue.getValue] via a get() accessor.
         * Specifically for non-nullable [String] properties
         */
        fun WritableValue<String?>.get(): String {
            return this.value.orEmpty()
        }

        /**
         * Convenience method to help with [WritableValue] containers.
         * Exposes [WritableValue.getValue] via a get() accessor
         */
        fun <T> WritableValue<T>.get(): T {
            return this.value
        }

        /**
         * Convenience method to help with [WritableValue] containers.
         * Exposes [WritableValue.setValue] via a set() modifier
         */
        fun <T> WritableValue<T>.set(value: T) {
            this.value = value
        }
    }
}
