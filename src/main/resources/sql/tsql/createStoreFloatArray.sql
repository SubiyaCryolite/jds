CREATE TABLE JdsStoreFloatArray(
    FieldId     BIGINT,
    EntityGuid    NVARCHAR(48),
    Sequence   INTEGER,
    Value       REAL,
    PRIMARY KEY (FieldId,EntityGuid,Sequence)
);