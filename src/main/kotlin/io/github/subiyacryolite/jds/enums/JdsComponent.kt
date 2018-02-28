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
    STORE_TEXT("jds_store_text", "st"),
    STORE_BOOLEAN("jds_store_boolean", "sbn"),
    STORE_FLOAT("jds_store_float", "sf"),
    STORE_INTEGER("jds_store_integer", "si"),
    STORE_LONG("jds_store_long", "sl"),
    STORE_DOUBLE("jds_store_double", "sd"),
    STORE_DATE_TIME("jds_store_date_time", "sdt"),
    STORE_ZONED_DATE_TIME("jds_store_zoned_date_time", "szdt"),
    STORE_TIME("jds_store_time", "stt"),
    STORE_ENTITY_OVERVIEW("jds_entity_overview", "eo"),
    STORE_BLOB("jds_store_blob", "sb"),
    REF_ENTITIES("jds_ref_entity", "et"),
    REF_ENUM_VALUES("jds_ref_enum", "env"),
    REF_FIELDS("jds_ref_field", "fd"),
    REF_FIELD_TYPES("jds_ref_field_type", "ft"),
    REF_INHERITANCE("jds_ref_entity_inheritance", "ri"),
    BIND_ENTITY_FIELDS("jds_ref_entity_field", "bef"),
    BIND_ENTITY_ENUMS("jds_ref_entity_enum", "bee"),
    PROC_STORE_BOOLEAN("proc_store_boolean", ""),
    PROC_STORE_TEXT("proc_store_text", ""),
    PROC_STORE_FLOAT("proc_store_float", ""),
    PROC_STORE_INTEGER("proc_store_integer", ""),
    PROC_STORE_LONG("proc_store_long", ""),
    PROC_STORE_DOUBLE("proc_store_double", ""),
    PROC_STORE_DATE_TIME("proc_store_date_time", ""),
    PROC_STORE_TIME("proc_store_time", ""),
    PROC_STORE_BLOB("proc_store_blob", ""),
    PROC_ZONED_DATE_TIME("proc_store_zoned_date_time", ""),
    PROC_ENTITY_OVERVIEW("proc_store_entity_overview_v3", ""),
    PROC_REF_ENTITY_INHERITANCE("proc_ref_entity_inheritance", ""),
    PROC_REF_ENTITY_FIELD("proc_ref_entity_field", ""),
    PROC_REF_ENTITY_ENUM("proc_ref_entity_enum", ""),
    PROC_REF_ENTITY("proc_ref_entity", ""),
    PROC_REF_ENUM("proc_ref_enum", ""),
    PROC_REF_FIELD("proc_ref_field", "");

    override fun toString(): String {
        return name
    }
}
