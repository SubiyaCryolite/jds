package io.github.subiyacryolite.jds.classes;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import io.github.subiyacryolite.jds.JdsEntity;
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation;

@JdsEntityAnnotation(entityId = 1, entityName = "Simple Address")
public class SimpleAddress extends JdsEntity {
    private final SimpleStringProperty streetName;
    private final SimpleIntegerProperty plotNumber;
    private final SimpleStringProperty area;
    private final SimpleStringProperty city;
    private final SimpleStringProperty provinceOrState;
    private final SimpleStringProperty country;

    public SimpleAddress() {
        this.streetName = new SimpleStringProperty("");
        this.plotNumber = new SimpleIntegerProperty(0);
        this.area = new SimpleStringProperty("");
        this.city = new SimpleStringProperty("");
        this.provinceOrState = new SimpleStringProperty("");
        this.country = new SimpleStringProperty("");
        //map your properties
        map(TestFields.STREET_NAME, streetName);
        map(TestFields.PLOT_NUMBER, plotNumber);
        map(TestFields.AREA_NAME, area);
        map(TestFields.CITY_NAME, city);
        map(TestFields.COUNTRY_NAME, country);
        map(TestFields.PROVINCE_NAME, provinceOrState);
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

    @Override
    public String toString() {
        return "SimpleAddress{" +
                "streetName=" + streetName.get() +
                ", plotNumber=" + plotNumber.get() +
                ", area=" + area.get() +
                ", city=" + city.get() +
                ", provinceOrState=" + provinceOrState.get() +
                ", country=" + country.get() +
                '}';
    }
}