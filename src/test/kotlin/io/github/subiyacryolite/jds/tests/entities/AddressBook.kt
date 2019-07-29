package io.github.subiyacryolite.jds.tests.entities

import io.github.subiyacryolite.jds.JdsEntity
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation
import io.github.subiyacryolite.jds.tests.constants.Entities

@JdsEntityAnnotation(id = 2, name = "address_book")
data class AddressBook(
        val addresses: MutableCollection<Address> = ArrayList()
) : JdsEntity() {
    
    init {
        map(Entities.ADDRESSES, addresses)
    }
}