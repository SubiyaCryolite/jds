CREATE TABLE JdsStoreDoubleArray(
    FieldId     BIGINT,
    ActionId    NVARCHAR(48),
    Sequence   INTEGER,
    Value       FLOAT,
    PRIMARY KEY (FieldId,ActionId,Sequence)
);