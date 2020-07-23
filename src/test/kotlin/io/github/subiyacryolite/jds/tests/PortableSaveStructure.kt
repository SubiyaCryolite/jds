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
import io.github.subiyacryolite.jds.tests.common.BaseTestConfig
import io.github.subiyacryolite.jds.tests.common.TestData
import io.github.subiyacryolite.jds.tests.entities.*
import io.github.subiyacryolite.jds.tests.enums.Right
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class PortableSaveStructure : BaseTestConfig(Description) {

    companion object {
        private const val Description = "Convert several entities to the portable format"
    }

    override fun testImpl(dbContext: DbContext) {
        addressBook(dbContext)
        timeConstruct(dbContext)
        example(dbContext)
        mapTests(dbContext)
        enumTests(dbContext)
    }

    @Test
    @Tag("PostGreSQL")
    @DisplayName("PostGreSQL: $Description")
    fun postGreSql() = testPostgreSql()

    @Test
    @Tag("SQLite")
    @DisplayName("SQLite: $Description")
    fun sqlLite() = testSqLite()

    @Test
    @Tag("MariaDb")
    @DisplayName("MariaDb: $Description")
    fun mariaDb() = testMariaDb()

    @Test
    @Tag("MySq")
    @DisplayName("MySq: $Description")
    fun mySql() = testMySql()

    @Test
    @Tag("Oracle")
    @DisplayName("Oracle: $Description")
    fun oracle() = testOracle()

    @Test
    @Tag("TSql")
    @DisplayName("T-SQL: $Description")
    fun transactionalSql() = testTransactionalSql()

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

        val outputJds = objectMapper.writeValueAsString(portableContainer)
        val outputReg = objectMapper.writeValueAsString(entity)
        println("")
        println("")
        println("================ Object Reg JSON ================")
        println(outputReg)
        println("================ Object JDS JSON ================")
        println(outputJds)
        println("")

        val loadFromMemory = LoadPortable(dbContext, clazz, portableContainer)
        val memoryPayload = loadFromMemory.call()

        val portableContainerFromJson = objectMapper.readValue(outputJds, PortableContainer::class.java)
        val loadFromJson = LoadPortable(dbContext, clazz, portableContainerFromJson)
        val jsonPayload = loadFromJson.call()

        println("Original: $entity")
        println("Memory instance  = $memoryPayload")
        println("JSON instance= $jsonPayload")

        val isTheSame = portableContainerFromJson == portableContainer
        println("Is the same after deserialisation? $isTheSame")

        val equal = memoryPayload == jsonPayload
        println("Equal? $equal")
    }
}