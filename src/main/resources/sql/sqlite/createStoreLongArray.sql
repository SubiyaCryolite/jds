CREATE TABLE JdsStoreLongArray(
    FieldId     BIGINT,
    EntityGuid    TEXT,
    Sequence   INTEGER,
    Value       INTEGER,
    PRIMARY KEY(FieldId,EntityGuid,Sequence)
);