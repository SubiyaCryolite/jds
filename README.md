[![Build Status](https://travis-ci.org/SubiyaCryolite/jds.svg?branch=master)](https://travis-ci.org/SubiyaCryolite/jds)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.subiyacryolite/jds/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.subiyacryolite/jds)
[![Javadocs](https://javadoc.io/badge/io.github.subiyacryolite/jds.svg)](https://javadoc.io/doc/io.github.subiyacryolite/jds)
[![License](https://img.shields.io/badge/License-BSD--3--Clause-blue.svg)](LICENSE.md)
![Size](https://github-size-badge.herokuapp.com/subiyacryolite/jds.svg)
<!--- [![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/oss.sonatype.org/io.github.subiyacryolite/jds.svg)](https://oss.sonatype.org/content/repositories/snapshots/io/github/subiyacryolite/jds/))--->

# Jenesis Data Store

Jenesis Data Store (JDS) was created to help developers persist data to a strongly-typed portable JSON format.

JDS has four goals:

 - To allow for the rapid development of complex Java/Kotlin systems with stringent data definition / quality requirements
 - To provide a flexible and reliable framework to persist and retrieve data against.
 - To provide a robust, strongly-typed Field Dictionary.
 - To leverage JSON as a datastore over EAV based paradigms.

The library eliminates the need to modify schemas once a class has been altered.

It also eliminates all concerns regarding "breaking changes" in regards to fields and their addition and/or removal.

Put simply, JDS is useful for any developer that requires a flexible data store running on top of a Relational databases.

JDS is licensed under the [3-Clause BSD License](https://opensource.org/licenses/BSD-3-Clause)

# Design

The concept behind JDS is quite simple. Extend a base **Entity** class, define strongly-typed **Fields** and then **map** them against implementations of the **Property** interface.

JDS was designed to avoid reflection, and its potential performance implications, as such mapping is used as oppossed to using Annotations .

## Features

* Transparent persistence
* Supports the persistence of NULL values for boxed types (e.g Integer, Double)
* Full support for generics and inheritance
* Easily integrates with new or existing databases
* Portable format which can be serialised to JSON allows for the flexibility of EAV without its drawbacks
* Supports MySQL, T-SQL, PostgreSQL, Oracle 11G, MariaDB and SQLite
* Supports a robust Field Dictonary allowing for metadata such as Tags and Alternate Coding to be applied to Fields and Entities.

# Maven Central

You can search on The Central Repository with GroupId and ArtifactId Maven Search for [![Maven Search](https://img.shields.io/badge/io.github.subiyacryolite-jds-blue.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.subiyacryolite/jds)

Maven

```xml
<dependency>
    <groupId>io.github.subiyacryolite</groupId>
    <artifactId>jds</artifactId>
    <version>20.4-SNAPSHOT</version>
</dependency>
```

Gradle

```groovy
compile 'io.github.subiyacryolite:jds:20.4-SNAPSHOT'
```

# Dependencies

The library depends on Java 1.8. Both 64 and 32 bit variants should suffice. Both the Development Kit and Runtime can be downloaded from [here](https://adoptopenjdk.net/).

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

public class Address : Entity
```

However, if you plan on using interfaces they must extend IEntity. Concrete classes can then extend Entity

```kotlin
import io.github.subiyacryolite.jds.Entity;
import io.github.subiyacryolite.jds.IEntity;

public interface IAddress : IEntity

public class Address : IAddress
```

Following that the following steps need to be taken.

### 1.1.1 Annotating Classes

Every class that extends Entity must have its own unique Entity Id as well as an Entity Name. This is done by annotating the class, or its parent interface, in the following manner

```kotlin
@EntityAnnotation(id = 1, name = "address", description = "An entity representing address information")
class Address : Entity()
```

Entity IDs MUST be unique in your application, any value of type long is valid. Entity Names do not enforce unique constraints but its best to use a unique name regardless. These values can be referenced to mine data.

### 1.1.2 Defining Fields

Fields are big part of the JDS framework. Each Field MUST have a unique Field Id. Field Names do not enforce unique constraints but its best to use a unique name regardless. These values can be referenced to mine data. Every Field that you define can be one of the following types.

| JDS Field Type       | Java Type                                  | Description                                                   |
| -------------------- | ------------------------------------------ | --------------------------------------------------------------|
| DateTimeCollection   | Collection\<LocalDateTime\>                | Collection of type LocalDateTime                              |
| DoubleCollection     | Collection\<Double\>                       | Collection of type Double                                     |
| EntityCollection     | Collection\<Class\<? extends Entity\>\>    | Collection of type Entity                                     |
| FloatCollection      | Collection\<Float\>                        | Collection of type Float                                      |
| IntCollection        | Collection\<Integer\>                      | Collection of type Integer                                    |
| ShortCollection      | Collection\<Short\>                        | Collection of type Short                                      |
| LongCollection       | Collection\<Long\>                         | Collection of type Long                                       |
| StringCollection     | Collection\<String\>                       | Collection of type String                                     |
| UuidCollection       | Collection\<UUID\>                         | Collection of type UUID                                       |
| Blob                 | byte[] or InputStream                      | Blob values                                                   |
| Boolean              | boolean / Boolean                          | Boolean values                                                |
| Entity               | Class\<? extends Entity\>                  | Object of type Entity                                         |
| DateTime             | LocalDateTime                              | DateTime instances based on the host machines local timezone  |
| Date                 | LocalDate                                  | Local date instances                                          |
| Double               | double / Double                            | Numeric double values                                         |
| Duration             | Duration                                   | Object of type Duration                                       |
| EnumCollection       | Collection\<Enum\>                         | Collection of type Enum                                       |
| Enum                 | Enum                                       | Object of type Enum                                           |
| Float                | float / Float                              | Numeric float values                                          |
| Int                  | int / Integer                              | Numeric integer values                                        |
| Short                | short / Short                              | Numeric SHORT values                                          |
| Long                 | long / Long                                | Numeric long values                                           |
| MonthDay             | MonthDay                                   | Object of type MonthDay                                       |
| Period               | Period                                     | Object of type Period                                         |
| String               | String                                     | String values with no max limit                               |
| Time                 | LocalTime                                  | Local time instances                                          |
| YearMonth            | YearMonth                                  | Object of type YearMonth                                      |
| ZonedDateTime        | ZonedDateTime                              | Zoned DateTime instances                                      |
| Uuid                 | UUID                                       | UUID instances                                                |

We recommend defining your Fields as **static constants**

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

Furthermore you can add descriptions, of up to **256 characters**, to each field

```kotlin
import io.github.subiyacryolite.jds.Field;
import io.github.subiyacryolite.jds.enumProperties.FieldType;

object Fields {
    val StreetName = Field(1, "street_name", FieldType.String, "The street name of the address")
    val PlotNumber = Field(2, "plot_number", FieldType.Int, "The street name of the address")
    val Area = Field(3, "area_name", FieldType.String, "The name of the area / neighbourhood")
    //...
}
```

JDS also supports **Tags** which can be applied to each **Field** and **Entity** definitions. Tags are implemented as a set of strings, there is no limit on how many tags a field can have. This can be useful for categorising certain kinds of information

```kotlin
import io.github.subiyacryolite.jds.Field;
import io.github.subiyacryolite.jds.enumProperties.FieldType;

object Fields {
    val StreetName = Field(1, "street_name", FieldType.String, description = "The street name of the address", tags = setOf("AddressInfo", "ClientInfo", "IdentifiableInfo"))
    //...
}
```

### 1.1.3 Defining Enums

Enums are an extension of **Fields**. However, they are designed for cases where one or more constant values are required. Usually these values would be represented by Check Boxes, Radio Buttons or Combo Boxes in a UI. In this example we will define the type of an address as an enumerated value with the following options (YES, NO).

First of all we'd have to define a standard Field of type **Enum**.

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

Depending on the type of Field, JDS will require that you set you objects properties to one of the following IValue container types.

| JDS Field Type       | Container                                                  | Java Mapping Call     |Kotlin Mapping Call |
| -------------------- | -----------------------------------------------------------| --------------------- | ------------ |
| DateTimeCollection   | MutableCollection\<LocalDateTime\>                         | mapDateTimes          | map          |
| DoubleCollection     | MutableCollection\<Double\>                                | mapDoubles            | map          |
| EntityCollection     | MutableCollection\<Class\<? extends Entity\>\>             | map                   | map          |
| FloatCollection      | MutableCollection\<Float\>                                 | mapFloats             | map          |
| IntCollection        | MutableCollection\<Integer\>                               | mapInts               | map          |
| LongCollection       | MutableCollection\<Long\>                                  | mapLongs              | map          |
| StringCollection     | MutableCollection\<String\>                                | mapStrings            | map          |
| Boolean              | IValue\<Boolean\>                                          | mapBoolean            | map          |
| Blob                 | IValue\<ByteArray\>                                        | map                   | map          |
| Entity               | Class\<? extends Entity\>                                  | map                   | map          |
| Date                 | IValue\<LocalDate\>                                        | mapDate               | map          |
| DateTime             | IValue\<LocalDateTime\>                                    | mapDateTime           | map          |
| Double               | IValue\<Double\>                                           | mapNumeric            | map          |
| Duration             | IValue\<Duration\>                                         | mapDuration           | map          |
| Enum                 | IValue\<Enum\>                                             | mapEnum               | map          |
| EnumCollection       | Collection\<Enum\>                                         | mapEnums              | map          |
| Float                | IValue\<Float\>                                            | mapNumeric            | map          |
| Int                  | IValue\<Integer\>                                          | mapNumeric            | map          |
| Long                 | IValue\<Long\>                                             | mapNumeric            | map          |
| MonthDay             | IValue\<MonthDay\>                                         | mapMonthDay           | map          |
| Period               | IValue\<Period\>                                           | mapPeriod             | map          |
| String               | IValue\<String\>                                           | mapString             | map          |
| Time                 | IValue\<LocalTime\>                                        | mapTime               | map          |
| YearMonth            | IValue\<YearMonth\>                                        | mapYearMonth          | map          |
| ZonedDateTime        | IValue\<ZonedDateTime\>                                    | mapZonedDateTime      | map          |
| Uuid                 | IValue\<ZonedDateTime\>                                    | mapZonedDateTime      | map          |

To simplify the mapping Process Jds has the following helper classes defined:

 - Generic containers (Entities and Enums)
     - ObjectValue<T>
 - Non null containers
     - BlobValue
     - BooleanValue
     - DoubleValue
     - DurationValue
     - EnumValue
     - FloatValue
     - IntegerValue
     - LocalDateValue
     - LocalDateTimeValue
     - LocalTimeValue
     - LongValue
     - MonthDayValue
     - PeriodValue
     - ShortValue
     - StringValue
     - UuidValue
     - YearMonthValue
     - ZonedDateTimeValue
 - Nullable containers
     - NullableBlobValue
     - NullableBooleanValue
     - NullableDoubleValue
     - NullableDurationValue
     - NullableEnumValue
     - NullableFloatValue
     - NullableIntegerValue
     - NullableLocalDateValue
     - NullableLocalDateTimeValue
     - NullableLocalTimeValue
     - NullableLongValue
     - NullableMonthDayValue
     - NullablePeriodValue
     - NullableShortValue
     - NullableStringValue
     - NullableUuidValue
     - NullableYearMonthValue
     - NullableZonedDateTimeValue

**Note:** JDS assumes that all collection types will **not** contain null entries.

**Note:** Collection types can be of any valid type e.g. ArrayList, LinkedList, HashSet etc

After your class and its properties have been defined you must map the property to its corresponding Field using the **map()** method. I recommend doing this in your primary constructor.

The example below shows a class definition with valid properties and bindings. With this your class can be persisted.

Note that the example below has a 3rd parameter to the map method, this is the **Property Name** 
 
The **Property Name** is used by the JDS **Field Dictionary** to know which **property** a particular **Field** is mapped to within an **Entity**.

This is necessary as one **Field** definition may be mapped to a different property amongst different **Entities**.

For example a **Field** called "FirstName" could be mapped to a property called "firstName" in one **Entity** and a property called "givenName" in another.

```kotlin
package io.github.subiyacryolite.jds.tests.entities

import io.github.subiyacryolite.jds.Entity
import io.github.subiyacryolite.jds.annotations.EntityAnnotation
import io.github.subiyacryolite.jds.beans.property.NullableBooleanValue
import io.github.subiyacryolite.jds.beans.property.NullableShortValue
import io.github.subiyacryolite.jds.tests.constants.Fields
import java.time.LocalDateTime

data class Address(
        private val _streetName: IValue<String> = StringValue(),
        private val _plotNumber: IValue<Short?> = NullableShortValue(),
        private val _area: IValue<String> = StringValue(),
        private val _city: IValue<String> = StringValue(),
        private val _provinceOrState: IValue<String> = StringValue(),
        private val _country: IValue<String> = StringValue(),
        private val _primaryAddress: IValue<Boolean?> = NullableBooleanValue(),
        private val _timestamp: IValue<LocalDateTime> = LocalDateTimeValue()
) : Entity(), IAddress {

    override fun bind() {
        super.bind()
        map(Fields.StreetName, _streetName, "streetName")
        map(Fields.PlotNumber, _plotNumber, "plotNumber")
        map(Fields.ResidentialArea, _area, "area")
        map(Fields.City, _city, "city")
        map(Fields.ProvinceOrState, _provinceOrState, "provinceOrState")
        map(Fields.Country, _country, "country")
        map(Fields.PrimaryAddress, _primaryAddress, "primaryAddress")
        map(Fields.TimeStamp, _timestamp, "timestamp")
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

    var timeOfEntry: LocalDateTime
        get() = _timestamp.get()
        set(timeOfEntry) = _timestamp.set(timeOfEntry)
}
```

### 1.1.5 Binding Objects and Object Arrays

JDS can also persist embedded objects and object arrays.

All that's required is a valid **Entity** or **IEntity** subclass to be mapped to a **Field** of type **Entity** or **EntityCollection** .

```kotlin
import io.github.subiyacryolite.jds.Field
import io.github.subiyacryolite.jds.enums.FieldType

object Fields
{
    val Addresses = Field(23, "addresses", FieldType.EntityCollection, "A collection of addresses")
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
        val addresses: MutableCollection<IAddress> = ArrayList()
) : Entity() {

    override fun bind() {
        super.bind()
        map(Entities.Addresses, addresses, "addresses")
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

### 1.2.4 Saving objects (Portable Format)
...

### 1.2.5 Loading objects (Portable Format)
...

# Development

I highly recommend the use of the [IntelliJ IDE](https://www.jetbrains.com/idea/download/) for development.

# Contributing to Jenesis Data Store

If you would like to contribute code you can do so through [Github](https://github.com/SubiyaCryolite/Jenesis-Data-Store/) by forking the repository and sending a pull request targeting the current development branch.

When submitting code, please make every effort to follow existing conventions and style in order to keep the code as readable as possible.

# Bugs and Feedback

For bugs, questions and discussions please use the [Github Issues](https://github.com/SubiyaCryolite/Jenesis-Data-Store/issues).

# Special Thanks

To all our users and contributors!
