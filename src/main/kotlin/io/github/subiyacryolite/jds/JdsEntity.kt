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
import javafx.beans.property.*
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
    internal val localDateTimeProperties: HashMap<Long, ObjectProperty<Temporal?>> = LinkedHashMap()
    @get:JsonIgnore
    internal val zonedDateTimeProperties: HashMap<Long, ObjectProperty<Temporal?>> = LinkedHashMap()
    @get:JsonIgnore
    internal val localDateProperties: HashMap<Long, ObjectProperty<Temporal?>> = LinkedHashMap()
    @get:JsonIgnore
    internal val localTimeProperties: HashMap<Long, ObjectProperty<Temporal?>> = LinkedHashMap()
    @get:JsonIgnore
    internal val monthDayProperties: HashMap<Long, ObjectProperty<MonthDay>> = LinkedHashMap()
    @get:JsonIgnore
    internal val yearMonthProperties: HashMap<Long, ObjectProperty<Temporal?>> = LinkedHashMap()
    @get:JsonIgnore
    internal val periodProperties: HashMap<Long, ObjectProperty<Period>> = LinkedHashMap()
    @get:JsonIgnore
    internal val durationProperties: HashMap<Long, ObjectProperty<Duration>> = LinkedHashMap()
    //strings
    @get:JsonIgnore
    internal val stringProperties: HashMap<Long, StringProperty> = LinkedHashMap()
    //numeric
    @get:JsonIgnore
    internal val floatProperties: HashMap<Long, WritableValue<Float>> = LinkedHashMap()
    @get:JsonIgnore
    internal val doubleProperties: HashMap<Long, WritableValue<Double>> = LinkedHashMap()
    @get:JsonIgnore
    internal val booleanProperties: HashMap<Long, WritableValue<Boolean>> = LinkedHashMap()
    @get:JsonIgnore
    internal val longProperties: HashMap<Long, WritableValue<Long>> = LinkedHashMap()
    @get:JsonIgnore
    internal val integerProperties: HashMap<Long, WritableValue<Int>> = LinkedHashMap()
    //arrays
    @get:JsonIgnore
    internal val objectArrayProperties: HashMap<JdsFieldEntity<*>, MutableCollection<JdsEntity>> = LinkedHashMap()
    @get:JsonIgnore
    internal val stringArrayProperties: HashMap<Long, MutableCollection<String>> = LinkedHashMap()
    @get:JsonIgnore
    internal val dateTimeArrayProperties: HashMap<Long, MutableCollection<LocalDateTime>> = LinkedHashMap()
    @get:JsonIgnore
    internal val floatArrayProperties: HashMap<Long, MutableCollection<Float>> = LinkedHashMap()
    @get:JsonIgnore
    internal val doubleArrayProperties: HashMap<Long, MutableCollection<Double>> = LinkedHashMap()
    @get:JsonIgnore
    internal val longArrayProperties: HashMap<Long, MutableCollection<Long>> = LinkedHashMap()
    @get:JsonIgnore
    internal val integerArrayProperties: HashMap<Long, MutableCollection<Int>> = LinkedHashMap()
    //enumProperties
    @get:JsonIgnore
    internal val enumProperties: HashMap<Long, ObjectProperty<Enum<*>?>> = LinkedHashMap()
    @get:JsonIgnore
    internal val enumCollectionProperties: HashMap<Long, MutableCollection<Enum<*>>> = LinkedHashMap()
    //objects
    @get:JsonIgnore
    internal val objectProperties: HashMap<JdsFieldEntity<*>, ObjectProperty<JdsEntity>> = LinkedHashMap()
    //blobs
    @get:JsonIgnore
    internal val blobProperties: HashMap<Long, BlobProperty> = LinkedHashMap()


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

    /**
     * @param field
     * @param property
     */
    protected fun map(field: JdsField, property: BlobProperty) {
        if (field.type != JdsFieldType.BLOB)
            throw RuntimeException("Please assign the correct type to field [$field]")
        mapField(overview.entityId, field.id)
        blobProperties[field.id] = property
    }

    protected fun mapMonthDay(field: JdsField, property: ObjectProperty<MonthDay>) {
        if (field.type != JdsFieldType.MONTH_DAY)
            throw RuntimeException("Please assign the correct type to field [$field]")
        mapField(overview.entityId, field.id)
        monthDayProperties[field.id] = property
    }

    protected fun mapPeriod(field: JdsField, property: ObjectProperty<Period>) {
        if (field.type != JdsFieldType.PERIOD)
            throw RuntimeException("Please assign the correct type to field [$field]")
        mapField(overview.entityId, field.id)
        periodProperties[field.id] = property
    }

    protected fun mapDuration(field: JdsField, property: ObjectProperty<Duration>) {
        if (field.type != JdsFieldType.DURATION)
            throw RuntimeException("Please assign the correct type to field [$field]")
        mapField(overview.entityId, field.id)
        durationProperties[field.id] = property
    }

    /**
     * @param field
     * @param temporalProperty
     */
    protected fun <T : Temporal?> map(field: JdsField, temporalProperty: ObjectProperty<T>) {
        val temporal = temporalProperty.get()
        when (temporal) {
            is LocalDateTime? -> {
                if (field.type != JdsFieldType.DATE_TIME)
                    throw RuntimeException("Please assign the correct type to field [$field]")
                mapField(overview.entityId, field.id)
                localDateTimeProperties[field.id] = temporalProperty as ObjectProperty<Temporal?>
            }
            is ZonedDateTime? -> {
                if (field.type != JdsFieldType.ZONED_DATE_TIME)
                    throw RuntimeException("Please assign the correct type to field [$field]")
                mapField(overview.entityId, field.id)
                zonedDateTimeProperties[field.id] = temporalProperty as ObjectProperty<Temporal?>
            }
            is LocalDate? -> {
                if (field.type != JdsFieldType.DATE)
                    throw RuntimeException("Please assign the correct type to field [$field]")
                mapField(overview.entityId, field.id)
                localDateProperties[field.id] = temporalProperty as ObjectProperty<Temporal?>
            }
            is LocalTime? -> {
                if (field.type != JdsFieldType.TIME)
                    throw RuntimeException("Please assign the correct type to field [$field]")
                mapField(overview.entityId, field.id)
                localTimeProperties[field.id] = temporalProperty as ObjectProperty<Temporal?>
            }
            is YearMonth? -> {
                if (field.type != JdsFieldType.YEAR_MONTH)
                    throw RuntimeException("Please assign the correct type to field [$field]")
                mapField(overview.entityId, field.id)
                yearMonthProperties[field.id] = temporalProperty as ObjectProperty<Temporal?>

            }
        }
    }

    /**
     * @param field
     * @param property
     */
    protected fun map(field: JdsField, property: StringProperty) {
        if (field.type != JdsFieldType.STRING)
            throw RuntimeException("Please assign the correct type to field [$field]")
        mapField(overview.entityId, field.id)
        stringProperties[field.id] = property

    }

    /**
     * @param field
     * @param property
     */
    protected fun map(field: JdsField, property: WritableValue<*>) {
        if (field.type != JdsFieldType.DOUBLE && field.type != JdsFieldType.LONG && field.type != JdsFieldType.INT && field.type != JdsFieldType.FLOAT && field.type != JdsFieldType.BOOLEAN)
            throw RuntimeException("Please assign the correct type to field [$field]")
        mapField(overview.entityId, field.id)
        when (field.type) {
            JdsFieldType.DOUBLE -> doubleProperties[field.id] = property as WritableValue<Double>
            JdsFieldType.LONG -> longProperties[field.id] = property as WritableValue<Long>
            JdsFieldType.INT -> integerProperties[field.id] = property as WritableValue<Int>
            JdsFieldType.FLOAT -> floatProperties[field.id] = property as WritableValue<Float>
            JdsFieldType.BOOLEAN -> booleanProperties[field.id] = property as WritableValue<Boolean>
        }
    }


    /**
     * @param field
     * @param properties
     */
    protected fun mapStrings(field: JdsField, properties: MutableCollection<String>) {
        if (field.type != JdsFieldType.STRING_COLLECTION)
            throw RuntimeException("Please assign the correct type to field [$field]")
        mapField(overview.entityId, field.id)
        stringArrayProperties[field.id] = properties
    }

    /**
     * @param field
     * @param properties
     */
    protected fun mapDateTimes(field: JdsField, properties: MutableCollection<LocalDateTime>) {
        if (field.type != JdsFieldType.DATE_TIME_COLLECTION)
            throw RuntimeException("Please assign the correct type to field [$field]")
        mapField(overview.entityId, field.id)
        dateTimeArrayProperties[field.id] = properties
    }

    /**
     * @param field
     * @param properties
     */
    protected fun mapFloats(field: JdsField, properties: MutableCollection<Float>) {
        if (field.type != JdsFieldType.FLOAT_COLLECTION)
            throw RuntimeException("Please assign the correct type to field [$field]")
        mapField(overview.entityId, field.id)
        floatArrayProperties[field.id] = properties
    }

    /**
     * @param field
     * @param properties
     */
    protected fun mapIntegers(field: JdsField, properties: MutableCollection<Int>) {
        if (field.type != JdsFieldType.INT_COLLECTION)
            throw RuntimeException("Please assign the correct type to field [$field]")
        mapField(overview.entityId, field.id)
        integerArrayProperties[field.id] = properties
    }

    /**
     * @param field
     * @param properties
     */
    protected fun mapDoubles(field: JdsField, properties: MutableCollection<Double>) {
        if (field.type != JdsFieldType.DOUBLE_COLLECTION)
            throw RuntimeException("Please assign the correct type to field [$field]")
        mapField(overview.entityId, field.id)
        doubleArrayProperties[field.id] = properties
    }

    /**
     * @param field
     * @param properties
     */
    protected fun mapLongs(field: JdsField, properties: MutableCollection<Long>) {
        if (field.type != JdsFieldType.LONG_COLLECTION)
            throw RuntimeException("Please assign the correct type to field [$field]")
        mapField(overview.entityId, field.id)
        longArrayProperties[field.id] = properties
    }

    /**
     * @param fieldEnum
     * @param property
     */
    protected fun map(fieldEnum: JdsFieldEnum<*>, property: ObjectProperty<out Enum<*>>) {
        if (fieldEnum.field.type != JdsFieldType.ENUM)
            throw RuntimeException("Please assign the correct type to field [${fieldEnum.field}]")
        mapEnums(overview.entityId, fieldEnum.field.id)
        mapField(overview.entityId, fieldEnum.field.id)
        enumProperties[fieldEnum.field.id] = property as ObjectProperty<Enum<*>?>
    }

    /**
     * @param fieldEnum
     * @param properties
     */
    protected fun mapEnums(fieldEnum: JdsFieldEnum<*>, properties: MutableCollection<out Enum<*>>) {
        if (fieldEnum.field.type != JdsFieldType.ENUM_COLLECTION)
            throw RuntimeException("Please assign the correct type to field [${fieldEnum.field}]")
        mapEnums(overview.entityId, fieldEnum.field.id)
        mapField(overview.entityId, fieldEnum.field.id)
        enumCollectionProperties[fieldEnum.field.id] = properties as MutableCollection<Enum<*>>
    }

    /**
     * @param entity
     * @param property
     */
    protected fun <T : IJdsEntity> map(fieldEntity: JdsFieldEntity<out T>, property: ObjectProperty<out T>) {
        if (fieldEntity.fieldEntity.type != JdsFieldType.ENTITY)
            throw RuntimeException("Please assign the correct type to field [$fieldEntity]")
        if (!objectArrayProperties.containsKey(fieldEntity) && !objectProperties.containsKey(fieldEntity)) {
            objectProperties[fieldEntity] = property as ObjectProperty<JdsEntity>
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
        if (!objectArrayProperties.containsKey(fieldEntity)) {
            objectArrayProperties[fieldEntity] = properties as MutableCollection<JdsEntity>
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
        source.booleanProperties.entries.forEach { dest.booleanProperties[it.key] = it.value }
        source.localDateTimeProperties.entries.forEach { dest.localDateTimeProperties[it.key]?.set(it.value.get()) }
        source.zonedDateTimeProperties.entries.forEach { dest.zonedDateTimeProperties[it.key]?.set(it.value.get()) }
        source.localTimeProperties.entries.forEach { dest.localTimeProperties[it.key]?.set(it.value.get()) }
        source.localDateProperties.entries.forEach { dest.localDateProperties[it.key]?.set(it.value.get()) }
        source.stringProperties.entries.forEach { dest.stringProperties[it.key]?.set(it.value.get()) }
        source.floatProperties.entries.forEach { dest.floatProperties[it.key] = it.value }
        source.doubleProperties.entries.forEach { dest.doubleProperties[it.key] = it.value }
        source.longProperties.entries.forEach { dest.longProperties[it.key] = it.value }
        source.integerProperties.entries.forEach { dest.integerProperties[it.key] = it.value }
        source.blobProperties.entries.forEach { dest.blobProperties[it.key]?.set(it.value.get()!!) }
        source.durationProperties.entries.forEach { dest.durationProperties[it.key]?.set(it.value.get()) }
        source.periodProperties.entries.forEach { dest.periodProperties[it.key]?.set(it.value.get()) }
        source.yearMonthProperties.entries.forEach { dest.yearMonthProperties[it.key]?.set(it.value.get()) }
        source.monthDayProperties.entries.forEach { dest.monthDayProperties[it.key]?.set(it.value.get()!!) }
    }

    /**
     * Copy all property array values
     *
     * @param source The entity to copy values from
     * @param <T>    A valid JDSEntity A valid JDSEntity
    </T> */
    private fun <T : JdsEntity> copyArrayValues(source: T) {
        val dest = this
        source.stringArrayProperties.entries.forEach {
            if (dest.stringArrayProperties.containsKey(it.key)) {
                val entry = dest.stringArrayProperties[it.key]
                entry?.clear()
                entry?.addAll(it.value)
            }
        }
        source.dateTimeArrayProperties.entries.forEach {
            if (dest.dateTimeArrayProperties.containsKey(it.key)) {
                val entry = dest.dateTimeArrayProperties[it.key]
                entry?.clear()
                entry?.addAll(it.value)
            }
        }
        source.floatArrayProperties.entries.forEach {
            if (dest.floatArrayProperties.containsKey(it.key)) {
                val entry = dest.floatArrayProperties[it.key]
                entry?.clear()
                entry?.addAll(it.value)
            }
        }
        source.doubleArrayProperties.entries.forEach {
            if (dest.doubleArrayProperties.containsKey(it.key)) {
                val entry = dest.doubleArrayProperties[it.key]
                entry?.clear()
                entry?.addAll(it.value)
            }
        }
        source.longArrayProperties.entries.forEach {
            if (dest.longArrayProperties.containsKey(it.key)) {
                val entry = dest.longArrayProperties[it.key]
                entry?.clear()
                entry?.addAll(it.value)
            }
        }
        source.integerArrayProperties.entries.forEach {
            if (dest.integerArrayProperties.containsKey(it.key)) {
                val entry = dest.integerArrayProperties[it.key]
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
        source.objectProperties.entries.forEach {
            if (dest.objectProperties.containsKey(it.key)) {
                dest.objectProperties[it.key]?.set(it.value.get())
            }
        }

        source.objectArrayProperties.entries.forEach {
            if (dest.objectArrayProperties.containsKey(it.key)) {
                val entry = dest.objectArrayProperties[it.key]
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
        source.enumCollectionProperties.entries.forEach {
            if (dest.enumCollectionProperties.containsKey(it.key)) {
                val dstEntry = dest.enumCollectionProperties[it.key]
                dstEntry?.clear()
                val it = it.value.iterator()
                while (it.hasNext()) {
                    val nxt = it.next()
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
    }

    @Throws(IOException::class)
    override fun writeExternal(objectOutputStream: ObjectOutput) {
        //fieldEntity and enum maps
        objectOutputStream.writeObject(overview)
        //objects
        objectOutputStream.writeObject(serializeObject(objectProperties))
        //time constructs
        objectOutputStream.writeObject(serializeTemporal(localDateTimeProperties))
        objectOutputStream.writeObject(serializeTemporal(zonedDateTimeProperties))
        objectOutputStream.writeObject(serializeTemporal(localDateProperties))
        objectOutputStream.writeObject(serializeTemporal(localTimeProperties))
        objectOutputStream.writeObject(monthDayProperties)
        objectOutputStream.writeObject(yearMonthProperties)
        objectOutputStream.writeObject(periodProperties)
        objectOutputStream.writeObject(durationProperties)
        //strings
        objectOutputStream.writeObject(serializableString(stringProperties))
        //numeric
        objectOutputStream.writeObject(floatProperties)
        objectOutputStream.writeObject(doubleProperties)
        objectOutputStream.writeObject(booleanProperties)
        objectOutputStream.writeObject(longProperties)
        objectOutputStream.writeObject(integerProperties)
        //blobs
        objectOutputStream.writeObject(serializeBlobs(blobProperties))
        //arrays
        objectOutputStream.writeObject(serializeObjects(objectArrayProperties))
        objectOutputStream.writeObject(serializeStrings(stringArrayProperties))
        objectOutputStream.writeObject(serializeDateTimes(dateTimeArrayProperties))
        objectOutputStream.writeObject(serializeFloats(floatArrayProperties))
        objectOutputStream.writeObject(serializeDoubles(doubleArrayProperties))
        objectOutputStream.writeObject(serializeLongs(longArrayProperties))
        objectOutputStream.writeObject(serializeIntegers(integerArrayProperties))
        //enumProperties
        objectOutputStream.writeObject(serializeEnums(enumProperties))
        objectOutputStream.writeObject(serializeEnumCollections(enumCollectionProperties))
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    override fun readExternal(objectInputStream: ObjectInput) {
        //fieldEntity and enum maps
        overview = objectInputStream.readObject() as JdsOverview
        //objects
        putObject(objectProperties, objectInputStream.readObject() as Map<JdsFieldEntity<*>, JdsEntity>)
        //time constructs
        putTemporal(localDateTimeProperties, objectInputStream.readObject() as Map<Long, Temporal>)
        putTemporal(zonedDateTimeProperties, objectInputStream.readObject() as Map<Long, Temporal>)
        putTemporal(localDateProperties, objectInputStream.readObject() as Map<Long, Temporal>)
        putTemporal(localTimeProperties, objectInputStream.readObject() as Map<Long, Temporal>)
        putMonthDays(monthDayProperties, objectInputStream.readObject() as Map<Long, MonthDay>)
        putYearMonths(yearMonthProperties, objectInputStream.readObject() as Map<Long, Temporal>)
        putPeriods(periodProperties, objectInputStream.readObject() as Map<Long, Period>)
        putDurations(durationProperties, objectInputStream.readObject() as Map<Long, Duration>)
        //string
        putString(stringProperties, objectInputStream.readObject() as Map<Long, String>)
        //numeric
        putFloat(floatProperties, objectInputStream.readObject() as Map<Long, WritableValue<Float>>)
        putDouble(doubleProperties, objectInputStream.readObject() as Map<Long, WritableValue<Double>>)
        putBoolean(booleanProperties, objectInputStream.readObject() as Map<Long, WritableValue<Boolean>>)
        putLong(longProperties, objectInputStream.readObject() as Map<Long, WritableValue<Long>>)
        putInteger(integerProperties, objectInputStream.readObject() as Map<Long, WritableValue<Int>>)
        //blobs
        putBlobs(blobProperties, objectInputStream.readObject() as Map<Long, BlobProperty>)
        //arrays
        putObjects(objectArrayProperties, objectInputStream.readObject() as Map<JdsFieldEntity<*>, List<JdsEntity>>)
        putStrings(stringArrayProperties, objectInputStream.readObject() as Map<Long, List<String>>)
        putDateTimes(dateTimeArrayProperties, objectInputStream.readObject() as Map<Long, List<LocalDateTime>>)
        putFloats(floatArrayProperties, objectInputStream.readObject() as Map<Long, List<Float>>)
        putDoubles(doubleArrayProperties, objectInputStream.readObject() as Map<Long, List<Double>>)
        putLongs(longArrayProperties, objectInputStream.readObject() as Map<Long, List<Long>>)
        putIntegers(integerArrayProperties, objectInputStream.readObject() as Map<Long, List<Int>>)
        //enumProperties
        putEnum(enumProperties, objectInputStream.readObject() as Map<Long, Enum<*>?>)
        putEnums(enumCollectionProperties, objectInputStream.readObject() as Map<Long, List<Enum<*>>>)
    }

    private fun serializeEnums(input: Map<Long, ObjectProperty<Enum<*>?>>): Map<Long, Enum<*>?> =
            input.entries.associateBy({ it.key }, { it.value.value })

    private fun serializeBlobs(input: Map<Long, BlobProperty>): Map<Long, BlobProperty> =
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
    private fun serializeIntegers(input: Map<Long, Collection<Int>>): Map<Long, List<Int>> =
            input.entries.associateBy({ it.key }, { ArrayList(it.value) })

    /**
     * Create a map that can be serialized
     * @param input an unserializable map
     * @return A serialisable map
     */
    private fun serializeLongs(input: Map<Long, Collection<Long>>): Map<Long, List<Long>> =
            input.entries.associateBy({ it.key }, { ArrayList(it.value) })

    /**
     * Create a map that can be serialized
     * @param input an unserializable map
     * @return A serialisable map
     */
    private fun serializeDoubles(input: Map<Long, Collection<Double>>): Map<Long, List<Double>> =
            input.entries.associateBy({ it.key }, { ArrayList(it.value) })

    /**
     * Create a map that can be serialized
     * @param input an unserializable map
     * @return A serialisable map
     */
    private fun serializeFloats(input: Map<Long, Collection<Float>>): Map<Long, List<Float>> =
            input.entries.associateBy({ it.key }, { ArrayList(it.value) })

    /**
     * Create a map that can be serialized
     * @param input an unserializable map
     * @return A serialisable map
     */
    private fun serializeDateTimes(input: Map<Long, Collection<LocalDateTime>>): Map<Long, List<LocalDateTime>> =
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
    private fun <T : Temporal?> serializeTemporal(input: Map<Long, ObjectProperty<T>>): Map<Long, T> =
            input.entries.associateBy({ it.key }, { it.value.get() })

    /**
     * Create a map that can be serialized
     * @param input an unserializable map
     * @return A serialisable map
     */
    private fun serializableString(input: Map<Long, StringProperty>): Map<Long, String> =
            input.entries.associateBy({ it.key }, { it.value.get() })

    private fun putDurations(destination: HashMap<Long, ObjectProperty<Duration>>, source: Map<Long, Duration>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putPeriods(destination: HashMap<Long, ObjectProperty<Period>>, source: Map<Long, Period>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putYearMonths(destination: HashMap<Long, ObjectProperty<Temporal?>>, source: Map<Long, Temporal?>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putMonthDays(destination: HashMap<Long, ObjectProperty<MonthDay>>, source: Map<Long, MonthDay>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putEnums(destination: Map<Long, MutableCollection<Enum<*>>>, source: Map<Long, List<Enum<*>>>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.addAll(entry.value) }
    }

    private fun putEnum(destination: Map<Long, ObjectProperty<Enum<*>?>>, source: Map<Long, Enum<*>?>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putObjects(destination: Map<JdsFieldEntity<*>, MutableCollection<JdsEntity>>, source: Map<JdsFieldEntity<*>, List<JdsEntity>>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.addAll(entry.value) }
    }

    private fun putStrings(destination: Map<Long, MutableCollection<String>>, source: Map<Long, List<String>>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.addAll(entry.value) }
    }

    private fun putDateTimes(destination: Map<Long, MutableCollection<LocalDateTime>>, source: Map<Long, List<LocalDateTime>>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.addAll(entry.value) }
    }

    private fun putFloats(destination: Map<Long, MutableCollection<Float>>, source: Map<Long, List<Float>>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.addAll(entry.value) }
    }

    private fun putDoubles(destination: Map<Long, MutableCollection<Double>>, source: Map<Long, List<Double>>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.addAll(entry.value) }
    }

    private fun putLongs(destination: Map<Long, MutableCollection<Long>>, source: Map<Long, List<Long>>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.addAll(entry.value) }
    }

    private fun putIntegers(destination: Map<Long, MutableCollection<Int>>, source: Map<Long, List<Int>>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.addAll(entry.value) }
    }

    private fun putInteger(destination: MutableMap<Long, WritableValue<Int>>, source: Map<Long, WritableValue<Int>>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key] = entry.value }
    }

    private fun putBlobs(destination: MutableMap<Long, BlobProperty>, source: Map<Long, BlobProperty>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.set(entry.value.get()!!) }
    }

    private fun putLong(destination: MutableMap<Long, WritableValue<Long>>, source: Map<Long, WritableValue<Long>>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key] = entry.value }
    }

    private fun putBoolean(destination: MutableMap<Long, WritableValue<Boolean>>, source: Map<Long, WritableValue<Boolean>>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key] = entry.value }
    }

    private fun putDouble(destination: MutableMap<Long, WritableValue<Double>>, source: Map<Long, WritableValue<Double>>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key] = entry.value }
    }

    private fun putObject(destination: Map<JdsFieldEntity<*>, ObjectProperty<JdsEntity>>, source: Map<JdsFieldEntity<*>, JdsEntity>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putFloat(destination: MutableMap<Long, WritableValue<Float>>, source: Map<Long, WritableValue<Float>>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key] = entry.value }
    }

    private fun putTemporal(destination: Map<Long, ObjectProperty<in Temporal>>, source: Map<Long, Temporal>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putString(destination: Map<Long, StringProperty>, source: Map<Long, String>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.set(entry.value) }
    }

    /**
     * @param embeddedObject
     */
    internal fun assign(embeddedObject: JdsEmbeddedObject) {
        //==============================================
        //PRIMITIVES, also saved to array struct to streamline json
        //==============================================
        booleanProperties.entries.forEach {
            val input = when (it.value.value) {
                true -> 1
                false -> 0
                else -> null
            }
            embeddedObject.booleanValues.add(JdsStoreBoolean(it.key, input))
        }
        stringProperties.entries.forEach { embeddedObject.stringValues.add(JdsStoreString(it.key, it.value.value)) }
        floatProperties.entries.forEach { embeddedObject.floatValue.add(JdsStoreFloat(it.key, it.value.value)) }
        doubleProperties.entries.forEach { embeddedObject.doubleValues.add(JdsStoreDouble(it.key, it.value.value)) }
        longProperties.entries.forEach { embeddedObject.longValues.add(JdsStoreLong(it.key, it.value.value)) }
        integerProperties.entries.forEach { embeddedObject.integerValues.add(JdsStoreInteger(it.key, it.value.value)) }
        //==============================================
        //Dates & Time
        //==============================================
        zonedDateTimeProperties.entries.forEach { embeddedObject.zonedDateTimeValues.add(JdsStoreZonedDateTime(it.key, (it.value.value as ZonedDateTime).toInstant().toEpochMilli())) }
        localTimeProperties.entries.forEach { embeddedObject.timeValues.add(JdsStoreTime(it.key, (it.value.value as LocalTime).toNanoOfDay())) }
        durationProperties.entries.forEach { embeddedObject.durationValues.add(JdsStoreDuration(it.key, it.value.value.toNanos())) }
        localDateTimeProperties.entries.forEach { embeddedObject.dateTimeValues.add(JdsStoreDateTime(it.key, Timestamp.valueOf(it.value.value as LocalDateTime))) }
        localDateProperties.entries.forEach { embeddedObject.dateValues.add(JdsStoreDate(it.key, Timestamp.valueOf((it.value.value as LocalDate).atStartOfDay()))) }
        monthDayProperties.entries.forEach { embeddedObject.monthDayValues.add(JdsStoreMonthDay(it.key, it.value.value?.toString())) }
        yearMonthProperties.entries.forEach { embeddedObject.yearMonthValues.add(JdsStoreYearMonth(it.key, (it.value.value as YearMonth?)?.toString())) }
        periodProperties.entries.forEach { embeddedObject.periodValues.add(JdsStorePeriod(it.key, it.value.value?.toString())) }
        //==============================================
        //BLOB
        //==============================================
        blobProperties.entries.forEach {
            embeddedObject.blobValues.add(JdsStoreBlob(it.key, it.value.get() ?: ByteArray(0)))
        }
        //==============================================
        //Enums
        //==============================================
        enumProperties.entries.forEach { embeddedObject.enumValues.add(JdsStoreEnum(it.key, it.value.value?.ordinal)) }
        enumCollectionProperties.entries.forEach { embeddedObject.enumCollections.add(JdsStoreEnumCollection(it.key, toIntCollection(it.value))) }
        //==============================================
        //ARRAYS
        //==============================================
        stringArrayProperties.entries.forEach { embeddedObject.stringCollections.add(JdsStoreStringCollection(it.key, it.value)) }
        dateTimeArrayProperties.entries.forEach { embeddedObject.dateTimeCollection.add(JdsStoreDateTimeCollection(it.key, toTimeStampCollection(it.value))) }
        floatArrayProperties.entries.forEach { embeddedObject.floatCollections.add(JdsStoreFloatCollection(it.key, it.value)) }
        doubleArrayProperties.entries.forEach { embeddedObject.doubleCollections.add(JdsStoreDoubleCollection(it.key, it.value)) }
        longArrayProperties.entries.forEach { embeddedObject.longCollections.add(JdsStoreLongCollection(it.key, it.value)) }
        integerArrayProperties.entries.forEach { embeddedObject.integerCollections.add(JdsStoreIntegerCollection(it.key, it.value)) }
        //==============================================
        //EMBEDDED OBJECTS
        //==============================================
        objectArrayProperties.forEach { key, itx ->
            itx.forEach {
                val eo = JdsEmbeddedObject()
                eo.fieldId = key.fieldEntity.id
                eo.init(it)
                embeddedObject.entityOverviews.add(eo)
            }
        }
        objectProperties.forEach { key, it ->
            val eo = JdsEmbeddedObject()
            eo.fieldId = key.fieldEntity.id
            eo.init(it.value)
            embeddedObject.entityOverviews.add(eo)
        }
    }

    private fun toTimeStampCollection(values: MutableCollection<LocalDateTime>): MutableCollection<Timestamp> {
        val dest = ArrayList<Timestamp>()
        values.forEach { dest.add(Timestamp.valueOf(it)) }
        return dest
    }

    private fun toIntCollection(values: MutableCollection<Enum<*>>): MutableCollection<Int> {
        val dest = ArrayList<Int>()
        values.forEach { dest.add(it.ordinal) }
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

            JdsFieldType.FLOAT -> floatProperties[fieldId]?.value = when (value) {
                is Double -> value.toFloat()
                else -> value as Float?
            }

            JdsFieldType.DOUBLE -> doubleProperties[fieldId]?.value = value as Double?

            JdsFieldType.LONG -> longProperties[fieldId]?.value = when (value) {
                is Long? -> value
                is BigDecimal -> value.toLong() //Oracle
                is Int -> value.toLong()
                else -> null
            }

            JdsFieldType.INT -> integerProperties[fieldId]?.value = when (value) {
                is Int? -> value
                is BigDecimal -> value.toInt() //Oracle
                else -> null
            }

            JdsFieldType.BOOLEAN -> booleanProperties[fieldId]?.value = when (value) {
                is Int -> value == 1
                is Boolean? -> value
                is BigDecimal -> value.intValueExact() == 1 //Oracle
                else -> null
            }

            JdsFieldType.DOUBLE_COLLECTION -> doubleArrayProperties[fieldId]?.add(value as Double)

            JdsFieldType.FLOAT_COLLECTION -> floatArrayProperties[fieldId]?.add(value as Float)

            JdsFieldType.LONG_COLLECTION -> longArrayProperties[fieldId]?.add(value as Long)

            JdsFieldType.INT_COLLECTION -> integerArrayProperties[fieldId]?.add(value as Int)

            JdsFieldType.ENUM -> enumProperties.filter { it.key == fieldId }.forEach {
                val fieldEnum = JdsFieldEnum.enums[it.key]
                if (fieldEnum != null)
                    it.value?.set(when (value) {
                        is BigDecimal -> fieldEnum.valueOf(value.intValueExact())
                        else -> fieldEnum.valueOf(value as Int)
                    })
            }

            JdsFieldType.ENUM_COLLECTION -> enumCollectionProperties.filter { it.key == fieldId }.forEach {
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

            JdsFieldType.STRING -> stringProperties[fieldId]?.set(value as String?)

            JdsFieldType.STRING_COLLECTION -> stringArrayProperties[fieldId]?.add(value as String)

            JdsFieldType.ZONED_DATE_TIME -> when (value) {
                is Long -> zonedDateTimeProperties[fieldId]?.set(ZonedDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault()))
                is Timestamp -> zonedDateTimeProperties[fieldId]?.set(value.let { ZonedDateTime.ofInstant(it.toInstant(), ZoneOffset.systemDefault()) })
                is String -> zonedDateTimeProperties[fieldId]?.set(value.let { it.toZonedDateTime() })
                is OffsetDateTime -> zonedDateTimeProperties[fieldId]?.set(value.let { it.atZoneSameInstant(ZoneId.systemDefault()) })
            }

            JdsFieldType.DATE -> when (value) {
                is Timestamp -> localDateProperties[fieldId]?.set(value.toLocalDateTime().toLocalDate())
                is LocalDate -> localDateProperties[fieldId]?.set(value)
                is String -> localDateProperties[fieldId]?.set(value.toLocalDate())
                else -> localDateProperties[fieldId]?.set(LocalDate.now())
            }

            JdsFieldType.TIME -> when (value) {
                is Long -> localTimeProperties[fieldId]?.set(LocalTime.MIN.plusNanos(value))
                is LocalTime -> localTimeProperties[fieldId]?.set(value)
                is String -> localTimeProperties[fieldId]?.set(value.toLocalTime())
            }

            JdsFieldType.DURATION -> durationProperties[fieldId]?.set(when (value) {
                is BigDecimal -> Duration.ofNanos(value.longValueExact())//Oracle
                else -> Duration.ofNanos(value as Long)
            })

            JdsFieldType.MONTH_DAY -> monthDayProperties[fieldId]?.value = (value as String).let { MonthDay.parse(it) }

            JdsFieldType.YEAR_MONTH -> yearMonthProperties[fieldId]?.value = (value as String).let { YearMonth.parse(it) }

            JdsFieldType.PERIOD -> periodProperties[fieldId]?.value = (value as String).let { Period.parse(it) }

            JdsFieldType.DATE_TIME -> localDateTimeProperties[fieldId]?.value = (value as Timestamp).let { it.toLocalDateTime() }

            JdsFieldType.DATE_TIME_COLLECTION -> dateTimeArrayProperties[fieldId]?.add((value as Timestamp).let { it.toLocalDateTime() })

            JdsFieldType.BLOB -> when (value) {
                is ByteArray -> blobProperties[fieldId]?.set(value)
                null -> blobProperties[fieldId]?.set(ByteArray(0))//Oracle
            }
        }
    }

    /**
     * This method enforces forward compatibility by ensuring that every property is present even if the field is not defined or known locally
     */
    private fun initBackingPropertyIfNotDefined(fieldType: JdsFieldType, fieldId: Long, value: Any?) {
        when (fieldType) {

            JdsFieldType.STRING -> if (!stringProperties.containsKey(fieldId))
                stringProperties[fieldId] = SimpleStringProperty("")

            JdsFieldType.DOUBLE_COLLECTION -> if (!doubleArrayProperties.containsKey(fieldId))
                doubleArrayProperties[fieldId] = ArrayList()

            JdsFieldType.FLOAT_COLLECTION -> if (!floatArrayProperties.containsKey(fieldId))
                floatArrayProperties[fieldId] = ArrayList()

            JdsFieldType.LONG_COLLECTION -> if (!longArrayProperties.containsKey(fieldId))
                longArrayProperties[fieldId] = ArrayList()

            JdsFieldType.INT_COLLECTION -> if (!integerArrayProperties.containsKey(fieldId))
                integerArrayProperties[fieldId] = ArrayList()

            JdsFieldType.STRING_COLLECTION -> if (!stringArrayProperties.containsKey(fieldId))
                stringArrayProperties[fieldId] = ArrayList()

            JdsFieldType.DATE_TIME_COLLECTION -> if (!dateTimeArrayProperties.containsKey(fieldId))
                dateTimeArrayProperties[fieldId] = ArrayList()

            JdsFieldType.ENUM_COLLECTION -> if (!enumCollectionProperties.containsKey(fieldId))
                enumCollectionProperties[fieldId] = ArrayList()

            JdsFieldType.ZONED_DATE_TIME -> if (!zonedDateTimeProperties.containsKey(fieldId))
                zonedDateTimeProperties[fieldId] = SimpleObjectProperty<Temporal>()

            JdsFieldType.DATE -> if (!localDateProperties.containsKey(fieldId))
                localDateProperties[fieldId] = SimpleObjectProperty<Temporal>()

            JdsFieldType.TIME -> if (!localTimeProperties.containsKey(fieldId))
                localTimeProperties[fieldId] = SimpleObjectProperty<Temporal>()

            JdsFieldType.DURATION -> if (!durationProperties.containsKey(fieldId))
                durationProperties[fieldId] = SimpleObjectProperty<Duration>()

            JdsFieldType.MONTH_DAY -> if (!monthDayProperties.containsKey(fieldId))
                monthDayProperties[fieldId] = SimpleObjectProperty<MonthDay>()

            JdsFieldType.YEAR_MONTH -> if (!yearMonthProperties.containsKey(fieldId))
                yearMonthProperties[fieldId] = SimpleObjectProperty<Temporal>()

            JdsFieldType.PERIOD -> if (!periodProperties.containsKey(fieldId))
                periodProperties[fieldId] = SimpleObjectProperty<Period>()

            JdsFieldType.DATE_TIME -> if (!localDateTimeProperties.containsKey(fieldId))
                localDateTimeProperties[fieldId] = SimpleObjectProperty<Temporal>()

            JdsFieldType.BLOB -> if (!blobProperties.containsKey(fieldId))
                blobProperties[fieldId] = SimpleBlobProperty(byteArrayOf())

            JdsFieldType.ENUM -> if (!enumProperties.containsKey(fieldId))
                enumProperties[fieldId] = SimpleObjectProperty()

            JdsFieldType.FLOAT -> if (!floatProperties.containsKey(fieldId))
                floatProperties[fieldId] = object : WritableValue<Float> {

                    private var backingValue: Float? = null

                    override fun setValue(value: Float?) {
                        backingValue = value
                    }

                    override fun getValue(): Float? = backingValue
                }

            JdsFieldType.DOUBLE -> if (!doubleProperties.containsKey(fieldId))
                doubleProperties[fieldId] = object : WritableValue<Double> {

                    private var backingValue: Double? = null

                    override fun setValue(value: Double?) {
                        backingValue = value
                    }

                    override fun getValue(): Double? = backingValue
                }

            JdsFieldType.LONG -> if (!longProperties.containsKey(fieldId))
                longProperties[fieldId] = object : WritableValue<Long> {

                    private var backingValue: Long? = null

                    override fun setValue(value: Long?) {
                        backingValue = value
                    }

                    override fun getValue(): Long? = backingValue
                }

            JdsFieldType.INT -> if (!integerProperties.containsKey(fieldId))
                integerProperties[fieldId] = object : WritableValue<Int> {

                    private var backingValue: Int? = null

                    override fun setValue(value: Int?) {
                        backingValue = value
                    }

                    override fun getValue(): Int? = backingValue
                }

            JdsFieldType.BOOLEAN -> if (!booleanProperties.containsKey(fieldId))
                booleanProperties[fieldId] = object : WritableValue<Boolean> {

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
            objectArrayProperties.filter { it.key.fieldEntity.id == fieldId }.forEach {
                val entity = jdsDb.classes[entityId]!!.newInstance()
                entity.overview.uuid = uuid
                entity.overview.editVersion = editVersion
                uuids.add(JdsEntityComposite(uuid, editVersion))
                it.value.add(entity)
                innerObjects.add(entity)
            }
            objectProperties.filter { it.key.fieldEntity.id == fieldId }.forEach {
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
            for (fieldIds in fieldIds) {
                val jdsFieldEnum = JdsFieldEnum.enums[fieldIds]!!
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
        if (localDateTimeProperties.containsKey(fieldId))
            return localDateTimeProperties[fieldId]!!.value as LocalDateTime
        if (zonedDateTimeProperties.containsKey(fieldId))
            return zonedDateTimeProperties[fieldId]!!.value as ZonedDateTime
        if (localDateProperties.containsKey(fieldId))
            return localDateProperties[fieldId]!!.value as LocalDate
        if (localTimeProperties.containsKey(fieldId))
            return localTimeProperties[fieldId]!!.value as LocalTime
        if (monthDayProperties.containsKey(fieldId))
            return monthDayProperties[fieldId]!!.value.toString()
        if (yearMonthProperties.containsKey(fieldId))
            return yearMonthProperties[fieldId]!!.value.toString()
        if (periodProperties.containsKey(fieldId))
            return periodProperties[fieldId]!!.value.toString()
        if (durationProperties.containsKey(fieldId))
            return durationProperties[fieldId]!!.value.toNanos()
        if (stringProperties.containsKey(fieldId))
            return stringProperties[fieldId]!!.value
        if (floatProperties.containsKey(fieldId))
            return floatProperties[fieldId]?.value
        if (doubleProperties.containsKey(fieldId))
            return doubleProperties[fieldId]?.value
        if (booleanProperties.containsKey(fieldId))
            return booleanProperties[fieldId]?.value
        if (longProperties.containsKey(fieldId))
            return longProperties[fieldId]?.value
        if (integerProperties.containsKey(fieldId))
            return integerProperties[fieldId]?.value
        enumProperties.filter { it.key == fieldId && it.value.value != null && it.value.value!!.ordinal == ordinal }.forEach {
            return 1
        }
        enumCollectionProperties.filter { it.key == fieldId }.forEach { it.value.filter { it != null && it.ordinal == ordinal }.forEach { return true } }
        objectProperties.filter { it.key.fieldEntity.id == fieldId }.forEach { return it.value.value.overview.uuid }
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
        standardizeObjectUuids(pattern, objectProperties)
        standardizeCollectionUuids(pattern, objectArrayProperties)
    }

    /**
     * Ensures child entities have edit versions that match their parents
     * For frequent refreshes/imports from different sources this is necessary to prevent duplicate entries of the same data
     * @param version The version to set for each nested entity
     */
    @JvmOverloads
    fun standardizeEditVersion(version: Int = overview.editVersion) {
        standardizeObjectEditVersion(version, objectProperties)
        standardizeCollectionEditVersion(version, objectArrayProperties)
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
                standardizeObjectUuids(entry.overview.uuid, entry.objectProperties)
                standardizeCollectionUuids(entry.overview.uuid, entry.objectArrayProperties)
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
            standardizeObjectUuids(entry.value.value.overview.uuid, entry.value.value.objectProperties)
            standardizeCollectionUuids(entry.value.value.overview.uuid, entry.value.value.objectArrayProperties)
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
                standardizeObjectEditVersion(version, entry.objectProperties)
                standardizeCollectionEditVersion(version, entry.objectArrayProperties)
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
            standardizeObjectEditVersion(version, entry.value.value.objectProperties)
            standardizeCollectionEditVersion(version, entry.value.value.objectArrayProperties)
        }
    }

    /**
     * Internal helper function that works with all nested objects
     */
    fun getNestedEntities(includeThisEntity: Boolean = true): Sequence<JdsEntity> = buildSequence {
        if (includeThisEntity)
            yield(this@JdsEntity)
        objectProperties.values.forEach { yieldAll(it.value.getNestedEntities()) }
        objectArrayProperties.values.forEach { it.forEach { yieldAll(it.getNestedEntities()) } }
    }

    /**
     * Internal helper function that works with all nested objects
     */
    fun getNestedEntities(collection: MutableCollection<JdsEntity>, includeThisEntity: Boolean = true) {
        if (includeThisEntity)
            collection.add(this@JdsEntity)
        objectProperties.values.forEach { it.value.getNestedEntities(collection) }
        objectArrayProperties.values.forEach { it.forEach { it.getNestedEntities(collection) } }
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
