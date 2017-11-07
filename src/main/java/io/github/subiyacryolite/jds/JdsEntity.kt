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
import java.io.IOException
import java.io.ObjectInput
import java.io.ObjectOutput
import java.sql.Connection
import java.sql.Timestamp
import java.time.*
import java.time.temporal.Temporal
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * This class allows for all mapping operations in JDS, it also uses
 * [JdsEntityBase] to store overview data
 */
abstract class JdsEntity : IJdsEntity {
    override var overview: IJdsOverview = JdsOverview()
    //fieldEntity and enum maps
    private val fields: MutableSet<JdsField> = HashSet()
    private val enums: MutableSet<JdsFieldEnum<*>> = HashSet()
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
    private val objectArrayProperties: HashMap<JdsFieldEntity<*>, ListProperty<JdsEntity>> = HashMap()
    private val stringArrayProperties: HashMap<Long, ListProperty<String>> = HashMap()
    private val dateTimeArrayProperties: HashMap<Long, ListProperty<LocalDateTime>> = HashMap()
    private val floatArrayProperties: HashMap<Long, ListProperty<Float>> = HashMap()
    private val doubleArrayProperties: HashMap<Long, ListProperty<Double>> = HashMap()
    private val longArrayProperties: HashMap<Long, ListProperty<Long>> = HashMap()
    private val integerArrayProperties: HashMap<Long, ListProperty<Int>> = HashMap()
    //enumProperties
    private val enumProperties: HashMap<JdsFieldEnum<*>, ObjectProperty<Enum<*>>> = HashMap()
    private val enumCollectionProperties: HashMap<JdsFieldEnum<*>, ListProperty<Enum<*>>> = HashMap()
    //objects
    private val objectProperties: HashMap<JdsFieldEntity<*>, ObjectProperty<JdsEntity>> = HashMap()
    //blobs
    private val blobProperties: HashMap<Long, BlobProperty> = HashMap()


    init {
        if (javaClass.isAnnotationPresent(JdsEntityAnnotation::class.java)) {
            val entityAnnotation = javaClass.getAnnotation(JdsEntityAnnotation::class.java)
            overview.entityId = entityAnnotation.entityId
            overview.version = entityAnnotation.version
        } else {
            throw RuntimeException("You must annotate the class [" + javaClass.canonicalName + "] with [" + JdsEntityAnnotation::class.java + "]")
        }
    }

    /**
     * @param jdsField
     * @param integerProperty
     */
    protected fun map(jdsField: JdsField, integerProperty: BlobProperty) {
        if (jdsField.type != JdsFieldType.BLOB)
            throw RuntimeException("Please set jdsField [$jdsField] to the correct type")
        fields.add(jdsField)
        blobProperties.put(jdsField.id, integerProperty)
    }

    /**
     * @param jdsField
     * @param integerProperty
     */
    protected fun map(jdsField: JdsField, integerProperty: IntegerProperty) {
        if (jdsField.type != JdsFieldType.INT)
            throw RuntimeException("Please set jdsField [$jdsField] to the correct type")
        fields.add(jdsField)
        integerProperties.put(jdsField.id, integerProperty)
    }

    protected fun mapMonthDay(jdsField: JdsField, property: ObjectProperty<MonthDay>) {
        if (jdsField.type != JdsFieldType.MONTH_DAY)
            throw RuntimeException("Please set jdsField [$jdsField] to the correct type")
        fields.add(jdsField)
        monthDayProperties.put(jdsField.id, property)
    }

    protected fun mapPeriod(jdsField: JdsField, property: ObjectProperty<Period>) {
        if (jdsField.type != JdsFieldType.PERIOD)
            throw RuntimeException("Please set jdsField [$jdsField] to the correct type")
        fields.add(jdsField)
        periodProperties.put(jdsField.id, property)
    }

    protected fun mapDuration(jdsField: JdsField, property: ObjectProperty<Duration>) {
        if (jdsField.type != JdsFieldType.DURATION)
            throw RuntimeException("Please set jdsField [$jdsField] to the correct type")
        fields.add(jdsField)
        durationProperties.put(jdsField.id, property)
    }

    /**
     * @param jdsField
     * @param temporalProperty
     */
    protected fun map(jdsField: JdsField, temporalProperty: ObjectProperty<out Temporal>) {
        val temporal = temporalProperty.get()
        when (temporal) {
            is LocalDateTime -> {
                if (jdsField.type != JdsFieldType.DATE_TIME)
                    throw RuntimeException("Please set jdsField [$jdsField] to the correct type")
                fields.add(jdsField)
                localDateTimeProperties.put(jdsField.id, temporalProperty as ObjectProperty<Temporal>)
            }
            is ZonedDateTime -> {
                if (jdsField.type != JdsFieldType.ZONED_DATE_TIME)
                    throw RuntimeException("Please set jdsField [$jdsField] to the correct type")
                fields.add(jdsField)
                zonedDateTimeProperties.put(jdsField.id, temporalProperty as ObjectProperty<Temporal>)
            }
            is LocalDate -> {
                if (jdsField.type != JdsFieldType.DATE)
                    throw RuntimeException("Please set jdsField [$jdsField] to the correct type")
                fields.add(jdsField)
                localDateProperties.put(jdsField.id, temporalProperty as ObjectProperty<Temporal>)
            }
            is LocalTime -> {
                if (jdsField.type != JdsFieldType.TIME)
                    throw RuntimeException("Please set jdsField [$jdsField] to the correct type")
                fields.add(jdsField)
                localTimeProperties.put(jdsField.id, temporalProperty as ObjectProperty<Temporal>)
            }
            is YearMonth -> {
                if (jdsField.type != JdsFieldType.YEAR_MONTH)
                    throw RuntimeException("Please set jdsField [$jdsField] to the correct type")
                fields.add(jdsField)
                yearMonthProperties.put(jdsField.id, temporalProperty as ObjectProperty<Temporal>)

            }
        }
    }

    /**
     * @param jdsField
     * @param property
     */
    protected fun map(jdsField: JdsField, property: StringProperty) {
        if (jdsField.type != JdsFieldType.STRING)
            throw RuntimeException("Please set jdsField [$jdsField] to the correct type")
        fields.add(jdsField)
        stringProperties.put(jdsField.id, property)

    }

    /**
     * @param jdsField
     * @param property
     */
    protected fun map(jdsField: JdsField, property: FloatProperty) {
        if (jdsField.type != JdsFieldType.FLOAT)
            throw RuntimeException("Please set jdsField [$jdsField] to the correct type")
        fields.add(jdsField)
        floatProperties.put(jdsField.id, property)
    }

    /**
     * @param jdsField
     * @param property
     */
    protected fun map(jdsField: JdsField, property: LongProperty) {
        if (jdsField.type != JdsFieldType.LONG)
            throw RuntimeException("Please set jdsField [$jdsField] to the correct type")
        fields.add(jdsField)
        longProperties.put(jdsField.id, property)
    }

    /**
     * @param jdsField
     * @param property
     */
    protected fun map(jdsField: JdsField, property: DoubleProperty) {
        if (jdsField.type != JdsFieldType.DOUBLE)
            throw RuntimeException("Please set jdsField [$jdsField] to the correct type")
        fields.add(jdsField)
        doubleProperties.put(jdsField.id, property)

    }

    /**
     * @param jdsField
     * @param property
     */
    protected fun map(jdsField: JdsField, property: BooleanProperty) {
        if (jdsField.type != JdsFieldType.BOOLEAN)
            throw RuntimeException("Please set jdsField [$jdsField] to the correct type")
        fields.add(jdsField)
        booleanProperties.put(jdsField.id, property)
    }

    /**
     * @param jdsField
     * @param properties
     */
    protected fun mapStrings(jdsField: JdsField, properties: ListProperty<String>) {
        if (jdsField.type != JdsFieldType.STRING_COLLECTION)
            throw RuntimeException("Please set jdsField [$jdsField] to the correct type")
        fields.add(jdsField)
        stringArrayProperties.put(jdsField.id, properties)
    }

    /**
     * @param jdsField
     * @param properties
     */
    protected fun mapDateTimes(jdsField: JdsField, properties: ListProperty<LocalDateTime>) {
        if (jdsField.type != JdsFieldType.DATE_TIME_COLLECTION)
            throw RuntimeException("Please set jdsField [$jdsField] to the correct type")
        fields.add(jdsField)
        dateTimeArrayProperties.put(jdsField.id, properties)
    }

    /**
     * @param jdsField
     * @param properties
     */
    protected fun mapFloats(jdsField: JdsField, properties: ListProperty<Float>) {
        if (jdsField.type != JdsFieldType.FLOAT_COLLECTION)
            throw RuntimeException("Please set jdsField [$jdsField] to the correct type")
        fields.add(jdsField)
        floatArrayProperties.put(jdsField.id, properties)
    }

    /**
     * @param jdsField
     * @param properties
     */
    protected fun mapIntegers(jdsField: JdsField, properties: ListProperty<Int>) {
        if (jdsField.type != JdsFieldType.INT_COLLECTION)
            throw RuntimeException("Please set jdsField [$jdsField] to the correct type")
        fields.add(jdsField)
        integerArrayProperties.put(jdsField.id, properties)
    }

    /**
     * @param jdsField
     * @param properties
     */
    protected fun mapDoubles(jdsField: JdsField, properties: ListProperty<Double>) {
        if (jdsField.type != JdsFieldType.DOUBLE_COLLECTION)
            throw RuntimeException("Please set jdsField [$jdsField] to the correct type")
        fields.add(jdsField)
        doubleArrayProperties.put(jdsField.id, properties)
    }

    /**
     * @param jdsField
     * @param properties
     */
    protected fun mapLongs(jdsField: JdsField, properties: ListProperty<Long>) {
        if (jdsField.type != JdsFieldType.LONG_COLLECTION)
            throw RuntimeException("Please set jdsField [$jdsField] to the correct type")
        fields.add(jdsField)
        longArrayProperties.put(jdsField.id, properties)
    }

    /**
     * @param jdsFieldEnum
     * @param property
     */
    protected fun map(jdsFieldEnum: JdsFieldEnum<*>, property: ObjectProperty<out Enum<*>>) {
        if (jdsFieldEnum.field.type != JdsFieldType.ENUM)
            throw RuntimeException("Please set fieldEntity [$jdsFieldEnum] to the correct type")
        enums.add(jdsFieldEnum)
        fields.add(jdsFieldEnum.field)
        enumProperties.put(jdsFieldEnum, property as ObjectProperty<Enum<*>>)
    }

    /**
     * @param jdsFieldEnum
     * @param properties
     */
    protected fun mapEnums(jdsFieldEnum: JdsFieldEnum<*>, properties: ListProperty<out Enum<*>>) {
        if (jdsFieldEnum.field.type != JdsFieldType.ENUM_COLLECTION)
            throw RuntimeException("Please set fieldEntity [$jdsFieldEnum] to the correct type")
        enums.add(jdsFieldEnum)
        fields.add(jdsFieldEnum.field)
        enumCollectionProperties.put(jdsFieldEnum, properties as ListProperty<Enum<*>>)

    }

    /**
     * @param entity
     * @param property
     */
    protected fun <T : IJdsEntity> map(fieldEntity: JdsFieldEntity<T>, property: ObjectProperty<T>) {
        if (fieldEntity.fieldEntity.type != JdsFieldType.ENTITY)
            throw RuntimeException("Please supply a valid type for JdsFieldEntity")
        if (!objectArrayProperties.containsKey(fieldEntity) && !objectProperties.containsKey(fieldEntity)) {
            objectProperties.put(fieldEntity, property as ObjectProperty<JdsEntity>)
            fields.add(fieldEntity.fieldEntity)
        } else {
            throw RuntimeException("You can only bind a class to one property. This class is already bound to one object or object array")
        }
    }

    /**
     * @param fieldEntity
     * @param properties
     */
    protected fun <T : IJdsEntity> map(fieldEntity: JdsFieldEntity<T>, properties: ListProperty<T>) {
        if (fieldEntity.fieldEntity.type != JdsFieldType.ENTITY_COLLECTION)
            throw RuntimeException("Please supply a valid type for JdsFieldEntity")
        if (!objectArrayProperties.containsKey(fieldEntity)) {
            objectArrayProperties.put(fieldEntity, properties as ListProperty<JdsEntity>)
            fields.add(fieldEntity.fieldEntity)
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
        copyEnumValues(source)
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
        source.booleanProperties.entries.parallelStream().forEach { srcEntry ->
            if (dest.booleanProperties.containsKey(srcEntry.key)) {
                dest.booleanProperties[srcEntry.key]?.set(srcEntry.value.get())
            }
        }
        source.localDateTimeProperties.entries.parallelStream().forEach { srcEntry ->
            if (dest.localDateTimeProperties.containsKey(srcEntry.key)) {
                dest.localDateTimeProperties[srcEntry.key]?.set(srcEntry.value.get())
            }
        }
        source.zonedDateTimeProperties.entries.parallelStream().forEach { srcEntry ->
            if (dest.zonedDateTimeProperties.containsKey(srcEntry.key)) {
                dest.zonedDateTimeProperties[srcEntry.key]?.set(srcEntry.value.get())
            }
        }
        source.localTimeProperties.entries.parallelStream().forEach { srcEntry ->
            if (dest.localTimeProperties.containsKey(srcEntry.key)) {
                dest.localTimeProperties[srcEntry.key]?.set(srcEntry.value.get())
            }
        }
        source.localDateProperties.entries.parallelStream().forEach { srcEntry ->
            if (dest.localDateProperties.containsKey(srcEntry.key)) {
                dest.localDateProperties[srcEntry.key]?.set(srcEntry.value.get())
            }
        }
        source.stringProperties.entries.parallelStream().forEach { srcEntry ->
            if (dest.stringProperties.containsKey(srcEntry.key)) {
                dest.stringProperties[srcEntry.key]?.set(srcEntry.value.get())
            }
        }
        source.floatProperties.entries.parallelStream().forEach { srcEntry ->
            if (dest.floatProperties.containsKey(srcEntry.key)) {
                dest.floatProperties[srcEntry.key]?.set(srcEntry.value.get())
            }
        }
        source.doubleProperties.entries.parallelStream().forEach { srcEntry ->
            if (dest.doubleProperties.containsKey(srcEntry.key)) {
                dest.doubleProperties[srcEntry.key]?.set(srcEntry.value.get())
            }
        }
        source.longProperties.entries.parallelStream().forEach { srcEntry ->
            if (dest.longProperties.containsKey(srcEntry.key)) {
                dest.longProperties[srcEntry.key]?.set(srcEntry.value.get())
            }
        }
        source.integerProperties.entries.parallelStream().forEach { srcEntry ->
            if (dest.integerProperties.containsKey(srcEntry.key)) {
                dest.integerProperties[srcEntry.key]?.set(srcEntry.value.get())
            }
        }
        source.blobProperties.entries.parallelStream().forEach { srcEntry ->
            if (dest.blobProperties.containsKey(srcEntry.key)) {
                dest.blobProperties[srcEntry.key]?.set(srcEntry.value.get()!!)
            }
        }
        source.durationProperties.entries.parallelStream().forEach { srcEntry ->
            if (dest.durationProperties.containsKey(srcEntry.key)) {
                dest.durationProperties[srcEntry.key]?.set(srcEntry.value.get())
            }
        }
        source.periodProperties.entries.parallelStream().forEach { srcEntry ->
            if (dest.periodProperties.containsKey(srcEntry.key)) {
                dest.periodProperties[srcEntry.key]?.set(srcEntry.value.get())
            }
        }
        source.yearMonthProperties.entries.parallelStream().forEach { srcEntry ->
            if (dest.yearMonthProperties.containsKey(srcEntry.key)) {
                dest.yearMonthProperties[srcEntry.key]?.set(srcEntry.value.get())
            }
        }
        source.monthDayProperties.entries.parallelStream().forEach { srcEntry ->
            if (dest.monthDayProperties.containsKey(srcEntry.key)) {
                dest.monthDayProperties[srcEntry.key]?.set(srcEntry.value.get()!!)
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
        source.stringArrayProperties.entries.parallelStream().forEach { srcEntry ->
            if (dest.stringArrayProperties.containsKey(srcEntry.key)) {
                val entry = dest.stringArrayProperties[srcEntry.key]
                entry?.clear()
                entry?.set(srcEntry.value.get())
            }
        }
        source.dateTimeArrayProperties.entries.parallelStream().forEach { srcEntry ->
            if (dest.dateTimeArrayProperties.containsKey(srcEntry.key)) {
                val entry = dest.dateTimeArrayProperties[srcEntry.key]
                entry?.clear()
                entry?.set(srcEntry.value.get())
            }
        }
        source.floatArrayProperties.entries.parallelStream().forEach { srcEntry ->
            if (dest.floatArrayProperties.containsKey(srcEntry.key)) {
                val entry = dest.floatArrayProperties[srcEntry.key]
                entry?.clear()
                entry?.set(srcEntry.value.get())
            }
        }
        source.doubleArrayProperties.entries.parallelStream().forEach { srcEntry ->
            if (dest.doubleArrayProperties.containsKey(srcEntry.key)) {
                val entry = dest.doubleArrayProperties[srcEntry.key]
                entry?.clear()
                entry?.set(srcEntry.value.get())
            }
        }
        source.longArrayProperties.entries.parallelStream().forEach { srcEntry ->
            if (dest.longArrayProperties.containsKey(srcEntry.key)) {
                val entry = dest.longArrayProperties[srcEntry.key]
                entry?.clear()
                entry?.set(srcEntry.value.get())
            }
        }
        source.integerArrayProperties.entries.parallelStream().forEach { srcEntry ->
            if (dest.integerArrayProperties.containsKey(srcEntry.key)) {
                val entry = dest.integerArrayProperties[srcEntry.key]
                entry?.clear()
                entry?.set(srcEntry.value.get())
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
        source.objectProperties.entries.parallelStream().forEach { srcEntry ->
            if (dest.objectProperties.containsKey(srcEntry.key)) {
                dest.objectProperties[srcEntry.key]?.set(srcEntry.value.get())
            }
        }

        source.objectArrayProperties.entries.parallelStream().forEach { srcEntry ->
            if (dest.objectArrayProperties.containsKey(srcEntry.key)) {
                val entry = dest.objectArrayProperties[srcEntry.key]
                entry?.clear()
                entry?.set(srcEntry.value.get())
            }
        }
    }

    /**
     * Copy over object enum values
     *
     * @param source The entity to copy values from
     * @param <T>    A valid JDSEntity
    </T> */
    private fun <T : JdsEntity> copyEnumValues(source: T) {
        val dest = this
        source.enumCollectionProperties.entries.parallelStream().forEach { srcEntry ->
            val key = srcEntry.key
            if (dest.enumCollectionProperties.containsKey(key)) {
                val dstEntry = dest.enumCollectionProperties[srcEntry.key]
                dstEntry?.clear()
                val it = srcEntry.value.iterator()
                while (it.hasNext()) {
                    val nxt = it.next()
                    dstEntry?.add(nxt)
                }
            }
        }
    }


    @Throws(IOException::class)
    override fun writeExternal(objectOutputStream: ObjectOutput) {
        //fieldEntity and enum maps
        objectOutputStream.writeObject(overview)
        objectOutputStream.writeObject(fields)
        objectOutputStream.writeObject(enums)
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

    private fun serializeEnums(input: Map<JdsFieldEnum<*>, ObjectProperty<Enum<*>>>): Map<JdsFieldEnum<*>, Enum<*>> {
        return input.entries.associateBy({ it.key }, { it.value.get() })
    }

    private fun serializeEnumCollections(input: Map<JdsFieldEnum<*>, ListProperty<Enum<*>>>): Map<JdsFieldEnum<*>, List<Enum<*>>> {
        return input.entries.associateBy({ it.key }, { ArrayList(it.value.get()) })
    }

    private fun serializeBlobs(input: Map<Long, BlobProperty>): Map<Long, BlobProperty> {
        return input.entries.associateBy({ it.key }, { it.value })
    }

    private fun serializeIntegers(input: Map<Long, ListProperty<Int>>): Map<Long, List<Int>> {
        return input.entries.associateBy({ it.key }, { ArrayList(it.value.get()) })
    }

    private fun serializeLongs(input: Map<Long, ListProperty<Long>>): Map<Long, List<Long>> {
        return input.entries.associateBy({ it.key }, { ArrayList(it.value.get()) })
    }

    private fun serializeDoubles(input: Map<Long, ListProperty<Double>>): Map<Long, List<Double>> {
        return input.entries.associateBy({ it.key }, { ArrayList(it.value.get()) })
    }

    private fun serializeFloats(input: Map<Long, ListProperty<Float>>): Map<Long, List<Float>> {
        return input.entries.associateBy({ it.key }, { ArrayList(it.value.get()) })
    }

    private fun serializeDateTimes(input: Map<Long, ListProperty<LocalDateTime>>): Map<Long, List<LocalDateTime>> {
        return input.entries.associateBy({ it.key }, { ArrayList(it.value.get()) })
    }

    private fun serializeStrings(input: Map<Long, ListProperty<String>>): Map<Long, List<String>> {
        return input.entries.associateBy({ it.key }, { ArrayList(it.value.get()) })
    }

    private fun serializeObjects(input: Map<JdsFieldEntity<*>, ListProperty<JdsEntity>>): Map<JdsFieldEntity<*>, List<JdsEntity>> {
        return input.entries.associateBy({ it.key }, { ArrayList(it.value.get()) })
    }

    private fun serializeObject(input: Map<JdsFieldEntity<*>, ObjectProperty<JdsEntity>>): Map<JdsFieldEntity<*>, JdsEntity> {
        return input.entries.associateBy({ it.key }, { it.value.get() })
    }

    private fun serializeFloat(input: Map<Long, FloatProperty>): Map<Long, Float> {
        return input.entries.associateBy({ it.key }, { it.value.get() })
    }

    private fun serializeDouble(input: Map<Long, DoubleProperty>): Map<Long, Double> {
        return input.entries.associateBy({ it.key }, { it.value.get() })
    }

    private fun serializeBoolean(input: Map<Long, BooleanProperty>): Map<Long, Boolean> {
        return input.entries.associateBy({ it.key }, { it.value.get() })
    }

    private fun serializeLong(input: Map<Long, LongProperty>): Map<Long, Long> {
        return input.entries.associateBy({ it.key }, { it.value.get() })
    }

    private fun serializeInteger(input: Map<Long, IntegerProperty>): Map<Long, Int> {
        return input.entries.associateBy({ it.key }, { it.value.get() })
    }

    private fun serializeTemporal(input: Map<Long, ObjectProperty<out Temporal>>): Map<Long, Temporal> {
        return input.entries.associateBy({ it.key }, { it.value.get() })
    }

    private fun serializableString(input: Map<Long, StringProperty>): Map<Long, String> {
        return input.entries.associateBy({ it.key }, { it.value.get() })
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    override fun readExternal(objectInputStream: ObjectInput) {
        //fieldEntity and enum maps
        overview = objectInputStream.readObject() as JdsOverview
        fields.addAll(objectInputStream.readObject() as Set<JdsField>)
        enums.addAll(objectInputStream.readObject() as Set<JdsFieldEnum<*>>)
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
        putStrings(stringArrayProperties, objectInputStream.readObject() as Map<Long, List<String>>)
        putDateTimes(dateTimeArrayProperties, objectInputStream.readObject() as Map<Long, List<LocalDateTime>>)
        putFloats(floatArrayProperties, objectInputStream.readObject() as Map<Long, List<Float>>)
        putDoubles(doubleArrayProperties, objectInputStream.readObject() as Map<Long, List<Double>>)
        putLongs(longArrayProperties, objectInputStream.readObject() as Map<Long, List<Long>>)
        putIntegers(integerArrayProperties, objectInputStream.readObject() as Map<Long, List<Int>>)
        //enumProperties
        putEnum(enumProperties, objectInputStream.readObject() as Map<JdsFieldEnum<*>, Enum<*>>)
        putEnums(enumCollectionProperties, objectInputStream.readObject() as Map<JdsFieldEnum<*>, List<Enum<*>>>)
    }

    private fun putDurations(destination: HashMap<Long, ObjectProperty<Duration>>, source: Map<Long, Duration>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putPeriods(destination: HashMap<Long, ObjectProperty<Period>>, source: Map<Long, Period>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putYearMonths(destination: HashMap<Long, ObjectProperty<Temporal>>, source: Map<Long, Temporal>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putMonthDays(destination: HashMap<Long, ObjectProperty<MonthDay>>, source: Map<Long, MonthDay>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putEnums(destination: Map<JdsFieldEnum<*>, ListProperty<Enum<*>>>, source: Map<JdsFieldEnum<*>, List<Enum<*>>>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.addAll(entry.value) }
    }

    private fun putEnum(destination: Map<JdsFieldEnum<*>, ObjectProperty<Enum<*>>>, source: Map<JdsFieldEnum<*>, Enum<*>>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putObjects(destination: Map<JdsFieldEntity<*>, ListProperty<JdsEntity>>, source: Map<JdsFieldEntity<*>, List<JdsEntity>>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.addAll(entry.value) }
    }

    private fun putStrings(destination: Map<Long, ListProperty<String>>, source: Map<Long, List<String>>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.addAll(entry.value) }
    }

    private fun putDateTimes(destination: Map<Long, ListProperty<LocalDateTime>>, source: Map<Long, List<LocalDateTime>>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.addAll(entry.value) }
    }

    private fun putFloats(destination: Map<Long, ListProperty<Float>>, source: Map<Long, List<Float>>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.addAll(entry.value) }
    }

    private fun putDoubles(destination: Map<Long, ListProperty<Double>>, source: Map<Long, List<Double>>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.addAll(entry.value) }
    }

    private fun putLongs(destination: Map<Long, ListProperty<Long>>, source: Map<Long, List<Long>>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.addAll(entry.value) }
    }

    private fun putIntegers(destination: Map<Long, ListProperty<Int>>, source: Map<Long, List<Int>>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.addAll(entry.value) }
    }

    private fun putInteger(destination: Map<Long, IntegerProperty>, source: Map<Long, Int>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putBlobs(destination: Map<Long, BlobProperty>, source: Map<Long, BlobProperty>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.set(entry.value.get()!!) }
    }

    private fun putLong(destination: Map<Long, LongProperty>, source: Map<Long, Long>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putBoolean(destination: Map<Long, BooleanProperty>, source: Map<Long, Boolean>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putDouble(destination: Map<Long, DoubleProperty>, source: Map<Long, Double>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putObject(destination: Map<JdsFieldEntity<*>, ObjectProperty<JdsEntity>>, source: Map<JdsFieldEntity<*>, JdsEntity>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putFloat(destination: Map<Long, FloatProperty>, source: Map<Long, Float>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putTemporal(destination: Map<Long, ObjectProperty<Temporal>>, source: Map<Long, Temporal>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putString(destination: Map<Long, StringProperty>, source: Map<Long, String>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.set(entry.value) }
    }

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

    internal fun assign(embeddedObject: JdsEmbeddedObject) {
        //==============================================
        //PRIMITIVES
        //==============================================
        booleanProperties.entries.parallelStream().forEach {
            embeddedObject.b.add(JdsBooleanValues(it.key, when (it.value.value) {true -> 1;false -> 0
            }))
        }
        stringProperties.entries.parallelStream().forEach { embeddedObject.s.add(JdsStringValues(it.key, it.value.value)) }
        floatProperties.entries.parallelStream().forEach { embeddedObject.f.add(JdsFloatValues(it.key, it.value.value)) }
        doubleProperties.entries.parallelStream().forEach { embeddedObject.d.add(JdsDoubleValues(it.key, it.value.value)) }
        longProperties.entries.parallelStream().forEach { embeddedObject.l.add(JdsLongValues(it.key, it.value.value)) }
        integerProperties.entries.parallelStream().forEach { embeddedObject.i.add(JdsIntegerValues(it.key, it.value.value)) }
        //==============================================
        //Dates & Time
        //==============================================
        localDateTimeProperties.entries.parallelStream().forEach { embeddedObject.ldt.add(JdsLocalDateTimeValues(it.key, Timestamp.valueOf(it.value.value as LocalDateTime))) }
        zonedDateTimeProperties.entries.parallelStream().forEach { embeddedObject.zdt.add(JdsZonedDateTimeValues(it.key, (it.value.value as ZonedDateTime).toInstant().toEpochMilli())) }
        localTimeProperties.entries.parallelStream().forEach { embeddedObject.t.add(JdsTimeValues(it.key, (it.value.value as LocalTime).toNanoOfDay())) }
        localDateProperties.entries.parallelStream().forEach { embeddedObject.ld.add(JdsLocalDateValues(it.key, Timestamp.valueOf((it.value.value as LocalDate).atStartOfDay()))) }
        durationProperties.entries.parallelStream().forEach { embeddedObject.du.add(JdsDurationValues(it.key, it.value.value.toNanos())) }
        monthDayProperties.entries.parallelStream().forEach { embeddedObject.md.add(JdsMonthDayValues(it.key, it.value.value.toString())) }
        yearMonthProperties.entries.parallelStream().forEach { embeddedObject.ym.add(JdsYearMonthValues(it.key, (it.value.value as YearMonth).toString())) }
        periodProperties.entries.parallelStream().forEach { embeddedObject.p.add(JdsPeriodValues(it.key, it.value.value.toString())) }
        //==============================================
        //BLOB
        //==============================================
        blobProperties.entries.parallelStream().forEach { embeddedObject.bl.add(JdsBlobValues(it.key, it.value.get() ?: ByteArray(0))) }
        //==============================================
        //Enums
        //==============================================
        enumProperties.entries.parallelStream().forEach { embeddedObject.e.add(JdsEnumValues(it.key.field.id, it.value.value.ordinal)) }
        enumCollectionProperties.entries.parallelStream().forEach { it.value.forEachIndexed { i, child -> embeddedObject.ea.add(JdsEnumCollections(it.key.field.id, i, child.ordinal)) } }
        //==============================================
        //ARRAYS
        //==============================================
        stringArrayProperties.entries.parallelStream().forEach { it.value.forEachIndexed { i, child -> embeddedObject.sa.add(JdsTextCollections(it.key, i, child)) } }
        dateTimeArrayProperties.entries.parallelStream().forEach { it.value.forEachIndexed { i, child -> embeddedObject.dta.add(JdsDateCollections(it.key, i, Timestamp.valueOf(child))) } }
        floatArrayProperties.entries.parallelStream().forEach { it.value.forEachIndexed { i, child -> embeddedObject.fa.add(JdsFloatCollections(it.key, i, child)) } }
        doubleArrayProperties.entries.parallelStream().forEach { it.value.forEachIndexed { i, child -> embeddedObject.da.add(JdsDoubleCollections(it.key, i, child)) } }
        longArrayProperties.entries.parallelStream().forEach { it.value.forEachIndexed { i, child -> embeddedObject.la.add(JdsLongCollections(it.key, i, child)) } }
        integerArrayProperties.entries.parallelStream().forEach { it.value.forEachIndexed { i, child -> embeddedObject.ia.add(JdsIntegerCollections(it.key, i, child)) } }
        //==============================================
        //EMBEDDED OBJECTS
        //==============================================
        objectArrayProperties.entries.parallelStream().forEach { itx ->
            itx.value.forEach {
                embeddedObject.eb.add(JdsEntityBinding(overview.uuid, it.overview.uuid, itx.key.fieldEntity.id, it.overview.entityId))
                embeddedObject.eo.add(JdsEmbeddedObject(it))
            }
        }
        objectProperties.entries.parallelStream().forEach {
            embeddedObject.eb.add(JdsEntityBinding(overview.uuid, it.value.value.overview.uuid, it.key.fieldEntity.id, it.value.value.overview.entityId))
            embeddedObject.eo.add(JdsEmbeddedObject(it.value.value))
        }
    }

    /**
     * @param jdsFieldType
     * @param fieldId
     * @param value
     */
    internal fun populateProperties(jdsFieldType: JdsFieldType, fieldId: Long, value: Any?) {
        if (value == null)
            return //I.HATE.NULL - Rather retain default values
        when (jdsFieldType) {
            JdsFieldType.FLOAT -> floatProperties[fieldId]?.set(value as Float)
            JdsFieldType.INT -> integerProperties[fieldId]?.set(value as Int)
            JdsFieldType.DOUBLE -> doubleProperties[fieldId]?.set(value as Double)
            JdsFieldType.LONG -> longProperties[fieldId]?.set(value as Long)
            JdsFieldType.STRING -> stringProperties[fieldId]?.set(value as String)
            JdsFieldType.DATE_TIME -> localDateTimeProperties[fieldId]?.set((value as Timestamp).toLocalDateTime())
            JdsFieldType.DOUBLE_COLLECTION -> doubleArrayProperties[fieldId]?.get()?.add(value as Double)
            JdsFieldType.FLOAT_COLLECTION -> floatArrayProperties[fieldId]?.get()?.add(value as Float)
            JdsFieldType.INT_COLLECTION -> integerArrayProperties[fieldId]?.get()?.add(value as Int)
            JdsFieldType.LONG_COLLECTION -> longArrayProperties[fieldId]?.get()?.add(value as Long)
            JdsFieldType.STRING_COLLECTION -> stringArrayProperties[fieldId]?.get()?.add(value as String)
            JdsFieldType.DATE_TIME_COLLECTION -> dateTimeArrayProperties[fieldId]?.get()?.add((value as Timestamp).toLocalDateTime())
            JdsFieldType.BOOLEAN -> when (value) {
                is Int -> booleanProperties[fieldId]?.set(value == 1)
                is Boolean -> booleanProperties[fieldId]?.set(value)
            }
            JdsFieldType.ZONED_DATE_TIME -> when (value) {
                is Long -> zonedDateTimeProperties[fieldId]?.set(ZonedDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault()))
                is Timestamp -> zonedDateTimeProperties[fieldId]?.set(ZonedDateTime.ofInstant(value.toInstant(), ZoneOffset.systemDefault()))
                is String -> zonedDateTimeProperties[fieldId]?.set(value.toZonedDateTime())
                is OffsetDateTime -> zonedDateTimeProperties[fieldId]?.set(value.toZonedDateTime())
            }
            JdsFieldType.DATE -> localDateProperties[fieldId]?.set((value as Timestamp).toLocalDateTime().toLocalDate())
            JdsFieldType.TIME -> when (value) {
                is Long -> localTimeProperties[fieldId]?.set(LocalTime.ofNanoOfDay(value))
                is LocalTime -> localTimeProperties[fieldId]?.set(value)
                is String -> localTimeProperties[fieldId]?.set(value.toLocalTimeSqlFormat())
            }
            JdsFieldType.BLOB -> blobProperties[fieldId]?.set(value as ByteArray)
            JdsFieldType.DURATION -> durationProperties[fieldId]?.set(Duration.ofNanos(value as Long))
            JdsFieldType.MONTH_DAY -> monthDayProperties[fieldId]?.value = MonthDay.parse(value as String)
            JdsFieldType.YEAR_MONTH -> yearMonthProperties[fieldId]?.value = YearMonth.parse(value as String)
            JdsFieldType.PERIOD -> periodProperties[fieldId]?.value = Period.parse(value as String)
            JdsFieldType.ENUM -> enumProperties.filter { it.key.field.id == fieldId }.forEach { it.value?.set(it.key.valueOf(value as Int)) }
            JdsFieldType.ENUM_COLLECTION -> enumCollectionProperties.filter { it.key.field.id == fieldId }.forEach {
                val enumValues = it.key.enumType.enumConstants
                val index = value as Int
                if (index < enumValues.size) {
                    it.value.get().add(enumValues[index] as Enum<*>)
                }
            }
        }
    }

    internal fun populateObjects(jdsDb: JdsDb, fieldId: Long, entityId: Long, uuid: String, innerObjects: ConcurrentLinkedQueue<JdsEntity>, uuids: HashSet<String>) {
        try {
            val entityClass = jdsDb.classes[entityId]!!
            objectArrayProperties.filter { it.key.fieldEntity.id == fieldId }.forEach {
                val entity = entityClass.newInstance()
                entity.overview.uuid = uuid
                uuids.add(uuid)
                it.value.get().add(entity)
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
    }

    /**
     * Binds all the fieldIds attached to an entity, updates the fieldIds dictionary
     *
     * @param connection the SQL connection to use for DB operations
     * @param entityId   the value representing the entity
     * @param fields     the values representing the entity's fieldIds
     */
    internal fun mapClassFields(jdsDb: JdsDb, connection: Connection, entityId: Long) {
        try {
            (if (jdsDb.supportsStatements) NamedCallableStatement(connection, jdsDb.mapClassFields()) else NamedPreparedStatement(connection, jdsDb.mapClassFields())).use { mapClassFields ->
                (if (jdsDb.supportsStatements) NamedCallableStatement(connection, jdsDb.mapFieldNames()) else NamedPreparedStatement(connection, jdsDb.mapFieldNames())).use { mapFieldNames ->
                    fields.forEach {
                        //1. map this fieldEntity ID to the entity type
                        mapClassFields.setLong("entityId", entityId)
                        mapClassFields.setLong("fieldId", it.id)
                        mapClassFields.addBatch()
                        //2. map this fieldEntity to the fieldEntity dictionary
                        mapFieldNames.setLong("fieldId", it.id)
                        mapFieldNames.setString("fieldName", it.name)
                        mapFieldNames.setString("fieldDescription", it.description)
                        mapFieldNames.addBatch()
                    }
                    mapClassFields.executeBatch()
                    mapFieldNames.executeBatch()
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    /**
     * Binds all the enumProperties attached to an entity
     *
     * @param entityId the value representing the entity
     * @param fields   the entity's enumProperties
     */
    @Synchronized
    internal fun mapClassEnums(jdsDb: JdsDb, connection: Connection, entityId: Long) {
        mapEnumValues(jdsDb, connection, enums)
        mapClassEnumsImplementation(jdsDb, connection, entityId, enums)
        if (jdsDb.isPrintingOutput)
            System.out.printf("Mapped Enums for Entity[%s]\n", entityId)
    }

    /**
     * Binds all the fieldEntity types and updates reference tables
     *
     * @param jdsDb the current database implementation
     * @param connection the SQL connection to use for DB operations
     * @param entityId   the value representing the entity
     */
    internal fun mapClassFieldTypes(jdsDb: JdsDb, connection: Connection, entityId: Long) {
        try {
            (if (jdsDb.supportsStatements) NamedCallableStatement(connection, jdsDb.mapFieldTypes()) else NamedPreparedStatement(connection, jdsDb.mapFieldTypes())).use { mapFieldTypes ->
                fields.forEach {
                    mapFieldTypes.setLong("typeId", it.id)
                    mapFieldTypes.setString("typeName", it.type.toString())
                    mapFieldTypes.addBatch()
                }
                mapFieldTypes.executeBatch()
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    /**
     * Binds all the enumProperties attached to an entity
     *
     * @param connection the SQL connection to use for DB operations
     * @param entityId   the value representing the entity
     * @param fields     the entity's enumProperties
     */
    @Synchronized
    private fun mapClassEnumsImplementation(jdsDb: JdsDb, connection: Connection, entityId: Long, fields: Set<JdsFieldEnum<*>>) {
        try {
            (if (jdsDb.supportsStatements) connection.prepareCall(jdsDb.mapClassEnumsImplementation()) else connection.prepareStatement(jdsDb.mapClassEnumsImplementation())).use { statement ->
                for (field in fields) {
                    for (index in 0 until field.values.size) {
                        statement.setLong(1, entityId)
                        statement.setLong(2, field.field.id)
                        statement.addBatch()
                    }
                }
                statement.executeBatch()
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    /**
     * Binds all the values attached to an enum
     *
     * @param connection the SQL connection to use for DB operations
     * @param fieldEnums the fieldEntity enum
     */
    @Synchronized
    private fun mapEnumValues(jdsDb: JdsDb, connection: Connection, fieldEnums: Set<JdsFieldEnum<*>>) {
        try {
            (if (jdsDb.supportsStatements) connection.prepareCall(jdsDb.mapEnumValues()) else connection.prepareStatement(jdsDb.mapEnumValues())).use { statement ->
                for (field in fieldEnums) {
                    for (index in 0 until field.values.size) {
                        statement.setLong(1, field.field.id)
                        statement.setInt(2, index)
                        statement.setString(3, field.values[index].toString())
                        statement.addBatch()
                    }
                }
                statement.executeBatch()
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

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

    override fun registerFields(jdsTable: JdsTable) {
        fields.forEach {
            jdsTable.registerField(it)
        }
    }
}
