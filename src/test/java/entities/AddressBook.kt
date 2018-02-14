package entities

import constants.Entities
import io.github.subiyacryolite.jds.JdsEntity
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections

@JdsEntityAnnotation(id = 2, name = "address_book")
class AddressBook : JdsEntity() {
    private val _addresses: SimpleListProperty<Address> = SimpleListProperty(FXCollections.observableArrayList())

    init {
        map(Entities.ADDRESS_FIELD, _addresses)
    }

    val addresses: MutableList<Address>
        get() = _addresses.get()

    override fun toString(): String = "{ addresses = $addresses }"
}