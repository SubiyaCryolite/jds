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
 * joins that implement search [io.github.subiyacryolite.jds.Filter].
 * @param component the component name
 * @param alias the component alias
 */
enum class Table(val component: String, val alias: String) {

    /**
     *
     */
    FieldDictionary("field_dictionary", "f"),
    
    /**
     *
     */
    EntityBinding("entity_binding", "eb"),

    /**
     *
     */
    EntityLive("entity_live","elv"),

    /**
     *
     */
    EntityOverview("entity_overview", "eo"),

    /**
     *
     */
    Entity("entity", "et"),

    /**
     *
     */
    EntityEnum("entity_enum", "bee"),

    /**
     *
     */
    EntityField("entity_field", "bef"),

    /**
     *
     */
    FieldEntity("field_entity", "bfe"),

    /**
     *
     */
    Enum("enum", "env"),

    /**
     *
     */
    FieldType("field_type", "ft"),

    /**
     *
     */
    Field("field", "f"),

    /**
     *
     */
    EntityInheritance("entity_inheritance", "ei"),

    /**
     *
     */
    StoreBlob("str_blob", "sb"),

    /**
     *
     */
    StoreBoolean("str_boolean", "sbn"),

    /**
     *
     */
    StoreDate("str_date", "sdd"),

    /**
     *
     */
    StoreDateTime("str_date_time", "sdt"),

    /**
     *
     */
    StoreDateTimeCollection("str_date_time_col", "sdt"),

    /**
     *
     */
    StoreDouble("str_double", "sd"),

    /**
     *
     */
    StoreDoubleCollection("str_double_col", "sdc"),

    /**
     *
     */
    StoreDuration("str_duration", "sd"),

    /**
     *
     */
    StoreEnum("str_enum", "se"),

    /**
     *
     */
    StoreEnumString("str_enum_string", "ses"),

    /**
     *
     */
    StoreEnumCollection("str_enum_col", "sec"),

    /**
     *
     */
    StoreEnumStringCollection("str_enum_string_col", "sesc"),

    /**
     *
     */
    StoreShort("str_short", "sst"),

    /**
     *
     */
    StoreFloat("str_float", "sf"),

    /**
     *
     */
    StoreUuid("str_uuid", "su"),

    /**
     *
     */
    StoreFloatCollection("str_float_col", "sfc"),

    /**
     *
     */
    StoreInteger("str_integer", "si"),

    /**
     *
     */
    StoreIntegerCollection("str_integer_col", "sic"),
    /**
     *
     */
    StoreLong("str_long", "sl"),

    /**
     *
     */
    StoreLongCollection("str_long_col", "slc"),

    /**
     *
     */
    StoreMonthDay("str_month_day", "monthDayValues"),

    /**
     *
     */
    StorePeriod("str_period", "sp"),
    /**
     *
     */
    /**
     *
     */
    StoreText("str_text", "st"),

    /**
     *
     */
    StoreTextCollection("str_text_col", "stc"),

    /**
     *
     */
    StoreTime("str_time", "stt"),

    /**
     *
     */
    StoreYearMonth("str_year_month", "yearMonthValues"),

    /**
     *
     */
    StoreZonedDateTime("str_zoned_date_time", "szdt");

    override fun toString(): String {
        return name
    }
}
