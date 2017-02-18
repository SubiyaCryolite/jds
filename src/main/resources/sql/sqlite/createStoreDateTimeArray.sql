CREATE TABLE JdsStoreDateTimeArray(
    FieldId     BIGINT,
    ActionId    TEXT,
    Sequence    INTEGER,
    Value       TIMESTAMP,
    PRIMARY KEY(FieldId,ActionId,Sequence)
);