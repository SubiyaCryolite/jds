package io.github.subiyacryolite.jds.tests

import io.github.subiyacryolite.jds.tests.common.BaseTestConfig
import io.github.subiyacryolite.jds.tests.common.TestData
import io.github.subiyacryolite.jds.JdsDb
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class Json :BaseTestConfig("Json tests"){

    @Throws(Exception::class)
    override fun testImpl(jdsDb: JdsDb) {}

    @Test
    @Throws(Exception::class)
    fun serialization() {
        val output = objectMapper.writeValueAsString(TestData.addressBook)
        Assertions.assertNotNull(output, "Something went bonkers")
        println(output)
    }
}
