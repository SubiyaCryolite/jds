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
package io.github.subiyacryolite.jds.embedded

import io.github.subiyacryolite.jds.JdsEntity
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.MonthDay
import java.time.Period
import java.time.YearMonth

//blobs
class JdsBlobValues(val id: Long, val v: ByteArray?)
//primitives
class JdsBooleanValues(val id: Long, val v: Int?)
class JdsDoubleValues(val id: Long, val v: Double?)
class JdsIntegerValues(val id: Long, val v: Int?)
class JdsLongValues(val id: Long, val v: Long?)
class JdsTextValues(val id: Long, val v: String?)
class JdsFloatValues(val id: Long, val v: Float?)
//time constructs
class JdsLocalDateTimeValues(val id: Long, val v: Timestamp?)
class JdsTimeValues(val id: Long, val v: Int?)
class JdsZonedDateTimeValues(val id: Long, val v: Long?)
class JdsLocalDateValues(val id: Long, val v: Timestamp?)
class JdsDurationValues(val id: Long, val v: Long?)
class JdsMonthDayValues(val id: Long, val v: String?)
class JdsYearMonthValues(val id: Long, val v: String?)
class JdsPeriodValues(val id: Long, val v: String?)

//enums
class JdsEnumValues(val i: Long, val v: Int?)
//arrays
class JdsDateCollections(val i: Long, val s: Int, val v: Timestamp?)
class JdsDoubleCollections(val i: Long, val s: Int, val v: Double?)
class JdsIntegerCollections(val i: Long, val s: Int, val v: Int?)
class JdsLongCollections(val i: Long, val s: Int, val v: Long?)
class JdsTextCollections(val i: Long, val s: Int, val v: String?)
class JdsFloatCollections(val i: Long, val s: Int, val v: Float?)
class JdsEnumCollections(val i: Long, val s: Int, val v: Int?)

class JdsEntityOverview(val uuid: String, val id: Long, val l: Boolean, val v: Long, val dc: LocalDateTime, val dm: LocalDateTime)
class JdsEntityBinding(val p: String, val c: String, val f: Long, val i: Long)

class JdsEmbeddedContainer(sources: Collection<JdsEntity>) {
    /**
     * Embedded objects
     */
    val e: MutableList<JdsEmbeddedObject> = ArrayList()
    init {
        sources.forEach {
            e.add(JdsEmbeddedObject(it))
        }
    }
}