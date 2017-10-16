package entities

import io.github.subiyacryolite.jds.JdsField
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation
import io.github.subiyacryolite.jds.enums.JdsFieldType
import javafx.beans.property.SimpleStringProperty

/**
 * Created by ifunga on 01/07/2017.
 */
@JdsEntityAnnotation(entityId = 1002, entityName = "entityc")
class EntityC : EntityB() {
    private val _field = SimpleStringProperty("C")

    init {
        map(ENTITY_C_FIELD, _field)
    }

    var entityCValue: String
        get() = _field.get()
        set(s) = _field.set(s)

    override fun toString(): String {
        return "{ field A = $entityAValue" +
                ", field B = $entityBValue" +
                ", field C = $entityCValue " +
                "}"
    }

    companion object {

        private val ENTITY_C_FIELD = JdsField(5000, "entity_c_field", JdsFieldType.TEXT)
    }
}
