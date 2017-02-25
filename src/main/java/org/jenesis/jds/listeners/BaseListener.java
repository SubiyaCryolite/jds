package org.jenesis.jds.listeners;

/**
 * Created by ifung on 25/02/2017.
 */
public abstract class BaseListener {
    private boolean changed;

    public final void setChanged(boolean value) {
        changed = value;
    }

    public final boolean getChanged() {
        return changed;
    }
}
