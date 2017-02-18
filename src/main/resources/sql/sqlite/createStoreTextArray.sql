CREATE TABLE JdsStoreTextArray(
    FieldId     BIGINT,
    ActionId    TEXT,
    Sequence   INTEGER,
    Value       TEXT,
    PRIMARY KEY (FieldId,ActionId,Sequence)
);