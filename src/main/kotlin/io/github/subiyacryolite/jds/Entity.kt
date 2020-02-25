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
import io.github.subiyacryolite.jds.Validate.validateEnumCollection
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
import io.github.subiyacryolite.jds.Validate.validateUuidCollection
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
abstract class Entity : IEntity {
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
    @get:JsonIgnore
    internal val uuidCollections: HashMap<Int, MutableCollection<UUID>> = HashMap()
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
    protected fun map(field: Field, value: Short, propertyName: String = "") = map(field, ShortProperty(value), propertyName)

    @JvmName("mapShort")
    protected fun map(field: Field, property: WritableProperty<Short>, propertyName: String = ""): WritableProperty<Short> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as WritableProperty<Short?>, propertyName)
        return property
    }

    @JvmName("mapNullableShort")
    protected fun map(field: Field, property: WritableProperty<Short?>, propertyName: String = ""): WritableProperty<Short?> {
        validateShort(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return shortValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapDouble")
    protected fun map(field: Field, value: Double, propertyName: String = "") = map(field, DoubleProperty(value), propertyName)

    @JvmName("mapDouble")
    protected fun map(field: Field, property: WritableProperty<Double>, propertyName: String = ""): WritableProperty<Double> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as WritableProperty<Double?>, propertyName)
        return property
    }

    @JvmName("mapNullableDouble")
    protected fun map(field: Field, property: WritableProperty<Double?>, propertyName: String = ""): WritableProperty<Double?> {
        validateDouble(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return doubleValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapInt")
    protected fun map(field: Field, value: Int, propertyName: String = "") = map(field, IntegerProperty(value), propertyName)

    @JvmName("mapInt")
    protected fun map(field: Field, property: WritableProperty<Int>, propertyName: String = ""): WritableProperty<Int> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as WritableProperty<Int?>, propertyName)
        return property
    }

    @JvmName("mapNullableInt")
    protected fun map(field: Field, property: WritableProperty<Int?>, propertyName: String = ""): WritableProperty<Int?> {
        validateInt(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return integerValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapLong")
    protected fun map(field: Field, value: Long, propertyName: String = "") = map(field, LongProperty(value), propertyName)

    @JvmName("mapLong")
    protected fun map(field: Field, property: WritableProperty<Long>, propertyName: String = ""): WritableProperty<Long> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as WritableProperty<Long?>, propertyName)
        return property
    }

    @JvmName("mapNullableLong")
    protected fun map(field: Field, property: WritableProperty<Long?>, propertyName: String = ""): WritableProperty<Long?> {
        validateLong(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return longValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapFloat")
    protected fun map(field: Field, value: Float, propertyName: String = "") = map(field, FloatProperty(value), propertyName)

    @JvmName("mapFloat")
    protected fun map(field: Field, property: WritableProperty<Float>, propertyName: String = ""): WritableProperty<Float> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as WritableProperty<Float?>, propertyName)
        return property
    }

    @JvmName("mapNullableFloat")
    protected fun map(field: Field, property: WritableProperty<Float?>, propertyName: String = ""): WritableProperty<Float?> {
        validateFloat(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return floatValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapBoolean")
    protected fun map(field: Field, value: Boolean, propertyName: String = "") = map(field, BooleanProperty(value), propertyName)

    @JvmName("mapBoolean")
    protected fun map(field: Field, property: WritableProperty<Boolean>, propertyName: String = ""): WritableProperty<Boolean> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as WritableProperty<Boolean?>, propertyName)
        return property
    }

    @JvmName("mapNullableBoolean")
    protected fun map(field: Field, property: WritableProperty<Boolean?>, propertyName: String = ""): WritableProperty<Boolean?> {
        validateBoolean(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return booleanValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapUuid")
    protected fun map(field: Field, value: UUID, propertyName: String = "") = map(field, UuidProperty(value), propertyName)

    @JvmName("mapUuid")
    protected fun map(field: Field, property: WritableProperty<UUID>, propertyName: String = ""): WritableProperty<UUID> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as WritableProperty<UUID?>, propertyName)
        return property
    }

    @JvmName("mapNullableUuid")
    protected fun map(field: Field, property: WritableProperty<UUID?>, propertyName: String = ""): WritableProperty<UUID?> {
        validateUuid(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return uuidValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapString")
    protected fun map(field: Field, value: String, propertyName: String = "") = map(field, StringProperty(value), propertyName)

    @JvmName("mapString")
    protected fun map(field: Field, property: WritableProperty<String>, propertyName: String = ""): WritableProperty<String> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as WritableProperty<String?>, propertyName)
        return property
    }

    @JvmName("mapNullableString")
    protected fun map(field: Field, property: WritableProperty<String?>, propertyName: String = ""): WritableProperty<String?> {
        validateString(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return stringValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapDateTime")
    protected fun map(field: Field, value: LocalDateTime, propertyName: String = "") = map(field, LocalDateTimeProperty(value), propertyName)

    @JvmName("mapDateTime")
    protected fun map(field: Field, property: WritableProperty<LocalDateTime>, propertyName: String = ""): WritableProperty<LocalDateTime> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as WritableProperty<LocalDateTime?>, propertyName)
        return property
    }

    @JvmName("mapNullableDateTime")
    protected fun map(field: Field, property: WritableProperty<LocalDateTime?>, propertyName: String = ""): WritableProperty<LocalDateTime?> {
        validateDateTime(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return localDateTimeValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapZonedDateTime")
    protected fun map(field: Field, value: ZonedDateTime, propertyName: String = "") = map(field, ZonedDateTimeProperty(value), propertyName)

    @JvmName("mapZonedDateTime")
    protected fun map(field: Field, property: WritableProperty<ZonedDateTime>, propertyName: String = ""): WritableProperty<ZonedDateTime> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as WritableProperty<ZonedDateTime?>, propertyName)
        return property
    }

    @JvmName("mapNullableZonedDateTime")
    protected fun map(field: Field, property: WritableProperty<ZonedDateTime?>, propertyName: String = ""): WritableProperty<ZonedDateTime?> {
        validateZonedDateTime(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return zonedDateTimeValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapDate")
    protected fun map(field: Field, value: LocalDate, propertyName: String = "") = map(field, LocalDateProperty(value), propertyName)

    @JvmName("mapDate")
    protected fun map(field: Field, property: WritableProperty<LocalDate>, propertyName: String = ""): WritableProperty<LocalDate> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as WritableProperty<LocalDate?>, propertyName)
        return property
    }

    @JvmName("mapNullableDate")
    protected fun map(field: Field, property: WritableProperty<LocalDate?>, propertyName: String = ""): WritableProperty<LocalDate?> {
        validateDate(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return localDateValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapTime")
    protected fun map(field: Field, value: LocalTime, propertyName: String = "") = map(field, LocalTimeProperty(value), propertyName)

    @JvmName("mapTime")
    protected fun map(field: Field, property: WritableProperty<LocalTime>, propertyName: String = ""): WritableProperty<LocalTime> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as WritableProperty<LocalTime?>, propertyName)
        return property
    }

    @JvmName("mapNullableTime")
    protected fun map(field: Field, property: WritableProperty<LocalTime?>, propertyName: String = ""): WritableProperty<LocalTime?> {
        validateTime(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return localTimeValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapBlob")
    protected fun map(field: Field, value: ByteArray, propertyName: String = "") = map(field, BlobProperty(value), propertyName)

    @JvmName("mapBlob")
    protected fun map(field: Field, property: WritableProperty<ByteArray>, propertyName: String = ""): WritableProperty<ByteArray> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as WritableProperty<ByteArray?>, propertyName)
        return property
    }

    @JvmName("mapNullableBlob")
    protected fun map(field: Field, property: WritableProperty<ByteArray?>, propertyName: String = ""): WritableProperty<ByteArray?> {
        validateBlob(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return blobValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapMonthDay")
    protected fun map(field: Field, value: MonthDay, propertyName: String = "") = map(field, MonthDayProperty(value), propertyName)

    @JvmName("mapMonthDay")
    protected fun map(field: Field, property: WritableProperty<MonthDay>, propertyName: String = ""): WritableProperty<MonthDay> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as WritableProperty<MonthDay?>, propertyName)
        return property
    }

    @JvmName("mapNullableMonthDay")
    protected fun map(field: Field, property: WritableProperty<MonthDay?>, propertyName: String = ""): WritableProperty<MonthDay?> {
        validateMonthDay(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return monthDayValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapYearMonth")
    protected fun map(field: Field, value: YearMonth, propertyName: String = "") = map(field, YearMonthProperty(value), propertyName)

    @JvmName("mapYearMonth")
    protected fun map(field: Field, property: WritableProperty<YearMonth>, propertyName: String = ""): WritableProperty<YearMonth> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as WritableProperty<YearMonth?>, propertyName)
        return property
    }

    @JvmName("mapNullableYearMonth")
    protected fun map(field: Field, property: WritableProperty<YearMonth?>, propertyName: String = ""): WritableProperty<YearMonth?> {
        validateYearMonth(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return yearMonthValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapPeriod")
    protected fun map(field: Field, value: Period, propertyName: String = "") = map(field, PeriodProperty(value), propertyName)

    @JvmName("mapPeriod")
    protected fun map(field: Field, property: WritableProperty<Period>, propertyName: String = ""): WritableProperty<Period> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as WritableProperty<Period?>, propertyName)
        return property
    }

    @JvmName("mapNullablePeriod")
    protected fun map(field: Field, property: WritableProperty<Period?>, propertyName: String = ""): WritableProperty<Period?> {
        validatePeriod(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return periodValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapDuration")
    protected fun map(field: Field, value: Duration, propertyName: String = "") = map(field, DurationProperty(value), propertyName)

    @JvmName("mapDuration")
    protected fun map(field: Field, property: WritableProperty<Duration>, propertyName: String = ""): WritableProperty<Duration> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as WritableProperty<Duration?>, propertyName)
        return property
    }

    @JvmName("mapNullableDuration")
    protected fun map(field: Field, property: WritableProperty<Duration?>, propertyName: String = ""): WritableProperty<Duration?> {
        validateDuration(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return durationValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapNullableEnum")
    protected fun <T : Enum<T>> map(fieldEnum: FieldEnum<T>, value: T, propertyName: String = "") = map(fieldEnum, EnumProperty(value), propertyName)

    @JvmName("mapNullableEnum")
    protected fun <T : Enum<T>> map(fieldEnum: FieldEnum<T>, property: WritableProperty<T?>, propertyName: String = ""): WritableProperty<T?> {
        validateEnum(fieldEnum.field)
        if (fieldEnum.field.type == FieldType.Enum) {
            enumValues[fieldEnum.field.id] = property as WritableProperty<Enum<*>?>
        } else {
            stringEnumValues[fieldEnum.field.id] = property as WritableProperty<Enum<*>?>
        }
        fieldEnum.field.bind()
        FieldDictionary.addEntityField(overview.entityId, fieldEnum.field.id, propertyName)
        mapField(overview.entityId, fieldEnum.field.id)
        mapEnums(overview.entityId, fieldEnum.field.id)
        return property
    }

    @JvmName("mapEnum")
    protected fun <T : Enum<T>> map(fieldEnum: FieldEnum<T>, property: WritableProperty<T>, propertyName: String = ""): WritableProperty<T> {
        validateEnum(fieldEnum.field)
        if (fieldEnum.field.type == FieldType.Enum) {
            enumValues[fieldEnum.field.id] = property as WritableProperty<Enum<*>?>
        } else {
            stringEnumValues[fieldEnum.field.id] = property as WritableProperty<Enum<*>?>
        }
        fieldEnum.field.bind()
        FieldDictionary.addEntityField(overview.entityId, fieldEnum.field.id, propertyName)
        mapField(overview.entityId, fieldEnum.field.id)
        mapEnums(overview.entityId, fieldEnum.field.id)
        return property
    }

    @JvmName("mapStrings")
    protected fun map(field: Field, collection: MutableCollection<String>, propertyName: String = ""): MutableCollection<String> {
        validateStringCollection(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return stringCollections.getOrPut(mapField(overview.entityId, field.bind())) { collection }
    }

    @JvmName("mapDateTimes")
    protected fun map(field: Field, collection: MutableCollection<LocalDateTime>, propertyName: String = ""): MutableCollection<LocalDateTime> {
        validateDateTimeCollection(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return dateTimeCollections.getOrPut(mapField(overview.entityId, field.bind())) { collection }
    }

    @JvmName("mapFloats")
    protected fun map(field: Field, collection: MutableCollection<Float>, propertyName: String = ""): MutableCollection<Float> {
        validateFloatCollection(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return floatCollections.getOrPut(mapField(overview.entityId, field.bind())) { collection }
    }

    @JvmName("mapIntegers")
    protected fun map(field: Field, collection: MutableCollection<Int>, propertyName: String = ""): MutableCollection<Int> {
        validateIntCollection(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return integerCollections.getOrPut(mapField(overview.entityId, field.bind())) { collection }
    }

    @JvmName("mapUuids")
    protected fun map(field: Field, collection: MutableCollection<UUID>, propertyName: String = ""): MutableCollection<UUID> {
        validateUuidCollection(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return uuidCollections.getOrPut(mapField(overview.entityId, field.bind())) { collection }
    }

    @JvmName("mapDoubles")
    protected fun map(field: Field, collection: MutableCollection<Double>, propertyName: String = ""): MutableCollection<Double> {
        validateDoubleCollection(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return doubleCollections.getOrPut(mapField(overview.entityId, field.bind())) { collection }
    }

    @JvmName("mapLongs")
    protected fun map(field: Field, collection: MutableCollection<Long>, propertyName: String = ""): MutableCollection<Long> {
        validateLongCollection(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return longCollections.getOrPut(mapField(overview.entityId, field.bind())) { collection }
    }

    @JvmName("mapEnums")
    protected fun <T : Enum<T>> map(fieldEnum: FieldEnum<T>, collection: MutableCollection<T>, propertyName: String = ""): MutableCollection<T> {
        if (fieldEnum.field.type != FieldType.EnumCollection && fieldEnum.field.type != FieldType.EnumStringCollection) {
            throw RuntimeException("Incorrect type supplied for field [$fieldEnum.field]")
        }
        if (fieldEnum.field.type == FieldType.EnumCollection) {
            enumCollections[fieldEnum.field.id] = collection as MutableCollection<Enum<*>>
        } else {
            enumStringCollections[fieldEnum.field.id] = collection as MutableCollection<Enum<*>>
        }
        validateEnumCollection(fieldEnum.field)
        fieldEnum.field.bind()
        FieldDictionary.addEntityField(overview.entityId, fieldEnum.field.id, propertyName)
        mapField(overview.entityId, fieldEnum.field.id)
        mapEnums(overview.entityId, fieldEnum.field.id)
        return collection
    }

    protected fun <T : IEntity> map(fieldEntity: FieldEntity<T>, entity: T): WritableProperty<T> {
        return map(fieldEntity, ObjectProperty(entity))
    }

    protected fun <T : IEntity> map(fieldEntity: FieldEntity<T>, property: WritableProperty<T>, propertyName: String = ""): WritableProperty<T> {
        if (fieldEntity.field.type != FieldType.Entity) {
            throw RuntimeException("Please assign the correct type to field [$fieldEntity]")
        }
        if (!objectCollections.containsKey(fieldEntity) && !objectValues.containsKey(fieldEntity)) {
            bindFieldIdToEntity(fieldEntity)
            objectValues[fieldEntity] = property as WritableProperty<IEntity>
            FieldDictionary.addEntityField(overview.entityId, fieldEntity.field.id, propertyName)
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
    protected fun <T : IEntity> map(fieldEntity: FieldEntity<T>, collection: MutableCollection<T>, propertyName: String = ""): MutableCollection<T> {
        if (fieldEntity.field.type != FieldType.EntityCollection) {
            throw RuntimeException("Please supply a valid type for JdsFieldEntity")
        }
        if (!objectCollections.containsKey(fieldEntity)) {
            bindFieldIdToEntity(fieldEntity)
            objectCollections[fieldEntity] = collection as MutableCollection<IEntity>
            FieldDictionary.addEntityField(overview.entityId, fieldEntity.field.id, propertyName)
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

        uuidCollections.clear()
        uuidCollections.putAll(DeepCopy.clone(source.uuidCollections)!!)

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
        booleanValues.filterIgnored(dbContext).forEach {
            val input = when (it.value.value) {
                true -> 1
                false -> 0
                else -> null
            }
            jdsPortableEntity.booleanValues.add(StoreBoolean(it.key, input))
        }
        stringValues.filterIgnored(dbContext).forEach { jdsPortableEntity.stringValues.add(StoreString(it.key, it.value.value)) }
        floatValues.filterIgnored(dbContext).forEach { jdsPortableEntity.floatValue.add(StoreFloat(it.key, it.value.value)) }
        doubleValues.filterIgnored(dbContext).forEach { jdsPortableEntity.doubleValues.add(StoreDouble(it.key, it.value.value)) }
        shortValues.filterIgnored(dbContext).forEach { jdsPortableEntity.shortValues.add(StoreShort(it.key, it.value.value)) }
        longValues.filterIgnored(dbContext).forEach { jdsPortableEntity.longValues.add(StoreLong(it.key, it.value.value)) }
        integerValues.filterIgnored(dbContext).forEach { jdsPortableEntity.integerValues.add(StoreInteger(it.key, it.value.value)) }
        uuidValues.filterIgnored(dbContext).forEach { jdsPortableEntity.uuidValues.add(StoreUuid(it.key, it.value.value.toByteArray())) }
        //==============================================
        //Dates & Time
        //==============================================
        zonedDateTimeValues.filterIgnored(dbContext).forEach { jdsPortableEntity.zonedDateTimeValues.add(StoreZonedDateTime(it.key, (it.value.value as ZonedDateTime?)?.toInstant()?.toEpochMilli())) }
        localTimeValues.filterIgnored(dbContext).forEach { jdsPortableEntity.timeValues.add(StoreTime(it.key, (it.value.value as LocalTime?)?.toNanoOfDay())) }
        durationValues.filterIgnored(dbContext).forEach { jdsPortableEntity.durationValues.add(StoreDuration(it.key, it.value.value?.toNanos())) }
        localDateTimeValues.filterIgnored(dbContext).forEach { jdsPortableEntity.dateTimeValues.add(StoreDateTime(it.key, safeLocalDateTime(it.value.value))) }
        localDateValues.filterIgnored(dbContext).forEach { jdsPortableEntity.dateValues.add(StoreDate(it.key, safeLocalDate(it.value.value))) }
        monthDayValues.filterIgnored(dbContext).forEach { jdsPortableEntity.monthDayValues.add(StoreMonthDay(it.key, it.value.value?.toString())) }
        yearMonthValues.filterIgnored(dbContext).forEach { jdsPortableEntity.yearMonthValues.add(StoreYearMonth(it.key, (it.value.value as YearMonth?)?.toString())) }
        periodValues.filterIgnored(dbContext).forEach { jdsPortableEntity.periodValues.add(StorePeriod(it.key, it.value.value?.toString())) }
        //==============================================
        //BLOB
        //==============================================
        blobValues.filterIgnored(dbContext).forEach {
            jdsPortableEntity.blobValues.add(StoreBlob(it.key, it.value.value ?: ByteArray(0)))
        }
        //==============================================
        //Enums
        //==============================================
        enumValues.filterIgnored(dbContext).forEach { jdsPortableEntity.enumValues.add(StoreEnum(it.key, it.value.value?.ordinal)) }
        stringEnumValues.filterIgnored(dbContext).forEach { jdsPortableEntity.enumStringValues.add(StoreEnumString(it.key, it.value.value?.name)) }
        enumCollections.filterIgnored(dbContext).forEach { jdsPortableEntity.enumCollections.add(StoreEnumCollection(it.key, toIntCollection(it.value))) }
        enumStringCollections.filterIgnored(dbContext).forEach { jdsPortableEntity.enumStringCollections.add(StoreEnumStringCollection(it.key, toStringCollection(it.value))) }
        //==============================================
        //ARRAYS
        //==============================================
        stringCollections.filterIgnored(dbContext).forEach { jdsPortableEntity.stringCollections.add(StoreStringCollection(it.key, it.value)) }
        dateTimeCollections.filterIgnored(dbContext).forEach { jdsPortableEntity.dateTimeCollection.add(StoreDateTimeCollection(it.key, toTimeStampCollection(it.value))) }
        floatCollections.filterIgnored(dbContext).forEach { jdsPortableEntity.floatCollections.add(StoreFloatCollection(it.key, it.value)) }
        doubleCollections.filterIgnored(dbContext).forEach { jdsPortableEntity.doubleCollections.add(StoreDoubleCollection(it.key, it.value)) }
        longCollections.filterIgnored(dbContext).forEach { jdsPortableEntity.longCollections.add(StoreLongCollection(it.key, it.value)) }
        integerCollections.filterIgnored(dbContext).forEach { jdsPortableEntity.integerCollections.add(StoreIntegerCollection(it.key, it.value)) }
        uuidCollections.filterIgnored(dbContext).forEach { jdsPortableEntity.uuidCollections.add(StoreUuidCollection(it.key, toByteArrayCollection(it.value))) }
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

    private fun toByteArrayCollection(values: MutableCollection<UUID>): MutableCollection<ByteArray> {
        val output = ArrayList<ByteArray>()
        values.forEach { value ->
            output.add(value.toByteArray()!!)
        }
        return output
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

        if (dbContext.options.ignoreTags.any { tag -> Field.values[fieldId]!!.tags.contains(tag) }) {
            return
        }

        initBackingWritablePropertyIfNotDefined(fieldType, fieldId)

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
            FieldType.UuidCollection -> {
                val uuid = when (value) {
                    is ByteArray -> value.toUuid()!!
                    is String -> UUID.fromString(value)
                    is UUID -> value
                    else -> UUID.fromString("00000000-0000-0000-0000-000000000000")
                }
                uuidCollections[fieldId]?.add(uuid)
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
            FieldType.UuidCollection -> if (!uuidCollections.containsKey(fieldId)) {
                uuidCollections[fieldId] = ArrayList()
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
        connection.prepareStatement("DELETE FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.FieldTag)} WHERE field_id = ?").use { clearFieldTag ->
            connection.prepareStatement("DELETE FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.FieldAlternateCode)} WHERE field_id = ?").use { clearFieldAlternateCode ->
                dbContext.getCallOrStatement(connection, dbContext.populateField()).use { populateField ->
                    dbContext.getCallOrStatement(connection, dbContext.populateEntityField()).use { populateEntityField ->
                        dbContext.getCallOrStatement(connection, dbContext.populateFieldEntity()).use { populateFieldEntity ->
                            dbContext.getCallOrStatement(connection, dbContext.populateFieldTag()).use { populateFieldTag ->
                                dbContext.getCallOrStatement(connection, dbContext.populateFieldAlternateCode()).use { populateFieldAlternateCode ->
                                    getFields(overview.entityId).forEach { fieldId ->
                                        val field = Field.values.getValue(fieldId)

                                        clearFieldTag.setInt(1, field.id)
                                        clearFieldTag.addBatch()

                                        clearFieldAlternateCode.setInt(1, field.id)
                                        clearFieldAlternateCode.addBatch()

                                        populateField.setInt(1, field.id)
                                        populateField.setString(2, field.name)
                                        populateField.setString(3, field.description)
                                        populateField.setInt(4, field.type.ordinal)
                                        populateField.addBatch()

                                        populateEntityField.setInt(1, entityId)
                                        populateEntityField.setInt(2, field.id)
                                        populateEntityField.addBatch()

                                        field.tags.forEach { tag ->
                                            populateFieldTag.setInt(1, field.id)
                                            populateFieldTag.setString(2, tag)
                                            populateFieldTag.addBatch()
                                        }

                                        field.alternateCodes.forEach { (alternateCode, value) ->
                                            populateFieldAlternateCode.setInt(1, field.id)
                                            populateFieldAlternateCode.setString(2, alternateCode)
                                            populateFieldAlternateCode.setString(3, value)
                                            populateFieldAlternateCode.addBatch()
                                        }

                                        if (field.type == FieldType.Entity || field.type == FieldType.EntityCollection) {
                                            val fieldEntity = FieldEntity.values[field.id]
                                            if (fieldEntity != null) {
                                                val entityAnnotation = getEntityAnnotation(fieldEntity.entity)
                                                if (entityAnnotation != null) {
                                                    populateFieldEntity.setInt(1, field.id)
                                                    populateFieldEntity.setInt(2, entityAnnotation.id)
                                                    populateFieldEntity.addBatch()
                                                }
                                            }
                                        }
                                    }
                                    clearFieldTag.executeBatch()
                                    clearFieldAlternateCode.executeBatch()
                                    populateField.executeBatch()
                                    populateEntityField.executeBatch()
                                    populateFieldTag.executeBatch()
                                    populateFieldAlternateCode.executeBatch()
                                    populateFieldEntity.executeBatch()
                                }
                            }
                        }
                    }
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
        (if (dbContext.supportsStatements) connection.prepareCall(dbContext.populateEntityEnum()) else connection.prepareStatement(dbContext.populateEntityEnum())).use {
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
        (if (dbContext.supportsStatements) connection.prepareCall(dbContext.populateEnum()) else connection.prepareStatement(dbContext.populateEnum())).use {
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

        internal var initialising: Boolean = false

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
            if (Entity.initialising) {
                getFields(entityId).add(fieldId)
            }
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
