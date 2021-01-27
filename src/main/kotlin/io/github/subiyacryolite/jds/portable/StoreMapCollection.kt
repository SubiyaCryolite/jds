package io.github.subiyacryolite.jds.portable

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.subiyacryolite.jds.Field

/**
 * Used to store values of type in a portable manner
 * @param key the value within [Field.id]
 * @param values the corresponding value
 */
data class StoreMapCollection(
    @get:JsonProperty("k")
    @set:JsonProperty("k")
    var key: Int = 0,

    @get:JsonProperty("v")
    @set:JsonProperty("v")
    var values: Map<String, MutableCollection<String>> = HashMap()
)