package io.github.subiyacryolite.jds;

import java.time.LocalDateTime;

interface IJdsOverview {

    var dateCreated:LocalDateTime

    var dateModified:LocalDateTime

    var entityId:Long

    var entityGuid:String

    var live:Boolean

    var version:Long
}
