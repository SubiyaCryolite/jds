package io.github.subiyacryolite.jds.beans.property

import java.io.Serializable

interface WritableProperty<T> : Serializable {
    var value: T
}