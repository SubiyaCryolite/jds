CREATE TABLE jds_store_old_field_values (
  uuid                  VARCHAR(96),
  field_id              BIGINT,
  date_of_modification  DATETIME DEFAULT CURRENT_TIMESTAMP,
  sequence              INT,
  string_value          TEXT,
  integer_value         INT,
  float_value           FLOAT,
  double_value          DOUBLE,
  long_value            BIGINT,
  date_time_value       DATETIME,
  time_value            TIME,
  boolean_value         BOOLEAN,
  zoned_date_time_value TIMESTAMP,
  blob_value            BLOB
);