CREATE TABLE JdsStoreIntegerArray(
    FieldId     BIGINT,
    EntityGuid    NVARCHAR(48),
    Sequence    INTEGER,
    Value       INTEGER,
    PRIMARY KEY (FieldId,EntityGuid,Sequence)
);