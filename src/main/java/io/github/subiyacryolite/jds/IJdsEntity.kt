package io.github.subiyacryolite.jds;

import java.io.Externalizable

interface IJdsEntity : Externalizable {

    val overview: IJdsOverview

    var entityName: String
}
