package io.github.subiyacryolite.jds.embedded

import io.github.subiyacryolite.jds.JdsEntity
import java.util.*
import java.util.concurrent.Callable

class JdsSaveEmbedded(private val entities: Collection<JdsEntity>) : Callable<JdsEmbeddedContainer> {

    constructor(entity: JdsEntity) : this(Arrays.asList(entity))

    override fun call(): JdsEmbeddedContainer {
        return JdsEmbeddedContainer(entities)
    }
}