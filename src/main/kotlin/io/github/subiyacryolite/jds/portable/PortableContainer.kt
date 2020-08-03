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

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.subiyacryolite.jds.Entity
import io.github.subiyacryolite.jds.annotations.EntityAnnotation
import io.github.subiyacryolite.jds.context.DbContext
import java.util.*

/**
 * @param portableEntities
 */
data class PortableContainer(
        @get:JsonProperty("e")
        val portableEntities: MutableCollection<PortableEntity> = ArrayList()
) {
    /**
     * @param dbContext an instance of [DbContext]
     * @param entities a collection of [Entity] objects to store in the embedded format
     */
    @Throws(Exception::class)
    constructor(dbContext: DbContext, entities: Iterable<Entity>) : this() {
        entities.forEach { entity ->
            val validClass = Entity.getEntityAnnotation(entity.javaClass)
            if (validClass != null) {
                val portableEntity = PortableEntity()
                portableEntity.fieldId = null//top level entities will not have a field ID
                portableEntity.init(dbContext, entity)
                portableEntities.add(portableEntity)
            } else {
                throw RuntimeException("You must annotate the class [${entity.javaClass.canonicalName}] or its parent with [${EntityAnnotation::class.java}]")
            }
        }
    }
}