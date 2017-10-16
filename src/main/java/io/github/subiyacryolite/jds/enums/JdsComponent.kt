/*
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
 */
enum class JdsComponent(val component: String, val prefix: String) {
    STORE_TEXT_ARRAY("JdsStoreTextArray", "sta"),
    STORE_FLOAT_ARRAY("JdsStoreFloatArray", "sfa"),
    STORE_INTEGER_ARRAY("JdsStoreIntegerArray", "sia"),
    STORE_LONG_ARRAY("JdsStoreLongArray", "sla"),
    STORE_DOUBLE_ARRAY("JdsStoreDoubleArray", "sda"),
    STORE_DATE_TIME_ARRAY("JdsStoreDateTimeArray", "sdta"),
    STORE_TEXT("JdsStoreText", "st"),
    STORE_FLOAT("JdsStoreFloat", "sf"),
    STORE_INTEGER("JdsStoreInteger", "si"),
    STORE_LONG("JdsStoreLong", "sl"),
    STORE_DOUBLE("JdsStoreDouble", "sd"),
    STORE_DATE_TIME("JdsStoreDateTime", "sdt"),
    STORE_ZONED_DATE_TIME("JdsStoreZonedDateTime", "szdt"),
    STORE_TIME("JdsStoreTime", "stt"),
    STORE_OLD_FIELD_VALUES("JdsStoreOldFieldValues", "sof"),
    STORE_ENTITY_OVERVIEW("JdsStoreEntityOverview", "eo"),
    STORE_ENTITY_BINDING("JdsStoreEntityBinding", "eb"),
    STORE_BLOB("JdsStoreBlob", "sb"),
    STORE_ENTITY_INHERITANCE("JdsStoreEntityInheritance", "sei"),
    REF_ENTITIES("JdsRefEntities", "et"),
    REF_ENUM_VALUES("JdsRefEnumValues", "env"),
    REF_FIELDS("JdsRefFields", "fd"),
    REF_FIELD_TYPES("JdsRefFieldTypes", "ft"),
    REF_INHERITANCE("JdsRefEntityInheritance", "ri"),
    BIND_ENTITY_FIELDS("JdsBindEntityFields", "bef"),
    BIND_ENTITY_ENUMS("JdsBindEntityEnums", "bee"),
    SAVE_TEXT("procStoreText", ""),
    SAVE_FLOAT("procStoreFloat", ""),
    SAVE_INTEGER("procStoreInteger", ""),
    SAVE_LONG("procStoreLong", ""),
    SAVE_DOUBLE("procStoreDouble", ""),
    SAVE_DATE_TIME("procStoreDateTime", ""),
    SAVE_TIME("procStoreTime", ""),
    SAVE_BLOB("procStoreBlob", ""),
    SAVE_ZONED_DATE_TIME("procStoreZonedDateTime", ""),
    SAVE_ENTITY_V_3("procStoreEntityOverviewV3", ""),
    SAVE_ENTITY_INHERITANCE("procStoreEntityInheritance", ""),
    MAP_ENTITY_INHERITANCE("procBindParentToChild", ""),
    MAP_ENTITY_FIELDS("procBindEntityFields", ""),
    MAP_ENTITY_ENUMS("procBindEntityEnums", ""),
    MAP_CLASS_NAME("procRefEntities", ""),
    MAP_ENUM_VALUES("procRefEnumValues", ""),
    TSQL_CASCADE_ENTITY_BINDING("triggerEntityBindingCascade", ""),
    MAP_FIELD_NAMES("procBindFieldNames", ""),
    MAP_FIELD_TYPES("procBindFieldTypes", "");

    override fun toString(): String {
        return name
    }
}
