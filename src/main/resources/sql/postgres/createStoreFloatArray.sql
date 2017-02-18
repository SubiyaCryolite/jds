CREATE TABLE JdsStoreFloatArray(
    FieldId     BIGINT,
    ActionId    VARCHAR(48),
    Sequence   INTEGER,
    Value       REAL,
    PRIMARY KEY (FieldId,ActionId,Sequence)
);