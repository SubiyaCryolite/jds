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
package io.github.subiyacryolite.jds

import io.github.subiyacryolite.jds.enums.JdsImplementation

/**
 * The MySQL implementation of [JdsDataBase][JdsDb]
 */
abstract class JdsDbMaria : JdsDbMySql(JdsImplementation.MariaDb, true) {

    /**
     * SQL executed in order to log String values
     * @return SQL executed in order to log String values
     */
    override fun saveOldStringValues(): String {
        return when (isLoggingAppendOnly) {
            true -> "INSERT INTO jds_store_old_field_value (uuid, field_id, sequence, string_value) VALUES(?, ?, ?, ?)"
            false -> "INSERT INTO jds_store_old_field_value (uuid, field_id, sequence, string_value) $logSqlSource " +
                    "FROM dual WHERE NOT EXISTS(SELECT uuid FROM jds_store_old_field_value WHERE uuid = ? AND field_id = ? AND sequence = ? AND $oldStringValue = ?)"
        }
    }

    /**
     * SQL executed in order to log Double values
     * @return SQL executed in order to log Double values
     */
    override fun saveOldDoubleValues(): String {
        return when (isLoggingAppendOnly) {
            true -> "INSERT INTO jds_store_old_field_value (uuid, field_id, sequence, double_value) VALUES(?, ?, ?, ?)"
            false -> "INSERT INTO jds_store_old_field_value (uuid, field_id, sequence, double_value) $logSqlSource " +
                    "FROM dual WHERE NOT EXISTS(SELECT uuid FROM jds_store_old_field_value WHERE uuid = ? AND field_id = ? AND sequence = ? AND double_value = ?)"
        }
    }

    /**
     * SQL executed in order to log Long values
     * @return SQL executed in order to log Long values
     */
    override fun saveOldLongValues(): String {
        return when (isLoggingAppendOnly) {
            true -> "INSERT INTO jds_store_old_field_value (uuid, field_id, sequence, long_value) VALUES(?, ?, ?, ?)"
            false -> "INSERT INTO jds_store_old_field_value (uuid, field_id, sequence, long_value) $logSqlSource " +
                    "FROM dual WHERE NOT EXISTS(SELECT uuid FROM jds_store_old_field_value WHERE uuid = ? AND field_id = ? AND sequence = ? AND long_value = ?)"
        }
    }

    /**
     * SQL executed in order to log Integer values
     * @return SQL executed in order to log Integer values
     */
    override fun saveOldIntegerValues(): String {
        return when (isLoggingAppendOnly) {
            true -> "INSERT INTO jds_store_old_field_value (uuid, field_id, sequence, integer_value) VALUES(?, ?, ?, ?)"
            false -> "INSERT INTO jds_store_old_field_value (uuid, field_id, sequence, integer_value) $logSqlSource " +
                    "FROM dual WHERE NOT EXISTS(SELECT uuid FROM jds_store_old_field_value WHERE uuid = ? AND field_id = ? AND sequence = ? AND integer_value = ?)"
        }
    }

    /**
     * SQL executed in order to log Float values
     * @return SQL executed in order to log Float values
     */
    override fun saveOldFloatValues(): String {
        return when (isLoggingAppendOnly) {
            true -> "INSERT INTO jds_store_old_field_value (uuid, field_id, sequence, float_value) VALUES(?, ?, ?, ?)"
            false -> "INSERT INTO jds_store_old_field_value (uuid, field_id, sequence, float_value) $logSqlSource " +
                    "FROM dual WHERE NOT EXISTS(SELECT uuid FROM jds_store_old_field_value WHERE uuid = ? AND field_id = ? AND sequence = ? AND float_value = ?)"
        }
    }

    /**
     * SQL executed in order to log DateTime values
     * @return SQL executed in order to log DateTime values
     */
    override fun saveOldDateTimeValues(): String {
        return when (isLoggingAppendOnly) {
            true -> "INSERT INTO jds_store_old_field_value (uuid, field_id, sequence, date_time_value) VALUES(?, ?, ?, ?)"
            false -> "INSERT INTO jds_store_old_field_value (uuid, field_id, sequence, date_time_value) $logSqlSource " +
                    "FROM dual WHERE NOT EXISTS(SELECT uuid FROM jds_store_old_field_value WHERE uuid = ? AND field_id = ? AND sequence = ? AND date_time_value = ?)"
        }
    }

    /**
     * SQL executed in order to log ZonedDateTime values
     * @return SQL executed in order to log ZonedDateTime values
     */
    override fun saveOldZonedDateTimeValues(): String {
        return when (isLoggingAppendOnly) {
            true -> "INSERT INTO jds_store_old_field_value (uuid, field_id, sequence, zoned_date_time_value) VALUES(?, ?, ?, ?)"
            false -> "INSERT INTO jds_store_old_field_value (uuid, field_id, sequence, zoned_date_time_value) $logSqlSource " +
                    "FROM dual WHERE NOT EXISTS(SELECT uuid FROM jds_store_old_field_value WHERE uuid = ? AND field_id = ? AND sequence = ? AND zoned_date_time_value = ?)"
        }
    }

    /**
     * SQL executed in order to log Time values
     * @return SQL executed in order to log Time values
     */
    override fun saveOldTimeValues(): String {
        return when (isLoggingAppendOnly) {
            true -> "INSERT INTO jds_store_old_field_value (uuid, field_id, sequence, time_value) VALUES(?, ?, ?, ?)"
            false -> "INSERT INTO jds_store_old_field_value (uuid, field_id, sequence, time_value) $logSqlSource " +
                    "FROM dual WHERE NOT EXISTS(SELECT uuid FROM jds_store_old_field_value WHERE uuid = ? AND field_id = ? AND sequence = ? AND time_value = ?)"
        }
    }

    /**
     * SQL executed in order to log Boolean values
     * @return SQL executed in order to log Boolean values
     */
    override fun saveOldBooleanValues(): String {
        return when (isLoggingAppendOnly) {
            true -> "INSERT INTO jds_store_old_field_value (uuid, field_id, sequence, boolean_value) VALUES(?, ?, ?, ?)"
            false -> "INSERT INTO jds_store_old_field_value (uuid, field_id, sequence, boolean_value) $logSqlSource " +
                    "FROM dual WHERE NOT EXISTS(SELECT uuid FROM jds_store_old_field_value WHERE uuid = ? AND field_id = ? AND sequence = ? AND boolean_value = ?)"
        }
    }
}