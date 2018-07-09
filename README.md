[![Build Status](https://travis-ci.org/SubiyaCryolite/jds.svg?branch=master)](https://travis-ci.org/SubiyaCryolite/jds)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.subiyacryolite/jds/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.subiyacryolite/jds)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/oss.sonatype.org/io.github.subiyacryolite/jds.svg)](https://oss.sonatype.org/content/repositories/snapshots/io/github/subiyacryolite/jds/)
[![Javadocs](https://javadoc.io/badge/io.github.subiyacryolite/jds.svg)](https://javadoc.io/doc/io.github.subiyacryolite/jds)
[![License](https://img.shields.io/badge/License-BSD--3--Clause-blue.svg)](LICENSE.md)
![Size](https://github-size-badge.herokuapp.com/subiyacryolite/jds.svg)

# Jenesis Data Store

Jenesis Data Store (JDS) was created to help developers persist their classes to relational databases in a fast and reliable manner, without requiring them to design elaborate relational schemas. The aim of JDS is to allow for the rapid creation and modification of java classes in order to facilitate rapid prototyping and quick development.

The library eliminates the need to modify schemas once a class has been altered. It also eliminates all concerns regarding "breaking changes" in regards to fields and their addition and/or removal. Fields, Objects and Collection types can be added, modified or removed at will. Beyond that the libraries data is structured in a way to promote fast and efficient Data Mining queries that can be used to support the application in question or to feed into specialised analytic software.

Put simply, JDS is useful for any developer that requires a flexible schema running on top of a traditional Relational Database

JDS is licensed under the [3-Clause BSD License](https://opensource.org/licenses/BSD-3-Clause)

# Design

The concept behind JDS is quite simple. Extend a base “Entity” class, define strongly-typed “Fields” and then “Map” them to a backing JavaFX Bean.

## Features

* Transparent persistence
* Serialization of JavaFX bean _values_
* Supports the persistence of NULL values for JVM primitive types
* Full support for generics and inheritance
* Easily integrates with new or existing databases
* Save, Updates and Deletes cascade to child objects and collections
* All saves and deletes are ACID (transaction based)
* Eager Loading is applied to embedded objects as well as on collections
* Supports the automatic generation of flat tables for reporting
* Supports a portable format that can be serialised to JSON to bypass EAV
* Supports MySQL, T-SQL, PostgreSQL, Oracle 11G, MariaDB and SQLite
* Underlying database implemented using the Star Schema

# Maven Central

You can search on The Central Repository with GroupId and ArtifactId Maven Search for [![Maven Search](https://img.shields.io/badge/io.github.subiyacryolite-jds-blue.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.subiyacryolite/jds)

Maven

```xml
<dependency>
    <groupId>io.github.subiyacryolite</groupId>
    <artifactId>jds</artifactId>
    <version>9.2.2-SNAPSHOT</version>
</dependency>
```

Gradle

```groovy
compile 'io.github.subiyacryolite:jds:9.2.2-SNAPSHOT'
```

# Dependencies

The library depends on Java 8. Both 64 and 32 bit variants should suffice. Both the Development Kit and Runtime can be downloaded from [here](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html).

# Supported Databases

The API currently supports the following Relational Databases, each of which has their own dependencies, versions and licensing requirements. Please consult the official sites for specifics.

| Database             | Version Tested Against | Official Site                                                                                                         | JDBC Driver Tested Against                                                                                        |
| -------------------- | ---------------------- | --------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------- |
| PostgreSQL           | 9.5                    | [Official Site](https://www.postgresql.org/)                                                                          | [org.postgresql](https://mvnrepository.com/artifact/org.postgresql/postgresql)                                    |
| MySQL                | 5.7.14                 | [Official Site](https://www.mysql.com/downloads/)                                                                     | [com.mysql.cj.jdbc.Driver](https://mvnrepository.com/artifact/mysql/mysql-connector-java)                         |
| MariaDb              | 10.2.12                | [Official Site](http://mariadb.org//download/)                                                                        | [org.mariadb.jdbc.Driver](https://mvnrepository.com/artifact/org.mariadb.jdbc/mariadb-java-client)                |
| Microsoft SQL Server | 2008 R2                | [Official Site](https://www.microsoft.com/en-us/sql-server/sql-server-downloads)                                      | [com.microsoft.sqlserver](https://mvnrepository.com/artifact/com.microsoft.sqlserver/sqljdbc4)                    |
| SQLite               | 3.16.1                 | [Official Site](https://www.sqlite.org/)                                                                              | [org.sqlite.JDBC](https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc)                                      |
| Oracle               | 11g Release 2          | [Official Site](http://www.oracle.com/technetwork/database/database-technologies/express-edition/overview/index.html) | [oracle.jdbc.driver.OracleDriver](https://mvnrepository.com/artifact/com.oracle/ojdbc6/12.1.0.1-atlassian-hosted) |

# 1 How it works

## 1.1 Creating Classes

Classes that use JDS need to extend JdsEntity.

```kotlin
import io.github.subiyacryolite.jds.JdsEntity;

public class Address extends JdsEntity(){}
```

However, if you plan on using interfaces they must extend IJdsEntity. Concrete classes can then extend JdsEntity

```kotlin
import io.github.subiyacryolite.jds.JdsEntity;
import io.github.subiyacryolite.jds.IJdsEntity;

public interface IAddress extends IJdsEntity{}
public class Address extends JdsEntity implements IAddress{}
```

Following that the following steps need to be taken.

### 1.1.1 Annotating Classes

Every class that extends JdsEntity must have its own unique Entity Id as well as an Entity Name. This is done by annotating the class in the following manner

```kotlin
@JdsEntityAnnotation(id = 1, name = "address", version = 1)
class Address : JdsEntity() {}
```

Entity IDs MUST be unique in your application, any value of type long is valid. Entity Names do not enforce unique constraints but its best to use a unique name regardless. These values can be referenced to mine data.

### 1.1.2 Defining Fields

Fields are big part of the JDS framework. Each Field MUST have a unique Field Id. Field Names do not enforce unique constraints but its best to use a unique name regardless. These values can be referenced to mine data. Every Field that you define can be one of the following types.

| JDS Field Type       | Java Type                                  | Description                                                  |
| -------------------- | ------------------------------------------ | ------------------------------------------------------------ |
| DATE_TIME_COLLECTION | Collection\<LocalDateTime\>                | Collection of type LocalDateTime                             |
| DOUBLE_COLLECTION    | Collection\<Double\>                       | Collection of type Double                                    |
| ENTITY_COLLECTION    | Collection\<Class\<? extends JdsEntity\>\> | Collection of type JdsEntity                                 |
| FLOAT_COLLECTION     | Collection\<Float\>                        | Collection of type Float                                     |
| INT_COLLECTION       | Collection\<Integer\>                      | Collection of type Integer                                   |
| LONG_COLLECTION      | Collection\<Long\>                         | Collection of type Long                                      |
| STRING_COLLECTION    | Collection\<String\>                       | Collection of type String                                    |
| BLOB                 | byte[] or InputStream                      | Blob values                                                  |
| BOOLEAN              | boolean / Boolean                          | Boolean values                                               |
| ENTITY               | Class\<? extends JdsEntity\>               | Object of type JdsEntity                                     |
| DATE_TIME            | LocalDateTime                              | DateTime instances based on the host machines local timezone |
| DATE                 | LocalDate                                  | Local date instances                                         |
| DOUBLE               | double / Double                            | Numeric double values                                        |
| DURATION             | Duration                                   | Object of type Duration                                      |
| ENUM_COLLECTION      | Collection\<Enum\>                         | Collection of type Enum                                      |
| ENUM                 | Enum                                       | Object of type Enum                                          |
| FLOAT                | float / Float                              | Numeric float values                                         |
| INT                  | int / Integer                              | Numeric integer values                                       |
| LONG                 | long / Long                                | Numeric long values                                          |
| MONTH_DAY            | MonthDay                                   | Object of type MonthDay                                      |
| PERIOD               | Period                                     | Object of type Period                                        |
| STRING               | String                                     | String values with no max limit                              |
| TIME                 | LocalTime                                  | Local time instances                                         |
| YEAR_MONTH           | YearMonth                                  | Object of type YearMonth                                     |
| ZONED_DATE_TIME      | ZonedDateTime                              | Zoned DateTime instances                                     |

I recommend defining your Fields as static constants

```kotlin
import io.github.subiyacryolite.jds.JdsField;
import io.github.subiyacryolite.jds.enumProperties.JdsFieldType;

object Fields {
    val STREET_NAME = JdsField(1, "street_name", JdsFieldType.STRING)
    val PLOT_NUMBER = JdsField(2, "plot_number", JdsFieldType.INT)
    val AREA_NAME = JdsField(3, "area_name", JdsFieldType.STRING)
    val PROVINCE_NAME = JdsField(4, "province_name", JdsFieldType.STRING)
    val CITY_NAME = JdsField(5, "city_name", JdsFieldType.STRING)
    val COUNTRY_NAME = JdsField(7, "country_name", JdsFieldType.STRING)
    val PRIMARY_ADDRESS_ENUM = JdsField(8, "primary_address_enum", JdsFieldType.ENUM)
    val TIME_OF_ENTRY = JdsField(9, "time_of_entry", JdsFieldType.TIME)
}
```

### 1.1.3 Defining Enums

Enums are an extension of Fields. However, they are designed for cases where one or more constant values are required. Usually these values would be represented by Check Boxes, Radio Buttons or Combo Boxes in a UI. In this example we will define the type of an address as an enumerated value with the following options (YES, NO).

First of all we'd have to define a standard Field of type ENUM.

```kotlin
import io.github.subiyacryolite.jds.JdsField
import io.github.subiyacryolite.jds.enums.JdsFieldType

public class Fields
{
    val PRIMARY_ADDRESS_ENUM = JdsField(8, "primary_address_enum", JdsFieldType.ENUM)
}
```

Then, we can define our actual enum in the following manner.

```kotlin
enum class PrimaryAddress {
    YES, NO
}
```

```kotlin
import io.github.subiyacryolite.jds.JdsFieldEnum

object Enums {
    val PRIMARY_ADDRESS_ENUM = JdsFieldEnum(PrimaryAddress::class.java, Fields.PRIMARY_ADDRESS_ENUM, *PrimaryAddress.values())
}
```

Behind the scenes these Enums will be stored as either an Integer (ENUM) or an Integer Array (ENUM_COLLECTION).

### 1.1.4 Binding Properties

Depending on the type of Field, JDS will require that you set you objects properties to one of the following JavaFX bean container types.

Kindly note that none of the JavaFX beans are serializable, however JDS supports serialization via the explicit implementation of Javas Externalizable interface. That said only Property values are serialized, not listeners or any other bean state.

| JDS Field Type       | Container                                                                                                            | Java Mapping Call |Kotlin Mapping Call |
| -------------------- | -------------------------------------------------------------------------------------------------------------------- | ------------ | ------------ |
| DATE_TIME_COLLECTION | [Collection\<LocalDateTime\>](https://docs.oracle.com/javase/8/docs/api/java/util/Collection.html)                   | mapDateTimes          | map          |
| DOUBLE_COLLECTION    | [Collection\<Double\>](https://docs.oracle.com/javase/8/docs/api/java/util/Collection.html)                          | mapDoubles          | map          |
| ENTITY_COLLECTION    | [Collection\<Class\<? extends JdsEntity\>\>](https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html)         | map          | map          |
| FLOAT_COLLECTION     | [Collection\<Float\>](https://docs.oracle.com/javase/8/docs/api/java/util/Collection.html)                           | mapFloats          | map          |
| INT_COLLECTION       | [Collection\<Integer\>](https://docs.oracle.com/javase/8/docs/api/java/util/Collection.html)                         | mapInts          | map          |
| LONG_COLLECTION      | [Collection\<Long\>](https://docs.oracle.com/javase/8/docs/api/java/util/Collection.html)                            | mapLongs          | map          |
| STRING_COLLECTION    | [Collection\<String\>](https://docs.oracle.com/javase/8/docs/api/java/util/Collection.html)                          | mapStrings          | map          |
| BOOLEAN              | [WritableValue\<Boolean\>](https://docs.oracle.com/javafx/2/api/javafx/beans/value/WritableValue.html)               | mapBoolean          | map          |
| BLOB                 | [WritableValue\<byte[]\>](https://static.javadoc.io/io.github.subiyacryolite/jds/3.4.3/javafx/beans/property/BlobProperty.html) | map          | map          |
| ENTITY               | [Class\<? extends JdsEntity\>](https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html)                       | map          | map          |
| DATE                 | [WritableValue\<LocalDate\>](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)                     | mapDate          | map          |
| DATE_TIME            | [WritableValue\<LocalDateTime\>](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDateTime.html)             | mapDateTime          | map          |
| DOUBLE               | [WritableValue\<Double\>](https://docs.oracle.com/javafx/2/api/javafx/beans/value/WritableValue.html)                | mapDouble          | map          |
| DURATION             | [WritableValue\<Duration\>](https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html)                       | mapDuration          | map          |
| ENUM                 | [WritableValue\<Enum\>](https://docs.oracle.com/javase/8/docs/api/java/lang/Enum.html)                               | mapEnum          | map          |
| ENUM_COLLECTION      | [Collection\<Enum\>](https://docs.oracle.com/javase/8/docs/api/java/lang/Enum.html)                                  | mapEnums          | map          |
| FLOAT                | [WritableValue\<Float\>](https://docs.oracle.com/javafx/2/api/javafx/beans/value/WritableValue.html)                 | mapFloat          | map          |
| INT                  | [WritableValue\<Integer\>](https://docs.oracle.com/javafx/2/api/javafx/beans/value/WritableValue.html)               | mapInt          | map          |
| LONG                 | [WritableValue\<Long\>](https://docs.oracle.com/javafx/2/api/javafx/beans/value/WritableValue.html)                  | mapLong          | map          |
| MONTH_DAY            | [WritableValue\<MonthDay\>](https://docs.oracle.com/javase/8/docs/api/java/time/MonthDay.html)                       | mapMonthDay          | map          |
| PERIOD               | [WritableValue\<Period\>](https://docs.oracle.com/javase/8/docs/api/java/time/Period.html)                           | mapPeriod          | map          |
| STRING               | [WritableValue\<String\>](https://docs.oracle.com/javafx/2/api/javafx/beans/property/StringProperty.html)            | mapString          | map          |
| TIME                 | [WritableValue\<LocalTime\>](https://docs.oracle.com/javase/8/docs/api/java/time/LocalTime.html)                     | mapTime          | map          |
| YEAR_MONTH           | [WritableValue\<YearMonth\>](https://docs.oracle.com/javase/8/docs/api/java/time/YearMonth.html)                     | mapYearMonth          | map          |
| ZONED_DATE_TIME      | [WritableValue\<ZonedDateTime\>](https://docs.oracle.com/javase/8/docs/api/java/time/ZonedDateTime.html)             | mapZonedDateTime          | map          |

**Note:** All supported primitive types (Boolean, Double, Float, Int, Long) can be persisted as nulls by providing your own implementation of WritableValue\<Number\> or using the helper classes: NullableBooleanProperty, NullableDoubleProperty, NullableFloatProperty, NullableIntegerProperty, NullableLongProperty and NullableNumberProperty.

**Note:** JDS assumes that all primitive collection types will not contain null entries.

After your class and its properties have been defined you must map the property to its corresponding Field using the **map()** method. I recommend doing this in your primary constructor.

The example below shows a class definition with valid properties and bindings. With this your class can be persisted.

```kotlin
import constants.Enums
import constants.Fields
import constants.PrimaryAddress
import io.github.subiyacryolite.jds.JdsEntity
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation
import javafx.beans.property.NullableIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import java.time.LocalTime

@JdsEntityAnnotation(id = 1, name = "address", version = 1)
class Address : JdsEntity() {
    private val _streetName = SimpleStringProperty("")
    private val _plotNumber = NullableIntegerProperty()
    private val _area = SimpleStringProperty("")
    private val _city = SimpleStringProperty("")
    private val _provinceOrState = SimpleStringProperty("")
    private val _country = SimpleStringProperty("")
    private val _primaryAddress = SimpleObjectProperty(PrimaryAddress.NO)
    private val _timeOfEntry = SimpleObjectProperty(LocalTime.now())

    init {
        map(Fields.STREET_NAME, _streetName)
        map(Fields.PLOT_NUMBER, _plotNumber)
        map(Fields.AREA_NAME, _area)
        map(Fields.CITY_NAME, _city)
        map(Fields.COUNTRY_NAME, _country)
        map(Fields.PROVINCE_NAME, _provinceOrState)
        map(Fields.TIME_OF_ENTRY, _timeOfEntry)
        map(Enums.PRIMARY_ADDRESS_ENUM, _primaryAddress)
    }

    var primaryAddress: PrimaryAddress
        get() = _primaryAddress.get()
        set(value) = _primaryAddress.set(value)

    var streetName: String
        get() = _streetName.get()
        set(value) = _streetName.set(value)

    var plotNumber: Int?
        get() = _plotNumber.value
        set(value) {
            _plotNumber.value = value
        }

    var area: String
        get() = _area.get()
        set(value) = _area.set(value)

    var city: String
        get() = _city.get()
        set(value) = _city.set(value)

    var provinceOrState: String
        get() = _provinceOrState.get()
        set(value) = _provinceOrState.set(value)

    var country: String
        get() = _country.get()
        set(value) = _country.set(value)

    var timeOfEntry: LocalTime
        get() = _timeOfEntry.get()
        set(timeOfEntry) = _timeOfEntry.set(timeOfEntry)

    override fun onPreSave(eventArgument: OnPreSaveEventArguments) {
        //Optional event i.e write to custom reporting tables, perform custom validation
        //Queries can be batched i.e
        //eventArgument.getOrAddStatement("Batched SQL to execute")
        //eventArgument.addBatch() -- this will be executed safely by jds
    }

    override fun onPostSave(eventArgument: OnPostSaveEventArguments) {
        //Optional event i.e write to custom reporting tables, perform custom validation
        //Queries can be batched i.e
        //eventArgument.getOrAddStatement("Batched SQL to execute")
        //eventArgument.addBatch() -- this will be executed safely by jds
    }

    override fun onPreLoad(eventArgument: OnPreLoadEventArguments) {
        //Optional event i.e write to custom reporting tables, perform custom validation
        //Queries can be batched i.e
        //eventArgument.getOrAddStatement("Batched SQL to execute")
        //eventArgument.addBatch() -- this will be executed safely by jds
    }

    override fun onPostLoad(eventArgument: OnPostLoadEventArguments) {
        //Optional event i.e write to custom reporting tables, perform custom validation
        //Queries can be batched i.e
        //eventArgument.getOrAddStatement("Batched SQL to execute")
        //eventArgument.addBatch() -- this will be executed safely by jds
    }

    override fun toString(): String {
        return "{" +
                "overview = $overview," +
                ", primaryAddress = $primaryAddress " +
                ", streetName = $streetName " +
                ", plotNumber = $plotNumber " +
                ", area = $area " +
                ", city = $city " +
                ", provinceOrState = $provinceOrState " +
                ", country = $country " +
                ", timeOfEntry = $timeOfEntry " +
                '}'
    }
}
```

### 1.1.5 Binding Objects and Object Arrays

JDS can also persist embedded objects and object arrays.

All that's required is a valid **JdsEntity** or **IJdsEntity** subclass to be mapped to a Field of type **ENTITY** or **ENTITY_COLLECTION** .

```kotlin
import io.github.subiyacryolite.jds.JdsField
import io.github.subiyacryolite.jds.enums.JdsFieldType

object Fields
{
    val ADDRESSES = JdsField(23, "addresses", JdsFieldType.ENTITY_COLLECTION)
}
```

```kotlin
import io.github.subiyacryolite.jds.JdsFieldEntity

object Entities {
    val ADDRESSES: JdsFieldEntity<Address> = JdsFieldEntity(Address::class.java, Fields.ADDRESSES)
}
```

```kotlin
import io.github.subiyacryolite.jds.JdsEntity
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections

@JdsEntityAnnotation(id = 2, name = "address_book")
class AddressBook : JdsEntity() {
    private val _addresses: SimpleListProperty<Address> = SimpleListProperty(FXCollections.observableArrayList())

    init {
        map(Entities.ADDRESSES, _addresses)
    }

    val addresses: MutableList<Address>
        get() = _addresses.get()

    override fun toString(): String = "{ addresses = $addresses }"
}
```

## 1.2 CRUD Operations

### 1.2.1 Initialising the database

In order to use JDS you will need an instance of JdsDb. Your instance of JdsDb will have to extend one of the following classes and override the **connection** property method: JdsDbMySql, JdsDbPostgreSql, JdsDbSqlite or JdsDbTransactionalSql.
Please note that your project must have the correct JDBC driver in its class path. The drivers that were used during development are listed under [Supported Databases](#supported-databases) above.

#### Postgres example

```kotlin
import io.github.subiyacryolite.jds.JdsDbPostgreSql
import java.io.File
import java.io.FileInputStream
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

class JdsDbPostgreSqlmplementation : JdsDbPostgreSql() {

    override val connection: Connection
        get () {
            Class.forName("org.postgresql.Driver")
            val properties = Properties()
            FileInputStream(File("db.pg.properties")).use { properties.load(it) }
            return DriverManager.getConnection("jdbc:postgresql://IP_ADDRESS:PORT/DATABASE", properties)
        }
}

fun initJds(){
    //jds declared in higher scope
    jdsDb = JdsDbPostgreSqlmplementation()
    jdsDb.init()
}
```

#### MySQL Example

```kotlin
import io.github.subiyacryolite.jds.JdsDbMySql
import java.io.File
import java.io.FileInputStream
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

class JdsDbMySqlImplementation : JdsDbMySql() {

    override val connection: Connection
        get () {
            Class.forName("com.mysql.cj.jdbc.Driver")
            val properties = Properties()
            FileInputStream(File("db.mysql.properties")).use { properties.load(it) }
            return DriverManager.getConnection("jdbc:mysql://IP_ADDRESS:PORT/DATABASE", properties)
        }
}

fun initJds(){
    //jds declared in higher scope
    jdsDb = JdsDbMySqlImplementation()
    jdsDb.init()
}
```

#### MariaDb Example

```kotlin
import io.github.subiyacryolite.jds.JdsDbMaria
import java.io.File
import java.io.FileInputStream
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

class JdsDbMariaImplementation : JdsDbMaria() {

    override val connection: Connection
        get () {
            Class.forName("org.mariadb.jdbc.Driver")
            val properties = Properties()
            FileInputStream(File("db.maria.properties")).use { properties.load(it) }
            return DriverManager.getConnection("jdbc:mariadb://IP_ADDRESS:PORT/DATABASE", properties)
        }
}

fun initJds(){
    //jds declared in higher scope
    jdsDb = JdsDbMariaImplementation()
    jdsDb.init()
}
```

#### Microsoft SQL Server Example

```kotlin
import io.github.subiyacryolite.jds.JdsDbTransactionalSql
import java.io.File
import java.io.FileInputStream
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

class JdsDbTransactionalSqllmplementation : JdsDbTransactionalSql() {

    override val connection: Connection
        get () {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
            val properties = Properties()
            FileInputStream(File("db.tsql.properties")).use { properties.load(it) }
            return DriverManager.getConnection("jdbc:sqlserver://IP_ADDRESS\\INSTANCE_NAME;databaseName=DATABASE_NAME", properties);
        }
}

fun initJds(){
    //jds declared in higher scope
    jdsDb = JdsDbTransactionalSqllmplementation()
    jdsDb.init()
}
```

#### Oracle Example

```kotlin
import io.github.subiyacryolite.jds.JdsDbOracle
import java.io.File
import java.io.FileInputStream
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

class JdsDbOracleImplementation : JdsDbOracle() {

    override val connection: Connection
        get () {
            Class.forName("oracle.jdbc.driver.OracleDriver")
            val properties = Properties()
            FileInputStream(File("db.ora.properties")).use { properties.load(it) }
            return DriverManager.getConnection("jdbc:oracle:thin:@IP_ADDRESS:PORT:DATABASE", properties)
        }
}

fun initJds(){
    //jds declared in higher scope
    val jdsDb = JdsDbOracleImplementation()
    jdsDb.init()
}
```

#### Sqlite Example

```kotlin
import io.github.subiyacryolite.jds.JdsDbSqlite
import org.sqlite.SQLiteConfig
import java.io.File
import java.sql.Connection
import java.sql.DriverManager

class JdsDbSqliteImplementation : JdsDbSqlite() {

    private val fileLocation: String
        get() {
            val path = File(System.getProperty("user.home") + File.separator + "DATABASE")
            if (!path.exists()) {
                val directory = path.parentFile
                if (!directory.exists()) {
                    directory.mkdirs()
                }
            }
            return path.absolutePath
        }

    override val connection: Connection
        get () {
            val url = "jdbc:sqlite:$fileLocation"
            val sqLiteConfig = SQLiteConfig()
            sqLiteConfig.enforceForeignKeys(true) //You must enable foreign keys in SQLite
            Class.forName("org.sqlite.JDBC")
            return DriverManager.getConnection(url, sqLiteConfig.toProperties())
        }
}

fun initJds(){
    //jds declared in higher scope
    jdsDb = JdsDbSqliteImplementation()
    jdsDb.init()
}
```

With this you should have a valid connection to your database and JDS will setup its tables and procedures automatically. Furthermore, you can use the **getConnection()** method OR **connection** property from your JdsDb instance in order to return a standard **java.sql.Connection** in your application.

### 1.2.2 Initialising JDS

Once you have initialised your database you can go ahead and initialise all your JDS classes. You can achieve this by mapping ALL your JDS classes in the following manner.

```kotlin
fun initialiseJdsClasses(jdsDb: JdsDb)
{
    jdsDb.map(Address::class.java);
    jdsDb.map(AddressBook::class.java);
}
```

You only have to do this once at start-up. Without this you will not be able to persist or load data.

### 1.2.3 Creating objects

Once you have defined your class you can initialise them. A dynamic **Entity Guid** is created for every Entity by default, this value is used to uniquely identify an object and it data in the database. You can set your own values if you wish.

```kotlin
    val primaryAddress = Address()
    primaryAddress.overview.uuid = "primaryAddress" //explicit uuid defined, JDS assigns a value by default on instantiation
    primaryAddress.area = "Norte Broad"
    primaryAddress.city = "Livingstone"
    primaryAddress.country = "Zambia"
    primaryAddress.plotNumber = null
    primaryAddress.provinceOrState = "Southern"
    primaryAddress.streetName = "East Street"
    primaryAddress.timeOfEntry = LocalTime.now()
    primaryAddress.primaryAddress = PrimaryAddress.YES
```

### 1.2.4 Save

The API has a single **save()** method within the class **JdsSave**. The method can takes the following arguments: **Iterable\<JdsEntity\> entities**, an optional **connection**, and an optional **JdsSaveEvent**. JdsSave extends **Callable** and thus can be wrapped in **Futures** and **Runnables**.

```kotlin
fun save() {
      val jdsSave = JdsSave(jdsDb, Arrays.asList(addressBook))
      jdsSave.call()
}
```

### 1.2.5 Load

The system currently has three variants of the **load()** method within the class **JdsLoad**. The first variant loads ALL the instances of a JdsEntity class. The second variant loads ALL the instances of a JdsEntity class with matching Entity Guids which are supplied by the user. The second variant adds an optional parameter "Comparator<? extends JdsEntity>" which allows you to load a sorted collection. JdsLoad extends **Callable** and thus can be wrapped in **Futures** and **Runnables**.

```kotlin
fun load() {
    val loadAllInstances = JdsLoad(jdsDb, Example::class.java)
    val loadSpecificInstance = JdsLoad(jdsDb, Example::class.java, "instance3")
    val loadSortedInstances = JdsLoad(jdsDb, Example::class.java)

    val executorService = Executors.newFixedThreadPool(3)
    val loadAllInstances = executorService.submit(loadAllInstances)
    val loadSpecificInstance = executorService.submit(loadSpecificInstance)
    val loadSortedInstances = executorService.submit(loadSortedInstances)

    while (!loadAllInstances.isDone)
        Thread.sleep(16)
    while (!loadSpecificInstance.isDone)
        Thread.sleep(16)
    while (!loadSortedInstances.isDone)
        Thread.sleep(16)

    val allInstances = loadAllInstances.get()
    val specificInstance = loadSpecificInstance.get()
    val sortedInstances = loadSortedInstances.get()

    println(allInstances)
    println(specificInstance)
    println(sortedInstances)
}
```

### 1.2.6 Load with Filter

A filter mechanism is present. This feature is basic and is still **being refined**. An example as to how the Filter API can be used is shown below. The filter mechanism only works when JDS is persisting data in EAV mode

```kotlin
fun filter(){
   val filter = JdsFilter(jdsDb, Address::class.java).between(Fields.PLOT_NUMBER, 1, 2).like(Fields.COUNTRY_NAME, "Zam").or().equals(Fields.PROVINCE_NAME, "Copperbelt")
   val process = Executors.newSingleThreadExecutor().submit(filter)
   while (!process.isDone)
       Thread.sleep(16)
   println(process.get())
}
```

### 1.2.7 Delete

You can delete by providing one or more JdsEntities.

```kotlin
fun delete() {
    val delete = JdsDelete(jdsDb, addressBook)
    val process = Executors.newSingleThreadExecutor().submit(delete)
    while (!process.isDone)
        Thread.sleep(16)
    println("Deleted successfully?  ${process.get()}")
}
```

## 1.3 Schema Generation

Starting with version 4 JDS can be set up to create Schemas to represent records in tabular format. These “JdsTables” pull predefined fields from one or more registered entities. On save and/or delete these tables are updated accordingly. Every JdsTable must be registered to an instance of JdsDb.

Below is an example of a JdsTable that will persist two specific fields from the Address entity type.

```kotlin
fun mapAndPrepareTablesWithSpecificFields(){

    val customTable = JdsTable()
    customTable.name = "address_specific"
    customTable.registerEntity(Address::class.java)
    customTable.registerField(Fields.AREA_NAME)
    customTable.registerField(Fields.CITY_NAME)
    customTable.uniqueBy = JdsFilterBy.UUID

    //register table
    jdsDb.mapTable(customTable)

    //after all tables have been mapped call this function
    jdsDb.prepareTables()
}
```

Below is an example of a JdsTable that will persist **all the fields** in the Address entity type

```kotlin
fun mapAndPrepareTablesWithAllFields(){
    val crtAddress = JdsTable(Address::class.java, true)
    customTable.isStoringLiveRecordsOnly = true

    //register table
    jdsDb.mapTable(customTable)

    //after all tables have been mapped call this function
    jdsDb.prepareTables()
}
```

You can define your JdsTables in code or you may deserialize them in JSON format

```json
{
  "name": "json_report",
  "storingLiveRecordsOnly": true,
  "entities": [1, 3],
  "fields": [3, 5, 7, 4]
}
```

## 1.4 Backend Design

The underlying database is based off of the star schema.

Each **jds*str*** table stores live data and are always up-to-date.

The **jds*ref*** tables are used as reference tables.

The **jds*entity*** tables are used to store all high level meta-data regarding every Jds Entity in the database.

<img src="https://github.com/SubiyaCryolite/jds/blob/master/database_design.svg" width="100%"/>

# Development

I highly recommend the use of the [IntelliJ IDE](https://www.jetbrains.com/idea/download/) for development.

# Contributing to Jenesis Data Store

If you would like to contribute code you can do so through [Github](https://github.com/SubiyaCryolite/Jenesis-Data-Store/) by forking the repository and sending a pull request targeting the current development branch.

When submitting code, please make every effort to follow existing conventions and style in order to keep the code as readable as possible.

# Bugs and Feedback

For bugs, questions and discussions please use the [Github Issues](https://github.com/SubiyaCryolite/Jenesis-Data-Store/issues).

# Special Thanks

To all our users and contributors!
