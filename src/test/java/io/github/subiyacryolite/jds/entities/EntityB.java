package io.github.subiyacryolite.jds.entities;

import io.github.subiyacryolite.jds.JdsField;
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation;
import io.github.subiyacryolite.jds.enums.JdsFieldType;
import javafx.beans.property.SimpleStringProperty;

/**
 * Created by ifunga on 01/07/2017.
 */
@JdsEntityAnnotation(entityId = 1001, entityName = "entityb")
public class EntityB extends EntityA {
    private static final JdsField ENTITY_B_FIELD = new JdsField(5001, "entity_b_field", JdsFieldType.TEXT);
    private final SimpleStringProperty field = new SimpleStringProperty("C");

    public EntityB() {
        map(ENTITY_B_FIELD, field);
    }

    public void setEntityBValue(String s) {
        field.set(s);
    }

    public String getEntityBValue() {
        return field.get();
    }

    @Override
    public String toString() {
        return "EntityB{" +
                "field A = " + getEntityAValue() +
                ",field B = " + getEntityBValue() +
                '}';
    }
}
