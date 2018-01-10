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

import java.io.Externalizable
import java.io.IOException
import java.io.ObjectInput
import java.io.ObjectOutput
import java.util.*

/**
 * Represents a fieldEntity enum in JDS
 */
class JdsFieldEnum<T : Enum<T>>() : Externalizable {
    lateinit var enumType: Class<T>
    lateinit var field: JdsField

    var values = arrayOfNulls<Enum<T>>(0)
        private set//keep order at all times

    /**
     * @param type
     */
    constructor(type: Class<T>) : this() {
        this.enumType = type
    }

    /**
     * @param type
     * @param field
     * @param values
     */
    constructor(type: Class<T>, field: JdsField, vararg values: T) : this(type) {
        this.field = field
        this.values = arrayOfNulls(values.size)
        System.arraycopy(values, 0, this.values, 0, values.size)
        bind()
    }

    /**
     *
     */
    private fun bind() {
        if (!enums.containsKey(field.id))
            enums.put(field.id, this)
    }

    override fun toString(): String {
        return "JdsFieldEnum{ fieldEntity= $field , values=$values }"
    }

    /**
     * @param enumText
     * @return
     *
     */
    fun indexOf(enumText: Enum<*>): Int {
        return values.indices.firstOrNull { values[it] === enumText } ?: -1
    }

    /**
     * @param index
     * @return
     */
    fun valueOf(index: Int): Enum<*>? {
        return if (index >= values.size) null else values[index]
    }

    /**
     * @param out
     * @throws IOException
     */
    @Throws(IOException::class)
    override fun writeExternal(out: ObjectOutput) {
        out.writeObject(enumType)
        out.writeObject(field)
        out.writeObject(values)
    }

    /**
     * @param input
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Throws(IOException::class, ClassNotFoundException::class)
    override fun readExternal(input: ObjectInput) {
        enumType = input.readObject() as Class<T>
        field = input.readObject() as JdsField
        values = input.readObject() as Array<Enum<T>?>
    }

    companion object {

        private val serialVersionUID = 20171109_0853L

        private val enums: HashMap<Long, JdsFieldEnum<*>> = HashMap<Long, JdsFieldEnum<*>>()

        /**
         * @param field
         * @return
         */
        operator fun get(field: JdsField): JdsFieldEnum<*>? {
            return if (enums.containsKey(field.id))
                enums[field.id]
            else
                throw RuntimeException("This jdsField $field has not been bound to any enums")
        }

        /**
         * @param fieldId
         * @return
         */
        operator fun get(fieldId: Long): JdsFieldEnum<*>? {
            return if (enums.containsKey(fieldId))
                enums[fieldId]
            else
                throw RuntimeException(String.format("This field [%s] has not been bound to any enums", fieldId))
        }
    }
}
