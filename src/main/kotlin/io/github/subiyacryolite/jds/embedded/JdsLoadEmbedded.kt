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
import java.util.concurrent.Callable

class JdsLoadEmbedded<T : JdsEntity>(private val jdsDb: JdsDb, private val referenceType: Class<T>, private vararg val container: JdsEmbeddedContainer) : Callable<List<T>> {

    /**
     *
     * @return
     */
    override fun call(): List<T> {
        val output: MutableList<T> = ArrayList()
        container.forEach { element ->
            element.e.forEach { innerElement ->
                val instance = referenceType.newInstance()
                populate(instance, innerElement)
                output.add(instance)
            }
        }
        return output
    }

    /**
     *
     * @param entity
     * @param embeddedObject
     */
    private fun populate(entity: JdsEntity, embeddedObject: JdsEmbeddedObject) {
        entity.overview.entityId = embeddedObject.overview.entityId
        entity.overview.uuid = embeddedObject.overview.uuid
        entity.overview.entityVersion = embeddedObject.overview.version
        entity.overview.editVersion = embeddedObject.overview.editVersion
        //==============================================
        embeddedObject.blv.forEach { entity.populateProperties(JdsFieldType.BLOB, it.id, it.v) }
        embeddedObject.bv.forEach { entity.populateProperties(JdsFieldType.BOOLEAN, it.k, it.v) }
        embeddedObject.dte.forEach { entity.populateProperties(JdsFieldType.DATE, it.k, it.v) }
        embeddedObject.dtc.forEach { parent -> parent.v.forEach { entity.populateProperties(JdsFieldType.DATE_TIME_COLLECTION, parent.k, it) } }
        embeddedObject.dtv.forEach { entity.populateProperties(JdsFieldType.DATE_TIME, it.k, it.v) }
        embeddedObject.dc.forEach { parent -> parent.v.forEach { entity.populateProperties(JdsFieldType.DOUBLE_COLLECTION, parent.k, it) } }
        embeddedObject.dv.forEach { entity.populateProperties(JdsFieldType.DOUBLE, it.k, it.v) }
        embeddedObject.du.forEach { entity.populateProperties(JdsFieldType.DURATION, it.k, it.v) }
        embeddedObject.ec.forEach { parent -> parent.v.forEach { entity.populateProperties(JdsFieldType.ENUM_COLLECTION, parent.k, it) } }
        embeddedObject.ev.forEach { entity.populateProperties(JdsFieldType.ENUM, it.k, it.v) }
        embeddedObject.fc.forEach { parent -> parent.v.forEach { entity.populateProperties(JdsFieldType.FLOAT_COLLECTION, parent.k, it) } }
        embeddedObject.fv.forEach { entity.populateProperties(JdsFieldType.FLOAT, it.k, it.v) }
        embeddedObject.ic.forEach { parent -> parent.v.forEach { entity.populateProperties(JdsFieldType.INT_COLLECTION, parent.k, it) } }
        embeddedObject.iv.forEach { entity.populateProperties(JdsFieldType.INT, it.k, it.v) }
        embeddedObject.lc.forEach { parent -> parent.v.forEach { entity.populateProperties(JdsFieldType.LONG_COLLECTION, parent.k, it) } }
        embeddedObject.lv.forEach { entity.populateProperties(JdsFieldType.LONG, it.k, it.v) }
        embeddedObject.md.forEach { entity.populateProperties(JdsFieldType.MONTH_DAY, it.k, it.v) }
        embeddedObject.pv.forEach { entity.populateProperties(JdsFieldType.PERIOD, it.k, it.v) }
        embeddedObject.sc.forEach { parent -> parent.v.forEach { entity.populateProperties(JdsFieldType.STRING_COLLECTION, parent.k, it) } }
        embeddedObject.sv.forEach { entity.populateProperties(JdsFieldType.STRING, it.k, it.v) }
        embeddedObject.tv.forEach { entity.populateProperties(JdsFieldType.TIME, it.k, it.v) }
        embeddedObject.ym.forEach { entity.populateProperties(JdsFieldType.YEAR_MONTH, it.k, it.v) }
        embeddedObject.zdt.forEach { entity.populateProperties(JdsFieldType.ZONED_DATE_TIME, it.k, it.v) }
        //==============================================
        embeddedObject.eo.forEach { populateObjects(entity, jdsDb, it.overview.fieldId, it.overview.entityId, it.overview.uuid, it.overview.editVersion, it) }
    }

    private fun populateObjects(entity: JdsEntity, jdsDb: JdsDb, fieldId: Long?, entityId: Long, uuid: String, editVersion: Int, eo: JdsEmbeddedObject) {
        if (fieldId == null) return
        entity.objectArrayProperties.filter { it.key.fieldEntity.id == fieldId }.forEach {
            val referenceClass = jdsDb.classes[entityId]
            if (referenceClass != null) {
                val entity = referenceClass.newInstance()//create array element
                entity.overview.uuid = uuid
                entity.overview.editVersion = editVersion
                populate(entity, eo)
                it.value.add(entity)
            }
        }
        //find existing elements
        entity.objectProperties.filter { it.key.fieldEntity.id == fieldId }.forEach {
            val referenceClass = jdsDb.classes[entityId]
            if (referenceClass != null) {
                if (it.value.value == null)
                    it.value.value = referenceClass.newInstance()//create array element
                it.value.value.overview.uuid = uuid
                it.value.value.overview.editVersion = editVersion
                populate(it.value.value, eo)
            }
        }
    }

    fun filterFunction(entity: JdsEntity, eo: JdsEmbeddedObject): Boolean {
        return entity.overview.uuid == eo.overview.uuid && entity.overview.editVersion == eo.overview.editVersion
    }
}