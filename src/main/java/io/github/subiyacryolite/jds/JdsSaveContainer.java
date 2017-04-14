package io.github.subiyacryolite.jds;

import javafx.beans.property.*;

import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.*;

/**
 * Helper class used when performing {@link JdsEntity JdsEntity} saves
 */
class JdsSaveContainer {
    //strings, dates and numeric
    public final List<Map<String, Map<Long, SimpleObjectProperty<Temporal>>>> localDateTimes = new ArrayList<>();
    public final List<Map<String, Map<Long, SimpleObjectProperty<Temporal>>>> localDates = new ArrayList<>();
    public final List<Map<String, Map<Long, SimpleObjectProperty<Temporal>>>> localTimes = new ArrayList<>();
    public final List<Map<String, Map<Long, SimpleObjectProperty<Temporal>>>> zonedDateTimes = new ArrayList<>();
    public final List<Map<String, Map<Long, SimpleStringProperty>>> strings = new ArrayList<>();
    public final List<Map<String, Map<Long, SimpleBooleanProperty>>> booleans = new ArrayList<>();
    public final List<Map<String, Map<Long, SimpleFloatProperty>>> floats = new ArrayList<>();
    public final List<Map<String, Map<Long, SimpleDoubleProperty>>> doubles = new ArrayList<>();
    public final List<Map<String, Map<Long, SimpleLongProperty>>> longs = new ArrayList<>();
    public final List<Map<String, Map<Long, SimpleIntegerProperty>>> integers = new ArrayList<>();
    //arrays
    public final List<Map<String, Map<Long, SimpleListProperty<? extends JdsEntity>>>> objectArrays = new ArrayList<>();
    public final List<Map<String, Map<Long, SimpleListProperty<String>>>> stringArrays = new ArrayList<>();
    public final List<Map<String, Map<Long, SimpleListProperty<LocalDateTime>>>> dateTimeArrays = new ArrayList<>();
    public final List<Map<String, Map<Long, SimpleListProperty<Float>>>> floatArrays = new ArrayList<>();
    public final List<Map<String, Map<Long, SimpleListProperty<Double>>>> doubleArrays = new ArrayList<>();
    public final List<Map<String, Map<Long, SimpleListProperty<Long>>>> longArrays = new ArrayList<>();
    public final List<Map<String, Map<Long, SimpleListProperty<Integer>>>> integerArrays = new ArrayList<>();
    //enums
    public final List<Map<String, Map<JdsFieldEnum, SimpleListProperty<String>>>> enums = new ArrayList<>();
    //objects
    public final List<Map<String, Map<Long, SimpleObjectProperty<? extends JdsEntity>>>> objects = new ArrayList<>();
    //overviews
    public final List<HashSet<JdsEntityOverview>> overviews = new LinkedList<>();
}
