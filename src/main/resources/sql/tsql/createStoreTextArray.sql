CREATE TABLE JdsStoreTextArray(
    FieldId     BIGINT,
    ActionId    NVARCHAR(48),
    Sequence   INTEGER,
    Value       NVARCHAR(MAX),
    PRIMARY KEY (FieldId,ActionId,Sequence)
);