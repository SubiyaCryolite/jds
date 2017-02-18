CREATE TABLE JdsStoreIntegerArray(
    FieldId     BIGINT,
    ActionId    NVARCHAR(48),
    Sequence    INTEGER,
    Value       INTEGER,
    PRIMARY KEY (FieldId,ActionId,Sequence)
);