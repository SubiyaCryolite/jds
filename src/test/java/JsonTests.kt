import com.fasterxml.jackson.databind.ObjectMapper
import entities.AddressBook
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class JsonTests {
    @Test
    @Throws(Exception::class)
    fun serialization() {
        val objectMapper = ObjectMapper()
        val output = objectMapper.writeValueAsString(AddressBook())
        Assertions.assertNotNull(output, "Something went bonkers")
    }
}
