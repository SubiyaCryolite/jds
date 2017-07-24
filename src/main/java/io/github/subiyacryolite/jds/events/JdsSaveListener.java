package io.github.subiyacryolite.jds.events;

/**
 * Created by indana on 5/31/2017.
 */
public interface JdsSaveListener {
    void onPreSave(final OnPreSaveEventArguments arguments);
    void onPostSave(final OnPostSaveEventArguments arguments);
}
