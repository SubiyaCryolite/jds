package entities;

import io.github.subiyacryolite.jds.JdsEntity;
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation;
import io.github.subiyacryolite.jds.events.*;
import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;

import static fields.JdsExampleFields.*;

/**
 * Created by ifunga on 12/04/2017.
 */
@JdsEntityAnnotation(entityId = 3, entityName = "Type Class")
public class JdsExample extends JdsEntity implements JdsLoadListener, JdsSaveListener {
    private final SimpleStringProperty stringField = new SimpleStringProperty("");
    private final SimpleObjectProperty<LocalTime> timeField = new SimpleObjectProperty<LocalTime>(LocalTime.now());
    private final SimpleObjectProperty<LocalDate> dateField = new SimpleObjectProperty<LocalDate>(LocalDate.now());
    private final SimpleObjectProperty<LocalDateTime> dateTimeField = new SimpleObjectProperty<LocalDateTime>(LocalDateTime.now());
    private final SimpleObjectProperty<ZonedDateTime> zonedDateTimeField = new SimpleObjectProperty<ZonedDateTime>(ZonedDateTime.now());
    private final SimpleLongProperty longField = new SimpleLongProperty(0);
    private final SimpleIntegerProperty intField = new SimpleIntegerProperty(0);
    private final SimpleDoubleProperty doubleField = new SimpleDoubleProperty(0);
    private final SimpleFloatProperty floatField = new SimpleFloatProperty(0);
    private final SimpleBooleanProperty booleanField = new SimpleBooleanProperty(false);
    private final SimpleBlobProperty blobField = new SimpleBlobProperty(new byte[0]);

    public JdsExample() {
        map(STRING_FIELD, stringField);
        map(DATE_FIELD, dateField);
        map(TIME_FIELD, timeField);
        map(DATE_TIME_FIELD, dateTimeField);
        map(ZONED_DATE_TIME_FIELD, zonedDateTimeField);
        map(LONG_FIELD, longField);
        map(INT_FIELD, intField);
        map(DOUBLE_FIELD, doubleField);
        map(FLOAT_FIELD, floatField);
        map(BOOLEAN_FIELD, booleanField);
        map(BLOB_FIELD, blobField);
    }

    public JdsExample(String str, LocalTime timeField, LocalDate localDate, LocalDateTime localDateTime, ZonedDateTime zonedDateTime, long l, int i, double d, float f, boolean b) {
        this();
        setStringField(str);
        setTimeField(timeField);
        setDateField(localDate);
        setDateTimeField(localDateTime);
        setZonedDateTimeField(zonedDateTime);
        setLongField(l);
        setIntField(i);
        setDoubleField(d);
        setFloatField(f);
        setBooleanField(b);
    }

    public String getStringField() {
        return stringField.get();
    }

    public void setStringField(String stringField) {
        this.stringField.set(stringField);
    }

    public LocalTime getTimeField() {
        return timeField.get();
    }

    public void setTimeField(LocalTime dateField) {
        this.timeField.set(dateField);
    }

    public LocalDate getDateField() {
        return dateField.get();
    }

    public void setDateField(LocalDate dateField) {
        this.dateField.set(dateField);
    }

    public LocalDateTime getDateTimeField() {
        return dateTimeField.get();
    }

    public void setDateTimeField(LocalDateTime dateTimeField) {
        this.dateTimeField.set(dateTimeField);
    }

    public ZonedDateTime getZonedDateTimeField() {
        return zonedDateTimeField.get();
    }

    public void setZonedDateTimeField(ZonedDateTime zonedDateTimeField) {
        this.zonedDateTimeField.set(zonedDateTimeField);
    }

    public long getLongField() {
        return longField.get();
    }

    public void setLongField(long longField) {
        this.longField.set(longField);
    }

    public int getIntField() {
        return intField.get();
    }

    public void setIntField(int intField) {
        this.intField.set(intField);
    }

    public double getDoubleField() {
        return doubleField.get();
    }

    public void setDoubleField(double doubleField) {
        this.doubleField.set(doubleField);
    }

    public float getFloatField() {
        return floatField.get();
    }

    public void setFloatField(float floatField) {
        this.floatField.set(floatField);
    }

    public boolean getBooleanField() {
        return booleanField.get();
    }

    public void setBooleanField(boolean booleanField) {
        this.booleanField.set(booleanField);
    }

    @Override
    public String toString() {
        return "JdsExample{" +
                "stringField = " + getStringField() +
                ", timeField = " + getTimeField() +
                ", dateField = " + getDateField() +
                ", dateTimeField = " + getDateTimeField() +
                ", zonedDateTimeField = " + getZonedDateTimeField() +
                ", longField = " + getLongField() +
                ", intField = " + getIntField() +
                ", doubleField = " + getDoubleField() +
                ", floatField = " + getFloatField() +
                ", booleanField = " + getBooleanField() +
                '}';
    }

    @Override
    public void onPreSave(OnPreSaveEventArguments arguments) {
        System.out.printf("Pre-Save :: Batch Sequence[%s]. Batch Size [%s]. Outer Batch Step [%s]\n", arguments.getBatchSequence(), arguments.getBatchSize(), arguments.getOuterBatchStep());
    }

    @Override
    public void onPostSave(OnPostSaveEventArguments arguments) {
        System.out.printf("Post-Save :: Batch Sequence[%s]. Batch Size [%s]\n", arguments.getBatchSequence(), arguments.getBatchSize());
    }

    @Override
    public void onPreLoad(OnPreLoadEventArguments arguments) {
        System.out.printf("Pre-Load :: Batch Sequence[%s]. Batch Size [%s]. Entity Guid [%s]\n", arguments.getBatchSequence(), arguments.getBatchSize(), arguments.getEntityGuid());
    }

    @Override
    public void onPostLoad(OnPostLoadEventArguments arguments) {
        System.out.printf("Post-Load :: Entity Guid [%s]\n", arguments.getEntityGuid());
    }
}
