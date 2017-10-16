CREATE TABLE JdsStoreOldFieldValues(
        EntityGuid			VARCHAR(48),
        FieldId				BIGINT,
        DateOfModification	DATETIME DEFAULT CURRENT_TIMESTAMP,
        Sequence            INT,
        TextValue			TEXT,
        IntegerValue		INT,
        FloatValue			FLOAT,
        DoubleValue			DOUBLE,
        LongValue			BIGINT,
        DateTimeValue		DATETIME,
        BlobValue           BLOB
);