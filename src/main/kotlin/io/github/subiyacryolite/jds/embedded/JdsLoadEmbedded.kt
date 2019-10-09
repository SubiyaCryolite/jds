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

class JdsLoadEmbedded<T : JdsEntity>(private val db: JdsDb, private val referenceType: Class<T>, private vararg val container: JdsEmbeddedContainer) : Callable<List<T>> {

    /**
     *
     * @return
     */
    @Throws(Exception::class)
    override fun call(): List<T> {
        val output: MutableList<T> = ArrayList()
        container.forEach { element ->
            element.embeddedObjects.forEach { innerElement ->
                val instance = referenceType.getDeclaredConstructor().newInstance()
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
    @Throws(Exception::class)
    private fun populate(entity: JdsEntity, embeddedObject: JdsEmbeddedObject) {
        entity.overview.entityId = embeddedObject.overview.entityId
        entity.overview.uuid = embeddedObject.overview.uuid
        entity.overview.editVersion = embeddedObject.overview.editVersion
        //==============================================
        embeddedObject.blobValues.forEach { entity.populateProperties(JdsFieldType.BLOB, it.key, it.value) }
        embeddedObject.booleanValues.forEach { entity.populateProperties(JdsFieldType.BOOLEAN, it.key, it.value) }
        embeddedObject.dateValues.forEach { entity.populateProperties(JdsFieldType.DATE, it.key, it.value) }
        embeddedObject.dateTimeCollection.forEach { parent -> parent.values.forEach { entity.populateProperties(JdsFieldType.DATE_TIME_COLLECTION, parent.key, it) } }
        embeddedObject.dateTimeValues.forEach { entity.populateProperties(JdsFieldType.DATE_TIME, it.key, it.value) }
        embeddedObject.doubleCollections.forEach { parent -> parent.values.forEach { entity.populateProperties(JdsFieldType.DOUBLE_COLLECTION, parent.key, it) } }
        embeddedObject.doubleValues.forEach { entity.populateProperties(JdsFieldType.DOUBLE, it.key, it.value) }
        embeddedObject.durationValues.forEach { entity.populateProperties(JdsFieldType.DURATION, it.key, it.value) }
        embeddedObject.enumCollections.forEach { parent -> parent.values.forEach { entity.populateProperties(JdsFieldType.ENUM_COLLECTION, parent.key, it) } }
        embeddedObject.enumStringCollections.forEach { parent -> parent.values.forEach { entity.populateProperties(JdsFieldType.ENUM_STRING_COLLECTION, parent.key, it) } }
        embeddedObject.enumValues.forEach { entity.populateProperties(JdsFieldType.ENUM, it.key, it.value) }
        embeddedObject.enumStringValues.forEach { entity.populateProperties(JdsFieldType.ENUM_STRING, it.key, it.value) }
        embeddedObject.floatCollections.forEach { parent -> parent.values.forEach { entity.populateProperties(JdsFieldType.FLOAT_COLLECTION, parent.key, it) } }
        embeddedObject.floatValue.forEach { entity.populateProperties(JdsFieldType.FLOAT, it.key, it.value) }
        embeddedObject.integerCollections.forEach { parent -> parent.values.forEach { entity.populateProperties(JdsFieldType.INT_COLLECTION, parent.key, it) } }
        embeddedObject.integerValues.forEach { entity.populateProperties(JdsFieldType.INT, it.key, it.value) }
        embeddedObject.shortValues.forEach { entity.populateProperties(JdsFieldType.SHORT, it.key, it.value) }
        embeddedObject.uuidValues.forEach { entity.populateProperties(JdsFieldType.UUID, it.key, it.value) }
        embeddedObject.longCollections.forEach { parent -> parent.values.forEach { entity.populateProperties(JdsFieldType.LONG_COLLECTION, parent.key, it) } }
        embeddedObject.longValues.forEach { entity.populateProperties(JdsFieldType.LONG, it.key, it.value) }
        embeddedObject.monthDayValues.forEach { entity.populateProperties(JdsFieldType.MONTH_DAY, it.key, it.value) }
        embeddedObject.periodValues.forEach { entity.populateProperties(JdsFieldType.PERIOD, it.key, it.value) }
        embeddedObject.stringCollections.forEach { parent -> parent.values.forEach { entity.populateProperties(JdsFieldType.STRING_COLLECTION, parent.key, it) } }
        embeddedObject.stringValues.forEach { entity.populateProperties(JdsFieldType.STRING, it.key, it.value) }
        embeddedObject.timeValues.forEach { entity.populateProperties(JdsFieldType.TIME, it.key, it.value) }
        embeddedObject.yearMonthValues.forEach { entity.populateProperties(JdsFieldType.YEAR_MONTH, it.key, it.value) }
        embeddedObject.zonedDateTimeValues.forEach { entity.populateProperties(JdsFieldType.ZONED_DATE_TIME, it.key, it.value) }
        //==============================================
        embeddedObject.entityOverviews.forEach { populateObjects(entity, db, it.overview.fieldId, it.overview.entityId, it.overview.uuid, it.overview.editVersion, it) }
    }

    private fun populateObjects(entity: JdsEntity, jdsDb: JdsDb, fieldId: Long?, entityId: Long, uuid: String, editVersion: Int, eo: JdsEmbeddedObject) {
        if (fieldId == null) return
        entity.objectCollections.filter { it.key.field.id == fieldId }.forEach {
            val referenceClass = jdsDb.classes[entityId]
            if (referenceClass != null) {
                val innerEntity = referenceClass.getDeclaredConstructor().newInstance()//create array element
                innerEntity.overview.uuid = uuid
                innerEntity.overview.editVersion = editVersion
                populate(innerEntity, eo)
                it.value.add(innerEntity)
            }
        }
        //find existing elements
        entity.objectValues.filter { it.key.field.id == fieldId }.forEach {
            val referenceClass = jdsDb.classes[entityId]
            if (referenceClass != null) {
                if (it.value.value == null)
                    it.value.value = referenceClass.getDeclaredConstructor().newInstance()//create array element
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