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
 * An enum containing the data-types supported by JDS
 * @param type field id
 * @param shortCode field alias
 */
enum class JdsFieldType(val type: Int, val shortCode: String) {
    FLOAT(0, "f"),
    INT(1, "i"),
    DOUBLE(2, "d"),
    LONG(3, "l"),
    STRING(4, "t"),
    DATE_TIME(5, "ldt"),
    FLOAT_COLLECTION(6, "fa"),
    INT_COLLECTION(7, "ia"),
    DOUBLE_COLLECTION(8, "da"),
    LONG_COLLECTION(9, "la"),
    STRING_COLLECTION(10, "ta"),
    DATE_TIME_COLLECTION(11, "dta"),
    ENUM_COLLECTION(12, "enumCollections"),
    BOOLEAN(13, "bl"),
    ZONED_DATE_TIME(14, "zonedDateTimeValues"),
    DATE(15, "dt"),
    TIME(16, "tm"),
    BLOB(17, "b"),
    ENUM(18, "flt"),
    ENTITY(19, "e"),
    MONTH_DAY(20, "monthDayValues"),
    YEAR_MONTH(21, "yearMonthValues"),
    PERIOD(22, "p"),
    DURATION(23, "dr"),
    ENTITY_COLLECTION(24, "enumCollections"),
    UNKNOWN(25, "un"),
    ENUM_STRING(26, "ens"),
    ENUM_STRING_COLLECTION(27, "ensc");
}
