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
import io.github.subiyacryolite.jds.FieldEnum
import io.github.subiyacryolite.jds.context.DbContext
import io.github.subiyacryolite.jds.enums.FieldType
import io.github.subiyacryolite.jds.extensions.toUuid
import java.time.*
import java.util.concurrent.Callable

class LoadPortable<T : Entity>(
        private val dbContext: DbContext,
        private val referenceType: Class<T>,
        private vararg val container: PortableContainer
) : Callable<MutableCollection<T>> {

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
    private fun populate(
            dbContext: DbContext,
            entity: Entity,
            portableEntity: PortableEntity
    ) {
        entity.overview.entityId = portableEntity.overview.entityId
        entity.overview.id = portableEntity.overview.id
        entity.overview.editVersion = portableEntity.overview.editVersion
        //==============================================
        portableEntity.blobValues.forEach { field ->
            val fieldId = field.key
            val value = field.value
            if (entity.populateProperty(dbContext, fieldId, FieldType.Blob)) {
                entity.blobValues.getValue(fieldId).value = when (value) {
                    is ByteArray -> value
                    else -> null
                }
            }
        }
        portableEntity.booleanValues.forEach { field ->
            val fieldId = field.key
            val value = field.value
            if (entity.populateProperty(dbContext, fieldId, FieldType.Boolean)) {
                entity.booleanValues.getValue(fieldId).value = when (value) {
                    is Int -> value == 1
                    else -> null
                }
            }
        }
        portableEntity.dateValues.forEach { field ->
            val fieldId = field.key
            val value = field.value
            if (entity.populateProperty(dbContext, fieldId, FieldType.Date)) {
                entity.localDateValues.getValue(fieldId).value = when (value) {
                    is Long -> LocalDate.ofEpochDay(value)
                    else -> null
                }
            }
        }
        portableEntity.doubleValues.forEach { field ->
            val fieldId = field.key
            val value = field.value
            if (entity.populateProperty(dbContext, fieldId, FieldType.Double)) {
                entity.doubleValues.getValue(fieldId).value = value
            }
        }
        portableEntity.durationValues.forEach { field ->
            val fieldId = field.key
            val value = field.value
            if (entity.populateProperty(dbContext, fieldId, FieldType.Duration)) {
                entity.durationValues.getValue(fieldId).value = when (value) {
                    is Long -> Duration.ofNanos(value)
                    else -> null
                }
            }
        }
        portableEntity.dateTimeValues.forEach { field ->
            val fieldId = field.key
            val value = field.value
            if (entity.populateProperty(dbContext, fieldId, FieldType.DateTime)) {
                entity.localDateTimeValues.getValue(fieldId).value = when (value) {
                    is Long -> LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.of("UTC"))
                    else -> null
                }
            }
        }
        portableEntity.floatValue.forEach { field ->
            val fieldId = field.key
            val value = field.value
            if (entity.populateProperty(dbContext, fieldId, FieldType.Float)) {
                entity.floatValues.getValue(fieldId).value = value
            }
        }
        portableEntity.integerValues.forEach { field ->
            val fieldId = field.key
            val value = field.value
            if (entity.populateProperty(dbContext, fieldId, FieldType.Int)) {
                entity.integerValues.getValue(fieldId).value = value
            }
        }
        portableEntity.shortValues.forEach { field ->
            val fieldId = field.key
            val value = field.value
            if (entity.populateProperty(dbContext, fieldId, FieldType.Short)) {
                entity.shortValues.getValue(fieldId).value = value
            }
        }
        portableEntity.uuidValues.forEach { field ->
            val fieldId = field.key
            val value = field.value
            if (entity.populateProperty(dbContext, fieldId, FieldType.Uuid)) {
                entity.uuidValues.getValue(fieldId).value = value?.toUuid()
            }
        }
        portableEntity.longValues.forEach { field ->
            val fieldId = field.key
            val value = field.value
            if (entity.populateProperty(dbContext, fieldId, FieldType.Long)) {
                entity.longValues.getValue(fieldId).value = value
            }
        }
        portableEntity.monthDayValues.forEach { field ->
            val fieldId = field.key
            val value = field.value
            if (entity.populateProperty(dbContext, fieldId, FieldType.MonthDay)) {
                entity.monthDayValues.getValue(fieldId).value = when (value) {
                    is String -> MonthDay.parse(value)
                    else -> null
                }
            }
        }
        portableEntity.periodValues.forEach { field ->
            val fieldId = field.key
            val value = field.value
            if (entity.populateProperty(dbContext, fieldId, FieldType.Period)) {
                entity.periodValues.getValue(fieldId).value = when (value) {
                    is String -> Period.parse(value)
                    else -> null
                }
            }
        }
        portableEntity.stringValues.forEach { field ->
            val fieldId = field.key
            val value = field.value
            if (entity.populateProperty(dbContext, fieldId, FieldType.String)) {
                entity.stringValues.getValue(fieldId).value = value
            }
        }
        portableEntity.timeValues.forEach { field ->
            val fieldId = field.key
            val value = field.value
            if (entity.populateProperty(dbContext, fieldId, FieldType.Time)) {
                entity.localTimeValues.getValue(fieldId).value = when (value) {
                    is Long -> LocalTime.ofNanoOfDay(value)
                    else -> null
                }
            }
        }
        portableEntity.yearMonthValues.forEach { field ->
            val fieldId = field.key
            val value = field.value
            if (entity.populateProperty(dbContext, fieldId, FieldType.YearMonth)) {
                entity.yearMonthValues.getValue(fieldId).value = when (value) {
                    is String -> YearMonth.parse(value)
                    else -> null
                }
            }
        }
        portableEntity.zonedDateTimeValues.forEach { field ->
            val fieldId = field.key
            val value = field.value
            if (entity.populateProperty(dbContext, fieldId, FieldType.ZonedDateTime)) {
                entity.zonedDateTimeValues.getValue(fieldId).value = when (value) {
                    is Long -> ZonedDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.of("UTC"))
                    else -> null
                }
            }
        }
        portableEntity.enumValues.forEach { field ->
            val fieldId = field.key
            val value = field.value
            if (entity.populateProperty(dbContext, fieldId, FieldType.Enum)) {
                val entry = entity.enumValues.getValue(fieldId)
                val fieldEnum = FieldEnum.enums[fieldId]
                if (fieldEnum != null && value != null) {
                    entry.value = fieldEnum.valueOf(value)
                }
            }
        }
        portableEntity.enumStringValues.forEach { field ->
            val fieldId = field.key
            val value = field.value
            if (entity.populateProperty(dbContext, fieldId, FieldType.EnumString)) {
                val entry = entity.stringEnumValues.getValue(fieldId)
                val fieldEnum = FieldEnum.enums[fieldId]
                if (fieldEnum != null && value != null) {
                    entry.value = fieldEnum.valueOf(value)
                }
            }
        }
        portableEntity.dateTimeCollection.forEach { field ->
            val fieldId = field.key
            val collection = field.values
            if (entity.populateProperty(dbContext, fieldId, FieldType.DateTimeCollection)) {
                val dest = entity.dateTimeCollections.getValue(fieldId)
                collection.forEach { value -> dest.add((value).toLocalDateTime()) }
            }
        }
        portableEntity.doubleCollections.forEach { field ->
            val fieldId = field.key
            val collection = field.values
            if (entity.populateProperty(dbContext, fieldId, FieldType.DoubleCollection)) {
                val dest = entity.doubleCollections.getValue(fieldId)
                collection.forEach { value -> dest.add(value) }
            }
        }
        portableEntity.floatCollections.forEach { field ->
            val fieldId = field.key
            val collection = field.values
            if (entity.populateProperty(dbContext, fieldId, FieldType.FloatCollection)) {
                val dest = entity.floatCollections.getValue(fieldId)
                collection.forEach { value -> dest.add(value) }
            }
        }
        portableEntity.integerCollections.forEach { field ->
            val fieldId = field.key
            val collection = field.values
            if (entity.populateProperty(dbContext, fieldId, FieldType.IntCollection)) {
                val dest = entity.integerCollections.getValue(fieldId)
                collection.forEach { value -> dest.add(value) }
            }
        }
        portableEntity.shortCollections.forEach { field ->
            val fieldId = field.key
            val collection = field.values
            if (entity.populateProperty(dbContext, fieldId, FieldType.ShortCollection)) {
                val dest = entity.shortCollections.getValue(fieldId)
                collection.forEach { value -> dest.add(value) }
            }
        }
        portableEntity.uuidCollections.forEach { field ->
            val fieldId = field.key
            val collection = field.values
            if (entity.populateProperty(dbContext, fieldId, FieldType.UuidCollection)) {
                val dest = entity.uuidCollections.getValue(fieldId)
                collection.forEach { value -> dest.add(value.toUuid()!!) }
            }
        }
        portableEntity.longCollections.forEach { field ->
            val fieldId = field.key
            val collection = field.values
            if (entity.populateProperty(dbContext, fieldId, FieldType.LongCollection)) {
                val dest = entity.longCollections.getValue(fieldId)
                collection.forEach { value -> dest.add(value) }
            }
        }
        portableEntity.stringCollections.forEach { field ->
            val fieldId = field.key
            val collection = field.values
            if (entity.populateProperty(dbContext, fieldId, FieldType.StringCollection)) {
                val dest = entity.stringCollections.getValue(fieldId)
                collection.forEach { value -> dest.add(value) }
            }
        }
        portableEntity.enumCollections.forEach { field ->
            val fieldId = field.key
            val collection = field.values
            if (entity.populateProperty(dbContext, fieldId, FieldType.EnumCollection)) {
                val dest = entity.enumCollections.getValue(fieldId)
                val fieldEnum = FieldEnum.enums[fieldId]
                if (fieldEnum != null) {
                    collection.forEach { enumOrdinal ->
                        val enumValues = fieldEnum.values
                        if (enumOrdinal < enumValues.size) {
                            dest.add(enumValues.find { enumValue -> enumValue.ordinal == enumOrdinal }!!)
                        }
                    }
                }
            }
        }
        portableEntity.enumStringCollections.forEach { field ->
            val fieldId = field.key
            val collection = field.values
            if (entity.populateProperty(dbContext, fieldId, FieldType.EnumStringCollection)) {
                val dest = entity.enumStringCollections.getValue(fieldId)
                val fieldEnum = FieldEnum.enums[fieldId]
                if (fieldEnum != null) {
                    collection.forEach { enumString ->
                        val enumVal = fieldEnum.valueOf(enumString)
                        if (enumVal != null)
                            dest.add(enumVal)
                    }
                }
            }
        }
        portableEntity.mapIntKeyValues.forEach { field ->
            val fieldId = field.key
            if (entity.populateProperty(dbContext, fieldId, FieldType.MapIntKey)) {
                entity.mapIntKeyValues.getValue(fieldId).putAll(field.values)
            }
        }
        portableEntity.mapStringKeyValues.forEach { field ->
            val fieldId = field.key
            if (entity.populateProperty(dbContext, fieldId, FieldType.MapStringKey)) {
                entity.mapStringKeyValues.getValue(fieldId).putAll(field.values)
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
            val referenceClass = Entity.classes[entityId]
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
            val referenceClass = Entity.classes[entityId]
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