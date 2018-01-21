package javafx.beans.property

import javafx.beans.value.WritableValue
import java.io.Serializable

class NullableBooleanProperty : WritableValue<Boolean>, Serializable {

    private var _value: Boolean? = null

    override fun setValue(value: Boolean?) {
        _value = value
    }

    override fun getValue(): Boolean? = _value
}