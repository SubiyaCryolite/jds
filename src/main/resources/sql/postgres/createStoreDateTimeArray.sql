CREATE TABLE JdsStoreDateTimeArray(
    FieldId     BIGINT,
    EntityGuid    VARCHAR(48),
    Sequence   INTEGER,
    Value       TIMESTAMP,
    PRIMARY KEY(FieldId,EntityGuid,Sequence)
);