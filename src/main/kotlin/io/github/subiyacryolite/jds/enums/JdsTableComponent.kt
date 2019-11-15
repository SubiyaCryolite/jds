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
package io.github.subiyacryolite.jds.enums;

/**
 * This enum contains all the components that make up the JDS framework on a target database.
 * Each enum contains the components name as well as an optional alias primarily used to create
 * joins that implement search [io.github.subiyacryolite.jds.JdsFilter].
 * @param component the component name
 * @param alias the component alias
 */
enum class JdsTableComponent(val component: String, val alias: String) {
    /**
     *
     */
    EntityBinding("jds_entity_binding", "eb"),

    /**
     *
     */
    EntityLiveVersion("jds_entity_live_version","elv"),

    /**
     *
     */
    EntityOverview("jds_entity_overview", "entityOverviews"),

    /**
     *
     */
    RefEntities("jds_ref_entity", "et"),

    /**
     *
     */
    RefEntityEnums("jds_ref_entity_enum", "bee"),

    /**
     *
     */
    RefEntityField("jds_ref_entity_field", "bef"),

    /**
     *
     */
    RefEnumValues("jds_ref_enum", "env"),

    /**
     *
     */
    RefFieldTypes("jds_ref_field_type", "ft"),

    /**
     *
     */
    RedFields("jds_ref_field", "fd"),

    /**
     *
     */
    RefInheritance("jds_ref_entity_inheritance", "ri"),

    /**
     *
     */
    StoreBlob("jds_str_blob", "sb"),

    /**
     *
     */
    StoreBoolean("jds_str_boolean", "sbn"),

    /**
     *
     */
    StoreDate("jds_str_date", "sdd"),

    /**
     *
     */
    StoreDateTime("jds_str_date_time", "sdt"),

    /**
     *
     */
    StoreDateTimeCollection("jds_str_date_time_col", "sdt"),

    /**
     *
     */
    StoreDouble("jds_str_double", "sd"),

    /**
     *
     */
    StoreDoubleCollection("jds_str_double_col", "sdc"),

    /**
     *
     */
    StoreDuration("jds_str_duration", "sd"),

    /**
     *
     */
    StoreEnum("jds_str_enum", "se"),

    /**
     *
     */
    StoreEnumString("jds_str_enum_string", "ses"),

    /**
     *
     */
    StoreEnumCollection("jds_str_enum_col", "sec"),

    /**
     *
     */
    StoreEnumStringCollection("jds_str_enum_string_col", "sesc"),

    /**
     *
     */
    StoreShort("jds_str_short", "sst"),

    /**
     *
     */
    StoreFloat("jds_str_float", "sf"),

    /**
     *
     */
    StoreUuid("jds_str_uuid", "su"),

    /**
     *
     */
    StoreFloatCollection("jds_str_float_col", "sfc"),

    /**
     *
     */
    StoreInteger("jds_str_integer", "si"),

    /**
     *
     */
    StoreIntegerCollection("jds_str_integer_col", "sic"),
    /**
     *
     */
    StoreLong("jds_str_long", "sl"),

    /**
     *
     */
    StoreLongCollection("jds_str_long_col", "slc"),

    /**
     *
     */
    StoreMonthDay("jds_str_month_day", "monthDayValues"),

    /**
     *
     */
    StorePeriod("jds_str_period", "sp"),
    /**
     *
     */
    /**
     *
     */
    StoreText("jds_str_text", "st"),

    /**
     *
     */
    StoreTextCollection("jds_str_text_col", "stc"),

    /**
     *
     */
    StoreTime("jds_str_time", "stt"),

    /**
     *
     */
    StoreYearMonth("jds_str_year_month", "yearMonthValues"),

    /**
     *
     */
    StoreZonedDateTime("jds_str_zoned_date_time", "szdt");

    override fun toString(): String {
        return name
    }
}
