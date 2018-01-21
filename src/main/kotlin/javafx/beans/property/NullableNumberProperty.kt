package javafx.beans.property

import javafx.beans.value.WritableValue
import java.io.Serializable

open class NullableNumberProperty<T : Number> : WritableValue<T>, Serializable {

    private var _value: T? = null

    override fun setValue(value: T?) {
        _value = value
    }

    override fun getValue(): T? = _value
}