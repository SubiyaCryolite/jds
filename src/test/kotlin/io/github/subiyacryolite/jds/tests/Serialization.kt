/**
 * Jenesis Data Store Copyright (c) 2017 Ifunga Ndana. All rights reserved.
 *
 * 1. Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 2. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 3. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * Neither the name Jenesis Data Store nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package io.github.subiyacryolite.jds.tests

import io.github.subiyacryolite.jds.beans.property.BlobProperty
import io.github.subiyacryolite.jds.context.DbContext
import io.github.subiyacryolite.jds.tests.common.BaseTestConfig
import org.junit.jupiter.api.Test
import java.io.*

class Serialization : BaseTestConfig("Serialization") {

    @Throws(Exception::class)
    override fun testImpl(dbContext: DbContext) {
        testSerialization(dbContext)
    }

    @Test
    fun testSerialization(dbContext: DbContext) {
        for (jdsEntity in dbContext.classes.values) {
            val canonicalName = jdsEntity.canonicalName
            serialize(jdsEntity, canonicalName)
            deserialize(canonicalName, jdsEntity)
        }
    }

    @Test
    fun testBlobSerialization() {
        val simpleBlobProperty = BlobProperty(byteArrayOf(0xC9.toByte(), 0xCB.toByte(), 0xBB.toByte(), 0xCC.toByte(), 0xCE.toByte(), 0xB9.toByte(), 0xC8.toByte(), 0xCA.toByte(), 0xBC.toByte(), 0xCC.toByte(), 0xCE.toByte(), 0xB9.toByte(), 0xC9.toByte(), 0xCB.toByte(), 0xBB.toByte()))
        val canonicalName = simpleBlobProperty.javaClass.canonicalName
        serialize(simpleBlobProperty, canonicalName)
        val out = deserialize(canonicalName, simpleBlobProperty.javaClass)
        System.out.printf("pre %s\n", simpleBlobProperty.value?.contentToString())
        System.out.printf("post %s\n", out!!.value?.contentToString())
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

    @Test
    fun postGreSql() {
        testPostgreSql()
    }

    @Test
    fun sqlLite() {
        testSqLite()
    }

    @Test
    fun mariaDb() {
        testMariaDb()
    }

    @Test
    fun mySql() {
        testMySql()
    }

    @Test
    fun oracle() {
        testOracle()
    }

    @Test
    fun transactionalSql() {
        testTransactionalSql()
    }
}
