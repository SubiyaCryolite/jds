package io.github.subiyacryolite.jds.tests.constants

import io.github.subiyacryolite.jds.JdsFieldEnum

/**
 * Created by ifunga on 18/02/2017.
 */
object Enums {

    val PRIMARY_ADDRESS_ENUM = JdsFieldEnum(PrimaryAddress::class.java, Fields.PRIMARY_ADDRESS_ENUM, *PrimaryAddress.values())
    val PRIMARY_ADDRESS_ENUM_STRING= JdsFieldEnum(PrimaryAddress::class.java, Fields.PRIMARY_ADDRESS_ENUM_STRING, *PrimaryAddress.values())
    val SEX_ENUM = JdsFieldEnum(Sex::class.java, Fields.SEX_ENUM, *Sex.values())
    val RIGHTS = JdsFieldEnum(Rights::class.java, Fields.RIGHTS, *Rights.values())
}