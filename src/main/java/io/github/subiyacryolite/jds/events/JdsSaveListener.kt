package io.github.subiyacryolite.jds.events

/**
 * This listener can be invoked when saving a jds entity. It can be used for logging operations or overriding field persistence
 */
 interface JdsSaveListener {

    /**
     * This method is called before saving preliminary overview information and before saving any {@link io.github.subiyacryolite.jds.JdsEntity JdsEntity} field values
     *@param arguments the arguments to reference for this invocation
     */
    fun onPreSave(arguments: OnPreSaveEventArguments)

    /**
     * This method is called after saving preliminary overview information and after saving all {@link io.github.subiyacryolite.jds.JdsEntity JdsEntity} field values
     *@param arguments the arguments to reference for this invocation
     */
    fun onPostSave(arguments: OnPostSaveEventArguments)
}
