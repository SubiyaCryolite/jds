package io.github.subiyacryolite.jds.events

import io.github.subiyacryolite.jds.JdsEntity
import java.sql.Connection

interface JdsSaveEvent {
    fun onSave(entities: Iterable<out JdsEntity>, connection: Connection)
}