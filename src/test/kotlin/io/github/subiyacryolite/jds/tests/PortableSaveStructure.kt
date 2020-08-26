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
import io.github.subiyacryolite.jds.context.DbContext
import io.github.subiyacryolite.jds.portable.LoadPortable
import io.github.subiyacryolite.jds.portable.PortableContainer
import io.github.subiyacryolite.jds.portable.SavePortable
import io.github.subiyacryolite.jds.tests.Serialization.Companion.fromByteArray
import io.github.subiyacryolite.jds.tests.Serialization.Companion.toByteArray
import io.github.subiyacryolite.jds.tests.common.BaseTestConfig
import io.github.subiyacryolite.jds.tests.common.TestData
import io.github.subiyacryolite.jds.tests.entities.*
import io.github.subiyacryolite.jds.tests.enums.Right
import org.junit.jupiter.api.DisplayName

@DisplayName("Convert several entities to the portable format")
class PortableSaveStructure : BaseTestConfig() {

    override fun testImpl(dbContext: DbContext) {
        addressBook(dbContext)
        timeConstruct(dbContext)
        example(dbContext)
        mapTests(dbContext)
        enumTests(dbContext)
    }

    private fun addressBook(dbContext: DbContext) {
        testPortableSave(dbContext, listOf(TestData.addressBook), AddressBook::class.java)
    }

    private fun timeConstruct(dbContext: DbContext) {
        testPortableSave(dbContext, listOf(TestData.timeConstruct), TimeConstruct::class.java)
    }

    private fun example(dbContext: DbContext) {
        testPortableSave(dbContext, TestData.collection, Example::class.java)
    }

    private fun mapTests(dbContext: DbContext) {
        val mapExample = MapExample()
        mapExample.intMap[5] = "Five"
        mapExample.intMap[6] = "Six"
        mapExample.stringMap["latitude"] = "50.0"
        mapExample.stringMap["longitude"] = "50.0"
        testPortableSave(dbContext, setOf(mapExample), MapExample::class.java)
    }

    private fun enumTests(dbContext: DbContext) {
        val login = Login()
        login.rights.add(Right.CreateUser)
        login.rights.add(Right.DeleteUser)
        testPortableSave(dbContext, setOf(login), Login::class.java)
    }

    private fun testPortableSave(dbContext: DbContext, entity: Collection<Entity>, clazz: Class<out Entity>) {
        val portableContainer = SavePortable(dbContext, entity).call()

        val portableContainerAsJson = objectMapper.writeValueAsString(portableContainer)
        val outputReg = objectMapper.writeValueAsString(entity)

        val portableContainerAsBinary = portableContainer.toByteArray()

        println("================================")
        println("Standard JSON: $outputReg")
        println("jds JSON: $portableContainerAsJson")
        println("jds Bytes: $portableContainerAsBinary")
        println("================================")
        println("")

        val loadFromMemory = LoadPortable(dbContext, clazz, portableContainer)
        val memoryPayload = loadFromMemory.call()

        val jsonPortableContainer = objectMapper.readValue(portableContainerAsJson, PortableContainer::class.java)
        val loadFromJson = LoadPortable(dbContext, clazz, jsonPortableContainer)
        val jsonPayload = loadFromJson.call()

        val binaryPortableContainer: PortableContainer = fromByteArray(portableContainerAsBinary)
        val loadFromBinary = LoadPortable(dbContext, clazz, binaryPortableContainer)
        val binaryPayload = loadFromBinary.call()

        println("Original: $entity")
        println("Memory instance  = $memoryPayload")
        println("JSON instance= $jsonPayload")
        println("Binary instance= $binaryPayload")

        var isTheSame = portableContainer == jsonPortableContainer
        println("Memory container equal to JSON container? $isTheSame")

        isTheSame = portableContainer == binaryPortableContainer
        println("Memory container equal to Binary container? $isTheSame")

        isTheSame = memoryPayload == jsonPayload
        println("Memory payload equal to JSON payload? $isTheSame")

        isTheSame = memoryPayload == binaryPayload
        println("Memory payload equal to Binary payload? $isTheSame")
    }
}