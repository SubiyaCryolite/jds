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
import io.github.subiyacryolite.jds.Validate.validateIntMap
import io.github.subiyacryolite.jds.Validate.validateLong
import io.github.subiyacryolite.jds.Validate.validateLongCollection
import io.github.subiyacryolite.jds.Validate.validateMonthDay
import io.github.subiyacryolite.jds.Validate.validatePeriod
import io.github.subiyacryolite.jds.Validate.validateShort
import io.github.subiyacryolite.jds.Validate.validateShortCollection
import io.github.subiyacryolite.jds.Validate.validateString
import io.github.subiyacryolite.jds.Validate.validateStringCollection
import io.github.subiyacryolite.jds.Validate.validateStringMap
import io.github.subiyacryolite.jds.Validate.validateTime
import io.github.subiyacryolite.jds.Validate.validateUuid
import io.github.subiyacryolite.jds.Validate.validateUuidCollection
import io.github.subiyacryolite.jds.Validate.validateYearMonth
import io.github.subiyacryolite.jds.Validate.validateZonedDateTime
import io.github.subiyacryolite.jds.annotations.EntityAnnotation
import io.github.subiyacryolite.jds.beans.property.*
import io.github.subiyacryolite.jds.context.DbContext
import io.github.subiyacryolite.jds.enums.FieldType
import io.github.subiyacryolite.jds.extensions.filterIgnored
import io.github.subiyacryolite.jds.extensions.toByteArray
import io.github.subiyacryolite.jds.portable.*
import io.github.subiyacryolite.jds.utility.DeepCopy
import java.io.Serializable
import java.sql.Connection
import java.sql.Timestamp
import java.time.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

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
    internal val localDateTimeValues: MutableMap<Int, WritableProperty<LocalDateTime?>> = HashMap()

    @get:JsonIgnore
    internal val zonedDateTimeValues: MutableMap<Int, WritableProperty<ZonedDateTime?>> = HashMap()

    @get:JsonIgnore
    internal val localDateValues: MutableMap<Int, WritableProperty<LocalDate?>> = HashMap()

    @get:JsonIgnore
    internal val localTimeValues: MutableMap<Int, WritableProperty<LocalTime?>> = HashMap()

    @get:JsonIgnore
    internal val monthDayValues: MutableMap<Int, WritableProperty<MonthDay?>> = HashMap()

    @get:JsonIgnore
    internal val yearMonthValues: MutableMap<Int, WritableProperty<YearMonth?>> = HashMap()

    @get:JsonIgnore
    internal val periodValues: MutableMap<Int, WritableProperty<Period?>> = HashMap()

    @get:JsonIgnore
    internal val durationValues: MutableMap<Int, WritableProperty<Duration?>> = HashMap()

    //strings
    @get:JsonIgnore
    internal val stringValues: MutableMap<Int, WritableProperty<String?>> = HashMap()

    //boolean
    @get:JsonIgnore
    internal val booleanValues: MutableMap<Int, WritableProperty<Boolean?>> = HashMap()

    //numeric
    @get:JsonIgnore
    internal val shortValues: MutableMap<Int, WritableProperty<Short?>> = HashMap()

    @get:JsonIgnore
    internal val floatValues: MutableMap<Int, WritableProperty<Float?>> = HashMap()

    @get:JsonIgnore
    internal val doubleValues: MutableMap<Int, WritableProperty<Double?>> = HashMap()

    @get:JsonIgnore
    internal val longValues: MutableMap<Int, WritableProperty<Long?>> = HashMap()

    @get:JsonIgnore
    internal val integerValues: MutableMap<Int, WritableProperty<Int?>> = HashMap()

    @get:JsonIgnore
    internal val uuidValues: MutableMap<Int, WritableProperty<UUID?>> = HashMap()

    //collections
    @get:JsonIgnore
    internal val objectCollections: MutableMap<FieldEntity<*>, MutableCollection<IEntity>> = HashMap()

    @get:JsonIgnore
    internal val stringCollections: MutableMap<Int, MutableCollection<String>> = HashMap()

    @get:JsonIgnore
    internal val dateTimeCollections: MutableMap<Int, MutableCollection<LocalDateTime>> = HashMap()

    @get:JsonIgnore
    internal val floatCollections: MutableMap<Int, MutableCollection<Float>> = HashMap()

    @get:JsonIgnore
    internal val doubleCollections: MutableMap<Int, MutableCollection<Double>> = HashMap()

    @get:JsonIgnore
    internal val longCollections: MutableMap<Int, MutableCollection<Long>> = HashMap()

    @get:JsonIgnore
    internal val integerCollections: MutableMap<Int, MutableCollection<Int>> = HashMap()

    @get:JsonIgnore
    internal val shortCollections: MutableMap<Int, MutableCollection<Short>> = HashMap()

    @get:JsonIgnore
    internal val uuidCollections: MutableMap<Int, MutableCollection<UUID>> = HashMap()

    //enums
    @get:JsonIgnore
    internal val enumValues: MutableMap<Int, WritableProperty<Enum<*>?>> = HashMap()

    @get:JsonIgnore
    internal val stringEnumValues: MutableMap<Int, WritableProperty<Enum<*>?>> = HashMap()

    @get:JsonIgnore
    internal val enumCollections: MutableMap<Int, MutableCollection<Enum<*>>> = HashMap()

    @get:JsonIgnore
    internal val enumStringCollections: MutableMap<Int, MutableCollection<Enum<*>>> = HashMap()

    //objects
    @get:JsonIgnore
    internal val objectValues: MutableMap<FieldEntity<*>, WritableProperty<IEntity>> = HashMap()

    //maps
    @get:JsonIgnore
    internal val mapIntKeyValues: MutableMap<Int, MutableMap<Int, String>> = HashMap()

    @get:JsonIgnore
    internal val mapStringKeyValues: MutableMap<Int, MutableMap<String, String>> = HashMap()

    //blobs
    @get:JsonIgnore
    internal val blobValues: MutableMap<Int, WritableProperty<ByteArray?>> = HashMap()

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
    protected fun <T : Enum<T>> map(fieldEnum: FieldEnum<T>, value: T, propertyName: String = "") = map(fieldEnum, NullableEnumProperty(value), propertyName)

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

    @JvmName("mapShorts")
    protected fun map(field: Field, collection: MutableCollection<Short>, propertyName: String = ""): MutableCollection<Short> {
        validateShortCollection(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return shortCollections.getOrPut(mapField(overview.entityId, field.bind())) { collection }
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

    @JvmName("mapIntMap")
    protected fun map(field: Field, map: MutableMap<Int, String>, propertyName: String = ""): MutableMap<Int, String> {
        validateIntMap(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return mapIntKeyValues.getOrPut(mapField(overview.entityId, field.bind())) { map }
    }

    @JvmName("mapStringMap")
    protected fun map(field: Field, map: MutableMap<String, String>, propertyName: String = ""): MutableMap<String, String> {
        validateStringMap(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return mapStringKeyValues.getOrPut(mapField(overview.entityId, field.bind())) { map }
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

    protected fun <T : IEntity> map(fieldEntity: FieldEntity<T>, entity: T, propertyName: String = ""): WritableProperty<T> {
        return map(fieldEntity, ObjectProperty(entity), propertyName)
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
        localDateTimeValues.putAll(DeepCopy.clone(source.localDateTimeValues))

        zonedDateTimeValues.clear()
        zonedDateTimeValues.putAll(DeepCopy.clone(source.zonedDateTimeValues))

        localDateValues.clear()
        localDateValues.putAll(DeepCopy.clone(source.localDateValues))

        localTimeValues.clear()
        localTimeValues.putAll(DeepCopy.clone(source.localTimeValues))

        monthDayValues.clear()
        monthDayValues.putAll(DeepCopy.clone(source.monthDayValues))

        yearMonthValues.clear()
        yearMonthValues.putAll(DeepCopy.clone(source.yearMonthValues))

        periodValues.clear()
        periodValues.putAll(DeepCopy.clone(source.periodValues))

        durationValues.clear()
        durationValues.putAll(DeepCopy.clone(source.durationValues))

        stringValues.clear()
        stringValues.putAll(DeepCopy.clone(source.stringValues))

        booleanValues.clear()
        booleanValues.putAll(DeepCopy.clone(source.booleanValues))

        floatValues.clear()
        floatValues.putAll(DeepCopy.clone(source.floatValues))

        doubleValues.clear()
        doubleValues.putAll(DeepCopy.clone(source.doubleValues))

        shortValues.clear()
        shortValues.putAll(DeepCopy.clone(source.shortValues))

        longValues.clear()
        longValues.putAll(DeepCopy.clone(source.longValues))

        integerValues.clear()
        integerValues.putAll(DeepCopy.clone(source.integerValues))

        uuidValues.clear()
        uuidValues.putAll(DeepCopy.clone(source.uuidValues))

        objectCollections.clear()
        objectCollections.putAll(DeepCopy.clone(source.objectCollections))

        stringCollections.clear()
        stringCollections.putAll(DeepCopy.clone(source.stringCollections))

        dateTimeCollections.clear()
        dateTimeCollections.putAll(DeepCopy.clone(source.dateTimeCollections))

        floatCollections.clear()
        floatCollections.putAll(DeepCopy.clone(source.floatCollections))

        doubleCollections.clear()
        doubleCollections.putAll(DeepCopy.clone(source.doubleCollections))

        longCollections.clear()
        longCollections.putAll(DeepCopy.clone(source.longCollections))

        integerCollections.clear()
        integerCollections.putAll(DeepCopy.clone(source.integerCollections))

        shortCollections.clear()
        shortCollections.putAll(DeepCopy.clone(source.shortCollections))

        uuidCollections.clear()
        uuidCollections.putAll(DeepCopy.clone(source.uuidCollections))

        enumValues.clear()
        enumValues.putAll(DeepCopy.clone(source.enumValues))

        stringEnumValues.clear()
        stringEnumValues.putAll(DeepCopy.clone(source.stringEnumValues))

        enumCollections.clear()
        enumCollections.putAll(DeepCopy.clone(source.enumCollections))

        enumStringCollections.clear()
        enumStringCollections.putAll(DeepCopy.clone(source.enumStringCollections))

        objectValues.clear()
        objectValues.putAll(DeepCopy.clone(source.objectValues))

        blobValues.clear()
        blobValues.putAll(DeepCopy.clone(source.blobValues))

        mapStringKeyValues.clear()
        mapStringKeyValues.putAll(DeepCopy.clone(source.mapStringKeyValues))

        mapIntKeyValues.clear()
        mapIntKeyValues.putAll(DeepCopy.clone(source.mapIntKeyValues))
    }

    internal fun populateProperty(dbContext: DbContext, fieldId: Int, fieldType: FieldType): Boolean {
        if (dbContext.options.ignoreTags.any { tag -> Field.values[fieldId]!!.tags.contains(tag) }) {
            return false
        }
        initBackingWritablePropertyIfNotDefined(fieldType, fieldId)
        return true
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
            FieldType.ShortCollection -> if (!shortCollections.containsKey(fieldId)) {
                shortCollections[fieldId] = ArrayList()
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
                localDateValues[fieldId] = NullableLocalDateProperty()
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
            FieldType.MapIntKey -> if (!mapIntKeyValues.containsKey(fieldId)) {
                mapIntKeyValues[fieldId] = HashMap()
            }
            FieldType.MapStringKey -> if (!mapStringKeyValues.containsKey(fieldId)) {
                mapStringKeyValues[fieldId] = HashMap()
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
            objectCollections.filter { entry ->
                entry.key.field.id == fieldId
            }.forEach { kvp ->
                val entity = dbContext.classes[entityId]!!.getDeclaredConstructor().newInstance()
                entity.overview.id = id
                entity.overview.editVersion = editVersion
                if (entity is IEntity) {
                    kvp.value.add(entity)
                }
                innerObjects.add(entity)
                compositeKeys.add(CompositeKey(id, editVersion))
            }
            objectValues.filter { entry ->
                entry.key.field.id == fieldId
            }.forEach { entry ->
                entry.value.set(dbContext.classes[entityId]!!.getDeclaredConstructor().newInstance())
                entry.value.value.overview.id = id
                entry.value.value.overview.editVersion = editVersion
                val jdsEntity = entry.value.value as Entity
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
     */
    internal fun populateRefFieldRefEntityField(
            dbContext: DbContext,
            connection: Connection
    ) = try {

        val clearFieldTag = connection.prepareStatement("DELETE FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.FieldTag)} WHERE field_id = ?")
        val clearFieldAlternateCode = connection.prepareStatement("DELETE FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.FieldAlternateCode)} WHERE field_id = ?")
        val populateField = dbContext.getCallOrStatement(connection, dbContext.populateField())
        val populateEntityField = dbContext.getCallOrStatement(connection, dbContext.populateEntityField())
        val populateFieldEntity = dbContext.getCallOrStatement(connection, dbContext.populateFieldEntity())
        val populateFieldTag = dbContext.getCallOrStatement(connection, dbContext.populateFieldTag())
        val populateFieldAlternateCode = dbContext.getCallOrStatement(connection, dbContext.populateFieldAlternateCode())

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

            populateEntityField.setInt(1, overview.entityId)
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

        clearFieldTag.use { it.executeBatch() }
        clearFieldAlternateCode.use { it.executeBatch() }
        populateField.use { it.executeBatch() }
        populateEntityField.use { it.executeBatch() }
        populateFieldTag.use { it.executeBatch() }
        populateFieldAlternateCode.use { it.executeBatch() }
        populateFieldEntity.use { it.executeBatch() }

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
            connection: Connection
    ) {
        populateRefEnum(dbContext, connection, getEnums(overview.entityId))
        populateRefEntityEnum(dbContext, connection, overview.entityId, getEnums(overview.entityId))
        if (dbContext.options.logOutput) {
            System.out.printf("Mapped Enums for Entity[%s]\n", overview.entityId)
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
        (if (dbContext.supportsStatements) connection.prepareCall(dbContext.populateEntityEnum()) else connection.prepareStatement(dbContext.populateEntityEnum())).use { statement ->
            for (fieldId in fieldIds) {
                val jdsFieldEnum = FieldEnum.enums[fieldId]!!
                statement.setInt(1, entityId)
                statement.setInt(2, jdsFieldEnum.field.id)
                statement.addBatch()
            }
            statement.executeBatch()
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
    private fun populateRefEnum(
            dbContext: DbContext,
            connection: Connection,
            fieldIds: Set<Int>
    ) = try {
        (if (dbContext.supportsStatements) connection.prepareCall(dbContext.populateEnum()) else connection.prepareStatement(dbContext.populateEnum())).use { statement ->
            for (fieldId in fieldIds) {
                val jdsFieldEnum = FieldEnum.enums[fieldId]!!
                jdsFieldEnum.values.forEach { enum ->
                    statement.setInt(1, jdsFieldEnum.field.id)
                    statement.setInt(2, enum.ordinal)
                    statement.setString(3, enum.name)
                    statement.setString(4, enum.toString())
                    statement.addBatch()
                }
            }
            statement.executeBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    companion object : Serializable {

        private const val serialVersionUID = 20180106_2125L

        private val allFields = ConcurrentHashMap<Int, LinkedHashSet<Int>>()

        private val allEnums = ConcurrentHashMap<Int, LinkedHashSet<Int>>()

        internal var initialising: Boolean = false

        internal fun getEntityAnnotation(clazz: Class<*>?): EntityAnnotation? {

            var interfaceAnnotation: EntityAnnotation? = null
            var classHit: EntityAnnotation? = null

            clazz?.interfaces?.forEach { clazzInterface ->
                val interfaceMatch = getEntityAnnotation(clazzInterface)
                if (interfaceMatch != null) {
                    interfaceAnnotation = interfaceMatch
                    return@forEach
                }
            }

            clazz?.declaredAnnotations?.forEach { annotation ->
                if (annotation is EntityAnnotation) {
                    classHit = annotation
                    return@forEach
                }
            }

            if (classHit != null) {
                return classHit//prioritise classes
            } else if (interfaceAnnotation != null) {
                return interfaceAnnotation//then interfaces
            }
            return null
        }

        protected fun mapField(entityId: Int, fieldId: Int): Int {
            if (initialising) {
                getFields(entityId).add(fieldId)
            }
            return fieldId
        }

        protected fun mapEnums(entityId: Int, fieldId: Int): Int {
            getEnums(entityId).add(fieldId)
            return fieldId
        }

        private fun getFields(entityId: Int) = allFields.getOrPut(entityId) { LinkedHashSet() }

        private fun getEnums(entityId: Int) = allEnums.getOrPut(entityId) { LinkedHashSet() }
    }
}
