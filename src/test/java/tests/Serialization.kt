package tests

import common.BaseTestConfig
import io.github.subiyacryolite.jds.beans.property.SimpleBlobProperty
import org.junit.jupiter.api.Test

import java.util.Arrays

/**
 * Created by indana on 5/10/2017.
 */
class Serialization : BaseTestConfig("Serialization") {

    @Test
    fun testSerialization() {
        initialiseSqLiteBackend()
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
}
