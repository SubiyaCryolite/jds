package entities

import io.github.subiyacryolite.jds.JdsField
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation
import io.github.subiyacryolite.jds.enums.JdsFieldType
import javafx.beans.property.SimpleStringProperty

/**
 * Created by ifunga on 01/07/2017.
 */
@JdsEntityAnnotation(id = 1001, name = "entityb")
open class EntityB : EntityA() {
    private val _field = SimpleStringProperty("C")

    init {
        map(ENTITY_B_FIELD, _field)
    }

    var entityBValue: String
        get() = _field.get()
        set(s) = _field.set(s)

    override fun toString(): String {
        return "{ EntityB { FieldA = $entityAValue, FieldB = $entityBValue } }"
    }

    companion object {
        private val ENTITY_B_FIELD = JdsField(5001, "entity_b_field", JdsFieldType.STRING)
    }
}
