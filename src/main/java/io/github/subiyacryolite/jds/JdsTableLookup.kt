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

import io.github.subiyacryolite.jds.enums.JdsComponent
import io.github.subiyacryolite.jds.enums.JdsComponent.*
import io.github.subiyacryolite.jds.enums.JdsFieldType

/**
 * Class used to look up the datastore of Jds Field Types
 * Used in dynamic view creation, filtering and other operations
 * Created by ifunga on 24/06/2017.
 */
object JdsTableLookup {

    /**
     * Retrieve the table that stores the requested fieldEntity type
     *
     * @param fieldType the requested fieldEntity type
     * @return the table that stores the requested fieldEntity type
     */
    fun getComponentForFieldType(fieldType: JdsFieldType): JdsComponent {
        when (fieldType) {
            JdsFieldType.FLOAT -> return STORE_FLOAT
            JdsFieldType.DOUBLE -> return STORE_DOUBLE
            JdsFieldType.FLOAT_COLLECTION -> return STORE_FLOAT_ARRAY
            JdsFieldType.INT_COLLECTION -> return STORE_INTEGER_ARRAY
            JdsFieldType.DOUBLE_COLLECTION -> return STORE_DOUBLE_ARRAY
            JdsFieldType.LONG_COLLECTION -> return STORE_LONG_ARRAY
            JdsFieldType.STRING_COLLECTION -> return STORE_TEXT_ARRAY
            JdsFieldType.DATE_TIME_COLLECTION -> return STORE_DATE_TIME_ARRAY
            JdsFieldType.ENUM_COLLECTION -> return STORE_INTEGER_ARRAY
            JdsFieldType.ZONED_DATE_TIME -> return STORE_ZONED_DATE_TIME
            JdsFieldType.TIME -> return STORE_TIME
            JdsFieldType.BLOB -> return STORE_BLOB
            JdsFieldType.BOOLEAN-> return STORE_BOOLEAN
            JdsFieldType.ENUM, JdsFieldType.INT -> return STORE_INTEGER
            JdsFieldType.DATE, JdsFieldType.DATE_TIME -> return STORE_DATE_TIME
            JdsFieldType.LONG, JdsFieldType.DURATION -> return STORE_LONG
            JdsFieldType.PERIOD, JdsFieldType.STRING, JdsFieldType.YEAR_MONTH, JdsFieldType.MONTH_DAY -> return STORE_TEXT
            else -> return NULL
        }
    }

    /**
     * Retrieve the table that stores the requested fieldEntity type
     *
     * @param fieldType the requested fieldEntity type
     * @return the table that stores the requested fieldEntity type
     */
    fun getTableForFieldType(fieldType: JdsFieldType): String {
        return getComponentForFieldType(fieldType).component
    }

    /**
     * Get the short version of the table that holds the requested fieldEntity type
     *
     * @param jdsFieldType the requested fieldEntity type
     * @return the short version of the table that holds the requested fieldEntity type
     */
    fun getTableAliasForFieldType(fieldType: JdsFieldType): String {
        return getComponentForFieldType(fieldType).alias
    }
}
