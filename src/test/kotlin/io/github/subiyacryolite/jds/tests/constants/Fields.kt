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
package io.github.subiyacryolite.jds.tests.constants

import io.github.subiyacryolite.jds.Field
import io.github.subiyacryolite.jds.enums.FieldType

object Fields {
    val StreetName = Field(1, "street_name", FieldType.String, description = "The street name of the address", tags = setOf("AddressInfo", "ClientInfo", "IdentifiableInfo"))
    val PlotNumber = Field(2, "plot_number", FieldType.Short, "The street name of the address")
    val ResidentialArea = Field(3, "area_name", FieldType.String, "The name of the area / neighbourhood")
    val ProvinceOrState = Field(4, "province_or_state", FieldType.String)
    val City = Field(5, "city", FieldType.String)
    val Country = Field(7, "country", FieldType.String)
    val PrimaryAddress = Field(8, "primary_address", FieldType.Boolean)
    val TimeStamp = Field(9, "timestamp", FieldType.DateTime)
    val Direction = Field(10, "direction", FieldType.Enum)
    val String = Field(12, "string_field", FieldType.String)
    val Time = Field(13, "time_field", FieldType.Time)
    val Date = Field(14, "date_field", FieldType.Date)
    val DateTime = Field(15, "date_time_field", FieldType.DateTime)
    val ZonedDateTime = Field(16, "zoned_date_time_field", FieldType.ZonedDateTime)
    val Long = Field(17, "long_field", FieldType.Long)
    val Int = Field(18, "int_field", FieldType.Int)
    val Double = Field(19, "double_field", FieldType.Double)
    val Float = Field(20, "float_field", FieldType.Float)
    val Boolean = Field(21, "boolean_field", FieldType.Boolean)
    val Blob = Field(22, "blob_field", FieldType.Blob)
    val Addresses = Field(23, "addresses", FieldType.EntityCollection)
    val Period = Field(24, "period_field", FieldType.Period)
    val Duration = Field(25, "duration_field", FieldType.Duration)
    val MonthDay = Field(26, "month_day_field", FieldType.MonthDay)
    val YearMonth = Field(27, "year_month_field", FieldType.YearMonth)
    val Right = Field(28, "rights", FieldType.EnumCollection)
}
