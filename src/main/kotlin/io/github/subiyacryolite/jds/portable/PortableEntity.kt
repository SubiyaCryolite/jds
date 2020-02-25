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
package io.github.subiyacryolite.jds.portable

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.subiyacryolite.jds.Entity
import io.github.subiyacryolite.jds.IEntity
import io.github.subiyacryolite.jds.context.DbContext

/**
 * Class used to represent [JdsEntity][Entity] objects in a portable manner
 */
data class PortableEntity(
        @get:JsonProperty("o")
        @set:JsonProperty("o")
        var overview: EntityOverview = EntityOverview(),

        @get:JsonProperty("f")
        @set:JsonProperty("f")
        var fieldId: Int? = null,

        @get:JsonProperty("blv")
        val blobValues: MutableCollection<StoreBlob> = ArrayList(),

        @get:JsonProperty("bv")
        val booleanValues: MutableCollection<StoreBoolean> = ArrayList(),

        @get:JsonProperty("dte")
        val dateValues: MutableCollection<StoreDate> = ArrayList(),

        @get:JsonProperty("md")
        val monthDayValues: MutableCollection<StoreMonthDay> = ArrayList(),

        @get:JsonProperty("ym")
        val yearMonthValues: MutableCollection<StoreYearMonth> = ArrayList(),

        @get:JsonProperty("dv")
        val doubleValues: MutableCollection<StoreDouble> = ArrayList(),

        @get:JsonProperty("stc")
        val shortValues: MutableCollection<StoreShort> = ArrayList(),

        @get:JsonProperty("su")
        val uuidValues: MutableCollection<StoreUuid> = ArrayList(),

        @get:JsonProperty("dc")
        val doubleCollections: MutableCollection<StoreDoubleCollection> = ArrayList(),

        @get:JsonProperty("iv")
        val integerValues: MutableCollection<StoreInteger> = ArrayList(),

        @get:JsonProperty("ic")
        val integerCollections: MutableCollection<StoreIntegerCollection> = ArrayList(),

        @get:JsonProperty("uc")
        val uuidCollections: MutableCollection<StoreUuidCollection> = ArrayList(),

        @get:JsonProperty("lv")
        val longValues: MutableCollection<StoreLong> = ArrayList(),

        @get:JsonProperty("lc")
        val longCollections: MutableCollection<StoreLongCollection> = ArrayList(),

        @get:JsonProperty("sv")
        val stringValues: MutableCollection<StoreString> = ArrayList(),

        @get:JsonProperty("sc")
        val stringCollections: MutableCollection<StoreStringCollection> = ArrayList(),

        @get:JsonProperty("fv")
        val floatValue: MutableCollection<StoreFloat> = ArrayList(),

        @get:JsonProperty("fc")
        val floatCollections: MutableCollection<StoreFloatCollection> = ArrayList(),

        @get:JsonProperty("dtv")
        val dateTimeValues: MutableCollection<StoreDateTime> = ArrayList(),

        @get:JsonProperty("dtc")
        val dateTimeCollection: MutableCollection<StoreDateTimeCollection> = ArrayList(),

        @get:JsonProperty("zdt")
        val zonedDateTimeValues: MutableCollection<StoreZonedDateTime> = ArrayList(),

        @get:JsonProperty("tv")
        val timeValues: MutableCollection<StoreTime> = ArrayList(),

        @get:JsonProperty("du")
        val durationValues: MutableCollection<StoreDuration> = ArrayList(),

        @get:JsonProperty("pv")
        val periodValues: MutableCollection<StorePeriod> = ArrayList(),

        @get:JsonProperty("ev")
        val enumValues: MutableCollection<StoreEnum> = ArrayList(),

        @get:JsonProperty("es")
        val enumStringValues: MutableCollection<StoreEnumString> = ArrayList(),

        @get:JsonProperty("ec")
        val enumCollections: MutableCollection<StoreEnumCollection> = ArrayList(),

        @get:JsonProperty("esc")
        val enumStringCollections: MutableCollection<StoreEnumStringCollection> = ArrayList(),

        @get:JsonProperty("eo")
        val entityOverviews: MutableCollection<PortableEntity> = ArrayList()
) {

    @Throws(Exception::class)
    fun init(dbContext: DbContext, entity: IEntity) {
        entity.assign(dbContext, this)
        overview = EntityOverview(
                entity.overview.id,
                entity.overview.editVersion,
                entity.overview.entityId,
                fieldId)
    }
}