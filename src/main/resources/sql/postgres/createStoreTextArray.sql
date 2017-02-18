CREATE TABLE JdsStoreTextArray(
    FieldId     BIGINT,
    ActionId    VARCHAR(48),
    Sequence   INTEGER,
    Value       TEXT,
    PRIMARY KEY (FieldId,ActionId,Sequence)
);