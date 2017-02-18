package org.jenesis.jds;

import javafx.beans.property.SimpleObjectProperty;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by ifunga on 11/02/2017.
 */
public class JdsFieldEnum {

    private static final HashMap<Long, JdsFieldEnum> fieldEnums = new HashMap<>();
    private final SimpleObjectProperty<JdsField> field;
    private final LinkedList<String> sequenceValues;//keep order at all times

    private JdsFieldEnum() {
        this.field = new SimpleObjectProperty();
        this.sequenceValues = new LinkedList<>();
    }

    public JdsFieldEnum(final JdsField jdsField, final String... values) {
        this();
        this.field.set(jdsField);
        for (String value : values)
            this.sequenceValues.addLast(value);
        bind();
    }

    private void bind() {
        if (!fieldEnums.containsKey(field.get()))
            fieldEnums.put(field.get().getId(), this);
    }

    public static JdsFieldEnum get(final JdsField jdsField) {
        if (fieldEnums.containsKey(jdsField.getId()))
            return fieldEnums.get(jdsField.getId());
        else
            throw new RuntimeException(String.format("This jdsField [%s] has not been bound to any enums", jdsField));
    }

    public static JdsFieldEnum get(final long fieldId) {
        if (fieldEnums.containsKey(fieldId))
            return fieldEnums.get(fieldId);
        else
            throw new RuntimeException(String.format("This field [%s] has not been bound to any enums", fieldId));
    }

    public JdsField getField() {
        return field.get();
    }

    public LinkedList<String> getSequenceValues() {
        return this.sequenceValues;
    }

    @Override
    public String toString() {
        return "JdsFieldEnum{" +
                "field=" + field.get() +
                ", sequenceValues=" + sequenceValues +
                '}';
    }

    public int getIndex(String enumText) {
        return sequenceValues.indexOf(enumText);
    }

    public String getValue(int index) {
        return (index >= sequenceValues.size()) ? "" : sequenceValues.get(index);
    }
}
