package io.github.subiyacryolite.jds.beans.property

import javafx.beans.value.WritableValue
import java.io.Serializable
import java.time.LocalDate

data class NullableLocalDateProperty(private var _value: LocalDate? = null) : WritableValue<LocalDate?>, Serializable {

    fun get(): LocalDate? = value

    fun set(v: LocalDate?) {
        value = v
    }

    override fun setValue(value: LocalDate?) {
        _value = value
    }

    override fun getValue(): LocalDate? {
        return _value
    }
}