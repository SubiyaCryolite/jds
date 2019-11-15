package io.github.subiyacryolite.jds.tests.entities

import io.github.subiyacryolite.jds.JdsEntity
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation
import io.github.subiyacryolite.jds.beans.property.NullableIntegerProperty
import io.github.subiyacryolite.jds.events.EventArguments
import io.github.subiyacryolite.jds.events.JdsLoadListener
import io.github.subiyacryolite.jds.events.JdsSaveListener
import io.github.subiyacryolite.jds.tests.constants.Fields
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleFloatProperty
import javafx.beans.property.SimpleLongProperty
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime

/**
 * Created by ifunga on 12/04/2017.
 */
@JdsEntityAnnotation(id = 3, name = "TypeClass")
class Example : JdsEntity(), JdsLoadListener, JdsSaveListener {

    private val _stringField = map(Fields.STRING_FIELD, "")
    private val _timeField = map(Fields.TIME_FIELD, LocalTime.now())
    private val _dateField = map(Fields.DATE_FIELD, LocalDate.now())
    private val _dateTimeField = map(Fields.DATE_TIME_FIELD, LocalDateTime.now())
    private val _zonedDateTimeField = map(Fields.ZONED_DATE_TIME_FIELD, ZonedDateTime.now())
    private val _longField = map(Fields.LONG_FIELD, SimpleLongProperty(0))
    private val _intField = map(Fields.INT_FIELD, NullableIntegerProperty())
    private val _doubleField = map(Fields.DOUBLE_FIELD, SimpleDoubleProperty(0.0))
    private val _floatField = map(Fields.FLOAT_FIELD, SimpleFloatProperty(0f))
    private val _booleanField = map(Fields.BOOLEAN_FIELD, false)
    private val _blobField = map(Fields.BLOB_FIELD, ByteArray(0))

    var stringField: String
        get() = _stringField.get()
        set(stringField) = _stringField.set(stringField)

    var timeField: LocalTime
        get() = _timeField.get()!!
        set(dateField) = _timeField.set(dateField)

    var dateField: LocalDate
        get() = _dateField.get()!!
        set(dateField) = _dateField.set(dateField)

    var dateTimeField: LocalDateTime
        get() = _dateTimeField.get()!!
        set(dateTimeField) = _dateTimeField.set(dateTimeField)

    var zonedDateTimeField: ZonedDateTime
        get() = _zonedDateTimeField.get()!!
        set(zonedDateTimeField) = _zonedDateTimeField.set(zonedDateTimeField)

    var longField: Long
        get() = _longField.get()!!
        set(longField) = _longField.set(longField)

    var intField: Int?
        get() = _intField.get()
        set(intField) = _intField.set(intField)

    var doubleField: Double
        get() = _doubleField.get()!!
        set(doubleField) = _doubleField.set(doubleField)

    var floatField: Float
        get() = _floatField.get()!!
        set(floatField) = _floatField.set(floatField)

    var booleanField: Boolean
        get() = _booleanField.get()!!
        set(booleanField) = _booleanField.set(booleanField)

    var blobField: ByteArray
        get() = _blobField.get()!!
        set(booleanField) = _blobField.set(booleanField)

    override fun onPreSave(eventArguments: EventArguments) {
        //Optional event i.e write to custom reporting tables, perform custom validation
        //Queries can be batched i.e eventArguments.getOrAddStatement("Batched SQL to execute")
    }

    override fun onPostSave(eventArguments: EventArguments) {
        //Optional event i.e write to custom reporting tables, perform custom validation
        //Queries can be batched i.e eventArguments.getOrAddStatement("Batched SQL to execute")
    }

    override fun onPreLoad(eventArguments: EventArguments) {
        //Optional event i.e write to custom reporting tables, perform custom validation
        //Queries can be batched i.e eventArguments.getOrAddStatement("Batched SQL to execute")
    }

    override fun onPostLoad(eventArguments: EventArguments) {
        //Optional event i.e write to custom reporting tables, perform custom validation
        //Queries can be batched i.e eventArguments.getOrAddStatement("Batched SQL to execute")
    }

    override fun toString(): String {
        return "{" +
                ", overview = $overview" +
                ", stringField = $stringField" +
                ", dateField = $dateField" +
                ", timeField = $timeField" +
                ", dateTimeField = $dateTimeField" +
                ", zonedDateTimeField = $zonedDateTimeField" +
                ", longField = $longField" +
                ", intField = $intField" +
                ", doubleField = $doubleField" +
                ", floatField = $floatField" +
                ", blobField = $blobField" +
                ", booleanField = $booleanField}"
    }
}