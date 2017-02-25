package org.jenesis.jds.listeners;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;


/**
 * Created by ifunga on 25/02/2017.
 */
public class StringListener extends BaseListener implements ChangeListener<String> {
    @Override
    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        setChanged(true);
    }
}
