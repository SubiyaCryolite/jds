CREATE TABLE jds_store_boolean (
  field_id NUMBER(19),
  uuid     NVARCHAR2(64),
  value    NUMBER(3),
  PRIMARY KEY (field_id, uuid),
  FOREIGN KEY (uuid) REFERENCES jds_entity_overview (uuid) ON DELETE CASCADE
)