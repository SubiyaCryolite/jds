package org.jenesis.jds;

import javafx.beans.property.*;
import org.jenesis.jds.annotations.JdsEntityAnnotation;
import org.jenesis.jds.enums.JdsFieldType;

import java.lang.annotation.Annotation;
import java.time.LocalDateTime;

/**
 * Created by ifunga on 04/02/2017.
 */
public abstract class JdsEntity extends Jds_Entity {
    private final SimpleStringProperty name;
    public JdsEntity() {
        this.name = new SimpleStringProperty();
        if (getClass().isAnnotationPresent(JdsEntityAnnotation.class)) {
            Annotation annotation = getClass().getAnnotation(JdsEntityAnnotation.class);
            JdsEntityAnnotation je = (JdsEntityAnnotation) annotation;
            setEntityCode(je.entityCode());
            setEntityName(je.entityName());
            //ClassMappings.add(getClass());
        } else
            throw new RuntimeException("You must annotate the class [" + getClass().getCanonicalName() + "] with [" + JdsEntityAnnotation.class + "]");
    }

    public String getActionId() {
        return getOverview().getActionId();
    }

    public void setActionId(String actionId) {
        getOverview().setActionId(actionId);
    }

    public LocalDateTime getDateCreated() {
        return getOverview().getDateCreated();
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.getOverview().setDateCreated(dateCreated);
    }

    public LocalDateTime getDateModified() {
        return this.getOverview().getDateModified();
    }

    public void setDateModified(LocalDateTime dateModified) {
        this.getOverview().setDateModified(dateModified);
    }

    public long getEntityCode() {
        return this.getOverview().getEntityCode();
    }

    public void setEntityCode(long serviceCode) {
        this.getOverview().setEntityCode(serviceCode);
    }

    private void setEntityName(String name) {
        this.name.set(name);
    }

    public String getEntityName() {
        return name.get();
    }

    protected final void map(final JdsField jdsField, final SimpleIntegerProperty prop) {
        allFields.add(jdsField.getId());
        if (jdsField.getType() == JdsFieldType.INT) {
            integerProperties.put(jdsField.getId(), prop);
        } else
            throw new RuntimeException("Please init jdsField [" + jdsField + "] to the correct type");
    }

    protected final void map(final JdsField jdsField, final SimpleObjectProperty<LocalDateTime> prop) {
        allFields.add(jdsField.getId());
        if (jdsField.getType() == JdsFieldType.DATE_TIME) {
            dateProperties.put(jdsField.getId(), prop);
        } else
            throw new RuntimeException("Please init jdsField [" + jdsField + "] to the correct type");
    }

    protected final void map(final JdsField jdsField, final SimpleStringProperty prop) {
        allFields.add(jdsField.getId());
        if (jdsField.getType() == JdsFieldType.TEXT) {
            stringProperties.put(jdsField.getId(), prop);
        } else
            throw new RuntimeException("Please init jdsField [" + jdsField + "] to the correct type");
    }

    protected final void map(final JdsField jdsField, final SimpleFloatProperty prop) {
        allFields.add(jdsField.getId());
        if (jdsField.getType() == JdsFieldType.FLOAT) {
            floatProperties.put(jdsField.getId(), prop);
        } else
            throw new RuntimeException("Please init jdsField [" + jdsField + "] to the correct type");
    }

    protected final void map(final JdsField jdsField, final SimpleLongProperty prop) {
        allFields.add(jdsField.getId());
        if (jdsField.getType() == JdsFieldType.LONG) {
            longProperties.put(jdsField.getId(), prop);
        } else
            throw new RuntimeException("Please init jdsField [" + jdsField + "] to the correct type");
    }

    protected final void map(final JdsField jdsField, final SimpleDoubleProperty prop) {
        allFields.add(jdsField.getId());
        if (jdsField.getType() == JdsFieldType.DOUBLE) {
            doubleProperties.put(jdsField.getId(), prop);
        } else
            throw new RuntimeException("Please init jdsField [" + jdsField + "] to the correct type");
    }

    protected final void map(final JdsField jdsField, final SimpleListProperty<String> prop) {
        allFields.add(jdsField.getId());
        if (jdsField.getType() == JdsFieldType.ARRAY_TEXT) {
            stringArrayProperties.put(jdsField.getId(), prop);
        } else
            throw new RuntimeException("Please init jdsField [" + jdsField + "] to the correct type");
    }

    protected final void map(final JdsFieldEnum jdsFieldEnum, final SimpleListProperty<String> prop) {
        allEnums.add(jdsFieldEnum);
        if (jdsFieldEnum.getField().getType() == JdsFieldType.ENUM_TEXT) {
            enumProperties.put(jdsFieldEnum, prop);
        } else
            throw new RuntimeException("Please init field [" + jdsFieldEnum + "] to the correct type");
    }

    protected final void map(Class<? extends Object> entity, final SimpleObjectProperty<? extends JdsEntity> properties) {
        if (entity.isAnnotationPresent(JdsEntityAnnotation.class)) {
            Annotation annotation = entity.getAnnotation(JdsEntityAnnotation.class);
            JdsEntityAnnotation je = (JdsEntityAnnotation) annotation;
            objectProperties.put(je.entityCode(), properties);
        } else
            throw new RuntimeException("You must annotate the class [" + entity.getCanonicalName() + "] with [" + JdsEntityAnnotation.class + "]");
    }

    protected final void map(Class<? extends Object> entity, final SimpleListProperty<? extends JdsEntity> properties) {
        if (entity.isAnnotationPresent(JdsEntityAnnotation.class)) {
            Annotation annotation = entity.getAnnotation(JdsEntityAnnotation.class);
            JdsEntityAnnotation je = (JdsEntityAnnotation) annotation;
            objectArrayProperties.put(je.entityCode(), properties);
        } else
            throw new RuntimeException("You must annotate the class [" + entity.getCanonicalName() + "] with [" + JdsEntityAnnotation.class + "]");
    }
}
