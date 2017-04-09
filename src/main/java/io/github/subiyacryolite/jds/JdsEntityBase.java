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

import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by ifunga on 13/02/2017.
 * This class simply hides all underlying data structures from the user
 * However, these fields are visible in package class JdsSave
 */
abstract class JdsEntityBase {
    protected final SimpleObjectProperty<JdsEntityOverview> overview = new SimpleObjectProperty<>(new JdsEntityOverview());
    //field and enum maps
    protected final static HashSet<Long> map = new HashSet<>();
    protected final HashSet<Long> properties = new HashSet<>();
    protected final HashSet<Long> objects = new HashSet<>();
    protected final HashSet<JdsFieldEnum> allEnums = new HashSet<>();
    //strings and localDateTimes
    protected final HashMap<Long, SimpleObjectProperty<Temporal>> localDateTimeProperties = new HashMap<>();
    protected final HashMap<Long, SimpleObjectProperty<Temporal>> zonedDateTimeProperties = new HashMap<>();
    protected final HashMap<Long, SimpleObjectProperty<Temporal>> localDateProperties = new HashMap<>();
    protected final HashMap<Long, SimpleObjectProperty<Temporal>> localTimeProperties = new HashMap<>();
    protected final HashMap<Long, SimpleStringProperty> stringProperties = new HashMap<>();
    //numeric
    protected final HashMap<Long, SimpleFloatProperty> floatProperties = new HashMap<>();
    protected final HashMap<Long, SimpleDoubleProperty> doubleProperties = new HashMap<>();
    protected final HashMap<Long, SimpleBooleanProperty> booleanProperties = new HashMap<>();
    protected final HashMap<Long, SimpleLongProperty> longProperties = new HashMap<>();
    protected final HashMap<Long, SimpleIntegerProperty> integerProperties = new HashMap<>();
    //arrays
    protected final HashMap<Long, SimpleListProperty<? extends JdsEntity>> objectArrayProperties = new HashMap<>();
    protected final HashMap<Long, SimpleListProperty<String>> stringArrayProperties = new HashMap<>();
    protected final HashMap<Long, SimpleListProperty<LocalDateTime>> dateTimeArrayProperties = new HashMap<>();
    protected final HashMap<Long, SimpleListProperty<Float>> floatArrayProperties = new HashMap<>();
    protected final HashMap<Long, SimpleListProperty<Double>> doubleArrayProperties = new HashMap<>();
    protected final HashMap<Long, SimpleListProperty<Long>> longArrayProperties = new HashMap<>();
    protected final HashMap<Long, SimpleListProperty<Integer>> integerArrayProperties = new HashMap<>();
    //enums
    protected final HashMap<JdsFieldEnum, SimpleListProperty<String>> enumProperties = new HashMap<>();
    //objects
    protected final HashMap<Long, SimpleObjectProperty<? extends JdsEntity>> objectProperties = new HashMap<>();

    public final JdsEntityOverview getOverview() {
        return this.overview.get();
    }
}
