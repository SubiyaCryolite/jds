CREATE TABLE jds_store_double (
  field_id NUMBER(19),
  uuid     NVARCHAR2(64),
  value    BINARY_DOUBLE,
  PRIMARY KEY (field_id, uuid),
  FOREIGN KEY (uuid) REFERENCES jds_entity_overview (uuid) ON DELETE CASCADE
)