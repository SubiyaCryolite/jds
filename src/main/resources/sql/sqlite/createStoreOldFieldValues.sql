CREATE TABLE JdsStoreOldFieldValues(
    EntityGuid			TEXT,
    FieldId				BIGINT,
    DateOfModification	TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Sequence            INTEGER,
    TextValue			TEXT,
    IntegerValue		INTEGER,
    FloatValue			REAL,
    DoubleValue			DOUBLE,
    LongValue			INTEGER,
    DateTimeValue		TIMESTAMP
);