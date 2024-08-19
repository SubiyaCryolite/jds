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

import io.github.subiyacryolite.jds.annotations.EntityAnnotation
import io.github.subiyacryolite.jds.beans.property.*
import io.github.subiyacryolite.jds.context.DbContext
import io.github.subiyacryolite.jds.enums.FieldType
import io.github.subiyacryolite.jds.extensions.filterIgnored
import io.github.subiyacryolite.jds.extensions.filterIgnoredEnums
import io.github.subiyacryolite.jds.extensions.toByteArray
import io.github.subiyacryolite.jds.extensions.toUuid
import io.github.subiyacryolite.jds.interfaces.IEntity
import io.github.subiyacryolite.jds.interfaces.IOverview
import io.github.subiyacryolite.jds.interfaces.IValue
import io.github.subiyacryolite.jds.portable.*
import java.io.Serializable
import java.sql.Timestamp
import java.time.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

data class EntityOptions(
    var populate: Boolean = false,
    var assign: Boolean = false,
    var portableEntity: PortableEntity? = null
) {
    fun skip(): Boolean {
        return assign || populate
    }
}

/**
 * This class allows for all mapping operations in JDS, it also uses
 * [IOverview] to store overview data
 */
abstract class Entity(

    /**
     * Use JsonIgnoreProperties or similar annotations to ignore this property in subclasses if need be
     */
    final override var overview: IOverview = Overview(),

    private val options: EntityOptions = EntityOptions(),

    /**
     *
     */
    private val objectCollections: MutableMap<FieldEntity<*>, MutableCollection<IEntity>> = HashMap(),

    /**
     *
     */
    private val dateTimeCollections: MutableMap<Int, MutableCollection<LocalDateTime>> = HashMap(),

    /**
     *
     */
    private val enumValues: MutableMap<Int, IValue<Enum<*>?>> = HashMap(),

    /**
     *
     */
    private val stringEnumValues: MutableMap<Int, IValue<Enum<*>?>> = HashMap(),

    /**
     *
     */
    private val enumCollections: MutableMap<Int, MutableCollection<Enum<*>>> = HashMap(),

    /**
     *
     */
    private val enumStringCollections: MutableMap<Int, MutableCollection<Enum<*>>> = HashMap(),

    /**
     *
     */
    private val objectValues: MutableMap<FieldEntity<*>, IValue<IEntity>> = HashMap(),

    /**
     *
     */
    private val mapIntKeyValues: MutableMap<Int, MutableMap<Int, String>> = HashMap(),

    /**
     *
     */
    private val mapStringKeyValues: MutableMap<Int, MutableMap<String, String>> = HashMap(),

    /**
     *
     */
    private val mapOfCollectionsValues: MutableMap<Int, MutableMap<String, MutableCollection<String>>> = HashMap(),

    /**
     *
     */
    private val blobValues: MutableMap<Int, IValue<ByteArray?>> = HashMap()
) : IEntity {

    init {
        val entityAnnotation = getEntityAnnotation(javaClass)
        if (entityAnnotation != null) {
            overview.entityId = entityAnnotation.id
        } else {
            throw RuntimeException("You must annotate the class [" + javaClass.canonicalName + "] or its immediate parent with [" + EntityAnnotation::class.java + "]")
        }
    }

    /**
     * See source interface IEntity [map()][IEntity.bind] documentation
     */
    override fun bind() {
    }

    /**
     *
     */
    private fun <T> map(
        field: Field,
        value: IValue<T>,
        fieldType: Collection<FieldType>,
        destination: MutableMap<Int, IValue<T>>,
        propertyName: String = ""
    ): IValue<T> {
        val key = mapField(overview.entityId, Field.bind(field, fieldType), propertyName, options.skip());
        return destination.getOrPut(key) { value }
    }

    @JvmName("mapShort")
    protected fun map(
        field: Field,
        value: IValue<Short>,
        propertyName: String = ""
    ): IValue<Short> {
        @Suppress("UNCHECKED_CAST")
        map(field, value as IValue<Short?>, propertyName)
        return value
    }

    @JvmName("mapNullableShort")
    protected fun map(
        field: Field,
        value: IValue<Short?>,
        propertyName: String = ""
    ): IValue<Short?> {
        if (options.assign) {
            options.portableEntity?.shortValues?.add(StoreShort(field.id, value.value))
        } else if (populate(field)) {
            options.portableEntity?.shortValues?.filter { it.key == field.id }?.forEach {
                value.set(it.value)
            }
        }
        return map(field, value, setOf(FieldType.Short), mutableMapOf(), propertyName)
    }

    @JvmName("mapDouble")
    protected fun map(
        field: Field,
        value: IValue<Double>,
        propertyName: String = ""
    ): IValue<Double> {
        @Suppress("UNCHECKED_CAST")
        map(field, value as IValue<Double?>, propertyName)
        return value
    }

    @JvmName("mapNullableDouble")
    protected fun map(
        field: Field,
        value: IValue<Double?>,
        propertyName: String = ""
    ): IValue<Double?> {
        if (options.assign) {
            options.portableEntity?.doubleValues?.add(StoreDouble(field.id, value.value))
        } else if (populate(field)) {
            options.portableEntity?.doubleValues?.filter { it.key == field.id }?.forEach {
                value.set(it.value)
            }
        }
        return map(field, value, setOf(FieldType.Double), mutableMapOf(), propertyName)
    }

    @JvmName("mapInt")
    protected fun map(
        field: Field,
        value: IValue<Int>,
        propertyName: String = ""
    ): IValue<Int> {
        @Suppress("UNCHECKED_CAST")
        map(field, value as IValue<Int?>, propertyName)
        return value
    }

    @JvmName("mapNullableInt")
    protected fun map(
        field: Field,
        value: IValue<Int?>,
        propertyName: String = ""
    ): IValue<Int?> {
        if (options.assign) {
            options.portableEntity?.integerValues?.add(StoreInteger(field.id, value.value))
        } else if (populate(field)) {
            options.portableEntity?.integerValues?.filter { it.key == field.id }?.forEach {
                value.set(it.value)
            }
        }
        return map(field, value, setOf(FieldType.Int), mutableMapOf(), propertyName)
    }

    @JvmName("mapLong")
    protected fun map(
        field: Field,
        value: IValue<Long>,
        propertyName: String = ""
    ): IValue<Long> {
        @Suppress("UNCHECKED_CAST")
        map(field, value as IValue<Long?>, propertyName)
        return value
    }

    @JvmName("mapNullableLong")
    protected fun map(
        field: Field,
        value: IValue<Long?>,
        propertyName: String = ""
    ): IValue<Long?> {
        if (options.assign) {
            options.portableEntity?.longValues?.add(StoreLong(field.id, value.value))
        } else if (populate(field)) {
            options.portableEntity?.longValues?.filter { it.key == field.id }?.forEach {
                value.set(it.value)
            }
        }
        return map(field, value, setOf(FieldType.Long), mutableMapOf(), propertyName)
    }

    @JvmName("mapFloat")
    protected fun map(
        field: Field,
        value: IValue<Float>,
        propertyName: String = ""
    ): IValue<Float> {
        @Suppress("UNCHECKED_CAST")
        map(field, value as IValue<Float?>, propertyName)
        return value
    }

    @JvmName("mapNullableFloat")
    protected fun map(
        field: Field,
        value: IValue<Float?>,
        propertyName: String = ""
    ): IValue<Float?> {
        if (options.assign) {
            options.portableEntity?.floatValue?.add(StoreFloat(field.id, value.value))
        } else if (populate(field)) {
            options.portableEntity?.floatValue?.filter { it.key == field.id }?.forEach {
                value.set(it.value)
            }
        }
        return map(field, value, setOf(FieldType.Float), mutableMapOf(), propertyName)
    }

    @JvmName("mapBoolean")
    protected fun map(
        field: Field,
        value: IValue<Boolean>,
        propertyName: String = ""
    ): IValue<Boolean> {
        @Suppress("UNCHECKED_CAST")
        map(field, value as IValue<Boolean?>, propertyName)
        return value
    }

    @JvmName("mapNullableBoolean")
    protected fun map(
        field: Field,
        value: IValue<Boolean?>,
        propertyName: String = ""
    ): IValue<Boolean?> {
        if (options.assign) {
            options.portableEntity?.booleanValues?.add(
                StoreBoolean(
                    field.id, when (value.value) {
                        true -> 1
                        false -> 0
                        else -> null
                    }
                )
            )
        } else if (populate(field)) {
            options.portableEntity?.booleanValues?.filter { it.key == field.id }?.forEach {
                value.set(
                    when (it.value) {
                        is Int -> it.value == 1
                        else -> null
                    }
                )
            }
        }
        return map(field, value, setOf(FieldType.Boolean), mutableMapOf(), propertyName)
    }

    @JvmName("mapUuid")
    protected fun map(
        field: Field,
        value: IValue<UUID>,
        propertyName: String = ""
    ): IValue<UUID> {
        @Suppress("UNCHECKED_CAST")
        map(field, value as IValue<UUID?>, propertyName)
        return value
    }

    @JvmName("mapNullableUuid")
    protected fun map(
        field: Field,
        value: IValue<UUID?>,
        propertyName: String = ""
    ): IValue<UUID?> {
        if (options.assign) {
            options.portableEntity?.uuidValues?.add(StoreUuid(field.id, value.value.toByteArray()))
        } else if (populate(field)) {
            options.portableEntity?.uuidValues?.filter { it.key == field.id }?.forEach {
                value.set(it.value?.toUuid())
            }
        }
        return map(field, value, setOf(FieldType.Uuid), mutableMapOf(), propertyName)
    }

    @JvmName("mapString")
    protected fun map(
        field: Field,
        value: IValue<String>,
        propertyName: String = ""
    ): IValue<String> {
        @Suppress("UNCHECKED_CAST")
        map(field, value as IValue<String?>, propertyName)
        return value
    }

    @JvmName("mapNullableString")
    protected fun map(
        field: Field,
        value: IValue<String?>,
        propertyName: String = ""
    ): IValue<String?> {
        if (options.assign) {
            options.portableEntity?.stringValues?.add(StoreString(field.id, value.get()))
        } else if (populate(field)) {
            options.portableEntity?.stringValues?.filter { it.key == field.id }?.forEach {
                value.set(it.value)
            }
        }
        return map(field, value, setOf(FieldType.String), mutableMapOf(), propertyName)
    }

    @JvmName("mapDateTime")
    protected fun map(
        field: Field,
        value: IValue<LocalDateTime>,
        propertyName: String = ""
    ): IValue<LocalDateTime> {
        @Suppress("UNCHECKED_CAST")
        map(field, value as IValue<LocalDateTime?>, propertyName)
        return value
    }

    @JvmName("mapNullableDateTime")
    protected fun map(
        field: Field,
        value: IValue<LocalDateTime?>,
        propertyName: String = ""
    ): IValue<LocalDateTime?> {
        if (options.assign) {
            options.portableEntity?.dateTimeValues?.add(
                StoreDateTime(
                    field.id,
                    value.value?.toInstant(ZoneOffset.UTC)?.toEpochMilli()
                )
            )
        } else if (populate(field)) {
            options.portableEntity?.dateTimeValues?.filter { it.key == field.id }?.forEach {
                val src = it.value
                value.set(
                    when (src) {
                        is Long -> LocalDateTime.ofInstant(Instant.ofEpochMilli(src), ZoneId.of("UTC"))
                        else -> null
                    }
                )
            }
        }
        return map(field, value, setOf(FieldType.DateTime), mutableMapOf(), propertyName)
    }

    @JvmName("mapZonedDateTime")
    protected fun map(
        field: Field,
        value: IValue<ZonedDateTime>,
        propertyName: String = ""
    ): IValue<ZonedDateTime> {
        @Suppress("UNCHECKED_CAST")
        map(field, value as IValue<ZonedDateTime?>, propertyName)
        return value
    }

    @JvmName("mapNullableZonedDateTime")
    protected fun map(
        field: Field,
        value: IValue<ZonedDateTime?>,
        propertyName: String = ""
    ): IValue<ZonedDateTime?> {
        if (options.assign) {
            options.portableEntity?.zonedDateTimeValues?.add(
                StoreZonedDateTime(
                    field.id,
                    value.value?.toInstant()?.toEpochMilli()
                )
            )
        } else if (populate(field)) {
            options.portableEntity?.zonedDateTimeValues?.filter { it.key == field.id }?.forEach {
                val src = it.value
                value.set(
                    when (src) {
                        is Long -> ZonedDateTime.ofInstant(Instant.ofEpochMilli(src), ZoneId.of("UTC"))
                        else -> null
                    }
                )
            }
        }
        return map(field, value, setOf(FieldType.ZonedDateTime), mutableMapOf(), propertyName)
    }

    @JvmName("mapDate")
    protected fun map(
        field: Field,
        value: IValue<LocalDate>,
        propertyName: String = ""
    ): IValue<LocalDate> {
        @Suppress("UNCHECKED_CAST")
        map(field, value as IValue<LocalDate?>, propertyName)
        return value
    }

    @JvmName("mapNullableDate")
    protected fun map(
        field: Field,
        value: IValue<LocalDate?>,
        propertyName: String = ""
    ): IValue<LocalDate?> {
        if (options.assign) {
            options.portableEntity?.dateValues?.add(StoreDate(field.id, value.get()?.toEpochDay()))
        } else if (populate(field)) {
            options.portableEntity?.dateValues?.filter { it.key == field.id }?.forEach {
                val src = it.value
                value.set(
                    when (src) {
                        is Long -> LocalDate.ofEpochDay(src)
                        else -> null
                    }
                )
            }
        }
        return map(field, value, setOf(FieldType.Date), mutableMapOf(), propertyName)
    }

    @JvmName("mapTime")
    protected fun map(
        field: Field,
        value: IValue<LocalTime>,
        propertyName: String = ""
    ): IValue<LocalTime> {
        @Suppress("UNCHECKED_CAST")
        map(field, value as IValue<LocalTime?>, propertyName)
        return value
    }

    @JvmName("mapNullableTime")
    protected fun map(
        field: Field,
        value: IValue<LocalTime?>,
        propertyName: String = ""
    ): IValue<LocalTime?> {
        if (options.assign) {
            options.portableEntity?.timeValues?.add(StoreTime(field.id, value.value?.toNanoOfDay()))
        } else if (populate(field)) {
            options.portableEntity?.timeValues?.filter { it.key == field.id }?.forEach {
                value.set(
                    when (it.value) {
                        is Long -> LocalTime.ofNanoOfDay(it.value!!)
                        else -> null
                    }
                )
            }
        }
        return map(field, value, setOf(FieldType.Time), mutableMapOf(), propertyName)
    }

    @JvmName("mapBlob")
    protected fun map(
        field: Field,
        value: IValue<ByteArray>,
        propertyName: String = ""
    ): IValue<ByteArray> {
        @Suppress("UNCHECKED_CAST")
        map(field, value as IValue<ByteArray?>, propertyName)
        return value
    }

    @JvmName("mapNullableBlob")
    protected fun map(
        field: Field,
        value: IValue<ByteArray?>,
        propertyName: String = ""
    ): IValue<ByteArray?> = map(field, value, setOf(FieldType.Blob), blobValues, propertyName)

    @JvmName("mapMonthDay")
    protected fun map(
        field: Field,
        value: IValue<MonthDay>,
        propertyName: String = ""
    ): IValue<MonthDay> {
        @Suppress("UNCHECKED_CAST")
        map(field, value as IValue<MonthDay?>, propertyName)
        return value
    }

    @JvmName("mapNullableMonthDay")
    protected fun map(
        field: Field,
        value: IValue<MonthDay?>,
        propertyName: String = ""
    ): IValue<MonthDay?> {
        if (options.assign) {
            options.portableEntity?.monthDayValues?.add(StoreMonthDay(field.id, value.value?.toString()))
        } else if (populate(field)) {
            options.portableEntity?.monthDayValues?.filter { it.key == field.id }?.forEach {
                value.set(
                    when (it.value) {
                        is String -> MonthDay.parse(it.value!!)
                        else -> null
                    }
                )
            }
        }
        return map(field, value, setOf(FieldType.MonthDay), mutableMapOf(), propertyName)
    }

    @JvmName("mapYearMonth")
    protected fun map(
        field: Field,
        value: IValue<YearMonth>,
        propertyName: String = ""
    ): IValue<YearMonth> {
        @Suppress("UNCHECKED_CAST")
        map(field, value as IValue<YearMonth?>, propertyName)
        return value
    }

    @JvmName("mapNullableYearMonth")
    protected fun map(
        field: Field,
        value: IValue<YearMonth?>,
        propertyName: String = ""
    ): IValue<YearMonth?> {
        if (options.assign) {
            options.portableEntity?.yearMonthValues?.add(StoreYearMonth(field.id, value.value?.toString()))
        } else if (populate(field)) {
            options.portableEntity?.yearMonthValues?.filter { it.key == field.id }?.forEach {
                value.set(
                    when (it.value) {
                        is String -> YearMonth.parse(it.value!!)
                        else -> null
                    }
                )
            }
        }
        return map(field, value, setOf(FieldType.YearMonth), mutableMapOf(), propertyName)
    }

    @JvmName("mapPeriod")
    protected fun map(
        field: Field,
        value: IValue<Period>,
        propertyName: String = ""
    ): IValue<Period> {
        @Suppress("UNCHECKED_CAST")
        map(field, value as IValue<Period?>, propertyName)
        return value
    }

    @JvmName("mapNullablePeriod")
    protected fun map(
        field: Field,
        value: IValue<Period?>,
        propertyName: String = ""
    ): IValue<Period?> {
        if (options.assign) {
            options.portableEntity?.periodValues?.add(StorePeriod(field.id, value.value?.toString()))
        } else if (populate(field)) {
            options.portableEntity?.periodValues?.filter { it.key == field.id }?.forEach {
                value.set(
                    when (it.value) {
                        is String -> Period.parse(it.value!!)
                        else -> null
                    }
                )
            }
        }
        return map(field, value, setOf(FieldType.Period), mutableMapOf(), propertyName)
    }

    @JvmName("mapDuration")
    protected fun map(
        field: Field,
        value: IValue<Duration>,
        propertyName: String = ""
    ): IValue<Duration> {
        @Suppress("UNCHECKED_CAST")
        map(field, value as IValue<Duration?>, propertyName)
        return value
    }

    @JvmName("mapNullableDuration")
    protected fun map(
        field: Field,
        value: IValue<Duration?>,
        propertyName: String = ""
    ): IValue<Duration?> {
        if (options.assign) {
            //TODO replace with generic JSON for better compatibility, all fields in map type <String, Any?>
            //TODO portable entity.values
            //TODO this can be extended in different languages
            options.portableEntity?.durationValues?.add(StoreDuration(field.id, value.value?.toNanos()))
        } else if (populate(field)) {
            options.portableEntity?.durationValues?.filter { it.key == field.id }?.forEach {
                value.set(
                    when (it.value) {
                        is Long -> Duration.ofNanos(it.value!!)
                        else -> null
                    }
                )
            }
        }
        return map(field, value, setOf(FieldType.Duration), mutableMapOf(), propertyName)
    }

    @JvmName("mapNullableEnum")
    protected fun <T : Enum<T>> map(
        fieldEnum: FieldEnum<T>,
        value: IValue<T?>,
        propertyName: String = ""
    ): IValue<T?> {
        val fieldId = Field.bind(fieldEnum.field, setOf(FieldType.Enum, FieldType.EnumString))
        if (fieldEnum.field.type == FieldType.Enum) {
            @Suppress("UNCHECKED_CAST")
            enumValues[fieldId] = value as IValue<Enum<*>?>
        } else {
            @Suppress("UNCHECKED_CAST")
            stringEnumValues[fieldId] = value as IValue<Enum<*>?>
        }
        mapField(overview.entityId, fieldId, propertyName)
        mapEnums(overview.entityId, fieldId)
        return value
    }

    @JvmName("mapEnum")
    protected fun <T : Enum<T>> map(
        fieldEnum: FieldEnum<T>,
        value: IValue<T>,
        propertyName: String = ""
    ): IValue<T> {
        val fieldId = Field.bind(fieldEnum.field, setOf(FieldType.Enum, FieldType.EnumString))
        when (fieldEnum.field.type) {
            FieldType.Enum -> {
                @Suppress("UNCHECKED_CAST")
                enumValues[fieldId] = value as IValue<Enum<*>?>
            }

            else -> {
                @Suppress("UNCHECKED_CAST")
                stringEnumValues[fieldId] = value as IValue<Enum<*>?>
            }
        }
        mapField(overview.entityId, fieldId, propertyName)
        mapEnums(overview.entityId, fieldId)
        return value
    }

    @JvmName("mapStrings")
    protected fun map(
        field: Field,
        collection: Collection<String>,
        propertyName: String = ""
    ): Collection<String> {
        if (options.assign) {
            options.portableEntity?.stringCollections?.add(StoreStringCollection(field.id, collection))
        } else if (populate(field)) {
            options.portableEntity?.stringCollections?.filter { it.key == field.id }?.forEach { match ->
                match.values.forEach { value ->
                    if (collection is MutableCollection) collection.add(value)
                }
            }
        }
        mapField(overview.entityId, Field.bind(field, FieldType.StringCollection), propertyName, options.skip())
        return listOf()
    }

    @JvmName("mapDateTimes")
    protected fun map(
        field: Field,
        collection: MutableCollection<LocalDateTime>,
        propertyName: String = ""
    ): MutableCollection<LocalDateTime> {
        return dateTimeCollections.getOrPut(
            mapField(
                overview.entityId,
                Field.bind(field, FieldType.DateTimeCollection),
                propertyName
            )
        ) { collection }
    }

    @JvmName("mapFloats")
    protected fun map(
        field: Field,
        collection: Collection<Float>,
        name: String = ""
    ): Collection<Float> {
        if (options.assign) {
            options.portableEntity?.floatCollections?.add(StoreFloatCollection(field.id, collection))
        } else if (populate(field)) {
            options.portableEntity?.floatCollections?.filter { it.key == field.id }?.forEach { match ->
                match.values.forEach { value ->
                    if (collection is MutableCollection) collection.add(value)
                }
            }
        }
        mapField(overview.entityId, Field.bind(field, FieldType.FloatCollection), name, options.skip())
        return listOf()
    }

    private fun populate(field: Field): Boolean {
        return options.populate && populateProperty(DbContext.instance, field.id)
    }

    @JvmName("mapIntegers")
    protected fun map(
        field: Field,
        collection: Collection<Int>,
        propertyName: String = ""
    ): Collection<Int> {
        if (options.assign) {
            options.portableEntity?.integerCollections?.add(StoreIntegerCollection(field.id, collection))
        } else if (populate(field)) {
            options.portableEntity?.integerCollections?.filter { it.key == field.id }?.forEach { match ->
                match.values.forEach { value ->
                    if (collection is MutableCollection) collection.add(value)
                }
            }
        }
        mapField(overview.entityId, Field.bind(field, FieldType.IntCollection), propertyName, options.skip())
        return emptyList()
    }

    @JvmName("mapShorts")
    protected fun map(
        field: Field,
        collection: Collection<Short>,
        propertyName: String = ""
    ): Collection<Short> {
        if (options.assign) {
            options.portableEntity?.shortCollections?.add(StoreShortCollection(field.id, collection))
        } else if (populate(field)) {
            options.portableEntity?.shortCollections?.filter { it.key == field.id }?.forEach { match ->
                match.values.forEach { value ->
                    if (collection is MutableCollection) collection.add(value)
                }
            }
        }
        mapField(overview.entityId, Field.bind(field, FieldType.ShortCollection), propertyName, options.skip())
        return emptyList()
    }

    @JvmName("mapUuids")
    protected fun map(
        field: Field,
        collection: Collection<UUID>,
        propertyName: String = ""
    ): Collection<UUID> {
        if (options.assign) {
            options.portableEntity?.uuidCollections?.add(StoreUuidCollection(field.id, toByteArrayCollection(collection)))
        } else if (populate(field)) {
            options.portableEntity?.uuidCollections?.filter { it.key == field.id }?.forEach { match ->
                match.values.forEach { value ->
                    if (collection is MutableCollection) collection.add(value.toUuid()!!)
                }
            }
        }
       mapField(overview.entityId, Field.bind(field, FieldType.UuidCollection), propertyName, options.skip())
       return emptyList()
    }

    @JvmName("mapDoubles")
    protected fun map(
        field: Field,
        collection: Collection<Double>,
        propertyName: String = ""
    ): Collection<Double> {
        if (options.assign) {
            options.portableEntity?.doubleCollections?.add(StoreDoubleCollection(field.id, collection))
        } else if (populate(field)) {
            options.portableEntity?.doubleCollections?.filter { it.key == field.id }?.forEach { match ->
                match.values.forEach { value ->
                    if (collection is MutableCollection) collection.add(value)
                }
            }
        }
        mapField(overview.entityId, Field.bind(field, FieldType.DoubleCollection), propertyName, options.skip())
        return listOf()
    }

    @JvmName("mapLongs")
    protected fun map(
        field: Field,
        collection: Collection<Long>,
        propertyName: String = ""
    ): Collection<Long> {
        if (options.assign) {
            options.portableEntity?.longCollections?.add(StoreLongCollection(field.id, collection))
        } else if (populate(field)) {
            options.portableEntity?.longCollections?.filter { it.key == field.id }?.forEach { match ->
                match.values.forEach { value ->
                    if (collection is MutableCollection) collection.add(value)
                }
            }
        }
        mapField(overview.entityId, Field.bind(field, FieldType.LongCollection), propertyName, options.skip())
        return listOf()
    }

    @JvmName("mapIntMap")
    protected fun map(
        field: Field,
        map: MutableMap<Int, String>,
        propertyName: String = ""
    ): MutableMap<Int, String> {
        return mapIntKeyValues.getOrPut(
            mapField(
                overview.entityId,
                Field.bind(field, FieldType.MapIntKey),
                propertyName
            )
        ) { map }
    }

    @JvmName("mapStringMap")
    protected fun map(
        field: Field,
        map: MutableMap<String, String>,
        propertyName: String = ""
    ): MutableMap<String, String> {
        return mapStringKeyValues.getOrPut(
            mapField(
                overview.entityId,
                Field.bind(field, FieldType.MapStringKey),
                propertyName
            )
        ) { map }
    }

    @JvmName("mapOfCollections")
    protected fun map(
        field: Field,
        map: MutableMap<String, MutableCollection<String>>,
        propertyName: String = ""
    ): Map<String, Collection<String>> {
        return mapOfCollectionsValues.getOrPut(
            mapField(
                overview.entityId,
                Field.bind(field, FieldType.MapOfCollections),
                propertyName
            )
        ) { map }
    }

    @JvmName("mapEnums")
    protected fun <T : Enum<T>> map(
        fieldEnum: FieldEnum<T>,
        collection: MutableCollection<T>,
        propertyName: String = ""
    ): MutableCollection<T> {
        val fieldId = Field.bind(fieldEnum.field, setOf(FieldType.EnumCollection, FieldType.EnumStringCollection))
        when (fieldEnum.field.type) {
            FieldType.EnumCollection -> {
                @Suppress("UNCHECKED_CAST")
                enumCollections[fieldId] = collection as MutableCollection<Enum<*>>
            }

            else -> {
                @Suppress("UNCHECKED_CAST")
                enumStringCollections[fieldId] = collection as MutableCollection<Enum<*>>
            }
        }
        mapField(overview.entityId, fieldId, propertyName)
        mapEnums(overview.entityId, fieldId)
        return collection
    }

    protected fun <T : IEntity> map(
        fieldEntity: FieldEntity<T>,
        entity: T,
        propertyName: String = ""
    ): IValue<T> {
        return map(fieldEntity, ObjectValue(entity), propertyName)
    }

    protected fun <T : IEntity> map(
        fieldEntity: FieldEntity<T>,
        value: IValue<T>,
        propertyName: String = ""
    ): IValue<T> {
        if (!objectCollections.containsKey(fieldEntity) && !objectValues.containsKey(fieldEntity)) {
            bindFieldIdToEntity(fieldEntity, FieldType.Entity)
            @Suppress("UNCHECKED_CAST")
            objectValues[fieldEntity] = value as IValue<IEntity>
            mapField(overview.entityId, fieldEntity.field.id, propertyName, options.skip())
        } else {
            if (!options.skip())
                throw RuntimeException("You can only bind a class to one Value. This class is already bound to one object or object array")
        }
        return value
    }

    /**
     * @param fieldEntity
     * @param collection
     */
    protected fun <T : IEntity> map(
        fieldEntity: FieldEntity<T>,
        collection: MutableCollection<T>,
        propertyName: String = ""
    ): MutableCollection<T> {
        if (!objectCollections.containsKey(fieldEntity)) {
            bindFieldIdToEntity(fieldEntity, FieldType.EntityCollection)
            @Suppress("UNCHECKED_CAST")
            objectCollections[fieldEntity] = collection as MutableCollection<IEntity>
            mapField(overview.entityId, fieldEntity.field.id, propertyName, options.skip())
        } else {
            if (!options.skip())
                throw RuntimeException("You can only bind a class to one Value. This class is already bound to one object or object array")
        }
        return collection
    }

    private fun <T : IEntity> bindFieldIdToEntity(
        fieldEntity: FieldEntity<T>,
        fieldType: FieldType
    ) {
        val fieldId = Field.bind(fieldEntity.field, fieldType)
        FieldEntity.values[fieldId] = fieldEntity
    }

    private fun populateProperty(
        dbContext: DbContext?,
        fieldId: Int,
    ): Boolean {
        var populate = false
        if (dbContext != null)
            populate = !dbContext.options.ignoreTags.any { tag -> Field.values[fieldId]!!.tags.contains(tag) }
        return populate
    }

    /**
     * This method enforces forward compatibility by ensuring that every Value is present even if the field is not defined or known locally
     * Delete this block
     */
    @Deprecated("No longer needed as the concept of backing values will be deleted entirely")
    @Suppress("NON_EXHAUSTIVE_WHEN")
    private fun initBackingValueIfNotDefined(
        fieldType: FieldType,
        fieldId: Int
    ) {
        when (fieldType) {
            FieldType.DateTimeCollection -> dateTimeCollections.putIfAbsent(fieldId, ArrayList())
            FieldType.EnumCollection -> enumCollections.putIfAbsent(fieldId, ArrayList())
            FieldType.EnumStringCollection -> enumStringCollections.putIfAbsent(fieldId, ArrayList())
            FieldType.Blob -> blobValues.putIfAbsent(fieldId, NullableBlobValue())
            FieldType.Enum -> enumValues.putIfAbsent(fieldId, ObjectValue<Enum<*>?>(null))
            FieldType.EnumString -> stringEnumValues.putIfAbsent(fieldId, ObjectValue<Enum<*>?>(null))
            FieldType.MapIntKey -> mapIntKeyValues.putIfAbsent(fieldId, HashMap())
            FieldType.MapStringKey -> mapStringKeyValues.putIfAbsent(fieldId, HashMap())
            FieldType.MapOfCollections -> mapOfCollectionsValues.putIfAbsent(fieldId, HashMap())
            else -> {}
        }
    }

    companion object : Serializable {

        private const val serialVersionUID = 20180106_2125L

        private val fields = ConcurrentHashMap<Int, LinkedHashSet<Int>>()

        private val enums = ConcurrentHashMap<Int, LinkedHashSet<Int>>()

        internal val classes = ConcurrentHashMap<Int, Class<out Entity>>()

        internal fun getEntityAnnotation(clazz: Class<*>?): EntityAnnotation? {

            var interfaceAnnotation: EntityAnnotation? = null
            var classHit: EntityAnnotation? = null

            clazz?.declaredAnnotations?.forEach { annotation ->
                if (annotation is EntityAnnotation) {
                    classHit = annotation
                    return@forEach
                }
            }
            if (classHit != null) {
                return classHit//prioritise classes
            }

            clazz?.interfaces?.forEach { clazzInterface ->
                val interfaceMatch = getEntityAnnotation(clazzInterface)
                if (interfaceMatch != null) {
                    interfaceAnnotation = interfaceMatch
                    return@forEach
                }
            }

            if (interfaceAnnotation != null) {
                return interfaceAnnotation//then interfaces
            }

            return null
        }

        protected fun mapField(entityId: Int, fieldId: Int, propertyName: String, skip: Boolean = false): Int {
            if (!skip && DbContext.initialising) {
                getFieldsImp(entityId).add(fieldId)
                FieldDictionary.registerField(entityId, fieldId, propertyName)
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

        private fun toByteArrayCollection(values: Collection<UUID>): Collection<ByteArray> {
            val output = ArrayList<ByteArray>()
            values.forEach { value ->
                output.add(value.toByteArray()!!)
            }
            return output
        }

        private fun toTimeStampCollection(values: Collection<LocalDateTime>) =
            values.map { Timestamp.valueOf(it) }

        private fun toIntCollection(values: Collection<Enum<*>>) = values.map { it.ordinal }

        private fun toStringCollection(values: Collection<Enum<*>>) = values.map { it.name }

        internal fun assign(entity: Entity, dbContext: DbContext, portableEntity: PortableEntity) {

            entity.options.assign = true
            entity.options.portableEntity = portableEntity
            try {
                entity.bind()
            } finally {
                entity.options.assign = false
                entity.options.portableEntity = null
            }
            //==============================================
            //BLOB
            //==============================================
            entity.blobValues.filterIgnored(dbContext).forEach { entry ->
                portableEntity.blobValues.add(StoreBlob(entry.key, entry.value.value ?: ByteArray(0)))
            }
            //==============================================
            //Enums
            //==============================================
            entity.enumValues.filterIgnored(dbContext).forEach { entry ->
                portableEntity.enumValues.add(StoreEnum(entry.key, entry.value.value?.ordinal))
            }
            entity.stringEnumValues.filterIgnored(dbContext).forEach { entry ->
                portableEntity.enumStringValues.add(StoreEnumString(entry.key, entry.value.value?.name))
            }
            entity.enumCollections.filterIgnoredEnums(dbContext).forEach { entry ->
                portableEntity.enumCollections.add(StoreEnumCollection(entry.key, toIntCollection(entry.value)))
            }
            entity.enumStringCollections.filterIgnoredEnums(dbContext).forEach { entry ->
                portableEntity.enumStringCollections.add(
                    StoreEnumStringCollection(
                        entry.key,
                        toStringCollection(entry.value)
                    )
                )
            }
            //==============================================
            //ARRAYS
            //==============================================
            entity.dateTimeCollections.filterIgnored(dbContext).forEach { entry ->
                portableEntity.dateTimeCollection.add(
                    StoreDateTimeCollection(
                        entry.key,
                        toTimeStampCollection(entry.value)
                    )
                )
            }
            //==============================================
            // Maps
            //==============================================
            entity.mapIntKeyValues.filterIgnored(dbContext).forEach { entry ->
                portableEntity.mapIntKeyValues.add(StoreMapIntKey(entry.key, entry.value))
            }
            entity.mapStringKeyValues.filterIgnored(dbContext).forEach { entry ->
                portableEntity.mapStringKeyValues.add(StoreMapStringKey(entry.key, entry.value))
            }
            entity.mapOfCollectionsValues.filterIgnored(dbContext).forEach { entry ->
                portableEntity.mapOfCollectionsValues.add(StoreMapCollection(entry.key, entry.value))
            }
            //==============================================
            //EMBEDDED OBJECTS
            //==============================================
            entity.objectCollections.forEach { (fieldEntity, mutableCollection) ->
                mutableCollection.forEach { entity ->
                    val innerPortableEntity = PortableEntity()
                    innerPortableEntity.fieldId = fieldEntity.field.id
                    innerPortableEntity.init(dbContext, entity)
                    portableEntity.entityOverviews.add(innerPortableEntity)
                }
            }
            entity.objectValues.forEach { (fieldEntity, objectValue) ->
                val innerPortableEntity = PortableEntity()
                innerPortableEntity.fieldId = fieldEntity.field.id
                innerPortableEntity.init(dbContext, objectValue.value)
                portableEntity.entityOverviews.add(innerPortableEntity)
            }
        }

        internal fun populate(entity: Entity, dbContext: DbContext, portableEntity: PortableEntity) {
            entity.options.populate = true
            entity.options.portableEntity = portableEntity
            try {
                entity.bind()
            } finally {
                entity.options.populate = false
                entity.options.portableEntity = null
            }

            portableEntity.blobValues.forEach { field ->
                val value = field.value
                if (entity.populateProperty(dbContext, field.key)) {
                    entity.blobValues.getValue(field.key).value = when (value) {
                        is ByteArray -> value
                        else -> null
                    }
                }
            }
            portableEntity.enumValues.forEach { field ->
                if (entity.populateProperty(dbContext, field.key)) {
                    val fieldEnum = FieldEnum.enums[field.key]
                    val value = field.value
                    if (fieldEnum != null && value != null) {
                        entity.enumValues.getValue(field.key).value = fieldEnum.valueOf(value)
                    }
                }
            }
            portableEntity.enumStringValues.forEach { field ->
                if (entity.populateProperty(dbContext, field.key)) {
                    val fieldEnum = FieldEnum.enums[field.key]
                    val value = field.value
                    if (fieldEnum != null && value != null) {
                        entity.stringEnumValues.getValue(field.key).value = fieldEnum.valueOf(value)
                    }
                }
            }
            portableEntity.dateTimeCollection.forEach { field ->
                if (entity.populateProperty(dbContext, field.key)) {
                    val dest = entity.dateTimeCollections.getValue(field.key)
                    field.values.forEach { value -> dest.add((value).toLocalDateTime()) }
                }
            }
            portableEntity.enumCollections.forEach { field ->
                if (entity.populateProperty(dbContext, field.key)) {
                    val dest = entity.enumCollections.getValue(field.key)
                    val fieldEnum = FieldEnum.enums[field.key]
                    if (fieldEnum != null) {
                        field.values.forEach { enumOrdinal ->
                            val enumValues = fieldEnum.values
                            if (enumOrdinal < enumValues.size) {
                                dest.add(enumValues.find { enumValue -> enumValue.ordinal == enumOrdinal }!!)
                            }
                        }
                    }
                }
            }
            portableEntity.enumStringCollections.forEach { field ->
                if (entity.populateProperty(dbContext, field.key)) {
                    val dest = entity.enumStringCollections.getValue(field.key)
                    val fieldEnum = FieldEnum.enums[field.key]
                    if (fieldEnum != null) {
                        field.values.forEach { enumString ->
                            val enumVal = fieldEnum.valueOf(enumString)
                            if (enumVal != null)
                                dest.add(enumVal)
                        }
                    }
                }
            }
            portableEntity.mapIntKeyValues.forEach { field ->
                if (entity.populateProperty(dbContext, field.key)) {
                    entity.mapIntKeyValues.getValue(field.key).putAll(field.values)
                }
            }
            portableEntity.mapStringKeyValues.forEach { field ->
                if (entity.populateProperty(dbContext, field.key)) {
                    entity.mapStringKeyValues.getValue(field.key).putAll(field.values)
                }
            }
            portableEntity.mapOfCollectionsValues.forEach { field ->
                if (entity.populateProperty(dbContext, field.key)) {
                    entity.mapOfCollectionsValues.getValue(field.key).putAll(field.values)
                }
            }
            //==============================================
            portableEntity.entityOverviews.forEach { subEntities ->
                populateObjects(
                    entity,
                    dbContext,
                    subEntities.overview.fieldId,
                    subEntities.overview.entityId,
                    subEntities.overview.id,
                    subEntities.overview.editVersion,
                    subEntities
                )
            }
        }

        private fun populateObjects(
            entity: Entity,
            dbContext: DbContext,
            fieldId: Int?,
            entityId: Int,
            id: String,
            editVersion: Int,
            portableEntity: PortableEntity
        ) {
            if (fieldId == null) return
            entity.objectCollections.filter { entry ->
                entry.key.field.id == fieldId
            }.forEach { entry ->
                val referenceClass = classes[entityId]
                if (referenceClass != null) {
                    val subEntity = referenceClass.getDeclaredConstructor().newInstance()//create array element
                    subEntity.overview.id = id
                    subEntity.overview.editVersion = editVersion
                    populate(subEntity, dbContext, portableEntity)
                    entry.value.add(subEntity)
                }
            }
            //find existing elements
            entity.objectValues.filter { entry ->
                entry.key.field.id == fieldId
            }.forEach { entry ->
                val referenceClass = classes[entityId]
                if (referenceClass != null) {
                    entry.value.value = referenceClass.getDeclaredConstructor().newInstance()//create array element
                    entry.value.value.overview.id = id
                    entry.value.value.overview.editVersion = editVersion
                    val subEntity = entry.value.value as Entity
                    populate(subEntity, dbContext, portableEntity)
                }
            }
        }
    }
}