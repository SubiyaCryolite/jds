CREATE TABLE JdsStoreDoubleArray(
    FieldId     BIGINT,
    Uuid    VARCHAR(96),
    Sequence   INTEGER,
    Value       FLOAT,
    PRIMARY KEY (FieldId,Uuid,Sequence),
    FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);