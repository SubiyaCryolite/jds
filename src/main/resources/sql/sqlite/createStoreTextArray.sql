CREATE TABLE JdsStoreTextArray(
    FieldId     BIGINT,
    EntityGuid    TEXT,
    Sequence   INTEGER,
    Value       TEXT,
    PRIMARY KEY (FieldId,EntityGuid,Sequence)
);