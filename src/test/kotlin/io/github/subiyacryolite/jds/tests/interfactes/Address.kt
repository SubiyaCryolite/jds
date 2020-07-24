package io.github.subiyacryolite.jds.tests.interfactes

import io.github.subiyacryolite.jds.annotations.EntityAnnotation
import io.github.subiyacryolite.jds.interfaces.IEntity
import java.time.LocalDateTime

@EntityAnnotation(id = 1, name = "address", description = "An entity representing address information")
interface IAddress : IEntity {
    var primaryAddress: Boolean?
    var streetName: String
    var plotNumber: Short?
    var area: String
    var city: String
    var provinceOrState: String
    var country: String
    var timeOfEntry: LocalDateTime
}