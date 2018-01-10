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

import io.github.subiyacryolite.jds.JdsDb
import io.github.subiyacryolite.jds.JdsEntity
import io.github.subiyacryolite.jds.enums.JdsFieldType
import java.util.HashSet
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.collections.ArrayList

class JdsLoadEmbedded<T : JdsEntity>(private val jdsDb: JdsDb, private val referenceType: Class<T>, private vararg val container: JdsEmbeddedContainer) : Callable<List<T>> {

    /**
     *
     * @return
     */
    override fun call(): List<T> {
        val output: MutableList<T> = ArrayList()
        container.forEachIndexed { index, element ->
            element.e.forEachIndexed { innerIndex, innerElement ->
                val instance = referenceType.newInstance()
                populate(index, innerIndex, instance, innerElement)
                output.add(instance)
            }
        }
        return output
    }

    /**
     *
     * @param outerIndex
     * @param innerIndex
     * @param entity
     * @param embeddedObject
     */
    private fun populate(outerIndex: Int, innerIndex: Int, entity: JdsEntity, embeddedObject: JdsEmbeddedObject) {
        //==============================================
        //Overviews
        //==============================================
        entity.overview.dateCreated = embeddedObject.o.dc
        entity.overview.dateModified = embeddedObject.o.dm
        entity.overview.entityId = embeddedObject.o.id
        entity.overview.uuid = embeddedObject.o.uuid
        entity.overview.live = embeddedObject.o.l
        entity.overview.version = embeddedObject.o.v
        //==============================================
        //PRIMITIVES :: Key-Value
        //==============================================
        embeddedObject.s.forEach { entity.populateProperties(JdsFieldType.STRING, it.k, it.v) }
        embeddedObject.l.forEach { entity.populateProperties(JdsFieldType.LONG, it.k, it.v) }
        embeddedObject.i.forEach { entity.populateProperties(JdsFieldType.INT, it.k, it.v) }
        embeddedObject.b.forEach { entity.populateProperties(JdsFieldType.BOOLEAN, it.k, it.v) }
        embeddedObject.f.forEach { entity.populateProperties(JdsFieldType.FLOAT, it.k, it.v) }
        embeddedObject.d.forEach { entity.populateProperties(JdsFieldType.DOUBLE, it.k, it.v) }
        //==============================================
        //Dates & Time :: Key-Value
        //==============================================
        embeddedObject.ldt.forEach { entity.populateProperties(JdsFieldType.DATE_TIME, it.k, it.v) }
        embeddedObject.ld.forEach { entity.populateProperties(JdsFieldType.DATE, it.k, it.v) }
        embeddedObject.zdt.forEach { entity.populateProperties(JdsFieldType.ZONED_DATE_TIME, it.k, it.v) }
        embeddedObject.t.forEach { entity.populateProperties(JdsFieldType.TIME, it.k, it.v) }
        embeddedObject.du.forEach { entity.populateProperties(JdsFieldType.DURATION, it.k, it.v) }
        embeddedObject.md.forEach { entity.populateProperties(JdsFieldType.MONTH_DAY, it.k, it.v) }
        embeddedObject.ym.forEach { entity.populateProperties(JdsFieldType.YEAR_MONTH, it.k, it.v) }
        embeddedObject.p.forEach { entity.populateProperties(JdsFieldType.PERIOD, it.k, it.v) }
        //==============================================
        //BLOB :: Key-Value
        //==============================================
        embeddedObject.bl.forEach { entity.populateProperties(JdsFieldType.BLOB, it.id, it.v) }
        //==============================================
        //Enums :: Index-Value
        //==============================================
        embeddedObject.e.forEach { entity.populateProperties(JdsFieldType.ENUM, it.k, it.v) }
        embeddedObject.ea.forEach { entity.populateProperties(JdsFieldType.ENUM_COLLECTION, it.k, it.v) }
        //==============================================
        //ARRAYS
        //==============================================
        embeddedObject.ia.forEach { entity.populateProperties(JdsFieldType.INT_COLLECTION, it.k, it.v) }
        embeddedObject.fa.forEach { entity.populateProperties(JdsFieldType.FLOAT_COLLECTION, it.k, it.v) }
        embeddedObject.la.forEach { entity.populateProperties(JdsFieldType.LONG_COLLECTION, it.k, it.v) }
        embeddedObject.sa.forEach { entity.populateProperties(JdsFieldType.STRING_COLLECTION, it.k, it.v) }
        embeddedObject.da.forEach { entity.populateProperties(JdsFieldType.DOUBLE_COLLECTION, it.k, it.v) }
        embeddedObject.dta.forEach { entity.populateProperties(JdsFieldType.DATE_TIME_COLLECTION, it.k, it.v) }
        //==============================================
        //EMBEDDED OBJECTS
        //==============================================
        val uuids = HashSet<String>()//ids should be unique
        val innerObjects = ConcurrentLinkedQueue<JdsEntity>()//can be multiple copies of the same object however
        embeddedObject.eb.forEach {
            entity.populateObjects(jdsDb, it.f, it.i, it.c, innerObjects, uuids)
        }
        innerObjects.forEach {
            //populate the inner objects
            populate(outerIndex, innerIndex, it, embeddedObject.eo.find { itx -> itx.o.uuid == it.overview.uuid }!!)
        }
    }
}