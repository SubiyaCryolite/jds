[![Build Status](https://travis-ci.org/SubiyaCryolite/Jenesis-Data-Store.svg?branch=master)](https://travis-ci.org/SubiyaCryolite/Jenesis-Data-Store)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.subiyacryolite/jds/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.subiyacryolite/jds)
[![Javadocs](https://javadoc.io/badge/io.github.subiyacryolite/jds.svg)](https://javadoc.io/doc/io.github.subiyacryolite/jds)
[![License](https://img.shields.io/badge/License-BSD--3--Clause-blue.svg)](LICENSE.md)
![Size](https://reposs.herokuapp.com/?path=SubiyaCryolite/Jenesis-Data-Store)

# Jenesis Data Store
Jenesis Data Store (JDS) was created to help developers persist their classes to relational databases in a fast and reliable manner, without requiring them to design elaborate relational schemas. The aim of JDS is to allow for the rapid creation and modification of java classes in order to facilitate rapid prototyping and quick development.

The library eliminates the need to modify schemas once a class has been altered. It also eliminates all concerns regarding "breaking changes" in regards to fields and their values. Fields, Objects and ArrayTypes can be added, modified or removed at will. Beyond that the libraries data is structured in a way to promote fast and efficient Data Mining queries that can be used to support the application in question or to feed into specialised analytic software.

Put simply, JDS is useful for any developer that requires a flexible schema running on top of a traditional Relational Database. Some use-cases include:
- Rapid prototyping
- Academic projects
- Single or Multi-user applications
- Self hosted REST/SOAP services

JDS is licensed under the [3-Clause BSD License](https://opensource.org/licenses/BSD-3-Clause)

# Design
The concept behind JDS is quite simple. Extend a base “Entity” class, define “Fields” of a particular datatype and lastly “Map” the said fields to a JavaFX Bean.

## Features
- Transparent persistence
- Serialization of JavaFX bean *values*
- Full support for generics and inheritance
- Easily integrates with new or existing databases
- Save, Updates and Deletes cascade to child objects and collections
- Eager Loading is applied to embedded objects as well as on collections
- Generation of views representing entities as conventional tables
- Supports MySQL, T-SQL, PostgreSQL and SQLite
- Underlying database implemented using the Star Schema

# Maven Central
You can search on The Central Repository with GroupId and ArtifactId Maven Search for [![Maven Search](https://img.shields.io/badge/io.github.subiyacryolite-jds-blue.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.subiyacryolite/jds)
```xml
<dependency>
    <groupId>io.github.subiyacryolite</groupId>
    <artifactId>jds</artifactId>
    <version>LATEST_VERSION</version>
</dependency>
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
|BOOLEAN|boolean|Boolean values|
|BLOB|byte[] or InputStream|Blob values|
|FLOAT|float|Numeric float values|
|INT|int|Numeric integer values|
|DOUBLE|double|Numeric double values|
|LONG|long|Numeric long values|
|TEXT|String|String values with no max limit|
|DATE_TIME|LocalDateTime|DateTime instances based on the host machines local timezone|
|ZONED_DATE_TIME|ZonedDateTime|Zoned DateTime instances|
|TIME|LocalTime|Local time instances|
|DATE|LocalDate|Local date instances|
|ARRAY_FLOAT|List\<Float\>|Lists of type Float|
|ARRAY_INT|List\<Integer\>|Lists of type Integer|
|ARRAY_DOUBLE|List\<Double\>|Lists of type Double|
|ARRAY_LONG|List\<Long\>|Lists of type Long|
|ARRAY_TEXT|List\<String\>|Lists of type String|
|ARRAY_DATE_TIME|List\<LocalDateTime\>|Lists of type LocalDateTime|
|ENUM|Enum|Object of type Enum|
|ENUM_COLLECTION|List\<Enum\>|Lists of type Enum|

I recommend defining your fields as static constants

```java
import io.github.subiyacryolite.jds.JdsField;
import io.github.subiyacryolite.jds.enums.JdsFieldType;

public class Fields {
        public static final JdsField STRING_FIELD = new JdsField(1000, "STRING_FIELD", JdsFieldType.TEXT);
        public static final JdsField TIME_FIELD = new JdsField(1009, "TIME_FIELD", JdsFieldType.TIME);
        public static final JdsField DATE_FIELD = new JdsField(1001, "DATE_FIELD", JdsFieldType.DATE);
        public static final JdsField DATE_TIME_FIELD = new JdsField(1002, "DATE_TIME_FIELD", JdsFieldType.DATE_TIME);
        public static final JdsField ZONED_DATE_TIME_FIELD = new JdsField(1003, "ZONED_DATE_TIME_FIELD", JdsFieldType.ZONED_DATE_TIME);
        public static final JdsField LONG_FIELD = new JdsField(1004, "LONG_FIELD", JdsFieldType.LONG);
        public static final JdsField INT_FIELD = new JdsField(1005, "INT_FIELD", JdsFieldType.INT);
        public static final JdsField DOUBLE_FIELD = new JdsField(1006, "DOUBLE_FIELD", JdsFieldType.DOUBLE);
        public static final JdsField FLOAT_FIELD = new JdsField(1007, "FLOAT_FIELD", JdsFieldType.FLOAT);
        public static final JdsField BOOLEAN_FIELD = new JdsField(1008, "BOOLEAN_FIELD", JdsFieldType.BOOLEAN);
        public static final JdsField BLOB_FIELD = new JdsField(1010, "BLOB_FIELD", JdsFieldType.BLOB);
        //=============================================================================================    
        public static final JdsField STREET_NAME = new JdsField(1, "street_name", JdsFieldType.TEXT);
        public static final JdsField PLOT_NUMBER = new JdsField(2, "plot_number", JdsFieldType.INT);
        public static final JdsField AREA_NAME = new JdsField(3, "area_name", JdsFieldType.TEXT);
        public static final JdsField PROVINCE_NAME = new JdsField(4, "province_name", JdsFieldType.TEXT);
        public static final JdsField CITY_NAME = new JdsField(5, "city_name", JdsFieldType.TEXT);
        public static final JdsField SEX_ENUM = new JdsField(6, "sex_enum", JdsFieldType.ENUM);
        public static final JdsField COUNTRY_NAME = new JdsField(7, "country_name", JdsFieldType.TEXT);
        public static final JdsField PRIMARY_ADDRESS_ENUM = new JdsField(8, "primary_address_enum", JdsFieldType.ENUM);
        public static final JdsField LOCAL_DATE_OF_REGISTRATION = new JdsField(9, "local_date_of_registration", JdsFieldType.DATE_TIME);
        public static final JdsField ZONED_DATE_OF_REGISTRATION = new JdsField(10, "zoned_date_of_registration", JdsFieldType.ZONED_DATE_TIME);        
}

```

### 1.1.3 Defining Enums
JdsEnums are an extension of fields. However, they are designed for cases where one or more constant values are required. Usually these values would be represented by CheckBoxes, RadioButtons or Combo Boxes in a UI. In this example we will define Sex as an enumerated value with the following options (Male, Female, Other).
First of all we'd have to define a standard fieldEntity of type ENUM_TEXT.
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
Behind the scenes these enums will be stored as either an Integer (ENUM) or an Integer Array (ENUM_COLLECTION).

### 1.1.4 Binding Properties
Depending on the type of fieldEntity, JDS will require that you set you objects properties to one of the following JavaFX bean container types.

Kindly note that none of the JavaFX beans are serializable, however JDS supports serialization via the explicit implementation of Javas Externalizable interface. That said only Property values are serialized, not listeners or their states.

|JDS Field Type|Java Property Type|
|-----|-----|
|BOOLEAN|[SimpleBooleanProperty](https://docs.oracle.com/javafx/2/api/javafx/beans/property/SimpleBooleanProperty.html)|
|BLOB|[SimpleBlobProperty](https://static.javadoc.io/io.github.subiyacryolite/jds/3.4.3/javafx/beans/property/SimpleBlobProperty.html)|
|FLOAT|[SimpleFloatProperty](https://docs.oracle.com/javafx/2/api/javafx/beans/property/SimpleFloatProperty.html)|
|INT|[SimpleIntegerProperty](https://docs.oracle.com/javafx/2/api/javafx/beans/property/SimpleIntegerProperty.html)|
|DOUBLE|[SimpleDoubleProperty](https://docs.oracle.com/javafx/2/api/javafx/beans/property/SimpleDoubleProperty.html)|
|LONG|[SimpleLongProperty](https://docs.oracle.com/javafx/2/api/javafx/beans/property/SimpleLongProperty.html)|
|TEXT|[SimpleStringProperty](https://docs.oracle.com/javafx/2/api/javafx/beans/property/SimpleStringProperty.html)|
|DATE_TIME|[SimpleObjectProperty\<LocalDateTime\>](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDateTime.html)|
|ZONED_DATE_TIME|[SimpleObjectProperty\<ZonedDateTime\>](https://docs.oracle.com/javase/8/docs/api/java/time/ZonedDateTime.html)|
|TIME|[SimpleObjectProperty\<LocalTime\>](https://docs.oracle.com/javase/8/docs/api/java/time/LocalTime.html)|
|DATE|[SimpleObjectProperty\<LocalDate\>](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)|
|ARRAY_FLOAT|[SimpleListProperty\<Float\>](https://docs.oracle.com/javafx/2/api/javafx/beans/property/SimpleListProperty.html)|
|ARRAY_INT|[SimpleListProperty\<Integer\>](https://docs.oracle.com/javafx/2/api/javafx/beans/property/SimpleListProperty.html)|
|ARRAY_DOUBLE|[SimpleListProperty\<Double\>](https://docs.oracle.com/javafx/2/api/javafx/beans/property/SimpleListProperty.html)|
|ARRAY_LONG|[SimpleListProperty\<Long\>](https://docs.oracle.com/javafx/2/api/javafx/beans/property/SimpleListProperty.html)|
|ARRAY_TEXT|[SimpleListProperty\<String\>](https://docs.oracle.com/javafx/2/api/javafx/beans/property/SimpleListProperty.html)|
|ARRAY_DATE_TIME|[SimpleListProperty\<LocalDateTime\>](https://docs.oracle.com/javafx/2/api/javafx/beans/property/SimpleListProperty.html)|
|ENUM|[SimpleObjectProperty\<Enum\>](https://docs.oracle.com/javase/8/docs/api/java/lang/Enum.html)|
|ENUM_COLLECTION|[SimpleListProperty\<Enum\>](https://docs.oracle.com/javafx/2/api/javafx/beans/property/SimpleListProperty.html)|

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
        //Queries can be batched i.e eventArguments.getOrAddStatement("Batched SQL to execute") 
    }

    @Override
    public void onPostSave(OnPostSaveEventArguments eventArguments) {
        //Optional event i.e write to custom reporting tables, perform custom validation
        //Queries can be batched i.e eventArguments.getOrAddStatement("Batched SQL to execute")
    }

    @Override
    public void onPreLoad(OnPreLoadEventArguments eventArguments) {
        //Optional event i.e write to custom reporting tables, perform custom validation
        //Queries can be batched i.e eventArguments.getOrAddStatement("Batched SQL to execute")
    }

    @Override
    public void onPostLoad(OnPostLoadEventArguments eventArguments) {
        //Optional event i.e write to custom reporting tables, perform custom validation
        //Queries can be batched i.e eventArguments.getOrAddStatement("Batched SQL to execute")
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
        //Queries can be batched i.e eventArguments.getOrAddStatement("Batched SQL to execute")
    }

    override fun onPostSave(eventArguments: OnPostSaveEventArguments) {
        //Optional event i.e write to custom reporting tables, perform custom validation
        //Queries can be batched i.e eventArguments.getOrAddStatement("Batched SQL to execute")
    }

    override fun onPreLoad(eventArguments: OnPreLoadEventArguments) {
        //Optional event i.e write to custom reporting tables, perform custom validation
        //Queries can be batched i.e eventArguments.getOrAddStatement("Batched SQL to execute")
    }

    override fun onPostLoad(eventArguments: OnPostLoadEventArguments) {
        //Optional event i.e write to custom reporting tables, perform custom validation
        //Queries can be batched i.e eventArguments.getOrAddStatement("Batched SQL to execute")
    }
}
```
### 1.1.5 Binding Objects and Object Arrays
Beyond saving numeric, string and date values JDS can also persist embedded objects and object arrays.

All that's required is a valid **JdsEntity** or **IJdsEntity** subclass to be mapped to a JdsField of type **CLASS**.

This allows a single parent entity to have multiple child entities of the same entity type i.e transfer: AccountTransfer { source: Account , destination: Account, amount: double }

```java
public class Fields
{
    //Create a field of type CLASS
    public static final JdsField ADDRESS_FIELD = new JdsField(9000, "ADDRESS_FIELD", JdsFieldType.CLASS);
}
```

```java
public class Entities
{
    //Create the appropriate JdsFieldEntity entry
    public static final JdsFieldEntity<Address> ADDRESS_FIELD = new JdsFieldEntity(Address.class, Fields.ADDRESS_FIELD);
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
        map(Entities.ADDRESS_FIELD, addresses = new SimpleListProperty<>(FXCollections.observableArrayList()));
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
        map(Entities.ADDRESS_FIELD, _addresses)
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
The API has a single **save()** method within the class **JdsSave**. The method can takes either one of the following arguments **(JdsEntity... entities)** or **(Collection\<JdsEntity\> entities)**. The method also expects the user to supply a batch size.
```java
List<Example> collection = getCollection();

//NEW APPROACH (INTRODUCED IN 1.170514)
List<Example> collection = getCollection();
Callable<Boolean> save = new JdsSave(jdsDb, 0, collection);
FutureTask<Boolean> saving = new FutureTask(save);
new Thread(saving).start();
while (!saving.isDone())
    System.out.println("Waiting for operation 1 to complete");
System.out.printf("Saved? %s\n", saving.get());

//OLD APPROACH (DEPRECATED IN 1.170514)
JdsSave.save(jdsDb, 1, collection);
System.out.printf("Saved %s\n", collection);
```

### 1.2.5 Load
The system currently has three variants of the **load()** method. The first variant loads ALL the instances of a JdsEntity class. The second variant loads ALL the instances of a JdsEntity class with matching Entity Guids which are supplied by the user. The second variant adds an optional parameter "Comparator<? extends JdsEntity>" which allows you to load a sorted collection
```java
//all entities of type AddressBook
List<Example> allInstances;
//all entities of type AddressBook with Entity Guids in range
List<Example> specificInstance;
//all entities of type AddressBook with Entity Guids in range SORTED by creation date
List<Example> sortedInstances;
//ordering comparator
Comparator<Example> comparator = Comparator.comparing(Example::getDateField);

//NEW APPROACH (INTRODUCED IN 1.170514)

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

//OLD APPROACH (DEPRECATED IN 1.170514)

    allInstances = JdsLoad.load(jdsDb, Example.class);
    specificInstance = JdsLoad.load(jdsDb, Example.class, "instance3");
    specificAddressBook = JdsLoad.load(jdsDb, Example.class, comparator);
    
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
    
//NEW APPROACH (INTRODUCED IN 1.170514)
    Callable<Boolean> delete = new JdsDelete(jdsDb, "instance2");
    FutureTask<Boolean> deleting = new FutureTask(delete);
    new Thread(deleting).start();
    while(!deleting.isDone())
        System.out.println("Waiting for operation to complete");
    System.out.println("Deleted? "+ deleting.get());

//OLD APPROACH (DEPRECATED IN 1.170514)
    //using strings representing entity guids
    JdsDelete.delete(jdsDb, "instance2");
    //using an object or an object ollection
    JdsDelete.delete(jdsDb, collection);
```

## 1.3 Backend Design
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
