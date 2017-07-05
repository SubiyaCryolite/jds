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
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a field enum in JDS
 */
public class JdsFieldEnum implements Externalizable {

    private static final HashMap<Long, JdsFieldEnum> fieldEnums = new HashMap<>();
    private final SimpleObjectProperty<JdsField> field;
    private final LinkedList<String> sequenceValues;//keep order at all times

    public JdsFieldEnum() {
        this.field = new SimpleObjectProperty();
        this.sequenceValues = new LinkedList<>();
    }

    public JdsFieldEnum(final JdsField jdsField, final String... values) {
        this();
        this.field.set(jdsField);
        for (String value : values)
            this.sequenceValues.addLast(value);
        bind();
    }

    public JdsFieldEnum(final JdsField jdsField, final Enum... values) {
        this();
        this.field.set(jdsField);
        for (Enum value : values)
            this.sequenceValues.addLast(value.toString());
        bind();
    }

    public static JdsFieldEnum get(final JdsField jdsField) {
        if (fieldEnums.containsKey(jdsField.getId()))
            return fieldEnums.get(jdsField.getId());
        else
            throw new RuntimeException(String.format("This jdsField [%s] has not been bound to any enums", jdsField));
    }

    public static JdsFieldEnum get(final long fieldId) {
        if (fieldEnums.containsKey(fieldId))
            return fieldEnums.get(fieldId);
        else
            throw new RuntimeException(String.format("This field [%s] has not been bound to any enums", fieldId));
    }

    private void bind() {
        if (!fieldEnums.containsKey(field.get()))
            fieldEnums.put(field.get().getId(), this);
    }

    public JdsField getField() {
        return field.get();
    }

    public LinkedList<String> getSequenceValues() {
        return this.sequenceValues;
    }

    @Override
    public String toString() {
        return "JdsFieldEnum{" +
                "field=" + field.get() +
                ", sequenceValues=" + sequenceValues +
                '}';
    }

    public int getIndex(String enumText) {
        return sequenceValues.indexOf(enumText);
    }

    public String getValue(int index) {
        return (index >= sequenceValues.size()) ? "" : sequenceValues.get(index);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(field.get());
        out.writeObject(sequenceValues);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        field.set((JdsField) in.readObject());
        sequenceValues.addAll((List<String>) in.readObject());
    }
}
