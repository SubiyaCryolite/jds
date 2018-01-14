BEGIN;
CREATE TABLE jds_store_old_field_value (
  uuid                  VARCHAR(96),
  field_id              BIGINT,
  date_of_modification  TIMESTAMP DEFAULT now(),
  sequence              INTEGER,
  string_value          TEXT,
  integer_value         INTEGER,
  float_value           REAL,
  double_value          FLOAT,
  long_value            BIGINT,
  date_time_value       TIMESTAMP,
  time_value            TIME WITHOUT TIME ZONE,
  boolean_value         BOOLEAN,
  zoned_date_time_value TIMESTAMP WITH TIME ZONE,
  blob_value            BYTEA
);
CREATE INDEX indx_jds_old_integer
  ON jds_store_old_field_value (uuid, field_id, sequence, integer_value);
CREATE INDEX indx_jds_old_float
  ON jds_store_old_field_value (uuid, field_id, sequence, float_value);
CREATE INDEX indx_jds_old_double
  ON jds_store_old_field_value (uuid, field_id, sequence, double_value);
CREATE INDEX indx_jds_old_long
  ON jds_store_old_field_value (uuid, field_id, sequence, long_value);
CREATE INDEX indx_jds_old_date_time_value
  ON jds_store_old_field_value (uuid, field_id, sequence, date_time_value);
CREATE INDEX indx_jds_old_time
  ON jds_store_old_field_value (uuid, field_id, sequence, time_value);
CREATE INDEX indx_jds_old_boolean
  ON jds_store_old_field_value (uuid, field_id, sequence, boolean_value);
CREATE INDEX indx_jds_old_zoned_date_time
  ON jds_store_old_field_value (uuid, field_id, sequence, zoned_date_time_value);
CREATE INDEX indx_jds_old_blob_text
  ON jds_store_old_field_value (uuid, field_id, sequence);
COMMIT;