CREATE TABLE JdsStoreDateTimeArray(
    FieldId     BIGINT,
    EntityGuid    NVARCHAR(48),
    Sequence   INTEGER,
    Value       DATETIME,
    PRIMARY KEY(FieldId,EntityGuid,Sequence)
);