CREATE TABLE JdsStoreIntegerArray(
    FieldId     BIGINT,
    Uuid  VARCHAR(96),
    Sequence    INTEGER,
    Value       INTEGER,
    PRIMARY KEY (FieldId,Uuid,Sequence),
    FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);