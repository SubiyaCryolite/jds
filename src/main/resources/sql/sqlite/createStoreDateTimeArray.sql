CREATE TABLE JdsStoreDateTimeArray(
    FieldId     BIGINT,
    EntityGuid    TEXT,
    Sequence    INTEGER,
    Value       TIMESTAMP,
    PRIMARY KEY(FieldId,EntityGuid,Sequence)
);