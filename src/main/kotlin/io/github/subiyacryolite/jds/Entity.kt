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
import io.github.subiyacryolite.jds.interfaces.IProperty
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
    internal val localDateTimeValues: MutableMap<Int, IProperty<LocalDateTime?>> = HashMap()

    @get:JsonIgnore
    internal val zonedDateTimeValues: MutableMap<Int, IProperty<ZonedDateTime?>> = HashMap()

    @get:JsonIgnore
    internal val localDateValues: MutableMap<Int, IProperty<LocalDate?>> = HashMap()

    @get:JsonIgnore
    internal val localTimeValues: MutableMap<Int, IProperty<LocalTime?>> = HashMap()

    @get:JsonIgnore
    internal val monthDayValues: MutableMap<Int, IProperty<MonthDay?>> = HashMap()

    @get:JsonIgnore
    internal val yearMonthValues: MutableMap<Int, IProperty<YearMonth?>> = HashMap()

    @get:JsonIgnore
    internal val periodValues: MutableMap<Int, IProperty<Period?>> = HashMap()

    @get:JsonIgnore
    internal val durationValues: MutableMap<Int, IProperty<Duration?>> = HashMap()

    //strings
    @get:JsonIgnore
    internal val stringValues: MutableMap<Int, IProperty<String?>> = HashMap()

    //boolean
    @get:JsonIgnore
    internal val booleanValues: MutableMap<Int, IProperty<Boolean?>> = HashMap()

    //numeric
    @get:JsonIgnore
    internal val shortValues: MutableMap<Int, IProperty<Short?>> = HashMap()

    @get:JsonIgnore
    internal val floatValues: MutableMap<Int, IProperty<Float?>> = HashMap()

    @get:JsonIgnore
    internal val doubleValues: MutableMap<Int, IProperty<Double?>> = HashMap()

    @get:JsonIgnore
    internal val longValues: MutableMap<Int, IProperty<Long?>> = HashMap()

    @get:JsonIgnore
    internal val integerValues: MutableMap<Int, IProperty<Int?>> = HashMap()

    @get:JsonIgnore
    internal val uuidValues: MutableMap<Int, IProperty<UUID?>> = HashMap()

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
    internal val enumValues: MutableMap<Int, IProperty<Enum<*>?>> = HashMap()

    @get:JsonIgnore
    internal val stringEnumValues: MutableMap<Int, IProperty<Enum<*>?>> = HashMap()

    @get:JsonIgnore
    internal val enumCollections: MutableMap<Int, MutableCollection<Enum<*>>> = HashMap()

    @get:JsonIgnore
    internal val enumStringCollections: MutableMap<Int, MutableCollection<Enum<*>>> = HashMap()

    //objects
    @get:JsonIgnore
    internal val objectValues: MutableMap<FieldEntity<*>, IProperty<IEntity>> = HashMap()

    //maps
    @get:JsonIgnore
    internal val mapIntKeyValues: MutableMap<Int, MutableMap<Int, String>> = HashMap()

    @get:JsonIgnore
    internal val mapStringKeyValues: MutableMap<Int, MutableMap<String, String>> = HashMap()

    //blobs
    @get:JsonIgnore
    internal val blobValues: MutableMap<Int, IProperty<ByteArray?>> = HashMap()

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
    protected fun map(field: Field, property: IProperty<Short>, propertyName: String = ""): IProperty<Short> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as IProperty<Short?>, propertyName)
        return property
    }

    @JvmName("mapNullableShort")
    protected fun map(field: Field, property: IProperty<Short?>, propertyName: String = ""): IProperty<Short?> {
        validateShort(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return shortValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapDouble")
    protected fun map(field: Field, value: Double, propertyName: String = "") = map(field, DoubleProperty(value), propertyName)

    @JvmName("mapDouble")
    protected fun map(field: Field, property: IProperty<Double>, propertyName: String = ""): IProperty<Double> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as IProperty<Double?>, propertyName)
        return property
    }

    @JvmName("mapNullableDouble")
    protected fun map(field: Field, property: IProperty<Double?>, propertyName: String = ""): IProperty<Double?> {
        validateDouble(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return doubleValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapInt")
    protected fun map(field: Field, value: Int, propertyName: String = "") = map(field, IntegerProperty(value), propertyName)

    @JvmName("mapInt")
    protected fun map(field: Field, property: IProperty<Int>, propertyName: String = ""): IProperty<Int> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as IProperty<Int?>, propertyName)
        return property
    }

    @JvmName("mapNullableInt")
    protected fun map(field: Field, property: IProperty<Int?>, propertyName: String = ""): IProperty<Int?> {
        validateInt(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return integerValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapLong")
    protected fun map(field: Field, value: Long, propertyName: String = "") = map(field, LongProperty(value), propertyName)

    @JvmName("mapLong")
    protected fun map(field: Field, property: IProperty<Long>, propertyName: String = ""): IProperty<Long> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as IProperty<Long?>, propertyName)
        return property
    }

    @JvmName("mapNullableLong")
    protected fun map(field: Field, property: IProperty<Long?>, propertyName: String = ""): IProperty<Long?> {
        validateLong(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return longValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapFloat")
    protected fun map(field: Field, value: Float, propertyName: String = "") = map(field, FloatProperty(value), propertyName)

    @JvmName("mapFloat")
    protected fun map(field: Field, property: IProperty<Float>, propertyName: String = ""): IProperty<Float> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as IProperty<Float?>, propertyName)
        return property
    }

    @JvmName("mapNullableFloat")
    protected fun map(field: Field, property: IProperty<Float?>, propertyName: String = ""): IProperty<Float?> {
        validateFloat(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return floatValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapBoolean")
    protected fun map(field: Field, value: Boolean, propertyName: String = "") = map(field, BooleanProperty(value), propertyName)

    @JvmName("mapBoolean")
    protected fun map(field: Field, property: IProperty<Boolean>, propertyName: String = ""): IProperty<Boolean> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as IProperty<Boolean?>, propertyName)
        return property
    }

    @JvmName("mapNullableBoolean")
    protected fun map(field: Field, property: IProperty<Boolean?>, propertyName: String = ""): IProperty<Boolean?> {
        validateBoolean(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return booleanValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapUuid")
    protected fun map(field: Field, value: UUID, propertyName: String = "") = map(field, UuidProperty(value), propertyName)

    @JvmName("mapUuid")
    protected fun map(field: Field, property: IProperty<UUID>, propertyName: String = ""): IProperty<UUID> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as IProperty<UUID?>, propertyName)
        return property
    }

    @JvmName("mapNullableUuid")
    protected fun map(field: Field, property: IProperty<UUID?>, propertyName: String = ""): IProperty<UUID?> {
        validateUuid(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return uuidValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapString")
    protected fun map(field: Field, value: String, propertyName: String = "") = map(field, StringProperty(value), propertyName)

    @JvmName("mapString")
    protected fun map(field: Field, property: IProperty<String>, propertyName: String = ""): IProperty<String> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as IProperty<String?>, propertyName)
        return property
    }

    @JvmName("mapNullableString")
    protected fun map(field: Field, property: IProperty<String?>, propertyName: String = ""): IProperty<String?> {
        validateString(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return stringValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapDateTime")
    protected fun map(field: Field, value: LocalDateTime, propertyName: String = "") = map(field, LocalDateTimeProperty(value), propertyName)

    @JvmName("mapDateTime")
    protected fun map(field: Field, property: IProperty<LocalDateTime>, propertyName: String = ""): IProperty<LocalDateTime> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as IProperty<LocalDateTime?>, propertyName)
        return property
    }

    @JvmName("mapNullableDateTime")
    protected fun map(field: Field, property: IProperty<LocalDateTime?>, propertyName: String = ""): IProperty<LocalDateTime?> {
        validateDateTime(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return localDateTimeValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapZonedDateTime")
    protected fun map(field: Field, value: ZonedDateTime, propertyName: String = "") = map(field, ZonedDateTimeProperty(value), propertyName)

    @JvmName("mapZonedDateTime")
    protected fun map(field: Field, property: IProperty<ZonedDateTime>, propertyName: String = ""): IProperty<ZonedDateTime> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as IProperty<ZonedDateTime?>, propertyName)
        return property
    }

    @JvmName("mapNullableZonedDateTime")
    protected fun map(field: Field, property: IProperty<ZonedDateTime?>, propertyName: String = ""): IProperty<ZonedDateTime?> {
        validateZonedDateTime(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return zonedDateTimeValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapDate")
    protected fun map(field: Field, value: LocalDate, propertyName: String = "") = map(field, LocalDateProperty(value), propertyName)

    @JvmName("mapDate")
    protected fun map(field: Field, property: IProperty<LocalDate>, propertyName: String = ""): IProperty<LocalDate> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as IProperty<LocalDate?>, propertyName)
        return property
    }

    @JvmName("mapNullableDate")
    protected fun map(field: Field, property: IProperty<LocalDate?>, propertyName: String = ""): IProperty<LocalDate?> {
        validateDate(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return localDateValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapTime")
    protected fun map(field: Field, value: LocalTime, propertyName: String = "") = map(field, LocalTimeProperty(value), propertyName)

    @JvmName("mapTime")
    protected fun map(field: Field, property: IProperty<LocalTime>, propertyName: String = ""): IProperty<LocalTime> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as IProperty<LocalTime?>, propertyName)
        return property
    }

    @JvmName("mapNullableTime")
    protected fun map(field: Field, property: IProperty<LocalTime?>, propertyName: String = ""): IProperty<LocalTime?> {
        validateTime(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return localTimeValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapBlob")
    protected fun map(field: Field, value: ByteArray, propertyName: String = "") = map(field, BlobProperty(value), propertyName)

    @JvmName("mapBlob")
    protected fun map(field: Field, property: IProperty<ByteArray>, propertyName: String = ""): IProperty<ByteArray> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as IProperty<ByteArray?>, propertyName)
        return property
    }

    @JvmName("mapNullableBlob")
    protected fun map(field: Field, property: IProperty<ByteArray?>, propertyName: String = ""): IProperty<ByteArray?> {
        validateBlob(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return blobValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapMonthDay")
    protected fun map(field: Field, value: MonthDay, propertyName: String = "") = map(field, MonthDayProperty(value), propertyName)

    @JvmName("mapMonthDay")
    protected fun map(field: Field, property: IProperty<MonthDay>, propertyName: String = ""): IProperty<MonthDay> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as IProperty<MonthDay?>, propertyName)
        return property
    }

    @JvmName("mapNullableMonthDay")
    protected fun map(field: Field, property: IProperty<MonthDay?>, propertyName: String = ""): IProperty<MonthDay?> {
        validateMonthDay(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return monthDayValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapYearMonth")
    protected fun map(field: Field, value: YearMonth, propertyName: String = "") = map(field, YearMonthProperty(value), propertyName)

    @JvmName("mapYearMonth")
    protected fun map(field: Field, property: IProperty<YearMonth>, propertyName: String = ""): IProperty<YearMonth> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as IProperty<YearMonth?>, propertyName)
        return property
    }

    @JvmName("mapNullableYearMonth")
    protected fun map(field: Field, property: IProperty<YearMonth?>, propertyName: String = ""): IProperty<YearMonth?> {
        validateYearMonth(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return yearMonthValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapPeriod")
    protected fun map(field: Field, value: Period, propertyName: String = "") = map(field, PeriodProperty(value), propertyName)

    @JvmName("mapPeriod")
    protected fun map(field: Field, property: IProperty<Period>, propertyName: String = ""): IProperty<Period> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as IProperty<Period?>, propertyName)
        return property
    }

    @JvmName("mapNullablePeriod")
    protected fun map(field: Field, property: IProperty<Period?>, propertyName: String = ""): IProperty<Period?> {
        validatePeriod(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return periodValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapDuration")
    protected fun map(field: Field, value: Duration, propertyName: String = "") = map(field, DurationProperty(value), propertyName)

    @JvmName("mapDuration")
    protected fun map(field: Field, property: IProperty<Duration>, propertyName: String = ""): IProperty<Duration> {
        @Suppress("UNCHECKED_CAST")
        map(field, property as IProperty<Duration?>, propertyName)
        return property
    }

    @JvmName("mapNullableDuration")
    protected fun map(field: Field, property: IProperty<Duration?>, propertyName: String = ""): IProperty<Duration?> {
        validateDuration(field)
        FieldDictionary.addEntityField(overview.entityId, field.id, propertyName)
        return durationValues.getOrPut(mapField(overview.entityId, field.bind())) { property }
    }

    @JvmName("mapNullableEnum")
    protected fun <T : Enum<T>> map(fieldEnum: FieldEnum<T>, value: T, propertyName: String = "") = map(fieldEnum, NullableEnumProperty(value), propertyName)

    @JvmName("mapNullableEnum")
    protected fun <T : Enum<T>> map(fieldEnum: FieldEnum<T>, property: IProperty<T?>, propertyName: String = ""): IProperty<T?> {
        validateEnum(fieldEnum.field)
        if (fieldEnum.field.type == FieldType.Enum) {
            enumValues[fieldEnum.field.id] = property as IProperty<Enum<*>?>
        } else {
            stringEnumValues[fieldEnum.field.id] = property as IProperty<Enum<*>?>
        }
        fieldEnum.field.bind()
        FieldDictionary.addEntityField(overview.entityId, fieldEnum.field.id, propertyName)
        mapField(overview.entityId, fieldEnum.field.id)
        mapEnums(overview.entityId, fieldEnum.field.id)
        return property
    }

    @JvmName("mapEnum")
    protected fun <T : Enum<T>> map(fieldEnum: FieldEnum<T>, property: IProperty<T>, propertyName: String = ""): IProperty<T> {
        validateEnum(fieldEnum.field)
        when (fieldEnum.field.type) {
            FieldType.Enum -> {
                enumValues[fieldEnum.field.id] = property as IProperty<Enum<*>?>
            }
            else -> {
                stringEnumValues[fieldEnum.field.id] = property as IProperty<Enum<*>?>
            }
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

    protected fun <T : IEntity> map(fieldEntity: FieldEntity<T>, entity: T, propertyName: String = ""): IProperty<T> {
        return map(fieldEntity, ObjectProperty(entity), propertyName)
    }

    protected fun <T : IEntity> map(fieldEntity: FieldEntity<T>, property: IProperty<T>, propertyName: String = ""): IProperty<T> {
        if (fieldEntity.field.type != FieldType.Entity) {
            throw RuntimeException("Please assign the correct type to field [$fieldEntity]")
        }
        if (!objectCollections.containsKey(fieldEntity) && !objectValues.containsKey(fieldEntity)) {
            bindFieldIdToEntity(fieldEntity)
            objectValues[fieldEntity] = property as IProperty<IEntity>
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
