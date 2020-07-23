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
import io.github.subiyacryolite.jds.interfaces.IEntity
import io.github.subiyacryolite.jds.interfaces.IOverview
import io.github.subiyacryolite.jds.interfaces.IValue
import java.io.Serializable
import java.sql.Connection
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
    internal val localDateTimeValues: MutableMap<Int, IValue<LocalDateTime?>> = HashMap()

    @get:JsonIgnore
    internal val zonedDateTimeValues: MutableMap<Int, IValue<ZonedDateTime?>> = HashMap()

    @get:JsonIgnore
    internal val localDateValues: MutableMap<Int, IValue<LocalDate?>> = HashMap()

    @get:JsonIgnore
    internal val localTimeValues: MutableMap<Int, IValue<LocalTime?>> = HashMap()

    @get:JsonIgnore
    internal val monthDayValues: MutableMap<Int, IValue<MonthDay?>> = HashMap()

    @get:JsonIgnore
    internal val yearMonthValues: MutableMap<Int, IValue<YearMonth?>> = HashMap()

    @get:JsonIgnore
    internal val periodValues: MutableMap<Int, IValue<Period?>> = HashMap()

    @get:JsonIgnore
    internal val durationValues: MutableMap<Int, IValue<Duration?>> = HashMap()

    //strings
    @get:JsonIgnore
    internal val stringValues: MutableMap<Int, IValue<String?>> = HashMap()

    //boolean
    @get:JsonIgnore
    internal val booleanValues: MutableMap<Int, IValue<Boolean?>> = HashMap()

    //numeric
    @get:JsonIgnore
    internal val shortValues: MutableMap<Int, IValue<Short?>> = HashMap()

    @get:JsonIgnore
    internal val floatValues: MutableMap<Int, IValue<Float?>> = HashMap()

    @get:JsonIgnore
    internal val doubleValues: MutableMap<Int, IValue<Double?>> = HashMap()

    @get:JsonIgnore
    internal val longValues: MutableMap<Int, IValue<Long?>> = HashMap()

    @get:JsonIgnore
    internal val integerValues: MutableMap<Int, IValue<Int?>> = HashMap()

    @get:JsonIgnore
    internal val uuidValues: MutableMap<Int, IValue<UUID?>> = HashMap()

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
    internal val enumValues: MutableMap<Int, IValue<Enum<*>?>> = HashMap()

    @get:JsonIgnore
    internal val stringEnumValues: MutableMap<Int, IValue<Enum<*>?>> = HashMap()

    @get:JsonIgnore
    internal val enumCollections: MutableMap<Int, MutableCollection<Enum<*>>> = HashMap()

    @get:JsonIgnore
    internal val enumStringCollections: MutableMap<Int, MutableCollection<Enum<*>>> = HashMap()

    //objects
    @get:JsonIgnore
    internal val objectValues: MutableMap<FieldEntity<*>, IValue<IEntity>> = HashMap()

    //maps
    @get:JsonIgnore
    internal val mapIntKeyValues: MutableMap<Int, MutableMap<Int, String>> = HashMap()

    @get:JsonIgnore
    internal val mapStringKeyValues: MutableMap<Int, MutableMap<String, String>> = HashMap()

    //blobs
    @get:JsonIgnore
    internal val blobValues: MutableMap<Int, IValue<ByteArray?>> = HashMap()

    init {
        val entityAnnotation = getEntityAnnotation(javaClass)
        if (entityAnnotation != null) {
            overview.entityId = entityAnnotation.id
        } else {
            throw RuntimeException("You must annotate the class [" + javaClass.canonicalName + "] or its immediate parent with [" + EntityAnnotation::class.java + "]")
        }
    }

    @JvmName("mapShort")
    protected fun map(field: Field, value: Short, propertyName: String = "") = map(field, ShortValue(value), propertyName)

    @JvmName("mapShort")
    protected fun map(field: Field, value: IValue<Short>, propertyName: String = ""): IValue<Short> {
        @Suppress("UNCHECKED_CAST")
        map(field, value as IValue<Short?>, propertyName)
        return value
    }

    @JvmName("mapNullableShort")
    protected fun map(field: Field, value: IValue<Short?>, propertyName: String = ""): IValue<Short?> {
        validateShort(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return shortValues.getOrPut(mapField(overview.entityId, field.bind())) { value }
    }

    @JvmName("mapDouble")
    protected fun map(field: Field, value: Double, propertyName: String = "") = map(field, DoubleValue(value), propertyName)

    @JvmName("mapDouble")
    protected fun map(field: Field, value: IValue<Double>, propertyName: String = ""): IValue<Double> {
        @Suppress("UNCHECKED_CAST")
        map(field, value as IValue<Double?>, propertyName)
        return value
    }

    @JvmName("mapNullableDouble")
    protected fun map(field: Field, value: IValue<Double?>, propertyName: String = ""): IValue<Double?> {
        validateDouble(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return doubleValues.getOrPut(mapField(overview.entityId, field.bind())) { value }
    }

    @JvmName("mapInt")
    protected fun map(field: Field, value: Int, propertyName: String = "") = map(field, IntegerValue(value), propertyName)

    @JvmName("mapInt")
    protected fun map(field: Field, value: IValue<Int>, propertyName: String = ""): IValue<Int> {
        @Suppress("UNCHECKED_CAST")
        map(field, value as IValue<Int?>, propertyName)
        return value
    }

    @JvmName("mapNullableInt")
    protected fun map(field: Field, value: IValue<Int?>, propertyName: String = ""): IValue<Int?> {
        validateInt(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return integerValues.getOrPut(mapField(overview.entityId, field.bind())) { value }
    }

    @JvmName("mapLong")
    protected fun map(field: Field, value: Long, propertyName: String = "") = map(field, LongValue(value), propertyName)

    @JvmName("mapLong")
    protected fun map(field: Field, value: IValue<Long>, propertyName: String = ""): IValue<Long> {
        @Suppress("UNCHECKED_CAST")
        map(field, value as IValue<Long?>, propertyName)
        return value
    }

    @JvmName("mapNullableLong")
    protected fun map(field: Field, value: IValue<Long?>, propertyName: String = ""): IValue<Long?> {
        validateLong(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return longValues.getOrPut(mapField(overview.entityId, field.bind())) { value }
    }

    @JvmName("mapFloat")
    protected fun map(field: Field, value: Float, propertyName: String = "") = map(field, FloatValue(value), propertyName)

    @JvmName("mapFloat")
    protected fun map(field: Field, value: IValue<Float>, propertyName: String = ""): IValue<Float> {
        @Suppress("UNCHECKED_CAST")
        map(field, value as IValue<Float?>, propertyName)
        return value
    }

    @JvmName("mapNullableFloat")
    protected fun map(field: Field, value: IValue<Float?>, propertyName: String = ""): IValue<Float?> {
        validateFloat(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return floatValues.getOrPut(mapField(overview.entityId, field.bind())) { value }
    }

    @JvmName("mapBoolean")
    protected fun map(field: Field, value: Boolean, propertyName: String = "") = map(field, BooleanValue(value), propertyName)

    @JvmName("mapBoolean")
    protected fun map(field: Field, value: IValue<Boolean>, propertyName: String = ""): IValue<Boolean> {
        @Suppress("UNCHECKED_CAST")
        map(field, value as IValue<Boolean?>, propertyName)
        return value
    }

    @JvmName("mapNullableBoolean")
    protected fun map(field: Field, value: IValue<Boolean?>, propertyName: String = ""): IValue<Boolean?> {
        validateBoolean(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return booleanValues.getOrPut(mapField(overview.entityId, field.bind())) { value }
    }

    @JvmName("mapUuid")
    protected fun map(field: Field, value: UUID, propertyName: String = "") = map(field, UuidValue(value), propertyName)

    @JvmName("mapUuid")
    protected fun map(field: Field, value: IValue<UUID>, propertyName: String = ""): IValue<UUID> {
        @Suppress("UNCHECKED_CAST")
        map(field, value as IValue<UUID?>, propertyName)
        return value
    }

    @JvmName("mapNullableUuid")
    protected fun map(field: Field, value: IValue<UUID?>, propertyName: String = ""): IValue<UUID?> {
        validateUuid(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return uuidValues.getOrPut(mapField(overview.entityId, field.bind())) { value }
    }

    @JvmName("mapString")
    protected fun map(field: Field, value: String, propertyName: String = "") = map(field, StringValue(value), propertyName)

    @JvmName("mapString")
    protected fun map(field: Field, value: IValue<String>, propertyName: String = ""): IValue<String> {
        @Suppress("UNCHECKED_CAST")
        map(field, value as IValue<String?>, propertyName)
        return value
    }

    @JvmName("mapNullableString")
    protected fun map(field: Field, value: IValue<String?>, propertyName: String = ""): IValue<String?> {
        validateString(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return stringValues.getOrPut(mapField(overview.entityId, field.bind())) { value }
    }

    @JvmName("mapDateTime")
    protected fun map(field: Field, value: LocalDateTime, propertyName: String = "") = map(field, LocalDateTimeValue(value), propertyName)

    @JvmName("mapDateTime")
    protected fun map(field: Field, value: IValue<LocalDateTime>, propertyName: String = ""): IValue<LocalDateTime> {
        @Suppress("UNCHECKED_CAST")
        map(field, value as IValue<LocalDateTime?>, propertyName)
        return value
    }

    @JvmName("mapNullableDateTime")
    protected fun map(field: Field, value: IValue<LocalDateTime?>, propertyName: String = ""): IValue<LocalDateTime?> {
        validateDateTime(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return localDateTimeValues.getOrPut(mapField(overview.entityId, field.bind())) { value }
    }

    @JvmName("mapZonedDateTime")
    protected fun map(field: Field, value: ZonedDateTime, propertyName: String = "") = map(field, ZonedDateTimeValue(value), propertyName)

    @JvmName("mapZonedDateTime")
    protected fun map(field: Field, value: IValue<ZonedDateTime>, propertyName: String = ""): IValue<ZonedDateTime> {
        @Suppress("UNCHECKED_CAST")
        map(field, value as IValue<ZonedDateTime?>, propertyName)
        return value
    }

    @JvmName("mapNullableZonedDateTime")
    protected fun map(field: Field, value: IValue<ZonedDateTime?>, propertyName: String = ""): IValue<ZonedDateTime?> {
        validateZonedDateTime(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return zonedDateTimeValues.getOrPut(mapField(overview.entityId, field.bind())) { value }
    }

    @JvmName("mapDate")
    protected fun map(field: Field, value: LocalDate, propertyName: String = "") = map(field, LocalDateValue(value), propertyName)

    @JvmName("mapDate")
    protected fun map(field: Field, value: IValue<LocalDate>, propertyName: String = ""): IValue<LocalDate> {
        @Suppress("UNCHECKED_CAST")
        map(field, value as IValue<LocalDate?>, propertyName)
        return value
    }

    @JvmName("mapNullableDate")
    protected fun map(field: Field, value: IValue<LocalDate?>, propertyName: String = ""): IValue<LocalDate?> {
        validateDate(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return localDateValues.getOrPut(mapField(overview.entityId, field.bind())) { value }
    }

    @JvmName("mapTime")
    protected fun map(field: Field, value: LocalTime, propertyName: String = "") = map(field, LocalTimeValue(value), propertyName)

    @JvmName("mapTime")
    protected fun map(field: Field, value: IValue<LocalTime>, propertyName: String = ""): IValue<LocalTime> {
        @Suppress("UNCHECKED_CAST")
        map(field, value as IValue<LocalTime?>, propertyName)
        return value
    }

    @JvmName("mapNullableTime")
    protected fun map(field: Field, value: IValue<LocalTime?>, propertyName: String = ""): IValue<LocalTime?> {
        validateTime(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return localTimeValues.getOrPut(mapField(overview.entityId, field.bind())) { value }
    }

    @JvmName("mapBlob")
    protected fun map(field: Field, value: ByteArray, propertyName: String = "") = map(field, BlobValue(value), propertyName)

    @JvmName("mapBlob")
    protected fun map(field: Field, value: IValue<ByteArray>, propertyName: String = ""): IValue<ByteArray> {
        @Suppress("UNCHECKED_CAST")
        map(field, value as IValue<ByteArray?>, propertyName)
        return value
    }

    @JvmName("mapNullableBlob")
    protected fun map(field: Field, value: IValue<ByteArray?>, propertyName: String = ""): IValue<ByteArray?> {
        validateBlob(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return blobValues.getOrPut(mapField(overview.entityId, field.bind())) { value }
    }

    @JvmName("mapMonthDay")
    protected fun map(field: Field, value: MonthDay, propertyName: String = "") = map(field, MonthDayValue(value), propertyName)

    @JvmName("mapMonthDay")
    protected fun map(field: Field, value: IValue<MonthDay>, propertyName: String = ""): IValue<MonthDay> {
        @Suppress("UNCHECKED_CAST")
        map(field, value as IValue<MonthDay?>, propertyName)
        return value
    }

    @JvmName("mapNullableMonthDay")
    protected fun map(field: Field, value: IValue<MonthDay?>, propertyName: String = ""): IValue<MonthDay?> {
        validateMonthDay(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return monthDayValues.getOrPut(mapField(overview.entityId, field.bind())) { value }
    }

    @JvmName("mapYearMonth")
    protected fun map(field: Field, value: YearMonth, propertyName: String = "") = map(field, YearMonthValue(value), propertyName)

    @JvmName("mapYearMonth")
    protected fun map(field: Field, value: IValue<YearMonth>, propertyName: String = ""): IValue<YearMonth> {
        @Suppress("UNCHECKED_CAST")
        map(field, value as IValue<YearMonth?>, propertyName)
        return value
    }

    @JvmName("mapNullableYearMonth")
    protected fun map(field: Field, value: IValue<YearMonth?>, propertyName: String = ""): IValue<YearMonth?> {
        validateYearMonth(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return yearMonthValues.getOrPut(mapField(overview.entityId, field.bind())) { value }
    }

    @JvmName("mapPeriod")
    protected fun map(field: Field, value: Period, propertyName: String = "") = map(field, PeriodValue(value), propertyName)

    @JvmName("mapPeriod")
    protected fun map(field: Field, value: IValue<Period>, propertyName: String = ""): IValue<Period> {
        @Suppress("UNCHECKED_CAST")
        map(field, value as IValue<Period?>, propertyName)
        return value
    }

    @JvmName("mapNullablePeriod")
    protected fun map(field: Field, value: IValue<Period?>, propertyName: String = ""): IValue<Period?> {
        validatePeriod(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return periodValues.getOrPut(mapField(overview.entityId, field.bind())) { value }
    }

    @JvmName("mapDuration")
    protected fun map(field: Field, value: Duration, propertyName: String = "") = map(field, DurationValue(value), propertyName)

    @JvmName("mapDuration")
    protected fun map(field: Field, value: IValue<Duration>, propertyName: String = ""): IValue<Duration> {
        @Suppress("UNCHECKED_CAST")
        map(field, value as IValue<Duration?>, propertyName)
        return value
    }

    @JvmName("mapNullableDuration")
    protected fun map(field: Field, value: IValue<Duration?>, propertyName: String = ""): IValue<Duration?> {
        validateDuration(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return durationValues.getOrPut(mapField(overview.entityId, field.bind())) { value }
    }

    @JvmName("mapNullableEnum")
    protected fun <T : Enum<T>> map(fieldEnum: FieldEnum<T>, value: T, propertyName: String = "") = map(fieldEnum, NullableEnumValue(value), propertyName)

    @JvmName("mapNullableEnum")
    @Suppress("UNCHECKED_CAST")
    protected fun <T : Enum<T>> map(fieldEnum: FieldEnum<T>, value: IValue<T?>, propertyName: String = ""): IValue<T?> {
        validateEnum(fieldEnum.field)
        if (fieldEnum.field.type == FieldType.Enum) {
            enumValues[fieldEnum.field.id] = value as IValue<Enum<*>?>
        } else {
            stringEnumValues[fieldEnum.field.id] = value as IValue<Enum<*>?>
        }
        fieldEnum.field.bind()
        FieldDictionary.addEntityField(overview.entityId, fieldEnum.field.id, propertyName)
        mapField(overview.entityId, fieldEnum.field.id)
        mapEnums(overview.entityId, fieldEnum.field.id)
        return value
    }

    @JvmName("mapEnum")
    @Suppress("UNCHECKED_CAST")
    protected fun <T : Enum<T>> map(fieldEnum: FieldEnum<T>, value: IValue<T>, propertyName: String = ""): IValue<T> {
        validateEnum(fieldEnum.field)
        when (fieldEnum.field.type) {
            FieldType.Enum -> {
                enumValues[fieldEnum.field.id] = value as IValue<Enum<*>?>
            }
            else -> {
                stringEnumValues[fieldEnum.field.id] = value as IValue<Enum<*>?>
            }
        }
        fieldEnum.field.bind()
        FieldDictionary.addEntityField(overview.entityId, fieldEnum.field.id, propertyName)
        mapField(overview.entityId, fieldEnum.field.id)
        mapEnums(overview.entityId, fieldEnum.field.id)
        return value
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
    @Suppress("UNCHECKED_CAST")
    protected fun <T : Enum<T>> map(fieldEnum: FieldEnum<T>, collection: MutableCollection<T>, propertyName: String = ""): MutableCollection<T> {
        if (
                fieldEnum.field.type != FieldType.EnumCollection &&
                fieldEnum.field.type != FieldType.EnumStringCollection
        ) {
            throw RuntimeException("Incorrect type supplied for field [$fieldEnum.field]")
        }
        when (fieldEnum.field.type) {
            FieldType.EnumCollection -> {
                enumCollections[fieldEnum.field.id] = collection as MutableCollection<Enum<*>>
            }
            else -> {
                enumStringCollections[fieldEnum.field.id] = collection as MutableCollection<Enum<*>>
            }
        }
        validateEnumCollection(fieldEnum.field)
        fieldEnum.field.bind()
        FieldDictionary.addEntityField(overview.entityId, fieldEnum.field.id, propertyName)
        mapField(overview.entityId, fieldEnum.field.id)
        mapEnums(overview.entityId, fieldEnum.field.id)
        return collection
    }

    protected fun <T : IEntity> map(fieldEntity: FieldEntity<T>, entity: T, propertyName: String = ""): IValue<T> {
        return map(fieldEntity, ObjectValue(entity), propertyName)
    }

    protected fun <T : IEntity> map(fieldEntity: FieldEntity<T>, value: IValue<T>, propertyName: String = ""): IValue<T> {
        if (fieldEntity.field.type != FieldType.Entity) {
            throw RuntimeException("Please assign the correct type to field [$fieldEntity]")
        }
        if (!objectCollections.containsKey(fieldEntity) && !objectValues.containsKey(fieldEntity)) {
            bindFieldIdToEntity(fieldEntity)
            @Suppress("UNCHECKED_CAST")
            objectValues[fieldEntity] = value as IValue<IEntity>
            FieldDictionary.addEntityField(overview.entityId, fieldEntity.field.id, propertyName)
            mapField(overview.entityId, fieldEntity.field.id)
        } else {
            throw RuntimeException("You can only bind a class to one WritableProperty. This class is already bound to one object or object array")
        }
        return value
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
            @Suppress("UNCHECKED_CAST")
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
    @Suppress("NON_EXHAUSTIVE_WHEN")
    private fun initBackingWritablePropertyIfNotDefined(fieldType: FieldType, fieldId: Int) {
        when (fieldType) {
            FieldType.String -> if (!stringValues.containsKey(fieldId)) {
                stringValues[fieldId] = NullableStringValue()
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
                zonedDateTimeValues[fieldId] = NullableZonedDateTimeValue()
            }
            FieldType.Date -> if (!localDateValues.containsKey(fieldId)) {
                localDateValues[fieldId] = NullableLocalDateValue()
            }
            FieldType.Time -> if (!localTimeValues.containsKey(fieldId)) {
                localTimeValues[fieldId] = NullableLocalTimeValue()
            }
            FieldType.Duration -> if (!durationValues.containsKey(fieldId)) {
                durationValues[fieldId] = NullableDurationValue()
            }
            FieldType.MonthDay -> if (!monthDayValues.containsKey(fieldId)) {
                monthDayValues[fieldId] = NullableMonthDayValue()
            }
            FieldType.YearMonth -> if (!yearMonthValues.containsKey(fieldId)) {
                yearMonthValues[fieldId] = NullableYearMonthValue()
            }
            FieldType.Period -> if (!periodValues.containsKey(fieldId)) {
                periodValues[fieldId] = NullablePeriodValue()
            }
            FieldType.DateTime -> if (!localDateTimeValues.containsKey(fieldId)) {
                localDateTimeValues[fieldId] = NullableLocalDateTimeValue()
            }
            FieldType.Blob -> if (!blobValues.containsKey(fieldId)) {
                blobValues[fieldId] = NullableBlobValue()
            }
            FieldType.Enum -> if (!enumValues.containsKey(fieldId)) {
                enumValues[fieldId] = ObjectValue<Enum<*>?>(null)
            }
            FieldType.EnumString -> if (!stringEnumValues.containsKey(fieldId)) {
                stringEnumValues[fieldId] = ObjectValue<Enum<*>?>(null)
            }
            FieldType.Float -> if (!floatValues.containsKey(fieldId)) {
                floatValues[fieldId] = NullableFloatValue()
            }
            FieldType.Double -> if (!doubleValues.containsKey(fieldId)) {
                doubleValues[fieldId] = NullableDoubleValue()
            }
            FieldType.Short -> if (!shortValues.containsKey(fieldId)) {
                shortValues[fieldId] = NullableShortValue()
            }
            FieldType.Long -> if (!longValues.containsKey(fieldId)) {
                longValues[fieldId] = NullableLongValue()
            }
            FieldType.Int -> if (!integerValues.containsKey(fieldId)) {
                integerValues[fieldId] = NullableIntegerValue()
            }
            FieldType.Uuid -> if (!uuidValues.containsKey(fieldId)) {
                uuidValues[fieldId] = NullableUuidValue()
            }
            FieldType.Boolean -> if (!booleanValues.containsKey(fieldId)) {
                booleanValues[fieldId] = NullableBooleanValue()
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

        getFieldsImp(overview.entityId).forEach { fieldId ->
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
        populateRefEnum(dbContext, connection, getEnumsImp(overview.entityId))
        populateRefEntityEnum(dbContext, connection, overview.entityId, getEnumsImp(overview.entityId))
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

        private val fields = ConcurrentHashMap<Int, LinkedHashSet<Int>>()

        private val enums = ConcurrentHashMap<Int, LinkedHashSet<Int>>()

        internal val classes = ConcurrentHashMap<Int, Class<out Entity>>()

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
                getFieldsImp(entityId).add(fieldId)
            }
            return fieldId
        }

        protected fun mapEnums(entityId: Int, fieldId: Int): Int {
            getEnumsImp(entityId).add(fieldId)
            return fieldId
        }

        private fun getFieldsImp(entityId: Int) = fields.getOrPut(entityId) { LinkedHashSet() }

        private fun getEnumsImp(entityId: Int) = enums.getOrPut(entityId) { LinkedHashSet() }

        /**
         * Public facing method to query the underlying [Field] integer ids (read-only).
         * Only the ids of mapped [Field] entries will appear in this collection
         */
        fun getFields(entityId: Int): Collection<Int> = getFieldsImp(entityId).toSet()

        /**
         * Public facing method to query the underlying [FieldEnum] integer ids (read-only).
         * Only the ids of mapped [FieldEnum] entries will appear in this collection
         */
        fun getEnums(entityId: Int): Collection<Int> = getEnumsImp(entityId).toSet()

        /**
         * Public facing method to query the underlying values.
         * Only mapped [FieldEntity] entries will appear in this collection
         */
        fun findAll(entityIds: Collection<Int>): Collection<Class<out Entity>> {
            return classes.filter { kvp -> entityIds.contains(kvp.key) }.map { kvp -> kvp.value }
        }

    }
}
