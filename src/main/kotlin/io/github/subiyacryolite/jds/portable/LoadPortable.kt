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

import io.github.subiyacryolite.jds.context.DbContext
import io.github.subiyacryolite.jds.Entity
import io.github.subiyacryolite.jds.enums.FieldType
import java.util.concurrent.Callable

class LoadPortable<T : Entity>(private val dbContext: DbContext, private val referenceType: Class<T>, private vararg val container: PortableContainer) : Callable<MutableCollection<T>> {

    /**
     *
     * @return
     */
    @Throws(Exception::class)
    override fun call(): MutableCollection<T> {
        val output: MutableCollection<T> = ArrayList()
        container.forEach { element ->
            element.portableEntities.forEach { innerElement ->
                val instance = referenceType.getDeclaredConstructor().newInstance()
                populate(dbContext, instance, innerElement)
                output.add(instance)
            }
        }
        return output
    }

    /**
     *
     * @param entity
     * @param portableEntity
     */
    @Throws(Exception::class)
    private fun populate(dbContext: DbContext, entity: Entity, portableEntity: PortableEntity) {
        entity.overview.entityId = portableEntity.overview.entityId
        entity.overview.id = portableEntity.overview.id
        entity.overview.editVersion = portableEntity.overview.editVersion
        //==============================================
        portableEntity.blobValues.forEach { entity.populateProperties(dbContext, FieldType.Blob, it.key, it.value) }
        portableEntity.booleanValues.forEach { entity.populateProperties(dbContext, FieldType.Boolean, it.key, it.value) }
        portableEntity.dateValues.forEach { entity.populateProperties(dbContext, FieldType.Date, it.key, it.value) }
        portableEntity.dateTimeCollection.forEach { parent -> parent.values.forEach { entity.populateProperties(dbContext, FieldType.DateTimeCollection, parent.key, it) } }
        portableEntity.dateTimeValues.forEach { entity.populateProperties(dbContext, FieldType.DateTime, it.key, it.value) }
        portableEntity.doubleCollections.forEach { parent -> parent.values.forEach { entity.populateProperties(dbContext, FieldType.DoubleCollection, parent.key, it) } }
        portableEntity.doubleValues.forEach { entity.populateProperties(dbContext, FieldType.Double, it.key, it.value) }
        portableEntity.durationValues.forEach { entity.populateProperties(dbContext, FieldType.Duration, it.key, it.value) }
        portableEntity.enumCollections.forEach { parent -> parent.values.forEach { entity.populateProperties(dbContext, FieldType.EnumCollection, parent.key, it) } }
        portableEntity.enumStringCollections.forEach { parent -> parent.values.forEach { entity.populateProperties(dbContext, FieldType.EnumStringCollection, parent.key, it) } }
        portableEntity.enumValues.forEach { entity.populateProperties(dbContext, FieldType.Enum, it.key, it.value) }
        portableEntity.enumStringValues.forEach { entity.populateProperties(dbContext, FieldType.EnumString, it.key, it.value) }
        portableEntity.floatCollections.forEach { parent -> parent.values.forEach { entity.populateProperties(dbContext, FieldType.FloatCollection, parent.key, it) } }
        portableEntity.floatValue.forEach { entity.populateProperties(dbContext, FieldType.Float, it.key, it.value) }
        portableEntity.integerCollections.forEach { parent -> parent.values.forEach { entity.populateProperties(dbContext, FieldType.IntCollection, parent.key, it) } }
        portableEntity.integerValues.forEach { entity.populateProperties(dbContext, FieldType.Int, it.key, it.value) }
        portableEntity.shortValues.forEach { entity.populateProperties(dbContext, FieldType.Short, it.key, it.value) }
        portableEntity.uuidValues.forEach { entity.populateProperties(dbContext, FieldType.Uuid, it.key, it.value) }
        portableEntity.longCollections.forEach { parent -> parent.values.forEach { entity.populateProperties(dbContext, FieldType.LongCollection, parent.key, it) } }
        portableEntity.longValues.forEach { entity.populateProperties(dbContext, FieldType.Long, it.key, it.value) }
        portableEntity.monthDayValues.forEach { entity.populateProperties(dbContext, FieldType.MonthDay, it.key, it.value) }
        portableEntity.periodValues.forEach { entity.populateProperties(dbContext, FieldType.Period, it.key, it.value) }
        portableEntity.stringCollections.forEach { parent -> parent.values.forEach { entity.populateProperties(dbContext, FieldType.StringCollection, parent.key, it) } }
        portableEntity.stringValues.forEach { entity.populateProperties(dbContext, FieldType.String, it.key, it.value) }
        portableEntity.timeValues.forEach { entity.populateProperties(dbContext, FieldType.Time, it.key, it.value) }
        portableEntity.yearMonthValues.forEach { entity.populateProperties(dbContext, FieldType.YearMonth, it.key, it.value) }
        portableEntity.zonedDateTimeValues.forEach { entity.populateProperties(dbContext, FieldType.ZonedDateTime, it.key, it.value) }
        //==============================================
        portableEntity.entityOverviews.forEach { populateObjects(entity, dbContext, it.overview.fieldId, it.overview.entityId, it.overview.id, it.overview.editVersion, it) }
    }

    private fun populateObjects(entity: Entity, dbContext: DbContext, fieldId: Int?, entityId: Int, id: String, editVersion: Int, eo: PortableEntity) {
        if (fieldId == null) return
        entity.objectCollections.filter { it.key.field.id == fieldId }.forEach {
            val referenceClass = dbContext.classes[entityId]
            if (referenceClass != null) {
                val innerEntity = referenceClass.getDeclaredConstructor().newInstance()//create array element
                innerEntity.overview.id = id
                innerEntity.overview.editVersion = editVersion
                populate(dbContext, innerEntity, eo)
                it.value.add(innerEntity)
            }
        }
        //find existing elements
        entity.objectValues.filter { it.key.field.id == fieldId }.forEach {
            val referenceClass = dbContext.classes[entityId]
            if (referenceClass != null) {
                it.value.value = referenceClass.getDeclaredConstructor().newInstance()//create array element
                it.value.value.overview.id = id
                it.value.value.overview.editVersion = editVersion
                val jdsEntity = it.value.value as Entity
                populate(dbContext, jdsEntity, eo)
            }
        }
    }

    fun filterFunction(entity: Entity, eo: PortableEntity): Boolean {
        return entity.overview.id == eo.overview.id && entity.overview.editVersion == eo.overview.editVersion
    }
}