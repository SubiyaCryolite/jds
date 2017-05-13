package io.github.subiyacryolite.jds.events;

/**
 * Created by ifunga on 13/05/2017.
 */
public class OnPostLoadEvent {
    private final String entityGuid;

    public OnPostLoadEvent(String entityGuid) {
        this.entityGuid = entityGuid;
    }

    public String getEntityGuid() {
        return entityGuid;
    }
}
