package org.jenesis.jds.listeners;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.time.LocalDateTime;


/**
 * Created by ifunga on 25/02/2017.
 */
public class LocalDateTimeListener extends BaseListener implements ChangeListener<LocalDateTime> {
    @Override
    public void changed(ObservableValue<? extends LocalDateTime> observable, LocalDateTime oldValue, LocalDateTime newValue) {
        setChanged(true);
    }
}
