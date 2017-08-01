    CREATE TABLE JdsStoreOldFieldValues(
    EntityGuid			NVARCHAR2(48),
    FieldId				NUMBER(19),
    DateOfModification	DATE DEFAULT SYSDATE NOT NULL,
    Sequence            NUMBER(10),
    TextValue			NCLOB,
    IntegerValue		NUMBER(10),
    FloatValue			BINARY_FLOAT,
    DoubleValue			BINARY_DOUBLE,
    LongValue			NUMBER(19),
    DateTimeValue		DATE,
    BlobValue           BLOB
    );
    CREATE INDEX IntegerValues  ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, IntegerValue);
    CREATE INDEX FloatValues    ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, FloatValue);
    CREATE INDEX DoubleValues   ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, DoubleValue);
    CREATE INDEX LongValues     ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, LongValue);
    CREATE INDEX DateTimeValues ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, DateTimeValue);
    CREATE INDEX TextBlobValues ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence);