package io.github.subiyacryolite.jds.beans.property

import javafx.beans.value.WritableValue
import java.io.Serializable
import java.time.LocalTime

data class NullableLocalTimeProperty(private var _value: LocalTime? = null) : WritableValue<LocalTime?>, Serializable {

    fun get(): LocalTime? = value

    fun set(v: LocalTime?) {
        value = v
    }

    override fun setValue(value: LocalTime?) {
        _value = value
    }

    override fun getValue(): LocalTime? {
        return _value
    }
}