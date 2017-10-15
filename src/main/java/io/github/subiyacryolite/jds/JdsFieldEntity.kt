package io.github.subiyacryolite.jds

import java.io.Externalizable
import java.io.IOException
import java.io.ObjectInput
import java.io.ObjectOutput

class JdsFieldEntity<T : IJdsEntity>() : Externalizable {
    lateinit var entityType: Class<out IJdsEntity>
        private set
    lateinit var fieldEntity: JdsField
        private set

    constructor(type: Class<T>) : this() {
        this.entityType = type
    }

    constructor(entityType: Class<T>, jdsField: JdsField) : this(entityType) {
        fieldEntity = jdsField
        this.entityType = entityType
    }

    override fun toString(): String {
        return "JdsFieldEntity{ fieldEntity=$fieldEntity, class= $entityType }"
    }

    @Throws(IOException::class)
    override fun writeExternal(out: ObjectOutput) {
        out.writeObject(entityType)
        out.writeObject(fieldEntity)
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    override fun readExternal(input: ObjectInput) {
        entityType = input.readObject() as Class<T>
        fieldEntity = input.readObject() as JdsField
    }
}
