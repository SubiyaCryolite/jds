package io.github.subiyacryolite.jds.tests.entities

import io.github.subiyacryolite.jds.JdsEntity
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation
import io.github.subiyacryolite.jds.beans.property.NullableShortProperty
import io.github.subiyacryolite.jds.tests.constants.Enums
import io.github.subiyacryolite.jds.tests.constants.Fields
import io.github.subiyacryolite.jds.tests.constants.PrimaryAddress
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import java.time.LocalTime
import java.util.*

@JdsEntityAnnotation(id = 1, name = "address")
data class Address(
        private val _streetName: StringProperty = SimpleStringProperty(""),
        private val _plotNumber: NullableShortProperty = NullableShortProperty(),
        private val _area: StringProperty = SimpleStringProperty(""),
        private val _city: StringProperty = SimpleStringProperty(""),
        private val _provinceOrState: StringProperty = SimpleStringProperty(""),
        private val _country: StringProperty = SimpleStringProperty(""),
        private val _entryUuid: ObjectProperty<UUID> = SimpleObjectProperty(UUID.randomUUID()),
        private val _primaryAddress: ObjectProperty<PrimaryAddress?> = SimpleObjectProperty(null),
        private val _timeOfEntry: ObjectProperty<LocalTime> = SimpleObjectProperty(LocalTime.now())
) : JdsEntity() {

    init {
        map(Fields.ENTRY_UUID, _entryUuid)
        map(Fields.STREET_NAME, _streetName)
        map(Fields.PLOT_NUMBER, _plotNumber)
        map(Fields.AREA_NAME, _area)
        map(Fields.CITY_NAME, _city)
        map(Fields.COUNTRY_NAME, _country)
        map(Fields.PROVINCE_NAME, _provinceOrState)
        map(Fields.TIME_OF_ENTRY, _timeOfEntry)
        map(Enums.PRIMARY_ADDRESS_ENUM, _primaryAddress)
        map(Enums.PRIMARY_ADDRESS_ENUM_STRING, _primaryAddress)
    }

    var entryUuid: UUID
        get() = _entryUuid.get()
        set(value) = _entryUuid.set(value)

    var primaryAddress: PrimaryAddress?
        get() = _primaryAddress.get()
        set(value) = _primaryAddress.set(value)

    var streetName: String
        get() = _streetName.get()
        set(value) = _streetName.set(value)

    var plotNumber: Short?
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

    var timeOfEntry: LocalTime
        get() = _timeOfEntry.get()
        set(timeOfEntry) = _timeOfEntry.set(timeOfEntry)
}