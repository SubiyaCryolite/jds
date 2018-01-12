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

//blobs
class JdsBlobValues(val id: Long, val v: ByteArray?)
//primitives
class JdsBooleanValues(val k: Long, val v: Int?)
class JdsDoubleValues(val k: Long, val v: Double?)
class JdsIntegerValues(val k: Long, val v: Int?)
class JdsLongValues(val k: Long, val v: Long?)
class JdsStringValues(val k: Long, val v: String?)
class JdsFloatValues(val k: Long, val v: Float?)
//time constructs
class JdsLocalDateTimeValues(val k: Long, val v: Timestamp?)
class JdsTimeValues(val k: Long, val v: Long?)
class JdsZonedDateTimeValues(val k: Long, val v: Long?)
class JdsLocalDateValues(val k: Long, val v: Timestamp?)
class JdsDurationValues(val k: Long, val v: Long?)
class JdsMonthDayValues(val k: Long, val v: String?)
class JdsYearMonthValues(val k: Long, val v: String?)
class JdsPeriodValues(val k: Long, val v: String?)
//enums
class JdsEnumValues(val k: Long, val v: Int?)
//arrays
class JdsDateCollections(val k: Long, val s: Int, val v: Timestamp?)
class JdsDoubleCollections(val k: Long, val s: Int, val v: Double?)
class JdsIntegerCollections(val k: Long, val s: Int, val v: Int?)
class JdsLongCollections(val k: Long, val s: Int, val v: Long?)
class JdsTextCollections(val k: Long, val s: Int, val v: String?)
class JdsFloatCollections(val k: Long, val s: Int, val v: Float?)
class JdsEnumCollections(val k: Long, val s: Int, val v: Int?)
//overviews
class JdsEntityOverview(val uuid: String, val id: Long, val l: Boolean, val v: Long, val dc: LocalDateTime, val dm: LocalDateTime)
class JdsEntityBinding(val p: String, val c: String, val f: Long, val i: Long)

/**
 * @param entities
 */
class JdsEmbeddedContainer(entities: Iterable<JdsEntity>) {
    /**
     * Embedded objects
     */
    val e: MutableList<JdsEmbeddedObject> = ArrayList()
    init {
        entities.forEach {
            e.add(JdsEmbeddedObject(it))
        }
    }
}