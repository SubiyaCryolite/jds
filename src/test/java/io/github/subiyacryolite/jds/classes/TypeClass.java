package io.github.subiyacryolite.jds.classes;

import io.github.subiyacryolite.jds.JdsEntity;
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation;
import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;

/**
 * Created by ifunga on 12/04/2017.
 */
@JdsEntityAnnotation(entityId = 3, entityName = "Type Class")
public class TypeClass extends JdsEntity {
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

    public TypeClass() {
        map(NewTestFields.STRING_FIELD, stringField);
        map(NewTestFields.DATE_FIELD, dateField);
        map(NewTestFields.TIME_FIELD, timeField);
        map(NewTestFields.DATE_TIME_FIELD, dateTimeField);
        map(NewTestFields.ZONED_DATE_TIME_FIELD, zonedDateTimeField);
        map(NewTestFields.LONG_FIELD, longField);
        map(NewTestFields.INT_FIELD, intField);
        map(NewTestFields.DOUBLE_FIELD, doubleField);
        map(NewTestFields.FLOAT_FIELD, floatField);
        map(NewTestFields.BOOLEAN_FIELD, booleanField);
    }

    public TypeClass(String str, LocalTime timeField, LocalDate localDate, LocalDateTime localDateTime, ZonedDateTime zonedDateTime, long l, int i, double d, float f, boolean b) {
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
        return "TypeClass{" +
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
}
