CREATE TABLE JdsStoreLongArray(
    FieldId     BIGINT,
    ActionId    TEXT,
    Sequence   INTEGER,
    Value       INTEGER,
    PRIMARY KEY(FieldId,ActionId,Sequence)
);