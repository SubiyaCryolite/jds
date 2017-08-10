package entities;

import io.github.subiyacryolite.jds.JdsEntity;
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation;
import enums.PrimaryAddress;
import enums.Enums;
import fields.Fields;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.ZonedDateTime;

@JdsEntityAnnotation(entityId = 1, entityName = "address")
public class Address extends JdsEntity {
    private final SimpleStringProperty streetName = new SimpleStringProperty("");
    private final SimpleIntegerProperty plotNumber = new SimpleIntegerProperty(0);
    private final SimpleStringProperty area = new SimpleStringProperty("");
    private final SimpleStringProperty city = new SimpleStringProperty("");
    private final SimpleStringProperty provinceOrState = new SimpleStringProperty("");
    private final SimpleStringProperty country = new SimpleStringProperty("");
    private final SimpleObjectProperty<PrimaryAddress> primaryAddress = new SimpleObjectProperty<>(PrimaryAddress.NO);
    private final SimpleObjectProperty<ZonedDateTime> timeOfEntry = new SimpleObjectProperty(ZonedDateTime.now());

    public Address() {
        map(Fields.STREET_NAME, streetName);
        map(Fields.PLOT_NUMBER, plotNumber);
        map(Fields.AREA_NAME, area);
        map(Fields.CITY_NAME, city);
        map(Fields.COUNTRY_NAME, country);
        map(Fields.PROVINCE_NAME, provinceOrState);
        map(Fields.ZONED_DATE_OF_REGISTRATION, timeOfEntry);
        map(Enums.PRIMARY_ADDRESS_ENUM, primaryAddress);
    }

    public PrimaryAddress getPrimaryAddress() {
        return this.primaryAddress.get();
    }

    public void setPrimaryAddress(PrimaryAddress value) {
        this.primaryAddress.set(value);
    }

    public String getStreetName() {
        return this.streetName.get();
    }

    public void setStreetName(String value) {
        this.streetName.set(value);
    }

    public int getPlotNumber() {
        return this.plotNumber.get();
    }

    public void setPlotNumber(int value) {
        this.plotNumber.set(value);
    }

    public String getArea() {
        return this.area.get();
    }

    public void setArea(String value) {
        this.area.set(value);
    }

    public String getCity() {
        return this.city.get();
    }

    public void setCity(String value) {
        this.city.set(value);
    }

    public String getProvinceOrState() {
        return this.provinceOrState.get();
    }

    public void setProvinceOrState(String value) {
        this.provinceOrState.set(value);
    }

    public String getCountry() {
        return this.country.get();
    }

    public void setCountry(String value) {
        this.country.set(value);
    }

    public ZonedDateTime getTimeOfEntry() {
        return timeOfEntry.get();
    }

    public void setTimeOfEntry(ZonedDateTime timeOfEntry) {
        this.timeOfEntry.set(timeOfEntry);
    }

    @Override
    public String toString() {
        return "Address{" +
                "primaryAddress=" + getPrimaryAddress() +
                ", streetName=" + getStreetName() +
                ", plotNumber=" + getPlotNumber() +
                ", area=" + getArea() +
                ", city=" + getCity() +
                ", provinceOrState=" + getProvinceOrState() +
                ", country=" + getCountry() +
                ", timeOfEntry=" + getTimeOfEntry() +
                '}';
    }
}