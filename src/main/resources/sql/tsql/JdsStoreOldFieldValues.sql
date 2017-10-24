BEGIN;
    CREATE TABLE JdsStoreOldFieldValues (
      Uuid         NVARCHAR(48),
      FieldId            BIGINT,
      DateOfModification DATETIME DEFAULT GETDATE(),
      Sequence           INTEGER,
      TextValue          NVARCHAR(MAX),
      IntegerValue       INTEGER,
      FloatValue         REAL,
      DoubleValue        FLOAT,
      LongValue          BIGINT,
      DateTimeValue      DATETIME,
      TimeValue          TIME(7),
      ZonedDateTimeValue DATETIMEOFFSET(7),
      BooleanValue       BIT,
      BlobValue          VARBINARY(MAX)
    );
    CREATE NONCLUSTERED INDEX IntegerValues         ON JdsStoreOldFieldValues(Uuid, FieldId, Sequence, IntegerValue);
    CREATE NONCLUSTERED INDEX FloatValues           ON JdsStoreOldFieldValues(Uuid, FieldId, Sequence, FloatValue);
    CREATE NONCLUSTERED INDEX DoubleValues          ON JdsStoreOldFieldValues(Uuid, FieldId, Sequence, DoubleValue);
    CREATE NONCLUSTERED INDEX LongValues            ON JdsStoreOldFieldValues(Uuid, FieldId, Sequence, LongValue);
    CREATE NONCLUSTERED INDEX DateTimeValues        ON JdsStoreOldFieldValues(Uuid, FieldId, Sequence, DateTimeValue);
    CREATE NONCLUSTERED INDEX ZonedDateTimeValues   ON JdsStoreOldFieldValues(Uuid, FieldId, Sequence, ZonedDateTimeValue);
    CREATE NONCLUSTERED INDEX BooleanValues         ON JdsStoreOldFieldValues(Uuid, FieldId, Sequence, BooleanValue);
    CREATE NONCLUSTERED INDEX TimeValues            ON JdsStoreOldFieldValues(Uuid, FieldId, Sequence, TimeValue);
    CREATE NONCLUSTERED INDEX TextBlobValues        ON JdsStoreOldFieldValues(Uuid, FieldId, Sequence);
END;