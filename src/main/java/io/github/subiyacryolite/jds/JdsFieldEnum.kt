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

import javafx.beans.property.SimpleObjectProperty
import java.io.Externalizable
import java.io.IOException
import java.io.ObjectInput
import java.io.ObjectOutput
import java.util.*

/**
 * Represents a field enum in JDS
 */
class JdsFieldEnum<T : Enum<T>>() : Externalizable {
    private val _field = SimpleObjectProperty<JdsField>()
    private var enumType: Class<T>? = null

    var field: JdsField
        get() = _field.get()
        set(value) = _field.set(value)

    var sequenceValues = arrayOfNulls<Enum<T>>(0)
        private set//keep order at all times

    constructor(type: Class<T>) : this() {
        this.enumType = type
    }

    constructor(type: Class<T>, jdsField: JdsField, vararg values: T) : this(type) {
        field=jdsField
        sequenceValues = arrayOfNulls(values.size)
        System.arraycopy(values, 0, sequenceValues, 0, values.size)
        bind()
    }

    fun getEnumType(): Class<out Enum<*>>? {
        return enumType
    }

    private fun bind() {
        if (!fieldEnums.containsKey(_field.get().id))
            fieldEnums.put(_field.get().id, this)
    }

    override fun toString(): String {
        return "JdsFieldEnum{" +
                "field=" + _field.get() +
                ", sequenceValues=" + sequenceValues +
                '}'
    }

    fun indexOf(enumText: Enum<*>): Int {
        for (i in sequenceValues.indices) {
            if (sequenceValues[i] === enumText)
                return i
        }
        return -1
    }

    fun valueOf(index: Int): Enum<*>? {
        return if (index >= sequenceValues.size) null else sequenceValues[index]
    }

    @Throws(IOException::class)
    override fun writeExternal(out: ObjectOutput) {
        out.writeObject(enumType)
        out.writeObject(_field.get())
        out.writeObject(sequenceValues)
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    override fun readExternal(input: ObjectInput) {
        enumType = input.readObject() as Class<T>
        _field.set(input.readObject() as JdsField)
        sequenceValues = input.readObject() as Array<Enum<T>?>
    }

    companion object {

        private val fieldEnums: HashMap<Long, JdsFieldEnum<*>> = HashMap<Long, JdsFieldEnum<*>>()

        operator fun get(jdsField: JdsField): JdsFieldEnum<*>? {
            return if (fieldEnums.containsKey(jdsField.id))
                fieldEnums[jdsField.id]
            else
                throw RuntimeException(String.format("This jdsField [%s] has not been bound to any enums", jdsField))
        }

        operator fun get(fieldId: Long): JdsFieldEnum<*>? {
            return if (fieldEnums.containsKey(fieldId))
                fieldEnums[fieldId]
            else
                throw RuntimeException(String.format("This field [%s] has not been bound to any enums", fieldId))
        }
    }
}
