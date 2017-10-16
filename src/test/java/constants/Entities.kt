package constants

import entities.Address
import io.github.subiyacryolite.jds.JdsFieldEntity

object Entities {
    val ADDRESS_FIELD: JdsFieldEntity<Address> = JdsFieldEntity(Address::class.java, Fields.ADDRESS_FIELD)
}
