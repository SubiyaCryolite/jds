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

import java.lang.annotation.Annotation;
import java.util.HashMap;

/**
 * This class maps {@link JdsEntity JdsEntities} for usage with JDS. It also
 * validates annotations and checks for duplicate entity codes
 */
public class JdsEntityClasses {
    private final static HashMap<Long, Class> classes = new HashMap<>();

    public static synchronized void map(Class<? extends JdsEntity> entity) {
        if (entity.isAnnotationPresent(JdsEntityAnnotation.class)) {
            Annotation annotation = entity.getAnnotation(JdsEntityAnnotation.class);
            JdsEntityAnnotation je = (JdsEntityAnnotation) annotation;
            if (!classes.containsKey(je.entityId())) {
                classes.put(je.entityId(), entity);
            } else
                throw new RuntimeException("Duplicate service code for class [" + entity.getCanonicalName() + "] - [" + je.entityId() + "]");
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
