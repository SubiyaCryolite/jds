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
package io.github.subiyacryolite.jds

import java.io.Serializable
import java.util.*
import kotlin.collections.HashSet

/**
 * Represents a jdsField enum in JDS
 */
data class FieldEnum<T : Enum<T>>(
        val enumType: Class<T>,
        val field: Field,
        val values: Set<T> = HashSet()
) : Serializable {

    constructor(enumType: Class<T>, field: Field, vararg values: T) : this(enumType, field, setOf(*values))

    init {
        if (!enums.containsKey(field.id)) {
            enums[field.id] = this
        }
    }

    fun valueOf(ordinal: Int): T? {
        return values.firstOrNull { value -> value.ordinal == ordinal }
    }

    fun valueOf(name: String): T? {
        return values.firstOrNull { value -> value.name == name }
    }

    companion object : Serializable {

        private const val serialVersionUID = 20171109_0853L

        val enums: HashMap<Int, FieldEnum<*>> = HashMap()

        /**
         * Public facing method to query the underlying values.
         * Only mapped [FieldEnum] entries will appear in this collection
         */
        fun findAll(fieldIds: Collection<Int>): Collection<FieldEnum<*>> {
            return enums.filter { fieldIds.contains(it.key) }.map { it.value }
        }
    }
}
