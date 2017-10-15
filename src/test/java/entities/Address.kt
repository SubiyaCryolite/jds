package entities

import constants.Enums
import constants.Fields
import constants.PrimaryAddress
import io.github.subiyacryolite.jds.JdsEntity
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import java.time.ZonedDateTime

@JdsEntityAnnotation(entityId = 1, entityName = "address")
class Address : JdsEntity() {
    private val _streetName = SimpleStringProperty("")
    private val _plotNumber = SimpleIntegerProperty(0)
    private val _area = SimpleStringProperty("")
    private val _city = SimpleStringProperty("")
    private val _provinceOrState = SimpleStringProperty("")
    private val _country = SimpleStringProperty("")
    private val _primaryAddress = SimpleObjectProperty(PrimaryAddress.NO)
    private val _timeOfEntry = SimpleObjectProperty(ZonedDateTime.now())

    init {
        map(Fields.STREET_NAME, _streetName)
        map(Fields.PLOT_NUMBER, _plotNumber)
        map(Fields.AREA_NAME, _area)
        map(Fields.CITY_NAME, _city)
        map(Fields.COUNTRY_NAME, _country)
        map(Fields.PROVINCE_NAME, _provinceOrState)
        map(Fields.ZONED_DATE_OF_REGISTRATION, _timeOfEntry)
        map(Enums.PRIMARY_ADDRESS_ENUM, _primaryAddress)
    }

    var primaryAddress: PrimaryAddress
        get() = _primaryAddress.get()
        set(value) = _primaryAddress.set(value)

    var streetName: String
        get() = _streetName.get()
        set(value) = _streetName.set(value)

    var plotNumber: Int
        get() = _plotNumber.get()
        set(value) = _plotNumber.set(value)

    var area: String
        get() = _area.get()
        set(value) = _area.set(value)

    var city: String
        get() = _city.get()
        set(value) = _city.set(value)

    var provinceOrState: String
        get() = _provinceOrState.get()
        set(value) = _provinceOrState.set(value)

    var country: String
        get() = _country.get()
        set(value) = _country.set(value)

    var timeOfEntry: ZonedDateTime
        get() = _timeOfEntry.get()
        set(timeOfEntry) = _timeOfEntry.set(timeOfEntry)

    override fun toString(): String {
        return "Address{" +
                "primaryAddress= $primaryAddress " +
                ", streetName= $streetName " +
                ", plotNumber= $plotNumber " +
                ", area= $area " +
                ", city= $city " +
                ", provinceOrState= $provinceOrState " +
                ", country= $country " +
                ", timeOfEntry= $timeOfEntry " +
                '}'
    }
}