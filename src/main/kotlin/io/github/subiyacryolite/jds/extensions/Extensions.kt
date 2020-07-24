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
package io.github.subiyacryolite.jds.extensions

import io.github.subiyacryolite.jds.Entity
import io.github.subiyacryolite.jds.Field
import io.github.subiyacryolite.jds.context.DbContext
import io.github.subiyacryolite.jds.interfaces.IEntity
import io.github.subiyacryolite.jds.interfaces.IValue
import java.nio.ByteBuffer
import java.time.temporal.Temporal
import java.util.*

fun ByteArray?.toUuid(): UUID? = if (this == null) {
    null
} else {
    val byteBuffer = ByteBuffer.wrap(this)
    UUID(byteBuffer.long, byteBuffer.long)
}

fun UUID?.toByteArray(): ByteArray? = if (this == null) {
    null
} else {
    val byteBuffer = ByteBuffer.wrap(ByteArray(16))
    byteBuffer.putLong(this.mostSignificantBits)
    byteBuffer.putLong(this.leastSignificantBits)
    byteBuffer.array()
}

/**
 * Extension method which filters the map of fields which are persisted to disk.
 * When sensitive data is not being saved, fields marked as sensitive will be excluded
 */
@JvmName("filterTemporalValue")
internal fun Map<Int, IValue<out Temporal?>>.filterIgnored(dbContext: DbContext): Map<Int, IValue<out Temporal?>> = if (dbContext.options.ignoreTags.isEmpty()) {
    this
} else {
    this.filter { kvp ->
        Field.values.getValue(kvp.key).tags.none { tag ->
            dbContext.options.ignoreTags.contains(tag)
        }
    }
}

/**
 * Extension method which filters the map of fields which are persisted to disk.
 * When sensitive data is not being saved, fields marked as sensitive will be excluded
 */
@JvmName("filterValue")
internal fun <T> Map<Int, IValue<T>>.filterIgnored(dbContext: DbContext): Map<Int, IValue<T>> = if (dbContext.options.ignoreTags.isEmpty()) {
    this
} else {
    this.filter { kvp ->
        Field.values.getValue(kvp.key).tags.none { tag ->
            dbContext.options.ignoreTags.contains(tag)
        }
    }
}

/**
 * Extension method which filters the map of fields which are persisted to disk.
 * When sensitive data is not being saved, fields marked as sensitive will be excluded
 */
internal fun Map<Int, MutableCollection<out Enum<*>>>.filterIgnoredEnums(dbContext: DbContext): Map<Int, MutableCollection<out Enum<*>>> = if (dbContext.options.ignoreTags.isEmpty()) {
    this
} else {
    this.filter { kvp ->
        Field.values.getValue(kvp.key).tags.none { tag ->
            dbContext.options.ignoreTags.contains(tag)
        }
    }
}

/**
 * Extension method which filters the map of fields which are persisted to disk.
 * When sensitive data is not being saved, fields marked as sensitive will be excluded
 */
@JvmName("filterCollection")
internal fun <T> Map<Int, MutableCollection<T>>.filterIgnored(dbContext: DbContext): Map<Int, MutableCollection<T>> = if (dbContext.options.ignoreTags.isEmpty()) {
    this
} else {
    this.filter { kvp ->
        Field.values.getValue(kvp.key).tags.none { tag ->
            dbContext.options.ignoreTags.contains(tag)
        }
    }
}

@JvmName("filterIntMap")
internal fun Map<Int, MutableMap<Int, String>>.filterIgnored(dbContext: DbContext): Map<Int, MutableMap<Int, String>> = if (dbContext.options.ignoreTags.isEmpty()) {
    this
} else {
    this.filter { kvp ->
        Field.values.getValue(kvp.key).tags.none { tag ->
            dbContext.options.ignoreTags.contains(tag)
        }
    }
}

@JvmName("filterStringMap")
internal fun Map<Int, MutableMap<String, String>>.filterIgnored(dbContext: DbContext): Map<Int, MutableMap<String, String>> = if (dbContext.options.ignoreTags.isEmpty()) {
    this
} else {
    this.filter { kvp ->
        Field.values.getValue(kvp.key).tags.none { tag ->
            dbContext.options.ignoreTags.contains(tag)
        }
    }
}

object Extensions {

    /**
     * @param entity
     * @param parentEntities
     */
    fun determineParents(entity: Class<out IEntity>, parentEntities: MutableCollection<Int>) {
        addAllToList(entity.superclass, parentEntities)
    }

    /**
     * @param superclass
     * @param parentEntities
     */
    private fun addAllToList(superclass: Class<*>?, parentEntities: MutableCollection<Int>) {
        if (superclass == null) return
        val annotation = Entity.getEntityAnnotation(superclass)
        if (annotation != null) {
            parentEntities.add(annotation.id)
            addAllToList(superclass.superclass, parentEntities)
        }
    }
}