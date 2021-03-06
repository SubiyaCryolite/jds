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
import io.github.subiyacryolite.jds.beans.property.LocalDateTimeValue
import io.github.subiyacryolite.jds.beans.property.NullableBooleanValue
import io.github.subiyacryolite.jds.beans.property.NullableShortValue
import io.github.subiyacryolite.jds.beans.property.StringValue
import io.github.subiyacryolite.jds.interfaces.IValue
import io.github.subiyacryolite.jds.tests.constants.Fields
import io.github.subiyacryolite.jds.tests.interfactes.IAddress
import java.time.LocalDateTime

data class Address(
        private val _streetName: IValue<String> = StringValue(),
        private val _plotNumber: IValue<Short?> = NullableShortValue(),
        private val _area: IValue<String> = StringValue(),
        private val _city: IValue<String> = StringValue(),
        private val _provinceOrState: IValue<String> = StringValue(),
        private val _country: IValue<String> = StringValue(),
        private val _primaryAddress: IValue<Boolean?> = NullableBooleanValue(),
        private val _timestamp: IValue<LocalDateTime> = LocalDateTimeValue()
) : Entity(), IAddress {

    override fun bind() {
        super.bind()
        map(Fields.StreetName, _streetName, "streetName")
        map(Fields.PlotNumber, _plotNumber, "plotNumber")
        map(Fields.ResidentialArea, _area, "area")
        map(Fields.City, _city, "city")
        map(Fields.ProvinceOrState, _provinceOrState, "provinceOrState")
        map(Fields.Country, _country, "country")
        map(Fields.PrimaryAddress, _primaryAddress, "primaryAddress")
        map(Fields.TimeStamp, _timestamp, "timestamp")
    }

    override var primaryAddress: Boolean?
        get() = _primaryAddress.get()
        set(value) = _primaryAddress.set(value)

    override var streetName: String
        get() = _streetName.get()
        set(value) = _streetName.set(value)

    override var plotNumber: Short?
        get() = _plotNumber.get()
        set(value) = _plotNumber.set(value)

    override var area: String
        get() = _area.get()
        set(value) = _area.set(value)

    override var city: String
        get() = _city.get()
        set(value) = _city.set(value)

    override var provinceOrState: String
        get() = _provinceOrState.get()
        set(value) = _provinceOrState.set(value)

    override var country: String
        get() = _country.get()
        set(value) = _country.set(value)

    override var timeOfEntry: LocalDateTime
        get() = _timestamp.get()
        set(timeOfEntry) = _timestamp.set(timeOfEntry)
}