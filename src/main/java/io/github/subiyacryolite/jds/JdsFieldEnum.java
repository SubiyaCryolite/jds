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

import javafx.beans.property.SimpleObjectProperty;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;

/**
 * Represents a field enum in JDS
 */
public class JdsFieldEnum<T extends Enum<T>> implements Externalizable {

    private static final HashMap<Long, JdsFieldEnum> fieldEnums = new HashMap<>();
    private final SimpleObjectProperty<JdsField> field = new SimpleObjectProperty<>();
    private Class<T> enumType;
    private Enum[] sequenceValues = new Enum[0];//keep order at all times

    /**
     *
     */
    public JdsFieldEnum() {
    }

    /**
     * @param type
     */
    public JdsFieldEnum(final Class<T> type) {
        this();
        this.enumType = type;
    }

    /**
     * @param type
     * @param jdsField
     * @param values
     */
    public JdsFieldEnum(final Class<T> type, final JdsField jdsField, final T... values) {
        this(type);
        setField(jdsField);
        sequenceValues = new Enum[values.length];
        System.arraycopy(values, 0, sequenceValues, 0, values.length);
        bind();
    }

    /**
     * @return
     */
    public final Class<? extends Enum> getEnumType() {
        return enumType;
    }

    /**
     * @param jdsField
     * @return
     */
    public static JdsFieldEnum get(final JdsField jdsField) {
        if (fieldEnums.containsKey(jdsField.getId()))
            return fieldEnums.get(jdsField.getId());
        else
            throw new RuntimeException(String.format("This jdsField [%s] has not been bound to any enums", jdsField));
    }

    /**
     * @param fieldId
     * @return
     */
    public static JdsFieldEnum get(final long fieldId) {
        if (fieldEnums.containsKey(fieldId))
            return fieldEnums.get(fieldId);
        else
            throw new RuntimeException(String.format("This field [%s] has not been bound to any enums", fieldId));
    }

    /**
     *
     */
    private void bind() {
        if (!fieldEnums.containsKey(field.get()))
            fieldEnums.put(field.get().getId(), this);
    }

    /**
     * @param f
     */
    private void setField(JdsField f) {
        field.set(f);
    }

    /**
     * @return
     */
    public JdsField getField() {
        return field.get();
    }

    /**
     * @return
     */
    public Enum[] getSequenceValues() {
        return this.sequenceValues;
    }

    @Override
    public String toString() {
        return "JdsFieldEnum{" +
                "field=" + field.get() +
                ", sequenceValues=" + sequenceValues +
                '}';
    }

    /**
     * @param enumText
     * @return
     */
    public int indexOf(Enum enumText) {
        for (int i = 0; i < sequenceValues.length; i++) {
            if (sequenceValues[i] == enumText)
                return i;
        }
        return -1;
    }

    /**
     * @param index
     * @return
     */
    public Enum valueOf(int index) {
        return (index >= sequenceValues.length) ? null : sequenceValues[index];
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(enumType);
        out.writeObject(field.get());
        out.writeObject(sequenceValues);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        enumType = (Class<T>) in.readObject();
        field.set((JdsField) in.readObject());
        sequenceValues = (Enum[]) in.readObject();
    }
}
