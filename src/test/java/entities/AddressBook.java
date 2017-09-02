package entities;

import io.github.subiyacryolite.jds.JdsEntity;
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

import java.util.List;

@JdsEntityAnnotation(entityId = 2, entityName = "address_book")
public class AddressBook extends JdsEntity {
    private final SimpleListProperty<Address> addresses;

    public AddressBook() {
        this.addresses = new SimpleListProperty<>(FXCollections.observableArrayList());
        //map your objects
        map(Address.class, addresses);
    }

    public List<Address> getAddresses() {
        return this.addresses.get();
    }

    @Override
    public String toString() {
        return "AddressBook{" +
                "addresses = " + getAddresses() +
                '}';
    }
}