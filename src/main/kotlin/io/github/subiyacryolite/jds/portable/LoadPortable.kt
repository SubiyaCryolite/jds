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
        Entity.populate(entity, dbContext, portableEntity)
    }
}