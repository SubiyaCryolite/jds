/*
* Jenesis Data Store Copyright (c) 2017 Ifunga Ndana. All rights reserved.
*
* 1. Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
*
* 2. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
*
* 3. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
*
* Neither the name Jenesis Data Store nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package io.github.subiyacryolite.jds;

import javafx.beans.property.*;
import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation;
import io.github.subiyacryolite.jds.enums.JdsFieldType;

import java.lang.annotation.Annotation;
import java.time.LocalDateTime;

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
            properties.add(jdsField.getId());
            integerProperties.put(jdsField.getId(), integerProperty);
        } else
            throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
    }

    protected final void map(final JdsField jdsField, final SimpleObjectProperty<LocalDateTime> localDateTimeProperty) {
        if (jdsField.getType() == JdsFieldType.DATE_TIME) {
            properties.add(jdsField.getId());
            dateProperties.put(jdsField.getId(), localDateTimeProperty);
        } else
            throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
    }

    protected final void map(final JdsField jdsField, final SimpleStringProperty stringProperty) {
        if (jdsField.getType() == JdsFieldType.TEXT) {
            properties.add(jdsField.getId());
            stringProperties.put(jdsField.getId(), stringProperty);
        } else
            throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
    }

    protected final void map(final JdsField jdsField, final SimpleFloatProperty floatProperty) {
        if (jdsField.getType() == JdsFieldType.FLOAT) {
            properties.add(jdsField.getId());
            floatProperties.put(jdsField.getId(), floatProperty);
        } else
            throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
    }

    protected final void map(final JdsField jdsField, final SimpleLongProperty longProperty) {
        if (jdsField.getType() == JdsFieldType.LONG) {
            properties.add(jdsField.getId());
            longProperties.put(jdsField.getId(), longProperty);
        } else
            throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
    }

    protected final void map(final JdsField jdsField, final SimpleDoubleProperty doubleProperty) {
        if (jdsField.getType() == JdsFieldType.DOUBLE) {
            properties.add(jdsField.getId());
            doubleProperties.put(jdsField.getId(), doubleProperty);
        } else
            throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
    }

    protected final void map(final JdsField jdsField, final SimpleBooleanProperty booleanProperty) {
        if (jdsField.getType() == JdsFieldType.BOOLEAN) {
            properties.add(jdsField.getId());
            booleanProperties.put(jdsField.getId(), booleanProperty);
        } else
            throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
    }

    protected final void mapStrings(final JdsField jdsField, final SimpleListProperty<String> strings) {
        if (jdsField.getType() == JdsFieldType.ARRAY_TEXT) {
            properties.add(jdsField.getId());
            stringArrayProperties.put(jdsField.getId(), strings);
        } else
            throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
    }

    protected final void mapFloats(final JdsField jdsField, final SimpleListProperty<Float> floats) {
        if (jdsField.getType() == JdsFieldType.ARRAY_FLOAT) {
            properties.add(jdsField.getId());
            floatArrayProperties.put(jdsField.getId(), floats);
        } else
            throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
    }

    protected final void mapDoubles(final JdsField jdsField, final SimpleListProperty<Double> doubles) {
        if (jdsField.getType() == JdsFieldType.ARRAY_DOUBLE) {
            properties.add(jdsField.getId());
            doubleArrayProperties.put(jdsField.getId(), doubles);
        } else
            throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
    }

    protected final void mapLongs(final JdsField jdsField, final SimpleListProperty<Long> longs) {
        if (jdsField.getType() == JdsFieldType.ARRAY_LONG) {
            properties.add(jdsField.getId());
            longArrayProperties.put(jdsField.getId(), longs);
        } else
            throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
    }

    protected final void mapEnums(final JdsFieldEnum jdsFieldEnum, final SimpleListProperty<String> strings) {
        allEnums.add(jdsFieldEnum);
        if (jdsFieldEnum.getField().getType() == JdsFieldType.ENUM_TEXT) {
            properties.add(jdsFieldEnum.getField().getId());
            enumProperties.put(jdsFieldEnum, strings);
        } else
            throw new RuntimeException("Please prepareDatabaseComponents field [" + jdsFieldEnum + "] to the correct type");
    }

    protected final void map(Class<? extends Object> entity, final SimpleObjectProperty<? extends JdsEntity> property) {
        if (entity.isAnnotationPresent(JdsEntityAnnotation.class)) {
            Annotation annotation = entity.getAnnotation(JdsEntityAnnotation.class);
            JdsEntityAnnotation entityAnnotation = (JdsEntityAnnotation) annotation;
            if (!objectArrayProperties.containsKey(entityAnnotation.entityId()) && !objectProperties.containsKey(entityAnnotation.entityId())) {
                objectProperties.put(entityAnnotation.entityId(), property);
                objects.add(entityAnnotation.entityId());
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
                objectArrayProperties.put(entityAnnotation.entityId(), properties);
                objects.add(entityAnnotation.entityId());
            } else
                throw new RuntimeException("You can only bind a class to one property. This class is already bound to one object or object array");
        } else
            throw new RuntimeException("You must annotate the class [" + entity.getCanonicalName() + "] with [" + JdsEntityAnnotation.class + "]");
    }
}
