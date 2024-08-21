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
        if (options.action == EntityAction.ASSIGN) {
            options.portableEntity?.shortValues?.add(StoreShort(field.id, value.value))
        }
        if (populate(field)) {
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
        if (options.action == EntityAction.ASSIGN) {
            options.portableEntity?.doubleValues?.add(StoreDouble(field.id, value.value))
        }
        if (populate(field)) {
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
        if (options.action == EntityAction.ASSIGN) {
            options.portableEntity?.integerValues?.add(StoreInteger(field.id, value.value))
        }
        if (populate(field)) {
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
        if (options.action == EntityAction.ASSIGN) {
            options.portableEntity?.longValues?.add(StoreLong(field.id, value.value))
        }
        if (populate(field)) {
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
        if (options.action == EntityAction.ASSIGN) {
            options.portableEntity?.floatValue?.add(StoreFloat(field.id, value.value))
        }
        if (populate(field)) {
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
        if (options.action == EntityAction.ASSIGN) {
            options.portableEntity?.booleanValues?.add(
                StoreBoolean(
                    field.id, when (value.value) {
                        true -> 1
                        false -> 0
                        else -> null
                    }
                )
            )
        }
        if (populate(field)) {
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
        if (options.action == EntityAction.ASSIGN) {
            options.portableEntity?.uuidValues?.add(StoreUuid(field.id, value.value))
        }
        if (populate(field)) {
            options.portableEntity?.uuidValues?.filter { it.key == field.id }?.forEach {
                value.set(it.get())
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
        if (options.action == EntityAction.ASSIGN) {
            options.portableEntity?.stringValues?.add(StoreString(field.id, value.get()))
        }
        if (populate(field)) {
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
        if (options.action == EntityAction.ASSIGN) {
            options.portableEntity?.dateTimeValues?.add(
                StoreDateTime(
                    field.id,
                    value.value
                )
            )
        }
        if (populate(field)) {
            options.portableEntity?.dateTimeValues?.filter { it.key == field.id }?.forEach {
                value.set(it.get())
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
        if (options.action == EntityAction.ASSIGN) {
            options.portableEntity?.zonedDateTimeValues?.add(StoreZonedDateTime(field.id, value.value))
        }
        if (populate(field)) {
            options.portableEntity?.zonedDateTimeValues?.filter { it.key == field.id }?.forEach {
                value.set(it.get())
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
        if (options.action == EntityAction.ASSIGN) {
            options.portableEntity?.dateValues?.add(StoreDate(field.id, value.value))
        }
        if (populate(field)) {
            options.portableEntity?.dateValues?.filter { it.key == field.id }?.forEach {
                value.set(it.get())
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
        if (options.action == EntityAction.ASSIGN) {
            options.portableEntity?.timeValues?.add(StoreTime(field.id, value.value))
        }
        if (populate(field)) {
            options.portableEntity?.timeValues?.filter { it.key == field.id }?.forEach {
                value.set(it.get())
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
    ): IValue<ByteArray?> {
        if (options.action == EntityAction.ASSIGN) {
            options.portableEntity?.blobValues?.add(StoreBlob(field.id, value.value ?: ByteArray(0)))
        }
        if (populate(field)) {
            options.portableEntity?.blobValues?.filter { it.key == field.id }?.forEach {
                value.set(
                    when (it.value) {
                        is ByteArray -> it.value
                        else -> null
                    }
                )
            }
        }
        return map(field, value, setOf(FieldType.Blob), mutableMapOf(), propertyName)
    }

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
        if (options.action == EntityAction.ASSIGN) {
            options.portableEntity?.monthDayValues?.add(StoreMonthDay(field.id, value.value))
        }
        if (populate(field)) {
            options.portableEntity?.monthDayValues?.filter { it.key == field.id }?.forEach {
                value.set(it.get())
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
        if (options.action == EntityAction.ASSIGN) {
            options.portableEntity?.yearMonthValues?.add(StoreYearMonth(field.id, value.value))
        }
        if (populate(field)) {
            options.portableEntity?.yearMonthValues?.filter { it.key == field.id }?.forEach {
                value.set(it.get())
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
        if (options.action == EntityAction.ASSIGN) {
            options.portableEntity?.periodValues?.add(StorePeriod(field.id, value.value))
        }
        if (populate(field)) {
            options.portableEntity?.periodValues?.filter { it.key == field.id }?.forEach {
                value.set(it.get())
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
        if (options.action == EntityAction.ASSIGN) {
            options.portableEntity?.durationValues?.add(StoreDuration(field.id, value.value))
        }
        if (populate(field)) {
            options.portableEntity?.durationValues?.filter { it.key == field.id }?.forEach {
                value.set(it.get())
            }
        }
        return map(field, value, setOf(FieldType.Duration), mutableMapOf(), propertyName)
    }

    @JvmName("mapNullableEnum")
    protected fun <T : Enum<T>> map(
        fieldEnum: FieldEnum<T>,
        value: IValue<T?>,
        propertyName: String = ""
    ) {
        val fieldId = Field.bind(fieldEnum.field, setOf(FieldType.Enum, FieldType.EnumString))
        if (fieldEnum.field.type == FieldType.Enum) {
            if (options.action == EntityAction.ASSIGN) {
                options.portableEntity?.enumValues?.add(StoreEnum(fieldEnum.field.id, value.value?.ordinal))
            }
            if (populate(fieldEnum.field)) {
                options.portableEntity?.enumValues?.filter { it.key == fieldEnum.field.id }?.forEach {
                    value.set(fieldEnum.valueOf(it.value!!))
                }
            }
        } else {
            if (options.action == EntityAction.ASSIGN) {
                options.portableEntity?.enumStringValues?.add(
                    StoreEnumString(
                        fieldEnum.field.id,
                        value.value?.name
                    )
                )
            }
            if (populate(fieldEnum.field)) {
                options.portableEntity?.enumStringValues?.filter { it.key == fieldEnum.field.id }?.forEach {
                    value.set(fieldEnum.valueOf(it.value!!))
                }
            }
        }
        mapField(overview.entityId, fieldId, propertyName, options.skip())
        mapEnums(overview.entityId, fieldId)
    }

    @JvmName("mapEnum")
    protected fun <T : Enum<T>> map(
        fieldEnum: FieldEnum<T>,
        value: IValue<T>,
        propertyName: String = ""
    ) {
        val fieldId = Field.bind(fieldEnum.field, setOf(FieldType.Enum, FieldType.EnumString))
        if (fieldEnum.field.type == FieldType.Enum) {
            if (options.action == EntityAction.ASSIGN) {
                options.portableEntity?.enumValues?.add(StoreEnum(fieldEnum.field.id, value.value.ordinal))
            }
            if (populate(fieldEnum.field)) {
                options.portableEntity?.enumValues?.filter { it.key == fieldEnum.field.id }?.forEach {
                    value.set(fieldEnum.valueOf(it.value!!)!!)
                }
            }
        } else {
            if (options.action == EntityAction.ASSIGN) {
                options.portableEntity?.enumStringValues?.add(
                    StoreEnumString(
                        fieldEnum.field.id,
                        value.value.name
                    )
                )
            }
            if (populate(fieldEnum.field)) {
                options.portableEntity?.enumStringValues?.filter { it.key == fieldEnum.field.id }?.forEach {
                    value.set(fieldEnum.valueOf(it.value!!)!!)
                }
            }
        }
        mapField(overview.entityId, fieldId, propertyName, options.skip())
        mapEnums(overview.entityId, fieldId)
    }

    @JvmName("mapStrings")
    protected fun map(
        field: Field,
        collection: Collection<String>,
        propertyName: String = ""
    ): Collection<String> {
        if (options.action == EntityAction.ASSIGN) {
            options.portableEntity?.stringCollections?.add(StoreStringCollection(field.id, collection))
        }
        if (populate(field)) {
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
        collection: Collection<LocalDateTime>,
        propertyName: String = ""
    ): Collection<LocalDateTime> {
        if (options.action == EntityAction.ASSIGN) {
            options.portableEntity?.dateTimeCollection?.add(
                StoreDateTimeCollection(
                    field.id,
                    toTimeStampCollection(collection)
                )
            )
        }
        if (populate(field)) {
            options.portableEntity?.dateTimeCollection?.filter { it.key == field.id }?.forEach { match ->
                match.values.forEach { value ->
                    if (collection is MutableCollection) collection.add(value.toLocalDateTime())
                }
            }
        }
        mapField(overview.entityId, Field.bind(field, FieldType.DateTimeCollection), propertyName, options.skip())
        return emptyList()
    }

    @JvmName("mapFloats")
    protected fun map(
        field: Field,
        collection: Collection<Float>,
        name: String = ""
    ): Collection<Float> {
        if (options.action == EntityAction.ASSIGN) {
            options.portableEntity?.floatCollections?.add(StoreFloatCollection(field.id, collection))
        }
        if (populate(field)) {
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
        return options.action == EntityAction.POPULATE && populateProperty(DbContext.instance, field.id)
    }

    @JvmName("mapIntegers")
    protected fun map(
        field: Field,
        collection: Collection<Int>,
        propertyName: String = ""
    ): Collection<Int> {
        if (options.action == EntityAction.ASSIGN) {
            options.portableEntity?.integerCollections?.add(StoreIntegerCollection(field.id, collection))
        }
        if (populate(field)) {
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
        if (options.action == EntityAction.ASSIGN) {
            options.portableEntity?.shortCollections?.add(StoreShortCollection(field.id, collection))
        }
        if (populate(field)) {
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
        if (options.action == EntityAction.ASSIGN) {
            options.portableEntity?.uuidCollections?.add(
                StoreUuidCollection(
                    field.id,
                    toByteArrayCollection(collection)
                )
            )
        }
        if (populate(field)) {
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
        if (options.action == EntityAction.ASSIGN) {
            options.portableEntity?.doubleCollections?.add(StoreDoubleCollection(field.id, collection))
        }
        if (populate(field)) {
            options.portableEntity?.doubleCollections?.filter { it.key == field.id }?.forEach { match ->
                match.values.forEach {
                    if (collection is MutableCollection) collection.add(it)
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
        if (options.action == EntityAction.ASSIGN) {
            options.portableEntity?.longCollections?.add(StoreLongCollection(field.id, collection))
        }
        if (populate(field)) {
            options.portableEntity?.longCollections?.filter { it.key == field.id }?.forEach {
                it.values.forEach { if (collection is MutableCollection) collection.add(it) }
            }
        }
        mapField(overview.entityId, Field.bind(field, FieldType.LongCollection), propertyName, options.skip())
        return listOf()
    }

    @JvmName("mapIntMap")
    protected fun map(
        field: Field,
        map: Map<Int, String>,
        propertyName: String = ""
    ): Map<Int, String> {
        if (options.action == EntityAction.ASSIGN) {
            options.portableEntity?.mapIntKeyValues?.add(StoreMapIntKey(field.id, map))
        }
        if (populate(field)) {
            options.portableEntity?.mapIntKeyValues?.filter { it.key == field.id }?.forEach {
                if (map is MutableMap) map.putAll(it.values)
            }
        }
        mapField(overview.entityId, Field.bind(field, FieldType.MapIntKey), propertyName, options.skip())
        return emptyMap()
    }

    @JvmName("mapStringMap")
    protected fun map(
        field: Field,
        map: Map<String, String>,
        propertyName: String = ""
    ): Map<String, String> {
        if (options.action == EntityAction.ASSIGN) {
            options.portableEntity?.mapStringKeyValues?.add(StoreMapStringKey(field.id, map))
        }
        if (populate(field)) {
            options.portableEntity?.mapStringKeyValues?.filter { it.key == field.id }?.forEach {
                if (map is MutableMap) map.putAll(it.values)
            }
        }
        mapField(overview.entityId, Field.bind(field, FieldType.MapStringKey), propertyName, options.skip())
        return emptyMap()
    }

    @JvmName("mapOfCollections")
    protected fun map(
        field: Field,
        map: Map<String, MutableCollection<String>>,
        propertyName: String = ""
    ): Map<String, Collection<String>> {
        if (options.action == EntityAction.ASSIGN) {
            options.portableEntity?.mapOfCollectionsValues?.add(StoreMapCollection(field.id, map))
        }
        if (populate(field)) {
            options.portableEntity?.mapOfCollectionsValues?.filter { it.key == field.id }?.forEach {
                if (map is MutableMap) map.putAll(it.values)
            }
        }
        mapField(overview.entityId, Field.bind(field, FieldType.MapOfCollections), propertyName, options.skip())
        return emptyMap()
    }

    @JvmName("mapEnums")
    protected fun <T : Enum<T>> map(
        fieldEnum: FieldEnum<T>,
        collection: Collection<T>,
        propertyName: String = ""
    ): Collection<T> {
        val fieldId = Field.bind(fieldEnum.field, setOf(FieldType.EnumCollection, FieldType.EnumStringCollection))
        when (fieldEnum.field.type) {
            FieldType.EnumCollection -> {
                if (options.action == EntityAction.ASSIGN) {
                    options.portableEntity?.enumCollections?.add(
                        StoreEnumCollection(
                            fieldEnum.field.id,
                            toIntCollection(collection)
                        )
                    )
                }
                if (populate(fieldEnum.field)) {
                    options.portableEntity?.enumCollections?.filter { it.key == fieldEnum.field.id }?.forEach { match ->
                        match.values.forEach { ordinal ->
                            if (collection is MutableCollection) {
                                fieldEnum.values
                                    .filter { enumValue -> enumValue.ordinal == ordinal }
                                    .forEach { collection.add(it) }
                            }
                        }
                    }
                }
            }

            else -> {
                if (options.action == EntityAction.ASSIGN) {
                    options.portableEntity?.enumStringCollections?.add(
                        StoreEnumStringCollection(
                            fieldEnum.field.id,
                            toStringCollection(collection)
                        )
                    )
                }
                if (populate(fieldEnum.field)) {
                    options.portableEntity?.enumStringCollections?.filter { it.key == fieldEnum.field.id }
                        ?.forEach { match ->
                            match.values.forEach { value ->
                                if (collection is MutableCollection) {
                                    fieldEnum.valueOf(value)?.let { collection.add(it) }
                                }
                            }
                        }
                }
            }
        }
        mapField(overview.entityId, fieldId, propertyName, options.skip())
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
        bindFieldIdToEntity(fieldEntity, FieldType.Entity)
        if (options.action == EntityAction.ASSIGN) {
            val portableEntity = PortableEntity()
            portableEntity.fieldId = fieldEntity.field.id
            portableEntity.init(DbContext.instance!!, value.value)
            options.portableEntity?.entityOverviews?.add(portableEntity)
        }
        if (populate(fieldEntity.field)) {
            fun onSuccess(entity: IEntity) {
                value.value = entity as T
            }
            options.portableEntity?.entityOverviews?.filter { it.fieldId == fieldEntity.field.id }?.forEach {
                populateObjects(DbContext.instance!!, it, ::onSuccess)
            }
        }
        mapField(overview.entityId, fieldEntity.field.id, propertyName, options.skip())
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
        bindFieldIdToEntity(fieldEntity, FieldType.EntityCollection)
        if (options.action == EntityAction.ASSIGN) {
            collection.forEach { entity ->
                val portableEntity = PortableEntity()
                portableEntity.fieldId = fieldEntity.field.id
                portableEntity.init(DbContext.instance!!, entity)
                options.portableEntity?.entityOverviews?.add(portableEntity)
            }
        }
        if (populate(fieldEntity.field)) {
            fun onSuccess(entity: IEntity) {
                collection.add(entity as T)
            }
            options.portableEntity?.entityOverviews?.filter { it.fieldId == fieldEntity.field.id }?.forEach {
                populateObjects(DbContext.instance!!, it, ::onSuccess)
            }
        }

        mapField(overview.entityId, fieldEntity.field.id, propertyName, options.skip())
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
            entity.options.action = EntityAction.ASSIGN
            entity.options.portableEntity = portableEntity
            try {
                entity.bind()
            } finally {
                entity.options.action = EntityAction.NONE
                entity.options.portableEntity = null
            }
        }

        internal fun populate(entity: Entity, dbContext: DbContext, portableEntity: PortableEntity) {
            entity.options.action = EntityAction.POPULATE
            entity.options.portableEntity = portableEntity
            try {
                entity.bind()
            } finally {
                entity.options.action = EntityAction.NONE
                entity.options.portableEntity = null
            }
        }

        private fun populateObjects(
            dbContext: DbContext,
            portableEntity: PortableEntity,
            onSuccess: (e: IEntity) -> Unit
        ) {
            val referenceClass = classes[portableEntity.overview.entityId]
            if (referenceClass != null) {
                val subEntity = referenceClass.getDeclaredConstructor().newInstance()//create array element
                subEntity.overview.id = portableEntity.overview.id
                subEntity.overview.editVersion = portableEntity.overview.editVersion
                populate(subEntity, dbContext, portableEntity)
                onSuccess(subEntity)
            }
        }
    }
}