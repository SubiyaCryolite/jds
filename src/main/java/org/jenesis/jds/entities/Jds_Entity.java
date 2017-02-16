package org.jenesis.jds.entities;

import javafx.beans.property.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by ifunga on 13/02/2017.
 * This class simply hides all underlying data structures from the user
 * However, these fields are visible in package class JdsSave
 */
abstract class Jds_Entity {
    protected final SimpleObjectProperty<JdsEntityOverview> overview = new SimpleObjectProperty<>(new JdsEntityOverview());
    //field and enum maps
    protected final static HashSet<Long> map = new HashSet<>();
    protected final HashSet<Long> allFields = new HashSet<>();
    protected final HashSet<JdsFieldEnum> allEnums = new HashSet<>();
    //stringProperties and dateProperties
    protected final HashMap<Long, SimpleObjectProperty<LocalDateTime>> dateProperties = new HashMap<>();
    protected final HashMap<Long, SimpleStringProperty> stringProperties = new HashMap<>();
    //numeric
    protected final HashMap<Long, SimpleFloatProperty> floatProperties = new HashMap<>();
    protected final HashMap<Long, SimpleDoubleProperty> doubleProperties = new HashMap<>();
    protected final HashMap<Long, SimpleLongProperty> longProperties = new HashMap<>();
    protected final HashMap<Long, SimpleIntegerProperty> integerProperties = new HashMap<>();
    //arrays
    protected final HashMap<Long, SimpleListProperty<? extends JdsEntity>> objectArrayProperties = new HashMap<>();
    protected final HashMap<Long, SimpleListProperty<String>> stringArrayProperties = new HashMap<>();
    protected final HashMap<Long, SimpleListProperty<LocalDateTime>> jdsArrayDates = new HashMap<>();
    protected final HashMap<Long, SimpleListProperty<Float>> jdsArrayFloats = new HashMap<>();
    protected final HashMap<Long, SimpleListProperty<Double>> jdsArrayDoubles = new HashMap<>();
    protected final HashMap<Long, SimpleListProperty<Long>> jdsArrayLongs = new HashMap<>();
    protected final HashMap<Long, SimpleListProperty<Integer>> jdsArrayIntegers = new HashMap<>();
    //enums
    protected final HashMap<JdsFieldEnum, SimpleListProperty<String>> enumProperties = new HashMap<>();
    //objectProperties
    protected final HashMap<Long, SimpleObjectProperty<? extends JdsEntity>> objectProperties = new HashMap<>();

    public final JdsEntityOverview getOverview()
    {
        return this.overview.get();
    }
}
