CREATE TABLE JdsStoreDateTimeArray(
    FieldId     BIGINT,
    ActionId    NVARCHAR(48),
    Sequence   INTEGER,
    Value       DATETIME,
    PRIMARY KEY(FieldId,ActionId,Sequence)
);