package io.github.subiyacryolite.jds.entities;

import io.github.subiyacryolite.jds.JdsEntity;
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation;
import io.github.subiyacryolite.jds.enums.SimpleAddressEnums;
import io.github.subiyacryolite.jds.fields.SimpleAddressFields;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.ZonedDateTime;
import java.util.List;

@JdsEntityAnnotation(entityId = 1, entityName = "Simple Address")
public class SimpleAddress extends JdsEntity {
    private final SimpleStringProperty streetName = new SimpleStringProperty("");
    private final SimpleIntegerProperty plotNumber = new SimpleIntegerProperty(0);
    private final SimpleStringProperty area = new SimpleStringProperty("");
    private final SimpleStringProperty city = new SimpleStringProperty("");
    private final SimpleStringProperty provinceOrState = new SimpleStringProperty("");
    private final SimpleStringProperty country = new SimpleStringProperty("");
    private final SimpleListProperty<String> primaryAddress = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final SimpleObjectProperty<ZonedDateTime> timeOfEntry = new SimpleObjectProperty(ZonedDateTime.now());

    public SimpleAddress() {
        map(SimpleAddressFields.STREET_NAME, streetName);
        map(SimpleAddressFields.PLOT_NUMBER, plotNumber);
        map(SimpleAddressFields.AREA_NAME, area);
        map(SimpleAddressFields.CITY_NAME, city);
        map(SimpleAddressFields.COUNTRY_NAME, country);
        map(SimpleAddressFields.PROVINCE_NAME, provinceOrState);
        map(SimpleAddressFields.ZONED_DATE_OF_REGISTRATION, timeOfEntry);
        mapEnums(SimpleAddressEnums.PRIMARY_ADDRESS_ENUM, primaryAddress);
    }

    public List<String> getPrimaryAddress() {
        return this.primaryAddress.get();
    }

    public void setPrimaryAddress(ObservableList<String> value) {
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
        return "SimpleAddress{" +
                "streetName=" + getStreetName() +
                ", plotNumber=" + getPlotNumber() +
                ", area=" + getArea() +
                ", city=" + getCity() +
                ", provinceOrState=" + getProvinceOrState() +
                ", country=" + getCountry() +
                ", timeOfEntry=" + getTimeOfEntry() +
                '}';
    }
}