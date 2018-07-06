package tests

import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

class TestNullable {

    @Test
    fun isNullale() {
        val zonedDateTime = ZonedDateTime.now()
        if (zonedDateTime is ZonedDateTime?)
            println("$zonedDateTime is a ZonedDateTime?")
        if (zonedDateTime is ZonedDateTime)
            println("$zonedDateTime is also a ZonedDateTime?")
    }
}