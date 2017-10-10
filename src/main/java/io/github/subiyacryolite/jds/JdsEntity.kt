/*
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
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation
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

    private val _overview = SimpleObjectProperty<IJdsEntityOverview>(JdsEntityOverview())
    override val overview: IJdsEntityOverview
        get() = _overview.get()

    //field and enum maps
    private val properties: MutableMap<Long, String> = HashMap()
    private val types: MutableMap<Long, String> = HashMap()
    private val objects: MutableSet<Long> = HashSet()
    private val allEnums: MutableSet<JdsFieldEnum<*>> = HashSet()
    private val _entityName = SimpleStringProperty("")
    //strings and localDateTimes
    private val localDateTimeProperties: HashMap<Long, SimpleObjectProperty<Temporal>> = HashMap()
    private val zonedDateTimeProperties: HashMap<Long, SimpleObjectProperty<Temporal>> = HashMap()
    private val localDateProperties: HashMap<Long, SimpleObjectProperty<Temporal>> = HashMap()
    private val localTimeProperties: HashMap<Long, SimpleObjectProperty<Temporal>> = HashMap()
    private val stringProperties: HashMap<Long, SimpleStringProperty> = HashMap()
    //numeric
    private val floatProperties: HashMap<Long, SimpleFloatProperty> = HashMap()
    private val doubleProperties: HashMap<Long, SimpleDoubleProperty> = HashMap()
    private val booleanProperties: HashMap<Long, SimpleBooleanProperty> = HashMap()
    private val longProperties: HashMap<Long, SimpleLongProperty> = HashMap()
    private val integerProperties: HashMap<Long, SimpleIntegerProperty> = HashMap()
    //arrays
    private val objectArrayProperties: HashMap<Long, SimpleListProperty<JdsEntity>> = HashMap()
    private val stringArrayProperties: HashMap<Long, SimpleListProperty<String>> = HashMap()
    private val dateTimeArrayProperties: HashMap<Long, SimpleListProperty<LocalDateTime>> = HashMap()
    private val floatArrayProperties: HashMap<Long, SimpleListProperty<Float>> = HashMap()
    private val doubleArrayProperties: HashMap<Long, SimpleListProperty<Double>> = HashMap()
    private val longArrayProperties: HashMap<Long, SimpleListProperty<Long>> = HashMap()
    private val integerArrayProperties: HashMap<Long, SimpleListProperty<Int>> = HashMap()
    //enums
    private val enumProperties: HashMap<JdsFieldEnum<*>, SimpleObjectProperty<Enum<*>>> = HashMap()
    private val enumCollectionProperties: HashMap<JdsFieldEnum<*>, SimpleListProperty<Enum<*>>> = HashMap()
    //objects
    private val objectProperties: HashMap<Long, SimpleObjectProperty<JdsEntity>> = HashMap()
    private val objectCascade: HashMap<Long, Boolean> = HashMap()
    //blobs
    private val blobProperties: HashMap<Long, SimpleBlobProperty> = HashMap()


    init {
        if (javaClass.isAnnotationPresent(JdsEntityAnnotation::class.java)) {
            val entityAnnotation = javaClass.getAnnotation(JdsEntityAnnotation::class.java)
            _entityName.set(entityAnnotation.entityName)
            overview.entityId = entityAnnotation.entityId
            overview.version = entityAnnotation.version
        } else {
            throw RuntimeException("You must annotate the class [" + javaClass.canonicalName + "] with [" + JdsEntityAnnotation::class.java + "]")
        }
    }

    override var entityName: String
        get() = _entityName.get()
        set(value) = _entityName.set(value)


    /**
     * @param jdsField
     * @param integerProperty
     */
    protected fun map(jdsField: JdsField, integerProperty: SimpleBlobProperty) {
        if (jdsField.type === JdsFieldType.BLOB) {
            properties.put(jdsField.id, jdsField.name)
            types.put(jdsField.id, jdsField.type.toString())
            blobProperties.put(jdsField.id, integerProperty)
        } else {
            throw RuntimeException("Please prepareDatabaseComponents jdsField [$jdsField] to the correct type")
        }
    }

    /**
     * @param jdsField
     * @param integerProperty
     */
    protected fun map(jdsField: JdsField, integerProperty: SimpleIntegerProperty) {
        if (jdsField.type === JdsFieldType.INT) {
            properties.put(jdsField.id, jdsField.name)
            types.put(jdsField.id, jdsField.type.toString())
            integerProperties.put(jdsField.id, integerProperty)
        } else {
            throw RuntimeException("Please prepareDatabaseComponents jdsField [$jdsField] to the correct type")
        }
    }

    /**
     * @param jdsField
     * @param temporalProperty
     */
    protected fun map(jdsField: JdsField, temporalProperty: SimpleObjectProperty<out Temporal>) {
        val temporal = temporalProperty.get()
        if (temporal is LocalDateTime) {
            if (jdsField.type === JdsFieldType.DATE_TIME) {
                properties.put(jdsField.id, jdsField.name)
                types.put(jdsField.id, jdsField.type.toString())
                localDateTimeProperties.put(jdsField.id, temporalProperty as SimpleObjectProperty<Temporal>)
            } else {
                throw RuntimeException("Please prepareDatabaseComponents jdsField [$jdsField] to the correct type")
            }
        } else if (temporal is ZonedDateTime) {
            if (jdsField.type === JdsFieldType.ZONED_DATE_TIME) {
                properties.put(jdsField.id, jdsField.name)
                types.put(jdsField.id, jdsField.type.toString())
                zonedDateTimeProperties.put(jdsField.id, temporalProperty as SimpleObjectProperty<Temporal>)
            } else {
                throw RuntimeException("Please prepareDatabaseComponents jdsField [$jdsField] to the correct type")
            }
        } else if (temporal is LocalDate) {
            if (jdsField.type === JdsFieldType.DATE) {
                properties.put(jdsField.id, jdsField.name)
                types.put(jdsField.id, jdsField.type.toString())
                localDateProperties.put(jdsField.id, temporalProperty as SimpleObjectProperty<Temporal>)
            } else {
                throw RuntimeException("Please prepareDatabaseComponents jdsField [$jdsField] to the correct type")
            }
        } else if (temporal is LocalTime) {
            if (jdsField.type === JdsFieldType.TIME) {
                properties.put(jdsField.id, jdsField.name)
                types.put(jdsField.id, jdsField.type.toString())
                localTimeProperties.put(jdsField.id, temporalProperty as SimpleObjectProperty<Temporal>)
            } else {
                throw RuntimeException("Please prepareDatabaseComponents jdsField [$jdsField] to the correct type")
            }
        }
    }

    /**
     * @param jdsField
     * @param stringProperty
     */
    protected fun map(jdsField: JdsField, stringProperty: SimpleStringProperty) {
        if (jdsField.type === JdsFieldType.TEXT) {
            properties.put(jdsField.id, jdsField.name)
            types.put(jdsField.id, jdsField.type.toString())
            stringProperties.put(jdsField.id, stringProperty)
        } else {
            throw RuntimeException("Please prepareDatabaseComponents jdsField [$jdsField] to the correct type")
        }
    }

    /**
     * @param jdsField
     * @param floatProperty
     */
    protected fun map(jdsField: JdsField, floatProperty: SimpleFloatProperty) {
        if (jdsField.type === JdsFieldType.FLOAT) {
            properties.put(jdsField.id, jdsField.name)
            types.put(jdsField.id, jdsField.type.toString())
            floatProperties.put(jdsField.id, floatProperty)
        } else {
            throw RuntimeException("Please prepareDatabaseComponents jdsField [$jdsField] to the correct type")
        }
    }

    /**
     * @param jdsField
     * @param longProperty
     */
    protected fun map(jdsField: JdsField, longProperty: SimpleLongProperty) {
        if (jdsField.type === JdsFieldType.LONG) {
            properties.put(jdsField.id, jdsField.name)
            types.put(jdsField.id, jdsField.type.toString())
            longProperties.put(jdsField.id, longProperty)
        } else {
            throw RuntimeException("Please prepareDatabaseComponents jdsField [$jdsField] to the correct type")
        }
    }

    /**
     * @param jdsField
     * @param doubleProperty
     */
    protected fun map(jdsField: JdsField, doubleProperty: SimpleDoubleProperty) {
        if (jdsField.type === JdsFieldType.DOUBLE) {
            properties.put(jdsField.id, jdsField.name)
            types.put(jdsField.id, jdsField.type.toString())
            doubleProperties.put(jdsField.id, doubleProperty)
        } else {
            throw RuntimeException("Please prepareDatabaseComponents jdsField [$jdsField] to the correct type")
        }
    }

    /**
     * @param jdsField
     * @param booleanProperty
     */
    protected fun map(jdsField: JdsField, booleanProperty: SimpleBooleanProperty) {
        if (jdsField.type === JdsFieldType.BOOLEAN) {
            properties.put(jdsField.id, jdsField.name)
            types.put(jdsField.id, jdsField.type.toString())
            booleanProperties.put(jdsField.id, booleanProperty)
        } else {
            throw RuntimeException("Please prepareDatabaseComponents jdsField [$jdsField] to the correct type")
        }
    }

    /**
     * @param jdsField
     * @param strings
     */
    protected fun mapStrings(jdsField: JdsField, strings: SimpleListProperty<String>) {
        if (strings == null) {
            return
        }
        if (jdsField.type === JdsFieldType.ARRAY_TEXT) {
            properties.put(jdsField.id, jdsField.name)
            types.put(jdsField.id, jdsField.type.toString())
            stringArrayProperties.put(jdsField.id, strings)
        } else {
            throw RuntimeException("Please prepareDatabaseComponents jdsField [$jdsField] to the correct type")
        }
    }

    /**
     * @param jdsField
     * @param floats
     */
    protected fun mapFloats(jdsField: JdsField, floats: SimpleListProperty<Float>) {
        if (floats == null) {
            return
        }
        if (jdsField.type === JdsFieldType.ARRAY_FLOAT) {
            properties.put(jdsField.id, jdsField.name)
            types.put(jdsField.id, jdsField.type.toString())
            floatArrayProperties.put(jdsField.id, floats)
        } else {
            throw RuntimeException("Please prepareDatabaseComponents jdsField [$jdsField] to the correct type")
        }
    }

    /**
     * @param jdsField
     * @param doubles
     */
    protected fun mapDoubles(jdsField: JdsField, doubles: SimpleListProperty<Double>) {
        if (doubles == null) {
            return
        }
        if (jdsField.type === JdsFieldType.ARRAY_DOUBLE) {
            properties.put(jdsField.id, jdsField.name)
            types.put(jdsField.id, jdsField.type.toString())
            doubleArrayProperties.put(jdsField.id, doubles)
        } else {
            throw RuntimeException("Please prepareDatabaseComponents jdsField [$jdsField] to the correct type")
        }
    }

    /**
     * @param jdsField
     * @param longs
     */
    protected fun mapLongs(jdsField: JdsField, longs: SimpleListProperty<Long>) {
        if (longs == null) {
            return
        }
        if (jdsField.type === JdsFieldType.ARRAY_LONG) {
            properties.put(jdsField.id, jdsField.name)
            types.put(jdsField.id, jdsField.type.toString())
            longArrayProperties.put(jdsField.id, longs)
        } else {
            throw RuntimeException("Please prepareDatabaseComponents jdsField [$jdsField] to the correct type")
        }
    }

    /**
     * @param jdsFieldEnum
     * @param enums
     */
    protected fun map(jdsFieldEnum: JdsFieldEnum<*>, enums: SimpleObjectProperty<out Enum<*>>) {
        if (enums == null) {
            return
        }
        allEnums.add(jdsFieldEnum)
        if (jdsFieldEnum.getField().type === JdsFieldType.ENUM) {
            properties.put(jdsFieldEnum.getField().id, jdsFieldEnum.getField().name)
            types.put(jdsFieldEnum.getField().id, jdsFieldEnum.getField().type!!.toString())
            enumProperties.put(jdsFieldEnum, enums as SimpleObjectProperty<Enum<*>>)
        } else {
            throw RuntimeException("Please prepareDatabaseComponents field [$jdsFieldEnum] to the correct type")
        }
    }

    /**
     * @param jdsFieldEnum
     * @param enums
     */
    protected fun mapEnums(jdsFieldEnum: JdsFieldEnum<*>, enums: SimpleListProperty<out Enum<*>>) {
        if (enums == null) {
            return
        }
        allEnums.add(jdsFieldEnum)
        if (jdsFieldEnum.getField().type === JdsFieldType.ENUM_COLLECTION) {
            properties.put(jdsFieldEnum.getField().id, jdsFieldEnum.getField().name)
            types.put(jdsFieldEnum.getField().id, jdsFieldEnum.getField().type!!.toString())
            enumCollectionProperties.put(jdsFieldEnum, enums as SimpleListProperty<Enum<*>>)
        } else {
            throw RuntimeException("Please prepareDatabaseComponents field [$jdsFieldEnum] to the correct type")
        }
    }

    /**
     * @param entity
     * @param property
     * @param cascadeOnDelete
     */
    protected fun <T : IJdsEntity> map(entity: Class<out T>, property: SimpleObjectProperty<T>, cascadeOnDelete: Boolean) {
        if (entity.isAnnotationPresent(JdsEntityAnnotation::class.java)) {
            val entityAnnotation = entity.getAnnotation(JdsEntityAnnotation::class.java)
            if (!objectArrayProperties.containsKey(entityAnnotation.entityId) && !objectProperties.containsKey(entityAnnotation.entityId)) {
                objectProperties.put(entityAnnotation.entityId, property as SimpleObjectProperty<JdsEntity>)
                objects.add(entityAnnotation.entityId)
                objectCascade.put(entityAnnotation.entityId, cascadeOnDelete)
            } else {
                throw RuntimeException("You can only bind a class to one property. This class is already bound to one object or object array")
            }
        } else {
            throw RuntimeException("You must annotate the class [" + entity.canonicalName + "] with [" + JdsEntityAnnotation::class.java + "]")
        }
    }

    /**
     * @param entity
     * @param property
     */
    protected fun <T : IJdsEntity> map(entity: Class<out T>, property: SimpleObjectProperty<T>) {
        map(entity, property, false)
    }

    /**
     * @param entity
     * @param properties
     * @param cascadeOnDelete
     */
    protected fun <T : IJdsEntity> map(entity: Class<out T>, properties: SimpleListProperty<T>, cascadeOnDelete: Boolean) {
        if (entity.isAnnotationPresent(JdsEntityAnnotation::class.java)) {
            val entityAnnotation = entity.getAnnotation(JdsEntityAnnotation::class.java)
            if (!objectArrayProperties.containsKey(entityAnnotation.entityId) && !objectProperties.containsKey(entityAnnotation.entityId)) {
                objectArrayProperties.put(entityAnnotation.entityId, properties as SimpleListProperty<JdsEntity>)
                objects.add(entityAnnotation.entityId)
                objectCascade.put(entityAnnotation.entityId, cascadeOnDelete)
            } else {
                throw RuntimeException("You can only bind a class to one property. This class is already bound to one object or object array")
            }
        } else {
            throw RuntimeException("You must annotate the class [" + entity.canonicalName + "] with [" + JdsEntityAnnotation::class.java + "]")
        }
    }

    /**
     * @param entity
     * @param properties
     */
    protected fun <T : IJdsEntity> map(entity: Class<out T>, properties: SimpleListProperty<T>) {
        map(entity, properties, false)
    }

    /**
     * Copy values from matching fields found in both objects
     *
     * @param source The entity to copy values from
     * @param <T>    A valid JDSEntity
    </T> */
    fun <T : JdsEntity> copy(source: T) {
        copyHeaderValues(source)
        copyPropertyValues(source)
        copyArrayValues(source)
        copyEnumValues(source)
        copyObjectAndObjectArrayValues(source)
    }

    /**
     * Copy all header overview information
     *
     * @param source The entity to copy values from
     * @param <T>    A valid JDSEntity
    </T> */
    private fun <T : IJdsEntity> copyArrayValues(source: T) {
        overview.dateCreated = source.overview.dateCreated
        overview.dateModified = source.overview.dateModified
        overview.entityGuid = source.overview.entityGuid
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
    }

    /**
     * Copy all property array values
     *
     * @param source The entity to copy values from
     * @param <T>    A valid JDSEntity A valid JDSEntity
    </T> */
    private fun <T : JdsEntity> copyHeaderValues(source: T) {
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
        //field and enum maps
        objectOutputStream.writeObject(overview)
        objectOutputStream.writeObject(properties)
        objectOutputStream.writeObject(types)
        objectOutputStream.writeObject(objects)
        objectOutputStream.writeObject(allEnums)
        objectOutputStream.writeUTF(entityName)
        //objects
        objectOutputStream.writeObject(serializeObject(objectProperties))
        //strings and localDateTimes
        objectOutputStream.writeObject(serializeTemporal(localDateTimeProperties))
        objectOutputStream.writeObject(serializeTemporal(zonedDateTimeProperties))
        objectOutputStream.writeObject(serializeTemporal(localDateProperties))
        objectOutputStream.writeObject(serializeTemporal(localTimeProperties))
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
        //enums
        objectOutputStream.writeObject(serializeEnums(enumProperties))
        objectOutputStream.writeObject(serializeEnumCollections(enumCollectionProperties))
    }

    private fun serializeEnums(input: Map<JdsFieldEnum<*>, SimpleObjectProperty<Enum<*>>>): Map<JdsFieldEnum<*>, Enum<*>> {
        return input.entries.associateBy({ it.key }, { it.value.get() })
    }

    private fun serializeEnumCollections(input: Map<JdsFieldEnum<*>, SimpleListProperty<Enum<*>>>): Map<JdsFieldEnum<*>, List<Enum<*>>> {
        return input.entries.associateBy({ it.key }, { it.value.get() })
    }

    private fun serializeBlobs(input: Map<Long, SimpleBlobProperty>): Map<Long, SimpleBlobProperty> {
        return input.entries.associateBy({ it.key }, { it.value })
    }

    private fun serializeIntegers(input: Map<Long, SimpleListProperty<Int>>): Map<Long, List<Int>> {
        return input.entries.associateBy({ it.key }, { it.value.get() })
    }

    private fun serializeLongs(input: Map<Long, SimpleListProperty<Long>>): Map<Long, List<Long>> {
        return input.entries.associateBy({ it.key }, { it.value.get() })
    }

    private fun serializeDoubles(input: Map<Long, SimpleListProperty<Double>>): Map<Long, List<Double>> {
        return input.entries.associateBy({ it.key }, { it.value.get() })
    }

    private fun serializeFloats(input: Map<Long, SimpleListProperty<Float>>): Map<Long, List<Float>> {
        return input.entries.associateBy({ it.key }, { it.value.get() })
    }

    private fun serializeDateTimes(input: Map<Long, SimpleListProperty<LocalDateTime>>): Map<Long, List<LocalDateTime>> {
        return input.entries.associateBy({ it.key }, { it.value.get() })
    }

    private fun serializeStrings(input: Map<Long, SimpleListProperty<String>>): Map<Long, List<String>> {
        return input.entries.associateBy({ it.key }, { it.value.get() })
    }

    private fun serializeObjects(input: Map<Long, SimpleListProperty<JdsEntity>>): Map<Long, List<JdsEntity>> {
        return input.entries.associateBy({ it.key }, { it.value.get() })
    }

    private fun serializeObject(input: Map<Long, SimpleObjectProperty<JdsEntity>>): Map<Long, JdsEntity> {
        return input.entries.associateBy({ it.key }, { it.value.get() })
    }

    private fun serializeFloat(input: Map<Long, SimpleFloatProperty>): Map<Long, Float> {
        return input.entries.associateBy({ it.key }, { it.value.get() })
    }

    private fun serializeDouble(input: Map<Long, SimpleDoubleProperty>): Map<Long, Double> {
        return input.entries.associateBy({ it.key }, { it.value.get() })
    }

    private fun serializeBoolean(input: Map<Long, SimpleBooleanProperty>): Map<Long, Boolean> {
        return input.entries.associateBy({ it.key }, { it.value.get() })
    }

    private fun serializeLong(input: Map<Long, SimpleLongProperty>): Map<Long, Long> {
        return input.entries.associateBy({ it.key }, { it.value.get() })
    }

    private fun serializeInteger(input: Map<Long, SimpleIntegerProperty>): Map<Long, Int> {
        return input.entries.associateBy({ it.key }, { it.value.get() })
    }

    private fun serializeTemporal(input: Map<Long, SimpleObjectProperty<out Temporal>>): Map<Long, Temporal> {
        return input.entries.associateBy({ it.key }, { it.value.get() })
    }

    private fun serializableString(input: Map<Long, SimpleStringProperty>): Map<Long, String> {
        return input.entries.associateBy({ it.key }, { it.value.get() })
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    override fun readExternal(objectInputStream: ObjectInput) {
        //field and enum maps
        _overview.set(objectInputStream.readObject() as JdsEntityOverview)
        properties.putAll(objectInputStream.readObject() as Map<Long, String>)
        types.putAll(objectInputStream.readObject() as Map<Long, String>)
        objects.addAll(objectInputStream.readObject() as Set<Long>)
        allEnums.addAll(objectInputStream.readObject() as Set<JdsFieldEnum<*>>)
        entityName = objectInputStream.readUTF()
        //objects
        putObject(objectProperties, objectInputStream.readObject() as Map<Long, JdsEntity>)
        //strings and localDateTimes
        putTemporal(localDateTimeProperties, objectInputStream.readObject() as Map<Long, Temporal>)
        putTemporal(zonedDateTimeProperties, objectInputStream.readObject() as Map<Long, Temporal>)
        putTemporal(localDateProperties, objectInputStream.readObject() as Map<Long, Temporal>)
        putTemporal(localTimeProperties, objectInputStream.readObject() as Map<Long, Temporal>)
        putString(stringProperties, objectInputStream.readObject() as Map<Long, String>)
        //numeric
        putFloat(floatProperties, objectInputStream.readObject() as Map<Long, Float>)
        putDouble(doubleProperties, objectInputStream.readObject() as Map<Long, Double>)
        putBoolean(booleanProperties, objectInputStream.readObject() as Map<Long, Boolean>)
        putLong(longProperties, objectInputStream.readObject() as Map<Long, Long>)
        putInteger(integerProperties, objectInputStream.readObject() as Map<Long, Int>)
        //blobs
        putBlobs(blobProperties, objectInputStream.readObject() as Map<Long, SimpleBlobProperty>)
        //arrays
        putObjects(objectArrayProperties, objectInputStream.readObject() as Map<Long, List<JdsEntity>>)
        putStrings(stringArrayProperties, objectInputStream.readObject() as Map<Long, List<String>>)
        putDateTimes(dateTimeArrayProperties, objectInputStream.readObject() as Map<Long, List<LocalDateTime>>)
        putFloats(floatArrayProperties, objectInputStream.readObject() as Map<Long, List<Float>>)
        putDoubles(doubleArrayProperties, objectInputStream.readObject() as Map<Long, List<Double>>)
        putLongs(longArrayProperties, objectInputStream.readObject() as Map<Long, List<Long>>)
        putIntegers(integerArrayProperties, objectInputStream.readObject() as Map<Long, List<Int>>)
        //enums
        putEnum(enumProperties, objectInputStream.readObject() as Map<JdsFieldEnum<*>, Enum<*>>)
        putEnums(enumCollectionProperties, objectInputStream.readObject() as Map<JdsFieldEnum<*>, List<Enum<*>>>)
    }

    private fun putEnums(destination: Map<JdsFieldEnum<*>, SimpleListProperty<Enum<*>>>, source: Map<JdsFieldEnum<*>, List<Enum<*>>>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.addAll(entry.value) }
    }

    private fun putEnum(destination: Map<JdsFieldEnum<*>, SimpleObjectProperty<Enum<*>>>, source: Map<JdsFieldEnum<*>, Enum<*>>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putObjects(destination: Map<Long, SimpleListProperty<JdsEntity>>, source: Map<Long, List<JdsEntity>>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.addAll(entry.value) }
    }


    private fun putStrings(destination: Map<Long, SimpleListProperty<String>>, source: Map<Long, List<String>>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.addAll(entry.value) }
    }

    private fun putDateTimes(destination: Map<Long, SimpleListProperty<LocalDateTime>>, source: Map<Long, List<LocalDateTime>>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.addAll(entry.value) }
    }

    private fun putFloats(destination: Map<Long, SimpleListProperty<Float>>, source: Map<Long, List<Float>>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.addAll(entry.value) }
    }

    private fun putDoubles(destination: Map<Long, SimpleListProperty<Double>>, source: Map<Long, List<Double>>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.addAll(entry.value) }
    }

    private fun putLongs(destination: Map<Long, SimpleListProperty<Long>>, source: Map<Long, List<Long>>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.addAll(entry.value) }
    }

    private fun putIntegers(destination: Map<Long, SimpleListProperty<Int>>, source: Map<Long, List<Int>>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.addAll(entry.value) }
    }

    private fun putInteger(destination: Map<Long, SimpleIntegerProperty>, source: Map<Long, Int>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putBlobs(destination: Map<Long, SimpleBlobProperty>, source: Map<Long, SimpleBlobProperty>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.set(entry.value.get()!!) }
    }

    private fun putLong(destination: Map<Long, SimpleLongProperty>, source: Map<Long, Long>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putBoolean(destination: Map<Long, SimpleBooleanProperty>, source: Map<Long, Boolean>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putDouble(destination: Map<Long, SimpleDoubleProperty>, source: Map<Long, Double>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putObject(destination: Map<Long, SimpleObjectProperty<JdsEntity>>, source: Map<Long, JdsEntity>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putFloat(destination: Map<Long, SimpleFloatProperty>, source: Map<Long, Float>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putTemporal(destination: Map<Long, SimpleObjectProperty<Temporal>>, source: Map<Long, Temporal>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.set(entry.value) }
    }

    private fun putString(destination: Map<Long, SimpleStringProperty>, source: Map<Long, String>) {
        source.entries.stream().filter { entry -> destination.containsKey(entry.key) }.forEachOrdered { entry -> destination[entry.key]?.set(entry.value) }
    }

    internal fun assign(step: Int, saveContainer: JdsSaveContainer) {
        saveContainer.booleans[step].put(overview.entityGuid, booleanProperties)
        saveContainer.localDateTimes[step].put(overview.entityGuid, localDateTimeProperties)
        saveContainer.zonedDateTimes[step].put(overview.entityGuid, zonedDateTimeProperties)
        saveContainer.localTimes[step].put(overview.entityGuid, localTimeProperties)
        saveContainer.localDates[step].put(overview.entityGuid, localDateProperties)
        saveContainer.strings[step].put(overview.entityGuid, stringProperties)
        saveContainer.floats[step].put(overview.entityGuid, floatProperties)
        saveContainer.doubles[step].put(overview.entityGuid, doubleProperties)
        saveContainer.longs[step].put(overview.entityGuid, longProperties)
        saveContainer.integers[step].put(overview.entityGuid, integerProperties)
        //assign blobs
        saveContainer.blobs[step].put(overview.entityGuid, blobProperties)
        //assign lists
        saveContainer.stringArrays[step].put(overview.entityGuid, stringArrayProperties)
        saveContainer.dateTimeArrays[step].put(overview.entityGuid, dateTimeArrayProperties)
        saveContainer.floatArrays[step].put(overview.entityGuid, floatArrayProperties)
        saveContainer.doubleArrays[step].put(overview.entityGuid, doubleArrayProperties)
        saveContainer.longArrays[step].put(overview.entityGuid, longArrayProperties)
        saveContainer.integerArrays[step].put(overview.entityGuid, integerArrayProperties)
        //assign
        saveContainer.enums[step].put(overview.entityGuid, enumProperties)
        saveContainer.enumCollections[step].put(overview.entityGuid, enumCollectionProperties)
        //assign objects
        saveContainer.objectArrays[step].put(overview.entityGuid, objectArrayProperties)
        saveContainer.objects[step].put(overview.entityGuid, objectProperties)
    }

    /**
     * @param jdsFieldType
     * @param key
     * @param value
     */
    internal fun populateProperties(jdsFieldType: JdsFieldType, key: Long, value: Any?) {
        when (jdsFieldType) {
            JdsFieldType.FLOAT -> floatProperties[key]?.set(value as Float)
            JdsFieldType.INT -> integerProperties[key]?.set(value as Int)
            JdsFieldType.DOUBLE -> doubleProperties[key]?.set(value as Double)
            JdsFieldType.LONG -> longProperties[key]?.set(value as Long)
            JdsFieldType.TEXT -> stringProperties[key]?.set(value as String)
            JdsFieldType.DATE_TIME -> localDateTimeProperties[key]?.set((value as Timestamp).toLocalDateTime())
            JdsFieldType.ARRAY_DOUBLE -> doubleArrayProperties[key]?.get()?.add(value as Double)
            JdsFieldType.ARRAY_FLOAT -> floatArrayProperties[key]?.get()?.add(value as Float)
            JdsFieldType.ARRAY_INT -> integerArrayProperties[key]?.get()?.add(value as Int)
            JdsFieldType.ARRAY_LONG -> longArrayProperties[key]?.get()?.add(value as Long)
            JdsFieldType.ARRAY_TEXT -> stringArrayProperties[key]?.get()?.add(value as String)
            JdsFieldType.ARRAY_DATE_TIME -> dateTimeArrayProperties[key]?.get()?.add((value as Timestamp).toLocalDateTime())
            JdsFieldType.BOOLEAN -> booleanProperties[key]?.set((value as Int) == 1)
            JdsFieldType.ZONED_DATE_TIME -> zonedDateTimeProperties[key]?.set(ZonedDateTime.ofInstant(Instant.ofEpochSecond(value as Long), ZoneId.systemDefault()))
            JdsFieldType.DATE -> localDateProperties[key]?.set((value as Timestamp).toLocalDateTime().toLocalDate())
            JdsFieldType.TIME -> localTimeProperties[key]?.set(LocalTime.ofSecondOfDay((value as Int).toLong()))
            JdsFieldType.BLOB -> blobProperties[key]?.set(value as ByteArray)
            JdsFieldType.ENUM -> enumProperties.filter { it.key.getField().id == key }.forEach { it.value?.set(it.key.valueOf(value as Int)) }
            JdsFieldType.ENUM_COLLECTION -> {
                enumCollectionProperties.filter { it.key.getField().id == key }.forEach {
                    val enumValues = it.key.getEnumType()!!.enumConstants
                    val index = value as Int
                    if (index < enumValues.size) {
                        it.value.get().add(enumValues[index] as Enum<*>)
                        enumCollectionProperties.keys.any { it -> it.getField().id == key }
                    }
                }
            }
        }
    }

    internal fun populateObjects(jdsDb: JdsDb, entityId: Long, entityGuid: String, innerObjects: ConcurrentLinkedQueue<JdsEntity>, entityGuids: HashSet<String>) {
        try {
            val entityClass = jdsDb.getBoundClass(entityId)!!
            if (objectArrayProperties.containsKey(entityId)) {
                val properties = objectArrayProperties[entityId]!!
                val entity = entityClass.newInstance()
                entity.overview.entityGuid = entityGuid
                entityGuids.add(entityGuid)
                properties.get().add(entity)
                innerObjects.add(entity)
            } else if (objectProperties.containsKey(entityId)) {
                val properties = objectProperties[entityId]!!
                val jdsEntity = entityClass!!.newInstance()
                jdsEntity.overview.entityGuid = entityGuid
                entityGuids.add(entityGuid)
                properties.set(jdsEntity)
                innerObjects.add(jdsEntity)
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    /**
     * Binds all the fields attached to an entity, updates the fields dictionary
     *
     * @param connection the SQL connection to use for DB operations
     * @param entityId   the value representing the entity
     * @param fields     the values representing the entity's fields
     */
    internal fun mapClassFields(jdsDb: JdsDb, connection: Connection, entityId: Long) {
        try {
            (if (jdsDb.supportsStatements()) NamedCallableStatement(connection, jdsDb.mapClassFields()) else NamedPreparedStatement(connection, jdsDb.mapClassFields())).use { mapClassFields ->
                (if (jdsDb.supportsStatements()) NamedCallableStatement(connection, jdsDb.mapFieldNames()) else NamedPreparedStatement(connection, jdsDb.mapFieldNames())).use { mapFieldNames ->
                    for ((key, value) in properties) {
                        //1. map this field ID to the entity type
                        mapClassFields.setLong("entityId", entityId)
                        mapClassFields.setLong("fieldId", key)
                        mapClassFields.addBatch()
                        //2. map this field to the field dictionary
                        mapFieldNames.setLong("fieldId", key)
                        mapFieldNames.setString("fieldName", value)
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
     * Binds all the enums attached to an entity
     *
     * @param entityId the value representing the entity
     * @param fields   the entity's enums
     */
    @Synchronized
    internal fun mapClassEnums(jdsDb: JdsDb, connection: Connection, entityId: Long) {
        mapEnumValues(jdsDb, connection, allEnums)
        mapClassEnumsImplementation(jdsDb, connection, entityId, allEnums)
        if (jdsDb.isPrintingOutput)
            System.out.printf("Mapped Enums for Entity[%s]\n", entityId)
    }

    /**
     * Binds all the field types and updates reference tables
     *
     * @param jdsDb the current database implementation
     * @param connection the SQL connection to use for DB operations
     * @param entityId   the value representing the entity
     */
    internal fun mapClassFieldTypes(jdsDb: JdsDb, connection: Connection, entityId: Long) {
        try {
            (if (jdsDb.supportsStatements()) NamedCallableStatement(connection, jdsDb.mapFieldTypes()) else NamedPreparedStatement(connection, jdsDb.mapFieldTypes())).use { mapFieldTypes ->
                for ((key, value) in types) {
                    mapFieldTypes.setLong("typeId", key)
                    mapFieldTypes.setString("typeName", value)
                    mapFieldTypes.addBatch()
                }
                mapFieldTypes.executeBatch()
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

    /**
     * Binds all the enums attached to an entity
     *
     * @param connection the SQL connection to use for DB operations
     * @param entityId   the value representing the entity
     * @param fields     the entity's enums
     */
    @Synchronized
    private fun mapClassEnumsImplementation(jdsDb: JdsDb, connection: Connection, entityId: Long, fields: Set<JdsFieldEnum<*>>) {
        try {
            (if (jdsDb.supportsStatements()) connection.prepareCall(jdsDb.mapClassEnumsImplementation()) else connection.prepareStatement(jdsDb.mapClassEnumsImplementation())).use { statement ->
                for (field in fields) {
                    for (index in 0..field.sequenceValues.size - 1) {
                        statement.setLong(1, entityId)
                        statement.setLong(2, field.getField().id)
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
     * @param fieldEnums the field enum
     */
    @Synchronized
    private fun mapEnumValues(jdsDb: JdsDb, connection: Connection, fieldEnums: Set<JdsFieldEnum<*>>) {
        try {
            (if (jdsDb.supportsStatements()) connection.prepareCall(jdsDb.mapEnumValues()) else connection.prepareStatement(jdsDb.mapEnumValues())).use { statement ->
                for (field in fieldEnums) {
                    for (index in 0 until field.sequenceValues.size) {
                        statement.setLong(1, field.getField().id)
                        statement.setInt(2, index)
                        statement.setString(3, field.sequenceValues[index].toString())
                        statement.addBatch()
                    }
                }
                statement.executeBatch()
            }
        } catch (ex: Exception) {
            ex.printStackTrace(System.err)
        }
    }

}
