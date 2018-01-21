package entities

import constants.Fields
import io.github.subiyacryolite.jds.JdsEntity
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation
import io.github.subiyacryolite.jds.events.*
import javafx.beans.property.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime

/**
 * Created by ifunga on 12/04/2017.
 */
@JdsEntityAnnotation(entityId = 3, entityName = "TypeClass")
class Example : JdsEntity(), JdsLoadListener, JdsSaveListener {
    private val _stringField = SimpleStringProperty("")
    private val _timeField = SimpleObjectProperty(LocalTime.now())
    private val _dateField = SimpleObjectProperty(LocalDate.now())
    private val _dateTimeField = SimpleObjectProperty(LocalDateTime.now())
    private val _zonedDateTimeField = SimpleObjectProperty(ZonedDateTime.now())
    private val _longField = SimpleLongProperty(0)
    private val _intField = NullableIntegerProperty()
    private val _doubleField = SimpleDoubleProperty(0.0)
    private val _floatField = SimpleFloatProperty(0f)
    private val _booleanField = SimpleBooleanProperty(false)
    private val _blobField = SimpleBlobProperty(ByteArray(0))

    init {
        map(Fields.STRING_FIELD, _stringField)
        map(Fields.DATE_FIELD, _dateField)
        map(Fields.TIME_FIELD, _timeField)
        map(Fields.DATE_TIME_FIELD, _dateTimeField)
        map(Fields.ZONED_DATE_TIME_FIELD, _zonedDateTimeField)
        map(Fields.LONG_FIELD, _longField)
        map(Fields.INT_FIELD, _intField)
        map(Fields.DOUBLE_FIELD, _doubleField)
        map(Fields.FLOAT_FIELD, _floatField)
        map(Fields.BOOLEAN_FIELD, _booleanField)
        map(Fields.BLOB_FIELD, _blobField)
    }

    var stringField: String
        get() = _stringField.get()
        set(stringField) = _stringField.set(stringField)

    var timeField: LocalTime
        get() = _timeField.get()
        set(dateField) = _timeField.set(dateField)

    var dateField: LocalDate
        get() = _dateField.get()
        set(dateField) = _dateField.set(dateField)

    var dateTimeField: LocalDateTime
        get() = _dateTimeField.get()
        set(dateTimeField) = _dateTimeField.set(dateTimeField)

    var zonedDateTimeField: ZonedDateTime
        get() = _zonedDateTimeField.get()
        set(zonedDateTimeField) = _zonedDateTimeField.set(zonedDateTimeField)

    var longField: Long
        get() = _longField.get()
        set(longField) = _longField.set(longField)

    var intField: Int?
        get() = _intField.value
        set(intField) { _intField.value=intField }

    var doubleField: Double
        get() = _doubleField.get()
        set(doubleField) = _doubleField.set(doubleField)

    var floatField: Float
        get() = _floatField.get()
        set(floatField) = _floatField.set(floatField)

    var booleanField: Boolean
        get() = _booleanField.get()
        set(booleanField) = _booleanField.set(booleanField)

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
                ", booleanField = $booleanField}"
    }
}