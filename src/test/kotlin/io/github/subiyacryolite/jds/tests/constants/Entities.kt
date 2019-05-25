package io.github.subiyacryolite.jds.tests.constants

import io.github.subiyacryolite.jds.tests.entities.Address
import io.github.subiyacryolite.jds.JdsFieldEntity

object Entities {
    val ADDRESSES: JdsFieldEntity<Address> = JdsFieldEntity(Address::class.java, Fields.ADDRESSES)
}
