package io.github.subiyacryolite.jds.beans.property

import javafx.beans.value.WritableValue
import java.io.Serializable
import java.time.LocalDateTime

data class NullableLocalDateTimeProperty(private var _value: LocalDateTime? = null) : WritableValue<LocalDateTime?>, Serializable {

    fun get(): LocalDateTime? = value

    fun set(v: LocalDateTime?) {
        value = v
    }

    override fun setValue(value: LocalDateTime?) {
        _value = value
    }

    override fun getValue(): LocalDateTime? {
        return _value
    }
}