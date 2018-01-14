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
    STORE_TEXT_ARRAY("jds_store_text_array", "sta"),
    STORE_FLOAT_ARRAY("jds_store_float_array", "sfa"),
    STORE_INTEGER_ARRAY("jds_store_integer_array", "sia"),
    STORE_LONG_ARRAY("jds_store_long_array", "sla"),
    STORE_DOUBLE_ARRAY("jds_store_double_array", "sda"),
    STORE_DATE_TIME_ARRAY("jds_store_date_time_array", "sdta"),
    STORE_TEXT("jds_store_text", "st"),
    STORE_BOOLEAN("jds_store_boolean", "sbn"),
    STORE_FLOAT("jds_store_float", "sf"),
    STORE_INTEGER("jds_store_integer", "si"),
    STORE_LONG("jds_store_long", "sl"),
    STORE_DOUBLE("jds_store_double", "sd"),
    STORE_DATE_TIME("jds_store_date_time", "sdt"),
    STORE_ZONED_DATE_TIME("jds_store_zoned_date_time", "szdt"),
    STORE_TIME("jds_store_time", "stt"),
    STORE_OLD_FIELD_VALUES("jds_store_old_field_value", "sof"),
    STORE_ENTITY_OVERVIEW("jds_entity_overview", "eo"),
    STORE_ENTITY_BINDING("jds_entity_binding", "eb"),
    STORE_BLOB("jds_store_blob", "sb"),
    STORE_ENTITY_INHERITANCE("jds_entity_instance", "sei"),
    REF_ENTITIES("jds_ref_entity", "et"),
    REF_ENUM_VALUES("jds_ref_enum", "env"),
    REF_FIELDS("jds_ref_field", "fd"),
    REF_FIELD_TYPES("jds_ref_field_type", "ft"),
    REF_INHERITANCE("jds_ref_entity_inheritance", "ri"),
    BIND_ENTITY_FIELDS("jds_ref_entity_field", "bef"),
    BIND_ENTITY_ENUMS("jds_ref_entity_enum", "bee"),
    SAVE_BOOLEAN("proc_store_boolean",""),
    SAVE_TEXT("proc_store_text", ""),
    SAVE_FLOAT("proc_store_float", ""),
    SAVE_INTEGER("proc_store_integer", ""),
    SAVE_LONG("proc_store_long", ""),
    SAVE_DOUBLE("proc_store_double", ""),
    SAVE_DATE_TIME("proc_store_date_time", ""),
    SAVE_TIME("proc_store_Time", ""),
    SAVE_BLOB("proc_store_Blob", ""),
    SAVE_ZONED_DATE_TIME("proc_store_zoned_date_time", ""),
    SAVE_ENTITY_V_3("proc_store_entity_overview_v3", ""),
    SAVE_ENTITY_INHERITANCE("proc_store_entity_inheritance", ""),
    MAP_ENTITY_INHERITANCE("proc_bind_parent_to_child", ""),
    MAP_ENTITY_FIELDS("proc_ref_entity_field", ""),
    MAP_ENTITY_ENUMS("proc_ref_entity_enum", ""),
    MAP_CLASS_NAME("proc_ref_entity", ""),
    MAP_ENUM_VALUES("proc_ref_enum", ""),
    TSQL_CASCADE_ENTITY_BINDING("trigger_entity_binding_cascade", ""),
    MAP_FIELD_NAMES("proc_ref_field", "");

    override fun toString(): String {
        return name
    }
}
