package org.jenesis.jds;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Created by ifunga on 13/02/2017.
 */
class Jds_StoreEntitySubclass {
    private final SimpleStringProperty entityGuid;
    private final SimpleStringProperty subEntityGuid;
    private final SimpleLongProperty entityId;

    public Jds_StoreEntitySubclass() {
        this.entityGuid = new SimpleStringProperty("");
        this.subEntityGuid = new SimpleStringProperty("");
        this.entityId = new SimpleLongProperty(0);
    }

    public String getEntityGuid() {
        return entityGuid.get();
    }

    public void setEntityGuid(String EntityGuid) {
        this.entityGuid.set(EntityGuid);
    }

    public String getSubEntityGuid() {
        return subEntityGuid.get();
    }

    public void setSubEntityGuid(String subEntityGuid) {
        this.subEntityGuid.set(subEntityGuid);
    }

    public long getEntityId() {
        return entityId.get();
    }

    public void setEntityId(long entityId) {
        this.entityId.set(entityId);
    }
}
