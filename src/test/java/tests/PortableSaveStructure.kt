package tests

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import common.BaseTestConfig
import entities.AddressBook
import entities.TimeConstruct
import io.github.subiyacryolite.jds.JdsEntity
import io.github.subiyacryolite.jds.embedded.JdsLoadEmbedded
import io.github.subiyacryolite.jds.embedded.JdsSaveEmbedded
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class PortableSaveStructure : BaseTestConfig() {

    @Test
    @Throws(Exception::class)
    fun addressBook() {
        testPortableSave(sampleAddressBook, AddressBook::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun timeConstruct() {
        testPortableSave(timeConstruct, TimeConstruct::class.java)
    }

    @Throws(Exception::class)
    private fun testPortableSave(entity: JdsEntity, clazz: Class<out JdsEntity>) {
        //fire-up JDS
        initialiseSqlLiteBackend()

        val saveEmbedded = JdsSaveEmbedded(entity)
        val embeddedObject = saveEmbedded.call()

        val objectMapper = ObjectMapper()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        objectMapper.enable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
        objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX)
        objectMapper.enable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)

        val output = objectMapper.writeValueAsString(embeddedObject)
        println("================ Object JSON ================")
        println("$output")

        val loadEmbedded = JdsLoadEmbedded(jdsDb, clazz, embeddedObject)
        val loadedEntity = loadEmbedded.call()

        val stringRepresentation1 = entity.toString()
        val stringRepresentation2 = loadedEntity[0].toString()

        println("Before = $stringRepresentation1")
        println("After  = $stringRepresentation2")

        val equal = stringRepresentation1 == stringRepresentation2
        println("Equal? $equal")
    }
}