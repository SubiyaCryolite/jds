package io.github.subiyacryolite.jds.events;

import java.sql.Connection;

/**
 * Created by ifunga on 13/05/2017.
 */
public class OnPreSaveEventArguments {
    private final int outerBatchStep;
    private final int batchSequence;
    private final int batchSize;
    private final Connection connection;

    public OnPreSaveEventArguments(Connection connection, int outerBatchStep, int batchSequence, int batchSize) {
        this.outerBatchStep = outerBatchStep;
        this.batchSequence = batchSequence;
        this.batchSize = batchSize;
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public int getOuterBatchStep() {
        return outerBatchStep;
    }

    public int getBatchSequence() {
        return batchSequence;
    }

    public int getBatchSize() {
        return batchSize;
    }
}
