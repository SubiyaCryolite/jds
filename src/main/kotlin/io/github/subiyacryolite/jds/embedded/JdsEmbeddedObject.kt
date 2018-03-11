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
class JdsEmbeddedObject {

    lateinit var overview: JdsEntityOverview

    var fieldId: Long? = null

    val blv: MutableList<JdsStoreBlob> = ArrayList()

    val bv: MutableList<JdsStoreBoolean> = ArrayList()

    val dte: MutableList<JdsStoreDate> = ArrayList()

    val md: MutableList<JdsStoreMonthDay> = ArrayList()

    val ym: MutableList<JdsStoreYearMonth> = ArrayList()

    val dv: MutableList<JdsStoreDouble> = ArrayList()

    val dc: MutableList<JdsStoreDoubleCollection> = ArrayList()

    val iv: MutableList<JdsStoreInteger> = ArrayList()

    val ic: MutableList<JdsStoreIntegerCollection> = ArrayList()

    val lv: MutableList<JdsStoreLong> = ArrayList()

    val lc: MutableList<JdsStoreLongCollection> = ArrayList()

    val sv: MutableList<JdsStoreString> = ArrayList()

    val sc: MutableList<JdsStoreStringCollection> = ArrayList()

    val fv: MutableList<JdsStoreFloat> = ArrayList()

    val fc: MutableList<JdsStoreFloatCollection> = ArrayList()

    val dtv: MutableList<JdsStoreDateTime> = ArrayList()

    val dtc: MutableList<JdsStoreDateTimeCollection> = ArrayList()

    val zdt: MutableList<JdsStoreZonedDateTime> = ArrayList()

    val tv: MutableList<JdsStoreTime> = ArrayList()

    val du: MutableList<JdsStoreDuration> = ArrayList()

    val pv: MutableList<JdsStorePeriod> = ArrayList()

    val ev: MutableList<JdsStoreEnum> = ArrayList()

    val ec: MutableList<JdsStoreEnumCollection> = ArrayList()

    val eo: MutableList<JdsEmbeddedObject> = ArrayList()

    fun init(entity: JdsEntity) {
        entity.assign(this)
        overview = JdsEntityOverview(
                entity.overview.uuid,
                entity.overview.editVersion,
                entity.overview.entityId,
                fieldId,
                entity.overview.entityVersion)
    }
}