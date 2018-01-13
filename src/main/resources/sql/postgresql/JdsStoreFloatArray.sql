CREATE TABLE jds_store_float_array (
  field_id BIGINT,
  uuid     VARCHAR(96),
  sequence INTEGER,
  value    REAL,
  PRIMARY KEY (field_id, uuid, sequence),
  FOREIGN KEY (uuid) REFERENCES jds_entity_overview (uuid) ON DELETE CASCADE
);