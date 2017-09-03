package io.github.subiyacryolite.jds;

import java.time.LocalDateTime;

interface IJdsEntityOverview {

    var dateCreated:LocalDateTime

    var dateModified:LocalDateTime

    var entityId:Long

    var entityGuid:String
}
