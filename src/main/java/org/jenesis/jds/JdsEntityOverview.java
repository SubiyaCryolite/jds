package org.jenesis.jds;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Created by ifung on 14/02/2017.
 */
public class JdsEntityOverview {
    private final SimpleObjectProperty<LocalDateTime> dateCreated;
    private final SimpleObjectProperty<LocalDateTime> dateModified;
    private final SimpleLongProperty serviceCode;
    private final SimpleStringProperty actionId;

    JdsEntityOverview() {
        this.actionId = new SimpleStringProperty(UUID.randomUUID().toString());
        this.dateCreated = new SimpleObjectProperty<>(LocalDateTime.now());
        this.dateModified = new SimpleObjectProperty<>(LocalDateTime.now());
        this.serviceCode = new SimpleLongProperty();
    }

    public LocalDateTime getDateCreated() {
        return dateCreated.get();
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated.set(dateCreated);
    }

    public LocalDateTime getDateModified() {
        return dateModified.get();
    }

    public void setDateModified(LocalDateTime dateModified) {
        this.dateModified.set(dateModified);
    }

    public long getEntityCode() {
        return serviceCode.get();
    }

    public void setEntityCode(long serviceCode) {
        this.serviceCode.set(serviceCode);
    }

    public String getActionId() {
        return actionId.get();
    }

    public void setActionId(String actionId) {
        this.actionId.set(actionId);
    }
}
