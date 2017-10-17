/**
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
package io.github.subiyacryolite.jds

import javafx.beans.property.*
import java.time.*

import java.time.temporal.Temporal
import java.util.*

/**
 * Helper class used when performing [JdsEntity] saves
 */
internal class JdsSaveContainer {
    //time constructs
    val localDateTimeProperties: MutableList<HashMap<String, HashMap<Long, SimpleObjectProperty<Temporal>>>> = ArrayList()
    val localDateProperties: MutableList<HashMap<String, HashMap<Long, SimpleObjectProperty<Temporal>>>> = ArrayList()
    val localTimeProperties: MutableList<HashMap<String, HashMap<Long, SimpleObjectProperty<Temporal>>>> = ArrayList()
    val zonedDateTimeProperties: MutableList<HashMap<String, HashMap<Long, SimpleObjectProperty<Temporal>>>> = ArrayList()
    val monthDayProperties: MutableList<HashMap<String, HashMap<Long, SimpleObjectProperty<MonthDay>>>> = ArrayList()
    val yearMonthProperties: MutableList<HashMap<String, HashMap<Long, SimpleObjectProperty<Temporal>>>> = ArrayList()
    val periodProperties: MutableList<HashMap<String, HashMap<Long, SimpleObjectProperty<Period>>>> = ArrayList()
    val durationProperties: MutableList<HashMap<String, HashMap<Long, SimpleObjectProperty<Duration>>>> = ArrayList()
    //strings
    val stringProperties: MutableList<HashMap<String, HashMap<Long, SimpleStringProperty>>> = ArrayList()
    //primitives
    val booleanProperties: MutableList<HashMap<String, HashMap<Long, SimpleBooleanProperty>>> = ArrayList()
    val floatProperties: MutableList<HashMap<String, HashMap<Long, SimpleFloatProperty>>> = ArrayList()
    val doubleProperties: MutableList<HashMap<String, HashMap<Long, SimpleDoubleProperty>>> = ArrayList()
    val longProperties: MutableList<HashMap<String, HashMap<Long, SimpleLongProperty>>> = ArrayList()
    val integerProperties: MutableList<HashMap<String, HashMap<Long, SimpleIntegerProperty>>> = ArrayList()
    //blobs
    val blobProperties: MutableList<HashMap<String, HashMap<Long, SimpleBlobProperty>>> = ArrayList()
    //arrays
    val objectCollections: MutableList<HashMap<String, HashMap<JdsFieldEntity<*>, SimpleListProperty<JdsEntity>>>> = ArrayList()
    val stringCollections: MutableList<HashMap<String, HashMap<Long, SimpleListProperty<String>>>> = ArrayList()
    val localDateTimeCollections: MutableList<HashMap<String, HashMap<Long, SimpleListProperty<LocalDateTime>>>> = ArrayList()
    val floatCollections: MutableList<HashMap<String, HashMap<Long, SimpleListProperty<Float>>>> = ArrayList()
    val doubleCollections: MutableList<HashMap<String, HashMap<Long, SimpleListProperty<Double>>>> = ArrayList()
    val longCollections: MutableList<HashMap<String, HashMap<Long, SimpleListProperty<Long>>>> = ArrayList()
    val integerCollections: MutableList<HashMap<String, HashMap<Long, SimpleListProperty<Int>>>> = ArrayList()
    //enumProperties
    val enumProperties: MutableList<HashMap<String, HashMap<JdsFieldEnum<*>, SimpleObjectProperty<Enum<*>>>>> = ArrayList()
    val enumCollections: MutableList<HashMap<String, HashMap<JdsFieldEnum<*>, SimpleListProperty<Enum<*>>>>> = ArrayList()
    //objects
    val objects: MutableList<HashMap<String, HashMap<JdsFieldEntity<*>, SimpleObjectProperty<JdsEntity>>>> = ArrayList()
    //overviews
    val overviews: MutableList<HashSet<IJdsOverview>> = LinkedList()
}
