CREATE TABLE JdsStoreDoubleArray(
    FieldId     BIGINT,
    ActionId    VARCHAR(48),
    Sequence   INTEGER,
    Value       FLOAT,
    PRIMARY KEY (FieldId,ActionId,Sequence)
);