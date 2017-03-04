CREATE TABLE JdsStoreOldFieldValues(
    EntityGuid			VARCHAR(48),
    FieldId				BIGINT,
    DateOfModification	TIMESTAMP DEFAULT now(),
    Sequence            INTEGER,
    TextValue			TEXT,
    IntegerValue		INTEGER,
    FloatValue			REAL,
    DoubleValue			FLOAT,
    LongValue			INTEGER,
    DateTimeValue		TIMESTAMP,
    FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);