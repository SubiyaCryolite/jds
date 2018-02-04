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
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation
import java.sql.Timestamp

/**
 * Class used to store blob values in a portable manner
 */
class JdsBlobValues(val id: Long, val v: ByteArray?)

/**
 * Class used to store [java.lang.Boolean] values in a portable manner
 */
class JdsBooleanValues(val k: Long, val v: Int?)

/**
 * Class used to store [java.lang.Double] in a portable manner
 */
class JdsDoubleValues(val k: Long, val v: Double?)

/**
 * Class used to store [java.lang.Integer] and [java.lang.Enum] values in a portable manner
 */
class JdsIntegerEnumValues(val k: Long, val v: Int?)

/**
 * Class used to store [java.lang.Long], [java.time.ZonedDateTime],[java.time.LocalTime] and [java.time.Duration] values in a portable manner
 */
class JdsLongValues(val k: Long, val v: Long?)

/**
 * Class used to store [java.lang.String], [java.time.YearMonth],[java.time.MonthDay] and [java.time.Period] values in a portable manner
 */
class JdsStringValues(val k: Long, val v: String?)

/**
 * Class used to store [java.lang.Float] values in a portable manner
 */
class JdsFloatValues(val k: Long, val v: Float?)

/**
 * Class used to store [java.time.LocalDate] and [java.time.LocalDateTime] values in a portable manner based on [java.sql.Timestamp]s
 */
class JdsLocalDateTimeValues(val k: Long, val v: Timestamp?)

/**
 *
 * @param compositeKey composite key
 * @param uuid uuid
 * @param uuidLocation uuid location
 * @param uuidLocationVersion uuid location version
 * @param entityId entity id
 * @param fieldId field id
 * @param live live
 * @param version version
 */
class JdsEntityOverview(val compositeKey: String, val uuid: String, val uuidLocation: String, val uuidLocationVersion: Int, val entityId: Long, val fieldId: Long?, val live: Boolean, val version: Long)

/**
 * @param entities a collection of [JdsEntity] objects to store in a portable manner
 */
class JdsEmbeddedContainer(entities: Iterable<JdsEntity>) {
    /**
     * Embedded objects
     */
    val e: MutableList<JdsEmbeddedObject> = ArrayList()

    init {
        entities.forEach {
            if (it.javaClass.isAnnotationPresent(JdsEntityAnnotation::class.java)) {
                e.add(JdsEmbeddedObject(it, null))
            } else {
                throw RuntimeException("You must annotate the class [" + it.javaClass.canonicalName + "] with [" + JdsEntityAnnotation::class.java + "]")
            }
        }
    }
}