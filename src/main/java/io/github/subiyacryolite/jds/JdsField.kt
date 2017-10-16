/*
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

import io.github.subiyacryolite.jds.enums.JdsFieldType
import io.github.subiyacryolite.jds.enums.JdsFieldType.TEXT
import java.io.Externalizable
import java.io.IOException
import java.io.ObjectInput
import java.io.ObjectOutput
import java.util.*

/**
 * Represents a fieldEntity in JDS
 */
class JdsField() : Externalizable {
    var id: Long = 0
        private set
    var name: String = ""
        private set
    var description: String = ""
        private set

    /**
     * @return
     */
    /**
     * @param type
     */
    var type: JdsFieldType = JdsFieldType.TEXT
        private set

    init {
        type = TEXT
    }

    /**
     * @param id
     * @param name
     * @param type
     */
    @JvmOverloads
    constructor(id: Long, name: String, type: JdsFieldType, description: String = "") : this() {
        this.id = id
        this.name = name
        this.type = type
        this.description = description
        bind(this)
    }

    override fun toString(): String {
        return "JdsField{ id = $id, name = $name, type = $type, description = $description }"
    }

    @Throws(IOException::class)
    override fun writeExternal(output: ObjectOutput) {
        output.writeLong(id)
        output.writeUTF(name)
        output.writeUTF(description)
        output.writeObject(type)
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    override fun readExternal(input: ObjectInput) {
        id = input.readLong()
        name = input.readUTF()
        description = input.readUTF()
        type = input.readObject() as JdsFieldType
    }

    companion object {
        private val fields = HashMap<Long, String>()

        /**
         * @param jdsField
         */
        private fun bind(jdsField: JdsField) {
            if (!fields.containsKey(jdsField.id))
                fields.put(jdsField.id, jdsField.name)
            else
                throw RuntimeException(String.format("This jdsField ID [${jdsField.id}] is already bound"))
        }

        val NULL = JdsField()
    }
}
