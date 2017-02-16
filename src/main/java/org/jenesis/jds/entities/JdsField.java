package org.jenesis.jds.entities;

import org.jenesis.jds.enums.JdsFieldType;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.HashMap;

import static org.jenesis.jds.enums.JdsFieldType.TEXT;

/**
 * Created by ifunga on 09/02/2017.
 */
public class JdsField {
    private static HashMap<Long, String> fields = new HashMap<>();

    private static void bind(final JdsField jdsField) {
        if (!fields.containsKey(jdsField.getId()))
            fields.put(jdsField.getId(), jdsField.getName());
        else
            throw new RuntimeException(String.format("This jdsField ID [%s] is already bound", jdsField.getId()));
    }

    private final SimpleLongProperty id;
    private final SimpleStringProperty name;
    private JdsFieldType type;

    private JdsField() {
        id = new SimpleLongProperty();
        name = new SimpleStringProperty();
        type = TEXT;
    }

    public JdsField(long id, String name, JdsFieldType type) {
        this();
        setId(id);
        setName(name);
        setType(type);
        bind(this);
    }

    public long getId() {
        return id.get();
    }

    private void setId(long id) {
        this.id.set(id);
    }

    public String getName() {
        return name.get();
    }

    private void setName(String name) {
        this.name.set(name);
    }

    private void setType(JdsFieldType type) {
        this.type = type;
    }

    public JdsFieldType getType() {
        return this.type;
    }

    @Override
    public String toString() {
        return "JdsField{" +
                "id=" + id.get() +
                ", name=" + name.get() +
                ", type=" + type +
                '}';
    }
}
