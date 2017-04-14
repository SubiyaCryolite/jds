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

import io.github.subiyacryolite.jds.enums.JdsFieldType;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.HashMap;

import static io.github.subiyacryolite.jds.enums.JdsFieldType.TEXT;

/**
 * Represents a field in JDS
 */
public class JdsField {
    private static HashMap<Long, String> fields = new HashMap<>();
    private final SimpleLongProperty id;
    private final SimpleStringProperty name;
    private JdsFieldType type;
    private JdsField() {
        id = new SimpleLongProperty();
        name = new SimpleStringProperty();
        type = TEXT;
    }

    public JdsField(long id, String name, JdsFieldType type) {
        this();
        setId(id);
        setName(name);
        setType(type);
        bind(this);
    }

    private static void bind(final JdsField jdsField) {
        if (!fields.containsKey(jdsField.getId()))
            fields.put(jdsField.getId(), jdsField.getName());
        else
            throw new RuntimeException(String.format("This jdsField ID [%s] is already bound", jdsField.getId()));
    }

    public long getId() {
        return id.get();
    }

    private void setId(long id) {
        this.id.set(id);
    }

    public String getName() {
        return name.get();
    }

    private void setName(String name) {
        this.name.set(name);
    }

    public JdsFieldType getType() {
        return this.type;
    }

    private void setType(JdsFieldType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "JdsField{" +
                "id=" + id.get() +
                ", name=" + name.get() +
                ", type=" + type +
                '}';
    }
}
