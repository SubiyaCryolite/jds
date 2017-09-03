package io.github.subiyacryolite.jds;

interface IJdsEntity : IJdsEntityBase {
    /**
     * @return
     */
    fun getEntityName(): String;

    /**
     * @param name
     */
    fun setEntityName(name: String);
}
