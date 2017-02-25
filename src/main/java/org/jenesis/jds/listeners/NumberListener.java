package org.jenesis.jds.listeners;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;


/**
 * Created by ifunga on 25/02/2017.
 */
public class NumberListener extends BaseListener implements ChangeListener<Number> {
    @Override
    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        setChanged(true);
    }
}
