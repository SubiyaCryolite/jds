CREATE TABLE JdsStoreDoubleArray(
    FieldId     BIGINT,
    ActionId    TEXT,
    Sequence    INTEGER,
    Value       DOUBLE,
    PRIMARY KEY (FieldId,ActionId,Sequence)
);