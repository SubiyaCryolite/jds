package io.github.subiyacryolite.jds.tests

import io.github.subiyacryolite.jds.JdsDb
import io.github.subiyacryolite.jds.JdsEntity
import io.github.subiyacryolite.jds.embedded.JdsEmbeddedContainer
import io.github.subiyacryolite.jds.embedded.JdsLoadEmbedded
import io.github.subiyacryolite.jds.embedded.JdsSaveEmbedded
import io.github.subiyacryolite.jds.tests.common.BaseTestConfig
import io.github.subiyacryolite.jds.tests.common.TestData
import io.github.subiyacryolite.jds.tests.entities.AddressBook
import io.github.subiyacryolite.jds.tests.entities.Example
import io.github.subiyacryolite.jds.tests.entities.TimeConstruct
import org.junit.jupiter.api.Test
import java.util.*

class PortableSaveStructure : BaseTestConfig("Portable save structures") {

    @Throws(Exception::class)
    override fun testImpl(jdsDb: JdsDb) {
        addressBook(jdsDb)
        timeConstruct(jdsDb)
        example(jdsDb)
    }

    @Test
    @Throws(Exception::class)
    fun addressBook(jdsDb: JdsDb) {
        testPortableSave(jdsDb, Arrays.asList(TestData.addressBook), AddressBook::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun timeConstruct(jdsDb: JdsDb) {
        testPortableSave(jdsDb, Arrays.asList(TestData.timeConstruct), TimeConstruct::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun example(jdsDb: JdsDb) {
        testPortableSave(jdsDb, TestData.collection, Example::class.java)
    }

    @Throws(Exception::class)
    private fun testPortableSave(jdsDb: JdsDb, entity: Collection<JdsEntity>, clazz: Class<out JdsEntity>) {
        //fire-up JDS
        initialiseSqLiteBackend()

        val saveEmbedded = JdsSaveEmbedded(jdsDb, entity)
        val embeddedObject = saveEmbedded.call()


        val outputJds = objectMapper.writeValueAsString(embeddedObject)
        val outputReg = objectMapper.writeValueAsString(entity)
        println("================ Object Reg JSON ================")
        println("$outputReg")
        println("================ Object JDS JSON ================")
        println("$outputJds")


        val embeddedObjectFromJson = objectMapper.readValue(outputJds, JdsEmbeddedContainer::class.java)
        val isTheSame = embeddedObjectFromJson == embeddedObject
        println("Is the same? = $isTheSame")

        val loadEmbeddedA = JdsLoadEmbedded(jdsDb, clazz, embeddedObject)
        val loadedEntityA = loadEmbeddedA.call()

        val loadEmbeddedB = JdsLoadEmbedded(jdsDb, clazz, embeddedObjectFromJson)
        val loadedEntityB = loadEmbeddedB.call()

        val stringRepresentation1 = entity.toString()
        val stringRepresentation2 = loadedEntityA.toString()
        val stringRepresentation3 = loadedEntityB.toString()

        println("Before = $stringRepresentation1")
        println("After  = $stringRepresentation2")
        println("After  FROM json = $stringRepresentation3")

        val equal = stringRepresentation1 == stringRepresentation2
        println("Equal? $equal")
    }
}