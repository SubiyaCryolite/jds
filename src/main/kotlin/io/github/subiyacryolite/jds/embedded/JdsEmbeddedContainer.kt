/**
 * Jenesis Data Store Copyright (c) 2017 Ifunga Ndana. All rights reserved.
 *
 * 1. Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 2. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 3. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * Neither the name Jenesis Data Store nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package io.github.subiyacryolite.jds.embedded

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.subiyacryolite.jds.JdsEntity
import io.github.subiyacryolite.jds.JdsField
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation
import java.sql.Timestamp
import java.util.*

/**
 * Used to store values of type in a portable manner
 * @param key the [field][JdsField]] [ID][JdsField.id]
 * @param value the corresponding value
 */
data class JdsStoreBlob(@get:JsonProperty("k") @set:JsonProperty("k") var key: Long = 0, @get:JsonProperty("v") @set:JsonProperty("v") var value: ByteArray? = null) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as JdsStoreBlob

        if (key != other.key) return false
        if (!Arrays.equals(value, other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + (value?.let { Arrays.hashCode(it) } ?: 0)
        return result
    }
}

/**
 * Used to store values of type in a portable manner
 * @param key the [field][JdsField] [ID][JdsField.id]
 * @param value the corresponding value
 */
data class JdsStoreBoolean(@get:JsonProperty("k") @set:JsonProperty("k") var key: Long = 0, @get:JsonProperty("v") @set:JsonProperty("v") var value: Int? = null)

/**
 * Used to store values of type in a portable manner
 * @param key the [field][JdsField] [ID][JdsField.id]
 * @param value the corresponding value
 */
data class JdsStoreDouble(@get:JsonProperty("k") @set:JsonProperty("k") var key: Long = 0, @get:JsonProperty("v") @set:JsonProperty("v") var value: Double? = null)

/**
 * Used to store values of type in a portable manner
 * @param key the [field][JdsField] [ID][JdsField.id]
 * @param values the corresponding value
 */
data class JdsStoreDoubleCollection(@get:JsonProperty("k") @set:JsonProperty("k") var key: Long = 0, @get:JsonProperty("v") @set:JsonProperty("v") var values: MutableCollection<Double?> = ArrayList())

/**
 * Used to store values of type in a portable manner
 * @param key the [field][JdsField] [ID][JdsField.id]
 * @param value the corresponding value
 */
data class JdsStoreEnum(@get:JsonProperty("k") @set:JsonProperty("k") var key: Long = 0, @get:JsonProperty("v") @set:JsonProperty("v") var value: Int? = null)

/**
 * Used to store values of type in a portable manner
 * @param key the [field][JdsField] [ID][JdsField.id]
 * @param value the corresponding value
 */
data class JdsStoreEnumString(@get:JsonProperty("k") @set:JsonProperty("k") var key: Long = 0, @get:JsonProperty("v") @set:JsonProperty("v") var value: String? = null)

/**
 * Used to store values of type in a portable manner
 * @param key the [field][JdsField] [ID][JdsField.id]
 * @param values the corresponding value
 */
data class JdsStoreEnumCollection(@get:JsonProperty("k") @set:JsonProperty("k") var key: Long = 0, @get:JsonProperty("v") @set:JsonProperty("v") var values: MutableCollection<Int?> = ArrayList())

/**
 * Used to store values of type in a portable manner
 * @param key the [field][JdsField] [ID][JdsField.id]
 * @param values the corresponding value
 */
data class JdsStoreEnumStringCollection(@get:JsonProperty("k") @set:JsonProperty("k") var key: Long = 0, @get:JsonProperty("v") @set:JsonProperty("v") var values: MutableCollection<String?> = ArrayList())


/**
 * Used to store values of type in a portable manner
 * @param key the [field][JdsField] [ID][JdsField.id]
 * @param value the corresponding value
 */
data class JdsStoreInteger(@get:JsonProperty("k") @set:JsonProperty("k") var key: Long = 0, @get:JsonProperty("v") @set:JsonProperty("v") var value: Int? = null)

/**
 * Used to store values of type in a portable manner
 * @param key the [field][JdsField] [ID][JdsField.id]
 * @param values the corresponding value
 */
data class JdsStoreIntegerCollection(@get:JsonProperty("k") @set:JsonProperty("k") var key: Long = 0, @get:JsonProperty("v") @set:JsonProperty("v") var values: MutableCollection<Int?> = ArrayList())

/**
 * Used to store values of type in a portable manner
 * @param key the [field][JdsField] [ID][JdsField.id]
 * @param value the corresponding value
 */
data class JdsStoreLong(@get:JsonProperty("k") @set:JsonProperty("k") var key: Long = 0, @get:JsonProperty("v") @set:JsonProperty("v") var value: Long? = null)

/**
 * Used to store values of type in a portable manner
 * @param key the [field][JdsField] [ID][JdsField.id]
 * @param value the corresponding value
 */
data class JdsStoreZonedDateTime(@get:JsonProperty("k") @set:JsonProperty("k") var key: Long = 0, @get:JsonProperty("v") @set:JsonProperty("v") var value: Long? = null)

/**
 * Used to store values of type in a portable manner
 * @param key the [field][JdsField] [ID][JdsField.id]
 * @param values the corresponding value
 */
data class JdsStoreLongCollection(@get:JsonProperty("k") @set:JsonProperty("k") var key: Long = 0, @get:JsonProperty("v") @set:JsonProperty("v") var values: MutableCollection<Long?> = ArrayList())

/**
 * Used to store values of type in a portable manner
 * @param key the [field][JdsField] [ID][JdsField.id]
 * @param value the corresponding value
 */
data class JdsStoreTime(@get:JsonProperty("k") @set:JsonProperty("k") var key: Long = 0, @get:JsonProperty("v") @set:JsonProperty("v") var value: Long? = null)

/**
 * Used to store values of type in a portable manner
 * @param key the [field][JdsField] [ID][JdsField.id]
 * @param value the corresponding value
 */
data class JdsStoreString(@get:JsonProperty("k") @set:JsonProperty("k") var key: Long = 0, @get:JsonProperty("v") @set:JsonProperty("v") var value: String? = null)

/**
 * Used to store values of type in a portable manner
 * @param key the [field][JdsField] [ID][JdsField.id]
 * @param values the corresponding value
 */
data class JdsStoreStringCollection(@get:JsonProperty("k") @set:JsonProperty("k") var key: Long = 0, @get:JsonProperty("v") @set:JsonProperty("v") var values: MutableCollection<String?> = ArrayList())

/**
 * Used to store values of type in a portable manner
 * @param key the [field][JdsField] [ID][JdsField.id]
 * @param value the corresponding value
 */
data class JdsStoreFloat(@get:JsonProperty("k") @set:JsonProperty("k") var key: Long = 0, @get:JsonProperty("v") @set:JsonProperty("v") var value: Float? = null)

/**
 * Used to store values of type in a portable manner
 * @param key the [field][JdsField] [ID][JdsField.id]
 * @param values the corresponding value
 */
data class JdsStoreFloatCollection(@get:JsonProperty("k") @set:JsonProperty("k") var key: Long = 0, @get:JsonProperty("v") @set:JsonProperty("v") var values: MutableCollection<Float?> = ArrayList())

/**
 * Used to store values of type in a portable manner
 * @param key the [field][JdsField] [ID][JdsField.id]
 * @param value the corresponding value
 */
data class JdsStoreDateTime(@get:JsonProperty("k") @set:JsonProperty("k") var key: Long = 0, @get:JsonProperty("v") @set:JsonProperty("v") var value: Timestamp? = null)

/**
 * Used to store values of type in a portable manner
 * @param key the [field][JdsField] [ID][JdsField.id]
 * @param values the corresponding value
 */
data class JdsStoreDateTimeCollection(@get:JsonProperty("k") @set:JsonProperty("k") var key: Long = 0, @get:JsonProperty("v") @set:JsonProperty("v") var values: MutableCollection<Timestamp?> = ArrayList())

/**
 * Used to store values of type in a portable manner
 * @param key the [field][JdsField] [ID][JdsField.id]
 * @param value the corresponding value
 */
data class JdsStoreDate(@get:JsonProperty("k") @set:JsonProperty("k") var key: Long = 0, @get:JsonProperty("v") @set:JsonProperty("v") var value: Timestamp? = null)

/**
 * Used to store values of type in a portable manner
 * @param key the [field][JdsField] [ID][JdsField.id]
 * @param value the corresponding value
 */
data class JdsStoreDuration(@get:JsonProperty("k") @set:JsonProperty("k") var key: Long = 0, @get:JsonProperty("v") @set:JsonProperty("v") var value: Long? = null)

/**
 * Used to store values of type in a portable manner
 * @param key the [field][JdsField]] [ID][JdsField.id]
 * @param value the corresponding value
 */
data class JdsStoreYearMonth(@get:JsonProperty("k") @set:JsonProperty("k") var key: Long = 0, @get:JsonProperty("v") @set:JsonProperty("v") var value: String? = null)

/**
 * Used to store values of type in a portable manner
 * @param key the [field][JdsField] [ID][JdsField.id]
 * @param value the corresponding value
 */
data class JdsStoreMonthDay(@get:JsonProperty("k") @set:JsonProperty("k") var key: Long = 0, @get:JsonProperty("v") @set:JsonProperty("v") var value: String? = null)

/**
 * Used to store values of type in a portable manner
 * @param key the [field][JdsField] [ID][JdsField.id]
 * @param value the corresponding value
 */
data class JdsStorePeriod(@get:JsonProperty("k") @set:JsonProperty("k") var key: Long = 0, @get:JsonProperty("v") @set:JsonProperty("v") var value: String? = null)

/**
 *
 * @param uuid uuid
 * @param editVersion uuid location version
 * @param entityId entity id
 * @param fieldId field id
 * @param version version
 */
data class JdsEntityOverview(@get:JsonProperty("u") @set:JsonProperty("u") var uuid: String = "",
                             @get:JsonProperty("ev") @set:JsonProperty("ev") var editVersion: Int = 0,
                             @get:JsonProperty("e") @set:JsonProperty("e") var entityId: Long = 0,
                             @get:JsonProperty("f") @set:JsonProperty("f") var fieldId: Long? = null)

/**
 * @param entities a collection of [JdsEntity][JdsEntity] objects to store in a portable manner
 */
class JdsEmbeddedContainer(entities: Iterable<JdsEntity>) {

    //empty constructor needed for json serialization
    constructor() : this(emptyList())

    /**
     * Embedded objects
     */
    @get:JsonProperty("e")
    val embeddedObjects: MutableList<JdsEmbeddedObject> = ArrayList()

    init {
        entities.forEach {
            val classHasAnnotation = it.javaClass.isAnnotationPresent(JdsEntityAnnotation::class.java)
            val superclassHasAnnotation = it.javaClass.superclass.isAnnotationPresent(JdsEntityAnnotation::class.java)
            if (classHasAnnotation || superclassHasAnnotation) {
                val embeddedObject = JdsEmbeddedObject()
                embeddedObject.fieldId = null
                embeddedObject.init(it)
                embeddedObjects.add(embeddedObject)
            } else {
                throw RuntimeException("You must annotate the class [" + it.javaClass.canonicalName + "] or its parent with [" + JdsEntityAnnotation::class.java + "]")
            }
        }
    }
}