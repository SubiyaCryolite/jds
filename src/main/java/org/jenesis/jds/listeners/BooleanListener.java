package org.jenesis.jds.listeners;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * Created by ifunga on 18/03/2017.
 */
public class BooleanListener extends BaseListener implements ChangeListener<Boolean> {
    @Override
    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        setChanged(true);
    }
}
