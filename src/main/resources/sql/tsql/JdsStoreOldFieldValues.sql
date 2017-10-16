BEGIN;
    CREATE TABLE JdsStoreOldFieldValues (
      EntityGuid         NVARCHAR(48),
      FieldId            BIGINT,
      DateOfModification DATETIME DEFAULT GETDATE(),
      Sequence           INTEGER,
      TextValue          NVARCHAR(MAX),
      IntegerValue       INTEGER,
      FloatValue         REAL,
      DoubleValue        FLOAT,
      LongValue          BIGINT,
      DateTimeValue      DATETIME,
      BlobValue          VARBINARY(MAX)
    );
    CREATE NONCLUSTERED INDEX IntegerValues  ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, IntegerValue);
    CREATE NONCLUSTERED INDEX FloatValues    ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, FloatValue);
    CREATE NONCLUSTERED INDEX DoubleValues   ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, DoubleValue);
    CREATE NONCLUSTERED INDEX LongValues     ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, LongValue);
    CREATE NONCLUSTERED INDEX DateTimeValues ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, DateTimeValue);
    CREATE NONCLUSTERED INDEX TextBlobValues ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence);
END;