package io.github.subiyacryolite.jds.events

/**
 * This listener can be invoked when loading a jds entity. It can be used for logging operations or overriding field initialization
 */
interface JdsLoadListener {

    /**
     * This method is called after initializing the base object and before populating any field values within a {@link io.github.subiyacryolite.jds.JdsEntity JdsEntity}
     *@param arguments the arguments to reference for this invocation
     */
    fun onPreLoad(arguments: OnPreLoadEventArguments)

    /**
     * This method is called after initializing the base object and after populating all field values within a {@link io.github.subiyacryolite.jds.JdsEntity JdsEntity}
     *@param arguments the arguments to reference for this invocation
     */
    fun onPostLoad(arguments: OnPostLoadEventArguments)
}
