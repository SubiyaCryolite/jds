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

import io.github.subiyacryolite.jds.enums.FieldType
import java.io.Serializable
import java.util.*

/**
 * Represents a jdsField in JDS
 * @param id the unique field identifier
 * @param name the fields name
 * @param type the fields type
 * @param description a description of the field
 * @param tags a set of tags that can be used to filter, populate, or ignore fields
 */
data class Field(
        var id: Int = 0,
        var name: String = "",
        var type: FieldType = FieldType.String,
        var description: String = "",
        var alternateCodes: Map<String, String> = emptyMap(),
        var tags: Set<String> = emptySet()
) : Serializable {

    internal fun bind(): Int {
        if (values.containsKey(this.id)) {
            val existingField = values.getValue(this.id)
            if (this != existingField) {
                throw RuntimeException("The field id [${this.id}] is already bound to [${existingField}]")
            }
        } else {
            values[this.id] = this
        }
        return this.id
    }

    companion object : Serializable {
        private const val serialVersionUID = 20171109_0853L
        internal val values = HashMap<Int, Field>()
    }
}
