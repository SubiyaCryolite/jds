CREATE TABLE JdsRefEnumValues(
    FieldId     BIGINT,
    EnumSeq     INTEGER,
    EnumValue   NVARCHAR(MAX),
    PRIMARY KEY (FieldId,EnumSeq)
);