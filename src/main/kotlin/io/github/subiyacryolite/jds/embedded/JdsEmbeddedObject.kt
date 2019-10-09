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

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.subiyacryolite.jds.JdsEntity

/**
 * Class used to represent [JdsEntity][JdsEntity] objects in a portable manner
 */
data class JdsEmbeddedObject(@get:JsonProperty("o")
                             @set:JsonProperty("o")
                             var overview: JdsEntityOverview = JdsEntityOverview(),

                             @get:JsonProperty("f")
                             @set:JsonProperty("f")
                             var fieldId: Long? = null,

                             @get:JsonProperty("blv")
                             val blobValues: MutableList<JdsStoreBlob> = ArrayList(),

                             @get:JsonProperty("bv")
                             val booleanValues: MutableList<JdsStoreBoolean> = ArrayList(),

                             @get:JsonProperty("dte")
                             val dateValues: MutableList<JdsStoreDate> = ArrayList(),

                             @get:JsonProperty("md")
                             val monthDayValues: MutableList<JdsStoreMonthDay> = ArrayList(),

                             @get:JsonProperty("ym")
                             val yearMonthValues: MutableList<JdsStoreYearMonth> = ArrayList(),

                             @get:JsonProperty("dv")
                             val doubleValues: MutableList<JdsStoreDouble> = ArrayList(),

                             @get:JsonProperty("stc")
                             val shortValues: MutableList<JdsStoreShort> = ArrayList(),

                             @get:JsonProperty("su")
                             val uuidValues: MutableList<JdsStoreUuid> = ArrayList(),

                             @get:JsonProperty("dc")
                             val doubleCollections: MutableList<JdsStoreDoubleCollection> = ArrayList(),

                             @get:JsonProperty("iv")
                             val integerValues: MutableList<JdsStoreInteger> = ArrayList(),

                             @get:JsonProperty("ic")
                             val integerCollections: MutableList<JdsStoreIntegerCollection> = ArrayList(),

                             @get:JsonProperty("lv")
                             val longValues: MutableList<JdsStoreLong> = ArrayList(),

                             @get:JsonProperty("lc")
                             val longCollections: MutableList<JdsStoreLongCollection> = ArrayList(),

                             @get:JsonProperty("sv")
                             val stringValues: MutableList<JdsStoreString> = ArrayList(),

                             @get:JsonProperty("sc")
                             val stringCollections: MutableList<JdsStoreStringCollection> = ArrayList(),

                             @get:JsonProperty("fv")
                             val floatValue: MutableList<JdsStoreFloat> = ArrayList(),

                             @get:JsonProperty("fc")
                             val floatCollections: MutableList<JdsStoreFloatCollection> = ArrayList(),

                             @get:JsonProperty("dtv")
                             val dateTimeValues: MutableList<JdsStoreDateTime> = ArrayList(),

                             @get:JsonProperty("dtc")
                             val dateTimeCollection: MutableList<JdsStoreDateTimeCollection> = ArrayList(),

                             @get:JsonProperty("zdt")
                             val zonedDateTimeValues: MutableList<JdsStoreZonedDateTime> = ArrayList(),

                             @get:JsonProperty("tv")
                             val timeValues: MutableList<JdsStoreTime> = ArrayList(),

                             @get:JsonProperty("du")
                             val durationValues: MutableList<JdsStoreDuration> = ArrayList(),

                             @get:JsonProperty("pv")
                             val periodValues: MutableList<JdsStorePeriod> = ArrayList(),

                             @get:JsonProperty("ev")
                             val enumValues: MutableList<JdsStoreEnum> = ArrayList(),

                             @get:JsonProperty("es")
                             val enumStringValues: MutableList<JdsStoreEnumString> = ArrayList(),

                             @get:JsonProperty("ec")
                             val enumCollections: MutableList<JdsStoreEnumCollection> = ArrayList(),

                             @get:JsonProperty("esc")
                             val enumStringCollections: MutableList<JdsStoreEnumStringCollection> = ArrayList(),

                             @get:JsonProperty("eo")
                             val entityOverviews: MutableList<JdsEmbeddedObject> = ArrayList()) {

    @Throws(Exception::class)
    fun init(entity: JdsEntity) {
        entity.assign(this)
        overview = JdsEntityOverview(
                entity.overview.uuid,
                entity.overview.editVersion,
                entity.overview.entityId,
                fieldId)
    }
}