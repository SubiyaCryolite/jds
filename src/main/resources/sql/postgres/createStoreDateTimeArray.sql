CREATE TABLE JdsStoreDateTimeArray(
    FieldId     BIGINT,
    ActionId    VARCHAR(48),
    Sequence   INTEGER,
    Value       TIMESTAMP,
    PRIMARY KEY(FieldId,ActionId,Sequence)
);