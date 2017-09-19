package entities

import io.github.subiyacryolite.jds.JdsEntity
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation
import io.github.subiyacryolite.jds.events.*
import javafx.beans.property.*

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime

import fields.Fields.*

/**
 * Created by ifunga on 12/04/2017.
 */
@JdsEntityAnnotation(entityId = 3, entityName = "Type Class")
class Example : JdsEntity(), JdsLoadListener, JdsSaveListener {
    private val _stringField = SimpleStringProperty("")
    private val _timeField = SimpleObjectProperty(LocalTime.now())
    private val _dateField = SimpleObjectProperty(LocalDate.now())
    private val _dateTimeField = SimpleObjectProperty(LocalDateTime.now())
    private val _zonedDateTimeField = SimpleObjectProperty(ZonedDateTime.now())
    private val _longField = SimpleLongProperty(0)
    private val _intField = SimpleIntegerProperty(0)
    private val _doubleField = SimpleDoubleProperty(0.0)
    private val _floatField = SimpleFloatProperty(0f)
    private val _booleanField = SimpleBooleanProperty(false)
    private val _blobField = SimpleBlobProperty(ByteArray(0))

    init {
        map(STRING_FIELD, _stringField)
        map(DATE_FIELD, _dateField)
        map(TIME_FIELD, _timeField)
        map(DATE_TIME_FIELD, _dateTimeField)
        map(ZONED_DATE_TIME_FIELD, _zonedDateTimeField)
        map(LONG_FIELD, _longField)
        map(INT_FIELD, _intField)
        map(DOUBLE_FIELD, _doubleField)
        map(FLOAT_FIELD, _floatField)
        map(BOOLEAN_FIELD, _booleanField)
        map(BLOB_FIELD, _blobField)
    }

    var stringField: String
        get() = _stringField.get()
        set(stringField) = this._stringField.set(stringField)

    var timeField: LocalTime
        get() = _timeField.get()
        set(dateField) = this._timeField.set(dateField)

    var dateField: LocalDate
        get() = _dateField.get()
        set(dateField) = this._dateField.set(dateField)

    var dateTimeField: LocalDateTime
        get() = _dateTimeField.get()
        set(dateTimeField) = this._dateTimeField.set(dateTimeField)

    var zonedDateTimeField: ZonedDateTime
        get() = _zonedDateTimeField.get()
        set(zonedDateTimeField) = this._zonedDateTimeField.set(zonedDateTimeField)

    var longField: Long
        get() = _longField.get()
        set(longField) = this._longField.set(longField)

    var intField: Int
        get() = _intField.get()
        set(intField) = this._intField.set(intField)

    var doubleField: Double
        get() = _doubleField.get()
        set(doubleField) = this._doubleField.set(doubleField)

    var floatField: Float
        get() = _floatField.get()
        set(floatField) = this._floatField.set(floatField)

    var booleanField: Boolean
        get() = _booleanField.get()
        set(booleanField) = this._booleanField.set(booleanField)

    override fun onPreSave(eventArguments: OnPreSaveEventArguments) {
        //Optional event i.e write to custom reporting tables, perform custom validation
        //Queries can be batched i.e eventArguments.getOrAddStatement("Batched SQL to execute")
    }

    override fun onPostSave(eventArguments: OnPostSaveEventArguments) {
        //Optional event i.e write to custom reporting tables, perform custom validation
        //Queries can be batched i.e eventArguments.getOrAddStatement("Batched SQL to execute")
    }

    override fun onPreLoad(eventArguments: OnPreLoadEventArguments) {
        //Optional event i.e write to custom reporting tables, perform custom validation
        //Queries can be batched i.e eventArguments.getOrAddStatement("Batched SQL to execute")
    }

    override fun onPostLoad(eventArguments: OnPostLoadEventArguments) {
        //Optional event i.e write to custom reporting tables, perform custom validation
        //Queries can be batched i.e eventArguments.getOrAddStatement("Batched SQL to execute")
    }
}