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
package io.github.subiyacryolite.jds;

import javafx.beans.property.*;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class simply hides all underlying data structures from the user However,
 * these fields are visible in package class JdsSave
 */
abstract class JdsEntityBase implements Externalizable {

    //field and enum maps
    protected final static HashSet<Long> map = new HashSet<>();
    protected final SimpleObjectProperty<JdsEntityOverview> overview = new SimpleObjectProperty<>(new JdsEntityOverview());
    protected final HashSet<Long> properties = new HashSet<>();
    protected final HashSet<Long> objects = new HashSet<>();
    protected final HashSet<JdsFieldEnum> allEnums = new HashSet<>();
    protected final SimpleStringProperty name = new SimpleStringProperty();
    //strings and localDateTimes
    protected final HashMap<Long, SimpleObjectProperty<Temporal>> localDateTimeProperties = new HashMap<>();
    protected final HashMap<Long, SimpleObjectProperty<Temporal>> zonedDateTimeProperties = new HashMap<>();
    protected final HashMap<Long, SimpleObjectProperty<Temporal>> localDateProperties = new HashMap<>();
    protected final HashMap<Long, SimpleObjectProperty<Temporal>> localTimeProperties = new HashMap<>();
    protected final HashMap<Long, SimpleStringProperty> stringProperties = new HashMap<>();
    //numeric
    protected final HashMap<Long, SimpleFloatProperty> floatProperties = new HashMap<>();
    protected final HashMap<Long, SimpleDoubleProperty> doubleProperties = new HashMap<>();
    protected final HashMap<Long, SimpleBooleanProperty> booleanProperties = new HashMap<>();
    protected final HashMap<Long, SimpleLongProperty> longProperties = new HashMap<>();
    protected final HashMap<Long, SimpleIntegerProperty> integerProperties = new HashMap<>();
    //arrays
    protected final HashMap<Long, SimpleListProperty<JdsEntity>> objectArrayProperties = new HashMap<>();
    protected final HashMap<Long, SimpleListProperty<String>> stringArrayProperties = new HashMap<>();
    protected final HashMap<Long, SimpleListProperty<LocalDateTime>> dateTimeArrayProperties = new HashMap<>();
    protected final HashMap<Long, SimpleListProperty<Float>> floatArrayProperties = new HashMap<>();
    protected final HashMap<Long, SimpleListProperty<Double>> doubleArrayProperties = new HashMap<>();
    protected final HashMap<Long, SimpleListProperty<Long>> longArrayProperties = new HashMap<>();
    protected final HashMap<Long, SimpleListProperty<Integer>> integerArrayProperties = new HashMap<>();
    //enums
    protected final HashMap<JdsFieldEnum, SimpleListProperty<String>> enumProperties = new HashMap<>();
    //objects
    protected final HashMap<Long, SimpleObjectProperty<JdsEntity>> objectProperties = new HashMap<>();
    //blobs
    protected final HashMap<Long, SimpleBlobProperty> blobProperties = new HashMap<>();

    public final JdsEntityOverview getOverview() {
        return this.overview.get();
    }

    @Override
    public void writeExternal(ObjectOutput objectOutputStream) throws IOException {
        //field and enum maps
        objectOutputStream.writeObject(overview.get());
        objectOutputStream.writeObject(map);
        objectOutputStream.writeObject(properties);
        objectOutputStream.writeObject(objects);
        objectOutputStream.writeObject(allEnums);
        objectOutputStream.writeUTF(name.get());
        //strings and localDateTimes
        objectOutputStream.writeObject(serializeTemporal(localDateTimeProperties));
        objectOutputStream.writeObject(serializeTemporal(zonedDateTimeProperties));
        objectOutputStream.writeObject(serializeTemporal(localDateProperties));
        objectOutputStream.writeObject(serializeTemporal(localTimeProperties));
        objectOutputStream.writeObject(serializableString(stringProperties));
        //numeric
        objectOutputStream.writeObject(serializeFloat(floatProperties));
        objectOutputStream.writeObject(serializeDouble(doubleProperties));
        objectOutputStream.writeObject(serializeBoolean(booleanProperties));
        objectOutputStream.writeObject(serializeLong(longProperties));
        objectOutputStream.writeObject(serializeInteger(integerProperties));
        //blobs
        objectOutputStream.writeObject(serializeBlobs(blobProperties));
        //arrays
        objectOutputStream.writeObject(serializeObjects(objectArrayProperties));
        objectOutputStream.writeObject(serializeStrings(stringArrayProperties));
        objectOutputStream.writeObject(serializeDateTimes(dateTimeArrayProperties));
        objectOutputStream.writeObject(serializeFloats(floatArrayProperties));
        objectOutputStream.writeObject(serializeDoubles(doubleArrayProperties));
        objectOutputStream.writeObject(serializeLongs(longArrayProperties));
        objectOutputStream.writeObject(serializeIntegers(integerArrayProperties));
    }

    private Map<Long, SimpleBlobProperty> serializeBlobs(HashMap<Long, SimpleBlobProperty> input) {
        return input.entrySet().parallelStream().collect(Collectors.toMap((entry) -> entry.getKey(), (entry) -> entry.getValue()));
    }

    private Map<Long, List<Integer>> serializeIntegers(HashMap<Long, SimpleListProperty<Integer>> input) {
        return input.entrySet().parallelStream().collect(Collectors.toMap((entry) -> entry.getKey(), (entry) -> new LinkedList<>(entry.getValue().get())));
    }

    private Map<Long, List<Long>> serializeLongs(HashMap<Long, SimpleListProperty<Long>> input) {
        return input.entrySet().parallelStream().collect(Collectors.toMap((entry) -> entry.getKey(), (entry) -> new LinkedList<>(entry.getValue().get())));
    }

    private Map<Long, List<Double>> serializeDoubles(HashMap<Long, SimpleListProperty<Double>> input) {
        return input.entrySet().parallelStream().collect(Collectors.toMap((entry) -> entry.getKey(), (entry) -> new LinkedList<>(entry.getValue().get())));
    }

    private Map<Long, List<Float>> serializeFloats(HashMap<Long, SimpleListProperty<Float>> input) {
        return input.entrySet().parallelStream().collect(Collectors.toMap((entry) -> entry.getKey(), (entry) -> new LinkedList<>(entry.getValue().get())));
    }

    private Map<Long, List<LocalDateTime>> serializeDateTimes(HashMap<Long, SimpleListProperty<LocalDateTime>> input) {
        return input.entrySet().parallelStream().collect(Collectors.toMap((entry) -> entry.getKey(), (entry) -> new LinkedList<>(entry.getValue().get())));
    }

    private Map<Long, List<String>> serializeStrings(HashMap<Long, SimpleListProperty<String>> input) {
        return input.entrySet().parallelStream().collect(Collectors.toMap((entry) -> entry.getKey(), (entry) -> new LinkedList<>(entry.getValue().get())));
    }

    private Map<Long, List<JdsEntity>> serializeObjects(HashMap<Long, SimpleListProperty<JdsEntity>> input) {
        return input.entrySet().parallelStream().collect(Collectors.toMap((entry) -> entry.getKey(), (entry) -> new LinkedList<>(entry.getValue().get())));
    }

    private Map<Long, Float> serializeFloat(HashMap<Long, SimpleFloatProperty> input) {
        return input.entrySet().parallelStream().collect(Collectors.toMap((entry) -> entry.getKey(), (entry) -> entry.getValue().get()));
    }

    private Map<Long, Double> serializeDouble(HashMap<Long, SimpleDoubleProperty> input) {
        return input.entrySet().parallelStream().collect(Collectors.toMap((entry) -> entry.getKey(), (entry) -> entry.getValue().get()));
    }

    private Map<Long, Boolean> serializeBoolean(HashMap<Long, SimpleBooleanProperty> input) {
        return input.entrySet().parallelStream().collect(Collectors.toMap((entry) -> entry.getKey(), (entry) -> entry.getValue().get()));
    }

    private Map<Long, Long> serializeLong(HashMap<Long, SimpleLongProperty> input) {
        return input.entrySet().parallelStream().collect(Collectors.toMap((entry) -> entry.getKey(), (entry) -> entry.getValue().get()));
    }

    private Map<Long, Integer> serializeInteger(HashMap<Long, SimpleIntegerProperty> input) {
        return input.entrySet().parallelStream().collect(Collectors.toMap((entry) -> entry.getKey(), (entry) -> entry.getValue().get()));
    }

    private Map<Long, Temporal> serializeTemporal(final HashMap<Long, SimpleObjectProperty<Temporal>> input) {
        return input.entrySet().parallelStream().collect(Collectors.toMap((entry) -> entry.getKey(), (entry) -> entry.getValue().get()));
    }

    private Map<Long, String> serializableString(final HashMap<Long, SimpleStringProperty> input) {
        return input.entrySet().parallelStream().collect(Collectors.toMap((entry) -> entry.getKey(), (entry) -> entry.getValue().get()));
    }

    @Override
    public void readExternal(ObjectInput objectInputStream) throws IOException, ClassNotFoundException {
        //field and enum maps
        overview.set((JdsEntityOverview) objectInputStream.readObject());
        map.addAll((Set<Long>) objectInputStream.readObject());
        properties.addAll((Set<Long>) objectInputStream.readObject());
        objects.addAll((Set<Long>) objectInputStream.readObject());
        allEnums.addAll((Set<JdsFieldEnum>) objectInputStream.readObject());
        name.set(objectInputStream.readUTF());
        //strings and localDateTimes
        putTemporal(localDateTimeProperties, (Map<Long, Temporal>) objectInputStream.readObject());
        putTemporal(zonedDateTimeProperties, (Map<Long, Temporal>) objectInputStream.readObject());
        putTemporal(localDateProperties, (Map<Long, Temporal>) objectInputStream.readObject());
        putTemporal(localTimeProperties, (Map<Long, Temporal>) objectInputStream.readObject());
        putString(stringProperties, (Map<Long, String>) objectInputStream.readObject());
        //numeric
        putFloat(floatProperties, (Map<Long, Float>) objectInputStream.readObject());
        putDouble(doubleProperties, (Map<Long, Double>) objectInputStream.readObject());
        putBoolean(booleanProperties, (Map<Long, Boolean>) objectInputStream.readObject());
        putLong(longProperties, (Map<Long, Long>) objectInputStream.readObject());
        putInteger(integerProperties, (Map<Long, Integer>) objectInputStream.readObject());
        //blobs
        putBlobs(blobProperties, (Map<Long, SimpleBlobProperty>) objectInputStream.readObject());
        //arrays
        putObjects(objectArrayProperties, (Map<Long, List<JdsEntity>>) objectInputStream.readObject());
        putStrings(stringArrayProperties, (Map<Long, List<String>>) objectInputStream.readObject());
        putDateTimes(dateTimeArrayProperties, (Map<Long, List<LocalDateTime>>) objectInputStream.readObject());
        putFloats(floatArrayProperties, (Map<Long, List<Float>>) objectInputStream.readObject());
        putDoubles(doubleArrayProperties, (Map<Long, List<Double>>) objectInputStream.readObject());
        putLongs(longArrayProperties, (Map<Long, List<Long>>) objectInputStream.readObject());
        putIntegers(integerArrayProperties, (Map<Long, List<Integer>>) objectInputStream.readObject());
    }

    private void putObjects(HashMap<Long, SimpleListProperty<JdsEntity>> destination, Map<Long, List<JdsEntity>> source) {
        source.entrySet().stream().filter((entry) -> (destination.containsKey(entry.getKey()))).forEachOrdered((entry) -> {
            destination.get(entry.getKey()).addAll(entry.getValue());
        });
    }


    private void putStrings(HashMap<Long, SimpleListProperty<String>> destination, Map<Long, List<String>> source) {
        source.entrySet().stream().filter((entry) -> (destination.containsKey(entry.getKey()))).forEachOrdered((entry) -> {
            destination.get(entry.getKey()).addAll(entry.getValue());
        });
    }

    private void putDateTimes(HashMap<Long, SimpleListProperty<LocalDateTime>> destination, Map<Long, List<LocalDateTime>> source) {
        source.entrySet().stream().filter((entry) -> (destination.containsKey(entry.getKey()))).forEachOrdered((entry) -> {
            destination.get(entry.getKey()).addAll(entry.getValue());
        });
    }

    private void putFloats(HashMap<Long, SimpleListProperty<Float>> destination, Map<Long, List<Float>> source) {
        source.entrySet().stream().filter((entry) -> (destination.containsKey(entry.getKey()))).forEachOrdered((entry) -> {
            destination.get(entry.getKey()).addAll(entry.getValue());
        });
    }

    private void putDoubles(HashMap<Long, SimpleListProperty<Double>> destination, Map<Long, List<Double>> source) {
        source.entrySet().stream().filter((entry) -> (destination.containsKey(entry.getKey()))).forEachOrdered((entry) -> {
            destination.get(entry.getKey()).addAll(entry.getValue());
        });
    }

    private void putLongs(HashMap<Long, SimpleListProperty<Long>> destination, Map<Long, List<Long>> source) {
        source.entrySet().stream().filter((entry) -> (destination.containsKey(entry.getKey()))).forEachOrdered((entry) -> {
            destination.get(entry.getKey()).addAll(entry.getValue());
        });
    }

    private void putIntegers(HashMap<Long, SimpleListProperty<Integer>> destination, Map<Long, List<Integer>> source) {
        source.entrySet().stream().filter((entry) -> (destination.containsKey(entry.getKey()))).forEachOrdered((entry) -> {
            destination.get(entry.getKey()).addAll(entry.getValue());
        });
    }

    private void putInteger(HashMap<Long, SimpleIntegerProperty> destination, Map<Long, Integer> source) {
        source.entrySet().stream().filter((entry) -> (destination.containsKey(entry.getKey()))).forEachOrdered((entry) -> {
            destination.get(entry.getKey()).set(entry.getValue());
        });
    }

    private void putBlobs(HashMap<Long, SimpleBlobProperty> destination, Map<Long, SimpleBlobProperty> source) {
        source.entrySet().stream().filter((entry) -> (destination.containsKey(entry.getKey()))).forEachOrdered((entry) -> {
            destination.get(entry.getKey()).set(entry.getValue().get());
        });
    }

    private void putLong(HashMap<Long, SimpleLongProperty> destination, Map<Long, Long> source) {
        source.entrySet().stream().filter((entry) -> (destination.containsKey(entry.getKey()))).forEachOrdered((entry) -> {
            destination.get(entry.getKey()).set(entry.getValue());
        });
    }

    private void putBoolean(HashMap<Long, SimpleBooleanProperty> destination, Map<Long, Boolean> source) {
        source.entrySet().stream().filter((entry) -> (destination.containsKey(entry.getKey()))).forEachOrdered((entry) -> {
            destination.get(entry.getKey()).set(entry.getValue());
        });
    }

    private void putDouble(HashMap<Long, SimpleDoubleProperty> destination, Map<Long, Double> source) {
        source.entrySet().stream().filter((entry) -> (destination.containsKey(entry.getKey()))).forEachOrdered((entry) -> {
            destination.get(entry.getKey()).set(entry.getValue());
        });
    }

    private void putFloat(HashMap<Long, SimpleFloatProperty> destination, Map<Long, Float> source) {
        source.entrySet().stream().filter((entry) -> (destination.containsKey(entry.getKey()))).forEachOrdered((entry) -> {
            destination.get(entry.getKey()).set(entry.getValue());
        });
    }

    private void putTemporal(HashMap<Long, SimpleObjectProperty<Temporal>> destination, Map<Long, Temporal> source) {
        source.entrySet().stream().filter((entry) -> (destination.containsKey(entry.getKey()))).forEachOrdered((entry) -> {
            destination.get(entry.getKey()).set(entry.getValue());
        });
    }

    private void putString(HashMap<Long, SimpleStringProperty> destination, Map<Long, String> source) {
        source.entrySet().stream().filter((entry) -> (destination.containsKey(entry.getKey()))).forEachOrdered((entry) -> {
            destination.get(entry.getKey()).set(entry.getValue());
        });
    }
}
