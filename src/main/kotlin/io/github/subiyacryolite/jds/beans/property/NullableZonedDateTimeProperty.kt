package io.github.subiyacryolite.jds.beans.property

import javafx.beans.value.WritableValue
import java.io.Serializable
import java.time.ZonedDateTime

data class NullableZonedDateTimeProperty(private var _value: ZonedDateTime? = null) : WritableValue<ZonedDateTime?>, Serializable {

    fun get(): ZonedDateTime? = value

    fun set(v: ZonedDateTime?) {
        value = v
    }

    override fun setValue(value: ZonedDateTime?) {
        _value = value
    }

    override fun getValue(): ZonedDateTime? {
        return _value
    }
}