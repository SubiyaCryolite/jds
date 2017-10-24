CREATE TABLE JdsStoreOldFieldValues(
        Uuid			VARCHAR(48),
        FieldId				BIGINT,
        DateOfModification	DATETIME DEFAULT CURRENT_TIMESTAMP,
        Sequence            INT,
        TextValue			TEXT,
        IntegerValue		INT,
        FloatValue			FLOAT,
        DoubleValue			DOUBLE,
        LongValue			BIGINT,
        DateTimeValue		DATETIME,
        TimeValue			TIME,
        BooleanValue		BOOLEAN,
        ZonedDateTimeValue	TIMESTAMP,
        BlobValue           BLOB
);