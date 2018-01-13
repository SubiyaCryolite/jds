CREATE TABLE jds_store_text (
  field_id NUMBER(19),
  uuid     NVARCHAR2(96),
  value    NCLOB,
  PRIMARY KEY (field_id, uuid),
  FOREIGN KEY (uuid) REFERENCES jds_entity_overview (uuid) ON DELETE CASCADE
)