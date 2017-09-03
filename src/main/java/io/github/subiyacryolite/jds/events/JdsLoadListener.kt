package io.github.subiyacryolite.jds.events

/**
 * Created by indana on 5/31/2017.
 */
interface JdsLoadListener {
    fun onPostLoad(arguments: OnPostLoadEventArguments)

    fun onPreLoad(arguments: OnPreLoadEventArguments)
}
