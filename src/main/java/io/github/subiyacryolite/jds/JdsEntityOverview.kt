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

import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import java.io.Externalizable
import java.io.IOException
import java.io.ObjectInput
import java.io.ObjectOutput
import java.time.LocalDateTime
import java.util.*

/**
 * A self contains a set of properties that form the overview of a
 * [JdsEntity]. Instances of this class are initialised in
 * [JdsEntityBase] and exposed via inheritance
 */
class JdsEntityOverview : IJdsEntityOverview, Externalizable {

    private var _dateCreated: SimpleObjectProperty<LocalDateTime> = SimpleObjectProperty(LocalDateTime.now())
    private var _dateModified: SimpleObjectProperty<LocalDateTime> = SimpleObjectProperty(LocalDateTime.now())
    private var _entityId: SimpleLongProperty = SimpleLongProperty(0)
    private var _entityGuid: SimpleStringProperty = SimpleStringProperty(UUID.randomUUID().toString())

    override var dateCreated: LocalDateTime
        get() = this._dateCreated.get()
        set(value) = this._dateCreated.set(value)

    override var dateModified: LocalDateTime
        get() = this._dateModified.get()
        set(value) = this._dateModified.set(value)

    override var entityId: Long
        get() = this._entityId.get()
        set(value) = this._entityId.set(value)

    override var entityGuid: String
        get() = this._entityGuid.get()
        set(value) = this._entityGuid.set(value)

    @Throws(IOException::class)
    override fun writeExternal(objectOutputStream: ObjectOutput) {
        objectOutputStream.writeUTF(entityGuid)
        objectOutputStream.writeLong(entityId)
        objectOutputStream.writeObject(dateCreated)
        objectOutputStream.writeObject(dateModified)
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    override fun readExternal(objectInputStream: ObjectInput) {
        entityGuid = objectInputStream.readUTF()
        entityId = objectInputStream.readLong()
        dateCreated = objectInputStream.readObject() as LocalDateTime
        dateModified = objectInputStream.readObject() as LocalDateTime
    }
}
