package io.github.subiyacryolite.jds.embedded

import io.github.subiyacryolite.jds.JdsEntity
import java.time.MonthDay
import java.time.Period
import java.time.YearMonth

class JdsEmbeddedObject(source: JdsEntity) {
    /**
     * Blob Values
     */
    val bl: MutableList<JdsBlobValues> = ArrayList()
    /**
     * Boolean values
     */
    val b: MutableList<JdsBooleanValues> = ArrayList()
    /**
     * Local Date Time Values
     */
    val ldt: MutableList<JdsLocalDateTimeValues> = ArrayList()
    /**
     * Double values
     */
    val d: MutableList<JdsDoubleValues> = ArrayList()
    /**
     * Integer values
     */
    val i: MutableList<JdsIntegerValues> = ArrayList()
    /**
     * Long values
     */
    val l: MutableList<JdsLongValues> = ArrayList()
    /**
     * String values
     */
    val s: MutableList<JdsTextValues> = ArrayList()
    /**
     * Float values
     */
    val f: MutableList<JdsFloatValues> = ArrayList()
    /**
     * Local Time values
     */
    val t: MutableList<JdsTimeValues> = ArrayList()
    /**
     * Month day values
     */
    val mdv: MutableList<JdsMonthDayValues> = ArrayList()
    /**
     * Zoned Date Time values
     */
    val zdt: MutableList<JdsZonedDateTimeValues> = ArrayList()
    /**
     * Local Date values
     */
    val ld: MutableList<JdsLocalDateValues> = ArrayList()
    /**
     * Date-Time collection values
     */
    val dta: MutableList<JdsDateCollections> = ArrayList()
    /**
     * Double collection values
     */
    val da: MutableList<JdsDoubleCollections> = ArrayList()
    /**
     * Integer collection values
     */
    val ia: MutableList<JdsIntegerCollections> = ArrayList()
    /**
     * Long collection tavles
     */
    val la: MutableList<JdsLongCollections> = ArrayList()
    /**
     * String collection values
     */
    val sa: MutableList<JdsTextCollections> = ArrayList()
    /**
     * Float collection values
     */
    val fa: MutableList<JdsFloatCollections> = ArrayList()
    /**
     * Enum values
     */
    val e: MutableList<JdsEnumValues> = ArrayList()
    /**
     * Enum collection values
     */
    val ea: MutableList<JdsEnumCollections> = ArrayList()
    /**
     * Duration values
     */
    val du: MutableList<JdsDurationValues> = ArrayList()
    /**
     * Month Day values
     */
    val md: MutableList<JdsMonthDayValues> = ArrayList()
    /**
     * Year Month values
     */
    val ym: MutableList<JdsYearMonthValues> = ArrayList()
    /**
     * Period values
     */
    val p: MutableList<JdsPeriodValues> = ArrayList()
    /**
     * Entity bindings [parent to child]
     */
    val eb: MutableList<JdsStoreEntityBinding> = ArrayList()
    /**
     * Embedded objects
     */
    val eo: MutableList<JdsEmbeddedObject> = ArrayList()
    /**
     * Object overview
     */
    val o: JdsStoreEntityOverview = JdsStoreEntityOverview(source.overview.entityGuid, source.overview.entityId, source.overview.live, source.overview.version, source.overview.dateCreated, source.overview.dateModified)

    init {
        source.assign(this);
    }
}