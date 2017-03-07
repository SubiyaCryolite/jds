package org.jenesis.jds.listeners;

import javafx.collections.ListChangeListener;

/**
 * Created by ifunga on 25/02/2017.
 */
public class ListStringListener extends BaseListener implements ListChangeListener<String> {
    @Override
    public void onChanged(Change<? extends String> change) {
        setChanged(true);
    }
}
