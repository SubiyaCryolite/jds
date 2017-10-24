BEGIN;
    CREATE TABLE JdsStoreOldFieldValues(
        Uuid			    VARCHAR(48),
        FieldId				    BIGINT,
        DateOfModification	    TIMESTAMP DEFAULT now(),
        Sequence                INTEGER,
        TextValue			    TEXT,
        IntegerValue		    INTEGER,
        FloatValue			    REAL,
        DoubleValue			    FLOAT,
        LongValue			    BIGINT,
        DateTimeValue		    TIMESTAMP,
        TimeValue			    TIME WITHOUT TIME ZONE,
        BooleanValue			BOOLEAN,
        ZonedDateTimeValue		TIMESTAMP WITH TIME ZONE,
        BlobValue               BYTEA
    );
    CREATE INDEX IntegerValues          ON JdsStoreOldFieldValues(Uuid, FieldId, Sequence, IntegerValue);
    CREATE INDEX FloatValues            ON JdsStoreOldFieldValues(Uuid, FieldId, Sequence, FloatValue);
    CREATE INDEX DoubleValues           ON JdsStoreOldFieldValues(Uuid, FieldId, Sequence, DoubleValue);
    CREATE INDEX LongValues             ON JdsStoreOldFieldValues(Uuid, FieldId, Sequence, LongValue);
    CREATE INDEX DateTimeValues         ON JdsStoreOldFieldValues(Uuid, FieldId, Sequence, DateTimeValue);
    CREATE INDEX TimeValues             ON JdsStoreOldFieldValues(Uuid, FieldId, Sequence, TimeValue);
    CREATE INDEX BooleanValues          ON JdsStoreOldFieldValues(Uuid, FieldId, Sequence, BooleanValue);
    CREATE INDEX ZonedDateTimeValues    ON JdsStoreOldFieldValues(Uuid, FieldId, Sequence, ZonedDateTimeValue);
    CREATE INDEX TextBlobValues         ON JdsStoreOldFieldValues(Uuid, FieldId, Sequence);
COMMIT;