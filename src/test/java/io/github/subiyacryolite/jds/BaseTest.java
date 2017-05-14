package io.github.subiyacryolite.jds;

import io.github.subiyacryolite.jds.classes.SimpleAddress;
import io.github.subiyacryolite.jds.classes.SimpleAddressBook;
import io.github.subiyacryolite.jds.classes.TypeClass;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by ifung on 08/04/2017.
 */
public abstract class BaseTest {

    protected final double DELTA = 1e-15;
    protected JdsDb jdsDataBase;

    public void saveAndLoad() throws Exception {
    }

    public void saveObject() throws Exception {
    }

    public void testLoads() throws Exception {
    }

    @Before
    public void beforeAll() {
        initialiseJdsClasses();
    }

    @Test
    public void initialiseJdsClasses() {
        JdsEntityClasses.map(TypeClass.class);
        JdsEntityClasses.map(SimpleAddress.class);
        JdsEntityClasses.map(SimpleAddressBook.class);
    }


    @Test
    public void initialiseSqlLiteBackend() {
        jdsDataBase = new JdsDbSqliteImplementation();
        jdsDataBase.init();
        jdsDataBase.logEdits(true);
    }

    @Test
    public void initialisePostgeSqlBackend() {
        jdsDataBase = new JdsDbPostgreSqlmplementation();
        jdsDataBase.init();
        jdsDataBase.logEdits(true);
    }

    @Test
    public void initialiseTSqlBackend() {
        jdsDataBase = new JdsDbTransactionalSqllmplementation();
        jdsDataBase.init();
        jdsDataBase.logEdits(true);
    }

    @Test
    public void initialiseMysqlBackend() {
        jdsDataBase = new JdsDbMySqlImplementation();
        jdsDataBase.init();
        jdsDataBase.logEdits(true);
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
}
