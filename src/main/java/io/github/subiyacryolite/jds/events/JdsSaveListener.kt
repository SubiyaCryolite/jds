package io.github.subiyacryolite.jds.events

/**
 * Created by indana on 5/31/2017.
 */
 interface JdsSaveListener {
    fun onPreSave(arguments: OnPreSaveEventArguments)

    fun onPostSave(arguments: OnPostSaveEventArguments)
}
