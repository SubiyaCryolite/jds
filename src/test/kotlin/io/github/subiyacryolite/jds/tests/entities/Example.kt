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
import io.github.subiyacryolite.jds.interfaces.IValue
import io.github.subiyacryolite.jds.tests.constants.Fields
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime

@EntityAnnotation(id = 6, name = "TypeClass")
data class Example(
        private val _stringField: IValue<String> = StringValue(),
        private val _timeField: IValue<LocalTime> = LocalTimeValue(),
        private val _dateField: IValue<LocalDate> = LocalDateValue(),
        private val _dateTimeField: IValue<LocalDateTime> = LocalDateTimeValue(),
        private val _zonedDateTimeField: IValue<ZonedDateTime> = ZonedDateTimeValue(),
        private val _longField: IValue<Long> = LongValue(),
        private val _intField: IValue<Int?> = NullableIntegerValue(),
        private val _doubleField: IValue<Double> = DoubleValue(),
        private val _floatField: IValue<Float> = FloatValue(),
        private val _booleanField: IValue<Boolean> = BooleanValue(),
        private val _blobField: IValue<ByteArray> = BlobValue(byteArrayOf(0, 1, 1, 1, 1, 0))
) : Entity() {

    override fun bind() {
        super.bind()
        map(Fields.String, _stringField)
        map(Fields.Time, _timeField)
        map(Fields.Date, _dateField)
        map(Fields.DateTime, _dateTimeField)
        map(Fields.ZonedDateTime, _zonedDateTimeField)
        map(Fields.Long, _longField)
        map(Fields.Int, _intField)
        map(Fields.Double, _doubleField)
        map(Fields.Float, _floatField)
        map(Fields.Boolean, _booleanField)
        map(Fields.Blob, _blobField)
    }

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
}