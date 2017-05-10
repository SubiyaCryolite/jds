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

import io.github.subiyacryolite.jds.annotations.JdsEntityAnnotation;
import io.github.subiyacryolite.jds.enums.JdsFieldType;
import javafx.beans.property.*;

import java.lang.annotation.Annotation;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;

/**
 * This class allows for all mapping operations in JDS, it also uses
 * {@link JdsEntityBase JdsEntityBase} to store overview data
 */
public abstract class JdsEntity extends JdsEntityBase {

    public JdsEntity() {
        if (getClass().isAnnotationPresent(JdsEntityAnnotation.class)) {
            JdsEntityAnnotation je = getClass().getAnnotation(JdsEntityAnnotation.class);
            setEntityCode(je.entityId());
            setEntityName(je.entityName());
        } else {
            throw new RuntimeException("You must annotate the class [" + getClass().getCanonicalName() + "] with [" + JdsEntityAnnotation.class + "]");
        }
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

    public String getEntityName() {
        return name.get();
    }

    private void setEntityName(String name) {
        this.name.set(name);
    }

    protected final void map(final JdsField jdsField, final SimpleIntegerProperty integerProperty) {
        if (integerProperty == null) {
            return;
        }
        if (jdsField.getType() == JdsFieldType.INT) {
            properties.add(jdsField.getId());
            integerProperties.put(jdsField.getId(), integerProperty);
        } else {
            throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
        }
    }

    protected final void map(final JdsField jdsField, final SimpleObjectProperty temporalProperty) {
        if (temporalProperty == null) {
            return;
        }
        Object temporal = temporalProperty.get();
        if (temporal instanceof LocalDateTime) {
            if (jdsField.getType() == JdsFieldType.DATE_TIME) {
                properties.add(jdsField.getId());
                localDateTimeProperties.put(jdsField.getId(), temporalProperty);
            } else {
                throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
            }
        } else if (temporal instanceof ZonedDateTime) {
            if (jdsField.getType() == JdsFieldType.ZONED_DATE_TIME) {
                properties.add(jdsField.getId());
                zonedDateTimeProperties.put(jdsField.getId(), temporalProperty);
            } else {
                throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
            }
        } else if (temporal instanceof LocalDate) {
            if (jdsField.getType() == JdsFieldType.DATE) {
                properties.add(jdsField.getId());
                localDateProperties.put(jdsField.getId(), temporalProperty);
            } else {
                throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
            }
        } else if (temporal instanceof LocalTime) {
            if (jdsField.getType() == JdsFieldType.TIME) {
                properties.add(jdsField.getId());
                localTimeProperties.put(jdsField.getId(), temporalProperty);
            } else {
                throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
            }
        }
    }

    protected final void map(final JdsField jdsField, final SimpleStringProperty stringProperty) {
        if (stringProperty == null) {
            return;
        }
        if (jdsField.getType() == JdsFieldType.TEXT) {
            properties.add(jdsField.getId());
            stringProperties.put(jdsField.getId(), stringProperty);
        } else {
            throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
        }
    }

    protected final void map(final JdsField jdsField, final SimpleFloatProperty floatProperty) {
        if (floatProperty == null) {
            return;
        }
        if (jdsField.getType() == JdsFieldType.FLOAT) {
            properties.add(jdsField.getId());
            floatProperties.put(jdsField.getId(), floatProperty);
        } else {
            throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
        }
    }

    protected final void map(final JdsField jdsField, final SimpleLongProperty longProperty) {
        if (longProperty == null) {
            return;
        }
        if (jdsField.getType() == JdsFieldType.LONG) {
            properties.add(jdsField.getId());
            longProperties.put(jdsField.getId(), longProperty);
        } else {
            throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
        }
    }

    protected final void map(final JdsField jdsField, final SimpleDoubleProperty doubleProperty) {
        if (doubleProperty == null) {
            return;
        }
        if (jdsField.getType() == JdsFieldType.DOUBLE) {
            properties.add(jdsField.getId());
            doubleProperties.put(jdsField.getId(), doubleProperty);
        } else {
            throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
        }
    }

    protected final void map(final JdsField jdsField, final SimpleBooleanProperty booleanProperty) {
        if (booleanProperty == null) {
            return;
        }
        if (jdsField.getType() == JdsFieldType.BOOLEAN) {
            properties.add(jdsField.getId());
            booleanProperties.put(jdsField.getId(), booleanProperty);
        } else {
            throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
        }
    }

    protected final void mapStrings(final JdsField jdsField, final SimpleListProperty<String> strings) {
        if (strings == null) {
            return;
        }
        if (jdsField.getType() == JdsFieldType.ARRAY_TEXT) {
            properties.add(jdsField.getId());
            stringArrayProperties.put(jdsField.getId(), strings);
        } else {
            throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
        }
    }

    protected final void mapFloats(final JdsField jdsField, final SimpleListProperty<Float> floats) {
        if (floats == null) {
            return;
        }
        if (jdsField.getType() == JdsFieldType.ARRAY_FLOAT) {
            properties.add(jdsField.getId());
            floatArrayProperties.put(jdsField.getId(), floats);
        } else {
            throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
        }
    }

    protected final void mapDoubles(final JdsField jdsField, final SimpleListProperty<Double> doubles) {
        if (doubles == null) {
            return;
        }
        if (jdsField.getType() == JdsFieldType.ARRAY_DOUBLE) {
            properties.add(jdsField.getId());
            doubleArrayProperties.put(jdsField.getId(), doubles);
        } else {
            throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
        }
    }

    protected final void mapLongs(final JdsField jdsField, final SimpleListProperty<Long> longs) {
        if (longs == null) {
            return;
        }
        if (jdsField.getType() == JdsFieldType.ARRAY_LONG) {
            properties.add(jdsField.getId());
            longArrayProperties.put(jdsField.getId(), longs);
        } else {
            throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
        }
    }

    protected final void mapEnums(final JdsFieldEnum jdsFieldEnum, final SimpleListProperty<String> strings) {
        if (strings == null) {
            return;
        }
        allEnums.add(jdsFieldEnum);
        if (jdsFieldEnum.getField().getType() == JdsFieldType.ENUM_TEXT) {
            properties.add(jdsFieldEnum.getField().getId());
            enumProperties.put(jdsFieldEnum, strings);
        } else {
            throw new RuntimeException("Please prepareDatabaseComponents field [" + jdsFieldEnum + "] to the correct type");
        }
    }

    protected final void map(Class<? extends JdsEntity> entity, final SimpleObjectProperty<? extends JdsEntity> property) {
        if (entity.isAnnotationPresent(JdsEntityAnnotation.class)) {
            Annotation annotation = entity.getAnnotation(JdsEntityAnnotation.class);
            JdsEntityAnnotation entityAnnotation = (JdsEntityAnnotation) annotation;
            if (!objectArrayProperties.containsKey(entityAnnotation.entityId()) && !objectProperties.containsKey(entityAnnotation.entityId())) {
                objectProperties.put(entityAnnotation.entityId(), (SimpleObjectProperty<JdsEntity>) property);
                objects.add(entityAnnotation.entityId());
            } else {
                throw new RuntimeException("You can only bind a class to one property. This class is already bound to one object or object array");
            }
        } else {
            throw new RuntimeException("You must annotate the class [" + entity.getCanonicalName() + "] with [" + JdsEntityAnnotation.class + "]");
        }
    }

    protected final void map(Class<? extends JdsEntity> entity, final SimpleListProperty<? extends JdsEntity> properties) {
        if (entity.isAnnotationPresent(JdsEntityAnnotation.class)) {
            Annotation annotation = entity.getAnnotation(JdsEntityAnnotation.class);
            JdsEntityAnnotation entityAnnotation = (JdsEntityAnnotation) annotation;
            if (!objectArrayProperties.containsKey(entityAnnotation.entityId()) && !objectProperties.containsKey(entityAnnotation.entityId())) {
                objectArrayProperties.put(entityAnnotation.entityId(), (SimpleListProperty<JdsEntity>) properties);
                objects.add(entityAnnotation.entityId());
            } else {
                throw new RuntimeException("You can only bind a class to one property. This class is already bound to one object or object array");
            }
        } else {
            throw new RuntimeException("You must annotate the class [" + entity.getCanonicalName() + "] with [" + JdsEntityAnnotation.class + "]");
        }
    }
}
