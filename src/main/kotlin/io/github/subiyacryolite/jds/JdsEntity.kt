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
import io.github.subiyacryolite.jds.JdsExtensions.toLocalDate
import io.github.subiyacryolite.jds.JdsExtensions.toLocalTime
import io.github.subiyacryolite.jds.JdsExtensions.toZonedDateTime
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation
import io.github.subiyacryolite.jds.embedded.*
import io.github.subiyacryolite.jds.enums.JdsFieldType
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBlobProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.WritableValue
import java.io.Externalizable
import java.io.IOException
import java.io.ObjectInput
import java.io.ObjectOutput
import java.math.BigDecimal
import java.sql.Connection
import java.sql.Timestamp
import java.time.*
import java.time.temporal.Temporal
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.collections.ArrayList
import kotlin.coroutines.experimental.buildSequence

/**
 * This class allows for all mapping operations in JDS, it also uses
 * [IJdsOverview] to store overview data
 */
abstract class JdsEntity : IJdsEntity {
    @set:JsonIgnore
    @get:JsonIgnore
    override var overview: IJdsOverview = JdsOverview()
    //time constructs
    @get:JsonIgnore
    internal val localDateTimeValues: HashMap<Long, WritableValue<out Temporal?>> = LinkedHashMap()
    @get:JsonIgnore
    internal val zonedDateTimeValues: HashMap<Long, WritableValue<out Temporal?>> = LinkedHashMap()
    @get:JsonIgnore
    internal val localDateValues: HashMap<Long, WritableValue<out Temporal?>> = LinkedHashMap()
    @get:JsonIgnore
    internal val localTimeValues: HashMap<Long, WritableValue<out Temporal?>> = LinkedHashMap()
    @get:JsonIgnore
    internal val monthDayValues: HashMap<Long, WritableValue<MonthDay?>> = LinkedHashMap()
    @get:JsonIgnore
    internal val yearMonthValues: HashMap<Long, WritableValue<out Temporal?>> = LinkedHashMap()
    @get:JsonIgnore
    internal val periodValues: HashMap<Long, WritableValue<Period?>> = LinkedHashMap()
    @get:JsonIgnore
    internal val durationValues: HashMap<Long, WritableValue<Duration?>> = LinkedHashMap()
    //strings
    @get:JsonIgnore
    internal val stringValues: HashMap<Long, WritableValue<String?>> = LinkedHashMap()
    //boolean
    @get:JsonIgnore
    internal val booleanValues: HashMap<Long, WritableValue<Boolean?>> = LinkedHashMap()
    //numeric
    @get:JsonIgnore
    internal val floatValues: HashMap<Long, WritableValue<out Number?>> = LinkedHashMap()
    @get:JsonIgnore
    internal val doubleValues: HashMap<Long, WritableValue<out Number?>> = LinkedHashMap()
    @get:JsonIgnore
    internal val longValues: HashMap<Long, WritableValue<out Number?>> = LinkedHashMap()
    @get:JsonIgnore
    internal val integerValues: HashMap<Long, WritableValue<out Number?>> = LinkedHashMap()
    //arrays
    @get:JsonIgnore
    internal val objectCollections: HashMap<JdsFieldEntity<*>, MutableCollection<JdsEntity>> = LinkedHashMap()
    @get:JsonIgnore
    internal val stringCollections: HashMap<Long, MutableCollection<String?>> = LinkedHashMap()
    @get:JsonIgnore
    internal val dateTimeCollections: HashMap<Long, MutableCollection<LocalDateTime?>> = LinkedHashMap()
    @get:JsonIgnore
    internal val floatCollections: HashMap<Long, MutableCollection<Float?>> = LinkedHashMap()
    @get:JsonIgnore
    internal val doubleCollections: HashMap<Long, MutableCollection<Double?>> = LinkedHashMap()
    @get:JsonIgnore
    internal val longCollections: HashMap<Long, MutableCollection<Long?>> = LinkedHashMap()
    @get:JsonIgnore
    internal val integerCollections: HashMap<Long, MutableCollection<Int?>> = LinkedHashMap()
    //enumProperties
    @get:JsonIgnore
    internal val enumProperties: HashMap<Long, WritableValue<Enum<*>?>> = LinkedHashMap()
    @get:JsonIgnore
    internal val enumStringProperties: HashMap<Long, WritableValue<Enum<*>?>> = LinkedHashMap()
    @get:JsonIgnore
    internal val enumCollections: HashMap<Long, MutableCollection<Enum<*>?>> = LinkedHashMap()
    @get:JsonIgnore
    internal val enumStringCollections: HashMap<Long, MutableCollection<String?>> = LinkedHashMap()
    //objects
    @get:JsonIgnore
    internal val objectValues: HashMap<JdsFieldEntity<*>, ObjectProperty<JdsEntity>> = LinkedHashMap()
    //blobs
    @get:JsonIgnore
    internal val blobValues: HashMap<Long, WritableValue<ByteArray?>> = LinkedHashMap()


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
        if (field.type != JdsFieldType.DOUBLE &&
                field.type != JdsFieldType.INT &&
                field.type != JdsFieldType.LONG &&
                field.type != JdsFieldType.FLOAT)
            throw RuntimeException("You must annotate the class [" + javaClass.canonicalName + "] or its parent with [" + JdsEntityAnnotation::class.java + "]")
        if (field.type == JdsFieldType.DOUBLE)
            doubleValues[field.id] = property
        if (field.type == JdsFieldType.INT)
            integerValues[field.id] = property
        if (field.type == JdsFieldType.LONG)
            longValues[field.id] = property
        if (field.type == JdsFieldType.FLOAT)
            floatValues[field.id] = property
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapBoolean")
    protected fun map(field: JdsField, property: WritableValue<Boolean?>) {
        if (field.type != JdsFieldType.BOOLEAN)
            throw RuntimeException("You must annotate the class [" + javaClass.canonicalName + "] or its parent with [" + JdsEntityAnnotation::class.java + "]")
        booleanValues[field.id] = property
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapString")
    protected fun map(field: JdsField, property: WritableValue<String?>) {
        if (field.type != JdsFieldType.STRING)
            throw RuntimeException("You must annotate the class [" + javaClass.canonicalName + "] or its parent with [" + JdsEntityAnnotation::class.java + "]")
        stringValues[field.id] = property
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapDateTime")
    protected fun map(field: JdsField, property: WritableValue<LocalDateTime?>) {
        if (field.type != JdsFieldType.DATE_TIME)
            throw RuntimeException("You must annotate the class [" + javaClass.canonicalName + "] or its parent with [" + JdsEntityAnnotation::class.java + "]")
        localDateTimeValues[field.id] = property
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapZonedDateTime")
    protected fun map(field: JdsField, property: WritableValue<ZonedDateTime?>) {
        if (field.type != JdsFieldType.ZONED_DATE_TIME)
            throw RuntimeException("You must annotate the class [" + javaClass.canonicalName + "] or its parent with [" + JdsEntityAnnotation::class.java + "]")
        zonedDateTimeValues[field.id] = property
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapDate")
    protected fun map(field: JdsField, property: WritableValue<LocalDate?>) {
        if (field.type != JdsFieldType.DATE)
            throw RuntimeException("You must annotate the class [" + javaClass.canonicalName + "] or its parent with [" + JdsEntityAnnotation::class.java + "]")
        localDateValues[field.id] = property
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapTime")
    protected fun map(field: JdsField, property: WritableValue<LocalTime?>) {
        if (field.type != JdsFieldType.TIME)
            throw RuntimeException("You must annotate the class [" + javaClass.canonicalName + "] or its parent with [" + JdsEntityAnnotation::class.java + "]")
        localTimeValues[field.id] = property
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapBlob")
    protected fun map(field: JdsField, property: WritableValue<ByteArray?>) {
        if (field.type != JdsFieldType.BLOB)
            throw RuntimeException("You must annotate the class [" + javaClass.canonicalName + "] or its parent with [" + JdsEntityAnnotation::class.java + "]")
        blobValues[field.id] = property
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapMonthDay")
    protected fun map(field: JdsField, property: WritableValue<MonthDay?>) {
        if (field.type != JdsFieldType.MONTH_DAY)
            throw RuntimeException("You must annotate the class [" + javaClass.canonicalName + "] or its parent with [" + JdsEntityAnnotation::class.java + "]")
        monthDayValues[field.id] = property
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapYearMonth")
    protected fun map(field: JdsField, property: WritableValue<YearMonth?>) {
        if (field.type != JdsFieldType.YEAR_MONTH)
            throw RuntimeException("You must annotate the class [" + javaClass.canonicalName + "] or its parent with [" + JdsEntityAnnotation::class.java + "]")
        yearMonthValues[field.id] = property
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapPeriod")
    protected fun map(field: JdsField, property: WritableValue<Period?>) {
        if (field.type != JdsFieldType.PERIOD)
            throw RuntimeException("You must annotate the class [" + javaClass.canonicalName + "] or its parent with [" + JdsEntityAnnotation::class.java + "]")
        periodValues[field.id] = property
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapDuration")
    protected fun map(field: JdsField, property: WritableValue<Duration?>) {
        if (field.type != JdsFieldType.DURATION)
            throw RuntimeException("You must annotate the class [" + javaClass.canonicalName + "] or its parent with [" + JdsEntityAnnotation::class.java + "]")
        durationValues[field.id] = property
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapEnum")
    protected fun <T : Enum<T>> map(field: JdsField, property: WritableValue<T?>) {
        if (field.type != JdsFieldType.ENUM && field.type != JdsFieldType.ENUM_STRING)
            throw RuntimeException("You must annotate the class [" + javaClass.canonicalName + "] or its parent with [" + JdsEntityAnnotation::class.java + "]")
        if (field.type == JdsFieldType.ENUM)
            enumProperties[field.id] = property as WritableValue<Enum<*>?>
        else
            enumStringProperties[field.id] = property as WritableValue<Enum<*>?>
        mapField(overview.entityId, field.id)
        mapEnums(overview.entityId, field.id)
    }

    protected fun <T : Enum<T>> map(fieldEnum: JdsFieldEnum<T>, property: WritableValue<T?>) = map(fieldEnum.field, property)

    @JvmName("mapStrings")
    protected fun map(field: JdsField, property: MutableCollection<String?>) {
        if (field.type != JdsFieldType.STRING_COLLECTION && field.type != JdsFieldType.ENUM_STRING_COLLECTION)
            throw RuntimeException("You must annotate the class [" + javaClass.canonicalName + "] or its parent with [" + JdsEntityAnnotation::class.java + "]")
        if (field.type == JdsFieldType.STRING_COLLECTION)
            stringCollections[field.id] = property
        else {
            enumStringCollections[field.id] = property
            mapEnums(overview.entityId, field.id)
        }
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapDateTimes")
    protected fun map(field: JdsField, property: MutableCollection<LocalDateTime?>) {
        if (field.type != JdsFieldType.DATE_TIME_COLLECTION)
            throw RuntimeException("You must annotate the class [" + javaClass.canonicalName + "] or its parent with [" + JdsEntityAnnotation::class.java + "]")
        dateTimeCollections[field.id] = property
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapFloats")
    protected fun map(field: JdsField, property: MutableCollection<Float?>) {
        if (field.type != JdsFieldType.FLOAT_COLLECTION)
            throw RuntimeException("You must annotate the class [" + javaClass.canonicalName + "] or its parent with [" + JdsEntityAnnotation::class.java + "]")
        floatCollections[field.id] = property
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapIntegers")
    protected fun map(field: JdsField, property: MutableCollection<Int?>) {
        if (field.type != JdsFieldType.INT_COLLECTION)
            throw RuntimeException("You must annotate the class [" + javaClass.canonicalName + "] or its parent with [" + JdsEntityAnnotation::class.java + "]")
        integerCollections[field.id] = property
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapDoubles")
    protected fun map(field: JdsField, property: MutableCollection<Double?>) {
        if (field.type != JdsFieldType.DOUBLE_COLLECTION)
            throw RuntimeException("You must annotate the class [" + javaClass.canonicalName + "] or its parent with [" + JdsEntityAnnotation::class.java + "]")
        doubleCollections[field.id] = property
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapLongs")
    protected fun map(field: JdsField, property: MutableCollection<Long?>) {
        if (field.type != JdsFieldType.LONG_COLLECTION)
            throw RuntimeException("You must annotate the class [" + javaClass.canonicalName + "] or its parent with [" + JdsEntityAnnotation::class.java + "]")
        longCollections[field.id] = property
        mapField(overview.entityId, field.id)
    }

    @JvmName("mapEnums")
    protected fun <T:Enum<T>>map(field: JdsField, property: MutableCollection<T?>) {
        if (field.type != JdsFieldType.ENUM_COLLECTION)
            throw RuntimeException("You must annotate the class [" + javaClass.canonicalName + "] or its parent with [" + JdsEntityAnnotation::class.java + "]")
        enumCollections[field.id] = property as MutableCollection<Enum<*>?>
        mapField(overview.entityId, field.id)
        mapEnums(overview.entityId, field.id)
    }

    /**
     * @param fieldEnum
     * @param properties
     */
    protected fun <T:Enum<T>>mapEnums(fieldEnum: JdsFieldEnum<T>, properties: MutableCollection<T?>) = map(fieldEnum.field, properties)

    /**
     * @param entity
     * @param property
     */
    protected fun <T : IJdsEntity> map(fieldEntity: JdsFieldEntity<out T>, property: ObjectProperty<out T>) {
        if (fieldEntity.fieldEntity.type != JdsFieldType.ENTITY)
            throw RuntimeException("Please assign the correct type to field [$fieldEntity]")
        if (!objectCollections.containsKey(fieldEntity) && !objectValues.containsKey(fieldEntity)) {
            objectValues[fieldEntity] = property as ObjectProperty<JdsEntity>
            mapField(overview.entityId, fieldEntity.fieldEntity.id)
        } else {
            throw RuntimeException("You can only bind a class to one property. This class is already bound to one object or object array")
        }
    }

    /**
     * @param fieldEntity
     * @param properties
     */
    protected fun <T : IJdsEntity> map(fieldEntity: JdsFieldEntity<out T>, properties: MutableCollection<out T>) {
        if (fieldEntity.fieldEntity.type != JdsFieldType.ENTITY_COLLECTION)
            throw RuntimeException("Please supply a valid type for JdsFieldEntity")
        if (!objectCollections.containsKey(fieldEntity)) {
            objectCollections[fieldEntity] = properties as MutableCollection<JdsEntity>
            mapField(overview.entityId, fieldEntity.fieldEntity.id)
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
        copyArrayValues(source)
        copyPropertyValues(source)
        copyOverviewValues(source)
        copyEnumAndEnumArrayValues(source)
        copyObjectAndObjectArrayValues(source)
    }

    /**
     * Copy all header overview information
     *
     * @param source The entity to copy values from
     * @param <T>    A valid JDSEntity
    </T> */
    private fun <T : IJdsEntity> copyOverviewValues(source: T) {
        overview.uuid = source.overview.uuid
        overview.editVersion = source.overview.editVersion
        overview.entityId = source.overview.entityId
    }

    /**
     * Copy all property values
     *
     * @param source The entity to copy values from
     * @param <T>    A valid JDSEntity A valid JDSEntity
    </T> */
    private fun <T : JdsEntity> copyPropertyValues(source: T) {
        val dest = this
        source.booleanValues.entries.forEach { dest.booleanValues[it.key] = it.value }
        source.localDateTimeValues.entries.forEach { dest.localDateTimeValues[it.key] = it.value }
        source.zonedDateTimeValues.entries.forEach { dest.zonedDateTimeValues[it.key] = it.value }
        source.localTimeValues.entries.forEach { dest.localTimeValues[it.key] = it.value }
        source.localDateValues.entries.forEach { dest.localDateValues[it.key] = it.value }
        source.stringValues.entries.forEach { dest.stringValues[it.key] = it.value }
        source.floatValues.entries.forEach { dest.floatValues[it.key] = it.value }
        source.doubleValues.entries.forEach { dest.doubleValues[it.key] = it.value }
        source.longValues.entries.forEach { dest.longValues[it.key] = it.value }
        source.integerValues.entries.forEach { dest.integerValues[it.key] = it.value }
        source.blobValues.entries.forEach { dest.blobValues[it.key] = it.value }
        source.durationValues.entries.forEach { dest.durationValues[it.key] = it.value }
        source.periodValues.entries.forEach { dest.periodValues[it.key] = it.value }
        source.yearMonthValues.entries.forEach { dest.yearMonthValues[it.key] = it.value }
        source.monthDayValues.entries.forEach { dest.monthDayValues[it.key] = it.value }
    }

    /**
     * Copy all property array values
     *
     * @param source The entity to copy values from
     * @param <T>    A valid JDSEntity A valid JDSEntity
    </T> */
    private fun <T : JdsEntity> copyArrayValues(source: T) {
        val dest = this
        source.stringCollections.entries.forEach {
            if (dest.stringCollections.containsKey(it.key)) {
                val entry = dest.stringCollections[it.key]
                entry?.clear()
                entry?.addAll(it.value)
            }
        }
        source.dateTimeCollections.entries.forEach {
            if (dest.dateTimeCollections.containsKey(it.key)) {
                val entry = dest.dateTimeCollections[it.key]
                entry?.clear()
                entry?.addAll(it.value)
            }
        }
        source.floatCollections.entries.forEach {
            if (dest.floatCollections.containsKey(it.key)) {
                val entry = dest.floatCollections[it.key]
                entry?.clear()
                entry?.addAll(it.value)
            }
        }
        source.doubleCollections.entries.forEach {
            if (dest.doubleCollections.containsKey(it.key)) {
                val entry = dest.doubleCollections[it.key]
                entry?.clear()
                entry?.addAll(it.value)
            }
        }
        source.longCollections.entries.forEach {
            if (dest.longCollections.containsKey(it.key)) {
                val entry = dest.longCollections[it.key]
                entry?.clear()
                entry?.addAll(it.value)
            }
        }
        source.integerCollections.entries.forEach {
            if (dest.integerCollections.containsKey(it.key)) {
                val entry = dest.integerCollections[it.key]
                entry?.clear()
                entry?.addAll(it.value)
            }
        }
    }

    /**
     * Copy over object and object array values
     *
     * @param source The entity to copy values from
     * @param <T>    A valid JDSEntity
    </T> */
    private fun <T : JdsEntity> copyObjectAndObjectArrayValues(source: T) {
        val dest = this
        source.objectValues.entries.forEach {
            if (dest.objectValues.containsKey(it.key)) {
                dest.objectValues[it.key]?.set(it.value.get())
            }
        }

        source.objectCollections.entries.forEach {
            if (dest.objectCollections.containsKey(it.key)) {
                val entry = dest.objectCollections[it.key]
                entry?.clear()
                entry?.addAll(it.value)
            }
        }
    }

    /**
     * Copy over object enum values
     *
     * @param source The entity to copy values from
     * @param <T> A valid JDSEntity
    </T> */
    private fun <T : JdsEntity> copyEnumAndEnumArrayValues(source: T) {
        val dest = this
        source.enumCollections.entries.forEach {
            if (dest.enumCollections.containsKey(it.key)) {
                val dstEntry = dest.enumCollections[it.key]
                dstEntry?.clear()
                val itz = it.value.iterator()
                while (itz.hasNext()) {
                    val nxt = itz.next()
                    dstEntry?.add(nxt)
                }
            }
        }
        source.enumStringCollections.entries.forEach {
            if (dest.enumStringCollections.containsKey(it.key)) {
                val dstEntry = dest.enumStringCollections[it.key]
                dstEntry?.clear()
                val itz = it.value.iterator()
                while (itz.hasNext()) {
                    val nxt = itz.next()
                    dstEntry?.add(nxt)
                }
            }
        }
        source.enumProperties.entries.forEach {
            if (dest.enumProperties.containsKey(it.key)) {
                val dstEntry = dest.enumProperties[it.key]
                dstEntry?.value = it.value.value
            }
        }
        source.enumStringProperties.entries.forEach {
            if (dest.enumStringProperties.containsKey(it.key)) {
                val dstEntry = dest.enumStringProperties[it.key]
                dstEntry?.value = it.value.value
            }
        }
    }

    @Throws(IOException::class)
    override fun writeExternal(objectOutputStream: ObjectOutput) {
        //fieldEntity and enum maps
        objectOutputStream.writeObject(overview)
        //objects
        objectOutputStream.writeObject(serializeObject(objectValues))
        //time constructs
        objectOutputStream.writeObject(serializeTemporal(localDateTimeValues))
        objectOutputStream.writeObject(serializeTemporal(zonedDateTimeValues))
        objectOutputStream.writeObject(serializeTemporal(localDateValues))
        objectOutputStream.writeObject(serializeTemporal(localTimeValues))
        objectOutputStream.writeObject(monthDayValues)
        objectOutputStream.writeObject(yearMonthValues)
        objectOutputStream.writeObject(periodValues)
        objectOutputStream.writeObject(durationValues)
        //strings
        objectOutputStream.writeObject(serializableString(stringValues))
        //numeric
        objectOutputStream.writeObject(floatValues)
        objectOutputStream.writeObject(doubleValues)
        objectOutputStream.writeObject(booleanValues)
        objectOutputStream.writeObject(longValues)
        objectOutputStream.writeObject(integerValues)
        //blobs
        objectOutputStream.writeObject(serializeBlobs(blobValues))
        //arrays
        objectOutputStream.writeObject(serializeObjects(objectCollections))
        objectOutputStream.writeObject(serializeStrings(stringCollections))
        objectOutputStream.writeObject(serializeDateTimes(dateTimeCollections))
        objectOutputStream.writeObject(serializeFloats(floatCollections))
        objectOutputStream.writeObject(serializeDoubles(doubleCollections))
        objectOutputStream.writeObject(serializeLongs(longCollections))
        objectOutputStream.writeObject(serializeIntegers(integerCollections))
        //enumProperties
        objectOutputStream.writeObject(serializeEnums(enumProperties))
        objectOutputStream.writeObject(serializeEnumCollections(enumCollections))
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    override fun readExternal(objectInputStream: ObjectInput) {
        //fieldEntity and enum maps
        overview = objectInputStream.readObject() as JdsOverview
        //objects
        putObject(objectValues, objectInputStream.readObject() as Map<JdsFieldEntity<*>, JdsEntity>)
        //time constructs
        putTemporal(localDateTimeValues, objectInputStream.readObject() as Map<Long, WritableValue<out Temporal?>>)
        putTemporal(zonedDateTimeValues, objectInputStream.readObject() as Map<Long, WritableValue<out Temporal?>>)
        putTemporal(localDateValues, objectInputStream.readObject() as Map<Long, WritableValue<out Temporal?>>)
        putTemporal(localTimeValues, objectInputStream.readObject() as Map<Long, WritableValue<out Temporal?>>)
        putMonthDays(monthDayValues, objectInputStream.readObject() as Map<Long, MonthDay?>)
        putTemporal(yearMonthValues, objectInputStream.readObject() as Map<Long, WritableValue<out Temporal?>>)
        putPeriods(periodValues, objectInputStream.readObject() as Map<Long, Period?>)
        putDurations(durationValues, objectInputStream.readObject() as Map<Long, Duration?>)
        //string
        putString(stringValues, objectInputStream.readObject() as Map<Long, String?>)
        //boolean
        putBoolean(booleanValues, objectInputStream.readObject() as Map<Long, WritableValue<Boolean?>>)
        //numeric
        putNumber(floatValues, objectInputStream.readObject() as Map<Long, WritableValue<out Number?>>)
        putNumber(doubleValues, objectInputStream.readObject() as Map<Long, WritableValue<out Number?>>)
        putNumber(longValues, objectInputStream.readObject() as Map<Long, WritableValue<out Number?>>)
        putNumber(integerValues, objectInputStream.readObject() as Map<Long, WritableValue<out Number?>>)
        //blobs
        putBlobs(blobValues, objectInputStream.readObject() as Map<Long, WritableValue<ByteArray?>>)
        //arrays
        putObjects(objectCollections, objectInputStream.readObject() as Map<JdsFieldEntity<*>, List<JdsEntity>>)
        putStrings(stringCollections, objectInputStream.readObject() as Map<Long, List<String?>>)
        putDateTimes(dateTimeCollections, objectInputStream.readObject() as Map<Long, List<LocalDateTime?>>)
        putFloats(floatCollections, objectInputStream.readObject() as Map<Long, List<Float?>>)
        putDoubles(doubleCollections, objectInputStream.readObject() as Map<Long, List<Double?>>)
        putLongs(longCollections, objectInputStream.readObject() as Map<Long, List<Long?>>)
        putIntegers(integerCollections, objectInputStream.readObject() as Map<Long, List<Int?>>)
        //enumProperties
        putEnum(enumProperties, objectInputStream.readObject() as Map<Long, Enum<*>?>)
        putEnums(enumCollections, objectInputStream.readObject() as Map<Long, List<Enum<*>?>>)
    }

    private fun serializeEnums(input: Map<Long, WritableValue<Enum<*>?>>): Map<Long, Enum<*>?> =
            input.entries.associateBy({ it.key }, { it.value.value })

    private fun serializeBlobs(input: Map<Long, WritableValue<ByteArray?>>): Map<Long, WritableValue<ByteArray?>> =
            input.entries.associateBy({ it.key }, { it.value })

    /**
     * Create a map that can be serialized
     * @param input an unserializable map
     * @return A serialisable map
     */
    private fun serializeEnumCollections(input: Map<Long, Collection<Enum<*>?>>): Map<Long, List<Enum<*>?>> =
            input.entries.associateBy({ it.key }, { ArrayList(it.value) })

    /**
     * Create a map that can be serialized
     * @param input an unserializable map
     * @return A serialisable map
     */
    private fun serializeIntegers(input: Map<Long, Collection<Int?>>): Map<Long, List<Int?>> =
            input.entries.associateBy({ it.key }, { ArrayList(it.value) })

    /**
     * Create a map that can be serialized
     * @param input an unserializable map
     * @return A serialisable map
     */
    private fun serializeLongs(input: Map<Long, Collection<Long?>>): Map<Long, List<Long?>> =
            input.entries.associateBy({ it.key }, { ArrayList(it.value) })

    /**
     * Create a map that can be serialized
     * @param input an unserializable map
     * @return A serialisable map
     */
    private fun serializeDoubles(input: Map<Long, Collection<Double?>>): Map<Long, List<Double?>> =
            input.entries.associateBy({ it.key }, { ArrayList(it.value) })

    /**
     * Create a map that can be serialized
     * @param input an unserializable map
     * @return A serialisable map
     */
    private fun serializeFloats(input: Map<Long, Collection<Float?>>): Map<Long, List<Float?>> =
            input.entries.associateBy({ it.key }, { ArrayList(it.value) })

    /**
     * Create a map that can be serialized
     * @param input an unserializable map
     * @return A serialisable map
     */
    private fun serializeDateTimes(input: Map<Long, Collection<LocalDateTime?>>): Map<Long, List<LocalDateTime?>> =
            input.entries.associateBy({ it.key }, { ArrayList(it.value) })

    /**
     * Create a map that can be serialized
     * @param input an unserializable map
     * @return A serialisable map
     */
    private fun serializeStrings(input: Map<Long, Collection<String?>>): Map<Long, List<String?>> =
            input.entries.associateBy({ it.key }, { ArrayList(it.value) })

    /**
     * Create a map that can be serialized
     * @param input an unserializable map
     * @return A serialisable map
     */
    private fun serializeObjects(input: Map<JdsFieldEntity<*>, Collection<JdsEntity>>): Map<JdsFieldEntity<*>, List<JdsEntity>> =
            input.entries.associateBy({ it.key }, { ArrayList(it.value) })

    /**
     * Create a map that can be serialized
     * @param input an unserializable map
     * @return A serialisable map
     */
    private fun serializeObject(input: Map<JdsFieldEntity<*>, ObjectProperty<JdsEntity>>): Map<JdsFieldEntity<*>, JdsEntity> =
            input.entries.associateBy({ it.key }, { it.value.get() })

    /**
     * Create a map that can be serialized
     * @param input an unserializable map
     * @return A serialisable map
     */
    private fun serializeTemporal(input: Map<Long, WritableValue<out Temporal?>>): Map<Long, Temporal?> =
            input.entries.associateBy({ it.key }, { it.value.value })

    /**
     * Create a map that can be serialized
     * @param input an unserializable map
     * @return A serialisable map
     */
    private fun serializableString(input: Map<Long, WritableValue<String?>>): Map<Long, String?> =
            input.entries.associateBy({ it.key }, { it.value.value })

    private fun putDurations(destination: HashMap<Long, WritableValue<Duration?>>, source: Map<Long, Duration?>) =
            source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.value = entry.value }

    private fun putPeriods(destination: HashMap<Long, WritableValue<Period?>>, source: Map<Long, Period?>) =
            source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.value = entry.value }

    private fun putMonthDays(destination: HashMap<Long, WritableValue<MonthDay?>>, source: Map<Long, MonthDay?>) =
            source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.value = entry.value }

    private fun putEnums(destination: Map<Long, MutableCollection<Enum<*>?>>, source: Map<Long, List<Enum<*>?>>) =
            source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.addAll(entry.value) }

    private fun putEnum(destination: Map<Long, WritableValue<Enum<*>?>>, source: Map<Long, Enum<*>?>) =
            source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.value = (entry.value) }

    private fun putObjects(destination: Map<JdsFieldEntity<*>, MutableCollection<JdsEntity>>, source: Map<JdsFieldEntity<*>, List<JdsEntity>>) =
            source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.addAll(entry.value) }

    private fun putStrings(destination: Map<Long, MutableCollection<String?>>, source: Map<Long, List<String?>>) =
            source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.addAll(entry.value) }

    private fun putDateTimes(destination: Map<Long, MutableCollection<LocalDateTime?>>, source: Map<Long, List<LocalDateTime?>>) =
            source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.addAll(entry.value) }

    private fun putFloats(destination: Map<Long, MutableCollection<Float?>>, source: Map<Long, List<Float?>>) =
            source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.addAll(entry.value) }

    private fun putDoubles(destination: Map<Long, MutableCollection<Double?>>, source: Map<Long, List<Double?>>) =
            source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.addAll(entry.value) }

    private fun putLongs(destination: Map<Long, MutableCollection<Long?>>, source: Map<Long, List<Long?>>) =
            source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.addAll(entry.value) }

    private fun putIntegers(destination: Map<Long, MutableCollection<Int?>>, source: Map<Long, List<Int?>>) =
            source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.addAll(entry.value) }

    private fun putBlobs(destination: MutableMap<Long, WritableValue<ByteArray?>>, source: Map<Long, WritableValue<ByteArray?>>) =
            source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key] = entry.value }

    private fun putBoolean(destination: MutableMap<Long, WritableValue<Boolean?>>, source: Map<Long, WritableValue<Boolean?>>) =
            source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key] = entry.value }

    private fun putObject(destination: Map<JdsFieldEntity<*>, ObjectProperty<JdsEntity>>, source: Map<JdsFieldEntity<*>, JdsEntity>) =
            source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.set(entry.value) }

    private fun putNumber(destination: MutableMap<Long, WritableValue<out Number?>>, source: Map<Long, WritableValue<out Number?>>) =
            source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key] = entry.value }

    private fun putTemporal(destination: MutableMap<Long, WritableValue<out Temporal?>>, source: Map<Long, WritableValue<out Temporal?>>) =
            source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key] = entry.value }

    private fun putString(destination: Map<Long, WritableValue<String?>>, source: Map<Long, String?>) =
            source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.value = entry.value }

    /**
     * @param embeddedObject
     */
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
        floatValues.entries.forEach { embeddedObject.floatValue.add(JdsStoreFloat(it.key, it.value.value?.toFloat())) }
        doubleValues.entries.forEach { embeddedObject.doubleValues.add(JdsStoreDouble(it.key, it.value.value?.toDouble())) }
        longValues.entries.forEach { embeddedObject.longValues.add(JdsStoreLong(it.key, it.value.value?.toLong())) }
        integerValues.entries.forEach { embeddedObject.integerValues.add(JdsStoreInteger(it.key, it.value.value?.toInt())) }
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
        enumProperties.entries.forEach { embeddedObject.enumValues.add(JdsStoreEnum(it.key, it.value.value?.ordinal)) }
        enumStringProperties.entries.forEach { embeddedObject.enumStringValues.add(JdsStoreEnumString(it.key, it.value.value.toString())) }
        enumCollections.entries.forEach { embeddedObject.enumCollections.add(JdsStoreEnumCollection(it.key, toIntCollection(it.value))) }
        enumStringCollections.entries.forEach { embeddedObject.enumStringCollections.add(JdsStoreEnumStringCollection(it.key, it.value)) }
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
        objectCollections.forEach { key, itx ->
            itx.forEach {
                val eo = JdsEmbeddedObject()
                eo.fieldId = key.fieldEntity.id
                eo.init(it)
                embeddedObject.entityOverviews.add(eo)
            }
        }
        objectValues.forEach { key, it ->
            val eo = JdsEmbeddedObject()
            eo.fieldId = key.fieldEntity.id
            eo.init(it.value)
            embeddedObject.entityOverviews.add(eo)
        }
    }

    private fun safeLocalDateTime(value: Temporal?): Timestamp? {
        val localDateTime = value as LocalDateTime?
        if (localDateTime != null)
            return Timestamp.valueOf(localDateTime)
        return null
    }

    private fun safeLocalDate(value: Temporal?): Timestamp? {
        val localDate = value as LocalDate?
        if (localDate != null)
            return Timestamp.valueOf(localDate.atStartOfDay())
        return null
    }

    private fun toTimeStampCollection(values: MutableCollection<LocalDateTime?>): MutableCollection<Timestamp?> {
        val dest = ArrayList<Timestamp?>()
        values.filterNotNull().forEach { dest.add(Timestamp.valueOf(it)) }
        return dest
    }

    private fun toIntCollection(values: MutableCollection<Enum<*>?>): MutableCollection<Int?> {
        val dest = ArrayList<Int?>()
        values.filterNotNull().forEach { dest.add(it.ordinal) }
        return dest
    }

    /**
     * @param fieldType
     * @param fieldId
     * @param value
     */
    internal fun populateProperties(fieldType: JdsFieldType, fieldId: Long, value: Any?) {
        //what happens when you supply an unknown field ID? Create an empty container and throw it in regardless, this way compatibility isnt broken
        initBackingPropertyIfNotDefined(fieldType, fieldId, value)
        when (fieldType) {

            JdsFieldType.FLOAT -> floatValues[fieldId]?.value = when (value) {
                is Double -> value.toFloat()
                else -> value as Float?
            }

            JdsFieldType.DOUBLE -> doubleValues[fieldId]?.value = value as Double?

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

            JdsFieldType.ENUM -> enumProperties.filter { it.key == fieldId }.forEach {
                val fieldEnum = JdsFieldEnum.enums[it.key]
                if (fieldEnum != null)
                    it.value?.value = (when (value) {
                        is BigDecimal -> fieldEnum.valueOf(value.intValueExact())
                        else -> fieldEnum.valueOf(value as Int)
                    })
            }

            JdsFieldType.ENUM_STRING -> enumProperties.filter { it.key == fieldId }.forEach {
                val fieldEnum = JdsFieldEnum.enums[it.key]
                if (fieldEnum != null && value is String?)
                    it.value.value = fieldEnum.valueOf(value)
            }

            JdsFieldType.ENUM_COLLECTION -> enumCollections.filter { it.key == fieldId }.forEach {
                val fieldEnum = JdsFieldEnum.enums[it.key]
                if (fieldEnum != null) {
                    val enumValues = fieldEnum.values
                    val index = when (value) {
                        is Int -> value
                        is BigDecimal -> value.intValueExact()
                        else -> enumValues.size
                    }
                    if (index < enumValues.size) {
                        it.value.add(enumValues[index] as Enum<*>)
                    }
                }
            }

            JdsFieldType.ENUM_STRING_COLLECTION -> enumStringCollections[fieldId]?.add(value as String)

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
        }
    }

    /**
     * This method enforces forward compatibility by ensuring that every property is present even if the field is not defined or known locally
     */
    private fun initBackingPropertyIfNotDefined(fieldType: JdsFieldType, fieldId: Long, value: Any?) {
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
                zonedDateTimeValues[fieldId] = SimpleObjectProperty<Temporal>()

            JdsFieldType.DATE -> if (!localDateValues.containsKey(fieldId))
                localDateValues[fieldId] = SimpleObjectProperty<Temporal>()

            JdsFieldType.TIME -> if (!localTimeValues.containsKey(fieldId))
                localTimeValues[fieldId] = SimpleObjectProperty<Temporal>()

            JdsFieldType.DURATION -> if (!durationValues.containsKey(fieldId))
                durationValues[fieldId] = SimpleObjectProperty<Duration>()

            JdsFieldType.MONTH_DAY -> if (!monthDayValues.containsKey(fieldId))
                monthDayValues[fieldId] = SimpleObjectProperty<MonthDay>()

            JdsFieldType.YEAR_MONTH -> if (!yearMonthValues.containsKey(fieldId))
                yearMonthValues[fieldId] = SimpleObjectProperty<Temporal>()

            JdsFieldType.PERIOD -> if (!periodValues.containsKey(fieldId))
                periodValues[fieldId] = SimpleObjectProperty<Period>()

            JdsFieldType.DATE_TIME -> if (!localDateTimeValues.containsKey(fieldId))
                localDateTimeValues[fieldId] = SimpleObjectProperty<Temporal>()

            JdsFieldType.BLOB -> if (!blobValues.containsKey(fieldId))
                blobValues[fieldId] = SimpleBlobProperty(byteArrayOf())

            JdsFieldType.ENUM -> if (!enumProperties.containsKey(fieldId))
                enumProperties[fieldId] = SimpleObjectProperty()

            JdsFieldType.FLOAT -> if (!floatValues.containsKey(fieldId))
                floatValues[fieldId] = object : WritableValue<Float?> {

                    private var backingValue: Float? = null

                    override fun setValue(value: Float?) {
                        backingValue = value
                    }

                    override fun getValue(): Float? = backingValue
                }

            JdsFieldType.DOUBLE -> if (!doubleValues.containsKey(fieldId))
                doubleValues[fieldId] = object : WritableValue<Double?> {

                    private var backingValue: Double? = null

                    override fun setValue(value: Double?) {
                        backingValue = value
                    }

                    override fun getValue(): Double? = backingValue
                }

            JdsFieldType.LONG -> if (!longValues.containsKey(fieldId))
                longValues[fieldId] = object : WritableValue<Long?> {

                    private var backingValue: Long? = null

                    override fun setValue(value: Long?) {
                        backingValue = value
                    }

                    override fun getValue(): Long? = backingValue
                }

            JdsFieldType.INT -> if (!integerValues.containsKey(fieldId))
                integerValues[fieldId] = object : WritableValue<Int?> {

                    private var backingValue: Int? = null

                    override fun setValue(value: Int?) {
                        backingValue = value
                    }

                    override fun getValue(): Int? = backingValue
                }

            JdsFieldType.BOOLEAN -> if (!booleanValues.containsKey(fieldId))
                booleanValues[fieldId] = object : WritableValue<Boolean?> {

                    private var backingValue: Boolean? = null

                    override fun setValue(value: Boolean?) {
                        backingValue = value
                    }

                    override fun getValue(): Boolean? = backingValue
                }
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
    internal fun populateObjects(jdsDb: JdsDb,
                                 fieldId: Long?,
                                 entityId: Long,
                                 uuid: String,
                                 editVersion: Int,
                                 innerObjects: ConcurrentLinkedQueue<JdsEntity>,
                                 uuids: MutableCollection<JdsEntityComposite>) {
        try {
            if (fieldId == null) return
            objectCollections.filter { it.key.fieldEntity.id == fieldId }.forEach {
                val entity = jdsDb.classes[entityId]!!.newInstance()
                entity.overview.uuid = uuid
                entity.overview.editVersion = editVersion
                uuids.add(JdsEntityComposite(uuid, editVersion))
                it.value.add(entity)
                innerObjects.add(entity)
            }
            objectValues.filter { it.key.fieldEntity.id == fieldId }.forEach {
                if (it.value.value == null)
                    it.value.value = jdsDb.classes[entityId]!!.newInstance()
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
    internal fun populateRefFieldRefEntityField(jdsDb: JdsDb, connection: Connection, entityId: Long) = try {
        (if (jdsDb.supportsStatements) connection.prepareCall(jdsDb.populateRefField()) else connection.prepareStatement(jdsDb.populateRefField())).use { populateRefField ->
            (if (jdsDb.supportsStatements) connection.prepareCall(jdsDb.populateRefEntityField()) else connection.prepareStatement(jdsDb.populateRefEntityField())).use { populateRefEntityField ->
                getFields(overview.entityId).forEach {
                    val lookup = JdsField.values[it]!!
                    //1. map this fieldEntity to the fieldEntity dictionary
                    populateRefField.setLong(1, lookup.id)
                    populateRefField.setString(2, lookup.name)
                    populateRefField.setString(3, lookup.description)
                    populateRefField.setInt(4, lookup.type.ordinal)
                    populateRefField.addBatch()
                    //2. map this fieldEntity ID to the entity type
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
     * Binds all the enumProperties attached to an entity
     * @param connection
     * @param entityId
     * @param jdsDb
     */
    @Synchronized
    internal fun populateRefEnumRefEntityEnum(jdsDb: JdsDb, connection: Connection, entityId: Long) {
        populateRefEnum(jdsDb, connection, getEnums(overview.entityId))
        populateRefEntityEnum(jdsDb, connection, entityId, getEnums(overview.entityId))
        if (jdsDb.options.isLoggingOutput)
            System.out.printf("Mapped Enums for Entity[%s]\n", entityId)
    }

    /**
     * Binds all the enumProperties attached to an entity
     * @param jdsDb
     * @param connection the SQL connection to use for DB operations
     * @param entityId   the value representing the entity
     * @param fieldIds     the entity's enumProperties
     */
    @Synchronized
    private fun populateRefEntityEnum(jdsDb: JdsDb, connection: Connection, entityId: Long, fieldIds: Set<Long>) = try {
        (if (jdsDb.supportsStatements) connection.prepareCall(jdsDb.populateRefEntityEnum()) else connection.prepareStatement(jdsDb.populateRefEntityEnum())).use {
            for (fieldId in fieldIds) {
                val jdsFieldEnum = JdsFieldEnum.enums[fieldId]!!
                for (index in 0 until jdsFieldEnum.values.size) {
                    it.setLong(1, entityId)
                    it.setLong(2, jdsFieldEnum.field.id)
                    it.addBatch()
                }
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
     * @param fieldIds the fieldEntity enum
     */
    @Synchronized
    private fun populateRefEnum(jdsDb: JdsDb, connection: Connection, fieldIds: Set<Long>) = try {
        (if (jdsDb.supportsStatements) connection.prepareCall(jdsDb.populateRefEnum()) else connection.prepareStatement(jdsDb.populateRefEnum())).use {
            for (fieldId in fieldIds) {
                val jdsFieldEnum = JdsFieldEnum.enums[fieldId]!!
                for (index in 0 until jdsFieldEnum.values.size) {
                    it.setLong(1, jdsFieldEnum.field.id)
                    it.setInt(2, index)
                    it.setString(3, jdsFieldEnum.values[index].toString())
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
        if (localDateTimeValues.containsKey(fieldId))
            return localDateTimeValues[fieldId]!!.value as LocalDateTime?
        if (zonedDateTimeValues.containsKey(fieldId))
            return zonedDateTimeValues[fieldId]!!.value as ZonedDateTime?
        if (localDateValues.containsKey(fieldId))
            return localDateValues[fieldId]!!.value as LocalDate?
        if (localTimeValues.containsKey(fieldId))
            return localTimeValues[fieldId]!!.value as LocalTime?
        if (monthDayValues.containsKey(fieldId))
            return (monthDayValues[fieldId]!!.value as MonthDay).toString()
        if (yearMonthValues.containsKey(fieldId))
            return yearMonthValues[fieldId]!!.value.toString()
        if (periodValues.containsKey(fieldId))
            return periodValues[fieldId]!!.value.toString()
        if (durationValues.containsKey(fieldId))
            return durationValues[fieldId]!!.value?.toNanos()
        if (stringValues.containsKey(fieldId))
            return stringValues[fieldId]!!.value
        if (floatValues.containsKey(fieldId))
            return floatValues[fieldId]?.value?.toFloat()
        if (doubleValues.containsKey(fieldId))
            return doubleValues[fieldId]?.value?.toDouble()
        if (booleanValues.containsKey(fieldId))
            return booleanValues[fieldId]?.value
        if (longValues.containsKey(fieldId))
            return longValues[fieldId]?.value?.toLong()
        if (enumStringProperties.containsKey(fieldId))
            return enumStringProperties[fieldId]?.value.toString()
        if (integerValues.containsKey(fieldId))
            return integerValues[fieldId]?.value?.toInt()
        if (enumProperties.containsKey(fieldId))
            return enumProperties[fieldId]?.value?.ordinal
        enumCollections.filter { it.key == fieldId }.forEach { it.value.filter { it != null && it.ordinal == ordinal }.forEach { return true } }
        objectValues.filter { it.key.fieldEntity.id == fieldId }.forEach { return it.value.value.overview.uuid }
        return null
    }

    /**
     * @param table
     */
    override fun registerFields(table: JdsTable) {
        getFields(overview.entityId).forEach {
            if (!JdsSchema.isIgnoredType(it))
                table.registerField(it)
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
     * @param uuid
     * @param objectArrayProperties
     */
    private fun standardizeCollectionUuids(uuid: String, objectArrayProperties: HashMap<JdsFieldEntity<*>, MutableCollection<JdsEntity>>) {
        objectArrayProperties.entries.forEach {
            //parent_uuid.entity_id.sequence e.g ab9d2da6-fb64-47a9-9a3c-a6e0a998703f.256.3
            it.value.forEachIndexed { sequence, entry ->
                entry.overview.uuid = "$uuid.${entry.overview.entityId}.$sequence"
                standardizeObjectUuids(entry.overview.uuid, entry.objectValues)
                standardizeCollectionUuids(entry.overview.uuid, entry.objectCollections)
            }
        }
    }

    /**
     * Ensures child entities have ids that link them to their parent.
     * @param uuid
     * @param objectProperties
     */
    private fun standardizeObjectUuids(uuid: String, objectProperties: HashMap<JdsFieldEntity<*>, ObjectProperty<JdsEntity>>) {
        //parent_uuid.entity_id e.g ab9d2da6-fb64-47a9-9a3c-a6e0a998703f.256
        objectProperties.entries.forEach { entry ->
            entry.value.value.overview.uuid = "$uuid.${entry.value.value.overview.entityId}"
            standardizeObjectUuids(entry.value.value.overview.uuid, entry.value.value.objectValues)
            standardizeCollectionUuids(entry.value.value.overview.uuid, entry.value.value.objectCollections)
        }
    }

    /**
     * Ensures child entities have ids that link them to their parent.
     * @param version
     * @param objectArrayProperties
     */
    private fun standardizeCollectionEditVersion(version: Int, objectArrayProperties: HashMap<JdsFieldEntity<*>, MutableCollection<JdsEntity>>) {
        objectArrayProperties.entries.forEach {
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
     * @param objectProperties
     */
    private fun standardizeObjectEditVersion(version: Int, objectProperties: HashMap<JdsFieldEntity<*>, ObjectProperty<JdsEntity>>) {
        //parent-version.entity_id.sequence e.g ab9d2da6-fb64-47a9-9a3c-a6e0a998703f.256
        objectProperties.entries.forEach { entry ->
            entry.value.value.overview.editVersion = version
            standardizeObjectEditVersion(version, entry.value.value.objectValues)
            standardizeCollectionEditVersion(version, entry.value.value.objectCollections)
        }
    }

    /**
     * Internal helper function that works with all nested objects
     */
    fun getNestedEntities(includeThisEntity: Boolean = true): Sequence<JdsEntity> = buildSequence {
        if (includeThisEntity)
            yield(this@JdsEntity)
        objectValues.values.forEach { yieldAll(it.value.getNestedEntities()) }
        objectCollections.values.forEach { it.forEach { yieldAll(it.getNestedEntities()) } }
    }

    /**
     * Internal helper function that works with all nested objects
     */
    fun getNestedEntities(collection: MutableCollection<JdsEntity>, includeThisEntity: Boolean = true) {
        if (includeThisEntity)
            collection.add(this@JdsEntity)
        objectValues.values.forEach { it.value.getNestedEntities(collection) }
        objectCollections.values.forEach { it.forEach { it.getNestedEntities(collection) } }
    }

    companion object : Externalizable {

        private const val serialVersionUID = 20180106_2125L
        private val allFields = ConcurrentHashMap<Long, LinkedHashSet<Long>>()
        private val allEnums = ConcurrentHashMap<Long, LinkedHashSet<Long>>()

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

        internal fun getFields(entityId: Long) = allFields.getOrPut(entityId) { LinkedHashSet() }

        internal fun getEnums(entityId: Long) = allEnums.getOrPut(entityId) { LinkedHashSet() }
    }
}
