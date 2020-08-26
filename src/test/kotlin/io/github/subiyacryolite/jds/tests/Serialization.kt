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

import io.github.subiyacryolite.jds.Entity
import io.github.subiyacryolite.jds.beans.property.BlobValue
import io.github.subiyacryolite.jds.context.DbContext
import io.github.subiyacryolite.jds.tests.common.BaseTestConfig
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.*

@DisplayName("Tests serialisation of all defined entities")
class Serialization : BaseTestConfig() {

    override fun testImpl(dbContext: DbContext) {
        for (jdsEntity in Entity.classes.values) {
            val canonicalName = jdsEntity.canonicalName
            val fileName = "$canonicalName.tmp"
            val ba = jdsEntity.toByteArray()
            deserialize(ba, jdsEntity)
            File(fileName).delete()
        }
    }

    @Test
    @Tag("standalone")
    @DisplayName("Test basic serialisation and deserialization")
    fun testBlobSerialization() {
        val simpleBlobProperty = BlobValue(byteArrayOf(0xC9.toByte(), 0xCB.toByte(), 0xBB.toByte(), 0xCC.toByte(), 0xCE.toByte(), 0xB9.toByte(), 0xC8.toByte(), 0xCA.toByte(), 0xBC.toByte(), 0xCC.toByte(), 0xCE.toByte(), 0xB9.toByte(), 0xC9.toByte(), 0xCB.toByte(), 0xBB.toByte()))
        val canonicalName = simpleBlobProperty.javaClass.canonicalName
        val fileName = "$canonicalName.tmp"
        val ba = simpleBlobProperty.toByteArray()
        val out = deserialize(ba, simpleBlobProperty.javaClass)
        System.out.printf("pre %s\n", simpleBlobProperty.value.contentToString())
        System.out.printf("post %s\n", out!!.value.contentToString())
        File(fileName).delete()
    }

    private fun <T> deserialize(byteArray: ByteArray, classBeingDeserialized: Class<out T>?): T? {
        return fromByteArray(byteArray)
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun <T : Serializable> fromByteArray(byteArray: ByteArray): T {
            val byteArrayInputStream = ByteArrayInputStream(byteArray)
            val objectInput: ObjectInput
            objectInput = ObjectInputStream(byteArrayInputStream)
            val result = objectInput.readObject() as T
            objectInput.close()
            byteArrayInputStream.close()
            return result
        }

        fun Serializable.toByteArray(): ByteArray {
            val byteArrayOutputStream = ByteArrayOutputStream()
            val objectOutputStream: ObjectOutputStream
            objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
            objectOutputStream.writeObject(this)
            objectOutputStream.flush()
            val result = byteArrayOutputStream.toByteArray()
            byteArrayOutputStream.close()
            objectOutputStream.close()
            return result
        }
    }
}
