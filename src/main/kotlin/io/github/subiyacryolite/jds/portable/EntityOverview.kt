package io.github.subiyacryolite.jds.portable

import com.fasterxml.jackson.annotation.JsonProperty

/**
 *
 * @param id the unique identified
 * @param editVersion the edit version
 * @param entityId the entity id
 * @param fieldId the field id
 */
data class EntityOverview(
        @get:JsonProperty("i")
        @set:JsonProperty("i")
        var id: String = "",

        @get:JsonProperty("ev")
        @set:JsonProperty("ev")
        var editVersion: Int = 0,

        @get:JsonProperty("e")
        @set:JsonProperty("e")
        var entityId: Int = 0,

        @get:JsonProperty("f")
        @set:JsonProperty("f")
        var fieldId: Int? = null
)