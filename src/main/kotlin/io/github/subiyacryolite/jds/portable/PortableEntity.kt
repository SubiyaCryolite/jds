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
import io.github.subiyacryolite.jds.context.DbContext
import io.github.subiyacryolite.jds.interfaces.IEntity

/**
 * Class used to represent [JdsEntity][Entity] objects in a portable manner
 */
data class PortableEntity(
        @get:JsonProperty("a")
        @set:JsonProperty("a")
        var overview: EntityOverview = EntityOverview(),

        @get:JsonProperty("b")
        @set:JsonProperty("b")
        var fieldId: Int? = null,

        @get:JsonProperty("c")
        val blobValues: MutableCollection<StoreBlob> = ArrayList(),

        @get:JsonProperty("d")
        val booleanValues: MutableCollection<StoreBoolean> = ArrayList(),

        @get:JsonProperty("e")
        val dateValues: MutableCollection<StoreDate> = ArrayList(),

        @get:JsonProperty("f")
        val monthDayValues: MutableCollection<StoreMonthDay> = ArrayList(),

        @get:JsonProperty("g")
        val yearMonthValues: MutableCollection<StoreYearMonth> = ArrayList(),

        @get:JsonProperty("h")
        val doubleValues: MutableCollection<StoreDouble> = ArrayList(),

        @get:JsonProperty("i")
        val shortValues: MutableCollection<StoreShort> = ArrayList(),

        @get:JsonProperty("j")
        val uuidValues: MutableCollection<StoreUuid> = ArrayList(),

        @get:JsonProperty("k")
        val doubleCollections: MutableCollection<StoreDoubleCollection> = ArrayList(),

        @get:JsonProperty("l")
        val integerValues: MutableCollection<StoreInteger> = ArrayList(),

        @get:JsonProperty("m")
        val integerCollections: MutableCollection<StoreIntegerCollection> = ArrayList(),

        @get:JsonProperty("n")
        val shortCollections: MutableCollection<StoreShortCollection> = ArrayList(),

        @get:JsonProperty("o")
        val uuidCollections: MutableCollection<StoreUuidCollection> = ArrayList(),

        @get:JsonProperty("p")
        val longValues: MutableCollection<StoreLong> = ArrayList(),

        @get:JsonProperty("q")
        val longCollections: MutableCollection<StoreLongCollection> = ArrayList(),

        @get:JsonProperty("r")
        val stringValues: MutableCollection<StoreString> = ArrayList(),

        @get:JsonProperty("s")
        val stringCollections: MutableCollection<StoreStringCollection> = ArrayList(),

        @get:JsonProperty("t")
        val floatValue: MutableCollection<StoreFloat> = ArrayList(),

        @get:JsonProperty("u")
        val floatCollections: MutableCollection<StoreFloatCollection> = ArrayList(),

        @get:JsonProperty("v")
        val dateTimeValues: MutableCollection<StoreDateTime> = ArrayList(),

        @get:JsonProperty("w")
        val dateTimeCollection: MutableCollection<StoreDateTimeCollection> = ArrayList(),

        @get:JsonProperty("x")
        val zonedDateTimeValues: MutableCollection<StoreZonedDateTime> = ArrayList(),

        @get:JsonProperty("y")
        val timeValues: MutableCollection<StoreTime> = ArrayList(),

        @get:JsonProperty("z")
        val durationValues: MutableCollection<StoreDuration> = ArrayList(),

        @get:JsonProperty("a1")
        val periodValues: MutableCollection<StorePeriod> = ArrayList(),

        @get:JsonProperty("b1")
        val enumValues: MutableCollection<StoreEnum> = ArrayList(),

        @get:JsonProperty("c1")
        val enumStringValues: MutableCollection<StoreEnumString> = ArrayList(),

        @get:JsonProperty("d1")
        val enumCollections: MutableCollection<StoreEnumCollection> = ArrayList(),

        @get:JsonProperty("e1")
        val enumStringCollections: MutableCollection<StoreEnumStringCollection> = ArrayList(),

        @get:JsonProperty("f1")
        val entityOverviews: MutableCollection<PortableEntity> = ArrayList(),

        @get:JsonProperty("g1")
        val mapIntKeyValues: MutableCollection<StoreMapIntKey> = ArrayList(),

        @get:JsonProperty("h1")
        val mapStringKeyValues: MutableCollection<StoreMapStringKey> = ArrayList(),

        @get:JsonProperty("i1")
        val mapOfCollectionsValues: MutableCollection<StoreMapCollection> = ArrayList(),
) {

    @Throws(Exception::class)
    fun init(dbContext: DbContext, entity: IEntity) {
        entity.bind()
        if (entity is Entity) {
            Entity.assign(entity, dbContext, this)
        }
        overview = EntityOverview(entity.overview, fieldId)
    }
}