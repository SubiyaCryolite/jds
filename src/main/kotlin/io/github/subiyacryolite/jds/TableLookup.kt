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

import io.github.subiyacryolite.jds.enums.FieldType
import io.github.subiyacryolite.jds.enums.Table
import io.github.subiyacryolite.jds.enums.Table.*

/**
 * Class used to look up the datastore of Jds Field Types
 * Used in dynamic view creation, filtering and other operations
 */
object TableLookup {

    /**
     * Retrieve the table that stores the requested jdsField type
     * @param fieldType the requested jdsField type
     * @return the table that stores the requested jdsField type
     */
    private fun getComponentForFieldType(fieldType: FieldType): Table {
        when (fieldType) {
            FieldType.Long, FieldType.Duration -> return StoreLong
            FieldType.Enum, FieldType.Int -> return StoreInteger
            FieldType.Period, FieldType.String, FieldType.YearMonth, FieldType.MonthDay -> return StoreText
            FieldType.FloatCollection -> return StoreFloat
            FieldType.IntCollection -> return StoreInteger
            FieldType.DoubleCollection -> return StoreDouble
            FieldType.LongCollection -> return StoreLong
            FieldType.StringCollection -> return StoreText
            FieldType.DateTimeCollection -> return StoreDateTime
            FieldType.EnumCollection -> return StoreInteger
            FieldType.ZonedDateTime -> return StoreZonedDateTime
            FieldType.Float -> return StoreFloat
            FieldType.Double -> return StoreDouble
            FieldType.Time -> return StoreTime
            FieldType.Blob -> return StoreBlob
            FieldType.Boolean -> return StoreBoolean
            FieldType.DateTime -> return StoreDateTime
            else-> return StoreDate
        }
    }

    /**
     * Retrieve the table that stores the requested jdsField type
     * @param fieldType the requested jdsField type
     * @return the table that stores the requested jdsField type
     */
    fun getTableForFieldType(fieldType: FieldType): String {
        return getComponentForFieldType(fieldType).component
    }

    /**
     * Get the short version of the table that holds the requested jdsField type
     * @param fieldType the requested jdsField type
     * @return the short version of the table that holds the requested jdsField type
     */
    fun getTableAliasForFieldType(fieldType: FieldType): String {
        return getComponentForFieldType(fieldType).alias
    }
}
