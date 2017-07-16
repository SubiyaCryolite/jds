package io.github.subiyacryolite.jds.events;

import io.github.subiyacryolite.jds.JdsDb;

/**
 * Created by ifunga on 16/07/2017.
 */
public class OnDeleteEventArguments {
    private final String entityGuid;
    private final JdsDb jdsDb;

    public OnDeleteEventArguments(JdsDb jdsDb, String entityGuid) {
        this.jdsDb = jdsDb;
        this.entityGuid = entityGuid;
    }

    public String getEntityGuid() {
        return entityGuid;
    }

    public JdsDb getJdsDb() {
        return jdsDb;
    }
}
