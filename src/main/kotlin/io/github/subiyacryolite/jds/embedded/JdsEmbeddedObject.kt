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

/**
 * Class used to represent [JdsEntity] objects in a portable manner
 * @param entity
 */
class JdsEmbeddedObject(entity: JdsEntity, fieldId: Long?) {

    /**
     * Used to store Byte[] values in a portable manner
     */
    val bl: MutableList<JdsBlobValues> = ArrayList()

    /**
     * Used to store [java.lang.Boolean] values in a portable manner
     */
    val b: MutableList<JdsBooleanValues> = ArrayList()

    /**
     * Used to store [java.time.LocalDate] and [java.time.LocalDateTime] values in a portable manner based on [java.sql.Timestamp]s
     */
    val ldt: MutableList<JdsLocalDateTimeValues> = ArrayList()

    /**
     * Used to store [java.lang.Double] in a portable manner
     */
    val d: MutableList<JdsDoubleValues> = ArrayList()

    /**
     *  Used to store [java.lang.Integer] and [java.lang.Enum] values in a portable manner
     */
    val i: MutableList<JdsIntegerEnumValues> = ArrayList()

    /**
     *  Class used to store long, [java.time.ZonedDateTime],[java.time.LocalTime] and [java.time.Duration] values in a portable manner
     */
    val l: MutableList<JdsLongValues> = ArrayList()

    /**
     * Used to store [java.lang.String], [java.time.YearMonth],[java.time.MonthDay] and [java.time.Period] values in a portable manner
     */
    val s: MutableList<JdsStringValues> = ArrayList()

    /**
     * Used to store [java.lang.Float] values in a portable manner
     */
    val f: MutableList<JdsFloatValues> = ArrayList()

    /**
     * Embedded objects
     */
    val eo: MutableList<JdsEmbeddedObject> = ArrayList()

    /**
     * Object overview
     */
    val o: JdsEntityOverview = JdsEntityOverview(entity.overview.compositeKey, entity.overview.uuid, entity.overview.uuidLocation, entity.overview.uuidLocationVersion, entity.overview.entityId, fieldId, entity.overview.live, entity.overview.version)

    init {
        entity.assign(this);
    }
}