CREATE TABLE JdsStoreFloatArray(
    FieldId     BIGINT,
    ActionId    NVARCHAR(48),
    Sequence   INTEGER,
    Value       REAL,
    PRIMARY KEY (FieldId,ActionId,Sequence)
);