CREATE TABLE JdsStoreLongArray(
    FieldId     BIGINT,
    EntityGuid    VARCHAR(48),
    Sequence   INTEGER,
    Value       INTEGER,
    PRIMARY KEY(FieldId,EntityGuid,Sequence)
);