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
package io.github.subiyacryolite.jds;

import java.sql.Connection
import java.sql.SQLException

/**
 * @author indana
 */
interface IJdsDb {

    /**
     * Acquire standard connection to the database
     *
     * @return standard connection to the database
     * @throws ClassNotFoundException when JDBC driver is not configured correctly
     * @throws SQLException when a standard SQL Exception occurs
     */
    @Throws(ClassNotFoundException::class, SQLException::class)
    fun getConnection(): Connection

    /**
     * Acquire a custom connection to a database
     * @param targetConnection a custom flag to access a custom database
     *
     * @return standard connection to the database
     * @throws ClassNotFoundException when JDBC driver is not configured correctly
     * @throws SQLException when a standard SQL Exception occurs
     */
    @Throws(ClassNotFoundException::class, SQLException::class)
    fun getConnection(targetConnection: Int): Connection

    /**
     * Checks if the specified index exists the the database
     *
     * @param connection the connection to use
     * @param columnName the column to look up
     * @param tableName  the table to inspect
     * @return true if the specified index exists the the database
     */
    fun doesColumnExist(connection: Connection, tableName: String, columnName: String): Boolean

    /**
     * Checks if the specified table exists the the database
     *
     * @param connection the connection to use
     * @param tableName the table to look up
     * @return true if the specified table exists the the database
     */
    fun doesTableExist(connection: Connection, tableName: String): Boolean

    /**
     * Checks if the specified procedure exists the the database
     *
     * @param connection the connection to use
     * @param procedureName the procedure to look up
     * @return true if the specified procedure exists the the database
     */
    fun doesProcedureExist(connection: Connection, procedureName: String): Boolean

    /**
     * Checks if the specified trigger exists the the database
     *
     * @param connection the connection to use
     * @param triggerName the trigger to look up
     * @return true if the specified trigger exists the the database
     */
    fun doesTriggerExist(connection: Connection, triggerName: String): Boolean

    /**
     * Checks if the specified index exists the the database
     *
     * @param connection the connection to use
     * @param indexName the index to look up
     * @return true if the specified index exists the the database
     */
    fun doesIndexExist(connection: Connection, indexName: String): Boolean

    /**
     * Gets the correct syntax needed to add a new column to the underlying database implementation
     * @return the correct syntax needed to add a new column to the underlying database implementation
     */
    open fun getDbAddColumnSyntax()="ALTER TABLE %s %s"

    /**
     * Gets the Float data-type of the current database implementation
     * @return the Float data-type of the database implementation
     */
    fun getDbFloatDataType(): String

    /**
     * Gets the Double data-type of the current database implementation
     * @return the Double data-type of the database implementation
     */
    fun getDbDoubleDataType(): String

    /**
     * Gets the ZonedDateTime data-type of the current database implementation
     * @return the ZonedDataTime data-type of the database implementation
     */
    fun getDbZonedDateTimeDataType(): String

    /**
     * Gets the Time data-type of the current database implementation
     * @return the Time data-type of the database implementation
     */
    fun getDbTimeDataType(): String

    /**
     * Gets the Blob data-type of the current database implementation
     * @return the Blob data-type of the database implementation
     */
    fun getDbBlobDataType(max: Int): String

    /**
     * Gets the Integer data-type of the current database implementation
     * @return the Integer data-type of the database implementation
     */
    fun getDbIntegerDataType(): String

    /**
     * Gets the DateTime data-type of the current database implementation
     * @return the DateTime data-type of the database implementation
     */
    fun getDbDateTimeDataType(): String

    /**
     * Gets the Long data-type of the current database implementation
     * @return the Long data-type of the database implementation
     */
    fun getDbLongDataType(): String

    /**
     * Gets the String data-type of the current database implementation
     * @return the String data-type of the database implementation
     */
    fun getDbStringDataType(max: Int): String

    /**
     * Gets the Boolean data-type of the current database implementation
     * @return the Boolean data-type of the database implementation
     */
    fun getDbBooleanDataType(): String

    /**
     * @param
     * @columnName
     * @indexName
     */
    fun getDbCreateIndexSyntax(tableName: String, columnName: String, indexName: String): String
}
