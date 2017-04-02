CREATE TABLE JdsStoreOldFieldValues(
    EntityGuid			NVARCHAR(48),
    FieldId				BIGINT,
    DateOfModification	DATETIME DEFAULT GETDATE(),
    Sequence            INTEGER,
    TextValue			NVARCHAR(MAX),
    IntegerValue		INTEGER,
    FloatValue			REAL,
    DoubleValue			FLOAT,
    LongValue			INTEGER,
    DateTimeValue		DATETIME
);