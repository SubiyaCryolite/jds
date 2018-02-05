CREATE TABLE jds_store_old_field_value (
  uuid                  NVARCHAR2(64),
  field_id              NUMBER(19),
  date_of_modification  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  sequence              NUMBER(10),
  string_value          NCLOB,
  integer_value         NUMBER(10),
  float_value           BINARY_FLOAT,
  double_value          BINARY_DOUBLE,
  long_value            NUMBER(19),
  date_time_value       TIMESTAMP,
  time_value            NUMBER(19),
  boolean_value         NUMBER(3),
  zoned_date_time_value TIMESTAMP WITH TIME ZONE,
  blob_value            BLOB
)