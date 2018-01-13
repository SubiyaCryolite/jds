BEGIN;
CREATE TABLE jds_store_old_field_values (
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
CREATE INDEX integer_values
  ON jds_store_old_field_values (uuid, field_id, sequence, integer_value);
CREATE INDEX float_values
  ON jds_store_old_field_values (uuid, field_id, sequence, float_value);
CREATE INDEX double_values
  ON jds_store_old_field_values (uuid, field_id, sequence, double_value);
CREATE INDEX long_values
  ON jds_store_old_field_values (uuid, field_id, sequence, long_value);
CREATE INDEX date_time_values
  ON jds_store_old_field_values (uuid, field_id, sequence, date_time_value);
CREATE INDEX time_values
  ON jds_store_old_field_values (uuid, field_id, sequence, time_value);
CREATE INDEX boolean_values
  ON jds_store_old_field_values (uuid, field_id, sequence, boolean_value);
CREATE INDEX zoned_date_time_values
  ON jds_store_old_field_values (uuid, field_id, sequence, zoned_date_time_value);
CREATE INDEX Textblob_values
  ON jds_store_old_field_values (uuid, field_id, sequence);
COMMIT;