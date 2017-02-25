package org.jenesis.jds.listeners;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.jenesis.jds.JdsEntity;


/**
 * Created by ifunga on 25/02/2017.
 */
public class ObjectListener extends BaseListener implements ChangeListener<JdsEntity> {
    @Override
    public void changed(ObservableValue<? extends JdsEntity> observable, JdsEntity oldValue, JdsEntity newValue) {
        setChanged(true);
    }
}
