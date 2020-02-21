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
package io.github.subiyacryolite.jds.enums

/**
 * @param component the component name
 */
enum class ProcedureComponent(val component: String) {
    /**
     *
     */
    PopEntityBinding("pop_entity_binding"),

    /**
     *
     */
    PopEntityLiveVersion("pop_entity_live_version"),

    /**
     *
     */
    PopEntityOverview("pop_entity_overview"),

    /**
     *
     */
    PopRefEntity("pop_ref_entity"),

    /**
     *
     */
    PopRefEntityEnum("pop_ref_entity_enum"),

    /**
     *
     */
    PopRefEntityField("pop_ref_entity_field"),

    /**
     *
     */
    PopRefFieldEntity("pop_ref_field_entity"),

    /**
     *
     */
    PopRefEntityInheritance("pop_ref_entity_inheritance"),

    /**
     *
     */
    PopRefEnum("pop_ref_enum"),

    /**
     *
     */
    PopRefField("pop_ref_field"),

    /**
     *
     */
    PopStoreBlob("pop_blob"),

    /**
     *
     */
    PopStoreBoolean("pop_boolean"),

    /**
     *
     */
    PopStoreDate("pop_date"),

    /**
     *
     */
    PopStoreDateTime("pop_date_time"),

    /**
     *
     */
    PopStoreDouble("pop_double"),

    /**
     *
     */
    PopStoreDoubleCollection("pop_double_col"),

    /**
     *
     */
    PopStoreDuration("pop_duration"),

    /**
     *
     */
    PopStoreEnum("pop_enum"),

    /**
     *
     */
    PopStoreEnumString("pop_enum_string"),

    /**
     *
     */
    PopStoreEnumCollection("pop_enum_col"),

    /**
     *
     */
    PopStoreEnumStringCollection("pop_enum_string_col"),

    /**
     *
     */
    PopStoreFloat("pop_float"),

    /**
     *
     */
    PopStoreShort("pop_short"),

    /**
     *
     */
    PopStoreUuid("pop_uuid"),

    /**
     *
     */
    PopStoreFloatCollection("pop_float_col"),

    /**
     *
     */
    PopStoreInteger("pop_integer"),

    /**
     *
     */
    PopStoreIntegerCollection("pop_integer_col"),

    /**
     *
     */
    PopStoreLong("pop_long"),

    /**
     *
     */
    PopStoreLongCollection("pop_long_col"),

    /**
     *
     */
    PopStoreMonthDay("pop_month_day"),

    /**
     *
     */
    PopStoreMonthYear("pop_month_year"),

    /**
     *
     */
    PopStorePeriod("pop_period"),

    /**
     *
     */
    PopStoreText("pop_text"),

    /**
     *
     */
    PopStoreTextCollection("pop_text_col"),

    /**
     *
     */
    PopStoreTime("pop_time"),

    /**
     *
     */
    PopStoreYearMonth("pop_year_month"),

    /**
     *
     */
    PopStoreZonedDateTime("pop_zoned_date_time")
}