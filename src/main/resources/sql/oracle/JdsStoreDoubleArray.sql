CREATE TABLE jds_store_double_array (
  field_id NUMBER(19),
  uuid     NVARCHAR2(96),
  sequence NUMBER(10),
  value    BINARY_DOUBLE,
  PRIMARY KEY (field_id, uuid, sequence),
  FOREIGN KEY (uuid) REFERENCES jds_entity_overview (uuid) ON DELETE CASCADE
)