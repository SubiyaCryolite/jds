[![Build Status](https://travis-ci.org/SubiyaCryolite/jds.svg?branch=master)](https://travis-ci.org/SubiyaCryolite/jds)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.subiyacryolite/jds/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.subiyacryolite/jds)
[![Javadocs](https://javadoc.io/badge/io.github.subiyacryolite/jds.svg)](https://javadoc.io/doc/io.github.subiyacryolite/jds)
[![License](https://img.shields.io/badge/License-BSD--3--Clause-blue.svg)](LICENSE.md)
![Size](https://github-size-badge.herokuapp.com/subiyacryolite/jds.svg)
<!--- [![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/oss.sonatype.org/io.github.subiyacryolite/jds.svg)](https://oss.sonatype.org/content/repositories/snapshots/io/github/subiyacryolite/jds/))--->

# Jenesis Data Store

Jenesis Data Store (JDS) was created to help developers persist their classes to relational databases in a fast and reliable manner, without requiring them to design elaborate relational schemas. JDS has two main aims:

 - To allow for the rapid creation and modification of Java and/or Kotlin classes in order to facilitate rapid development; and
 - To provide a flexible data store framework to persist data against: (a) an EAV based database or (b) serialized JSON.

The library eliminates the need to modify schemas once a class has been altered. It also eliminates all concerns regarding "breaking changes" in regards to fields and their addition and/or removal. Fields, Objects and Collection types can be added, modified or removed at will. Beyond that the libraries data is structured in a way to promote fast and efficient Data Mining queries that can be used to support the application in question or to feed into specialised analytic software.

Put simply, JDS is useful for any developer that requires a flexible schema running on top of a traditional Relational Database.

JDS is licensed under the [3-Clause BSD License](https://opensource.org/licenses/BSD-3-Clause)

# Design

The concept behind JDS is quite simple. Extend a base “Entity” class, define strongly-typed “Fields” and then “Map” them against instances of the [WritableValue](https://openjfx.io/javadoc/11/javafx.base/javafx/beans/value/WritableValue.html) interface.

## Features

* Transparent persistence
* Serialization of JavaFX bean _values_
* Supports the persistence of NULL values for JVM primitive types
* Full support for generics and inheritance
* Easily integrates with new or existing databases
* Save, Updates and Deletes cascade to child objects and collections
* All saves and deletes are ACID (transaction based)
* Eager Loading is applied to embedded objects as well as on collections
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
    <version>14.0.1-SNAPSHOT</version>
</dependency>
```

Gradle

```groovy
compile 'io.github.subiyacryolite:jds:14.0.1-SNAPSHOT'
```

# Dependencies

The library depends on Java 11. Both 64 and 32 bit variants should suffice. Both the Development Kit and Runtime can be downloaded from [here](https://adoptopenjdk.net/).

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

Classes that use JDS need to extend Entity.

```kotlin
import io.github.subiyacryolite.jds.Entity;

public class Address extends Entity(){}
```

However, if you plan on using interfaces they must extend IEntity. Concrete classes can then extend Entity

```kotlin
import io.github.subiyacryolite.jds.Entity;
import io.github.subiyacryolite.jds.IEntity;

public interface IAddress extends IEntity{}
public class Address extends Entity implements IAddress{}
```

Following that the following steps need to be taken.

### 1.1.1 Annotating Classes

Every class that extends Entity must have its own unique Entity Id as well as an Entity Name. This is done by annotating the class in the following manner

```kotlin
@EntityAnnotation(id = 1, name = "address", description = "An entity representing address information")
class Address : Entity() {}
```

Entity IDs MUST be unique in your application, any value of type long is valid. Entity Names do not enforce unique constraints but its best to use a unique name regardless. These values can be referenced to mine data.

### 1.1.2 Defining Fields

Fields are big part of the JDS framework. Each Field MUST have a unique Field Id. Field Names do not enforce unique constraints but its best to use a unique name regardless. These values can be referenced to mine data. Every Field that you define can be one of the following types.

| JDS Field Type       | Java Type                                  | Description                                                  |
| -------------------- | ------------------------------------------ | ------------------------------------------------------------ |
| DateTimeCollection   | Collection\<LocalDateTime\>                | Collection of type LocalDateTime                             |
| DoubleCollection     | Collection\<Double\>                       | Collection of type Double                                    |
| EntityCollection     | Collection\<Class\<? extends Entity\>\> | Collection of type Entity                                 |
| FloatCollection      | Collection\<Float\>                        | Collection of type Float                                     |
| IntCollection        | Collection\<Integer\>                      | Collection of type Integer                                   |
| LongCollection       | Collection\<Long\>                         | Collection of type Long                                      |
| StringCollection     | Collection\<String\>                       | Collection of type String                                    |
| Blob                 | byte[] or InputStream                      | Blob values                                                  |
| Boolean              | boolean / Boolean                          | Boolean values                                               |
| Entity               | Class\<? extends Entity\>               | Object of type Entity                                     |
| DateTime             | LocalDateTime                              | DateTime instances based on the host machines local timezone |
| Date                 | LocalDate                                  | Local date instances                                         |
| Double               | double / Double                            | Numeric double values                                        |
| Duration             | Duration                                   | Object of type Duration                                      |
| EnumCollection       | Collection\<Enum\>                         | Collection of type Enum                                      |
| Enum                 | Enum                                       | Object of type Enum                                          |
| Float                | float / Float                              | Numeric float values                                         |
| Int                  | int / Integer                              | Numeric integer values                                       |
| Long                 | long / Long                                | Numeric long values                                          |
| MonthDay             | MonthDay                                   | Object of type MonthDay                                      |
| Period               | Period                                     | Object of type Period                                        |
| String               | String                                     | String values with no max limit                              |
| Time                 | LocalTime                                  | Local time instances                                         |
| YearMonth            | YearMonth                                  | Object of type YearMonth                                     |
| ZonedDateTime        | ZonedDateTime                              | Zoned DateTime instances                                     |

We recommend defining your Fields as static constants

```kotlin
import io.github.subiyacryolite.jds.Field;
import io.github.subiyacryolite.jds.enumProperties.FieldType;

object Fields {
    val StreetName = Field(1, "street_name", FieldType.String)
    val PlotNumber = Field(2, "plot_number", FieldType.Int)
    val Area = Field(3, "area_name", FieldType.String)
    val ProvinceOrState = Field(4, "province_name", FieldType.String)
    val City = Field(5, "city_name", FieldType.String)
    val Country = Field(7, "country_name", FieldType.String)
    val PrimaryAddress = Field(8, "primary_address", FieldType.Boolean)
    val Timestamp = Field(9, "timestamp", FieldType.DateTime)
}
```

### 1.1.3 Defining Enums

Enums are an extension of Fields. However, they are designed for cases where one or more constant values are required. Usually these values would be represented by Check Boxes, Radio Buttons or Combo Boxes in a UI. In this example we will define the type of an address as an enumerated value with the following options (YES, NO).

First of all we'd have to define a standard Field of type Enum.

```kotlin
import io.github.subiyacryolite.jds.Field
import io.github.subiyacryolite.jds.enums.FieldType

public class Fields
{
    val Direction = Field(10, "direction", FieldType.Enum)
}
```

Then, we can define our actual enum in the following manner.

```kotlin
enum class Direction {
    North, West, South, East
}
```

```kotlin
import io.github.subiyacryolite.jds.FieldEnum

object Enums {
    val Directions = FieldEnum(Direction::class.java, Fields.Direction, *Direction.values())
}
```

Behind the scenes these Enums will be stored as either:
 - an Integer (FieldType.Enum);
 - a String (FieldType.EnumString);
 - an Integer Array (FieldType.EnumCollection); or
 - a String Collection (FieldType.EnumStringCollection)

### 1.1.4 Binding Properties

Depending on the type of Field, JDS will require that you set you objects properties to one of the following JavaFX bean container types.

Kindly note that none of the JavaFX beans are serializable, however JDS supports serialization via the explicit implementation of Javas Externalizable interface. That said only Property values are serialized, not listeners or any other bean state.

| JDS Field Type       | Container                                                                                                            | Java Mapping Call |Kotlin Mapping Call |
| -------------------- | -------------------------------------------------------------------------------------------------------------------- | ------------ | ------------ |
| DateTimeCollection   | [Collection\<LocalDateTime\>](https://docs.oracle.com/javase/8/docs/api/java/util/Collection.html)                   | mapDateTimes          | map          |
| DoubleCollection     | [Collection\<Double\>](https://docs.oracle.com/javase/8/docs/api/java/util/Collection.html)                          | mapDoubles          | map          |
| EntityCollection     | [Collection\<Class\<? extends Entity\>\>](https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html)         | map          | map          |
| FloatCollection      | [Collection\<Float\>](https://docs.oracle.com/javase/8/docs/api/java/util/Collection.html)                           | mapFloats          | map          |
| IntCollection        | [Collection\<Integer\>](https://docs.oracle.com/javase/8/docs/api/java/util/Collection.html)                         | mapInts          | map          |
| LongCollection       | [Collection\<Long\>](https://docs.oracle.com/javase/8/docs/api/java/util/Collection.html)                            | mapLongs          | map          |
| StringCollection     | [Collection\<String\>](https://docs.oracle.com/javase/8/docs/api/java/util/Collection.html)                          | mapStrings          | map          |
| Boolean              | [WritableValue\<Boolean\>](https://docs.oracle.com/javafx/2/api/javafx/beans/value/WritableValue.html)               | mapBoolean          | map          |
| Blob                 | [WritableValue\<ByteArray\>](https://static.javadoc.io/io.github.subiyacryolite/jds/3.4.3/javafx/beans/property/BlobProperty.html) | map          | map          |
| Entity               | [Class\<? extends Entity\>](https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html)                       | map          | map          |
| Date                 | [WritableValue\<LocalDate\>](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)                     | mapDate          | map          |
| DateTime             | [WritableValue\<LocalDateTime\>](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDateTime.html)             | mapDateTime          | map          |
| Double               | [WritableValue\<Double\>](https://docs.oracle.com/javafx/2/api/javafx/beans/value/WritableValue.html)                | mapNumeric          | map          |
| Duration             | [WritableValue\<Duration\>](https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html)                       | mapDuration          | map          |
| Enum                 | [WritableValue\<Enum\>](https://docs.oracle.com/javase/8/docs/api/java/lang/Enum.html)                               | mapEnum          | map          |
| EnumCollection       | [Collection\<Enum\>](https://docs.oracle.com/javase/8/docs/api/java/lang/Enum.html)                                  | mapEnums          | map          |
| Float                | [WritableValue\<Float\>](https://docs.oracle.com/javafx/2/api/javafx/beans/value/WritableValue.html)                 | mapNumeric          | map          |
| Int                  | [WritableValue\<Integer\>](https://docs.oracle.com/javafx/2/api/javafx/beans/value/WritableValue.html)               | mapNumeric          | map          |
| Long                 | [WritableValue\<Long\>](https://docs.oracle.com/javafx/2/api/javafx/beans/value/WritableValue.html)                  | mapNumeric          | map          |
| MonthDay             | [WritableValue\<MonthDay\>](https://docs.oracle.com/javase/8/docs/api/java/time/MonthDay.html)                       | mapMonthDay          | map          |
| Period               | [WritableValue\<Period\>](https://docs.oracle.com/javase/8/docs/api/java/time/Period.html)                           | mapPeriod          | map          |
| String               | [WritableValue\<String\>](https://docs.oracle.com/javafx/2/api/javafx/beans/property/StringProperty.html)            | mapString          | map          |
| Time                 | [WritableValue\<LocalTime\>](https://docs.oracle.com/javase/8/docs/api/java/time/LocalTime.html)                     | mapTime          | map          |
| YearMonth            | [WritableValue\<YearMonth\>](https://docs.oracle.com/javase/8/docs/api/java/time/YearMonth.html)                     | mapYearMonth          | map          |
| ZonedDateTime        | [WritableValue\<ZonedDateTime\>](https://docs.oracle.com/javase/8/docs/api/java/time/ZonedDateTime.html)             | mapZonedDateTime          | map          |

**Note:** All supported primitive types (Boolean, Double, Float, Int, Long) can be persisted as nulls by providing your own implementation of WritableValue\<Number\> or using the helper classes: NullableBooleanProperty, NullableDoubleProperty, NullableFloatProperty, NullableIntegerProperty, NullableLongProperty and NullableNumberProperty.

**Note:** JDS assumes that all primitive collection types will not contain null entries.

After your class and its properties have been defined you must map the property to its corresponding Field using the **map()** method. I recommend doing this in your primary constructor.

The example below shows a class definition with valid properties and bindings. With this your class can be persisted.

```kotlin
import io.github.subiyacryolite.jds.Entity
import io.github.subiyacryolite.jds.annotations.EntityAnnotation
import io.github.subiyacryolite.jds.beans.property.NullableBooleanProperty
import io.github.subiyacryolite.jds.beans.property.NullableShortProperty
import io.github.subiyacryolite.jds.tests.constants.Fields
import javafx.beans.property.*
import javafx.beans.value.WritableValue
import java.time.LocalDateTime

@EntityAnnotation(id = 1, name = "address", description = "An entity representing address information")
data class Address(
        private val _streetName: StringProperty = SimpleStringProperty(""),
        private val _plotNumber: NullableShortProperty = NullableShortProperty(),
        private val _residentialArea: StringProperty = SimpleStringProperty(""),
        private val _city: StringProperty = SimpleStringProperty(""),
        private val _provinceOrState: StringProperty = SimpleStringProperty(""),
        private val _country: StringProperty = SimpleStringProperty(""),
        private val _primaryAddress: WritableValue<Boolean?> = NullableBooleanProperty(null),
        private val _timestamp: ObjectProperty<LocalDateTime> = SimpleObjectProperty(LocalDateTime.now())
) : Entity() {

    init {
        map(Fields.StreetName, _streetName)
        map(Fields.PlotNumber, _plotNumber)
        map(Fields.ResidentialArea, _residentialArea)
        map(Fields.City, _city)
        map(Fields.Country, _country)
        map(Fields.ProvinceOrState, _provinceOrState)
        map(Fields.TimeStamp, _timestamp)
        map(Fields.PrimaryAddress, _primaryAddress)
    }

    var primaryAddress: Boolean?
        get() = _primaryAddress.get()
        set(value) = _primaryAddress.set(value)

    var streetName: String
        get() = _streetName.get()
        set(value) = _streetName.set(value)

    var plotNumber: Short?
        get() = _plotNumber.get()
        set(value) = _plotNumber.set(value)

    var residentialArea: String
        get() = _residentialArea.get()
        set(value) = _residentialArea.set(value)

    var city: String
        get() = _city.get()
        set(value) = _city.set(value)

    var provinceOrState: String
        get() = _provinceOrState.get()
        set(value) = _provinceOrState.set(value)

    var country: String
        get() = _country.get()
        set(value) = _country.set(value)

    var timeOfEntry: LocalDateTime
        get() = _timestamp.get()
        set(timeOfEntry) = _timestamp.set(timeOfEntry)
}
```

Alternatively you can use this shorthand to define and map your properties with one command. This approach returns a strongly typed instance of WritableValue determined by which value (e.g. "") or container (e.g. NullableShortProperty) you pass in an initial parameter.

```kotlin
import io.github.subiyacryolite.jds.Entity
import io.github.subiyacryolite.jds.annotations.EntityAnnotation
import io.github.subiyacryolite.jds.beans.property.NullableBooleanProperty
import io.github.subiyacryolite.jds.beans.property.NullableShortProperty
import io.github.subiyacryolite.jds.tests.constants.Fields
import java.time.LocalDateTime

@EntityAnnotation(id = 1, name = "address", description = "An entity representing address information")
class Address : Entity() {

    private val _streetName = map(Fields.StreetName, "")
    private val _plotNumber = map(Fields.PlotNumber, NullableShortProperty())
    private val _residentialArea = map(Fields.ResidentialArea, "")
    private val _city = map(Fields.City, "")
    private val _provinceOrState = map(Fields.ProvinceOrState, "")
    private val _country = map(Fields.Country, "")
    private val _primaryAddress = map(Fields.TimeStamp, NullableBooleanProperty())
    private val _timestamp = map(Fields.TimeStamp, LocalDateTime.now())

    var primaryAddress: Boolean?
        get() = _primaryAddress.get()
        set(value) = _primaryAddress.set(value)

    var streetName: String
        get() = _streetName.get()
        set(value) = _streetName.set(value)

    var plotNumber: Short?
        get() = _plotNumber.get()
        set(value) = _plotNumber.set(value)

    var residentialArea: String
        get() = _residentialArea.get()
        set(value) = _residentialArea.set(value)

    var city: String
        get() = _city.get()
        set(value) = _city.set(value)

    var provinceOrState: String
        get() = _provinceOrState.get()
        set(value) = _provinceOrState.set(value)

    var country: String
        get() = _country.get()
        set(value) = _country.set(value)

    var timeOfEntry: LocalDateTime
        get() = _timestamp.get()!!
        set(timeOfEntry) = _timestamp.set(timeOfEntry)
}
```

### 1.1.5 Binding Objects and Object Arrays

JDS can also persist embedded objects and object arrays.

All that's required is a valid **Entity** or **IEntity** subclass to be mapped to a Field of type **Entity** or **EntityCollection** .

```kotlin
import io.github.subiyacryolite.jds.Field
import io.github.subiyacryolite.jds.enums.FieldType

object Fields
{
    val Addresses = Field(23, "addresses", FieldType.EntityCollection)
}
```

```kotlin
import io.github.subiyacryolite.jds.FieldEntity

object Entities {
    val Addresses: FieldEntity<Address> = FieldEntity(Address::class.java, Fields.Addresses)
}
```

```kotlin
import io.github.subiyacryolite.jds.Entity
import io.github.subiyacryolite.jds.annotations.EntityAnnotation
import io.github.subiyacryolite.jds.tests.constants.Entities

@EntityAnnotation(id = 2, name = "address_book")
data class AddressBook(
        val addresses: MutableCollection<Address> = ArrayList()
) : Entity() {

    init {
        map(Entities.Addresses, addresses)
    }
}
```

## 1.2 CRUD Operations

### 1.2.1 Initialising the database

In order to use JDS you will need an instance of DbContext. Your instance of DbContext will have to extend one of the following classes: 
 - MariaDbContext;
 - MySqlContext;
 - OracleContext;
 - SqLiteDbContext; or
 - TransactionalSqlContext

After this you must override the **dataSource** property.

Please note that your project must have the correct JDBC driver in its class path. The drivers that were used during development are listed under [Supported Databases](#supported-databases) above.

These samples use [HikariCP](https://github.com/brettwooldridge/HikariCP) to provide connection pooling for enhanced performance.

#### Postgres example

```kotlin
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.subiyacryolite.jds.context.PostGreSqlContext
import java.io.File
import java.io.FileInputStream
import java.util.*
import javax.sql.DataSource

class PostGreSqlContextImplementation : PostGreSqlContext() {

    private val properties: Properties = Properties()
    private val hikariDataSource: DataSource

    init {
        FileInputStream(File("db.pg.properties")).use { properties.load(it) }

        val hikariConfig = HikariConfig()
        hikariConfig.driverClassName = properties["driverClassName"].toString()
        hikariConfig.maximumPoolSize = properties["maximumPoolSize"].toString().toInt()
        hikariConfig.username = properties["username"].toString()
        hikariConfig.password = properties["password"].toString()
        hikariConfig.dataSourceProperties = properties //additional props
        hikariConfig.jdbcUrl = "jdbc:postgresql://${properties["dbUrl"]}:${properties["dbPort"]}/${properties["dbName"]}"
        hikariDataSource = HikariDataSource(hikariConfig)
    }

    override val dataSource: DataSource
        get () = hikariDataSource
}
```

#### MySQL Example

```kotlin
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.subiyacryolite.jds.context.MySqlContext
import java.io.File
import java.io.FileInputStream
import java.util.*
import javax.sql.DataSource

class MySqlContextImplementation : MySqlContext() {

    private val properties: Properties = Properties()
    private val hikariDataSource: DataSource

    init {
        FileInputStream(File("db.mysql.properties")).use { properties.load(it) }

        val hikariConfig = HikariConfig()
        hikariConfig.driverClassName = properties["driverClassName"].toString()
        hikariConfig.maximumPoolSize = properties["maximumPoolSize"].toString().toInt()
        hikariConfig.username = properties["username"].toString()
        hikariConfig.password = properties["password"].toString()
        hikariConfig.dataSourceProperties = properties //additional props
        hikariConfig.jdbcUrl = "jdbc:mysql://${properties["dbUrl"]}:${properties["dbPort"]}/${properties["dbName"]}"
        hikariDataSource = HikariDataSource(hikariConfig)
    }

    override val dataSource: DataSource
        get () = hikariDataSource
}
```

#### MariaDb Example

```kotlin
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.subiyacryolite.jds.context.MariaDbContext
import java.io.File
import java.io.FileInputStream
import java.util.*
import javax.sql.DataSource

class MariaDbContextImplementation : MariaDbContext() {

    private val properties: Properties = Properties()
    private val hikariDataSource: DataSource

    init {
        FileInputStream(File("db.mariadb.properties")).use { properties.load(it) }

        val hikariConfig = HikariConfig()
        hikariConfig.driverClassName = properties["driverClassName"].toString()
        hikariConfig.maximumPoolSize = properties["maximumPoolSize"].toString().toInt()
        hikariConfig.username = properties["username"].toString()
        hikariConfig.password = properties["password"].toString()
        hikariConfig.dataSourceProperties = properties //additional props
        hikariConfig.jdbcUrl = "jdbc:mariadb://${properties["dbUrl"]}:${properties["dbPort"]}/${properties["dbName"]}"

        hikariDataSource = HikariDataSource(hikariConfig)
    }

    override val dataSource: DataSource
        get () = hikariDataSource
}
```

#### Microsoft SQL Server Example

```kotlin
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.subiyacryolite.jds.context.TransactionalSqlContext
import java.io.File
import java.io.FileInputStream
import java.util.*
import javax.sql.DataSource

class TransactionalSqlContextImplementation : TransactionalSqlContext() {

    private val properties: Properties = Properties()
    private val hikariDataSource: DataSource

    init {
        FileInputStream(File("db.tsql.properties")).use { properties.load(it) }

        val hikariConfig = HikariConfig()
        hikariConfig.driverClassName = properties["driverClassName"].toString()
        hikariConfig.maximumPoolSize = properties["maximumPoolSize"].toString().toInt()
        hikariConfig.username = properties["username"].toString()
        hikariConfig.password = properties["password"].toString()
        hikariConfig.jdbcUrl = "jdbc:sqlserver://${properties["dbUrl"]}\\${properties["dbInstance"]};databaseName=${properties["dbName"]}"
        hikariConfig.dataSourceProperties = properties //additional props
        hikariDataSource = HikariDataSource(hikariConfig)
    }

    override val dataSource: DataSource
        get () = hikariDataSource
}
```

#### Oracle Example

```kotlin
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.subiyacryolite.jds.context.OracleContext
import java.io.File
import java.io.FileInputStream
import java.util.*
import javax.sql.DataSource

class OracleContextImplementation : OracleContext() {

    private val properties: Properties = Properties()
    private val hikariDataSource: DataSource

    init {
        FileInputStream(File("db.ora.properties")).use { properties.load(it) }

        val hikariConfig = HikariConfig()
        hikariConfig.driverClassName = properties["driverClassName"].toString()
        hikariConfig.maximumPoolSize = properties["maximumPoolSize"].toString().toInt()
        hikariConfig.username = properties["username"].toString()
        hikariConfig.password = properties["password"].toString()
        hikariConfig.dataSourceProperties = properties //additional props
        hikariConfig.jdbcUrl = "jdbc:oracle:thin:@${properties["dbUrl"]}:${properties["dbPort"]}:${properties["dbName"]}"
        hikariDataSource = HikariDataSource(hikariConfig)
    }

    override val dataSource: DataSource
        get () = hikariDataSource
}
```

#### Sqlite Example

```kotlin
import io.github.subiyacryolite.jds.context.SqLiteDbContext
import org.sqlite.SQLiteConfig
import org.sqlite.SQLiteDataSource
import java.io.File
import javax.sql.DataSource

class SqLiteDbContextImplementation : SqLiteDbContext() {

    private val sqLiteDataSource: DataSource

    init {
        val path = File(System.getProperty("user.home") + File.separator + ".jdstest" + File.separator + "jds.db")
        if (!path.exists())
            if (!path.parentFile.exists())
                path.parentFile.mkdirs()
        val sqLiteConfig = SQLiteConfig()
        sqLiteConfig.enforceForeignKeys(true) //You must enable foreign keys in SQLite
        sqLiteDataSource = SQLiteDataSource(sqLiteConfig)
        sqLiteDataSource.url = "jdbc:sqlite:${path.absolutePath}"
    }

    override val dataSource: DataSource
        get () {
            Class.forName("org.sqlite.JDBC")
            return sqLiteDataSource
        }
}
```

With this you should have a valid data source allowing you to access your database. JDS will automatically setup its tables and procedures at runtime.

Furthermore, you can use the **getConnection()** method OR **connection** property from this dataSource property in order to return a standard **java.sql.Connection** in your application.

### 1.2.2 Initialising JDS

Once you have initialised your database you can go ahead and initialise all your JDS classes. You can achieve this by mapping ALL your JDS classes in the following manner.

```kotlin
fun initialiseJdsClasses(dbContext: DbContext)
{
    dbContext.map(Address::class.java);
    dbContext.map(AddressBook::class.java);
}
```

You only have to do this **once** at start-up. Without this you will not be able to persist or load data.

### 1.2.3 Creating objects

Once you have defined your class you can initialise them. A dynamic **id** is created for every Entity by default (using javas UUID class). This value is used to uniquely identify an object and it data in the database. You can set your own values if you wish.

```kotlin
    val primaryAddress = Address()
    primaryAddress.overview.id = "primaryAddress" //explicit id defined, JDS assigns a value by default on instantiation
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

The API has a single **save()** method within the class **Save**. The method can takes the following arguments: **Iterable\<Entity\> entities**, an optional **connection**, and an optional **SaveEvent**. Save extends **Callable** and thus can be wrapped in **Futures** and **Runnables**.

```kotlin
fun save() {
      val jdsSave = Save(dbContext, listOf(addressBook))
      jdsSave.call()
}
```

### 1.2.5 Load

The system currently has three variants of the **load()** method within the class **Load**. The first variant loads ALL the instances of a Entity class. The second variant loads ALL the instances of a Entity class with matching Entity Guids which are supplied by the user. The second variant adds an optional parameter "Comparator<? extends Entity>" which allows you to load a sorted collection. Load extends **Callable** and thus can be wrapped in **Futures** and **Runnables**.

```kotlin
fun load() {
    val loadAllInstances = Load(dbContext, Example::class.java)
    val loadSpecificInstance = Load(dbContext, Example::class.java, "instance3")
    val loadSortedInstances = Load(dbContext, Example::class.java)

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
   val filter = Filter(dbContext, Address::class.java).between(Fields.PlotNumber, 1, 2).like(Fields.Country, "Zam").or().equals(Fields.Province, "Copperbelt")
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
    val delete =  Delete(dbContext, addressBook)
    val process = Executors.newSingleThreadExecutor().submit(delete)
    while (!process.isDone)
        Thread.sleep(16)
    println("Deleted successfully?  ${process.get()}")
}
```

## 1.3 Schema Generation

Starting with version 4 JDS can be set up to create Schemas to represent records in tabular format. These “Tables” pull predefined fields from one or more registered entities. On save and/or delete these tables are updated accordingly. Every Table must be registered to an instance of DbContext.

Below is an example of a Table that will persist two specific fields from the Address entity type.

```kotlin
fun mapAndPrepareTablesWithSpecificFields(){

    val customTable = Table()
    customTable.name = "address_specific"
    customTable.registerEntity(Address::class.java)
    customTable.registerField(Fields.ResidentialArea)
    customTable.registerField(Fields.City)
    customTable.uniqueBy = FilterBy.ID

    //register table
    dbContext.mapTable(customTable)

    //after all tables have been mapped call this function
    dbContext.prepareTables()
}
```

Below is an example of a Table that will persist **all the fields** in the Address entity type

```kotlin
fun mapAndPrepareTablesWithAllFields(){
    val crtAddress = Table(Address::class.java, true)
    customTable.isStoringLiveRecordsOnly = true

    //register table
    dbContext.mapTable(customTable)

    //after all tables have been mapped call this function
    dbContext.prepareTables()
}
```

You can define your Tables in code or you may deserialize them in JSON format

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
