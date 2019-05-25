package io.github.subiyacryolite.jds.tests.entities

import io.github.subiyacryolite.jds.tests.constants.Fields
import io.github.subiyacryolite.jds.JdsEntity
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation
import javafx.beans.property.SimpleObjectProperty

import java.time.*

@JdsEntityAnnotation(name = "TimeConstruct", id = 8000)
class TimeConstruct : JdsEntity() {
    private val _period = SimpleObjectProperty(Period.ZERO)
    private val _duration = SimpleObjectProperty(Duration.ZERO)
    private val _monthDay = SimpleObjectProperty(MonthDay.of(Month.APRIL, 14))
    private val _yearMonth = SimpleObjectProperty(YearMonth.of(1991, 7))

    init {
        map(Fields.PERIOD, _period)
        map(Fields.DURATION, _duration)
        map(Fields.MONTH_DAY, _monthDay)
        map(Fields.YEAR_MONTH, _yearMonth)
    }

    var period: Period
        get() = _period.value
        set(value) = _period.set(value)

    var duration: Duration
        get() = _duration.value
        set(value) = _duration.set(value)

    var monthDay: MonthDay
        get() = _monthDay.value
        set(value) = _monthDay.set(value)

    var yearMonth: YearMonth
        get() = _yearMonth.value
        set(value) = _yearMonth.set(value)

    override fun toString(): String {
        return "{ period = $period" +
                ", duration = $duration" +
                ", monthDay = $monthDay" +
                ", yearMonth = $yearMonth " +
                "}"
    }
}
