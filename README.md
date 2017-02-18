[![License](https://img.shields.io/badge/license-BSD-blue.svg?style=flat-square)](LICENSE.md) ![Size](https://reposs.herokuapp.com/?path=SubiyaCryolite/Jenesis-Data-Store)

# Jenesis Data Store
Jenesis Data Store (JDS) was created to help developers persist their classes to relational databases in a fast and reliable manner, without requiring them to design elaborate relational schemas. The aim of JDS is to allow for the rapid creation and modification of java classes in order to facilitate rapid prototyping prototyping and quick debelopment. The library eliminates the need to modify schemas once a class has been altered. It also eliminates all concerns regarding "breaking changes" in regards to fields and their values. Fields, Objects and ArrayTypes can be added, modified or removed at will. Beyond that the libraries data is structured in a way to promote fast and efficient Data Mining queries that can be used to support the application in question or to feed into specialised analytic software..

#Dependancies
The library depends on Java 8. Both 64 and 32 bit variants should suffice. Both the Development Kit and Runtime can be downloaded from [here](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html).

#Supported Databases
The API currently supports the following Relational Databases, each of which has their own dependancies, versions and licensing requirements. Please consult the official sites for specifics.

|Database|Version Tested Against|Official Site|JDBC Driver Tested Against
|-------|-----------|-----------|-----------|
| Postgres 				| 9.5    		| [Official Site](https://www.postgresql.org/) 		| [org.postgresql](https://mvnrepository.com/artifact/org.postgresql/postgresql)|
| Microsoft SQL Server	| 2008 R2		| [Official Site](https://www.microsoft.com/en-us/sql-server/sql-server-downloads) 		| [com.microsoft.sqlserver](https://mvnrepository.com/artifact/com.microsoft.sqlserver/sqljdbc4)|
| SQLite 				| Windows 10 	| [Official Site](https://www.sqlite.org/)		| [org.sqlite.JDBC](https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc)|

Note: I plan on adding MySQL support soon.

#1 How it works

##1.1 Creating Classes
Classes that use JDS need to extend JdsEntity.
```java
public class Customer extends JdsEntity
```
Following that the following steps need to be taken.

###1.1.1 Annotating Classes
Every class that extends JdsEntity must have its own unique Entity Code as well as Entity Name. This is done by annotating the class in the following manner
```java
@JdsEntityAnnotation(entityCode = 5, entityName = "Customer")
public class Customer extends JdsEntity
```
Entity codes MUST be unique in your application, any value of type long is valid. Entity Names do not enforce unique constraints but its best to use a unique name regardless. These values can be refeenced to mine data.

###1.1.2 Defining Fields
JdsFields are big part of the JDS framework. Each field MUST have a unique Field Id. Field Names do not enforce unique constraints but its best to use a unique name regardless. These values can be refeenced to mine data. Every field that you define can be one of the following types.

|Field Type|Java Type|Description|
|-----|-----|
|FLOAT|float|Numeric float values|
|INT|int|Numeric integer values|
|DOUBLE|double|Numeric double values|
|LONG|long|Numeric long values|
|TEXT|String|String values with no max limit|
|DATE_TIME|LocalDateTime|DateTime instances based on the host machines local timezone|
|ARRAY_FLOAT|List\<Float\>|Lists of type Float|
|ARRAY_INT|List\<Integer\>|Lists of type Integer|
|ARRAY_DOUBLE|List\<Double\>|Lists of type Double|
|ARRAY_LONG|List\<Long\>|Lists of type Long|
|ARRAY_TEXT|List\<String\>|Lists of type String|
|ARRAY_DATE_TIME|List\<LocalDateTime\>|Lists of type LocalDateTime|
|ENUM_TEXT|List\<String\>|Lists of type String|

I recommend defining your fields as static constants

```java
public class MyFields
{
    public static final JdsField FirstName = new JdsField(1, "first_name", JdsFieldType.TEXT);
    public static final JdsField MiddleName = new JdsField(2, "middle_name", JdsFieldType.TEXT);
    public static final JdsField SurName = new JdsField(3, "surname_name", JdsFieldType.TEXT);
    public static final JdsField MaidenName = new JdsField(4, "maiden_name", JdsFieldType.TEXT);
}
```

###1.1.3 Defining Enums
JdsEnums are an extension of fields. However they are designed for cases where one or more constant values are required. Usually these values would be represented by CheckBoxes or RadioButtons in a UI. In this example we will define Sex as an enummerated value with the following options (Male, Female, Other).
First of all we'd have to define a standard field of type ENUM_TEXT.
```java
public class MyFields
{
    ... 
    ...
    public static final JdsField SexEnum = new JdsField(5, "sex", JdsFieldType.ENUM_TEXT);
}
```
Then, we can define our actual enum in the following manner.
```java
public class MyEnumss
{
    public final static JdsFieldEnum SexEnums = new JdsFieldEnum(MyFields.SexEnum, "Male", "Female", "Other");
}
```
Behind the scenes these enums will be stored as an Integer Array. However you'd be presented with a List\<String\> in-memory containing one or more of the deined values.

###1.1.4 Binding Properties
Depending on the type of field JDS will require that you set you objects properties to one of the following container types.

|Field Type|Java Property Type|
|-----|-----|
|FLOAT|[SimpleFloatProperty](https://docs.oracle.com/javafx/2/api/javafx/beans/property/SimpleFloatProperty.html)|
|INT|[SimpleIntegerProperty](https://docs.oracle.com/javafx/2/api/javafx/beans/property/SimpleIntegerProperty.html)|
|DOUBLE|[SimpleDoubleProperty](https://docs.oracle.com/javafx/2/api/javafx/beans/property/SimpleDoubleProperty.html)|
|LONG|[SimpleLongProperty](https://docs.oracle.com/javafx/2/api/javafx/beans/property/SimpleLongProperty.html)|
|TEXT|[SimpleStringProperty](https://docs.oracle.com/javafx/2/api/javafx/beans/property/SimpleStringProperty.html)|
|DATE_TIME|[SimpleObjectProperty\<LocalDateTime\>](https://docs.oracle.com/javafx/2/api/javafx/beans/property/SimpleObjectProperty.html)|
|ARRAY_FLOAT|[SimpleListProperty\<Float\>](https://docs.oracle.com/javafx/2/api/javafx/beans/property/SimpleListProperty.html)|
|ARRAY_INT|[SimpleListProperty\<Integer\>](https://docs.oracle.com/javafx/2/api/javafx/beans/property/SimpleListProperty.html)|
|ARRAY_DOUBLE|[SimpleListProperty\<Double\>](https://docs.oracle.com/javafx/2/api/javafx/beans/property/SimpleListProperty.html)|
|ARRAY_LONG|[SimpleListProperty\<Long\>](https://docs.oracle.com/javafx/2/api/javafx/beans/property/SimpleListProperty.html)|
|ARRAY_TEXT|[SimpleListProperty\<String\>](https://docs.oracle.com/javafx/2/api/javafx/beans/property/SimpleListProperty.html)|
|ARRAY_DATE_TIME|[SimpleListProperty\<LocalDateTime\>](https://docs.oracle.com/javafx/2/api/javafx/beans/property/SimpleListProperty.html)|
|ENUM_TEXT|[SimpleListProperty\<String\>](https://docs.oracle.com/javafx/2/api/javafx/beans/property/SimpleListProperty.html)|

 After your class and its properties have been defined you must map the property to its corresponding field using the "map()" method. I recommend doing this in your constructor. 
 
 The example below shows a class definition with valid properties and bindings. With this your class can be persisted.

```java
@JdsEntityAnnotation(entityCode = 1, entityName = "Simple Address")
public class SimpleAddress extends JdsEntity
{
    private final SimpleStringProperty streetName;
    private final SimpleIntegerProperty plotNumber;
    private final SimpleStringProperty area;
    private final SimpleStringProperty city;
    private final SimpleStringProperty provinceOrState;
    private final SimpleStringProperty country;
    
    public SimpleAddress()
    {
        this.streetName = new SimpleStringProperty("");
        this.plotNumber = new SimpleIntegerProperty(0);
        this.area = new SimpleStringProperty("");
        this.city = new SimpleStringProperty("");
        this.provinceOrState = new SimpleStringProperty("");
        this.country = new SimpleStringProperty("");
        //map your properties
        map(MyFields.StreetName, streetName);
        map(MyFields.PlotNameOrNumber, plotNumber);
        map(MyFields.AreaName, area);
        map(MyFields.CityName, city);
        map(MyFields.ProvinceName, provinceOrState);
    }
    
    public String getStreetName()
    {
    	return this.streetName.get(); 
    }
    
    public int getPlotNumber()
    {
    	return this.plotNumber.get();
    }
    
    public String getArea()
    {
    	return this.area().get();
    }
    
    public String getCity()
    {
    	return this.city.get();
    }
    
    public String getProvinceOrState()
    {
    	return this.provinceOrState.get();
    }
    
    public String getCountry()
    {
    	return this.country.get();
    }
	
	public void setStreetName(String value)
	{
		this.streetName.get(value);
	}

	public void setPlotNumber(int value)
	{
		this.plotNumber.get(value);
	}

	public void setArea(String value)
	{
		this.area().get(value);
	}

	public void setCity(String value)
	{
		this.city.get(value);
	}

	public void setProvinceOrState(String value)
	{
		this.provinceOrState.get(value);
	}

	public void setCountry(String value)
	{
		this.country.get(value);
	}
}
```
###1.1.5 Binding Objects and Object Arrays
Beyond saving numeric, string and date values JDS can also persist embedded objects and object arrays. All that's required is a valid JdsEntity subclass to be mapped based on the embedded objects annotations.

The class below shows how you can achieve this.

```java
@JdsEntityAnnotation(entityCode = 2, entityName = "Simple Address Book")
public class SimpleAddressBook extends JdsEntity
{
    private final SimpleObjectProperty<SimpleAddress> primaryAddress;
    private final SimpleListProperty<SimpleAddress> alternativeAddresses;
    
    public SimpleAddressBook()
    {
        this.primaryAddress = new SimpleObjectProperty(new SimpleAddress());//avoid nulls
        this.alternativeAddresses = new SimpleListProperty<>(FXCollections.observableArrayList());
        //map your objects
        map(SimpleAddress.class, primaryAddress);
        map(SimpleAddress.class, alternativeAddresses);
    }
    
    public SimpleAddressBook getPrimaryAddress()
    {
        return this.primaryAddress.get();
    }
    
    public void setPrimaryAddress(SimpleAddressBook value)
    {
        this.primaryAddress.set(value);  
    }
    
    public List<SimpleAddress> getAlternativeAddresses()
    {
        return this.primaryAddress.get();
    }
        
    public void SetAlternativeAddresses(List<SimpleAddress> value)
    {
        this.primaryAddress.set(value);  
    }
}
```


##1.2 CRUD Operations
###1.2.1 Initialising the database
In order to use JDS you will need an instance of JdsDatabase. The instance you create will depend on your underlying backend. Beyond that your project must have the correct JDBC driver in its classpath. The drivers that were used during development are listed under "Supported Databases" above.
####Postgres example
```java
    JdsDatabase jdsDatabase = JdsDatabase.getImplementation(JdsImplementation.POSTGRES);
    jdsDatabase.setConnectionProperties("org.postgresql.Driver", "jdbc:postgresql://127.0.0.1:5432/PROJECT_DATABASE", "DATABASE_USER", "DATABASE_PASSWORD");
    jdsDatabase.init();
```
####Microsoft SQL Server Example
```java
    JdsDatabase jdsDatabase = JdsDatabase.getImplementation(JdsImplementation.TSQL);
    jdsDatabase.setConnectionProperties("com.microsoft.sqlserver.jdbc.SQLServerDriver", "jdbc:sqlserver://127.0.0.1\\DATABASE_INSTANCE;databaseName=PROJECT_DATABASE", "DATABASE_USER", "DATABASE_PASSWORD");
    jdsDatabase.init();
```
####Sqlite Example
```java
    String databaseLocation = "jdbc:sqlite:" + getDatabaseFileLocation();
    JdsDatabase jdsDatabase = JdsDatabase.getImplementation(JdsImplementation.SQLITE);
    jdsDatabase.setConnectionProperties(databaseLocation); //Note the difference for SQLite. Its a file based database with different connection parameters
    jdsDatabase.init();
```

With this you should have a valid connection to your database and JDS will setup its tables and procedures automatically. Furthermore, you can use the "getConnection()" method from your JdsDatabase instance in order to return a standard "java.sql.Connection" in your application. 

###1.2.2 Initialising JDS

Once you have initialised your database you can go ahead and init all your JDS classes. You can achieve this by mapping ALL your JDS classes in the following manner.

```java
    public void initialiseJdsClasses()
    {
    JdsEntityClasses.map(SimpleAddress.class);
    JdsEntityClasses.map(SimpleAddressBook.class);
    }
```

You only have to do this once at startup but it is vital that you do so. Without this you will face problems when loading or saving records

###1.2.3 Load
###1.2.4 Load with Args
###1.2.5 Save
###1.2.6 Delete [W.I.P]

#License

#Development
I highly recommend the use of the [IntelliJ IDE](https://www.jetbrains.com/idea/download/) 

#Special Thanks
To all the users and contributors!