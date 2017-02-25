package org.jenesis.jds;

import javafx.beans.property.*;
import org.jenesis.jds.annotations.JdsEntityAnnotation;
import org.jenesis.jds.enums.JdsFieldType;
import org.jenesis.jds.listeners.*;

import java.lang.annotation.Annotation;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by ifunga on 04/02/2017.
 */
public abstract class JdsEntity extends JdsEntityBase {
    private final SimpleStringProperty name;

    public JdsEntity() {
        this.name = new SimpleStringProperty();
        if (getClass().isAnnotationPresent(JdsEntityAnnotation.class)) {
            JdsEntityAnnotation je = getClass().getAnnotation(JdsEntityAnnotation.class);
            setEntityCode(je.entityId());
            setEntityName(je.entityName());
        } else
            throw new RuntimeException("You must annotate the class [" + getClass().getCanonicalName() + "] with [" + JdsEntityAnnotation.class + "]");
    }

    public String getEntityGuid() {
        return getOverview().getEntityGuid();
    }

    public void setEntityGuid(String actionId) {
        getOverview().setEntityGuid(actionId);
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

    private void setEntityCode(long serviceCode) {
        this.getOverview().setEntityCode(serviceCode);
    }

    private void setEntityName(String name) {
        this.name.set(name);
    }

    public String getEntityName() {
        return name.get();
    }

    protected final void map(final JdsField jdsField, final SimpleIntegerProperty integerProperty) {
        if (jdsField.getType() == JdsFieldType.INT) {
            NumberListener numberListener = new NumberListener();
            allFields.put(jdsField.getId(), numberListener);
            integerProperties.put(jdsField.getId(), integerProperty);
            integerProperty.addListener(numberListener);
        } else
            throw new RuntimeException("Please init jdsField [" + jdsField + "] to the correct type");
    }

    protected final void map(final JdsField jdsField, final SimpleObjectProperty<LocalDateTime> localDateTimeProperty) {
        if (jdsField.getType() == JdsFieldType.DATE_TIME) {
            LocalDateTimeListener localDateTimeListener = new LocalDateTimeListener();
            allFields.put(jdsField.getId(), localDateTimeListener);
            dateProperties.put(jdsField.getId(), localDateTimeProperty);
            localDateTimeProperty.addListener(localDateTimeListener);
        } else
            throw new RuntimeException("Please init jdsField [" + jdsField + "] to the correct type");
    }

    protected final void map(final JdsField jdsField, final SimpleStringProperty stringProperty) {
        if (jdsField.getType() == JdsFieldType.TEXT) {
            StringListener stringListener = new StringListener();
            allFields.put(jdsField.getId(), stringListener);
            stringProperties.put(jdsField.getId(), stringProperty);
            stringProperty.addListener(stringListener);
        } else
            throw new RuntimeException("Please init jdsField [" + jdsField + "] to the correct type");
    }

    protected final void map(final JdsField jdsField, final SimpleFloatProperty floatProperty) {
        if (jdsField.getType() == JdsFieldType.FLOAT) {
            NumberListener numberListener = new NumberListener();
            allFields.put(jdsField.getId(), numberListener);
            floatProperties.put(jdsField.getId(), floatProperty);
            floatProperty.addListener(numberListener);
        } else
            throw new RuntimeException("Please init jdsField [" + jdsField + "] to the correct type");
    }

    protected final void map(final JdsField jdsField, final SimpleLongProperty longProperty) {
        if (jdsField.getType() == JdsFieldType.LONG) {
            NumberListener numberListener = new NumberListener();
            allFields.put(jdsField.getId(), numberListener);
            longProperties.put(jdsField.getId(), longProperty);
            longProperty.addListener(numberListener);
        } else
            throw new RuntimeException("Please init jdsField [" + jdsField + "] to the correct type");
    }

    protected final void map(final JdsField jdsField, final SimpleDoubleProperty doubleProperty) {
        if (jdsField.getType() == JdsFieldType.DOUBLE) {
            NumberListener numberListener = new NumberListener();
            allFields.put(jdsField.getId(), numberListener);
            doubleProperties.put(jdsField.getId(), doubleProperty);
            doubleProperty.addListener(numberListener);
        } else
            throw new RuntimeException("Please init jdsField [" + jdsField + "] to the correct type");
    }

    protected final void mapStrings(final JdsField jdsField, final SimpleListProperty<String> strings) {
        if (jdsField.getType() == JdsFieldType.ARRAY_TEXT) {
            ListStringListener listStringListener = new ListStringListener();
            allFields.put(jdsField.getId(), listStringListener);
            stringArrayProperties.put(jdsField.getId(), strings);
            strings.addListener(listStringListener);
        } else
            throw new RuntimeException("Please init jdsField [" + jdsField + "] to the correct type");
    }

    protected final void mapFloats(final JdsField jdsField, final SimpleListProperty<Float> floats) {
        if (jdsField.getType() == JdsFieldType.ARRAY_FLOAT) {
            ListNumberListener listStringListener = new ListNumberListener();
            allFields.put(jdsField.getId(), listStringListener);
            floatArrayProperties.put(jdsField.getId(), floats);
            floats.addListener(listStringListener);
        } else
            throw new RuntimeException("Please init jdsField [" + jdsField + "] to the correct type");
    }

    protected final void mapDoubles(final JdsField jdsField, final SimpleListProperty<Double> doubles) {
        if (jdsField.getType() == JdsFieldType.ARRAY_DOUBLE) {
            ListNumberListener listStringListener = new ListNumberListener();
            allFields.put(jdsField.getId(), listStringListener);
            doubleArrayProperties.put(jdsField.getId(), doubles);
            doubles.addListener(listStringListener);
        } else
            throw new RuntimeException("Please init jdsField [" + jdsField + "] to the correct type");
    }

    protected final void mapLongs(final JdsField jdsField, final SimpleListProperty<Long> longs) {
        if (jdsField.getType() == JdsFieldType.ARRAY_LONG) {
            ListNumberListener listStringListener = new ListNumberListener();
            allFields.put(jdsField.getId(), listStringListener);
            longArrayProperties.put(jdsField.getId(), longs);
            longs.addListener(listStringListener);
        } else
            throw new RuntimeException("Please init jdsField [" + jdsField + "] to the correct type");
    }

    protected final void mapEnums(final JdsFieldEnum jdsFieldEnum, final SimpleListProperty<String> strings) {
        allEnums.add(jdsFieldEnum);
        if (jdsFieldEnum.getField().getType() == JdsFieldType.ENUM_TEXT) {
            ListStringListener listStringListener = new ListStringListener();
            allFields.put(jdsFieldEnum.getField().getId(), listStringListener);
            enumProperties.put(jdsFieldEnum, strings);
            strings.addListener(listStringListener);
        } else
            throw new RuntimeException("Please init field [" + jdsFieldEnum + "] to the correct type");
    }

    protected final void map(Class<? extends Object> entity, final SimpleObjectProperty<? extends JdsEntity> property) {
        if (entity.isAnnotationPresent(JdsEntityAnnotation.class)) {
            Annotation annotation = entity.getAnnotation(JdsEntityAnnotation.class);
            JdsEntityAnnotation entityAnnotation = (JdsEntityAnnotation) annotation;
            if (!objectArrayProperties.containsKey(entityAnnotation.entityId()) && !objectProperties.containsKey(entityAnnotation.entityId())) {
                ObjectListener objectListener = new ObjectListener();
                objectProperties.put(entityAnnotation.entityId(), property);
                allObjects.put(entityAnnotation.entityId(), objectListener);
                property.addListener(objectListener);
            } else
                throw new RuntimeException("You can only bind a class to one property. This class is already bound to one object or object array");
        } else
            throw new RuntimeException("You must annotate the class [" + entity.getCanonicalName() + "] with [" + JdsEntityAnnotation.class + "]");
    }

    protected final void map(Class<? extends Object> entity, final SimpleListProperty<? extends JdsEntity> properties) {
        if (entity.isAnnotationPresent(JdsEntityAnnotation.class)) {
            Annotation annotation = entity.getAnnotation(JdsEntityAnnotation.class);
            JdsEntityAnnotation entityAnnotation = (JdsEntityAnnotation) annotation;
            if (!objectArrayProperties.containsKey(entityAnnotation.entityId()) && !objectProperties.containsKey(entityAnnotation.entityId())) {
                ListObjectListener listObjectListener = new ListObjectListener();
                objectArrayProperties.put(entityAnnotation.entityId(), properties);
                allObjects.put(entityAnnotation.entityId(), listObjectListener);
                properties.addListener(listObjectListener);
            } else
                throw new RuntimeException("You can only bind a class to one property. This class is already bound to one object or object array");
        } else
            throw new RuntimeException("You must annotate the class [" + entity.getCanonicalName() + "] with [" + JdsEntityAnnotation.class + "]");
    }

    public final List<Long> modifiedFields() {
        return allFields.entrySet().parallelStream().filter(set -> set.getValue().getChanged() == true).map(p -> p.getKey()).collect(Collectors.toList());
    }

    public final List<Long> modifiedObjects() {
        return allObjects.entrySet().parallelStream().filter(set -> set.getValue().getChanged() == true).map(p -> p.getKey()).collect(Collectors.toList());
    }
}
