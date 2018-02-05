CREATE TABLE jds_store_text_array (
  field_id NUMBER(19),
  uuid     NVARCHAR2(64),
  sequence NUMBER(10),
  value    NCLOB,
  PRIMARY KEY (field_id, uuid, sequence),
  FOREIGN KEY (uuid) REFERENCES jds_entity_overview (uuid) ON DELETE CASCADE
)