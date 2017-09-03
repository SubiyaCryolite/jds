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

import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation
import io.github.subiyacryolite.jds.enums.JdsFieldType
import javafx.beans.property.*

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.temporal.Temporal

/**
 * This class allows for all mapping operations in JDS, it also uses
 * [JdsEntityBase] to store overview data
 */
abstract class JdsEntity : JdsEntityBase(), IJdsEntity {
    /**
     *
     */
    init {
        if (javaClass.isAnnotationPresent(JdsEntityAnnotation::class.java)) {
            val entityAnnotation = javaClass.getAnnotation(JdsEntityAnnotation::class.java)
            overview.entityId = entityAnnotation.entityId
            setEntityName(entityAnnotation.entityName)
        } else {
            throw RuntimeException("You must annotate the class [" + javaClass.canonicalName + "] with [" + JdsEntityAnnotation::class.java + "]")
        }
    }

    /**
     * @return
     */
    override fun getEntityName(): String {
        return name.get()
    }

    /**
     * @param name
     */
    override fun setEntityName(name: String) {
        this.name.set(name)
    }

    /**
     * @param jdsField
     * @param integerProperty
     */
    protected fun map(jdsField: JdsField, integerProperty: SimpleBlobProperty) {
        if (integerProperty == null) {
            return
        }
        if (jdsField.type === JdsFieldType.BLOB) {
            properties.put(jdsField.id, jdsField.name)
            types.put(jdsField.id, jdsField.type!!.toString())
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
        if (integerProperty == null) {
            return
        }
        if (jdsField.type === JdsFieldType.INT) {
            properties.put(jdsField.id, jdsField.name)
            types.put(jdsField.id, jdsField.type!!.toString())
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
        if (temporalProperty == null) {
            return
        }
        val temporal = temporalProperty.get()
        if (temporal is LocalDateTime) {
            if (jdsField.type === JdsFieldType.DATE_TIME) {
                properties.put(jdsField.id, jdsField.name)
                types.put(jdsField.id, jdsField.type!!.toString())
                localDateTimeProperties.put(jdsField.id, temporalProperty as SimpleObjectProperty<Temporal>)
            } else {
                throw RuntimeException("Please prepareDatabaseComponents jdsField [$jdsField] to the correct type")
            }
        } else if (temporal is ZonedDateTime) {
            if (jdsField.type === JdsFieldType.ZONED_DATE_TIME) {
                properties.put(jdsField.id, jdsField.name)
                types.put(jdsField.id, jdsField.type!!.toString())
                zonedDateTimeProperties.put(jdsField.id, temporalProperty as SimpleObjectProperty<Temporal>)
            } else {
                throw RuntimeException("Please prepareDatabaseComponents jdsField [$jdsField] to the correct type")
            }
        } else if (temporal is LocalDate) {
            if (jdsField.type === JdsFieldType.DATE) {
                properties.put(jdsField.id, jdsField.name)
                types.put(jdsField.id, jdsField.type!!.toString())
                localDateProperties.put(jdsField.id, temporalProperty as SimpleObjectProperty<Temporal>)
            } else {
                throw RuntimeException("Please prepareDatabaseComponents jdsField [$jdsField] to the correct type")
            }
        } else if (temporal is LocalTime) {
            if (jdsField.type === JdsFieldType.TIME) {
                properties.put(jdsField.id, jdsField.name)
                types.put(jdsField.id, jdsField.type!!.toString())
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
        if (stringProperty == null) {
            return
        }
        if (jdsField.type === JdsFieldType.TEXT) {
            properties.put(jdsField.id, jdsField.name)
            types.put(jdsField.id, jdsField.type!!.toString())
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
        if (floatProperty == null) {
            return
        }
        if (jdsField.type === JdsFieldType.FLOAT) {
            properties.put(jdsField.id, jdsField.name)
            types.put(jdsField.id, jdsField.type!!.toString())
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
        if (longProperty == null) {
            return
        }
        if (jdsField.type === JdsFieldType.LONG) {
            properties.put(jdsField.id, jdsField.name)
            types.put(jdsField.id, jdsField.type!!.toString())
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
        if (doubleProperty == null) {
            return
        }
        if (jdsField.type === JdsFieldType.DOUBLE) {
            properties.put(jdsField.id, jdsField.name)
            types.put(jdsField.id, jdsField.type!!.toString())
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
        if (booleanProperty == null) {
            return
        }
        if (jdsField.type === JdsFieldType.BOOLEAN) {
            properties.put(jdsField.id, jdsField.name)
            types.put(jdsField.id, jdsField.type!!.toString())
            booleanProperties.put(jdsField.id, booleanProperty)
        } else {
            throw RuntimeException("Please prepareDatabaseComponents jdsField [$jdsField] to the correct type")
        }
    }

    /**
     * @param jdsField
     * @param strings
     */
    protected fun mapStrings(jdsField: JdsField, strings: SimpleListProperty<String>?) {
        if (strings == null) {
            return
        }
        if (jdsField.type === JdsFieldType.ARRAY_TEXT) {
            properties.put(jdsField.id, jdsField.name)
            types.put(jdsField.id, jdsField.type!!.toString())
            stringArrayProperties.put(jdsField.id, strings)
        } else {
            throw RuntimeException("Please prepareDatabaseComponents jdsField [$jdsField] to the correct type")
        }
    }

    /**
     * @param jdsField
     * @param floats
     */
    protected fun mapFloats(jdsField: JdsField, floats: SimpleListProperty<Float>?) {
        if (floats == null) {
            return
        }
        if (jdsField.type === JdsFieldType.ARRAY_FLOAT) {
            properties.put(jdsField.id, jdsField.name)
            types.put(jdsField.id, jdsField.type!!.toString())
            floatArrayProperties.put(jdsField.id, floats)
        } else {
            throw RuntimeException("Please prepareDatabaseComponents jdsField [$jdsField] to the correct type")
        }
    }

    /**
     * @param jdsField
     * @param doubles
     */
    protected fun mapDoubles(jdsField: JdsField, doubles: SimpleListProperty<Double>?) {
        if (doubles == null) {
            return
        }
        if (jdsField.type === JdsFieldType.ARRAY_DOUBLE) {
            properties.put(jdsField.id, jdsField.name)
            types.put(jdsField.id, jdsField.type!!.toString())
            doubleArrayProperties.put(jdsField.id, doubles)
        } else {
            throw RuntimeException("Please prepareDatabaseComponents jdsField [$jdsField] to the correct type")
        }
    }

    /**
     * @param jdsField
     * @param longs
     */
    protected fun mapLongs(jdsField: JdsField, longs: SimpleListProperty<Long>?) {
        if (longs == null) {
            return
        }
        if (jdsField.type === JdsFieldType.ARRAY_LONG) {
            properties.put(jdsField.id, jdsField.name)
            types.put(jdsField.id, jdsField.type!!.toString())
            longArrayProperties.put(jdsField.id, longs)
        } else {
            throw RuntimeException("Please prepareDatabaseComponents jdsField [$jdsField] to the correct type")
        }
    }

    /**
     * @param jdsFieldEnum
     * @param enums
     */
    protected fun map(jdsFieldEnum: JdsFieldEnum<*>, enums: SimpleObjectProperty<out Enum<*>>?) {
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
    protected fun mapEnums(jdsFieldEnum: JdsFieldEnum<*>, enums: SimpleListProperty<out Enum<*>>?) {
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
}
