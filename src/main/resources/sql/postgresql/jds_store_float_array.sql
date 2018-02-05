CREATE TABLE jds_store_float_array (
  composite_key VARCHAR(195),
  field_id      BIGINT,
  sequence      INTEGER,
  value         REAL,
  PRIMARY KEY (field_id, composite_key, sequence),
  FOREIGN KEY (composite_key) REFERENCES jds_entity_overview (composite_key) ON DELETE CASCADE
);