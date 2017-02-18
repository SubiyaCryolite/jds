CREATE TABLE JdsStoreLongArray(
    FieldId     BIGINT,
    ActionId    VARCHAR(48),
    Sequence   INTEGER,
    Value       INTEGER,
    PRIMARY KEY(FieldId,ActionId,Sequence)
);