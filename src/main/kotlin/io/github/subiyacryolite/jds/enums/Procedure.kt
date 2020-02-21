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
package io.github.subiyacryolite.jds.enums

/**
 * @param component the component name
 */
enum class Procedure(val component: String) {
    /**
     *
     */
    EntityBinding("pop_${Table.EntityBinding.component}"),

    /**
     *
     */
    EntityLive("pop_${Table.EntityLive.component}"),

    /**
     *
     */
    EntityOverview("pop_${Table.EntityOverview.component}"),

    /**
     *
     */
    Entity("pop_${Table.Entity.component}"),

    /**
     *
     */
    EntityEnum("pop_${Table.EntityEnum.component}"),

    /**
     *
     */
    EntityField("pop_${Table.EntityField.component}"),

    /**
     *
     */
    FieldEntity("pop_${Table.FieldEntity.component}"),

    /**
     *
     */
    EntityInheritance("pop_${Table.EntityInheritance.component}"),

    /**
     *
     */
    Enum("pop_${Table.Enum.component}"),

    /**
     *
     */
    Field("pop_${Table.Field.component}"),

    /**
     *
     */
    FieldDictionary("pop_${Table.FieldDictionary.component}"),

    /**
     *
     */
    FieldTag("pop_${Table.FieldTag.component}"),

    /**
     *
     */
    FieldAlternateCode("pop_${Table.FieldAlternateCode.component}"),

    /**
     *
     */
    StoreBlob("pop_${Table.StoreBlob.component}"),

    /**
     *
     */
    StoreBoolean("pop_${Table.StoreBoolean.component}"),

    /**
     *
     */
    StoreDate("pop_${Table.StoreDate.component}"),

    /**
     *
     */
    StoreDateTime("pop_${Table.StoreDateTime.component}"),

    /**
     *
     */
    StoreDouble("pop_${Table.StoreDouble.component}"),

    /**
     *
     */
    StoreDoubleCollection("pop_${Table.StoreDoubleCollection.component}"),

    /**
     *
     */
    StoreDuration("pop_${Table.StoreDuration.component}"),

    /**
     *
     */
    StoreEnum("pop_${Table.StoreEnum.component}"),

    /**
     *
     */
    StoreEnumString("pop_${Table.StoreEnumString.component}"),

    /**
     *
     */
    StoreEnumCollection("pop_${Table.StoreEnumCollection.component}"),

    /**
     *
     */
    StoreEnumStringCollection("pop_${Table.StoreEnumStringCollection.component}"),

    /**
     *
     */
    StoreDateTimeCollection("pop_${Table.StoreDateTimeCollection.component}"),

    /**
     *
     */
    StoreFloat("pop_${Table.StoreFloat.component}"),

    /**
     *
     */
    StoreShort("pop_${Table.StoreShort.component}"),

    /**
     *
     */
    StoreUuid("pop_${Table.StoreUuid.component}"),

    /**
     *
     */
    StoreFloatCollection("pop_${Table.StoreFloatCollection.component}"),

    /**
     *
     */
    StoreInteger("pop_${Table.StoreInteger.component}"),

    /**
     *
     */
    StoreIntegerCollection("pop_${Table.StoreIntegerCollection.component}"),

    /**
     *
     */
    StoreLong("pop_${Table.StoreLong.component}"),

    /**
     *
     */
    StoreLongCollection("pop_${Table.StoreLongCollection.component}"),

    /**
     *
     */
    StoreMonthDay("pop_${Table.StoreMonthDay.component}"),

    /**
     *
     */
    StorePeriod("pop_${Table.StorePeriod.component}"),

    /**
     *
     */
    StoreText("pop_${Table.StoreText.component}"),

    /**
     *
     */
    StoreTextCollection("pop_${Table.StoreTextCollection.component}"),

    /**
     *
     */
    StoreTime("pop_${Table.StoreTime.component}"),

    /**
     *
     */
    StoreYearMonth("pop_${Table.StoreYearMonth.component}"),

    /**
     *
     */
    StoreZonedDateTime("pop_${Table.StoreZonedDateTime.component}")
}