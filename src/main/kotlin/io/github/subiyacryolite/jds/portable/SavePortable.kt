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
import io.github.subiyacryolite.jds.extensions.filterIgnored
import io.github.subiyacryolite.jds.extensions.toByteArray
import java.sql.Timestamp
import java.time.*
import java.util.*
import java.util.concurrent.Callable
import kotlin.collections.ArrayList

/**
 * A helper class to transform [Entity] objects or collections in a portable format that can be serialized to JSON, XML, YAML or any other format of choice
 * @param entities a collection of entities to represent in a portable matter
 */
class SavePortable(private val dbContext: DbContext, private val entities: Iterable<Entity>) : Callable<PortableContainer> {

    override fun call(): PortableContainer {
        return PortableContainer(dbContext, entities)
    }

    companion object {

        private fun toByteArrayCollection(values: MutableCollection<UUID>): MutableCollection<ByteArray> {
            val output = ArrayList<ByteArray>()
            values.forEach { value ->
                output.add(value.toByteArray()!!)
            }
            return output
        }

        private fun toTimeStampCollection(values: MutableCollection<LocalDateTime>) = values.map { Timestamp.valueOf(it) }.toMutableList()

        private fun toIntCollection(values: MutableCollection<Enum<*>>) = values.map { it.ordinal }.toMutableList()

        private fun toStringCollection(values: MutableCollection<Enum<*>>) = values.map { it.name }.toMutableList()

        internal fun assign(entity: Entity, dbContext: DbContext, portableEntity: PortableEntity) {
            //==============================================
            //PRIMITIVES, also saved to array struct to streamline json
            //==============================================
            entity.booleanValues.filterIgnored(dbContext).forEach { entry ->
                val input = when (entry.value.value) {
                    true -> 1
                    false -> 0
                    else -> null
                }
                portableEntity.booleanValues.add(StoreBoolean(entry.key, input))
            }
            entity.stringValues.filterIgnored(dbContext).forEach { entry ->
                portableEntity.stringValues.add(StoreString(entry.key, entry.value.value))
            }
            entity.floatValues.filterIgnored(dbContext).forEach { entry ->
                portableEntity.floatValue.add(StoreFloat(entry.key, entry.value.value))
            }
            entity.doubleValues.filterIgnored(dbContext).forEach { entry ->
                portableEntity.doubleValues.add(StoreDouble(entry.key, entry.value.value))
            }
            entity.shortValues.filterIgnored(dbContext).forEach { entry ->
                portableEntity.shortValues.add(StoreShort(entry.key, entry.value.value))
            }
            entity.longValues.filterIgnored(dbContext).forEach { entry ->
                portableEntity.longValues.add(StoreLong(entry.key, entry.value.value))
            }
            entity.integerValues.filterIgnored(dbContext).forEach { entry ->
                portableEntity.integerValues.add(StoreInteger(entry.key, entry.value.value))
            }
            entity.uuidValues.filterIgnored(dbContext).forEach { entry ->
                portableEntity.uuidValues.add(StoreUuid(entry.key, entry.value.value.toByteArray()))
            }
            //==============================================
            //Dates & Time
            //==============================================
            entity.zonedDateTimeValues.filterIgnored(dbContext).forEach { entry ->
                val zonedDateTime = entry.value.value as ZonedDateTime?
                portableEntity.zonedDateTimeValues.add(StoreZonedDateTime(entry.key, zonedDateTime?.toInstant()?.toEpochMilli()))
            }
            entity.localTimeValues.filterIgnored(dbContext).forEach { entry ->
                val localTime = entry.value.value as LocalTime?
                portableEntity.timeValues.add(StoreTime(entry.key, localTime?.toNanoOfDay()))
            }
            entity.durationValues.filterIgnored(dbContext).forEach { entry ->
                val duration = entry.value.value
                portableEntity.durationValues.add(StoreDuration(entry.key, duration?.toNanos()))
            }
            entity.localDateTimeValues.filterIgnored(dbContext).forEach { entry ->
                val localDateTime = entry.value.value as LocalDateTime?
                portableEntity.dateTimeValues.add(StoreDateTime(entry.key, localDateTime?.toInstant(ZoneOffset.UTC)?.toEpochMilli()))
            }
            entity.localDateValues.filterIgnored(dbContext).forEach { entry ->
                val localDate = entry.value.value as LocalDate?
                portableEntity.dateValues.add(StoreDate(entry.key, localDate?.toEpochDay()))
            }
            entity.monthDayValues.filterIgnored(dbContext).forEach { entry ->
                portableEntity.monthDayValues.add(StoreMonthDay(entry.key, entry.value.value?.toString()))
            }
            entity.yearMonthValues.filterIgnored(dbContext).forEach { entry ->
                portableEntity.yearMonthValues.add(StoreYearMonth(entry.key, entry.value.value?.toString()))
            }
            entity.periodValues.filterIgnored(dbContext).forEach { entry ->
                portableEntity.periodValues.add(StorePeriod(entry.key, entry.value.value?.toString()))
            }
            //==============================================
            //BLOB
            //==============================================
            entity.blobValues.filterIgnored(dbContext).forEach { entry ->
                portableEntity.blobValues.add(StoreBlob(entry.key, entry.value.value ?: ByteArray(0)))
            }
            //==============================================
            //Enums
            //==============================================
            entity.enumValues.filterIgnored(dbContext).forEach { entry ->
                portableEntity.enumValues.add(StoreEnum(entry.key, entry.value.value?.ordinal))
            }
            entity.stringEnumValues.filterIgnored(dbContext).forEach { entry ->
                portableEntity.enumStringValues.add(StoreEnumString(entry.key, entry.value.value?.name))
            }
            entity.enumCollections.filterIgnored(dbContext).forEach { entry ->
                portableEntity.enumCollections.add(StoreEnumCollection(entry.key, toIntCollection(entry.value)))
            }
            entity.enumStringCollections.filterIgnored(dbContext).forEach { entry ->
                portableEntity.enumStringCollections.add(StoreEnumStringCollection(entry.key, toStringCollection(entry.value)))
            }
            //==============================================
            //ARRAYS
            //==============================================
            entity.stringCollections.filterIgnored(dbContext).forEach { entry ->
                portableEntity.stringCollections.add(StoreStringCollection(entry.key, entry.value))
            }
            entity.dateTimeCollections.filterIgnored(dbContext).forEach { entry ->
                portableEntity.dateTimeCollection.add(StoreDateTimeCollection(entry.key, toTimeStampCollection(entry.value)))
            }
            entity.floatCollections.filterIgnored(dbContext).forEach { entry ->
                portableEntity.floatCollections.add(StoreFloatCollection(entry.key, entry.value))
            }
            entity.doubleCollections.filterIgnored(dbContext).forEach { entry ->
                portableEntity.doubleCollections.add(StoreDoubleCollection(entry.key, entry.value))
            }
            entity.longCollections.filterIgnored(dbContext).forEach { entry ->
                portableEntity.longCollections.add(StoreLongCollection(entry.key, entry.value))
            }
            entity.integerCollections.filterIgnored(dbContext).forEach { entry ->
                portableEntity.integerCollections.add(StoreIntegerCollection(entry.key, entry.value))
            }
            entity.shortCollections.filterIgnored(dbContext).forEach { entry ->
                portableEntity.shortCollections.add(StoreShortCollection(entry.key, entry.value))
            }
            entity.uuidCollections.filterIgnored(dbContext).forEach { entry ->
                portableEntity.uuidCollections.add(StoreUuidCollection(entry.key, toByteArrayCollection(entry.value)))
            }
            //==============================================
            // Maps
            //==============================================
            entity.mapIntKeyValues.filterIgnored(dbContext).forEach { entry ->
                portableEntity.mapIntKeyValues.add(StoreMapIntKey(entry.key, entry.value))
            }
            entity.mapStringKeyValues.filterIgnored(dbContext).forEach { entry ->
                portableEntity.mapStringKeyValues.add(StoreMapStringKey(entry.key, entry.value))
            }
            //==============================================
            //EMBEDDED OBJECTS
            //==============================================
            entity.objectCollections.forEach { (fieldEntity, mutableCollection) ->
                mutableCollection.forEach { entity ->
                    val embeddedObject = PortableEntity()
                    embeddedObject.fieldId = fieldEntity.field.id
                    embeddedObject.init(dbContext, entity)
                    portableEntity.entityOverviews.add(embeddedObject)
                }
            }
            entity.objectValues.forEach { (fieldEntity, objectWritableProperty) ->
                val embeddedPortableEntity = PortableEntity()
                embeddedPortableEntity.fieldId = fieldEntity.field.id
                embeddedPortableEntity.init(dbContext, objectWritableProperty.value)
                portableEntity.entityOverviews.add(embeddedPortableEntity)
            }
        }
    }
}