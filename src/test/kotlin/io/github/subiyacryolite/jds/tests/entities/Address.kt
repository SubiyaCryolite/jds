/**
 * Jenesis Data Store Copyright (c) 2017 Ifunga Ndana. All rights reserved.
 *
 * 1. Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 2. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 3. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * Neither the name Jenesis Data Store nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package io.github.subiyacryolite.jds.tests.entities

import io.github.subiyacryolite.jds.Entity
import io.github.subiyacryolite.jds.annotations.EntityAnnotation
import io.github.subiyacryolite.jds.beans.property.NullableBooleanProperty
import io.github.subiyacryolite.jds.beans.property.NullableShortProperty
import io.github.subiyacryolite.jds.tests.constants.Fields
import java.time.LocalDateTime

@EntityAnnotation(id = 1, name = "address", description = "An entity representing address information")
class Address : Entity() {

    private val _streetName = map(Fields.StreetName, "","streetName")
    private val _plotNumber = map(Fields.PlotNumber, NullableShortProperty(),"plotNumber")
    private val _area = map(Fields.ResidentialArea, "area")
    private val _city = map(Fields.City, "city")
    private val _provinceOrState = map(Fields.ProvinceOrState, "provinceOrState")
    private val _country = map(Fields.Country, "country")
    private val _primaryAddress = map(Fields.PrimaryAddress, NullableBooleanProperty(),"primaryAddress")
    private val _timestamp = map(Fields.TimeStamp, LocalDateTime.now(),"timestamp")

    var primaryAddress: Boolean?
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

    var timeOfEntry: LocalDateTime
        get() = _timestamp.get()
        set(timeOfEntry) = _timestamp.set(timeOfEntry)
}