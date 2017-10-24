CREATE TABLE JdsStoreOldFieldValues(
    Uuid			NVARCHAR2(48),
    FieldId				NUMBER(19),
    DateOfModification	TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    Sequence            NUMBER(10),
    StringValue			NCLOB,
    IntegerValue		NUMBER(10),
    FloatValue			BINARY_FLOAT,
    DoubleValue			BINARY_DOUBLE,
    LongValue			NUMBER(19),
    DateTimeValue		TIMESTAMP,
    TimeValue			NUMBER(19),
    BooleanValue		NUMBER(3),
    ZonedDateTimeValue	TIMESTAMP WITH TIME ZONE,
    BlobValue           BLOB
)