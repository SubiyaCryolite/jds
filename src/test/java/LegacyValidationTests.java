import common.BaseTestConfig;
import entities.JdsExample;
import io.github.subiyacryolite.jds.JdsLoad;
import io.github.subiyacryolite.jds.JdsSave;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by indana on 5/17/2017.
 */
public class LegacyValidationTests extends BaseTestConfig {
    @Test
    @Override
    public void saveAndLoad() throws Exception {
        System.out.printf("=========== %s ===========\n", jdsDb.getImplementation());
        List<JdsExample> memObjects = getCollection();
        JdsSave.Companion.save(jdsDb, 1, memObjects);
        System.out.printf("Saved %s\n", memObjects);

        List<JdsExample> savObjects = JdsLoad.Companion.load(jdsDb, JdsExample.class); //load all entities of type AddressBook
        List<JdsExample> specificObject = JdsLoad.Companion.load(jdsDb, JdsExample.class, "instance4"); //load all entities of type AddressBook with Entity Guids in range
        System.out.printf("All entities [%s]\n", savObjects);
        System.out.printf("Specific entities [%s]\n", specificObject);

        Optional<JdsExample> srcA = savObjects.parallelStream().filter(z -> z.getOverview().getEntityGuid().equals("instance1")).findAny();
        Optional<JdsExample> srcB = memObjects.parallelStream().filter(z -> z.getOverview().getEntityGuid().equals("instance1")).findAny();
        Optional<JdsExample> srcC = savObjects.parallelStream().filter(z -> z.getOverview().getEntityGuid().equals("instance2")).findAny();
        Optional<JdsExample> srcD = memObjects.parallelStream().filter(z -> z.getOverview().getEntityGuid().equals("instance2")).findAny();
        Optional<JdsExample> srcE = savObjects.parallelStream().filter(z -> z.getOverview().getEntityGuid().equals("instance3")).findAny();
        Optional<JdsExample> srcF = memObjects.parallelStream().filter(z -> z.getOverview().getEntityGuid().equals("instance3")).findAny();
        Optional<JdsExample> srcG = savObjects.parallelStream().filter(z -> z.getOverview().getEntityGuid().equals("instance4")).findAny();
        Optional<JdsExample> srcH = memObjects.parallelStream().filter(z -> z.getOverview().getEntityGuid().equals("instance4")).findAny();
        match(srcA, srcB);
        match(srcC, srcD);
        match(srcE, srcF);
        match(srcG, srcH);
    }

    private void match(Optional<JdsExample> srcA, Optional<JdsExample> srcB) {
        assertTrue(srcA.isPresent(), "srcA is not present");
        assertTrue(srcB.isPresent(), "srcB is not present");
        //date, localTime, zonedDateTime and localDateTime is accurate up to seconds [not nanoseconds]
        assertEquals(srcA.get().getDateField().getYear(), srcB.get().getDateField().getYear(), "DateField getYear " + srcA.get().getOverview().getEntityGuid());
        assertEquals(srcA.get().getDateField().getMonth(), srcB.get().getDateField().getMonth(), "DateField getMonth " + srcA.get().getOverview().getEntityGuid());
        assertEquals(srcA.get().getDateField().getDayOfMonth(), srcB.get().getDateField().getDayOfMonth(), "DateField getDayOfMonth " + srcA.get().getOverview().getEntityGuid());

        assertEquals(srcA.get().getTimeField().getHour(), srcB.get().getTimeField().getHour(), "TimeField getHour " + srcA.get().getOverview().getEntityGuid());
        assertEquals(srcA.get().getTimeField().getMinute(), srcB.get().getTimeField().getMinute(), "TimeField getMinute " + srcA.get().getOverview().getEntityGuid());
        assertEquals(srcA.get().getTimeField().getSecond(), srcB.get().getTimeField().getSecond(), "TimeField getSecond " + srcA.get().getOverview().getEntityGuid());

        assertEquals(srcA.get().getDateTimeField().getYear(), srcB.get().getDateTimeField().getYear(), "DateTimeField getYear " + srcA.get().getOverview().getEntityGuid());
        assertEquals(srcA.get().getDateTimeField().getMonth(), srcB.get().getDateTimeField().getMonth(), "DateTimeField getMonth " + srcA.get().getOverview().getEntityGuid());
        assertEquals(srcA.get().getDateTimeField().getDayOfMonth(), srcB.get().getDateTimeField().getDayOfMonth(), "DateTimeField getDayOfMonth " + srcA.get().getOverview().getEntityGuid());
        assertEquals(srcA.get().getDateTimeField().getHour(), srcB.get().getDateTimeField().getHour(), "DateTimeField getHour " + srcA.get().getOverview().getEntityGuid());
        assertEquals(srcA.get().getDateTimeField().getMinute(), srcB.get().getDateTimeField().getMinute(), "DateTimeField getMinute " + srcA.get().getOverview().getEntityGuid());
        assertEquals(srcA.get().getDateTimeField().getSecond(), srcB.get().getDateTimeField().getSecond(), "DateTimeField getSecond " + srcA.get().getOverview().getEntityGuid());

        assertEquals(srcA.get().getZonedDateTimeField().getYear(), srcB.get().getZonedDateTimeField().getYear(), "ZonedDateTimeField getYear " + srcA.get().getOverview().getEntityGuid());
        assertEquals(srcA.get().getZonedDateTimeField().getMonth(), srcB.get().getZonedDateTimeField().getMonth(), "ZonedDateTimeField getMonth " + srcA.get().getOverview().getEntityGuid());
        assertEquals(srcA.get().getZonedDateTimeField().getDayOfMonth(), srcB.get().getZonedDateTimeField().getDayOfMonth(), "ZonedDateTimeField getDayOfMonth " + srcA.get().getOverview().getEntityGuid());
        assertEquals(srcA.get().getZonedDateTimeField().getHour(), srcB.get().getZonedDateTimeField().getHour(), "ZonedDateTimeField getHour " + srcA.get().getOverview().getEntityGuid());
        assertEquals(srcA.get().getZonedDateTimeField().getMinute(), srcB.get().getZonedDateTimeField().getMinute(), "ZonedDateTimeField getMinute " + srcA.get().getOverview().getEntityGuid());
        assertEquals(srcA.get().getZonedDateTimeField().getSecond(), srcB.get().getZonedDateTimeField().getSecond(), "ZonedDateTimeField getSecond " + srcA.get().getOverview().getEntityGuid());
        assertEquals(srcA.get().getZonedDateTimeField().getZone(), srcB.get().getZonedDateTimeField().getZone(), "ZonedDateTimeField getZone " + srcA.get().getOverview().getEntityGuid());
        assertEquals(srcA.get().getZonedDateTimeField().getOffset(), srcB.get().getZonedDateTimeField().getOffset(), "ZonedDateTimeField getOffset " + srcA.get().getOverview().getEntityGuid());

        assertEquals(srcA.get().getStringField(), srcB.get().getStringField(), "StringField " + srcA.get().getOverview().getEntityGuid());
        assertEquals(srcA.get().getIntField(), srcB.get().getIntField(), "IntField " + srcA.get().getOverview().getEntityGuid());
        assertEquals(srcA.get().getFloatField(), srcB.get().getFloatField(), DELTA, "FloatField " + srcA.get().getOverview().getEntityGuid());
        assertEquals(srcA.get().getDoubleField(), srcB.get().getDoubleField(), DELTA, "DoubleField " + srcA.get().getOverview().getEntityGuid());
        assertEquals(srcA.get().getLongField(), srcB.get().getLongField(), "LongField " + srcA.get().getOverview().getEntityGuid());
        assertEquals(srcA.get().getBooleanField(), srcB.get().getBooleanField(), "BooleanField " + srcA.get().getOverview().getEntityGuid());
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
