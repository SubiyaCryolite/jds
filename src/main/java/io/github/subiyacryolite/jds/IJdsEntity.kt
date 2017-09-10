package io.github.subiyacryolite.jds;

import java.io.Externalizable

interface IJdsEntity : Externalizable {

    val overview: IJdsEntityOverview

    fun getEntityName(): String;

    fun setEntityName(name: String);
}
