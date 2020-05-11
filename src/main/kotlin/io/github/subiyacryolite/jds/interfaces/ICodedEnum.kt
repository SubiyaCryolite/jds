package io.github.subiyacryolite.jds.interfaces

interface ICodedEnum<T : Enum<T>>  {
    val code: Int
}