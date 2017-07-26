package io.github.subiyacryolite.jds.events;

import java.sql.Connection;

/**
 * Created by ifunga on 13/05/2017.
 */
public class OnPostSaveEventArguments {
    private final int batchSequence;
    private final int batchSize;

    public OnPostSaveEventArguments(int batchSequence, int batchSize) {
        this.batchSequence = batchSequence;
        this.batchSize = batchSize;
    }

    public int getBatchSequence() {
        return batchSequence;
    }

    public int getBatchSize() {
        return batchSize;
    }

}
