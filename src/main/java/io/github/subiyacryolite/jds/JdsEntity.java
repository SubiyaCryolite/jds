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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Iterator;

/**
 * This class allows for all mapping operations in JDS, it also uses
 * {@link JdsEntityBase JdsEntityBase} to store overview data
 */
public abstract class JdsEntity extends JdsEntityBase implements IJdsEntity{

    /**
     *
     */
    public JdsEntity() {
        if (getClass().isAnnotationPresent(JdsEntityAnnotation.class)) {
            JdsEntityAnnotation entityAnnotation = getClass().getAnnotation(JdsEntityAnnotation.class);
            getOverview().setEntityId(entityAnnotation.entityId());
            setEntityName(entityAnnotation.entityName());
        } else {
            throw new RuntimeException("You must annotate the class [" + getClass().getCanonicalName() + "] with [" + JdsEntityAnnotation.class + "]");
        }
    }
    /**
     * @return
     */
    public String getEntityName() {
        return name.get();
    }

    /**
     * @param name
     */
    private void setEntityName(String name) {
        this.name.set(name);
    }

    /**
     * @param jdsField
     * @param integerProperty
     */
    protected final void map(final JdsField jdsField, final SimpleBlobProperty integerProperty) {
        if (integerProperty == null) {
            return;
        }
        if (jdsField.getType() == JdsFieldType.BLOB) {
            properties.put(jdsField.getId(), jdsField.getName());
            types.put(jdsField.getId(), jdsField.getType().toString());
            blobProperties.put(jdsField.getId(), integerProperty);
        } else {
            throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
        }
    }

    /**
     * @param jdsField
     * @param integerProperty
     */
    protected final void map(final JdsField jdsField, final SimpleIntegerProperty integerProperty) {
        if (integerProperty == null) {
            return;
        }
        if (jdsField.getType() == JdsFieldType.INT) {
            properties.put(jdsField.getId(), jdsField.getName());
            types.put(jdsField.getId(), jdsField.getType().toString());
            integerProperties.put(jdsField.getId(), integerProperty);
        } else {
            throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
        }
    }

    /**
     * @param jdsField
     * @param temporalProperty
     */
    protected final void map(final JdsField jdsField, final SimpleObjectProperty temporalProperty) {
        if (temporalProperty == null) {
            return;
        }
        Object temporal = temporalProperty.get();
        if (temporal instanceof LocalDateTime) {
            if (jdsField.getType() == JdsFieldType.DATE_TIME) {
                properties.put(jdsField.getId(), jdsField.getName());
                types.put(jdsField.getId(), jdsField.getType().toString());
                localDateTimeProperties.put(jdsField.getId(), temporalProperty);
            } else {
                throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
            }
        } else if (temporal instanceof ZonedDateTime) {
            if (jdsField.getType() == JdsFieldType.ZONED_DATE_TIME) {
                properties.put(jdsField.getId(), jdsField.getName());
                types.put(jdsField.getId(), jdsField.getType().toString());
                zonedDateTimeProperties.put(jdsField.getId(), temporalProperty);
            } else {
                throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
            }
        } else if (temporal instanceof LocalDate) {
            if (jdsField.getType() == JdsFieldType.DATE) {
                properties.put(jdsField.getId(), jdsField.getName());
                types.put(jdsField.getId(), jdsField.getType().toString());
                localDateProperties.put(jdsField.getId(), temporalProperty);
            } else {
                throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
            }
        } else if (temporal instanceof LocalTime) {
            if (jdsField.getType() == JdsFieldType.TIME) {
                properties.put(jdsField.getId(), jdsField.getName());
                types.put(jdsField.getId(), jdsField.getType().toString());
                localTimeProperties.put(jdsField.getId(), temporalProperty);
            } else {
                throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
            }
        }
    }

    /**
     * @param jdsField
     * @param stringProperty
     */
    protected final void map(final JdsField jdsField, final SimpleStringProperty stringProperty) {
        if (stringProperty == null) {
            return;
        }
        if (jdsField.getType() == JdsFieldType.TEXT) {
            properties.put(jdsField.getId(), jdsField.getName());
            types.put(jdsField.getId(), jdsField.getType().toString());
            stringProperties.put(jdsField.getId(), stringProperty);
        } else {
            throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
        }
    }

    /**
     * @param jdsField
     * @param floatProperty
     */
    protected final void map(final JdsField jdsField, final SimpleFloatProperty floatProperty) {
        if (floatProperty == null) {
            return;
        }
        if (jdsField.getType() == JdsFieldType.FLOAT) {
            properties.put(jdsField.getId(), jdsField.getName());
            types.put(jdsField.getId(), jdsField.getType().toString());
            floatProperties.put(jdsField.getId(), floatProperty);
        } else {
            throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
        }
    }

    /**
     * @param jdsField
     * @param longProperty
     */
    protected final void map(final JdsField jdsField, final SimpleLongProperty longProperty) {
        if (longProperty == null) {
            return;
        }
        if (jdsField.getType() == JdsFieldType.LONG) {
            properties.put(jdsField.getId(), jdsField.getName());
            types.put(jdsField.getId(), jdsField.getType().toString());
            longProperties.put(jdsField.getId(), longProperty);
        } else {
            throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
        }
    }

    /**
     * @param jdsField
     * @param doubleProperty
     */
    protected final void map(final JdsField jdsField, final SimpleDoubleProperty doubleProperty) {
        if (doubleProperty == null) {
            return;
        }
        if (jdsField.getType() == JdsFieldType.DOUBLE) {
            properties.put(jdsField.getId(), jdsField.getName());
            types.put(jdsField.getId(), jdsField.getType().toString());
            doubleProperties.put(jdsField.getId(), doubleProperty);
        } else {
            throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
        }
    }

    /**
     * @param jdsField
     * @param booleanProperty
     */
    protected final void map(final JdsField jdsField, final SimpleBooleanProperty booleanProperty) {
        if (booleanProperty == null) {
            return;
        }
        if (jdsField.getType() == JdsFieldType.BOOLEAN) {
            properties.put(jdsField.getId(), jdsField.getName());
            types.put(jdsField.getId(), jdsField.getType().toString());
            booleanProperties.put(jdsField.getId(), booleanProperty);
        } else {
            throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
        }
    }

    /**
     * @param jdsField
     * @param strings
     */
    protected final void mapStrings(final JdsField jdsField, final SimpleListProperty<String> strings) {
        if (strings == null) {
            return;
        }
        if (jdsField.getType() == JdsFieldType.ARRAY_TEXT) {
            properties.put(jdsField.getId(), jdsField.getName());
            types.put(jdsField.getId(), jdsField.getType().toString());
            stringArrayProperties.put(jdsField.getId(), strings);
        } else {
            throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
        }
    }

    /**
     * @param jdsField
     * @param floats
     */
    protected final void mapFloats(final JdsField jdsField, final SimpleListProperty<Float> floats) {
        if (floats == null) {
            return;
        }
        if (jdsField.getType() == JdsFieldType.ARRAY_FLOAT) {
            properties.put(jdsField.getId(), jdsField.getName());
            types.put(jdsField.getId(), jdsField.getType().toString());
            floatArrayProperties.put(jdsField.getId(), floats);
        } else {
            throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
        }
    }

    /**
     * @param jdsField
     * @param doubles
     */
    protected final void mapDoubles(final JdsField jdsField, final SimpleListProperty<Double> doubles) {
        if (doubles == null) {
            return;
        }
        if (jdsField.getType() == JdsFieldType.ARRAY_DOUBLE) {
            properties.put(jdsField.getId(), jdsField.getName());
            types.put(jdsField.getId(), jdsField.getType().toString());
            doubleArrayProperties.put(jdsField.getId(), doubles);
        } else {
            throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
        }
    }

    /**
     * @param jdsField
     * @param longs
     */
    protected final void mapLongs(final JdsField jdsField, final SimpleListProperty<Long> longs) {
        if (longs == null) {
            return;
        }
        if (jdsField.getType() == JdsFieldType.ARRAY_LONG) {
            properties.put(jdsField.getId(), jdsField.getName());
            types.put(jdsField.getId(), jdsField.getType().toString());
            longArrayProperties.put(jdsField.getId(), longs);
        } else {
            throw new RuntimeException("Please prepareDatabaseComponents jdsField [" + jdsField + "] to the correct type");
        }
    }

    /**
     * @param jdsFieldEnum
     * @param enums
     */
    protected final void map(final JdsFieldEnum jdsFieldEnum, final SimpleObjectProperty<? extends Enum> enums) {
        if (enums == null) {
            return;
        }
        allEnums.add(jdsFieldEnum);
        if (jdsFieldEnum.getField().getType() == JdsFieldType.ENUM) {
            properties.put(jdsFieldEnum.getField().getId(), jdsFieldEnum.getField().getName());
            types.put(jdsFieldEnum.getField().getId(), jdsFieldEnum.getField().getType().toString());
            enumProperties.put(jdsFieldEnum, (SimpleObjectProperty<Enum>) enums);
        } else {
            throw new RuntimeException("Please prepareDatabaseComponents field [" + jdsFieldEnum + "] to the correct type");
        }
    }

    /**
     * @param jdsFieldEnum
     * @param enums
     */
    protected final void mapEnums(final JdsFieldEnum jdsFieldEnum, final SimpleListProperty<? extends Enum> enums) {
        if (enums == null) {
            return;
        }
        allEnums.add(jdsFieldEnum);
        if (jdsFieldEnum.getField().getType() == JdsFieldType.ENUM_COLLECTION) {
            properties.put(jdsFieldEnum.getField().getId(), jdsFieldEnum.getField().getName());
            types.put(jdsFieldEnum.getField().getId(), jdsFieldEnum.getField().getType().toString());
            enumCollectionProperties.put(jdsFieldEnum, (SimpleListProperty<Enum>) enums);
        } else {
            throw new RuntimeException("Please prepareDatabaseComponents field [" + jdsFieldEnum + "] to the correct type");
        }
    }

    /**
     * @param entity
     * @param property
     * @param cascadeOnDelete
     */
    protected final <T extends IJdsEntity> void map(Class<T> entity, final SimpleObjectProperty<T> property, boolean cascadeOnDelete) {
        if (entity.isAnnotationPresent(JdsEntityAnnotation.class)) {
            JdsEntityAnnotation entityAnnotation = entity.getAnnotation(JdsEntityAnnotation.class);
            if (!objectArrayProperties.containsKey(entityAnnotation.entityId()) && !objectProperties.containsKey(entityAnnotation.entityId())) {
                objectProperties.put(entityAnnotation.entityId(), (SimpleObjectProperty<JdsEntity>) property);
                objects.add(entityAnnotation.entityId());
                objectCascade.put(entityAnnotation.entityId(), cascadeOnDelete);
            } else {
                throw new RuntimeException("You can only bind a class to one property. This class is already bound to one object or object array");
            }
        } else {
            throw new RuntimeException("You must annotate the class [" + entity.getCanonicalName() + "] with [" + JdsEntityAnnotation.class + "]");
        }
    }

    /**
     * @param entity
     * @param property
     */
    protected final <T extends IJdsEntity> void map(Class<T> entity, final SimpleObjectProperty<T> property) {
        map(entity, property, false);
    }

    /**
     * @param entity
     * @param properties
     * @param cascadeOnDelete
     */
    protected final <T extends IJdsEntity> void map(Class<T> entity, final SimpleListProperty<T> properties, boolean cascadeOnDelete) {
        if (entity.isAnnotationPresent(JdsEntityAnnotation.class)) {
            JdsEntityAnnotation entityAnnotation = entity.getAnnotation(JdsEntityAnnotation.class);
            if (!objectArrayProperties.containsKey(entityAnnotation.entityId()) && !objectProperties.containsKey(entityAnnotation.entityId())) {
                objectArrayProperties.put(entityAnnotation.entityId(), (SimpleListProperty<JdsEntity>) properties);
                objects.add(entityAnnotation.entityId());
                objectCascade.put(entityAnnotation.entityId(), cascadeOnDelete);
            } else {
                throw new RuntimeException("You can only bind a class to one property. This class is already bound to one object or object array");
            }
        } else {
            throw new RuntimeException("You must annotate the class [" + entity.getCanonicalName() + "] with [" + JdsEntityAnnotation.class + "]");
        }
    }

    /**
     * @param entity
     * @param properties
     */
    protected final <T extends IJdsEntity> void map(Class<T> entity, final SimpleListProperty<T> properties) {
        map(entity, properties, false);
    }

    /**
     * Copy values from matching fields found in both objects
     *
     * @param source The entity to copy values from
     * @param <T>    A valid JDSEntity
     */
    public <T extends JdsEntity> void copy(T source) {
        copyHeaderValues(source);
        copyPropertyValues(source);
        copyArrayValues(source);
        copyEnumValues(source);
        copyObjectAndObjectArrayValues(source);
    }

    /**
     * Copy all header overview information
     *
     * @param source The entity to copy values from
     * @param <T>    A valid JDSEntity
     */
    private <T extends JdsEntity> void copyArrayValues(final T source) {
        JdsEntityOverview dest = this.getOverview();
        dest.setDateCreated(source.getOverview().getDateCreated());
        dest.setDateModified(source.getOverview().getDateModified());
        dest.setEntityGuid(source.getOverview().getEntityGuid());
    }

    /**
     * Copy all property values
     *
     * @param source The entity to copy values from
     * @param <T>    A valid JDSEntity A valid JDSEntity
     */
    private <T extends JdsEntity> void copyPropertyValues(final T source) {
        JdsEntity dest = this;
        source.booleanProperties.entrySet().parallelStream().forEach(srcEntry -> {
            if (dest.booleanProperties.containsKey(srcEntry.getKey())) {
                dest.booleanProperties.get(srcEntry.getKey()).set(srcEntry.getValue().get());
            }
        });
        source.localDateTimeProperties.entrySet().parallelStream().forEach(srcEntry -> {
            if (dest.localDateTimeProperties.containsKey(srcEntry.getKey())) {
                dest.localDateTimeProperties.get(srcEntry.getKey()).set(srcEntry.getValue().get());
            }
        });
        source.zonedDateTimeProperties.entrySet().parallelStream().forEach(srcEntry -> {
            if (dest.zonedDateTimeProperties.containsKey(srcEntry.getKey())) {
                dest.zonedDateTimeProperties.get(srcEntry.getKey()).set(srcEntry.getValue().get());
            }
        });
        source.localTimeProperties.entrySet().parallelStream().forEach(srcEntry -> {
            if (dest.localTimeProperties.containsKey(srcEntry.getKey())) {
                dest.localTimeProperties.get(srcEntry.getKey()).set(srcEntry.getValue().get());
            }
        });
        source.localDateProperties.entrySet().parallelStream().forEach(srcEntry -> {
            if (dest.localDateProperties.containsKey(srcEntry.getKey())) {
                dest.localDateProperties.get(srcEntry.getKey()).set(srcEntry.getValue().get());
            }
        });
        source.stringProperties.entrySet().parallelStream().forEach(srcEntry -> {
            if (dest.stringProperties.containsKey(srcEntry.getKey())) {
                dest.stringProperties.get(srcEntry.getKey()).set(srcEntry.getValue().get());
            }
        });
        source.floatProperties.entrySet().parallelStream().forEach(srcEntry -> {
            if (dest.floatProperties.containsKey(srcEntry.getKey())) {
                dest.floatProperties.get(srcEntry.getKey()).set(srcEntry.getValue().get());
            }
        });
        source.doubleProperties.entrySet().parallelStream().forEach(srcEntry -> {
            if (dest.doubleProperties.containsKey(srcEntry.getKey())) {
                dest.doubleProperties.get(srcEntry.getKey()).set(srcEntry.getValue().get());
            }
        });
        source.longProperties.entrySet().parallelStream().forEach(srcEntry -> {
            if (dest.longProperties.containsKey(srcEntry.getKey())) {
                dest.longProperties.get(srcEntry.getKey()).set(srcEntry.getValue().get());
            }
        });
        source.integerProperties.entrySet().parallelStream().forEach(srcEntry -> {
            if (dest.integerProperties.containsKey(srcEntry.getKey())) {
                dest.integerProperties.get(srcEntry.getKey()).set(srcEntry.getValue().get());
            }
        });
        source.blobProperties.entrySet().parallelStream().forEach(srcEntry -> {
            if (dest.blobProperties.containsKey(srcEntry.getKey())) {
                dest.blobProperties.get(srcEntry.getKey()).set(srcEntry.getValue().get());
            }
        });
    }

    /**
     * Copy all property array values
     *
     * @param source The entity to copy values from
     * @param <T>    A valid JDSEntity A valid JDSEntity
     */
    private <T extends JdsEntity> void copyHeaderValues(final T source) {
        JdsEntity dest = this;
        source.stringArrayProperties.entrySet().parallelStream().forEach(srcEntry -> {
            if (dest.stringArrayProperties.containsKey(srcEntry.getKey())) {
                SimpleListProperty<String> entry = dest.stringArrayProperties.get(srcEntry.getKey());
                entry.clear();
                entry.set(srcEntry.getValue().get());
            }
        });
        source.dateTimeArrayProperties.entrySet().parallelStream().forEach(srcEntry -> {
            if (dest.dateTimeArrayProperties.containsKey(srcEntry.getKey())) {
                SimpleListProperty<LocalDateTime> entry = dest.dateTimeArrayProperties.get(srcEntry.getKey());
                entry.clear();
                entry.set(srcEntry.getValue().get());
            }
        });
        source.floatArrayProperties.entrySet().parallelStream().forEach(srcEntry -> {
            if (dest.floatArrayProperties.containsKey(srcEntry.getKey())) {
                SimpleListProperty<Float> entry = dest.floatArrayProperties.get(srcEntry.getKey());
                entry.clear();
                entry.set(srcEntry.getValue().get());
            }
        });
        source.doubleArrayProperties.entrySet().parallelStream().forEach(srcEntry -> {
            if (dest.doubleArrayProperties.containsKey(srcEntry.getKey())) {
                SimpleListProperty<Double> entry = dest.doubleArrayProperties.get(srcEntry.getKey());
                entry.clear();
                entry.set(srcEntry.getValue().get());
            }
        });
        source.longArrayProperties.entrySet().parallelStream().forEach(srcEntry -> {
            if (dest.longArrayProperties.containsKey(srcEntry.getKey())) {
                SimpleListProperty<Long> entry = dest.longArrayProperties.get(srcEntry.getKey());
                entry.clear();
                entry.set(srcEntry.getValue().get());
            }
        });
        source.integerArrayProperties.entrySet().parallelStream().forEach(srcEntry -> {
            if (dest.integerArrayProperties.containsKey(srcEntry.getKey())) {
                SimpleListProperty<Integer> entry = dest.integerArrayProperties.get(srcEntry.getKey());
                entry.clear();
                entry.set(srcEntry.getValue().get());
            }
        });
    }

    /**
     * Copy over object and object array values
     *
     * @param source The entity to copy values from
     * @param <T>    A valid JDSEntity
     */
    private <T extends JdsEntity> void copyObjectAndObjectArrayValues(T source) {
        JdsEntity dest = this;
        source.objectProperties.entrySet().parallelStream().forEach(srcEntry -> {
            if (dest.objectProperties.containsKey(srcEntry.getKey())) {
                dest.objectProperties.get(srcEntry.getKey()).set(srcEntry.getValue().get());
            }
        });

        source.objectArrayProperties.entrySet().parallelStream().forEach(srcEntry -> {
            if (dest.objectArrayProperties.containsKey(srcEntry.getKey())) {
                SimpleListProperty<JdsEntity> entry = dest.objectArrayProperties.get(srcEntry.getKey());
                entry.clear();
                entry.set(srcEntry.getValue().get());
            }
        });
    }

    /**
     * Copy over object enum values
     *
     * @param source The entity to copy values from
     * @param <T>    A valid JDSEntity
     */
    private <T extends JdsEntity> void copyEnumValues(T source) {
        JdsEntity dest = this;
        source.enumCollectionProperties.entrySet().parallelStream().forEach(srcEntry -> {
            JdsFieldEnum key = srcEntry.getKey();
            if (dest.enumCollectionProperties.containsKey(key)) {
                SimpleListProperty<Enum> dstEntry = dest.enumCollectionProperties.get(srcEntry.getKey());
                dstEntry.clear();
                Iterator<? extends Enum> it = srcEntry.getValue().iterator();
                while (it.hasNext()) {
                    Enum nxt = it.next();
                    dstEntry.add(nxt);
                }
            }
        });
    }
}
