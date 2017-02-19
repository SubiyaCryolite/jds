CREATE TABLE JdsStoreDoubleArray(
    FieldId     BIGINT,
    EntityGuid    VARCHAR(48),
    Sequence   INTEGER,
    Value       FLOAT,
    PRIMARY KEY (FieldId,EntityGuid,Sequence)
);