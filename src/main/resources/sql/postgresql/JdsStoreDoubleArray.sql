CREATE TABLE JdsStoreDoubleArray(
    FieldId     BIGINT,
    Uuid    VARCHAR(48),
    Sequence   INTEGER,
    Value       FLOAT,
    PRIMARY KEY (FieldId,Uuid,Sequence),
    FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);