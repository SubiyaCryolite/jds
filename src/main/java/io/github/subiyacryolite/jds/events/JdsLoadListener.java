package io.github.subiyacryolite.jds.events;

/**
 * Created by indana on 5/31/2017.
 */
public interface JdsLoadListener {
    void onPostLoad(final OnPostLoadEvent eventArguments);
    void onPreLoad(final OnPreLoadEventArguments eventArguments);
}
