package entities;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import io.github.subiyacryolite.jds.JdsEntity;
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation;

import java.util.List;

@JdsEntityAnnotation(entityId = 2, entityName = "Simple Address Book")
public class SimpleAddressBook extends JdsEntity {
    private final SimpleListProperty<SimpleAddress> addresses;

    public SimpleAddressBook() {
        this.addresses = new SimpleListProperty<>(FXCollections.observableArrayList());
        //map your objects
        map(SimpleAddress.class, addresses);
    }

    public List<SimpleAddress> getAddresses() {
        return this.addresses.get();
    }

    @Override
    public String toString() {
        return "SimpleAddressBook{" +
                "addresses = " + getAddresses() +
                '}';
    }
}