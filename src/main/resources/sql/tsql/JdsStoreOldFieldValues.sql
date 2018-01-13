BEGIN;
  CREATE TABLE jds_store_old_field_values (
    uuid                  NVARCHAR(96),
    field_id              BIGINT,
    date_of_modification  DATETIME DEFAULT GETDATE(),
    sequence              INTEGER,
    string_value          NVARCHAR(MAX),
    integer_value         INTEGER,
    float_value           REAL,
    double_value          FLOAT,
    long_value            BIGINT,
    date_time_value       DATETIME,
    time_value            TIME(7),
    zoned_date_time_value DATETIMEOFFSET(7),
    boolean_value         BIT,
    blob_value            VARBINARY(MAX)
  );
  CREATE NONCLUSTERED INDEX integer_values
    ON jds_store_old_field_values (uuid, field_id, SEQUENCE, integer_value);
  CREATE NONCLUSTERED INDEX float_values
    ON jds_store_old_field_values (uuid, field_id, SEQUENCE, float_value);
  CREATE NONCLUSTERED INDEX double_values
    ON jds_store_old_field_values (uuid, field_id, SEQUENCE, double_value);
  CREATE NONCLUSTERED INDEX long_values
    ON jds_store_old_field_values (uuid, field_id, SEQUENCE, long_value);
  CREATE NONCLUSTERED INDEX date_time_values
    ON jds_store_old_field_values (uuid, field_id, SEQUENCE, date_time_value);
  CREATE NONCLUSTERED INDEX zoned_date_time_values
    ON jds_store_old_field_values (uuid, field_id, SEQUENCE, zoned_date_time_value);
  CREATE NONCLUSTERED INDEX boolean_values
    ON jds_store_old_field_values (uuid, field_id, SEQUENCE, boolean_value);
  CREATE NONCLUSTERED INDEX time_values
    ON jds_store_old_field_values (uuid, field_id, SEQUENCE, time_value);
  CREATE NONCLUSTERED INDEX Textblob_values
    ON jds_store_old_field_values (uuid, field_id, SEQUENCE);
END;