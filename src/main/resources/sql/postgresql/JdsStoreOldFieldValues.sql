BEGIN;
    CREATE TABLE JdsStoreOldFieldValues(
        EntityGuid			    VARCHAR(48),
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
    CREATE INDEX IntegerValues          ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, IntegerValue);
    CREATE INDEX FloatValues            ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, FloatValue);
    CREATE INDEX DoubleValues           ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, DoubleValue);
    CREATE INDEX LongValues             ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, LongValue);
    CREATE INDEX DateTimeValues         ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, DateTimeValue);
    CREATE INDEX TimeValues             ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, TimeValue);
    CREATE INDEX BooleanValues          ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, BooleanValue);
    CREATE INDEX ZonedDateTimeValues    ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, ZonedDateTimeValue);
    CREATE INDEX TextBlobValues         ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence);
COMMIT;