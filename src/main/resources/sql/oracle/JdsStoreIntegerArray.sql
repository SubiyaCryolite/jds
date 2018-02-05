CREATE TABLE jds_store_integer_array (
  field_id NUMBER(19),
  uuid     NVARCHAR2(64),
  sequence NUMBER(10),
  value    NUMBER(10),
  PRIMARY KEY (field_id, uuid, sequence),
  FOREIGN KEY (uuid) REFERENCES jds_entity_overview (uuid) ON DELETE CASCADE
)