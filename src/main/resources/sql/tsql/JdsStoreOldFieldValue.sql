BEGIN;
  CREATE TABLE jds_store_old_field_value (
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
  CREATE NONCLUSTERED INDEX indx_jds_old_integer
    ON jds_store_old_field_value (uuid, field_id, SEQUENCE, integer_value);
  CREATE NONCLUSTERED INDEX indx_jds_old_float
    ON jds_store_old_field_value (uuid, field_id, SEQUENCE, float_value);
  CREATE NONCLUSTERED INDEX indx_jds_old_double
    ON jds_store_old_field_value (uuid, field_id, SEQUENCE, double_value);
  CREATE NONCLUSTERED INDEX indx_jds_old_long
    ON jds_store_old_field_value (uuid, field_id, SEQUENCE, long_value);
  CREATE NONCLUSTERED INDEX indx_jds_old_date_time_value
    ON jds_store_old_field_value (uuid, field_id, SEQUENCE, date_time_value);
  CREATE NONCLUSTERED INDEX indx_jds_old_zoned_date_time
    ON jds_store_old_field_value (uuid, field_id, SEQUENCE, zoned_date_time_value);
  CREATE NONCLUSTERED INDEX indx_jds_old_boolean
    ON jds_store_old_field_value (uuid, field_id, SEQUENCE, boolean_value);
  CREATE NONCLUSTERED INDEX indx_jds_old_time
    ON jds_store_old_field_value (uuid, field_id, SEQUENCE, time_value);
  CREATE NONCLUSTERED INDEX indx_jds_old_blob_text
    ON jds_store_old_field_value (uuid, field_id, SEQUENCE);
END;