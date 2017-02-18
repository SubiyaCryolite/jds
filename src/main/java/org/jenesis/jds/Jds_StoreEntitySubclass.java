package org.jenesis.jds;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Created by ifunga on 13/02/2017.
 */
class Jds_StoreEntitySubclass {
    private final SimpleStringProperty actionId;
    private final SimpleStringProperty subActionId;
    private final SimpleLongProperty entityId;

    public Jds_StoreEntitySubclass() {
        this.actionId = new SimpleStringProperty("");
        this.subActionId = new SimpleStringProperty("");
        this.entityId = new SimpleLongProperty(0);
    }

    public String getActionId() {
        return actionId.get();
    }

    public void setActionId(String actionId) {
        this.actionId.set(actionId);
    }

    public String getSubActionId() {
        return subActionId.get();
    }

    public void setSubActionId(String subActionId) {
        this.subActionId.set(subActionId);
    }

    public long getEntityId() {
        return entityId.get();
    }

    public void setEntityId(long entityId) {
        this.entityId.set(entityId);
    }
}
