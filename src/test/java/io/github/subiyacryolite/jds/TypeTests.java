package io.github.subiyacryolite.jds;

import io.github.subiyacryolite.jds.classes.TypeClass;
import org.junit.Assert;
import org.junit.Test;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.FutureTask;

/**
 * Created by ifung on 12/04/2017.
 */
public class TypeTests extends BaseTest {


    @Test
    @Override
    public void saveAndLoad() throws Exception {
        List<TypeClass> memObjects = getCollection();
        JdsSave.save(jdsDataBase, 1, memObjects);
        System.out.printf("Saved %s\n", memObjects);

        List<TypeClass> savObjects = new FutureTask<List<TypeClass>>(new JdsLoad(jdsDataBase, TypeClass.class)).get(); //load all entities of type SimpleAddressBook
        List<TypeClass> specificObject = new FutureTask<List<TypeClass>>(new JdsLoad(jdsDataBase, TypeClass.class, "instance4")).get(); //load all entities of type SimpleAddressBook with Entity Guids in range
        System.out.printf("All entities [%s]\n", savObjects);
        System.out.printf("Specific entities [%s]\n", specificObject);

        Optional<TypeClass> srcA = savObjects.parallelStream().filter(z -> z.getEntityGuid().equals("instance1")).findAny();
        Optional<TypeClass> srcB = memObjects.parallelStream().filter(z -> z.getEntityGuid().equals("instance1")).findAny();
        Optional<TypeClass> srcC = savObjects.parallelStream().filter(z -> z.getEntityGuid().equals("instance2")).findAny();
        Optional<TypeClass> srcD = memObjects.parallelStream().filter(z -> z.getEntityGuid().equals("instance2")).findAny();
        Optional<TypeClass> srcE = savObjects.parallelStream().filter(z -> z.getEntityGuid().equals("instance3")).findAny();
        Optional<TypeClass> srcF = memObjects.parallelStream().filter(z -> z.getEntityGuid().equals("instance3")).findAny();
        Optional<TypeClass> srcG = savObjects.parallelStream().filter(z -> z.getEntityGuid().equals("instance4")).findAny();
        Optional<TypeClass> srcH = memObjects.parallelStream().filter(z -> z.getEntityGuid().equals("instance4")).findAny();
        match(srcA, srcB);
        match(srcC, srcD);
        match(srcE, srcF);
        match(srcG, srcH);
    }

    private void match(Optional<TypeClass> srcA, Optional<TypeClass> srcB) {
        Assert.assertTrue(srcA.isPresent());
        Assert.assertTrue(srcB.isPresent());
        //date, localTime, zonedDateTime and localDateTime is accurate up to seconds [not nanoseconds]
        Assert.assertEquals("DateField getYear " + srcA.get().getEntityGuid(), srcA.get().getDateField().getYear(), srcB.get().getDateField().getYear());
        Assert.assertEquals("DateField getMonth " + srcA.get().getEntityGuid(), srcA.get().getDateField().getMonth(), srcB.get().getDateField().getMonth());
        Assert.assertEquals("DateField getDayOfMonth " + srcA.get().getEntityGuid(), srcA.get().getDateField().getDayOfMonth(), srcB.get().getDateField().getDayOfMonth());

        Assert.assertEquals("TimeField getHour " + srcA.get().getEntityGuid(), srcA.get().getTimeField().getHour(), srcB.get().getTimeField().getHour());
        Assert.assertEquals("TimeField getMinute " + srcA.get().getEntityGuid(), srcA.get().getTimeField().getMinute(), srcB.get().getTimeField().getMinute());
        Assert.assertEquals("TimeField getSecond " + srcA.get().getEntityGuid(), srcA.get().getTimeField().getSecond(), srcB.get().getTimeField().getSecond());

        Assert.assertEquals("DateTimeField getYear " + srcA.get().getEntityGuid(), srcA.get().getDateTimeField().getYear(), srcB.get().getDateTimeField().getYear());
        Assert.assertEquals("DateTimeField getMonth " + srcA.get().getEntityGuid(), srcA.get().getDateTimeField().getMonth(), srcB.get().getDateTimeField().getMonth());
        Assert.assertEquals("DateTimeField getDayOfMonth " + srcA.get().getEntityGuid(), srcA.get().getDateTimeField().getDayOfMonth(), srcB.get().getDateTimeField().getDayOfMonth());
        Assert.assertEquals("DateTimeField getHour " + srcA.get().getEntityGuid(), srcA.get().getDateTimeField().getHour(), srcB.get().getDateTimeField().getHour());
        Assert.assertEquals("DateTimeField getMinute " + srcA.get().getEntityGuid(), srcA.get().getDateTimeField().getMinute(), srcB.get().getDateTimeField().getMinute());
        Assert.assertEquals("DateTimeField getSecond " + srcA.get().getEntityGuid(), srcA.get().getDateTimeField().getSecond(), srcB.get().getDateTimeField().getSecond());

        Assert.assertEquals("ZonedDateTimeField getYear " + srcA.get().getEntityGuid(), srcA.get().getZonedDateTimeField().getYear(), srcB.get().getZonedDateTimeField().getYear());
        Assert.assertEquals("ZonedDateTimeField getMonth " + srcA.get().getEntityGuid(), srcA.get().getZonedDateTimeField().getMonth(), srcB.get().getZonedDateTimeField().getMonth());
        Assert.assertEquals("ZonedDateTimeField getDayOfMonth " + srcA.get().getEntityGuid(), srcA.get().getZonedDateTimeField().getDayOfMonth(), srcB.get().getZonedDateTimeField().getDayOfMonth());
        Assert.assertEquals("ZonedDateTimeField getHour " + srcA.get().getEntityGuid(), srcA.get().getZonedDateTimeField().getHour(), srcB.get().getZonedDateTimeField().getHour());
        Assert.assertEquals("ZonedDateTimeField getMinute " + srcA.get().getEntityGuid(), srcA.get().getZonedDateTimeField().getMinute(), srcB.get().getZonedDateTimeField().getMinute());
        Assert.assertEquals("ZonedDateTimeField getSecond " + srcA.get().getEntityGuid(), srcA.get().getZonedDateTimeField().getSecond(), srcB.get().getZonedDateTimeField().getSecond());
        Assert.assertEquals("ZonedDateTimeField getZone " + srcA.get().getEntityGuid(), srcA.get().getZonedDateTimeField().getZone(), srcB.get().getZonedDateTimeField().getZone());
        Assert.assertEquals("ZonedDateTimeField getOffset " + srcA.get().getEntityGuid(), srcA.get().getZonedDateTimeField().getOffset(), srcB.get().getZonedDateTimeField().getOffset());

        Assert.assertEquals("StringField " + srcA.get().getEntityGuid(), srcA.get().getStringField(), srcB.get().getStringField());
        Assert.assertEquals("IntField " + srcA.get().getEntityGuid(), srcA.get().getIntField(), srcB.get().getIntField());
        Assert.assertEquals("FloatField " + srcA.get().getEntityGuid(), srcA.get().getFloatField(), srcB.get().getFloatField(), DELTA);
        Assert.assertEquals("DoubleField " + srcA.get().getEntityGuid(), srcA.get().getDoubleField(), srcB.get().getDoubleField(), DELTA);
        Assert.assertEquals("LongField " + srcA.get().getEntityGuid(), srcA.get().getLongField(), srcB.get().getLongField());
        Assert.assertEquals("BooleanField " + srcA.get().getEntityGuid(), srcA.get().getBooleanField(), srcB.get().getBooleanField());
    }

    private List<TypeClass> getCollection() {
        List<TypeClass> collection = new ArrayList<>();

        TypeClass instance1 = new TypeClass();
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
        instance1.setEntityGuid("instance1");

        TypeClass instance2 = new TypeClass();
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
        instance2.setEntityGuid("instance2");

        TypeClass instance3 = new TypeClass();
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
        instance3.setEntityGuid("instance3");

        TypeClass instance4 = new TypeClass();
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

    @Test
    public void saveAndLoadPostreSqlImplementation() throws Exception {
        initialisePostgeSqlBackend();
        saveAndLoad();
    }

    @Test
    public void saveAndLoadTsqlImplementation() throws Exception {
        initialiseTSqlBackend();
        saveAndLoad();
    }

    @Test
    public void saveAndLoadSqliteImplementation() throws Exception {
        initialiseSqlLiteBackend();
        saveAndLoad();
    }

    @Test
    public void saveAndLoadMySqlImplementation() throws Exception {
        initialiseMysqlBackend();
        saveAndLoad();
    }

    @Test
    public void saveAndLoadAllImplementations() throws Exception {
        saveAndLoadSqliteImplementation();
        saveAndLoadTsqlImplementation();
        saveAndLoadPostreSqlImplementation();
        saveAndLoadMySqlImplementation();
    }
}
