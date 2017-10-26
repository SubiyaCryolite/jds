package entities

import constants.Enums
import constants.Rights
import io.github.subiyacryolite.jds.JdsEntity
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections

@JdsEntityAnnotation(entityName = "Login", entityId = 7900)
class Login : JdsEntity() {

    private val _rights = SimpleListProperty<Rights>(FXCollections.observableArrayList())

    init {
        mapEnums(Enums.RIGHTS, _rights)
    }

    var rights: MutableList<Rights>
        get() = _rights.get()
        set(value) {
            _rights.clear()
            _rights.addAll(value)
        }
}