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
package io.github.subiyacryolite.jds.tests.entities

import io.github.subiyacryolite.jds.Entity
import io.github.subiyacryolite.jds.annotations.EntityAnnotation
import io.github.subiyacryolite.jds.beans.property.*
import io.github.subiyacryolite.jds.events.EventArguments
import io.github.subiyacryolite.jds.events.LoadListener
import io.github.subiyacryolite.jds.events.SaveListener
import io.github.subiyacryolite.jds.tests.constants.Fields
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime

@EntityAnnotation(id = 3, name = "TypeClass")
class Example : Entity(), LoadListener, SaveListener {

    private val _stringField = map(Fields.String, "")
    private val _timeField = map(Fields.Time, LocalTime.now())
    private val _dateField = map(Fields.Date, LocalDate.now())
    private val _dateTimeField = map(Fields.DateTime, LocalDateTime.now())
    private val _zonedDateTimeField = map(Fields.ZonedDateTime, ZonedDateTime.now())
    private val _longField = map(Fields.Long, 0L)
    private val _intField = map(Fields.Int, IntegerProperty())
    private val _doubleField = map(Fields.Double, 0.0)
    private val _floatField = map(Fields.Float, 0f)
    private val _booleanField = map(Fields.Boolean, false)
    private val _blobField = map(Fields.Blob, ByteArray(0))

    var stringField: String
        get() = _stringField.get()
        set(stringField) = _stringField.set(stringField)

    var timeField: LocalTime
        get() = _timeField.get()
        set(dateField) = _timeField.set(dateField)

    var dateField: LocalDate
        get() = _dateField.get()
        set(dateField) = _dateField.set(dateField)

    var dateTimeField: LocalDateTime
        get() = _dateTimeField.get()
        set(dateTimeField) = _dateTimeField.set(dateTimeField)

    var zonedDateTimeField: ZonedDateTime
        get() = _zonedDateTimeField.get()
        set(zonedDateTimeField) = _zonedDateTimeField.set(zonedDateTimeField)

    var longField: Long
        get() = _longField.get()
        set(longField) = _longField.set(longField)

    var intField: Int?
        get() = _intField.get()
        set(intField) = _intField.set(intField)

    var doubleField: Double
        get() = _doubleField.get()
        set(doubleField) = _doubleField.set(doubleField)

    var floatField: Float
        get() = _floatField.get()
        set(floatField) = _floatField.set(floatField)

    var booleanField: Boolean
        get() = _booleanField.get()
        set(booleanField) = _booleanField.set(booleanField)

    var blobField: ByteArray
        get() = _blobField.get()
        set(booleanField) = _blobField.set(booleanField)

    override fun onPreSave(eventArguments: EventArguments) {
        //Optional event i.e write to custom reporting tables, perform custom validation
        //Queries can be batched i.e eventArguments.getOrAddStatement("Batched SQL to execute")
    }

    override fun onPostSave(eventArguments: EventArguments) {
        //Optional event i.e write to custom reporting tables, perform custom validation
        //Queries can be batched i.e eventArguments.getOrAddStatement("Batched SQL to execute")
    }

    override fun onPreLoad(eventArguments: EventArguments) {
        //Optional event i.e write to custom reporting tables, perform custom validation
        //Queries can be batched i.e eventArguments.getOrAddStatement("Batched SQL to execute")
    }

    override fun onPostLoad(eventArguments: EventArguments) {
        //Optional event i.e write to custom reporting tables, perform custom validation
        //Queries can be batched i.e eventArguments.getOrAddStatement("Batched SQL to execute")
    }

    override fun toString(): String {
        return "{" +
                ", overview = $overview" +
                ", stringField = $stringField" +
                ", dateField = $dateField" +
                ", timeField = $timeField" +
                ", dateTimeField = $dateTimeField" +
                ", zonedDateTimeField = $zonedDateTimeField" +
                ", longField = $longField" +
                ", intField = $intField" +
                ", doubleField = $doubleField" +
                ", floatField = $floatField" +
                ", blobField = $blobField" +
                ", booleanField = $booleanField}"
    }
}