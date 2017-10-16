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
        source.s.forEach { target.populateProperties(JdsFieldType.TEXT, it.id, it.v) }
        source.l.forEach { target.populateProperties(JdsFieldType.LONG, it.id, it.v) }
        source.i.forEach { target.populateProperties(JdsFieldType.INT, it.id, it.v) }
        source.b.forEach { target.populateProperties(JdsFieldType.BOOLEAN, it.id, it.v) }
        source.f.forEach { target.populateProperties(JdsFieldType.FLOAT, it.id, it.v) }
        source.d.forEach { target.populateProperties(JdsFieldType.DOUBLE, it.id, it.v) }
        //==============================================
        //Dates & Time
        //==============================================
        source.ldt.forEach { target.populateProperties(JdsFieldType.DATE_TIME, it.id, it.v) }
        source.ld.forEach { target.populateProperties(JdsFieldType.DATE, it.id, it.v) }
        source.zdt.forEach { target.populateProperties(JdsFieldType.ZONED_DATE_TIME, it.id, it.v) }
        source.t.forEach { target.populateProperties(JdsFieldType.TIME, it.id, it.v) }
        source.du.forEach { target.populateProperties(JdsFieldType.DURATION, it.id, it.v) }
        source.md.forEach { target.populateProperties(JdsFieldType.MONTH_DAY, it.id, it.v) }
        source.ym.forEach { target.populateProperties(JdsFieldType.YEAR_MONTH, it.id, it.v) }
        source.p.forEach { target.populateProperties(JdsFieldType.PERIOD, it.id, it.v) }
        //==============================================
        //BLOB
        //==============================================
        source.bl.forEach { target.populateProperties(JdsFieldType.BLOB, it.id, it.v) }
        //==============================================
        //Enums
        //==============================================
        source.e.forEach { target.populateProperties(JdsFieldType.ENUM, it.i, it.v) }
        source.ea.forEach { target.populateProperties(JdsFieldType.ENUM_COLLECTION, it.i, it.v) }
        //==============================================
        //ARRAYS
        //==============================================
        source.ia.forEach { target.populateProperties(JdsFieldType.ARRAY_INT, it.i, it.v) }
        source.fa.forEach { target.populateProperties(JdsFieldType.ARRAY_FLOAT, it.i, it.v) }
        source.la.forEach { target.populateProperties(JdsFieldType.ARRAY_LONG, it.i, it.v) }
        source.sa.forEach { target.populateProperties(JdsFieldType.ARRAY_TEXT, it.i, it.v) }
        source.da.forEach { target.populateProperties(JdsFieldType.ARRAY_DOUBLE, it.i, it.v) }
        source.dta.forEach { target.populateProperties(JdsFieldType.ARRAY_DATE_TIME, it.i, it.v) }
        //==============================================
        //EMBEDDED OBJECTS
        //==============================================
        val entityGuids = HashSet<String>()//ids should be unique
        val innerObjects = ConcurrentLinkedQueue<JdsEntity>()//can be multiple copies of the same object however
        source.eb.forEach {
            target.populateObjects(jdsDb, it.f, it.i, it.c, innerObjects, entityGuids)
        }
        innerObjects.forEach {
            //populate the inner objects
            populate(outerIndex, innerIndex, it, source.eo.find { itx -> itx.o.uuid == it.overview.entityGuid }!!)
        }
    }
}