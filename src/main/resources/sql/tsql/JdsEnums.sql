CREATE TABLE JdsEnums(
    FieldId     BIGINT,
    EnumSeq     INTEGER,
    EnumValue   NVARCHAR(MAX),
    PRIMARY KEY (FieldId,EnumSeq)
);