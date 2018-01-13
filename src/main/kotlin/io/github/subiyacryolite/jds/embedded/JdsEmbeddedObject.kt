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
 *
 * @param entity
 */
class JdsEmbeddedObject(entity: JdsEntity) {
    /**
     * Blob Values
     */
    val bl: MutableList<JdsBlobValues> = ArrayList()
    /**
     * Boolean values
     */
    val b: MutableList<JdsBooleanValues> = ArrayList()
    /**
     * Local Date Time Values
     */
    val ldt: MutableList<JdsLocalDateTimeValues> = ArrayList()
    /**
     * Double values
     */
    val d: MutableList<JdsDoubleValues> = ArrayList()
    /**
     * Integer values
     */
    val i: MutableList<JdsIntegerValues> = ArrayList()
    /**
     * Long values
     */
    val l: MutableList<JdsLongValues> = ArrayList()
    /**
     * String values
     */
    val s: MutableList<JdsStringValues> = ArrayList()
    /**
     * Float values
     */
    val f: MutableList<JdsFloatValues> = ArrayList()
    /**
     * Local Time values
     */
    val t: MutableList<JdsTimeValues> = ArrayList()
    /**
     * Month day values
     */
    val mdv: MutableList<JdsMonthDayValues> = ArrayList()
    /**
     * Zoned Date Time values
     */
    val zdt: MutableList<JdsZonedDateTimeValues> = ArrayList()
    /**
     * Local Date values
     */
    val ld: MutableList<JdsLocalDateValues> = ArrayList()
    /**
     * Date-Time collection values
     */
    val dta: MutableList<JdsDateCollections> = ArrayList()
    /**
     * Double collection values
     */
    val da: MutableList<JdsDoubleCollections> = ArrayList()
    /**
     * Integer collection values
     */
    val ia: MutableList<JdsIntegerCollections> = ArrayList()
    /**
     * Long collection tavles
     */
    val la: MutableList<JdsLongCollections> = ArrayList()
    /**
     * String collection values
     */
    val sa: MutableList<JdsTextCollections> = ArrayList()
    /**
     * Float collection values
     */
    val fa: MutableList<JdsFloatCollections> = ArrayList()
    /**
     * Enum values
     */
    val e: MutableList<JdsEnumValues> = ArrayList()
    /**
     * Enum collection values
     */
    val ea: MutableList<JdsEnumCollections> = ArrayList()
    /**
     * Duration values
     */
    val du: MutableList<JdsDurationValues> = ArrayList()
    /**
     * Month Day values
     */
    val md: MutableList<JdsMonthDayValues> = ArrayList()
    /**
     * Year Month values
     */
    val ym: MutableList<JdsYearMonthValues> = ArrayList()
    /**
     * Period values
     */
    val p: MutableList<JdsPeriodValues> = ArrayList()
    /**
     * Entity bindings [parent to child]
     */
    val eb: MutableList<JdsEntityBinding> = ArrayList()
    /**
     * Embedded objects
     */
    val eo: MutableList<JdsEmbeddedObject> = ArrayList()
    /**
     * Object overview
     */
    val o: jds_entity_overview = jds_entity_overview(entity.overview.uuid, entity.overview.entityId, entity.overview.live, entity.overview.version, entity.overview.dateCreated, entity.overview.dateModified)

    init {
        entity.assign(this);
    }
}