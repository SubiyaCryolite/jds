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
package javafx.beans.property

import javafx.beans.value.WritableValue
import java.io.Serializable

/**
 * Created by ifunga on 18/06/2017.
 * This class was designed to store binary values in a backing byte array. The byte array can also be read as an input stream
 */
open class BlobProperty : WritableValue<ByteArray?>,Serializable {

    private var bytes: ByteArray? = null

    constructor():this(null)

    /**
     * Constructor
     *
     * @param value byte array input
     */
    constructor(value: ByteArray?) {
        set(value)
    }

    /**
     * Acquire the blob as an array of bytes
     *
     * @return the blob as an array of bytes
     */
    fun get(): ByteArray? {
        return bytes
    }

    /**
     * Set the blob
     *
     * @param bytes the blob as an array of bytes
     */
    fun set(bytes: ByteArray?) {
        this.bytes = bytes
    }

    /**
     * Determine if the blob is empty
     *
     * @return true if the blob is empty
     */
    val isEmpty: Boolean
        get() = bytes == null || bytes!!.isEmpty()

    override fun setValue(value: ByteArray?) {
        set(value)
    }

    override fun getValue(): ByteArray? {
        return get()
    }

    companion object {
        private const val serialVersionUID = 20171109_0853L
    }
}