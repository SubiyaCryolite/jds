package common;

import connectivity.*;
import entities.*;
import io.github.subiyacryolite.jds.JdsDb;
import enums.PrimaryAddress;

import java.io.*;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ifunga on 08/04/2017.
 */
public abstract class BaseTestConfig {

    protected final double DELTA = 1e-15;
    protected JdsDb jdsDb;

    public void saveAndLoad() throws Exception {
        save();
        load();
    }

    public void save() throws Exception {
    }

    public void load() throws Exception {
    }

    public void initialiseJdsClasses() {
        jdsDb.map(EntityA.class);
        jdsDb.map(EntityB.class);
        jdsDb.map(EntityC.class);
        jdsDb.map(JdsExample.class);
        jdsDb.map(Address.class);
        jdsDb.map(AddressBook.class);
    }

    private void initJds() {
        jdsDb.init();
        jdsDb.isLoggingEdits(true);
        initialiseJdsClasses();
    }

    public void initialiseSqlLiteBackend() {
        jdsDb = new JdsDbSqliteImplementation();
        initJds();
    }

    public void initialisePostgeSqlBackend() {
        jdsDb = new JdsDbPostgreSqlmplementation();
        initJds();
    }

     public void initialiseTSqlBackend() {
        jdsDb = new JdsDbTransactionalSqllmplementation();
        initJds();
    }

    public void initialiseMysqlBackend() {
        jdsDb = new JdsDbMySqlImplementation();
        initJds();
    }

    public void initialiseOracleBackend() {
        jdsDb = new JdsDbOracleImplementation();
        initJds();
    }

    protected <T> void serialize(final T objectToSerialize, final String fileName) {
        if (fileName == null) {
            throw new IllegalArgumentException(
                    "Name of file to which to serialize object to cannot be null.");
        }
        if (objectToSerialize == null) {
            throw new IllegalArgumentException("Object to be serialized cannot be null.");
        }
        try (FileOutputStream fos = new FileOutputStream(fileName); ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(objectToSerialize);
            System.out.println("Serialization of completed: " + objectToSerialize);
        } catch (IOException ioException) {
            ioException.printStackTrace(System.err);
        }
    }

    protected <T> T deserialize(final String fileToDeserialize, final Class<T> classBeingDeserialized) {
        if (fileToDeserialize == null) {
            throw new IllegalArgumentException("Cannot deserialize from a null filename.");
        }
        if (classBeingDeserialized == null) {
            throw new IllegalArgumentException("Type of class to be deserialized cannot be null.");
        }
        T objectOut = null;
        try (FileInputStream fis = new FileInputStream(fileToDeserialize); ObjectInputStream ois = new ObjectInputStream(fis)) {
            objectOut = (T) ois.readObject();
            System.out.println("Deserialization of completed: " + objectOut);
        } catch (IOException | ClassNotFoundException exception) {
            exception.printStackTrace(System.err);
        }
        return objectOut;
    }

    protected AddressBook getSimpleAddressBook() {
        Address primaryAddress = new Address();
        primaryAddress.getOverview().setEntityGuid("primaryAddress"); //setting a custom Entity Guid
        primaryAddress.getOverview().setDateModified(LocalDateTime.of(2012, Month.APRIL, 12, 13, 49));
        primaryAddress.setArea("Norte Broad");
        primaryAddress.setCity("Livingstone");
        primaryAddress.setCountry("Zambia");
        primaryAddress.setPlotNumber(23);
        primaryAddress.setProvinceOrState("Southern");
        primaryAddress.setStreetName("East Street");
        primaryAddress.setPrimaryAddress(PrimaryAddress.YES);

        Address secondAddress = new Address();
        secondAddress.getOverview().setEntityGuid("secondAddress"); //setting a custom Entity Guid
        secondAddress.getOverview().setDateModified(LocalDateTime.of(2009, Month.OCTOBER, 16, 03, 34));
        secondAddress.setArea("Roma");
        secondAddress.setCity("Lusaka");
        secondAddress.setCountry("Zambia");
        secondAddress.setPlotNumber(2);
        secondAddress.setProvinceOrState("Lusaka");
        secondAddress.setStreetName("West Street");
        secondAddress.setPrimaryAddress(PrimaryAddress.NO);

        Address thirdAddress = new Address();
        thirdAddress.getOverview().setEntityGuid("thirdAddress"); //setting a custom Entity Guid
        thirdAddress.getOverview().setDateModified(LocalDateTime.of(2007, Month.JULY, 04, 05, 10));
        thirdAddress.setArea("Riverdale");
        thirdAddress.setCity("Ndola");
        thirdAddress.setCountry("Zambia");
        thirdAddress.setPlotNumber(9);
        thirdAddress.setProvinceOrState("Copperbelt");
        thirdAddress.setStreetName("West Street");
        thirdAddress.setPrimaryAddress(PrimaryAddress.NO);

        AddressBook addressBook = new AddressBook();
        addressBook.getOverview().setEntityGuid("testGuid0001"); //setting a custom Entity Guid
        addressBook.getAddresses().add(primaryAddress);
        addressBook.getAddresses().add(secondAddress);
        addressBook.getAddresses().add(thirdAddress);
        return addressBook;
    }

    protected List<JdsExample> getCollection() {
        List<JdsExample> collection = new ArrayList<>();

        JdsExample instance1 = new JdsExample();
        instance1.setStringField("One");
        instance1.setTimeField(LocalTime.of(15, 24));
        instance1.setDateField(LocalDate.of(2012, 8, 26));
        instance1.setDateTimeField(LocalDateTime.of(1991, 07, 01, 8, 33, 12));
        instance1.setZonedDateTimeField(ZonedDateTime.of(LocalDateTime.now().minusMonths(3), ZoneId.systemDefault()));
        instance1.setIntField(99);
        instance1.setLongField(888);
        instance1.setDoubleField(777.666);
        instance1.setFloatField(5555.4444f);
        instance1.setBooleanField(true);
        instance1.getOverview().setEntityGuid("instance1");

        JdsExample instance2 = new JdsExample();
        instance2.setStringField("tWO");
        instance2.setTimeField(LocalTime.of(19, 24));
        instance2.setDateField(LocalDate.of(2011, 4, 2));
        instance2.setDateTimeField(LocalDateTime.of(1999, 02, 21, 11, 13, 43));
        instance2.setZonedDateTimeField(ZonedDateTime.of(LocalDateTime.now().minusMonths(7), ZoneId.systemDefault()));
        instance2.setIntField(66);
        instance2.setLongField(555);
        instance2.setDoubleField(444.333);
        instance2.setFloatField(2222.1111f);
        instance2.setBooleanField(false);
        instance2.getOverview().setEntityGuid("instance2");

        JdsExample instance3 = new JdsExample();
        instance3.setStringField("Three");
        instance3.setTimeField(LocalTime.of(03, 14));
        instance3.setDateField(LocalDate.of(2034, 6, 14));
        instance3.setDateTimeField(LocalDateTime.of(1987, 07, 24, 13, 22, 45));
        instance3.setZonedDateTimeField(ZonedDateTime.of(LocalDateTime.now().plusDays(3), ZoneId.systemDefault()));
        instance3.setIntField(22);
        instance3.setLongField(333);
        instance3.setDoubleField(444.555);
        instance3.setFloatField(5555.6666f);
        instance3.setBooleanField(true);
        instance3.getOverview().setEntityGuid("instance3");

        JdsExample instance4 = new JdsExample();
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
        instance4.getOverview().setEntityGuid("instance4");

        collection.add(instance1);
        collection.add(instance2);
        collection.add(instance3);
        collection.add(instance4);
        return collection;
    }
}
