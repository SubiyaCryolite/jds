package entities

import io.github.subiyacryolite.jds.JdsEntity
import io.github.subiyacryolite.jds.JdsField
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation
import io.github.subiyacryolite.jds.enums.JdsFieldType
import javafx.beans.property.SimpleStringProperty

/**
 * Created by ifunga on 01/07/2017.
 */
@JdsEntityAnnotation(id = 1000, name = "entitya")
open class EntityA : JdsEntity() {
    private val _field = SimpleStringProperty("A")

    init {
        map(ENTITY_A_FIELD, _field)
    }

    var entityAValue: String
        get() = _field.get()
        set(value) = _field.set(value)

    override fun toString(): String {
        return "{ EntityA { FieldA = $entityAValue } }"
    }

    companion object {
        private val ENTITY_A_FIELD = JdsField(5002, "entity_a_field", JdsFieldType.STRING)
    }
}
