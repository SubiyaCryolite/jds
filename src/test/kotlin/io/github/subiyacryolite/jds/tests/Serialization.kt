package io.github.subiyacryolite.jds.tests

import io.github.subiyacryolite.jds.tests.common.BaseTestConfig
import io.github.subiyacryolite.jds.JdsDb
import io.github.subiyacryolite.jds.beans.property.SimpleBlobProperty
import org.junit.jupiter.api.Test
import java.io.*
import java.util.*

/**
 * Created by indana on 5/10/2017.
 */
class Serialization : BaseTestConfig("Serialization") {

    @Throws(Exception::class)
    override fun testImpl(jdsDb: JdsDb) {
        testSerialization(jdsDb)
    }

    @Test
    fun testSerialization(jdsDb: JdsDb) {
        for (jdsEntity in jdsDb.classes.values) {
            val canonicalName = jdsEntity.canonicalName
            serialize(jdsEntity, canonicalName)
            deserialize(canonicalName, jdsEntity)
        }
    }

    @Test
    fun testBlobSerialization() {
        val simpleBlobProperty = SimpleBlobProperty(byteArrayOf(0xC9.toByte(), 0xCB.toByte(), 0xBB.toByte(), 0xCC.toByte(), 0xCE.toByte(), 0xB9.toByte(), 0xC8.toByte(), 0xCA.toByte(), 0xBC.toByte(), 0xCC.toByte(), 0xCE.toByte(), 0xB9.toByte(), 0xC9.toByte(), 0xCB.toByte(), 0xBB.toByte()))
        val canonicalName = simpleBlobProperty.javaClass.canonicalName
        serialize(simpleBlobProperty, canonicalName)
        val out = deserialize(canonicalName, simpleBlobProperty.javaClass)
        System.out.printf("pre %s\n", Arrays.toString(simpleBlobProperty.get()))
        System.out.printf("post %s\n", Arrays.toString(out!!.get()))
    }

    private fun <T> serialize(objectToSerialize: T?, fileName: String?) {
        if (fileName == null) {
            throw IllegalArgumentException(
                    "Name of file to which to serialize object to cannot be null.")
        }
        if (objectToSerialize == null) {
            throw IllegalArgumentException("Object to be serialized cannot be null.")
        }
        try {
            FileOutputStream(fileName).use { fos ->
                ObjectOutputStream(fos).use { oos ->
                    oos.writeObject(objectToSerialize)
                    println("Serialization of completed: $objectToSerialize")
                }
            }
        } catch (ioException: IOException) {
            ioException.printStackTrace(System.err)
        }
    }

    private fun <T> deserialize(fileToDeserialize: String?, classBeingDeserialized: Class<out T>?): T? {
        if (fileToDeserialize == null) {
            throw IllegalArgumentException("Cannot deserialize from a null filename.")
        }
        if (classBeingDeserialized == null) {
            throw IllegalArgumentException("Type of class to be deserialized cannot be null.")
        }
        var objectOut: T? = null
        try {
            FileInputStream(fileToDeserialize).use { fis ->
                ObjectInputStream(fis).use { ois ->
                    objectOut = ois.readObject() as T
                    println("Deserialization completed: $objectOut")
                }
            }
        } catch (exception: IOException) {
            exception.printStackTrace(System.err)
        } catch (exception: ClassNotFoundException) {
            exception.printStackTrace(System.err)
        }
        return objectOut
    }
}
