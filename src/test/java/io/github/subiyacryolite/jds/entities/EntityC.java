package io.github.subiyacryolite.jds.entities;

import io.github.subiyacryolite.jds.JdsField;
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation;
import io.github.subiyacryolite.jds.enums.JdsFieldType;
import javafx.beans.property.SimpleStringProperty;

/**
 * Created by ifunga on 01/07/2017.
 */
@JdsEntityAnnotation(entityId = 1002, entityName = "entityc")
public class EntityC extends EntityB {

    private static final JdsField ENTITY_C_FIELD = new JdsField(5000, "entity_c_field", JdsFieldType.TEXT);
    private final SimpleStringProperty field = new SimpleStringProperty("C");

    public EntityC() {
        map(ENTITY_C_FIELD, field);
    }

    public void setEntityCValue(String s) {
        field.set(s);
    }

    public String getEntityCValue() {
        return field.get();
    }
    @Override
    public String toString() {
        return "EntityB{" +
                "field A = " + getEntityAValue() +
                ",field B = " + getEntityBValue() +
                ",field C = " + getEntityCValue() +
                '}';
    }
}
