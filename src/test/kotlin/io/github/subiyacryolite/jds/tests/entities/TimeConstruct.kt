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
package io.github.subiyacryolite.jds.tests.entities

import io.github.subiyacryolite.jds.Entity
import io.github.subiyacryolite.jds.annotations.EntityAnnotation
import io.github.subiyacryolite.jds.beans.property.*
import io.github.subiyacryolite.jds.tests.constants.Fields
import java.time.*

@EntityAnnotation(name = "TimeConstruct", id = 8000)
class TimeConstruct : Entity() {
    private val _period = PeriodProperty(Period.ZERO)
    private val _duration = DurationProperty(Duration.ZERO)
    private val _monthDay = MonthDayProperty(MonthDay.of(Month.APRIL, 14))
    private val _yearMonth = YearMonthProperty(YearMonth.of(1991, 7))

    init {
        map(Fields.Period, _period)
        map(Fields.Duration, _duration)
        map(Fields.MonthDay, _monthDay)
        map(Fields.YearMonth, _yearMonth)
    }

    var period: Period
        get() = _period.get()
        set(value) = _period.set(value)

    var duration: Duration
        get() = _duration.get()
        set(value) = _duration.set(value)

    var monthDay: MonthDay
        get() = _monthDay.get()
        set(value) = _monthDay.set(value)

    var yearMonth: YearMonth
        get() = _yearMonth.get()
        set(value) = _yearMonth.set(value)

    override fun toString(): String {
        return "{ period = $period" +
                ", duration = $duration" +
                ", monthDay = $monthDay" +
                ", yearMonth = $yearMonth " +
                "}"
    }
}
