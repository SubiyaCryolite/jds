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
import io.github.subiyacryolite.jds.annotations.EntityAnnotation
import io.github.subiyacryolite.jds.beans.property.*
import io.github.subiyacryolite.jds.context.DbContext
import io.github.subiyacryolite.jds.enums.FieldType
import io.github.subiyacryolite.jds.extensions.*
import io.github.subiyacryolite.jds.portable.*
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
import kotlin.collections.ArrayList

/**
 * This class allows for all mapping operations in JDS, it also uses
 * [IOverview] to store overview data
 */
abstract class Entity : IEntity, Serializable {
    @set:JsonIgnore
    @get:JsonIgnore
    final override var overview: IOverview = Overview()
    //time constructs
    @get:JsonIgnore
    internal val localDateTimeValues: HashMap<Int, WritableValue<out Temporal?>> = HashMap()
    @get:JsonIgnore
    internal val zonedDateTimeValues: HashMap<Int, WritableValue<out Temporal?>> = HashMap()
    @get:JsonIgnore
    internal val localDateValues: HashMap<Int, WritableValue<out Temporal?>> = HashMap()
    @get:JsonIgnore
    internal val localTimeValues: HashMap<Int, WritableValue<out Temporal?>> = HashMap()
    @get:JsonIgnore
    internal val monthDayValues: HashMap<Int, WritableValue<MonthDay?>> = HashMap()
    @get:JsonIgnore
    internal val yearMonthValues: HashMap<Int, WritableValue<out Temporal?>> = HashMap()
    @get:JsonIgnore
    internal val periodValues: HashMap<Int, WritableValue<Period?>> = HashMap()
    @get:JsonIgnore
    internal val durationValues: HashMap<Int, WritableValue<Duration?>> = HashMap()
    //strings
    @get:JsonIgnore
    internal val stringValues: HashMap<Int, WritableValue<String?>> = HashMap()
    //boolean
    @get:JsonIgnore
    internal val booleanValues: HashMap<Int, WritableValue<Boolean?>> = HashMap()
    //numeric
    @get:JsonIgnore
    internal val shortValues: HashMap<Int, WritableValue<Short?>> = HashMap()
    @get:JsonIgnore
    internal val floatValues: HashMap<Int, WritableValue<Float?>> = HashMap()
    @get:JsonIgnore
    internal val doubleValues: HashMap<Int, WritableValue<Double?>> = HashMap()
    @get:JsonIgnore
    internal val longValues: HashMap<Int, WritableValue<Long?>> = HashMap()
    @get:JsonIgnore
    internal val integerValues: HashMap<Int, WritableValue<Int?>> = HashMap()
    @get:JsonIgnore
    internal val uuidValues: HashMap<Int, WritableValue<UUID?>> = HashMap()
    //arrays
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
    //enumValues - enums can be null, enum collections cannot (skip unknown entries)
    @get:JsonIgnore
    internal val enumValues: HashMap<Int, WritableValue<Enum<*>?>> = HashMap()
    @get:JsonIgnore
    internal val stringEnumValues: HashMap<Int, WritableValue<Enum<*>?>> = HashMap()
    @get:JsonIgnore
    internal val enumCollections: HashMap<Int, MutableCollection<Enum<*>>> = HashMap()
    @get:JsonIgnore
    internal val enumStringCollections: HashMap<Int, MutableCollection<Enum<*>>> = HashMap()
    //objects
    @get:JsonIgnore
    override val objectValues: HashMap<FieldEntity<*>, WritableValue<out IEntity>> = HashMap()
    //blobs
    @get:JsonIgnore
    internal val blobValues: HashMap<Int, WritableValue<ByteArray?>> = HashMap()

    init {
        val entityAnnotation = getEntityAnnotation(javaClass)
        if (entityAnnotation != null) {
            overview.entityId = entityAnnotation.id
        } else {
            throw RuntimeException("You must annotate the class [" + javaClass.canonicalName + "] or its immediate parent with [" + EntityAnnotation::class.java + "]")
        }
    }

    @JvmName("mapShort")
    protected fun map(field: Field, value: Short?) = map(field, SimpleObjectProperty(value))

    @JvmName("mapShort")
    protected fun map(field: Field, property: WritableValue<Short?>): WritableValue<Short?> {
        if (field.type != FieldType.Short) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        shortValues[mapField(overview.entityId, field.bind())] = property
        return property
    }

    @JvmName("mapDouble")
    protected fun map(field: Field, value: Double?) = map(field, SimpleObjectProperty(value))

    @JvmName("mapDouble")
    protected fun map(field: Field, value: DoubleProperty) = map(field, value as WritableValue<Double?>)

    @JvmName("mapDouble")
    protected fun map(field: Field, property: WritableValue<Double?>): WritableValue<Double?> {
        if (field.type != FieldType.Double) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        doubleValues[mapField(overview.entityId, field.bind())] = property
        return property
    }

    @JvmName("mapInt")
    protected fun map(field: Field, value: Int?) = map(field, SimpleObjectProperty(value))

    @JvmName("mapInt")
    protected fun map(field: Field, value: IntegerProperty) = map(field, value as WritableValue<Int?>)

    @JvmName("mapInt")
    protected fun map(field: Field, property: WritableValue<Int?>): WritableValue<Int?> {
        if (field.type != FieldType.Int) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        integerValues[mapField(overview.entityId, field.bind())] = property
        return property
    }

    @JvmName("mapLong")
    protected fun map(field: Field, value: Long?) = map(field, SimpleObjectProperty(value))

    @JvmName("mapLong")
    protected fun map(field: Field, value: LongProperty) = map(field, value as WritableValue<Long?>)

    @JvmName("mapLong")
    protected fun map(field: Field, property: WritableValue<Long?>): WritableValue<Long?> {
        if (field.type != FieldType.Long) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        longValues[mapField(overview.entityId, field.bind())] = property
        return property
    }

    @JvmName("mapFloat")
    protected fun map(field: Field, value: Float?) = map(field, SimpleObjectProperty(value))

    @JvmName("mapFloat")
    protected fun map(field: Field, value: FloatProperty) = map(field, value as WritableValue<Float?>)

    @JvmName("mapFloat")
    protected fun map(field: Field, property: WritableValue<Float?>): WritableValue<Float?> {
        if (field.type != FieldType.Float) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        floatValues[mapField(overview.entityId, field.bind())] = property
        return property
    }

    @JvmName("mapBoolean")
    protected fun map(field: Field, value: Boolean?) = map(field, SimpleObjectProperty(value))

    @JvmName("mapBoolean")
    protected fun map(field: Field, property: WritableValue<Boolean?>): WritableValue<Boolean?> {
        if (field.type != FieldType.Boolean) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        booleanValues[mapField(overview.entityId, field.bind())] = property
        return property
    }

    @JvmName("mapUuid")
    protected fun map(field: Field, value: UUID?) = map(field, SimpleObjectProperty(value))

    @JvmName("mapUuid")
    protected fun map(field: Field, property: WritableValue<UUID?>): WritableValue<UUID?> {
        if (field.type != FieldType.Uuid) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        uuidValues[mapField(overview.entityId, field.bind())] = property
        return property
    }

    @JvmName("mapString")
    protected fun map(field: Field, value: String?) = map(field, SimpleStringProperty(value))

    @JvmName("mapString")
    protected fun map(field: Field, property: WritableValue<String?>): WritableValue<String?> {
        if (field.type != FieldType.String) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        stringValues[mapField(overview.entityId, field.bind())] = property
        return property
    }

    @JvmName("mapDateTime")
    protected fun map(field: Field, value: LocalDateTime?) = map(field, SimpleObjectProperty(value))

    @JvmName("mapDateTime")
    protected fun map(field: Field, property: WritableValue<LocalDateTime?>): WritableValue<LocalDateTime?> {
        if (field.type != FieldType.DateTime) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        localDateTimeValues[mapField(overview.entityId, field.bind())] = property
        return property
    }

    @JvmName("mapZonedDateTime")
    protected fun map(field: Field, value: ZonedDateTime?) = map(field, SimpleObjectProperty(value))

    @JvmName("mapZonedDateTime")
    protected fun map(field: Field, property: WritableValue<ZonedDateTime?>): WritableValue<ZonedDateTime?> {
        if (field.type != FieldType.ZonedDateTime) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        zonedDateTimeValues[mapField(overview.entityId, field.bind())] = property
        return property
    }

    @JvmName("mapDate")
    protected fun map(field: Field, value: LocalDate?) = map(field, SimpleObjectProperty(value))

    @JvmName("mapDate")
    protected fun map(field: Field, property: WritableValue<LocalDate?>): WritableValue<LocalDate?> {
        if (field.type != FieldType.Date) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        localDateValues[mapField(overview.entityId, field.bind())] = property
        return property
    }

    @JvmName("mapTime")
    protected fun map(field: Field, value: LocalTime?) = map(field, SimpleObjectProperty(value))

    @JvmName("mapTime")
    protected fun map(field: Field, property: WritableValue<LocalTime?>): WritableValue<LocalTime?> {
        if (field.type != FieldType.Time) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        localTimeValues[mapField(overview.entityId, field.bind())] = property
        return property
    }

    @JvmName("mapBlob")
    protected fun map(field: Field, value: ByteArray?) = map(field, SimpleObjectProperty(value))

    @JvmName("mapBlob")
    protected fun map(field: Field, property: WritableValue<ByteArray?>): WritableValue<ByteArray?> {
        if (field.type != FieldType.Blob) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        blobValues[mapField(overview.entityId, field.bind())] = property
        return property
    }

    @JvmName("mapMonthDay")
    protected fun map(field: Field, value: MonthDay?) = map(field, SimpleObjectProperty(value))

    @JvmName("mapMonthDay")
    protected fun map(field: Field, property: WritableValue<MonthDay?>): WritableValue<MonthDay?> {
        if (field.type != FieldType.MonthDay) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        monthDayValues[mapField(overview.entityId, field.bind())] = property
        return property
    }

    @JvmName("mapYearMonth")
    protected fun map(field: Field, value: YearMonth?) = map(field, SimpleObjectProperty(value))

    @JvmName("mapYearMonth")
    protected fun map(field: Field, property: WritableValue<YearMonth?>): WritableValue<YearMonth?> {
        if (field.type != FieldType.YearMonth) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        yearMonthValues[mapField(overview.entityId, field.bind())] = property
        return property
    }

    @JvmName("mapPeriod")
    protected fun map(field: Field, value: Period?) = map(field, SimpleObjectProperty(value))

    @JvmName("mapPeriod")
    protected fun map(field: Field, property: WritableValue<Period?>): WritableValue<Period?> {
        if (field.type != FieldType.Period) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        periodValues[mapField(overview.entityId, field.bind())] = property
        return property
    }

    @JvmName("mapDuration")
    protected fun map(field: Field, value: Duration?) = map(field, SimpleObjectProperty(value))

    @JvmName("mapDuration")
    protected fun map(field: Field, property: WritableValue<Duration?>): WritableValue<Duration?> {
        if (field.type != FieldType.Duration) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        durationValues[mapField(overview.entityId, field.bind())] = property
        return property
    }

    @JvmName("mapEnum")
    protected fun <T : Enum<T>> map(fieldEnum: FieldEnum<T>, value: T?) = map(fieldEnum, SimpleObjectProperty(value))

    @JvmName("mapEnum")
    protected fun <T : Enum<T>> map(fieldEnum: FieldEnum<T>, property: WritableValue<T?>): WritableValue<T?> {
        if (fieldEnum.field.type != FieldType.Enum && fieldEnum.field.type != FieldType.EnumString) {
            throw RuntimeException("Incorrect type supplied for field [$fieldEnum.field]")
        }
        if (fieldEnum.field.type == FieldType.Enum) {
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
    protected fun map(field: Field, collection: MutableCollection<String>): MutableCollection<String> {
        if (field.type != FieldType.StringCollection) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        stringCollections[mapField(overview.entityId, field.bind())] = collection
        return collection
    }

    @JvmName("mapDateTimes")
    protected fun map(field: Field, collection: MutableCollection<LocalDateTime>): MutableCollection<LocalDateTime> {
        if (field.type != FieldType.DateTimeCollection) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        dateTimeCollections[mapField(overview.entityId, field.bind())] = collection
        return collection
    }

    @JvmName("mapFloats")
    protected fun map(field: Field, collection: MutableCollection<Float>): MutableCollection<Float> {
        if (field.type != FieldType.FloatCollection) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        floatCollections[mapField(overview.entityId, field.bind())] = collection
        return collection
    }

    @JvmName("mapIntegers")
    protected fun map(field: Field, collection: MutableCollection<Int>): MutableCollection<Int> {
        if (field.type != FieldType.IntCollection) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        integerCollections[mapField(overview.entityId, field.bind())] = collection
        return collection
    }

    @JvmName("mapDoubles")
    protected fun map(field: Field, collection: MutableCollection<Double>): MutableCollection<Double> {
        if (field.type != FieldType.DoubleCollection) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        doubleCollections[mapField(overview.entityId, field.bind())] = collection
        return collection
    }

    @JvmName("mapLongs")
    protected fun map(field: Field, collection: MutableCollection<Long>): MutableCollection<Long> {
        if (field.type != FieldType.LongCollection) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        longCollections[mapField(overview.entityId, field.bind())] = collection
        return collection
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

    protected fun <T : IEntity> map(fieldEntity: FieldEntity<T>, entity: T): WritableValue<T> {
        return map(fieldEntity, SimpleObjectProperty(entity))
    }

    protected fun <T : IEntity> map(fieldEntity: FieldEntity<T>, property: WritableValue<T>): WritableValue<T> {
        if (fieldEntity.field.type != FieldType.Entity) {
            throw RuntimeException("Please assign the correct type to field [$fieldEntity]")
        }
        if (!objectCollections.containsKey(fieldEntity) && !objectValues.containsKey(fieldEntity)) {
            bindFieldIdToEntity(fieldEntity)

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
    protected fun <T : IEntity> map(fieldEntity: FieldEntity<T>, collection: MutableCollection<T>): MutableCollection<T> {
        if (fieldEntity.field.type != FieldType.EntityCollection) {
            throw RuntimeException("Please supply a valid type for JdsFieldEntity")
        }
        if (!objectCollections.containsKey(fieldEntity)) {
            bindFieldIdToEntity(fieldEntity)

            objectCollections[fieldEntity] = collection as MutableCollection<IEntity>
            mapField(overview.entityId, fieldEntity.field.id)
        } else {
            throw RuntimeException("You can only bind a class to one property. This class is already bound to one object or object array")
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
        objectValues.forEach { (fieldEntity, objectProperty) ->
            val embeddedObject = PortableEntity()
            embeddedObject.fieldId = fieldEntity.field.id
            embeddedObject.init(dbContext, objectProperty.value)
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
        initBackingPropertyIfNotDefined(fieldType, fieldId)

        if (!dbContext.options.populateSensitiveData) {
            if (Field.values[fieldId]!!.sensitive) {
                return
            }
        }

        when (fieldType) {

            FieldType.Float -> floatValues[fieldId]?.value = when (value) {
                is Double -> value.toFloat()
                else -> value as Float?
            }

            FieldType.Double -> doubleValues[fieldId]?.value = value as Double?

            FieldType.Short -> shortValues[fieldId]?.value = when (value) {
                is Double -> value.toShort()
                is Int -> value.toShort()
                is Short? -> value
                else -> null
            }

            FieldType.Long -> longValues[fieldId]?.value = when (value) {
                is Long? -> value
                is BigDecimal -> value.toLong() //Oracle
                is Int -> value.toLong()
                else -> null
            }

            FieldType.Int -> integerValues[fieldId]?.value = when (value) {
                is Int? -> value
                is BigDecimal -> value.toInt() //Oracle
                else -> null
            }

            FieldType.Uuid -> uuidValues[fieldId]?.value = when (value) {
                is ByteArray? -> value.toUuid()
                is String -> UUID.fromString(value)
                is UUID? -> value
                else -> null
            }

            FieldType.Boolean -> booleanValues[fieldId]?.value = when (value) {
                is Int -> value == 1
                is Boolean? -> value
                is BigDecimal -> value.intValueExact() == 1 //Oracle
                else -> null
            }

            FieldType.DoubleCollection -> doubleCollections[fieldId]?.add(value as Double)

            FieldType.FloatCollection -> floatCollections[fieldId]?.add(value as Float)

            FieldType.LongCollection -> longCollections[fieldId]?.add(value as Long)

            FieldType.IntCollection -> integerCollections[fieldId]?.add(value as Int)

            //Enums may be null
            FieldType.Enum -> enumValues.filter { it.key == fieldId }.forEach {
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

            //Enums may be null
            FieldType.EnumString -> enumValues.filter { it.key == fieldId }.forEach {
                val fieldEnum = FieldEnum.enums[it.key]
                if (fieldEnum != null && value is String)
                    it.value.value = fieldEnum.valueOf(value)
            }

            //Enum collections should NOT accept nulls. Unknown collection should be skipped
            FieldType.EnumCollection -> enumCollections.filter { it.key == fieldId }.forEach {
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

            //Enum collections should NOT accept nulls. Unknown collection should be skipped
            FieldType.EnumStringCollection -> enumStringCollections.filter { it.key == fieldId }.forEach {
                val fieldEnum = FieldEnum.enums[it.key]
                if (fieldEnum != null) {
                    val enumStr = value.toString()
                    val enumVal = fieldEnum.valueOf(enumStr)
                    if (enumVal != null)
                        it.value.add(enumVal)
                }
            }

            FieldType.String -> stringValues[fieldId]?.value = (value as String?)

            FieldType.StringCollection -> stringCollections[fieldId]?.add(value as String)

            FieldType.ZonedDateTime -> when (value) {
                is Long -> zonedDateTimeValues[fieldId]?.value = ZonedDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault())
                is Timestamp -> zonedDateTimeValues[fieldId]?.value = ZonedDateTime.ofInstant(value.toInstant(), ZoneOffset.systemDefault())
                is String -> zonedDateTimeValues[fieldId]?.value = value.toZonedDateTime()
                is OffsetDateTime -> zonedDateTimeValues[fieldId]?.value = value.atZoneSameInstant(ZoneId.systemDefault())
            }

            FieldType.Date -> when (value) {
                is Timestamp -> localDateValues[fieldId]?.value = (value.toLocalDateTime().toLocalDate())
                is LocalDate -> localDateValues[fieldId]?.value = (value)
                is String -> localDateValues[fieldId]?.value = (value.toLocalDate())
                else -> localDateValues[fieldId]?.value = null
            }

            FieldType.Time -> when (value) {
                is Long -> localTimeValues[fieldId]?.value = (LocalTime.MIN.plusNanos(value))
                is LocalTime -> localTimeValues[fieldId]?.value = (value)
                is String -> localTimeValues[fieldId]?.value = (value.toLocalTime())
            }

            FieldType.Duration -> durationValues[fieldId]?.value = (when (value) {
                is BigDecimal -> Duration.ofNanos(value.longValueExact())//Oracle
                else -> Duration.ofNanos(value as Long)
            })

            FieldType.MonthDay -> monthDayValues[fieldId]?.value = if (value is String) MonthDay.parse(value) else null

            FieldType.YearMonth -> yearMonthValues[fieldId]?.value = if (value is String) YearMonth.parse(value) else null

            FieldType.Period -> periodValues[fieldId]?.value = if (value is String) Period.parse(value) else null

            FieldType.DateTime -> localDateTimeValues[fieldId]?.value = if (value is Timestamp) value.toLocalDateTime() else null

            FieldType.DateTimeCollection -> dateTimeCollections[fieldId]?.add((value as Timestamp).toLocalDateTime())

            FieldType.Blob -> when (value) {
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
    private fun initBackingPropertyIfNotDefined(fieldType: FieldType, fieldId: Int) {
        when (fieldType) {

            FieldType.String -> if (!stringValues.containsKey(fieldId))
                stringValues[fieldId] = SimpleStringProperty("")

            FieldType.DoubleCollection -> if (!doubleCollections.containsKey(fieldId))
                doubleCollections[fieldId] = ArrayList()

            FieldType.FloatCollection -> if (!floatCollections.containsKey(fieldId))
                floatCollections[fieldId] = ArrayList()

            FieldType.LongCollection -> if (!longCollections.containsKey(fieldId))
                longCollections[fieldId] = ArrayList()

            FieldType.IntCollection -> if (!integerCollections.containsKey(fieldId))
                integerCollections[fieldId] = ArrayList()

            FieldType.StringCollection -> if (!stringCollections.containsKey(fieldId))
                stringCollections[fieldId] = ArrayList()

            FieldType.DateTimeCollection -> if (!dateTimeCollections.containsKey(fieldId))
                dateTimeCollections[fieldId] = ArrayList()

            FieldType.EnumCollection -> if (!enumCollections.containsKey(fieldId))
                enumCollections[fieldId] = ArrayList()

            FieldType.ZonedDateTime -> if (!zonedDateTimeValues.containsKey(fieldId))
                zonedDateTimeValues[fieldId] = SimpleObjectProperty()

            FieldType.Date -> if (!localDateValues.containsKey(fieldId))
                localDateValues[fieldId] = SimpleObjectProperty()

            FieldType.Time -> if (!localTimeValues.containsKey(fieldId))
                localTimeValues[fieldId] = SimpleObjectProperty()

            FieldType.Duration -> if (!durationValues.containsKey(fieldId))
                durationValues[fieldId] = SimpleObjectProperty()

            FieldType.MonthDay -> if (!monthDayValues.containsKey(fieldId))
                monthDayValues[fieldId] = SimpleObjectProperty()

            FieldType.YearMonth -> if (!yearMonthValues.containsKey(fieldId))
                yearMonthValues[fieldId] = SimpleObjectProperty()

            FieldType.Period -> if (!periodValues.containsKey(fieldId))
                periodValues[fieldId] = SimpleObjectProperty()

            FieldType.DateTime -> if (!localDateTimeValues.containsKey(fieldId))
                localDateTimeValues[fieldId] = SimpleObjectProperty<Temporal>()

            FieldType.Blob -> if (!blobValues.containsKey(fieldId))
                blobValues[fieldId] = SimpleBlobProperty(byteArrayOf())

            FieldType.Enum -> if (!enumValues.containsKey(fieldId))
                enumValues[fieldId] = SimpleObjectProperty()

            FieldType.Float -> if (!floatValues.containsKey(fieldId))
                floatValues[fieldId] = NullableFloatProperty()

            FieldType.Double -> if (!doubleValues.containsKey(fieldId))
                doubleValues[fieldId] = NullableDoubleProperty()

            FieldType.Short -> if (!shortValues.containsKey(fieldId))
                shortValues[fieldId] = NullableShortProperty()

            FieldType.Long -> if (!longValues.containsKey(fieldId))
                longValues[fieldId] = NullableLongProperty()

            FieldType.Int -> if (!integerValues.containsKey(fieldId))
                integerValues[fieldId] = NullableIntegerProperty()

            FieldType.Uuid -> if (!uuidValues.containsKey(fieldId))
                uuidValues[fieldId] = SimpleObjectProperty()

            FieldType.Boolean -> if (!booleanValues.containsKey(fieldId))
                booleanValues[fieldId] = NullableBooleanProperty()
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
                if (it.value.value == null) {
                    it.value.value = dbContext.classes[entityId]!!.getDeclaredConstructor().newInstance()
                }
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
        objectValues.values.forEach { objectProperty -> yieldAll(objectProperty.value.getNestedEntities()) }
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
