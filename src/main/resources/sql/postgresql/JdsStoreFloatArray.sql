CREATE TABLE JdsStoreFloatArray(
    FieldId     BIGINT,
    Uuid    VARCHAR(96),
    Sequence   INTEGER,
    Value       REAL,
    PRIMARY KEY (FieldId,Uuid,Sequence),
    FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);