package org.jenesis.jds.listeners;

import javafx.collections.ListChangeListener;

/**
 * Created by ifung on 25/02/2017.
 */
public class ListNumberListener extends BaseListener implements ListChangeListener<Number> {
    @Override
    public void onChanged(Change<? extends Number> change) {
        setChanged(true);
    }
}
