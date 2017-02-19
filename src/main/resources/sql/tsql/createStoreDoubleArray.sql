CREATE TABLE JdsStoreDoubleArray(
    FieldId     BIGINT,
    EntityGuid    NVARCHAR(48),
    Sequence   INTEGER,
    Value       FLOAT,
    PRIMARY KEY (FieldId,EntityGuid,Sequence)
);