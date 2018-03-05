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
        //==============================================
        //Overviews
        //==============================================
        entity.overview.entityId = embeddedObject.o.entityId
        entity.overview.uuid = embeddedObject.o.uuid
        entity.overview.entityVersion = embeddedObject.o.version
        entity.overview.editVersion = embeddedObject.o.editVersion
        //==============================================
        //PRIMITIVES :: Key-Value
        //==============================================
        embeddedObject.i.forEach {
            when (jdsDb.typeOfField(it.k)) {
                JdsFieldType.INT -> entity.populateProperties(JdsFieldType.INT, it.k, it.v)
                JdsFieldType.ENUM -> entity.populateProperties(JdsFieldType.ENUM, it.k, it.v)
                JdsFieldType.ENUM_COLLECTION -> entity.populateProperties(JdsFieldType.ENUM_COLLECTION, it.k, it.v)
                JdsFieldType.INT_COLLECTION -> entity.populateProperties(JdsFieldType.INT_COLLECTION, it.k, it.v)
            }
        }
        embeddedObject.s.forEach {
            when (jdsDb.typeOfField(it.k)) {
                JdsFieldType.STRING -> entity.populateProperties(JdsFieldType.STRING, it.k, it.v)
                JdsFieldType.STRING_COLLECTION -> entity.populateProperties(JdsFieldType.STRING_COLLECTION, it.k, it.v)
                JdsFieldType.MONTH_DAY -> entity.populateProperties(JdsFieldType.MONTH_DAY, it.k, it.v)
                JdsFieldType.YEAR_MONTH -> entity.populateProperties(JdsFieldType.YEAR_MONTH, it.k, it.v)
                JdsFieldType.PERIOD -> entity.populateProperties(JdsFieldType.PERIOD, it.k, it.v)
            }
        }
        embeddedObject.f.forEach {
            when (jdsDb.typeOfField(it.k)) {
                JdsFieldType.FLOAT -> entity.populateProperties(JdsFieldType.FLOAT, it.k, it.v)
                JdsFieldType.FLOAT_COLLECTION -> entity.populateProperties(JdsFieldType.FLOAT_COLLECTION, it.k, it.v)
            }
        }
        embeddedObject.d.forEach {
            when (jdsDb.typeOfField(it.k)) {
                JdsFieldType.DOUBLE -> entity.populateProperties(JdsFieldType.DOUBLE, it.k, it.v)
                JdsFieldType.DOUBLE_COLLECTION -> entity.populateProperties(JdsFieldType.DOUBLE_COLLECTION, it.k, it.v)
            }
        }
        embeddedObject.l.forEach {
            when (jdsDb.typeOfField(it.k)) {
                JdsFieldType.LONG -> entity.populateProperties(JdsFieldType.LONG, it.k, it.v)
                JdsFieldType.LONG_COLLECTION -> entity.populateProperties(JdsFieldType.LONG_COLLECTION, it.k, it.v)
                JdsFieldType.ZONED_DATE_TIME -> entity.populateProperties(JdsFieldType.ZONED_DATE_TIME, it.k, it.v)
                JdsFieldType.TIME -> entity.populateProperties(JdsFieldType.TIME, it.k, it.v)
                JdsFieldType.DURATION -> entity.populateProperties(JdsFieldType.DURATION, it.k, it.v)
            }
        }
        embeddedObject.b.forEach { entity.populateProperties(JdsFieldType.BOOLEAN, it.k, it.v) }
        //==============================================
        //Dates & Time :: Key-Value
        //==============================================
        embeddedObject.ldt.forEach {
            when (jdsDb.typeOfField(it.k)) {
                JdsFieldType.DATE -> entity.populateProperties(JdsFieldType.DATE, it.k, it.v)
                JdsFieldType.DATE_TIME -> entity.populateProperties(JdsFieldType.DATE_TIME, it.k, it.v)
                JdsFieldType.DATE_TIME_COLLECTION -> entity.populateProperties(JdsFieldType.DATE_TIME_COLLECTION, it.k, it.v)
            }
        }
        //==============================================
        //BLOB :: Key-Value
        //==============================================
        embeddedObject.bl.forEach { entity.populateProperties(JdsFieldType.BLOB, it.id, it.v) }
        //==============================================
        //EMBEDDED OBJECTS
        //==============================================
        val uuids = HashSet<String>()//ids should be unique
        val innerObjects = ConcurrentLinkedQueue<JdsEntity>()//can be multiple copies of the same object however

        embeddedObject.eo.forEach {
            populateObjects(entity, jdsDb, it.o.fieldId, it.o.entityId, it.o.uuid, it.o.editVersion, it)
        }
    }

    private fun populateObjects(entity: JdsEntity, jdsDb: JdsDb, fieldId: Long?, entityId: Long, uuid: String, editVersion: Int, eo: JdsEmbeddedObject) {
        if (fieldId == null) return
        entity.objectArrayProperties.filter { it.key.fieldEntity.id == fieldId }.forEach {
            val entity = jdsDb.classes[entityId]!!.newInstance()//create array element
            entity.overview.uuid = uuid
            entity.overview.editVersion = editVersion
            populate(entity, eo)
            it.value.add(entity)
        }
        //find existing elements
        entity.objectProperties.filter { it.key.fieldEntity.id == fieldId }.forEach {
            if (it.value.value == null)
                it.value.value = jdsDb.classes[entityId]!!.newInstance()//create array element
            it.value.value.overview.uuid = uuid
            it.value.value.overview.editVersion = editVersion
            populate(it.value.value, eo)
        }
    }

    fun filterFunction(entity: JdsEntity, eo: JdsEmbeddedObject): Boolean {
        return entity.overview.uuid == eo.o.uuid &&  entity.overview.editVersion == eo.o.editVersion
    }
}