package io.github.subiyacryolite.jds

import javafx.beans.property.*

import java.time.LocalDateTime
import java.time.temporal.Temporal
import java.util.*

/**
 * Helper class used when performing [JdsEntity] saves
 */
internal class JdsSaveContainer {
    //strings, dates and numeric
    val localDateTimes: MutableList<HashMap<String, HashMap<Long, SimpleObjectProperty<Temporal>>>> = ArrayList()
    val localDates: MutableList<HashMap<String, HashMap<Long, SimpleObjectProperty<Temporal>>>> = ArrayList()
    val localTimes: MutableList<HashMap<String, HashMap<Long, SimpleObjectProperty<Temporal>>>> = ArrayList()
    val zonedDateTimes: MutableList<HashMap<String, HashMap<Long, SimpleObjectProperty<Temporal>>>> = ArrayList()
    val strings: MutableList<HashMap<String, HashMap<Long, SimpleStringProperty>>> = ArrayList()
    val booleans: MutableList<HashMap<String, HashMap<Long, SimpleBooleanProperty>>> = ArrayList()
    val floats: MutableList<HashMap<String, HashMap<Long, SimpleFloatProperty>>> = ArrayList()
    val doubles: MutableList<HashMap<String, HashMap<Long, SimpleDoubleProperty>>> = ArrayList()
    val longs: MutableList<HashMap<String, HashMap<Long, SimpleLongProperty>>> = ArrayList()
    val integers: MutableList<HashMap<String, HashMap<Long, SimpleIntegerProperty>>> = ArrayList()
    val blobs: MutableList<HashMap<String, HashMap<Long, SimpleBlobProperty>>> = ArrayList()
    //arrays
    val objectArrays: MutableList<HashMap<String, HashMap<JdsFieldEntity<*>, SimpleListProperty<JdsEntity>>>> = ArrayList()
    val stringArrays: MutableList<HashMap<String, HashMap<Long, SimpleListProperty<String>>>> = ArrayList()
    val dateTimeArrays: MutableList<HashMap<String, HashMap<Long, SimpleListProperty<LocalDateTime>>>> = ArrayList()
    val floatArrays: MutableList<HashMap<String, HashMap<Long, SimpleListProperty<Float>>>> = ArrayList()
    val doubleArrays: MutableList<HashMap<String, HashMap<Long, SimpleListProperty<Double>>>> = ArrayList()
    val longArrays: MutableList<HashMap<String, HashMap<Long, SimpleListProperty<Long>>>> = ArrayList()
    val integerArrays: MutableList<HashMap<String, HashMap<Long, SimpleListProperty<Int>>>> = ArrayList()
    //enums
    val enums: MutableList<HashMap<String, HashMap<JdsFieldEnum<*>, SimpleObjectProperty<Enum<*>>>>> = ArrayList()
    val enumCollections: MutableList<HashMap<String, HashMap<JdsFieldEnum<*>, SimpleListProperty<Enum<*>>>>> = ArrayList()
    //objects
    val objects: MutableList<HashMap<String, HashMap<JdsFieldEntity<*>, SimpleObjectProperty<JdsEntity>>>> = ArrayList()
    //overviews
    val overviews: MutableList<HashSet<IJdsEntityOverview>> = LinkedList()
}
