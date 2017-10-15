package io.github.subiyacryolite.jds.embedded


import io.github.subiyacryolite.jds.JdsDb
import io.github.subiyacryolite.jds.JdsEntity
import io.github.subiyacryolite.jds.enums.JdsFieldType
import java.util.HashSet
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.collections.ArrayList

class JdsLoadEmbedded<T : JdsEntity>(private val jdsDb: JdsDb, private val referenceType: Class<T>, private vararg val container: JdsEmbeddedContainer) : Callable<List<T>> {

    override fun call(): List<T> {
        return implementation()
    }

    private fun implementation(): List<T> {
        val output: MutableList<T> = ArrayList()
        container.forEachIndexed { index, element ->
            element.e.forEachIndexed { innerIndex, innerElement ->
                val instance = referenceType.newInstance()
                populate(index, innerIndex, instance, innerElement)
                output.add(instance)
            }
        }
        return output
    }

    private fun populate(outerIndex: Int, innerIndex: Int, target: JdsEntity, source: JdsEmbeddedObject) {
        //==============================================
        //Overviews
        //==============================================
        target.overview.dateCreated = source.o.dc
        target.overview.dateModified = source.o.dm
        target.overview.entityId = source.o.id
        target.overview.entityGuid = source.o.uuid
        target.overview.live = source.o.l
        target.overview.version = source.o.v
        //==============================================
        //PRIMITIVES
        //==============================================
        source.sv.forEach { target.populateProperties(JdsFieldType.TEXT, it.id, it.`val`) }
        source.lv.forEach { target.populateProperties(JdsFieldType.LONG, it.id, it.`val`) }
        source.iv.forEach { target.populateProperties(JdsFieldType.INT, it.id, it.`val`) }
        source.bv.forEach { target.populateProperties(JdsFieldType.BOOLEAN, it.id, it.`val`) }
        source.fv.forEach { target.populateProperties(JdsFieldType.FLOAT, it.id, it.`val`) }
        source.dv.forEach { target.populateProperties(JdsFieldType.DOUBLE, it.id, it.`val`) }
        //==============================================
        //Dates & Time
        //==============================================
        source.ldtv.forEach { target.populateProperties(JdsFieldType.DATE_TIME, it.id, it.`val`) }
        source.ldv.forEach { target.populateProperties(JdsFieldType.DATE, it.id, it.`val`) }
        source.zdtv.forEach { target.populateProperties(JdsFieldType.ZONED_DATE_TIME, it.id, it.`val`) }
        source.ltv.forEach { target.populateProperties(JdsFieldType.TIME, it.id, it.`val`) }
        source.mdv.forEach { target.populateProperties(JdsFieldType.MONTH_DAY, it.id, it.`val`) }
        //==============================================
        //BLOB
        //==============================================
        source.blv.forEach { target.populateProperties(JdsFieldType.BLOB, it.id, it.`val`) }
        //==============================================
        //Enums
        //==============================================
        source.ev.forEach { target.populateProperties(JdsFieldType.ENUM, it.id, it.`val`) }
        source.eav.forEach { target.populateProperties(JdsFieldType.ENUM_COLLECTION, it.id, it.`val`) }
        //==============================================
        //ARRAYS
        //==============================================
        source.iav.forEach { target.populateProperties(JdsFieldType.ARRAY_INT, it.id, it.`val`) }
        source.fav.forEach { target.populateProperties(JdsFieldType.ARRAY_FLOAT, it.id, it.`val`) }
        source.lav.forEach { target.populateProperties(JdsFieldType.ARRAY_LONG, it.id, it.`val`) }
        source.sav.forEach { target.populateProperties(JdsFieldType.ARRAY_TEXT, it.id, it.`val`) }
        source.dav.forEach { target.populateProperties(JdsFieldType.ARRAY_DOUBLE, it.id, it.`val`) }
        source.dtav.forEach { target.populateProperties(JdsFieldType.ARRAY_DATE_TIME, it.id, it.`val`) }
        //==============================================
        //EMBEDDED OBJECTS
        //==============================================
        val entityGuids = HashSet<String>()//ids should be unique
        val innerObjects = ConcurrentLinkedQueue<JdsEntity>()//can be multiple copies of the same object however
        source.eb.forEach {
            target.populateObjects(jdsDb, it.fid, it.id, it.uid, innerObjects, entityGuids)
        }
        innerObjects.forEach {
            //populate the inner objects
            populate(outerIndex, innerIndex, it, source.eo.find { itx -> itx.o.uuid == it.overview.entityGuid }!!)
        }
    }
}