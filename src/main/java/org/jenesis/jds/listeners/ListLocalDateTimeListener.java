package org.jenesis.jds.listeners;

import javafx.collections.ListChangeListener;

import java.time.LocalDateTime;

/**
 * Created by ifung on 25/02/2017.
 */
public class ListLocalDateTimeListener extends BaseListener implements ListChangeListener<LocalDateTime> {
    @Override
    public void onChanged(Change<? extends LocalDateTime> change) {
        setChanged(true);
    }
}
