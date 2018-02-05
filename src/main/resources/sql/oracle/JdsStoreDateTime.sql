CREATE TABLE jds_store_date_time (
  field_id NUMBER(19),
  uuid     NVARCHAR2(64),
  value    TIMESTAMP,
  PRIMARY KEY (field_id, uuid),
  FOREIGN KEY (uuid) REFERENCES jds_entity_overview (uuid) ON DELETE CASCADE
)