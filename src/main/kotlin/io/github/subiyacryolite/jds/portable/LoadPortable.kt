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

import io.github.subiyacryolite.jds.Entity
import io.github.subiyacryolite.jds.context.DbContext
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
        portableEntity.blobValues.forEach { kvp ->
            entity.populateProperties(dbContext, FieldType.Blob, kvp.key, kvp.value)
        }
        portableEntity.booleanValues.forEach { kvp ->
            entity.populateProperties(dbContext, FieldType.Boolean, kvp.key, kvp.value)
        }
        portableEntity.dateValues.forEach { kvp ->
            entity.populateProperties(dbContext, FieldType.Date, kvp.key, kvp.value)
        }
        portableEntity.doubleValues.forEach { kvp ->
            entity.populateProperties(dbContext, FieldType.Double, kvp.key, kvp.value)
        }
        portableEntity.durationValues.forEach { kvp ->
            entity.populateProperties(dbContext, FieldType.Duration, kvp.key, kvp.value)
        }
        portableEntity.dateTimeValues.forEach { kvp ->
            entity.populateProperties(dbContext, FieldType.DateTime, kvp.key, kvp.value)
        }
        portableEntity.floatValue.forEach { kvp ->
            entity.populateProperties(dbContext, FieldType.Float, kvp.key, kvp.value)
        }
        portableEntity.integerValues.forEach { kvp ->
            entity.populateProperties(dbContext, FieldType.Int, kvp.key, kvp.value)
        }
        portableEntity.shortValues.forEach { kvp ->
            entity.populateProperties(dbContext, FieldType.Short, kvp.key, kvp.value)
        }
        portableEntity.uuidValues.forEach { kvp ->
            entity.populateProperties(dbContext, FieldType.Uuid, kvp.key, kvp.value)
        }
        portableEntity.longValues.forEach { kvp ->
            entity.populateProperties(dbContext, FieldType.Long, kvp.key, kvp.value)
        }
        portableEntity.monthDayValues.forEach { kvp ->
            entity.populateProperties(dbContext, FieldType.MonthDay, kvp.key, kvp.value)
        }
        portableEntity.periodValues.forEach { kvp ->
            entity.populateProperties(dbContext, FieldType.Period, kvp.key, kvp.value)
        }
        portableEntity.stringValues.forEach { kvp ->
            entity.populateProperties(dbContext, FieldType.String, kvp.key, kvp.value)
        }
        portableEntity.timeValues.forEach { kvp ->
            entity.populateProperties(dbContext, FieldType.Time, kvp.key, kvp.value)
        }
        portableEntity.yearMonthValues.forEach { kvp ->
            entity.populateProperties(dbContext, FieldType.YearMonth, kvp.key, kvp.value)
        }
        portableEntity.zonedDateTimeValues.forEach { kvp ->
            entity.populateProperties(dbContext, FieldType.ZonedDateTime, kvp.key, kvp.value)
        }
        portableEntity.enumValues.forEach { kvp ->
            entity.populateProperties(dbContext, FieldType.Enum, kvp.key, kvp.value)
        }
        portableEntity.enumStringValues.forEach { kvp ->
            entity.populateProperties(dbContext, FieldType.EnumString, kvp.key, kvp.value)
        }
        portableEntity.dateTimeCollection.forEach { collection ->
            collection.values.forEach { entry ->
                entity.populateProperties(dbContext, FieldType.DateTimeCollection, collection.key, entry)
            }
        }
        portableEntity.doubleCollections.forEach { collection ->
            collection.values.forEach { entry ->
                entity.populateProperties(dbContext, FieldType.DoubleCollection, collection.key, entry)
            }
        }
        portableEntity.enumCollections.forEach { collection ->
            collection.values.forEach { entry ->
                entity.populateProperties(dbContext, FieldType.EnumCollection, collection.key, entry)
            }
        }
        portableEntity.enumStringCollections.forEach { collection ->
            collection.values.forEach { entry ->
                entity.populateProperties(dbContext, FieldType.EnumStringCollection, collection.key, entry)
            }
        }
        portableEntity.floatCollections.forEach { collection ->
            collection.values.forEach { entry ->
                entity.populateProperties(dbContext, FieldType.FloatCollection, collection.key, entry)
            }
        }
        portableEntity.integerCollections.forEach { collection ->
            collection.values.forEach { entry ->
                entity.populateProperties(dbContext, FieldType.IntCollection, collection.key, entry)
            }
        }
        portableEntity.uuidCollections.forEach { collection ->
            collection.values.forEach { entry ->
                entity.populateProperties(dbContext, FieldType.UuidCollection, collection.key, entry)
            }
        }
        portableEntity.longCollections.forEach { collection ->
            collection.values.forEach { entry ->
                entity.populateProperties(dbContext, FieldType.LongCollection, collection.key, entry)
            }
        }
        portableEntity.stringCollections.forEach { collection ->
            collection.values.forEach { entry ->
                entity.populateProperties(dbContext, FieldType.StringCollection, collection.key, entry)
            }
        }
        //==============================================
        portableEntity.entityOverviews.forEach { subEntities ->
            populateObjects(entity, dbContext, subEntities.overview.fieldId, subEntities.overview.entityId, subEntities.overview.id, subEntities.overview.editVersion, subEntities)
        }
    }

    private fun populateObjects(
            entity: Entity,
            dbContext: DbContext,
            fieldId: Int?,
            entityId: Int,
            id: String,
            editVersion: Int,
            portableEntity: PortableEntity
    ) {
        if (fieldId == null) return
        entity.objectCollections.filter { entry ->
            entry.key.field.id == fieldId
        }.forEach { entry ->
            val referenceClass = dbContext.classes[entityId]
            if (referenceClass != null) {
                val subEntity = referenceClass.getDeclaredConstructor().newInstance()//create array element
                subEntity.overview.id = id
                subEntity.overview.editVersion = editVersion
                populate(dbContext, subEntity, portableEntity)
                entry.value.add(subEntity)
            }
        }
        //find existing elements
        entity.objectValues.filter { entry ->
            entry.key.field.id == fieldId
        }.forEach { entry ->
            val referenceClass = dbContext.classes[entityId]
            if (referenceClass != null) {
                entry.value.value = referenceClass.getDeclaredConstructor().newInstance()//create array element
                entry.value.value.overview.id = id
                entry.value.value.overview.editVersion = editVersion
                val subEntity = entry.value.value as Entity
                populate(dbContext, subEntity, portableEntity)
            }
        }
    }
}