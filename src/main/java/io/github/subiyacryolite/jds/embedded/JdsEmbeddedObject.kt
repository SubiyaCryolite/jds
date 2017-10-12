package io.github.subiyacryolite.jds.embedded

import io.github.subiyacryolite.jds.JdsEntity

class JdsEmbeddedObject(source: JdsEntity) {
    /**
     * Blob Values
     */
    val blv: MutableList<JdsStoreBlob> = ArrayList()
    /**
     * Boolean values
     */
    val bv: MutableList<JdsStoreBoolean> = ArrayList()
    /**
     * Local Date Time Values
     */
    val ldtv: MutableList<JdsStoreDateTime> = ArrayList()
    /**
     * Double values
     */
    val dv: MutableList<JdsStoreDouble> = ArrayList()
    /**
     * Integer values
     */
    val iv: MutableList<JdsStoreInteger> = ArrayList()
    /**
     * Long values
     */
    val lv: MutableList<JdsStoreLong> = ArrayList()
    /**
     * String values
     */
    val sv: MutableList<JdsStoreText> = ArrayList()
    /**
     * Float values
     */
    val fv: MutableList<JdsStoreFloat> = ArrayList()
    /**
     * Local Time values
     */
    val ltv: MutableList<JdsStoreTime> = ArrayList()
    /**
     * Zoned Date Time values
     */
    val zdtv: MutableList<JdsStoreZonedDateTime> = ArrayList()
    /**
     * Local Date values
     */
    val ldv: MutableList<JdsStoreLocalDate> = ArrayList()
    /**
     * Date-Time collection values
     */
    val dtav: MutableList<JdsStoreDateTimeArray> = ArrayList()
    /**
     * Double collection values
     */
    val dav: MutableList<JdsStoreDoubleArray> = ArrayList()
    /**
     * Integer collection values
     */
    val iav: MutableList<JdsStoreIntegerArray> = ArrayList()
    /**
     * Long collection tavles
     */
    val lav: MutableList<JdsStoreLongArray> = ArrayList()
    /**
     * String collection values
     */
    val sav: MutableList<JdsStoreTextArray> = ArrayList()
    /**
     * Float collection values
     */
    val fav: MutableList<JdsStoreFloatArray> = ArrayList()
    /**
     * Enum values
     */
    val ev: MutableList<JdsStoreEnum> = ArrayList()
    /**
     * Enum collection values
     */
    val eav: MutableList<JdsStoreEnumArray> = ArrayList()
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
    val o: JdsStoreEntityOverview = JdsStoreEntityOverview(source.overview.entityGuid, source.overview.entityId, source.overview.live, source.overview.dateCreated, source.overview.dateModified)

    init {
        source.assign(this);
    }
}