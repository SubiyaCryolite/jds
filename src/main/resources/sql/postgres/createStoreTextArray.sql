CREATE TABLE JdsStoreTextArray(
    FieldId     BIGINT,
    EntityGuid    VARCHAR(48),
    Sequence   INTEGER,
    Value       TEXT,
    PRIMARY KEY (FieldId,EntityGuid,Sequence)
);