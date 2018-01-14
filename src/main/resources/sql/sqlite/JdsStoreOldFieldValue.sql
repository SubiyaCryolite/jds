CREATE TABLE jds_store_old_field_value (
  uuid                  TEXT,
  field_id              BIGINT,
  date_of_modification  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  sequence              INTEGER,
  string_value          TEXT,
  integer_value         INTEGER,
  float_value           REAL,
  double_value          DOUBLE,
  long_value            BIGINT,
  date_time_value       TIMESTAMP,
  time_value            INTEGER,
  boolean_value         BOOLEAN,
  zoned_date_time_value BIGINT,
  blob_value            BLOB
);