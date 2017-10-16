package tests

import common.BaseTestConfig
import entities.AddressBook
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class Json :BaseTestConfig(){
    @Test
    @Throws(Exception::class)
    fun serialization() {
        val output = objectMapper.writeValueAsString(AddressBook())
        Assertions.assertNotNull(output, "Something went bonkers")
        println(output)
    }
}
