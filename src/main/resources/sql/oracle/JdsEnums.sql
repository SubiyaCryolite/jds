CREATE TABLE JdsEnums(
    FieldId     NUMBER(19),
    EnumSeq     NUMBER(10),
    EnumValue   NCLOB,
    PRIMARY KEY (FieldId,EnumSeq)
)