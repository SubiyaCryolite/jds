import io.github.subiyacryolite.jds.JdsLoad;
import io.github.subiyacryolite.jds.JdsSave;
import common.BaseTestConfig;
import entities.JdsExample;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

/**
 * Created by indana on 5/17/2017.
 */
public class LegacyValidationTests extends BaseTestConfig {
    @Test
    @Override
    public void saveAndLoad() throws Exception {
        System.out.printf("=========== %s ===========\n", jdsDb.getImplementation());
        List<JdsExample> memObjects = getCollection();
        JdsSave.save(jdsDb, 1, memObjects);
        System.out.printf("Saved %s\n", memObjects);

        List<JdsExample> savObjects = JdsLoad.load(jdsDb, JdsExample.class); //load all entities of type AddressBook
        List<JdsExample> specificObject = JdsLoad.load(jdsDb, JdsExample.class, "instance4"); //load all entities of type AddressBook with Entity Guids in range
        System.out.printf("All entities [%s]\n", savObjects);
        System.out.printf("Specific entities [%s]\n", specificObject);

        Optional<JdsExample> srcA = savObjects.parallelStream().filter(z -> z.getEntityGuid().equals("instance1")).findAny();
        Optional<JdsExample> srcB = memObjects.parallelStream().filter(z -> z.getEntityGuid().equals("instance1")).findAny();
        Optional<JdsExample> srcC = savObjects.parallelStream().filter(z -> z.getEntityGuid().equals("instance2")).findAny();
        Optional<JdsExample> srcD = memObjects.parallelStream().filter(z -> z.getEntityGuid().equals("instance2")).findAny();
        Optional<JdsExample> srcE = savObjects.parallelStream().filter(z -> z.getEntityGuid().equals("instance3")).findAny();
        Optional<JdsExample> srcF = memObjects.parallelStream().filter(z -> z.getEntityGuid().equals("instance3")).findAny();
        Optional<JdsExample> srcG = savObjects.parallelStream().filter(z -> z.getEntityGuid().equals("instance4")).findAny();
        Optional<JdsExample> srcH = memObjects.parallelStream().filter(z -> z.getEntityGuid().equals("instance4")).findAny();
        match(srcA, srcB);
        match(srcC, srcD);
        match(srcE, srcF);
        match(srcG, srcH);
    }

    private void match(Optional<JdsExample> srcA, Optional<JdsExample> srcB) {
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
    public void saveAndLoadOracleImplementation() throws Exception {
        initialiseOracleBackend();
        saveAndLoad();
    }

    @Test
    public void saveAndLoadAllImplementations() throws Exception {
        saveAndLoadSqliteImplementation();
        saveAndLoadTsqlImplementation();
        saveAndLoadPostreSqlImplementation();
        saveAndLoadMySqlImplementation();
        saveAndLoadOracleImplementation();
    }
}
