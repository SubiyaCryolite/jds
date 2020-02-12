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
import io.github.subiyacryolite.jds.Validate.validateBlob
import io.github.subiyacryolite.jds.Validate.validateBoolean
import io.github.subiyacryolite.jds.Validate.validateDate
import io.github.subiyacryolite.jds.Validate.validateDateTime
import io.github.subiyacryolite.jds.Validate.validateDateTimeCollection
import io.github.subiyacryolite.jds.Validate.validateDouble
import io.github.subiyacryolite.jds.Validate.validateDoubleCollection
import io.github.subiyacryolite.jds.Validate.validateDuration
import io.github.subiyacryolite.jds.Validate.validateEnum
import io.github.subiyacryolite.jds.Validate.validateFloat
import io.github.subiyacryolite.jds.Validate.validateFloatCollection
import io.github.subiyacryolite.jds.Validate.validateInt
import io.github.subiyacryolite.jds.Validate.validateIntCollection
import io.github.subiyacryolite.jds.Validate.validateLong
import io.github.subiyacryolite.jds.Validate.validateLongCollection
import io.github.subiyacryolite.jds.Validate.validateMonthDay
import io.github.subiyacryolite.jds.Validate.validatePeriod
import io.github.subiyacryolite.jds.Validate.validateShort
import io.github.subiyacryolite.jds.Validate.validateString
import io.github.subiyacryolite.jds.Validate.validateStringCollection
import io.github.subiyacryolite.jds.Validate.validateTime
import io.github.subiyacryolite.jds.Validate.validateUuid
import io.github.subiyacryolite.jds.Validate.validateYearMonth
import io.github.subiyacryolite.jds.Validate.validateZonedDateTime
import io.github.subiyacryolite.jds.annotations.EntityAnnotation
import io.github.subiyacryolite.jds.beans.property.*
import io.github.subiyacryolite.jds.context.DbContext
import io.github.subiyacryolite.jds.enums.FieldType
import io.github.subiyacryolite.jds.extensions.*
import io.github.subiyacryolite.jds.portable.*
import io.github.subiyacryolite.jds.utility.DeepCopy
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
import kotlin.collections.ArrayList

/**
 * This class allows for all mapping operations in JDS, it also uses
 * [IOverview] to store overview data
 */
abstract class Entity : IEntity, Serializable {
    @set:JsonIgnore
    @get:JsonIgnore
    final override var overview: IOverview = Overview()
    //temporals
    @get:JsonIgnore
    internal val localDateTimeValues: HashMap<Int, WritableProperty<LocalDateTime?>> = HashMap()
    @get:JsonIgnore
    internal val zonedDateTimeValues: HashMap<Int, WritableProperty<ZonedDateTime?>> = HashMap()
    @get:JsonIgnore
    internal val localDateValues: HashMap<Int, WritableProperty<LocalDate?>> = HashMap()
    @get:JsonIgnore
    internal val localTimeValues: HashMap<Int, WritableProperty<LocalTime?>> = HashMap()
    @get:JsonIgnore
    internal val monthDayValues: HashMap<Int, WritableProperty<MonthDay?>> = HashMap()
    @get:JsonIgnore
    internal val yearMonthValues: HashMap<Int, WritableProperty<YearMonth?>> = HashMap()
    @get:JsonIgnore
    internal val periodValues: HashMap<Int, WritableProperty<Period?>> = HashMap()
    @get:JsonIgnore
    internal val durationValues: HashMap<Int, WritableProperty<Duration?>> = HashMap()
    //strings
    @get:JsonIgnore
    internal val stringValues: HashMap<Int, WritableProperty<String?>> = HashMap()
    //boolean
    @get:JsonIgnore
    internal val booleanValues: HashMap<Int, WritableProperty<Boolean?>> = HashMap()
    //numeric
    @get:JsonIgnore
    internal val shortValues: HashMap<Int, WritableProperty<Short?>> = HashMap()
    @get:JsonIgnore
    internal val floatValues: HashMap<Int, WritableProperty<Float?>> = HashMap()
    @get:JsonIgnore
    internal val doubleValues: HashMap<Int, WritableProperty<Double?>> = HashMap()
    @get:JsonIgnore
    internal val longValues: HashMap<Int, WritableProperty<Long?>> = HashMap()
    @get:JsonIgnore
    internal val integerValues: HashMap<Int, WritableProperty<Int?>> = HashMap()
    @get:JsonIgnore
    internal val uuidValues: HashMap<Int, WritableProperty<UUID?>> = HashMap()
    //collections
    @get:JsonIgnore
    override val objectCollections: HashMap<FieldEntity<*>, MutableCollection<IEntity>> = HashMap()
    @get:JsonIgnore
    internal val stringCollections: HashMap<Int, MutableCollection<String>> = HashMap()
    @get:JsonIgnore
    internal val dateTimeCollections: HashMap<Int, MutableCollection<LocalDateTime>> = HashMap()
    @get:JsonIgnore
    internal val floatCollections: HashMap<Int, MutableCollection<Float>> = HashMap()
    @get:JsonIgnore
    internal val doubleCollections: HashMap<Int, MutableCollection<Double>> = HashMap()
    @get:JsonIgnore
    internal val longCollections: HashMap<Int, MutableCollection<Long>> = HashMap()
    @get:JsonIgnore
    internal val integerCollections: HashMap<Int, MutableCollection<Int>> = HashMap()
    //enums
    @get:JsonIgnore
    internal val enumValues: HashMap<Int, WritableProperty<Enum<*>?>> = HashMap()
    @get:JsonIgnore
    internal val stringEnumValues: HashMap<Int, WritableProperty<Enum<*>?>> = HashMap()
    @get:JsonIgnore
    internal val enumCollections: HashMap<Int, MutableCollection<Enum<*>>> = HashMap()
    @get:JsonIgnore
    internal val enumStringCollections: HashMap<Int, MutableCollection<Enum<*>>> = HashMap()
    //objects
    @get:JsonIgnore
    override val objectValues: HashMap<FieldEntity<*>, WritableProperty<IEntity>> = HashMap()
    //blobs
    @get:JsonIgnore
    internal val blobValues: HashMap<Int, WritableProperty<ByteArray?>> = HashMap()

    init {
        val entityAnnotation = getEntityAnnotation(javaClass)
        if (entityAnnotation != null) {
            overview.entityId = entityAnnotation.id
        } else {
            throw RuntimeException("You must annotate the class [" + javaClass.canonicalName + "] or its immediate parent with [" + EntityAnnotation::class.java + "]")
        }
    }

    @JvmName("mapShort")
    protected fun map(field: Field, value: Short) = map(field, ShortProperty(value))

    @JvmName("mapShort")
    protected fun map(field: Field, property: WritableProperty<Short>): WritableProperty<Short> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as WritableProperty<Short?>)
        return property
    }

    @JvmName("mapNullableShort")
    protected fun map(field: Field, property: WritableProperty<Short?>): WritableProperty<Short?> {
        validateShort(field)
        return shortValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapDouble")
    protected fun map(field: Field, value: Double) = map(field, DoubleProperty(value))

    @JvmName("mapDouble")
    protected fun map(field: Field, property: WritableProperty<Double>): WritableProperty<Double> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as WritableProperty<Double?>)
        return property
    }

    @JvmName("mapNullableDouble")
    protected fun map(field: Field, property: WritableProperty<Double?>): WritableProperty<Double?> {
        validateDouble(field)
        return doubleValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapInt")
    protected fun map(field: Field, value: Int) = map(field, IntegerProperty(value))

    @JvmName("mapInt")
    protected fun map(field: Field, property: WritableProperty<Int>): WritableProperty<Int> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as WritableProperty<Int?>)
        return property
    }

    @JvmName("mapNullableInt")
    protected fun map(field: Field, property: WritableProperty<Int?>): WritableProperty<Int?> {
        validateInt(field)
        return integerValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapLong")
    protected fun map(field: Field, value: Long) = map(field, LongProperty(value))

    @JvmName("mapLong")
    protected fun map(field: Field, property: WritableProperty<Long>): WritableProperty<Long> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as WritableProperty<Long?>)
        return property
    }

    @JvmName("mapNullableLong")
    protected fun map(field: Field, property: WritableProperty<Long?>): WritableProperty<Long?> {
        validateLong(field)
        return longValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapFloat")
    protected fun map(field: Field, value: Float) = map(field, FloatProperty(value))

    @JvmName("mapFloat")
    protected fun map(field: Field, property: WritableProperty<Float>): WritableProperty<Float> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as WritableProperty<Float?>)
        return property
    }

    @JvmName("mapNullableFloat")
    protected fun map(field: Field, property: WritableProperty<Float?>): WritableProperty<Float?> {
        validateFloat(field)
        return floatValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapBoolean")
    protected fun map(field: Field, value: Boolean) = map(field, BooleanProperty(value))

    @JvmName("mapBoolean")
    protected fun map(field: Field, property: WritableProperty<Boolean>): WritableProperty<Boolean> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as WritableProperty<Boolean?>)
        return property
    }

    @JvmName("mapNullableBoolean")
    protected fun map(field: Field, property: WritableProperty<Boolean?>): WritableProperty<Boolean?> {
        validateBoolean(field)
        return booleanValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapUuid")
    protected fun map(field: Field, value: UUID) = map(field, UuidProperty(value))

    @JvmName("mapUuid")
    protected fun map(field: Field, property: WritableProperty<UUID>): WritableProperty<UUID> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as WritableProperty<UUID?>)
        return property
    }

    @JvmName("mapNullableUuid")
    protected fun map(field: Field, property: WritableProperty<UUID?>): WritableProperty<UUID?> {
        validateUuid(field)
        return uuidValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapString")
    protected fun map(field: Field, value: String) = map(field, StringProperty(value))

    @JvmName("mapString")
    protected fun map(field: Field, property: WritableProperty<String>): WritableProperty<String> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as WritableProperty<String?>)
        return property
    }

    @JvmName("mapNullableString")
    protected fun map(field: Field, property: WritableProperty<String?>): WritableProperty<String?> {
        validateString(field)
        return stringValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapDateTime")
    protected fun map(field: Field, value: LocalDateTime) = map(field, LocalDateTimeProperty(value))

    @JvmName("mapDateTime")
    protected fun map(field: Field, property: WritableProperty<LocalDateTime>): WritableProperty<LocalDateTime> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as WritableProperty<LocalDateTime?>)
        return property
    }

    @JvmName("mapNullableDateTime")
    protected fun map(field: Field, property: WritableProperty<LocalDateTime?>): WritableProperty<LocalDateTime?> {
        validateDateTime(field)
        return localDateTimeValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapZonedDateTime")
    protected fun map(field: Field, value: ZonedDateTime) = map(field, ZonedDateTimeProperty(value))

    @JvmName("mapZonedDateTime")
    protected fun map(field: Field, property: WritableProperty<ZonedDateTime>): WritableProperty<ZonedDateTime> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as WritableProperty<ZonedDateTime?>)
        return property
    }

    @JvmName("mapNullableZonedDateTime")
    protected fun map(field: Field, property: WritableProperty<ZonedDateTime?>): WritableProperty<ZonedDateTime?> {
        validateZonedDateTime(field)
        return zonedDateTimeValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapDate")
    protected fun map(field: Field, value: LocalDate) = map(field, LocalDateProperty(value))

    @JvmName("mapDate")
    protected fun map(field: Field, property: WritableProperty<LocalDate>): WritableProperty<LocalDate> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as WritableProperty<LocalDate?>)
        return property
    }

    @JvmName("mapNullableDate")
    protected fun map(field: Field, property: WritableProperty<LocalDate?>): WritableProperty<LocalDate?> {
        validateDate(field)
        return localDateValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapTime")
    protected fun map(field: Field, value: LocalTime) = map(field, LocalTimeProperty(value))

    @JvmName("mapTime")
    protected fun map(field: Field, property: WritableProperty<LocalTime>): WritableProperty<LocalTime> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as WritableProperty<LocalTime?>)
        return property
    }

    @JvmName("mapNullableTime")
    protected fun map(field: Field, property: WritableProperty<LocalTime?>): WritableProperty<LocalTime?> {
        validateTime(field)
        return localTimeValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapBlob")
    protected fun map(field: Field, value: ByteArray) = map(field, BlobProperty(value))

    @JvmName("mapBlob")
    protected fun map(field: Field, property: WritableProperty<ByteArray>): WritableProperty<ByteArray> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as WritableProperty<ByteArray?>)
        return property
    }

    @JvmName("mapNullableBlob")
    protected fun map(field: Field, property: WritableProperty<ByteArray?>): WritableProperty<ByteArray?> {
        validateBlob(field)
        return blobValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapMonthDay")
    protected fun map(field: Field, value: MonthDay) = map(field, MonthDayProperty(value))

    @JvmName("mapMonthDay")
    protected fun map(field: Field, property: WritableProperty<MonthDay>): WritableProperty<MonthDay> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as WritableProperty<MonthDay?>)
        return property
    }

    @JvmName("mapNullableMonthDay")
    protected fun map(field: Field, property: WritableProperty<MonthDay?>): WritableProperty<MonthDay?> {
        validateMonthDay(field)
        return monthDayValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapYearMonth")
    protected fun map(field: Field, value: YearMonth) = map(field, YearMonthProperty(value))

    @JvmName("mapYearMonth")
    protected fun map(field: Field, property: WritableProperty<YearMonth>): WritableProperty<YearMonth> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as WritableProperty<YearMonth?>)
        return property
    }

    @JvmName("mapNullableYearMonth")
    protected fun map(field: Field, property: WritableProperty<YearMonth?>): WritableProperty<YearMonth?> {
        validateYearMonth(field)
        return yearMonthValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapPeriod")
    protected fun map(field: Field, value: Period) = map(field, PeriodProperty(value))

    @JvmName("mapPeriod")
    protected fun map(field: Field, property: WritableProperty<Period>): WritableProperty<Period> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as WritableProperty<Period?>)
        return property
    }

    @JvmName("mapNullablePeriod")
    protected fun map(field: Field, property: WritableProperty<Period?>): WritableProperty<Period?> {
        validatePeriod(field)
        return periodValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapDuration")
    protected fun map(field: Field, value: Duration) = map(field, DurationProperty(value))

    @JvmName("mapDuration")
    protected fun map(field: Field, property: WritableProperty<Duration>): WritableProperty<Duration> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as WritableProperty<Duration?>)
        return property
    }

    @JvmName("mapNullableDuration")
    protected fun map(field: Field, property: WritableProperty<Duration?>): WritableProperty<Duration?> {
        validateDuration(field)
        return durationValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapNullableEnum")
    protected fun <T : Enum<T>> map(fieldEnum: FieldEnum<T>, value: T) = map(fieldEnum, EnumProperty(value))

    @JvmName("mapNullableEnum")
    protected fun <T : Enum<T>> map(fieldEnum: FieldEnum<T>, property: WritableProperty<T?>): WritableProperty<T?> {
        validateEnum(fieldEnum.field)
        if (fieldEnum.field.type == FieldType.Enum) {
            enumValues[fieldEnum.field.id] = property as WritableProperty<Enum<*>?>
        } else {
            stringEnumValues[fieldEnum.field.id] = property as WritableProperty<Enum<*>?>
        }
        fieldEnum.field.bind()
        mapField(overview.entityId, fieldEnum.field.id)
        mapEnums(overview.entityId, fieldEnum.field.id)
        return property
    }

    @JvmName("mapEnum")
    protected fun <T : Enum<T>> map(fieldEnum: FieldEnum<T>, property: WritableProperty<T>): WritableProperty<T> {
        validateEnum(fieldEnum.field)
        if (fieldEnum.field.type == FieldType.Enum) {
            enumValues[fieldEnum.field.id] = property as WritableProperty<Enum<*>?>
        } else {
            stringEnumValues[fieldEnum.field.id] = property as WritableProperty<Enum<*>?>
        }
        fieldEnum.field.bind()
        mapField(overview.entityId, fieldEnum.field.id)
        mapEnums(overview.entityId, fieldEnum.field.id)
        return property
    }

    @JvmName("mapStrings")
    protected fun map(field: Field, collection: MutableCollection<String>): MutableCollection<String> {
        validateStringCollection(field)
        return stringCollections.getOrPut(mapField(overview.entityId, field.bind())) { collection }
    }

    @JvmName("mapDateTimes")
    protected fun map(field: Field, collection: MutableCollection<LocalDateTime>): MutableCollection<LocalDateTime> {
        validateDateTimeCollection(field)
        return dateTimeCollections.getOrPut(mapField(overview.entityId, field.bind())) { collection }
    }

    @JvmName("mapFloats")
    protected fun map(field: Field, collection: MutableCollection<Float>): MutableCollection<Float> {
        validateFloatCollection(field)
        return floatCollections.getOrPut(mapField(overview.entityId, field.bind())) { collection }
    }

    @JvmName("mapIntegers")
    protected fun map(field: Field, collection: MutableCollection<Int>): MutableCollection<Int> {
        validateIntCollection(field)
        return integerCollections.getOrPut(mapField(overview.entityId, field.bind())) { collection }
    }

    @JvmName("mapDoubles")
    protected fun map(field: Field, collection: MutableCollection<Double>): MutableCollection<Double> {
        validateDoubleCollection(field)
        return doubleCollections.getOrPut(mapField(overview.entityId, field.bind())) { collection }
    }

    @JvmName("mapLongs")
    protected fun map(field: Field, collection: MutableCollection<Long>): MutableCollection<Long> {
        validateLongCollection(field)
        return longCollections.getOrPut(mapField(overview.entityId, field.bind())) { collection }
    }

    @JvmName("mapEnums")
    protected fun <T : Enum<T>> map(fieldEnum: FieldEnum<T>, collection: MutableCollection<T>): MutableCollection<T> {
        if (fieldEnum.field.type != FieldType.EnumCollection && fieldEnum.field.type != FieldType.EnumStringCollection) {
            throw RuntimeException("Incorrect type supplied for field [$fieldEnum.field]")
        }
        if (fieldEnum.field.type == FieldType.EnumCollection) {
            enumCollections[fieldEnum.field.id] = collection as MutableCollection<Enum<*>>
        } else {
            enumStringCollections[fieldEnum.field.id] = collection as MutableCollection<Enum<*>>
        }
        fieldEnum.field.bind()
        mapField(overview.entityId, fieldEnum.field.id)
        mapEnums(overview.entityId, fieldEnum.field.id)
        return collection
    }

    protected fun <T : IEntity> map(fieldEntity: FieldEntity<T>, entity: T): WritableProperty<T> {
        return map(fieldEntity, ObjectProperty(entity))
    }

    protected fun <T : IEntity> map(fieldEntity: FieldEntity<T>, property: WritableProperty<T>): WritableProperty<T> {
        if (fieldEntity.field.type != FieldType.Entity) {
            throw RuntimeException("Please assign the correct type to field [$fieldEntity]")
        }
        if (!objectCollections.containsKey(fieldEntity) && !objectValues.containsKey(fieldEntity)) {
            bindFieldIdToEntity(fieldEntity)
            objectValues[fieldEntity] = property as WritableProperty<IEntity>
            mapField(overview.entityId, fieldEntity.field.id)
        } else {
            throw RuntimeException("You can only bind a class to one WritableProperty. This class is already bound to one object or object array")
        }
        return property
    }

    /**
     * @param fieldEntity
     * @param collection
     */
    protected fun <T : IEntity> map(fieldEntity: FieldEntity<T>, collection: MutableCollection<T>): MutableCollection<T> {
        if (fieldEntity.field.type != FieldType.EntityCollection) {
            throw RuntimeException("Please supply a valid type for JdsFieldEntity")
        }
        if (!objectCollections.containsKey(fieldEntity)) {
            bindFieldIdToEntity(fieldEntity)
            objectCollections[fieldEntity] = collection as MutableCollection<IEntity>
            mapField(overview.entityId, fieldEntity.field.id)
        } else {
            throw RuntimeException("You can only bind a class to one WritableProperty. This class is already bound to one object or object array")
        }
        return collection
    }

    private fun <T : IEntity> bindFieldIdToEntity(fieldEntity: FieldEntity<T>) {
        fieldEntity.field.bind()
        FieldEntity.values[fieldEntity.field.id] = fieldEntity
    }

    /**
     * Copy values from matching fieldIds found in both objects
     *
     * @param source The entity to copy values from
     * @param <T>    A valid JDSEntity
    </T> */
    fun <T : Entity> copy(source: T) {
        this.overview.id = source.overview.id
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
     * @param jdsPortableEntity
     */
    @Throws(Exception::class)
    override fun assign(dbContext: DbContext, jdsPortableEntity: PortableEntity) {
        //==============================================
        //PRIMITIVES, also saved to array struct to streamline json
        //==============================================
        booleanValues.filterSensitiveFields(dbContext).forEach {
            val input = when (it.value.value) {
                true -> 1
                false -> 0
                else -> null
            }
            jdsPortableEntity.booleanValues.add(StoreBoolean(it.key, input))
        }
        stringValues.filterSensitiveFields(dbContext).forEach { jdsPortableEntity.stringValues.add(StoreString(it.key, it.value.value)) }
        floatValues.filterSensitiveFields(dbContext).forEach { jdsPortableEntity.floatValue.add(StoreFloat(it.key, it.value.value)) }
        doubleValues.filterSensitiveFields(dbContext).forEach { jdsPortableEntity.doubleValues.add(StoreDouble(it.key, it.value.value)) }
        shortValues.filterSensitiveFields(dbContext).forEach { jdsPortableEntity.shortValues.add(StoreShort(it.key, it.value.value)) }
        longValues.filterSensitiveFields(dbContext).forEach { jdsPortableEntity.longValues.add(StoreLong(it.key, it.value.value)) }
        integerValues.filterSensitiveFields(dbContext).forEach { jdsPortableEntity.integerValues.add(StoreInteger(it.key, it.value.value)) }
        uuidValues.filterSensitiveFields(dbContext).forEach { jdsPortableEntity.uuidValues.add(StoreUuid(it.key, it.value.value.toByteArray())) }
        //==============================================
        //Dates & Time
        //==============================================
        zonedDateTimeValues.filterSensitiveFields(dbContext).forEach { jdsPortableEntity.zonedDateTimeValues.add(StoreZonedDateTime(it.key, (it.value.value as ZonedDateTime?)?.toInstant()?.toEpochMilli())) }
        localTimeValues.filterSensitiveFields(dbContext).forEach { jdsPortableEntity.timeValues.add(StoreTime(it.key, (it.value.value as LocalTime?)?.toNanoOfDay())) }
        durationValues.filterSensitiveFields(dbContext).forEach { jdsPortableEntity.durationValues.add(StoreDuration(it.key, it.value.value?.toNanos())) }
        localDateTimeValues.filterSensitiveFields(dbContext).forEach { jdsPortableEntity.dateTimeValues.add(StoreDateTime(it.key, safeLocalDateTime(it.value.value))) }
        localDateValues.filterSensitiveFields(dbContext).forEach { jdsPortableEntity.dateValues.add(StoreDate(it.key, safeLocalDate(it.value.value))) }
        monthDayValues.filterSensitiveFields(dbContext).forEach { jdsPortableEntity.monthDayValues.add(StoreMonthDay(it.key, it.value.value?.toString())) }
        yearMonthValues.filterSensitiveFields(dbContext).forEach { jdsPortableEntity.yearMonthValues.add(StoreYearMonth(it.key, (it.value.value as YearMonth?)?.toString())) }
        periodValues.filterSensitiveFields(dbContext).forEach { jdsPortableEntity.periodValues.add(StorePeriod(it.key, it.value.value?.toString())) }
        //==============================================
        //BLOB
        //==============================================
        blobValues.filterSensitiveFields(dbContext).forEach {
            jdsPortableEntity.blobValues.add(StoreBlob(it.key, it.value.value ?: ByteArray(0)))
        }
        //==============================================
        //Enums
        //==============================================
        enumValues.filterSensitiveFields(dbContext).forEach { jdsPortableEntity.enumValues.add(StoreEnum(it.key, it.value.value?.ordinal)) }
        stringEnumValues.filterSensitiveFields(dbContext).forEach { jdsPortableEntity.enumStringValues.add(StoreEnumString(it.key, it.value.value?.name)) }
        enumCollections.filterSensitiveFields(dbContext).forEach { jdsPortableEntity.enumCollections.add(StoreEnumCollection(it.key, toIntCollection(it.value))) }
        enumStringCollections.filterSensitiveFields(dbContext).forEach { jdsPortableEntity.enumStringCollections.add(StoreEnumStringCollection(it.key, toStringCollection(it.value))) }
        //==============================================
        //ARRAYS
        //==============================================
        stringCollections.filterSensitiveFields(dbContext).forEach { jdsPortableEntity.stringCollections.add(StoreStringCollection(it.key, it.value)) }
        dateTimeCollections.filterSensitiveFields(dbContext).forEach { jdsPortableEntity.dateTimeCollection.add(StoreDateTimeCollection(it.key, toTimeStampCollection(it.value))) }
        floatCollections.filterSensitiveFields(dbContext).forEach { jdsPortableEntity.floatCollections.add(StoreFloatCollection(it.key, it.value)) }
        doubleCollections.filterSensitiveFields(dbContext).forEach { jdsPortableEntity.doubleCollections.add(StoreDoubleCollection(it.key, it.value)) }
        longCollections.filterSensitiveFields(dbContext).forEach { jdsPortableEntity.longCollections.add(StoreLongCollection(it.key, it.value)) }
        integerCollections.filterSensitiveFields(dbContext).forEach { jdsPortableEntity.integerCollections.add(StoreIntegerCollection(it.key, it.value)) }
        //==============================================
        //EMBEDDED OBJECTS
        //==============================================
        objectCollections.forEach { (fieldEntity, mutableCollection) ->
            mutableCollection.forEach { iJdsEntity ->
                val embeddedObject = PortableEntity()
                embeddedObject.fieldId = fieldEntity.field.id
                embeddedObject.init(dbContext, iJdsEntity)
                jdsPortableEntity.entityOverviews.add(embeddedObject)
            }
        }
        objectValues.forEach { (fieldEntity, objectWritableProperty) ->
            val embeddedObject = PortableEntity()
            embeddedObject.fieldId = fieldEntity.field.id
            embeddedObject.init(dbContext, objectWritableProperty.value)
            jdsPortableEntity.entityOverviews.add(embeddedObject)
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
    internal fun populateProperties(dbContext: DbContext, fieldType: FieldType, fieldId: Int, value: Any?) {
        initBackingWritablePropertyIfNotDefined(fieldType, fieldId)
        if (!dbContext.options.populateSensitiveData) {
            if (Field.values[fieldId]!!.sensitive) {
                return
            }
        }
        when (fieldType) {
            FieldType.Float -> {
                floatValues[fieldId]?.value = when (value) {
                    is Double -> value.toFloat()
                    else -> value as Float?
                }
            }
            FieldType.Double -> {
                doubleValues[fieldId]?.value = value as Double?
            }
            FieldType.Short -> {
                shortValues[fieldId]?.value = when (value) {
                    is Double -> value.toShort()
                    is Int -> value.toShort()
                    is Short? -> value
                    else -> null
                }
            }
            FieldType.Long -> {
                longValues[fieldId]?.value = when (value) {
                    is Long? -> value
                    is BigDecimal -> value.toLong() //Oracle
                    is Int -> value.toLong()
                    else -> null
                }
            }
            FieldType.Int -> {
                integerValues[fieldId]?.value = when (value) {
                    is Int? -> value
                    is BigDecimal -> value.toInt() //Oracle
                    else -> null
                }
            }
            FieldType.Uuid -> {
                uuidValues[fieldId]?.value = when (value) {
                    is ByteArray? -> value.toUuid()
                    is String -> UUID.fromString(value)
                    is UUID? -> value
                    else -> null
                }
            }
            FieldType.Boolean -> {
                booleanValues[fieldId]?.value = when (value) {
                    is Int -> value == 1
                    is Boolean? -> value
                    is BigDecimal -> value.intValueExact() == 1 //Oracle
                    else -> null
                }
            }
            FieldType.DoubleCollection -> {
                doubleCollections[fieldId]?.add(value as Double)
            }
            FieldType.FloatCollection -> {
                floatCollections[fieldId]?.add(value as Float)
            }
            FieldType.LongCollection -> {
                longCollections[fieldId]?.add(value as Long)
            }
            FieldType.IntCollection -> {
                integerCollections[fieldId]?.add(value as Int)
            }
            FieldType.Enum -> {
                enumValues.filter { it.key == fieldId }.forEach {
                    val fieldEnum = FieldEnum.enums[it.key]
                    if (fieldEnum != null) {
                        if (value != null) {
                            it.value.value = (when (value) {
                                is BigDecimal -> fieldEnum.valueOf(value.intValueExact())
                                else -> fieldEnum.valueOf(value as Int)
                            })
                        }
                    }
                }
            }
            FieldType.EnumString -> {
                enumValues.filter { it.key == fieldId }.forEach {
                    val fieldEnum = FieldEnum.enums[it.key]
                    if (fieldEnum != null && value is String)
                        it.value.value = fieldEnum.valueOf(value)
                }
            }
            FieldType.EnumCollection -> {
                //Enum collections should NOT accept nulls. Unknown collection should be skipped
                enumCollections.filter { it.key == fieldId }.forEach {
                    val fieldEnum = FieldEnum.enums[it.key]
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
            }
            FieldType.EnumStringCollection -> {
                //Enum collections should NOT accept nulls. Unknown collection should be skipped
                enumStringCollections.filter { it.key == fieldId }.forEach {
                    val fieldEnum = FieldEnum.enums[it.key]
                    if (fieldEnum != null) {
                        val enumStr = value.toString()
                        val enumVal = fieldEnum.valueOf(enumStr)
                        if (enumVal != null)
                            it.value.add(enumVal)
                    }
                }
            }
            FieldType.String -> {
                stringValues[fieldId]?.value = (value as String?)
            }
            FieldType.StringCollection -> {
                stringCollections[fieldId]?.add(value as String)
            }
            FieldType.ZonedDateTime -> {
                when (value) {
                    is Long -> zonedDateTimeValues[fieldId]?.value = ZonedDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault())
                    is Timestamp -> zonedDateTimeValues[fieldId]?.value = ZonedDateTime.ofInstant(value.toInstant(), ZoneOffset.systemDefault())
                    is String -> zonedDateTimeValues[fieldId]?.value = value.toZonedDateTime()
                    is OffsetDateTime -> zonedDateTimeValues[fieldId]?.value = value.atZoneSameInstant(ZoneId.systemDefault())
                }
            }
            FieldType.Date -> {
                when (value) {
                    is Timestamp -> localDateValues[fieldId]?.value = (value.toLocalDateTime().toLocalDate())
                    is LocalDate -> localDateValues[fieldId]?.value = (value)
                    is String -> localDateValues[fieldId]?.value = (value.toLocalDate())
                    else -> localDateValues[fieldId]?.value = null
                }
            }
            FieldType.Time -> {
                when (value) {
                    is Long -> localTimeValues[fieldId]?.value = (LocalTime.MIN.plusNanos(value))
                    is LocalTime -> localTimeValues[fieldId]?.value = (value)
                    is String -> localTimeValues[fieldId]?.value = (value.toLocalTime())
                }
            }
            FieldType.Duration -> {
                durationValues[fieldId]?.value = (when (value) {
                    is BigDecimal -> Duration.ofNanos(value.longValueExact())//Oracle
                    else -> Duration.ofNanos(value as Long)
                })
            }
            FieldType.MonthDay -> {
                monthDayValues[fieldId]?.value = if (value is String) MonthDay.parse(value) else null
            }
            FieldType.YearMonth -> {
                yearMonthValues[fieldId]?.value = if (value is String) YearMonth.parse(value) else null
            }
            FieldType.Period -> {
                periodValues[fieldId]?.value = if (value is String) Period.parse(value) else null
            }
            FieldType.DateTime -> {
                localDateTimeValues[fieldId]?.value = if (value is Timestamp) value.toLocalDateTime() else null
            }
            FieldType.DateTimeCollection -> {
                dateTimeCollections[fieldId]?.add((value as Timestamp).toLocalDateTime())
            }
            FieldType.Blob -> when (value) {
                is ByteArray -> blobValues[fieldId]?.value = value
                null -> blobValues[fieldId]?.value = ByteArray(0)//Oracle
            }
            else -> {
            }
        }
    }

    /**
     * This method enforces forward compatibility by ensuring that every WritableProperty is present even if the field is not defined or known locally
     */
    private fun initBackingWritablePropertyIfNotDefined(fieldType: FieldType, fieldId: Int) {
        when (fieldType) {
            FieldType.String -> if (!stringValues.containsKey(fieldId)) {
                stringValues[fieldId] = NullableStringProperty()
            }
            FieldType.DoubleCollection -> if (!doubleCollections.containsKey(fieldId)) {
                doubleCollections[fieldId] = ArrayList()
            }
            FieldType.FloatCollection -> if (!floatCollections.containsKey(fieldId)) {
                floatCollections[fieldId] = ArrayList()
            }
            FieldType.LongCollection -> if (!longCollections.containsKey(fieldId)) {
                longCollections[fieldId] = ArrayList()
            }
            FieldType.IntCollection -> if (!integerCollections.containsKey(fieldId)) {
                integerCollections[fieldId] = ArrayList()
            }
            FieldType.StringCollection -> if (!stringCollections.containsKey(fieldId)) {
                stringCollections[fieldId] = ArrayList()
            }
            FieldType.DateTimeCollection -> if (!dateTimeCollections.containsKey(fieldId)) {
                dateTimeCollections[fieldId] = ArrayList()
            }
            FieldType.EnumCollection -> if (!enumCollections.containsKey(fieldId)) {
                enumCollections[fieldId] = ArrayList()
            }
            FieldType.ZonedDateTime -> if (!zonedDateTimeValues.containsKey(fieldId)) {
                zonedDateTimeValues[fieldId] = NullableZonedDateTimeProperty()
            }
            FieldType.Date -> if (!localDateValues.containsKey(fieldId)) {
                localDateValues[fieldId] = ObjectProperty<LocalDate?>(null)
            }
            FieldType.Time -> if (!localTimeValues.containsKey(fieldId)) {
                localTimeValues[fieldId] = NullableLocalTimeProperty()
            }
            FieldType.Duration -> if (!durationValues.containsKey(fieldId)) {
                durationValues[fieldId] = NullableDurationProperty()
            }
            FieldType.MonthDay -> if (!monthDayValues.containsKey(fieldId)) {
                monthDayValues[fieldId] = NullableMonthDayProperty()
            }
            FieldType.YearMonth -> if (!yearMonthValues.containsKey(fieldId)) {
                yearMonthValues[fieldId] = NullableYearMonthProperty()
            }
            FieldType.Period -> if (!periodValues.containsKey(fieldId)) {
                periodValues[fieldId] = NullablePeriodProperty()
            }
            FieldType.DateTime -> if (!localDateTimeValues.containsKey(fieldId)) {
                localDateTimeValues[fieldId] = NullableLocalDateTimeProperty()
            }
            FieldType.Blob -> if (!blobValues.containsKey(fieldId)) {
                blobValues[fieldId] = NullableBlobProperty()
            }
            FieldType.Enum -> if (!enumValues.containsKey(fieldId)) {
                enumValues[fieldId] = ObjectProperty<Enum<*>?>(null)
            }
            FieldType.EnumString -> if (!stringEnumValues.containsKey(fieldId)) {
                stringEnumValues[fieldId] = ObjectProperty<Enum<*>?>(null)
            }
            FieldType.Float -> if (!floatValues.containsKey(fieldId)) {
                floatValues[fieldId] = NullableFloatProperty()
            }
            FieldType.Double -> if (!doubleValues.containsKey(fieldId)) {
                doubleValues[fieldId] = NullableDoubleProperty()
            }
            FieldType.Short -> if (!shortValues.containsKey(fieldId)) {
                shortValues[fieldId] = NullableShortProperty()
            }
            FieldType.Long -> if (!longValues.containsKey(fieldId)) {
                longValues[fieldId] = NullableLongProperty()
            }
            FieldType.Int -> if (!integerValues.containsKey(fieldId)) {
                integerValues[fieldId] = NullableIntegerProperty()
            }
            FieldType.Uuid -> if (!uuidValues.containsKey(fieldId)) {
                uuidValues[fieldId] = NullableUuidProperty()
            }
            FieldType.Boolean -> if (!booleanValues.containsKey(fieldId)) {
                booleanValues[fieldId] = NullableBooleanProperty()
            }
        }

    }

    /**
     * @param dbContext
     * @param fieldId
     * @param entityId
     * @param id
     * @param innerObjects
     * @param compositeKeys
     */
    internal fun populateObjects(
            dbContext: DbContext,
            fieldId: Int?,
            entityId: Int,
            id: String,
            editVersion: Int,
            innerObjects: MutableCollection<Entity>,
            compositeKeys: MutableCollection<CompositeKey>
    ) {
        try {
            if (fieldId == null) return
            objectCollections.filter { it.key.field.id == fieldId }.forEach { kvp ->
                val entity = dbContext.classes[entityId]!!.getDeclaredConstructor().newInstance()
                entity.overview.id = id
                entity.overview.editVersion = editVersion

                if (entity is IEntity) {
                    kvp.value.add(entity)
                }
                innerObjects.add(entity)

                compositeKeys.add(CompositeKey(id, editVersion))
            }
            objectValues.filter { it.key.field.id == fieldId }.forEach {
                it.value.set(dbContext.classes[entityId]!!.getDeclaredConstructor().newInstance())
                it.value.value.overview.id = id
                it.value.value.overview.editVersion = editVersion
                val jdsEntity = it.value.value as Entity
                innerObjects.add(jdsEntity)

                compositeKeys.add(CompositeKey(id, editVersion))
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
            dbContext: DbContext,
            connection: Connection,
            entityId: Int
    ) = try {
        (if (dbContext.supportsStatements) connection.prepareCall(dbContext.populateRefField()) else connection.prepareStatement(dbContext.populateRefField())).use { populateRefField ->
            (if (dbContext.supportsStatements) connection.prepareCall(dbContext.populateRefEntityField()) else connection.prepareStatement(dbContext.populateRefEntityField())).use { populateRefEntityField ->
                (if (dbContext.supportsStatements) connection.prepareCall(dbContext.populateRefFieldEntity()) else connection.prepareStatement(dbContext.populateRefFieldEntity())).use { populateRefFieldEntity ->
                    getFields(overview.entityId).forEach { fieldId ->
                        val field = Field.values[fieldId]!!
                        //1. map this jdsField to the jdsField dictionary
                        populateRefField.setInt(1, field.id)
                        populateRefField.setString(2, field.name)
                        populateRefField.setString(3, field.description)
                        populateRefField.setInt(4, field.type.ordinal)
                        populateRefField.addBatch()
                        //2. map this jdsField ID to the entity type
                        populateRefEntityField.setInt(1, entityId)
                        populateRefEntityField.setInt(2, field.id)
                        populateRefEntityField.addBatch()
                        //3. zzzzzzzzzz
                        if (field.type == FieldType.Entity || field.type == FieldType.EntityCollection) {
                            val fieldEntity = FieldEntity.values[field.id]
                            if (fieldEntity != null) {
                                val entity = getEntityAnnotation(fieldEntity.entity)
                                if (entity != null) {
                                    populateRefFieldEntity.setInt(1, field.id)
                                    populateRefFieldEntity.setInt(2, entity.id)
                                    populateRefFieldEntity.addBatch()
                                }
                            }
                        }
                    }
                    populateRefField.executeBatch()
                    populateRefEntityField.executeBatch()
                    populateRefFieldEntity.executeBatch()
                }
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * Binds all the enumValues attached to an entity
     * @param connection
     * @param entityId
     * @param dbContext
     */
    @Synchronized
    internal fun populateRefEnumRefEntityEnum(
            dbContext: DbContext,
            connection: Connection,
            entityId: Int
    ) {
        populateRefEnum(dbContext, connection, getEnums(overview.entityId))
        populateRefEntityEnum(dbContext, connection, entityId, getEnums(overview.entityId))
        if (dbContext.options.logOutput) {
            System.out.printf("Mapped Enums for Entity[%s]\n", entityId)
        }
    }

    /**
     * Binds all the enumValues attached to an entity
     * @param dbContext
     * @param connection the SQL connection to use for DB operations
     * @param entityId the value representing the entity
     * @param fieldIds the entity's enumValues
     */
    @Synchronized
    private fun populateRefEntityEnum(
            dbContext: DbContext,
            connection: Connection,
            entityId: Int,
            fieldIds: Set<Int>
    ) = try {
        (if (dbContext.supportsStatements) connection.prepareCall(dbContext.populateRefEntityEnum()) else connection.prepareStatement(dbContext.populateRefEntityEnum())).use {
            for (fieldId in fieldIds) {
                val jdsFieldEnum = FieldEnum.enums[fieldId]!!
                it.setInt(1, entityId)
                it.setInt(2, jdsFieldEnum.field.id)
                it.addBatch()
            }
            it.executeBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * Binds all the values attached to an enum
     * @param dbContext
     * @param connection the SQL connection to use for DB operations
     * @param fieldIds the jdsField enum
     */
    @Synchronized
    private fun populateRefEnum(dbContext: DbContext, connection: Connection, fieldIds: Set<Int>) = try {
        (if (dbContext.supportsStatements) connection.prepareCall(dbContext.populateRefEnum()) else connection.prepareStatement(dbContext.populateRefEnum())).use {
            for (fieldId in fieldIds) {
                val jdsFieldEnum = FieldEnum.enums[fieldId]!!
                jdsFieldEnum.values.forEach { enum ->
                    it.setInt(1, jdsFieldEnum.field.id)
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
    fun getReportAtomicValue(fieldId: Int, ordinal: Int): Any? {
        if (localDateTimeValues.containsKey(fieldId)) {
            return localDateTimeValues[fieldId]?.value
        }
        if (zonedDateTimeValues.containsKey(fieldId)) {
            return zonedDateTimeValues[fieldId]?.value
        }
        if (localDateValues.containsKey(fieldId)) {
            return localDateValues[fieldId]?.value
        }
        if (localTimeValues.containsKey(fieldId)) {
            return localTimeValues[fieldId]?.value
        }
        if (monthDayValues.containsKey(fieldId)) {
            return monthDayValues[fieldId]?.value.toString()
        }
        if (yearMonthValues.containsKey(fieldId)) {
            return yearMonthValues[fieldId]?.value.toString()
        }
        if (periodValues.containsKey(fieldId)) {
            return periodValues[fieldId]?.value.toString()
        }
        if (durationValues.containsKey(fieldId)) {
            return durationValues[fieldId]?.value?.toNanos()
        }
        if (stringValues.containsKey(fieldId)) {
            return stringValues[fieldId]?.value
        }
        if (floatValues.containsKey(fieldId)) {
            return floatValues[fieldId]?.value
        }
        if (doubleValues.containsKey(fieldId)) {
            return doubleValues[fieldId]?.value
        }
        if (shortValues.containsKey(fieldId)) {
            return shortValues[fieldId]?.value
        }
        if (booleanValues.containsKey(fieldId)) {
            return booleanValues[fieldId]?.value
        }
        if (longValues.containsKey(fieldId)) {
            return longValues[fieldId]?.value
        }
        if (stringEnumValues.containsKey(fieldId)) {
            return stringEnumValues[fieldId]?.value.toString()
        }
        if (integerValues.containsKey(fieldId)) {
            return integerValues[fieldId]?.value
        }
        if (uuidValues.containsKey(fieldId)) {
            return uuidValues[fieldId]?.value
        }
        if (enumValues.containsKey(fieldId)) {
            return enumValues[fieldId]?.value?.ordinal
        }
        enumCollections.filter { it.key == fieldId }.forEach { it -> it.value.filter { it.ordinal == ordinal }.forEach { _ -> return true } }
        objectValues.filter { it.key.field.id == fieldId }.forEach { return it.value.value.overview.id }
        return null
    }

    /**
     * @param table
     */
    override fun registerFields(table: Table) {
        getFields(overview.entityId).forEach {
            if (!Schema.isIgnoredType(it)) {
                table.registerField(it)
            }
        }
    }

    /**
     * Internal helper function that works with all nested objects
     */
    override fun getNestedEntities(includeThisEntity: Boolean): Sequence<Entity> = sequence {
        if (includeThisEntity) {
            yield(this@Entity)
        }
        objectValues.values.forEach { objectWritableProperty -> yieldAll(objectWritableProperty.value.getNestedEntities()) }
        objectCollections.values.forEach { objectCollection -> objectCollection.forEach { entity -> yieldAll(entity.getNestedEntities()) } }
    }

    /**
     * Internal helper function that works with all nested objects
     */
    override fun getNestedEntities(collection: MutableCollection<Entity>, includeThisEntity: Boolean) {
        if (includeThisEntity) {
            collection.add(this@Entity)
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
        return "JdsEntity(uuid=${overview.id},editVersion=${overview.editVersion},entityId=${overview.entityId})"
    }

    companion object : Externalizable {

        private const val serialVersionUID = 20180106_2125L
        private val allFields = ConcurrentHashMap<Int, LinkedHashSet<Int>>()
        private val allEnums = ConcurrentHashMap<Int, LinkedHashSet<Int>>()

        override fun readExternal(objectInput: ObjectInput) {
            allFields.clear()
            allFields.putAll(objectInput.readObject() as Map<Int, LinkedHashSet<Int>>)
            allEnums.clear()
            allEnums.putAll(objectInput.readObject() as Map<Int, LinkedHashSet<Int>>)
        }

        override fun writeExternal(objectOutput: ObjectOutput) {
            objectOutput.writeObject(allFields)
            objectOutput.writeObject(allEnums)
        }

        internal fun getEntityAnnotation(clazz: Class<*>): EntityAnnotation? {
            val attemptOne = clazz.isAnnotationPresent(EntityAnnotation::class.java)
            if (attemptOne) {
                return clazz.getAnnotation(EntityAnnotation::class.java)
            } else {
                val attemptTwo = clazz.superclass !== null && clazz.superclass.isAnnotationPresent(EntityAnnotation::class.java)
                if (attemptTwo) {
                    clazz.superclass.getAnnotation(EntityAnnotation::class.java)
                }
            }
            return null
        }

        protected fun mapField(entityId: Int, fieldId: Int): Int {
            getFields(entityId).add(fieldId)
            return fieldId
        }

        protected fun mapEnums(entityId: Int, fieldId: Int): Int {
            getEnums(entityId).add(fieldId)
            return fieldId
        }

        private fun toTimeStampCollection(values: MutableCollection<LocalDateTime>) = values.map { Timestamp.valueOf(it) }.toMutableList()

        private fun toIntCollection(values: MutableCollection<Enum<*>>) = values.map { it.ordinal }.toMutableList()

        private fun toStringCollection(values: MutableCollection<Enum<*>>) = values.map { it.name }.toMutableList()

        private fun getFields(entityId: Int) = allFields.getOrPut(entityId) { LinkedHashSet() }

        private fun getEnums(entityId: Int) = allEnums.getOrPut(entityId) { LinkedHashSet() }

        fun WritableProperty<String?>.getOrEmpty(): String = this.value.orEmpty()
    }
}
