CREATE TABLE jds_store_long_array (
  field_id NUMBER(19),
  uuid     NVARCHAR2(96),
  sequence NUMBER(10),
  value    NUMBER(19),
  PRIMARY KEY (field_id, uuid, sequence),
  FOREIGN KEY (uuid) REFERENCES jds_entity_overview (uuid) ON DELETE CASCADE
)