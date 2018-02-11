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
import java.time.LocalDateTime
import java.util.*

/**
 * A self contains a set of properties that form the overview of a
 * [JdsEntity]. Instances of this class are exposed via inheritance
 */
class JdsOverview : IJdsOverview, Externalizable {

    override var entityId: Long = 0
    override var uuid: String = UUID.randomUUID().toString()
    override var uuidLocation: String = ""
    override var uuidLocationVersion: Int = 0
    override var parentUuid: String? = null
    override var parentCompositeKey: String? = null
    override var entityVersion: Long = 1L
    override var live: Boolean = false
    override var lastEdit: LocalDateTime = LocalDateTime.now()
    override val compositeKey: String
        get() {
            val stringBuilder = StringBuilder()
            stringBuilder.append(uuid)
            if (uuidLocation.isNotBlank()) {
                stringBuilder.append('.')
                stringBuilder.append(uuidLocation)
            }
            stringBuilder.append('.')
            stringBuilder.append(uuidLocationVersion)
            return stringBuilder.toString()
        }

    @Throws(IOException::class)
    override fun writeExternal(objectOutputStream: ObjectOutput) {
        objectOutputStream.writeUTF(uuid)
        objectOutputStream.writeUTF(uuidLocation)
        objectOutputStream.writeInt(uuidLocationVersion)
        objectOutputStream.writeUTF(parentUuid)
        objectOutputStream.writeUTF(parentCompositeKey)
        objectOutputStream.writeLong(entityId)
        objectOutputStream.writeBoolean(live)
        objectOutputStream.writeLong(entityVersion)
        objectOutputStream.writeObject(lastEdit)
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    override fun readExternal(objectInputStream: ObjectInput) {
        uuid = objectInputStream.readUTF()
        uuidLocation = objectInputStream.readUTF()
        uuidLocationVersion = objectInputStream.readInt()
        parentUuid = objectInputStream.readUTF()
        parentCompositeKey = objectInputStream.readUTF()
        entityId = objectInputStream.readLong()
        live = objectInputStream.readBoolean()
        entityVersion = objectInputStream.readLong()
        lastEdit = objectInputStream.readObject() as LocalDateTime
    }

    override fun toString(): String {
        return "{ uuid = $uuid, entityId = $entityId, version = $entityVersion, live = $live }"
    }

    companion object {
        private val serialVersionUID = 20171109_0853L
    }
}
