package io.github.subiyacryolite.jds.events;

/**
 * Created by ifunga on 13/05/2017.
 */
public class OnPreLoadEvenArguments {
    private final String entityGuid;
    private final int batchSequence;
    private final int batchSize;

    public OnPreLoadEvenArguments(String entityGuid, int batchSequence, int batchSize) {
        this.entityGuid = entityGuid;
        this.batchSequence = batchSequence;
        this.batchSize = batchSize;
    }

    public String getEntityGuid() {
        return entityGuid;
    }

    public int getBatchSequence() {
        return batchSequence;
    }

    public int getBatchSize() {
        return batchSize;
    }
}
