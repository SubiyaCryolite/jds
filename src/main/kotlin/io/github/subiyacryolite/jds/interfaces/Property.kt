package io.github.subiyacryolite.jds.interfaces

import java.io.Serializable

interface Property<T> : Serializable {

    var value: T

    fun get(): T {
        return value
    }

    fun set(value: T) {
        this.value = value
    }
}