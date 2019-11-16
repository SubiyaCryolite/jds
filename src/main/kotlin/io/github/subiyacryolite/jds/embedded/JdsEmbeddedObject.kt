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
import io.github.subiyacryolite.jds.IJdsEntity
import io.github.subiyacryolite.jds.JdsDb
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
                             val blobValues: MutableCollection<JdsStoreBlob> = ArrayList(),

                             @get:JsonProperty("bv")
                             val booleanValues: MutableCollection<JdsStoreBoolean> = ArrayList(),

                             @get:JsonProperty("dte")
                             val dateValues: MutableCollection<JdsStoreDate> = ArrayList(),

                             @get:JsonProperty("md")
                             val monthDayValues: MutableCollection<JdsStoreMonthDay> = ArrayList(),

                             @get:JsonProperty("ym")
                             val yearMonthValues: MutableCollection<JdsStoreYearMonth> = ArrayList(),

                             @get:JsonProperty("dv")
                             val doubleValues: MutableCollection<JdsStoreDouble> = ArrayList(),

                             @get:JsonProperty("stc")
                             val shortValues: MutableCollection<JdsStoreShort> = ArrayList(),

                             @get:JsonProperty("su")
                             val uuidValues: MutableCollection<JdsStoreUuid> = ArrayList(),

                             @get:JsonProperty("dc")
                             val doubleCollections: MutableCollection<JdsStoreDoubleCollection> = ArrayList(),

                             @get:JsonProperty("iv")
                             val integerValues: MutableCollection<JdsStoreInteger> = ArrayList(),

                             @get:JsonProperty("ic")
                             val integerCollections: MutableCollection<JdsStoreIntegerCollection> = ArrayList(),

                             @get:JsonProperty("lv")
                             val longValues: MutableCollection<JdsStoreLong> = ArrayList(),

                             @get:JsonProperty("lc")
                             val longCollections: MutableCollection<JdsStoreLongCollection> = ArrayList(),

                             @get:JsonProperty("sv")
                             val stringValues: MutableCollection<JdsStoreString> = ArrayList(),

                             @get:JsonProperty("sc")
                             val stringCollections: MutableCollection<JdsStoreStringCollection> = ArrayList(),

                             @get:JsonProperty("fv")
                             val floatValue: MutableCollection<JdsStoreFloat> = ArrayList(),

                             @get:JsonProperty("fc")
                             val floatCollections: MutableCollection<JdsStoreFloatCollection> = ArrayList(),

                             @get:JsonProperty("dtv")
                             val dateTimeValues: MutableCollection<JdsStoreDateTime> = ArrayList(),

                             @get:JsonProperty("dtc")
                             val dateTimeCollection: MutableCollection<JdsStoreDateTimeCollection> = ArrayList(),

                             @get:JsonProperty("zdt")
                             val zonedDateTimeValues: MutableCollection<JdsStoreZonedDateTime> = ArrayList(),

                             @get:JsonProperty("tv")
                             val timeValues: MutableCollection<JdsStoreTime> = ArrayList(),

                             @get:JsonProperty("du")
                             val durationValues: MutableCollection<JdsStoreDuration> = ArrayList(),

                             @get:JsonProperty("pv")
                             val periodValues: MutableCollection<JdsStorePeriod> = ArrayList(),

                             @get:JsonProperty("ev")
                             val enumValues: MutableCollection<JdsStoreEnum> = ArrayList(),

                             @get:JsonProperty("es")
                             val enumStringValues: MutableCollection<JdsStoreEnumString> = ArrayList(),

                             @get:JsonProperty("ec")
                             val enumCollections: MutableCollection<JdsStoreEnumCollection> = ArrayList(),

                             @get:JsonProperty("esc")
                             val enumStringCollections: MutableCollection<JdsStoreEnumStringCollection> = ArrayList(),

                             @get:JsonProperty("eo")
                             val entityOverviews: MutableCollection<JdsEmbeddedObject> = ArrayList()) {

    @Throws(Exception::class)
    fun init(jdsDb: JdsDb, entity: IJdsEntity) {
        entity.assign(jdsDb ,this)
        overview = JdsEntityOverview(
                entity.overview.uuid,
                entity.overview.editVersion,
                entity.overview.entityId,
                fieldId)
    }
}