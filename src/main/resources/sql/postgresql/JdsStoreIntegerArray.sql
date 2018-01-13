CREATE TABLE jds_store_integer_array (
  field_id BIGINT,
  uuid     VARCHAR(96),
  sequence INTEGER,
  value    INTEGER,
  PRIMARY KEY (field_id, uuid, sequence),
  FOREIGN KEY (uuid) REFERENCES jds_entity_overview (uuid) ON DELETE CASCADE
);