package io.github.subiyacryolite.jds.entities;

import io.github.subiyacryolite.jds.JdsEntity;
import io.github.subiyacryolite.jds.JdsField;
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation;
import io.github.subiyacryolite.jds.enums.JdsFieldType;
import javafx.beans.property.SimpleStringProperty;

/**
 * Created by ifunga on 01/07/2017.
 */
@JdsEntityAnnotation(entityId = 1000, entityName = "entitya")
public class EntityA extends JdsEntity {
    private static final JdsField ENTITY_A_FIELD = new JdsField(5002, "entity_a_field", JdsFieldType.TEXT);
    private final SimpleStringProperty field = new SimpleStringProperty("C");

    public EntityA() {
        map(ENTITY_A_FIELD, field);
    }

    public void setEntityAValue(String s) {
        field.set(s);
    }

    public String getEntityAValue() {
        return field.get();
    }

    @Override
    public String toString() {
        return "EntityA{" +
                "field A = " + getEntityAValue() +
                '}';
    }
}
