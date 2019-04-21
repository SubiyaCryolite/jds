package constants

import entities.Address
import io.github.subiyacryolite.jds.JdsFieldEntity

object Entities {
    val ADDRESSES: JdsFieldEntity<Address> = JdsFieldEntity(Address::class.java, Fields.ADDRESSES)
}
