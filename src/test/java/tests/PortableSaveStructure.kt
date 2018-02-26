package tests

import common.BaseTestConfig
import entities.AddressBook
import entities.Example
import entities.TimeConstruct
import io.github.subiyacryolite.jds.JdsEntity
import io.github.subiyacryolite.jds.embedded.JdsEmbeddedContainer
import io.github.subiyacryolite.jds.embedded.JdsLoadEmbedded
import io.github.subiyacryolite.jds.embedded.JdsSaveEmbedded
import org.junit.jupiter.api.Test
import java.util.*

class PortableSaveStructure : BaseTestConfig("Portable save structures") {

    @Test
    @Throws(Exception::class)
    fun addressBook() {
        testPortableSave(Arrays.asList(addressBook), AddressBook::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun timeConstruct() {
        testPortableSave(Arrays.asList(timeConstruct), TimeConstruct::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun example() {
        testPortableSave(collection, Example::class.java)
    }

    @Throws(Exception::class)
    private fun testPortableSave(entity: Collection<JdsEntity>, clazz: Class<out JdsEntity>) {
        //fire-up JDS
        initialiseSqLiteBackend()

        val saveEmbedded = JdsSaveEmbedded(entity)
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

        val loadEmbedded = JdsLoadEmbedded(jdsDb, clazz, embeddedObject)
        val loadedEntity = loadEmbedded.call()

        val stringRepresentation1 = entity.toString()
        val stringRepresentation2 = loadedEntity.toString()

        println("Before = $stringRepresentation1")
        println("After  = $stringRepresentation2")

        val equal = stringRepresentation1 == stringRepresentation2
        println("Equal? $equal")
    }
}