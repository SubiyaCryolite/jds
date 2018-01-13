CREATE TABLE jds_store_date_time_array (
  field_id NUMBER(19),
  uuid     NVARCHAR2(96),
  sequence NUMBER(10),
  value    TIMESTAMP,
  PRIMARY KEY (field_id, uuid, sequence),
  FOREIGN KEY (uuid) REFERENCES jds_entity_overview (uuid) ON DELETE CASCADE
)