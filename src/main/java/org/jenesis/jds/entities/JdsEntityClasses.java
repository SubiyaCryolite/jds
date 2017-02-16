package org.jenesis.jds.entities;

import org.jenesis.jds.annotations.JdsEntityAnnotation;

import java.lang.annotation.Annotation;
import java.util.HashMap;

/**
 * Created by ifunga on 11/02/2017.
 */
public class JdsEntityClasses {
    private final static HashMap<Long, Class> classes = new HashMap<>();

    public static synchronized void add(Class<? extends JdsEntity> entity) {
        if (entity.isAnnotationPresent(JdsEntityAnnotation.class)) {
            Annotation annotation = entity.getAnnotation(JdsEntityAnnotation.class);
            JdsEntityAnnotation je = (JdsEntityAnnotation) annotation;
            if (!classes.containsKey(je.entityCode())) {
                classes.put(je.entityCode(), entity);
            } else
                throw new RuntimeException("Duplicate service code for class [" + entity.getCanonicalName() + "] - [" + je.entityCode() + "]");
        } else
            throw new RuntimeException("You must annotate the class [" + entity.getCanonicalName() + "] with [" + JdsEntityAnnotation.class + "]");
    }

    public static boolean hasClass(long serviceCode) {
        return classes.containsKey(serviceCode);
    }

    public static Class<JdsEntity> getBoundClass(long serviceCode) {
        return classes.get(serviceCode);
    }

    @Override
    public String toString() {
        return "JdsEntityClasses{" + classes + "}";
    }
}
