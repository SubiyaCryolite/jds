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
 * joins that implement search {@link io.github.subiyacryolite.jds.JdsFilter filter's}.
 * @param component the component name
 * @param alias the component alias
 */
enum class JdsComponent(val component: String, val alias: String) {
    NULL("Null", "null"),
    ENTITY_BINDING("jds_entity_binding", "eb"),
    ENTITY_LIVE_VERSION("jds_entity_live_version","elv"),
    ENTITY_OVERVIEW("jds_entity_overview", "entityOverviews"),
    POP_ENTITY_BINDING("jds_pop_entity_binding", ""),
    POP_ENTITY_LIVE_VERSION("jds_pop_entity_live_version",""),
    POP_ENTITY_OVERVIEW("jds_pop_entity_overview", ""),
    POP_REF_ENTITY("jds_pop_ref_entity", ""),
    POP_REF_ENTITY_ENUM("jds_pop_ref_entity_enum", ""),
    POP_REF_ENTITY_FIELD("jds_pop_ref_entity_field", ""),
    POP_REF_ENTITY_INHERITANCE("jds_pop_ref_entity_inheritance", ""),
    POP_REF_ENUM("jds_pop_ref_enum", ""),
    POP_REF_FIELD("jds_pop_ref_field", ""),
    POP_STORE_BLOB("jds_pop_blob", ""),
    POP_STORE_BOOLEAN("jds_pop_boolean", ""),
    POP_STORE_DATE("jds_pop_date", ""),
    POP_STORE_DATE_TIME("jds_pop_date_time", ""),
    POP_STORE_DATE_TIME_COLLECTION("jds_pop_date_time_collection", ""),
    POP_STORE_DOUBLE("jds_pop_double", ""),
    POP_STORE_DOUBLE_COLLECTION("jds_pop_double_collection", ""),
    POP_STORE_DURATION("jds_pop_duration", ""),
    POP_STORE_ENUM("jds_pop_enum", ""),
    POP_STORE_ENUM_COLLECTION("jds_pop_enum_collection", ""),
    POP_STORE_FLOAT("jds_pop_float", ""),
    POP_STORE_FLOAT_COLLECTION("jds_pop_float_collection", ""),
    POP_STORE_INTEGER("jds_pop_integer", ""),
    POP_STORE_INTEGER_COLLECTION("jds_pop_integer_collection", ""),
    POP_STORE_LONG("jds_pop_long", ""),
    POP_STORE_LONG_COLLECTION("jds_pop_long_collection", ""),
    POP_STORE_MONTH_DAY("jds_pop_month_day", ""),
    POP_STORE_MONTH_YEAR("jds_pop_month_year", ""),
    POP_STORE_PERIOD("jds_pop_period", ""),
    POP_STORE_TEXT("jds_pop_text", ""),
    POP_STORE_TEXT_COLLECTION("jds_pop_text_collection", ""),
    POP_STORE_TIME("jds_pop_time", ""),
    POP_STORE_YEAR_MONTH("jds_pop_year_month", ""),
    POP_STORE_ZONED_DATE_TIME("jds_pop_zoned_date_time", ""),
    REF_ENTITIES("jds_ref_entity", "et"),
    REF_ENTITY_ENUMS("jds_ref_entity_enum", "bee"),
    REF_ENTITY_FIELD("jds_ref_entity_field", "bef"),
    REF_ENUM_VALUES("jds_ref_enum", "env"),
    REF_FIELD_TYPES("jds_ref_field_type", "ft"),
    REF_FIELDS("jds_ref_field", "fd"),
    REF_INHERITANCE("jds_ref_entity_inheritance", "ri"),
    STORE_BLOB("jds_str_blob", "sb"),
    STORE_BOOLEAN("jds_str_boolean", "sbn"),
    STORE_DATE("jds_str_date", "sdd"),
    STORE_DATE_TIME("jds_str_date_time", "sdt"),
    STORE_DATE_TIME_COLLECTION("jds_str_date_time_collection", "sdt"),
    STORE_DOUBLE("jds_str_double", "sd"),
    STORE_DOUBLE_COLLECTION("jds_str_double_collection", "sdc"),
    STORE_DURATION("jds_str_duration", "sd"),
    STORE_ENUM("jds_str_enum", "se"),
    STORE_ENUM_COLLECTION("jds_str_enum_collection", "sec"),
    STORE_FLOAT("jds_str_float", "sf"),
    STORE_FLOAT_COLLECTION("jds_str_float_collection", "sfc"),
    STORE_INTEGER("jds_str_integer", "si"),
    STORE_INTEGER_COLLECTION("jds_str_integer_collection", "sic"),
    STORE_LONG("jds_str_long", "sl"),
    STORE_LONG_COLLECTION("jds_str_long_collection", "slc"),
    STORE_MONTH_DAY("jds_str_month_day", "monthDayValues"),
    STORE_PERIOD("jds_str_period", "sp"),
    STORE_TEXT("jds_str_text", "st"),
    STORE_TEXT_COLLECTION("jds_str_text_collection", "stc"),
    STORE_TIME("jds_str_time", "stt"),
    STORE_YEAR_MONTH("jds_str_year_month", "yearMonthValues"),
    STORE_ZONED_DATE_TIME("jds_str_zoned_date_time", "szdt"),
    ;

    override fun toString(): String {
        return name
    }
}
