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

import com.javaworld.NamedCallableStatement
import com.javaworld.NamedPreparedStatement
import io.github.subiyacryolite.jds.JdsExtensions.toLocalTimeSqlFormat
import io.github.subiyacryolite.jds.JdsExtensions.toZonedDateTime
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation
import io.github.subiyacryolite.jds.embedded.*
import io.github.subiyacryolite.jds.enums.JdsFieldType
import javafx.beans.property.*
import java.io.Externalizable
import java.io.IOException
import java.io.ObjectInput
import java.io.ObjectOutput
import java.sql.Connection
import java.sql.Timestamp
import java.time.*
import java.time.temporal.Temporal
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.collections.HashSet
import kotlin.coroutines.experimental.buildSequence

/**
 * This class allows for all mapping operations in JDS, it also uses
 * [IJdsOverview] to store overview data
 */
abstract class JdsEntity : IJdsEntity {
    override var overview: IJdsOverview = JdsOverview()
    //time constructs
    private val localDateTimeProperties: HashMap<Long, ObjectProperty<Temporal>> = HashMap()
    private val zonedDateTimeProperties: HashMap<Long, ObjectProperty<Temporal>> = HashMap()
    private val localDateProperties: HashMap<Long, ObjectProperty<Temporal>> = HashMap()
    private val localTimeProperties: HashMap<Long, ObjectProperty<Temporal>> = HashMap()
    private val monthDayProperties: HashMap<Long, ObjectProperty<MonthDay>> = HashMap()
    private val yearMonthProperties: HashMap<Long, ObjectProperty<Temporal>> = HashMap()
    private val periodProperties: HashMap<Long, ObjectProperty<Period>> = HashMap()
    private val durationProperties: HashMap<Long, ObjectProperty<Duration>> = HashMap()
    //strings
    private val stringProperties: HashMap<Long, StringProperty> = HashMap()
    //numeric
    private val floatProperties: HashMap<Long, FloatProperty> = HashMap()
    private val doubleProperties: HashMap<Long, DoubleProperty> = HashMap()
    private val booleanProperties: HashMap<Long, BooleanProperty> = HashMap()
    private val longProperties: HashMap<Long, LongProperty> = HashMap()
    private val integerProperties: HashMap<Long, IntegerProperty> = HashMap()
    //arrays
    private val objectArrayProperties: HashMap<JdsFieldEntity<*>, MutableCollection<JdsEntity>> = HashMap()
    private val stringArrayProperties: HashMap<Long, MutableCollection<String?>> = HashMap()
    private val dateTimeArrayProperties: HashMap<Long, MutableCollection<LocalDateTime>> = HashMap()
    private val floatArrayProperties: HashMap<Long, MutableCollection<Float>> = HashMap()
    private val doubleArrayProperties: HashMap<Long, MutableCollection<Double>> = HashMap()
    private val longArrayProperties: HashMap<Long, MutableCollection<Long>> = HashMap()
    private val integerArrayProperties: HashMap<Long, MutableCollection<Int>> = HashMap()
    //enumProperties
    private val enumProperties: HashMap<JdsFieldEnum<*>, ObjectProperty<Enum<*>>> = HashMap()
    private val enumCollectionProperties: HashMap<JdsFieldEnum<*>, MutableCollection<Enum<*>>> = HashMap()
    //objects
    private val objectProperties: HashMap<JdsFieldEntity<*>, ObjectProperty<JdsEntity>> = HashMap()
    //blobs
    private val blobProperties: HashMap<Long, BlobProperty> = HashMap()


    init {
        val classHasAnnotation = javaClass.isAnnotationPresent(JdsEntityAnnotation::class.java)
        val superclassHasAnnotation = javaClass.superclass.isAnnotationPresent(JdsEntityAnnotation::class.java)
        if (classHasAnnotation || superclassHasAnnotation) {
            val entityAnnotation = when (classHasAnnotation) {
                true -> javaClass.getAnnotation(JdsEntityAnnotation::class.java)
                false -> javaClass.superclass.getAnnotation(JdsEntityAnnotation::class.java)
            }
            overview.entityId = entityAnnotation.entityId
            overview.version = entityAnnotation.version
        } else {
            throw RuntimeException("You must annotate the class [" + javaClass.canonicalName + "] with [" + JdsEntityAnnotation::class.java + "]")
        }
    }

    /**
     * @param field
     * @param integerProperty
     */
    protected fun map(field: JdsField, integerProperty: BlobProperty) {
        if (field.type != JdsFieldType.BLOB)
            throw RuntimeException("Please assign the correct type to field [$field]")
        mapField(overview.entityId, field.id)
        blobProperties.put(field.id, integerProperty)
    }

    /**
     * @param field
     * @param integerProperty
     */
    protected fun map(field: JdsField, integerProperty: IntegerProperty) {
        if (field.type != JdsFieldType.INT)
            throw RuntimeException("Please assign the correct type to field [$field]")
        mapField(overview.entityId, field.id)
        integerProperties.put(field.id, integerProperty)
    }

    protected fun mapMonthDay(field: JdsField, property: ObjectProperty<MonthDay>) {
        if (field.type != JdsFieldType.MONTH_DAY)
            throw RuntimeException("Please assign the correct type to field [$field]")
        mapField(overview.entityId, field.id)
        monthDayProperties.put(field.id, property)
    }

    protected fun mapPeriod(field: JdsField, property: ObjectProperty<Period>) {
        if (field.type != JdsFieldType.PERIOD)
            throw RuntimeException("Please assign the correct type to field [$field]")
        mapField(overview.entityId, field.id)
        periodProperties.put(field.id, property)
    }

    protected fun mapDuration(field: JdsField, property: ObjectProperty<Duration>) {
        if (field.type != JdsFieldType.DURATION)
            throw RuntimeException("Please assign the correct type to field [$field]")
        mapField(overview.entityId, field.id)
        durationProperties.put(field.id, property)
    }

    /**
     * @param field
     * @param temporalProperty
     */
    protected fun map(field: JdsField, temporalProperty: ObjectProperty<out Temporal>) {
        val temporal = temporalProperty.get()
        when (temporal) {
            is LocalDateTime -> {
                if (field.type != JdsFieldType.DATE_TIME)
                    throw RuntimeException("Please assign the correct type to field [$field]")
                mapField(overview.entityId, field.id)
                localDateTimeProperties.put(field.id, temporalProperty as ObjectProperty<Temporal>)
            }
            is ZonedDateTime -> {
                if (field.type != JdsFieldType.ZONED_DATE_TIME)
                    throw RuntimeException("Please assign the correct type to field [$field]")
                mapField(overview.entityId, field.id)
                zonedDateTimeProperties.put(field.id, temporalProperty as ObjectProperty<Temporal>)
            }
            is LocalDate -> {
                if (field.type != JdsFieldType.DATE)
                    throw RuntimeException("Please assign the correct type to field [$field]")
                mapField(overview.entityId, field.id)
                localDateProperties.put(field.id, temporalProperty as ObjectProperty<Temporal>)
            }
            is LocalTime -> {
                if (field.type != JdsFieldType.TIME)
                    throw RuntimeException("Please assign the correct type to field [$field]")
                mapField(overview.entityId, field.id)
                localTimeProperties.put(field.id, temporalProperty as ObjectProperty<Temporal>)
            }
            is YearMonth -> {
                if (field.type != JdsFieldType.YEAR_MONTH)
                    throw RuntimeException("Please assign the correct type to field [$field]")
                mapField(overview.entityId, field.id)
                yearMonthProperties.put(field.id, temporalProperty as ObjectProperty<Temporal>)

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
        stringProperties.put(field.id, property)

    }

    /**
     * @param field
     * @param property
     */
    protected fun map(field: JdsField, property: FloatProperty) {
        if (field.type != JdsFieldType.FLOAT)
            throw RuntimeException("Please assign the correct type to field [$field]")
        mapField(overview.entityId, field.id)
        floatProperties.put(field.id, property)
    }

    /**
     * @param field
     * @param property
     */
    protected fun map(field: JdsField, property: LongProperty) {
        if (field.type != JdsFieldType.LONG)
            throw RuntimeException("Please assign the correct type to field [$field]")
        mapField(overview.entityId, field.id)
        longProperties.put(field.id, property)
    }

    /**
     * @param field
     * @param property
     */
    protected fun map(field: JdsField, property: DoubleProperty) {
        if (field.type != JdsFieldType.DOUBLE)
            throw RuntimeException("Please assign the correct type to field [$field]")
        mapField(overview.entityId, field.id)
        doubleProperties.put(field.id, property)

    }

    /**
     * @param field
     * @param property
     */
    protected fun map(field: JdsField, property: BooleanProperty) {
        if (field.type != JdsFieldType.BOOLEAN)
            throw RuntimeException("Please assign the correct type to field [$field]")
        mapField(overview.entityId, field.id)
        booleanProperties.put(field.id, property)
    }

    /**
     * @param field
     * @param properties
     */
    protected fun mapStrings(field: JdsField, properties: MutableCollection<String?>) {
        if (field.type != JdsFieldType.STRING_COLLECTION)
            throw RuntimeException("Please assign the correct type to field [$field]")
        mapField(overview.entityId, field.id)
        stringArrayProperties.put(field.id, properties)
    }

    /**
     * @param field
     * @param properties
     */
    protected fun mapDateTimes(field: JdsField, properties: MutableCollection<LocalDateTime>) {
        if (field.type != JdsFieldType.DATE_TIME_COLLECTION)
            throw RuntimeException("Please assign the correct type to field [$field]")
        mapField(overview.entityId, field.id)
        dateTimeArrayProperties.put(field.id, properties)
    }

    /**
     * @param field
     * @param properties
     */
    protected fun mapFloats(field: JdsField, properties: MutableCollection<Float>) {
        if (field.type != JdsFieldType.FLOAT_COLLECTION)
            throw RuntimeException("Please assign the correct type to field [$field]")
        mapField(overview.entityId, field.id)
        floatArrayProperties.put(field.id, properties)
    }

    /**
     * @param field
     * @param properties
     */
    protected fun mapIntegers(field: JdsField, properties: MutableCollection<Int>) {
        if (field.type != JdsFieldType.INT_COLLECTION)
            throw RuntimeException("Please assign the correct type to field [$field]")
        mapField(overview.entityId, field.id)
        integerArrayProperties.put(field.id, properties)
    }

    /**
     * @param field
     * @param properties
     */
    protected fun mapDoubles(field: JdsField, properties: MutableCollection<Double>) {
        if (field.type != JdsFieldType.DOUBLE_COLLECTION)
            throw RuntimeException("Please assign the correct type to field [$field]")
        mapField(overview.entityId, field.id)
        doubleArrayProperties.put(field.id, properties)
    }

    /**
     * @param field
     * @param properties
     */
    protected fun mapLongs(field: JdsField, properties: MutableCollection<Long>) {
        if (field.type != JdsFieldType.LONG_COLLECTION)
            throw RuntimeException("Please assign the correct type to field [$field]")
        mapField(overview.entityId, field.id)
        longArrayProperties.put(field.id, properties)
    }

    /**
     * @param fieldEnum
     * @param property
     */
    protected fun map(fieldEnum: JdsFieldEnum<*>, property: ObjectProperty<out Enum<*>>) {
        if (fieldEnum.field.type != JdsFieldType.ENUM)
            throw RuntimeException("Please assign the correct type to field [$fieldEnum]")
        mapEnums(overview.entityId, fieldEnum.field.id)
        mapField(overview.entityId, fieldEnum.field.id)
        enumProperties.put(fieldEnum, property as ObjectProperty<Enum<*>>)
    }

    /**
     * @param fieldEnum
     * @param properties
     */
    protected fun mapEnums(fieldEnum: JdsFieldEnum<*>, properties: MutableCollection<out Enum<*>>) {
        if (fieldEnum.field.type != JdsFieldType.ENUM_COLLECTION)
            throw RuntimeException("Please assign the correct type to field [$fieldEnum]")
        mapEnums(overview.entityId, fieldEnum.field.id)
        mapField(overview.entityId, fieldEnum.field.id)
        enumCollectionProperties.put(fieldEnum, properties as MutableCollection<Enum<*>>)

    }

    /**
     * @param entity
     * @param property
     */
    protected fun <T : IJdsEntity> map(fieldEntity: JdsFieldEntity<out T>, property: ObjectProperty<out T>) {
        if (fieldEntity.fieldEntity.type != JdsFieldType.ENTITY)
            throw RuntimeException("Please assign the correct type to field [$fieldEntity]")
        if (!objectArrayProperties.containsKey(fieldEntity) && !objectProperties.containsKey(fieldEntity)) {
            objectProperties.put(fieldEntity, property as ObjectProperty<JdsEntity>)
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
            objectArrayProperties.put(fieldEntity, properties as MutableCollection<JdsEntity>)
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
        overview.dateCreated = source.overview.dateCreated
        overview.dateModified = source.overview.dateModified
        overview.uuid = source.overview.uuid
        overview.live = source.overview.live
        overview.version = source.overview.version
    }

    /**
     * Copy all property values
     *
     * @param source The entity to copy values from
     * @param <T>    A valid JDSEntity A valid JDSEntity
    </T> */
    private fun <T : JdsEntity> copyPropertyValues(source: T) {
        val dest = this
        source.booleanProperties.entries.forEach {
            if (dest.booleanProperties.containsKey(it.key)) {
                dest.booleanProperties[it.key]?.set(it.value.get())
            }
        }
        source.localDateTimeProperties.entries.forEach {
            if (dest.localDateTimeProperties.containsKey(it.key)) {
                dest.localDateTimeProperties[it.key]?.set(it.value.get())
            }
        }
        source.zonedDateTimeProperties.entries.forEach {
            if (dest.zonedDateTimeProperties.containsKey(it.key)) {
                dest.zonedDateTimeProperties[it.key]?.set(it.value.get())
            }
        }
        source.localTimeProperties.entries.forEach {
            if (dest.localTimeProperties.containsKey(it.key)) {
                dest.localTimeProperties[it.key]?.set(it.value.get())
            }
        }
        source.localDateProperties.entries.forEach {
            if (dest.localDateProperties.containsKey(it.key)) {
                dest.localDateProperties[it.key]?.set(it.value.get())
            }
        }
        source.stringProperties.entries.forEach {
            if (dest.stringProperties.containsKey(it.key)) {
                dest.stringProperties[it.key]?.set(it.value.get())
            }
        }
        source.floatProperties.entries.forEach {
            if (dest.floatProperties.containsKey(it.key)) {
                dest.floatProperties[it.key]?.set(it.value.get())
            }
        }
        source.doubleProperties.entries.forEach {
            if (dest.doubleProperties.containsKey(it.key)) {
                dest.doubleProperties[it.key]?.set(it.value.get())
            }
        }
        source.longProperties.entries.forEach {
            if (dest.longProperties.containsKey(it.key)) {
                dest.longProperties[it.key]?.set(it.value.get())
            }
        }
        source.integerProperties.entries.forEach {
            if (dest.integerProperties.containsKey(it.key)) {
                dest.integerProperties[it.key]?.set(it.value.get())
            }
        }
        source.blobProperties.entries.forEach {
            if (dest.blobProperties.containsKey(it.key)) {
                dest.blobProperties[it.key]?.set(it.value.get()!!)
            }
        }
        source.durationProperties.entries.forEach {
            if (dest.durationProperties.containsKey(it.key)) {
                dest.durationProperties[it.key]?.set(it.value.get())
            }
        }
        source.periodProperties.entries.forEach {
            if (dest.periodProperties.containsKey(it.key)) {
                dest.periodProperties[it.key]?.set(it.value.get())
            }
        }
        source.yearMonthProperties.entries.forEach {
            if (dest.yearMonthProperties.containsKey(it.key)) {
                dest.yearMonthProperties[it.key]?.set(it.value.get())
            }
        }
        source.monthDayProperties.entries.forEach {
            if (dest.monthDayProperties.containsKey(it.key)) {
                dest.monthDayProperties[it.key]?.set(it.value.get()!!)
            }
        }
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
        objectOutputStream.writeObject(serializeFloat(floatProperties))
        objectOutputStream.writeObject(serializeDouble(doubleProperties))
        objectOutputStream.writeObject(serializeBoolean(booleanProperties))
        objectOutputStream.writeObject(serializeLong(longProperties))
        objectOutputStream.writeObject(serializeInteger(integerProperties))
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
        putFloat(floatProperties, objectInputStream.readObject() as Map<Long, Float>)
        putDouble(doubleProperties, objectInputStream.readObject() as Map<Long, Double>)
        putBoolean(booleanProperties, objectInputStream.readObject() as Map<Long, Boolean>)
        putLong(longProperties, objectInputStream.readObject() as Map<Long, Long>)
        putInteger(integerProperties, objectInputStream.readObject() as Map<Long, Int>)
        //blobs
        putBlobs(blobProperties, objectInputStream.readObject() as Map<Long, BlobProperty>)
        //arrays
        putObjects(objectArrayProperties, objectInputStream.readObject() as Map<JdsFieldEntity<*>, List<JdsEntity>>)
        putStrings(stringArrayProperties, objectInputStream.readObject() as Map<Long, List<String?>>)
        putDateTimes(dateTimeArrayProperties, objectInputStream.readObject() as Map<Long, List<LocalDateTime>>)
        putFloats(floatArrayProperties, objectInputStream.readObject() as Map<Long, List<Float>>)
        putDoubles(doubleArrayProperties, objectInputStream.readObject() as Map<Long, List<Double>>)
        putLongs(longArrayProperties, objectInputStream.readObject() as Map<Long, List<Long>>)
        putIntegers(integerArrayProperties, objectInputStream.readObject() as Map<Long, List<Int>>)
        //enumProperties
        putEnum(enumProperties, objectInputStream.readObject() as Map<JdsFieldEnum<*>, Enum<*>>)
        putEnums(enumCollectionProperties, objectInputStream.readObject() as Map<JdsFieldEnum<*>, List<Enum<*>>>)
    }

    private fun serializeEnums(input: Map<JdsFieldEnum<*>, ObjectProperty<Enum<*>>>): Map<JdsFieldEnum<*>, Enum<*>> =
            input.entries.associateBy({ it.key }, { it.value.get() })

    private fun serializeBlobs(input: Map<Long, BlobProperty>): Map<Long, BlobProperty> =
            input.entries.associateBy({ it.key }, { it.value })

    /**
     * Create a map that can be serialized
     * @param input an unserializable map
     * @return A serialisable map
     */
    private fun serializeEnumCollections(input: Map<JdsFieldEnum<*>, Collection<Enum<*>>>): Map<JdsFieldEnum<*>, List<Enum<*>>> =
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
    private fun serializeFloat(input: Map<Long, FloatProperty>): Map<Long, Float> =
            input.entries.associateBy({ it.key }, { it.value.get() })

    /**
     * Create a map that can be serialized
     * @param input an unserializable map
     * @return A serialisable map
     */
    private fun serializeDouble(input: Map<Long, DoubleProperty>): Map<Long, Double> =
            input.entries.associateBy({ it.key }, { it.value.get() })

    /**
     * Create a map that can be serialized
     * @param input an unserializable map
     * @return A serialisable map
     */
    private fun serializeBoolean(input: Map<Long, BooleanProperty>): Map<Long, Boolean> =
            input.entries.associateBy({ it.key }, { it.value.get() })

    /**
     * Create a map that can be serialized
     * @param input an unserializable map
     * @return A serialisable map
     */
    private fun serializeLong(input: Map<Long, LongProperty>): Map<Long, Long> {
        return input.entries.associateBy({ it.key }, { it.value.get() })
    }

    /**
     * Create a map that can be serialized
     * @param input an unserializable map
     * @return A serialisable map
     */
    private fun serializeInteger(input: Map<Long, IntegerProperty>): Map<Long, Int> =
            input.entries.associateBy({ it.key }, { it.value.get() })

    /**
     * Create a map that can be serialized
     * @param input an unserializable map
     * @return A serialisable map
     */
    private fun serializeTemporal(input: Map<Long, ObjectProperty<out Temporal>>): Map<Long, Temporal> =
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

    private fun putYearMonths(destination: HashMap<Long, ObjectProperty<Temporal>>, source: Map<Long, Temporal>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putMonthDays(destination: HashMap<Long, ObjectProperty<MonthDay>>, source: Map<Long, MonthDay>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putEnums(destination: Map<JdsFieldEnum<*>, MutableCollection<Enum<*>>>, source: Map<JdsFieldEnum<*>, List<Enum<*>>>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.addAll(entry.value) }
    }

    private fun putEnum(destination: Map<JdsFieldEnum<*>, ObjectProperty<Enum<*>>>, source: Map<JdsFieldEnum<*>, Enum<*>>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putObjects(destination: Map<JdsFieldEntity<*>, MutableCollection<JdsEntity>>, source: Map<JdsFieldEntity<*>, List<JdsEntity>>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.addAll(entry.value) }
    }

    private fun putStrings(destination: Map<Long, MutableCollection<String?>>, source: Map<Long, List<String?>>) {
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

    private fun putInteger(destination: Map<Long, IntegerProperty>, source: Map<Long, Int>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putBlobs(destination: Map<Long, BlobProperty>, source: Map<Long, BlobProperty>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.set(entry.value.get()!!) }
    }

    private fun putLong(destination: Map<Long, LongProperty>, source: Map<Long, Long>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putBoolean(destination: Map<Long, BooleanProperty>, source: Map<Long, Boolean>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putDouble(destination: Map<Long, DoubleProperty>, source: Map<Long, Double>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putObject(destination: Map<JdsFieldEntity<*>, ObjectProperty<JdsEntity>>, source: Map<JdsFieldEntity<*>, JdsEntity>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putFloat(destination: Map<Long, FloatProperty>, source: Map<Long, Float>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putTemporal(destination: Map<Long, ObjectProperty<Temporal>>, source: Map<Long, Temporal>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putString(destination: Map<Long, StringProperty>, source: Map<Long, String>) {
        source.entries.filter { entry -> destination.containsKey(entry.key) }.forEach { entry -> destination[entry.key]?.set(entry.value) }
    }

    /**
     * @param step
     * @param saveContainer
     */
    internal fun assign(step: Int, saveContainer: JdsSaveContainer) {
        //==============================================
        //PRIMITIVES
        //==============================================
        saveContainer.booleanProperties[step].put(overview.uuid, booleanProperties)
        saveContainer.stringProperties[step].put(overview.uuid, stringProperties)
        saveContainer.floatProperties[step].put(overview.uuid, floatProperties)
        saveContainer.doubleProperties[step].put(overview.uuid, doubleProperties)
        saveContainer.longProperties[step].put(overview.uuid, longProperties)
        saveContainer.integerProperties[step].put(overview.uuid, integerProperties)
        //==============================================
        //Dates & Time
        //==============================================
        saveContainer.localDateTimeProperties[step].put(overview.uuid, localDateTimeProperties)
        saveContainer.zonedDateTimeProperties[step].put(overview.uuid, zonedDateTimeProperties)
        saveContainer.localTimeProperties[step].put(overview.uuid, localTimeProperties)
        saveContainer.localDateProperties[step].put(overview.uuid, localDateProperties)
        saveContainer.monthDayProperties[step].put(overview.uuid, monthDayProperties)
        saveContainer.yearMonthProperties[step].put(overview.uuid, yearMonthProperties)
        saveContainer.periodProperties[step].put(overview.uuid, periodProperties)
        saveContainer.durationProperties[step].put(overview.uuid, durationProperties)
        //==============================================
        //BLOB
        //==============================================
        saveContainer.blobProperties[step].put(overview.uuid, blobProperties)
        //==============================================
        //Enums
        //==============================================
        saveContainer.enumProperties[step].put(overview.uuid, enumProperties)
        saveContainer.enumCollections[step].put(overview.uuid, enumCollectionProperties)
        //==============================================
        //ARRAYS
        //==============================================
        saveContainer.stringCollections[step].put(overview.uuid, stringArrayProperties)
        saveContainer.localDateTimeCollections[step].put(overview.uuid, dateTimeArrayProperties)
        saveContainer.floatCollections[step].put(overview.uuid, floatArrayProperties)
        saveContainer.doubleCollections[step].put(overview.uuid, doubleArrayProperties)
        saveContainer.longCollections[step].put(overview.uuid, longArrayProperties)
        saveContainer.integerCollections[step].put(overview.uuid, integerArrayProperties)
        //==============================================
        //EMBEDDED OBJECTS
        //==============================================
        saveContainer.objectCollections[step].put(overview.uuid, objectArrayProperties)
        saveContainer.objects[step].put(overview.uuid, objectProperties)
    }

    /**
     * @param embeddedObject
     */
    internal fun assign(embeddedObject: JdsEmbeddedObject) {
        //==============================================
        //PRIMITIVES, also saved to array struct to streamline json
        //==============================================
        booleanProperties.entries.forEach {
            embeddedObject.b.add(JdsBooleanValues(it.key, when (it.value.value) {
                true -> 1
                false -> 0
            }))
        }
        stringProperties.entries.forEach { embeddedObject.s.add(JdsStringValues(it.key, it.value.value)) }
        floatProperties.entries.forEach { embeddedObject.f.add(JdsFloatValues(it.key, it.value.value)) }
        doubleProperties.entries.forEach { embeddedObject.d.add(JdsDoubleValues(it.key, it.value.value)) }
        longProperties.entries.forEach { embeddedObject.l.add(JdsLongValues(it.key, it.value.value)) }
        integerProperties.entries.forEach { embeddedObject.i.add(JdsIntegerEnumValues(it.key, it.value.value)) }
        //==============================================
        //Dates & Time
        //==============================================
        zonedDateTimeProperties.entries.forEach { embeddedObject.l.add(JdsLongValues(it.key, (it.value.value as ZonedDateTime).toInstant().toEpochMilli())) }
        localTimeProperties.entries.forEach { embeddedObject.l.add(JdsLongValues(it.key, (it.value.value as LocalTime).toNanoOfDay())) }
        durationProperties.entries.forEach { embeddedObject.l.add(JdsLongValues(it.key, it.value.value.toNanos())) }
        localDateTimeProperties.entries.forEach { embeddedObject.ldt.add(JdsLocalDateTimeValues(it.key, Timestamp.valueOf(it.value.value as LocalDateTime))) }
        localDateProperties.entries.forEach { embeddedObject.ld.add(JdsLocalDateValues(it.key, Timestamp.valueOf((it.value.value as LocalDate).atStartOfDay()))) }
        monthDayProperties.entries.forEach { embeddedObject.s.add(JdsStringValues(it.key, it.value.value?.toString())) }
        yearMonthProperties.entries.forEach { embeddedObject.s.add(JdsStringValues(it.key, (it.value.value as YearMonth?)?.toString())) }
        periodProperties.entries.forEach { embeddedObject.s.add(JdsStringValues(it.key, it.value.value?.toString())) }
        //==============================================
        //BLOB
        //==============================================
        blobProperties.entries.forEach { embeddedObject.bl.add(JdsBlobValues(it.key, it.value.get() ?: ByteArray(0))) }
        //==============================================
        //Enums
        //==============================================
        enumProperties.entries.forEach { embeddedObject.i.add(JdsIntegerEnumValues(it.key.field.id, it.value.value.ordinal)) }
        enumCollectionProperties.entries.forEach { it.value.forEach { child -> embeddedObject.i.add(JdsIntegerEnumValues(it.key.field.id, child.ordinal)) } }
        //==============================================
        //ARRAYS
        //==============================================
        stringArrayProperties.entries.forEach { it.value.forEach { child -> embeddedObject.s.add(JdsStringValues(it.key, child)) } }
        dateTimeArrayProperties.entries.forEach { it.value.forEach { child -> embeddedObject.ldt.add(JdsLocalDateTimeValues(it.key, Timestamp.valueOf(child))) } }
        floatArrayProperties.entries.forEach { it.value.forEach { child -> embeddedObject.f.add(JdsFloatValues(it.key, child)) } }
        doubleArrayProperties.entries.forEach { it.value.forEach { child -> embeddedObject.d.add(JdsDoubleValues(it.key, child)) } }
        longArrayProperties.entries.forEach { it.value.forEach { child -> embeddedObject.l.add(JdsLongValues(it.key, child)) } }
        integerArrayProperties.entries.forEach { it.value.forEach { child -> embeddedObject.i.add(JdsIntegerEnumValues(it.key, child)) } }
        //==============================================
        //EMBEDDED OBJECTS
        //==============================================
        objectArrayProperties.entries.forEach { itx ->
            itx.value.forEach {
                embeddedObject.eb.add(JdsEntityBinding(overview.uuid, it.overview.uuid, itx.key.fieldEntity.id, it.overview.entityId))
                embeddedObject.eo.add(JdsEmbeddedObject(it))
            }
        }
        objectProperties.entries.forEach {
            embeddedObject.eb.add(JdsEntityBinding(overview.uuid, it.value.value.overview.uuid, it.key.fieldEntity.id, it.value.value.overview.entityId))
            embeddedObject.eo.add(JdsEmbeddedObject(it.value.value))
        }
    }

    /**
     * @param fieldType
     * @param fieldId
     * @param value
     */
    internal fun populateProperties(fieldType: JdsFieldType, fieldId: Long, value: Any?) {
        when (fieldType) {
            JdsFieldType.FLOAT -> floatProperties[fieldId]?.set(value as Float)
            JdsFieldType.DOUBLE -> doubleProperties[fieldId]?.set(value as Double)
            JdsFieldType.LONG -> longProperties[fieldId]?.set(value as Long)
            JdsFieldType.DOUBLE_COLLECTION -> doubleArrayProperties[fieldId]?.add(value as Double)
            JdsFieldType.FLOAT_COLLECTION -> floatArrayProperties[fieldId]?.add(value as Float)
            JdsFieldType.LONG_COLLECTION -> longArrayProperties[fieldId]?.add(value as Long)
            JdsFieldType.BOOLEAN -> when (value) {
                is Int -> booleanProperties[fieldId]?.set(value == 1)
                is Boolean -> booleanProperties[fieldId]?.set(value)
            }
            JdsFieldType.ZONED_DATE_TIME -> when (value) {
                is Long -> zonedDateTimeProperties[fieldId]?.set(ZonedDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault()))
                is Timestamp -> zonedDateTimeProperties[fieldId]?.set(value.let { ZonedDateTime.ofInstant(it.toInstant(), ZoneOffset.systemDefault())})
                is String -> zonedDateTimeProperties[fieldId]?.set(value.let { it.toZonedDateTime()})
                is OffsetDateTime -> zonedDateTimeProperties[fieldId]?.set(value.let { it.atZoneSameInstant(ZoneId.systemDefault())})
            }
            JdsFieldType.DATE -> localDateProperties[fieldId]?.set((value as Timestamp).toLocalDateTime().toLocalDate())
            JdsFieldType.TIME -> when (value) {
                is Long -> localTimeProperties[fieldId]?.set(LocalTime.MIN.plusNanos(value))
                is LocalTime -> localTimeProperties[fieldId]?.set(value)
                is String -> localTimeProperties[fieldId]?.set(value.toLocalTimeSqlFormat())
            }
            JdsFieldType.BLOB -> blobProperties[fieldId]?.set(value as ByteArray)
            JdsFieldType.DURATION -> durationProperties[fieldId]?.set(Duration.ofNanos(value as Long))
        //====================  from strings ====================
            JdsFieldType.STRING -> stringProperties[fieldId]?.set(value as String?)
            JdsFieldType.STRING_COLLECTION -> stringArrayProperties[fieldId]?.add(value as String?)
            JdsFieldType.MONTH_DAY -> monthDayProperties[fieldId]?.value = (value as String).let { MonthDay.parse(it) }
            JdsFieldType.YEAR_MONTH -> yearMonthProperties[fieldId]?.value = (value as String).let { YearMonth.parse(it) }
            JdsFieldType.PERIOD -> periodProperties[fieldId]?.value = (value as String).let { Period.parse(it) }
        //==================== from ints ====================
            JdsFieldType.INT -> integerProperties[fieldId]?.set(value as Int)
            JdsFieldType.INT_COLLECTION -> integerArrayProperties[fieldId]?.add(value as Int)
            JdsFieldType.ENUM -> enumProperties.filter { it.key.field.id == fieldId }.forEach { it.value?.set(it.key.valueOf(value as Int)) }
            JdsFieldType.ENUM_COLLECTION -> enumCollectionProperties.filter { it.key.field.id == fieldId }.forEach {
                val enumValues = it.key.enumType.enumConstants
                val index = value as Int
                if (index < enumValues.size) {
                    it.value.add(enumValues[index] as Enum<*>)
                }
            }
        //====================  from timestamps ====================
            JdsFieldType.DATE_TIME -> localDateTimeProperties[fieldId]?.value = (value as Timestamp).let { it.toLocalDateTime() }
            JdsFieldType.DATE_TIME_COLLECTION -> dateTimeArrayProperties[fieldId]?.add((value as Timestamp).let { it.toLocalDateTime() })
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
    internal fun populateObjects(jdsDb: JdsDb, fieldId: Long, entityId: Long, uuid: String, innerObjects: ConcurrentLinkedQueue<JdsEntity>, uuids: HashSet<String>) = try {
        val entityClass = jdsDb.classes[entityId]!!
        objectArrayProperties.filter { it.key.fieldEntity.id == fieldId }.forEach {
            val entity = entityClass.newInstance()
            entity.overview.uuid = uuid
            uuids.add(uuid)
            it.value.add(entity)
            innerObjects.add(entity)
        }
        objectProperties.filter { it.key.fieldEntity.id == fieldId }.forEach {
            val jdsEntity = entityClass.newInstance()
            jdsEntity.overview.uuid = uuid
            uuids.add(uuid)
            it.value.set(jdsEntity)
            innerObjects.add(jdsEntity)
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }


    /**
     * Binds all the fieldIds attached to an entity, updates the fieldIds dictionary
     * @param connection the SQL connection to use for DB operations
     * @param entityId   the value representing the entity
     */
    internal fun mapClassFields(jdsDb: JdsDb, connection: Connection, entityId: Long) = try {
        (if (jdsDb.supportsStatements) NamedCallableStatement(connection, jdsDb.mapClassFields()) else NamedPreparedStatement(connection, jdsDb.mapClassFields())).use { mapClassFields ->
            (if (jdsDb.supportsStatements) connection.prepareCall(jdsDb.mapFieldName()) else connection.prepareStatement(jdsDb.mapFieldName())).use { mapFieldName ->
                getFields(overview.entityId).forEach {
                    val lookup = JdsField.values[it]!!
                    //1. map this fieldEntity to the fieldEntity dictionary
                    mapFieldName.setLong(1, lookup.id)
                    mapFieldName.setString(2, lookup.name)
                    mapFieldName.setString(3, lookup.description)
                    mapFieldName.setInt(4, lookup.type.ordinal)
                    mapFieldName.addBatch()
                    //2. map this fieldEntity ID to the entity type
                    mapClassFields.setLong("entityId", entityId)
                    mapClassFields.setLong("fieldId", lookup.id)
                    mapClassFields.addBatch()
                }
                mapFieldName.executeBatch()
                mapClassFields.executeBatch()
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
    internal fun mapClassEnums(jdsDb: JdsDb, connection: Connection, entityId: Long) {
        mapEnumValues(jdsDb, connection, getEnums(overview.entityId))
        mapClassEnumsImplementation(jdsDb, connection, entityId, getEnums(overview.entityId))
        if (jdsDb.isPrintingOutput)
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
    private fun mapClassEnumsImplementation(jdsDb: JdsDb, connection: Connection, entityId: Long, fieldIds: Set<Long>) = try {
        (if (jdsDb.supportsStatements) connection.prepareCall(jdsDb.mapClassEnumsImplementation()) else connection.prepareStatement(jdsDb.mapClassEnumsImplementation())).use {
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
    private fun mapEnumValues(jdsDb: JdsDb, connection: Connection, fieldIds: Set<Long>) = try {
        (if (jdsDb.supportsStatements) connection.prepareCall(jdsDb.mapEnumValues()) else connection.prepareStatement(jdsDb.mapEnumValues())).use {
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
     * @param id
     * @param ordinal
     * @return
     */
    fun getReportAtomicValue(id: Long, ordinal: Int): Any? {
        //time constructs
        if (localDateTimeProperties.containsKey(id))
            return Timestamp.valueOf(localDateTimeProperties[id]!!.value as LocalDateTime)
        if (zonedDateTimeProperties.containsKey(id))
            return (zonedDateTimeProperties[id]!!.value as ZonedDateTime)
        if (localDateProperties.containsKey(id))
            return Timestamp.valueOf((localDateProperties[id]!!.value as LocalDate).atStartOfDay())
        if (localTimeProperties.containsKey(id))
            return (localTimeProperties[id]!!.value as LocalTime)
        if (monthDayProperties.containsKey(id))
            return monthDayProperties[id]!!.value.toString()
        if (yearMonthProperties.containsKey(id))
            return yearMonthProperties[id]!!.value.toString()
        if (periodProperties.containsKey(id))
            return periodProperties[id]!!.value.toString()
        if (durationProperties.containsKey(id))
            return durationProperties[id]!!.value.toNanos()
        //string
        if (stringProperties.containsKey(id))
            return stringProperties[id]!!.value
        //primitives
        if (floatProperties.containsKey(id))
            return floatProperties[id]!!.value
        if (doubleProperties.containsKey(id))
            return doubleProperties[id]!!.value
        if (booleanProperties.containsKey(id))
            return booleanProperties[id]!!.value
        if (longProperties.containsKey(id))
            return longProperties[id]!!.value
        if (integerProperties.containsKey(id))
            return integerProperties[id]!!.value
        enumProperties.filter { it.key.field.id == id && it.value.value.ordinal == ordinal }.forEach {
            return 1
        }
        enumCollectionProperties.filter { it.key.field.id == id }.forEach { it.value.filter { it.ordinal == ordinal }.forEach { return true } }
        //single object references
        objectProperties.filter { it.key.fieldEntity.id == id }.forEach {
            return it.value.value.overview.uuid
        }
        return null
    }

    /**
     * @param table
     */
    override fun registerFields(table: JdsTable) {
        getFields(overview.entityId).forEach { table.registerField(it) }
    }

    /**
     * Ensures child entities have ids that link them to their parent.
     * For frequent refreshes/imports from different sources this is necessary to prevent duplicate entries of the same data
     */
    fun standardizeUUIDs() {
        val parentUUID = overview.uuid
        standardizeObjectUUIDs(parentUUID, objectProperties)
        standardizeObjectCollectionUUIDs(parentUUID, objectArrayProperties)
    }

    private fun standardizeObjectCollectionUUIDs(parentUUID: String, objectArrayProperties: HashMap<JdsFieldEntity<*>, MutableCollection<JdsEntity>>) {
        objectArrayProperties.entries.forEach {
            //parent-uuid.entity_id.sequence e.g ab9d2da6-fb64-47a9-9a3c-a6e0a998703f.256.3
            it.value.forEachIndexed { sequence, entry ->
                val entityId = entry.overview.entityId
                entry.overview.uuid = "$parentUUID.$entityId.$sequence"
                //process children
                standardizeObjectUUIDs(entry.overview.uuid, entry.objectProperties)
                standardizeObjectCollectionUUIDs(entry.overview.uuid, entry.objectArrayProperties)
            }
        }
    }

    private fun standardizeObjectUUIDs(parentUUID: String, objectProperties: HashMap<JdsFieldEntity<*>, ObjectProperty<JdsEntity>>) {
        //parent-uuid.entity_id.sequence e.g ab9d2da6-fb64-47a9-9a3c-a6e0a998703f.256
        objectProperties.entries.forEach { entry ->
            val entityId = entry.value.value.overview.entityId
            entry.value.value.overview.uuid = "$parentUUID.$entityId"
            //process children
            standardizeObjectUUIDs(entry.value.value.overview.uuid, entry.value.value.objectProperties)
            standardizeObjectCollectionUUIDs(entry.value.value.overview.uuid, entry.value.value.objectArrayProperties)
        }
    }

    internal fun yieldOverviews(): Sequence<IJdsOverview> = buildSequence {
        yield(overview)
        objectProperties.values.forEach { yieldAll(it.value.yieldOverviews()) }
        objectArrayProperties.values.forEach { it.forEach { yieldAll(it.yieldOverviews()) } }
    }

    companion object : Externalizable {

        private val serialVersionUID = 20180106_2125L
        private val allFields: ConcurrentHashMap<Long, MutableSet<Long>> = ConcurrentHashMap()
        private val allEnums: ConcurrentHashMap<Long, MutableSet<Long>> = ConcurrentHashMap()

        override fun readExternal(objectInput: ObjectInput) {
            allFields.clear()
            allFields.putAll(objectInput.readObject() as Map<out Long, MutableSet<Long>>)
            allEnums.clear()
            allEnums.putAll(objectInput.readObject() as Map<out Long, MutableSet<Long>>)
        }

        override fun writeExternal(objectOutput: ObjectOutput) {
            objectOutput.writeObject(allFields)
            objectOutput.writeObject(allEnums)
        }

        internal fun mapField(entityId: Long, fieldId: Long) {
            getFields(entityId).add(fieldId)
        }

        internal fun mapEnums(entityId: Long, fieldId: Long) {
            getEnums(entityId).add(fieldId)
        }

        internal fun getFields(entityId: Long) = allFields.getOrPut(entityId) { HashSet() }

        internal fun getEnums(entityId: Long) = allEnums.getOrPut(entityId) { HashSet() }
    }
}
