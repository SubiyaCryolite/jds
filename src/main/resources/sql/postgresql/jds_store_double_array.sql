CREATE TABLE jds_store_double_array (
  composite_key VARCHAR(128),
  field_id      BIGINT,
  sequence      INTEGER,
  value         FLOAT,
  PRIMARY KEY (field_id, composite_key, sequence),
  FOREIGN KEY (composite_key) REFERENCES jds_entity_overview (composite_key) ON DELETE CASCADE
);