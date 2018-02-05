CREATE TABLE jds_store_float_array (
  field_id NUMBER(19),
  uuid     NVARCHAR2(64),
  sequence NUMBER(10),
  value    BINARY_FLOAT,
  PRIMARY KEY (field_id, uuid, sequence),
  FOREIGN KEY (uuid) REFERENCES jds_entity_overview (uuid) ON DELETE CASCADE
)