CREATE TABLE JdsStoreOldFieldValues(
    Uuid			NVARCHAR2(48),
    FieldId				NUMBER(19),
    DateOfModification	DATE DEFAULT SYSDATE NOT NULL,
    Sequence            NUMBER(10),
    TextValue			NCLOB,
    IntegerValue		NUMBER(10),
    FloatValue			BINARY_FLOAT,
    DoubleValue			BINARY_DOUBLE,
    LongValue			NUMBER(19),
    DateTimeValue		DATE,
    TimeValue			NUMBER(10),
    BooleanValue		NUMBER(3),
    ZonedDateTimeValue	TIMESTAMP WITH TIME ZONE,
    BlobValue           BLOB
)