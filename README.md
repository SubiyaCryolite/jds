[![Build Status](https://travis-ci.org/SubiyaCryolite/jds.svg?branch=master)](https://travis-ci.org/SubiyaCryolite/Jenesis-Data-Store)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.subiyacryolite/jds/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.subiyacryolite/jds)
[![Javadocs](https://javadoc.io/badge/io.github.subiyacryolite/jds.svg)](https://javadoc.io/doc/io.github.subiyacryolite/jds)
[![License](https://img.shields.io/badge/License-BSD--3--Clause-blue.svg)](LICENSE.md)
![Size](https://reposs.herokuapp.com/?path=SubiyaCryolite/jds)

# Jenesis Data Store
Jenesis Data Store (JDS) was created to help developers persist their classes to relational databases in a fast and reliable manner, without requiring them to design elaborate relational schemas. The aim of JDS is to allow for the rapid creation and modification of java classes in order to facilitate rapid prototyping and quick development.

The library eliminates the need to modify schemas once a class has been altered. It also eliminates all concerns regarding "breaking changes" in regards to fieldIds and their values. Fields, Objects and ArrayTypes can be added, modified or removed at will. Beyond that the libraries data is structured in a way to promote fast and efficient Data Mining queries that can be used to support the application in question or to feed into specialised analytic software.

Put simply, JDS is useful for any developer that requires a flexible schema running on top of a traditional Relational Database. Some use-cases include:
- Rapid prototyping
- Academic projects
- Single or Multi-user applications
- Self hosted REST/SOAP services

JDS is licensed under the [3-Clause BSD License](https://opensource.org/licenses/BSD-3-Clause)

# Design
The concept behind JDS is quite simple. Extend a base “Entity” class, define “Fields” of a particular data-type and lastly “Map” the said fieldIds to a JavaFX Bean.

## Features
- Transparent persistence
- Serialization of JavaFX bean *values*
- Full support for generics and inheritance
- Easily integrates with new or existing databases
- Save, Updates and Deletes cascade to child objects and collections
- All saves and deletes are ACID (transaction based)
- Eager Loading is applied to embedded objects as well as on collections
- Supports the automatic generation of tables
- Supports MySQL, T-SQL, PostgreSQL, Oracle 11G and SQLite
- Underlying database implemented using the Star Schema

# Maven Central
You can search on The Central Repository with GroupId and ArtifactId Maven Search for [![Maven Search](https://img.shields.io/badge/io.github.subiyacryolite-jds-blue.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.subiyacryolite/jds)

Maven
```xml
<dependency>
    <groupId>io.github.subiyacryolite</groupId>
    <artifactId>jds</artifactId>
    <version>3.4.3</version>
</dependency>
```
Gradle
```groovy
compile 'io.github.subiyacryolite:jds:3.4.3'
```

# Dependencies
The library depends on Java 8. Both 64 and 32 bit variants should suffice. Both the Development Kit and Runtime can be downloaded from [here](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html).

# Supported Databases
The API currently supports the following Relational Databases, each of which has their own dependencies, versions and licensing requirements. Please consult the official sites for specifics.

|Database|Version Tested Against|Official Site|JDBC Driver Tested Against
|-------|-----------|-----------|-----------|
| PostgreSQL            | 9.5         | [Official Site](https://www.postgresql.org/)        | [org.postgresql](https://mvnrepository.com/artifact/org.postgresql/postgresql)|
| MySQL            |5.7.14         | [Official Site](https://www.mysql.com/downloads/)        | [com.mysql.cj.jdbc.Driver](https://mvnrepository.com/artifact/mysql/mysql-connector-java)|
| Microsoft SQL Server | 2008 R2     | [Official Site](https://www.microsoft.com/en-us/sql-server/sql-server-downloads)        | [com.microsoft.sqlserver](https://mvnrepository.com/artifact/com.microsoft.sqlserver/sqljdbc4)|
| SQLite            | 3.16.1   | [Official Site](https://www.sqlite.org/)    | [org.sqlite.JDBC](https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc)|
| Oracle            | 11g Release 2   | [Official Site](http://www.oracle.com/technetwork/database/database-technologies/express-edition/overview/index.html)    | [oracle.jdbc.driver.OracleDriver](https://mvnrepository.com/artifact/com.oracle/ojdbc6/12.1.0.1-atlassian-hosted)|

# 1 How it works

## 1.1 Creating Classes
Classes that use JDS need to extend JdsEntity.
```java
import io.github.subiyacryolite.jds.JdsEntity;

public class Customer extends JdsEntity
```

However, if you plan on using interfaces they must extend IJdsEntity. Concrete classes can then extend JdsEntity

```java
import io.github.subiyacryolite.jds.JdsEntity;
import io.github.subiyacryolite.jds.IJdsEntity;

public interface ICustomer extends IJdsEntity
public class Customer extends JdsEntity implements ICustomer
```

Following that the following steps need to be taken.

### 1.1.1 Annotating Classes
Every class that extends JdsEntity must have its own unique Entity Id as well as Entity Name. This is done by annotating the class in the following manner
```java
@JdsEntityAnnotation(entityId = 5, entityName = "Customer")
public class Customer extends JdsEntity
```
Entity IDs MUST be unique in your application, any value of type long is valid. Entity Names do not enforce unique constraints but its best to use a unique name regardless. These values can be referenced to mine data.

### 1.1.2 Defining Fields
JdsFields are big part of the JDS framework. Each fieldEntity MUST have a unique Field Id. Field Names do not enforce unique constraints but its best to use a unique name regardless. These values can be referenced to mine data. Every fieldEntity that you define can be one of the following types.

|JDS Field Type|Java Type|Description|
|-----|-----|-----|
|DATE_TIME_COLLECTION|List\<LocalDateTime\>|List of type LocalDateTime|
|DOUBLE_COLLECTION|List\<Double\>|List of type Double|
|ENTITY_COLLECTION|List\<Class\<? extends JdsEntity\>\>|List of type JdsEntity|
|FLOAT_COLLECTION|List\<Float\>|List of type Float|
|INT_COLLECTION|List\<Integer\>|List of type Integer|
|LONG_COLLECTION|List\<Long\>|List of type Long|
|STRING_COLLECTION|List\<String\>|List of type String|
|BLOB|byte[] or InputStream|Blob values|
|BOOLEAN|boolean|Boolean values|
|ENTITY|Class\<? extends JdsEntity\>|Object of type JdsEntity|
|DATE_TIME|LocalDateTime|DateTime instances based on the host machines local timezone|
|DATE|LocalDate|Local date instances|
|DOUBLE|double|Numeric double values|
|DURATION|Duration|Object of type Duration|
|ENUM_COLLECTION|List\<Enum\>|Lists of type Enum|
|ENUM|Enum|Object of type Enum|
|FLOAT|float|Numeric float values|
|INT|int|Numeric integer values|
|LONG|long|Numeric long values|
|MONTH_DAY|MonthDay|Object of type MonthDay|
|PERIOD|Period|Object of type Period|
|STRING|String|String values with no max limit|
|TIME|LocalTime|Local time instances|
|YEAR_MONTH|YearMonth|Object of type YearMonth|
|ZONED_DATE_TIME|ZonedDateTime|Zoned DateTime instances|

I recommend defining your fieldIds as static constants

```java
import io.github.subiyacryolite.jds.JdsField;
import io.github.subiyacryolite.jds.enumProperties.JdsFieldType;

public class Fields {
    public static final JdsField STRING_FIELD = new JdsField(1000, "string_field", JdsFieldType.STRING, "Optional description");
    public static final JdsField TIME_FIELD = new JdsField(1009, "time_field", JdsFieldType.TIME, "Optional description");
    public static final JdsField DATE_FIELD = new JdsField(1001, "date_field", JdsFieldType.DATE);
    public static final JdsField DATE_TIME_FIELD = new JdsField(1002, "date_time_field", JdsFieldType.DATE_TIME);
    public static final JdsField ZONED_DATE_TIME_FIELD = new JdsField(1003, "zoned_date_time_field", JdsFieldType.ZONED_DATE_TIME);
    public static final JdsField LONG_FIELD = new JdsField(1004, "long_field", JdsFieldType.LONG);
    public static final JdsField INT_FIELD = new JdsField(1005, "int_field", JdsFieldType.INT);
    public static final JdsField DOUBLE_FIELD = new JdsField(1006, "double_field", JdsFieldType.DOUBLE);
    public static final JdsField FLOAT_FIELD = new JdsField(1007, "float_field", JdsFieldType.FLOAT);
    public static final JdsField BOOLEAN_FIELD = new JdsField(1008, "boolean_field", JdsFieldType.BOOLEAN);
    public static final JdsField BLOB_FIELD = new JdsField(1010, "blob_field", JdsFieldType.BLOB);
    //=============================================================================================
    public static final JdsField STREET_NAME = new JdsField(1, "street_name", JdsFieldType.STRING);
    public static final JdsField PLOT_NUMBER = new JdsField(2, "plot_number", JdsFieldType.INT);
    public static final JdsField AREA_NAME = new JdsField(3, "area_name", JdsFieldType.STRING);
    public static final JdsField PROVINCE_NAME = new JdsField(4, "province_name", JdsFieldType.STRING);
    public static final JdsField CITY_NAME = new JdsField(5, "city_name", JdsFieldType.STRING);
    public static final JdsField SEX_ENUM = new JdsField(6, "sex_enum", JdsFieldType.ENUM);
    public static final JdsField COUNTRY_NAME = new JdsField(7, "country_name", JdsFieldType.STRING);
    public static final JdsField PRIMARY_ADDRESS_ENUM = new JdsField(8, "primary_address_enum", JdsFieldType.ENUM);
    public static final JdsField LOCAL_DATE_OF_REGISTRATION = new JdsField(9, "local_date_of_registration", JdsFieldType.DATE_TIME);
    public static final JdsField ZONED_DATE_OF_REGISTRATION = new JdsField(10, "zoned_date_of_registration", JdsFieldType.ZONED_DATE_TIME);        
}

```

### 1.1.3 Defining Enums
JdsEnums are an extension of fieldIds. However, they are designed for cases where one or more constant values are required. Usually these values would be represented by CheckBoxes, RadioButtons or Combo Boxes in a UI. In this example we will define Sex as an enumerated value with the following options (Male, Female, Other).
First of all we'd have to define a standard fieldEntity of type ENUM.
```java
public class Fields
{
    //---
    public static final JdsField SEX_ENUM = new JdsField(6, "sex_enum", JdsFieldType.ENUM);
}
```
Then, we can define our actual enum in the following manner.
```java
public enum Sex
{
    MALE,
    FEMALE
}


public class Enums
{
    public final static JdsFieldEnum SEX_ENUMS = new JdsFieldEnum(Sex.class, Fields.SEX_ENUM, Sex.values());
}
```
Behind the scenes these enumProperties will be stored as either an Integer (ENUM) or an Integer Array (ENUM_COLLECTION).

### 1.1.4 Binding Properties
Depending on the type of fieldEntity, JDS will require that you set you objects properties to one of the following JavaFX bean container types.

Kindly note that none of the JavaFX beans are serializable, however JDS supports serialization via the explicit implementation of Javas Externalizable interface. That said only Property values are serialized, not listeners or their states.

|JDS Field Type|Container|Mapping Call|
|-----|-----|-----|
|DATE_TIME_COLLECTION|[ListProperty\<LocalDateTime\>](https://docs.oracle.com/javafx/2/api/javafx/beans/property/ListProperty.html)|mapDateTimes|
|DOUBLE_COLLECTION|[ListProperty\<Double\>](https://docs.oracle.com/javafx/2/api/javafx/beans/property/ListProperty.html)|mapDoubles|
|ENTITY_COLLECTION|[ListProperty\<Class\<? extends JdsEntity\>\>](https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html)|map|
|FLOAT_COLLECTION|[ListProperty\<Float\>](https://docs.oracle.com/javafx/2/api/javafx/beans/property/ListProperty.html)|mapFloats|
|INT_COLLECTION|[ListProperty\<Integer\>](https://docs.oracle.com/javafx/2/api/javafx/beans/property/ListProperty.html)|mapIntegers|
|LONG_COLLECTION|[ListProperty\<Long\>](https://docs.oracle.com/javafx/2/api/javafx/beans/property/ListProperty.html)|mapLongs|
|STRING_COLLECTION|[ListProperty\<String\>](https://docs.oracle.com/javafx/2/api/javafx/beans/property/ListProperty.html)|mapStrings|
|BOOLEAN|[BooleanProperty](https://docs.oracle.com/javafx/2/api/javafx/beans/property/BooleanProperty.html)|map|
|BLOB|[BlobProperty](https://static.javadoc.io/io.github.subiyacryolite/jds/3.4.3/javafx/beans/property/BlobProperty.html)|map|
|ENTITY|[Class\<? extends JdsEntity\>](https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html)|map|
|DATE|[ObjectProperty\<LocalDate\>](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)|map|
|DATE_TIME|[ObjectProperty\<LocalDateTime\>](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDateTime.html)|map|
|DOUBLE|[DoubleProperty](https://docs.oracle.com/javafx/2/api/javafx/beans/property/DoubleProperty.html)|map|
|DURATION|[ObjectProperty\<Duration\>](https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html)|mapDuration|
|ENUM|[ObjectProperty\<Enum\>](https://docs.oracle.com/javase/8/docs/api/java/lang/Enum.html)|map|
|ENUM_COLLECTION|[ListProperty\<Enum\>](https://docs.oracle.com/javafx/2/api/javafx/beans/property/ListProperty.html)|mapEnums|
|FLOAT|[FloatProperty](https://docs.oracle.com/javafx/2/api/javafx/beans/property/FloatProperty.html)|map|
|INT|[IntegerProperty](https://docs.oracle.com/javafx/2/api/javafx/beans/property/IntegerProperty.html)|map|
|LONG|[LongProperty](https://docs.oracle.com/javafx/2/api/javafx/beans/property/LongProperty.html)|map|
|MONTH_DAY|[ObjectProperty\<MonthDay\>](https://docs.oracle.com/javase/8/docs/api/java/time/MonthDay.html)|mapMonthDay|
|PERIOD|[ObjectProperty\<Period\>](https://docs.oracle.com/javase/8/docs/api/java/time/Period.html)|mapPeriod|
|STRING|[StringProperty](https://docs.oracle.com/javafx/2/api/javafx/beans/property/StringProperty.html)|map|
|TIME|[ObjectProperty\<LocalTime\>](https://docs.oracle.com/javase/8/docs/api/java/time/LocalTime.html)|map|
|YEAR_MONTH|[ObjectProperty\<YearMonth\>](https://docs.oracle.com/javase/8/docs/api/java/time/YearMonth.html)|map|
|ZONED_DATE_TIME|[ObjectProperty\<ZonedDateTime\>](https://docs.oracle.com/javase/8/docs/api/java/time/ZonedDateTime.html)|map|

 After your class and its properties have been defined you must map the property to its corresponding fieldEntity using the **map()** method. I recommend doing this in your constructor. 
 
 The example below shows a class definition with valid properties and bindings. With this your class can be persisted.


```java
import io.github.subiyacryolite.jds.JdsEntity;
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation;
import io.github.subiyacryolite.jds.events.*;
import javafx.beans.property.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;

import static constants.Fields.*;

@JdsEntityAnnotation(entityId = 3, entityName = "example")
public class Example extends JdsEntity implements JdsLoadListener, JdsSaveListener {
    private final SimpleStringProperty _stringField;
    private final SimpleObjectProperty<LocalTime> _timeField;
    private final SimpleObjectProperty<LocalDate> _dateField;
    private final SimpleObjectProperty<LocalDateTime> _dateTimeField;
    private final SimpleObjectProperty<ZonedDateTime> _zonedDateTimeField;
    private final SimpleLongProperty _longField;
    private final SimpleIntegerProperty _intField;
    private final SimpleDoubleProperty _doubleField;
    private final SimpleFloatProperty _floatField;
    private final SimpleBooleanProperty _booleanField;
    private final SimpleBlobProperty _blobField;

    public Example() {
        map(STRING_FIELD, _stringField = new SimpleStringProperty(""));
        map(DATE_FIELD, _dateField = new SimpleObjectProperty<>(LocalDate.now()));
        map(TIME_FIELD, _timeField = new SimpleObjectProperty<>(LocalTime.now()));
        map(DATE_TIME_FIELD, _dateTimeField = new SimpleObjectProperty<>(LocalDateTime.now()));
        map(ZONED_DATE_TIME_FIELD, _zonedDateTimeField = new SimpleObjectProperty<>(ZonedDateTime.now()));
        map(LONG_FIELD, _longField = new SimpleLongProperty(0));
        map(INT_FIELD, _intField = new SimpleIntegerProperty(0));
        map(DOUBLE_FIELD, _doubleField = new SimpleDoubleProperty(0));
        map(FLOAT_FIELD, _floatField = new SimpleFloatProperty(0));
        map(BOOLEAN_FIELD, _booleanField = new SimpleBooleanProperty(false));
        map(BLOB_FIELD, _blobField = new SimpleBlobProperty(new byte[0]));
    }

    public String getStringField() {
        return _stringField.get();
    }

    public void setStringField(String stringField) {
        _stringField.set(stringField);
    }

    public LocalTime getTimeField() {
        return _timeField.get();
    }

    public void setTimeField(LocalTime dateField) {
        _timeField.set(dateField);
    }

    public LocalDate getDateField() {
        return _dateField.get();
    }

    public void setDateField(LocalDate dateField) {
        _dateField.set(dateField);
    }

    public LocalDateTime getDateTimeField() {
        return _dateTimeField.get();
    }

    public void setDateTimeField(LocalDateTime dateTimeField) {
        _dateTimeField.set(dateTimeField);
    }

    public ZonedDateTime getZonedDateTimeField() {
        return _zonedDateTimeField.get();
    }

    public void setZonedDateTimeField(ZonedDateTime zonedDateTimeField) {
        _zonedDateTimeField.set(zonedDateTimeField);
    }

    public long getLongField() {
        return _longField.get();
    }

    public void setLongField(long longField) {
        _longField.set(longField);
    }

    public int getIntField() {
        return _intField.get();
    }

    public void setIntField(int intField) {
        _intField.set(intField);
    }

    public double getDoubleField() {
        return _doubleField.get();
    }

    public void setDoubleField(double doubleField) {
        _doubleField.set(doubleField);
    }

    public float getFloatField() {
        return _floatField.get();
    }

    public void setFloatField(float floatField) {
        _floatField.set(floatField);
    }

    public boolean getBooleanField() {
        return _booleanField.get();
    }

    public void setBooleanField(boolean booleanField) {
        _booleanField.set(booleanField);
    }

    public byte[] getBlobAsByteArray() {
        return _blobField.get();
    }

    public InputStream getBlobAsInputStream() {
        return _blobField.getResourceAsStream();
    }

    public void setBlob(byte[] blob) {
        _blobField.set(blob);
    }

    public void setBlob(InputStream blob) throws IOException {
        _blobField.set(blob);
    }

    @Override
    public void onPreSave(OnPreSaveEventArguments eventArguments) {
        //Optional event i.e write to custom reporting tables, perform custom validation
        //Queries can be batched i.e
        //eventArguments.getOrAddStatement("Batched SQL to execute")
        //eventArguments.addBatch() -- this will be executed safely by jds 
    }

    @Override
    public void onPostSave(OnPostSaveEventArguments eventArguments) {
        //Optional event i.e write to custom reporting tables, perform custom validation
        //Queries can be batched i.e
        //eventArguments.getOrAddStatement("Batched SQL to execute")
        //eventArguments.addBatch() -- this will be executed safely by jds
    }

    @Override
    public void onPreLoad(OnPreLoadEventArguments eventArguments) {
        //Optional event i.e write to custom reporting tables, perform custom validation
        //Queries can be batched i.e
        //eventArguments.getOrAddStatement("Batched SQL to execute")
        //eventArguments.addBatch() -- this will be executed safely by jds
    }

    @Override
    public void onPostLoad(OnPostLoadEventArguments eventArguments) {
        //Optional event i.e write to custom reporting tables, perform custom validation
        //Queries can be batched i.e
        //eventArguments.getOrAddStatement("Batched SQL to execute")
        //eventArguments.addBatch() -- this will be executed safely by jds
    }
}
```
Or in Kotlin
```kotlin
import io.github.subiyacryolite.jds.JdsEntity
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation
import io.github.subiyacryolite.jds.events.*
import javafx.beans.property.*

import java.io.IOException
import java.io.InputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime

import constants.Fields.*

@JdsEntityAnnotation(entityId = 3, entityName = "example")
class Example : JdsEntity(), JdsLoadListener, JdsSaveListener {
    private val _stringField = SimpleStringProperty("")
    private val _timeField = SimpleObjectProperty(LocalTime.now())
    private val _dateField = SimpleObjectProperty(LocalDate.now())
    private val _dateTimeField = SimpleObjectProperty(LocalDateTime.now())
    private val _zonedDateTimeField = SimpleObjectProperty(ZonedDateTime.now())
    private val _longField = SimpleLongProperty(0)
    private val _intField = SimpleIntegerProperty(0)
    private val _doubleField = SimpleDoubleProperty(0.0)
    private val _floatField = SimpleFloatProperty(0f)
    private val _booleanField = SimpleBooleanProperty(false)
    private val _blobField = SimpleBlobProperty(ByteArray(0))

    init {
        map(STRING_FIELD, _stringField)
        map(DATE_FIELD, _dateField)
        map(TIME_FIELD, _timeField)
        map(DATE_TIME_FIELD, _dateTimeField)
        map(ZONED_DATE_TIME_FIELD, _zonedDateTimeField)
        map(LONG_FIELD, _longField)
        map(INT_FIELD, _intField)
        map(DOUBLE_FIELD, _doubleField)
        map(FLOAT_FIELD, _floatField)
        map(BOOLEAN_FIELD, _booleanField)
        map(BLOB_FIELD, _blobField)
    }

    var stringField: String
        get() = _stringField.get()
        set(stringField) = _stringField.set(stringField)

    var timeField: LocalTime
        get() = _timeField.get()
        set(dateField) = _timeField.set(dateField)

    var dateField: LocalDate
        get() = _dateField.get()
        set(dateField) = _dateField.set(dateField)

    var dateTimeField: LocalDateTime
        get() = _dateTimeField.get()
        set(dateTimeField) = _dateTimeField.set(dateTimeField)

    var zonedDateTimeField: ZonedDateTime
        get() = _zonedDateTimeField.get()
        set(zonedDateTimeField) = _zonedDateTimeField.set(zonedDateTimeField)

    var longField: Long
        get() = _longField.get()
        set(longField) = _longField.set(longField)

    var intField: Int
        get() = _intField.get()
        set(intField) = _intField.set(intField)

    var doubleField: Double
        get() = _doubleField.get()
        set(doubleField) = _doubleField.set(doubleField)

    var floatField: Float
        get() = _floatField.get()
        set(floatField) = _floatField.set(floatField)

    var booleanField: Boolean
        get() = _booleanField.get()
        set(booleanField) = _booleanField.set(booleanField)

    val blobAsByteArray: ByteArray?
        get() = _blobField.get()

    val blobAsInputStream: InputStream
        get() = _blobField.resourceAsStream

    fun setBlob(blob: ByteArray) {
        _blobField.set(blob)
    }

    @Throws(IOException::class)
    fun setBlob(blob: InputStream) {
        _blobField.set(blob)
    }

    override fun onPreSave(eventArguments: OnPreSaveEventArguments) {
        //Optional event i.e write to custom reporting tables, perform custom validation
        //Queries can be batched i.e
        //eventArguments.getOrAddStatement("Batched SQL to execute")
        //eventArguments.addBatch() -- this will be executed safely by jds
    }

    override fun onPostSave(eventArguments: OnPostSaveEventArguments) {
        //Optional event i.e write to custom reporting tables, perform custom validation
        //Queries can be batched i.e
        //eventArguments.getOrAddStatement("Batched SQL to execute")
        //eventArguments.addBatch() -- this will be executed safely by jds
    }

    override fun onPreLoad(eventArguments: OnPreLoadEventArguments) {
        //Optional event i.e write to custom reporting tables, perform custom validation
        //Queries can be batched i.e
        //eventArguments.getOrAddStatement("Batched SQL to execute")
        //eventArguments.addBatch() -- this will be executed safely by jds
    }

    override fun onPostLoad(eventArguments: OnPostLoadEventArguments) {
        //Optional event i.e write to custom reporting tables, perform custom validation
        //Queries can be batched i.e
        //eventArguments.getOrAddStatement("Batched SQL to execute")
        //eventArguments.addBatch() -- this will be executed safely by jds
    }
}
```
### 1.1.5 Binding Objects and Object Arrays
Beyond saving numeric, string and date values JDS can also persist embedded objects and object arrays.

All that's required is a valid **JdsEntity** or **IJdsEntity** subclass to be mapped to a JdsField of type **ENTITY**.

This allows a single parent entity to have multiple child entityVersions of the same entity type i.e transfer: AccountTransfer { source: Account , destination: Account, amount: double }

```java
public class Fields
{
    //Create a field of type ENTITY
    public static final JdsField ADDRESSES = new JdsField(9000, "ADDRESSES", JdsFieldType.ENTITY_COLLECTION);
}
```

```java
public class Entities
{
    //Create the appropriate JdsFieldEntity entry
    public static final JdsFieldEntity<Address> ADDRESSES = new JdsFieldEntity(Address.class, Fields.ADDRESSES);
}
```

```java
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import io.github.subiyacryolite.jds.JdsEntity;
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation;

import java.util.List;

@JdsEntityAnnotation(entityId = 2, entityName = "AddressBook", version = 1)
public class AddressBook extends JdsEntity {
    private final SimpleListProperty<Address> addresses;

    public AddressBook() {
        map(Entities.ADDRESSES, addresses = new SimpleListProperty<>(FXCollections.observableArrayList()));
    }

    public List<Address> getAddresses() {
        return this.addresses.get();
    }

    public void setAddresses(List<Address> value) {
        this.addresses.set((ObservableList<Address>) value);
    }

    @Override
    public String toString() {
        return "AddressBook{" +
                "addresses = " + getAddresses() +
                '}';
    }
}
```
Or in Kotlin
```kotlin
import constants.Entities
import io.github.subiyacryolite.jds.JdsEntity
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections

@JdsEntityAnnotation(entityId = 2, entityName = "AddressBook", version = 1)
class AddressBook : JdsEntity() {
    private val _addresses: SimpleListProperty<Address> = SimpleListProperty(FXCollections.observableArrayList())

    init {
        map(Entities.ADDRESSES, _addresses)
    }

    val addresses: MutableList<Address>
        get() = _addresses.get()

    override fun toString(): String {
        return "AddressBook{ addresses = $addresses }"
    }
}
```

## 1.2 CRUD Operations
### 1.2.1 Initialising the database
In order to use JDS you will need an instance of JdsDb. Your instance of JdsDb will have to extend one of the following classes and override the getConnection() method: JdsDbMySql, JdsDbPostgreSql, JdsDbSqlite or JdsDbTransactionalSql.
Please note that your project must have the correct JDBC driver in its class path. The drivers that were used during development are listed under [Supported Databases](#supported-databases) above.
#### Postgres example
```java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class JdsDbPostgreSqlmplementation extends JdsDbPostgreSql {

    @Override
    public Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        Properties properties = new Properties();
        properties.put("user", "USER_NAME");
        properties.put("password", "PASSWORD");
        return DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/DATABASE", properties);
    }
}

........

JdsDb jdsDb = new JdsDbPostgreSqlmplementation();
jdsDb.init();
```
#### MySQL Example
```java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class JdsDbMySqlImplementation extends JdsDbMySql {

    @Override
    public Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Properties properties = new Properties();
        properties.put("user", "USER_NAME");
        properties.put("password", "PASSWORD");
        properties.put("autoReconnect","true");
        properties.put("useSSL","false");
        properties.put("rewriteBatchedStatements","true");
        properties.put("continueBatchOnError","true");
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/DATABASE", properties);
    }
}

...

JdsDb jdsDb = new JdsDbMySqlImplementation();
jdsDb.init();
```
#### Microsoft SQL Server Example
```java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class JdsDbTransactionalSqllmplementation extends JdsDbTransactionalSql {

    @Override
    public Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        Properties properties = new Properties();
        properties.put("user", "USER_NAME");
        properties.put("password", "PASSWORD");
        return DriverManager.getConnection("jdbc:sqlserver://127.0.0.1\\DB_INSTANCE;databaseName=DATABASE", properties);
    }
}

....

JdsDb jdsDb = new JdsDbTransactionalSqllmplementation();
jdsDb.init();
```
#### Oracle Example
```java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


public class JdsDbOracleImplementation extends JdsDbOracle {

    @Override
    public Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Properties properties = new Properties();
        properties.put("user", "USER_NAME");
        properties.put("password", "PASSWORD");
        return DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:DATABASE", properties);
    }
}

....

JdsDb jdsDb = new JdsDbOracleImplementation();
jdsDb.init();
```
#### Sqlite Example
```java
import org.sqlite.SQLiteConfig;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JdsDbSqliteImplementation extends JdsDbSqlite {

    @Override
    public Connection getConnection() throws ClassNotFoundException, SQLException {
        String url = "jdbc:sqlite:" + getDatabaseFile();
        SQLiteConfig sqLiteConfig = new SQLiteConfig();
        sqLiteConfig.enforceForeignKeys(true); //You must enable foreign keys in SQLite
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection(url, sqLiteConfig.toProperties());
    }

    public String getDatabaseFile() {
        File path = new File(System.getProperty("user.home") + File.separator + ".jdstest" + File.separator + "jds.db");
        if (!path.exists()) {
            File directory = path.getParentFile();
            if (!directory.exists()) {
                directory.mkdirs();
            }
        }
        String absolutePath = path.getAbsolutePath();
        return absolutePath;
    }
}

...

JdsDb jdsDb = new JdsDbSqliteImplementation();
jdsDb.init();
```
With this you should have a valid connection to your database and JDS will setup its tables and procedures automatically. Furthermore, you can use the **getConnection()** method from your JdsDataBase instance in order to return a standard **java.sql.Connection** in your application. 

### 1.2.2 Initialising JDS
Once you have initialised your database you can go ahead and initialise all your JDS classes. You can achieve this by mapping ALL your JDS classes in the following manner.
```java
public void initialiseJdsClasses()
{
    //jdsDb is a reference to your instance of JdsDb.java
    jdsDb.map(Example.class);
    jdsDb.map(Address.class);
    jdsDb.map(AddressBook.class);
}
```
You only have to do this once at start-up but it is vital that you do so. Without this you will face problems when loading or saving records

### 1.2.3 Creating objects
Once you have defined your class you can initialise them. A dynamic **Entity Guid** is created for every jdsEntity by default, this value is used to uniquely identify an object and it data in the database. You can set your own values if you wish.
```java
private List<Example> getCollection() {
    List<Example> collection = new ArrayList<>();
    
    Example instance1 = new Example();
    instance1.setEntityGuid("instance1");
    instance1.setStringField("One");
    
    Example instance2 = new Example();
    instance2.setStringField("Two");
    instance2.setTimeField(LocalTime.of(15, 24));
    instance2.setDateField(LocalDate.of(2012, 8, 26));
    instance2.setDateTimeField(LocalDateTime.of(1991, 07, 01, 8, 33, 12));
    instance2.setZonedDateTimeField(ZonedDateTime.of(LocalDateTime.now().minusMonths(3), ZoneId.systemDefault()));
    instance2.setIntField(99);
    instance2.setLongField(888);
    instance2.setDoubleField(777.666);
    instance2.setFloatField(5555.4444f);
    instance2.setBooleanField(false);
    instance2.setEntityGuid("instance2");
    
    Example instance3 = new Example();
    instance3.setStringField("Three");
    instance3.setTimeField(LocalTime.of(03, 14));
    instance3.setDateField(LocalDate.of(2034, 6, 14));
    instance3.setDateTimeField(LocalDateTime.of(1987, 07, 24, 13, 22, 45));
    instance3.setZonedDateTimeField(ZonedDateTime.of(LocalDateTime.now().plusDays(3), ZoneId.systemDefault()));
    instance3.setIntField(22);
    instance3.setLongField(333);
    instance3.setDoubleField(444.555);
    instance3.setFloatField(5555.6666f);
    instance3.setBooleanField(false);
    instance3.setEntityGuid("instance3");
    
    Example instance4 = new Example();
    instance4.setStringField("Four");
    instance4.setTimeField(LocalTime.of(12, 44));
    instance4.setDateField(LocalDate.of(3034, 12, 1));
    instance4.setDateTimeField(LocalDateTime.of(1964, 10, 24, 2, 12, 14));
    instance4.setZonedDateTimeField(ZonedDateTime.of(LocalDateTime.now().minusDays(3), ZoneId.systemDefault()));
    instance4.setIntField(10);
    instance4.setLongField(100);
    instance4.setDoubleField(100.22);
    instance4.setFloatField(1000.0f);
    instance4.setBooleanField(false);
    instance4.setEntityGuid("instance4");
    
    collection.add(instance1);
    collection.add(instance2);
    collection.add(instance3);
    collection.add(instance4);
    return collection;
}
```

### 1.2.4 Save
The API has a single **save()** method within the class **JdsSave**. The method can takes either one of the following arguments **(JdsEntity... entityVersions)** or **(Collection\<JdsEntity\> entityVersions)**. The method also expects the user to supply a batch size.
```java
List<Example> collection = getCollection();

List<Example> collection = getCollection();
Callable<Boolean> save = new JdsSave(jdsDb, 0, collection);
FutureTask<Boolean> saving = new FutureTask(save);
new Thread(saving).start();
while (!saving.isDone())
    System.out.println("Waiting for operation 1 to complete");
System.out.printf("Saved? %s\n", saving.get());
```

### 1.2.5 Load
The system currently has three variants of the **load()** method. The first variant loads ALL the instances of a JdsEntity class. The second variant loads ALL the instances of a JdsEntity class with matching Entity Guids which are supplied by the user. The second variant adds an optional parameter "Comparator<? extends JdsEntity>" which allows you to load a sorted collection
```java
//all entityVersions of type AddressBook
List<Example> allInstances;
//all entityVersions of type AddressBook with Entity Guids in range
List<Example> specificInstance;
//all entityVersions of type AddressBook with Entity Guids in range SORTED by creation date
List<Example> sortedInstances;
//ordering comparator
Comparator<Example> comparator = Comparator.comparing(Example::getDateField);


Callable<List<Example>> loadAllInstances = new JdsLoad(jdsDb, Example.class);
Callable<List<Example>> loadSpecificInstance = new JdsLoad(jdsDb, Example.class, "instance3");
Callable<List<Example>> loadSortedInstances = new JdsLoad(jdsDb, Example.class, comparator);

FutureTask<List<Example>> loadingAllInstances = new FutureTask(loadAllInstances);
FutureTask<List<Example>> loadingSpecificInstance = new FutureTask(loadSpecificInstance);
FutureTask<List<Example>> loadingSortedInstances = new FutureTask(loadSortedInstances);

new Thread(loadingAllInstances).start();
new Thread(loadingSpecificInstance).start();
new Thread(loadingSortedInstances).start();

while (!loadingAllInstances.isDone())
System.out.println("Waiting for operation 1 to complete");
while (!loadingSpecificInstance.isDone())
System.out.println("Waiting for operation 2 to complete");
while (!loadingSortedInstances.isDone())
System.out.println("Waiting for operation 3 to complete");

List<Example> allInstances = loadingAllInstances.get();
List<Example> specificInstance = loadingSpecificInstance.get();
List<Example> sortedInstances = loadingSortedInstances.get();

System.out.println(allInstances);
System.out.println(specificInstance);
System.out.println(sortedInstances);
System.out.println("DONE");
```

### 1.2.6 Load with Filter
A filter mechanisim is present. It is failry basic and is still being refined. An example to sample usage is shown below.
```java
JdsFilter filter = new JdsFilter(jdsDb, Address.class).equals(AddressFields.AREA_NAME, "Riverdale").like(AddressFields.COUNTRY_NAME, "Zam").or().equals(AddressFields.PROVINCE_NAME, "Copperbelt");
List<Address> output = new FutureTask<List<Address>>(filter).get();
```

### 1.2.7 Delete
You can delete by providing one or more JdsEntities or via a collection of strings representing JdsEntity UUIDS.
```java
List<Example> collection = getCollection();
    
Callable<Boolean> delete = new JdsDelete(jdsDb, "instance2");
FutureTask<Boolean> deleting = new FutureTask(delete);
new Thread(deleting).start();
while(!deleting.isDone())
    System.out.println("Waiting for operation to complete");
System.out.println("Deleted? "+ deleting.get());
```

## 1.3 Schema Generation
Starting with version 4 JDS can be set up to create schemas to store unique or append only line records. These “JdsTables” pull predefined fields from one or more registered entities. On save and/or delete these tables will be updated. Every JdsTable must be registered to an instance of JdsDb.

Below is an example of a JdsTable that will persist two specific fields from the Address entity type.
```kotlin
val customTable = JdsTable()
customTable.uniqueEntries = false
customTable.name = "CrtAddressSpecific"
customTable.registerEntity(Address::class.java)
customTable.registerField(Fields.AREA_NAME)
customTable.registerField(Fields.CITY_NAME)

//register table
jdsDb.mapTable(customTable)

//after all tables have been mapped call this function
jdsDb.prepareTables()
```

Below is an example of a JdsTable that will persist **all the fields** in the Address entity type
```kotlin
val crtAddress = JdsTable(Address::class.java, true)

//register table
jdsDb.mapTable(customTable)

//after all tables have been mapped call this function
jdsDb.prepareTables()
```

You can define your JdsTables in code or you may deserialize them in JSON format
```json
{
  "name": "CrtJsonTest",
  "uniqueEntries": false,
  "onlyLiveRecords": false,
  "onlyDeprecatedRecords": false,
  "entities": [1, 3],
  "fields": [3, 5, 7, 4]
}
```

## 1.4 Backend Design
The underlying database is based off of the star schema.

Each **JdsStore[X]** table stores live data and are always up-to-date.

The **JdsRef[X]** and **JdsBind[X]** tables are used by the systems to link different relations and metadata.

**JdsSoreOldFieldValues** stores every single update/insert in the system and is the single point of reference for historical data. It is written to by calling **logEdits(true)** on an instance of JdsDb. This behaviour is disabled by default.

If you wanted to view the changes made to a specific fieldEntity you could use a query such as

```sql
select EntityGuid, FieldId, IntegerValue, min(DateOfModification) 
from JdsStoreOldFieldValues
where EntityGuid = 'record_of_interest' and FieldId = 1001
group by (EntityGuid, FieldId, IntegerValue)
```

![Database design](database_design.png?raw=true)

# Development
I highly recommend the use of the [IntelliJ IDE](https://www.jetbrains.com/idea/download/) for development.

# Contributing to Jenesis Data Store
If you would like to contribute code you can do so through [Github](https://github.com/SubiyaCryolite/Jenesis-Data-Store/) by forking the repository and sending a pull request targeting the current development branch.

When submitting code, please make every effort to follow existing conventions and style in order to keep the code as readable as possible.

# Bugs and Feedback
For bugs, questions and discussions please use the [Github Issues](https://github.com/SubiyaCryolite/Jenesis-Data-Store/issues).

# Special Thanks
To all our users and contributors!
