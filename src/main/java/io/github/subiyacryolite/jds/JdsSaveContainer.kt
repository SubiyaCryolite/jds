package io.github.subiyacryolite.jds

import javafx.beans.property.*
import java.time.*

import java.time.temporal.Temporal
import java.util.*

/**
 * Helper class used when performing [JdsEntity] saves
 */
internal class JdsSaveContainer {
    //time constructs
    val localDateTimeProperties: MutableList<HashMap<String, HashMap<Long, SimpleObjectProperty<Temporal>>>> = ArrayList()
    val localDateProperties: MutableList<HashMap<String, HashMap<Long, SimpleObjectProperty<Temporal>>>> = ArrayList()
    val localTimeProperties: MutableList<HashMap<String, HashMap<Long, SimpleObjectProperty<Temporal>>>> = ArrayList()
    val zonedDateTimeProperties: MutableList<HashMap<String, HashMap<Long, SimpleObjectProperty<Temporal>>>> = ArrayList()
    val monthDayProperties: MutableList<HashMap<String, HashMap<Long, SimpleObjectProperty<MonthDay>>>> = ArrayList()
    val yearMonthProperties: MutableList<HashMap<String, HashMap<Long, SimpleObjectProperty<Temporal>>>> = ArrayList()
    val periodProperties: MutableList<HashMap<String, HashMap<Long, SimpleObjectProperty<Period>>>> = ArrayList()
    val durationProperties: MutableList<HashMap<String, HashMap<Long, SimpleObjectProperty<Duration>>>> = ArrayList()
    //strings
    val stringProperties: MutableList<HashMap<String, HashMap<Long, SimpleStringProperty>>> = ArrayList()
    //primitives
    val booleanProperties: MutableList<HashMap<String, HashMap<Long, SimpleBooleanProperty>>> = ArrayList()
    val floatProperties: MutableList<HashMap<String, HashMap<Long, SimpleFloatProperty>>> = ArrayList()
    val doubleProperties: MutableList<HashMap<String, HashMap<Long, SimpleDoubleProperty>>> = ArrayList()
    val longProperties: MutableList<HashMap<String, HashMap<Long, SimpleLongProperty>>> = ArrayList()
    val integerProperties: MutableList<HashMap<String, HashMap<Long, SimpleIntegerProperty>>> = ArrayList()
    //blobs
    val blobProperties: MutableList<HashMap<String, HashMap<Long, SimpleBlobProperty>>> = ArrayList()
    //arrays
    val objectCollections: MutableList<HashMap<String, HashMap<JdsFieldEntity<*>, SimpleListProperty<JdsEntity>>>> = ArrayList()
    val stringCollections: MutableList<HashMap<String, HashMap<Long, SimpleListProperty<String>>>> = ArrayList()
    val localDateTimeCollections: MutableList<HashMap<String, HashMap<Long, SimpleListProperty<LocalDateTime>>>> = ArrayList()
    val floatCollections: MutableList<HashMap<String, HashMap<Long, SimpleListProperty<Float>>>> = ArrayList()
    val doubleCollections: MutableList<HashMap<String, HashMap<Long, SimpleListProperty<Double>>>> = ArrayList()
    val longCollections: MutableList<HashMap<String, HashMap<Long, SimpleListProperty<Long>>>> = ArrayList()
    val integerCollections: MutableList<HashMap<String, HashMap<Long, SimpleListProperty<Int>>>> = ArrayList()
    //enumProperties
    val enumProperties: MutableList<HashMap<String, HashMap<JdsFieldEnum<*>, SimpleObjectProperty<Enum<*>>>>> = ArrayList()
    val enumCollections: MutableList<HashMap<String, HashMap<JdsFieldEnum<*>, SimpleListProperty<Enum<*>>>>> = ArrayList()
    //objects
    val objects: MutableList<HashMap<String, HashMap<JdsFieldEntity<*>, SimpleObjectProperty<JdsEntity>>>> = ArrayList()
    //overviews
    val overviews: MutableList<HashSet<IJdsOverview>> = LinkedList()
}
