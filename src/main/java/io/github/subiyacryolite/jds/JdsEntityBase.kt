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

import javafx.beans.property.*
import java.io.Externalizable
import java.io.IOException
import java.io.ObjectInput
import java.io.ObjectOutput
import java.time.LocalDateTime
import java.time.temporal.Temporal
import java.util.*

/**
 * This class simply hides all underlying data structures from the user However,
 * these fields are visible in package class JdsSave
 */
abstract class JdsEntityBase : IJdsEntityBase, Externalizable {

    //field and enum maps
    private val _overview = SimpleObjectProperty<IJdsEntityOverview>(JdsEntityOverview())
    override val overview: IJdsEntityOverview
        get() = _overview.get();

    internal val properties: MutableMap<Long, String> = HashMap()
    internal val types: MutableMap<Long, String> = HashMap()
    internal val objects: MutableSet<Long> = HashSet()
    internal val allEnums: MutableSet<JdsFieldEnum<*>> = HashSet()
    protected val name = SimpleStringProperty()
    //strings and localDateTimes
    internal val localDateTimeProperties: HashMap<Long, SimpleObjectProperty<Temporal>> = HashMap()
    internal val zonedDateTimeProperties: HashMap<Long, SimpleObjectProperty<Temporal>> = HashMap()
    internal val localDateProperties: HashMap<Long, SimpleObjectProperty<Temporal>> = HashMap()
    internal val localTimeProperties: HashMap<Long, SimpleObjectProperty<Temporal>> = HashMap()
    internal val stringProperties: HashMap<Long, SimpleStringProperty> = HashMap()
    //numeric
    internal val floatProperties: HashMap<Long, SimpleFloatProperty> = HashMap()
    internal val doubleProperties: HashMap<Long, SimpleDoubleProperty> = HashMap()
    internal val booleanProperties: HashMap<Long, SimpleBooleanProperty> = HashMap()
    internal val longProperties: HashMap<Long, SimpleLongProperty> = HashMap()
    internal val integerProperties: HashMap<Long, SimpleIntegerProperty> = HashMap()
    //arrays
    internal val objectArrayProperties: HashMap<Long, SimpleListProperty<JdsEntity>> = HashMap()
    internal val stringArrayProperties: HashMap<Long, SimpleListProperty<String>> = HashMap()
    internal val dateTimeArrayProperties: HashMap<Long, SimpleListProperty<LocalDateTime>> = HashMap()
    internal val floatArrayProperties: HashMap<Long, SimpleListProperty<Float>> = HashMap()
    internal val doubleArrayProperties: HashMap<Long, SimpleListProperty<Double>> = HashMap()
    internal val longArrayProperties: HashMap<Long, SimpleListProperty<Long>> = HashMap()
    internal val integerArrayProperties: HashMap<Long, SimpleListProperty<Int>> = HashMap()
    //enums
    internal val enumProperties: HashMap<JdsFieldEnum<*>, SimpleObjectProperty<Enum<*>>> = HashMap()
    internal val enumCollectionProperties: HashMap<JdsFieldEnum<*>, SimpleListProperty<Enum<*>>> = HashMap()
    //objects
    internal val objectProperties: HashMap<Long, SimpleObjectProperty<JdsEntity>> = HashMap()
    internal val objectCascade: HashMap<Long, Boolean> = HashMap()
    //blobs
    internal val blobProperties: HashMap<Long, SimpleBlobProperty> = HashMap()


    @Throws(IOException::class)
    override fun writeExternal(objectOutputStream: ObjectOutput) {
        //field and enum maps
        objectOutputStream.writeObject(_overview.get())
        objectOutputStream.writeObject(properties)
        objectOutputStream.writeObject(types)
        objectOutputStream.writeObject(objects)
        objectOutputStream.writeObject(allEnums)
        objectOutputStream.writeUTF(name.get())
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
        return input.entries.associateBy ({ it.key }, { it.value.get() })
    }

    private fun serializeEnumCollections(input: Map<JdsFieldEnum<*>, SimpleListProperty<Enum<*>>>): Map<JdsFieldEnum<*>, List<Enum<*>>> {
        return input.entries.associateBy ({ it.key }, { it.value.get() })
    }

    private fun serializeBlobs(input: Map<Long, SimpleBlobProperty>): Map<Long, SimpleBlobProperty> {
        return input.entries.associateBy ({ it.key }, { it.value })
    }

    private fun serializeIntegers(input: Map<Long, SimpleListProperty<Int>>): Map<Long, List<Int>> {
        return input.entries.associateBy ({ it.key }, { it.value.get() })
    }

    private fun serializeLongs(input: Map<Long, SimpleListProperty<Long>>): Map<Long, List<Long>> {
        return input.entries.associateBy ({ it.key }, { it.value.get() })
    }

    private fun serializeDoubles(input: Map<Long, SimpleListProperty<Double>>): Map<Long, List<Double>> {
        return input.entries.associateBy ({ it.key }, { it.value.get() })
    }

    private fun serializeFloats(input: Map<Long, SimpleListProperty<Float>>): Map<Long, List<Float>> {
        return input.entries.associateBy ({ it.key }, { it.value.get() })
    }

    private fun serializeDateTimes(input: Map<Long, SimpleListProperty<LocalDateTime>>): Map<Long, List<LocalDateTime>> {
        return input.entries.associateBy ({ it.key }, { it.value.get() })
    }

    private fun serializeStrings(input: Map<Long, SimpleListProperty<String>>): Map<Long, List<String>> {
        return input.entries.associateBy ({ it.key }, { it.value.get() })
    }

    private fun serializeObjects(input: Map<Long, SimpleListProperty<JdsEntity>>): Map<Long, List<JdsEntity>> {
        return input.entries.associateBy ({ it.key }, { it.value.get() })
    }

    private fun serializeObject(input: Map<Long, SimpleObjectProperty<JdsEntity>>): Map<Long, JdsEntity> {
        return input.entries.associateBy ({ it.key }, { it.value.get() })
    }

    private fun serializeFloat(input: Map<Long, SimpleFloatProperty>): Map<Long, Float> {
        return input.entries.associateBy ({ it.key }, { it.value.get() })
    }

    private fun serializeDouble(input: Map<Long, SimpleDoubleProperty>): Map<Long, Double> {
        return input.entries.associateBy ({ it.key }, { it.value.get() })
    }

    private fun serializeBoolean(input: Map<Long, SimpleBooleanProperty>): Map<Long, Boolean> {
        return input.entries.associateBy ({ it.key }, { it.value.get() })
    }

    private fun serializeLong(input: Map<Long, SimpleLongProperty>): Map<Long, Long> {
        return input.entries.associateBy ({ it.key }, { it.value.get() })
    }

    private fun serializeInteger(input: Map<Long, SimpleIntegerProperty>): Map<Long, Int> {
        return input.entries.associateBy ({ it.key }, { it.value.get() })
    }

    private fun serializeTemporal(input: Map<Long, SimpleObjectProperty<out Temporal>>): Map<Long, Temporal> {
        return input.entries.associateBy ({ it.key }, { it.value.get() })
    }

    private fun serializableString(input: Map<Long, SimpleStringProperty>): Map<Long, String> {
        return input.entries.associateBy ({ it.key }, { it.value.get() })
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    override fun readExternal(objectInputStream: ObjectInput) {
        //field and enum maps
        _overview.set(objectInputStream.readObject() as JdsEntityOverview)
        properties.putAll(objectInputStream.readObject() as Map<Long, String>)
        types.putAll(objectInputStream.readObject() as Map<Long, String>)
        objects.addAll(objectInputStream.readObject() as Set<Long>)
        allEnums.addAll(objectInputStream.readObject() as Set<JdsFieldEnum<*>>)
        name.set(objectInputStream.readUTF())
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
}