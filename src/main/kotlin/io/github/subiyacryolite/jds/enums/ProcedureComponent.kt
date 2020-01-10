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
    PopEntityBinding("jds_pop_entity_binding"),

    /**
     *
     */
    PopEntityLiveVersion("jds_pop_entity_live_version"),

    /**
     *
     */
    PopEntityOverview("jds_pop_entity_overview"),

    /**
     *
     */
    PopRefEntity("jds_pop_ref_entity"),

    /**
     *
     */
    PopRefEntityEnum("jds_pop_ref_entity_enum"),

    /**
     *
     */
    PopRefEntityField("jds_pop_ref_entity_field"),

    /**
     *
     */
    PopRefFieldEntity("jds_pop_ref_field_entity"),

    /**
     *
     */
    PopRefEntityInheritance("jds_pop_ref_entity_inheritance"),

    /**
     *
     */
    PopRefEnum("jds_pop_ref_enum"),

    /**
     *
     */
    PopRefField("jds_pop_ref_field"),

    /**
     *
     */
    PopStoreBlob("jds_pop_blob"),

    /**
     *
     */
    PopStoreBoolean("jds_pop_boolean"),

    /**
     *
     */
    PopStoreDate("jds_pop_date"),

    /**
     *
     */
    PopStoreDateTime("jds_pop_date_time"),

    /**
     *
     */
    PopStoreDouble("jds_pop_double"),

    /**
     *
     */
    PopStoreDoubleCollection("jds_pop_double_col"),

    /**
     *
     */
    PopStoreDuration("jds_pop_duration"),

    /**
     *
     */
    PopStoreEnum("jds_pop_enum"),

    /**
     *
     */
    PopStoreEnumString("jds_pop_enum_string"),

    /**
     *
     */
    PopStoreEnumCollection("jds_pop_enum_col"),

    /**
     *
     */
    PopStoreEnumStringCollection("jds_pop_enum_string_col"),

    /**
     *
     */
    PopStoreFloat("jds_pop_float"),

    /**
     *
     */
    PopStoreShort("jds_pop_short"),

    /**
     *
     */
    PopStoreUuid("jds_pop_uuid"),

    /**
     *
     */
    PopStoreFloatCollection("jds_pop_float_col"),

    /**
     *
     */
    PopStoreInteger("jds_pop_integer"),

    /**
     *
     */
    PopStoreIntegerCollection("jds_pop_integer_col"),

    /**
     *
     */
    PopStoreLong("jds_pop_long"),

    /**
     *
     */
    PopStoreLongCollection("jds_pop_long_col"),

    /**
     *
     */
    PopStoreMonthDay("jds_pop_month_day"),

    /**
     *
     */
    PopStoreMonthYear("jds_pop_month_year"),

    /**
     *
     */
    PopStorePeriod("jds_pop_period"),

    /**
     *
     */
    PopStoreText("jds_pop_text"),

    /**
     *
     */
    PopStoreTextCollection("jds_pop_text_col"),

    /**
     *
     */
    PopStoreTime("jds_pop_time"),

    /**
     *
     */
    PopStoreYearMonth("jds_pop_year_month"),

    /**
     *
     */
    PopStoreZonedDateTime("jds_pop_zoned_date_time")
}