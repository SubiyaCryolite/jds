CREATE TABLE JdsStoreFloatArray(
    FieldId     BIGINT,
    ActionId    TEXT,
    Sequence    INTEGER,
    Value       REAL,
    PRIMARY KEY (FieldId,ActionId,Sequence)
);