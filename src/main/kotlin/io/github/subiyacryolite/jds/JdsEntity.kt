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
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
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
    internal val objectCollections: HashMap<JdsFieldEntity<*>, MutableCollection<JdsEntity>> = HashMap()
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
    internal val objectValues: HashMap<JdsFieldEntity<*>, ObjectProperty<JdsEntity>> = HashMap()
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

    @JvmName("mapNumeric")
    protected fun map(field: JdsField, property: WritableValue<out Number?>) {
        if (
                field.type != JdsFieldType.DOUBLE &&
                field.type != JdsFieldType.INT &&
                field.type != JdsFieldType.LONG &&
                field.type != JdsFieldType.FLOAT &&
                field.type != JdsFieldType.SHORT
        ) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        field.bind()
        try {
            if (field.type == JdsFieldType.SHORT) {
                shortValues[field.id] = property as WritableValue<Short?>
            }
            if (field.type == JdsFieldType.DOUBLE) {
                doubleValues[field.id] = property as WritableValue<Double?>
            }
            if (field.type == JdsFieldType.INT) {
                integerValues[field.id] = property as WritableValue<Int?>
            }
            if (field.type == JdsFieldType.LONG) {
                longValues[field.id] = property as WritableValue<Long?>
            }
            if (field.type == JdsFieldType.FLOAT) {
                floatValues[field.id] = property as WritableValue<Float?>
            }
        } catch (ex: java.lang.Exception) {
            throw RuntimeException("Incorrect numeric type supplied for field [$field]")
        }
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapBoolean")
    protected fun map(field: JdsField, property: WritableValue<Boolean?>) {
        if (field.type != JdsFieldType.BOOLEAN) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        field.bind()
        booleanValues[field.id] = property
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapUuid")
    protected fun map(field: JdsField, property: WritableValue<UUID?>) {
        if (field.type != JdsFieldType.UUID) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        field.bind()
        uuidValues[field.id] = property
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapString")
    protected fun map(field: JdsField, property: WritableValue<String?>) {
        if (field.type != JdsFieldType.STRING) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        field.bind()
        stringValues[field.id] = property
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapDateTime")
    protected fun map(field: JdsField, property: WritableValue<LocalDateTime?>) {
        if (field.type != JdsFieldType.DATE_TIME) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        field.bind()
        localDateTimeValues[field.id] = property
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapZonedDateTime")
    protected fun map(field: JdsField, property: WritableValue<ZonedDateTime?>) {
        if (field.type != JdsFieldType.ZONED_DATE_TIME) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        field.bind()
        zonedDateTimeValues[field.id] = property
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapDate")
    protected fun map(field: JdsField, property: WritableValue<LocalDate?>) {
        if (field.type != JdsFieldType.DATE) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        field.bind()
        localDateValues[field.id] = property
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapTime")
    protected fun map(field: JdsField, property: WritableValue<LocalTime?>) {
        if (field.type != JdsFieldType.TIME) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        field.bind()
        localTimeValues[field.id] = property
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapBlob")
    protected fun map(field: JdsField, property: WritableValue<ByteArray?>) {
        if (field.type != JdsFieldType.BLOB) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        field.bind()
        blobValues[field.id] = property
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapMonthDay")
    protected fun map(field: JdsField, property: WritableValue<MonthDay?>) {
        if (field.type != JdsFieldType.MONTH_DAY) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        field.bind()
        monthDayValues[field.id] = property
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapYearMonth")
    protected fun map(field: JdsField, property: WritableValue<YearMonth?>) {
        if (field.type != JdsFieldType.YEAR_MONTH) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        field.bind()
        yearMonthValues[field.id] = property
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapPeriod")
    protected fun map(field: JdsField, property: WritableValue<Period?>) {
        if (field.type != JdsFieldType.PERIOD) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        field.bind()
        periodValues[field.id] = property
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapDuration")
    protected fun map(field: JdsField, property: WritableValue<Duration?>) {
        if (field.type != JdsFieldType.DURATION) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        field.bind()
        durationValues[field.id] = property
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapEnum")
    protected fun <T : Enum<T>> map(fieldEnum: JdsFieldEnum<T>, property: WritableValue<T?>) {
        if (fieldEnum.field.type != JdsFieldType.ENUM && fieldEnum.field.type != JdsFieldType.ENUM_STRING) {
            throw RuntimeException("Incorrect type supplied for field [$fieldEnum.field]")
        }
        if (fieldEnum.field.type == JdsFieldType.ENUM) {
            enumValues[fieldEnum.field.id] = property as WritableValue<Enum<*>?>
        } else {
            stringEnumValues[fieldEnum.field.id] = property as WritableValue<Enum<*>?>
        }
        fieldEnum.field.bind()
        mapField(overview.entityId, fieldEnum.field.id)
        mapEnums(overview.entityId, fieldEnum.field.id)
    }

    @JvmName("mapStrings")
    protected fun map(field: JdsField, collection: MutableCollection<String>) {
        if (field.type != JdsFieldType.STRING_COLLECTION) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        field.bind()
        stringCollections[field.id] = collection
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapDateTimes")
    protected fun map(field: JdsField, collection: MutableCollection<LocalDateTime>) {
        if (field.type != JdsFieldType.DATE_TIME_COLLECTION) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        field.bind()
        dateTimeCollections[field.id] = collection
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapFloats")
    protected fun map(field: JdsField, collection: MutableCollection<Float>) {
        if (field.type != JdsFieldType.FLOAT_COLLECTION) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        field.bind()
        floatCollections[field.id] = collection
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapIntegers")
    protected fun map(field: JdsField, collection: MutableCollection<Int>) {
        if (field.type != JdsFieldType.INT_COLLECTION) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        field.bind()
        integerCollections[field.id] = collection
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapDoubles")
    protected fun map(field: JdsField, collection: MutableCollection<Double>) {
        if (field.type != JdsFieldType.DOUBLE_COLLECTION) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        field.bind()
        doubleCollections[field.id] = collection
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapLongs")
    protected fun map(field: JdsField, collection: MutableCollection<Long>) {
        if (field.type != JdsFieldType.LONG_COLLECTION) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
        field.bind()
        longCollections[field.id] = collection
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapEnums")
    protected fun <T : Enum<T>> map(fieldEnum: JdsFieldEnum<T>, collection: MutableCollection<T>) {
        if (fieldEnum.field.type != JdsFieldType.ENUM_COLLECTION && fieldEnum.field.type != JdsFieldType.ENUM_STRING_COLLECTION) {
            throw RuntimeException("Incorrect type supplied for field [$fieldEnum.field]")
        }
        if (fieldEnum.field.type == JdsFieldType.ENUM_COLLECTION) {
            enumCollections[fieldEnum.field.id] = collection as MutableCollection<Enum<*>>
        } else {
            enumStringCollections[fieldEnum.field.id] = collection as MutableCollection<Enum<*>>
        }
        fieldEnum.field.bind()
        mapField(overview.entityId, fieldEnum.field.id)
        mapEnums(overview.entityId, fieldEnum.field.id)
    }

    /**
     * @param fieldEntity
     * @param property
     */
    protected fun <T : IJdsEntity> map(fieldEntity: JdsFieldEntity<T>, property: ObjectProperty<T>) {
        if (fieldEntity.field.type != JdsFieldType.ENTITY) {
            throw RuntimeException("Please assign the correct type to field [$fieldEntity]")
        }
        if (!objectCollections.containsKey(fieldEntity) && !objectValues.containsKey(fieldEntity)) {
            fieldEntity.field.bind()
            objectValues[fieldEntity] = property as ObjectProperty<JdsEntity>
            mapField(overview.entityId, fieldEntity.field.id)
        } else {
            throw RuntimeException("You can only bind a class to one property. This class is already bound to one object or object array")
        }
    }

    /**
     * @param fieldEntity
     * @param collection
     */
    protected fun <T : IJdsEntity> map(fieldEntity: JdsFieldEntity<T>, collection: MutableCollection<T>) {
        if (fieldEntity.field.type != JdsFieldType.ENTITY_COLLECTION) {
            throw RuntimeException("Please supply a valid type for JdsFieldEntity")
        }
        if (!objectCollections.containsKey(fieldEntity)) {
            fieldEntity.field.bind()
            objectCollections[fieldEntity] = collection as MutableCollection<JdsEntity>
            mapField(overview.entityId, fieldEntity.field.id)
        } else {
            throw RuntimeException("You can only bind a class to one property. This class is already bound to one object or object array")
        }
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
     * @param embeddedObject
     */
    @Throws(Exception::class)
    internal fun assign(embeddedObject: JdsEmbeddedObject) {
        //==============================================
        //PRIMITIVES, also saved to array struct to streamline json
        //==============================================
        booleanValues.entries.forEach {
            val input = when (it.value.value) {
                true -> 1
                false -> 0
                else -> null
            }
            embeddedObject.booleanValues.add(JdsStoreBoolean(it.key, input))
        }
        stringValues.entries.forEach { embeddedObject.stringValues.add(JdsStoreString(it.key, it.value.value)) }
        floatValues.entries.forEach { embeddedObject.floatValue.add(JdsStoreFloat(it.key, it.value.value)) }
        doubleValues.entries.forEach { embeddedObject.doubleValues.add(JdsStoreDouble(it.key, it.value.value)) }
        shortValues.entries.forEach { embeddedObject.shortValues.add(JdsStoreShort(it.key, it.value.value)) }
        longValues.entries.forEach { embeddedObject.longValues.add(JdsStoreLong(it.key, it.value.value)) }
        integerValues.entries.forEach { embeddedObject.integerValues.add(JdsStoreInteger(it.key, it.value.value)) }
        uuidValues.entries.forEach { embeddedObject.uuidValues.add(JdsStoreUuid(it.key, it.value.value.toByteArray())) }
        //==============================================
        //Dates & Time
        //==============================================
        zonedDateTimeValues.entries.forEach { embeddedObject.zonedDateTimeValues.add(JdsStoreZonedDateTime(it.key, (it.value.value as ZonedDateTime?)?.toInstant()?.toEpochMilli())) }
        localTimeValues.entries.forEach { embeddedObject.timeValues.add(JdsStoreTime(it.key, (it.value.value as LocalTime?)?.toNanoOfDay())) }
        durationValues.entries.forEach { embeddedObject.durationValues.add(JdsStoreDuration(it.key, it.value.value?.toNanos())) }
        localDateTimeValues.entries.forEach { embeddedObject.dateTimeValues.add(JdsStoreDateTime(it.key, safeLocalDateTime(it.value.value))) }
        localDateValues.entries.forEach { embeddedObject.dateValues.add(JdsStoreDate(it.key, safeLocalDate(it.value.value))) }
        monthDayValues.entries.forEach { embeddedObject.monthDayValues.add(JdsStoreMonthDay(it.key, it.value.value?.toString())) }
        yearMonthValues.entries.forEach { embeddedObject.yearMonthValues.add(JdsStoreYearMonth(it.key, (it.value.value as YearMonth?)?.toString())) }
        periodValues.entries.forEach { embeddedObject.periodValues.add(JdsStorePeriod(it.key, it.value.value?.toString())) }
        //==============================================
        //BLOB
        //==============================================
        blobValues.entries.forEach {
            embeddedObject.blobValues.add(JdsStoreBlob(it.key, it.value.value ?: ByteArray(0)))
        }
        //==============================================
        //Enums
        //==============================================
        enumValues.entries.forEach { embeddedObject.enumValues.add(JdsStoreEnum(it.key, it.value.value?.ordinal)) }
        stringEnumValues.entries.forEach { embeddedObject.enumStringValues.add(JdsStoreEnumString(it.key, it.value.value?.name)) }
        enumCollections.entries.forEach { embeddedObject.enumCollections.add(JdsStoreEnumCollection(it.key, toIntCollection(it.value))) }
        enumStringCollections.entries.forEach { embeddedObject.enumStringCollections.add(JdsStoreEnumStringCollection(it.key, toStringCollection(it.value))) }
        //==============================================
        //ARRAYS
        //==============================================
        stringCollections.entries.forEach { embeddedObject.stringCollections.add(JdsStoreStringCollection(it.key, it.value)) }
        dateTimeCollections.entries.forEach { embeddedObject.dateTimeCollection.add(JdsStoreDateTimeCollection(it.key, toTimeStampCollection(it.value))) }
        floatCollections.entries.forEach { embeddedObject.floatCollections.add(JdsStoreFloatCollection(it.key, it.value)) }
        doubleCollections.entries.forEach { embeddedObject.doubleCollections.add(JdsStoreDoubleCollection(it.key, it.value)) }
        longCollections.entries.forEach { embeddedObject.longCollections.add(JdsStoreLongCollection(it.key, it.value)) }
        integerCollections.entries.forEach { embeddedObject.integerCollections.add(JdsStoreIntegerCollection(it.key, it.value)) }
        //==============================================
        //EMBEDDED OBJECTS
        //==============================================
        objectCollections.forEach { (key, itx) ->
            itx.forEach {
                val eo = JdsEmbeddedObject()
                eo.fieldId = key.field.id
                eo.init(it)
                embeddedObject.entityOverviews.add(eo)
            }
        }
        objectValues.forEach { (key, it) ->
            val eo = JdsEmbeddedObject()
            eo.fieldId = key.field.id
            eo.init(it.value)
            embeddedObject.entityOverviews.add(eo)
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

            JdsFieldType.FLOAT -> floatValues[fieldId]?.value = when (value) {
                is Double -> value.toFloat()
                else -> value as Float?
            }

            JdsFieldType.DOUBLE -> doubleValues[fieldId]?.value = value as Double?

            JdsFieldType.SHORT -> shortValues[fieldId]?.value = when (value) {
                is Double -> value.toShort()
                is Int -> value.toShort()
                is Short? -> value
                else -> null
            }

            JdsFieldType.LONG -> longValues[fieldId]?.value = when (value) {
                is Long? -> value
                is BigDecimal -> value.toLong() //Oracle
                is Int -> value.toLong()
                else -> null
            }

            JdsFieldType.INT -> integerValues[fieldId]?.value = when (value) {
                is Int? -> value
                is BigDecimal -> value.toInt() //Oracle
                else -> null
            }

            JdsFieldType.UUID -> uuidValues[fieldId]?.value = when (value) {
                is ByteArray? -> value.toUuid()
                is String -> UUID.fromString(value)
                is UUID? -> value
                else -> null
            }

            JdsFieldType.BOOLEAN -> booleanValues[fieldId]?.value = when (value) {
                is Int -> value == 1
                is Boolean? -> value
                is BigDecimal -> value.intValueExact() == 1 //Oracle
                else -> null
            }

            JdsFieldType.DOUBLE_COLLECTION -> doubleCollections[fieldId]?.add(value as Double)

            JdsFieldType.FLOAT_COLLECTION -> floatCollections[fieldId]?.add(value as Float)

            JdsFieldType.LONG_COLLECTION -> longCollections[fieldId]?.add(value as Long)

            JdsFieldType.INT_COLLECTION -> integerCollections[fieldId]?.add(value as Int)

            //Enums may be null
            JdsFieldType.ENUM -> enumValues.filter { it.key == fieldId }.forEach {
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
            JdsFieldType.ENUM_STRING -> enumValues.filter { it.key == fieldId }.forEach {
                val fieldEnum = JdsFieldEnum.enums[it.key]
                if (fieldEnum != null && value is String)
                    it.value.value = fieldEnum.valueOf(value)
            }

            //Enum collections should NOT accept nulls. Unknown collection should be skipped
            JdsFieldType.ENUM_COLLECTION -> enumCollections.filter { it.key == fieldId }.forEach {
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
            JdsFieldType.ENUM_STRING_COLLECTION -> enumStringCollections.filter { it.key == fieldId }.forEach {
                val fieldEnum = JdsFieldEnum.enums[it.key]
                if (fieldEnum != null) {
                    val enumStr = value.toString()
                    val enumVal = fieldEnum.valueOf(enumStr)
                    if (enumVal != null)
                        it.value.add(enumVal)
                }
            }

            JdsFieldType.STRING -> stringValues[fieldId]?.value = (value as String?)

            JdsFieldType.STRING_COLLECTION -> stringCollections[fieldId]?.add(value as String)

            JdsFieldType.ZONED_DATE_TIME -> when (value) {
                is Long -> zonedDateTimeValues[fieldId]?.value = (ZonedDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault()))
                is Timestamp -> zonedDateTimeValues[fieldId]?.value = (value.let { ZonedDateTime.ofInstant(it.toInstant(), ZoneOffset.systemDefault()) })
                is String -> zonedDateTimeValues[fieldId]?.value = (value.let { it.toZonedDateTime() })
                is OffsetDateTime -> zonedDateTimeValues[fieldId]?.value = (value.let { it.atZoneSameInstant(ZoneId.systemDefault()) })
            }

            JdsFieldType.DATE -> when (value) {
                is Timestamp -> localDateValues[fieldId]?.value = (value.toLocalDateTime().toLocalDate())
                is LocalDate -> localDateValues[fieldId]?.value = (value)
                is String -> localDateValues[fieldId]?.value = (value.toLocalDate())
                else -> localDateValues[fieldId]?.value = (LocalDate.now())
            }

            JdsFieldType.TIME -> when (value) {
                is Long -> localTimeValues[fieldId]?.value = (LocalTime.MIN.plusNanos(value))
                is LocalTime -> localTimeValues[fieldId]?.value = (value)
                is String -> localTimeValues[fieldId]?.value = (value.toLocalTime())
            }

            JdsFieldType.DURATION -> durationValues[fieldId]?.value = (when (value) {
                is BigDecimal -> Duration.ofNanos(value.longValueExact())//Oracle
                else -> Duration.ofNanos(value as Long)
            })

            JdsFieldType.MONTH_DAY -> monthDayValues[fieldId]?.value = (value as String).let { MonthDay.parse(it) }

            JdsFieldType.YEAR_MONTH -> yearMonthValues[fieldId]?.value = (value as String).let { YearMonth.parse(it) }

            JdsFieldType.PERIOD -> periodValues[fieldId]?.value = (value as String).let { Period.parse(it) }

            JdsFieldType.DATE_TIME -> localDateTimeValues[fieldId]?.value = (value as Timestamp).let { it.toLocalDateTime() }

            JdsFieldType.DATE_TIME_COLLECTION -> dateTimeCollections[fieldId]?.add((value as Timestamp).let { it.toLocalDateTime() })

            JdsFieldType.BLOB -> when (value) {
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

            JdsFieldType.STRING -> if (!stringValues.containsKey(fieldId))
                stringValues[fieldId] = SimpleStringProperty("")

            JdsFieldType.DOUBLE_COLLECTION -> if (!doubleCollections.containsKey(fieldId))
                doubleCollections[fieldId] = ArrayList()

            JdsFieldType.FLOAT_COLLECTION -> if (!floatCollections.containsKey(fieldId))
                floatCollections[fieldId] = ArrayList()

            JdsFieldType.LONG_COLLECTION -> if (!longCollections.containsKey(fieldId))
                longCollections[fieldId] = ArrayList()

            JdsFieldType.INT_COLLECTION -> if (!integerCollections.containsKey(fieldId))
                integerCollections[fieldId] = ArrayList()

            JdsFieldType.STRING_COLLECTION -> if (!stringCollections.containsKey(fieldId))
                stringCollections[fieldId] = ArrayList()

            JdsFieldType.DATE_TIME_COLLECTION -> if (!dateTimeCollections.containsKey(fieldId))
                dateTimeCollections[fieldId] = ArrayList()

            JdsFieldType.ENUM_COLLECTION -> if (!enumCollections.containsKey(fieldId))
                enumCollections[fieldId] = ArrayList()

            JdsFieldType.ZONED_DATE_TIME -> if (!zonedDateTimeValues.containsKey(fieldId))
                zonedDateTimeValues[fieldId] = SimpleObjectProperty<Temporal?>()

            JdsFieldType.DATE -> if (!localDateValues.containsKey(fieldId))
                localDateValues[fieldId] = SimpleObjectProperty<Temporal?>()

            JdsFieldType.TIME -> if (!localTimeValues.containsKey(fieldId))
                localTimeValues[fieldId] = SimpleObjectProperty<Temporal?>()

            JdsFieldType.DURATION -> if (!durationValues.containsKey(fieldId))
                durationValues[fieldId] = SimpleObjectProperty<Duration?>()

            JdsFieldType.MONTH_DAY -> if (!monthDayValues.containsKey(fieldId))
                monthDayValues[fieldId] = SimpleObjectProperty<MonthDay?>()

            JdsFieldType.YEAR_MONTH -> if (!yearMonthValues.containsKey(fieldId))
                yearMonthValues[fieldId] = SimpleObjectProperty<Temporal?>()

            JdsFieldType.PERIOD -> if (!periodValues.containsKey(fieldId))
                periodValues[fieldId] = SimpleObjectProperty<Period?>()

            JdsFieldType.DATE_TIME -> if (!localDateTimeValues.containsKey(fieldId))
                localDateTimeValues[fieldId] = SimpleObjectProperty<Temporal>()

            JdsFieldType.BLOB -> if (!blobValues.containsKey(fieldId))
                blobValues[fieldId] = SimpleBlobProperty(byteArrayOf())

            JdsFieldType.ENUM -> if (!enumValues.containsKey(fieldId))
                enumValues[fieldId] = SimpleObjectProperty()

            JdsFieldType.FLOAT -> if (!floatValues.containsKey(fieldId))
                floatValues[fieldId] = NullableFloatProperty()

            JdsFieldType.DOUBLE -> if (!doubleValues.containsKey(fieldId))
                doubleValues[fieldId] = NullableDoubleProperty()

            JdsFieldType.SHORT -> if (!shortValues.containsKey(fieldId))
                shortValues[fieldId] = NullableShortProperty()

            JdsFieldType.LONG -> if (!longValues.containsKey(fieldId))
                longValues[fieldId] = NullableLongProperty()

            JdsFieldType.INT -> if (!integerValues.containsKey(fieldId))
                integerValues[fieldId] = NullableIntegerProperty()

            JdsFieldType.UUID -> if (!uuidValues.containsKey(fieldId))
                uuidValues[fieldId] = SimpleObjectProperty<UUID?>()

            JdsFieldType.BOOLEAN -> if (!booleanValues.containsKey(fieldId))
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
            objectCollections.filter { it.key.field.id == fieldId }.forEach {
                val entity = jdsDb.classes[entityId]!!.getDeclaredConstructor().newInstance()
                entity.overview.uuid = uuid
                entity.overview.editVersion = editVersion
                uuids.add(JdsEntityComposite(uuid, editVersion))
                it.value.add(entity)
                innerObjects.add(entity)
            }
            objectValues.filter { it.key.field.id == fieldId }.forEach {
                if (it.value.value == null) {
                    it.value.value = jdsDb.classes[entityId]!!.getDeclaredConstructor().newInstance()
                }
                it.value.value.overview.uuid = uuid
                it.value.value.overview.editVersion = editVersion
                uuids.add(JdsEntityComposite(uuid, editVersion))
                innerObjects.add(it.value.value)
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
        standardizeObjectUuids(pattern, objectValues)
        standardizeCollectionUuids(pattern, objectCollections)
    }

    /**
     * Ensures child entities have edit versions that match their parents
     * For frequent refreshes/imports from different sources this is necessary to prevent duplicate entries of the same data
     * @param version The version to set for each nested entity
     */
    @JvmOverloads
    fun standardizeEditVersion(version: Int = overview.editVersion) {
        standardizeObjectEditVersion(version, objectValues)
        standardizeCollectionEditVersion(version, objectCollections)
    }

    /**
     * Ensures child entities have ids that link them to their parent.
     * @param parentUuid
     * @param objectArrayCollection
     */
    private fun standardizeCollectionUuids(parentUuid: String, objectArrayCollection: HashMap<JdsFieldEntity<*>, MutableCollection<JdsEntity>>) {
        objectArrayCollection.entries.forEach {
            it.value.forEachIndexed { sequence, entry ->
                entry.overview.uuid = standardiseLength(parentUuid, ":${entry.overview.entityId}:$sequence")
                standardizeObjectUuids(entry.overview.uuid, entry.objectValues)
                standardizeCollectionUuids(entry.overview.uuid, entry.objectCollections)
            }
        }
    }

    /**
     * Ensures child entities have ids that link them to their parent.
     * @param parentUuid
     * @param objectCollection
     */
    private fun standardizeObjectUuids(parentUuid: String, objectCollection: HashMap<JdsFieldEntity<*>, ObjectProperty<JdsEntity>>) {
        objectCollection.entries.forEach { entry ->
            entry.value.value.overview.uuid = standardiseLength(parentUuid, ":${entry.value.value.overview.entityId}")
            standardizeObjectUuids(entry.value.value.overview.uuid, entry.value.value.objectValues)
            standardizeCollectionUuids(entry.value.value.overview.uuid, entry.value.value.objectCollections)
        }
    }

    /**
     * Ensures child entities have ids that link them to their parent.
     * @param version
     * @param objectArrayCollection
     */
    private fun standardizeCollectionEditVersion(version: Int, objectArrayCollection: HashMap<JdsFieldEntity<*>, MutableCollection<JdsEntity>>) {
        objectArrayCollection.entries.forEach {
            it.value.forEach { entry ->
                entry.overview.editVersion = version
                standardizeObjectEditVersion(version, entry.objectValues)
                standardizeCollectionEditVersion(version, entry.objectCollections)
            }
        }
    }

    /**
     * Ensures child entities have ids that link them to their parent.
     * @param version
     * @param objectCollection
     */
    private fun standardizeObjectEditVersion(version: Int, objectCollection: HashMap<JdsFieldEntity<*>, ObjectProperty<JdsEntity>>) {
        objectCollection.entries.forEach { objectProperty ->
            objectProperty.value.value.overview.editVersion = version
            standardizeObjectEditVersion(version, objectProperty.value.value.objectValues)
            standardizeCollectionEditVersion(version, objectProperty.value.value.objectCollections)
        }
    }

    /**
     * Internal helper function that works with all nested objects
     */
    fun getNestedEntities(includeThisEntity: Boolean = true): Sequence<JdsEntity> = sequence {
        if (includeThisEntity) {
            yield(this@JdsEntity)
        }
        objectValues.values.forEach { objectProperty -> yieldAll(objectProperty.value.getNestedEntities()) }
        objectCollections.values.forEach { objectCollection -> objectCollection.forEach { entity -> yieldAll(entity.getNestedEntities()) } }
    }

    /**
     * Internal helper function that works with all nested objects
     */
    private fun getNestedEntities(collection: MutableCollection<JdsEntity>, includeThisEntity: Boolean = true) {
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

        protected fun mapField(entityId: Long, fieldId: Long) {
            getFields(entityId).add(fieldId)
        }

        protected fun mapEnums(entityId: Long, fieldId: Long) {
            getEnums(entityId).add(fieldId)
        }

        private fun toTimeStampCollection(values: MutableCollection<LocalDateTime>) = values.map { Timestamp.valueOf(it) }.toMutableList()

        private fun toIntCollection(values: MutableCollection<Enum<*>>) = values.map { it.ordinal }.toMutableList()

        private fun toStringCollection(values: MutableCollection<Enum<*>>) = values.map { it.name }.toMutableList()

        private fun getFields(entityId: Long) = allFields.getOrPut(entityId) { LinkedHashSet() }

        private fun getEnums(entityId: Long) = allEnums.getOrPut(entityId) { LinkedHashSet() }
    }
}
