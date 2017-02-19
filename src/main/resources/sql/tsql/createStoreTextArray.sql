CREATE TABLE JdsStoreTextArray(
    FieldId     BIGINT,
    EntityGuid    NVARCHAR(48),
    Sequence   INTEGER,
    Value       NVARCHAR(MAX),
    PRIMARY KEY (FieldId,EntityGuid,Sequence)
);