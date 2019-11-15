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

import io.github.subiyacryolite.jds.enums.JdsFieldType
import io.github.subiyacryolite.jds.enums.JdsTableComponent
import io.github.subiyacryolite.jds.enums.JdsTableComponent.*

/**
 * Class used to look up the datastore of Jds Field Types
 * Used in dynamic view creation, filtering and other operations
 * Created by ifunga on 24/06/2017.
 */
object JdsTableLookup {

    /**
     * Retrieve the table that stores the requested jdsField type
     * @param fieldType the requested jdsField type
     * @return the table that stores the requested jdsField type
     */
    private fun getComponentForFieldType(fieldType: JdsFieldType): JdsTableComponent {
        when (fieldType) {
            JdsFieldType.Long, JdsFieldType.Duration -> return StoreLong
            JdsFieldType.Enum, JdsFieldType.Int -> return StoreInteger
            JdsFieldType.Period, JdsFieldType.String, JdsFieldType.YearMonth, JdsFieldType.MonthDay -> return StoreText
            JdsFieldType.FloatCollection -> return StoreFloat
            JdsFieldType.IntCollection -> return StoreInteger
            JdsFieldType.DoubleCollection -> return StoreDouble
            JdsFieldType.LongCollection -> return StoreLong
            JdsFieldType.StringCollection -> return StoreText
            JdsFieldType.DateTimeCollection -> return StoreDateTime
            JdsFieldType.EnumCollection -> return StoreInteger
            JdsFieldType.ZonedDateTime -> return StoreZonedDateTime
            JdsFieldType.Float -> return StoreFloat
            JdsFieldType.Double -> return StoreDouble
            JdsFieldType.Time -> return StoreTime
            JdsFieldType.Blob -> return StoreBlob
            JdsFieldType.Boolean -> return StoreBoolean
            JdsFieldType.DateTime -> return StoreDateTime
            else-> return StoreDate
        }
    }

    /**
     * Retrieve the table that stores the requested jdsField type
     * @param fieldType the requested jdsField type
     * @return the table that stores the requested jdsField type
     */
    fun getTableForFieldType(fieldType: JdsFieldType): String {
        return getComponentForFieldType(fieldType).component
    }

    /**
     * Get the short version of the table that holds the requested jdsField type
     * @param fieldType the requested jdsField type
     * @return the short version of the table that holds the requested jdsField type
     */
    fun getTableAliasForFieldType(fieldType: JdsFieldType): String {
        return getComponentForFieldType(fieldType).alias
    }
}
