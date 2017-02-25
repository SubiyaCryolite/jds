package org.jenesis.jds.listeners;

import javafx.collections.ListChangeListener;
import org.jenesis.jds.JdsEntity;

/**
 * Created by ifung on 25/02/2017.
 */
public class ListObjectListener extends BaseListener implements ListChangeListener<JdsEntity> {
    @Override
    public void onChanged(Change<? extends JdsEntity> change) {
        setChanged(true);
    }
}
