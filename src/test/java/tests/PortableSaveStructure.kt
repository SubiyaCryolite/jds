package tests

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import common.BaseTestConfig
import entities.AddressBook
import io.github.subiyacryolite.jds.embedded.JdsLoadEmbedded
import io.github.subiyacryolite.jds.embedded.JdsSaveEmbedded
import org.junit.jupiter.api.Test


class PortableSaveStructure : BaseTestConfig() {
    @Test
    @Throws(Exception::class)
    fun testPortableSave() {
        //fire-up JDS
        initialiseSqlLiteBackend()

        val saveEmbedded = JdsSaveEmbedded(sampleAddressBook)
        val embeddedObject = saveEmbedded.call()

        val objectMapper = ObjectMapper()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        //objectMapper.enable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
        //objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX)
        //objectMapper.enable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)

        val output = objectMapper.writeValueAsString(embeddedObject)
        println("================ Object JSON ================")
        println("$output")

        val loadEmbedded = JdsLoadEmbedded(jdsDb, AddressBook::class.java, embeddedObject)
        val loadedAddressBook = loadEmbedded.call()

        val stringRepresentation1 = sampleAddressBook.toString()
        val stringRepresentation2 = loadedAddressBook[0].toString()

        val equal = stringRepresentation1 == stringRepresentation2

        println("Before = $stringRepresentation1")
        println("After  = $stringRepresentation2")
        //Will never be equal. ZonedDateTime doesn't resolve down to nanoseconds
        println("Equal? $equal")
    }
}