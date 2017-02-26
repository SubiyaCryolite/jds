CREATE TABLE JdsStoreFloatArray(
    FieldId     BIGINT,
    EntityGuid    VARCHAR(48),
    Sequence   INTEGER,
    Value       REAL,
    PRIMARY KEY (FieldId,EntityGuid,Sequence)
);